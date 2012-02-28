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

import java.text.Format;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


/** A {@link TypeSerializer} for date values.
 */
public class DateSerializer extends TypeSerializerImpl {
    /** Tag name of a date value.
     */
    public static final String DATE_TAG = "dateTime.iso8601";

    private final Format format;

    /** Creates a new instance with the given formatter.
     */
    public DateSerializer(Format pFormat) {
        format = pFormat;
    }

	public void write(ContentHandler pHandler, Object pObject) throws SAXException {
        write(pHandler, DATE_TAG, format.format(pObject));
	}
}
