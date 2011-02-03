/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/TestVirtualHost.java,v 1.2 2004/10/31 14:42:59 olegk Exp $
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
 * [Additional notices, if required by prior licensing conditions]
 *
 */

package org.apache.commons.httpclient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.server.HttpService;
import org.apache.commons.httpclient.server.RequestLine;
import org.apache.commons.httpclient.server.SimpleRequest;
import org.apache.commons.httpclient.server.SimpleResponse;

/**
 * HTTP protocol versioning tests.
 *
 * @author Oleg Kalnichevski
 * 
 * @version $Revision: 480424 $
 */
public class TestVirtualHost extends HttpClientTestBase {

    // ------------------------------------------------------------ Constructor
    public TestVirtualHost(final String testName) throws IOException {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestVirtualHost.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestVirtualHost.class);
    }

    private class VirtualService implements HttpService {

        public VirtualService() {
            super();
        }

        public boolean process(final SimpleRequest request, final SimpleResponse response)
            throws IOException
        {
            HttpVersion httpversion = request.getRequestLine().getHttpVersion();
            Header hostheader = request.getFirstHeader("Host");
            if (hostheader == null) {
                response.setStatusLine(httpversion, HttpStatus.SC_BAD_REQUEST);
                response.setBodyString("Host header missing");
            } else {
                response.setStatusLine(httpversion, HttpStatus.SC_OK);
                response.setBodyString(hostheader.getValue());
            }
            return true;
        }
    }

    public void testVirtualHostHeader() throws IOException {
        this.server.setHttpService(new VirtualService());

        GetMethod httpget = new GetMethod("/test/");
        
        HostConfiguration hostconf = new HostConfiguration();
        hostconf.setHost(this.server.getLocalAddress(), this.server.getLocalPort(), "http");
        hostconf.getParams().setVirtualHost("somehost");
        try {
            this.client.executeMethod(hostconf, httpget);
            String hostheader = "somehost:" + this.server.getLocalPort();
            assertEquals(hostheader, httpget.getResponseBodyAsString());
        } finally {
            httpget.releaseConnection();
        }
    }

    public void testNoVirtualHostHeader() throws IOException {
        this.server.setHttpService(new VirtualService());

        GetMethod httpget = new GetMethod("/test/");
        
        HostConfiguration hostconf = new HostConfiguration();
        hostconf.setHost(this.server.getLocalAddress(), this.server.getLocalPort(), "http");
        hostconf.getParams().setVirtualHost(null);
        try {
            this.client.executeMethod(hostconf, httpget);
            String hostheader = this.server.getLocalAddress() + ":" + this.server.getLocalPort();
            assertEquals(hostheader, httpget.getResponseBodyAsString());
        } finally {
            httpget.releaseConnection();
        }
    }
    
    private class VirtualHostService implements HttpService {

        public VirtualHostService() {
            super();
        }

        public boolean process(final SimpleRequest request, final SimpleResponse response)
            throws IOException {
            RequestLine reqline = request.getRequestLine();
            HttpVersion ver = reqline.getHttpVersion();
            Header header =  request.getFirstHeader("Host");
            if (header == null) {
                response.setStatusLine(ver, HttpStatus.SC_BAD_REQUEST);
                return true;
            }
            String host = header.getValue();
            if (host.equalsIgnoreCase("whatever.com")) {
                response.setStatusLine(ver, HttpStatus.SC_MOVED_TEMPORARILY);
                response.setHeader(new Header("Location", "testhttp://www.whatever.com/"));
                return true;
            } else if (host.equalsIgnoreCase("www.whatever.com")) {
                response.setStatusLine(ver, HttpStatus.SC_MOVED_TEMPORARILY);
                response.setHeader(new Header("Location", "testhttp://www.whatever.co.nz/"));
                return true;
            } else if (host.equalsIgnoreCase("www.whatever.co.nz")) {
                response.setStatusLine(ver, HttpStatus.SC_OK);
                return true;
            } else {
                response.setStatusLine(ver, HttpStatus.SC_NOT_FOUND);
                return true;
            }
        }
    }
    
    private class VirtualSocketFactory implements ProtocolSocketFactory {
        
        private final String hostname;
        private final int port;
        
        public VirtualSocketFactory(final String hostname, int port) {
            super();
            this.hostname = hostname;
            this.port = port;
        }

        public Socket createSocket(
                final String host, 
                int port, 
                final InetAddress localAddress, 
                int localPort, 
                final HttpConnectionParams params) throws IOException, UnknownHostException, ConnectTimeoutException {
            return new Socket(this.hostname, this.port);
        }

        public Socket createSocket(String host, int port, InetAddress localAddress, int localPort) throws IOException, UnknownHostException {
            return new Socket(this.hostname, this.port);
        }

        public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
            return new Socket(this.hostname, this.port);
        }
        
    }

    public void testRedirectWithVirtualHost() throws IOException {
        String host = this.server.getLocalAddress();
        int port = this.server.getLocalPort();

        Protocol testhttp = new Protocol("http", new VirtualSocketFactory(host, port), port);
        Protocol.registerProtocol("testhttp", testhttp);
        try {
            this.server.setHttpService(new VirtualHostService());
            this.client.getHostConfiguration().setHost(host, port, "testhttp");
            this.client.getHostConfiguration().getParams().setVirtualHost("whatever.com");
            GetMethod httpget = new GetMethod("/");
            httpget.setFollowRedirects(true);
            try {
                this.client.executeMethod(httpget);
                assertEquals(HttpStatus.SC_OK, httpget.getStatusCode());
                assertEquals("http://www.whatever.co.nz/", httpget.getURI().toString());
            } finally {
                httpget.releaseConnection();
            }
        } finally {
            Protocol.unregisterProtocol("testhttp");
        }
        
    }
    
}
