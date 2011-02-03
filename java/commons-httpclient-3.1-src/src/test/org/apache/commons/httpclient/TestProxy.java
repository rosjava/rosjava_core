/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/TestProxy.java,v 1.11 2004/12/11 22:35:26 olegk Exp $
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
import java.util.Enumeration;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.server.AuthRequestHandler;
import org.apache.commons.httpclient.server.HttpRequestHandlerChain;
import org.apache.commons.httpclient.server.HttpServiceHandler;

/**
 * Tests for proxied connections.
 * 
 * @author Ortwin Glueck
 * @author Oleg Kalnichevski
 */
public class TestProxy extends HttpClientTestBase {

    public TestProxy(String testName) throws IOException {
        super(testName);
        setUseProxy(true);
    }

    static class SSLDecorator extends TestSetup {

        public static void addTests(TestSuite suite) {
            TestSuite ts2 = new TestSuite();
            addTest(ts2, suite);
            suite.addTest(ts2);        
        }
        
        private static void addTest(TestSuite suite, Test t) {
            if (t instanceof TestProxy) {
                suite.addTest(new SSLDecorator((TestProxy) t));
            } else if (t instanceof TestSuite) {
                Enumeration en = ((TestSuite) t).tests();
                while (en.hasMoreElements()) {
                    addTest(suite, (Test) en.nextElement());
                }
            }
        }
        
        public SSLDecorator(TestProxy test) {
            super(test);
        }
                
        protected void setUp() throws Exception {
            TestProxy base = (TestProxy)getTest();
            base.setUseSSL(true);
        }  
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(TestProxy.class);
        SSLDecorator.addTests(suite);
        return suite;
    }

    class GetItWrongThenGetItRight implements CredentialsProvider {
        
        private int hostcount = 0;
        private int proxycount = 0;
        
        public GetItWrongThenGetItRight() {
            super();
        }
        
        public Credentials getCredentials(AuthScheme scheme, String host, int port, boolean proxy)
                throws CredentialsNotAvailableException {
            if (!proxy) {
                this.hostcount++;
                return provideCredentials(this.hostcount);
            } else {
                this.proxycount++;
                return provideCredentials(this.proxycount);
            }
        }
        
        private Credentials provideCredentials(int count) {
            switch (count) {
            case 1: 
                return new UsernamePasswordCredentials("testuser", "wrongstuff");
            case 2: 
                return new UsernamePasswordCredentials("testuser", "testpass");
            default:
                return null;
            }
        }

    }
    
    /**
     * Tests GET via non-authenticating proxy
     */
    public void testSimpleGet() throws Exception {
        this.server.setHttpService(new FeedbackService());
        GetMethod get = new GetMethod("/");
        try {
            this.client.executeMethod(get);
            assertEquals(HttpStatus.SC_OK, get.getStatusCode());
        } finally {
            get.releaseConnection();
        }
    }
    
