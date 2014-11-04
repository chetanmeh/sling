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
package org.apache.sling.replication.monitor;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyUnbounded;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.References;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.hc.api.HealthCheck;
import org.apache.sling.hc.api.Result;
import org.apache.sling.hc.util.FormattingResultLog;
import org.apache.sling.replication.agent.ReplicationAgent;
import org.apache.sling.replication.queue.ReplicationQueue;
import org.apache.sling.replication.queue.ReplicationQueueItem;
import org.apache.sling.replication.queue.ReplicationQueueItemState;
import org.apache.sling.replication.queue.ReplicationQueueProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link HealthCheck} that checks if replication queues' first item has been retried more than a configurable amount
 * of times
 */
@Component(immediate = true,
        metatype = true,
        label = "Apache Sling Replication Queue Health Check")
@Properties({
        @Property(name = HealthCheck.NAME, value = "SlingReplicationQueueHC", description = "Health Check name", label = "Name"),
        @Property(name = HealthCheck.TAGS, unbounded = PropertyUnbounded.ARRAY, description = "Health Check tags", label = "Tags"),
        @Property(name = HealthCheck.MBEAN_NAME, value = "slingReplicationQueue", description = "Health Check MBean name", label = "MBean name")
})
@References({
        @Reference(name = "replicationAgent",
                referenceInterface = ReplicationAgent.class,
                cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE,
                policy = ReferencePolicy.DYNAMIC)
})

@Service(value = HealthCheck.class)
public class ReplicationQueueHealthCheck implements HealthCheck {

    private static final Logger log = LoggerFactory.getLogger(ReplicationQueueHealthCheck.class);

    private static final int DEFAULT_NUMBER_OF_RETRIES_ALLOWED = 3;

    private int numberOfRetriesAllowed;

    @Property(intValue = DEFAULT_NUMBER_OF_RETRIES_ALLOWED, description = "Number of allowed retries", label = "Allowed retries")
    private static final String NUMBER_OF_RETRIES_ALLOWED = "numberOfRetriesAllowed";

    private final List<ReplicationAgent> replicationAgents = new CopyOnWriteArrayList<ReplicationAgent>();

    @Activate
    public void activate(final Map<String, Object> properties) {
        numberOfRetriesAllowed = PropertiesUtil.toInteger(properties.get(NUMBER_OF_RETRIES_ALLOWED), DEFAULT_NUMBER_OF_RETRIES_ALLOWED);
        log.info("Activated, numberOfRetriesAllowed={}", numberOfRetriesAllowed);
    }

    @Deactivate
    protected void deactivate() {
        replicationAgents.clear();
    }

    protected void bindReplicationAgent(final ReplicationAgent replicationAgent) {
        replicationAgents.add(replicationAgent);

        log.debug("Registering replication agent {} ", replicationAgent);
    }

    protected void unbindReplicationAgent(final ReplicationAgent replicationAgent) {
        replicationAgents.remove(replicationAgent);
        log.debug("Unregistering replication agent {} ", replicationAgent);
    }

    public Result execute() {
        final FormattingResultLog resultLog = new FormattingResultLog();
        Map<String, Integer> failures = new HashMap<String, Integer>();
        if (replicationAgents.size() > 0) {

            for (ReplicationAgent replicationAgent : replicationAgents) {
                for (String queueName : replicationAgent.getQueueNames()) {
                    try {
                        ReplicationQueue q = replicationAgent.getQueue(queueName);

                        ReplicationQueueItem item = q.getHead();
                        if (item != null) {
                            ReplicationQueueItemState status = q.getStatus(item);
                            if (status.getAttempts() <= numberOfRetriesAllowed) {
                                resultLog.debug("Queue: [{}], first item: [{}], number of retries: {}", q.getName(), item.getId(), status.getAttempts());
                            } else {
                                // the no. of attempts is higher than the configured threshold
                                resultLog.warn("Queue: [{}], first item: [{}], number of retries: {}, expected number of retries <= {}",
                                        q.getName(), item.getId(), status.getAttempts(), numberOfRetriesAllowed);
                                failures.put(q.getName(), status.getAttempts());
                            }
                        } else {
                            resultLog.debug("No items in queue [{}]", q.getName());
                        }

                    } catch (Exception e) {
                        resultLog.warn("Exception while inspecting replication queue [{}]: {}", queueName, e);
                    }
                }
            }
        } else {
            resultLog.debug("No replication queue providers found");
        }

        if (failures.size() > 0) {
            // a specific log entry (using markdown) to provide a recommended user action
            for (Map.Entry<String, Integer> entry : failures.entrySet()) {
                resultLog.warn("Replication queue {}'s first item in the default queue has been retried {} times (threshold: {})",
                        entry.getKey(), entry.getValue(), numberOfRetriesAllowed);
            }
        }

        return new Result(resultLog);
    }

}