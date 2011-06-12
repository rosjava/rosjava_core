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
import java.io.StringWriter;

import javax.servlet.ServletException;

import junit.framework.TestCase;

import org.apache.ws.commons.serialize.XMLWriter;
import org.apache.ws.commons.serialize.XMLWriterImpl;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfig;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.common.TypeConverterFactory;
import org.apache.xmlrpc.common.TypeConverterFactoryImpl;
import org.apache.xmlrpc.common.XmlRpcStreamConfig;
import org.apache.xmlrpc.serializer.XmlRpcWriter;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
import org.xml.sax.SAXException;


/** Abstract base class for deriving test cases.
 */
public abstract class XmlRpcTestCase extends TestCase {
    protected ClientProvider[] providers;

    protected abstract XmlRpcHandlerMapping getHandlerMapping() throws IOException, XmlRpcException;

    protected XmlRpcClientConfigImpl getConfig(ClientProvider pProvider) throws Exception {
        return pProvider.getConfig();
    }

    protected XmlRpcClientConfig getExConfig(ClientProvider pProvider) throws Exception {
        XmlRpcClientConfigImpl config = getConfig(pProvider);
        config.setEnabledForExtensions(true);
        config.setEnabledForExceptions(true);
        return config;
    }

    protected XmlRpcHandlerMapping getHandlerMapping(String pResource) throws IOException, XmlRpcException {
        PropertyHandlerMapping mapping = new PropertyHandlerMapping();
        mapping.setVoidMethodEnabled(true);
        mapping.load(getClass().getClassLoader(), getClass().getResource(pResource));
        mapping.setTypeConverterFactory(getTypeConverterFactory());
        return mapping;
    }

    protected ClientProvider[] initProviders(XmlRpcHandlerMapping pMapping) throws ServletException, IOException {
        return new ClientProvider[]{
                new LocalTransportProvider(pMapping),
                new LocalStreamTransportProvider(pMapping),
                new LiteTransportProvider(pMapping, true),
                // new LiteTransportProvider(mapping, false), Doesn't support HTTP/1.1
                new SunHttpTransportProvider(pMapping, true),
                new SunHttpTransportProvider(pMapping, false),
                new CommonsProvider(pMapping),
                new ServletWebServerProvider(pMapping, true),
                new ServletWebServerProvider(pMapping, false)
            };
    }

    public void setUp() throws Exception {
        if (providers == null) {
            providers = initProviders(getHandlerMapping());
        }
    }

    public void tearDown() throws Exception {
        if (providers != null) {
            for (int i = 0;  i < providers.length;  i++) {
                providers[i].shutdown();
            }
        }
    }

    protected TypeConverterFactory getTypeConverterFactory() {
        return new TypeConverterFactoryImpl();
    }

    static String writeRequest(XmlRpcClient pClient, XmlRpcRequest pRequest)
            throws SAXException {
        StringWriter sw = new StringWriter();
        XMLWriter xw = new XMLWriterImpl();
        xw.setEncoding("US-ASCII");
        xw.setDeclarating(true);
        xw.setIndenting(false);
        xw.setWriter(sw);
        XmlRpcWriter xrw = new XmlRpcWriter((XmlRpcStreamConfig) pClient.getConfig(), xw, pClient.getTypeFactory());
        xrw.write(pRequest);
        return sw.toString();
    }
}
