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
package org.apache.sling.models.factory;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;


/**
 * Exception which is triggered whenever a Sling Model cannot be instantiated
 * due to some missing elements (i.e. required fields/methods/constructor params
 * could not be injected).
 * 
 * @see ModelFactory
 *
 */
public class MissingElementsException extends RuntimeException {
    private static final long serialVersionUID = 7870762030809272254L;
    
    private final Collection<? extends AnnotatedElement> missingElements;

    private String formatString;

    private Class<?> type;
    
    public MissingElementsException(String format, Collection<? extends AnnotatedElement> elements, Class<?> type) {
        super();
        this.formatString = format;
        this.missingElements = elements;
        this.type = type;
    }
    
    @Override
    public String getMessage() {
        return String.format(formatString, missingElements, type);
    }
    
    public Class<?> getType() {
        return type;
    }
    
    public Collection<? extends AnnotatedElement> getMissingElements() {
        return missingElements;
    }
}
