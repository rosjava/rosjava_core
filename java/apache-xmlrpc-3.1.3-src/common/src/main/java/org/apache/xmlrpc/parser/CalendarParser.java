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

import java.text.ParseException;

import org.apache.ws.commons.util.XsDateTimeFormat;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/** Parser for integer values.
 */
public class CalendarParser extends AtomicParser {
	private static final XsDateTimeFormat format = new XsDateTimeFormat();

    protected void setResult(String pResult) throws SAXException {
		try {
			super.setResult(format.parseObject(pResult.trim()));
		} catch (ParseException e) {
            int offset = e.getErrorOffset();
            final String msg;
            if (offset == -1) {
                msg = "Failed to parse dateTime value: " + pResult;
            } else {
                msg = "Failed to parse dateTime value " + pResult
                    + " at position " + e.getErrorOffset();
            }
            throw new SAXParseException(msg, getDocumentLocator(), e);
		}
	}
}
