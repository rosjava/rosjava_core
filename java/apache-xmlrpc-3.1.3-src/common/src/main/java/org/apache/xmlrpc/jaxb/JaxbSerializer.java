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
package org.apache.xmlrpc.jaxb;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.xmlrpc.serializer.ExtSerializer;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;


/** A serializer for JAXB objects.
 */
public class JaxbSerializer extends ExtSerializer {
	private final JAXBContext context;

	/** The tag name for serializing JAXB objects.
	 */
	public static final String JAXB_TAG = "jaxb";

	/** Creates a new instance with the given context.
	 * @param pContext The context being used for creating marshallers.
	 */
	public JaxbSerializer(JAXBContext pContext) {
		context = pContext;
	}

	protected String getTagName() { return JAXB_TAG; }

	protected void serialize(final ContentHandler pHandler, Object pObject) throws SAXException {
		/* We must ensure, that startDocument() and endDocument() events
		 * are suppressed. So we replace the content handler with the following:
		 */
		ContentHandler h = new ContentHandler() {
			public void endDocument() throws SAXException {}
			public void startDocument() throws SAXException {}
			public void characters(char[] pChars, int pOffset, int pLength) throws SAXException {
				pHandler.characters(pChars, pOffset, pLength);
			}
			public void ignorableWhitespace(char[] pChars, int pOffset, int pLength) throws SAXException {
				pHandler.ignorableWhitespace(pChars, pOffset, pLength);
			}
			public void endPrefixMapping(String pPrefix) throws SAXException {
				pHandler.endPrefixMapping(pPrefix);
			}
			public void skippedEntity(String pName) throws SAXException {
				pHandler.endPrefixMapping(pName);
			}
			public void setDocumentLocator(Locator pLocator) {
				pHandler.setDocumentLocator(pLocator);
			}
			public void processingInstruction(String pTarget, String pData) throws SAXException {
				pHandler.processingInstruction(pTarget, pData);
			}
			public void startPrefixMapping(String pPrefix, String pURI) throws SAXException {
				pHandler.startPrefixMapping(pPrefix, pURI);
			}
			public void endElement(String pURI, String pLocalName, String pQName) throws SAXException {
				pHandler.endElement(pURI, pLocalName, pQName);
			}
			public void startElement(String pURI, String pLocalName, String pQName, Attributes pAttrs) throws SAXException {
				pHandler.startElement(pURI, pLocalName, pQName, pAttrs);
			}
		};
		try {
			context.createMarshaller().marshal(pObject, h);
		} catch (JAXBException e) {
			Throwable t = e.getLinkedException();
			if (t != null  &&  t instanceof SAXException) {
				throw (SAXException) t;
			} else {
				throw new SAXException(e);
			}
		}
	}
}
