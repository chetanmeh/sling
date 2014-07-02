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

package org.apache.sling.scripting.core.impl.helper;

import javax.script.CompiledScript;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;

public class CachedScript {
    private final CompiledScript script;
    private final long lastCompiled = System.currentTimeMillis();

    public CachedScript(CompiledScript script) {
        this.script = script;
    }

    public boolean isStale(Resource scriptResource) {
        ResourceMetadata md = scriptResource.getResourceMetadata();
        long modifiedTime = md.getModificationTime();
        if (modifiedTime > 0 && modifiedTime < lastCompiled) {
            return false;
        }
        //Return true for -1 case i.e. where modifiedTime is unknown
        //as we cannot cache such resource safely
        return true;
    }

    public CompiledScript getScript() {
        return script;
    }
}
