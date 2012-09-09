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

package org.apache.sling.auth.form.impl;

import org.apache.jackrabbit.core.security.authentication.Authentication;
import org.apache.sling.jcr.jackrabbit.server.security.SlingDefaultLoginModule;

import javax.jcr.Credentials;
import javax.jcr.RepositoryException;
import java.security.Principal;

/**
 * User: chetanm
 * Date: 9/9/12
 * Time: 1:04 PM
 */
final class FormLoginModule extends SlingDefaultLoginModule {

    /**
     * The {@link FormAuthenticationHandler} used to validate the credentials
     * and its contents.
     */
    private final FormAuthenticationHandler authHandler;

    FormLoginModule(FormAuthenticationHandler authHandler) {
        this.authHandler = authHandler;
    }

    @Override
    protected Authentication getAuthentication(Principal principal, Credentials creds) throws RepositoryException {
        if(canHandle(creds)){
            return new Authentication() {
                public boolean canHandle(Credentials credentials) {
                    return FormLoginModule.this.canHandle(credentials);
                }

                public boolean authenticate(Credentials credentials) throws RepositoryException {
                    return authHandler.isValid(credentials);
                }
            };
        }
        return super.getAuthentication(principal, creds);
    }

    /**
     * Returns <code>true</code> indicating support if the credentials is a
     * <code>SimplerCredentials</code> object and has an authentication data
     * attribute.
     *
     * @see CookieAuthenticationHandler#hasAuthData(Credentials)
     */
    public boolean canHandle(Credentials credentials) {
        return authHandler.hasAuthData(credentials);
    }
}
