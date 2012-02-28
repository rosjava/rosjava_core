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
package org.apache.xmlrpc.server;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcHandler;


/** Maps from a handler name to a handler object.
 * @since 1.2
 */
public interface XmlRpcHandlerMapping {
	/** Return the handler for the specified handler name.
	 * @param handlerName The name of the handler to retrieve.
	 * @return Object The desired handler. Never null, an exception
	 * is thrown if no such handler is available.
	 * @throws XmlRpcNoSuchHandlerException The handler is not available.
	 * @throws XmlRpcException An internal error occurred.
	 */
	public XmlRpcHandler getHandler(String handlerName)
		throws XmlRpcNoSuchHandlerException, XmlRpcException;
}
