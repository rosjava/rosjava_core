/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/java/org/apache/commons/httpclient/auth/BasicScheme.java,v 1.17 2004/05/13 04:02:00 mbecke Exp $
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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Basic authentication scheme as defined in RFC 2617.
 * </p>
 * 
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @author Rodney Waldhoff
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 * @author Ortwin Gl?ck
 * @author Sean C. Sullivan
 * @author <a href="mailto:adrian@ephox.com">Adrian Sutton</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 */

public class BasicScheme extends RFC2617Scheme {
    
    /** Log object for this class. */
    private static final Log LOG = LogFactory.getLog(BasicScheme.class);
    
    /** Whether the basic authentication process is complete */
    private boolean complete;
    
    /**
     * Default constructor for the basic authetication scheme.
     * 
     * @since 3.0
     */
    public BasicScheme() {
        super();
        this.complete = false;
    }

    /**
     * Constructor for the basic authetication scheme.
     * 
     * @param challenge authentication challenge
     * 
     * @throws MalformedChallengeException is thrown if the authentication challenge
     * is malformed
     * 
     * @deprecated Use parameterless constructor and {@link AuthScheme#processChallenge(String)} 
     *             method
     */
    public BasicScheme(final String challenge) throws MalformedChallengeException {
        super(challenge);
        this.complete = true;
    }

    /**
     * Returns textual designation of the basic authentication scheme.
     * 
     * @return <code>basic</code>
     */
    public String getSchemeName() {
        return "basic";
    }

    /**
     * Processes the Basic challenge.
     *  
     * @param challenge the challenge string
     * 
     * @throws MalformedChallengeException is thrown if the authentication challenge
     * is malformed
     * 
     * @since 3.0
     */
    public void processChallenge(String challenge) 
        throws MalformedChallengeException 
    {
        super.processChallenge(challenge);
        this.complete = true;
    }

    /**
     * Tests if the Basic authentication process has been completed.
     * 
     * @return <tt>true</tt> if Basic authorization has been processed,
     *   <tt>false</tt> otherwise.
     * 
     * @since 3.0
     */
    public boolean isComplete() {
        return this.complete;
    }

    /**
     * Produces basic authorization string for the given set of 
     * {@link Credentials}.
     * 
     * @param credentials The set of credentials to be used for athentication
     * @param method Method name is ignored by the basic authentication scheme
     * @param uri URI is ignored by the basic authentication scheme
     * @throws InvalidCredentialsException if authentication credentials
     *         are not valid or not applicable for this authentication scheme
     * @throws AuthenticationException if authorization string cannot 
     *   be generated due to an authentication failure
     * 
     * @return a basic authorization string
     * 
     * @deprecated Use {@link #authenticate(Credentials, HttpMethod)}
     */
    public String authenticate(Credentials credentials, String method, String uri)
      throws AuthenticationException {

        LOG.trace("enter BasicScheme.authenticate(Credentials, String, String)");

        UsernamePasswordCredentials usernamepassword = null;
        try {
            usernamepassword = (UsernamePasswordCredentials) credentials;
        } catch (ClassCastException e) {
            throw new InvalidCredentialsException(
             "Credentials cannot be used for basic authentication: " 
              + credentials.getClass().getName());
        }
        return BasicScheme.authenticate(usernamepassword);
    }

    /**
     * Returns <tt>false</tt>. Basic authentication scheme is request based.
     * 
     * @return <tt>false</tt>.
     * 
     * @since 3.0
     */
    public boolean isConnectionBased() {
        return false;    
    }

    /**
     * Produces basic authorization string for the given set of {@link Credentials}.
     * 
     * @param credentials The set of credentials to be used for athentication
     * @param method The method being authenticated
     * @throws InvalidCredentialsException if authentication credentials
     *         are not valid or not applicable for this authentication scheme
     * @throws AuthenticationException if authorization string cannot 
     *   be generated due to an authentication failure
     * 
     * @return a basic authorization string
     * 
     * @since 3.0
     */
    public String authenticate(Credentials credentials, HttpMethod method) throws AuthenticationException {

        LOG.trace("enter BasicScheme.authenticate(Credentials, HttpMethod)");

        if (method == null) {
            throw new IllegalArgumentException("Method may not be null");
        }
        UsernamePasswordCredentials usernamepassword = null;
        try {
            usernamepassword = (UsernamePasswordCredentials) credentials;
        } catch (ClassCastException e) {
            throw new InvalidCredentialsException(
                    "Credentials cannot be used for basic authentication: " 
                    + credentials.getClass().getName());
        }
        return BasicScheme.authenticate(
            usernamepassword, 
            method.getParams().getCredentialCharset());
    }
    
    /**
     * @deprecated Use {@link #authenticate(UsernamePasswordCredentials, String)}
     * 
     * Returns a basic <tt>Authorization</tt> header value for the given 
     * {@link UsernamePasswordCredentials}.
     * 
     * @param credentials The credentials to encode.
     * 
     * @return a basic authorization string
     */
    public static String authenticate(UsernamePasswordCredentials credentials) {
        return authenticate(credentials, "ISO-8859-1");
    }

    /**
     * Returns a basic <tt>Authorization</tt> header value for the given 
     * {@link UsernamePasswordCredentials} and charset.
     * 
     * @param credentials The credentials to encode.
     * @param charset The charset to use for encoding the credentials
     * 
     * @return a basic authorization string
     * 
     * @since 3.0
     */
    public static String authenticate(UsernamePasswordCredentials credentials, String charset) {

        LOG.trace("enter BasicScheme.authenticate(UsernamePasswordCredentials, String)");

        if (credentials == null) {
            throw new IllegalArgumentException("Credentials may not be null"); 
        }
        if (charset == null || charset.length() == 0) {
            throw new IllegalArgumentException("charset may not be null or empty");
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append(credentials.getUserName());
        buffer.append(":");
        buffer.append(credentials.getPassword());
        
        return "Basic " + EncodingUtil.getAsciiString(
                Base64.encodeBase64(EncodingUtil.getBytes(buffer.toString(), charset)));
    }
    
}
