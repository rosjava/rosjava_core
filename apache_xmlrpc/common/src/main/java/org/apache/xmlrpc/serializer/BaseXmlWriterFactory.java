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

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.apache.ws.commons.serialize.XMLWriter;
import org.apache.ws.commons.serialize.XMLWriterImpl;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.common.XmlRpcStreamConfig;
import org.xml.sax.ContentHandler;


/** The default instance of {@link XmlWriterFactory} creates
 * instances of {@link org.apache.ws.commons.serialize.XMLWriterImpl}.
 * This works for any Java version since 1.2
 */
public class BaseXmlWriterFactory implements XmlWriterFactory {
	protected XMLWriter newXmlWriter() {
		return new XMLWriterImpl();
	}

	public ContentHandler getXmlWriter(XmlRpcStreamConfig pConfig, OutputStream pStream)
			throws XmlRpcException {
		XMLWriter xw = newXmlWriter();
		xw.setDeclarating(true);
		String enc = pConfig.getEncoding();
		if (enc == null) {
			enc = XmlRpcStreamConfig.UTF8_ENCODING;
		}
		xw.setEncoding(enc);
		xw.setIndenting(false);
		xw.setFlushing(true);
		try {
			xw.setWriter(new BufferedWriter(new OutputStreamWriter(pStream, enc)));
		} catch (UnsupportedEncodingException e) {
			throw new XmlRpcException("Unsupported encoding: " + enc, e);
		}
		return xw;
	}
}
