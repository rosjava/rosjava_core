/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/java/org/apache/commons/httpclient/auth/NTLMScheme.java,v 1.21 2004/05/13 04:02:00 mbecke Exp $
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

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** An implementation of the Microsoft proprietary NTLM authentication scheme.  For a detailed
 * explanation of the NTLM scheme please see <a href="http://davenport.sourceforge.net/ntlm.html">
 * http://davenport.sourceforge.net/ntlm.html</a>.
 * 
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @author Rodney Waldhoff
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 * @author Ortwin Gl???ck
 * @author Sean C. Sullivan
 * @author <a href="mailto:adrian@ephox.com">Adrian Sutton</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 */
public class NTLMScheme implements AuthScheme {

    /** Log object for this class. */
    private static final Log LOG = LogFactory.getLog(NTLMScheme.class);

    /** NTLM challenge string. */
    private String ntlmchallenge = null;

    private static final int UNINITIATED         = 0;
    private static final int INITIATED           = 1;
    private static final int TYPE1_MSG_GENERATED = 2;
    private static final int TYPE2_MSG_RECEIVED  = 3;
    private static final int TYPE3_MSG_GENERATED = 4;
    private static final int FAILED              = Integer.MAX_VALUE;

    /** Authentication process state */
    private int state;
    
    /**
     * Default constructor for the NTLM authentication scheme.
     * 
     * @since 3.0
     */
    public NTLMScheme() {
        super();
        this.state = UNINITIATED;
    }

    /**
     * Constructor for the NTLM authentication scheme.
     * 
     * @param challenge The authentication challenge
     * 
     * @throws MalformedChallengeException is thrown if the authentication challenge
     * is malformed
     */
    public NTLMScheme(final String challenge) throws MalformedChallengeException {
        super();
        processChallenge(challenge);
    }

    /**
     * Processes the NTLM challenge.
     *  
     * @param challenge the challenge string
     * 
     * @throws MalformedChallengeException is thrown if the authentication challenge
     * is malformed
     * 
     * @since 3.0
     */
    public void processChallenge(final String challenge) throws MalformedChallengeException {
        String s = AuthChallengeParser.extractScheme(challenge);
        if (!s.equalsIgnoreCase(getSchemeName())) {
            throw new MalformedChallengeException("Invalid NTLM challenge: " + challenge);
        }
        int i = challenge.indexOf(' ');
        if (i != -1) {
            s = challenge.substring(i, challenge.length());
            this.ntlmchallenge = s.trim();
            this.state = TYPE2_MSG_RECEIVED;
        } else {
            this.ntlmchallenge = "";
            if (this.state == UNINITIATED) {
                this.state = INITIATED;
            } else {
                this.state = FAILED;
            }
        }
    }

    /**
     * Tests if the NTLM authentication process has been completed.
     * 
     * @return <tt>true</tt> if Basic authorization has been processed,
     *   <tt>false</tt> otherwise.
     * 
     * @since 3.0
     */
    public boolean isComplete() {
        return this.state == TYPE3_MSG_GENERATED || this.state == FAILED;
    }

    /**
     * Returns textual designation of the NTLM authentication scheme.
     * 
     * @return <code>ntlm</code>
     */
    public String getSchemeName() {
        return "ntlm";
    }

    /**
     * The concept of an authentication realm is not supported by the NTLM 
     * authentication scheme. Always returns <code>null</code>.
     * 
     * @return <code>null</code>
     */
    public String getRealm() {
        return null;
    }
    
    /**
     * Returns a String identifying the authentication challenge.  This is
     * used, in combination with the host and port to determine if
     * authorization has already been attempted or not.  Schemes which
     * require multiple requests to complete the authentication should
     * return a different value for each stage in the request.
     * 
     * <p>Additionally, the ID should take into account any changes to the
     * authentication challenge and return a different value when appropriate.
     * For example when the realm changes in basic authentication it should be
     * considered a different authentication attempt and a different value should
     * be returned.</p>
     * 
     * @return String a String identifying the authentication challenge.  The
     * returned value may be null.
     * 
     * @deprecated no longer used
     */
    public String getID() {
        return ntlmchallenge;
    }
    
