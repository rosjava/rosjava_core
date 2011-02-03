/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/TestRedirects.java,v 1.9 2005/01/14 19:40:39 olegk Exp $
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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.server.HttpService;
import org.apache.commons.httpclient.server.RequestLine;
import org.apache.commons.httpclient.server.SimpleHttpServer;
import org.apache.commons.httpclient.server.SimpleRequest;
import org.apache.commons.httpclient.server.SimpleResponse;

/**
 * Redirection test cases.
 *
 * @author Oleg Kalnichevski
 * 
 * @version $Id: TestRedirects.java 480424 2006-11-29 05:56:49Z bayard $
 */
public class TestRedirects extends HttpClientTestBase {

    // ------------------------------------------------------------ Constructor
    public TestRedirects(final String testName) throws IOException {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestRedirects.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        TestSuite suite = new TestSuite(TestRedirects.class);
        ProxyTestDecorator.addTests(suite);
        return suite;
    }

    private class BasicRedirectService implements HttpService {
        private int statuscode = HttpStatus.SC_MOVED_TEMPORARILY;
        private String host = null;
        private int port;

        public BasicRedirectService(final String host, int port, int statuscode) {
            super();
            this.host = host;
            this.port = port;
            if (statuscode > 0) {
                this.statuscode = statuscode;
            }
        }

        public BasicRedirectService(final String host, int port) {
            this(host, port, -1);
        }

        public boolean process(final SimpleRequest request, final SimpleResponse response)
            throws IOException {
            RequestLine reqline = request.getRequestLine();
            HttpVersion ver = reqline.getHttpVersion();
            if (reqline.getUri().equals("/oldlocation/")) {
                response.setStatusLine(ver, this.statuscode);
                response.addHeader(new Header("Location", 
                        "http://" + this.host + ":" + this.port + "/newlocation/"));
                response.addHeader(new Header("Connection", "close"));
            } else if (reqline.getUri().equals("/newlocation/")) {
                response.setStatusLine(ver, HttpStatus.SC_OK);
                response.setBodyString("Successful redirect");
            } else {
                response.setStatusLine(ver, HttpStatus.SC_NOT_FOUND);
            }
            return true;
        }
    }

    private class CircularRedirectService implements HttpService {

        private int invocations = 0;
        
        public CircularRedirectService() {
            super();
        }
        
        public boolean process(final SimpleRequest request, final SimpleResponse response)
            throws IOException
        {
            RequestLine reqline = request.getRequestLine();
            HttpVersion ver = reqline.getHttpVersion();
            if (reqline.getUri().startsWith("/circular-oldlocation")) {
                response.setStatusLine(ver, HttpStatus.SC_MOVED_TEMPORARILY);
                response.addHeader(new Header("Location", "/circular-location2?invk=" + (++this.invocations)));
            } else if (reqline.getUri().startsWith("/circular-location2")) {
                response.setStatusLine(ver, HttpStatus.SC_MOVED_TEMPORARILY);
                response.addHeader(new Header("Location", "/circular-oldlocation?invk=" + (++this.invocations)));
            } else {
                response.setStatusLine(ver, HttpStatus.SC_NOT_FOUND);
            }
            return true;
        }
    }

    private class RelativeRedirectService implements HttpService {
        
            public RelativeRedirectService() {
                super();
            }

            public boolean process(final SimpleRequest request, final SimpleResponse response)
                throws IOException
            {
                RequestLine reqline = request.getRequestLine();
                HttpVersion ver = reqline.getHttpVersion();
                if (reqline.getUri().equals("/oldlocation/")) {
                    response.setStatusLine(ver, HttpStatus.SC_MOVED_TEMPORARILY);
                    response.addHeader(new Header("Location", "/relativelocation/"));
                } else if (reqline.getUri().equals("/relativelocation/")) {
                    response.setStatusLine(ver, HttpStatus.SC_OK);
                    response.setBodyString("Successful redirect");
                } else {
                    response.setStatusLine(ver, HttpStatus.SC_NOT_FOUND);
                }
                return true;
            }
        }

    private class BogusRedirectService implements HttpService {
        private String url;
        
        public BogusRedirectService(String redirectUrl) {
            super();
            this.url = redirectUrl;
        }

