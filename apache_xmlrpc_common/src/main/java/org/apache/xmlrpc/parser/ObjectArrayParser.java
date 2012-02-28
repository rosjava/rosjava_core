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
package org.apache.xmlrpc.parser;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.ws.commons.util.NamespaceContextImpl;
import org.apache.xmlrpc.common.TypeFactory;
import org.apache.xmlrpc.common.XmlRpcStreamConfig;
import org.apache.xmlrpc.serializer.ObjectArraySerializer;
import org.apache.xmlrpc.serializer.TypeSerializerImpl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/** Parser for an array of objects, as created by
 * {@link org.apache.xmlrpc.serializer.ObjectArraySerializer}.
 */
public class ObjectArrayParser extends RecursiveTypeParserImpl {
	private int level = 0;
	private List list;
	
	/** Creates a new instance.
	 * @param pContext The namespace context.
	 * @param pConfig The request or response configuration.
	 * @param pFactory The type factory.
	 */
	public ObjectArrayParser(XmlRpcStreamConfig pConfig,
							 NamespaceContextImpl pContext,
							 TypeFactory pFactory) {
		super(pConfig, pContext, pFactory);
	}

	public void startDocument() throws SAXException {
		level = 0;
		list = new ArrayList();
		super.startDocument();
	}

	protected void addResult(Object pValue) {
		list.add(pValue);
	}

	public void endElement(String pURI, String pLocalName, String pQName) throws SAXException {
		switch (--level) {
			case 0:
				setResult(list.toArray());
				break;
			case 1:
				break;
			case 2:
				endValueTag();
				break;
			default:
				super.endElement(pURI, pLocalName, pQName);
		}
	}

	public void startElement(String pURI, String pLocalName, String pQName, Attributes pAttrs) throws SAXException {
		switch (level++) {
			case 0:
				if (!"".equals(pURI)  ||  !ObjectArraySerializer.ARRAY_TAG.equals(pLocalName)) {
					throw new SAXParseException("Expected array element, got "
												+ new QName(pURI, pLocalName),
												getDocumentLocator());
				}
				break;
			case 1:
				if (!"".equals(pURI)  ||  !ObjectArraySerializer.DATA_TAG.equals(pLocalName)) {
					throw new SAXParseException("Expected data element, got "
												+ new QName(pURI, pLocalName),
												getDocumentLocator());
				}
				break;
			case 2:
				if (!"".equals(pURI)  ||  !TypeSerializerImpl.VALUE_TAG.equals(pLocalName)) {
					throw new SAXParseException("Expected data element, got "
												+ new QName(pURI, pLocalName),
												getDocumentLocator());
				}
				startValueTag();
				break;
			default:
				super.startElement(pURI, pLocalName, pQName, pAttrs);
				break;
		}
	}

}
