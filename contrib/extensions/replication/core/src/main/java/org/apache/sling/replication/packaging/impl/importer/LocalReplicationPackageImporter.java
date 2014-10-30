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
package org.apache.sling.replication.packaging.impl.importer;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.replication.event.impl.ReplicationEventFactory;
import org.apache.sling.replication.event.ReplicationEventType;
import org.apache.sling.replication.packaging.ReplicationPackage;
import org.apache.sling.replication.packaging.ReplicationPackageImportException;
import org.apache.sling.replication.packaging.ReplicationPackageImporter;
import org.apache.sling.replication.serialization.ReplicationPackageBuilder;
import org.apache.sling.replication.serialization.ReplicationPackageReadingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link org.apache.sling.replication.packaging.ReplicationPackageImporter} implementation which imports a FileVault
 * based {@link org.apache.sling.replication.packaging.ReplicationPackage} locally.
 */

public class LocalReplicationPackageImporter implements ReplicationPackageImporter {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ReplicationPackageBuilder packageBuilder;

    private final ReplicationEventFactory replicationEventFactory;


    public LocalReplicationPackageImporter(ReplicationPackageBuilder packageBuilder,
                                           ReplicationEventFactory replicationEventFactory) {

        if (packageBuilder == null) {
            throw new IllegalArgumentException("A package builder is required");
        }

        if (replicationEventFactory == null) {
            throw new IllegalArgumentException("An event factory is required");
        }
        this.replicationEventFactory = replicationEventFactory;
        this.packageBuilder = packageBuilder;
    }


    public boolean importPackage(@Nonnull ResourceResolver resourceResolver, @Nonnull ReplicationPackage replicationPackage) throws ReplicationPackageImportException {
        boolean success;
        try {
            success = packageBuilder.installPackage(resourceResolver, replicationPackage);

            if (success) {
                log.info("replication package read and installed for path(s) {}", Arrays.toString(replicationPackage.getPaths()));

                Dictionary<String, Object> dictionary = new Hashtable<String, Object>();
                dictionary.put("replication.action", replicationPackage.getAction());
                dictionary.put("replication.path", replicationPackage.getPaths());
                replicationEventFactory.generateEvent(ReplicationEventType.PACKAGE_INSTALLED, dictionary);

            } else {
                log.warn("could not read a replication package");
            }
        } catch (Exception e) {
            log.error("cannot import a package from the given stream of type {}", replicationPackage.getType());
            throw new ReplicationPackageImportException(e);
        }
        return success;
    }

    public ReplicationPackage importStream(@Nonnull ResourceResolver resourceResolver, @Nonnull InputStream stream) throws ReplicationPackageImportException {
        try {
            ReplicationPackage replicationPackage = packageBuilder.readPackage(resourceResolver, stream);
            if (importPackage(resourceResolver, replicationPackage)) {
                return replicationPackage;
            } else {
                throw new ReplicationPackageImportException("could not import the package " + replicationPackage);
            }
        } catch (ReplicationPackageReadingException e) {
            throw new ReplicationPackageImportException("cannot read a package from the given stream", e);
        }
    }

}
