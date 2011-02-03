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
package org.apache.commons.logging.tccl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.PathableClassLoader;
import org.apache.commons.logging.PathableTestSuite;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Simulates the case when TCCL is badly set and cannot load JCL.
 */
public class BadTCCLTestCase extends TestCase {

    public static Test suite() throws Exception {
        PathableClassLoader contextClassLoader = new PathableClassLoader(null);
        contextClassLoader.useExplicitLoader("junit.", Test.class.getClassLoader());
        PathableTestSuite suite = new PathableTestSuite(BadTCCLTestCase.class, contextClassLoader);
        return suite;
    }

    // test methods
    
    /**
     * This test just tests that a log implementation can be found
     * by the LogFactory.
     */
    public void testGetLog() {
         Log log = LogFactory.getLog(BadTCCLTestCase.class);
         log.debug("Hello, Mum");
    }
}
