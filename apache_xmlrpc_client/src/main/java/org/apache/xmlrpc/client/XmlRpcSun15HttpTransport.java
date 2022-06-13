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

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.apache.xmlrpc.XmlRpcRequest;


/**
 * Default implementation of an HTTP transport in Java 1.4, based on the
 * {@link java.net.HttpURLConnection} class. Adds support for the
 * {@link Proxy} class.
 */
final class XmlRpcSun15HttpTransport extends XmlRpcSun14HttpTransport {
    /**
     * Creates a new instance.
     * @param pClient The client controlling this instance.
     */
    public XmlRpcSun15HttpTransport(XmlRpcClient pClient) {
        super(pClient);
    }

    private Proxy proxy;

    /**
     * Sets the proxy to use.
     */
    public void setProxy(Proxy pProxy) {
        proxy = pProxy;
    }

    /**
     * Returns the proxy to use.
     */
    public Proxy getProxy() {
        return proxy;
    }

    protected void initHttpHeaders(XmlRpcRequest pRequest)
            throws XmlRpcClientException {
        final XmlRpcHttpClientConfig config = (XmlRpcHttpClientConfig) pRequest.getConfig();
        int connectionTimeout = config.getConnectionTimeout();
        if (connectionTimeout > 0) {
            getURLConnection().setConnectTimeout(connectionTimeout);
        }
        int replyTimeout = config.getReplyTimeout();
        if (replyTimeout > 0) {
            getURLConnection().setReadTimeout(replyTimeout);
        }
        super.initHttpHeaders(pRequest);
    }

    protected URLConnection newURLConnection(URL pURL) throws IOException {
        final Proxy prox = getProxy();
        final URLConnection conn = prox == null ? pURL.openConnection() : pURL.openConnection(prox);
        final SSLSocketFactory sslSockFactory = getSSLSocketFactory();
        if (sslSockFactory != null  &&  conn instanceof HttpsURLConnection) {
            ((HttpsURLConnection)conn).setSSLSocketFactory(sslSockFactory);
        }
        return conn;
    }
}
