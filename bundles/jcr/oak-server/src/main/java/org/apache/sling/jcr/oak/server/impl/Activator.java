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

import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.oak.api.ContentRepository;
import org.apache.jackrabbit.oak.util.TODO;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Activator implements BundleActivator, ServiceTrackerCustomizer {

    private BundleContext context;

    private ScheduledExecutorService executor;

    private ServiceTracker tracker;

    private Logger log = LoggerFactory.getLogger(getClass());

    private final Map<ServiceReference, ServiceRegistration> slingRepositories =
            new HashMap<ServiceReference, ServiceRegistration>();
    private final Map<ServiceReference, SlingRepositoryImpl> repositories =
            new HashMap<ServiceReference, SlingRepositoryImpl>();

    //-----------------------------------------------------< BundleActivator >--

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        TODO.relax();
        context = bundleContext;
        executor = Executors.newScheduledThreadPool(1);
        tracker = new ServiceTracker(
                context, ContentRepository.class.getName(), this);
        tracker.open();
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        tracker.close();
        executor.shutdown();
    }

    //--------------------------------------------< ServiceTrackerCustomizer >--

    @Override
    public Object addingService(ServiceReference reference) {
        Object service = context.getService(reference);
        if (service instanceof ContentRepository) {
            SlingRepositoryImpl repository = new SlingRepositoryImpl(
                    (ContentRepository) service, executor,context);
            createSystemUsers(repository);
            slingRepositories.put(reference, context.registerService(
                    new String[] { Repository.class.getName(), SlingRepository.class.getName() },
                    repository, new Properties()));
            repositories.put(reference,repository);
            return service;
        } else {
            context.ungetService(reference);
            return null;
        }
    }

    private void createSystemUsers(SlingRepositoryImpl repository) {
        Session s = null;
        try{
            s = repository.loginAdministrative(null);
            UserManager um = ((JackrabbitSession) s).getUserManager();
            Authorizable a = um.getAuthorizable("admin");
            if(a == null){
                um.createUser("admin","admin");
                s.save();
            }
        } catch (RepositoryException e) {
            log.error("Error occurred while bootstrapping system user",e);
        } finally{
             if(s != null){
                 s.logout();
             }
        }
    }

    @Override
    public void modifiedService(ServiceReference reference, Object service) {
    }

    @Override
    public void removedService(ServiceReference reference, Object service) {
        if(slingRepositories.containsKey(reference)){
            slingRepositories.remove(reference).unregister();
        }
        if(repositories.containsKey(reference)){
            repositories.remove(reference).dispose();
        }

        context.ungetService(reference);
    }

}
