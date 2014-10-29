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
package org.apache.sling.replication.serialization.impl;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.replication.communication.ReplicationActionType;
import org.apache.sling.replication.communication.ReplicationRequest;
import org.apache.sling.replication.event.ReplicationEventFactory;
import org.apache.sling.replication.event.ReplicationEventType;
import org.apache.sling.replication.packaging.ReplicationPackage;
import org.apache.sling.replication.serialization.ReplicationPackageBuilder;
import org.apache.sling.replication.serialization.ReplicationPackageBuildingException;
import org.apache.sling.replication.serialization.ReplicationPackageReadingException;
import org.apache.sling.replication.util.ReplicationJcrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * base abstract implementation of a JCR based {@link ReplicationPackageBuilder}
 */
public abstract class AbstractReplicationPackageBuilder implements ReplicationPackageBuilder {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final String type;

    private final ReplicationEventFactory replicationEventFactory;

    public AbstractReplicationPackageBuilder(String type, ReplicationEventFactory replicationEventFactory) {
        this.type = type;
        this.replicationEventFactory = replicationEventFactory;
    }

    @CheckForNull
    public ReplicationPackage createPackage(@Nonnull ResourceResolver resourceResolver, @Nonnull ReplicationRequest request)
            throws ReplicationPackageBuildingException {
        ReplicationPackage replicationPackage;
        if (ReplicationActionType.ADD.equals(request.getAction())) {
            replicationPackage = createPackageForAdd(resourceResolver, request);
        } else if (ReplicationActionType.DELETE.equals(request.getAction())) {
            replicationPackage = new VoidReplicationPackage(request, type);
        } else if (ReplicationActionType.POLL.equals(request.getAction())) {
            replicationPackage = new VoidReplicationPackage(request, type);
        } else {
            throw new ReplicationPackageBuildingException("unknown action type "
                    + request.getAction());
        }
        if (replicationPackage != null && replicationEventFactory != null) {
            Dictionary<String, Object> dictionary = new Hashtable<String, Object>();
            dictionary.put("replication.action", replicationPackage.getAction());
            dictionary.put("replication.path", replicationPackage.getPaths());
            replicationEventFactory.generateEvent(ReplicationEventType.PACKAGE_CREATED, dictionary);
        }
        return replicationPackage;
    }

    @CheckForNull
    public ReplicationPackage readPackage(@Nonnull ResourceResolver resourceResolver, @Nonnull InputStream stream) throws ReplicationPackageReadingException {
        ReplicationPackage replicationPackage = null;
        if (!stream.markSupported()) {
            stream = new BufferedInputStream(stream);
        }
        try {
            stream.mark(6);
            byte[] buffer = new byte[6];
            int bytesRead = stream.read(buffer, 0, 6);
            stream.reset();
            String s = new String(buffer, "UTF-8");
            log.info("read {} bytes as {}", bytesRead, s);

            if (bytesRead > 0 && buffer[0] > 0 && s.startsWith("DEL")) {
                replicationPackage = VoidReplicationPackage.fromStream(stream);
            }
        } catch (Exception e) {
            log.warn("cannot parse stream", e);
        }
        stream.mark(-1);
        if (replicationPackage == null) {
            replicationPackage = readPackageInternal(resourceResolver, stream);
        }
        return replicationPackage;
    }

    public boolean installPackage(@Nonnull ResourceResolver resourceResolver, @Nonnull ReplicationPackage replicationPackage) throws ReplicationPackageReadingException {
        ReplicationActionType actionType = ReplicationActionType.fromName(replicationPackage.getAction());
        boolean installed;
        if (ReplicationActionType.DELETE.equals(actionType)) {
            installed = installDeletePackage(resourceResolver, replicationPackage);
        } else {
            installed = installPackageInternal(resourceResolver, replicationPackage);
        }

        if (installed && replicationEventFactory != null) {
            Dictionary<String, Object> dictionary = new Hashtable<String, Object>();
            dictionary.put("replication.action", replicationPackage.getAction());
            dictionary.put("replication.path", replicationPackage.getPaths());
            replicationEventFactory.generateEvent(ReplicationEventType.PACKAGE_INSTALLED, dictionary);
        }

        return installed;
    }

    private boolean installDeletePackage(ResourceResolver resourceResolver, ReplicationPackage replicationPackage) throws ReplicationPackageReadingException {
        Session session = null;
        try {
            if (replicationPackage != null) {
                session = getSession(resourceResolver);
                for (String path : replicationPackage.getPaths()) {
                    if (session.itemExists(path)) {
                        session.removeItem(path);
                    }
                }
                session.save();
                return true;
            }
        } catch (Exception e) {
            throw new ReplicationPackageReadingException(e);
        } finally {
            ungetSession(session);
        }

        return false;
    }

    public ReplicationPackage getPackage(@Nonnull ResourceResolver resourceResolver, @Nonnull String id) {
        ReplicationPackage replicationPackage = null;
        try {
            replicationPackage = VoidReplicationPackage.fromStream(new ByteArrayInputStream(id.getBytes("UTF-8")));
        } catch (IOException ex) {
            // not a void package
        }

        if (replicationPackage == null) {
            replicationPackage = getPackageInternal(resourceResolver, id);
        }
        return replicationPackage;
    }

    protected Session getSession(ResourceResolver resourceResolver) throws RepositoryException {
        Session session = resourceResolver.adaptTo(Session.class);
        if (session != null) {
            ReplicationJcrUtils.setDoNotReplicate(session);
        } else {
            throw new RepositoryException("could not obtain a session from calling user " + resourceResolver.getUserID());
        }
        return session;
    }

    protected void ungetSession(Session session) {
       if (session != null) {
           try {
               session.save();
           } catch (RepositoryException e) {
               log.debug("Cannot save session", e);
           }
       }
    }

    protected abstract ReplicationPackage createPackageForAdd(ResourceResolver resourceResolver, ReplicationRequest request)
            throws ReplicationPackageBuildingException;


    protected abstract ReplicationPackage readPackageInternal(ResourceResolver resourceResolver, InputStream stream)
            throws ReplicationPackageReadingException;


    protected abstract boolean installPackageInternal(ResourceResolver resourceResolver, ReplicationPackage replicationPackage)
            throws ReplicationPackageReadingException;

    protected abstract ReplicationPackage getPackageInternal(ResourceResolver resourceResolver, String id);

}
