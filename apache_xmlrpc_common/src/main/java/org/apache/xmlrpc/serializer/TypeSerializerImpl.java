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

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


/** Abstract base implementation of a type serializer.
 */
public abstract class TypeSerializerImpl implements TypeSerializer {
	protected static final Attributes ZERO_ATTRIBUTES = new AttributesImpl();
	/** Tag name of a value element.
	 */
	

	protected void write(ContentHandler pHandler, String pTagName, String pValue) throws SAXException {
		write(pHandler, pTagName, pValue.toCharArray());
	}

	protected void write(ContentHandler pHandler, String pTagName, char[] pValue) throws SAXException {
		pHandler.startElement(XmlRpcConstants.EMPTY_STRING, XmlRpcConstants.VALUE, XmlRpcConstants.VALUE, ZERO_ATTRIBUTES);
		if (pTagName != null) {
			pHandler.startElement(XmlRpcConstants.EMPTY_STRING, pTagName, pTagName, ZERO_ATTRIBUTES);
		}
		pHandler.characters(pValue, 0, pValue.length);
		if (pTagName != null) {
			pHandler.endElement(XmlRpcConstants.EMPTY_STRING, pTagName, pTagName);
		}
		pHandler.endElement(XmlRpcConstants.EMPTY_STRING, XmlRpcConstants.VALUE, XmlRpcConstants.VALUE);
	}

	protected void write(ContentHandler pHandler, String pLocalName, String pQName,
						 String pValue) throws SAXException {
		pHandler.startElement(XmlRpcConstants.EMPTY_STRING, XmlRpcConstants.VALUE, XmlRpcConstants.VALUE, ZERO_ATTRIBUTES);
		pHandler.startElement(XmlRpcConstants.EXTENSIONS_URI, pLocalName, pQName, ZERO_ATTRIBUTES);
		char[] value = pValue.toCharArray();
		pHandler.characters(value, 0, value.length);
		pHandler.endElement(XmlRpcConstants.EXTENSIONS_URI, pLocalName, pQName);
		pHandler.endElement(XmlRpcConstants.EMPTY_STRING, XmlRpcConstants.VALUE, XmlRpcConstants.VALUE);
	}
}