    /**
     * Returns the authentication parameter with the given name, if available.
     * 
     * <p>There are no valid parameters for NTLM authentication so this method always returns
     * <tt>null</tt>.</p>
     * 
     * @param name The name of the parameter to be returned
     * 
     * @return the parameter with the given name
     */
    public String getParameter(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Parameter name may not be null"); 
        }
        return null;
    }

    /**
     * Returns <tt>true</tt>. NTLM authentication scheme is connection based.
     * 
     * @return <tt>true</tt>.
     * 
     * @since 3.0
     */
    public boolean isConnectionBased() {
        return true;    
    }

    /**
     * Create a NTLM authorization string for the given
     * challenge and NT credentials.
     *
     * @param challenge The challenge.
     * @param credentials {@link NTCredentials}
     *
     * @return a ntlm authorization string
     * @throws AuthenticationException is thrown if authentication fails
     * 
     * @deprecated Use non-static {@link #authenticate(Credentials, HttpMethod)}
     */
    public static String authenticate(
     final NTCredentials credentials, final String challenge) 
      throws AuthenticationException {

        LOG.trace("enter NTLMScheme.authenticate(NTCredentials, String)");

        if (credentials == null) {
            throw new IllegalArgumentException("Credentials may not be null");
        }
        
        NTLM ntlm = new NTLM();
        String s = ntlm.getResponseFor(challenge,
        credentials.getUserName(), credentials.getPassword(),
        credentials.getHost(), credentials.getDomain());
        return "NTLM " + s;
    }

    /**
     * Create a NTLM authorization string for the given
     * challenge and NT credentials.
     *
     * @param challenge The challenge.
     * @param credentials {@link NTCredentials}
     * @param charset The charset to use for encoding the credentials
     *
     * @return a ntlm authorization string
     * @throws AuthenticationException is thrown if authentication fails
     * 
     * @deprecated Use non-static {@link #authenticate(Credentials, HttpMethod)}
     * 
     * @since 3.0
     */
    public static String authenticate(
        final NTCredentials credentials, 
        final String challenge,
        String charset
    ) throws AuthenticationException {

        LOG.trace("enter NTLMScheme.authenticate(NTCredentials, String)");

        if (credentials == null) {
            throw new IllegalArgumentException("Credentials may not be null");
        }
        
        NTLM ntlm = new NTLM();
        ntlm.setCredentialCharset(charset);
        String s = ntlm.getResponseFor(
            challenge,
            credentials.getUserName(), 
            credentials.getPassword(),
            credentials.getHost(), 
            credentials.getDomain());
        return "NTLM " + s;
    }
    
    /**
     * Produces NTLM authorization string for the given set of 
     * {@link Credentials}.
     * 
     * @param credentials The set of credentials to be used for athentication
     * @param method Method name is ignored by the NTLM authentication scheme
     * @param uri URI is ignored by the NTLM authentication scheme
     * @throws InvalidCredentialsException if authentication credentials
     *         are not valid or not applicable for this authentication scheme
     * @throws AuthenticationException if authorization string cannot 
     *   be generated due to an authentication failure
     * 
     * @return an NTLM authorization string
     * 
     * @deprecated Use {@link #authenticate(Credentials, HttpMethod)}
     */
    public String authenticate(Credentials credentials, String method, String uri) 
      throws AuthenticationException {
        LOG.trace("enter NTLMScheme.authenticate(Credentials, String, String)");

        NTCredentials ntcredentials = null;
        try {
            ntcredentials = (NTCredentials) credentials;
        } catch (ClassCastException e) {
            throw new InvalidCredentialsException(
             "Credentials cannot be used for NTLM authentication: " 
              + credentials.getClass().getName());
        }
        return NTLMScheme.authenticate(ntcredentials, this.ntlmchallenge);
    }
    
    /**
     * Produces NTLM authorization string for the given set of 
     * {@link Credentials}.
     * 
     * @param credentials The set of credentials to be used for athentication
     * @param method The method being authenticated
     * 
     * @throws InvalidCredentialsException if authentication credentials
     *         are not valid or not applicable for this authentication scheme
     * @throws AuthenticationException if authorization string cannot 
     *   be generated due to an authentication failure
     * 
     * @return an NTLM authorization string
     * 
     * @since 3.0
     */
    public String authenticate(
        Credentials credentials, 
        HttpMethod method
    ) throws AuthenticationException {
        LOG.trace("enter NTLMScheme.authenticate(Credentials, HttpMethod)");

        if (this.state == UNINITIATED) {
            throw new IllegalStateException("NTLM authentication process has not been initiated");
        }

        NTCredentials ntcredentials = null;
        try {
            ntcredentials = (NTCredentials) credentials;
        } catch (ClassCastException e) {
            throw new InvalidCredentialsException(
                    "Credentials cannot be used for NTLM authentication: " 
                    + credentials.getClass().getName());
        }
        NTLM ntlm = new NTLM();
        ntlm.setCredentialCharset(method.getParams().getCredentialCharset());
        String response = null;
        if (this.state == INITIATED || this.state == FAILED) {
            response = ntlm.getType1Message(
                ntcredentials.getHost(), 
                ntcredentials.getDomain());
            this.state = TYPE1_MSG_GENERATED;
        } else {
            response = ntlm.getType3Message(
                ntcredentials.getUserName(), 
                ntcredentials.getPassword(),
                ntcredentials.getHost(), 
                ntcredentials.getDomain(),
                ntlm.parseType2Message(this.ntlmchallenge));
            this.state = TYPE3_MSG_GENERATED;
        }
        return "NTLM " + response;
    }    
}
