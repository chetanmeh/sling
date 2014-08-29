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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.felix.scr.annotations.*;
import org.apache.http.client.fluent.Executor;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.replication.communication.ReplicationEndpoint;
import org.apache.sling.replication.event.ReplicationEventFactory;
import org.apache.sling.replication.packaging.ReplicationPackage;
import org.apache.sling.replication.packaging.ReplicationPackageImporter;
import org.apache.sling.replication.serialization.ReplicationPackageReadingException;
import org.apache.sling.replication.transport.ReplicationTransportHandler;
import org.apache.sling.replication.transport.authentication.TransportAuthenticationProvider;
import org.apache.sling.replication.transport.authentication.TransportAuthenticationProviderFactory;
import org.apache.sling.replication.transport.impl.MultipleEndpointReplicationTransportHandler;
import org.apache.sling.replication.transport.impl.ReplicationTransportConstants;
import org.apache.sling.replication.transport.impl.SimpleHttpReplicationTransportHandler;
import org.apache.sling.replication.transport.impl.TransportEndpointStrategyType;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Remote implementation of {@link org.apache.sling.replication.packaging.ReplicationPackageImporter}
 */
@Component(label = "Remote Replication Package Importer", configurationFactory = true)
@Service(value = ReplicationPackageImporter.class)
public class RemoteReplicationPackageImporterFactory implements ReplicationPackageImporter {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Property
    private static final String NAME = "name";

    @Property(name = ReplicationTransportConstants.TRANSPORT_AUTHENTICATION_FACTORY)
    @Reference(name = "TransportAuthenticationProviderFactory", policy = ReferencePolicy.DYNAMIC)
    private TransportAuthenticationProviderFactory transportAuthenticationProviderFactory;

    @Property(options = {
            @PropertyOption(name = "All",
                    value = "all endpoints"
            ),
            @PropertyOption(name = "One",
                    value = "one endpoint"
            )},
            value = "One"
    )
    private static final String ENDPOINT_STRATEGY = ReplicationTransportConstants.ENDPOINT_STRATEGY;

    @Reference
    private ReplicationEventFactory replicationEventFactory;

    private RemoteReplicationPackageImporter importer;

    @Activate
    protected void activate(BundleContext context, Map<String, Object> config) throws Exception {

       importer = getInstance(config, transportAuthenticationProviderFactory);

    }

    public static RemoteReplicationPackageImporter getInstance(Map<String, Object> config, TransportAuthenticationProviderFactory transportAuthenticationProviderFactory) {

        if (transportAuthenticationProviderFactory == null) {
            throw new IllegalArgumentException("transportAuthenticationProviderFactory is required");
        }

        Map<String, String> authenticationProperties = PropertiesUtil.toMap(config.get(ReplicationTransportConstants.AUTHENTICATION_PROPERTIES), new String[0]);

        String[] endpoints = PropertiesUtil.toStringArray(config.get(ReplicationTransportConstants.ENDPOINTS), new String[0]);

        String endpointStrategyName = PropertiesUtil.toString(config.get(ENDPOINT_STRATEGY),
                TransportEndpointStrategyType.One.name());
        TransportEndpointStrategyType transportEndpointStrategyType = TransportEndpointStrategyType.valueOf(endpointStrategyName);


        return new RemoteReplicationPackageImporter(transportAuthenticationProviderFactory,
                authenticationProperties, endpoints, transportEndpointStrategyType);

    }


    public boolean importPackage(ReplicationPackage replicationPackage) {
       return importer.importPackage(replicationPackage);
    }

    public ReplicationPackage readPackage(InputStream stream) throws ReplicationPackageReadingException {
        return importer.readPackage(stream);
    }

}
