/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/java/org/apache/commons/httpclient/auth/HttpAuthenticator.java,v 1.19 2004/10/06 17:32:04 olegk Exp $
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

package org.apache.commons.httpclient.auth;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility methods for HTTP authorization and authentication.  This class
 * provides utility methods for generating responses to HTTP www and proxy
 * authentication challenges.
 * 
 * <blockquote>
 * A client SHOULD assume that all paths at or deeper than the depth of the
 * last symbolic element in the path field of the Request-URI also are within
 * the protection space specified by the basic realm value of the current
 * challenge. A client MAY preemptively send the corresponding Authorization
 * header with requests for resources in that space without receipt of another
 * challenge from the server. Similarly, when a client sends a request to a
 * proxy, it may reuse a userid and password in the Proxy-Authorization header
 * field without receiving another challenge from the proxy server.
 * </blockquote>
 * </p>
 * 
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @author Rodney Waldhoff
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 * @author Ortwin Gl�ck
 * @author Sean C. Sullivan
 * @author <a href="mailto:adrian@ephox.com">Adrian Sutton</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * 
 * @deprecated no longer used
 */
public final class HttpAuthenticator {

    /** Log object for this class. */
    private static final Log LOG = LogFactory.getLog(HttpAuthenticator.class);

    /**
     * The www authenticate challange header.
     */
    public static final String WWW_AUTH = "WWW-Authenticate";

    /**
     * The www authenticate response header.
     */
    public static final String WWW_AUTH_RESP = "Authorization";

    /**
     * The proxy authenticate challange header.
     */
    public static final String PROXY_AUTH = "Proxy-Authenticate";

    /**
     * The proxy authenticate response header.
     */
    public static final String PROXY_AUTH_RESP = "Proxy-Authorization";

    /** Chooses the strongest authentication scheme supported from the
     * array of authentication challenges. Currently only <code>NTLM</code>,
     * <code>Digest</code>, <code>Basic</code> schemes are recognized. 
     * The <code>NTLM</code> scheme is considered the strongest and is 
     * preferred to all others. The <code>Digest</code> scheme is preferred to 
     * the <code>Basic</code> one which provides no encryption for credentials.
     * The <code>Basic</code> scheme is used only if it is the only one 
     * supported.
     * 
     * @param challenges The array of authentication challenges
     * 
     * @return The strongest authentication scheme supported
     * 
     * @throws MalformedChallengeException is thrown if an authentication 
     *  challenge is malformed
     * @throws UnsupportedOperationException when none of challenge types
     *  available is supported.
     * 
     * @deprecated Use {@link AuthChallengeParser#parseChallenges(Header[])} and 
     *      {@link AuthPolicy#getAuthScheme(String)}
     */
    public static AuthScheme selectAuthScheme(final Header[] challenges)
      throws MalformedChallengeException {
        LOG.trace("enter HttpAuthenticator.selectAuthScheme(Header[])");
        if (challenges == null) {
            throw new IllegalArgumentException("Array of challenges may not be null");
        }
        if (challenges.length == 0) {
            throw new IllegalArgumentException("Array of challenges may not be empty");
        }
        String challenge = null;
        Map challengemap = new HashMap(challenges.length); 
        for (int i = 0; i < challenges.length; i++) {
            challenge = challenges[i].getValue();
            String s = AuthChallengeParser.extractScheme(challenge);
            challengemap.put(s, challenge);
        }
        challenge = (String) challengemap.get("ntlm");
        if (challenge != null) {
            return new NTLMScheme(challenge);
        }
        challenge = (String) challengemap.get("digest");
        if (challenge != null) {
            return new DigestScheme(challenge);
        }
        challenge = (String) challengemap.get("basic");
        if (challenge != null) {
            return new BasicScheme(challenge);
        }
        throw new UnsupportedOperationException(
          "Authentication scheme(s) not supported: " + challengemap.toString()); 
    }
    
    private static boolean doAuthenticateDefault(
        HttpMethod method, 
        HttpConnection conn,
        HttpState state, 
        boolean proxy)
      throws AuthenticationException {
        if (method == null) {
            throw new IllegalArgumentException("HTTP method may not be null");
        }
        if (state == null) {
            throw new IllegalArgumentException("HTTP state may not be null");
        }
        String host = null;
        if (conn != null) {
            host = proxy ? conn.getProxyHost() : conn.getHost();
        }
        Credentials credentials = proxy 
            ? state.getProxyCredentials(null, host) : state.getCredentials(null, host);
        if (credentials == null) {
            return false;
        }
        if (!(credentials instanceof UsernamePasswordCredentials)) {
            throw new InvalidCredentialsException(
             "Credentials cannot be used for basic authentication: " 
              + credentials.toString());
        }
        String auth = BasicScheme.authenticate(
            (UsernamePasswordCredentials) credentials,
            method.getParams().getCredentialCharset());
        if (auth != null) {
            String s = proxy ? PROXY_AUTH_RESP : WWW_AUTH_RESP;
            Header header = new Header(s, auth, true);
            method.addRequestHeader(header);
            return true;
        } else {
            return false;
        }
    }
    
    
    /**
     * Attempt to provide default authentication credentials 
     * to the given method in the given context using basic 
     * authentication scheme.
     * 
     * @param method the HttpMethod which requires authentication
     * @param conn the connection to a specific host. This parameter 
     *   may be <tt>null</tt> if default credentials (not specific 
     *   to any particular host) are to be used
     * @param state the HttpState object providing Credentials
     * 
     * @return true if the <tt>Authenticate</tt> response header 
     *   was added
     * 
     * @throws InvalidCredentialsException if authentication credentials
     *         are not valid or not applicable for basic scheme
     * @throws AuthenticationException when a parsing or other error occurs
     *
     * @see HttpState#setCredentials(String,String,Credentials)
     * 
     * @deprecated use AuthScheme
     */
    public static boolean authenticateDefault(
        HttpMethod method, 
        HttpConnection conn,
        HttpState state)
      throws AuthenticationException {
        LOG.trace(
            "enter HttpAuthenticator.authenticateDefault(HttpMethod, HttpConnection, HttpState)");
        return doAuthenticateDefault(method, conn, state, false);
    }


