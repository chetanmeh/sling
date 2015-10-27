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
package org.apache.sling.spi.resource.provider;

import java.util.Set;

import javax.annotation.Nonnull;

import aQute.bnd.annotation.ProviderType;

/**
 * The provider context...
 */
@ProviderType
public interface ProviderContext {

    /**
     * Get the observation reporter for this instance.
     * @return The observation reporter.
     */
    @Nonnull ObservationReporter getObservationReporter();

    /**
     * Set of paths which are "hidden" by other resource providers.
     * @return A set of paths. The set might be empty
     */
    Set<String> getExcludedPaths();
}
