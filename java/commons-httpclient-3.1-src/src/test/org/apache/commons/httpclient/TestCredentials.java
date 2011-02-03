/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/TestCredentials.java,v 1.1 2004/10/31 13:46:54 olegk Exp $
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
 * [Additional notices, if required by prior licensing conditions]
 *
 */

package org.apache.commons.httpclient;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit tests for {@link Credentials}.
 *
 * @author Rodney Waldhoff
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 * @version $Id: TestCredentials.java 480424 2006-11-29 05:56:49Z bayard $
 */
public class TestCredentials extends TestCase {

    // ------------------------------------------------------------ Constructor
    public TestCredentials(String testName) {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestCredentials.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestCredentials.class);
    }

    public void testCredentialConstructors() {
        try {
            new UsernamePasswordCredentials(null, null);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            new NTCredentials("user", "password", null, null);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            new NTCredentials("user", "password", "host", null);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {
            // expected
        }
        NTCredentials creds = new NTCredentials("user", null, "host", "domain");
        assertNotNull(creds.getUserName());
        assertNull(creds.getPassword());
        assertNotNull(creds.getDomain());
        assertNotNull(creds.getHost());
    }

    /**
     * Verifies that credentials report equal when they should.
     */
    public void testCredentialEquals() {

        Credentials creds1 = new UsernamePasswordCredentials("user1", "password1");
        Credentials creds1Again = new UsernamePasswordCredentials("user1", "password1");
        Credentials creds2 = new UsernamePasswordCredentials("user2", "password2");
        Credentials creds3 = new UsernamePasswordCredentials("user3", null);
        Credentials creds3Again = new UsernamePasswordCredentials("user3", null);

        assertEquals(creds1, creds1Again);
        assertNotSame(creds1, creds2);
        assertEquals(creds3, creds3Again);

        Credentials ntCreds1 = new NTCredentials("user1", "password1", "host1", "domain1");
        Credentials ntCreds1Again = new NTCredentials("user1", "password1", "host1", "domain1");
        Credentials ntCreds2 = new NTCredentials("user1", "password2", "host1", "domain1");
        Credentials ntCreds3 = new NTCredentials("user1", "password1", "host2", "domain1");
        Credentials ntCreds4 = new NTCredentials("user1", "password1", "host1", "domain2");

        assertEquals(ntCreds1, ntCreds1Again);
        assertNotSame(ntCreds1, creds1);
        assertNotSame(creds1, ntCreds1);
        assertNotSame(ntCreds1, ntCreds2);
        assertNotSame(ntCreds1, ntCreds3);
        assertNotSame(ntCreds1, ntCreds4);
    }
}
