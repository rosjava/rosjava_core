/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/examples/InteractiveAuthenticationExample.java,v 1.2 2004/02/22 18:08:45 olegk Exp $
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
 * [Additional notices, if required by prior licensing conditions]
 *
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
import org.apache.commons.httpclient.auth.NTLMScheme;
import org.apache.commons.httpclient.auth.RFC2617Scheme;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * A simple example that uses HttpClient to perform interactive
 * authentication.
 *
 * @author Oleg Kalnichevski
 */
public class InteractiveAuthenticationExample {

    /**
     * Constructor for InteractiveAuthenticationExample.
     */
    public InteractiveAuthenticationExample() {
        super();
    }

    public static void main(String[] args) throws Exception {

        InteractiveAuthenticationExample demo = new InteractiveAuthenticationExample();
        demo.doDemo();
    }
    
    private void doDemo() throws IOException {

        HttpClient client = new HttpClient();
        client.getParams().setParameter(
            CredentialsProvider.PROVIDER, new ConsoleAuthPrompter());
        GetMethod httpget = new GetMethod("http://target-host/requires-auth.html");
        httpget.setDoAuthentication(true);
        try {
            // execute the GET
            int status = client.executeMethod(httpget);
            // print the status and response
            System.out.println(httpget.getStatusLine().toString());
            System.out.println(httpget.getResponseBodyAsString());
        } finally {
            // release any connection resources used by the method
            httpget.releaseConnection();
        }
    }

    public class ConsoleAuthPrompter implements CredentialsProvider {

        private BufferedReader in = null; 
        public ConsoleAuthPrompter() {
            super();
            this.in = new BufferedReader(new InputStreamReader(System.in));
        }
        
        private String readConsole() throws IOException {
            return this.in.readLine();
        }
        
        public Credentials getCredentials(
            final AuthScheme authscheme, 
            final String host, 
            int port, 
            boolean proxy)
            throws CredentialsNotAvailableException 
        {
            if (authscheme == null) {
                return null;
            }
            try{
                if (authscheme instanceof NTLMScheme) {
                    System.out.println(host + ":" + port + " requires Windows authentication");
                    System.out.print("Enter domain: ");
                    String domain = readConsole();   
                    System.out.print("Enter username: ");
                    String user = readConsole();   
                    System.out.print("Enter password: ");
                    String password = readConsole();
                    return new NTCredentials(user, password, host, domain);    
                } else
                if (authscheme instanceof RFC2617Scheme) {
                    System.out.println(host + ":" + port + " requires authentication with the realm '" 
                        + authscheme.getRealm() + "'");
                    System.out.print("Enter username: ");
                    String user = readConsole();   
                    System.out.print("Enter password: ");
                    String password = readConsole();
                    return new UsernamePasswordCredentials(user, password);    
                } else {
                    throw new CredentialsNotAvailableException("Unsupported authentication scheme: " +
                        authscheme.getSchemeName());
                }
            } catch (IOException e) {
                throw new CredentialsNotAvailableException(e.getMessage(), e);
            }
        }
    }
}
