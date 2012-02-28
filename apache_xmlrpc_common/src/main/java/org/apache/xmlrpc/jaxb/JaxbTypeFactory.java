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

import javax.xml.bind.Element;
import javax.xml.bind.JAXBContext;

import org.apache.ws.commons.util.NamespaceContextImpl;
import org.apache.xmlrpc.common.TypeFactoryImpl;
import org.apache.xmlrpc.common.XmlRpcController;
import org.apache.xmlrpc.common.XmlRpcStreamConfig;
import org.apache.xmlrpc.parser.TypeParser;
import org.apache.xmlrpc.serializer.TypeSerializer;
import org.apache.xmlrpc.serializer.XmlRpcWriter;
import org.xml.sax.SAXException;


/** A type factory with support for JAXB objects.
 */
public class JaxbTypeFactory extends TypeFactoryImpl {
	private final JAXBContext context;
	private final JaxbSerializer serializer; 

	/** Creates a new instance with the given controller and
	 * JAXB context.
	 * @param pController The controller, which will invoke the factory.
	 * @param pContext The context being used to create marshallers
	 * and unmarshallers.
	 */
	public JaxbTypeFactory(XmlRpcController pController, JAXBContext pContext) {
		super(pController);
		context = pContext;
		serializer = new JaxbSerializer(context);
	}

	public TypeParser getParser(XmlRpcStreamConfig pConfig, NamespaceContextImpl pContext, String pURI, String pLocalName) {
		TypeParser tp = super.getParser(pConfig, pContext, pURI, pLocalName);
		if (tp == null) {
			if (XmlRpcWriter.EXTENSIONS_URI.equals(pURI)  &&  JaxbSerializer.JAXB_TAG.equals(pLocalName)) {
				return new JaxbParser(context);
			}
		}
		return tp;
	}

	public TypeSerializer getSerializer(XmlRpcStreamConfig pConfig, Object pObject) throws SAXException {
		TypeSerializer ts = super.getSerializer(pConfig, pObject);
		if (ts == null) {
			if (pObject instanceof Element) {
				return serializer;
			}
		}
		return ts;
	}
}
