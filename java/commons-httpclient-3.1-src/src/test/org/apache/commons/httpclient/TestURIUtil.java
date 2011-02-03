/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/TestURIUtil.java,v 1.6 2004/02/22 18:08:50 olegk Exp $
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

import org.apache.commons.httpclient.util.URIUtil;

/**
 *
 * Unit tests for {@link URIUtil}.  These tests care currently quite limited
 * and should be expanded to test more functionality.
 *
 * @author Marc A. Saegesser
 * @version $Id: TestURIUtil.java 480424 2006-11-29 05:56:49Z bayard $
 */
public class TestURIUtil extends TestCase {
    // ----------------------------------------------------- Instance Variables
    URITestCase pathTests[] = {new URITestCase("http://www.server.com/path1/path2", "/path1/path2"),
                               new URITestCase("http://www.server.com/path1/path2/", "/path1/path2/"),
                               new URITestCase("http://www.server.com/path1/path2?query=string", "/path1/path2"),
                               new URITestCase("http://www.server.com/path1/path2/?query=string", "/path1/path2/"),
                               new URITestCase("www.noscheme.com/path1/path2", "/path1/path2"),
                               new URITestCase("www.noscheme.com/path1/path2#anchor?query=string", "/path1/path2"),
                               new URITestCase("/noscheme/nohost/path", "/noscheme/nohost/path"),
                               new URITestCase("http://www.server.com", "/"),
                               new URITestCase("https://www.server.com:443/ssl/path", "/ssl/path"),
                               new URITestCase("http://www.server.com:8080/path/with/port", "/path/with/port"),
                               new URITestCase("http://www.server.com/path1/path2?query1=string?1&query2=string2", "/path1/path2")};

    URITestCase queryTests[] = {new URITestCase("http://www.server.com/path1/path2", null),
                                new URITestCase("http://www.server.com/path1/path2?query=string", "query=string"),
                                new URITestCase("http://www.server.com/path1/path2/?query=string", "query=string"),
                                new URITestCase("www.noscheme.com/path1/path2#anchor?query=string", "query=string"),
                                new URITestCase("/noscheme/nohost/path?query1=string1&query2=string2", "query1=string1&query2=string2"),
                                new URITestCase("https://www.server.com:443/ssl/path?query1=string1&query2=string2", "query1=string1&query2=string2"),
                                new URITestCase("http://www.server.com:8080/path/with/port?query1=string1&query2=string2", "query1=string1&query2=string2"),
                                new URITestCase("http://www.server.com/path1/path2?query1=string?1&query2=string2", "query1=string?1&query2=string2")};



    // ------------------------------------------------------------ Constructor
    public TestURIUtil(String testName) {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestURIUtil.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestURIUtil.class);
    }


    // ----------------------------------------------------------- Test Methods
    public void testGetPath()
    {
        String testValue = "";
        String expectedResult = "";

        for(int i=0;i<pathTests.length;i++){
            testValue = pathTests[i].getTestValue();
            expectedResult = pathTests[i].getExpectedResult();
            assertEquals("Path test", expectedResult, URIUtil.getPath(testValue));
        }
    }

    public void testGetQueryString()
    {
        String testValue = "";
        String expectedResult = "";

        for(int i=0;i<queryTests.length;i++){
            testValue = queryTests[i].getTestValue();
            expectedResult = queryTests[i].getExpectedResult();
            assertEquals("Path test", expectedResult, URIUtil.getQuery(testValue));
        }
    }

    private class URITestCase{
        private String testValue;
        private String expectedResult;

        public URITestCase(String testValue, String expectedResult){
            this.testValue = testValue;
            this.expectedResult = expectedResult;
        }

        public String getTestValue(){
            return testValue;
        }

        public String getExpectedResult(){
            return expectedResult;
        }
    }
}
