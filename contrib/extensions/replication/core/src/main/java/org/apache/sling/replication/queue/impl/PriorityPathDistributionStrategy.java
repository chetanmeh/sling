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
package org.apache.sling.replication.queue.impl;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.replication.packaging.ReplicationPackage;
import org.apache.sling.replication.queue.ReplicationQueue;
import org.apache.sling.replication.queue.ReplicationQueueDistributionStrategy;
import org.apache.sling.replication.queue.ReplicationQueueException;
import org.apache.sling.replication.queue.ReplicationQueueItem;
import org.apache.sling.replication.queue.ReplicationQueueItemState;
import org.apache.sling.replication.queue.ReplicationQueueItemState.ItemState;
import org.apache.sling.replication.queue.ReplicationQueueProvider;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Distribution algorithm which keeps one specific queue to handle specific paths and another queue
 * for handling all the other paths
 */
public class PriorityPathDistributionStrategy implements ReplicationQueueDistributionStrategy {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final String[] priorityPaths;

    public PriorityPathDistributionStrategy(String[] priorityPaths) {
        this.priorityPaths = priorityPaths;

    }




    private ReplicationQueue getQueue(ReplicationQueueItem replicationPackage, ReplicationQueueProvider queueProvider)
            throws ReplicationQueueException {
        String[] paths = replicationPackage.getPaths();

        log.info("calculating priority for paths {}", Arrays.toString(paths));

        boolean usePriorityQueue = false;
        String pp = null;
        for (String path : paths) {
            for (String priorityPath : priorityPaths) {
                if (path.startsWith(priorityPath)) {
                    usePriorityQueue = true;
                    pp = priorityPath;
                    break;
                }
            }
        }

        ReplicationQueue queue;
        if (usePriorityQueue) {
            log.info("using priority queue for path {}", pp);
            queue = queueProvider.getQueue(pp);
        } else {
            log.info("using default queue");
            queue = queueProvider.getQueue(DEFAULT_QUEUE_NAME);
        }
        return queue;
    }

    public boolean add(ReplicationPackage replicationPackage, ReplicationQueueProvider queueProvider) throws ReplicationQueueException {

        ReplicationQueueItem queueItem = getItem(replicationPackage);
        ReplicationQueue queue = getQueue(queueItem, queueProvider);
        if (queue != null) {
            return queue.add(queueItem);
        } else {
            throw new ReplicationQueueException("could not get a queue");
        }
    }



    public List<String> getQueueNames() {
        List<String> paths = Arrays.asList(priorityPaths);
        paths.add(DEFAULT_QUEUE_NAME);

        return paths;
    }

    private ReplicationQueueItem getItem(ReplicationPackage replicationPackage) {
        ReplicationQueueItem replicationQueueItem = new ReplicationQueueItem(replicationPackage.getId(),
                replicationPackage.getPaths(),
                replicationPackage.getAction(),
                replicationPackage.getType(),
                replicationPackage.getInfo());

        return replicationQueueItem;
    }




}
