/*
 * $HeadURL: https://svn.apache.org/repos/asf/jakarta/httpcomponents/oac.hc3x/tags/HTTPCLIENT_3_1/src/test/org/apache/commons/httpclient/TestAll.java $
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

package org.apache.commons.httpclient;

import org.apache.commons.httpclient.auth.TestAuthAll;
import org.apache.commons.httpclient.cookie.TestCookieAll;
import org.apache.commons.httpclient.params.TestParamsAll;

import junit.framework.*;

/**
 * @author Remy Maucherat
 * @author Rodney Waldhoff
 * @version $Id: TestAll.java 480424 2006-11-29 05:56:49Z bayard $
 */
public class TestAll extends TestCase {

    public TestAll(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        // Fundamentals
        suite.addTest(TestHttpMethodFundamentals.suite());
        suite.addTest(TestHttpStatus.suite());
        suite.addTest(TestStatusLine.suite());
        suite.addTest(TestRequestLine.suite());
        suite.addTest(TestHeader.suite());
        suite.addTest(TestHeaderElement.suite());
        suite.addTest(TestHeaderOps.suite());
        suite.addTest(TestResponseHeaders.suite());
        suite.addTest(TestRequestHeaders.suite());
        suite.addTest(TestStreams.suite());
        suite.addTest(TestParameterParser.suite());
        suite.addTest(TestParameterFormatter.suite());
        suite.addTest(TestNVP.suite());
        suite.addTest(TestMethodCharEncoding.suite());
        suite.addTest(TestHttpVersion.suite());
        suite.addTest(TestEffectiveHttpVersion.suite());
        suite.addTest(TestHttpParser.suite());
        suite.addTest(TestBadContentLength.suite());
        suite.addTest(TestEquals.suite());
        suite.addTest(TestQueryParameters.suite());
        // Exceptions
        suite.addTest(TestExceptions.suite());        
        // HTTP state management
        suite.addTest(TestHttpState.suite());
        suite.addTest(TestCookieAll.suite());
        // Authentication 
        suite.addTest(TestCredentials.suite());
        suite.addTest(TestAuthAll.suite());
        // Redirects
        suite.addTest(TestRedirects.suite());
        // Connection management
        suite.addTest(TestHttpConnection.suite());
        suite.addTest(TestHttpConnectionManager.suite());
        suite.addTest(TestConnectionPersistence.suite());
        suite.addTest(TestIdleConnectionTimeout.suite());
        suite.addTest(TestMethodAbort.suite());
        // Preferences
        suite.addTest(TestParamsAll.suite());
        suite.addTest(TestVirtualHost.suite());        
        suite.addTest(TestHostConfiguration.suite());        
        // URIs
        suite.addTest(TestURI.suite());
        suite.addTest(TestURIUtil.suite());
        suite.addTest(TestURIUtil2.suite());
        // Method specific
        suite.addTest(TestEntityEnclosingMethod.suite());
        suite.addTest(TestPostParameterEncoding.suite());
        suite.addTest(TestPostMethod.suite());
        suite.addTest(TestPartsNoHost.suite());
        suite.addTest(TestMultipartPost.suite());
        // Non compliant behaviour
        suite.addTest(TestNoncompliant.suite());
        // Proxy
        suite.addTest(TestProxy.suite());
        suite.addTest(TestProxyWithRedirect.suite());
        return suite;
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestAll.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

}
