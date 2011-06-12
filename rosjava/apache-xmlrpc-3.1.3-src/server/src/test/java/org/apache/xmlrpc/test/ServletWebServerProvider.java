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
import java.net.URL;

import javax.servlet.ServletException;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcSunHttpTransportFactory;
import org.apache.xmlrpc.client.XmlRpcTransportFactory;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.ServletWebServer;
import org.apache.xmlrpc.webserver.XmlRpcServlet;


/** A provider class for testing the {@link ServletWebServer}.
 */
public class ServletWebServerProvider extends ClientProviderImpl {
	protected final ServletWebServer webServer;
    protected final XmlRpcServlet servlet;
	private final boolean contentLength;
	private final int port;

	/**
	 * Creates a new instance of {@link XmlRpcServlet}.
	 */
	protected XmlRpcServlet newXmlRpcServlet() {
	    return new XmlRpcServlet();
    }
	
	/** Creates a new instance.
	 * @param pMapping The test servers handler mapping.
	 * @throws ServletException 
	 * @throws IOException 
	 */
	protected ServletWebServerProvider(XmlRpcHandlerMapping pMapping, boolean pContentLength) throws ServletException, IOException {
		super(pMapping);
		contentLength = pContentLength;
		servlet = newXmlRpcServlet();
		webServer = new ServletWebServer(servlet, 0);
		XmlRpcServer server = servlet.getXmlRpcServletServer();
		server.setHandlerMapping(mapping);
		XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) server.getConfig();
		serverConfig.setEnabledForExtensions(true);
		serverConfig.setContentLengthOptional(!contentLength);
        serverConfig.setEnabledForExceptions(true);
		webServer.start();
		port = webServer.getPort();
	 }

	public final XmlRpcClientConfigImpl getConfig() throws Exception {
		return getConfig(new URL("http://127.0.0.1:" + port + "/"));
	}

	protected XmlRpcClientConfigImpl getConfig(URL pServerURL) throws Exception {
		XmlRpcClientConfigImpl config = super.getConfig();
		config.setServerURL(pServerURL);
		config.setContentLengthOptional(!contentLength);
		return config;
	}

	protected XmlRpcTransportFactory getTransportFactory(XmlRpcClient pClient) {
		return new XmlRpcSunHttpTransportFactory(pClient);
	}

    public XmlRpcServer getServer() {
        return servlet.getXmlRpcServletServer();
    }

    public void shutdown() {
        webServer.shutdown();
    }
}
