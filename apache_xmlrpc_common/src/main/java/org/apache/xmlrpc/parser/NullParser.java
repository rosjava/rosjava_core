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

import org.apache.xmlrpc.serializer.XmlRpcConstants;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/** SAX parser for a nil element (null value).
 */
public class NullParser extends AtomicParser {
	protected void setResult(String pResult) throws SAXException {
		if (pResult == null  ||  XmlRpcConstants.EMPTY_STRING.equals(pResult.trim())) {
			super.setResult((Object) null);
		} else {
			throw new SAXParseException("Unexpected characters in nil element.",
										getDocumentLocator());
		}
	}
}
