/*
 * $HeadURL: https://svn.apache.org/repos/asf/jakarta/httpcomponents/oac.hc3x/tags/HTTPCLIENT_3_1/src/test/org/apache/commons/httpclient/TestEntityEnclosingMethod.java $
 * $Revision: 480424 $
 * $Date: 2006-11-29 06:56:49 +0100 (Wed, 29 Nov 2006) $
 *
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.server.AuthRequestHandler;
import org.apache.commons.httpclient.server.HttpRequestHandlerChain;
import org.apache.commons.httpclient.server.HttpService;
import org.apache.commons.httpclient.server.HttpServiceHandler;
import org.apache.commons.httpclient.server.SimpleRequest;
import org.apache.commons.httpclient.server.SimpleResponse;

/**
 * Tests specific to entity enclosing methods.
 *
 * @author Oleg Kalnichevski
 * @version $Id: TestEntityEnclosingMethod.java 480424 2006-11-29 05:56:49Z bayard $
 */
public class TestEntityEnclosingMethod extends HttpClientTestBase {

    public TestEntityEnclosingMethod(String testName) throws IOException {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestEntityEnclosingMethod.class);
        return suite;
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestEntityEnclosingMethod.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------------------ Tests
    
    public void testEnclosedEntityAutoLength() throws Exception {
        String inputstr = "This is a test message";
        byte[] input = inputstr.getBytes("US-ASCII");
        InputStream instream = new ByteArrayInputStream(input);
        
        RequestEntity requestentity = new InputStreamRequestEntity(
                instream, InputStreamRequestEntity.CONTENT_LENGTH_AUTO); 
        PostMethod method = new PostMethod("/");
        method.setRequestEntity(requestentity);
        this.server.setHttpService(new EchoService());
        try {
            this.client.executeMethod(method);
            assertEquals(200, method.getStatusCode());
            String body = method.getResponseBodyAsString();
            assertEquals(inputstr, body);
            assertNull(method.getRequestHeader("Transfer-Encoding"));
            assertNotNull(method.getRequestHeader("Content-Length"));
            assertEquals(input.length, Integer.parseInt(
                    method.getRequestHeader("Content-Length").getValue()));
        } finally {
            method.releaseConnection();
        }
    }

    public void testEnclosedEntityExplicitLength() throws Exception {
        String inputstr = "This is a test message";
        byte[] input = inputstr.getBytes("US-ASCII");
        InputStream instream = new ByteArrayInputStream(input);
        
        RequestEntity requestentity = new InputStreamRequestEntity(
                instream, 14); 
        PostMethod method = new PostMethod("/");
        method.setRequestEntity(requestentity);
        this.server.setHttpService(new EchoService());
        try {
            this.client.executeMethod(method);
            assertEquals(200, method.getStatusCode());
            String body = method.getResponseBodyAsString();
            assertEquals("This is a test", body);
            assertNull(method.getRequestHeader("Transfer-Encoding"));
            assertNotNull(method.getRequestHeader("Content-Length"));
            assertEquals(14, Integer.parseInt(
                    method.getRequestHeader("Content-Length").getValue()));
        } finally {
            method.releaseConnection();
        }
    }

    public void testEnclosedEntityChunked() throws Exception {
        String inputstr = "This is a test message";
        byte[] input = inputstr.getBytes("US-ASCII");
        InputStream instream = new ByteArrayInputStream(input);
        
        RequestEntity requestentity = new InputStreamRequestEntity(
                instream, InputStreamRequestEntity.CONTENT_LENGTH_AUTO); 
        PostMethod method = new PostMethod("/");
        method.setRequestEntity(requestentity);
        method.setContentChunked(true);
        this.server.setHttpService(new EchoService());
        try {
            this.client.executeMethod(method);
            assertEquals(200, method.getStatusCode());
            String body = method.getResponseBodyAsString();
            assertEquals(inputstr, body);
            assertNotNull(method.getRequestHeader("Transfer-Encoding"));
            assertNull(method.getRequestHeader("Content-Length"));
        } finally {
            method.releaseConnection();
        }
    }
    
