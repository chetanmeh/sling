/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sling.jcr.jackrabbit.server.impl;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.core.config.BeanConfig;
import org.apache.jackrabbit.core.config.BeanFactory;
import org.apache.jackrabbit.core.config.ConfigurationException;
import org.apache.jackrabbit.core.config.SimpleBeanFactory;
import org.apache.jackrabbit.core.security.user.action.AuthorizableAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An OSGi aware BeanFactory implementation. It pulls in dependencies from OSGi and provides it
 * to jackrabbit
 */
@Component
@Service
public class OsgiBeanFactory implements BeanFactory{
    private static final String FACTORY_TYPE_OSGI = "osgi";

    private BeanFactory delegate = new SimpleBeanFactory();

    private Logger log = LoggerFactory.getLogger(getClass());

    @Reference
    private AuthorizableAction authorizableAction;

    public Object newInstance(Class<?> klass, BeanConfig config) throws ConfigurationException {
        if(FACTORY_TYPE_OSGI.equals(config.getFactoryType())){
            if(klass.isAssignableFrom(AuthorizableAction.class)){
                return authorizableAction;
            }

            throw new ConfigurationException("No reference configured for type "+klass.getName());
        }

        return delegate.newInstance(klass,config);
    }
}
