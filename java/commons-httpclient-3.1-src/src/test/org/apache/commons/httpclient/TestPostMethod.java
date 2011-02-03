/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/TestWebappPostMethod.java,v 1.7 2004/05/12 20:43:54 olegk Exp $
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

import junit.framework.*;
import org.apache.commons.httpclient.methods.*;
import java.io.*;

/**
 * Webapp tests specific to the PostMethod.
 *
 * @author <a href="jsdever@apache.org">Jeff Dever</a>
 * @version $Id: TestPostMethod.java 480424 2006-11-29 05:56:49Z bayard $
 */
public class TestPostMethod extends HttpClientTestBase {

    public TestPostMethod(String testName) throws IOException {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestPostMethod.class);
        return suite;
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestPostMethod.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------------------ Tests
    
    /**
     * Test that the body can be set as a array of parameters
     */
    public void testParametersBodyToParamServlet() throws Exception {
        PostMethod method = new PostMethod("/");
        NameValuePair[] parametersBody =  new NameValuePair[] { 
            new NameValuePair("pname1","pvalue1"),
            new NameValuePair("pname2","pvalue2") 
        };
        method.setRequestBody(parametersBody);
        this.server.setHttpService(new EchoService());
        try {
            this.client.executeMethod(method);
            assertEquals(200, method.getStatusCode());
            String body = method.getResponseBodyAsString();
            assertEquals("pname1=pvalue1&pname2=pvalue2", body);
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * Test that the body can be set as a String
     */
    public void testStringBodyToParamServlet() throws Exception {
        PostMethod method = new PostMethod("/");
        String stringBody = "pname1=pvalue1&pname2=pvalue2";
        method.setRequestEntity(
            new StringRequestEntity(stringBody, PostMethod.FORM_URL_ENCODED_CONTENT_TYPE, null));
        this.server.setHttpService(new EchoService());
        try {
            this.client.executeMethod(method);
            assertEquals(200, method.getStatusCode());
            String body = method.getResponseBodyAsString();
            assertEquals("pname1=pvalue1&pname2=pvalue2", body);
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * Test that the body can be set as a String without an explict 
     * content type
     */
    public void testStringBodyToBodyServlet() throws Exception {
        PostMethod method = new PostMethod("/");
        String stringBody = "pname1=pvalue1&pname2=pvalue2";

        method.setRequestEntity(new StringRequestEntity(stringBody, null, null));
        this.server.setHttpService(new EchoService());
        try {
            this.client.executeMethod(method);
            assertEquals(200, method.getStatusCode());
            String body = method.getResponseBodyAsString();
            assertEquals("pname1=pvalue1&pname2=pvalue2", body);
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * Test that parameters can be added.
     */
    public void testAddParametersToParamServlet() throws Exception {
        PostMethod method = new PostMethod("/");

        method.addParameter(new NameValuePair("pname1","pvalue1"));
        method.addParameter(new NameValuePair("pname2","pvalue2"));

        this.server.setHttpService(new EchoService());
        try {
            this.client.executeMethod(method);
            assertEquals(200, method.getStatusCode());
            String body = method.getResponseBodyAsString();
            assertEquals("pname1=pvalue1&pname2=pvalue2", body);
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * Test that parameters can be added and removed.
     */
    public void testAddRemoveParametersToParamServlet() throws Exception {
        PostMethod method = new PostMethod("/");

        method.addParameter(new NameValuePair("pname0","pvalue0"));
        method.addParameter(new NameValuePair("pname1","pvalue1"));
        method.addParameter(new NameValuePair("pname2","pvalue2"));
        method.addParameter(new NameValuePair("pname3","pvalue3"));
        method.removeParameter("pname0");
        method.removeParameter("pname3");

        this.server.setHttpService(new EchoService());
        try {
            this.client.executeMethod(method);
            assertEquals(200, method.getStatusCode());
            String body = method.getResponseBodyAsString();
            assertEquals("pname1=pvalue1&pname2=pvalue2", body);
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * Test the return value of the PostMethod#removeParameter.
     */
    public void testRemoveParameterReturnValue() throws Exception {
        PostMethod method = new PostMethod("/");

        method.addParameter("param", "whatever");
        assertTrue("Return value of the method is expected to be true", method.removeParameter("param"));
        assertFalse("Return value of the method is expected to be false", method.removeParameter("param"));
    }

    private String getRequestAsString(RequestEntity entity) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        entity.writeRequest(bos);
        return new String(bos.toByteArray(), "UTF-8");
    }
    
    /**
     * Test if setParameter overwrites existing parameter values.
     */
    public void testAddParameterFollowedBySetParameter() throws Exception {
        PostMethod method = new PostMethod("/");

        method.addParameter("param", "a");
        method.addParameter("param", "b");
        method.addParameter("param", "c");
        assertEquals("param=a&param=b&param=c", getRequestAsString(method.getRequestEntity()));
        method.setParameter("param", "a");
        assertEquals("param=a", getRequestAsString(method.getRequestEntity()));
    }

}

