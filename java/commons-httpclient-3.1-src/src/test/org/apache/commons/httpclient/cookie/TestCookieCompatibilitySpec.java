/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/cookie/TestCookieCompatibilitySpec.java,v 1.7 2004/09/14 20:11:32 olegk Exp $
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

import java.util.Collection;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.params.DefaultHttpParamsFactory;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.params.HttpParams;


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
public class TestCookieCompatibilitySpec extends TestCookieBase {


    // ------------------------------------------------------------ Constructor


    public TestCookieCompatibilitySpec(String name) {
        super(name);
    }


    // ------------------------------------------------------- TestCase Methods


    public static Test suite() {
        return new TestSuite(TestCookieCompatibilitySpec.class);
    }

    public void testParseAttributeInvalidAttrib() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        try {
            cookiespec.parseAttribute(null, null);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testParseAttributeInvalidCookie() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        try {
            cookiespec.parseAttribute(new NameValuePair("name", "value"), null);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testParseAttributeNullPath() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie cookie = new Cookie();
        cookiespec.parseAttribute(new NameValuePair("path", null), cookie);
        assertEquals("/", cookie.getPath());
    }

    public void testParseAttributeBlankPath() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie cookie = new Cookie();
        cookiespec.parseAttribute(new NameValuePair("path", "   "), cookie);
        assertEquals("/", cookie.getPath());
    }