        public boolean process(final SimpleRequest request, final SimpleResponse response)
            throws IOException {
            RequestLine reqline = request.getRequestLine();
            HttpVersion ver = reqline.getHttpVersion();
            if (reqline.getUri().equals("/oldlocation/")) {
                response.setStatusLine(ver, HttpStatus.SC_MOVED_TEMPORARILY);
                response.addHeader(new Header("Location", url));
            } else if (reqline.getUri().equals("/relativelocation/")) {
                response.setStatusLine(ver, HttpStatus.SC_OK);
                response.setBodyString("Successful redirect");
            } else {
                response.setStatusLine(ver, HttpStatus.SC_NOT_FOUND);
            }
            return true;
        }
    }

    public void testBasicRedirect300() throws IOException {
        String host = this.server.getLocalAddress();
        int port = this.server.getLocalPort();
        this.server.setHttpService(
                new BasicRedirectService(host, port, HttpStatus.SC_MULTIPLE_CHOICES));
        GetMethod httpget = new GetMethod("/oldlocation/");
        httpget.setFollowRedirects(false);
        try {
            this.client.executeMethod(httpget);
            assertEquals(HttpStatus.SC_MULTIPLE_CHOICES, httpget.getStatusCode());
            assertEquals("/oldlocation/", httpget.getPath());
            assertEquals(new URI("/oldlocation/", false), httpget.getURI());
        } finally {
            httpget.releaseConnection();
        }
    }

    public void testBasicRedirect301() throws IOException {
        String host = this.server.getLocalAddress();
        int port = this.server.getLocalPort();
        this.server.setHttpService(
                new BasicRedirectService(host, port, HttpStatus.SC_MOVED_PERMANENTLY));
        GetMethod httpget = new GetMethod("/oldlocation/");
        httpget.setFollowRedirects(true);
        try {
            this.client.executeMethod(httpget);
            assertEquals(HttpStatus.SC_OK, httpget.getStatusCode());
            assertEquals("/newlocation/", httpget.getPath());
            assertEquals(host, httpget.getURI().getHost());
            assertEquals(port, httpget.getURI().getPort());
            assertEquals(new URI("http://" + host + ":" + port + "/newlocation/", false), httpget.getURI());
        } finally {
            httpget.releaseConnection();
        }
    }

    public void testBasicRedirect302() throws IOException {
        String host = this.server.getLocalAddress();
        int port = this.server.getLocalPort();
        this.server.setHttpService(
                new BasicRedirectService(host, port, HttpStatus.SC_MOVED_TEMPORARILY));
        GetMethod httpget = new GetMethod("/oldlocation/");
        httpget.setFollowRedirects(true);
        try {
            this.client.executeMethod(httpget);
            assertEquals(HttpStatus.SC_OK, httpget.getStatusCode());
            assertEquals("/newlocation/", httpget.getPath());
            assertEquals(host, httpget.getURI().getHost());
            assertEquals(port, httpget.getURI().getPort());
            assertEquals(new URI("http://" + host + ":" + port + "/newlocation/", false), httpget.getURI());
        } finally {
            httpget.releaseConnection();
        }
    }

    public void testBasicRedirect303() throws IOException {
        String host = this.server.getLocalAddress();
        int port = this.server.getLocalPort();
        this.server.setHttpService(
                new BasicRedirectService(host, port, HttpStatus.SC_SEE_OTHER));
        GetMethod httpget = new GetMethod("/oldlocation/");
        httpget.setFollowRedirects(true);
        try {
            this.client.executeMethod(httpget);
            assertEquals(HttpStatus.SC_OK, httpget.getStatusCode());
            assertEquals("/newlocation/", httpget.getPath());
            assertEquals(host, httpget.getURI().getHost());
            assertEquals(port, httpget.getURI().getPort());
            assertEquals(new URI("http://" + host + ":" + port + "/newlocation/", false), httpget.getURI());
        } finally {
            httpget.releaseConnection();
        }
    }

    public void testBasicRedirect304() throws IOException {
        String host = this.server.getLocalAddress();
        int port = this.server.getLocalPort();
        this.server.setHttpService(
                new BasicRedirectService(host, port, HttpStatus.SC_NOT_MODIFIED));
        GetMethod httpget = new GetMethod("/oldlocation/");
        httpget.setFollowRedirects(true);
        try {
            this.client.executeMethod(httpget);
            assertEquals(HttpStatus.SC_NOT_MODIFIED, httpget.getStatusCode());
            assertEquals("/oldlocation/", httpget.getPath());
            assertEquals(new URI("/oldlocation/", false), httpget.getURI());
        } finally {
            httpget.releaseConnection();
        }
    }