    /**
     * Attempt to provide default proxy authentication credentials 
     * to the given method in the given context using basic 
     * authentication scheme.
     * 
     * @param method the HttpMethod which requires authentication
     * @param conn the connection to a specific host. This parameter 
     *   may be <tt>null</tt> if default credentials (not specific 
     *   to any particular host) are to be used
     * @param state the HttpState object providing Credentials
     * 
     * @return true if the <tt>Proxy-Authenticate</tt> response header 
     *   was added
     * 
     * @throws InvalidCredentialsException if authentication credentials
     *         are not valid or not applicable for basic scheme
     * @throws AuthenticationException when a parsing or other error occurs

     * @see HttpState#setCredentials(String,String,Credentials)
     * 
     * @deprecated use AuthScheme
     */
    public static boolean authenticateProxyDefault(
        HttpMethod method, 
        HttpConnection conn,
        HttpState state)
      throws AuthenticationException {
        LOG.trace("enter HttpAuthenticator.authenticateProxyDefault(HttpMethod, HttpState)");
        return doAuthenticateDefault(method, conn, state, true);
    }


    private static boolean doAuthenticate(
        AuthScheme authscheme, 
        HttpMethod method, 
        HttpConnection conn,
        HttpState state, 
        boolean proxy)
       throws AuthenticationException {
        if (authscheme == null) {
            throw new IllegalArgumentException("Authentication scheme may not be null");
        }
        if (method == null) {
            throw new IllegalArgumentException("HTTP method may not be null");
        }
        if (state == null) {
            throw new IllegalArgumentException("HTTP state may not be null");
        }
        String host = null;
        if (conn != null) {
            if (proxy) {
                host = conn.getProxyHost();
            } else {
                host = method.getParams().getVirtualHost();
                if (host == null) {
                    host = conn.getHost();
                }
            }
        }
        String realm = authscheme.getRealm();
        if (LOG.isDebugEnabled()) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("Using credentials for ");
            if (realm == null) {
                buffer.append("default");
            } else {
                buffer.append('\'');
                buffer.append(realm);
                buffer.append('\'');
            }
            buffer.append(" authentication realm at "); 
            buffer.append(host); 
            LOG.debug(buffer.toString());
        }
        Credentials credentials = proxy 
            ? state.getProxyCredentials(realm, host) 
            : state.getCredentials(realm, host);
        if (credentials == null) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("No credentials available for the "); 
            if (realm == null) {
                buffer.append("default");
            } else {
                buffer.append('\'');
                buffer.append(realm);
                buffer.append('\'');
            }
            buffer.append(" authentication realm at "); 
            buffer.append(host); 
            throw new CredentialsNotAvailableException(buffer.toString());
        }
        String auth = authscheme.authenticate(credentials, method);
        if (auth != null) {
            String s = proxy ? PROXY_AUTH_RESP : WWW_AUTH_RESP;
            Header header = new Header(s, auth, true);
            method.addRequestHeader(header);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Attempt to provide requisite authentication credentials to the 
     * given method in the given context using the given 
     * authentication scheme.
     * 
     * @param authscheme The authentication scheme to be used
     * @param method The HttpMethod which requires authentication
     * @param conn the connection to a specific host. This parameter 
     *   may be <tt>null</tt> if default credentials (not specific 
     *   to any particular host) are to be used
     * @param state The HttpState object providing Credentials
     * 
     * @return true if the <tt>Authenticate</tt> response header was added
     * 
     * @throws CredentialsNotAvailableException if authentication credentials
     *         required to respond to the authentication challenge are not available
     * @throws AuthenticationException when a parsing or other error occurs

     * @see HttpState#setCredentials(String,String,Credentials)
     * 
     * @deprecated use AuthScheme
     */
    public static boolean authenticate(
        AuthScheme authscheme, 
        HttpMethod method, 
        HttpConnection conn,
        HttpState state) 
        throws AuthenticationException {
       LOG.trace(
            "enter HttpAuthenticator.authenticate(AuthScheme, HttpMethod, HttpConnection, "
            + "HttpState)");
        return doAuthenticate(authscheme, method, conn, state, false);
    }


    /**
     * Attempt to provide requisite proxy authentication credentials 
     * to the given method in the given context using 
     * the given authentication scheme.
     * 
     * @param authscheme The authentication scheme to be used
     * @param method the HttpMethod which requires authentication
     * @param conn the connection to a specific host. This parameter 
     *   may be <tt>null</tt> if default credentials (not specific 
     *   to any particular host) are to be used
     * @param state the HttpState object providing Credentials
     * 
     * @return true if the <tt>Proxy-Authenticate</tt> response header 
     *  was added
     * 
     * @throws CredentialsNotAvailableException if authentication credentials
     *         required to respond to the authentication challenge are not available
     * @throws AuthenticationException when a parsing or other error occurs

     * @see HttpState#setCredentials(String,String,Credentials)
     * 
     * @deprecated use AuthScheme
     */
    public static boolean authenticateProxy(
        AuthScheme authscheme, 
        HttpMethod method, 
        HttpConnection conn,
        HttpState state
    ) throws AuthenticationException {
       LOG.trace("enter HttpAuthenticator.authenticateProxy(AuthScheme, HttpMethod, HttpState)");
       return doAuthenticate(authscheme, method, conn, state, true);
    }
}
