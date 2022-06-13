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

import java.util.List;

import org.apache.xmlrpc.XmlRpcConfig;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.XmlRpcController;
import org.apache.xmlrpc.common.XmlRpcWorkerFactory;
import org.apache.xmlrpc.serializer.XmlWriterFactory;


/** <p>The main access point of an XML-RPC client. This object serves mainly
 * as an object factory. It is designed with singletons in mind: Basically,
 * an application should be able to hold a single instance of
 * <code>XmlRpcClient</code> in a static variable, unless you would be
 * working with different factories.</p>
 * <p>Until Apache XML-RPC 2.0, this object was used both as an object
 * factory and as a place, where configuration details (server URL,
 * suggested encoding, user credentials and the like) have been stored.
 * In Apache XML-RPC 3.0, the configuration details has been moved to
 * the {@link org.apache.xmlrpc.client.XmlRpcClientConfig} object.
 * The configuration object is designed for being passed through the
 * actual worker methods.</p>
 * <p>A configured XmlRpcClient object is thread safe: In other words,
 * the suggested use is, that you configure the client using
 * {@link #setTransportFactory(XmlRpcTransportFactory)} and similar
 * methods, store it in a field and never modify it again. Without
 * modifications, the client may be used for an arbitrary number
 * of concurrent requests.</p>
 * @since 3.0
 */
public final class XmlRpcClient extends XmlRpcController {
	private XmlRpcTransportFactory transportFactory = XmlRpcClientDefaults.newTransportFactory(this);
	private XmlRpcClientConfig config = XmlRpcClientDefaults.newXmlRpcClientConfig();
	private XmlWriterFactory xmlWriterFactory = XmlRpcClientDefaults.newXmlWriterFactory();

	protected XmlRpcWorkerFactory getDefaultXmlRpcWorkerFactory() {
		return new XmlRpcClientWorkerFactory(this);
	}

	/** Sets the clients default configuration. This configuration
	 * is used by the methods
	 * {@link #execute(String, List)},
	 * {@link #execute(String, Object[])}, and
	 * {@link #execute(XmlRpcRequest)}.
	 * You may overwrite this per request by using
	 * {@link #execute(XmlRpcClientConfig, String, List)},
	 * or {@link #execute(XmlRpcClientConfig, String, Object[])}.
	 * @param pConfig The default request configuration.
	 */
	public void setConfig(XmlRpcClientConfig pConfig) {
		config = pConfig;
	}

	/** Returns the clients default configuration. This configuration
	 * is used by the methods
	 * {@link #execute(String, List)},
	 * {@link #execute(String, Object[])}.
	 * You may overwrite this per request by using
	 * {@link #execute(XmlRpcClientConfig, String, List)},
	 * or {@link #execute(XmlRpcClientConfig, String, Object[])}.
	 * @return The default request configuration.
	 */
	public XmlRpcConfig getConfig() {
		return config;
	}

	/** Returns the clients default configuration. Shortcut for
	 * <code>(XmlRpcClientConfig) getConfig()</code>.
	 * This configuration is used by the methods
	 * {@link #execute(String, List)},
	 * {@link #execute(String, Object[])}.
	 * You may overwrite this per request by using
	 * {@link #execute(XmlRpcClientConfig, String, List)}, or
	 * {@link #execute(XmlRpcClientConfig, String, Object[])}
	 * @return The default request configuration.
	 */
	public XmlRpcClientConfig getClientConfig() {
		return config;
	}

	/** Sets the clients transport factory. The client will invoke the
	 * factory method {@link XmlRpcTransportFactory#getTransport()}
	 * for any request.
	 * @param pFactory The clients transport factory.
	 */
	public void setTransportFactory(XmlRpcTransportFactory pFactory) {
		transportFactory = pFactory;
	}
	
	/** Returns the clients transport factory. The client will use this factory
	 * for invocation of {@link XmlRpcTransportFactory#getTransport()}
	 * for any request.
	 * @return The clients transport factory.
	 */
	public final XmlRpcTransportFactory getTransportFactory() {
		return this.transportFactory;
	}

	/** Performs a request with the clients default configuration.
	 * @param pMethodName The method being performed.
	 * @param pParams The parameters.
	 * @return The result object.
	 * @throws XmlRpcException Performing the request failed.
	 */
	public Object execute(String pMethodName, Object[] pParams) throws XmlRpcException {
		return execute(getClientConfig(), pMethodName, pParams);
	}

	/** Performs a request with the given configuration.
	 * @param pConfig The request configuration.
	 * @param pMethodName The method being performed.
	 * @param pParams The parameters.
	 * @return The result object.
	 * @throws XmlRpcException Performing the request failed.
	 */
	public Object execute(XmlRpcClientConfig pConfig, String pMethodName, Object[] pParams) throws XmlRpcException {
		return execute(new XmlRpcClientRequestImpl(pConfig, pMethodName, pParams));
	}

