/*
 * $Header:$
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

package org.apache.commons.httpclient.contrib.auth;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.auth.AuthChallengeException;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.AuthenticationException;
import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
import org.apache.commons.httpclient.auth.InvalidCredentialsException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

/** 
 * 
 * @author <a href="mailto:mikael.wikstrom@it.su.se">Mikael Wilstrom</a>
 * @author Mikael Wikstrom
 */
public class NegotiateScheme implements AuthScheme {

    /** Log object for this class. */
    private static final Log LOG = LogFactory.getLog(NegotiateScheme.class);

    /** challenge string. */
    private String challenge = null;

    private static final int UNINITIATED         = 0;
    private static final int INITIATED           = 1;
    private static final int NEGOTIATING         = 3;
    private static final int ESTABLISHED         = 4;
    private static final int FAILED              = Integer.MAX_VALUE;

    private GSSContext context = null;

    /** Authentication process state */
    private int state;

    /** base64 decoded challenge **/
    byte[] token = new byte[0];

    /**
     * Init GSSContext for negotiation.
     * 
     * @param server servername only (e.g: radar.it.su.se)
     */
    protected void init(String server) throws GSSException {
         LOG.debug("init " + server);
         /* Kerberos v5 GSS-API mechanism defined in RFC 1964. */
         Oid krb5Oid = new Oid("1.2.840.113554.1.2.2");
         GSSManager manager = GSSManager.getInstance();
         GSSName serverName = manager.createName("HTTP/"+server, null); 
         context = manager.createContext(serverName, krb5Oid, null,
                                    GSSContext.DEFAULT_LIFETIME);
         context.requestMutualAuth(true); 
         context.requestCredDeleg(true);
         state = INITIATED;
    }
    
    /**
     * Default constructor for the Negotiate authentication scheme.
     * 
     * @since 3.0
     */
    public NegotiateScheme() {
        super();
        state = UNINITIATED;
    }

    /**
     * Constructor for the Negotiate authentication scheme.
     * 
     * @param challenge The authentication challenge
     */
    public NegotiateScheme(final String challenge) {
        super();
        LOG.debug("enter NegotiateScheme("+challenge+")");
        processChallenge(challenge);
    }

    /**
     * Processes the Negotiate challenge.
     *  
     * @param challenge the challenge string
     * 
     * @since 3.0
     */
    public void processChallenge(final String challenge){
        LOG.debug("enter processChallenge(challenge=\""+challenge+"\")");
        if (challenge.startsWith("Negotiate")) {
            if(isComplete() == false)
                state = NEGOTIATING;
            
            if (challenge.startsWith("Negotiate "))
                token = new Base64().decode(challenge.substring(10).getBytes());
            else
                token = new byte[0];
        }
    }

    /**
     * Tests if the Negotiate authentication process has been completed.
     * 
     * @return <tt>true</tt> if authorization has been processed,
     *   <tt>false</tt> otherwise.
     * 
     * @since 3.0
     */
    public boolean isComplete() {
        LOG.debug("enter isComplete()");
        return this.state == ESTABLISHED || this.state == FAILED;
    }

    /**
     * Returns textual designation of the Negotiate authentication scheme.
     * 
     * @return <code>Negotiate</code>
     */
    public String getSchemeName() {
        return "Negotiate";
    }

    /**
     * The concept of an authentication realm is not supported by the Negotiate 
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
        LOG.debug("enter getID(): " + challenge);
        return challenge;
    }
    
    /**
     * Returns the authentication parameter with the given name, if available.
     * 
     * <p>There are no valid parameters for Negotiate authentication so this 
     * method always returns <tt>null</tt>.</p>
     * 
     * @param name The name of the parameter to be returned
     * 
     * @return the parameter with the given name
     */
    public String getParameter(String name) {
        LOG.debug("enter getParameter("+name+")");
        if (name == null) {
            throw new IllegalArgumentException("Parameter name may not be null"); 
        }
        return null;
    }

    /**
     * Returns <tt>true</tt>. 
     * Negotiate authentication scheme is connection based.
     * 
     * @return <tt>true</tt>.
     * 
     * @since 3.0
     */
    public boolean isConnectionBased() {
        LOG.info("enter isConnectionBased()");
        return true;
    }

    /**
     * Method not supported by Negotiate scheme. 
     * 
     * @throws AuthenticationException if called.
     * 
     * @deprecated Use {@link #authenticate(Credentials, HttpMethod)}
     */
    public String authenticate(Credentials credentials, String method, String uri) 
      throws AuthenticationException {
        throw new AuthenticationException("method not supported by Negotiate scheme");
    }
    
    /**
     * Produces Negotiate authorization string based on token created by 
     * processChallenge.
     * 
     * @param credentials Never used be the Negotiate scheme but must be provided to 
     * satisfy common-httpclient API. Credentials from JAAS will be used insted.
     * @param method The method being authenticated
     * 
     * @throws AuthenticationException if authorization string cannot 
     *   be generated due to an authentication failure
     * 
     * @return an Negotiate authorization string
     * 
     * @since 3.0
     */
    public String authenticate(
        Credentials credentials, 
        HttpMethod method
    ) throws AuthenticationException {
        LOG.debug("enter NegotiateScheme.authenticate(Credentials, HttpMethod)");

        if (state == UNINITIATED) {
            throw new IllegalStateException(
               "Negotiation authentication process has not been initiated");
        }

        try {
            try {                
                if(context==null) {
                    LOG.info("host: " + method.getURI().getHost());
                    init( method.getURI().getHost() );
                }
            } catch (org.apache.commons.httpclient.URIException urie) {
                LOG.error(urie.getMessage());
                state = FAILED;
                throw new AuthenticationException(urie.getMessage());
            }
        
            // HTTP 1.1 issue:
            // Mutual auth will never complete do to 200 insted of 401 in 
            // return from server. "state" will never reach ESTABLISHED
            // but it works anyway
            token = context.initSecContext(token, 0, token.length);
            LOG.info("got token, sending " + token.length + " to server");
        } catch (GSSException gsse) {
            LOG.fatal(gsse.getMessage());
            state = FAILED;
            if( gsse.getMajor() == GSSException.DEFECTIVE_CREDENTIAL
                    || gsse.getMajor() == GSSException.CREDENTIALS_EXPIRED )
                throw new InvalidCredentialsException(gsse.getMessage(),gsse);
            if( gsse.getMajor() == GSSException.NO_CRED )
                throw new CredentialsNotAvailableException(gsse.getMessage(),gsse);
            if( gsse.getMajor() == GSSException.DEFECTIVE_TOKEN
                    || gsse.getMajor() == GSSException.DUPLICATE_TOKEN
                    || gsse.getMajor() == GSSException.OLD_TOKEN )
                throw new AuthChallengeException(gsse.getMessage(),gsse);
            // other error
            throw new AuthenticationException(gsse.getMessage());
        }
        return "Negotiate " + new String(new Base64().encode(token));
    }
}
