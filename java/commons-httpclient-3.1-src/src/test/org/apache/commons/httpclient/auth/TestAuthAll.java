/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/auth/TestAuthAll.java,v 1.1 2004/10/31 13:46:54 olegk Exp $
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
 */

package org.apache.commons.httpclient.auth;

import junit.framework.*;

/**
 * @author Oleg Kalnichevski 
 * 
 * @version $Id: TestAuthAll.java 480424 2006-11-29 05:56:49Z bayard $
 */
public class TestAuthAll extends TestCase {

    public TestAuthAll(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(TestChallengeParser.suite());
        suite.addTest(TestChallengeProcessor.suite());
        suite.addTest(TestBasicAuth.suite());
        suite.addTest(TestDigestAuth.suite());
        suite.addTest(TestNTLMAuth.suite());
        return suite;
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestAuthAll.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

}
