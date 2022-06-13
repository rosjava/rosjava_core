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

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.XmlRpcController;
import org.apache.xmlrpc.common.XmlRpcWorker;


/** Object, which performs a request on the clients behalf.
 * The client maintains a pool of workers. The main purpose of the
 * pool is limitation of the maximum number of concurrent requests.
 * @since 3.0
 */
final class XmlRpcClientWorker implements XmlRpcWorker {
	private final XmlRpcClientWorkerFactory factory;

	/** Creates a new instance.
	 * @param pFactory The factory, which is being notified, if
	 * the worker's ready.
	 */
	public XmlRpcClientWorker(XmlRpcClientWorkerFactory pFactory) {
		factory = pFactory;
	}

	public XmlRpcController getController() {
		return factory.getController();
	}

	/** Performs a synchronous request.
	 * @param pRequest The request being performed.
	 * @return The requests result.
	 * @throws XmlRpcException Performing the request failed.
	 */
	public Object execute(XmlRpcRequest pRequest)
			throws XmlRpcException {
		try {
			XmlRpcClient client = (XmlRpcClient) getController();
			return client.getTransportFactory().getTransport().sendRequest(pRequest);
		} finally {
			factory.releaseWorker(this);
		}
	}

	protected Thread newThread(Runnable pRunnable) {
		Thread result = new Thread(pRunnable);
		result.setDaemon(true);
		return result;
	}

	/** Performs an synchronous request.
	 * @param pRequest The request being performed.
	 * @param pCallback The callback being invoked, when the request is finished.
	 */
	public void execute(final XmlRpcRequest pRequest,
						final AsyncCallback pCallback) {
		Runnable runnable = new Runnable(){
			public void run(){
				Object result = null;
				Throwable th = null;
				try {
					XmlRpcClient client = (XmlRpcClient) getController();
					result = client.getTransportFactory().getTransport().sendRequest(pRequest);
				} catch (Throwable t) {
					th = t;
				}
				factory.releaseWorker(XmlRpcClientWorker.this);
				if (th == null) {
					pCallback.handleResult(pRequest, result);
				} else {
					pCallback.handleError(pRequest, th);
				}
			}
		};
		newThread(runnable).start();
	}
}
