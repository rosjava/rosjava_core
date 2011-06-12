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
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocketFactory;


/**
 * A "light" HTTP transport implementation for Java 1.4.
 */
public class XmlRpcLite14HttpTransport extends XmlRpcLiteHttpTransport {
    private SSLSocketFactory sslSocketFactory;

    /**
     * Creates a new instance.
     * @param pClient The client controlling this instance.
     */
    public XmlRpcLite14HttpTransport(XmlRpcClient pClient) {
        super(pClient);
    }

    /**
     * Sets the SSL Socket Factory to use for https connections.
     */
    public SSLSocketFactory getSSLSocketFactory() {
        return sslSocketFactory;
    }

    /**
     * Returns the SSL Socket Factory to use for https connections.
     */
    public void setSSLSocketFactory(SSLSocketFactory pSSLSocketFactory) {
        sslSocketFactory = pSSLSocketFactory;
    }

    protected Socket newSocket(boolean pSSL, String pHostName, int pPort) throws UnknownHostException, IOException {
        if (pSSL) {
            SSLSocketFactory sslSockFactory = getSSLSocketFactory();
            if (sslSockFactory == null) {
                sslSockFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            }
            return sslSockFactory.createSocket(pHostName, pPort);
        } else {
            return super.newSocket(pSSL, pHostName, pPort);
        }
    }
}
