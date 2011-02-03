/*
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
 * [Additional notices, if required by prior licensing conditions]
 *
 */
package org.apache.commons.httpclient;

import java.io.IOException;

import junit.framework.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.server.HttpRequestHandler;
import org.apache.commons.httpclient.server.ResponseWriter;
import org.apache.commons.httpclient.server.SimpleHttpServerConnection;
import org.apache.commons.httpclient.server.SimpleRequest;

/**
 * Tests handling of non-compliant responses.
 * 
 * @author Oleg Kalnichevski
 * @author Jeff Dever
 */
public class TestNoncompliant extends HttpClientTestBase {

    public TestNoncompliant(String s) throws IOException {
        super(s);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestNoncompliant.class);
        return suite;
    }

    /**
     * Tests if client is able to recover gracefully when HTTP server or
     * proxy fails to send 100 status code when expected. The client should
     * resume sending the request body after a defined timeout without having
     * received "continue" code.
     */
    public void testNoncompliantPostMethodString() throws Exception {
        this.server.setRequestHandler(new HttpRequestHandler() {
            public boolean processRequest(SimpleHttpServerConnection conn,
                    SimpleRequest request) throws IOException {
                ResponseWriter out = conn.getWriter();
                out.println("HTTP/1.1 200 OK");
                out.println("Connection: close");
                out.println("Content-Length: 0");
                out.println();
                out.flush();
                return true;
            }
        });

        PostMethod method = new PostMethod("/");
        method.getParams().setBooleanParameter(
                HttpMethodParams.USE_EXPECT_CONTINUE, true);
        method.setRequestEntity(new StringRequestEntity(
                "This is data to be sent in the body of an HTTP POST.", null, null));
        client.executeMethod(method);
        assertEquals(200, method.getStatusCode());
    }

    /**
     * Tests that a response status line containing \r and \n is handled.
     */
    public void testNoncompliantStatusLine() {
        this.server.setRequestHandler(new HttpRequestHandler() {
            public boolean processRequest(SimpleHttpServerConnection conn,
                    SimpleRequest request) throws IOException {
                ResponseWriter out = conn.getWriter();
                out.println("HTTP/1.1 444 This status message contains\n"
                        + " a newline and a\r"
                        + " carrage return but that should be OK.");
                out.println("Connection: close");
                out.println("Content-Length: 0");
                out.println();
                out.flush();
                return true;
            }
        });
        GetMethod method = new GetMethod("/");
        try {
            client.executeMethod(method);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: " + e.toString());
        }
        assertEquals(444, method.getStatusCode());
    }

    /**
     * Test if a response to HEAD method from non-compliant server that contains
     * an unexpected body content can be correctly redirected
     */
    public void testNoncompliantHeadWithResponseBody() throws Exception {
        final String body = "Test body";
        this.server.setRequestHandler(new HttpRequestHandler() {
            public boolean processRequest(SimpleHttpServerConnection conn,
                    SimpleRequest request) throws IOException {
                ResponseWriter out = conn.getWriter();
                out.println("HTTP/1.1 200 OK");
                out.println("Connection: close");
                out.println("Content-Length: " + body.length());
                out.println();
                out.print(body);
                out.flush();
                return true;
            }
        });
        HeadMethod method = new HeadMethod("/");
        method.getParams().setIntParameter(
                HttpMethodParams.HEAD_BODY_CHECK_TIMEOUT, 50);
        client.executeMethod(method);
        assertEquals(200, method.getStatusCode());
        method.releaseConnection();
    }

    /**
     * Test if a response to HEAD method from non-compliant server causes an
     * HttpException to be thrown
     */
    public void testNoncompliantHeadStrictMode() throws Exception {
        final String body = "Test body";
        this.server.setRequestHandler(new HttpRequestHandler() {
            public boolean processRequest(SimpleHttpServerConnection conn,
                    SimpleRequest request) throws IOException {
                ResponseWriter out = conn.getWriter();
                out.println("HTTP/1.1 200 OK");
                out.println("Connection: close");
                out.println("Content-Length: " + body.length());
                out.println();
                out.print(body);
                out.flush();
                return true;
            }
        });
        client.getParams().setBooleanParameter(
                HttpMethodParams.REJECT_HEAD_BODY, true);
        HeadMethod method = new NoncompliantHeadMethod("/");
        method.getParams().setIntParameter(
                HttpMethodParams.HEAD_BODY_CHECK_TIMEOUT, 50);
        try {
            client.executeMethod(method);
            fail("HttpException should have been thrown");
        } catch (HttpException e) {
            // Expected
        }
        method.releaseConnection();
    }

    /**
     * Tests if client is able to handle gracefully malformed responses
     * that may not include response body.
     */
    public void testMalformed304Response() throws Exception {
        this.server.setRequestHandler(new HttpRequestHandler() {
            public boolean processRequest(SimpleHttpServerConnection conn,
                    SimpleRequest request) throws IOException {
                conn.setSocketTimeout(20000);
                ResponseWriter out = conn.getWriter();
                out.println("HTTP/1.1 304 OK");
                out.println("Connection: keep-alive");
                out.println("Content-Length: 100");
                out.println();
                out.flush();
                conn.setKeepAlive(true);
                return true;
            }
        });

        GetMethod method = new GetMethod("/");
        method.getParams().setSoTimeout(1000);
        client.executeMethod(method);
        assertEquals(HttpStatus.SC_NOT_MODIFIED, method.getStatusCode());
        method.getResponseBody();
    }

    public void testMalformed204Response() throws Exception {
        this.server.setRequestHandler(new HttpRequestHandler() {
            public boolean processRequest(SimpleHttpServerConnection conn,
                    SimpleRequest request) throws IOException {
                conn.setSocketTimeout(20000);
                ResponseWriter out = conn.getWriter();
                out.println("HTTP/1.1 204 OK");
                out.println("Connection: close");
                out.println("Content-Length: 100");
                out.println();
                out.flush();
                conn.setKeepAlive(true);
                return true;
            }
        });

        GetMethod method = new GetMethod("/");
        method.getParams().setSoTimeout(1000);
        client.executeMethod(method);
        assertEquals(HttpStatus.SC_NO_CONTENT, method.getStatusCode());
        method.getResponseBody();
    }
    
}
