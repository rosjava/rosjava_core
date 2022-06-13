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
package org.apache.xmlrpc.client;

import org.apache.xmlrpc.serializer.DefaultXMLWriterFactory;
import org.apache.xmlrpc.serializer.XmlWriterFactory;


/**
 * This class is responsible to provide default settings.
 */
final class XmlRpcClientDefaults {
    private static final XmlWriterFactory xmlWriterFactory = new DefaultXMLWriterFactory();

    /**
     * Creates a new transport factory for the given client.
     */
    public static XmlRpcTransportFactory newTransportFactory(XmlRpcClient pClient) {
        try {
            return new XmlRpcSun15HttpTransportFactory(pClient);
        } catch (Throwable t1) {
            try {
                return new XmlRpcSun14HttpTransportFactory(pClient);
            } catch (Throwable t2) {
                return new XmlRpcSunHttpTransportFactory(pClient);
            }
        }
    }

    /**
     * Creates a new instance of {@link XmlRpcClientConfig}.
     */
    public static XmlRpcClientConfig newXmlRpcClientConfig() {
        return new XmlRpcClientConfigImpl();
    }
    
    /**
     * Creates a new {@link XmlWriterFactory}.
     */
    public static XmlWriterFactory newXmlWriterFactory() {
        return xmlWriterFactory;
    }
}
