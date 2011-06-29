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

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.XmlRpcRequestConfig;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.util.ClientFactory;
import org.apache.xmlrpc.common.XmlRpcHttpRequestConfig;
import org.apache.xmlrpc.server.AbstractReflectiveHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
import org.apache.xmlrpc.server.AbstractReflectiveHandlerMapping.AuthenticationHandler;


/**
 * Test case for supported authentication variants.
 */
public class AuthenticationTest extends XmlRpcTestCase {
    private static final String PASSWORD = "98765432109876543210987654321098765432109876543210";
    private static final String USER_NAME = "01234567890123456789012345678901234567890123456789"
        + "\u00C4\u00D6\u00DC\u00F6\u00FC\u00E4\u00DF";

    /** An interface, which is being implemented by the
     * server.
     */
    public interface Adder {
        /** Returns the sum of the given integers.
         */
        public int add(int pNum1, int pNum2);
    }

    /** Implementation of {@link DynamicProxyTest.Adder}, which is used by
     * the server.
     */
    public static class AdderImpl implements Adder {
        public int add(int pNum1, int pNum2) {
            return pNum1 + pNum2;
        }
    }

    protected XmlRpcHandlerMapping getHandlerMapping() throws IOException, XmlRpcException {
        XmlRpcHandlerMapping mapping = getHandlerMapping("AuthenticationTest.properties");
        ((AbstractReflectiveHandlerMapping) mapping).setAuthenticationHandler(new AuthenticationHandler(){
            public boolean isAuthorized(XmlRpcRequest pRequest)
                    throws XmlRpcException {
                XmlRpcRequestConfig config = pRequest.getConfig();
                if (config instanceof XmlRpcHttpRequestConfig) {
                    XmlRpcHttpRequestConfig httpRequestConfig = (XmlRpcHttpRequestConfig) config;
                    return USER_NAME.equals(httpRequestConfig.getBasicUserName())
                        &&  PASSWORD.equals(httpRequestConfig.getBasicPassword());
                }
                return true;
            }
        });
        return mapping;
    }

    protected XmlRpcClientConfigImpl getConfig(ClientProvider pProvider)
            throws Exception {
        XmlRpcClientConfigImpl config = super.getConfig(pProvider);
        config.setBasicUserName(USER_NAME);
        config.setBasicPassword(PASSWORD);
        return config;
    }

    private ClientFactory getClientFactory(ClientProvider pProvider) throws Exception {
        XmlRpcClient client = pProvider.getClient();
        client.setConfig(getConfig(pProvider));
        return new ClientFactory(client);
    }

    /** Tests calling the {@link Adder#add(int,int)} method
     * by using an object, which has been created by the
     * {@link ClientFactory}.
     */
    public void testAdderCall() throws Exception {
        for (int i = 0;  i < providers.length;  i++) {
            testAdderCall(providers[i]);
        }
    }

    private void testAdderCall(ClientProvider pProvider) throws Exception {
        ClientFactory factory = getClientFactory(pProvider);
        Adder adder = (Adder) factory.newInstance(Adder.class);
        assertEquals(6, adder.add(2, 4));
    }
}
