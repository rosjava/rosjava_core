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

import java.text.Format;
import java.text.ParseException;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/** Parser for integer values.
 */
public class DateParser extends AtomicParser {
	private final Format f;

    /** Creates a new instance with the given format.
     */
    public DateParser(Format pFormat) {
        f = pFormat;
    }

    protected void setResult(String pResult) throws SAXException {
        final String s = pResult.trim();
        if (s.length() == 0) {
            return;
        }
		try {
			super.setResult(f.parseObject(s));
		} catch (ParseException e) {
            final String msg;
            int offset = e.getErrorOffset();
            if (e.getErrorOffset() == -1) {
                msg = "Failed to parse date value: " + pResult;
            } else {
                msg = "Failed to parse date value " + pResult
                    + " at position " + offset;
            }
			throw new SAXParseException(msg, getDocumentLocator(), e);
		}
	}
}
