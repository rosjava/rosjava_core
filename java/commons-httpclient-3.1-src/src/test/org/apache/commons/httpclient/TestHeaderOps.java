/*
 * $HeadURL: https://svn.apache.org/repos/asf/jakarta/httpcomponents/oac.hc3x/tags/HTTPCLIENT_3_1/src/test/org/apache/commons/httpclient/TestHeaderOps.java $
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
import java.net.InetAddress;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.server.HttpService;
import org.apache.commons.httpclient.server.SimpleRequest;
import org.apache.commons.httpclient.server.SimpleResponse;

/**
 * @author Rodney Waldhoff
 * @version $Id: TestHeaderOps.java 480424 2006-11-29 05:56:49Z bayard $
 */
public class TestHeaderOps extends HttpClientTestBase {

    public TestHeaderOps(String testName) throws Exception {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestHeaderOps.class);
        return suite;
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestHeaderOps.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------------------ Tests

    class HeaderDumpService implements HttpService {

        public HeaderDumpService() {
            super();
        }

        public boolean process(final SimpleRequest request, final SimpleResponse response)
            throws IOException
        {
            HttpVersion httpversion = request.getRequestLine().getHttpVersion();
            response.setStatusLine(httpversion, HttpStatus.SC_OK);
            response.addHeader(new Header("Content-Type", "text/plain"));            
            response.addHeader(new Header("HeaderSetByServlet", "Yes"));            

            StringBuffer buffer = new StringBuffer(); 
            buffer.append("Request headers: \r\n");
            for (Iterator i = request.getHeaderIterator(); i.hasNext(); ) {
                Header header = (Header) i.next();
                buffer.append("name=\"");
                buffer.append(header.getName().toLowerCase());
                buffer.append("\";value=\"");
                buffer.append(header.getValue());
                buffer.append("\"\r\n");
            }
            response.setBodyString(buffer.toString());
            return true;
        }
    }

    /**
     * Test {@link HttpMethod#addRequestHeader}.
     */
    public void testAddRequestHeader() throws Exception {
        this.server.setHttpService(new HeaderDumpService());
        
        GetMethod method = new GetMethod("/");
        method.setRequestHeader(new Header("addRequestHeader(Header)","True"));
        method.setRequestHeader("addRequestHeader(String,String)","Also True");
        try {
            this.client.executeMethod(method);
            String s = method.getResponseBodyAsString();
            assertTrue(s.indexOf("name=\"addrequestheader(header)\";value=\"True\"") >= 0);
            assertTrue(s.indexOf("name=\"addrequestheader(string,string)\";value=\"Also True\"") >= 0);
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * Test {@link HttpMethod#removeRequestHeader}.
     */
    public void testRemoveRequestHeader() throws Exception {
        this.server.setHttpService(new HeaderDumpService());
        
        GetMethod method = new GetMethod("/");
        method.setRequestHeader(new Header("XXX-A-HEADER","true"));
        method.removeRequestHeader("XXX-A-HEADER");
        
        try {
            this.client.executeMethod(method);
            String s = method.getResponseBodyAsString();
            assertTrue(!(s.indexOf("xxx-a-header") >= 0));
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * Test {@link HttpMethod#setRequestHeader}.
     */
    public void testOverwriteRequestHeader() throws Exception {
        this.server.setHttpService(new HeaderDumpService());
        
        GetMethod method = new GetMethod("/");
        method.setRequestHeader(new Header("xxx-a-header","one"));
        method.setRequestHeader("XXX-A-HEADER","two");
        
        try {
            this.client.executeMethod(method);
            String s = method.getResponseBodyAsString();
            assertTrue(s.indexOf("name=\"xxx-a-header\";value=\"two\"") >= 0);
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * Test {@link HttpMethod#getResponseHeader}.
     */
    public void testGetResponseHeader() throws Exception {
        this.server.setHttpService(new HeaderDumpService());
        
        GetMethod method = new GetMethod("/");
        try {
            this.client.executeMethod(method);
            Header h = new Header("HeaderSetByServlet","Yes");
            assertEquals(h, method.getResponseHeader("headersetbyservlet"));
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * Test {@link HttpMethodBase.addHostRequestHeader}.
     */
    public void testHostRequestHeader() throws Exception {
        this.server.setHttpService(new HeaderDumpService());

        String hostname = this.server.getLocalAddress();
        int port = this.server.getLocalPort();
        
        InetAddress addr = InetAddress.getByName(hostname);
        String ip = addr.getHostAddress();

        GetMethod get = new GetMethod("/");

        // Open connection using IP.  Host header should be sent
        // Note: RFC 2616 is somewhat unclear on whether a host should
        // be sent in this context - however, both Mozilla and IE send
        // the header for an IP address, instead of sending blank.
        this.client.getHostConfiguration().setHost(ip, port);
        try {
            this.client.executeMethod(get);
            Header hostHeader = get.getRequestHeader("Host");
            assertTrue(hostHeader != null);
            if (port == Protocol.getProtocol("http").getDefaultPort()) {
                // no port information should be in the value
                assertTrue(hostHeader.getValue().equals(ip));
            } else {
                assertTrue(hostHeader.getValue().equals(ip + ":" + port));
            }
        } finally {
            get.releaseConnection();
        }

        get = new GetMethod("/");
        // Open connection using Host.  Host header should
        // contain this value (this test will fail if DNS
        // is not available. Additionally, if the port is
        // something other that 80, then the port value
        // should also be present in the header.
        this.client.getHostConfiguration().setHost(hostname, port);
        try {
            this.client.executeMethod(get);
            Header hostHeader = get.getRequestHeader("Host");
            assertTrue(hostHeader != null);
            if (port == Protocol.getProtocol("http").getDefaultPort()) {
                // no port information should be in the value
                assertTrue(hostHeader.getValue().equals(hostname));
            } else {
                assertTrue(hostHeader.getValue().equals(hostname + ":" + port));
            }
        } finally {
            get.releaseConnection();
        }
    }
}

