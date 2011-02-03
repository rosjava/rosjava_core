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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.LocalStreamConnection;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.common.XmlRpcStreamRequestProcessor;
import org.xml.sax.SAXException;


/** Another local transport for debugging and testing. This one is
 * similar to the {@link org.apache.xmlrpc.client.XmlRpcLocalTransport},
 * except that it adds request serialization. In other words, it is
 * particularly well suited for development and testing of XML serialization
 * and parsing.
 */
public class XmlRpcLocalStreamTransport extends XmlRpcStreamTransport {
	private final XmlRpcStreamRequestProcessor localServer;
	private LocalStreamConnection conn;
    private XmlRpcRequest request;
	
	/** Creates a new instance.
	 * @param pClient The client, which is controlling the transport.
	 * @param pServer An instance of {@link XmlRpcStreamRequestProcessor}.
	 */
	public XmlRpcLocalStreamTransport(XmlRpcClient pClient,
			XmlRpcStreamRequestProcessor pServer) {
		super(pClient);
		localServer = pServer;
	}

	protected boolean isResponseGzipCompressed(XmlRpcStreamRequestConfig pConfig) {
		return pConfig.isGzipRequesting();
	}

	protected void close() throws XmlRpcClientException {
	}

	protected InputStream getInputStream() throws XmlRpcException {
		localServer.execute(conn.getConfig(), conn.getServerStreamConnection());
		return new ByteArrayInputStream(conn.getResponse().toByteArray());
	}

	protected ReqWriter newReqWriter(XmlRpcRequest pRequest)
            throws XmlRpcException, IOException, SAXException {
	    request = pRequest;
        return super.newReqWriter(pRequest);
    }

    protected void writeRequest(ReqWriter pWriter)
            throws XmlRpcException, IOException, SAXException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();		
		pWriter.write(baos);
		XmlRpcStreamRequestConfig config = (XmlRpcStreamRequestConfig) request.getConfig();
		conn = new LocalStreamConnection(config, new ByteArrayInputStream(baos.toByteArray()));
	}
}
