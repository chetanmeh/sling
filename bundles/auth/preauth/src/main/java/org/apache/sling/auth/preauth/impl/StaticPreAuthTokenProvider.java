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

package org.apache.sling.auth.preauth.impl;

import org.apache.felix.scr.annotations.*;
import org.apache.sling.auth.preauth.PreAuthTokenProvider;
import org.apache.sling.commons.osgi.OsgiUtil;

import java.util.Map;

/**
 * User: chetanm
 * Date: 4/24/12
 * Time: 3:51 PM
 */
@Component(metatype = true, policy = ConfigurationPolicy.REQUIRE, label = "%preauth.name", description = "%preauth.description")
@Service
public class StaticPreAuthTokenProvider implements PreAuthTokenProvider {

    /**
     * The default trusted credentials attribute name.
     */
    private static final String DEFAULT_TRUSTED_CREDENTIALS_ATTRIBUTE = "TrustedInfo";

    @Property(value = DEFAULT_TRUSTED_CREDENTIALS_ATTRIBUTE)
    public static final String PROPERTY_TRUSTED_CREDENTIALS_ATTRIBUTE = "preauth.trustedCredentialsAttribute";

    private String trustedCredentialsAttribute;

    @Activate
    private void activate(Map<String, Object> config){
        this.trustedCredentialsAttribute = OsgiUtil.toString(
                        config.get(PROPERTY_TRUSTED_CREDENTIALS_ATTRIBUTE),
                        DEFAULT_TRUSTED_CREDENTIALS_ATTRIBUTE);
    }

    /**
     * Provides the token which can be passed as part of the <code>AuthenticationInfo</code> to enable
     * pre authentication.
     *
     * @return pre auth token
     */
    public String getPreAuthToken() {
        return trustedCredentialsAttribute;
    }
}
