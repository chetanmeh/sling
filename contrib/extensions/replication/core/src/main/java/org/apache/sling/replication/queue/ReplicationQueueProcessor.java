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
package org.apache.sling.replication.queue;

import aQute.bnd.annotation.ConsumerType;

import javax.annotation.Nonnull;

/**
 * Processor of {@link org.apache.sling.replication.queue.ReplicationQueueItem}s
 */
@ConsumerType
public interface ReplicationQueueProcessor {

    /**
     * Process an item from a certain <code>ReplicationQueue</code>
     *
     * @param queueName            the name of the <code>ReplicationQueue</code> to be processed
     * @param replicationQueueItem the <code>ReplicationQueueItem</code> to be processed
     * @return <code>true</code> if the item was successfully processed, <code>false</code> otherwise
     */
    public boolean process(@Nonnull String queueName, @Nonnull ReplicationQueueItem replicationQueueItem);
}
