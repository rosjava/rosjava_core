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

import org.apache.xmlrpc.XmlRpcRequestConfig;


/** Interface of a client configuration for a transport, which
 * is implemented by writing to a stream.
 */
public interface XmlRpcStreamRequestConfig extends XmlRpcStreamConfig, XmlRpcRequestConfig {
	/** Returns, whether the request stream is being compressed. Note,
	 * that the response stream may still be uncompressed.
	 * @return Whether to use Gzip compression or not. Defaults to false.
	 * @see #isGzipRequesting()
	 */
	boolean isGzipCompressing();
	/** Returns, whether compression is requested for the response stream.
	 * Note, that the request is stull uncompressed, unless
	 * {@link #isGzipCompressing()} is activated. Also note, that the
	 * server may still decide to send uncompressed data.
	 * @return Whether to use Gzip compression or not. Defaults to false.
	 * @see #isGzipCompressing()
	 */
	boolean isGzipRequesting();
	/** Returns, whether the response should contain a "faultCause" element
     * in case of errors. The "faultCause" is an exception, which the
     * server has trapped and written into a byte stream as a serializable
     * object.
	 */
	boolean isEnabledForExceptions();
}
