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

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.ws.commons.util.NamespaceContextImpl;
import org.apache.xmlrpc.common.TypeFactory;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;

import org.apache.xmlrpc.serializer.XmlRpcConstants;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/** A SAX parser for an {@link org.apache.xmlrpc.server.XmlRpcServer}'s
 * response.
 */
public class XmlRpcResponseParser extends RecursiveTypeParserImpl {
	private int level;
	private boolean isSuccess;
	private int errorCode;
	private String errorMessage;
    private Throwable errorCause;

	/** Creates a new instance.
	 * @param pConfig The response configuration.
	 * @param pTypeFactory The type factory for creating instances of
	 * {@link TypeParser}.
	 */
	public XmlRpcResponseParser(XmlRpcStreamRequestConfig pConfig,
								TypeFactory pTypeFactory) {
		super(pConfig, new NamespaceContextImpl(), pTypeFactory);
	}

	protected void addResult(Object pResult) throws SAXException {
		if (isSuccess) {
			super.setResult(pResult);
		} else {
			final Map map = (Map) pResult;
			final Integer faultCode = (Integer) map.get(XmlRpcConstants.FAULT_CODE);
			if (faultCode == null) {
				throw new SAXParseException("Missing faultCode", getDocumentLocator());
			}
			try {
				errorCode = faultCode.intValue();
			} catch (NumberFormatException e) {
				throw new SAXParseException("Invalid faultCode: " + faultCode,
											getDocumentLocator());
			}
			errorMessage = (String) map.get("faultString");
            Object exception = map.get("faultCause");
            if (exception != null) {
                try {
                    byte[] bytes = (byte[]) exception;
                    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    errorCause = (Throwable) ois.readObject();
                    ois.close();
                    bais.close();
                } catch (Throwable t) {
                    // Ignore me
                }
            }
		}
	}

	public void startDocument() throws SAXException {
		super.startDocument();
		level = 0;
        isSuccess = false;
        errorCode = 0;
        errorMessage = null;
	}

	public void startElement(String pURI, String pLocalName, String pQName,
							 Attributes pAttrs) throws SAXException {
		switch (level++) {
			case 0:
				if (!XmlRpcConstants.EMPTY_STRING.equals(pURI)  ||  !XmlRpcConstants.METHOD_RESPONSE.equals(pLocalName)) {
					throw new SAXParseException("Expected methodResponse element, got "
												+ new QName(pURI, pLocalName),
												getDocumentLocator());
				}
				break;
			case 1:
				if (XmlRpcConstants.EMPTY_STRING.equals(pURI)  &&  XmlRpcConstants.PARAMS.equals(pLocalName)) {
					isSuccess = true;
				} else if (XmlRpcConstants.EMPTY_STRING.equals(pURI)  &&  XmlRpcConstants.FAULT.equals(pLocalName)) {
					isSuccess = false;
				} else {
					throw new SAXParseException("Expected params or fault element, got "
												+ new QName(pURI, pLocalName),
												getDocumentLocator());
				}
				break;
			case 2:
				if (isSuccess) {
					if (!XmlRpcConstants.EMPTY_STRING.equals(pURI)  ||  !XmlRpcConstants.PARAM.equals(pLocalName)) {
						throw new SAXParseException("Expected param element, got "
													+ new QName(pURI, pLocalName),
													getDocumentLocator());
					}
				} else {
					if (XmlRpcConstants.EMPTY_STRING.equals(pURI)  &&  XmlRpcConstants.VALUE.equals(pLocalName)) {
						startValueTag();
					} else {
						throw new SAXParseException("Expected value element, got "
													+ new QName(pURI, pLocalName),
													getDocumentLocator());
					}
				}
				break;
			case 3:
				if (isSuccess) {
					if (XmlRpcConstants.EMPTY_STRING.equals(pURI)  &&  XmlRpcConstants.VALUE.equals(pLocalName)) {
						startValueTag();
					} else {
						throw new SAXParseException("Expected value element, got "
								+ new QName(pURI, pLocalName),
								getDocumentLocator());
					}
				} else {
					super.startElement(pURI, pLocalName, pQName, pAttrs);
				}
				break;
			default:
				super.startElement(pURI, pLocalName, pQName, pAttrs);
				break;
		}
	}

	public void endElement(String pURI, String pLocalName, String pQName) throws SAXException {
		switch (--level) {
			case 0:
				if (!XmlRpcConstants.EMPTY_STRING.equals(pURI)  ||  !XmlRpcConstants.METHOD_RESPONSE.equals(pLocalName)) {
					throw new SAXParseException("Expected /methodResponse element, got "
												+ new QName(pURI, pLocalName),
												getDocumentLocator());
				}
				break;
			case 1:
				{
					String tag;
					if (isSuccess) {
						tag = XmlRpcConstants.PARAMS;
					} else {
						tag = XmlRpcConstants.FAULT;
					}
					if (!XmlRpcConstants.EMPTY_STRING.equals(pURI)  ||  !tag.equals(pLocalName)) {
						throw new SAXParseException("Expected /" + tag + " element, got "
								+ new QName(pURI, pLocalName),
								getDocumentLocator());
					}
					break;
				}
			case 2:
				if (isSuccess) {
					if (!XmlRpcConstants.EMPTY_STRING.equals(pURI)  ||  !XmlRpcConstants.PARAM.equals(pLocalName)) {
						throw new SAXParseException("Expected /param, got "
													+ new QName(pURI, pLocalName),
													getDocumentLocator());
					}
				} else {
					if (XmlRpcConstants.EMPTY_STRING.equals(pURI)  &&  XmlRpcConstants.VALUE.equals(pLocalName)) {
						endValueTag();
					} else {
						throw new SAXParseException("Expected /value, got "
								+ new QName(pURI, pLocalName),
								getDocumentLocator());
					}
				}
				break;
			case 3:
				if (isSuccess) {
					if (XmlRpcConstants.EMPTY_STRING.equals(pURI)  &&  XmlRpcConstants.VALUE.equals(pLocalName)) {
						endValueTag();
					} else {
						throw new SAXParseException("Expected /value, got "
								+ new QName(pURI, pLocalName),
								getDocumentLocator());
					}
				} else {
					super.endElement(pURI, pLocalName, pQName);
				}
				break;
			default:
				super.endElement(pURI, pLocalName, pQName);
				break;
		}
	}

	/** Returns whether the response returned success. If so, the
	 * result object may be fetched using {@link #getResult()}.
	 * Otherwise, you may use the methods
	 * {@link #getErrorCode()} and {@link #getErrorMessage()} to
	 * check for error reasons.
	 * @return True, if the response indicated success, false otherwise.
	 */
	public boolean isSuccess() { return isSuccess; }

	/** If the response contained a fault, returns the error code.
	 * @return The numeric error code.
	 */
	public int getErrorCode() { return errorCode; }

	/** If the response contained a fault, returns the error message.
	 * @return The error message.
	 */
	public String getErrorMessage() { return errorMessage; }

	/** If the response contained a fault, returns the (optional)
     * exception.
	 */
    public Throwable getErrorCause() { return errorCause; }
}
