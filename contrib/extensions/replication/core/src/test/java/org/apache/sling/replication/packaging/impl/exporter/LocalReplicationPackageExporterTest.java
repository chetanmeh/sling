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
package org.apache.sling.replication.packaging.impl.exporter;

import java.util.List;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.replication.communication.ReplicationRequest;
import org.apache.sling.replication.packaging.ReplicationPackage;
import org.apache.sling.replication.serialization.ReplicationPackageBuilder;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Testcase for {@link org.apache.sling.replication.packaging.impl.exporter.LocalReplicationPackageExporter}
 */
public class LocalReplicationPackageExporterTest {

    @Test
    public void testDummyExport() throws Exception {
        ReplicationPackageBuilder packageBuilder = mock(ReplicationPackageBuilder.class);
        LocalReplicationPackageExporter localReplicationPackageExporter = new LocalReplicationPackageExporter(packageBuilder);
        ResourceResolver resourceResolver = mock(ResourceResolver.class);
        ReplicationRequest replicationRequest = mock(ReplicationRequest.class);
        List<ReplicationPackage> replicationPackages = localReplicationPackageExporter.exportPackages(resourceResolver, replicationRequest);
        assertNotNull(replicationPackages);
        assertTrue(replicationPackages.isEmpty());
    }
}