    public void testBasicRedirect305() throws IOException {
        String host = this.server.getLocalAddress();
        int port = this.server.getLocalPort();
        this.server.setHttpService(
                new BasicRedirectService(host, port, HttpStatus.SC_USE_PROXY));
        GetMethod httpget = new GetMethod("/oldlocation/");
        httpget.setFollowRedirects(true);
        try {
            this.client.executeMethod(httpget);
            assertEquals(HttpStatus.SC_USE_PROXY, httpget.getStatusCode());
            assertEquals("/oldlocation/", httpget.getPath());
            assertEquals(new URI("/oldlocation/", false), httpget.getURI());
        } finally {
            httpget.releaseConnection();
        }
    }

    public void testBasicRedirect307() throws IOException {
        String host = this.server.getLocalAddress();
        int port = this.server.getLocalPort();
        this.server.setHttpService(
                new BasicRedirectService(host, port, HttpStatus.SC_TEMPORARY_REDIRECT));
        GetMethod httpget = new GetMethod("/oldlocation/");
        httpget.setFollowRedirects(true);
        try {
            this.client.executeMethod(httpget);
            assertEquals(HttpStatus.SC_OK, httpget.getStatusCode());
            assertEquals("/newlocation/", httpget.getPath());
            assertEquals(host, httpget.getURI().getHost());
            assertEquals(port, httpget.getURI().getPort());
            assertEquals(new URI("http://" + host + ":" + port + "/newlocation/", false), httpget.getURI());
        } finally {
            httpget.releaseConnection();
        }
    }

