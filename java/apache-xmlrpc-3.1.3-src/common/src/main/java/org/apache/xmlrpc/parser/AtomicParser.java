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

import javax.xml.namespace.QName;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/** Abstract base implementation of {@link org.apache.xmlrpc.parser.TypeParser}
 * for parsing an atomic value.
 */
public abstract class AtomicParser extends TypeParserImpl {
	private int level;
	protected StringBuffer sb;

	/** Creates a new instance.
	 */
	protected AtomicParser() {
	}

	protected abstract void setResult(String pResult) throws SAXException;

	public void startDocument() throws SAXException {
		level = 0;
	}

	public void characters(char[] pChars, int pStart, int pLength) throws SAXException {
        if (sb == null) {
			if (!isEmpty(pChars, pStart, pLength)) {
				throw new SAXParseException("Unexpected non-whitespace characters",
											getDocumentLocator());
			}
		} else {
			sb.append(pChars, pStart, pLength);
		}
	}

	public void endElement(String pURI, String pLocalName, String pQName) throws SAXException {
		if (--level == 0) {
			setResult(sb.toString());
		} else {
			throw new SAXParseException("Unexpected end tag in atomic element: "
										+ new QName(pURI, pLocalName),
										getDocumentLocator());
		}
	}

	public void startElement(String pURI, String pLocalName, String pQName, Attributes pAttrs) throws SAXException {
		if (level++ == 0) {
			sb = new StringBuffer();
		} else {
			throw new SAXParseException("Unexpected start tag in atomic element: "
										+ new QName(pURI, pLocalName),
										getDocumentLocator());
		}
	}
}
