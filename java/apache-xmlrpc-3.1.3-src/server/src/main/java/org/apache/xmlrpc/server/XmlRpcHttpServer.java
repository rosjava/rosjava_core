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
package org.apache.xmlrpc.server;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.xmlrpc.common.ServerStreamConnection;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;



/** Abstract extension of {@link XmlRpcStreamServer} for deriving
 * HTTP servers.
 */
public abstract class XmlRpcHttpServer extends XmlRpcStreamServer {
	protected abstract void setResponseHeader(ServerStreamConnection pConnection, String pHeader, String pValue);

	protected OutputStream getOutputStream(ServerStreamConnection pConnection, XmlRpcStreamRequestConfig pConfig, OutputStream pStream) throws IOException {
		if (pConfig.isEnabledForExtensions()  &&  pConfig.isGzipRequesting()) {
			setResponseHeader(pConnection, "Content-Encoding", "gzip");
		}
		return super.getOutputStream(pConnection, pConfig, pStream);
	}
}
