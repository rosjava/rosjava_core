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
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.XmlRpcController;
import org.apache.xmlrpc.common.XmlRpcWorker;


/** Server specific implementation of {@link XmlRpcWorker}.
 */
public class XmlRpcServerWorker implements XmlRpcWorker {
	private final XmlRpcServerWorkerFactory factory;

	/** Creates a new instance.
	 * @param pFactory The factory creating the worker.
	 */
	public XmlRpcServerWorker(XmlRpcServerWorkerFactory pFactory) {
		factory = pFactory;
	}

	public XmlRpcController getController() { return factory.getController(); }

	public Object execute(XmlRpcRequest pRequest) throws XmlRpcException {
		XmlRpcServer server = (XmlRpcServer) getController();
		XmlRpcHandlerMapping mapping = server.getHandlerMapping();
		XmlRpcHandler handler = mapping.getHandler(pRequest.getMethodName());
		return handler.execute(pRequest);
	}
}
