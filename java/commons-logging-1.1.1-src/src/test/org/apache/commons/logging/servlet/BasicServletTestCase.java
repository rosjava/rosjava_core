/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.apache.commons.logging.servlet;

import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.commons.logging.PathableClassLoader;
import org.apache.commons.logging.PathableTestSuite;
import org.apache.commons.logging.impl.ServletContextCleaner;


/**
 * Tests for ServletContextCleaner utility class.
 */

public class BasicServletTestCase extends TestCase {

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() throws Exception {
        // LogFactory in parent
        // LogFactory in child (loads test)
        // LogFactory in tccl
        //
        // Having the test loaded via a loader above the tccl emulates the situation
        // where a web.xml file specifies ServletContextCleaner as a listener, and
        // that class is deployed via a shared classloader.

        PathableClassLoader parent = new PathableClassLoader(null);
        parent.useExplicitLoader("junit.", Test.class.getClassLoader());
        parent.addLogicalLib("commons-logging");
        parent.addLogicalLib("servlet-api");

        PathableClassLoader child = new PathableClassLoader(parent);
        child.setParentFirst(false);
        child.addLogicalLib("commons-logging");
        child.addLogicalLib("testclasses");

        PathableClassLoader tccl = new PathableClassLoader(child);
        tccl.setParentFirst(false);
        tccl.addLogicalLib("commons-logging");

        Class testClass = child.loadClass(BasicServletTestCase.class.getName());
        return new PathableTestSuite(testClass, tccl);
    }
    
    /**
     * Test that calling ServletContextCleaner.contextDestroyed doesn't crash.
     * Testing anything else is rather difficult...
     */
    public void testBasics() {
        ServletContextCleaner scc = new ServletContextCleaner();
        scc.contextDestroyed(null);
    }
}