	/** Performs a request with the clients default configuration.
	 * @param pMethodName The method being performed.
	 * @param pParams The parameters.
	 * @return The result object.
	 * @throws XmlRpcException Performing the request failed.
	 */
	public Object execute(String pMethodName, List pParams) throws XmlRpcException {
		return execute(getClientConfig(), pMethodName, pParams);
	}

	/** Performs a request with the given configuration.
	 * @param pConfig The request configuration.
	 * @param pMethodName The method being performed.
	 * @param pParams The parameters.
	 * @return The result object.
	 * @throws XmlRpcException Performing the request failed.
	 */
	public Object execute(XmlRpcClientConfig pConfig, String pMethodName, List pParams) throws XmlRpcException {
		return execute(new XmlRpcClientRequestImpl(pConfig, pMethodName, pParams));
	}

	/** Performs a request with the clients default configuration.
	 * @param pRequest The request being performed.
	 * @return The result object.
	 * @throws XmlRpcException Performing the request failed.
	 */
	public Object execute(XmlRpcRequest pRequest) throws XmlRpcException {
		return getWorkerFactory().getWorker().execute(pRequest);
	}

	/** Performs an asynchronous request with the clients default configuration.
	 * @param pMethodName The method being performed.
	 * @param pParams The parameters.
	 * @param pCallback The callback being notified when the request is finished.
	 * @throws XmlRpcException Performing the request failed.
	 */
	public void executeAsync(String pMethodName, Object[] pParams,
							 AsyncCallback pCallback) throws XmlRpcException {
		executeAsync(getClientConfig(), pMethodName, pParams, pCallback);
	}

	/** Performs an asynchronous request with the given configuration.
	 * @param pConfig The request configuration.
	 * @param pMethodName The method being performed.
	 * @param pParams The parameters.
	 * @param pCallback The callback being notified when the request is finished.
	 * @throws XmlRpcException Performing the request failed.
	 */
	public void executeAsync(XmlRpcClientConfig pConfig,
							 String pMethodName, Object[] pParams,
							 AsyncCallback pCallback) throws XmlRpcException {
		executeAsync(new XmlRpcClientRequestImpl(pConfig, pMethodName, pParams),
					 pCallback);
	}

	/** Performs an asynchronous request with the clients default configuration.
	 * @param pMethodName The method being performed.
	 * @param pParams The parameters.
	 * @param pCallback The callback being notified when the request is finished.
	 * @throws XmlRpcException Performing the request failed.
	 */
	public void executeAsync(String pMethodName, List pParams,
						 	   AsyncCallback pCallback) throws XmlRpcException {
		executeAsync(getClientConfig(), pMethodName, pParams, pCallback);
	}

	/** Performs an asynchronous request with the given configuration.
	 * @param pConfig The request configuration.
	 * @param pMethodName The method being performed.
	 * @param pParams The parameters.
	 * @param pCallback The callback being notified when the request is finished.
	 * @throws XmlRpcException Performing the request failed.
	 */
	public void executeAsync(XmlRpcClientConfig pConfig,
							 String pMethodName, List pParams,
						 	 AsyncCallback pCallback) throws XmlRpcException {
		executeAsync(new XmlRpcClientRequestImpl(pConfig, pMethodName, pParams), pCallback);
	}

	/** Performs a request with the clients default configuration.
	 * @param pRequest The request being performed.
	 * @param pCallback The callback being notified when the request is finished.
	 * @throws XmlRpcException Performing the request failed.
	 */
	public void executeAsync(XmlRpcRequest pRequest,
							 AsyncCallback pCallback) throws XmlRpcException {
		XmlRpcClientWorker w = (XmlRpcClientWorker) getWorkerFactory().getWorker();
		w.execute(pRequest, pCallback);
	}

	/** Returns the clients instance of
	 * {@link org.apache.xmlrpc.serializer.XmlWriterFactory}.
	 * @return A factory for creating instances of
	 * {@link org.apache.ws.commons.serialize.XMLWriter}.
	 */
	public XmlWriterFactory getXmlWriterFactory() {
		return xmlWriterFactory;
	}

	/** Sets the clients instance of
	 * {@link org.apache.xmlrpc.serializer.XmlWriterFactory}.
	 * @param pFactory A factory for creating instances of
	 * {@link org.apache.ws.commons.serialize.XMLWriter}.
	 */
	public void setXmlWriterFactory(XmlWriterFactory pFactory) {
		this.xmlWriterFactory = pFactory;
	}
}
