
/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ******************************************************************************/

import org.apache.http.*;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.localserver.LocalServerTestBase;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.sling.hapi.client.ClientException;
import org.apache.sling.hapi.client.Document;
import org.apache.sling.hapi.client.microdata.MicrodataHtmlClient;
import org.hamcrest.core.StringContains;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.core.StringContains.containsString;

public class GetPostTest extends LocalServerTestBase {
    public static final String GET_URL = "/test";
    public static final String GET_AUTH_URL = "/testauth";
    public static final String OK_RESPONSE = "TEST_OK";
    public static final String FAIL_RESPONSE = "TEST_FAIL";
    public static final String USER = "admin";
    private static final String PASSWORD = "admin";
    private static final String AUTH_STRING = "Basic YWRtaW46YWRtaW4=";
    private static final String REDIRECT_URL = "/test_redirect";


    private HttpHost host;
    private URI uri;

    @Before
    public void setup() throws Exception {
        super.setUp();
        this.serverBootstrap.registerHandler(GET_URL, new HttpRequestHandler() {
            @Override
            public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext)
                    throws HttpException, IOException {
                httpResponse.setEntity(new StringEntity(OK_RESPONSE));
            }
        }).registerHandler(GET_AUTH_URL, new HttpRequestHandler() {
            @Override
            public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext)
                    throws HttpException, IOException {
                Header[] headers = httpRequest.getHeaders("Authorization");
                if (null == headers || headers.length == 0 || !headers[0].getValue().equals(AUTH_STRING)) {
                    httpResponse.setStatusCode(401);
                    httpResponse.setHeader("WWW-Authenticate",  "Basic realm=\"TEST\"");
                } else {
                    httpResponse.setEntity(new StringEntity(OK_RESPONSE));
                }
            }
        }).registerHandler(REDIRECT_URL, new HttpRequestHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
                response.setStatusCode(307);
                response.setHeader("Location", GET_URL);
            }
        });

        // start server
        this.host = this.start();
        this.uri = URIUtils.rewriteURI(new URI("/"), host);
    }

    @Test
    public void testValidGet() throws ClientException, URISyntaxException {
        MicrodataHtmlClient client = new MicrodataHtmlClient(uri.toString());
        Document doc = client.get(GET_URL);
        Assert.assertThat("GET request failed", doc.toString(), new StringContains(OK_RESPONSE));
    }

    @Test
    public void testValidAuthGet() throws ClientException, URISyntaxException {
        MicrodataHtmlClient client = new MicrodataHtmlClient(uri.toString(), USER, PASSWORD);
        Document doc = client.get(GET_AUTH_URL);
        Assert.assertThat("GET request failed with basic auth", doc.toString(), containsString(OK_RESPONSE));
    }

    @Test
    public void testRedirect() throws ClientException, URISyntaxException, UnsupportedEncodingException {
        MicrodataHtmlClient client = new MicrodataHtmlClient(uri.toString(), USER, PASSWORD);
        Document doc = client.post(REDIRECT_URL, new StringEntity("test"));
        Assert.assertThat("POST request failed to redirect", doc.toString(), containsString(OK_RESPONSE));
    }

}
