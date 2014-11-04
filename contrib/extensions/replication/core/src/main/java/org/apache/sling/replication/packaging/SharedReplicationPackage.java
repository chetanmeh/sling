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
package org.apache.sling.replication.packaging;

import aQute.bnd.annotation.ConsumerType;

/**
 * A {@link ReplicationPackage} that offers basic reference counting
 */
@ConsumerType
public interface SharedReplicationPackage extends ReplicationPackage {


    /**
     * acquire a reference to this package and increase the reference count.
     */
    void acquire(String holderName);

    /**
     * release a reference to this package and decrease the reference count.
     * when no more references are hold the package <code>delete</code> method is called.
     */
    void release(String holderName);

}
