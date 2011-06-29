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
package org.apache.xmlrpc;

import java.util.TimeZone;


/** A common base interface for
 * {@link org.apache.xmlrpc.client.XmlRpcClientConfig}, and
 * {@link org.apache.xmlrpc.server.XmlRpcServerConfig}.
 */
public interface XmlRpcConfig {
	/** Returns, whether support for extensions are enabled.
	 * By default, extensions are disabled and your client is
	 * interoperable with other XML-RPC implementations.
	 * Interoperable XML-RPC implementations are those, which
	 * are compliant to the
	 * <a href="http://www.xmlrpc.org/spec">XML-RPC Specification</a>.
	 * @return Whether extensions are enabled or not.
	 */
	boolean isEnabledForExtensions();

	/** Returns the timezone, which is used to interpret date/time
     * values. Defaults to {@link TimeZone#getDefault()}.
	 */
    TimeZone getTimeZone();
}
