/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/HttpClientTestBase.java,v 1.7 2004/11/07 12:31:42 olegk Exp $
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
 */

package org.apache.commons.httpclient;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.server.SimpleHttpServer;
import org.apache.commons.httpclient.server.SimplePlainSocketFactory;
import org.apache.commons.httpclient.server.SimpleProxy;
import org.apache.commons.httpclient.server.SimpleSocketFactory;
import org.apache.commons.httpclient.ssl.SimpleSSLSocketFactory;
import org.apache.commons.httpclient.ssl.SimpleSSLTestProtocolSocketFactory;

/**
 * Base class for test cases using 
 * {@link org.apache.commons.httpclient.server.SimpleHttpServer} based 
 * testing framework.
 *
 * @author Oleg Kalnichevski
 * 
 * @version $Id: HttpClientTestBase.java 480424 2006-11-29 05:56:49Z bayard $
 */
public class HttpClientTestBase extends TestCase {

    protected HttpClient client = null;
    protected SimpleHttpServer server = null;

    protected SimpleProxy proxy = null;
    private boolean useProxy = false;
    private boolean useSSL = false;
    
    // ------------------------------------------------------------ Constructor
    public HttpClientTestBase(final String testName) throws IOException {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { HttpClientTestBase.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(HttpClientTestBase.class);
    }

    public void setUseProxy(boolean useProxy) {
        this.useProxy = useProxy;
    }
    
    public void setUseSSL(boolean b) {
        this.useSSL = b;
    }
    
    public boolean isUseSSL() {
        return this.useSSL;
    }
    
    // ------------------------------------------------- TestCase setup/shutdown

    public void setUp() throws IOException {
        // Configure socket factories
        SimpleSocketFactory serversocketfactory = null; 
        Protocol testhttp = null;
        if (this.useSSL) {
            serversocketfactory = new SimpleSSLSocketFactory(); 
            testhttp = new Protocol("https", 
                    (ProtocolSocketFactory)new SimpleSSLTestProtocolSocketFactory(), 443);
        } else {
            serversocketfactory = new SimplePlainSocketFactory(); 
            testhttp = Protocol.getProtocol("http"); 
        }

        this.server = new SimpleHttpServer(serversocketfactory, 0); // use arbitrary port
        this.server.setTestname(getName());

        this.client = new HttpClient();

        this.client.getHostConfiguration().setHost(
                this.server.getLocalAddress(), 
                this.server.getLocalPort(),
                testhttp);
        
        if (this.useProxy) {
            this.proxy = new SimpleProxy();
            client.getHostConfiguration().setProxy(
                proxy.getLocalAddress(), 
                proxy.getLocalPort());                
        }
    }

    public void tearDown() throws IOException {
        this.client = null;
        this.server.destroy();
        this.server = null;
        if (proxy != null) {
            proxy.destroy();
            proxy = null;
        }
    }    
}
