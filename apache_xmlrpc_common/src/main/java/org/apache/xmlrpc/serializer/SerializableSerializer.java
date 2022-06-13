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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.ws.commons.util.Base64;
import org.apache.ws.commons.util.Base64.Encoder;
import org.apache.ws.commons.util.Base64.EncoderOutputStream;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


/** A {@link org.apache.xmlrpc.serializer.TypeSerializer} for
 * instances of {@link java.io.Serializable}.
 */
public class SerializableSerializer extends TypeSerializerImpl {

	public void write(final ContentHandler pHandler, Object pObject) throws SAXException {
		pHandler.startElement(XmlRpcConstants.EMPTY_STRING, XmlRpcConstants.VALUE, XmlRpcConstants.VALUE, ZERO_ATTRIBUTES);
		pHandler.startElement(XmlRpcConstants.EMPTY_STRING, XmlRpcConstants.SERIALIZABLE, XmlRpcConstants.EX_SERIALIZABLE, ZERO_ATTRIBUTES);
		char[] buffer = new char[1024];
		Encoder encoder = new Base64.SAXEncoder(buffer, 0, null, pHandler);
		try {
			OutputStream ostream = new EncoderOutputStream(encoder);
			ObjectOutputStream oos = new ObjectOutputStream(ostream);
			oos.writeObject(pObject);
			oos.close();
		} catch (Base64.SAXIOException e) {
			throw e.getSAXException();
		} catch (IOException e) {
			throw new SAXException(e);
		}
		pHandler.endElement(XmlRpcConstants.EMPTY_STRING, XmlRpcConstants.SERIALIZABLE, XmlRpcConstants.EX_SERIALIZABLE);
		pHandler.endElement(XmlRpcConstants.EMPTY_STRING, XmlRpcConstants.VALUE, XmlRpcConstants.VALUE);
	}
}
