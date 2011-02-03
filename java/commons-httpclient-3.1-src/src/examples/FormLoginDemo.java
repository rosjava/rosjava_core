/*
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

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.cookie.CookieSpec;
import org.apache.commons.httpclient.methods.*;

/**
 * <p>
 * A example that demonstrates how HttpClient APIs can be used to perform 
 * form-based logon.
 * </p>
 *
 * @author Oleg Kalnichevski
 *
 */
public class FormLoginDemo
{
    static final String LOGON_SITE = "developer.java.sun.com";
    static final int    LOGON_PORT = 80;

    public FormLoginDemo() {
        super();
    }

    public static void main(String[] args) throws Exception {

        HttpClient client = new HttpClient();
        client.getHostConfiguration().setHost(LOGON_SITE, LOGON_PORT, "http");
        client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        // 'developer.java.sun.com' has cookie compliance problems
        // Their session cookie's domain attribute is in violation of the RFC2109
        // We have to resort to using compatibility cookie policy

        GetMethod authget = new GetMethod("/servlet/SessionServlet");

        client.executeMethod(authget);
        System.out.println("Login form get: " + authget.getStatusLine().toString()); 
        // release any connection resources used by the method
        authget.releaseConnection();
        // See if we got any cookies
        CookieSpec cookiespec = CookiePolicy.getDefaultSpec();
        Cookie[] initcookies = cookiespec.match(
            LOGON_SITE, LOGON_PORT, "/", false, client.getState().getCookies());
        System.out.println("Initial set of cookies:");    
        if (initcookies.length == 0) {
            System.out.println("None");    
        } else {
            for (int i = 0; i < initcookies.length; i++) {
                System.out.println("- " + initcookies[i].toString());    
            }
        }
        
        PostMethod authpost = new PostMethod("/servlet/SessionServlet");
        // Prepare login parameters
        NameValuePair action   = new NameValuePair("action", "login");
        NameValuePair url      = new NameValuePair("url", "/index.html");
        NameValuePair userid   = new NameValuePair("UserId", "userid");
        NameValuePair password = new NameValuePair("Password", "password");
        authpost.setRequestBody( 
          new NameValuePair[] {action, url, userid, password});
        
        client.executeMethod(authpost);
        System.out.println("Login form post: " + authpost.getStatusLine().toString()); 
        // release any connection resources used by the method
        authpost.releaseConnection();
        // See if we got any cookies
        // The only way of telling whether logon succeeded is 
        // by finding a session cookie
        Cookie[] logoncookies = cookiespec.match(
            LOGON_SITE, LOGON_PORT, "/", false, client.getState().getCookies());
        System.out.println("Logon cookies:");    
        if (logoncookies.length == 0) {
            System.out.println("None");    
        } else {
            for (int i = 0; i < logoncookies.length; i++) {
                System.out.println("- " + logoncookies[i].toString());    
            }
        }
        // Usually a successful form-based login results in a redicrect to 
        // another url
        int statuscode = authpost.getStatusCode();
        if ((statuscode == HttpStatus.SC_MOVED_TEMPORARILY) ||
            (statuscode == HttpStatus.SC_MOVED_PERMANENTLY) ||
            (statuscode == HttpStatus.SC_SEE_OTHER) ||
            (statuscode == HttpStatus.SC_TEMPORARY_REDIRECT)) {
            Header header = authpost.getResponseHeader("location");
            if (header != null) {
                String newuri = header.getValue();
                if ((newuri == null) || (newuri.equals(""))) {
                    newuri = "/";
                }
                System.out.println("Redirect target: " + newuri); 
                GetMethod redirect = new GetMethod(newuri);

                client.executeMethod(redirect);
                System.out.println("Redirect: " + redirect.getStatusLine().toString()); 
                // release any connection resources used by the method
                redirect.releaseConnection();
            } else {
                System.out.println("Invalid redirect");
                System.exit(1);
            }
        }
    }
}
