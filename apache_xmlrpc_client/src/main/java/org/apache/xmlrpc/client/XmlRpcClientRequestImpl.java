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

import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.XmlRpcRequestConfig;


/** Default implementation of
 * {@link org.apache.xmlrpc.XmlRpcRequest}.
 */
public class XmlRpcClientRequestImpl implements XmlRpcRequest {
    private static final Object[] ZERO_PARAMS = new Object[0];
    private final XmlRpcRequestConfig config;
	private final String methodName;
	private final Object[] params;

	/** Creates a new instance.
	 * @param pConfig The request configuration.
	 * @param pMethodName The method name being performed.
	 * @param pParams The parameters.
	 * @throws NullPointerException One of the parameters is null.
	 */
	public XmlRpcClientRequestImpl(XmlRpcRequestConfig pConfig,
								   String pMethodName, Object[] pParams) {
		config = pConfig;
		if (config == null) {
			throw new NullPointerException("The request configuration must not be null.");
		}
		methodName = pMethodName;
		if (methodName == null) {
			throw new NullPointerException("The method name must not be null.");
		}
		params = pParams == null ? ZERO_PARAMS : pParams;
	}

	/** Creates a new instance.
	 * @param pConfig The request configuration.
	 * @param pMethodName The method name being performed.
	 * @param pParams The parameters.
	 * @throws NullPointerException The method name or the parameters are null.
	 */
	public XmlRpcClientRequestImpl(XmlRpcRequestConfig pConfig,
								   String pMethodName, List pParams) {
		this(pConfig, pMethodName, pParams == null ? null : pParams.toArray());
	}

	public String getMethodName() { return methodName; }

	public int getParameterCount() { return params.length; }

	public Object getParameter(int pIndex) { return params[pIndex]; }

	public XmlRpcRequestConfig getConfig() { return config; }
}
