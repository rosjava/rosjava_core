/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/cookie/TestCookieNetscapeDraft.java,v 1.2 2004/04/24 23:28:04 olegk Exp $
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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;


/**
 * Test cases for Netscape cookie draft
 *
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * 
 * @version $Revision: 480424 $
 */
public class TestCookieNetscapeDraft extends TestCookieBase {

    // ------------------------------------------------------------ Constructor

    public TestCookieNetscapeDraft(String name) {
        super(name);
    }


    // ------------------------------------------------------- TestCase Methods


    public static Test suite() {
        return new TestSuite(TestCookieNetscapeDraft.class);
    }

    public void testParseAttributeInvalidAttrib() throws Exception {
        CookieSpec cookiespec = new NetscapeDraftSpec();
        try {
            cookiespec.parseAttribute(null, null);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testParseAttributeInvalidCookie() throws Exception {
        CookieSpec cookiespec = new NetscapeDraftSpec();
        try {
            cookiespec.parseAttribute(new NameValuePair("name", "value"), null);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testParseAttributeInvalidCookieExpires() throws Exception {
        CookieSpec cookiespec = new NetscapeDraftSpec();
        Cookie cookie = new Cookie();
        try {
            cookiespec.parseAttribute(new NameValuePair("expires", null), cookie);
            fail("MalformedCookieException must have been thrown");
        } catch (MalformedCookieException expected) {
        }
    }

    public void testParseWithNullHost() throws Exception {
        Header header = new Header("Set-Cookie",
            "cookie-name=cookie-value; domain=127.0.0.1; path=/; secure");

        CookieSpec cookiespec = new NetscapeDraftSpec();
        try {
            Cookie[] parsed = cookieParse(cookiespec, null, 80, "/", false, header);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testParseWithBlankHost() throws Exception {
        Header header = new Header("Set-Cookie",
            "cookie-name=cookie-value; domain=127.0.0.1; path=/; secure");

        CookieSpec cookiespec = new NetscapeDraftSpec();
        try {
            Cookie[] parsed = cookieParse(cookiespec, "  ", 80, "/", false, header);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testParseWithNullPath() throws Exception {
        Header header = new Header("Set-Cookie",
            "cookie-name=cookie-value; domain=127.0.0.1; path=/; secure");

        CookieSpec cookiespec = new NetscapeDraftSpec();
        try {
            Cookie[] parsed = cookieParse(cookiespec, "127.0.0.1", 80, null, false, header);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testParseWithBlankPath() throws Exception {
        Header header = new Header("Set-Cookie",
            "cookie-name=cookie-value; domain=127.0.0.1; path=/; secure");

        CookieSpec cookiespec = new NetscapeDraftSpec();
        Cookie[] parsed = cookieParse(cookiespec, "127.0.0.1", 80, "  ", false, header);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
        assertEquals("/", parsed[0].getPath());
    }

    public void testParseWithNegativePort() throws Exception {
        Header header = new Header("Set-Cookie",
            "cookie-name=cookie-value; domain=127.0.0.1; path=/; secure");

        CookieSpec cookiespec = new NetscapeDraftSpec();
        try {
            Cookie[] parsed = cookieParse(cookiespec, "127.0.0.1", -80, null, false, header);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testParseWithInvalidHeader1() throws Exception {
        CookieSpec cookiespec = new NetscapeDraftSpec();
        try {
            Cookie[] parsed = cookiespec.parse("127.0.0.1", 80, "/foo", false, (String)null);
            fail("IllegalArgumentException should have been thrown.");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testParseAbsPath() throws Exception {
        Header header = new Header("Set-Cookie", "name1=value1;Path=/path/");

        CookieSpec cookiespec = new NetscapeDraftSpec();
        Cookie[] parsed = cookieParse(cookiespec, "host", 80, "/path/", true, header);
        assertEquals("Found 1 cookies.",1,parsed.length);
        assertEquals("Name","name1",parsed[0].getName());
        assertEquals("Value","value1",parsed[0].getValue());
        assertEquals("Domain","host",parsed[0].getDomain());
        assertEquals("Path","/path/",parsed[0].getPath());
    }

    public void testParseAbsPath2() throws Exception {
        Header header = new Header("Set-Cookie", "name1=value1;Path=/");

        CookieSpec cookiespec = new NetscapeDraftSpec();
        Cookie[] parsed = cookieParse(cookiespec, "host", 80, "/", true, header);
        assertEquals("Found 1 cookies.",1,parsed.length);
        assertEquals("Name","name1",parsed[0].getName());
        assertEquals("Value","value1",parsed[0].getValue());
        assertEquals("Domain","host",parsed[0].getDomain());
        assertEquals("Path","/",parsed[0].getPath());
    }

    public void testParseRelativePath() throws Exception {
        Header header = new Header("Set-Cookie", "name1=value1;Path=whatever");

        CookieSpec cookiespec = new NetscapeDraftSpec();
        Cookie[] parsed = cookieParse(cookiespec, "host", 80, "whatever", true, header);
        assertEquals("Found 1 cookies.",1,parsed.length);
        assertEquals("Name","name1",parsed[0].getName());
        assertEquals("Value","value1",parsed[0].getValue());
        assertEquals("Domain","host",parsed[0].getDomain());
        assertEquals("Path","whatever",parsed[0].getPath());
    }

    public void testParseWithIllegalNetscapeDomain1() throws Exception {
        Header header = new Header("Set-Cookie","cookie-name=cookie-value; domain=.com");

        CookieSpec cookiespec = new NetscapeDraftSpec();
        try {
            Cookie[] parsed = cookieParse(cookiespec, "a.com", 80, "/", false, header);
            fail("HttpException exception should have been thrown");
        } catch (HttpException e) {
            // expected
        }
    }

    public void testParseWithWrongNetscapeDomain2() throws Exception {
        Header header = new Header("Set-Cookie","cookie-name=cookie-value; domain=.y.z");
        
        CookieSpec cookiespec = new NetscapeDraftSpec();
        try {
            Cookie[] parsed = cookieParse(cookiespec, "x.y.z", 80, "/", false, header);
            fail("HttpException exception should have been thrown");
        } catch (HttpException e) {
            // expected
        }
    }

    /**
     * Tests Netscape specific cookie formatting.
     */
    
    public void testNetscapeCookieFormatting() throws Exception {
        Header header = new Header(
          "Set-Cookie", "name=value; path=/; domain=.mydomain.com");
        CookieSpec cookiespec = new NetscapeDraftSpec();
        Cookie[] cookies = cookiespec.parse("myhost.mydomain.com", 80, "/", false, header );
        cookiespec.validate("myhost.mydomain.com", 80, "/", false, cookies[0]);
        String s = cookiespec.formatCookie(cookies[0]);
        assertEquals("name=value", s);
    }
    
    /**
     * Tests Netscape specific expire attribute parsing.
     */
    public void testNetscapeCookieExpireAttribute() throws Exception {
        CookieSpec cookiespec = new NetscapeDraftSpec();
        Header header = new Header("Set-Cookie", 
            "name=value; path=/; domain=.mydomain.com; expires=Thu, 01-Jan-2070 00:00:10 GMT; comment=no_comment");
        Cookie[] cookies = cookiespec.parse("myhost.mydomain.com", 80, "/", false, header );
        cookiespec.validate("myhost.mydomain.com", 80, "/", false, cookies[0]);
        header = new Header("Set-Cookie", 
            "name=value; path=/; domain=.mydomain.com; expires=Thu 01-Jan-2070 00:00:10 GMT; comment=no_comment");
        try {
            cookies = cookiespec.parse("myhost.mydomain.com", 80, "/", false, header );
            cookiespec.validate("myhost.mydomain.com", 80, "/", false, cookies[0]);
            fail("MalformedCookieException must have been thrown");
        }
        catch (MalformedCookieException expected) {
        }
    }

    /**
     * Tests Netscape specific expire attribute without a time zone.
     */
    public void testNetscapeCookieExpireAttributeNoTimeZone() throws Exception {
        CookieSpec cookiespec = new NetscapeDraftSpec();
        Header header = new Header("Set-Cookie", 
            "name=value; expires=Thu, 01-Jan-2006 00:00:00 ");
        try {
            cookiespec.parse("myhost.mydomain.com", 80, "/", false, header );
            fail("MalformedCookieException should have been thrown");
        } catch (MalformedCookieException ex) {
            // expected
        }
    }
    
    /**
     * Tests if cookie values with embedded comma are handled correctly.
     */
    public void testCookieWithComma() throws Exception {
        Header header = new Header("Set-Cookie", "a=b,c");

        CookieSpec cookiespec = new NetscapeDraftSpec();
        Cookie[] cookies = cookiespec.parse("localhost", 80, "/", false, header);
        assertEquals("number of cookies", 1, cookies.length);
        assertEquals("a", cookies[0].getName());
        assertEquals("b,c", cookies[0].getValue());
    }
    
}

