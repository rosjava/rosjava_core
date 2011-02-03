/*
 * $Header: $
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

import java.util.Date;

/**
 * Test cases for RFC2965 cookie spec
 *
 * @author jain.samit@gmail.com (Samit Jain)
 */
public class TestCookieRFC2965Spec extends TestCookieBase {

    // ------------------------------------------------------------ Constructor

    public TestCookieRFC2965Spec(String name) {
        super(name);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestCookieRFC2965Spec.class);
    }


    // ------------------------------------------------------- Test Cookie Parsing

    /**
     * Test <tt>parse</tt> with invalid params.
     */
    public void testParseInvalidParams() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        try {
            // invalid header
            cookiespec.parse("www.domain.com", 80, "/", false, (Header) null /* header */);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException expected) {}

        Header header = new Header("Set-Cookie2", "name=value;Version=1");
        try {
            // invalid request host
            cookiespec.parse(null /* host */, 80, "/", false, header);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException expected) {}
        try {
            // invalid request port
            cookiespec.parse("www.domain.com", -32 /* port */, "/", false, header);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException expected) {}
        try {
            // invalid request path
            cookiespec.parse("www.domain.com", 80, null /* path */, false, header);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException expected) {}
    }

    /**
     * Test parsing cookie <tt>"Path"</tt> attribute.
     */
    public void testParsePath() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        Header header = new Header("Set-Cookie2", "name=value;Path=/;Version=1;Path=");
        Cookie[] parsed = cookiespec.parse("www.domain.com", 80, "/", false, header);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
        // only the first occurence of path attribute is considered, others ignored
        Cookie2 cookie = (Cookie2) parsed[0];
        assertEquals("/", cookie.getPath());
        assertTrue(cookie.isPathAttributeSpecified());
    }

    public void testParsePathDefault() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        // Path is OPTIONAL, defaults to the request path
        Header header = new Header("Set-Cookie2", "name=value;Version=1");
        Cookie[] parsed = cookiespec.parse("www.domain.com", 80, "/path" /* request path */, false, header);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
        Cookie2 cookie = (Cookie2) parsed[0];
        assertEquals("/path", cookie.getPath());
        assertFalse(cookie.isPathAttributeSpecified());
    }

    public void testParseNullPath() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        Header header = new Header("Set-Cookie2", "name=value;Path=;Version=1");
        try {
            cookiespec.parse("www.domain.com", 80, "/", false, header);
            fail("MalformedCookieException should have been thrown");
        } catch (MalformedCookieException ex) {
            // expected
        }
    }

    public void testParseBlankPath() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        Header header = new Header("Set-Cookie2", "name=value;Path=\"   \";Version=1");
        try {
            cookiespec.parse("www.domain.com", 80, "/", false, header);
            fail("MalformedCookieException should have been thrown");
        } catch (MalformedCookieException ex) {
            // expected
        }
    }
    /**
     * Test parsing cookie <tt>"Domain"</tt> attribute.
     */
    public void testParseDomain() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        Header header = new Header("Set-Cookie2", "name=value;Domain=.domain.com;Version=1;Domain=");
        Cookie[] parsed = cookiespec.parse("www.domain.com", 80, "/", false, header);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
        // only the first occurence of domain attribute is considered, others ignored
        Cookie2 cookie = (Cookie2) parsed[0];
        assertEquals(".domain.com", cookie.getDomain());
        assertTrue(cookie.isDomainAttributeSpecified());

        // should put a leading dot if there is no dot in front of domain
        header = new Header("Set-Cookie2", "name=value;Domain=domain.com;Version=1");
        parsed = cookiespec.parse("www.domain.com", 80, "/", false, header);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
        cookie = (Cookie2) parsed[0];
        assertEquals(".domain.com", cookie.getDomain());
    }

    public void testParseDomainDefault() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        // Domain is OPTIONAL, defaults to the request host
        Header header = new Header("Set-Cookie2", "name=value;Version=1");
        Cookie[] parsed = cookiespec.parse("www.domain.com" /* request host */, 80, "/", false, header);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
        Cookie2 cookie = (Cookie2) parsed[0];
        assertEquals("www.domain.com", cookie.getDomain());
        assertFalse(cookie.isDomainAttributeSpecified());
    }

    public void testParseNullDomain() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        // domain cannot be null
        Header header = new Header("Set-Cookie2", "name=value;Domain=;Version=1");
        try {
            cookiespec.parse("www.domain.com", 80, "/", false, header);
            fail("MalformedCookieException should have been thrown");
        } catch (MalformedCookieException ex) {
            // expected
        }
    }

    public void testParseBlankDomain() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        Header header = new Header("Set-Cookie2", "name=value;Domain=\"   \";Version=1");
        try {
            cookiespec.parse("www.domain.com", 80, "/", false, header);
            fail("MalformedCookieException should have been thrown");
        } catch (MalformedCookieException ex) {
            // expected
        }
    }

    /**
     * Test parsing cookie <tt>"Port"</tt> attribute.
     */
    public void testParsePort() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        Header header = new Header("Set-Cookie2", "name=value;Port=\"80,800,8000\";Version=1;Port=nonsense");
        Cookie[] parsed = cookiespec.parse("www.domain.com", 80, "/", false, header);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
        // only the first occurence of port attribute is considered, others ignored
        Cookie2 cookie = (Cookie2) parsed[0];
        int[] ports = cookie.getPorts();
        assertNotNull(ports);
        assertEquals(3, ports.length);
        assertEquals(80, ports[0]);
        assertEquals(800, ports[1]);
        assertEquals(8000, ports[2]);
        assertTrue(cookie.isPortAttributeSpecified());
    }

    public void testParsePortDefault() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        // Port is OPTIONAL, cookie can be accepted from any port
        Header header = new Header("Set-Cookie2", "name=value;Version=1");
        Cookie[] parsed = cookiespec.parse("www.domain.com", 80, "/", false, header);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
        Cookie2 cookie = (Cookie2) parsed[0];
        assertFalse(cookie.isPortAttributeSpecified());
    }

    public void testParseNullPort() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        // null port defaults to request port
        Header header = new Header("Set-Cookie2", "name=value;Port=;Version=1");
        Cookie[] parsed = cookiespec.parse("www.domain.com", 80 /* request port */, "/", false, header);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
        Cookie2 cookie = (Cookie2) parsed[0];
        int[] ports = cookie.getPorts();
        assertNotNull(ports);
        assertEquals(1, ports.length);
        assertEquals(80, ports[0]);
        assertTrue(cookie.isPortAttributeSpecified() && cookie.isPortAttributeBlank());
    }

    public void testParseBlankPort() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        // blank port defaults to request port
        Header header = new Header("Set-Cookie2", "name=value;Port=\"  \";Version=1");
        Cookie[] parsed = cookiespec.parse("www.domain.com", 80 /* request port */, "/", false, header);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
        Cookie2 cookie = (Cookie2) parsed[0];
        int[] ports = cookie.getPorts();
        assertNotNull(ports);
        assertEquals(1, ports.length);
        assertEquals(80, ports[0]);
        assertTrue(cookie.isPortAttributeSpecified() && cookie.isPortAttributeBlank());
    }

    public void testParseInvalidPort() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        Header header = new Header("Set-Cookie2", "name=value;Port=nonsense;Version=1");
        try {
            cookiespec.parse("www.domain.com", 80, "/", false, header);
            fail("MalformedCookieException should have been thrown");
        } catch (MalformedCookieException ex) {
            // expected
        }
    }

    public void testParseNegativePort() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        Header header = new Header("Set-Cookie2", "name=value;Port=\"80,-800,8000\";Version=1");
        try {
            cookiespec.parse("www.domain.com", 80, "/", false, header);
            fail("MalformedCookieException should have been thrown");
        } catch (MalformedCookieException ex) {
            // expected
        }
    }

    /**
     * test parsing cookie name/value.
     */
    public void testParseNameValue() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        Header header = new Header("Set-Cookie2", "name=value;Version=1;");
        Cookie[] parsed = cookiespec.parse("www.domain.com", 80, "/", false, header);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
        Cookie2 cookie = (Cookie2) parsed[0];
        assertEquals("name", cookie.getName());
        assertEquals("value", cookie.getValue());
    }

    /**
     * test parsing cookie <tt>"Version"</tt> attribute.
     */
    public void testParseVersion() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        Header header = new Header("Set-Cookie2", "name=value;Version=1;");
        Cookie[] parsed = cookiespec.parse("www.domain.com", 80, "/", false, header);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
        Cookie2 cookie = (Cookie2) parsed[0];
        assertEquals(1, cookie.getVersion());
        assertTrue(cookie.isVersionAttributeSpecified());
    }

    public void testParseNullVersion() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        // version cannot ne null
        Header header = new Header("Set-Cookie2", "name=value;Version=;");
        try {
            cookiespec.parse("www.domain.com", 80, "/", false, header);
            fail("MalformedCookieException should have been thrown");
        } catch (MalformedCookieException ex) {
            // expected
        }
    }
    
    public void testParseNegativeVersion() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        Header header = new Header("Set-Cookie2", "name=value;Version=-1;");
        try {
            cookiespec.parse("www.domain.com", 80, "/", false, header);
            fail("MalformedCookieException should have been thrown");
        } catch (MalformedCookieException ex) {
            // expected
        }
    }
    /**
     * test parsing cookie <tt>"Max-age"</tt> attribute.
     */
    public void testParseMaxage() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        Header header = new Header("Set-Cookie2", "name=value;Max-age=3600;Version=1;Max-age=nonsense");
        Cookie[] parsed = cookiespec.parse("www.domain.com", 80, "/", false, header);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
        // only the first occurence of max-age attribute is considered, others ignored
        Cookie2 cookie = (Cookie2) parsed[0];
        assertFalse(cookie.isExpired());
    }

    public void testParseMaxageDefault() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        // Max-age is OPTIONAL, defaults to session cookie
        Header header = new Header("Set-Cookie2", "name=value;Version=1");
        Cookie[] parsed = cookiespec.parse("www.domain.com", 80, "/", false, header);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
        Cookie2 cookie = (Cookie2) parsed[0];
        assertFalse(cookie.isPersistent());
    }

    public void testParseNullMaxage() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        Header header = new Header("Set-Cookie2", "name=value;Max-age=;Version=1");
        try {
            cookiespec.parse("www.domain.com", 80, "/", false, header);
            fail("MalformedCookieException should have been thrown");
        } catch (MalformedCookieException ex) {
            // expected
        }
    }

    public void testParseNegativeMaxage() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        Header header = new Header("Set-Cookie2", "name=value;Max-age=-3600;Version=1;");
        try {
            cookiespec.parse("www.domain.com", 80, "/", false, header);
            fail("MalformedCookieException should have been thrown");
        } catch (MalformedCookieException ex) {
            // expected
        }
    }

    /**
     * test parsing <tt>"Secure"</tt> attribute.
     */
    public void testParseSecure() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        Header header = new Header("Set-Cookie2", "name=value;Secure;Version=1");
        Cookie[] parsed = cookiespec.parse("www.domain.com", 80, "/", false, header);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
        Cookie2 cookie = (Cookie2) parsed[0];
        assertTrue(cookie.getSecure());
    }

    /**
     * test parsing <tt>"Discard"</tt> attribute.
     */
    public void testParseDiscard() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        Header header = new Header("Set-Cookie2", "name=value;Discard;Max-age=36000;Version=1");
        Cookie[] parsed = cookiespec.parse("www.domain.com", 80, "/", false, header);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
        Cookie2 cookie = (Cookie2) parsed[0];
        // discard overrides max-age
        assertFalse(cookie.isPersistent());

        // Discard is OPTIONAL, default behavior is dictated by max-age
        header = new Header("Set-Cookie2", "name=value;Max-age=36000;Version=1");
        parsed = cookiespec.parse("www.domain.com", 80, "/", false, header);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
        cookie = (Cookie2) parsed[0];
        assertTrue(cookie.isPersistent());
    }

    /**
     * test parsing <tt>"Comment"</tt>, <tt>"CommentURL"</tt> and
     * <tt>"Secure"</tt> attributes.
     */
    public void testParseOtherAttributes() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        Header header = new Header("Set-Cookie2", "name=value;Comment=\"good cookie\";" +
                "CommentURL=\"www.domain.com/goodcookie/\";Secure;Version=1");
        Cookie[] parsed = cookiespec.parse("www.domain.com", 80, "/", false, header);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
        Cookie2 cookie = (Cookie2) parsed[0];
        assertEquals("good cookie", cookie.getComment());
        assertEquals("www.domain.com/goodcookie/", cookie.getCommentURL());
        assertTrue(cookie.getSecure());

        // Comment, CommentURL, Secure are OPTIONAL
        header = new Header("Set-Cookie2", "name=value;Version=1");
        parsed = cookiespec.parse("www.domain.com", 80, "/", false, header);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
        cookie = (Cookie2) parsed[0];
        assertFalse(cookie.getSecure());
    }

    /**
     * Test parsing header with 2 cookies (separated by comma)
     */
    public void testCookiesWithComma() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        Header header = new Header("Set-Cookie2", "a=b,c");
        Cookie[] parsed = cookiespec.parse("www.domain.com", 80, "/", false, header);
        assertNotNull(parsed);
        assertEquals(2, parsed.length);
        assertEquals("a", parsed[0].getName());
        assertEquals("b", parsed[0].getValue());
        assertEquals("c", parsed[1].getName());
        assertEquals(null, parsed[1].getValue());
    }

    // ------------------------------------------------------- Test Cookie Validation

    /**
     * Test <tt>Domain</tt> validation when domain is not specified
     * in <tt>Set-Cookie2</tt> header.
     */
    public void testValidateNoDomain() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        Header header = new Header("Set-Cookie2", "name=value;Version=1");
        Cookie[] parsed = cookieParse(cookiespec, "www.domain.com" /* request host */, 80, "/", false, header);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
        Cookie2 cookie = (Cookie2) parsed[0];
        // cookie domain must string match request host
        assertEquals("www.domain.com", cookie.getDomain());
    }

    /**
     * Test <tt>Domain</tt> validation. Cookie domain attribute must have a
     * leading dot.
     */
    public void testValidateDomainLeadingDot() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        Header header = new Header("Set-Cookie2", "name=value;Domain=domain.com;Version=1");
        Cookie[] parsed = cookieParse(cookiespec, "www.domain.com", 80, "/", false, header);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
        Cookie2 cookie = (Cookie2) parsed[0];
        assertEquals(".domain.com", cookie.getDomain());
    }

    /**
     * Test <tt>Domain</tt> validation. Domain must have atleast one embedded dot.
     */
    public void testValidateDomainEmbeddedDot() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        Header header = new Header("Set-Cookie2", "name=value; domain=.com; version=1");
        try {
            cookieParse(cookiespec, "b.com", 80, "/", false, header);
            fail("MalformedCookieException should have been thrown");
        } catch (MalformedCookieException expected) {}

        header = new Header("Set-Cookie2", "name=value;Domain=domain.com;Version=1");
        Cookie[] parsed = cookieParse(cookiespec, "www.domain.com", 80, "/", false, header);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
    }

    /**
     * Test local <tt>Domain</tt> validation. Simple host names
     * (without any dots) are valid only when cookie domain is specified
     * as ".local".
     */
    public void testValidateDomainLocal() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        // when domain is specified as .local, simple host names are valid
        Header header = new Header("Set-Cookie2", "name=value; domain=.local; version=1");
        Cookie[] parsed = cookieParse(cookiespec, "simplehost" /* request host */, 80, "/", false, header);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
        Cookie2 cookie = (Cookie2) parsed[0];
        assertEquals(".local", cookie.getDomain());

        // when domain is NOT specified as .local, simple host names are invalid
        header = new Header("Set-Cookie2", "name=value; domain=domain.com; version=1");
        try {
            // since domain is not .local, this must fail
            parsed = cookieParse(cookiespec, "simplehost" /* request host */, 80, "/", false, header);
            fail("MalformedCookieException should have been thrown");
        } catch (MalformedCookieException expected) {}
    }


    /**
     * Test <tt>Domain</tt> validation. Effective host name
     * must domain-match domain attribute.
     */
    public void testValidateDomainEffectiveHost() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();

        // cookie domain does not domain-match request host
        Header header = new Header("Set-Cookie2", "name=value; domain=.domain.com; version=1");
        try {
            cookieParse(cookiespec, "www.domain.org" /* request host */, 80, "/", false, header);
            fail("MalformedCookieException should have been thrown");
        } catch (MalformedCookieException expected) {}

        // cookie domain domain-matches request host
        header = new Header("Set-Cookie2", "name=value; domain=.domain.com; version=1");
        Cookie[] parsed = cookieParse(cookiespec, "www.domain.com" /* request host */, 80, "/", false, header);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
    }

    /**
     * Test local <tt>Domain</tt> validation.
     * Effective host name minus domain must not contain any dots.
     */
    public void testValidateDomainIllegal() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        Header header = new Header("Set-Cookie2", "name=value; domain=.domain.com; version=1");
        try {
            cookieParse(cookiespec, "a.b.domain.com" /* request host */, 80, "/", false, header);
            fail("MalformedCookieException should have been thrown");
        } catch (MalformedCookieException expected) {}
    }

    /**
     * Test cookie <tt>Path</tt> validation. Cookie path attribute must path-match
     * request path.
     */
    public void testValidatePath() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        Header header = new Header("Set-Cookie2", "name=value;path=/path;version=1");
        try {
            cookieParse(cookiespec, "www.domain.com", 80, "/" /* request path */, false, header);
            fail("MalformedCookieException exception should have been thrown");
        } catch (MalformedCookieException expected) {}

        // path-matching is case-sensitive
        header = new Header("Set-Cookie2", "name=value;path=/Path;version=1");
        try {
            cookieParse(cookiespec, "www.domain.com", 80, "/path" /* request path */, false, header);
            fail("MalformedCookieException exception should have been thrown");
        } catch (MalformedCookieException expected) {}

        header = new Header("Set-Cookie2", "name=value;path=/path;version=1");
        Cookie[] parsed = cookieParse(cookiespec, "www.domain.com",
                                      80, "/path/path1" /* request path */, false, header);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
        assertEquals("/path", parsed[0].getPath());
    }

    /**
     * Test cookie name validation.
     */
    public void testValidateCookieName() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        // cookie name must not contain blanks
        Header header = new Header("Set-Cookie2", "invalid name=value; version=1");
        try {
            cookieParse(cookiespec, "127.0.0.1", 80, "/", false, header);
            fail("MalformedCookieException exception should have been thrown");
        } catch (MalformedCookieException expected) {}

        // cookie name must not start with '$'.
        header = new Header("Set-Cookie2", "$invalid_name=value; version=1");
        try {
            cookieParse(cookiespec, "127.0.0.1", 80, "/", false, header);
            fail("MalformedCookieException exception should have been thrown");
        } catch (MalformedCookieException expected) {}

        // valid name
        header = new Header("Set-Cookie2", "name=value; version=1");
        Cookie[] parsed = cookieParse(cookiespec, "www.domain.com", 80, "/", false, header);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
        Cookie2 cookie = (Cookie2) parsed[0];
        assertEquals("name", cookie.getName());
        assertEquals("value", cookie.getValue());
    }

    /**
     * Test cookie <tt>Port</tt> validation. Request port must be in the
     * port attribute list.
     */
    public void testValidatePort() throws Exception {
        Header header = new Header("Set-Cookie2", "name=value; Port=\"80,800\"; version=1");
        CookieSpec cookiespec = new RFC2965Spec();
        try {
            cookieParse(cookiespec, "www.domain.com", 8000 /* request port */, "/", false, header);
            fail("MalformedCookieException should have been thrown");
        } catch (MalformedCookieException e) {}

        // valid port list
        Cookie[] parsed = cookieParse(cookiespec, "www.domain.com", 80 /* request port */, "/", false, header);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
        Cookie2 cookie = (Cookie2) parsed[0];
        int[] ports = cookie.getPorts();
        assertNotNull(ports);
        assertEquals(2, ports.length);
        assertEquals(80, ports[0]);
        assertEquals(800, ports[1]);
    }

    /**
     * Test cookie <tt>Version</tt> validation.
     */
    public void testValidateVersion() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        // version attribute is REQUIRED
        Header header = new Header("Set-Cookie2", "name=value");
        try {
            cookieParse(cookiespec, "www.domain.com", 8000, "/", false, header);
            fail("MalformedCookieException should have been thrown");
        } catch (MalformedCookieException e) {}
    }

    // ------------------------------------------------------- Test Cookie Matching

    /**
     * test cookie <tt>Path</tt> matching. Cookie path attribute must path-match
     * path of the request URI.
     */
    public void testMatchPath() throws Exception {
        Cookie2 cookie = new Cookie2(".domain.com", "name",
                                     "value", "/path" /* path */, null, false, new int[] {80});
        CookieSpec cookiespec = new RFC2965Spec();
        assertFalse(cookiespec.match("www.domain.com", 80, "/" /* request path */, false, cookie));
        assertTrue(cookiespec.match("www.domain.com", 80, "/path/path1" /* request path */, false, cookie));
    }

    /**
     * test cookie <tt>Domain</tt> matching.
     */
    public void testMatchDomain() throws Exception {
        Cookie2 cookie = new Cookie2(".domain.com" /* domain */, "name",
                                     "value", "/", null, false, new int[] {80});
        CookieSpec cookiespec = new RFC2965Spec();
        // effective host name minus domain must not contain any dots
        assertFalse(cookiespec.match("a.b.domain.com" /* request host */, 80, "/", false, cookie));
        // The effective host name MUST domain-match the Domain
        // attribute of the cookie.
        assertFalse(cookiespec.match("www.domain.org" /* request host */, 80, "/", false, cookie));
        assertTrue(cookiespec.match("www.domain.com" /* request host */, 80, "/", false, cookie));
    }

    /**
     * test cookie local <tt>Domain</tt> matching.
     */
    public void testMatchDomainLocal() throws Exception {
        Cookie2 cookie = new Cookie2(".local" /* domain */, "name",
                                     "value", "/", null, false, new int[] {80});
        CookieSpec cookiespec = new RFC2965Spec();
        assertTrue(cookiespec.match("host" /* request host */, 80, "/", false, cookie));
        assertFalse(cookiespec.match("host.com" /* request host */, 80, "/", false, cookie));
    }

    /**
     * test cookie <tt>Port</tt> matching.
     */
    public void testMatchPort() throws Exception {
        // cookie can be sent to any port if port attribute not specified
        Cookie2 cookie = new Cookie2(".domain.com", "name",
                                     "value", "/", null, false, null /* ports */);
        CookieSpec cookiespec = new RFC2965Spec();
        cookie.setPortAttributeSpecified(false);
        assertTrue(cookiespec.match("www.domain.com", 8080 /* request port */, "/", false, cookie));
        assertTrue(cookiespec.match("www.domain.com", 323  /* request port */, "/", false, cookie));

        // otherwise, request port must be in cookie's port list
        cookie = new Cookie2(".domain.com", "name",
                             "value", "/", null, false, new int[] {80, 8080} /* ports */);
        cookie.setPortAttributeSpecified(true);
        assertFalse(cookiespec.match("www.domain.com", 434 /* request port */, "/", false, cookie));
        assertTrue(cookiespec.match("www.domain.com", 8080 /* request port */, "/", false, cookie));
    }

    /**
     * test cookie expiration.
     */
    public void testCookieExpiration() throws Exception {
        Date afterOneHour = new Date(System.currentTimeMillis() + 3600 * 1000L);
        Cookie2 cookie = new Cookie2(".domain.com", "name",
                                     "value", "/", afterOneHour /* expiry */, false, null);
        CookieSpec cookiespec = new RFC2965Spec();
        assertTrue(cookiespec.match("www.domain.com", 80, "/", false, cookie));

        Date beforeOneHour = new Date(System.currentTimeMillis() - 3600 * 1000L);
        cookie = new Cookie2(".domain.com", "name",
                             "value", "/", beforeOneHour /* expiry */, false, null);
        assertFalse(cookiespec.match("www.domain.com", 80, "/", false, cookie));

        // discard attributes overrides cookie age, makes it a session cookie.
        cookie.setDiscard(true);
        assertFalse(cookie.isPersistent());
        assertTrue(cookiespec.match("www.domain.com", 80, "/", false, cookie));
    }

    /**
     * test cookie <tt>Secure</tt> attribute.
     */
    public void testCookieSecure() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        // secure cookie can only be sent over a secure connection
        Cookie2 cookie = new Cookie2(".domain.com", "name",
                                     "value", "/", null, true /* secure */, null);
        assertFalse(cookiespec.match("www.domain.com", 80, "/", false /* request secure */, cookie));
        assertTrue(cookiespec.match("www.domain.com", 80, "/", true /* request secure */, cookie));
    }

    // ------------------------------------------------------- Test Cookie Formatting

    public void testFormatInvalidCookie() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        try {
            cookiespec.formatCookie(null);
            fail("IllegalArgumentException nust have been thrown");
        } catch (IllegalArgumentException expected) {}
    }

    /**
     * Tests RFC 2965 compliant cookie formatting.
     */
    public void testRFC2965CookieFormatting() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        Cookie2 cookie1 = new Cookie2(".domain.com", "name1",
                                     "value", "/", null, false, new int[] {80,8080});
        cookie1.setVersion(1);
        // domain, path, port specified
        cookie1.setDomainAttributeSpecified(true);
        cookie1.setPathAttributeSpecified(true);
        cookie1.setPortAttributeSpecified(true);
        assertEquals("$Version=\"1\"; name1=\"value\"; $Domain=\".domain.com\"; $Path=\"/\"; $Port=\"80,8080\"",
                     cookiespec.formatCookie(cookie1));

        Cookie2 cookie2 = new Cookie2(".domain.com", "name2",
                "value", "/a/", null, false, new int[] {80,8080});
        cookie2.setVersion(2);
        // domain, path specified  but port unspecified
        cookie2.setDomainAttributeSpecified(true);
        cookie2.setPathAttributeSpecified(true);
        cookie2.setPortAttributeSpecified(false);
        assertEquals("$Version=\"2\"; name2=\"value\"; $Domain=\".domain.com\"; $Path=\"/a/\"",
                     cookiespec.formatCookie(cookie2));

        Cookie2 cookie3 = new Cookie2(".domain.com", "name3",
                "value", "/a/b/", null, false, new int[] {80,8080});
        cookie3.setVersion(1);
        // path specified, port specified but blank, domain unspecified
        cookie3.setDomainAttributeSpecified(false);
        cookie3.setPathAttributeSpecified(true);
        cookie3.setPortAttributeSpecified(true);
        cookie3.setPortAttributeBlank(true);
        assertEquals("$Version=\"1\"; name3=\"value\"; $Path=\"/a/b/\"; $Port=\"\"",
                     cookiespec.formatCookie(cookie3));

        assertEquals("$Version=\"2\"; " +
                "name3=\"value\"; $Path=\"/a/b/\"; $Port=\"\"; " +
                "name2=\"value\"; $Domain=\".domain.com\"; $Path=\"/a/\"; " +
                "name1=\"value\"; $Domain=\".domain.com\"; $Path=\"/\"; $Port=\"80,8080\"",
                cookiespec.formatCookies(new Cookie[] {cookie3, cookie2, cookie1}));
    }

    /**
     * Tests RFC 2965 compliant cookies formatting.
     */
    public void testRFC2965CookiesFormatting() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        Cookie2 cookie1 = new Cookie2(".domain.com", "name1",
                                      "value1", "/", null, false, new int[] {80,8080});
        cookie1.setVersion(1);
        // domain, path, port specified
        cookie1.setDomainAttributeSpecified(true);
        cookie1.setPathAttributeSpecified(true);
        cookie1.setPortAttributeSpecified(true);
        Cookie2 cookie2 = new Cookie2(".domain.com", "name2",
                                      null, "/", null, false, null);
        cookie2.setVersion(1);
        // value null, domain, path, port specified
        cookie2.setDomainAttributeSpecified(true);
        cookie2.setPathAttributeSpecified(true);
        cookie2.setPortAttributeSpecified(false);
        Cookie[] cookies = new Cookie[] {cookie1, cookie2};
        assertEquals("$Version=\"1\"; name1=\"value1\"; $Domain=\".domain.com\"; $Path=\"/\"; $Port=\"80,8080\"; " +
            "name2=\"\"; $Domain=\".domain.com\"; $Path=\"/\"", cookiespec.formatCookies(cookies));
    }

    // ------------------------------------------------------- Backward compatibility tests

    /**
     * Test backward compatibility with <tt>Set-Cookie</tt> header.
     */
    public void testCompatibilityWithSetCookie() throws Exception {
        CookieSpec cookiespec = new RFC2965Spec();
        Header header = new Header("Set-Cookie", "name=value; domain=.domain.com; version=1");
        Cookie[] parsed = cookieParse(cookiespec, "www.domain.com", 80, "/", false, header);
        assertNotNull(parsed);
        assertEquals(1, parsed.length);
        assertEquals("name", parsed[0].getName());
        assertEquals("value", parsed[0].getValue());
        assertEquals(".domain.com", parsed[0].getDomain());
        assertEquals("/", parsed[0].getPath());
    }

}

