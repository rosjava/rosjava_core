/*
 * $HeadURL: https://svn.apache.org/repos/asf/jakarta/httpcomponents/oac.hc3x/tags/HTTPCLIENT_3_1/src/test/org/apache/commons/httpclient/TestBadContentLength.java $
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
import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.server.HttpRequestHandler;
import org.apache.commons.httpclient.server.RequestLine;
import org.apache.commons.httpclient.server.ResponseWriter;
import org.apache.commons.httpclient.server.SimpleHttpServer;
import org.apache.commons.httpclient.server.SimpleHttpServerConnection;
import org.apache.commons.httpclient.server.SimpleRequest;

/**
 * Tests HttpClient's behaviour when receiving more response data than expected.
 * <p>
 * A very simple HTTP Server will be setup on a free port during testing, which
 * returns an incorrect response Content-Length, sending surplus response data,
 * which may contain malicious/fake response headers.
 * </p> 
 * 
 * @author Christian Kohlschuetter
 * @version $Id: TestBadContentLength.java 480424 2006-11-29 05:56:49Z bayard $
 */
public class TestBadContentLength extends TestCase {
    private HttpClient client = null;
    private SimpleHttpServer server = null;

    // ------------------------------------------------------------ Constructor
    public TestBadContentLength(String testName) {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestBadContentLength.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestBadContentLength.class);
    }

    // ----------------------------------------------------------- Test Methods

    public void setUp() throws IOException {
        client = new HttpClient();
        server = new SimpleHttpServer(); // use arbitrary port
        server.setTestname(getName());
        server.setRequestHandler(new MyHttpRequestHandler());
    }

    public void tearDown() throws IOException {
        client = null;

        server.destroy();
    }

    /**
     * HttpClient connects to the test server and performs two subsequent
     * requests to the same URI in <u>lenient</u> mode.
     * 
     * Expected behavior:
     * For both requests, status code 200 and a response body of "12345"
     * should be returned.
     *
     * @throws IOException
     */
    public void test1Lenient() throws IOException {
        client.getParams().makeLenient();
        
        GetMethod m =
            new GetMethod("http://localhost:" + server.getLocalPort() + "/");

        client.executeMethod(m);
        assertEquals(200, m.getStatusCode());
        assertEquals("12345", m.getResponseBodyAsString());

        m = new GetMethod("http://localhost:" + server.getLocalPort() + "/");

        client.executeMethod(m);
        assertEquals(200, m.getStatusCode());
        assertEquals("12345", m.getResponseBodyAsString());
        m.releaseConnection();
    }

    /**
     * HttpClient connects to the test server and performs two subsequent
     * requests to the same URI in <u>strict</u> mode.
     * <p>
     * The first response body will be read with getResponseBodyAsString(),
     * which returns null if an error occured.
     * </p>
     * <p>
     * The second response body will be read using an InputStream, which
     * throws an IOException if something went wrong.
     * </p>
     * Expected behavior:
     * For both requests, status code 200 should be returned.<br />
     * For request 1, a <code>null</code> response body should be returned.<br />
     * For request 2, a {@link ProtocolException} is expected.
     *
     * @throws IOException
     */
    public void test1Strict() throws IOException {
        client.getParams().makeStrict();

        GetMethod m =
            new GetMethod("http://localhost:" + server.getLocalPort() + "/");

        client.executeMethod(m);
        assertEquals(200, m.getStatusCode());
        assertEquals("12345", m.getResponseBodyAsString());

        m = new GetMethod("http://localhost:" + server.getLocalPort() + "/");

        client.executeMethod(m);
        assertEquals(200, m.getStatusCode());

        InputStream in = m.getResponseBodyAsStream();
        while (in.read() != -1) {
        }

        m.releaseConnection();
    }

    public void enableThisTestForDebuggingOnly()
        throws InterruptedException {
        while (server.isRunning()) {
            Thread.sleep(100);
        }
    }

    private class MyHttpRequestHandler implements HttpRequestHandler {
        private int requestNo = 0;

        public boolean processRequest(
            final SimpleHttpServerConnection conn,
            final SimpleRequest request) throws IOException
        {
            RequestLine requestLine = request.getRequestLine();
            ResponseWriter out = conn.getWriter();
            if ("GET".equals(requestLine.getMethod())
                && "/".equals(requestLine.getUri())) {

                requestNo++;

                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/html");
                out.println("Content-Length: 5");
                out.println("Connection: keep-alive");
                out.println();
                out.println("12345"); // send exactly 5 bytes

                // and some more garbage!
                out.println("AND SOME MORE\r\nGARBAGE!");
                out.println("HTTP/1.0 404 Not Found");
                out.println("Content-Type: text/plain");
                out.println("");
                out.println("THIS-IS-A-FAKE-RESPONSE!");

                out.flush();
                // process max. 2 subsequents requests per connection
                if (requestNo < 2) {
                    conn.setKeepAlive(true);
                }
            }
            return true;
        }
    }

}
