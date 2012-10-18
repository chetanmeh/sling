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
package org.apache.sling.jcr.jackrabbit.server.impl.security;

import java.util.Map;
import java.util.TreeMap;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.core.security.user.action.AbstractAuthorizableAction;
import org.apache.jackrabbit.core.security.user.action.AuthorizableAction;
import org.apache.sling.commons.osgi.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
@Service(value = AuthorizableAction.class)
@Reference(name = "AuthorizableAction",
        policy = ReferencePolicy.DYNAMIC,
        cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE,
        referenceInterface = AuthorizableAction.class,
        target = "(!(jackrabbit.extension=true))" //Do include this instance
)
@Property(name = "jackrabbit.extension",value="true")
public class MultiplexingAuthorizableAction extends AbstractAuthorizableAction{
    private Logger log = LoggerFactory.getLogger(getClass());

    private Map<Comparable<Object>,AuthorizableAction> actionMap = new TreeMap<Comparable<Object>, AuthorizableAction>();
    private AuthorizableAction[] actions = new AuthorizableAction[0];

    public void onCreate(User user, String password, Session session) throws RepositoryException {
        log.info("Created user {}", user.getID());
        for(AuthorizableAction a : getActions()){
            a.onCreate(user,password,session);
        }
    }

    @Override
    public void onCreate(Group group, Session session) throws RepositoryException {
        log.info("Created group {}", group.getID());
        for(AuthorizableAction a : getActions()){
            a.onCreate(group,session);
        }
    }

    @Override
    public void onRemove(Authorizable authorizable, Session session) throws RepositoryException {
        log.info("Removed authorizable {}", authorizable.getID());
        for(AuthorizableAction a : getActions()){
            a.onRemove(authorizable,session);
        }
    }

    @Override
    public void onPasswordChange(User user, String newPassword, Session session) throws RepositoryException {
        log.info("Password changed for user {}", user.getID());
        for(AuthorizableAction a : getActions()){
            a.onPasswordChange(user,newPassword,session);
        }
    }

    @Deactivate
    private void deactivate(){
        actionMap.clear();
    }

    private AuthorizableAction[] getActions() {
        return actions;
    }

    private void bindAuthorizableAction(AuthorizableAction action,Map<String,Object> config){
        actionMap.put(ServiceUtil.getComparableForServiceRanking(config),action);
        recreateActionArray();
    }

    private void unbindAuthorizableAction(AuthorizableAction action,Map<String,Object> config){
        actionMap.remove(ServiceUtil.getComparableForServiceRanking(config));
        recreateActionArray();
    }

    private void recreateActionArray(){
        AuthorizableAction[] temp = actionMap.values().toArray(new AuthorizableAction[actionMap.size()]);
        synchronized (this){
            actions = temp;
        }
    }

}
