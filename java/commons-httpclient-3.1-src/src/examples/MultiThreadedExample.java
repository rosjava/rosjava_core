/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/examples/MultiThreadedExample.java,v 1.3 2004/02/22 18:08:45 olegk Exp $
 * $Revision: 554236 $
 * $Date: 2007-07-07 20:15:09 +0200 (Sat, 07 Jul 2007) $
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
 
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * An example that performs GETs from multiple threads.
 * 
 * @author Michael Becke
 */
public class MultiThreadedExample {

    /**
     * Constructor for MultiThreadedExample.
     */
    public MultiThreadedExample() {
        super();
    }

    public static void main(String[] args) {
        
        // Create an HttpClient with the MultiThreadedHttpConnectionManager.
        // This connection manager must be used if more than one thread will
        // be using the HttpClient.
        HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        // Set the default host/protocol for the methods to connect to.
        // This value will only be used if the methods are not given an absolute URI
        httpClient.getHostConfiguration().setHost("jakarta.apache.org", 80, "http");
        
        // create an array of URIs to perform GETs on
        String[] urisToGet = {
            "/",
            "/commons/",
            "/commons/httpclient/",
            "http://svn.apache.org/viewvc/jakarta/httpcomponents/oac.hc3x/"
        };
        
        // create a thread for each URI
        GetThread[] threads = new GetThread[urisToGet.length];
        for (int i = 0; i < threads.length; i++) {
            GetMethod get = new GetMethod(urisToGet[i]);
            get.setFollowRedirects(true);
            threads[i] = new GetThread(httpClient, get, i + 1);
        }
        
        // start the threads
        for (int j = 0; j < threads.length; j++) {
            threads[j].start();
        }
        
    }
    
    /**
     * A thread that performs a GET.
     */
    static class GetThread extends Thread {
        
        private HttpClient httpClient;
        private GetMethod method;
        private int id;
        
        public GetThread(HttpClient httpClient, GetMethod method, int id) {
            this.httpClient = httpClient;
            this.method = method;
            this.id = id;
        }
        
        /**
         * Executes the GetMethod and prints some satus information.
         */
        public void run() {
            
            try {
                
                System.out.println(id + " - about to get something from " + method.getURI());
                // execute the method
                httpClient.executeMethod(method);
                
                System.out.println(id + " - get executed");
                // get the response body as an array of bytes
                byte[] bytes = method.getResponseBody();
                
                System.out.println(id + " - " + bytes.length + " bytes read");
                
            } catch (Exception e) {
                System.out.println(id + " - error: " + e);
            } finally {
                // always release the connection after we're done 
                method.releaseConnection();
                System.out.println(id + " - connection released");
            }
        }
       
    }
    
}
