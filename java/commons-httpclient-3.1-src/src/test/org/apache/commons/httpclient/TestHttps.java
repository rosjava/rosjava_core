/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/TestHttps.java,v 1.12 2004/06/13 12:13:08 olegk Exp $
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * Simple tests for HTTPS support in HttpClient.
 *
 * To run this test you'll need:
 *  + a JSSE implementation installed (see README.txt)
 *  + the java.protocol.handler.pkgs system property set
 *    for your provider.  e.g.:
 *     -Djava.protocol.handler.pkgs=com.sun.net.ssl.internal.www.protocol
 *    (see build.xml)
 *
 * @author Rodney Waldhoff
 * @author Ortwin Glück
 * @version $Id: TestHttps.java 480424 2006-11-29 05:56:49Z bayard $
 */
public class TestHttps extends TestCase {

    // ---------------------------------------------------------------- Members
    private String _urlWithPort = null;
    private String _urlWithoutPort = null;
    private final String PROXY_HOST = System.getProperty("httpclient.test.proxyHost");
    private final String PROXY_PORT = System.getProperty("httpclient.test.proxyPort");
    private final String PROXY_USER = System.getProperty("httpclient.test.proxyUser");
    private final String PROXY_PASS = System.getProperty("httpclient.test.proxyPass");

    // ------------------------------------------------------------ Constructor
    public TestHttps(String testName) {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestHttps.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods
    public static Test suite() {
        return new TestSuite(TestHttps.class);
    }

    public void setUp() throws Exception {
        _urlWithPort = "https://www.verisign.com:443/";
        _urlWithoutPort = "https://www.verisign.com/";
    }

    public void testHttpsGet() {
        HttpClient client = new HttpClient();
        if (PROXY_HOST != null) {
            if (PROXY_USER != null) {
                HttpState state = client.getState();
                state.setProxyCredentials(AuthScope.ANY, new UsernamePasswordCredentials(
                    PROXY_USER, PROXY_PASS));
            }
            client.getHostConfiguration().setProxy(PROXY_HOST, Integer.parseInt(PROXY_PORT));
        }
        GetMethod method = new GetMethod(_urlWithPort);
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Exception thrown during HTTPS GET: " + t.toString());
        }

        try {
            String data = method.getResponseBodyAsString();
            // This enumeration musn't be empty
            assertTrue("No data returned.", (data.length() > 0));
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Exception thrown while retrieving data : " + t.toString());
        }
    }

    public void testHttpsGetNoPort() {
        HttpClient client = new HttpClient();
        if (PROXY_HOST != null) {
            if (PROXY_USER != null) {
                HttpState state = client.getState();
                state.setProxyCredentials(AuthScope.ANY, new UsernamePasswordCredentials(
                    PROXY_USER, PROXY_PASS));
            }
            client.getHostConfiguration().setProxy(PROXY_HOST, Integer.parseInt(PROXY_PORT));
        }
        GetMethod method = new GetMethod(_urlWithoutPort);
        
        try {
            client.executeMethod(method);
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Exception thrown during HTTPS GET: " + t.toString());
        }

        try {
            String data = method.getResponseBodyAsString();
            // This enumeration musn't be empty
            assertTrue("No data returned.", (data.length() > 0));
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Exception thrown while retrieving data : " + t.toString());
        }
    }
}
