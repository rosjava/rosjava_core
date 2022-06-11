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

import org.apache.ws.commons.util.Base64;
import org.apache.ws.commons.util.Base64.Encoder;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import static org.apache.xmlrpc.serializer.XmlRpcConstants.*;



/** A {@link TypeSerializer} for byte arrays.
 */
public class ByteArraySerializer extends TypeSerializerImpl {
	public void write(final ContentHandler pHandler, Object pObject) throws SAXException {
		pHandler.startElement(EMPTY_STRING, VALUE, VALUE, ZERO_ATTRIBUTES);
		pHandler.startElement(EMPTY_STRING, BASE_64, BASE_64, ZERO_ATTRIBUTES);
		byte[] buffer = (byte[]) pObject;
		if (buffer.length > 0) {
			char[] charBuffer = new char[buffer.length >= 1024 ? 1024 : ((buffer.length+3)/4)*4];
			Encoder encoder = new Base64.SAXEncoder(charBuffer, 0, null, pHandler);
			try {
				encoder.write(buffer, 0, buffer.length);
				encoder.flush();
			} catch (Base64.SAXIOException e) {
				throw e.getSAXException();
			} catch (IOException e) {
				throw new SAXException(e);
			}
		}
		pHandler.endElement(EMPTY_STRING, BASE_64, BASE_64);
		pHandler.endElement(EMPTY_STRING, VALUE, VALUE);
	}
}