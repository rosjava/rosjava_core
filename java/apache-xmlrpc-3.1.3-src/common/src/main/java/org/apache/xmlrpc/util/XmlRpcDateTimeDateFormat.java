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
package org.apache.xmlrpc.util;

import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;



/** An extension of {@link XmlRpcDateTimeFormat}, which accepts
 * and/or creates instances of {@link Date}.
 */
public abstract class XmlRpcDateTimeDateFormat extends XmlRpcDateTimeFormat {
    private static final long serialVersionUID = -5107387618606150784L;

    public StringBuffer format(Object pCalendar, StringBuffer pBuffer, FieldPosition pPos) {
        final Object cal;
        if (pCalendar != null  &&  pCalendar instanceof Date) {
            Calendar calendar = Calendar.getInstance(getTimeZone());
            calendar.setTime((Date) pCalendar);
            cal = calendar;
        } else {
            cal = pCalendar;
        }
        return super.format(cal, pBuffer, pPos);
    }

    public Object parseObject(String pString, ParsePosition pParsePosition) {
        Calendar cal = (Calendar) super.parseObject(pString, pParsePosition);
        return cal == null ? null : cal.getTime();
    }
}
