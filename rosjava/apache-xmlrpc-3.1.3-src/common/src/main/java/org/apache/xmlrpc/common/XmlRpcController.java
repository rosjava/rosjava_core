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
package org.apache.xmlrpc.common;

import org.apache.xmlrpc.XmlRpcConfig;


/** A common base class for
 * {@link org.apache.xmlrpc.server.XmlRpcServer} and
 * {@link org.apache.xmlrpc.client.XmlRpcClient}.
 */
public abstract class XmlRpcController {
	private XmlRpcWorkerFactory workerFactory = getDefaultXmlRpcWorkerFactory();
	private int maxThreads;
	private TypeFactory typeFactory = new TypeFactoryImpl(this);

	/** Creates the controllers default worker factory.
	 * @return The default factory for workers.
	 */
	protected abstract XmlRpcWorkerFactory getDefaultXmlRpcWorkerFactory();

	/** Sets the maximum number of concurrent requests. This includes
	 * both synchronous and asynchronous requests.
	 * @param pMaxThreads Maximum number of threads or 0 to disable
	 * the limit.
	 */
	public void setMaxThreads(int pMaxThreads) {
		maxThreads = pMaxThreads;
	}

	/** Returns the maximum number of concurrent requests. This includes
	 * both synchronous and asynchronous requests.
	 * @return Maximum number of threads or 0 to disable
	 * the limit.
	 */
	public int getMaxThreads() {
		return maxThreads;
	}

	/** Sets the clients worker factory.
	 * @param pFactory The factory being used to create workers.
	 */
	public void setWorkerFactory(XmlRpcWorkerFactory pFactory) {
		workerFactory = pFactory;
	}

	/** Returns the clients worker factory.
	 * @return The factory being used to create workers.
	 */
	public XmlRpcWorkerFactory getWorkerFactory() {
		return workerFactory;
	}

	/** Returns the controllers default configuration.
	 * @return The default configuration.
	 */
	public abstract XmlRpcConfig getConfig();

	/** Sets the type factory.
	 * @param pTypeFactory The type factory.
	 */
	public void setTypeFactory(TypeFactory pTypeFactory) {
		typeFactory = pTypeFactory;
	}

	/** Returns the type factory.
	 * @return The type factory.
	 */
	public TypeFactory getTypeFactory() {
		return typeFactory;
	}
}
