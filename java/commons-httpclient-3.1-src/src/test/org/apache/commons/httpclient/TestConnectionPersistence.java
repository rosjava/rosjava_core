/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/TestConnectionPersistence.java,v 1.2 2004/12/20 11:42:30 olegk Exp $
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
 */

package org.apache.commons.httpclient;

import java.io.IOException;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.server.HttpRequestHandler;
import org.apache.commons.httpclient.server.SimpleHttpServerConnection;
import org.apache.commons.httpclient.server.SimpleProxy;
import org.apache.commons.httpclient.server.SimpleRequest;
import org.apache.commons.httpclient.server.SimpleResponse;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Connection persistence tests
 * 
 * @author Oleg Kalnichevski
 *
 * @version $Id: TestConnectionPersistence.java 480424 2006-11-29 05:56:49Z bayard $
 */
public class TestConnectionPersistence extends HttpClientTestBase {
    
    // ------------------------------------------------------------ Constructor
    public TestConnectionPersistence(final String testName) throws IOException {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestConnectionPersistence.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestConnectionPersistence.class);
    }

    // ----------------------------------------------------------- Test Methods

    public void testConnPersisenceHTTP10() throws Exception {
        this.server.setHttpService(new EchoService());

        AccessibleHttpConnectionManager connman = new AccessibleHttpConnectionManager();
        
        this.client.getParams().setVersion(HttpVersion.HTTP_1_0);
        this.client.setHttpConnectionManager(connman);
        
        PostMethod httppost = new PostMethod("/test/");
        httppost.setRequestEntity(new StringRequestEntity("stuff", null, null));
        try {
            this.client.executeMethod(httppost);
        } finally {
            httppost.releaseConnection();
        }
        assertFalse(connman.getConection().isOpen());

        httppost = new PostMethod("/test/");
        httppost.setRequestEntity(new StringRequestEntity("more stuff", null, null));
        try {
            this.client.executeMethod(httppost);
        } finally {
            httppost.releaseConnection();
        }
        assertFalse(connman.getConection().isOpen());
    }

    public void testConnPersisenceHTTP11() throws Exception {
        this.server.setHttpService(new EchoService());

        AccessibleHttpConnectionManager connman = new AccessibleHttpConnectionManager();
        
        this.client.getParams().setVersion(HttpVersion.HTTP_1_1);
        this.client.setHttpConnectionManager(connman);
        
        PostMethod httppost = new PostMethod("/test/");
        httppost.setRequestEntity(new StringRequestEntity("stuff", null, null));
        try {
            this.client.executeMethod(httppost);
        } finally {
            httppost.releaseConnection();
        }
        assertTrue(connman.getConection().isOpen());

        httppost = new PostMethod("/test/");
        httppost.setRequestEntity(new StringRequestEntity("more stuff", null, null));
        try {
            this.client.executeMethod(httppost);
        } finally {
            httppost.releaseConnection();
        }
        assertTrue(connman.getConection().isOpen());
    }

    public void testConnClose() throws Exception {
        this.server.setHttpService(new EchoService());

        AccessibleHttpConnectionManager connman = new AccessibleHttpConnectionManager();
        
        this.client.getParams().setVersion(HttpVersion.HTTP_1_1);
        this.client.setHttpConnectionManager(connman);
        
        PostMethod httppost = new PostMethod("/test/");
        httppost.setRequestEntity(new StringRequestEntity("stuff", null, null));
        try {
            this.client.executeMethod(httppost);
        } finally {
            httppost.releaseConnection();
        }
        assertTrue(connman.getConection().isOpen());

        httppost = new PostMethod("/test/");
        httppost.setRequestHeader("Connection", "close");
        httppost.setRequestEntity(new StringRequestEntity("more stuff", null, null));
        try {
            this.client.executeMethod(httppost);
        } finally {
            httppost.releaseConnection();
        }
        assertFalse(connman.getConection().isOpen());
    }

    public void testConnKeepAlive() throws Exception {
        this.server.setHttpService(new EchoService());

        AccessibleHttpConnectionManager connman = new AccessibleHttpConnectionManager();
        
        this.client.getParams().setVersion(HttpVersion.HTTP_1_0);
        this.client.setHttpConnectionManager(connman);
        
        PostMethod httppost = new PostMethod("/test/");
        httppost.setRequestEntity(new StringRequestEntity("stuff", null, null));
        try {
            this.client.executeMethod(httppost);
        } finally {
            httppost.releaseConnection();
        }
        assertFalse(connman.getConection().isOpen());

        httppost = new PostMethod("/test/");
        httppost.setRequestHeader("Connection", "keep-alive");
        httppost.setRequestEntity(new StringRequestEntity("more stuff", null, null));
        try {
            this.client.executeMethod(httppost);
        } finally {
            httppost.releaseConnection();
        }
        assertTrue(connman.getConection().isOpen());
    }

    public void testRequestConnClose() throws Exception {
        this.server.setRequestHandler(new HttpRequestHandler() {
           
            public boolean processRequest(
                    final SimpleHttpServerConnection conn,
                    final SimpleRequest request) throws IOException {

                // Make sure the request if fully consumed
                request.getBodyBytes();
                
                SimpleResponse response = new SimpleResponse();
                response.setStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK);
                response.setBodyString("stuff back");

                conn.setKeepAlive(true);
                conn.writeResponse(response);
                
                return true;
            }
            
        });

        AccessibleHttpConnectionManager connman = new AccessibleHttpConnectionManager();
        
        this.client.getParams().setVersion(HttpVersion.HTTP_1_0);
        this.client.setHttpConnectionManager(connman);
        
        PostMethod httppost = new PostMethod("/test/");
        httppost.setRequestHeader("Connection", "close");
        httppost.setRequestEntity(new StringRequestEntity("stuff", null, null));
        try {
            this.client.executeMethod(httppost);
        } finally {
            httppost.releaseConnection();
        }
        assertFalse(connman.getConection().isOpen());
    }

    public void testProxyConnClose() throws Exception {
        this.server.setHttpService(new EchoService());
        this.proxy = new SimpleProxy();
        this.client.getHostConfiguration().setProxy(
            proxy.getLocalAddress(), 
            proxy.getLocalPort());                

        AccessibleHttpConnectionManager connman = new AccessibleHttpConnectionManager();
        
        this.client.setHttpConnectionManager(connman);
        
        GetMethod httpget = new GetMethod("/test/");
        try {
            this.client.executeMethod(httpget);
        } finally {
            httpget.releaseConnection();
        }
        assertTrue(connman.getConection().isOpen());

        httpget = new GetMethod("/test/");
        httpget.setRequestHeader("Proxy-Connection", "Close");
        try {
            this.client.executeMethod(httpget);
        } finally {
            httpget.releaseConnection();
        }
        assertFalse(connman.getConection().isOpen());
        assertEquals("Close", httpget.getRequestHeader("Proxy-Connection").getValue());
    }

    
}

