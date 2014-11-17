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
package org.apache.sling.distribution.packaging.impl.exporter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyOption;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.distribution.communication.DistributionRequest;
import org.apache.sling.distribution.component.DistributionComponent;
import org.apache.sling.distribution.component.DistributionComponentProvider;
import org.apache.sling.distribution.component.impl.DefaultDistributionComponentFactoryConstants;
import org.apache.sling.distribution.component.impl.DistributionComponentFactoryManager;
import org.apache.sling.distribution.component.impl.SettingsUtils;
import org.apache.sling.distribution.packaging.DistributionPackage;
import org.apache.sling.distribution.packaging.DistributionPackageExportException;
import org.apache.sling.distribution.packaging.DistributionPackageExporter;
import org.apache.sling.distribution.transport.authentication.TransportAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link org.apache.sling.distribution.packaging.DistributionPackageExporter}
 */
@Component(label = "Sling Distribution - Remote Package Exporter Factory",
        metatype = true,
        configurationFactory = true,
        specVersion = "1.1",
        policy = ConfigurationPolicy.REQUIRE)
@Service(value = DistributionPackageExporter.class)
public class RemoteDistributionPackageExporterFactory implements DistributionPackageExporter, DistributionComponentProvider {
    private static final String TRANSPORT_AUTHENTICATION_PROVIDER_TARGET = DefaultDistributionComponentFactoryConstants.COMPONENT_TRANSPORT_AUTHENTICATION_PROVIDER + ".target";


    private final Logger log = LoggerFactory.getLogger(getClass());

    @Property(value = DefaultDistributionComponentFactoryConstants.PACKAGE_EXPORTER_REMOTE, propertyPrivate = true)
    private static final String TYPE = DefaultDistributionComponentFactoryConstants.COMPONENT_TYPE;

    @Property
    private static final String NAME = DefaultDistributionComponentFactoryConstants.COMPONENT_NAME;

    @Property(name = TRANSPORT_AUTHENTICATION_PROVIDER_TARGET)
    @Reference(name = "TransportAuthenticationProvider", policy = ReferencePolicy.STATIC)
    private volatile TransportAuthenticationProvider transportAuthenticationProvider;

    @Property(cardinality = 100)
    public static final String ENDPOINTS = DefaultDistributionComponentFactoryConstants.PACKAGE_EXPORTER_REMOTE_PROPERTY_ENDPOINTS;

    @Property(name = "poll items", description = "number of subsequent poll requests to make", intValue = 1)
    public static final String POLL_ITEMS = "poll.items";

    @Property(options = {
            @PropertyOption(name = "All",
                    value = "all endpoints"
            ),
            @PropertyOption(name = "One",
                    value = "one endpoint"
            )},
            value = "One"
    )
    private static final String ENDPOINT_STRATEGY = DefaultDistributionComponentFactoryConstants.PACKAGE_EXPORTER_REMOTE_PROPERTY_ENDPOINTS_STRATEGY;


    @Property(label = "Package Builder Properties", cardinality = 100)
    public static final String PACKAGE_BUILDER = DefaultDistributionComponentFactoryConstants.COMPONENT_PACKAGE_BUILDER;

    @Reference
    private DistributionComponentFactoryManager componentManager;

    private DistributionPackageExporter exporter;

    @Activate
    protected void activate(Map<String, Object> config) throws Exception {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.putAll(config);
        String[] packageBuilderProperties = PropertiesUtil.toStringArray(config.get(PACKAGE_BUILDER));
        properties.put(PACKAGE_BUILDER, SettingsUtils.parseLines(packageBuilderProperties));
        properties.put(DefaultDistributionComponentFactoryConstants.COMPONENT_PROVIDER, this);

        exporter = componentManager.createComponent(DistributionPackageExporter.class, properties);
    }


    @Deactivate
    protected void deactivate() {
        exporter = null;
    }

    @Nonnull
    public List<DistributionPackage> exportPackages(@Nonnull ResourceResolver resourceResolver, @Nonnull DistributionRequest distributionRequest) throws DistributionPackageExportException {
        return exporter.exportPackages(resourceResolver, distributionRequest);
    }

    public DistributionPackage getPackage(@Nonnull ResourceResolver resourceResolver, @Nonnull String distributionPackageId) {
        return exporter.getPackage(resourceResolver, distributionPackageId);
    }

    public <ComponentType extends DistributionComponent> ComponentType getComponent(@Nonnull Class<ComponentType> type,
                                                                                   @Nullable String componentName) {
        if (type.isAssignableFrom(TransportAuthenticationProvider.class)) {
            return (ComponentType) transportAuthenticationProvider;
        }

        return null;
    }
}
