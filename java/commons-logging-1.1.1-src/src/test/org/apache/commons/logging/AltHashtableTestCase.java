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

package org.apache.commons.logging;

import junit.framework.*;

/**
 * Test the ability to force the LogFactory class to use some
 * arbitrary Hashtable implementation to store its mapping from
 * context-classloader -> LogFactory object.
 * <p>
 * This is done by 
 */
public class AltHashtableTestCase extends TestCase {

    public static Test suite() throws Exception {
        Class thisClass = AltHashtableTestCase.class;
        ClassLoader thisClassLoader = thisClass.getClassLoader();

        PathableClassLoader loader = new PathableClassLoader(null);
        loader.useExplicitLoader("junit.", thisClassLoader);
        loader.addLogicalLib("testclasses");
        loader.addLogicalLib("commons-logging");

        Class testClass = loader.loadClass(thisClass.getName());
        return new PathableTestSuite(testClass, loader);
    }

    /**
     * Set up before each test.
     * <p>
     * This method ensures that the appropriate system property is defined
     * to force the LogFactory class to use the AltHashtable class as its
     * Hashtable implementation for storing factories in. 
     * <p>
     * This does make the assumption that whatever JVM we are running in
     * doesn't initialise classes until they are actually referenced (ie the
     * LogFactory class hasn't been initialised before this method is called).
     * This is true of all JVMs I know of; and if it isn't then this test will
     * fail and someone will tell us. 
     */
    public void setUp() {
        System.setProperty(
                "org.apache.commons.logging.LogFactory.HashtableImpl",
                AltHashtable.class.getName());
    }
    
    /**
     * Verify that initialising the LogFactory class will cause it
     * to instantiate an object of type specified in system property
     * "org.apache.commons.logging.LogFactory.HashtableImpl".
     */
    public void testType() {
        // Here, the reference to the LogFactory class should cause the
        // class to be loaded and initialised. It will see the property
        // set and use the AltHashtable class. If other tests in this
        // class have already been run within the same classloader then
        // LogFactory will already have been initialised, but that
        // doesn't change the effectiveness of this test.
        assertTrue(LogFactory.factories instanceof AltHashtable);
    }
    
    /**
     * Verify that when LogFactory sees a context-classloader for the
     * first time that it creates a new entry in the LogFactory.factories
     * hashmap. In particular, this checks that this process works ok when
     * a system property has been used to specify an alternative Hashtable
     * implementation for LogFactory to use.
     */
    public void testPutCalled() throws Exception {
        AltHashtable.lastKey = null;
        AltHashtable.lastValue = null;
        
        LogFactory.getLog(AltHashtableTestCase.class);
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        assertEquals(contextLoader, AltHashtable.lastKey);
        assertNotNull(AltHashtable.lastValue);
    }
}
