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


/** <p>A transport factory being used for local XML-RPC calls. Local XML-RPC
 * calls are mainly useful for development and unit testing: Both client
 * and server are runing within the same JVM and communication is implemented
 * in simple method invokcations.</p>
 * <p>This class is thread safe and the returned instance of
 * {@link org.apache.xmlrpc.client.XmlRpcTransport} will always return the
 * same object, an instance of {@link XmlRpcLocalTransport}</p>
 */
public class XmlRpcLocalTransportFactory extends XmlRpcTransportFactoryImpl {
	/** Creates a new instance, operated by the given client.
	 * @param pClient The client, which will invoke the factory.
	 */
	public XmlRpcLocalTransportFactory(XmlRpcClient pClient) {
		super(pClient);
	}

	private final XmlRpcTransport LOCAL_TRANSPORT = new XmlRpcLocalTransport(getClient());

	public XmlRpcTransport getTransport() { return LOCAL_TRANSPORT; }
}
