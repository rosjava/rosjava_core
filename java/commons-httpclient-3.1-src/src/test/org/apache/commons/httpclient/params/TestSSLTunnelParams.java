/*
 * $Header$
 * $Revision: 515317 $
 * $Date: 2007-03-06 22:41:04 +0100 (Tue, 06 Mar 2007) $
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

package org.apache.commons.httpclient.params;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.FeedbackService;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClientTestBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.server.HttpRequestHandler;
import org.apache.commons.httpclient.server.SimpleHttpServerConnection;
import org.apache.commons.httpclient.server.SimpleRequest;
import org.apache.commons.httpclient.server.SimpleResponse;

/**
 * Tunnelling proxy configuration.
 *
 * @author Oleg Kalnichevski
 * 
 * @version $Id: TestSSLTunnelParams.java 515317 2007-03-06 21:41:04Z sebb $
 */
public class TestSSLTunnelParams extends HttpClientTestBase {

    // ------------------------------------------------------------ Constructor
    public TestSSLTunnelParams(final String testName) throws IOException {
        super(testName);
        setUseProxy(true);
        setUseSSL(true);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestSSLTunnelParams.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        TestSuite suite = new TestSuite(TestSSLTunnelParams.class);
        return suite;
    }

    
    static class HttpVersionHandler implements HttpRequestHandler {
        
        public HttpVersionHandler() {
            super();
        }

        public boolean processRequest(
                final SimpleHttpServerConnection conn,
                final SimpleRequest request) throws IOException
            {
                HttpVersion ver = request.getRequestLine().getHttpVersion();
                if (ver.equals(HttpVersion.HTTP_1_0)) {
                    return false;
                } else {
                    SimpleResponse response = new SimpleResponse();
                    response.setStatusLine(ver, HttpStatus.SC_HTTP_VERSION_NOT_SUPPORTED);
                    response.addHeader(new Header("Proxy-Connection", "close"));
                    conn.setKeepAlive(false);
                    // Make sure the request body is fully consumed
                    request.getBodyBytes();
                    conn.writeResponse(response);
                    return true;
                }
            }
        
    }
   
    /**
     * Tests correct proparation of HTTP params from the client to
     * CONNECT method.
     */
    public void testTunnellingParamsAgentLevel() throws IOException {
        this.proxy.addHandler(new HttpVersionHandler());
        this.server.setHttpService(new FeedbackService());

        this.client.getParams().setVersion(HttpVersion.HTTP_1_1);
        GetMethod httpget = new GetMethod("/test/");
        try {
            this.client.executeMethod(httpget);
            assertNotNull(httpget.getStatusLine());
            assertEquals(HttpStatus.SC_HTTP_VERSION_NOT_SUPPORTED, 
                    httpget.getStatusLine().getStatusCode());
        } finally {
            httpget.releaseConnection();
        }

        this.client.getParams().setVersion(HttpVersion.HTTP_1_0);
        httpget = new GetMethod("/test/");
        try {
            this.client.executeMethod(httpget);
            assertNotNull(httpget.getStatusLine());
            assertEquals(HttpStatus.SC_OK, 
                    httpget.getStatusLine().getStatusCode());
        } finally {
            httpget.releaseConnection();
        }
    }

    /**
     * Tests correct proparation of HTTP params from the host config to
     * CONNECT method.
     */
    public void testTunnellingParamsHostLevel() throws IOException {
        this.proxy.addHandler(new HttpVersionHandler());
        this.server.setHttpService(new FeedbackService());

        this.client.getHostConfiguration().getParams().setParameter(
                HttpMethodParams.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        GetMethod httpget = new GetMethod("/test/");
        try {
            this.client.executeMethod(httpget);
            assertNotNull(httpget.getStatusLine());
            assertEquals(HttpStatus.SC_HTTP_VERSION_NOT_SUPPORTED, 
                    httpget.getStatusLine().getStatusCode());
        } finally {
            httpget.releaseConnection();
        }

        this.client.getHostConfiguration().getParams().setParameter(
                HttpMethodParams.PROTOCOL_VERSION, HttpVersion.HTTP_1_0);
        httpget = new GetMethod("/test/");
        try {
            this.client.executeMethod(httpget);
            assertNotNull(httpget.getStatusLine());
            assertEquals(HttpStatus.SC_OK, 
                    httpget.getStatusLine().getStatusCode());
        } finally {
            httpget.releaseConnection();
        }
    }

    /**
     * Tests ability to use HTTP/1.0 to execute CONNECT method and HTTP/1.1 to
     * execute methods once the tunnel is established.
     */
    public void testTunnellingParamsHostHTTP10AndMethodHTTP11() throws IOException {
        this.proxy.addHandler(new HttpVersionHandler());
        this.server.setHttpService(new FeedbackService());

        this.client.getHostConfiguration().getParams().setParameter(
                HttpMethodParams.PROTOCOL_VERSION, HttpVersion.HTTP_1_0);
        GetMethod httpget = new GetMethod("/test/");
        httpget.getParams().setVersion(HttpVersion.HTTP_1_1);
        try {
            this.client.executeMethod(httpget);
            assertNotNull(httpget.getStatusLine());
            assertEquals(HttpStatus.SC_OK, 
                    httpget.getStatusLine().getStatusCode());
            assertEquals(HttpVersion.HTTP_1_1, 
                    httpget.getEffectiveVersion());
        } finally {
            httpget.releaseConnection();
        }
    }
}
