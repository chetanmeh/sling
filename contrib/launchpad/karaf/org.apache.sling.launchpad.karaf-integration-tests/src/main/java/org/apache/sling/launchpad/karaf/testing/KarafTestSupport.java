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
package org.apache.sling.launchpad.karaf.testing;

import java.io.File;
import java.net.ServerSocket;

import javax.inject.Inject;

import org.apache.karaf.features.BootFinished;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.util.Filter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.streamBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFileExtend;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;

public abstract class KarafTestSupport {

    protected int sshPort;

    protected int httpPort;

    protected int rmiRegistryPort;

    protected int rmiServerPort;

    @Inject
    protected BundleContext bundleContext;

    @Inject
    @Filter(timeout = 300000)
    BootFinished bootFinished;

    public static final String KARAF_GROUP_ID = "org.apache.karaf";

    public static final String KARAF_ARTIFACT_ID = "apache-karaf";

    public static final String KARAF_VERSION = "3.0.0";

    public static final String KARAF_NAME = "Apache Karaf";

    protected KarafTestSupport() {
    }

    protected synchronized int findFreePort() {
        try {
            final ServerSocket serverSocket = new ServerSocket(0);
            final int port = serverSocket.getLocalPort();
            serverSocket.close();
            return port;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected synchronized int sshPort() {
        if (sshPort == 0) {
            sshPort = findFreePort();
        }
        return sshPort;
    }

    protected synchronized int httpPort() {
        if (httpPort == 0) {
            httpPort = findFreePort();
        }
        return httpPort;
    }

    protected synchronized int rmiRegistryPort() {
        if (rmiRegistryPort == 0) {
            rmiRegistryPort = findFreePort();
        }
        return rmiRegistryPort;
    }

    protected synchronized int rmiServerPort() {
        if (rmiServerPort == 0) {
            rmiServerPort = findFreePort();
        }
        return rmiServerPort;
    }

    public String karafGroupId() {
        return KARAF_GROUP_ID;
    }

    public String karafArtifactId() {
        return KARAF_ARTIFACT_ID;
    }

    public String karafVersion() {
        return KARAF_VERSION;
    }

    public String karafName() {
        return KARAF_NAME;
    }

    protected Option addBootFeature(final String feature) {
        return editConfigurationFileExtend("etc/org.apache.karaf.features.cfg", "featuresBoot", "," + feature);
    }

    protected Bundle findBundle(final String symbolicName) {
        for (final Bundle bundle : bundleContext.getBundles()) {
            if (symbolicName.equals(bundle.getSymbolicName())) {
                return bundle;
            }
        }
        return null;
    }

    protected String featureRepository() {
        return "mvn:org.apache.sling/org.apache.sling.launchpad.karaf-features/0.1.1-SNAPSHOT/xml/features";
    }

    protected Option karafTestSupportBundle() {
        return streamBundle(
            bundle()
                .add(KarafTestSupport.class)
                .set(Constants.BUNDLE_SYMBOLICNAME, "org.apache.sling.launchpad.karaf-integration-tests")
                .set(Constants.EXPORT_PACKAGE, "org.apache.sling.launchpad.karaf.testing")
                .set(Constants.IMPORT_PACKAGE, "javax.inject, org.apache.karaf.features, org.ops4j.pax.exam, org.ops4j.pax.exam.options, org.ops4j.pax.exam.util, org.ops4j.pax.tinybundles.core, org.osgi.framework")
                .build()
        ).start();
    }

    protected Option[] baseConfiguration() {
        return options(
            karafDistributionConfiguration()
                .frameworkUrl(maven().groupId(karafGroupId()).artifactId(karafArtifactId()).version(karafVersion()).type("tar.gz"))
                .karafVersion(karafVersion())
                .useDeployFolder(false)
                .name(karafName())
                .unpackDirectory(new File("target/paxexam/")),
            keepRuntimeFolder(),
            logLevel(LogLevelOption.LogLevel.INFO),
            editConfigurationFileExtend("etc/org.apache.karaf.features.cfg", "featuresRepositories", "," + featureRepository()),
            editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiRegistryPort", Integer.toString(rmiRegistryPort())),
            editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiServerPort", Integer.toString(rmiServerPort())),
            editConfigurationFilePut("etc/org.apache.karaf.shell.cfg", "sshPort", Integer.toString(sshPort())),
            editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.osgi.service.http.port", Integer.toString(httpPort())),
            mavenBundle()
                .groupId("org.ops4j.pax.tinybundles")
                .artifactId("tinybundles")
                .version("2.0.0"),
            karafTestSupportBundle()
        );
    }

}
