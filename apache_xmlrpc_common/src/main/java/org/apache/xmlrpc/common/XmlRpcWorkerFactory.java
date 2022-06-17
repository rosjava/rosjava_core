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

import java.util.ArrayList;
import java.util.List;


/** A factory for {@link XmlRpcWorker} instances.
 */
public abstract class XmlRpcWorkerFactory {
	private final XmlRpcWorker singleton = newWorker();
	private final XmlRpcController controller;
	private final List<XmlRpcWorker> pool = new ArrayList<>();
	private int numThreads;

	/** Creates a new instance.
	 * @param pController The client controlling the factory.
	 */
	public XmlRpcWorkerFactory(XmlRpcController pController) {
		this.controller = pController;
	}

	/** Creates a new worker instance.
	 * @return New instance of {@link XmlRpcWorker}.
	 */
	protected abstract XmlRpcWorker newWorker();

	/** Returns the factory controller.
	 * @return The controller, an instance of
	 * {@link org.apache.xmlrpc.client.XmlRpcClient}, or
	 * {@link org.apache.xmlrpc.server.XmlRpcServer}.
	 */
	public XmlRpcController getController() {
		return this.controller;
	}

	/** Returns a worker for synchronous processing.
	 * @return An instance of {@link XmlRpcWorker}, which is ready
	 * for use.
	 * @throws XmlRpcLoadException The clients maximum number of concurrent
	 * threads is exceeded.
	 */
	public synchronized XmlRpcWorker getWorker() throws XmlRpcLoadException {
		int max = this.controller.getMaxThreads();
		if (max > 0  &&  this.numThreads == max) {
			throw new XmlRpcLoadException("Maximum number of concurrent requests exceeded: " + max);
		}
		if (max == 0) {
			return this.singleton;
		}
        ++this.numThreads;
		if (this.pool.isEmpty()) {
			return newWorker();
		} else {
			return this.pool.remove(this.pool.size() - 1);
		}
	}

	/** Called, when the worker did its job. Frees resources and
	 * decrements the number of concurrent requests.
	 * @param pWorker The worker being released.
	 */
	public synchronized void releaseWorker(XmlRpcWorker pWorker) {
		--this.numThreads;
		int max = this.controller.getMaxThreads();
		if (pWorker == this.singleton) {
			// Do nothing, it's the singleton
		} else {
			if (this.pool.size() < max) {
				this.pool.add(pWorker);
			}
		}
	}

	/** Returns the number of currently running requests.
	 * @return Current number of concurrent requests.
	 */
	public synchronized int getCurrentRequests() {
		return this.numThreads;
	}
}
