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

import org.apache.ws.commons.util.XsDateTimeFormat;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


/** A {@link TypeSerializer} for date values.
 */
public class CalendarSerializer extends TypeSerializerImpl {
    private static final XsDateTimeFormat format = new XsDateTimeFormat();

    /** Tag name of a BigDecimal value.
     */
    public static final String CALENDAR_TAG = "dateTime";

    private static final String EX_CALENDAR_TAG = "ex:" + CALENDAR_TAG;

    /** Tag name of a date value.
     */
    public static final String DATE_TAG = "dateTime.iso8601";

	public void write(ContentHandler pHandler, Object pObject) throws SAXException {
        write(pHandler, CALENDAR_TAG, EX_CALENDAR_TAG, format.format(pObject));
	}
}
