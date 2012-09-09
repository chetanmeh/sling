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

package org.apache.sling.jcr.jackrabbit.server.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.ConfigurationSpi;
import java.util.Properties;

/**
 * User: chetanm
 * Date: 9/9/12
 * Time: 7:40 PM
 */
public class ConfigurationSpiTracker extends ServiceTracker {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final BundleContext context;
    private ServiceRegistration reg;

    public ConfigurationSpiTracker(BundleContext context) {
        super(context,"javax.security.auth.login.ConfigurationSpi",null);
        this.context = context;
    }

    @Override
    public Object addingService(ServiceReference reference) {
        Properties props = new Properties();
        props.setProperty(Constants.SERVICE_DESCRIPTION,"Marker object to indicate that repository can be started");
        log.info("Registering the marker instance as JAAS ConfigurationSpi is registered");
        reg = context.registerService(RepositoryStarter.class.getName(),new RepositoryStarter(),props);
        return super.addingService(reference);
    }

    @Override
    public synchronized void close() {
        if(reg != null){
            reg.unregister();
        }
        super.close();
    }
}
