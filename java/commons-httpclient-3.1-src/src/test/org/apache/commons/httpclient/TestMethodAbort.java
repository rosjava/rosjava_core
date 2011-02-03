/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/TestMethodAbort.java,v 1.3 2004/10/31 14:42:59 olegk Exp $
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

package org.apache.commons.httpclient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.server.HttpRequestHandler;
import org.apache.commons.httpclient.server.ResponseWriter;
import org.apache.commons.httpclient.server.SimpleHttpServerConnection;
import org.apache.commons.httpclient.server.SimpleRequest;

/**
 * Tests ability to abort method execution.
 *
 * @author Oleg Kalnichevski
 * 
 * @version $Revision: 480424 $
 */
public class TestMethodAbort extends HttpClientTestBase {

    // ------------------------------------------------------------ Constructor
    public TestMethodAbort(final String testName) throws IOException {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestMethodAbort.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestMethodAbort.class);
    }

    private class ProduceGarbageHandler implements HttpRequestHandler {

        public ProduceGarbageHandler() {
            super();
        }

        public boolean processRequest(
            final SimpleHttpServerConnection conn,
            final SimpleRequest request) throws IOException
        {

            final String garbage = "garbage!\r\n";  
            final long count = 1000000000;  

            HttpVersion httpversion = request.getRequestLine().getHttpVersion();
            ResponseWriter out = conn.getWriter();
            out.println(httpversion + " 200 OK");
            out.println("Content-Type: text/plain");
            out.println("Content-Length: " + count * garbage.length()) ;
            out.println("Connection: close");
            out.println();
            for (int i = 0; i < count; i++) {
                out.print(garbage);
            }
            return true;
        }
    }

    public void testAbortMethod() throws IOException {
        this.server.setRequestHandler(new ProduceGarbageHandler());
        final GetMethod httpget = new GetMethod("/test/");
        
        Thread thread = new Thread(new Runnable() {
            public void run() {            
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
                httpget.abort();
            }
            
        });
        thread.setDaemon(true); 
        thread.start();
        
        try {
            this.client.executeMethod(httpget);
            BufferedReader in = new BufferedReader(new InputStreamReader(
                httpget.getResponseBodyAsStream()));
            String line = null;
            while ((line = in.readLine()) != null) {  
            }
            fail("IOException must have been thrown");
        } catch (IOException e) {
            // expected
        } finally {
            httpget.releaseConnection();
        }
        assertTrue(httpget.isAborted());
    }

    public void testAbortedMethodExecute() throws IOException {
        final GetMethod httpget = new GetMethod("/test/");
        
        try {
            httpget.abort();
            try {
                this.client.executeMethod(httpget);
                fail("IllegalStateException must have been thrown");
            } catch (IllegalStateException e) {
            }
        } finally {
            httpget.releaseConnection();
        }
        assertTrue(httpget.isAborted());
    }
}
