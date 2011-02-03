/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/ProxyTestDecorator.java,v 1.1 2004/11/01 02:21:15 mbecke Exp $
 * $Revision: 480424 $
 * $Date: 2006-11-29 06:56:49 +0100 (Wed, 29 Nov 2006) $
 *
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
package org.apache.commons.httpclient;

import java.util.Enumeration;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * A TestDecorator that configures instances of HttpClientTestBase to use
 * a proxy server.
 */
public class ProxyTestDecorator extends TestSetup {

    /**
     * Iterates through all test cases included in the suite and adds
     * copies of them modified to use a proxy server.
     * @param suite
     */
    public static void addTests(TestSuite suite) {
        TestSuite ts2 = new TestSuite();
        addTest(ts2, suite);
        suite.addTest(ts2);        
    }
    
    private static void addTest(TestSuite suite, Test t) {
        if (t instanceof HttpClientTestBase) {
            suite.addTest(new ProxyTestDecorator((HttpClientTestBase) t));
        } else if (t instanceof TestSuite) {
            Enumeration en = ((TestSuite) t).tests();
            while (en.hasMoreElements()) {
                addTest(suite, (Test) en.nextElement());
            }
        }
    }
    
    public ProxyTestDecorator(HttpClientTestBase test) {
        super(test);
    }
            
    protected void setUp() throws Exception {
        HttpClientTestBase base = (HttpClientTestBase) fTest;
        base.setUseProxy(true);
    }        
}
