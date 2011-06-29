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


/** Interface of an object, which may be used
 * to create instances of {@link XmlRpcRequestProcessor}.
 */
public interface XmlRpcRequestProcessorFactory {
	/** Returns the {@link XmlRpcRequestProcessor} being invoked.
	 * @return Server object being invoked. This will typically
	 * be a singleton instance, but could as well create a new
	 * instance with any call.
	 */
	XmlRpcRequestProcessor getXmlRpcServer();
}
