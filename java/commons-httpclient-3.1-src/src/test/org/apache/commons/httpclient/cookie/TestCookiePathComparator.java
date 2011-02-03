/*
 * $HeaderURL$
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

import java.util.Comparator;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.Cookie;

/**
 * Test cases for {@link CookiePathComparator}.
 */
public class TestCookiePathComparator extends TestCookieBase {


    // ------------------------------------------------------------ Constructor

    public TestCookiePathComparator(String name) {
        super(name);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestCookiePathComparator.class);
    }

    public void testUnequality1() {
        Cookie cookie1 = new Cookie(".whatever.com", "name1", "value", "/a/b/", null, false); 
        Cookie cookie2 = new Cookie(".whatever.com", "name1", "value", "/a/", null, false);
        Comparator comparator = new CookiePathComparator();
        assertTrue(comparator.compare(cookie1, cookie2) < 0);
        assertTrue(comparator.compare(cookie2, cookie1) > 0);
    }

    public void testUnequality2() {
        Cookie cookie1 = new Cookie(".whatever.com", "name1", "value", "/a/b", null, false); 
        Cookie cookie2 = new Cookie(".whatever.com", "name1", "value", "/a", null, false);
        Comparator comparator = new CookiePathComparator();
        assertTrue(comparator.compare(cookie1, cookie2) < 0);
        assertTrue(comparator.compare(cookie2, cookie1) > 0);
    }

    public void testEquality1() {
        Cookie cookie1 = new Cookie(".whatever.com", "name1", "value", "/a", null, false); 
        Cookie cookie2 = new Cookie(".whatever.com", "name1", "value", "/a", null, false);
        Comparator comparator = new CookiePathComparator();
        assertTrue(comparator.compare(cookie1, cookie2) == 0);
        assertTrue(comparator.compare(cookie2, cookie1) == 0);
    }

    public void testEquality2() {
        Cookie cookie1 = new Cookie(".whatever.com", "name1", "value", "/a/", null, false); 
        Cookie cookie2 = new Cookie(".whatever.com", "name1", "value", "/a", null, false);
        Comparator comparator = new CookiePathComparator();
        assertTrue(comparator.compare(cookie1, cookie2) == 0);
        assertTrue(comparator.compare(cookie2, cookie1) == 0);
    }

    public void testEquality3() {
        Cookie cookie1 = new Cookie(".whatever.com", "name1", "value", null, null, false); 
        Cookie cookie2 = new Cookie(".whatever.com", "name1", "value", "/", null, false);
        Comparator comparator = new CookiePathComparator();
        assertTrue(comparator.compare(cookie1, cookie2) == 0);
        assertTrue(comparator.compare(cookie2, cookie1) == 0);
    }

    public void testEquality4() {
        Cookie cookie1 = new Cookie(".whatever.com", "name1", "value", "/this", null, false); 
        Cookie cookie2 = new Cookie(".whatever.com", "name1", "value", "/that", null, false);
        Comparator comparator = new CookiePathComparator();
        assertTrue(comparator.compare(cookie1, cookie2) == 0);
        assertTrue(comparator.compare(cookie2, cookie1) == 0);
    }
    
}

