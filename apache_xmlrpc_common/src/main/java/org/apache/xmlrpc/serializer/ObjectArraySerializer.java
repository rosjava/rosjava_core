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

import org.apache.xmlrpc.common.TypeFactory;
import org.apache.xmlrpc.common.XmlRpcStreamConfig;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


/** A {@link TypeSerializer} for object arrays.
 */
public class ObjectArraySerializer extends TypeSerializerImpl {

	private final XmlRpcStreamConfig config;
	private final TypeFactory typeFactory;

	/** Creates a new instance.
	 * @param pTypeFactory The factory being used for creating serializers.
	 * @param pConfig The configuration being used for creating serializers.
	 */
	public ObjectArraySerializer(TypeFactory pTypeFactory, XmlRpcStreamConfig pConfig) {
		typeFactory = pTypeFactory;
		config = pConfig;
	}
	protected void writeObject(ContentHandler pHandler, Object pObject) throws SAXException {
		TypeSerializer ts = typeFactory.getSerializer(config, pObject);
		if (ts == null) {
			throw new SAXException("Unsupported Java type: " + pObject.getClass().getName());
		}
		ts.write(pHandler, pObject);
	}
	protected void writeData(ContentHandler pHandler, Object pObject) throws SAXException {
		Object[] data = (Object[]) pObject;
		for (int i = 0;  i < data.length;  i++) {
			writeObject(pHandler, data[i]);
		}
	}
	public void write(final ContentHandler pHandler, Object pObject) throws SAXException {
		pHandler.startElement(XmlRpcConstants.EMPTY_STRING, XmlRpcConstants.VALUE, XmlRpcConstants.VALUE, ZERO_ATTRIBUTES);
		pHandler.startElement(XmlRpcConstants.EMPTY_STRING, XmlRpcConstants.ARRAY, XmlRpcConstants.ARRAY, ZERO_ATTRIBUTES);
		pHandler.startElement(XmlRpcConstants.EMPTY_STRING, XmlRpcConstants.DATA, XmlRpcConstants.DATA, ZERO_ATTRIBUTES);
		writeData(pHandler, pObject);
		pHandler.endElement(XmlRpcConstants.EMPTY_STRING, XmlRpcConstants.DATA, XmlRpcConstants.DATA);
		pHandler.endElement(XmlRpcConstants.EMPTY_STRING, XmlRpcConstants.ARRAY, XmlRpcConstants.ARRAY);
		pHandler.endElement(XmlRpcConstants.EMPTY_STRING, XmlRpcConstants.VALUE, XmlRpcConstants.VALUE);
	}
}