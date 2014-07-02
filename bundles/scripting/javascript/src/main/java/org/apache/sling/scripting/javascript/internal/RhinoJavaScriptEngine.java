/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sling.scripting.javascript.internal;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.scripting.api.AbstractSlingScriptEngine;
import org.apache.sling.scripting.api.ScriptNameAware;
import org.apache.sling.scripting.javascript.io.EspReader;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.WrapFactory;
import org.mozilla.javascript.Wrapper;
import org.slf4j.Logger;

/**
 * A ScriptEngine that uses the Rhino interpreter to process Sling requests with
 * server-side javascript.
 */
public class RhinoJavaScriptEngine extends AbstractSlingScriptEngine implements Compilable{

    private Scriptable rootScope;

    public RhinoJavaScriptEngine(ScriptEngineFactory factory,
            Scriptable rootScope) {
        super(factory);
        this.rootScope = rootScope;
    }

    public CompiledScript compile(String script) throws ScriptException {
        throw new UnsupportedOperationException("Cannot compile script referred via path ");
    }

    public CompiledScript compile(Reader scriptReader) throws ScriptException {
        String scriptName = getScriptName(scriptReader);
        scriptReader = wrapReaderIfEspScript(scriptReader, scriptName);
        try {
            final Context rhinoContext = Context.enter();
            rhinoContext.setOptimizationLevel(optimizationLevel());

            if (!ScriptRuntime.hasTopCall(rhinoContext)) {
                // setup the context for use
                WrapFactory wrapFactory = ((RhinoJavaScriptEngineFactory) getFactory()).getWrapFactory();
                rhinoContext.setWrapFactory(wrapFactory);
            }

            final int lineNumber = 1;
            final Object securityDomain = null;

            Script script = rhinoContext.compileReader(scriptReader, scriptName, lineNumber, securityDomain);
            return new SlingCompiledScript(script, scriptName);
        } catch (IOException e) {
            final ScriptException se = new ScriptException(
                    "Failure running script " + scriptName + ": " + e.getMessage());
            se.initCause(e);
            throw se;
        } finally {
            Context.exit();
        }
    }

    public Object eval(Reader scriptReader, ScriptContext scriptContext)
            throws ScriptException {
        Bindings bindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
        String scriptName = "NO_SCRIPT_NAME";
        {
            SlingScriptHelper helper = (SlingScriptHelper) bindings.get(SlingBindings.SLING);
            if (helper != null) {
                scriptName = helper.getScript().getScriptResource().getPath();
            }
        }
        scriptReader = wrapReaderIfEspScript(scriptReader, scriptName);
        return compile(scriptReader).eval(scriptContext);
    }

    private Reader wrapReaderIfEspScript(Reader scriptReader, String scriptName) {
        // wrap the reader in an EspReader for ESP scripts
        if (scriptName.endsWith(RhinoJavaScriptEngineFactory.ESP_SCRIPT_EXTENSION)) {
            scriptReader = new EspReader(scriptReader);
        }
        return scriptReader;
    }

    private class SlingCompiledScript extends CompiledScript {
        private final Script script;
        private final String scriptName;

        private SlingCompiledScript(Script script, String scriptName) {
            this.script = script;
            this.scriptName = scriptName;
        }

