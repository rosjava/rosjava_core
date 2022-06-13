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

import org.apache.commons.httpclient.HttpClient;


/** An HTTP transport factory, which is based on the Jakarta Commons
 * HTTP Client.
 */
public final class XmlRpcCommonsTransportFactory extends XmlRpcTransportFactoryImpl {
    private HttpClient httpClient;

    /** Creates a new instance.
	 * @param pClient The client, which is controlling the factory.
	 */
	public XmlRpcCommonsTransportFactory(XmlRpcClient pClient) {
		super(pClient);
	}

	public XmlRpcTransport getTransport() {
		return new XmlRpcCommonsTransport(this);
	}

	/**
     * <p>Sets the factories {@link HttpClient}. By default, a new instance
     * of {@link HttpClient} is created for any request.</p>
     * <p>Reusing the {@link HttpClient} is required, if you want to preserve
     * some state between requests. This applies, in particular, if you want
     * to use cookies: In that case, create an instance of {@link HttpClient},
     * give it to the factory, and use {@link HttpClient#getState()} to
     * read or set cookies.
	 */
    public void setHttpClient(HttpClient pHttpClient) {
        httpClient = pHttpClient;
    }

    /**
     * <p>Returns the factories {@link HttpClient}. By default, a new instance
     * of {@link HttpClient} is created for any request.</p>
     * <p>Reusing the {@link HttpClient} is required, if you want to preserve
     * some state between requests. This applies, in particular, if you want
     * to use cookies: In that case, create an instance of {@link HttpClient},
     * give it to the factory, and use {@link HttpClient#getState()} to
     * read or set cookies.
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }
}
