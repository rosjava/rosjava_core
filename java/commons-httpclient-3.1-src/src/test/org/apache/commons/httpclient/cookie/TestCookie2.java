/*
 * $Header: /cvsroot/httpc-cookie2/httpc-cookie2/httpcookie2SVN-patch.082805-2100.diff,v 1.1 2005/08/29 05:01:58 sjain700 Exp $
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

import java.util.*;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;


/**
 * Test cases for {@link Cookie2}.
 *
 * @author Samit Jain (jain.samit@gmail.com)
 */
public class TestCookie2 extends TestCookieBase {


    // ------------------------------------------------------------ Constructor

    public TestCookie2(String name) {
        super(name);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestCookie2.class);
    }

    /**
     * Tests default constructor.
     */
    public void testDefaultConstuctor() {
        Cookie2 dummy = new Cookie2();
        // check cookie properties (default values)
        assertNull(dummy.getPorts());
        assertFalse(dummy.getSecure());
        assertFalse(dummy.isExpired());
        assertFalse(dummy.isDomainAttributeSpecified());
        assertFalse(dummy.isPathAttributeSpecified());
        assertFalse(dummy.isPortAttributeSpecified());
        assertFalse(dummy.isVersionAttributeSpecified());
        assertFalse(dummy.isPersistent());

        Cookie2 dummy2 = new Cookie2();
        assertEquals(dummy, dummy2);
    }

    public void testComparator() throws Exception {
        Header setCookie2 = null;
        Cookie[] parsed = null;
        List cookies = new LinkedList();
        CookieSpec cookiespec = new RFC2965Spec();
        // Cookie 0
        setCookie2 = new Header("Set-Cookie2","cookie-name=Cookie0; Version=1");
        parsed = cookieParse(cookiespec, "domain.com", 80,
                             "/path/path1", true, setCookie2);
        cookies.add(parsed[0]);
        // Cookie 1
        setCookie2 = new Header("Set-Cookie2","cookie-name=Cookie1; Version=1");
        parsed = cookieParse(cookiespec, "domain.com", 80, "/path", true, setCookie2);
        cookies.add(parsed[0]);
        // Cookie 2
        setCookie2 = new Header("Set-Cookie2","cookie-name=Cookie2; Version=1");
        parsed = cookieParse(cookiespec, "domain.com", 80, "/", true, setCookie2);
        cookies.add(parsed[0]);
        // Cookie 3
        setCookie2 = new Header("Set-Cookie2","cookie-name=Cookie3; Version=1");
        parsed = cookieParse(cookiespec, "domain.com", 80,
                             "/path/path1/path2", true, setCookie2);
        cookies.add(parsed[0]);
        // Cookie 4
        setCookie2 = new Header("Set-Cookie2","cookie-name=Cookie4; Version=1");
        parsed = cookieParse(cookiespec, "domain.com", 80,
                             "/path/path1/path2/path3", true, setCookie2);
        cookies.add(parsed[0]);

        // The ascending order should be:
        // 2, 1, 0, 3, 4
        int[] expectedOrder = new int[] {2, 1, 0, 3, 4};
        Set sortedCookies = new TreeSet(parsed[0]);
        sortedCookies.addAll(cookies);

        int pass = 0;
        for (Iterator itr = sortedCookies.iterator(); itr.hasNext(); ++pass) {
            Cookie2 cookie = (Cookie2) itr.next();
            assertTrue("sortedCookies[" + pass + "] should be cookies[" + expectedOrder[pass] + "]",
                       cookie == cookies.get(expectedOrder[pass]));
        }

        try {
            parsed[0].compare(parsed[0], "foo");
            fail("Should have thrown an exception trying to compare non-cookies");
        } catch (ClassCastException expected) {}
    }
}

