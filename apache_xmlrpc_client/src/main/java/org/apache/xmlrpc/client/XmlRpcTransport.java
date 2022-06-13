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


/** <p>Interface from XML-RPC to an underlying transport, most likely based on HTTP.</p>
 * Replaces the interface <code>org.apache.xmlrpc.client</code> from Apache XML-RPC
 * 2.0, which has actually been a stream based transport.
 * @since 3.0
 */
interface XmlRpcTransport {
	/**  Send an XML-RPC message. This method is called to send a message to the
	 * other party.
	 * @param pRequest The request being performed.
	 * @return Result object, if invoking the remote method was successfull.
	 * @throws XmlRpcException Performing the request failed.
	 */
	public Object sendRequest(XmlRpcRequest pRequest) throws XmlRpcException;
}
