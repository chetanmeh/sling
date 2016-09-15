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
package org.apache.sling.contextaware.config.resource.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.contextaware.config.resource.ConfigurationResourceResolver;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service=ConfigurationResourceResolver.class, immediate=true)
@Designate(ocd=ConfigurationResourceResolverImpl.Config.class)
public class ConfigurationResourceResolverImpl implements ConfigurationResourceResolver {

    @ObjectClassDefinition(name="Apache Sling Context Aware Configuration Resolver",
                           description="Standardized access to configurations in the resource tree.")
    public static @interface Config {

        @AttributeDefinition(name="Allowed paths",
                             description = "Whitelist of paths where configurations can reside in.")
        String[] allowedPaths() default {"/conf", "/apps/conf", "/libs/conf"};

        @AttributeDefinition(name="Fallback paths",
                description = "Global fallback configurations, ordered from most specific (checked first) to least specific.")
        String[] fallbackPaths() default {"/conf/global", "/apps/conf", "/libs/conf"};
    }

    private static final String PROPERTY_CONFIG = "sling:config-ref";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private volatile Config configuration;

    Config getConfiguration() {
        return this.configuration;
    }

    @Activate
    private void activate(final Config config) {
        this.configuration = config;
    }

    @Deactivate
    private void deactivate() {
        this.configuration = null;
    }

    List<String> getResolvePaths(final Resource contentResource) {
        final List<String> refPaths = new ArrayList<>();

        // add all config references found in resource hierarchy
        final List<ConfigReference> refs = new ArrayList<>();
        findConfigRefs(refs, contentResource);
        for (ConfigReference ref : refs) {
            refPaths.add(ref.getConfigReference());
        }

        // finally add the global fallbacks
        if ( this.configuration.fallbackPaths() != null ) {
            for(final String path : this.configuration.fallbackPaths()) {
                logger.debug("[{}] fallback config => {}", refs.size(), path);
                refPaths.add(path);
            }
        }

        return refPaths;
    }

    /**
     * Check the name.
     * A name must not be null and relative.
     * @param name The name
     * @return {@code true} if it is valid
     */
    private boolean checkName(final String name) {
        if (name == null || name.isEmpty() || name.startsWith("/") || name.contains("../") ) {
            return false;
        }
        return true;
    }

    /**
     * Find next configuration refrence for given resource or one of it's parents.
     * @param startResource Resource to start searching
     * @return Config reference or null if none found
     */
    private ConfigReference findNextConfigRef(final Resource startResource) {
        // start at resource, go up
        Resource resource = startResource;
        while (resource != null) {
            String ref = getReference(resource);
            if (ref != null) {
                // if absolute path found we are (probably) done
                if (ref.startsWith("/")) {
                    // combine full path if relativeRef is present
                    ref = ResourceUtil.normalize(ref);

                    if (ref != null && !isAllowedConfigPath(ref)) {
                        logger.warn("Ignoring reference to {} from {} - not in allowed paths.", ref, resource.getPath());
                        ref = null;
                    }

                    if (ref != null && isFallbackConfigPath(ref)) {
                        logger.warn("Ignoring reference to {} from {} - already a fallback path.", ref, resource.getPath());
                        ref = null;
                    }

                    if (ref != null) {
                        return new ConfigReference(resource, ref);
                    }

                } else {
                    logger.error("Invalid relative reference found for {} : {}. This entry is ignored", resource.getPath(), ref);
                }
            }
            // if getParent() returns null, stop
            resource = resource.getParent();
        }

        // if hit root and nothing found, return null
        return null;
    }

    /**
     * Searches the resource hierarchy upwards for all config references and returns them.
     * @param refs List to add found resources to
     * @param startResource Resource to start searching
     */
    private void findConfigRefs(final List<ConfigReference> refs, final Resource startResource) {
        ConfigReference ref = findNextConfigRef(startResource);
        if (ref != null) {
            refs.add(ref);
            findConfigRefs(refs, ref.getContentResource().getParent());
        }
    }

