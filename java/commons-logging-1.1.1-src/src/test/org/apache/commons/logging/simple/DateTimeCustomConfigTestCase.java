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

package org.apache.commons.logging.simple;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Test;

import org.apache.commons.logging.PathableClassLoader;
import org.apache.commons.logging.PathableTestSuite;


/**
 * Tests custom date time format configuration
 */
public class DateTimeCustomConfigTestCase extends CustomConfigTestCase {
    
    // ----------------------------------------------------------- Constructors

    /**
     * Return the tests included in this test suite.
     * <p>
     * We need to use a PathableClassLoader here because the SimpleLog class
     * is a pile of junk and chock-full of static variables. Any other test
     * (like simple.CustomConfigTestCase) that has used the SimpleLog class
     * will already have caused it to do once-only initialisation that we
     * can't reset, even by calling LogFactory.releaseAll, because of those
     * ugly statics. The only clean solution is to load a clean copy of
     * commons-logging including SimpleLog via a nice clean classloader.
     * Or we could fix SimpleLog to be sane...
     */
    public static Test suite() throws Exception {
        Class thisClass = DateTimeCustomConfigTestCase.class;

        PathableClassLoader loader = new PathableClassLoader(null);
        loader.useExplicitLoader("junit.", Test.class.getClassLoader());
        loader.addLogicalLib("testclasses");
        loader.addLogicalLib("commons-logging");
        
        Class testClass = loader.loadClass(thisClass.getName());
        return new PathableTestSuite(testClass, loader);
    }


    /**
     * Set up system properties required by this unit test. Here, we
     * set up the props defined in the parent class setProperties method, 
     * and add a few to configure the SimpleLog class date/time output.
     */
    public void setProperties() {
        super.setProperties();
        
        System.setProperty(
            "org.apache.commons.logging.simplelog.dateTimeFormat",
            "dd.mm.yyyy");
        System.setProperty(
            "org.apache.commons.logging.simplelog.showdatetime",
            "true");
    }

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() throws Exception {
        super.setUp();
    }


    // ----------------------------------------------------------- Methods

    /** Checks that the date time format has been successfully set */
    protected void checkDecoratedDateTime() {
        assertEquals("Expected date format to be set", "dd.mm.yyyy",
                     ((DecoratedSimpleLog) log).getDateTimeFormat());
        
        // try the formatter
        Date now = new Date();
        DateFormat formatter = ((DecoratedSimpleLog) log).getDateTimeFormatter(); 
        SimpleDateFormat sampleFormatter = new SimpleDateFormat("dd.mm.yyyy");
        assertEquals("Date should be formatters to pattern dd.mm.yyyy", sampleFormatter.format(now), formatter.format(now));
    }
    
    /** Hook for subclassses */
    protected void checkShowDateTime() {
        assertTrue(((DecoratedSimpleLog) log).getShowDateTime());
    }
    
}