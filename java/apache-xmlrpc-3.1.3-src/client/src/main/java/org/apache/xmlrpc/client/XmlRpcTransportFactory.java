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


/** Interface of an object creating instances of
 * {@link org.apache.xmlrpc.client.XmlRpcTransport}. The implementation
 * is typically based on singletons.
 */
public interface XmlRpcTransportFactory {
    /** Returns an instance of {@link XmlRpcTransport}. This may
	 * be a singleton, but the caller should not depend on that:
	 * A new instance may as well be created for any request.
	 * @return The configured transport.
	 */
	public XmlRpcTransport getTransport();
}
