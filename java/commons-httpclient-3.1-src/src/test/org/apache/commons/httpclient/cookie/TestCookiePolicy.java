/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/cookie/TestCookiePolicy.java,v 1.2 2004/09/14 20:11:32 olegk Exp $
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


/**
 * Test cases for Cookie Policy
 *
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * 
 * @version $Revision: 480424 $
 */
public class TestCookiePolicy extends TestCookieBase {

    // ------------------------------------------------------------ Constructor

    public TestCookiePolicy(String name) {
        super(name);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestCookiePolicy.class);
    }

    public void testRegisterNullPolicyId() {
        try {
            CookiePolicy.registerCookieSpec(null, null);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testRegisterNullPolicy() {
        try {
            CookiePolicy.registerCookieSpec("whatever", null);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testUnregisterNullPolicy() {
        try {
            CookiePolicy.unregisterCookieSpec(null);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testGetPolicyNullId() {
        try {
            CookiePolicy.getCookieSpec(null);
            fail("IllegalArgumentException must have been thrown");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testRegisterUnregister() {
        CookiePolicy.registerCookieSpec("whatever", CookieSpecBase.class);
        CookiePolicy.unregisterCookieSpec("whatever");
        try {
            CookiePolicy.getCookieSpec("whatever");
            fail("IllegalStateException must have been thrown");
        } catch (IllegalStateException expected) {
        }
    }

    public void testGetDefaultPolicy() {
        assertNotNull(CookiePolicy.getDefaultSpec());
    }
}

