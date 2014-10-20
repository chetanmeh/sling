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
package org.apache.sling.replication.trigger.impl;

import javax.jcr.observation.Event;

import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.replication.communication.ReplicationRequest;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Testcase for {@link org.apache.sling.replication.trigger.impl.JcrEventReplicationTrigger}
 */
public class JcrEventReplicationTriggerTest {

    @Test
    public void testProcessEventWithoutPathProperty() throws Exception {
        SlingRepository repository = mock(SlingRepository.class);
        String path = "/some/path";
        String serviceName = "serviceId";
        JcrEventReplicationTrigger jcrEventReplicationTrigger = new JcrEventReplicationTrigger(repository, path, serviceName);
        Event event = mock(Event.class);
        ReplicationRequest replicationRequest = jcrEventReplicationTrigger.processEvent(event);
        assertNull(replicationRequest);
    }

    @Test
    public void testProcessEventWithPathProperty() throws Exception {
        SlingRepository repository = mock(SlingRepository.class);
        String path = "/some/path";
        String serviceName = "serviceId";
        JcrEventReplicationTrigger jcrEventReplicationTrigger = new JcrEventReplicationTrigger(repository, path, serviceName);
        Event event = mock(Event.class);
        when(event.getPath()).thenReturn("/some/path/generating/event");
        ReplicationRequest replicationRequest = jcrEventReplicationTrigger.processEvent(event);
        assertNotNull(replicationRequest);
    }
}