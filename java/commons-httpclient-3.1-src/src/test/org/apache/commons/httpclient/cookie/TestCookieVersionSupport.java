/*
 * $HeadURL: https://svn.apache.org/repos/asf/jakarta/httpcomponents/oac.hc3x/tags/HTTPCLIENT_3_1/src/test/org/apache/commons/httpclient/cookie/TestCookieVersionSupport.java $
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

package org.apache.commons.httpclient.cookie;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClientTestBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.server.HttpService;
import org.apache.commons.httpclient.server.SimpleRequest;
import org.apache.commons.httpclient.server.SimpleResponse;

/**
 * Cookie version support tests.
 *
 * @author Oleg Kalnichevski
 * 
 * @version $Revision: 480424 $
 */
public class TestCookieVersionSupport extends HttpClientTestBase {

    // ------------------------------------------------------------ Constructor
    public TestCookieVersionSupport(final String testName) throws IOException {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestCookieVersionSupport.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestCookieVersionSupport.class);
    }

    private static class CookieVer0Service implements HttpService {

        public CookieVer0Service() {
            super();
        }

        public boolean process(final SimpleRequest request, final SimpleResponse response)
            throws IOException
        {
            HttpVersion httpversion = request.getRequestLine().getHttpVersion();
            response.setStatusLine(httpversion, HttpStatus.SC_OK);
            response.addHeader(new Header("Set-Cookie", "name1=value1; path=/test"));
            response.setBodyString("whatever");
            return true;
        }
    }
    
    
    public void testCookieVersionSupportHeader1() throws IOException {
        this.server.setHttpService(new CookieVer0Service());
        this.client.getParams().setCookiePolicy(CookiePolicy.RFC_2965);
        GetMethod httpget1 = new GetMethod("/test/");
        try {
            this.client.executeMethod(httpget1);
        } finally {
            httpget1.releaseConnection();
        }
        GetMethod httpget2 = new GetMethod("/test/");
        try {
            this.client.executeMethod(httpget2);
        } finally {
            httpget2.releaseConnection();
        }
        Header cookiesupport = httpget2.getRequestHeader("Cookie2");
        assertNotNull(cookiesupport);
        assertEquals("$Version=\"1\"", cookiesupport.getValue());
    }
    
    private static class CookieVer1Service implements HttpService {

        public CookieVer1Service() {
            super();
        }

        public boolean process(final SimpleRequest request, final SimpleResponse response)
            throws IOException
        {
            HttpVersion httpversion = request.getRequestLine().getHttpVersion();
            response.setStatusLine(httpversion, HttpStatus.SC_OK);
            response.addHeader(new Header("Set-Cookie", "name1=value1; Path=\"/test\"; Version=\"1\""));
            response.addHeader(new Header("Set-Cookie2", "name2=value2; Path=\"/test\"; Version=\"1\""));
            response.setBodyString("whatever");
            return true;
        }
    }
    
    
    public void testCookieVersionSupportHeader2() throws IOException {
        this.server.setHttpService(new CookieVer1Service());
        this.client.getParams().setCookiePolicy(CookiePolicy.RFC_2965);
        GetMethod httpget1 = new GetMethod("/test/");
        try {
            this.client.executeMethod(httpget1);
        } finally {
            httpget1.releaseConnection();
        }
        GetMethod httpget2 = new GetMethod("/test/");
        try {
            this.client.executeMethod(httpget2);
        } finally {
            httpget2.releaseConnection();
        }
        Header cookiesupport = httpget2.getRequestHeader("Cookie2");
        assertNull(cookiesupport);
    }

    private static class CookieVer2Service implements HttpService {

        public CookieVer2Service() {
            super();
        }

        public boolean process(final SimpleRequest request, final SimpleResponse response)
            throws IOException
        {
            HttpVersion httpversion = request.getRequestLine().getHttpVersion();
            response.setStatusLine(httpversion, HttpStatus.SC_OK);
            response.addHeader(new Header("Set-Cookie2", "name2=value2; Path=\"/test\"; Version=\"2\""));
            response.setBodyString("whatever");
            return true;
        }
    }
    
    
    public void testCookieVersionSupportHeader3() throws IOException {
        this.server.setHttpService(new CookieVer2Service());
        this.client.getParams().setCookiePolicy(CookiePolicy.RFC_2965);
        GetMethod httpget1 = new GetMethod("/test/");
        try {
            this.client.executeMethod(httpget1);
        } finally {
            httpget1.releaseConnection();
        }
        GetMethod httpget2 = new GetMethod("/test/");
        try {
            this.client.executeMethod(httpget2);
        } finally {
            httpget2.releaseConnection();
        }
        Header cookiesupport = httpget2.getRequestHeader("Cookie2");
        assertNotNull(cookiesupport);
        assertEquals("$Version=\"1\"", cookiesupport.getValue());
    }

    private static class SetCookieVersionMixService implements HttpService {

        public SetCookieVersionMixService() {
            super();
        }

        public boolean process(final SimpleRequest request, final SimpleResponse response)
            throws IOException
        {
            HttpVersion httpversion = request.getRequestLine().getHttpVersion();
            response.setStatusLine(httpversion, HttpStatus.SC_OK);
            response.addHeader(new Header("Set-Cookie", "name=wrong; Path=/test"));
            response.addHeader(new Header("Set-Cookie2", "name=right; Path=\"/test\"; Version=\"1\""));
            response.setBodyString("whatever");
            return true;
        }
    }
    
    public static class TestHttpState extends HttpState {
        
        public synchronized void addCookie(Cookie cookie) {
            if (cookie != null) {
                if ("localhost.local".equals(cookie.getDomain())) {
                    cookie.setDomain("localhost");
                }
                super.addCookie(cookie);
            }
        }
    }
    
    public void testSetCookieVersionMix() throws IOException {
        this.server.setHttpService(new SetCookieVersionMixService());
        this.client.setState(new TestHttpState());
        this.client.getParams().setCookiePolicy(CookiePolicy.RFC_2965);
        GetMethod httpget1 = new GetMethod("/test/");
        try {
            this.client.executeMethod(httpget1);
        } finally {
            httpget1.releaseConnection();
        }
        Cookie[] cookies = this.client.getState().getCookies();
        assertNotNull(cookies);
        assertEquals(1, cookies.length);
        assertEquals("right", cookies[0].getValue());
        assertTrue(cookies[0] instanceof Cookie2);
    }

    
}
