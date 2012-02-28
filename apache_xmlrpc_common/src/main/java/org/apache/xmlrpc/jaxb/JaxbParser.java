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
import javax.xml.bind.UnmarshallerHandler;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.parser.ExtParser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


/** A parser for JAXB objects.
 */
public class JaxbParser extends ExtParser {
	private final JAXBContext context;
	private UnmarshallerHandler handler;

	/** Creates a new instance with the given context.
	 * @param pContext The context being used for creating unmarshallers.
	 */
	public JaxbParser(JAXBContext pContext) {
		context = pContext;
	}

	protected ContentHandler getExtHandler() throws SAXException {
		try {
			handler = context.createUnmarshaller().getUnmarshallerHandler();
		} catch (JAXBException e) {
			throw new SAXException(e);
		}
		return handler;
	}

	protected String getTagName() { return JaxbSerializer.JAXB_TAG; }

	public Object getResult() throws XmlRpcException {
		try {
			return handler.getResult();
		} catch (JAXBException e) {
			throw new XmlRpcException("Failed to create result object: " + e.getMessage(), e);
		}
	}
}
