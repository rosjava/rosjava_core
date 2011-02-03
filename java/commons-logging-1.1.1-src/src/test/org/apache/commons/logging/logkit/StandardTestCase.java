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

package org.apache.commons.logging.logkit;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.Test;

import org.apache.commons.logging.AbstractLogTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.PathableClassLoader;
import org.apache.commons.logging.PathableTestSuite;
import org.apache.commons.logging.impl.LogKitLogger;

/**
 * Basic tests for Avalon LogKit logger adapter.
 */

public class StandardTestCase extends AbstractLogTest {


    // ----------------------------------------------------- Instance Variables


    /**
     * <p>The {@link LogFactory} implementation we have selected.</p>
     */
    protected LogFactory factory = null;


    /**
     * <p>The {@link Log} implementation we have selected.</p>
     */
    protected Log log = null;


    // ------------------------------------------- JUnit Infrastructure Methods


    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() throws Exception {
        Class thisClass = StandardTestCase.class;

        PathableClassLoader loader = new PathableClassLoader(null);
        loader.useExplicitLoader("junit.", Test.class.getClassLoader());
        loader.addLogicalLib("testclasses");
        loader.addLogicalLib("commons-logging");
        loader.addLogicalLib("logkit");

        Class testClass = loader.loadClass(thisClass.getName());
        return new PathableTestSuite(testClass, loader);
    }

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() throws Exception {
        LogFactory.releaseAll();

        System.setProperty(
                "org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.LogKitLogger");

        factory = LogFactory.getFactory();
        log = LogFactory.getLog("TestLogger");
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        log = null;
        factory = null;
        LogFactory.releaseAll();
    }

    // ----------------------------------------------------------- Test Methods

    /**
     * Override the abstract method from the parent class so that the
     * inherited tests can access the right Log object type. 
     */
    public Log getLogObject()
    {
        return new LogKitLogger(this.getClass().getName());
    }

    // Test pristine LogFactory instance
    public void testPristineFactory() {

        assertNotNull("LogFactory exists", factory);
        assertEquals("LogFactory class",
                     "org.apache.commons.logging.impl.LogFactoryImpl",
                     factory.getClass().getName());

        String names[] = factory.getAttributeNames();
        assertNotNull("Names exists", names);
        assertEquals("Names empty", 0, names.length);
    }

    // Test pristine Log instance
    public void testPristineLog() {
        checkStandard();
    }

    // Test Serializability of standard instance
    public void testSerializable() throws Exception {
        checkStandard();

        // Serialize and deserialize the instance
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(log);
        oos.close();
        ByteArrayInputStream bais =
            new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        log = (Log) ois.readObject();
        ois.close();

        checkStandard();
    }


    // -------------------------------------------------------- Support Methods

    // Check the standard log instance
    protected void checkStandard() {

        assertNotNull("Log exists", log);
        assertEquals("Log class",
                     "org.apache.commons.logging.impl.LogKitLogger",
                     log.getClass().getName());

        // Can we call level checkers with no exceptions?
        // Note that by default *everything* is enabled for LogKit
        assertTrue(log.isTraceEnabled());
        assertTrue(log.isDebugEnabled());
        assertTrue(log.isInfoEnabled());
        assertTrue(log.isWarnEnabled());
        assertTrue(log.isErrorEnabled());
        assertTrue(log.isFatalEnabled());
    }
}
