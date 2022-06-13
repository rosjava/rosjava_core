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

import org.apache.xmlrpc.serializer.XmlRpcConstants;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/** Base class for parsing external XML representations, like DOM,
 * or JAXB.
 */
public abstract class ExtParser implements TypeParser {
	private Locator locator;
	private ContentHandler handler;
	private int level = 0;
	private final List prefixes = new ArrayList();

	/** Returns a content handler for parsing the actual
	 * contents.
	 * @return A SAX handler for parsing the XML inside
	 * the outer ex:foo element.
	 * @throws SAXException Creating the handler failed.
	 */
	protected abstract ContentHandler getExtHandler() throws SAXException;

	/** Returns the outer node name.
	 */
	protected abstract String getTagName();

	public void endDocument() throws SAXException {
	}

	public void startDocument() throws SAXException {
	}

	public void characters(char[] pChars, int pOffset, int pLength)
			throws SAXException {
		if (handler == null) {
			if (!TypeParserImpl.isEmpty(pChars, pOffset, pLength)) {
				throw new SAXParseException("Unexpected non-whitespace content: " + new String(pChars, pOffset, pLength),
											locator);
			}
		} else {
			handler.characters(pChars, pOffset, pLength);
		}
	}

	public void ignorableWhitespace(char[] pChars, int pOffset, int pLength)
			throws SAXException {
		if (handler != null) {
			ignorableWhitespace(pChars, pOffset, pLength);
		}
	}

	public void endPrefixMapping(String pPrefix) throws SAXException {
		if (handler != null) {
			handler.endPrefixMapping(pPrefix);
		}
	}

	public void skippedEntity(String pName) throws SAXException {
		if (handler == null) {
			throw new SAXParseException("Don't know how to handle entity " + pName,
										locator);
		} else {
			handler.skippedEntity(pName);
		}
	}

	public void setDocumentLocator(Locator pLocator) {
		locator = pLocator;
		if (handler != null) {
			handler.setDocumentLocator(pLocator);
		}
	}

	public void processingInstruction(String pTarget, String pData)
			throws SAXException {
		if (handler != null) {
			handler.processingInstruction(pTarget, pData);
		}
	}

	public void startPrefixMapping(String pPrefix, String pURI)
			throws SAXException {
		if (handler == null) {
			prefixes.add(pPrefix);
			prefixes.add(pURI);
		} else {
			handler.startPrefixMapping(pPrefix, pURI);
		}
	}

	public void startElement(String pURI, String pLocalName,
							 String pQName, Attributes pAttrs) throws SAXException {
		switch (level++) {
			case 0:
				final String tag = getTagName();
				if (!XmlRpcConstants.EXTENSIONS_URI.equals(pURI)  ||
					!tag.equals(pLocalName)) {
					throw new SAXParseException("Expected " +
												new QName(XmlRpcConstants.EXTENSIONS_URI, tag) +
												", got " +
												new QName(pURI, pLocalName),
												locator);
				}
				handler = getExtHandler();
				handler.startDocument();
				for (int i = 0;  i < prefixes.size();  i += 2) {
					handler.startPrefixMapping((String) prefixes.get(i),
											   (String) prefixes.get(i+1));
				}
				break;
			default:
				handler.startElement(pURI, pLocalName, pQName, pAttrs);
				break;
		}
	}

	public void endElement(String pURI, String pLocalName, String pQName)
			throws SAXException {
		switch (--level) {
			case 0:
				for (int i = 0;  i < prefixes.size();  i += 2) {
					handler.endPrefixMapping((String) prefixes.get(i));
				}
				handler.endDocument();
				handler = null;
				break;
			default:
				handler.endElement(pURI, pLocalName, pQName);
				break;
		}
	}
}
