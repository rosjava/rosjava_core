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

import org.apache.xmlrpc.XmlRpcConfig;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.TypeConverterFactory;
import org.apache.xmlrpc.common.TypeConverterFactoryImpl;
import org.apache.xmlrpc.common.XmlRpcController;
import org.apache.xmlrpc.common.XmlRpcRequestProcessor;
import org.apache.xmlrpc.common.XmlRpcWorker;
import org.apache.xmlrpc.common.XmlRpcWorkerFactory;


/** A multithreaded, reusable XML-RPC server object. The name may
 * be misleading because this does not open any server sockets.
 * Instead it is fed by passing instances of
 * {@link org.apache.xmlrpc.XmlRpcRequest} from
 * a transport.
 */
public class XmlRpcServer extends XmlRpcController
		implements XmlRpcRequestProcessor {
	private XmlRpcHandlerMapping handlerMapping;
    private TypeConverterFactory typeConverterFactory = new TypeConverterFactoryImpl();
	private XmlRpcServerConfig config = new XmlRpcServerConfigImpl();

	protected XmlRpcWorkerFactory getDefaultXmlRpcWorkerFactory() {
		return new XmlRpcServerWorkerFactory(this);
	}

    /** Sets the servers {@link TypeConverterFactory}.
     */
    public void setTypeConverterFactory(TypeConverterFactory pFactory) {
        typeConverterFactory = pFactory;
    }
    public TypeConverterFactory getTypeConverterFactory() {
        return typeConverterFactory;
    }

	/** Sets the servers configuration.
	 * @param pConfig The new server configuration.
	 */
	public void setConfig(XmlRpcServerConfig pConfig) { config = pConfig; }
	public XmlRpcConfig getConfig() { return config; }

	/** Sets the servers handler mapping.
	 * @param pMapping The servers handler mapping.
	 */
	public void setHandlerMapping(XmlRpcHandlerMapping pMapping) {
		handlerMapping = pMapping;
	}

	/** Returns the servers handler mapping.
	 * @return The servers handler mapping.
	 */
	public XmlRpcHandlerMapping getHandlerMapping() {
		return handlerMapping;
	}

	/** Performs the given request.
	 * @param pRequest The request being executed.
	 * @return The result object.
	 * @throws XmlRpcException The request failed.
	 */
	public Object execute(XmlRpcRequest pRequest) throws XmlRpcException {
	    final XmlRpcWorkerFactory factory = getWorkerFactory();
	    final XmlRpcWorker worker = factory.getWorker();
        try {
            return worker.execute(pRequest);
        } finally {
            factory.releaseWorker(worker);
        }
	}
}