    /**
     * Tests GET via non-authenticating proxy + host auth + connection keep-alive 
     */
    public void testGetHostAuthConnKeepAlive() throws Exception {

        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        this.client.getState().setCredentials(AuthScope.ANY, creds);
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds, "test", true));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));
        
        this.server.setRequestHandler(handlerchain);
        
        GetMethod get = new GetMethod("/");
        try {
            this.client.executeMethod(get);
            assertEquals(HttpStatus.SC_OK, get.getStatusCode());
        } finally {
            get.releaseConnection();
        }
    }
    
    /**
     * Tests GET via non-authenticating proxy + host auth + connection close 
     */
    public void testGetHostAuthConnClose() throws Exception {

        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        this.client.getState().setCredentials(AuthScope.ANY, creds);
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds, "test", false));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));
        
        this.server.setRequestHandler(handlerchain);
        
        GetMethod get = new GetMethod("/");
        try {
            this.client.executeMethod(get);
            assertEquals(HttpStatus.SC_OK, get.getStatusCode());
        } finally {
            get.releaseConnection();
        }
    }

    /**
     * Tests GET via non-authenticating proxy + invalid host auth 
     */
    public void testGetHostInvalidAuth() throws Exception {

        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        this.client.getState().setCredentials(AuthScope.ANY, creds);
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));
        
        this.client.getState().setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("testuser", "wrongstuff"));
        
        this.server.setRequestHandler(handlerchain);
        
        GetMethod get = new GetMethod("/");
        try {
            this.client.executeMethod(get);
            assertEquals(HttpStatus.SC_UNAUTHORIZED, get.getStatusCode());
        } finally {
            get.releaseConnection();
        }
    }

    /**
     * Tests GET via non-authenticating proxy + interactive host auth + connection keep-alive 
     */
    public void testGetInteractiveHostAuthConnKeepAlive() throws Exception {

        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        this.client.getParams().setParameter(CredentialsProvider.PROVIDER, 
                new GetItWrongThenGetItRight());
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds, "test", true));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));
        
        this.server.setRequestHandler(handlerchain);
        
        GetMethod get = new GetMethod("/");
        try {
            this.client.executeMethod(get);
            assertEquals(HttpStatus.SC_OK, get.getStatusCode());
        } finally {
            get.releaseConnection();
        }
    }
   
    /**
     * Tests GET via non-authenticating proxy + interactive host auth + connection close 
     */
    public void testGetInteractiveHostAuthConnClose() throws Exception {

        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        this.client.getParams().setParameter(CredentialsProvider.PROVIDER, 
                new GetItWrongThenGetItRight());
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds, "test", false));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));
        
        this.server.setRequestHandler(handlerchain);
        
        GetMethod get = new GetMethod("/");
        try {
            this.client.executeMethod(get);
            assertEquals(HttpStatus.SC_OK, get.getStatusCode());
        } finally {
            get.releaseConnection();
        }
    }

    /**
     * Tests GET via authenticating proxy + host auth + connection keep-alive 
     */
    public void testGetProxyAuthHostAuthConnKeepAlive() throws Exception {

        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        this.client.getState().setCredentials(AuthScope.ANY, creds);
        this.client.getState().setProxyCredentials(AuthScope.ANY, creds);
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds, "test", true));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));
        
        this.server.setRequestHandler(handlerchain);
        
        this.proxy.requireAuthentication(creds, "test", true);
        
        GetMethod get = new GetMethod("/");
        try {
            this.client.executeMethod(get);
            assertEquals(HttpStatus.SC_OK, get.getStatusCode());
        } finally {
            get.releaseConnection();
        }
    }
    
    /**
     * Tests GET via authenticating proxy
     */
    public void testGetAuthProxy() throws Exception {
        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        this.client.getState().setProxyCredentials(AuthScope.ANY, creds);
        this.server.setHttpService(new FeedbackService());

        this.proxy.requireAuthentication(creds, "test", true);
        
        GetMethod get = new GetMethod("/");
        try {
            this.client.executeMethod(get);
            assertEquals(HttpStatus.SC_OK, get.getStatusCode());
        } finally {
            get.releaseConnection();
        }
    }
    
    /**
     * Tests GET via authenticating proxy + host auth + connection close 
     */
    public void testGetProxyAuthHostAuthConnClose() throws Exception {

        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        this.client.getState().setCredentials(AuthScope.ANY, creds);
        this.client.getState().setProxyCredentials(AuthScope.ANY, creds);
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds, "test", false));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));
        
        this.server.setRequestHandler(handlerchain);

        this.proxy.requireAuthentication(creds, "test", true);
        
        GetMethod get = new GetMethod("/");
        try {
            this.client.executeMethod(get);
            assertEquals(HttpStatus.SC_OK, get.getStatusCode());
        } finally {
            get.releaseConnection();
        }
    }
    
    /**
     * Tests GET via authenticating proxy + invalid host auth 
     */
    public void testGetProxyAuthHostInvalidAuth() throws Exception {

        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));
        
        this.client.getState().setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("testuser", "wrongstuff"));
        this.client.getState().setProxyCredentials(AuthScope.ANY, creds);
        
        this.server.setRequestHandler(handlerchain);
        
        this.proxy.requireAuthentication(creds, "test", true);
        
        GetMethod get = new GetMethod("/");
        try {
            this.client.executeMethod(get);
            assertEquals(HttpStatus.SC_UNAUTHORIZED, get.getStatusCode());
        } finally {
            get.releaseConnection();
        }
    }

    /**
     * Tests GET via authenticating proxy + interactive host and proxy auth + connection keep-alive 
     */
    public void testGetInteractiveProxyAuthHostAuthConnKeepAlive() throws Exception {

        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        this.client.getParams().setParameter(CredentialsProvider.PROVIDER, 
                new GetItWrongThenGetItRight());
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds, "test", true));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));
        
        this.server.setRequestHandler(handlerchain);

        this.proxy.requireAuthentication(creds, "test", true);
        
        GetMethod get = new GetMethod("/");
        try {
            this.client.executeMethod(get);
            assertEquals(HttpStatus.SC_OK, get.getStatusCode());
        } finally {
            get.releaseConnection();
        }
    }

    /**
     * Tests GET via authenticating proxy + interactive host and proxy auth + connection close 
     */
    public void testGetInteractiveProxyAuthHostAuthConnClose() throws Exception {

        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        this.client.getParams().setParameter(CredentialsProvider.PROVIDER, 
                new GetItWrongThenGetItRight());
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds, "test", false));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));
        
        this.server.setRequestHandler(handlerchain);
        
        this.proxy.requireAuthentication(creds, "test", true);
        
        GetMethod get = new GetMethod("/");
        try {
            this.client.executeMethod(get);
            assertEquals(HttpStatus.SC_OK, get.getStatusCode());
        } finally {
            get.releaseConnection();
        }
    }

    /**
     * Tests POST via non-authenticating proxy
     */
    public void testSimplePost() throws Exception {
        this.server.setHttpService(new FeedbackService());
        PostMethod post = new PostMethod("/");
        post.setRequestEntity(new StringRequestEntity("Like tons of stuff", null, null));
        try {
            this.client.executeMethod(post);
            assertEquals(HttpStatus.SC_OK, post.getStatusCode());
            assertNotNull(post.getResponseBodyAsString());
        } finally {
            post.releaseConnection();
        }
    }

    /**
     * Tests POST via non-authenticating proxy + host auth + connection keep-alive 
     */
    public void testPostHostAuthConnKeepAlive() throws Exception {
        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        this.client.getState().setCredentials(AuthScope.ANY, creds);
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds, "test", true));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));
        
        this.server.setRequestHandler(handlerchain);
        
        PostMethod post = new PostMethod("/");
        post.setRequestEntity(new StringRequestEntity("Like tons of stuff", null, null));
        try {
            this.client.executeMethod(post);
            assertEquals(HttpStatus.SC_OK, post.getStatusCode());
            assertNotNull(post.getResponseBodyAsString());
        } finally {
            post.releaseConnection();
        }
    }

    /**
     * Tests POST via non-authenticating proxy + host auth + connection close 
     */
    public void testPostHostAuthConnClose() throws Exception {
        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        this.client.getState().setCredentials(AuthScope.ANY, creds);
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds, "test", false));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));
        
        this.server.setRequestHandler(handlerchain);
        
        PostMethod post = new PostMethod("/");
        post.setRequestEntity(new StringRequestEntity("Like tons of stuff", null, null));
        try {
            this.client.executeMethod(post);
            assertEquals(HttpStatus.SC_OK, post.getStatusCode());
            assertNotNull(post.getResponseBodyAsString());
        } finally {
            post.releaseConnection();
        }
    }

    /**
     * Tests POST via non-authenticating proxy + invalid host auth 
     */
    public void testPostHostInvalidAuth() throws Exception {

        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        this.client.getState().setCredentials(AuthScope.ANY, creds);
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));
        
        this.client.getState().setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("testuser", "wrongstuff"));
        
        this.server.setRequestHandler(handlerchain);
        
        PostMethod post = new PostMethod("/");
        post.setRequestEntity(new StringRequestEntity("Like tons of stuff", null, null));
        try {
            this.client.executeMethod(post);
            assertEquals(HttpStatus.SC_UNAUTHORIZED, post.getStatusCode());
        } finally {
            post.releaseConnection();
        }
    }

    /**
     * Tests POST via non-authenticating proxy + interactive host auth + connection keep-alive 
     */
    public void testPostInteractiveHostAuthConnKeepAlive() throws Exception {
        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        this.client.getParams().setParameter(CredentialsProvider.PROVIDER, 
                new GetItWrongThenGetItRight());
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds, "test", true));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));
        
        this.server.setRequestHandler(handlerchain);
        
        PostMethod post = new PostMethod("/");
        post.setRequestEntity(new StringRequestEntity("Like tons of stuff", null, null));
        try {
            this.client.executeMethod(post);
            assertEquals(HttpStatus.SC_OK, post.getStatusCode());
            assertNotNull(post.getResponseBodyAsString());
        } finally {
            post.releaseConnection();
        }
    }

    /**
     * Tests POST via non-authenticating proxy + interactive host auth + connection close 
     */
    public void testPostInteractiveHostAuthConnClose() throws Exception {
        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        this.client.getParams().setParameter(CredentialsProvider.PROVIDER, 
                new GetItWrongThenGetItRight());
                
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds, "test", false));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));
        
        this.server.setRequestHandler(handlerchain);
        
        PostMethod post = new PostMethod("/");
        post.setRequestEntity(new StringRequestEntity("Like tons of stuff", null, null));
        try {
            this.client.executeMethod(post);
            assertEquals(HttpStatus.SC_OK, post.getStatusCode());
            assertNotNull(post.getResponseBodyAsString());
        } finally {
            post.releaseConnection();
        }
    }

    /**
     * Tests POST via authenticating proxy
     */
    public void testPostAuthProxy() throws Exception {
        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        this.client.getState().setProxyCredentials(AuthScope.ANY, creds);
        this.server.setHttpService(new FeedbackService());

        this.proxy.requireAuthentication(creds, "test", true);

        PostMethod post = new PostMethod("/");
        post.setRequestEntity(new StringRequestEntity("Like tons of stuff", null, null));
        try {
            this.client.executeMethod(post);
            assertEquals(HttpStatus.SC_OK, post.getStatusCode());
            assertNotNull(post.getResponseBodyAsString());
        } finally {
            post.releaseConnection();
        }
    }

    /**
     * Tests POST via authenticating proxy + host auth + connection keep-alive 
     */
    public void testPostProxyAuthHostAuthConnKeepAlive() throws Exception {
        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        this.client.getState().setCredentials(AuthScope.ANY, creds);
        this.client.getState().setProxyCredentials(AuthScope.ANY, creds);
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds, "test", true));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));
        
        this.server.setRequestHandler(handlerchain);
        
        this.proxy.requireAuthentication(creds, "test", true);

        PostMethod post = new PostMethod("/");
        post.setRequestEntity(new StringRequestEntity("Like tons of stuff", null, null));
        try {
            this.client.executeMethod(post);
            assertEquals(HttpStatus.SC_OK, post.getStatusCode());
            assertNotNull(post.getResponseBodyAsString());
        } finally {
            post.releaseConnection();
        }
    }

    /**
     * Tests POST via authenticating proxy + host auth + connection close 
     */
    public void testPostProxyAuthHostAuthConnClose() throws Exception {
        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        this.client.getState().setCredentials(AuthScope.ANY, creds);
        this.client.getState().setProxyCredentials(AuthScope.ANY, creds);
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds, "test", false));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));
        
        this.server.setRequestHandler(handlerchain);
        
        this.proxy.requireAuthentication(creds, "test", true);

        PostMethod post = new PostMethod("/");
        post.setRequestEntity(new StringRequestEntity("Like tons of stuff", null, null));
        try {
            this.client.executeMethod(post);
            assertEquals(HttpStatus.SC_OK, post.getStatusCode());
            assertNotNull(post.getResponseBodyAsString());
        } finally {
            post.releaseConnection();
        }
    }

    /**
     * Tests POST via non-authenticating proxy + invalid host auth 
     */
    public void testPostProxyAuthHostInvalidAuth() throws Exception {

        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        this.client.getState().setProxyCredentials(AuthScope.ANY, creds);
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));
        
        this.client.getState().setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("testuser", "wrongstuff"));
        
        this.server.setRequestHandler(handlerchain);
        
        this.proxy.requireAuthentication(creds, "test", true);

        PostMethod post = new PostMethod("/");
        post.setRequestEntity(new StringRequestEntity("Like tons of stuff", null, null));
        try {
            this.client.executeMethod(post);
            assertEquals(HttpStatus.SC_UNAUTHORIZED, post.getStatusCode());
        } finally {
            post.releaseConnection();
        }
    }

    /**
     * Tests POST via non-authenticating proxy + interactive host auth + connection keep-alive 
     */
    public void testPostInteractiveProxyAuthHostAuthConnKeepAlive() throws Exception {
        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        this.client.getParams().setParameter(CredentialsProvider.PROVIDER, 
                new GetItWrongThenGetItRight());
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds, "test", true));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));
        
        this.server.setRequestHandler(handlerchain);
        
        this.proxy.requireAuthentication(creds, "test", true);

        PostMethod post = new PostMethod("/");
        post.setRequestEntity(new StringRequestEntity("Like tons of stuff", null, null));
        try {
            this.client.executeMethod(post);
            assertEquals(HttpStatus.SC_OK, post.getStatusCode());
            assertNotNull(post.getResponseBodyAsString());
        } finally {
            post.releaseConnection();
        }
    }

    /**
     * Tests POST via non-authenticating proxy + interactive host auth + connection close 
     */
    public void testPostInteractiveProxyAuthHostAuthConnClose() throws Exception {
        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        this.client.getParams().setParameter(CredentialsProvider.PROVIDER, 
                new GetItWrongThenGetItRight());
                
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds, "test", false));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));
        
        this.server.setRequestHandler(handlerchain);
        
        this.proxy.requireAuthentication(creds, "test", true);

        PostMethod post = new PostMethod("/");
        post.setRequestEntity(new StringRequestEntity("Like tons of stuff", null, null));
        try {
            this.client.executeMethod(post);
            assertEquals(HttpStatus.SC_OK, post.getStatusCode());
            assertNotNull(post.getResponseBodyAsString());
        } finally {
            post.releaseConnection();
        }
    }

    public void testPreemptiveAuthProxy() throws Exception {
        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        this.client.getState().setProxyCredentials(AuthScope.ANY, creds);
        this.client.getParams().setAuthenticationPreemptive(true);
        this.server.setHttpService(new FeedbackService());

        this.proxy.requireAuthentication(creds, "test", true);
        
        GetMethod get = new GetMethod("/");
        try {
            this.client.executeMethod(get);
            assertEquals(HttpStatus.SC_OK, get.getStatusCode());
            if (isUseSSL()) {
                assertNull(get.getRequestHeader("Proxy-Authorization"));
            } else {
                assertNotNull(get.getRequestHeader("Proxy-Authorization"));
            }
        } finally {
            get.releaseConnection();
        }
    }

    /**
     * Tests GET via authenticating proxy + host auth + HTTP/1.0 
     */
    public void testGetProxyAuthHostAuthHTTP10() throws Exception {

        UsernamePasswordCredentials creds = 
            new UsernamePasswordCredentials("testuser", "testpass");
        
        this.client.getState().setCredentials(AuthScope.ANY, creds);
        this.client.getState().setProxyCredentials(AuthScope.ANY, creds);
        this.client.getParams().setVersion(HttpVersion.HTTP_1_0);
        
        HttpRequestHandlerChain handlerchain = new HttpRequestHandlerChain();
        handlerchain.appendHandler(new AuthRequestHandler(creds, "test", true));
        handlerchain.appendHandler(new HttpServiceHandler(new FeedbackService()));
        
        this.server.setRequestHandler(handlerchain);
        
        this.proxy.requireAuthentication(creds, "test", false);
        
        GetMethod get = new GetMethod("/");
        try {
            this.client.executeMethod(get);
            assertEquals(HttpStatus.SC_OK, get.getStatusCode());
        } finally {
            get.releaseConnection();
        }
    }
    
}
