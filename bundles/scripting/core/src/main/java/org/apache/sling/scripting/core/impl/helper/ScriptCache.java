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

import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.sling.api.resource.Resource;

public class ScriptCache {
    private final LinkedHashMap<String, SoftReference<CachedScript>> map;

    public ScriptCache(final int cacheSize) {
        if (cacheSize < 1)
            throw new IllegalArgumentException("cache size must be greater than 0");

        map = new LinkedHashMap<String, SoftReference<CachedScript>>() {
            private static final long serialVersionUID = 5857390063785416719L;

            @Override
            protected boolean removeEldestEntry(Map.Entry<String, SoftReference<CachedScript>> eldest) {
                return size() > cacheSize;
            }
        };
    }

    public synchronized CachedScript put(String key, CachedScript value) {
        SoftReference<CachedScript> previousValueReference = map.put(key, new SoftReference<CachedScript>(value));
        return previousValueReference != null ? previousValueReference.get() : null;
    }

    public synchronized CachedScript get(String key) {
        SoftReference<CachedScript> valueReference = map.get(key);
        return valueReference != null ? valueReference.get() : null;
    }

    public synchronized void remove(String key) {
        map.remove(key);
    }

    public boolean isCacheable(Resource res) {
        return res.getResourceMetadata().getModificationTime() > 0;
    }
}
