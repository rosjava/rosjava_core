/*
 * $HeadURL: https://svn.apache.org/repos/asf/jakarta/httpcomponents/oac.hc3x/tags/HTTPCLIENT_3_1/src/test/org/apache/commons/httpclient/TestQueryParameters.java $
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

import junit.framework.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.server.HttpService;
import org.apache.commons.httpclient.server.SimpleRequest;
import org.apache.commons.httpclient.server.SimpleResponse;

/**
 * @author Rodney Waldhoff
 * @version $Id: TestQueryParameters.java 480424 2006-11-29 05:56:49Z bayard $
 */
public class TestQueryParameters extends HttpClientTestBase {

    public TestQueryParameters(String testName) throws Exception {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestQueryParameters.class);
        return suite;
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestQueryParameters.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------------------ Tests

    class QueryInfoService implements HttpService {

        public QueryInfoService() {
            super();
        }

        public boolean process(final SimpleRequest request, final SimpleResponse response)
            throws IOException
        {
            HttpVersion httpversion = request.getRequestLine().getHttpVersion();
            response.setStatusLine(httpversion, HttpStatus.SC_OK);
            response.addHeader(new Header("Content-Type", "text/plain"));
            
            URI uri = new URI(request.getRequestLine().getUri(), true);

            StringBuffer buffer = new StringBuffer();
            buffer.append("QueryString=\"");
            buffer.append(uri.getQuery());
            buffer.append("\"\r\n");
            response.setBodyString(buffer.toString());
            return true;
        }
    }
    
    /**
     * Test that {@link GetMethod#setQueryString(java.lang.String)}
     * can include a leading question mark.
     */
    public void testGetMethodQueryString() throws Exception {
        this.server.setHttpService(new QueryInfoService());
        GetMethod method = new GetMethod("/");
        method.setQueryString("?hadQuestionMark=true");
        try {
            this.client.executeMethod(method);
            assertEquals(200, method.getStatusCode());
            String response = method.getResponseBodyAsString(); 
            assertTrue(response.indexOf("QueryString=\"hadQuestionMark=true\"") >= 0);
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * Test that {@link GetMethod#setQueryString(java.lang.String)}
     * doesn't have to include a leading question mark.
     */
    public void testGetMethodQueryString2() throws Exception {
        this.server.setHttpService(new QueryInfoService());
        GetMethod method = new GetMethod("/");
        method.setQueryString("hadQuestionMark=false");
        try {
            this.client.executeMethod(method);
            assertEquals(200, method.getStatusCode());
            String response = method.getResponseBodyAsString(); 
            assertTrue(response.indexOf("QueryString=\"hadQuestionMark=false\"") >= 0);
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * Test that {@link GetMethod#addParameter(java.lang.String,java.lang.String)}
     * values get added to the query string.
     */
    public void testGetMethodParameters() throws Exception {
        this.server.setHttpService(new QueryInfoService());
        GetMethod method = new GetMethod("/");
        method.setQueryString(new NameValuePair[] { new NameValuePair("param-one","param-value") });
        try {
            this.client.executeMethod(method);
            assertEquals(200, method.getStatusCode());
            String response = method.getResponseBodyAsString(); 
            assertTrue(response.indexOf("QueryString=\"param-one=param-value\"") >= 0);
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * Test that {@link GetMethod#addParameter(java.lang.String,java.lang.String)}
     * works with multiple parameters.
     */
    public void testGetMethodMultiParameters() throws Exception {
        this.server.setHttpService(new QueryInfoService());
        GetMethod method = new GetMethod("/");
        method.setQueryString(new NameValuePair[] {
                                new NameValuePair("param-one","param-value"),
                                new NameValuePair("param-two","param-value2"),
                                new NameValuePair("special-chars",":/?~.")
                              });
        try {
            this.client.executeMethod(method);
            assertEquals(200, method.getStatusCode());
            String response = method.getResponseBodyAsString();
            assertTrue(response.indexOf("QueryString=\"param-one=param-value&param-two=param-value2&special-chars=:/?~.\"") >= 0);
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * Test that {@link GetMethod#addParameter(java.lang.String,java.lang.String)}
     * works with a parameter name but no value.
     */
    public void testGetMethodParameterWithoutValue() throws Exception {
        this.server.setHttpService(new QueryInfoService());
        GetMethod method = new GetMethod("/");
        method.setQueryString(new NameValuePair[] { new NameValuePair("param-without-value", null) });
        try {
            this.client.executeMethod(method);
            assertEquals(200, method.getStatusCode());
            String response = method.getResponseBodyAsString();
            assertTrue(response.indexOf("QueryString=\"param-without-value=\"") >= 0);
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * Test that {@link GetMethod#addParameter(java.lang.String,java.lang.String)}
     * works with a parameter name that occurs more than once.
     */
    public void testGetMethodParameterAppearsTwice() throws Exception {
        this.server.setHttpService(new QueryInfoService());
        GetMethod method = new GetMethod("/");
        method.setQueryString(new NameValuePair[] {
                                  new NameValuePair("foo","one"),
                                  new NameValuePair("foo","two")
                             });
        try {
            this.client.executeMethod(method);
            assertEquals(200, method.getStatusCode());
            String response = method.getResponseBodyAsString();
            assertTrue(response.indexOf("QueryString=\"foo=one&foo=two\"") >= 0);
        } finally {
            method.releaseConnection();
        }
    }

    public void testGetMethodOverwriteQueryString() throws Exception {
        this.server.setHttpService(new QueryInfoService());
        GetMethod method = new GetMethod("/");
        method.setQueryString("query=string");
        method.setQueryString(new NameValuePair[] {
                                  new NameValuePair("param","eter"),
                                  new NameValuePair("para","meter")
                             });
        try {
            this.client.executeMethod(method);
            assertEquals(200, method.getStatusCode());
            String response = method.getResponseBodyAsString();
            assertFalse(response.indexOf("QueryString=\"query=string\"") >= 0);
            assertTrue(response.indexOf("QueryString=\"param=eter&para=meter\"") >= 0);
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * Test that {@link PostMethod#addParameter(java.lang.String,java.lang.String)}
     * and {@link PostMethod#setQueryString(java.lang.String)} combine
     * properly.
     */
    public void testPostMethodParameterAndQueryString() throws Exception {
        this.server.setHttpService(new QueryInfoService());
        PostMethod method = new PostMethod("/");
        method.setQueryString("query=string");
        method.setRequestBody(new NameValuePair[] { 
           new NameValuePair("param","eter"),
           new NameValuePair("para","meter") } );
        try {
            this.client.executeMethod(method);
            assertEquals(200, method.getStatusCode());
            String response = method.getResponseBodyAsString();
            assertTrue(response.indexOf("QueryString=\"query=string\"") >= 0);
            assertFalse(response.indexOf("QueryString=\"param=eter&para=meter\"") >= 0);
        } finally {
            method.releaseConnection();
        }
    }
}

