/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/cookie/TestCookieRFC2109Spec.java,v 1.3 2004/06/05 16:49:20 olegk Exp $
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
import org.apache.commons.httpclient.NameValuePair;

/**
 * Test cases for RFC2109 cookie spec
 *
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * 
 * @version $Revision: 480424 $
 */
public class TestCookieRFC2109Spec extends TestCookieBase {


    // ------------------------------------------------------------ Constructor

    public TestCookieRFC2109Spec(String name) {
        super(name);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestCookieRFC2109Spec.class);
    }

    public void testParseAttributeInvalidAttrib() throws Exception {
        CookieSpec cookiespec = new RFC2109Spec();
        try {
            cookiespec.parseAttribute(null, null);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testParseAttributeInvalidCookie() throws Exception {
        CookieSpec cookiespec = new RFC2109Spec();
        try {
            cookiespec.parseAttribute(new NameValuePair("name", "value"), null);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testParseAttributeNullPath() throws Exception {
        CookieSpec cookiespec = new RFC2109Spec();
        try {
            Cookie cookie = new Cookie();
            cookiespec.parseAttribute(new NameValuePair("path", null), cookie);
            fail("MalformedCookieException must have been thrown");
        } catch (MalformedCookieException expected) {
        }
    }

    public void testParseAttributeBlankPath() throws Exception {
        CookieSpec cookiespec = new RFC2109Spec();
        try {
            Cookie cookie = new Cookie();
            cookiespec.parseAttribute(new NameValuePair("path", "   "), cookie);
            fail("MalformedCookieException must have been thrown");
        } catch (MalformedCookieException expected) {
        }
    }

    public void testParseAttributeNullVersion() throws Exception {
        CookieSpec cookiespec = new RFC2109Spec();
        try {
            Cookie cookie = new Cookie();
            cookiespec.parseAttribute(new NameValuePair("version", null), cookie);
            fail("MalformedCookieException must have been thrown");
        } catch (MalformedCookieException expected) {
        }
    }

    public void testParseAttributeInvalidVersion() throws Exception {
        CookieSpec cookiespec = new RFC2109Spec();
        try {
            Cookie cookie = new Cookie();
            cookiespec.parseAttribute(new NameValuePair("version", "nonsense"), cookie);
            fail("MalformedCookieException must have been thrown");
        } catch (MalformedCookieException expected) {
        }
    }

    public void testParseVersion() throws Exception {
        Header header = new Header("Set-Cookie","cookie-name=cookie-value; version=1");

        CookieSpec cookiespec = new RFC2109Spec();
        Cookie[] parsed = cookieParse(cookiespec, "127.0.0.1", 80, "/", false, header);
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertEquals("Value","cookie-value",parsed[0].getValue());
        assertEquals("Version",1,parsed[0].getVersion());
    }

    /**
     * Test domain equals host 
     */
    public void testParseDomainEqualsHost() throws Exception {
        Header header = new Header("Set-Cookie",
            "cookie-name=cookie-value; domain=www.b.com; version=1");

        CookieSpec cookiespec = new RFC2109Spec();
        Cookie[] parsed = cookieParse(cookiespec, "www.b.com", 80, "/", false, header);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
        assertEquals("www.b.com", parsed[0].getDomain());
    }

    /**
     * Domain does not start with a dot
     */
    public void testParseWithIllegalDomain1() throws Exception {
        Header header = new Header("Set-Cookie",
            "cookie-name=cookie-value; domain=a.b.com; version=1");

        CookieSpec cookiespec = new RFC2109Spec();
        try {
            Cookie[] parsed = cookieParse(cookiespec, "www.a.b.com", 80, "/", false, header);
            fail("MalformedCookieException should have been thrown");
        } catch (MalformedCookieException e) {
            // expected
        }
    }

    /**
     * Domain must have alt least one embedded dot
     */
    public void testParseWithIllegalDomain2() throws Exception {
        Header header = new Header("Set-Cookie",
            "cookie-name=cookie-value; domain=.com; version=1");

        CookieSpec cookiespec = new RFC2109Spec();
        try {
            Cookie[] parsed = cookieParse(cookiespec, "b.com", 80, "/", false, header);
            fail("MalformedCookieException should have been thrown");
        } catch (MalformedCookieException e) {
            // expected
        }
    }
    /**
     * Domain must have alt least one embedded dot
     */
    public void testParseWithIllegalDomain3() throws Exception {
        Header header = new Header("Set-Cookie",
            "cookie-name=cookie-value; domain=.com.; version=1");

        CookieSpec cookiespec = new RFC2109Spec();
        try {
            Cookie[] parsed = cookieParse(cookiespec, "b.com", 80, "/", false, header);
            fail("HttpException exception should have been thrown");
        } catch (MalformedCookieException e) {
            // expected
        }
    }

    /**
     * Host minus domain may not contain any dots
     */
    public void testParseWithIllegalDomain4() throws Exception {
        Header header = new Header("Set-Cookie",
            "cookie-name=cookie-value; domain=.c.com; version=1");

        CookieSpec cookiespec = new RFC2109Spec();
        try {
            Cookie[] parsed = cookieParse(cookiespec, "a.b.c.com", 80, "/", false, header);
            fail("MalformedCookieException should have been thrown");
        } catch (MalformedCookieException e) {
            // expected
        }
    }

    /**
     * Tests if that invalid second domain level cookie gets 
     * rejected in the strict mode, but gets accepted in the
     * browser compatibility mode.
     */
    public void testSecondDomainLevelCookie() throws Exception {
        Cookie cookie = new Cookie(".sourceforge.net", "name", null, "/", null, false); 
        cookie.setDomainAttributeSpecified(true);
        cookie.setPathAttributeSpecified(true);

        CookieSpec cookiespec = new RFC2109Spec();
        try {
            cookiespec.validate("sourceforge.net", 80, "/", false, cookie);
            fail("MalformedCookieException should have been thrown");
        } catch (MalformedCookieException e) {
            // Expected
        }
    }    

    public void testSecondDomainLevelCookieMatch() throws Exception {
        Cookie cookie = new Cookie(".sourceforge.net", "name", null, "/", null, false); 
        cookie.setDomainAttributeSpecified(true);
        cookie.setPathAttributeSpecified(true);

        CookieSpec cookiespec = new RFC2109Spec();
        assertFalse(cookiespec.match("sourceforge.net", 80, "/", false, cookie));
    }
    
    public void testParseWithWrongPath() throws Exception {
        Header header = new Header("Set-Cookie",
            "cookie-name=cookie-value; domain=127.0.0.1; path=/not/just/root");

        CookieSpec cookiespec = new RFC2109Spec();
        try {
            Cookie[] parsed = cookieParse(cookiespec, "127.0.0.1", 80, "/", false, header);
            fail("HttpException exception should have been thrown");
        } catch (MalformedCookieException e) {
            // expected
        }
    }

    /**
     * Tests if cookie constructor rejects cookie name containing blanks.
     */
    public void testCookieNameWithBlanks() throws Exception {
        Header setcookie = new Header("Set-Cookie", "invalid name=");
        CookieSpec cookiespec = new RFC2109Spec();
        try {
            Cookie[] parsed = cookieParse(cookiespec, "127.0.0.1", 80, "/", false, setcookie);
            fail("MalformedCookieException exception should have been thrown");
        } catch (MalformedCookieException e) {
            // expected
        }
    }


    /**
     * Tests if cookie constructor rejects cookie name starting with $.
     */
    public void testCookieNameStartingWithDollarSign() throws Exception {
        Header setcookie = new Header("Set-Cookie", "$invalid_name=");
        CookieSpec cookiespec = new RFC2109Spec();
        try {
            Cookie[] parsed = cookieParse(cookiespec, "127.0.0.1", 80, "/", false, setcookie);
            fail("MalformedCookieException exception should have been thrown");
        } catch (MalformedCookieException e) {
            // expected
        }
    }

    /**
     * Tests if default cookie validator rejects cookies originating from a host without domain
     * where domain attribute does not match the host of origin 
     */
    public void testInvalidDomainWithSimpleHostName() throws Exception {    
        CookieSpec cookiespec = new RFC2109Spec();
        Header header = new Header("Set-Cookie", 
            "name=\"value\"; version=\"1\"; path=\"/\"; domain=\".mydomain.com\"");
        Cookie[]cookies = cookiespec.parse("host", 80, "/", false, header );
        try {
            cookiespec.validate("host", 80, "/", false, cookies[0]);
            fail("MalformedCookieException must have thrown");
        }
        catch(MalformedCookieException expected) {
        }
        header = new Header("Set-Cookie", 
            "name=\"value\"; version=\"1\"; path=\"/\"; domain=\"host1\"");
        cookies = cookiespec.parse("host2", 80, "/", false, header );
        try {
            cookiespec.validate("host2", 80, "/", false, cookies[0]);
            fail("MalformedCookieException must have thrown");
        }
        catch(MalformedCookieException expected) {
        }
    }

    /**
     * Tests if cookie values with embedded comma are handled correctly.
     */
    public void testCookieWithComma() throws Exception {
        Header header = new Header("Set-Cookie", "a=b,c");

        CookieSpec cookiespec = new RFC2109Spec();
        Cookie[] cookies = cookiespec.parse("localhost", 80, "/", false, header);
        assertEquals("number of cookies", 2, cookies.length);
        assertEquals("a", cookies[0].getName());
        assertEquals("b", cookies[0].getValue());
        assertEquals("c", cookies[1].getName());
        assertEquals(null, cookies[1].getValue());
    }

    public void testFormatInvalidCookies() throws Exception {
        CookieSpec cookiespec = new RFC2109Spec();
        try {
            String s = cookiespec.formatCookie(null);
            fail("IllegalArgumentException nust have been thrown");
        } catch (IllegalArgumentException expected) {
        }
    }    

    /**
     * Tests RFC 2109 compiant cookie formatting.
     */
    public void testRFC2109CookieFormatting() throws Exception {
        CookieSpec cookiespec = new RFC2109Spec();
        Header header = new Header("Set-Cookie", 
            "name=\"value\"; version=\"1\"; path=\"/\"; domain=\".mydomain.com\"");
        Cookie[] cookies  = cookiespec.parse("myhost.mydomain.com", 80, "/", false, header );
        cookiespec.validate("myhost.mydomain.com", 80, "/", false, cookies[0]);
        String s1 = cookiespec.formatCookie(cookies[0]);
        assertEquals(s1, "$Version=\"1\"; name=\"value\"; $Path=\"/\"; $Domain=\".mydomain.com\"");

        header = new Header( "Set-Cookie", 
            "name=value; path=/; domain=.mydomain.com");
        cookies = cookiespec.parse("myhost.mydomain.com", 80, "/", false, header );
        cookiespec.validate("myhost.mydomain.com", 80, "/", false, cookies[0]);
        String s2 = cookiespec.formatCookie(cookies[0]);
        assertEquals(s2, "$Version=0; name=value; $Path=/; $Domain=.mydomain.com");
    }

    public void testRFC2109CookiesFormatting() throws Exception {
        CookieSpec cookiespec = new RFC2109Spec();
        Header header = new Header("Set-Cookie", 
            "name1=value1; path=/; domain=.mydomain.com, " + 
            "name2=\"value2\"; version=\"1\"; path=\"/\"; domain=\".mydomain.com\"");
        Cookie[] cookies = cookieParse(cookiespec, "myhost.mydomain.com", 80, "/", false, header);
        assertNotNull(cookies);
        assertEquals(2, cookies.length);
        String s1 = cookiespec.formatCookies(cookies);
        assertEquals(s1, 
            "$Version=0; name1=value1; $Path=/; $Domain=.mydomain.com; " + 
            "name2=value2; $Path=/; $Domain=.mydomain.com");

        header = new Header("Set-Cookie", 
            "name1=value1; version=1; path=/; domain=.mydomain.com, " + 
            "name2=\"value2\"; version=\"1\"; path=\"/\"; domain=\".mydomain.com\"");
        cookies = cookieParse(cookiespec, "myhost.mydomain.com", 80, "/", false, header);
        assertNotNull(cookies);
        assertEquals(2, cookies.length);
        String s2 = cookiespec.formatCookies(cookies);
        assertEquals(s2, 
            "$Version=\"1\"; name1=\"value1\"; $Path=\"/\"; $Domain=\".mydomain.com\"; " + 
            "name2=\"value2\"; $Path=\"/\"; $Domain=\".mydomain.com\"");
    }
    
    /**
     * Tests if null cookie values are handled correctly.
     */
    public void testNullCookieValueFormatting() {
        Cookie cookie = new Cookie(".whatever.com", "name", null, "/", null, false); 
        cookie.setDomainAttributeSpecified(true);
        cookie.setPathAttributeSpecified(true);

        CookieSpec cookiespec = new RFC2109Spec();
        String s = cookiespec.formatCookie(cookie);
        assertEquals("$Version=0; name=; $Path=/; $Domain=.whatever.com", s);

        cookie.setVersion(1);
        s = cookiespec.formatCookie(cookie);
        assertEquals("$Version=\"1\"; name=\"\"; $Path=\"/\"; $Domain=\".whatever.com\"", s);
    }

    public void testCookieNullDomainNullPathFormatting() {
        Cookie cookie = new Cookie(null, "name", null, "/", null, false); 
        cookie.setDomainAttributeSpecified(true);
        cookie.setPathAttributeSpecified(true);

        CookieSpec cookiespec = new RFC2109Spec();
        String s = cookiespec.formatCookie(cookie);
        assertEquals("$Version=0; name=; $Path=/", s);

        cookie.setDomainAttributeSpecified(false);
        cookie.setPathAttributeSpecified(false);
        s = cookiespec.formatCookie(cookie);
        assertEquals("$Version=0; name=", s);
    }

}

