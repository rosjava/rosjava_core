/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/cookie/TestCookie.java,v 1.2 2004/04/25 12:25:09 olegk Exp $
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

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;


/**
 * Test cases for Cookie
 *
 * @author BC Holmes
 * @author Rod Waldhoff
 * @author dIon Gillard
 * @author <a href="mailto:JEvans@Cyveillance.com">John Evans</a>
 * @author Marc A. Saegesser
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @version $Revision: 480424 $
 */
public class TestCookie extends TestCookieBase {


    // ------------------------------------------------------------ Constructor

    public TestCookie(String name) {
        super(name);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestCookie.class);
    }

    /**
     * Tests default constructor.
     */
    public void testDefaultConstuctor() {
        Cookie dummy = new Cookie();
        assertEquals( "noname=", dummy.toExternalForm() );
    }

    public void testComparator() throws Exception {
        Header setCookie = null;
        Cookie[] parsed = null;
        Vector cookies = new Vector();
        // Cookie 0
        setCookie = new Header("Set-Cookie","cookie-name=cookie-value;Path=/commons;Domain=.apache.org;Expires=Thu, 01-Jan-1970 00:00:10 GMT");
        CookieSpec cookiespec = new CookieSpecBase();
        parsed = cookieParse(cookiespec, ".apache.org", 80, "/commons/httpclient", true, setCookie);
        cookies.add(parsed[0]);
        // Cookie 1
        setCookie = new Header("Set-Cookie","cookie-name=cookie-value;Path=/commons/bif;Domain=.apache.org;Expires=Thu, 01-Jan-1970 00:00:10 GMT");
        parsed = cookieParse(cookiespec, ".apache.org", 80, "/commons/bif/httpclient", true, setCookie);
        cookies.add(parsed[0]);
        // Cookie 2
        setCookie = new Header("Set-Cookie","cookie-name=cookie-value;Path=/commons;Domain=.baz.org;Expires=Thu, 01-Jan-1970 00:00:10 GMT");
        parsed = cookieParse(cookiespec, ".baz.org", 80, "/commons/httpclient", true, setCookie);
        cookies.add(parsed[0]);
        // Cookie 3
        setCookie = new Header("Set-Cookie","cookie-name=cookie-value;Path=/commons/bif;Domain=.baz.org;Expires=Thu, 01-Jan-1970 00:00:10 GMT");
        parsed = cookieParse(cookiespec, ".baz.org", 80, "/commons/bif/httpclient", true, setCookie);
        cookies.add(parsed[0]);
        // Cookie 4
        setCookie = new Header("Set-Cookie","cookie-name=cookie-value;Path=/commons;Domain=.baz.com;Expires=Thu, 01-Jan-1970 00:00:10 GMT");
        parsed = cookieParse(cookiespec, ".baz.com", 80, "/commons/httpclient", true, setCookie);
        cookies.add(parsed[0]);
        // The order should be:
        // 1, 0, 3, 2, 4
        parsed = (Cookie[])cookies.toArray(new Cookie[0]);
        SortedSet set = new TreeSet(parsed[0]);
        int pass = 0;
        for (Iterator itr = set.iterator(); itr.hasNext();) {
            Cookie cookie = (Cookie)itr.next();
            switch (pass) {
                case 0:
                    assertTrue("0th cookie should be cookie[1]", cookie == parsed[1]);
                    break;
                case 1:
                    assertTrue("1st cookie should be cookie[0]", cookie == parsed[0]);
                    break;
                case 2:
                    assertTrue("2nd cookie should be cookie[3]", cookie == parsed[3]);
                    break;
                case 3:
                    assertTrue("3rd cookie should be cookie[2]", cookie == parsed[2]);
                    break;
                case 4:
                    assertTrue("4th cookie should be cookie[4]", cookie == parsed[4]);
                    break;
                default:
                    fail("This should never happen.");
            }
            pass++;
        }
        try {
            parsed[0].compare("foo", "bar");
            fail("Should have thrown an exception trying to compare non-cookies");
        }
        catch (ClassCastException ex) {
            // expected
        }
    }
}

