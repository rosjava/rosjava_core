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

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


/** A {@link TypeSerializer} for null values.
 */
public class NullSerializer extends TypeSerializerImpl {
	/** Tag name of a nil value.
	 */
	public static final String NIL_TAG = "nil";
	/** Qualified tag name of a nil value.
	 */
	public static final String EX_NIL_TAG = "ex:nil";

	public void write(ContentHandler pHandler, Object pObject) throws SAXException {
		pHandler.startElement("", VALUE_TAG, VALUE_TAG, ZERO_ATTRIBUTES);
		pHandler.startElement(XmlRpcWriter.EXTENSIONS_URI, NIL_TAG, EX_NIL_TAG, ZERO_ATTRIBUTES);
		pHandler.endElement(XmlRpcWriter.EXTENSIONS_URI, NIL_TAG, EX_NIL_TAG);
		pHandler.endElement("", VALUE_TAG, VALUE_TAG);
	}
}
