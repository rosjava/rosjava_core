/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/TestHttpMethodFundamentals.java,v 1.6 2004/11/06 23:47:58 olegk Exp $
 * $Revision: 481225 $
 * $Date: 2006-12-01 12:26:28 +0100 (Fri, 01 Dec 2006) $
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
 */

package org.apache.commons.httpclient;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.server.HttpService;
import org.apache.commons.httpclient.server.SimpleRequest;
import org.apache.commons.httpclient.server.SimpleResponse;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests basic method functionality.
 *
 * @author Remy Maucherat
 * @author Rodney Waldhoff
 * @author Oleg Kalnichevski
 * 
 * @version $Id: TestHttpMethodFundamentals.java 481225 2006-12-01 11:26:28Z oglueck $
 */
public class TestHttpMethodFundamentals extends HttpClientTestBase {

    public TestHttpMethodFundamentals(final String testName) throws IOException {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestHttpMethodFundamentals.class);
        ProxyTestDecorator.addTests(suite);
        return suite;
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestHttpMethodFundamentals.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }
    
    class ManyAService implements HttpService {

        public ManyAService() {
            super();
        }

        public boolean process(final SimpleRequest request, final SimpleResponse response)
            throws IOException
        {
            HttpVersion httpversion = request.getRequestLine().getHttpVersion();
            response.setStatusLine(httpversion, HttpStatus.SC_OK);
            response.addHeader(new Header("Content-Type", "text/plain"));            
            response.addHeader(new Header("Connection", "close"));            
            StringBuffer buffer = new StringBuffer(1024);
            for (int i = 0; i < 1024; i++) {
                buffer.append('A');
            }
            response.setBodyString(buffer.toString());
            return true;
        }
    }

    class SimpleChunkedService implements HttpService {

        public SimpleChunkedService() {
            super();
        }

        public boolean process(final SimpleRequest request, final SimpleResponse response)
            throws IOException
        {
            HttpVersion httpversion = request.getRequestLine().getHttpVersion();
            response.setStatusLine(httpversion, HttpStatus.SC_OK);
            response.addHeader(new Header("Content-Type", "text/plain"));            
            response.addHeader(new Header("Content-Length", "garbage")); 
            response.addHeader(new Header("Transfer-Encoding", "chunked")); 
            response.addHeader(new Header("Connection", "close"));            
            response.setBodyString("1234567890123");
            return true;
        }
    }

    class EmptyResponseService implements HttpService {

        public EmptyResponseService() {
            super();
        }

        public boolean process(final SimpleRequest request, final SimpleResponse response)
            throws IOException
        {
            HttpVersion httpversion = request.getRequestLine().getHttpVersion();
            response.setStatusLine(httpversion, HttpStatus.SC_OK);
            response.addHeader(new Header("Content-Type", "text/plain"));            
            response.addHeader(new Header("Transfer-Encoding", "chunked")); 
            response.addHeader(new Header("Connection", "close"));            
            return true;
        }
    }

    public void testHttpMethodBasePaths() throws Exception {
        HttpMethod simple = new FakeHttpMethod();
        String[] paths = {
           "/some/absolute/path",
           "../some/relative/path",
           "/",
           "/some/path/with?query=string"
       };
    
        for (int i=0; i<paths.length; i++){
            simple.setPath(paths[i]);
            assertEquals(paths[i], simple.getPath());
        }
    }

    public void testHttpMethodBaseDefaultPath() throws Exception {
        HttpMethod simple = new FakeHttpMethod();
        assertEquals("/", simple.getPath());

        simple.setPath("");
        assertEquals("/", simple.getPath());

        simple.setPath(null);
        assertEquals("/", simple.getPath());
    }

    public void testHttpMethodBasePathConstructor() throws Exception {
        HttpMethod simple = new FakeHttpMethod();
        assertEquals("/", simple.getPath());

        simple = new FakeHttpMethod("");
        assertEquals("/", simple.getPath());

        simple = new FakeHttpMethod("/some/path/");
        assertEquals("/some/path/", simple.getPath());
    }

    /** 
     * Tests response with a Trasfer-Encoding and Content-Length 
     */
    public void testHttpMethodBaseTEandCL() throws Exception {
        this.server.setHttpService(new SimpleChunkedService());
        
        GetMethod httpget = new GetMethod("/test/");
        try {
            this.client.executeMethod(httpget);
            assertEquals(HttpStatus.SC_OK, httpget.getStatusCode());
            assertEquals("1234567890123", httpget.getResponseBodyAsString());
            assertTrue(this.client.getHttpConnectionManager() instanceof SimpleHttpConnectionManager);
            HttpConnection conn = this.client.getHttpConnectionManager().
                getConnection(this.client.getHostConfiguration());
            assertNotNull(conn);
            conn.assertNotOpen();
        } finally {
            httpget.releaseConnection();
        }
    }

    public void testConnectionAutoClose() throws Exception {
        this.server.setHttpService(new ManyAService());
        
        GetMethod httpget = new GetMethod("/test/");
        try {
            this.client.executeMethod(httpget);
            assertEquals(HttpStatus.SC_OK, httpget.getStatusCode());
            Reader response = new InputStreamReader(httpget.getResponseBodyAsStream());
            int c;
            while ((c = response.read()) != -1) {
               assertEquals((int) 'A', c);
            }
            assertTrue(this.client.getHttpConnectionManager() instanceof SimpleHttpConnectionManager);
            HttpConnection conn = this.client.getHttpConnectionManager().
                getConnection(this.client.getHostConfiguration());
            assertNotNull(conn);
            conn.assertNotOpen();
        } finally {
            httpget.releaseConnection();
        }
    }

    public void testSetGetQueryString1() {
        HttpMethod method = new GetMethod();
        String qs1 = "name1=value1&name2=value2";
        method.setQueryString(qs1);
        assertEquals(qs1, method.getQueryString());
    }

    public void testQueryURIEncoding() {
        HttpMethod method = new GetMethod("http://server/servlet?foo=bar&baz=schmoo");
        assertEquals("foo=bar&baz=schmoo", method.getQueryString());
    }

    public void testSetGetQueryString2() {
        HttpMethod method = new GetMethod();
        NameValuePair[] q1 = new NameValuePair[] {
            new NameValuePair("name1", "value1"),
            new NameValuePair("name2", "value2")
        };
        method.setQueryString(q1);
        String qs1 = "name1=value1&name2=value2";
        assertEquals(qs1, method.getQueryString());
    }

    /**
     * Make sure that its OK to call releaseConnection if the connection has not been.
     */
    public void testReleaseConnection() {
        HttpMethod method = new GetMethod("http://bogus.url/path/");
        method.releaseConnection();
    }

    /** 
     * Tests empty body response
     */
    public void testEmptyBodyAsString() throws Exception {
        this.server.setHttpService(new EmptyResponseService());
        
        GetMethod httpget = new GetMethod("/test/");
        try {
            this.client.executeMethod(httpget);
            assertEquals(HttpStatus.SC_OK, httpget.getStatusCode());
            String response = httpget.getResponseBodyAsString();
            assertNull(response);

            this.client.executeMethod(httpget);
            assertEquals(HttpStatus.SC_OK, httpget.getStatusCode());
            response = httpget.getResponseBodyAsString(1);
            assertNull(response);
        } finally {
            httpget.releaseConnection();
        }
    }


    public void testEmptyBodyAsByteArray() throws Exception {
        this.server.setHttpService(new EmptyResponseService());
        
        GetMethod httpget = new GetMethod("/test/");
        try {
            this.client.executeMethod(httpget);
            assertEquals(HttpStatus.SC_OK, httpget.getStatusCode());
            byte[] response = httpget.getResponseBody();
            assertNull(response);
        } finally {
            httpget.releaseConnection();
        }
    }
    
    public void testLongBodyAsString() throws Exception {
        this.server.setHttpService(new SimpleChunkedService());
        
        GetMethod httpget = new GetMethod("/test/");
        try {
            this.client.executeMethod(httpget);
            assertEquals(HttpStatus.SC_OK, httpget.getStatusCode());
            try {
                httpget.getResponseBodyAsString(5); // too small
            } catch(HttpContentTooLargeException e) {
                /* expected */
                assertEquals(5, e.getMaxLength());
            }
            
            this.client.executeMethod(httpget);
            assertEquals(HttpStatus.SC_OK, httpget.getStatusCode());
            String response = httpget.getResponseBodyAsString(13); // exact size
            assertEquals("1234567890123", response);

            this.client.executeMethod(httpget);
            assertEquals(HttpStatus.SC_OK, httpget.getStatusCode());
            response = httpget.getResponseBodyAsString(128); // plenty
            assertEquals("1234567890123", response);
        } finally {
            httpget.releaseConnection();
        }
    }
    
    public void testUrlGetMethodWithPathQuery() {
        GetMethod method = new GetMethod("http://www.fubar.com/path1/path2?query=string");
        try {
            assertEquals(
                "Get URL",
                "http://www.fubar.com/path1/path2?query=string",
                method.getURI().toString()
            );
        } catch ( URIException e ) {
            fail( "trouble getting URI: " + e );
        }
        assertEquals("Get Path", "/path1/path2", method.getPath());
        assertEquals("Get query string", "query=string", method.getQueryString());
     
    }

    public void testUrlGetMethodWithPath() {
        GetMethod method = new GetMethod("http://www.fubar.com/path1/path2");
        try {
            assertEquals(
                "Get URL",
                "http://www.fubar.com/path1/path2",
                method.getURI().toString()
            );
        } catch ( URIException e ) {
            fail( "trouble getting URI: " + e );
        }
        assertEquals("Get Path", "/path1/path2", method.getPath());
        assertEquals("Get query string", null, method.getQueryString());
    }

    public void testUrlGetMethod() {
        GetMethod method = new GetMethod("http://www.fubar.com/");
        try {
            assertEquals(
                "Get URL",
                "http://www.fubar.com/",
                method.getURI().toString()
            );
        } catch ( URIException e ) {
            fail( "trouble getting URI: " + e );
        }
        assertEquals("Get Path", "/", method.getPath());
        assertEquals("Get query string", null, method.getQueryString());

    }
    

    public void testUrlGetMethodWithInvalidProtocol() {
        try {
            GetMethod method = new GetMethod("crap://www.fubar.com/");
            fail("The use of invalid protocol must have resulted in an IllegalStateException");
        }
        catch(IllegalStateException expected) {
        }
    }
}
