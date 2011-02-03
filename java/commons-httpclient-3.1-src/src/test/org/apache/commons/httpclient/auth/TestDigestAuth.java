/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/auth/TestDigestAuth.java,v 1.2 2004/11/07 12:31:42 olegk Exp $
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
import java.util.Map;

import org.apache.commons.httpclient.FakeHttpMethod;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.server.HttpService;
import org.apache.commons.httpclient.server.RequestLine;
import org.apache.commons.httpclient.server.SimpleHttpServer;
import org.apache.commons.httpclient.server.SimpleRequest;
import org.apache.commons.httpclient.server.SimpleResponse;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test Methods for DigestScheme Authentication.
 *
 * @author Rodney Waldhoff
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 */
public class TestDigestAuth extends TestCase {

    // ------------------------------------------------------------ Constructor
    public TestDigestAuth(String testName) {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestDigestAuth.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestDigestAuth.class);
    }

    public void testDigestAuthenticationWithNoRealm() throws Exception {
        String challenge = "Digest";
        try {
            AuthScheme authscheme = new DigestScheme();
            authscheme.processChallenge(challenge);
            fail("Should have thrown MalformedChallengeException");
        } catch(MalformedChallengeException e) {
            // expected
        }
    }

    public void testDigestAuthenticationWithNoRealm2() throws Exception {
        String challenge = "Digest ";
        try {
            AuthScheme authscheme = new DigestScheme();
            authscheme.processChallenge(challenge);
            fail("Should have thrown MalformedChallengeException");
        } catch(MalformedChallengeException e) {
            // expected
        }
    }

    public void testDigestAuthenticationWithDefaultCreds() throws Exception {
        String challenge = "Digest realm=\"realm1\", nonce=\"f2a3f18799759d4f1a1c068b92b573cb\"";
        FakeHttpMethod method = new FakeHttpMethod("/");
        UsernamePasswordCredentials cred = new UsernamePasswordCredentials("username","password");
        AuthScheme authscheme = new DigestScheme();
        authscheme.processChallenge(challenge);
        String response = authscheme.authenticate(cred, method);
        Map table = AuthChallengeParser.extractParams(response);
        assertEquals("username", table.get("username"));
        assertEquals("realm1", table.get("realm"));
        assertEquals("/", table.get("uri"));
        assertEquals("f2a3f18799759d4f1a1c068b92b573cb", table.get("nonce"));
        assertEquals("e95a7ddf37c2eab009568b1ed134f89a", table.get("response"));
    }

    public void testDigestAuthentication() throws Exception {
        String challenge = "Digest realm=\"realm1\", nonce=\"f2a3f18799759d4f1a1c068b92b573cb\"";
        UsernamePasswordCredentials cred = new UsernamePasswordCredentials("username","password");
        FakeHttpMethod method = new FakeHttpMethod("/");
        AuthScheme authscheme = new DigestScheme();
        authscheme.processChallenge(challenge);
        String response = authscheme.authenticate(cred, method);
        Map table = AuthChallengeParser.extractParams(response);
        assertEquals("username", table.get("username"));
        assertEquals("realm1", table.get("realm"));
        assertEquals("/", table.get("uri"));
        assertEquals("f2a3f18799759d4f1a1c068b92b573cb", table.get("nonce"));
        assertEquals("e95a7ddf37c2eab009568b1ed134f89a", table.get("response"));
    }

    public void testDigestAuthenticationWithQueryStringInDigestURI() throws Exception {
        String challenge = "Digest realm=\"realm1\", nonce=\"f2a3f18799759d4f1a1c068b92b573cb\"";
        UsernamePasswordCredentials cred = new UsernamePasswordCredentials("username","password");
        FakeHttpMethod method = new FakeHttpMethod("/");
        method.setQueryString("param=value");
        AuthScheme authscheme = new DigestScheme();
        authscheme.processChallenge(challenge);
        String response = authscheme.authenticate(cred, method);
        Map table = AuthChallengeParser.extractParams(response);
        assertEquals("username", table.get("username"));
        assertEquals("realm1", table.get("realm"));
        assertEquals("/?param=value", table.get("uri"));
        assertEquals("f2a3f18799759d4f1a1c068b92b573cb", table.get("nonce"));
        assertEquals("a847f58f5fef0bc087bcb9c3eb30e042", table.get("response"));
    }

    public void testDigestAuthenticationWithMultipleRealms() throws Exception {
        String challenge1 = "Digest realm=\"realm1\", nonce=\"abcde\"";
        String challenge2 = "Digest realm=\"realm2\", nonce=\"123546\"";
        UsernamePasswordCredentials cred = new UsernamePasswordCredentials("username","password");
        UsernamePasswordCredentials cred2 = new UsernamePasswordCredentials("uname2","password2");

        FakeHttpMethod method = new FakeHttpMethod("/");
        AuthScheme authscheme1 = new DigestScheme();
        authscheme1.processChallenge(challenge1);
        String response1 = authscheme1.authenticate(cred, method);
        Map table = AuthChallengeParser.extractParams(response1);
        assertEquals("username", table.get("username"));
        assertEquals("realm1", table.get("realm"));
        assertEquals("/", table.get("uri"));
        assertEquals("abcde", table.get("nonce"));
        assertEquals("786f500303eac1478f3c2865e676ed68", table.get("response"));

        AuthScheme authscheme2 = new DigestScheme();
        authscheme2.processChallenge(challenge2);
        String response2 = authscheme2.authenticate(cred2, method);
        table = AuthChallengeParser.extractParams(response2);
        assertEquals("uname2", table.get("username"));
        assertEquals("realm2", table.get("realm"));
        assertEquals("/", table.get("uri"));
        assertEquals("123546", table.get("nonce"));
        assertEquals("0283edd9ef06a38b378b3b74661391e9", table.get("response"));
    }

    /** 
     * Test digest authentication using the MD5-sess algorithm.
     */
    public void testDigestAuthenticationMD5Sess() throws Exception {
        // Example using Digest auth with MD5-sess

        String realm="realm";
        String username="username";
        String password="password";
        String nonce="e273f1776275974f1a120d8b92c5b3cb";

        String challenge="Digest realm=\"" + realm + "\", "
            + "nonce=\"" + nonce + "\", "
            + "opaque=\"SomeString\", "
            + "stale=false, "
            + "algorithm=MD5-sess, "
            + "qop=\"auth,auth-int\""; // we pass both but expect auth to be used

        UsernamePasswordCredentials cred =
            new UsernamePasswordCredentials(username, password);
        FakeHttpMethod method = new FakeHttpMethod("/");

        AuthScheme authscheme = new DigestScheme();
        authscheme.processChallenge(challenge);
        String response = authscheme.authenticate(cred, method);
        assertTrue(response.indexOf("nc=00000001") > 0); // test for quotes
        assertTrue(response.indexOf("qop=auth") > 0); // test for quotes
        Map table = AuthChallengeParser.extractParams(response);
        assertEquals(username, table.get("username"));
        assertEquals(realm, table.get("realm"));
        assertEquals("MD5-sess", table.get("algorithm"));
        assertEquals("/", table.get("uri"));
        assertEquals(nonce, table.get("nonce"));
        assertEquals(1, Integer.parseInt((String) table.get("nc"),16));
        assertTrue(null != table.get("cnonce"));
        assertEquals("SomeString", table.get("opaque"));
        assertEquals("auth", table.get("qop"));
        //@TODO: add better check
        assertTrue(null != table.get("response")); 
    }

    /** 
     * Test digest authentication using the MD5-sess algorithm.
     */
    public void testDigestAuthenticationMD5SessNoQop() throws Exception {
        // Example using Digest auth with MD5-sess

        String realm="realm";
        String username="username";
        String password="password";
        String nonce="e273f1776275974f1a120d8b92c5b3cb";

        String challenge="Digest realm=\"" + realm + "\", "
            + "nonce=\"" + nonce + "\", "
            + "opaque=\"SomeString\", "
            + "stale=false, "
            + "algorithm=MD5-sess";

        UsernamePasswordCredentials cred =
            new UsernamePasswordCredentials(username, password);
        FakeHttpMethod method = new FakeHttpMethod("/");

        AuthScheme authscheme = new DigestScheme();
        authscheme.processChallenge(challenge);
        String response = authscheme.authenticate(cred, method);

        Map table = AuthChallengeParser.extractParams(response);
        assertEquals(username, table.get("username"));
        assertEquals(realm, table.get("realm"));
        assertEquals("MD5-sess", table.get("algorithm"));
        assertEquals("/", table.get("uri"));
        assertEquals(nonce, table.get("nonce"));
        assertTrue(null == table.get("nc"));
        assertEquals("SomeString", table.get("opaque"));
        assertTrue(null == table.get("qop"));
        //@TODO: add better check
        assertTrue(null != table.get("response")); 
    }

    /** 
     * Test digest authentication with invalud qop value
     */
    public void testDigestAuthenticationMD5SessInvalidQop() throws Exception {
        // Example using Digest auth with MD5-sess

        String realm="realm";
        String username="username";
        String password="password";
        String nonce="e273f1776275974f1a120d8b92c5b3cb";

        String challenge="Digest realm=\"" + realm + "\", "
            + "nonce=\"" + nonce + "\", "
            + "opaque=\"SomeString\", "
            + "stale=false, "
            + "algorithm=MD5-sess, "
            + "qop=\"jakarta\""; // jakarta is an invalid qop value

        UsernamePasswordCredentials cred =
            new UsernamePasswordCredentials(username, password);
        try {
            AuthScheme authscheme = new DigestScheme();
            authscheme.processChallenge(challenge);
            fail("MalformedChallengeException exception expected due to invalid qop value");
        } catch(MalformedChallengeException e) {
        }
    }

    private class StaleNonceService implements HttpService {

        public StaleNonceService() {
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
                response.addHeader(new Header("WWW-Authenticate", 
                        "Digest realm=\"realm1\", nonce=\"ABC123\""));
                response.setBodyString("Authorization required");
                return true;
            } else {
                Map table = AuthChallengeParser.extractParams(auth.getValue());
                String nonce = (String)table.get("nonce");
                if (nonce.equals("ABC123")) {
                    response.setStatusLine(ver, HttpStatus.SC_UNAUTHORIZED);
                    response.addHeader(new Header("WWW-Authenticate", 
                            "Digest realm=\"realm1\", nonce=\"321CBA\", stale=\"true\""));
                    response.setBodyString("Authorization required");
                    return true;
                } else {
                    response.setStatusLine(ver, HttpStatus.SC_OK);
                    response.setBodyString("Authorization successful");
                    return true;
                }
            }
        }
    }

    
    public void testDigestAuthenticationWithStaleNonce() throws Exception {
        // configure the server
        SimpleHttpServer server = new SimpleHttpServer(); // use arbitrary port
        server.setTestname(getName());
        server.setHttpService(new StaleNonceService());

        // configure the client
        HttpClient client = new HttpClient();
        client.getHostConfiguration().setHost(
                server.getLocalAddress(), server.getLocalPort(),
                Protocol.getProtocol("http"));
        
        client.getState().setCredentials(AuthScope.ANY, 
                new UsernamePasswordCredentials("username","password"));
        
        FakeHttpMethod httpget = new FakeHttpMethod("/");
        try {
            client.executeMethod(httpget);
        } finally {
            httpget.releaseConnection();
        }
        assertNotNull(httpget.getStatusLine());
        assertEquals(HttpStatus.SC_OK, httpget.getStatusLine().getStatusCode());
        Map table = AuthChallengeParser.extractParams(
                httpget.getRequestHeader("Authorization").getValue());
        assertEquals("username", table.get("username"));
        assertEquals("realm1", table.get("realm"));
        assertEquals("/", table.get("uri"));
        assertEquals("321CBA", table.get("nonce"));
        assertEquals("7f5948eefa115296e9279225041527b3", table.get("response"));
        server.destroy();
    }

}
