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

/** <p>This is thrown by many of the client classes if an error occured processing
 * and XML-RPC request or response due to client side processing. This exception
 * will wrap a cause exception in the JDK 1.4 style.</p>
 * <p>This class replaces the class <code>org.apache.xmlrpc.XmlRpcClientException</code>
 * from Apache XML-RPC 2.0</p>
 * @since 3.0
 */
public class XmlRpcClientException extends XmlRpcException {
	private static final long serialVersionUID = 3545798797134608691L;

	/**
     * Create an XmlRpcClientException with the given message and
     * underlying cause exception.
     *
     * @param pMessage the message for this exception.
     * @param pCause the cause of the exception.
     */
    public XmlRpcClientException(String pMessage, Throwable pCause) {
        super(0, pMessage, pCause);
    }
}
