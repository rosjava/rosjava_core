/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/examples/CustomAuthenticationExample.java,v 1.1 2004/09/06 20:10:02 mbecke Exp $
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
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.AuthenticationException;
import org.apache.commons.httpclient.auth.MalformedChallengeException;
import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.HttpParams;

/**
 * A simple custom AuthScheme example.  The included auth scheme is meant 
 * for demonstration purposes only.  It does not actually implement a usable
 * authentication method.
 */
public class CustomAuthenticationExample {

    public static void main(String[] args) {
        
        // register the auth scheme
        AuthPolicy.registerAuthScheme(SecretAuthScheme.NAME, SecretAuthScheme.class);

        // include the scheme in the AuthPolicy.AUTH_SCHEME_PRIORITY preference,
        // this can be done on a per-client or per-method basis but we'll do it
        // globally for this example
        HttpParams params = DefaultHttpParams.getDefaultParams();        
        ArrayList schemes = new ArrayList();
        schemes.add(SecretAuthScheme.NAME);
        schemes.addAll((Collection) params.getParameter(AuthPolicy.AUTH_SCHEME_PRIORITY));
        params.setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, schemes);
        
        // now that our scheme has been registered we can execute methods against
        // servers that require "Secret" authentication... 
    }
    
    /**
     * A custom auth scheme that just uses "Open Sesame" as the authentication
     * string.
     */
    private class SecretAuthScheme implements AuthScheme {

        public static final String NAME = "Secret";

        public SecretAuthScheme() {
            // All auth schemes must have a no arg constructor.
        }
        public String authenticate(Credentials credentials, HttpMethod method)
            throws AuthenticationException {
            return "Open Sesame";
        }
        public String authenticate(Credentials credentials, String method,
                String uri) throws AuthenticationException {
            return "Open Sesame";
        }
        public String getID() {
            return NAME;
        }
        public String getParameter(String name) {
            // this scheme does not use parameters, see RFC2617Scheme for an example
            return null;
        }
        public String getRealm() {
            // this scheme does not use realms
            return null;
        }
        public String getSchemeName() {
            return NAME;
        }
        public boolean isConnectionBased() {
            return false;
        }
        public void processChallenge(String challenge)
                throws MalformedChallengeException {
            // Nothing to do here, this is not a challenge based
            // auth scheme.  See NTLMScheme for a good example.
        }
        public boolean isComplete() {
            // again we're not a challenge based scheme so this is always true
            return true;
        }
    }
}
