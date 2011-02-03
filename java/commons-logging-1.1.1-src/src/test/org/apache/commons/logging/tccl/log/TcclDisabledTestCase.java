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

package org.apache.commons.logging.tccl.log;


import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.PathableClassLoader;
import org.apache.commons.logging.PathableTestSuite;


/**
 * Verify that by default LogFactoryImpl is loaded from the tccl classloader.
 */

public class TcclDisabledTestCase extends TestCase {

    public static final String MY_LOG_PKG = 
        "org.apache.commons.logging.tccl.custom";

    public static final String MY_LOG_IMPL =
        MY_LOG_PKG + ".MyLog";

    // ------------------------------------------- JUnit Infrastructure Methods

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() throws Exception {
        Class thisClass = TcclDisabledTestCase.class;

        // Determine the URL to this .class file, so that we can then
        // append the priority dirs to it. For tidiness, load this
        // class through a dummy loader though this is not absolutely
        // necessary...
        PathableClassLoader dummy = new PathableClassLoader(null);
        dummy.useExplicitLoader("junit.", Test.class.getClassLoader());
        dummy.addLogicalLib("testclasses");
        dummy.addLogicalLib("commons-logging");
        
        String thisClassPath = thisClass.getName().replace('.', '/') + ".class";
        URL baseUrl = dummy.findResource(thisClassPath);

        // Now set up the desired classloader hierarchy. Everything goes into
        // the parent classpath, but we exclude the custom Log class.
        //
        // We then create a tccl classloader that can see the custom
        // Log class. Therefore if that class can be found, then the
        // TCCL must have been used to load it.
        PathableClassLoader emptyLoader = new PathableClassLoader(null);
        
        PathableClassLoader parentLoader = new PathableClassLoader(null);
        parentLoader.useExplicitLoader("junit.", Test.class.getClassLoader());
        parentLoader.addLogicalLib("commons-logging");
        parentLoader.addLogicalLib("testclasses");
        // hack to ensure that the testcase classloader can't see
        // the custom MyLog
        parentLoader.useExplicitLoader(MY_LOG_PKG + ".", emptyLoader);
        
        URL propsEnableUrl = new URL(baseUrl, "props_disable_tccl/");
        parentLoader.addURL(propsEnableUrl);

        PathableClassLoader tcclLoader = new PathableClassLoader(parentLoader);
        tcclLoader.addLogicalLib("testclasses");

        Class testClass = parentLoader.loadClass(thisClass.getName());
        return new PathableTestSuite(testClass, tcclLoader);
    }

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() throws Exception {
        LogFactory.releaseAll();
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        LogFactory.releaseAll();
    }

    // ----------------------------------------------------------- Test Methods

    /**
     * Verify that MyLog is only loadable via the tccl.
     */
    public void testLoader() throws Exception {
        
        ClassLoader thisClassLoader = this.getClass().getClassLoader();
        ClassLoader tcclLoader = Thread.currentThread().getContextClassLoader();

        // the tccl loader should NOT be the same as the loader that loaded this test class.
        assertNotSame("tccl not same as test classloader", thisClassLoader, tcclLoader);

        // MyLog should not be loadable via parent loader
        try {
            Class clazz = thisClassLoader.loadClass(MY_LOG_IMPL);
            fail("Unexpectedly able to load MyLog via test class classloader");
            assertNotNull(clazz); // silence warnings about unused var
        } catch(ClassNotFoundException ex) {
            // ok, expected
        }
        
        // MyLog should be loadable via tccl loader
        try {
            Class clazz = tcclLoader.loadClass(MY_LOG_IMPL);
            assertNotNull(clazz);
        } catch(ClassNotFoundException ex) {
            fail("Unexpectedly unable to load MyLog via tccl classloader");
        }
    }

    /**
     * Verify that the custom Log implementation which is only accessable
     * via the TCCL has NOT been loaded. Because this is only accessable via the
     * TCCL, and we've use a commons-logging.properties that disables TCCL loading,
     * we should see the default Log rather than the custom one.
     */
    public void testTcclLoading() throws Exception {
        LogFactory instance = LogFactory.getFactory();
        assertEquals(
                "Correct LogFactory loaded", 
                "org.apache.commons.logging.impl.LogFactoryImpl",
                instance.getClass().getName());

        try {
            Log log = instance.getInstance("test");
            fail("Unexpectedly succeeded in loading a custom Log class"
                + " that is only accessable via the tccl.");
            assertNotNull(log); // silence compiler warning about unused var
        } catch(LogConfigurationException ex) {
            // ok, expected
            int index = ex.getMessage().indexOf(MY_LOG_IMPL);
            assertTrue("MyLog not found", index >= 0);
        }
    }
}
