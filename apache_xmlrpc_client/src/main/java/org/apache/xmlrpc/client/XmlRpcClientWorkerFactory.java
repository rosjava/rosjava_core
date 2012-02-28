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

import org.apache.xmlrpc.common.XmlRpcWorker;
import org.apache.xmlrpc.common.XmlRpcWorkerFactory;


/** A worker factory for the client, creating instances of
 * {@link org.apache.xmlrpc.client.XmlRpcClientWorker}.
 */
public class XmlRpcClientWorkerFactory extends XmlRpcWorkerFactory {
	/** Creates a new instance.
	 * @param pClient The factory controller.
	 */
	public XmlRpcClientWorkerFactory(XmlRpcClient pClient) {
		super(pClient);
	}

	/** Creates a new worker instance.
	 * @return New instance of {@link XmlRpcClientWorker}.
	 */
	protected XmlRpcWorker newWorker() {
		return new XmlRpcClientWorker(this);
	}
}
