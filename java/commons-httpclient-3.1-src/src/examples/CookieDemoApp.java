/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/examples/CookieDemoApp.java,v 1.14 2004/02/22 18:08:45 olegk Exp $
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

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 *
 * This is a sample application that demonstrates
 * how to use the Jakarta HttpClient API.
 *
 * This application sets an HTTP cookie and
 * updates the cookie's value across multiple
 * HTTP GET requests.
 *
 * @author Sean C. Sullivan
 * @author Oleg Kalnichevski
 *
 */
public class CookieDemoApp {

    /**
     *
     * Usage:
     *          java CookieDemoApp http://mywebserver:80/
     *
     *  @param args command line arguments
     *                 Argument 0 is a URL to a web server
     *
     *
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: java CookieDemoApp <url>");
            System.err.println("<url> The url of a webpage");
            System.exit(1);
        }
        // Get target URL
        String strURL = args[0];
        System.out.println("Target URL: " + strURL);

        // Get initial state object
        HttpState initialState = new HttpState();
        // Initial set of cookies can be retrieved from persistent storage and 
        // re-created, using a persistence mechanism of choice,
        Cookie mycookie = new Cookie(".foobar.com", "mycookie", "stuff", "/", null, false);
        // and then added to your HTTP state instance
        initialState.addCookie(mycookie);

        // Get HTTP client instance
        HttpClient httpclient = new HttpClient();
        httpclient.getHttpConnectionManager().
            getParams().setConnectionTimeout(30000);
        httpclient.setState(initialState);

        // RFC 2101 cookie management spec is used per default
        // to parse, validate, format & match cookies
        httpclient.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
        // A different cookie management spec can be selected
        // when desired

        //httpclient.getParams().setCookiePolicy(CookiePolicy.NETSCAPE);
        // Netscape Cookie Draft spec is provided for completeness
        // You would hardly want to use this spec in real life situations
        //httppclient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        // Compatibility policy is provided in order to mimic cookie
        // management of popular web browsers that is in some areas 
        // not 100% standards compliant

        // Get HTTP GET method
        GetMethod httpget = new GetMethod(strURL);
        // Execute HTTP GET
        int result = httpclient.executeMethod(httpget);
        // Display status code
        System.out.println("Response status code: " + result);
        // Get all the cookies
        Cookie[] cookies = httpclient.getState().getCookies();
        // Display the cookies
        System.out.println("Present cookies: ");
        for (int i = 0; i < cookies.length; i++) {
            System.out.println(" - " + cookies[i].toExternalForm());
        }
        // Release current connection to the connection pool once you are done
        httpget.releaseConnection();
    }
}
