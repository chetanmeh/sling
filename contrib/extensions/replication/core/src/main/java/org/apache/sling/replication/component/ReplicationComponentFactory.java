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
package org.apache.sling.replication.component;

import aQute.bnd.annotation.ProviderType;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * factory for {@link ReplicationComponent}s
 */
@ProviderType
public interface ReplicationComponentFactory {

    /**
     * the type of the component
     */
    String COMPONENT_TYPE = "type";

    /**
     * the name of the component
     */
    String COMPONENT_NAME = "name";

    /**
     * type for components referencing OSGi services
     */
    String COMPONENT_TYPE_SERVICE = "service";

    /**
     * component enabled
     */
    String COMPONENT_ENABLED = "enabled";


    /**
     * replication agent component
     */
    String COMPONENT_AGENT = "agent";

    /**
     * package exporter component
     */
    String COMPONENT_PACKAGE_EXPORTER = "packageExporter";

    /**
     * package importer component
     */
    String COMPONENT_PACKAGE_IMPORTER = "packageImporter";

    /**
     * request authorization strategy component
     */
    String COMPONENT_REQUEST_AUTHORIZATION_STRATEGY = "requestAuthorizationStrategy";

    /**
     * queue distribution strategy component
     */
    String COMPONENT_QUEUE_DISTRIBUTION_STRATEGY = "queueDistributionStrategy";

    /**
     * queue provider component
     */
    String COMPONENT_QUEUE_PROVIDER = "queueProvider";

    /**
     * trigger component
     */
    String COMPONENT_TRIGGER = "trigger";

    /**
     * package builder component
     */
    String COMPONENT_PACKAGE_BUILDER = "packageBuilder";

    /**
     * transport authentication provider component
     */
    String COMPONENT_TRANSPORT_AUTHENTICATION_PROVIDER = "transportAuthenticationProvider";

    /**
     * simple replication agent type
     */
    String AGENT_SIMPLE = "simple";

    /**
     * service user property
     */
    String AGENT_SIMPLE_PROPERTY_SERVICE_NAME = "serviceName";

    /**
     * 'passive' property for agents (for defining "queueing agents")
     */
    String AGENT_SIMPLE_PROPERTY_IS_PASSIVE = "isPassive";

    /**
     * local package exporter type
     */
    String PACKAGE_EXPORTER_LOCAL = "local";

    /**
     * remote package exporter type
     */
    String PACKAGE_EXPORTER_REMOTE = "remote";

    /**
     * endpoints property
     */
    String PACKAGE_EXPORTER_REMOTE_PROPERTY_ENDPOINTS = "endpoints";

    /**
     * endpoint strategy property
     */
    String PACKAGE_EXPORTER_REMOTE_PROPERTY_ENDPOINTS_STRATEGY = "endpoints.strategy";

    /**
     * no. of items to poll property
     */
    String PACKAGE_EXPORTER_REMOTE_PROPERTY_POLL_ITEMS = "poll.items";

    /**
     * package exporter's agent property
     */
    String PACKAGE_EXPORTER_AGENT = "agent";

    /**
     * local package importer type
     */
    String PACKAGE_IMPORTER_LOCAL = "local";

    /**
     * remote package importer type
     */
    String PACKAGE_IMPORTER_REMOTE = "remote";

    /**
     * endpoints property
     */
    String PACKAGE_IMPORTER_REMOTE_PROPERTY_ENDPOINTS = "endpoints";

    /**
     * endpoint strategy property
     */
    String PACKAGE_IMPORTER_REMOTE_PROPERTY_ENDPOINTS_STRATEGY = "endpoints.strategy";

    /**
     * user property
     */
    String TRANSPORT_AUTHENTICATION_PROVIDER_USER = "user";

    /**
     * username property
     */
    String TRANSPORT_AUTHENTICATION_PROVIDER_USER_PROPERTY_USERNAME = "username";

    /**
     * password property
     */
    String TRANSPORT_AUTHENTICATION_PROVIDER_USER_PROPERTY_PASSWORD = "password";

