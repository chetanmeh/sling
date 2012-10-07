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

package org.apache.sling.jcr.jackrabbit.security.principalprovider.impl;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.References;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.core.security.principal.PrincipalProvider;
import org.apache.jackrabbit.core.security.principal.PrincipalProviderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: chetanm
 * Date: 9/9/12
 * Time: 12:33 AM
 */
@Component(specVersion = "1.1")
@Service
@References({
        @Reference(
                name = "PrincipalProvider",
                referenceInterface = PrincipalProvider.class,
                cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE,
                policy = ReferencePolicy.DYNAMIC)
})
public class DynamicPrincipalProviderRegistry implements PrincipalProviderRegistry {
    /**
     * Property-Key if the <code>PrincipalProvider</code> configured with
     * {@link LoginModuleConfig#PARAM_PRINCIPAL_PROVIDER_CLASS} be registered using the
     * specified name instead of the class name which is used by default if the
     * name parameter is missing.
     *
     * @see <a href="https://issues.apache.org/jira/browse/JCR-2629">JCR-2629</a>
     */
    private static final String COMPAT_PRINCIPAL_PROVIDER_NAME = "principal_provider.name";

    private Logger log = LoggerFactory.getLogger(getClass());

    private final Map<String, PrincipalProvider> providers = new ConcurrentHashMap<String, PrincipalProvider>();
    private PrincipalProvider[] providerArray = new PrincipalProvider[0];

    public PrincipalProvider registerProvider(Properties properties) throws RepositoryException {
        throw new UnsupportedOperationException("The PrincipalProvider are only registered as OSGi services");
    }

    public PrincipalProvider getDefault() {
        throw new UnsupportedOperationException("Default provider is handled via WorkspaceBasedPrincipalProviderRegistry");
    }

    public PrincipalProvider getProvider(String providerName) {
        return providers.get(providerName);
    }

    public PrincipalProvider[] getProviders() {
        return providerArray;
//        return Arrays.copyOf(providerArray,providerArray.length);
    }

    private void bindPrincipalProvider(PrincipalProvider provider,Map config){
        String providerName = getProviderName(provider,config);
        if(providers.containsKey(providerName)){
           log.warn("Provider with name {} is already registered. PrincipalProvider {} " +
                   "would not be registered",providerName,provider.getClass().getName());
            return;
        }
        providers.put(providerName,provider);
        reloadProviders();
    }

    private void unbindPrincipalProvider(PrincipalProvider provider,Map config){
        String providerName = getProviderName(provider,config);
        PrincipalProvider removed = providers.remove(providerName);
        if(removed != null){
            reloadProviders();
        }
    }

    private synchronized  void reloadProviders() {
        providerArray = providers.values().toArray(new PrincipalProvider[providers.size()]);
    }

    private String getProviderName(PrincipalProvider provider,Map config){
        String providerName = (String) config.get(COMPAT_PRINCIPAL_PROVIDER_NAME);
        if(providerName == null){
            providerName = provider.getClass().getName();
        }
        return providerName;
    }
}
