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

import java.net.URL;

import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;


/** Abstract base class for providers, which require a webserver.
 */
public abstract class WebServerProvider extends ClientProviderImpl {
	protected final WebServer webServer = new WebServer(0);
	private boolean isActive;
	private final boolean contentLength;

	/** Creates a new instance.
	 * @param pMapping The test servers handler mapping.
	 */
	protected WebServerProvider(XmlRpcHandlerMapping pMapping, boolean pContentLength) {
		super(pMapping);
		contentLength = pContentLength;
	}

	public final XmlRpcClientConfigImpl getConfig() throws Exception {
		initWebServer();
		return getConfig(new URL("http://127.0.0.1:" + webServer.getPort() + "/"));
	}

	protected XmlRpcClientConfigImpl getConfig(URL pServerURL) throws Exception {
		XmlRpcClientConfigImpl config = super.getConfig();
		config.setServerURL(pServerURL);
		config.setContentLengthOptional(!contentLength);
		return config;
	}

	protected void initWebServer() throws Exception {
		if (!isActive) {
			XmlRpcServer server = webServer.getXmlRpcServer();
			server.setHandlerMapping(mapping);
			XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) server.getConfig();
			serverConfig.setEnabledForExtensions(true);
			serverConfig.setContentLengthOptional(!contentLength);
            serverConfig.setEnabledForExceptions(true);
			webServer.start();
			isActive = true;
		}
	}

	public XmlRpcServer getServer() {
	    return webServer.getXmlRpcServer();
    }

	public void shutdown() {
	    webServer.shutdown();
    }
}