    /**
     * remote event trigger type
     */
    String TRIGGER_REMOTE_EVENT = "remoteEvent";

    /**
     * remote event endpoint property
     */
    String TRIGGER_REMOTE_EVENT_PROPERTY_ENDPOINT = "endpoint";

    /**
     * resource event trigger type
     */
    String TRIGGER_RESOURCE_EVENT = "resourceEvent";

    /**
     * resource event path property
     */
    String TRIGGER_RESOURCE_EVENT_PROPERTY_PATH = "path";

    /**
     * scheduled trigger type
     */
    String TRIGGER_SCHEDULED_EVENT = "scheduledEvent";

    /**
     * scheduled trigger action property
     */
    String TRIGGER_SCHEDULED_EVENT_PROPERTY_ACTION = "action";

    /**
     * scheduled trigger path property
     */
    String TRIGGER_SCHEDULED_EVENT_PROPERTY_PATH = "path";

    /**
     * scheduled trigger seconds property
     */
    String TRIGGER_SCHEDULED_EVENT_PROPERTY_SECONDS = "seconds";

    /**
     * chain replication trigger type
     */
    String TRIGGER_REPLICATION_EVENT = "replicationEvent";

    /**
     * chain replication path property
     */
    String TRIGGER_REPLICATION_EVENT_PROPERTY_PATH = "path";

    /**
     * jcr event trigger type
     */
    String TRIGGER_JCR_EVENT = "jcrEvent";

    /**
     * jcr event trigger path property
     */
    String TRIGGER_JCR_EVENT_PROPERTY_PATH = "path";

    /**
     * jcr event trigger service user property
     */
    String TRIGGER_JCR_EVENT_PROPERTY_SERVICE_NAME = "servicename";

    /**
     * jcr persisting event trigger type
     */
    String TRIGGER_PERSISTED_JCR_EVENT = "persistedJcrEvent";

    /**
     * jcr persisting event trigger path property
     */
    String TRIGGER_PERSISTED_JCR_EVENT_PROPERTY_PATH = "path";

    /**
     * jcr persisting event trigger service user property
     */
    String TRIGGER_PERSISTED_JCR_EVENT_PROPERTY_SERVICE_NAME = "servicename";

    /**
     * jcr persisting event trigger nuggets path property
     */
    String TRIGGER_PERSISTED_JCR_EVENT_PROPERTY_NUGGETS_PATH = "nuggetsPath";

    /**
     * privilege request authorization strategy type
     */
    String REQUEST_AUTHORIZATION_STRATEGY_PRIVILEGE = "privilege";

    /**
     * privilege request authorization strategy jcr privilege property
     */
    String REQUEST_AUTHORIZATION_STRATEGY_PRIVILEGE_PROPERTY_JCR_PRIVILEGE = "jcrPrivilege";

    /**
     * file vault package builder type
     */
    String PACKAGE_BUILDER_FILEVLT = "vlt";

    /**
     * import mode property for file vault package builder
     */
    String PACKAGE_BUILDER_FILEVLT_IMPORT_MODE = "importOptions";

    /**
     * ACL handling property for file vault package builder
     */
    String PACKAGE_BUILDER_FILEVLT_ACLHANDLING = "aclHandling";


    /**
     * create a {@link ReplicationComponent}
     *
     * @param type              the {@link java.lang.Class} of the component to be created
     * @param properties        the properties to be supplied for the initialization of the component
     * @param componentProvider the {@link ReplicationComponentProvider} used to eventually
     *                          wire additional required {@link ReplicationComponent}s
     * @param <ComponentType>   the actual type of the {@link ReplicationComponent}
     *                          to be created
     * @return a {@link ReplicationComponent} of the specified type initialized with given properties or <code>null</code>
     * if that could not be created
     */
    @CheckForNull
    <ComponentType extends ReplicationComponent> ComponentType createComponent(@Nonnull java.lang.Class<ComponentType> type,
                                                                               @Nonnull Map<String, Object> properties,
                                                                               @Nullable ReplicationComponentProvider componentProvider);
}
