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
package org.apache.xmlrpc.metadata;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcHandler;
import org.apache.xmlrpc.server.AbstractReflectiveHandlerMapping;


/** A metadata handler is able to provide metadata about
 * itself, as specified
 * <a href="http://scripts.incutio.com/xmlrpc/introspection.html">
 * here</a>.<br>
 * 
 * @see <a href="http://scripts.incutio.com/xmlrpc/introspection.html">
 * Specification of XML-RPC introspection</a>
 */
public interface XmlRpcMetaDataHandler extends XmlRpcHandler {
	/** <p>This method may be used to implement
	 * {@link XmlRpcListableHandlerMapping#getMethodSignature(String)}.
	 * Typically, the handler mapping will pick up the
	 * matching handler, invoke its method
	 * {@link #getSignatures()}, and return the result.</p>
	 * <p>Method handlers, which are created by the
	 * {@link AbstractReflectiveHandlerMapping}, will typically
	 * return a single signature only.</p>
	 * @return An array of arrays. Any element in the outer
	 * array is a signature. The elements in the inner array
	 * are being concatenated with commas. The inner arrays
	 * first element is the return type, followed by the
	 * parameter types.
	 */
    String[][] getSignatures() throws XmlRpcException;

	/** <p>This method may be used to implement
	 * {@link XmlRpcListableHandlerMapping#getMethodHelp(String)}.
	 * Typically, the handler mapping will pick up the
	 * matching handler, invoke its method
	 * {@link #getMethodHelp()}, and return the result.</p>
     */
    String getMethodHelp() throws XmlRpcException;
}
