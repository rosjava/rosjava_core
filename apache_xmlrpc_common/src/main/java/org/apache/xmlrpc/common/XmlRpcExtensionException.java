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


/** This exception is thrown, if an attempt to use extensions
 * is made, but extensions aren't explicitly enabled.
 */
public final class XmlRpcExtensionException extends XmlRpcException {
	private static final long serialVersionUID = 3617014169594311221L;

	/** Creates a new instance with the given error message.
	 * @param pMessage The error message.
	 */
	public XmlRpcExtensionException(String pMessage) {
		super(0, pMessage);
	}
}