        @Override
        public Object eval(ScriptContext scriptContext) throws ScriptException {
            Bindings bindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
            // container for replaced properties
            Map<String, Object> replacedProperties = null;
            Scriptable scope = null;
            boolean isTopLevelCall = false;

            // create a rhino Context and execute the script
            try {

                final Context rhinoContext = Context.enter();
                rhinoContext.setOptimizationLevel(optimizationLevel());

                if (ScriptRuntime.hasTopCall(rhinoContext)) {
                    // reuse the top scope if we are included
                    scope = ScriptRuntime.getTopCallScope(rhinoContext);

                } else {
                    // create the request top scope, use the ImporterToplevel here
                    // to support the importPackage and importClasses functions
                    scope = new ImporterTopLevel();

                    // Set the global scope to be our prototype
                    scope.setPrototype(rootScope);

                    // We want "scope" to be a new top-level scope, so set its
                    // parent scope to null. This means that any variables created
                    // by assignments will be properties of "scope".
                    scope.setParentScope(null);

                    // setup the context for use
                    WrapFactory wrapFactory = ((RhinoJavaScriptEngineFactory) getFactory()).getWrapFactory();
                    rhinoContext.setWrapFactory(wrapFactory);

                    // this is the top level call
                    isTopLevelCall = true;
                }

                // add initial properties to the scope
                replacedProperties = setBoundProperties(scope, bindings);

                Object result = script.exec(rhinoContext, scope);

                if (result instanceof Wrapper) {
                    result = ((Wrapper) result).unwrap();
                }

                return (result instanceof Undefined) ? null : result;

            } catch (JavaScriptException t) {

                // prevent variables to be pushed back in case of errors
                isTopLevelCall = false;

                final ScriptException se = new ScriptException(t.details(),
                        t.sourceName(), t.lineNumber());

                // log the script stack trace
                ((Logger) bindings.get(SlingBindings.LOG)).error(t.getScriptStackTrace());

                // set the exception cause
                Object value = t.getValue();
                if (value != null) {
                    if (value instanceof Wrapper) {
                        value = ((Wrapper) value).unwrap();
                    }
                    if (value instanceof Throwable) {
                        se.initCause((Throwable) value);
                    }
                }

                // if the cause could not be set, overwrite the stack trace
                if (se.getCause() == null) {
                    se.setStackTrace(t.getStackTrace());
                }

                throw se;

            } catch (Throwable t) {

                // prevent variables to be pushed back in case of errors
                isTopLevelCall = false;

                final ScriptException se = new ScriptException(
                        "Failure running script " + scriptName + ": " + t.getMessage());
                se.initCause(t);
                throw se;

            } finally {

                // if we are the top call (the Context is now null) we have to
                // play back any properties from the scope back to the bindings
                if (isTopLevelCall) {
                    getBoundProperties(scope, bindings);
                }

                // if properties have been replaced, reset them
                resetBoundProperties(scope, replacedProperties);

                Context.exit();
            }
        }

        @Override
        public ScriptEngine getEngine() {
            return RhinoJavaScriptEngine.this;
        }
    }

    private String getScriptName(Reader scriptReader) {
        if(scriptReader instanceof ScriptNameAware){
            return ((ScriptNameAware) scriptReader).getScriptName();
        }
        return "NO_SCRIPT_NAME";
    }

    private Map<String, Object> setBoundProperties(Scriptable scope,
            Bindings bindings) {
        Map<String, Object> replacedProperties = new HashMap<String, Object>();

        for (Object entryObject : bindings.entrySet()) {
            Entry<?, ?> entry = (Entry<?, ?>) entryObject;
            String name = (String) entry.getKey();
            Object value = entry.getValue();

            if (value != null) {
                // get the current property value, if set
                if (ScriptableObject.hasProperty(scope, name)) {
                    replacedProperties.put(name, ScriptableObject.getProperty(
                        scope, name));
                }

                // wrap the new value and set it
                Object wrapped = ScriptRuntime.toObject(scope, value);
                ScriptableObject.putProperty(scope, name, wrapped);
            }
        }

        return replacedProperties;
    }

    private void getBoundProperties(Scriptable scope, Bindings bindings) {
        Object[] ids = scope.getIds();
        for (Object id : ids) {
            if (id instanceof String) {
                String key = (String) id;
                Object value = scope.get(key, scope);
                if (value != Scriptable.NOT_FOUND) {
                    if (value instanceof Wrapper) {
                        bindings.put(key, ((Wrapper) value).unwrap());
                    } else {
                        bindings.put(key, value);
                    }
                }
            }
        }
    }

    private void resetBoundProperties(Scriptable scope,
            Map<String, Object> properties) {
        if (scope != null && properties != null && properties.size() > 0) {
            for (Entry<String, Object> entry : properties.entrySet()) {
                ScriptableObject.putProperty(scope, entry.getKey(),
                    entry.getValue());
            }
        }
    }

    private int optimizationLevel() {
        return ((RhinoJavaScriptEngineFactory)getFactory()).getOptimizationLevel();
    }
}
