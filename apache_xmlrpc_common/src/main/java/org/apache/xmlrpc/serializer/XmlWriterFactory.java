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
package org.apache.xmlrpc.serializer;

import java.io.OutputStream;

import org.apache.ws.commons.serialize.XMLWriter;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.common.XmlRpcStreamConfig;
import org.xml.sax.ContentHandler;


/** This factory is responsible for creating instances of
 * {@link org.apache.ws.commons.serialize.XMLWriter}.
 */
public interface XmlWriterFactory {
	/** Creates a new instance of {@link ContentHandler},
	 * writing to the given {@link java.io.OutputStream}.
	 * @return A SAX handler, typically an instance of
	 * {@link XMLWriter}.
	 * @param pStream The destination stream.
	 * @param pConfig The request or response configuration.
	 * @throws XmlRpcException Creating the handler failed.
	 */
	public ContentHandler getXmlWriter(XmlRpcStreamConfig pConfig,
									   OutputStream pStream) throws XmlRpcException;
}
