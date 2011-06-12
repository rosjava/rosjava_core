/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.xmlrpc.test;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcHandler;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcNoSuchHandlerException;
import org.apache.xmlrpc.webserver.XmlRpcServlet;


/**
 * Test case for reading the clients IP address.
 */
public class ClientIpTest extends XmlRpcTestCase {
    /**
     * An object, which provides additional information
     * about the client to the user.
     */
    public static class ClientInfo {
        private final String ipAddress;

        /**
         * Creates a new instance.
         */
        public ClientInfo(String pIpAddress) {
            ipAddress = pIpAddress;
        }

        /**
         * Returns the clients IP address.
         */
        public String getIpAddress() {
            return ipAddress;
        }
    }

    /**
     * An extension of the {@link XmlRpcServlet}, which
     * ensures the availability of a {@link ClientIpTest.ClientInfo}
     * object.
     */
    public static class ClientInfoServlet extends XmlRpcServlet {
        private static final long serialVersionUID = 8210342625908021538L;
        private static ThreadLocal clientInfo = new ThreadLocal();

        /**
         * Returns the current threads. client info object.
         */
        public static ClientInfo getClientInfo() {
            return (ClientInfo) clientInfo.get();
        }

        public void doPost(HttpServletRequest pRequest,
                HttpServletResponse pResponse) throws IOException,
                ServletException {
            clientInfo.set(new ClientInfo(pRequest.getRemoteAddr()));
            super.doPost(pRequest, pResponse);
        }
    }

    private static class ClientIpTestProvider extends ServletWebServerProvider {
        ClientIpTestProvider(XmlRpcHandlerMapping pMapping, boolean pContentLength)
                throws ServletException, IOException {
            super(pMapping, pContentLength);
        }

        protected XmlRpcServlet newXmlRpcServlet() {
            return new ClientInfoServlet();
        }
    }
    
    protected ClientProvider[] initProviders(XmlRpcHandlerMapping pMapping)
            throws ServletException, IOException {
        return new ClientProvider[]{
            new ClientIpTestProvider(pMapping, false),
            new ClientIpTestProvider(pMapping, true)
        };
    }

    protected XmlRpcHandlerMapping getHandlerMapping() throws IOException,
            XmlRpcException {
        final XmlRpcHandler handler = new XmlRpcHandler(){
            public Object execute(XmlRpcRequest pRequest) throws XmlRpcException {
                final ClientInfo clientInfo = ClientInfoServlet.getClientInfo();
                if (clientInfo == null) {
                    return "";
                }
                final String ip = clientInfo.getIpAddress();
                if (ip == null) {
                    return "";
                }
                return ip;
            }
        };
        return new XmlRpcHandlerMapping(){
            public XmlRpcHandler getHandler(String pHandlerName)
                    throws XmlRpcNoSuchHandlerException, XmlRpcException {
                return handler;
            }
        };
    }

    private void testClientIpAddress(ClientProvider pProvider) throws Exception {
        final XmlRpcClient client = pProvider.getClient();
        client.setConfig(getConfig(pProvider));
        final String ip = (String) client.execute("getIpAddress", new Object[]{});
        assertEquals("127.0.0.1", ip);
    }
    
    /** Test, whether we can invoke a method, returning a byte.
     * @throws Exception The test failed.
     */
    public void testClientIpAddress() throws Exception {
        for (int i = 0;  i < providers.length;  i++) {
            testClientIpAddress(providers[i]);
        }
    }
}
