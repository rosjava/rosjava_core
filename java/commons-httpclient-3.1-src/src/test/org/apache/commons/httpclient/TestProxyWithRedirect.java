/*
 * $HeadURL: https://svn.apache.org/repos/asf/jakarta/httpcomponents/oac.hc3x/tags/HTTPCLIENT_3_1/src/test/org/apache/commons/httpclient/TestProxyWithRedirect.java $
 * $Revision: 480424 $
 * $Date: 2006-11-29 06:56:49 +0100 (Wed, 29 Nov 2006) $
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.commons.httpclient;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.server.RequestLine;
import org.apache.commons.httpclient.server.SimpleRequest;
import org.apache.commons.httpclient.server.SimpleResponse;

/**
 * Tests for proxied connections.
 * 
 * @author Ortwin Glueck
 * @author Oleg Kalnichevski
 */
public class TestProxyWithRedirect extends HttpClientTestBase {

    public TestProxyWithRedirect(String testName) throws IOException {
        super(testName);
        setUseProxy(true);
    }

    public static Test suite() {
        return new TestSuite(TestProxyWithRedirect.class);
    }
    
    private class BasicRedirectService extends EchoService {
        
        private String location = null;

        public BasicRedirectService(final String location) {
            super();
            this.location = location;
        }

        public boolean process(final SimpleRequest request, final SimpleResponse response)
            throws IOException
        {
            RequestLine reqline = request.getRequestLine();
            HttpVersion ver = reqline.getHttpVersion();
            if (reqline.getUri().equals("/redirect/")) {
                response.setStatusLine(ver, HttpStatus.SC_MOVED_TEMPORARILY);
                response.addHeader(new Header("Location", this.location));
                response.addHeader(new Header("Connection", "Close"));
                return true;
            } else {
                return super.process(request, response);
            }
        }
    }
    
    public void testAuthProxyWithRedirect() throws Exception {
        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        this.client.getState().setProxyCredentials(AuthScope.ANY, creds);
        this.server.setHttpService(new BasicRedirectService("/"));
        this.proxy.requireAuthentication(creds, "test", true);
        
        GetMethod get = new GetMethod("/redirect/");
        try {
            this.client.executeMethod(get);
            assertEquals(HttpStatus.SC_OK, get.getStatusCode());
        } finally {
            get.releaseConnection();
        }
    }
    
    public void testAuthProxyWithCrossSiteRedirect() throws Exception {
        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        this.client.getState().setProxyCredentials(AuthScope.ANY, creds);
        this.server.setHttpService(new BasicRedirectService(
                "http://127.0.0.1:" + this.server.getLocalPort()));

        this.proxy.requireAuthentication(creds, "test", true);
        
        GetMethod get = new GetMethod("/redirect/");
        try {
            this.client.executeMethod(get);
            assertEquals(HttpStatus.SC_OK, get.getStatusCode());
        } finally {
            get.releaseConnection();
        }
    }

    public void testPreemptiveAuthProxyWithCrossSiteRedirect() throws Exception {
        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        this.client.getState().setProxyCredentials(AuthScope.ANY, creds);
        this.client.getParams().setAuthenticationPreemptive(true);
        this.server.setHttpService(new BasicRedirectService(
                "http://127.0.0.1:" + this.server.getLocalPort()));

        this.proxy.requireAuthentication(creds, "test", true);
        
        GetMethod get = new GetMethod("/redirect/");
        try {
            this.client.executeMethod(get);
            assertEquals(HttpStatus.SC_OK, get.getStatusCode());
        } finally {
            get.releaseConnection();
        }
    }
    
}
