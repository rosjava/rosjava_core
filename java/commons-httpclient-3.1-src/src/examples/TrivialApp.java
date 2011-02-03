/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/examples/TrivialApp.java,v 1.18 2004/06/12 22:47:23 olegk Exp $
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
 * [Additional notices, if required by prior licensing conditions]
 *
 */

import java.io.IOException;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 *
 * This is a simple text mode application that demonstrates
 * how to use the Jakarta HttpClient API.
 *
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 * @author Ortwin Glück
 */
public class TrivialApp
{

    private static final void printUsage()
    {
        System.out.println();
        System.out.println("Usage: java -classpath <classpath> [-Dorg.apache.commons.logging.simplelog.defaultlog=<loglevel>] TrivialApp <url> [<username> <password>]");
        System.out.println("<classpath> - must contain the commons-httpclient.jar and commons-logging.jar");
        System.out.println("<loglevel> - one of error, warn, info, debug, trace");
        System.out.println("<url> - some valid URL");
        System.out.println("<username> - username for protected page");
        System.out.println("<password> - password for protected page");
        System.out.println();
    }

    public static void main(String[] args)
    {
        if ((args.length != 1) && (args.length != 3)) {
            printUsage();
            System.exit(-1);
        }

        Credentials creds = null;
        if (args.length >= 3) {
            creds = new UsernamePasswordCredentials(args[1], args[2]);
        }

        //create a singular HttpClient object
        HttpClient client = new HttpClient();

        //establish a connection within 5 seconds
        client.getHttpConnectionManager().
            getParams().setConnectionTimeout(5000);

        //set the default credentials
        if (creds != null) {
            client.getState().setCredentials(AuthScope.ANY, creds);
        }

        String url = args[0];
        HttpMethod method = null;

        //create a method object
            method = new GetMethod(url);
            method.setFollowRedirects(true);
        //} catch (MalformedURLException murle) {
        //    System.out.println("<url> argument '" + url
        //            + "' is not a valid URL");
        //    System.exit(-2);
        //}

        //execute the method
        String responseBody = null;
        try{
            client.executeMethod(method);
            responseBody = method.getResponseBodyAsString();
        } catch (HttpException he) {
            System.err.println("Http error connecting to '" + url + "'");
            System.err.println(he.getMessage());
            System.exit(-4);
        } catch (IOException ioe){
            System.err.println("Unable to connect to '" + url + "'");
            System.exit(-3);
        }


        //write out the request headers
        System.out.println("*** Request ***");
        System.out.println("Request Path: " + method.getPath());
        System.out.println("Request Query: " + method.getQueryString());
        Header[] requestHeaders = method.getRequestHeaders();
        for (int i=0; i<requestHeaders.length; i++){
            System.out.print(requestHeaders[i]);
        }

        //write out the response headers
        System.out.println("*** Response ***");
        System.out.println("Status Line: " + method.getStatusLine());
        Header[] responseHeaders = method.getResponseHeaders();
        for (int i=0; i<responseHeaders.length; i++){
            System.out.print(responseHeaders[i]);
        }

        //write out the response body
        System.out.println("*** Response Body ***");
        System.out.println(responseBody);

        //clean up the connection resources
        method.releaseConnection();

        System.exit(0);
    }
}
