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

import java.net.URL;

import org.apache.xmlrpc.common.XmlRpcHttpRequestConfig;


/** Extension of {@link org.apache.xmlrpc.client.XmlRpcClientConfig}
 * for HTTP based transport. Provides details like server URL,
 * user credentials, and so on.
 */
public interface XmlRpcHttpClientConfig extends XmlRpcHttpRequestConfig {
	/** Returns the HTTP servers URL.
	 * @return XML-RPC servers URL; for example, this may be the URL of a
	 * servlet
	 */
	URL getServerURL();
    
    /**
     * Returns the user agent header to use 
     * @return the http user agent header to set when doing xmlrpc requests
     */
    String getUserAgent();
}
