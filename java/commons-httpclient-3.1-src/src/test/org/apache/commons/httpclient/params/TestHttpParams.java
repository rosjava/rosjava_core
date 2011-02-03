/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/params/TestHttpParams.java,v 1.4 2004/10/31 14:42:59 olegk Exp $
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

package org.apache.commons.httpclient.params;

import java.io.IOException;
import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClientTestBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HostParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.server.HttpService;
import org.apache.commons.httpclient.server.SimpleRequest;
import org.apache.commons.httpclient.server.SimpleResponse;

/**
 * HTTP preference framework tests.
 *
 * @author Oleg Kalnichevski
 * 
 * @version $Revision: 480424 $
 */
public class TestHttpParams extends HttpClientTestBase {

    // ------------------------------------------------------------ Constructor
    public TestHttpParams(final String testName) throws IOException {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestHttpParams.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestHttpParams.class);
    }

    private class SimpleService implements HttpService {

        public SimpleService() {
            super();
        }

        public boolean process(final SimpleRequest request, final SimpleResponse response)
            throws IOException
        {
            String uri = request.getRequestLine().getUri();  
            HttpVersion httpversion = request.getRequestLine().getHttpVersion();
            
            if ("/miss/".equals(uri)) {
                response.setStatusLine(httpversion, HttpStatus.SC_MOVED_TEMPORARILY);
                response.addHeader(new Header("Location", "/hit/"));
                response.setBodyString("Missed!");
            } else if ("/hit/".equals(uri)) {
                response.setStatusLine(httpversion, HttpStatus.SC_OK);
                response.setBodyString("Hit!");
            } else {
                response.setStatusLine(httpversion, HttpStatus.SC_NOT_FOUND);
                response.setBodyString(uri + " not found");
            }
            return true;
        }
    }

    public void testDefaultHeaders() throws IOException {
        this.server.setHttpService(new SimpleService());

        ArrayList defaults = new ArrayList();
        defaults.add(new Header("this-header", "value1"));
        defaults.add(new Header("that-header", "value1"));
        defaults.add(new Header("that-header", "value2"));
        defaults.add(new Header("User-Agent", "test"));

        HostConfiguration hostconfig = new HostConfiguration();
        hostconfig.setHost(
                this.server.getLocalAddress(), 
                this.server.getLocalPort(),
                Protocol.getProtocol("http"));
        hostconfig.getParams().setParameter(HostParams.DEFAULT_HEADERS, defaults);
        
        GetMethod httpget = new GetMethod("/miss/");
        try {
            this.client.executeMethod(hostconfig, httpget);
        } finally {
            httpget.releaseConnection();
        }
        assertEquals(HttpStatus.SC_OK, httpget.getStatusCode());
        Header[] thisheader = httpget.getRequestHeaders("this-header");
        assertEquals(1, thisheader.length);
        Header[] thatheader = httpget.getRequestHeaders("that-header");
        assertEquals(2, thatheader.length);
        assertEquals("test", httpget.getRequestHeader("User-Agent").getValue());
    }

    public void testDefaults() throws IOException {
        this.server.setHttpService(new SimpleService());

        this.client.getParams().setParameter(HttpMethodParams.USER_AGENT, "test");
        HostConfiguration hostconfig = new HostConfiguration();
        hostconfig.setHost(
                this.server.getLocalAddress(), 
                this.server.getLocalPort(),
                Protocol.getProtocol("http"));
        
        GetMethod httpget = new GetMethod("/miss/");
        try {
            this.client.executeMethod(hostconfig, httpget);
        } finally {
            httpget.releaseConnection();
        }
        assertEquals(HttpStatus.SC_OK, httpget.getStatusCode());
        assertEquals("test", httpget.getRequestHeader("User-Agent").getValue());
        assertEquals("test", httpget.getParams().
                getParameter(HttpMethodParams.USER_AGENT));
        assertEquals("test", hostconfig.getParams().
                getParameter(HttpMethodParams.USER_AGENT));
        assertEquals("test", client.getParams().
                getParameter(HttpMethodParams.USER_AGENT));
    }
}
