/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/auth/TestNTLMAuth.java,v 1.2 2004/11/07 12:31:42 olegk Exp $
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

package org.apache.commons.httpclient.auth;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.FakeHttpMethod;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClientTestBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.server.HttpService;
import org.apache.commons.httpclient.server.RequestLine;
import org.apache.commons.httpclient.server.SimpleRequest;
import org.apache.commons.httpclient.server.SimpleResponse;

/**
 * Test Methods for NTLM Authentication.
 *
 * @author Rodney Waldhoff
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 * @version $Id: TestNTLMAuth.java 480424 2006-11-29 05:56:49Z bayard $
 */
public class TestNTLMAuth extends HttpClientTestBase {

    // ------------------------------------------------------------ Constructor
    public TestNTLMAuth(String testName) throws IOException {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestNTLMAuth.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestNTLMAuth.class);
    }

    // --------------------------------- 

    public void testNTLMAuthenticationResponse1() throws Exception {
        String challenge = "NTLM";
        String expected = "NTLM TlRMTVNTUAABAAAABlIAAAYABgAkAAAABAAEACAAAABIT" +
            "1NURE9NQUlO";
        NTCredentials cred = new NTCredentials("username","password", "host", "domain");
        FakeHttpMethod method = new FakeHttpMethod(); 
        AuthScheme authscheme = new NTLMScheme(challenge);
        authscheme.processChallenge(challenge);
        String response = authscheme.authenticate(cred, method);
        assertEquals(expected, response);
        assertFalse(authscheme.isComplete());
    }
    
    public void testNTLMAuthenticationResponse2() throws Exception {
        String challenge = 
            "NTLM TlRMTVNTUAACAAAACgAKADAAAAAGgoEAPc4kP4LtCV8AAAAAAAAAAJ4AngA" +
            "6AAAASU5UUkFFUEhPWAIAFABJAE4AVABSAEEARQBQAEgATwBYAAEAEgBCAE8AQQB" +
            "SAEQAUgBPAE8ATQAEACgAaQBuAHQAcgBhAGUAcABoAG8AeAAuAGUAcABoAG8AeAA" +
            "uAGMAbwBtAAMAPABCAG8AYQByAGQAcgBvAG8AbQAuAGkAbgB0AHIAYQBlAHAAaAB" +
            "vAHgALgBlAHAAaABvAHgALgBjAG8AbQAAAAAA";

        String expected = "NTLM TlRMTVNTUAADAAAAGAAYAFIAAAAAAAAAagAAAAYABgB" +
            "AAAAACAAIAEYAAAAEAAQATgAAAAAAAABqAAAABlIAAERPTUFJTlVTRVJOQU1FSE" +
            "9TVAaC+vLxUEHnUtpItj9Dp4kzwQfd61Lztg==";
        NTCredentials cred = new NTCredentials("username","password", "host", "domain");
        FakeHttpMethod method = new FakeHttpMethod(); 
        AuthScheme authscheme = new NTLMScheme(challenge);
        authscheme.processChallenge(challenge);
        String response = authscheme.authenticate(cred, method);
        assertEquals(expected, response);
        assertTrue(authscheme.isComplete());
    }

    private class NTLMAuthService implements HttpService {

        public NTLMAuthService() {
            super();
        }

        public boolean process(final SimpleRequest request, final SimpleResponse response)
            throws IOException
        {
            RequestLine requestLine = request.getRequestLine();
            HttpVersion ver = requestLine.getHttpVersion();
            Header auth = request.getFirstHeader("Authorization");
            if (auth == null) { 
                response.setStatusLine(ver, HttpStatus.SC_UNAUTHORIZED);
                response.addHeader(new Header("WWW-Authenticate", "NTLM"));
                response.setBodyString("Authorization required");
                return true;
            } else {
                String authstr = auth.getValue();
                
                if (authstr.equals("NTLM TlRMTVNTUAABAAAABlIAAAYABgAkAAAABAAEACAAAABIT1NURE9NQUlO")) {
                    response.setStatusLine(ver, HttpStatus.SC_UNAUTHORIZED);
                    response.addHeader(new Header("WWW-Authenticate", 
                            "NTLM TlRMTVNTUAACAAAAAAAAACgAAAABggAAU3J2Tm9uY2UAAAAAAAAAAA=="));
                    response.setBodyString("Authorization required");
                    return true;
                } if (authstr.equals("NTLM TlRMTVNTUAADAAAAGAAYAFIAAAAAAAAAagAAAAYABgBAAAAACAAIAEYAAAAEAAQATgAAAAAAAABqAAAABlIAAERPTUFJTlVTRVJOQU1FSE9TVJxndWIt46bHm11TPrt5Z6wrz7ziq04yRA==")) {
                    response.setStatusLine(ver, HttpStatus.SC_OK);
                    response.setBodyString("Authorization successful");
                    return true;
                } else {
                    response.setStatusLine(ver, HttpStatus.SC_UNAUTHORIZED);
                    response.addHeader(new Header("WWW-Authenticate", "NTLM"));
                    response.setBodyString("Authorization required");
                    return true;
                }
            }
        }
    }

    
    public void testNTLMAuthenticationRetry() throws Exception {

        this.server.setHttpService(new NTLMAuthService());

        // configure the client
        this.client.getHostConfiguration().setHost(
                server.getLocalAddress(), server.getLocalPort(),
                Protocol.getProtocol("http"));
        
        this.client.getState().setCredentials(AuthScope.ANY, 
                new NTCredentials("username", "password", "host", "domain"));
        
        FakeHttpMethod httpget = new FakeHttpMethod("/");
        try {
            client.executeMethod(httpget);
        } finally {
            httpget.releaseConnection();
        }
        assertNull(httpget.getResponseHeader("WWW-Authenticate"));
        assertEquals(200, httpget.getStatusCode());
    }

    private class PreemptiveNTLMAuthService implements HttpService {

        public PreemptiveNTLMAuthService() {
            super();
        }

        public boolean process(final SimpleRequest request, final SimpleResponse response)
            throws IOException
        {
            RequestLine requestLine = request.getRequestLine();
            HttpVersion ver = requestLine.getHttpVersion();
            Header auth = request.getFirstHeader("Authorization");
            if (auth == null) { 
                response.setStatusLine(ver, HttpStatus.SC_BAD_REQUEST);
                response.setBodyString("Authorization header missing");
                return true;
            } else {
                String authstr = auth.getValue();
                
                if (authstr.indexOf("NTLM") != -1) {
                    response.setStatusLine(ver, HttpStatus.SC_OK);
                    return true;
                } else if (authstr.indexOf("Basic") != -1) {
                    response.setStatusLine(ver, HttpStatus.SC_UNAUTHORIZED);
                    response.addHeader(new Header("WWW-Authenticate", "Negotiate"));
                    response.addHeader(new Header("WWW-Authenticate", "NTLM"));
                    response.setBodyString("Authorization required");
                    return true;
                } else {
                    response.setStatusLine(ver, HttpStatus.SC_BAD_REQUEST);
                    response.setBodyString("Unknown auth type: " + authstr);
                    return true;
                }
            }
        }
    }

    /**
     * Make sure preemptive authorization works when the server requires NLM.
     * @throws Exception
     */
    public void testPreemptiveAuthorization() throws Exception {

        NTCredentials creds = 
            new NTCredentials("testuser", "testpass", "host", "domain");
        
        HttpState state = new HttpState();
        state.setCredentials(AuthScope.ANY, creds);
        this.client.setState(state);
        this.client.getParams().setAuthenticationPreemptive(true);

        this.server.setHttpService(new PreemptiveNTLMAuthService());

        GetMethod httpget = new GetMethod("/test/");
        try {
            this.client.executeMethod(httpget);
        } finally {
            httpget.releaseConnection();
        }
        assertNotNull(httpget.getStatusLine());
        assertEquals(HttpStatus.SC_OK, httpget.getStatusLine().getStatusCode());
    }
    
    
}
