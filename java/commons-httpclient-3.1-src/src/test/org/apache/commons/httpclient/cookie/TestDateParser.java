/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/cookie/TestDateParser.java,v 1.1 2004/12/24 20:36:13 olegk Exp $
 * $Revision: 480424 $
 * $Date: 2006-11-29 06:56:49 +0100 (Wed, 29 Nov 2006) $
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.commons.httpclient.cookie;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.httpclient.util.DateUtil;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test cases for expiry date parsing
 *
 * @author <a href="mailto:oleg at ural.ru">Oleg Kalnichevski</a>
 * 
 * @version $Revision: 480424 $
 */
public class TestDateParser extends TestCase {

    // ------------------------------------------------------------ Constructor

    public TestDateParser(String name) {
        super(name);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestDateParser.class);
    }

    private static final String PATTERN = "EEE, dd-MMM-yy HH:mm:ss zzz";
    private static final List PATTERNS = new ArrayList();
    
    static {
        PATTERNS.add(PATTERN);
    }
    
    public void testFourDigitYear() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateUtil.parseDate("Thu, 23-Dec-2004 24:00:00 CET", PATTERNS));
        assertEquals(2004, calendar.get(Calendar.YEAR));
    }

    public void testThreeDigitYear() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateUtil.parseDate("Thu, 23-Dec-994 24:00:00 CET", PATTERNS));
        assertEquals(994, calendar.get(Calendar.YEAR));
    }

    public void testTwoDigitYear() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateUtil.parseDate("Thu, 23-Dec-04 24:00:00 CET", PATTERNS));
        assertEquals(2004, calendar.get(Calendar.YEAR));

        calendar.setTime(DateUtil.parseDate("Thu, 23-Dec-94 24:00:00 CET", PATTERNS));
        assertEquals(2094, calendar.get(Calendar.YEAR));
    }

}

