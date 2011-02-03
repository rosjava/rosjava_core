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
package org.apache.xmlrpc;


/** Interface to an XML-RPC request made by a client. Replaces the
 * class <code>org.apache.xmlrpc.XmlRpcClientRequest</code> from
 * Apache XML-RPC 2.0.
 * @since 3.0
 */
public interface XmlRpcRequest {
	/** Returns the request configuration.
	 * @return The request configuration.
	 */
	XmlRpcRequestConfig getConfig();
	/** Returns the requests method name.
	 * @return Name of the method being invoked.
	 */
    String getMethodName();
	/** Returns the number of parameters.
	 * @return Number of parameters.
	 */
	int getParameterCount();
	/** Returns the parameter with index <code>pIndex</code>.
	 * @param pIndex Number between 0 and {@link #getParameterCount()}-1.
	 * @return Parameter being sent to the server.
	 */
    public Object getParameter(int pIndex);
}
