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
package org.apache.sling.jcr.oak.server.impl;

import org.apache.jackrabbit.oak.api.ContentRepository;
import org.apache.jackrabbit.oak.core.ContentRepositoryImpl;
import org.apache.jackrabbit.oak.jcr.RepositoryImpl;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.jcr.base.AbstractNamespaceMappingRepository;
import org.osgi.framework.BundleContext;

import javax.jcr.*;
import java.util.concurrent.ScheduledExecutorService;

public class SlingRepositoryImpl
        extends AbstractNamespaceMappingRepository implements SlingRepository {

    private final RepositoryImpl delegatee;

    public SlingRepositoryImpl(
            ContentRepository repository,
            ScheduledExecutorService executor,
            BundleContext context) {
        delegatee = new RepositoryImpl(repository,executor);
        this.setup(context);
    }

    public void dispose() {
        this.tearDown();
    }

    @Override
    public String getDefaultWorkspace() {
        return "crx.default";
    }

    @Override
    public Session login(Credentials credentials, String workspace)
            throws RepositoryException {
        Thread thread = Thread.currentThread();
        ClassLoader loader = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(
                    ContentRepositoryImpl.class.getClassLoader());
            if(credentials == null){
                credentials = new GuestCredentials();
            }
            return this.getNamespaceAwareSession(delegatee.login(credentials, null)); // FIXME: workspace
        } finally {
            thread.setContextClassLoader(loader);
        }
    }

    @Override
    public Session loginAdministrative(String workspace)
            throws RepositoryException {
        Thread thread = Thread.currentThread();
        ClassLoader loader = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(
                    ContentRepositoryImpl.class.getClassLoader());
            return login(
                    new SimpleCredentials("admin", "admin".toCharArray()),
                    null); // FIXME: workspace
        } finally {
            thread.setContextClassLoader(loader);
        }
    }

    @Override
    public String[] getDescriptorKeys() {
        return delegatee.getDescriptorKeys();
    }

    @Override
    public boolean isStandardDescriptor(String key) {
        return delegatee.isStandardDescriptor(key);
    }

    /**
     * @see javax.jcr.Repository#isSingleValueDescriptor(String)
     */
    public boolean isSingleValueDescriptor(String key) {
        return delegatee.isSingleValueDescriptor(key);
    }

    /**
     * @see javax.jcr.Repository#getDescriptorValue(String)
     */
    public Value getDescriptorValue(String key) {
        return delegatee.getDescriptorValue(key);
    }

    /**
     * @see javax.jcr.Repository#getDescriptorValues(String)
     */
    public Value[] getDescriptorValues(String key) {
        return delegatee.getDescriptorValues(key);
    }

    /**
     * @see javax.jcr.Repository#getDescriptor(String)
     */
    public String getDescriptor(String key) {
        return delegatee.getDescriptor(key);
    }

    /**
     * @see javax.jcr.Repository#login(javax.jcr.Credentials)
     */
    public Session login(Credentials credentials) throws RepositoryException {
        return this.getNamespaceAwareSession(delegatee.login(credentials));
    }

    /**
     * @see javax.jcr.Repository#login(String)
     */
    public Session login(String workspaceName) throws RepositoryException {
        return this.getNamespaceAwareSession(delegatee.login(workspaceName));
    }

    /**
     * @see javax.jcr.Repository#login()
     */
    public Session login() throws RepositoryException {
        return this.getNamespaceAwareSession(delegatee.login());
    }

}
