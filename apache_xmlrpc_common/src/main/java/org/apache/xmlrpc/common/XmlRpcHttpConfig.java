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


/** Interface of a configuration for HTTP requests.
 */
public interface XmlRpcHttpConfig extends XmlRpcStreamConfig {
	/** Returns the encoding being used to convert the String "username:password"
	 * into bytes.
	 * @return Encoding being used for basic HTTP authentication credentials,
	 * or null, if the default encoding
	 * ({@link org.apache.xmlrpc.common.XmlRpcStreamRequestConfig#UTF8_ENCODING})
	 * is being used.
	 */
	String getBasicEncoding();
	/** Returns, whether a "Content-Length" header may be
	 * omitted. The XML-RPC specification demands, that such
	 * a header be present.
	 * @return True, if the content length may be omitted.
	 */
	boolean isContentLengthOptional();
}