    private String getReference(final Resource resource) {
        final String ref = resource.getValueMap().get(PROPERTY_CONFIG, String.class);
        logger.trace("Reference '{}' found at {}", ref, resource.getPath());

        return ref;
    }

    private boolean isAllowedConfigPath(String path) {
        if (this.configuration.allowedPaths() == null) {
            return false;
        }
        for (String pattern : this.configuration.allowedPaths()) {
            if (logger.isTraceEnabled()) {
                logger.trace("- checking if '{}' starts with {}", path, pattern);
            }
            if (path.equals(pattern) || path.startsWith(pattern + "/")) {
                return true;
            }
        }
        return false;
    }

    private boolean isFallbackConfigPath(final String ref) {
        if ( this.configuration.fallbackPaths() != null ) {
            for(final String name : this.configuration.fallbackPaths()) {
                if ( name.equals(ref) ) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Resource getResource(final Resource contentResource, final String bucketName, final String configName) {
        if (contentResource == null || !checkName(bucketName) || !checkName(configName)) {
            return null;
        }
        String name = bucketName + "/" + configName;
        logger.debug("Searching {} for resource {}", name, contentResource.getPath());

        // strategy: find first item among all configured paths
        int idx = 1;
        for (final String path : getResolvePaths(contentResource)) {
            final Resource item = contentResource.getResourceResolver().getResource(ResourceUtil.normalize(path + "/" + name));
            if (item != null) {
                logger.debug("Resolved config item at [{}]: {}", idx, item.getPath());

                return item;
            }
            idx++;
        }

        logger.debug("Could not resolve any config item for '{}' (or no permissions to read it)", name);

        // nothing found
        return null;
    }

    @Override
    public Collection<Resource> getResourceCollection(final Resource contentResource, final String bucketName, final String configName) {
        if (contentResource == null || !checkName(bucketName) || !checkName(configName)) {
            return Collections.emptyList();
        }
        String name = bucketName + "/" + configName;
        if (logger.isTraceEnabled()) {
            logger.trace("- searching for list '{}'", name);
        }

        final Set<String> names = new HashSet<>();
        final List<Resource> result = new ArrayList<>();
        int idx = 1;
        for (String path : this.getResolvePaths(contentResource)) {
            Resource item = contentResource.getResourceResolver().getResource(ResourceUtil.normalize(path + "/" + name));
            if (item != null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("+ resolved config item at [{}]: {}", idx, item.getPath());
                }

                for (Resource child : item.getChildren()) {
                    if ( !child.getName().contains(":") && !names.contains(child.getName()) ) {
                        result.add(child);
                        names.add(child.getName());
                    }
                }

            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace("- no item '{}' under config '{}'", name, path);
                }
            }
            idx++;
        }

        if (logger.isTraceEnabled()) {
            logger.trace("- final list has {} items", result.size());
        }

        return result;
    }

    @Override
    public String getContextPath(Resource resource) {
        ConfigReference ref = findNextConfigRef(resource);
        if (ref != null) {
            return ref.getContentResource().getPath();
        }
        else {
            return null;
        }
    }

    @Override
    public Collection<String> getAllContextPaths(Resource resource) {
        final List<String> contextPaths = new ArrayList<>();
        final List<ConfigReference> refs = new ArrayList<>();
        findConfigRefs(refs, resource);
        for (ConfigReference ref : refs) {
            contextPaths.add(ref.getContentResource().getPath());
        }
        return contextPaths;
    }

    private static class ConfigReference {
        private final Resource contentResource;
        private final String configReference;

        public ConfigReference(Resource contentResource, String configReference) {
            this.contentResource = contentResource;
            this.configReference = configReference;
        }
        public Resource getContentResource() {
            return contentResource;
        }
        public String getConfigReference() {
            return configReference;
        }
    }

}
