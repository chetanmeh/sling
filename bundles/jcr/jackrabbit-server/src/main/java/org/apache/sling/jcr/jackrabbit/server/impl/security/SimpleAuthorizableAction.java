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

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.core.security.user.action.AbstractAuthorizableAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Service
public class SimpleAuthorizableAction extends AbstractAuthorizableAction {
    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void onCreate(User user, String password, Session session) throws RepositoryException {
        log.info("Simple Action :- Created user {}", user.getID());
    }
}