    public void testParseAttributeNullDomain() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie cookie = new Cookie();
        try {
            cookiespec.parseAttribute(new NameValuePair("domain", null), cookie);
            fail("MalformedCookieException must have been thrown");
        } catch (MalformedCookieException expected) {
        }
    }

    public void testParseAttributeBlankDomain() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie cookie = new Cookie();
        try {
            cookiespec.parseAttribute(new NameValuePair("domain", "   "), cookie);
            fail("MalformedCookieException must have been thrown");
        } catch (MalformedCookieException expected) {
        }
    }

    public void testParseAttributeNullMaxAge() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie cookie = new Cookie();
        try {
            cookiespec.parseAttribute(new NameValuePair("max-age", null), cookie);
            fail("MalformedCookieException must have been thrown");
        } catch (MalformedCookieException expected) {
        }
    }

    public void testParseAttributeInvalidMaxAge() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie cookie = new Cookie();
        try {
            cookiespec.parseAttribute(new NameValuePair("max-age", "crap"), cookie);
            fail("MalformedCookieException must have been thrown");
        } catch (MalformedCookieException expected) {
        }
    }

    public void testParseAttributeNullExpires() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie cookie = new Cookie();
        try {
            cookiespec.parseAttribute(new NameValuePair("expires", null), cookie);
            fail("MalformedCookieException must have been thrown");
        } catch (MalformedCookieException expected) {
        }
    }

    public void testParseAttributeUnknownValue() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie cookie = new Cookie();
        cookiespec.parseAttribute(new NameValuePair("nonsense", null), cookie);
    }
    
    public void testValidateNullHost() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie cookie = new Cookie();
        try {
            cookiespec.validate(null, 80, "/", false, cookie);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testValidateBlankHost() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie cookie = new Cookie();
        try {
            cookiespec.validate("   ", 80, "/", false, cookie);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testValidateNullPath() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie cookie = new Cookie();
        try {
            cookiespec.validate("host", 80, null, false, cookie);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testValidateBlankPath() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie cookie = new Cookie("host", "name", "value", "/", null, false);
        cookiespec.validate("host", 80, "   ", false, cookie);
    }

    public void testValidateInvalidPort() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie cookie = new Cookie();
        try {
            cookiespec.validate("host", -80, "/", false, cookie);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testValidateInvalidCookieVersion() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie cookie = new Cookie();
        cookie.setVersion(-1);
        try {
            cookiespec.validate("host", 80, "/", false, cookie);
            fail("MalformedCookieException must have been thrown");
        } catch (MalformedCookieException expected) {
        }
    }

    /**
     * Tests whether domain attribute check is case-insensitive.
     */
    public void testDomainCaseInsensitivity() throws Exception {
        Header header = new Header("Set-Cookie", 
            "name=value; path=/; domain=.whatever.com");

        CookieSpec cookiespec = new CookieSpecBase();
        Cookie[] parsed = cookieParse(cookiespec, "www.WhatEver.com", 80, "/", false, header);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
        assertEquals(".whatever.com", parsed[0].getDomain());
    }
    
    /**
     * Test basic parse (with various spacings
     */
    public void testParse1() throws Exception {
        String headerValue = "custno = 12345; comment=test; version=1," +
            " name=John; version=1; max-age=600; secure; domain=.apache.org";

        Header header = new Header("set-cookie", headerValue);

        CookieSpec cookiespec = new CookieSpecBase();
        Cookie[] cookies = cookieParse(cookiespec, "www.apache.org", 80, "/", false, header);
        assertEquals(2, cookies.length);

        assertEquals("custno", cookies[0].getName());
        assertEquals("12345", cookies[0].getValue());
        assertEquals("test", cookies[0].getComment());
        assertEquals(0, cookies[0].getVersion());
        assertEquals("www.apache.org", cookies[0].getDomain());
        assertEquals("/", cookies[0].getPath());
        assertFalse(cookies[0].getSecure());

        assertEquals("name", cookies[1].getName());
        assertEquals("John", cookies[1].getValue());
        assertEquals(null, cookies[1].getComment());
        assertEquals(0, cookies[1].getVersion());
        assertEquals(".apache.org", cookies[1].getDomain());
        assertEquals("/", cookies[1].getPath());
        assertTrue(cookies[1].getSecure());
    }


    /**
     * Test no spaces
     */
    public void testParse2() throws Exception {
        String headerValue = "custno=12345;comment=test; version=1," +
            "name=John;version=1;max-age=600;secure;domain=.apache.org";

        Header header = new Header("set-cookie", headerValue);

        CookieSpec cookiespec = new CookieSpecBase();
        Cookie[] cookies = cookieParse(cookiespec, "www.apache.org", 80, "/", false, header);

        assertEquals(2, cookies.length);

        assertEquals("custno", cookies[0].getName());
        assertEquals("12345", cookies[0].getValue());
        assertEquals("test", cookies[0].getComment());
        assertEquals(0, cookies[0].getVersion());
        assertEquals("www.apache.org", cookies[0].getDomain());
        assertEquals("/", cookies[0].getPath());
        assertFalse(cookies[0].getSecure());

        assertEquals("name", cookies[1].getName());
        assertEquals("John", cookies[1].getValue());
        assertEquals(null, cookies[1].getComment());
        assertEquals(0, cookies[1].getVersion());
        assertEquals(".apache.org", cookies[1].getDomain());
        assertEquals("/", cookies[1].getPath());
        assertTrue(cookies[1].getSecure());
    }


    /**
     * Test parse with quoted text
     */
    public void testParse3() throws Exception {
        String headerValue =
            "name=\"Doe, John\";version=1;max-age=600;secure;domain=.apache.org";
        Header header = new Header("set-cookie", headerValue);

        CookieSpec cookiespec = new CookieSpecBase();
        Cookie[] cookies = cookieParse(cookiespec, "www.apache.org", 80, "/", false, header);

        assertEquals(1, cookies.length);

        assertEquals("name", cookies[0].getName());
        assertEquals("Doe, John", cookies[0].getValue());
        assertEquals(null, cookies[0].getComment());
        assertEquals(0, cookies[0].getVersion());
        assertEquals(".apache.org", cookies[0].getDomain());
        assertEquals("/", cookies[0].getPath());
        assertTrue(cookies[0].getSecure());
    }


    // see issue #5279
    public void testQuotedExpiresAttribute() throws Exception {
        String headerValue = "custno=12345;Expires='Thu, 01-Jan-2070 00:00:10 GMT'";

        Header header = new Header("set-cookie", headerValue);

        CookieSpec cookiespec = new CookieSpecBase();
        Cookie[] cookies = cookieParse(cookiespec, "www.apache.org", 80, "/", true, header);
        assertNotNull("Expected some cookies",cookies);
        assertEquals("Expected 1 cookie",1,cookies.length);
        assertNotNull("Expected cookie to have getExpiryDate",cookies[0].getExpiryDate());
    }

    public void testSecurityError() throws Exception {
        String headerValue = "custno=12345;comment=test; version=1," +
            "name=John;version=1;max-age=600;secure;domain=jakarta.apache.org";
        Header header = new Header("set-cookie", headerValue);

        CookieSpec cookiespec = new CookieSpecBase();
        try {
            Cookie[] cookies = cookieParse(cookiespec, "www.apache.org", 80, "/", false, header);
            fail("HttpException exception should have been thrown");
        } catch (HttpException e) {
            // expected
        }
    }

    public void testParseSimple() throws Exception {
        Header header = new Header("Set-Cookie","cookie-name=cookie-value");
        
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie[] parsed = cookieParse(cookiespec, "127.0.0.1", 80, "/path/path", false, header);
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertEquals("Value","cookie-value",parsed[0].getValue());
        assertTrue("Comment",null == parsed[0].getComment());
        assertTrue("ExpiryDate",null == parsed[0].getExpiryDate());
        //assertTrue("isToBeDiscarded",parsed[0].isToBeDiscarded());
        assertTrue("isPersistent",!parsed[0].isPersistent());
        assertEquals("Domain","127.0.0.1",parsed[0].getDomain());
        assertEquals("Path","/path",parsed[0].getPath());
        assertTrue("Secure",!parsed[0].getSecure());
        assertEquals("Version",0,parsed[0].getVersion());
    }
 
    public void testParseSimple2() throws Exception {
        Header header = new Header("Set-Cookie", "cookie-name=cookie-value");
    
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie[] parsed = cookieParse(cookiespec, "127.0.0.1", 80, "/path", false, header);
        assertEquals("Found 1 cookie.", 1, parsed.length);
        assertEquals("Name", "cookie-name", parsed[0].getName());
        assertEquals("Value", "cookie-value", parsed[0].getValue());
        assertTrue("Comment", null == parsed[0].getComment());
        assertTrue("ExpiryDate", null == parsed[0].getExpiryDate());
        //assertTrue("isToBeDiscarded",parsed[0].isToBeDiscarded());
        assertTrue("isPersistent", !parsed[0].isPersistent());
        assertEquals("Domain", "127.0.0.1", parsed[0].getDomain());
        assertEquals("Path", "/", parsed[0].getPath());
        assertTrue("Secure", !parsed[0].getSecure());
        assertEquals("Version", 0, parsed[0].getVersion());
    }
 
    public void testParseNoName() throws Exception {
        Header header = new Header("Set-Cookie","=stuff; path=/");

        CookieSpec cookiespec = new CookieSpecBase();
        try {
            Cookie[] parsed = cookieParse(cookiespec, "127.0.0.1", 80, "/", false, header);
            fail("MalformedCookieException should have been thrown");
        } catch (MalformedCookieException ex) {
            // expected
        }
    }
 
    public void testParseNoValue() throws Exception {
        Header header = new Header("Set-Cookie","cookie-name=");

        CookieSpec cookiespec = new CookieSpecBase();
        Cookie[] parsed = cookieParse(cookiespec, "127.0.0.1", 80, "/", false, header);
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertEquals("Value", "", parsed[0].getValue());
        assertTrue("Comment",null == parsed[0].getComment());
        assertTrue("ExpiryDate",null == parsed[0].getExpiryDate());
        //assertTrue("isToBeDiscarded",parsed[0].isToBeDiscarded());
        assertTrue("isPersistent",!parsed[0].isPersistent());
        assertEquals("Domain","127.0.0.1",parsed[0].getDomain());
        assertEquals("Path","/",parsed[0].getPath());
        assertTrue("Secure",!parsed[0].getSecure());
        assertEquals("Version",0,parsed[0].getVersion());
    }

    public void testParseWithWhiteSpace() throws Exception {
        Header header = new Header("Set-Cookie"," cookie-name  =    cookie-value  ");

        CookieSpec cookiespec = new CookieSpecBase();
        Cookie[] parsed = cookieParse(cookiespec, "127.0.0.1", 80, "/", false, header);
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertEquals("Value","cookie-value",parsed[0].getValue());
        assertEquals("Domain","127.0.0.1",parsed[0].getDomain());
        assertEquals("Path","/",parsed[0].getPath());
        assertTrue("Secure",!parsed[0].getSecure());
        assertTrue("ExpiryDate",null == parsed[0].getExpiryDate());
        assertTrue("Comment",null == parsed[0].getComment());
    }

    public void testParseWithQuotes() throws Exception {
        Header header = new Header("Set-Cookie"," cookie-name  =  \" cookie-value \" ;path=/");

        CookieSpec cookiespec = new CookieSpecBase();
        Cookie[] parsed = cookieParse(cookiespec, "127.0.0.1",80, "/", false, header);
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertEquals("Value"," cookie-value ",parsed[0].getValue());
        assertEquals("Domain","127.0.0.1",parsed[0].getDomain());
        assertEquals("Path","/",parsed[0].getPath());
        assertTrue("Secure",!parsed[0].getSecure());
        assertTrue("ExpiryDate",null == parsed[0].getExpiryDate());
        assertTrue("Comment",null == parsed[0].getComment());
    }

    public void testParseWithPath() throws Exception {
        Header header = new Header("Set-Cookie","cookie-name=cookie-value; Path=/path/");

        CookieSpec cookiespec = new CookieSpecBase();
        Cookie[] parsed = cookieParse(cookiespec, "127.0.0.1",80, "/path/path", false, header);
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertEquals("Value","cookie-value",parsed[0].getValue());
        assertEquals("Domain","127.0.0.1",parsed[0].getDomain());
        assertEquals("Path","/path/",parsed[0].getPath());
        assertTrue("Secure",!parsed[0].getSecure());
        assertTrue("ExpiryDate",null == parsed[0].getExpiryDate());
        assertTrue("Comment",null == parsed[0].getComment());
    }

    public void testParseWithDomain() throws Exception {
        Header header = new Header("Set-Cookie","cookie-name=cookie-value; Domain=127.0.0.1");

        CookieSpec cookiespec = new CookieSpecBase();
        Cookie[] parsed = cookieParse(cookiespec, "127.0.0.1", 80, "/", false, header);
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertEquals("Value","cookie-value",parsed[0].getValue());
        assertEquals("Domain","127.0.0.1",parsed[0].getDomain());
        assertEquals("Path","/",parsed[0].getPath());
        assertTrue("Secure",!parsed[0].getSecure());
        assertTrue("ExpiryDate",null == parsed[0].getExpiryDate());
        assertTrue("Comment",null == parsed[0].getComment());
    }

    public void testParseWithSecure() throws Exception {
        Header header = new Header("Set-Cookie","cookie-name=cookie-value; secure");

        CookieSpec cookiespec = new CookieSpecBase();
        Cookie[] parsed = cookieParse(cookiespec, "127.0.0.1", 80, "/", true, header);
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertEquals("Value","cookie-value",parsed[0].getValue());
        assertEquals("Domain","127.0.0.1",parsed[0].getDomain());
        assertEquals("Path","/",parsed[0].getPath());
        assertTrue("Secure",parsed[0].getSecure());
        assertTrue("ExpiryDate",null == parsed[0].getExpiryDate());
        assertTrue("Comment",null == parsed[0].getComment());
    }

    public void testParseWithComment() throws Exception {
        Header header = new Header("Set-Cookie",
            "cookie-name=cookie-value; comment=\"This is a comment.\"");

        CookieSpec cookiespec = new CookieSpecBase();
        Cookie[] parsed = cookieParse(cookiespec, "127.0.0.1", 80, "/", true, header);
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertEquals("Value","cookie-value",parsed[0].getValue());
        assertEquals("Domain","127.0.0.1",parsed[0].getDomain());
        assertEquals("Path","/",parsed[0].getPath());
        assertTrue("Secure",!parsed[0].getSecure());
        assertTrue("ExpiryDate",null == parsed[0].getExpiryDate());
        assertEquals("Comment","This is a comment.",parsed[0].getComment());
    }

    public void testParseWithExpires() throws Exception {
        Header header = new Header("Set-Cookie",
            "cookie-name=cookie-value;Expires=Thu, 01-Jan-1970 00:00:10 GMT");

        CookieSpec cookiespec = new CookieSpecBase();
        Cookie[] parsed = cookieParse(cookiespec, "127.0.0.1", 80, "/", true, header);
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertEquals("Value","cookie-value",parsed[0].getValue());
        assertEquals("Domain","127.0.0.1",parsed[0].getDomain());
        assertEquals("Path","/",parsed[0].getPath());
        assertTrue("Secure",!parsed[0].getSecure());
        assertEquals(new Date(10000L),parsed[0].getExpiryDate());
        assertTrue("Comment",null == parsed[0].getComment());
    }

    public void testParseWithAll() throws Exception {
        Header header = new Header("Set-Cookie",
            "cookie-name=cookie-value;Version=1;Path=/commons;Domain=.apache.org;" + 
            "Comment=This is a comment.;secure;Expires=Thu, 01-Jan-1970 00:00:10 GMT");

        CookieSpec cookiespec = new CookieSpecBase();
        Cookie[] parsed = cookieParse(cookiespec, ".apache.org", 80, "/commons/httpclient", true, header);
        assertEquals("Found 1 cookie.",1,parsed.length);
        assertEquals("Name","cookie-name",parsed[0].getName());
        assertEquals("Value","cookie-value",parsed[0].getValue());
        assertEquals("Domain",".apache.org",parsed[0].getDomain());
        assertEquals("Path","/commons",parsed[0].getPath());
        assertTrue("Secure",parsed[0].getSecure());
        assertEquals(new Date(10000L),parsed[0].getExpiryDate());
        assertEquals("Comment","This is a comment.",parsed[0].getComment());
        assertEquals("Version",0,parsed[0].getVersion());
    }

    public void testParseMultipleDifferentPaths() throws Exception {
        Header header = new Header("Set-Cookie",
            "name1=value1;Version=1;Path=/commons,name1=value2;Version=1;" +
            "Path=/commons/httpclient;Version=1");

        CookieSpec cookiespec = new CookieSpecBase();
        Cookie[] parsed = cookieParse(cookiespec, ".apache.org", 80, "/commons/httpclient", true, header);
        HttpState state = new HttpState();
        state.addCookies(parsed);
        Cookie[] cookies = state.getCookies();
        assertEquals("Wrong number of cookies.",2,cookies.length);
        assertEquals("Name","name1",cookies[0].getName());
        assertEquals("Value","value1",cookies[0].getValue());
        assertEquals("Name","name1",cookies[1].getName());
        assertEquals("Value","value2",cookies[1].getValue());
    }

    public void testParseMultipleSamePaths() throws Exception {
        Header header = new Header("Set-Cookie",
            "name1=value1;Version=1;Path=/commons,name1=value2;Version=1;Path=/commons");

        CookieSpec cookiespec = new CookieSpecBase();
        Cookie[] parsed = cookieParse(cookiespec, ".apache.org", 80, "/commons/httpclient", true, header);
        HttpState state = new HttpState();
        state.addCookies(parsed);
        Cookie[] cookies = state.getCookies();
        assertEquals("Found 1 cookies.",1,cookies.length);
        assertEquals("Name","name1",cookies[0].getName());
        assertEquals("Value","value2",cookies[0].getValue());
    }

    public void testParseRelativePath() throws Exception {
        Header header = new Header("Set-Cookie", "name1=value1;Path=whatever");

        CookieSpec cookiespec = new CookieSpecBase();
        Cookie[] parsed = cookieParse(cookiespec, ".apache.org", 80, "whatever", true, header);
        assertEquals("Found 1 cookies.",1,parsed.length);
        assertEquals("Name","name1",parsed[0].getName());
        assertEquals("Value","value1",parsed[0].getValue());
        assertEquals("Path","whatever",parsed[0].getPath());
    }

    public void testParseWithWrongDomain() throws Exception {
        Header header = new Header("Set-Cookie",
            "cookie-name=cookie-value; domain=127.0.0.1; version=1");

        CookieSpec cookiespec = new CookieSpecBase();
        try {
            Cookie[] parsed = cookieParse(cookiespec, "127.0.0.2", 80, "/", false, header);
            fail("HttpException exception should have been thrown");
        } catch (HttpException e) {
            // expected
        }
    }

    public void testParseWithNullHost() throws Exception {
        Header header = new Header("Set-Cookie",
            "cookie-name=cookie-value; domain=127.0.0.1; path=/; secure");

        CookieSpec cookiespec = new CookieSpecBase();
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

        CookieSpec cookiespec = new CookieSpecBase();
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

        CookieSpec cookiespec = new CookieSpecBase();
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

        CookieSpec cookiespec = new CookieSpecBase();
        Cookie[] parsed = cookieParse(cookiespec, "127.0.0.1", 80, "  ", false, header);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
        assertEquals("/", parsed[0].getPath());
    }

    public void testParseWithNegativePort() throws Exception {
        Header header = new Header("Set-Cookie",
            "cookie-name=cookie-value; domain=127.0.0.1; path=/; secure");

        CookieSpec cookiespec = new CookieSpecBase();
        try {
            Cookie[] parsed = cookieParse(cookiespec, "127.0.0.1", -80, null, false, header);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testParseWithNullHostAndPath() throws Exception {
        Header header = new Header("Set-Cookie",
            "cookie-name=cookie-value; domain=127.0.0.1; path=/; secure");

        CookieSpec cookiespec = new CookieSpecBase();
        try {
            Cookie[] parsed = cookieParse(cookiespec, null, 80, null, false, header);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testParseWithPathMismatch() throws Exception {
        Header header = new Header("Set-Cookie",
            "cookie-name=cookie-value; path=/path/path/path");

        CookieSpec cookiespec = new CookieSpecBase();
        try {
            Cookie[] parsed = cookieParse(cookiespec, "127.0.0.1", 80, "/path", false, header);
            fail("MalformedCookieException should have been thrown.");
        } catch (MalformedCookieException e) {
            // expected
        }
    }
    
    public void testParseWithPathMismatch2() throws Exception {
        Header header = new Header("Set-Cookie",
            "cookie-name=cookie-value; path=/foobar");

        CookieSpec cookiespec = new CookieSpecBase();
        try {
            Cookie[] parsed = cookieParse(cookiespec, "127.0.0.1", 80, "/foo", false, header);
            fail("MalformedCookieException should have been thrown.");
        } catch (MalformedCookieException e) {
            // expected
        }
    }


    public void testParseWithInvalidHeader1() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        try {
            Cookie[] parsed = cookiespec.parse("127.0.0.1", 80, "/foo", false, (Header)null);
            fail("IllegalArgumentException should have been thrown.");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testParseWithInvalidHeader2() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        try {
            Cookie[] parsed = cookiespec.parse("127.0.0.1", 80, "/foo", false, (String)null);
            fail("IllegalArgumentException should have been thrown.");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * Tests if cookie constructor rejects cookie name containing blanks.
     */
    public void testCookieNameWithBlanks() throws Exception {
        Header setcookie = new Header("Set-Cookie", "invalid name=");
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie[] parsed = cookieParse(cookiespec, "127.0.0.1", 80, "/", false, setcookie);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
    }


    /**
     * Tests if cookie constructor rejects cookie name starting with $.
     */
    public void testCookieNameStartingWithDollarSign() throws Exception {
        Header setcookie = new Header("Set-Cookie", "$invalid_name=");
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie[] parsed = cookieParse(cookiespec, "127.0.0.1", 80, "/", false, setcookie);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
    }


    /**
     * Tests if malformatted expires attribute is parsed correctly.
     */
    public void testCookieWithComma() throws Exception {
        Header header = new Header("Set-Cookie", "name=value; expires=\"Thu, 01-Jan-1970 00:00:00 GMT");

        CookieSpec cookiespec = new CookieSpecBase();
        try {
            Cookie[] cookies = cookiespec.parse("localhost", 80, "/", false, header);
            fail("MalformedCookieException should have been thrown");
        } catch (MalformedCookieException expected) {
        }
    }
    

    /**
     * Tests several date formats.
     */
    public void testDateFormats() throws Exception {
        //comma, dashes
        checkDate("Thu, 01-Jan-70 00:00:10 GMT");
        checkDate("Thu, 01-Jan-2070 00:00:10 GMT");
        //no comma, dashes
        checkDate("Thu 01-Jan-70 00:00:10 GMT");
        checkDate("Thu 01-Jan-2070 00:00:10 GMT");
        //comma, spaces
        checkDate("Thu, 01 Jan 70 00:00:10 GMT");
        checkDate("Thu, 01 Jan 2070 00:00:10 GMT");
        //no comma, spaces
        checkDate("Thu 01 Jan 70 00:00:10 GMT");
        checkDate("Thu 01 Jan 2070 00:00:10 GMT");
        //weird stuff
        checkDate("Wed, 20-Nov-2002 09-38-33 GMT");


        try {
            checkDate("this aint a date");
            fail("Date check is bogous");
        } catch(Exception e) {
            /* must fail */
        }
    }

    private void checkDate(String date) throws Exception {
        Header header = new Header("Set-Cookie", "custno=12345;Expires='"+date+"';");
        HttpParams params = new DefaultHttpParamsFactory().getDefaultParams();
        CookieSpec cookiespec = new CookieSpecBase();
        cookiespec.setValidDateFormats(
                (Collection)params.getParameter(HttpMethodParams.DATE_PATTERNS));
        cookieParse(cookiespec, "localhost", 80, "/", false, header);
    }

    /**
     * Tests if invalid second domain level cookie gets accepted in the
     * browser compatibility mode.
     */
    public void testSecondDomainLevelCookie() throws Exception {
        Cookie cookie = new Cookie(".sourceforge.net", "name", null, "/", null, false); 
        cookie.setDomainAttributeSpecified(true);
        cookie.setPathAttributeSpecified(true);

        CookieSpec cookiespec = new CookieSpecBase();
        cookiespec.validate("sourceforge.net", 80, "/", false, cookie);
    }

    public void testSecondDomainLevelCookieMatch1() throws Exception {
        Cookie cookie = new Cookie(".sourceforge.net", "name", null, "/", null, false); 
        cookie.setDomainAttributeSpecified(true);
        cookie.setPathAttributeSpecified(true);

        CookieSpec cookiespec = new CookieSpecBase();
        assertTrue(cookiespec.match("sourceforge.net", 80, "/", false, cookie));
    }

    public void testSecondDomainLevelCookieMatch2() throws Exception {
        Cookie cookie = new Cookie("sourceforge.net", "name", null, "/", null, false); 
        cookie.setDomainAttributeSpecified(true);
        cookie.setPathAttributeSpecified(true);

        CookieSpec cookiespec = new CookieSpecBase();
        assertTrue(cookiespec.match("www.sourceforge.net", 80, "/", false, cookie));
    }

    public void testSecondDomainLevelCookieMatch3() throws Exception {
        Cookie cookie = new Cookie(".sourceforge.net", "name", null, "/", null, false); 
         cookie.setDomainAttributeSpecified(true);
         cookie.setPathAttributeSpecified(true);

         CookieSpec cookiespec = new CookieSpecBase();
         assertTrue(cookiespec.match("www.sourceforge.net", 80, "/", false, cookie));
    }
         
    public void testInvalidSecondDomainLevelCookieMatch1() throws Exception {
        Cookie cookie = new Cookie(".sourceforge.net", "name", null, "/", null, false); 
        cookie.setDomainAttributeSpecified(true);
        cookie.setPathAttributeSpecified(true);

        CookieSpec cookiespec = new CookieSpecBase();
        assertFalse(cookiespec.match("antisourceforge.net", 80, "/", false, cookie));
    }

    public void testInvalidSecondDomainLevelCookieMatch2() throws Exception {
        Cookie cookie = new Cookie("sourceforge.net", "name", null, "/", null, false); 
        cookie.setDomainAttributeSpecified(true);
        cookie.setPathAttributeSpecified(true);

        CookieSpec cookiespec = new CookieSpecBase();
        assertFalse(cookiespec.match("antisourceforge.net", 80, "/", false, cookie));
    }

    public void testMatchNullHost() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie cookie = new Cookie();
        try {
            cookiespec.match(null, 80, "/", false, cookie);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testMatchBlankHost() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie cookie = new Cookie();
        try {
            cookiespec.match("   ", 80, "/", false, cookie);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testMatchInvalidPort() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie cookie = new Cookie();
        try {
            cookiespec.match("host", -80, "/", false, cookie);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testMatchNullPath() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie cookie = new Cookie();
        try {
            cookiespec.match("host", 80, null, false, cookie);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testMatchBlankPath() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie cookie = new Cookie("host", "name", "value", "/", null, false);
        assertTrue(cookiespec.match("host", 80, "  ", false, cookie));
    }

    public void testMatchNullCookie() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        try {
            cookiespec.match("host", 80, "/", false, (Cookie)null);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testMatchNullCookieDomain() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie cookie = new Cookie(null, "name", "value", "/", null, false);
        assertFalse(cookiespec.match("host", 80, "/", false, cookie));
    }

    public void testMatchNullCookiePath() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie cookie = new Cookie("host", "name", "value", null, null, false);
        assertFalse(cookiespec.match("host", 80, "/", false, cookie));
    }
    
    public void testCookieMatch1() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie cookie = new Cookie("host", "name", "value", "/", null, false);
        assertTrue(cookiespec.match("host", 80, "/", false, cookie));
    }
    
    public void testCookieMatch2() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie cookie = new Cookie(".whatever.com", "name", "value", "/", null, false);
        assertTrue(cookiespec.match(".whatever.com", 80, "/", false, cookie));
    }
    
    public void testCookieMatch3() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie cookie = new Cookie(".whatever.com", "name", "value", "/", null, false);
        assertTrue(cookiespec.match(".really.whatever.com", 80, "/", false, cookie));
    }
    
    public void testCookieMatch4() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie cookie = new Cookie("host", "name", "value", "/", null, false);
        assertTrue(cookiespec.match("host", 80, "/foobar", false, cookie));
    }
    
    public void testCookieMismatch1() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie cookie = new Cookie("host1", "name", "value", "/", null, false);
        assertFalse(cookiespec.match("host2", 80, "/", false, cookie));
    }
    
    public void testCookieMismatch2() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie cookie = new Cookie(".aaaaaaaaa.com", "name", "value", "/", null, false);
        assertFalse(cookiespec.match(".bbbbbbbb.com", 80, "/", false, cookie));
    }
    
    public void testCookieMismatch3() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie cookie = new Cookie("host", "name", "value", "/foobar", null, false);
        assertFalse(cookiespec.match("host", 80, "/foo", false, cookie));
    }
    
    public void testCookieMismatch4() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie cookie = new Cookie("host", "name", "value", "/foobar", null, true);
        assertFalse(cookiespec.match("host", 80, "/foobar/", false, cookie));
    }
    
    public void testCookieMatch5() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie cookie = new Cookie("host", "name", "value", "/foobar/r", null, false);
        assertFalse(cookiespec.match("host", 80, "/foobar/", false, cookie));
    }
    
    public void testCookieMismatch6() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie cookie = new Cookie("host", "name", "value", "/foobar", null, true);
        assertFalse(cookiespec.match("host", 80, "/foobar", false, cookie));
    }
    
    public void testMatchNullCookies() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie[] matched = cookiespec.match("host", 80, "/foobar", false, (Cookie[])null);
        assertNull(matched);
    }
    
    public void testMatchedCookiesOrder() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie[] cookies = {
            new Cookie("host", "nomatch", "value", "/noway", null, false),
            new Cookie("host", "name2", "value", "/foobar/yada", null, false),
            new Cookie("host", "name3", "value", "/foobar", null, false),
            new Cookie("host", "name1", "value", "/foobar/yada/yada", null, false)};
        Cookie[] matched = cookiespec.match("host", 80, "/foobar/yada/yada", false, cookies);
        assertNotNull(matched);
        assertEquals(3, matched.length);
        assertEquals("name1", matched[0].getName());
        assertEquals("name2", matched[1].getName());
        assertEquals("name3", matched[2].getName());
    }

    public void testInvalidMatchDomain() throws Exception {
        Cookie cookie = new Cookie("beta.gamma.com", "name", null, "/", null, false); 
        cookie.setDomainAttributeSpecified(true);
        cookie.setPathAttributeSpecified(true);

        CookieSpec cookiespec = new CookieSpecBase();
        cookiespec.validate("alpha.beta.gamma.com", 80, "/", false, cookie);
        assertTrue(cookiespec.match("alpha.beta.gamma.com", 80, "/", false, cookie));
    }

    public void testFormatInvalidCookie() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        try {
            String s = cookiespec.formatCookie(null);
            fail("IllegalArgumentException nust have been thrown");
        } catch (IllegalArgumentException expected) {
        }
    }    

    /**
     * Tests generic cookie formatting.
     */
    public void testGenericCookieFormatting() throws Exception {
        Header header = new Header("Set-Cookie", 
            "name=value; path=/; domain=.mydomain.com");
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie[] cookies = cookiespec.parse("myhost.mydomain.com", 80, "/", false, header);
        cookiespec.validate("myhost.mydomain.com", 80, "/", false, cookies[0]);
        String s = cookiespec.formatCookie(cookies[0]);
        assertEquals("name=value", s);
    }    

    public void testGenericCookieFormattingAsHeader() throws Exception {
        Header header = new Header("Set-Cookie", 
            "name=value; path=/; domain=.mydomain.com");
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie[] cookies = cookiespec.parse("myhost.mydomain.com", 80, "/", false, header);
        cookiespec.validate("myhost.mydomain.com", 80, "/", false, cookies[0]);
        Header cookieheader = cookiespec.formatCookieHeader(cookies[0]);
        assertEquals("name=value", cookieheader.getValue());
    }    

    /**
     * Tests if null cookie values are handled correctly.
     */
    public void testNullCookieValueFormatting() {
        Cookie cookie = new Cookie(".whatever.com", "name", null, "/", null, false); 
        cookie.setDomainAttributeSpecified(true);
        cookie.setPathAttributeSpecified(true);

        CookieSpec cookiespec = new CookieSpecBase();
        String s = cookiespec.formatCookie(cookie);
        assertEquals("name=", s);
    }

    public void testFormatInvalidCookies() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        try {
            String s = cookiespec.formatCookies(null);
            fail("IllegalArgumentException nust have been thrown");
        } catch (IllegalArgumentException expected) {
        }
    }    

    public void testFormatZeroCookies() throws Exception {
        CookieSpec cookiespec = new CookieSpecBase();
        try {
            String s = cookiespec.formatCookies(new Cookie[] {});
            fail("IllegalArgumentException nust have been thrown");
        } catch (IllegalArgumentException expected) {
        }
    }    

    /**
     * Tests generic cookie formatting.
     */
    public void testFormatSeveralCookies() throws Exception {
        Header header = new Header("Set-Cookie", 
            "name1=value1; path=/; domain=.mydomain.com, name2 = value2 ; path=/; domain=.mydomain.com");
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie[] cookies = cookiespec.parse("myhost.mydomain.com", 80, "/", false, header);
        String s = cookiespec.formatCookies(cookies);
        assertEquals("name1=value1; name2=value2", s);
    }    

    public void testFormatOneCookie() throws Exception {
        Header header = new Header("Set-Cookie", 
            "name1=value1; path=/; domain=.mydomain.com;");
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie[] cookies = cookiespec.parse("myhost.mydomain.com", 80, "/", false, header);
        String s = cookiespec.formatCookies(cookies);
        assertEquals("name1=value1", s);
    }    

    public void testFormatSeveralCookiesAsHeader() throws Exception {
        Header header = new Header("Set-Cookie", 
            "name1=value1; path=/; domain=.mydomain.com, name2 = value2 ; path=/; domain=.mydomain.com");
        CookieSpec cookiespec = new CookieSpecBase();
        Cookie[] cookies = cookiespec.parse("myhost.mydomain.com", 80, "/", false, header);
        Header cookieheader = cookiespec.formatCookieHeader(cookies);
        assertEquals("name1=value1; name2=value2", cookieheader.getValue());
    }    

    public void testKeepCloverHappy() throws Exception {
        MalformedCookieException ex1 = new MalformedCookieException(); 
        MalformedCookieException ex2 = new MalformedCookieException("whatever"); 
        MalformedCookieException ex3 = new MalformedCookieException("whatever", null); 
    }

}

