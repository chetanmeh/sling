/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.distribution.serialization.impl.vlt;

import javax.annotation.Nonnull;
import javax.jcr.Session;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.vault.fs.api.ImportMode;
import org.apache.jackrabbit.vault.fs.api.WorkspaceFilter;
import org.apache.jackrabbit.vault.fs.io.AccessControlHandling;
import org.apache.jackrabbit.vault.fs.io.ImportOptions;
import org.apache.jackrabbit.vault.packaging.ExportOptions;
import org.apache.jackrabbit.vault.packaging.PackageException;
import org.apache.jackrabbit.vault.packaging.Packaging;
import org.apache.jackrabbit.vault.packaging.VaultPackage;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.distribution.DistributionRequest;
import org.apache.sling.distribution.packaging.DistributionPackage;
import org.apache.sling.distribution.serialization.DistributionPackageBuilder;
import org.apache.sling.distribution.serialization.DistributionPackageBuildingException;
import org.apache.sling.distribution.serialization.DistributionPackageReadingException;
import org.apache.sling.distribution.serialization.impl.AbstractDistributionPackageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a {@link org.apache.sling.distribution.serialization.DistributionPackageBuilder} based on Apache Jackrabbit FileVault.
 * <p/>
 * Each {@link org.apache.sling.distribution.packaging.DistributionPackage} created by {@link FileVaultDistributionPackageBuilder} is
 * backed by a {@link org.apache.jackrabbit.vault.packaging.VaultPackage}. 
 */
public class FileVaultDistributionPackageBuilder extends AbstractDistributionPackageBuilder implements
        DistributionPackageBuilder {

    private static final String VERSION = "0.0.1";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Packaging packaging;

    private ImportMode importMode;

    private AccessControlHandling aclHandling;

    private final String[] packageRoots;
    private final File tempDirectory;

    public FileVaultDistributionPackageBuilder(String type, Packaging packaging, ImportMode importMode, AccessControlHandling aclHandling, String[] packageRoots, String tempFilesFolder) {
        super(type);
        this.packaging = packaging;
        this.importMode = importMode;
        this.aclHandling = aclHandling;
        this.packageRoots = packageRoots;



        tempDirectory = VltUtils.getTempFolder(tempFilesFolder);

        log.info("using temp directory {}", tempDirectory == null ? tempDirectory : tempDirectory.getPath());
    }

    @Override
    protected DistributionPackage createPackageForAdd(@Nonnull ResourceResolver resourceResolver, @Nonnull DistributionRequest request)
            throws DistributionPackageBuildingException {
        Session session = null;
        VaultPackage vaultPackage = null;
        try {
            session = getSession(resourceResolver);

            String packageGroup = "sling/distribution";
            String packageName = getType() + "_" + System.currentTimeMillis() + "_" +  UUID.randomUUID();

            WorkspaceFilter filter = VltUtils.createFilter(request);
            ExportOptions opts = VltUtils.getExportOptions(filter, packageRoots, packageGroup, packageName, VERSION);

            log.debug("assembling package {}", packageGroup + '/' + packageName + "-" + VERSION);

            vaultPackage = VltUtils.createPackage(packaging.getPackageManager(), session, opts, tempDirectory);
            return new FileVaultDistributionPackage(getType(), vaultPackage);
        } catch (Exception e) {
            VltUtils.deletePackage(vaultPackage);
            throw new DistributionPackageBuildingException(e);
        } finally {
            ungetSession(session);
        }
    }

    @Override
    protected DistributionPackage readPackageInternal(@Nonnull ResourceResolver resourceResolver, @Nonnull final InputStream stream)
            throws DistributionPackageReadingException {
        log.debug("reading a stream");
        VaultPackage vaultPackage = null;
        try {
            vaultPackage = VltUtils.readPackage(packaging.getPackageManager(), stream, tempDirectory);

            return new FileVaultDistributionPackage(getType(), vaultPackage);

        } catch (Exception e) {
            VltUtils.deletePackage(vaultPackage);
            throw new DistributionPackageReadingException("could not read package", e);
        }
    }


    @Override
    protected DistributionPackage getPackageInternal(@Nonnull ResourceResolver resourceResolver, @Nonnull String id) {
        DistributionPackage distributionPackage = null;
        try {
            File file = new File(id);
            if (file.exists()) {
                VaultPackage pkg = packaging.getPackageManager().open(file);
                distributionPackage = new FileVaultDistributionPackage(getType(), pkg);
            }
        } catch (IOException e) {
            log.warn("could not find a package with id {}", id, e);
        }
        return distributionPackage;
    }


    @Override
    public boolean installPackageInternal(@Nonnull ResourceResolver resourceResolver, @Nonnull DistributionPackage distributionPackage) throws DistributionPackageReadingException {
        log.debug("reading a distribution package stream");

        Session session = null;
        try {
            session = getSession(resourceResolver);
            File file = new File(distributionPackage.getId());
            if (file.exists()) {
                VaultPackage pkg = packaging.getPackageManager().open(file);
                ImportOptions opts = VltUtils.getImportOptions(aclHandling, importMode);

                log.debug("using import mode {} and acl {}", opts.getImportMode(), opts.getAccessControlHandling());
                pkg.extract(session, opts);
                return true;
            }
        } catch (Exception e) {
            log.error("could not install the package", e);
            throw new DistributionPackageReadingException(e);
        } finally {
            ungetSession(session);
        }
        return false;
    }
}
