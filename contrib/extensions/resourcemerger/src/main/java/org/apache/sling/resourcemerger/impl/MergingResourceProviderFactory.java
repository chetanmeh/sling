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
package org.apache.sling.resourcemerger.impl;

import java.util.Map;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceProvider;
import org.apache.sling.api.resource.ResourceProviderFactory;
import org.apache.sling.resourcemerger.spi.MergedResourcePicker;

class MergingResourceProviderFactory implements ResourceProviderFactory {

    private final String mergeRootPath;

    private final MergedResourcePicker picker;

    MergingResourceProviderFactory(String mergeRootPath, MergedResourcePicker picker) {
        this.mergeRootPath = mergeRootPath;
        this.picker = picker;
    }

    public ResourceProvider getResourceProvider(Map<String, Object> authenticationInfo) throws LoginException {
        return new MergingResourceProvider(mergeRootPath, picker);
    }

    public ResourceProvider getAdministrativeResourceProvider(Map<String, Object> authenticationInfo)
            throws LoginException {
        return new MergingResourceProvider(mergeRootPath, picker);
    }

}
