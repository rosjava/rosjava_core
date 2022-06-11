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

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.XmlRpcRequestConfig;
import org.apache.xmlrpc.common.TypeFactory;
import org.apache.xmlrpc.common.XmlRpcStreamConfig;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import static org.apache.xmlrpc.serializer.XmlRpcConstants.*;

/** This class is responsible for writing an XmlRpc request or an
 * XmlRpc response to an output stream.
 */
public class XmlRpcWriter {
	private static final Attributes ZERO_ATTRIBUTES = new AttributesImpl();
	private final XmlRpcStreamConfig config;
	private final TypeFactory typeFactory;
	private final ContentHandler handler;

	/** Creates a new instance.
	 * @param pConfig The clients configuration.
	 * @param pHandler The target SAX handler.
	 * @param pTypeFactory The type factory being used to create serializers.
	 */
	public XmlRpcWriter(XmlRpcStreamConfig pConfig, ContentHandler pHandler,
					    TypeFactory pTypeFactory) {
		config = pConfig;
		handler = pHandler;
		typeFactory = pTypeFactory;
	}

	/** Writes a clients request to the output stream.
	 * @param pRequest The request being written.
	 * @throws SAXException Writing the request failed.
	 */
	public void write(XmlRpcRequest pRequest) throws SAXException {
		handler.startDocument();
		boolean extensions = pRequest.getConfig().isEnabledForExtensions();
		if (extensions) {
			handler.startPrefixMapping(EX, EXTENSIONS_URI);
		}
		handler.startElement(EMPTY_STRING, METHOD_CALL, METHOD_CALL, ZERO_ATTRIBUTES);
		handler.startElement(EMPTY_STRING, METHOD_NAME, METHOD_NAME, ZERO_ATTRIBUTES);
		String s = pRequest.getMethodName();
		handler.characters(s.toCharArray(), 0, s.length());
		handler.endElement(EMPTY_STRING, METHOD_NAME, METHOD_NAME);
		handler.startElement(EMPTY_STRING, PARAMS, PARAMS, ZERO_ATTRIBUTES);
		int num = pRequest.getParameterCount();
		for (int i = 0;  i < num;  i++) {
			handler.startElement(EMPTY_STRING, PARAM, PARAM, ZERO_ATTRIBUTES);
			writeValue(pRequest.getParameter(i));
			handler.endElement(EMPTY_STRING, PARAM, PARAM);
		}
		handler.endElement(EMPTY_STRING, PARAMS, PARAMS);
        handler.endElement(EMPTY_STRING, METHOD_CALL, METHOD_CALL);
		if (extensions) {
			handler.endPrefixMapping(EX);
		}
		handler.endDocument();
	}

	/** Writes a servers response to the output stream.
	 * @param pConfig The request configuration.
	 * @param pResult The result object.
	 * @throws SAXException Writing the response failed.
	 */
	public void write(XmlRpcRequestConfig pConfig, Object pResult) throws SAXException {
		handler.startDocument();
		boolean extensions = pConfig.isEnabledForExtensions();
		if (extensions) {
			handler.startPrefixMapping(EX, EXTENSIONS_URI);
		}
		handler.startElement(EMPTY_STRING, METHOD_RESPONSE, METHOD_RESPONSE, ZERO_ATTRIBUTES);
		handler.startElement(EMPTY_STRING, PARAMS, PARAMS, ZERO_ATTRIBUTES);
		handler.startElement(EMPTY_STRING, PARAM, PARAM, ZERO_ATTRIBUTES);
		writeValue(pResult);
		handler.endElement(EMPTY_STRING, PARAM, PARAM);
		handler.endElement(EMPTY_STRING, PARAMS, PARAMS);
		handler.endElement(EMPTY_STRING, METHOD_RESPONSE, METHOD_RESPONSE);
		if (extensions) {
			handler.endPrefixMapping(EX);
		}
		handler.endDocument();
	}

    /** Writes a servers error message to the output stream.
     * @param pConfig The request configuration.
     * @param pCode The error code
     * @param pMessage The error message
     * @throws SAXException Writing the error message failed.
     */
    public void write(XmlRpcRequestConfig pConfig, int pCode, String pMessage) throws SAXException {
        write(pConfig, pCode, pMessage, null);
    }

        /** Writes a servers error message to the output stream.
	 * @param pConfig The request configuration.
	 * @param pCode The error code
	 * @param pMessage The error message
     * @param pThrowable An exception, which is being sent to the client
	 * @throws SAXException Writing the error message failed.
	 */
	public void write(XmlRpcRequestConfig pConfig, int pCode, String pMessage,
            Throwable pThrowable) throws SAXException {
		this.handler.startDocument();
		final boolean extensions = pConfig.isEnabledForExtensions();
		if (extensions) {
			this.handler.startPrefixMapping(EX, EXTENSIONS_URI);
		}
		this.handler.startElement(EMPTY_STRING, METHOD_RESPONSE, METHOD_RESPONSE, ZERO_ATTRIBUTES);
		this.handler.startElement(EMPTY_STRING, FAULT, FAULT, ZERO_ATTRIBUTES);
		final Map map = new HashMap();
        map.put(FAULT_CODE, Integer.valueOf(pCode));
        map.put(FAULT_STRING, pMessage == null ? EMPTY_STRING : pMessage);
        if (pThrowable != null  &&  extensions  &&  (pConfig instanceof XmlRpcStreamRequestConfig)  &&
                ((XmlRpcStreamRequestConfig) pConfig).isEnabledForExceptions()) {
            try {
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(pThrowable);
                oos.close();
                baos.close();
                map.put(FAULT_CAUSE, baos.toByteArray());
            } catch (Throwable t) {
                // Ignore me
            }
        }
        writeValue(map);
		handler.endElement(EMPTY_STRING, FAULT, FAULT);
		handler.endElement(EMPTY_STRING, METHOD_RESPONSE, METHOD_RESPONSE);
		if (extensions) {
			handler.endPrefixMapping(EX);
		}
		handler.endDocument();
	}

	/** Writes the XML representation of a Java object.
	 * @param pObject The object being written.
	 * @throws SAXException Writing the object failed.
	 */
	protected void writeValue(Object pObject) throws SAXException {
		final TypeSerializer serializer = this.typeFactory.getSerializer(config, pObject);
		if (serializer == null) {
			throw new SAXException("Unsupported Java type: " + pObject.getClass().getName());
		}
		serializer.write(handler, pObject);
	}
}
