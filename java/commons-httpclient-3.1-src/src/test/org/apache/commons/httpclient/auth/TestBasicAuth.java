/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/auth/TestBasicAuth.java,v 1.9 2004/11/20 17:56:40 olegk Exp $
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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.EchoService;
import org.apache.commons.httpclient.FeedbackService;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClientTestBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.ProxyTestDecorator;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.server.AuthRequestHandler;
import org.apache.commons.httpclient.server.HttpRequestHandlerChain;
import org.apache.commons.httpclient.server.HttpServiceHandler;
import org.apache.commons.httpclient.util.EncodingUtil;

/**
 * Basic authentication test cases.
 *
 * @author Oleg Kalnichevski
 * 
 * @version $Id: TestBasicAuth.java 480424 2006-11-29 05:56:49Z bayard $
 */
public class TestBasicAuth extends HttpClientTestBase {

    // ------------------------------------------------------------ Constructor
    public TestBasicAuth(final String testName) throws IOException {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestBasicAuth.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        TestSuite suite = new TestSuite(TestBasicAuth.class);
        ProxyTestDecorator.addTests(suite);
        return suite;
    }

    public void testBasicAuthenticationWithNoCreds() throws IOException {

        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));
        
        this.server.setRequestHandler(handlerchain);
        GetMethod httpget = new GetMethod("/test/");
        try {
            this.client.executeMethod(httpget);
            assertNotNull(httpget.getStatusLine());
            assertEquals(HttpStatus.SC_UNAUTHORIZED, httpget.getStatusLine().getStatusCode());
            AuthState authstate = httpget.getHostAuthState();
            assertNotNull(authstate.getAuthScheme());
            assertTrue(authstate.getAuthScheme() instanceof BasicScheme);
            assertEquals("test", authstate.getRealm());
        } finally {
            httpget.releaseConnection();
        }
    }

    public void testBasicAuthenticationWithNoCredsRetry() throws IOException {
        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));
        
        this.server.setRequestHandler(handlerchain);

        GetMethod httpget = new GetMethod("/test/");
        try {
            this.client.executeMethod(httpget);
            assertNotNull(httpget.getStatusLine());
            assertEquals(HttpStatus.SC_UNAUTHORIZED, httpget.getStatusLine().getStatusCode());
            AuthState authstate = httpget.getHostAuthState();
            assertNotNull(authstate.getAuthScheme());
            assertTrue(authstate.getAuthScheme() instanceof BasicScheme);
            assertEquals("test", authstate.getRealm());
        } finally {
            httpget.releaseConnection();
        }
        // now try with credentials
        httpget = new GetMethod("/test/");
        try {
            this.client.getState().setCredentials(AuthScope.ANY, creds);
            this.client.executeMethod(httpget);
            assertNotNull(httpget.getStatusLine());
            assertEquals(HttpStatus.SC_OK, httpget.getStatusLine().getStatusCode());
        } finally {
            httpget.releaseConnection();
        }
    }
    
    public void testBasicAuthenticationWithNoRealm() {
        String challenge = "Basic";
        try {
            AuthScheme authscheme = new BasicScheme();
            authscheme.processChallenge(challenge);
            fail("Should have thrown MalformedChallengeException");
        } catch(MalformedChallengeException e) {
            // expected
        }
    }

    public void testBasicAuthenticationWith88591Chars() throws Exception {
        int[] germanChars = { 0xE4, 0x2D, 0xF6, 0x2D, 0xFc };
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < germanChars.length; i++) {
            buffer.append((char)germanChars[i]); 
        }
        
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("dh", buffer.toString());
        assertEquals("Basic ZGg65C32Lfw=", 
            BasicScheme.authenticate(credentials, "ISO-8859-1"));
    }
    
    public void testBasicAuthenticationWithDefaultCreds() throws Exception {
        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));

        HttpState state = new HttpState();
        state.setCredentials(AuthScope.ANY, creds);
        this.client.setState(state);
        
        this.server.setRequestHandler(handlerchain);

        GetMethod httpget = new GetMethod("/test/");
        try {
            this.client.executeMethod(httpget);
        } finally {
            httpget.releaseConnection();
        }
        assertNotNull(httpget.getStatusLine());
        assertEquals(HttpStatus.SC_OK, httpget.getStatusLine().getStatusCode());
        Header auth = httpget.getRequestHeader("Authorization");
        assertNotNull(auth);
        String expected = "Basic " + EncodingUtil.getAsciiString(
            Base64.encodeBase64(EncodingUtil.getAsciiBytes("testuser:testpass")));
        assertEquals(expected, auth.getValue());
        AuthState authstate = httpget.getHostAuthState();
        assertNotNull(authstate.getAuthScheme());
        assertTrue(authstate.getAuthScheme() instanceof BasicScheme);
        assertEquals("test", authstate.getRealm());
    }

    public void testBasicAuthentication() throws Exception {
        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));

        HttpState state = new HttpState();
        AuthScope authscope = new AuthScope(
            this.server.getLocalAddress(), 
            this.server.getLocalPort(),
            "test");
        state.setCredentials(authscope, creds);
        this.client.setState(state);

        this.server.setRequestHandler(handlerchain);

        GetMethod httpget = new GetMethod("/test/");
        try {
            this.client.executeMethod(httpget);
        } finally {
            httpget.releaseConnection();
        }
        assertNotNull(httpget.getStatusLine());
        assertEquals(HttpStatus.SC_OK, httpget.getStatusLine().getStatusCode());
        Header auth = httpget.getRequestHeader("Authorization");
        assertNotNull(auth);
        String expected = "Basic " + EncodingUtil.getAsciiString(
            Base64.encodeBase64(EncodingUtil.getAsciiBytes("testuser:testpass")));
        assertEquals(expected, auth.getValue());
        AuthState authstate = httpget.getHostAuthState();
        assertNotNull(authstate.getAuthScheme());
        assertTrue(authstate.getAuthScheme() instanceof BasicScheme);
        assertEquals("test", authstate.getRealm());
    }

    public void testBasicAuthenticationWithInvalidCredentials() throws Exception {
        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));

        HttpState state = new HttpState();
        AuthScope authscope = new AuthScope(
            this.server.getLocalAddress(), 
            this.server.getLocalPort(),
            "test");
        state.setCredentials(authscope, new UsernamePasswordCredentials("test", "stuff"));
        this.client.setState(state);

        this.server.setRequestHandler(handlerchain);
        
        GetMethod httpget = new GetMethod("/test/");
        try {
            this.client.executeMethod(httpget);
        } finally {
            httpget.releaseConnection();
        }
        assertNotNull(httpget.getStatusLine());
        assertEquals(HttpStatus.SC_UNAUTHORIZED, httpget.getStatusLine().getStatusCode());
        AuthState authstate = httpget.getHostAuthState();
        assertNotNull(authstate.getAuthScheme());
        assertTrue(authstate.getAuthScheme() instanceof BasicScheme);
        assertEquals("test", authstate.getRealm());
    }

    public void testBasicAuthenticationWithMutlipleRealms1() throws Exception {
        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));

        HttpState state = new HttpState();
        AuthScope realm1 = new AuthScope(
            this.server.getLocalAddress(), 
            this.server.getLocalPort(),
            "test");
        AuthScope realm2 = new AuthScope(
            this.server.getLocalAddress(), 
            this.server.getLocalPort(),
            "test2");
        state.setCredentials(realm1, new UsernamePasswordCredentials("testuser","testpass"));
        state.setCredentials(realm2, new UsernamePasswordCredentials("testuser2","testpass2"));
        this.client.setState(state);

        this.server.setRequestHandler(handlerchain);
        
        GetMethod httpget = new GetMethod("/test/");
        try {
            this.client.executeMethod(httpget);
        } finally {
            httpget.releaseConnection();
        }
        assertNotNull(httpget.getStatusLine());
        assertEquals(HttpStatus.SC_OK, httpget.getStatusLine().getStatusCode());
        Header auth = httpget.getRequestHeader("Authorization");
        assertNotNull(auth);
        String expected = "Basic " + EncodingUtil.getAsciiString(
            Base64.encodeBase64(EncodingUtil.getAsciiBytes("testuser:testpass")));
        assertEquals(expected, auth.getValue());
        AuthState authstate = httpget.getHostAuthState();
        assertNotNull(authstate.getAuthScheme());
        assertTrue(authstate.getAuthScheme() instanceof BasicScheme);
        assertEquals("test", authstate.getRealm());
    }

    public void testBasicAuthenticationWithMutlipleRealms2() throws Exception {
        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser2", "testpass2");
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds, "test2"));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));

        HttpState state = new HttpState();
        AuthScope realm1 = new AuthScope(
            this.server.getLocalAddress(), 
            this.server.getLocalPort(),
            "test");
        AuthScope realm2 = new AuthScope(
            this.server.getLocalAddress(), 
            this.server.getLocalPort(),
            "test2");
        state.setCredentials(realm1, new UsernamePasswordCredentials("testuser","testpass"));
        state.setCredentials(realm2, new UsernamePasswordCredentials("testuser2","testpass2"));
        this.client.setState(state);

        this.server.setRequestHandler(handlerchain);
        
        GetMethod httpget = new GetMethod("/test2/");
        try {
            this.client.executeMethod(httpget);
        } finally {
            httpget.releaseConnection();
        }
        assertNotNull(httpget.getStatusLine());
        assertEquals(HttpStatus.SC_OK, httpget.getStatusLine().getStatusCode());
        Header auth = httpget.getRequestHeader("Authorization");
        assertNotNull(auth);
        String expected = "Basic " + EncodingUtil.getAsciiString(
            Base64.encodeBase64(EncodingUtil.getAsciiBytes("testuser2:testpass2")));
        assertEquals(expected, auth.getValue());
        AuthState authstate = httpget.getHostAuthState();
        assertNotNull(authstate.getAuthScheme());
        assertTrue(authstate.getAuthScheme() instanceof BasicScheme);
        assertEquals("test2", authstate.getRealm());
    }

    public void testPreemptiveAuthorizationTrueWithCreds() throws Exception {
        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));

        HttpState state = new HttpState();
        state.setCredentials(AuthScope.ANY, creds);
        this.client.setState(state);
        this.client.getParams().setAuthenticationPreemptive(true);
        
        this.server.setRequestHandler(handlerchain);

        GetMethod httpget = new GetMethod("/test/");
        try {
            this.client.executeMethod(httpget);
        } finally {
            httpget.releaseConnection();
        }
        assertNotNull(httpget.getStatusLine());
        assertEquals(HttpStatus.SC_OK, httpget.getStatusLine().getStatusCode());
        Header auth = httpget.getRequestHeader("Authorization");
        assertNotNull(auth);
        String expected = "Basic " + EncodingUtil.getAsciiString(
            Base64.encodeBase64(EncodingUtil.getAsciiBytes("testuser:testpass")));
        assertEquals(expected, auth.getValue());
        AuthState authstate = httpget.getHostAuthState();
        assertNotNull(authstate.getAuthScheme());
        assertTrue(authstate.getAuthScheme() instanceof BasicScheme);
        assertNull(authstate.getRealm());
        assertTrue(authstate.isPreemptive());
    }

    public void testPreemptiveAuthorizationTrueWithoutCreds() throws Exception {
        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));

        HttpState state = new HttpState();
        this.client.setState(state);
        this.client.getParams().setAuthenticationPreemptive(true);
        
        this.server.setRequestHandler(handlerchain);

        GetMethod httpget = new GetMethod("/test/");
        try {
            this.client.executeMethod(httpget);
        } finally {
            httpget.releaseConnection();
        }
        assertNotNull(httpget.getStatusLine());
        assertEquals(HttpStatus.SC_UNAUTHORIZED, httpget.getStatusLine().getStatusCode());
        Header auth = httpget.getRequestHeader("Authorization");
        assertNull(auth);
        AuthState authstate = httpget.getHostAuthState();
        assertNotNull(authstate.getAuthScheme());
        assertTrue(authstate.getAuthScheme() instanceof BasicScheme);
        assertNotNull(authstate.getRealm());
        assertTrue(authstate.isPreemptive());
    }

    public void testCustomAuthorizationHeader() throws Exception {
        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));

        this.server.setRequestHandler(handlerchain);

        GetMethod httpget = new GetMethod("/test/");
        String authResponse = "Basic " + EncodingUtil.getAsciiString(
                Base64.encodeBase64(EncodingUtil.getAsciiBytes("testuser:testpass")));
        httpget.addRequestHeader(new Header("Authorization", authResponse));
        try {
            this.client.executeMethod(httpget);
        } finally {
            httpget.releaseConnection();
        }
        assertNotNull(httpget.getStatusLine());
        assertEquals(HttpStatus.SC_OK, httpget.getStatusLine().getStatusCode());
    }
    
    public void testHeadBasicAuthentication() throws Exception {
        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));

        HttpState state = new HttpState();
        AuthScope authscope = new AuthScope(
            this.server.getLocalAddress(), 
            this.server.getLocalPort(),
            "test");
        state.setCredentials(authscope, creds);
        this.client.setState(state);

        this.server.setRequestHandler(handlerchain);

        HeadMethod head = new HeadMethod("/test/");
        try {
            this.client.executeMethod(head);
        } finally {
            head.releaseConnection();
        }
        assertNotNull(head.getStatusLine());
        assertEquals(HttpStatus.SC_OK, head.getStatusLine().getStatusCode());
        Header auth = head.getRequestHeader("Authorization");
        assertNotNull(auth);
        String expected = "Basic " + EncodingUtil.getAsciiString(
            Base64.encodeBase64(EncodingUtil.getAsciiBytes("testuser:testpass")));
        assertEquals(expected, auth.getValue());
        AuthState authstate = head.getHostAuthState();
        assertNotNull(authstate.getAuthScheme());
        assertTrue(authstate.getAuthScheme() instanceof BasicScheme);
        assertEquals("test", authstate.getRealm());
    }

    public void testPostBasicAuthentication() throws Exception {
        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds));
        handlerchain.appendHandler(new HttpServiceHandler(new EchoService()));

        HttpState state = new HttpState();
        AuthScope authscope = new AuthScope(
            this.server.getLocalAddress(), 
            this.server.getLocalPort(),
            "test");
        state.setCredentials(authscope, creds);
        this.client.setState(state);

        this.server.setRequestHandler(handlerchain);

        PostMethod post = new PostMethod("/test/");
        post.setRequestEntity(new StringRequestEntity("Test body", null, null));
        try {
            this.client.executeMethod(post);
            assertEquals("Test body", post.getResponseBodyAsString());
        } finally {
            post.releaseConnection();
        }
        assertNotNull(post.getStatusLine());
        assertEquals(HttpStatus.SC_OK, post.getStatusLine().getStatusCode());
        Header auth = post.getRequestHeader("Authorization");
        assertNotNull(auth);
        String expected = "Basic " + EncodingUtil.getAsciiString(
            Base64.encodeBase64(EncodingUtil.getAsciiBytes("testuser:testpass")));
        assertEquals(expected, auth.getValue());
        AuthState authstate = post.getHostAuthState();
        assertNotNull(authstate.getAuthScheme());
        assertTrue(authstate.getAuthScheme() instanceof BasicScheme);
        assertEquals("test", authstate.getRealm());
    }
    
    public void testPutBasicAuthentication() throws Exception {
        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds));
        handlerchain.appendHandler(new HttpServiceHandler(new EchoService()));

        HttpState state = new HttpState();
        AuthScope authscope = new AuthScope(
            this.server.getLocalAddress(), 
            this.server.getLocalPort(),
            "test");
        state.setCredentials(authscope, creds);
        this.client.setState(state);

        this.server.setRequestHandler(handlerchain);

        PutMethod put = new PutMethod("/test/");
        put.setRequestEntity(new StringRequestEntity("Test body", null, null));
        try {
            this.client.executeMethod(put);
            assertEquals("Test body", put.getResponseBodyAsString());
        } finally {
            put.releaseConnection();
        }
        assertNotNull(put.getStatusLine());
        assertEquals(HttpStatus.SC_OK, put.getStatusLine().getStatusCode());
        Header auth = put.getRequestHeader("Authorization");
        assertNotNull(auth);
        String expected = "Basic " + EncodingUtil.getAsciiString(
            Base64.encodeBase64(EncodingUtil.getAsciiBytes("testuser:testpass")));
        assertEquals(expected, auth.getValue());
        AuthState authstate = put.getHostAuthState();
        assertNotNull(authstate.getAuthScheme());
        assertTrue(authstate.getAuthScheme() instanceof BasicScheme);
        assertEquals("test", authstate.getRealm());
    }

    public void testPreemptiveAuthorizationFailure() throws Exception {
        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        UsernamePasswordCredentials wrongcreds = 
            new UsernamePasswordCredentials("testuser", "garbage");
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));

        HttpState state = new HttpState();
        state.setCredentials(AuthScope.ANY, wrongcreds);
        this.client.setState(state);
        this.client.getParams().setAuthenticationPreemptive(true);
        
        this.server.setRequestHandler(handlerchain);

        GetMethod httpget = new GetMethod("/test/");
        try {
            this.client.executeMethod(httpget);
        } finally {
            httpget.releaseConnection();
        }
        assertNotNull(httpget.getStatusLine());
        assertEquals(HttpStatus.SC_UNAUTHORIZED, httpget.getStatusLine().getStatusCode());
        AuthState authstate = httpget.getHostAuthState();
        assertNotNull(authstate.getAuthScheme());
        assertTrue(authstate.getAuthScheme() instanceof BasicScheme);
        assertEquals("test", authstate.getRealm());
        assertTrue(authstate.isPreemptive());
    }
    
}
