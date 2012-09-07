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

import org.apache.felix.dm.Component;
import org.apache.felix.dm.Dependency;
import org.apache.felix.dm.DependencyManager;
import org.apache.felix.scr.annotations.Activate;
import org.apache.jackrabbit.core.config.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.jackrabbit.core.config.RepositoryConfigurationParser.REPOSITORY_HOME_VARIABLE;

/**
 * An OSGi aware BeanFactory implementation. It pulls in dependencies from OSGi and provides it
 * to jackrabbit
 */
@org.apache.felix.scr.annotations.Component
public class OsgiBeanFactory implements BeanFactory{
    private static final String FACTORY_TYPE_OSGI = "osgi";

    private BeanFactory delegate = new SimpleBeanFactory();

    private Logger log = LoggerFactory.getLogger(getClass());

    private Map<String,Class> classNameMapping = new HashMap<String,Class>();
    private Map<Class,Object> instances = new ConcurrentHashMap<Class, Object>();

    @Activate
    private void activate(BundleContext context) throws IOException, ConfigurationException {
        parseAndReadConfig(context);

        log.info("Would be finding implementations for "+ classNameMapping.values());
        DependencyManager dm = new DependencyManager(context);
        Component c = dm.createComponent();

        c.setInterface(BeanFactory.class.getName(),new Properties())
         .setImplementation(this);

        for(Class clazz : classNameMapping.values()){
            Dependency d = dm.createServiceDependency()
              .setService(clazz)
              .setRequired(true)
              .setCallbacks(this,"added","removed");

            c.add(d);
        }

        dm.add(c);
    }

    /**
     * A hacky incorrect way to read the config. For proper implementation we should read config as managed in
     * org.apache.sling.jcr.jackrabbit.server.impl.SlingServerRepository#acquireRepository()
     *
     * This is just done to demonstrate working of concept!!
     */
    private void parseAndReadConfig(BundleContext context) throws ConfigurationException, IOException {
        URL u = context.getBundle().getResource("repository.xml");
        Properties variables = new Properties(System.getProperties());
        variables.setProperty(REPOSITORY_HOME_VARIABLE, "");
        RepositoryConfigurationParser parser = new RepositoryConfigurationParser(variables);
        parser.setConfigVisitor(new DepFinderBeanConfigVisitor());

        InputStream is = null;

        try{
            is = u.openStream();
            parser.parseRepositoryConfig(new InputSource(is));
        }finally{
            if(is != null){
                is.close();
            }
        }
    }

    public Object newInstance(Class<?> klass, BeanConfig config) throws ConfigurationException {
        if(FACTORY_TYPE_OSGI.equals(config.getFactoryType())){
            if(instances.containsKey(klass)){
                return instances.get(klass);
            }

            throw new ConfigurationException("No reference configured for type "+klass.getName());
        }

        return delegate.newInstance(klass,config);
    }

    @SuppressWarnings("UnusedDeclaration")
    private void added(ServiceReference serviceReference,Object o){
        //Use OBJECTCLASS property from SR as that determines under what classes
        //a given service instance is published
        //Class[] interfaces = o.getClass().getInterfaces();
        String[] interfaces = (String[]) serviceReference.getProperty(Constants.OBJECTCLASS);
        for(String intf : interfaces){
            if(classNameMapping.containsKey(intf)){
                instances.put(classNameMapping.get(intf),o);
            }
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    private void removed(Object o){
        instances.remove(o);
    }

    /**
     * Would be invoked by Felix DM if any dependency gets removed and it has to deregister
     * the OsgiBeanFactory. In that case we clear our cached instances
     */
    private void stop(){
        instances.clear();
    }



    private class DepFinderBeanConfigVisitor implements BeanConfigVisitor {

        public void visit(BeanConfig config) {
            if(FACTORY_TYPE_OSGI.equals(config.getFactoryType())){
                String className = config.getClassName();
                try {
                    Class clazz = getClass().getClassLoader().loadClass(className);
                    classNameMapping.put(className,clazz);
                } catch (ClassNotFoundException e) {
                    log.warn("Could not load class for "+className,e);
                }
            }
        }
    }
}