    public void testEnclosedEntityChunkedHTTP1_0() throws Exception {
        String inputstr = "This is a test message";
        byte[] input = inputstr.getBytes("US-ASCII");
        InputStream instream = new ByteArrayInputStream(input);
        
        RequestEntity requestentity = new InputStreamRequestEntity(
                instream, InputStreamRequestEntity.CONTENT_LENGTH_AUTO); 
        PostMethod method = new PostMethod("/");
        method.setRequestEntity(requestentity);
        method.setContentChunked(true);
        method.getParams().setVersion(HttpVersion.HTTP_1_0);
        this.server.setHttpService(new EchoService());
        try {
            this.client.executeMethod(method);
            fail("ProtocolException should have been thrown");
        } catch (ProtocolException ex) {
            // expected
        } finally {
            method.releaseConnection();
        }
    }

    public void testEnclosedEntityRepeatable() throws Exception {
        String inputstr = "This is a test message";
        byte[] input = inputstr.getBytes("US-ASCII");
        InputStream instream = new ByteArrayInputStream(input);
        
        RequestEntity requestentity = new InputStreamRequestEntity(
                instream, InputStreamRequestEntity.CONTENT_LENGTH_AUTO); 
        PostMethod method = new PostMethod("/");
        method.setRequestEntity(requestentity);

        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds));
        handlerchain.appendHandler(new HttpServiceHandler(new EchoService()));
        this.server.setRequestHandler(handlerchain);
        this.client.getState().setCredentials(AuthScope.ANY, creds);
        try {
            this.client.executeMethod(method);
            assertEquals(200, method.getStatusCode());
            String body = method.getResponseBodyAsString();
            assertEquals(inputstr, body);
            assertNull(method.getRequestHeader("Transfer-Encoding"));
            assertNotNull(method.getRequestHeader("Content-Length"));
            assertEquals(input.length, Integer.parseInt(
                    method.getRequestHeader("Content-Length").getValue()));
        } finally {
            method.releaseConnection();
        }
    }

    public void testEnclosedEntityNonRepeatable() throws Exception {
        String inputstr = "This is a test message";
        byte[] input = inputstr.getBytes("US-ASCII");
        InputStream instream = new ByteArrayInputStream(input);
        
        RequestEntity requestentity = new InputStreamRequestEntity(
                instream, InputStreamRequestEntity.CONTENT_LENGTH_AUTO); 
        PostMethod method = new PostMethod("/");
        method.setRequestEntity(requestentity);
        method.setContentChunked(true);

        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds));
        handlerchain.appendHandler(new HttpServiceHandler(new EchoService()));
        this.server.setRequestHandler(handlerchain);
        this.client.getState().setCredentials(AuthScope.ANY, creds);
        try {
            this.client.executeMethod(method);
            fail("ProtocolException should have been thrown");
        } catch (ProtocolException ex) {
            // expected
        } finally {
            method.releaseConnection();
        }
    }
    
    public void testEnclosedEntityNegativeLength() throws Exception {
        
        String inputstr = "This is a test message";
        byte[] input = inputstr.getBytes("US-ASCII");
        InputStream instream = new ByteArrayInputStream(input);
        
        RequestEntity requestentity = new InputStreamRequestEntity(
                instream, -14); 
        PostMethod method = new PostMethod("/");
        method.setRequestEntity(requestentity);
        method.setContentChunked(false);
        this.server.setHttpService(new EchoService());
        try {
            this.client.executeMethod(method);
            assertEquals(200, method.getStatusCode());
            String body = method.getResponseBodyAsString();
            assertEquals(inputstr, body);
            assertNotNull(method.getRequestHeader("Transfer-Encoding"));
            assertNull(method.getRequestHeader("Content-Length"));
        } finally {
            method.releaseConnection();
        }
    }

    public void testEnclosedEntityNegativeLengthHTTP1_0() throws Exception {
        
        String inputstr = "This is a test message";
        byte[] input = inputstr.getBytes("US-ASCII");
        InputStream instream = new ByteArrayInputStream(input);
        
        RequestEntity requestentity = new InputStreamRequestEntity(
                instream, -14); 
        PostMethod method = new PostMethod("/");
        method.setRequestEntity(requestentity);
        method.setContentChunked(false);
        method.getParams().setVersion(HttpVersion.HTTP_1_0);
        this.server.setHttpService(new EchoService());
        try {
            this.client.executeMethod(method);
            fail("ProtocolException should have been thrown");
        } catch (ProtocolException ex) {
            // expected
        } finally {
            method.releaseConnection();
        }
    }
    
    class RequestBodyStatsService implements HttpService {

        public RequestBodyStatsService() {
            super();
        }

        public boolean process(final SimpleRequest request, final SimpleResponse response)
            throws IOException
        {
            HttpVersion httpversion = request.getRequestLine().getHttpVersion();
            response.setStatusLine(httpversion, HttpStatus.SC_OK);
            response.addHeader(new Header("Content-Type", "text/plain"));            

            StringBuffer buffer = new StringBuffer();
            buffer.append("Request bosy stats:\r\n");
            buffer.append("===================\r\n");
            long l = request.getContentLength();
            if (l >= 0) {
                buffer.append("Content-Length: ");
                buffer.append(l);
                buffer.append("\r\n");
            }
            Header te = request.getFirstHeader("Transfer-Encoding");
            if (te != null) {
                buffer.append("Content-Length: ");
                buffer.append(te.getValue());
                buffer.append("\r\n");
            }
            byte[] b = request.getBodyBytes();
            if (b.length <= 0) {
                buffer.append("No body submitted\r\n");
            }
            response.setBodyString(buffer.toString());
            return true;
        }
    }
    
    public void testEmptyPostMethod() throws Exception {
        this.server.setHttpService(new RequestBodyStatsService());

        PostMethod method = new PostMethod("/");
        method.setRequestHeader("Content-Type", "text/plain");
        this.client.executeMethod(method);
        assertEquals(200,method.getStatusLine().getStatusCode());
        String response = method.getResponseBodyAsString();
        assertNotNull(method.getRequestHeader("Content-Length"));
        assertTrue(response.indexOf("No body submitted") >= 0);

        method = new PostMethod("/");
        method.setRequestHeader("Content-Type", "text/plain");
        method.setRequestEntity(new StringRequestEntity("", null, null));
        this.client.executeMethod(method);
        assertEquals(200,method.getStatusLine().getStatusCode());
        assertNotNull(method.getRequestHeader("Content-Length"));
        response = method.getResponseBodyAsString();
        assertTrue(response.indexOf("No body submitted") >= 0);

        method = new PostMethod("/");
        method.setRequestHeader("Content-Type", "text/plain");
        method.setContentChunked(true);
        this.client.executeMethod(method);
        assertEquals(200,method.getStatusLine().getStatusCode());
        assertNotNull(method.getRequestHeader("Content-Length"));
        response = method.getResponseBodyAsString();
        assertTrue(response.indexOf("No body submitted") >= 0);

        method = new PostMethod("/");
        method.setRequestHeader("Content-Type", "text/plain");
        method.setRequestEntity(new StringRequestEntity("", null, null));
        method.setContentChunked(true);
        this.client.executeMethod(method);
        assertNull(method.getRequestHeader("Content-Length"));
        assertNotNull(method.getRequestHeader("Transfer-Encoding"));
        assertEquals(200,method.getStatusLine().getStatusCode());
        response = method.getResponseBodyAsString();
        assertTrue(response.indexOf("No body submitted") >= 0);
    }
    
}

