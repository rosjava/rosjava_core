/*
 * $Header: $
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

import java.util.ArrayList;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.HttpParams;

/**
 * A simple custom AuthScheme example.  The included auth scheme is meant 
 * for demonstration purposes only.  It does not actually implement a usable
 * authentication method.
 *
 * <pre>
 Login Configuration file bcsLogin.conf for JAAS.
 -----------------------------------------------
   com.sun.security.jgss.initiate {
     com.sun.security.auth.module.Krb5LoginModule 
         required 
         client=TRUE 
         useTicketCache="true" 
         ticketCache="${user.krb5cc}" 
         debug=true;
   };

   com.sun.security.jgss.accept {
     com.sun.security.auth.module.Krb5LoginModule 
         required 
         client=TRUE 
         useTicketCache="true" 
         ticketCache="${user.krb5cc}" 
         debug=true;
   };
 -----------------------------------------------
 
   java  -Djava.security.krb5.realm=REALM \
      -Djava.security.krb5.kdc=kdc.domain \
      -Djavax.security.auth.useSubjectCredsOnly=false \
      -Djava.security.auth.login.config=src/conf/bcsLogin.conf \
      -Duser.krb5cc="$KRB5CCNAME" \
      -classpath $CP \
      CustomAuthenticationNegotiateExample "http://localhost/gsstest/"
   </pre>
 */
public class CustomAuthenticationNegotiateExample {

    public static void main(String[] args) {
        
        // register the auth scheme
        AuthPolicy.registerAuthScheme("Negotiate", NegotiateScheme.class);

        // include the scheme in the AuthPolicy.AUTH_SCHEME_PRIORITY preference
        ArrayList schemes = new ArrayList();
        schemes.add("Negotiate");

        HttpParams params = DefaultHttpParams.getDefaultParams();        
        params.setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, schemes);
        
        // now that our scheme has been registered we can execute methods against
        // servers that require "Negotiate" authentication... 
        HttpClient client = new HttpClient();
        
        // The Negotiate scheme uses JAAS as credential provider but the
        // httpclient api require us to supply cred anyway.
        // a work around is to provide an empty set of creds.
        Credentials use_jaas_creds = new Credentials() {};
        client.getState().setCredentials(
            new AuthScope(null, -1, null),
            use_jaas_creds);
        GetMethod httpget = new GetMethod(args[0]);

        try {
            client.executeMethod(httpget);
            //System.out.println(httpget.getStatusLine());
            //System.out.println(httpget.getResponseBodyAsString());
        } catch (Exception e) {
            e.printStackTrace();            
        } finally {
            // release any connection resources used by the method
            httpget.releaseConnection();
        }            
        
    }
} 
