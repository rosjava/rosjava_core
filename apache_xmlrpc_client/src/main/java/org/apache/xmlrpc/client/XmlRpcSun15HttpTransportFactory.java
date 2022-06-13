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

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * Default implementation of an HTTP transport in Java 1.5, based on the
 * {@link java.net.HttpURLConnection} class.
 */
final class XmlRpcSun15HttpTransportFactory extends XmlRpcSun14HttpTransportFactory {
    private Proxy proxy;

    /**
     * Creates a new factory, which creates transports for the given client.
     * @param pClient The client, which is operating the factory.
     */
    public XmlRpcSun15HttpTransportFactory(XmlRpcClient pClient) {
        super(pClient);
    }

    /**
     * Sets the proxy to use.
     * @param proxyHost The proxy hostname.
     * @param proxyPort The proxy port number.
     * @throws IllegalArgumentException if the proxyHost parameter is null or if
     *     the proxyPort parameter is outside the range of valid port values.
     */
    public void setProxy(String proxyHost, int proxyPort) {
        setProxy(new Proxy(Proxy.Type.HTTP,new InetSocketAddress(proxyHost,proxyPort)));
    }

    /**
     * Sets the proxy to use.
     * @param pProxy The proxy settings.
     */
    public void setProxy(Proxy pProxy) {
        proxy = pProxy;
    }

    public XmlRpcTransport getTransport() {
        XmlRpcSun15HttpTransport transport = new XmlRpcSun15HttpTransport(getClient());
        transport.setSSLSocketFactory(getSSLSocketFactory());
        transport.setProxy(proxy);
        return transport;
    }
}
