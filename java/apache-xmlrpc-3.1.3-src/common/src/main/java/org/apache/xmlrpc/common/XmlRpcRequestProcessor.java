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

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;


/** Interface of an object, which is able to process
 * XML-RPC requests.
 */
public interface XmlRpcRequestProcessor {
	/** Processes the given request and returns a
	 * result object.
	 * @throws XmlRpcException Processing the request failed.
	 */
	Object execute(XmlRpcRequest pRequest) throws XmlRpcException;

	/** Returns the request processors {@link TypeConverterFactory}.
	 */
    TypeConverterFactory getTypeConverterFactory();
}
