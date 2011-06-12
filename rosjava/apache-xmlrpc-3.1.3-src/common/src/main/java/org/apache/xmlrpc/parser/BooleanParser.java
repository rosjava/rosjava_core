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

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/** Parser for boolean values.
 */
public class BooleanParser extends AtomicParser {
	protected void setResult(String pResult) throws SAXException {
		String s = pResult.trim();
		if ("1".equals(s)) {
			super.setResult(Boolean.TRUE);
		} else if ("0".equals(s)) {
			super.setResult(Boolean.FALSE);
		} else {
			throw new SAXParseException("Failed to parse boolean value: " + pResult,
										getDocumentLocator());
		}
	}
}