    public void testNoRedirect() throws IOException {
        String host = this.server.getLocalAddress();
        int port = this.server.getLocalPort();
        this.server.setHttpService(new BasicRedirectService(host, port));
        GetMethod httpget = new GetMethod("/oldlocation/");
        httpget.setFollowRedirects(false);
        try {
            this.client.executeMethod(httpget);
            assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, httpget.getStatusCode());
            assertEquals("/oldlocation/", httpget.getPath());
            assertEquals(new URI("/oldlocation/", false), httpget.getURI());
        } finally {
            httpget.releaseConnection();
        }
    }

    public void testMaxRedirectCheck() throws IOException {
        this.server.setHttpService(new CircularRedirectService());
        GetMethod httpget = new GetMethod("/circular-oldlocation/");
        try {
            this.client.getParams().setBooleanParameter(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS, true);
            this.client.getParams().setIntParameter(HttpClientParams.MAX_REDIRECTS, 5);
            this.client.executeMethod(httpget);
            fail("RedirectException exception should have been thrown");
        }
        catch (RedirectException e) {
            // expected
        } finally {
            httpget.releaseConnection();
        }
    }

    public void testCircularRedirect() throws IOException {
        this.server.setHttpService(new CircularRedirectService());
        GetMethod httpget = new GetMethod("/circular-oldlocation/");
        try {
            this.client.getParams().setBooleanParameter(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS, false);
            this.client.executeMethod(httpget);
            fail("CircularRedirectException exception should have been thrown");
        } catch (CircularRedirectException expected) {
        } finally {
            httpget.releaseConnection();
        }
    }

    public void testPostRedirect() throws IOException {
        String host = this.server.getLocalAddress();
        int port = this.server.getLocalPort();
        this.server.setHttpService(new BasicRedirectService(host, port));
        PostMethod httppost = new PostMethod("/oldlocation/");
        httppost.setRequestEntity(new StringRequestEntity("stuff", null, null));
        try {
            this.client.executeMethod(httppost);
            assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, httppost.getStatusCode());
            assertEquals("/oldlocation/", httppost.getPath());
            assertEquals(new URI("/oldlocation/", false), httppost.getURI());
        } finally {
            httppost.releaseConnection();
        }
    }

    public void testRelativeRedirect() throws IOException {
        String host = this.server.getLocalAddress();
        int port = this.server.getLocalPort();
        this.server.setHttpService(new RelativeRedirectService());
        this.client.getParams().setBooleanParameter(
                HttpClientParams.REJECT_RELATIVE_REDIRECT, false);
        GetMethod httpget = new GetMethod("/oldlocation/");
        httpget.setFollowRedirects(true);
        try {
            this.client.executeMethod(httpget);
            assertEquals("/relativelocation/", httpget.getPath());
            assertEquals(host, httpget.getURI().getHost());
            assertEquals(port, httpget.getURI().getPort());
            assertEquals(new URI("http://" + host + ":" + port + "/relativelocation/", false), 
                    httpget.getURI());
        } finally {
            httpget.releaseConnection();
        }
    }

    public void testRejectRelativeRedirect() throws IOException {
        String host = this.server.getLocalAddress();
        int port = this.server.getLocalPort();
        this.server.setHttpService(new RelativeRedirectService());
        this.client.getParams().setBooleanParameter(
                HttpClientParams.REJECT_RELATIVE_REDIRECT, true);
        GetMethod httpget = new GetMethod("/oldlocation/");
        httpget.setFollowRedirects(true);
        try {
            this.client.executeMethod(httpget);
            assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, httpget.getStatusCode());
            assertEquals("/oldlocation/", httpget.getPath());
            assertEquals(new URI("/oldlocation/", false), httpget.getURI());
        } finally {
            httpget.releaseConnection();
        }
    }

    public void testRejectBogusRedirectLocation() throws IOException {
        String host = this.server.getLocalAddress();
        int port = this.server.getLocalPort();
        this.server.setHttpService(new BogusRedirectService("xxx://bogus"));
        GetMethod httpget = new GetMethod("/oldlocation/");
        httpget.setFollowRedirects(true);
        try {
            this.client.executeMethod(httpget);
            fail("BogusRedirectService should have been thrown");
        } catch (IllegalStateException e) {
            //expected
        } finally {
            httpget.releaseConnection();
        }
    }

    public void testRejectInvalidRedirectLocation() throws IOException {
        String host = this.server.getLocalAddress();
        int port = this.server.getLocalPort();
        this.server.setHttpService(new BogusRedirectService("http://"+ host +":"+ port +"/newlocation/?p=I have spaces"));
        GetMethod httpget = new GetMethod("/oldlocation/");
        httpget.setFollowRedirects(true);
        try {
            this.client.executeMethod(httpget);
            fail("InvalidRedirectLocationException should have been thrown");
        } catch (InvalidRedirectLocationException e) {
            //expected a protocol exception
        } finally {
            httpget.releaseConnection();
        }
    }

    public void testCrossSiteRedirect() throws IOException {
        String host = this.server.getLocalAddress();
        int port = this.server.getLocalPort();
        
        SimpleHttpServer thatserver = new SimpleHttpServer();
        this.server.setHttpService(new BasicRedirectService(host, port));
        thatserver.setHttpService(new BasicRedirectService(host, port));
        thatserver.setTestname(getName());
        
        HostConfiguration hostconfig = new HostConfiguration();
        hostconfig.setHost(
                thatserver.getLocalAddress(), 
                thatserver.getLocalPort(),
                Protocol.getProtocol("http"));

        GetMethod httpget = new GetMethod("/oldlocation/");
        httpget.setFollowRedirects(true);
        try {
            this.client.executeMethod(hostconfig, httpget);
            assertEquals(HttpStatus.SC_OK, httpget.getStatusCode());
            assertEquals("/newlocation/", httpget.getPath());
            assertEquals(host, httpget.getURI().getHost());
            assertEquals(port, httpget.getURI().getPort());
            assertEquals(new URI("http://" + host + ":" + port + "/newlocation/", false), 
                    httpget.getURI());
        } finally {
            httpget.releaseConnection();
        }
        thatserver.destroy();
    }

    public void testRedirectWithCookie() throws IOException {
        
        client.getState().addCookie(new Cookie("localhost", "name", "value", "/", -1, false)); 

        String host = this.server.getLocalAddress();
        int port = this.server.getLocalPort();

        this.server.setHttpService(new BasicRedirectService(host, port));
        GetMethod httpget = new GetMethod("/oldlocation/");
        httpget.setFollowRedirects(true);
        try {
            this.client.executeMethod(httpget);
            assertEquals(HttpStatus.SC_OK, httpget.getStatusCode());
            assertEquals("/newlocation/", httpget.getPath());

            Header[] headers = httpget.getRequestHeaders();
            int cookiecount = 0;
            for (int i = 0; i < headers.length; i++) {
                if ("cookie".equalsIgnoreCase(headers[i].getName())) {
                    ++cookiecount;
                }
            }
            assertEquals("There can only be one (cookie)", 1, cookiecount);            
        } finally {
            httpget.releaseConnection();
        }
    }
}
