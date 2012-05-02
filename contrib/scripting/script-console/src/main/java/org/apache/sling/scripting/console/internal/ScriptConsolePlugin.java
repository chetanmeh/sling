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

package org.apache.sling.scripting.console.internal;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.*;
import org.apache.felix.webconsole.DefaultVariableResolver;
import org.apache.felix.webconsole.SimpleWebConsolePlugin;
import org.apache.felix.webconsole.WebConsoleUtil;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.scripting.ScriptEvaluationException;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.api.scripting.SlingScript;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;
import org.osgi.framework.BundleContext;

/**
 * User: chetanm
 * Date: 4/30/12
 * Time: 9:06 PM
 */
@Component
@Service
@Property(name = "felix.webconsole.label", value = ScriptConsolePlugin.NAME)
public class ScriptConsolePlugin extends SimpleWebConsolePlugin {
    public static final String NAME = "scriptconsole";
    private static final String TITLE = "%script.title";
    private static final String[] CSS = {"/res/ui/codemirror/lib/codemirror.css","/res/ui/script-console.css"};
    private final String TEMPLATE;
    private BundleContext bundleContext;

    @Reference
    private ScriptEngineManager scriptEngineManager;

    public ScriptConsolePlugin() {
        super(NAME, TITLE, processFileNames(CSS));
        TEMPLATE = readTemplateFile("/templates/script-console.html");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void renderContent(HttpServletRequest request,
                                 HttpServletResponse response) throws ServletException, IOException {
        final PrintWriter pw = response.getWriter();
        DefaultVariableResolver varResolver = (DefaultVariableResolver) WebConsoleUtil.getVariableResolver(request);
        varResolver.put("__scriptConfig__",getScriptConfig());
        pw.println(TEMPLATE);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String contentType = getContentType(req);
        resp.setContentType(contentType);
        if (contentType.startsWith("text/")) {
            resp.setCharacterEncoding("UTF-8");
        }

        final SlingBindings bindings = new SlingBindings();
        final PrintWriter pw = resp.getWriter();
        //Populate bindings
        bindings.put(SlingBindings.REQUEST, req);
        bindings.put(SlingBindings.READER, req.getReader());
        bindings.put(SlingBindings.RESPONSE, resp);
        bindings.put(SlingBindings.OUT, pw);

        //Also expose the bundleContext to simplify scripts interaction with the
        //enclosing OSGi container
        bindings.put("bundleContext", bundleContext);

        final String script = WebConsoleUtil.getParameter(req, "code");
        final String lang = WebConsoleUtil.getParameter(req, "lang");
        final Resource resource = new RuntimeScriptResource(lang, script);

        SlingScript slingScript = resource.adaptTo(SlingScript.class);
        try {
            slingScript.eval(bindings);
        } catch (Throwable t){
            pw.println(exceptionToString(t));
        }
    }

    private String getContentType(HttpServletRequest req) {
        String passedContentType = WebConsoleUtil.getParameter(req,"responseContentType");
        if(passedContentType != null){
            return passedContentType;
        }
        return req.getPathInfo().endsWith(".json") ? "application/json" : "text/plain";
    }

    private String exceptionToString(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }


    private static String[] processFileNames(String[] cssFiles) {
        String[] css = new String[cssFiles.length];
        for(int i = 0; i < cssFiles.length; i++){
            css[i] =  '/' + NAME + CSS[i];
        }
        return css;
    }

    private String getScriptConfig() {
        try {
            return getScriptConfig0();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private String getScriptConfig0() throws JSONException {
        StringWriter sw = new StringWriter();
        JSONWriter jw = new JSONWriter(sw);
        jw.setTidy(true);
        jw.array();

        for(ScriptEngineFactory sef : scriptEngineManager.getEngineFactories()){
            jw.object();
            if(sef.getExtensions().isEmpty()){
                continue;
            }
            jw.key("langName").value(sef.getLanguageName());
            jw.key("langCode").value(sef.getExtensions().get(0));

            //Language mode as per CodeMirror names
            String mode = determineMode(sef.getExtensions());
            if(mode != null){
                jw.key("mode").value(mode);
            }

            jw.endObject();
        }

        jw.endArray();
        return sw.toString();
    }

    private String determineMode(List<String> extensions) {
        if(extensions.contains("groovy")){
            return "groovy";
        }else if (extensions.contains("esp")){
            return "javascript";
        }
        return null;
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        super.activate(bundleContext);
        this.bundleContext = bundleContext;
    }

    @Deactivate
    public void deactivate() {
        super.deactivate();
    }


}
