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

package org.apache.commons.logging.jdk14;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import junit.framework.Test;

import org.apache.commons.logging.DummyException;
import org.apache.commons.logging.PathableClassLoader;
import org.apache.commons.logging.PathableTestSuite;


/**
 * <p>TestCase for JDK 1.4 logging when running on a JDK 1.4 system with
 * custom configuration, so that JDK 1.4 should be selected and an appropriate
 * logger configured per the configuration properties.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 568760 $ $Date: 2007-08-23 00:19:45 +0200 (to, 23 aug 2007) $
 */

public class CustomConfigTestCase extends DefaultConfigTestCase {

    protected static final String HANDLER_NAME 
        = "org.apache.commons.logging.jdk14.TestHandler";

    // ----------------------------------------------------------- Constructors


    /**
     * <p>Construct a new instance of this test case.</p>
     *
     * @param name Name of the test case
     */
    public CustomConfigTestCase(String name) {
        super(name);
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * <p>The customized <code>Handler</code> we will be using.</p>
     */
    protected TestHandler handler = null;


    /**
     * <p>The underlying <code>Handler</code>s we will be using.</p>
     */
    protected Handler handlers[] = null;


    /**
     * <p>The underlying <code>Logger</code> we will be using.</p>
     */
    protected Logger logger = null;


    /**
     * <p>The underlying <code>LogManager</code> we will be using.</p>
     */
    protected LogManager manager = null;


    /**
     * <p>The message levels that should have been logged.</p>
     */
    protected Level testLevels[] =
    { Level.FINE, Level.INFO, Level.WARNING, Level.SEVERE, Level.SEVERE };


    /**
     * <p>The message strings that should have been logged.</p>
     */
    protected String testMessages[] =
    { "debug", "info", "warn", "error", "fatal" };


    // ------------------------------------------- JUnit Infrastructure Methods


    /**
     * Given the name of a class that is somewhere in the classpath of the provided
     * classloader, return the contents of the corresponding .class file.
     */
    protected static byte[] readClass(String name, ClassLoader srcCL) throws Exception {
        String resName = name.replace('.', '/') + ".class";
        System.err.println("Trying to load resource [" + resName + "]");
        InputStream is = srcCL.getResourceAsStream(resName);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.err.println("Reading resource [" + resName + "]");
        byte[] buf = new byte[1000];
        for(;;) {
            int read = is.read(buf);
            if (read <= 0) {
                break;
            }
            baos.write(buf, 0, read);
        }
        is.close();
        return baos.toByteArray();
    }

    /**
     * Make a class available in the system classloader even when its classfile is
     * not present in the classpath configured for that classloader. This only
     * works for classes for which all dependencies are already loaded in
     * that classloader.
     */
    protected static void loadTestHandler(String className, ClassLoader targetCL) {
        try {
            targetCL.loadClass(className);
            // fail("Class already in target classloader");
            return;
        } catch(ClassNotFoundException ex) {
            // ok, go ahead and load it
        }

        try {
            ClassLoader srcCL = CustomConfigAPITestCase.class.getClassLoader();
            byte[] classData = readClass(className, srcCL);

            Class[] params = new Class[] {
                String.class, classData.getClass(), 
                Integer.TYPE, Integer.TYPE};
            Method m = ClassLoader.class.getDeclaredMethod("defineClass", params);

            Object[] args = new Object[4];
            args[0] = className;
            args[1] = classData;
            args[2] = new Integer(0);
            args[3] = new Integer(classData.length);
            m.setAccessible(true);
            m.invoke(targetCL, args);
        } catch(Exception e) {
            e.printStackTrace();
            fail("Unable to load class " + className);
        }
    }

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() throws Exception {
        setUpManager
            ("org/apache/commons/logging/jdk14/CustomConfig.properties");
        setUpLogger("TestLogger");
        setUpHandlers();
        setUpFactory();
        setUpLog("TestLogger");
    }


    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() throws Exception {
        PathableClassLoader cl = new PathableClassLoader(null);
        cl.useExplicitLoader("junit.", Test.class.getClassLoader());

        // the TestHandler class must be accessable from the System classloader
        // in order for java.util.logging.LogManager.readConfiguration to
        // be able to instantiate it. And this test case must see the same
        // class in order to be able to access its data. Yes this is ugly
        // but the whole jdk14 API is a ******* mess anyway.
        ClassLoader scl = ClassLoader.getSystemClassLoader();
        loadTestHandler(HANDLER_NAME, scl);
        cl.useExplicitLoader(HANDLER_NAME, scl);
        cl.addLogicalLib("commons-logging");
        cl.addLogicalLib("testclasses");
        
        Class testClass = cl.loadClass(CustomConfigTestCase.class.getName());
        return new PathableTestSuite(testClass, cl);
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
        handlers = null;
        logger = null;
        manager = null;
    }


    // ----------------------------------------------------------- Test Methods


    // Test logging message strings with exceptions
    public void testExceptionMessages() throws Exception {

        logExceptionMessages();
        checkLogRecords(true);

    }


    // Test logging plain message strings
    public void testPlainMessages() throws Exception {

        logPlainMessages();
        checkLogRecords(false);

    }


    // Test pristine Handlers instances
    public void testPristineHandlers() {

        assertNotNull(handlers);
        assertEquals(1, handlers.length);
        assertTrue(handlers[0] instanceof TestHandler);
        assertNotNull(handler);

    }


    // Test pristine Logger instance
    public void testPristineLogger() {

        assertNotNull("Logger exists", logger);
        assertEquals("Logger name", "TestLogger", logger.getName());

        // Assert which logging levels have been enabled
        assertTrue(logger.isLoggable(Level.SEVERE));
        assertTrue(logger.isLoggable(Level.WARNING));
        assertTrue(logger.isLoggable(Level.INFO));
        assertTrue(logger.isLoggable(Level.CONFIG));
        assertTrue(logger.isLoggable(Level.FINE));
        assertTrue(!logger.isLoggable(Level.FINER));
        assertTrue(!logger.isLoggable(Level.FINEST));

    }


    // Test Serializability of Log instance
    public void testSerializable() throws Exception {

        super.testSerializable();
        testExceptionMessages();

    }


    // -------------------------------------------------------- Support Methods


    // Check the log instance
    protected void checkLog() {

        assertNotNull("Log exists", log);
        assertEquals("Log class",
                     "org.apache.commons.logging.impl.Jdk14Logger",
                     log.getClass().getName());

        // Assert which logging levels have been enabled
        assertTrue(log.isFatalEnabled());
        assertTrue(log.isErrorEnabled());
        assertTrue(log.isWarnEnabled());
        assertTrue(log.isInfoEnabled());
        assertTrue(log.isDebugEnabled());
        assertTrue(!log.isTraceEnabled());

    }


    // Check the recorded messages
    protected void checkLogRecords(boolean thrown) {
        Iterator records = handler.records();
        for (int i = 0; i < testMessages.length; i++) {
            assertTrue(records.hasNext());
            LogRecord record = (LogRecord) records.next();
            assertEquals("LogRecord level",
                         testLevels[i], record.getLevel());
            assertEquals("LogRecord message",
                         testMessages[i], record.getMessage());
            assertTrue("LogRecord class",
                         record.getSourceClassName().startsWith(
                                 "org.apache.commons.logging.jdk14.CustomConfig"));
            if (thrown) {
                assertEquals("LogRecord method",
                             "logExceptionMessages",
                             record.getSourceMethodName());
            } else {
                assertEquals("LogRecord method",
                             "logPlainMessages",
                             record.getSourceMethodName());
            }
            if (thrown) {
                assertNotNull("LogRecord thrown", record.getThrown());
                assertTrue("LogRecord thrown type",
                           record.getThrown() instanceof DummyException);
            } else {
                assertNull("LogRecord thrown",
                           record.getThrown());
            }
        }
        assertTrue(!records.hasNext());
        handler.flush();
    }


    // Log the messages with exceptions
    protected void logExceptionMessages() {
        Throwable t = new DummyException();
        log.trace("trace", t); // Should not actually get logged
        log.debug("debug", t);
        log.info("info", t);
        log.warn("warn", t);
        log.error("error", t);
        log.fatal("fatal", t);
    }


    // Log the plain messages
    protected void logPlainMessages() {
        log.trace("trace"); // Should not actually get logged
        log.debug("debug");
        log.info("info");
        log.warn("warn");
        log.error("error");
        log.fatal("fatal");
    }


    // Set up handlers instance
    protected void setUpHandlers() throws Exception {
        Logger parent = logger;
        while (parent.getParent() != null) {
            parent = parent.getParent();
        }
        handlers = parent.getHandlers();
        
        // The CustomConfig.properties file explicitly defines one handler class
        // to be attached to the root logger, so if it isn't there then 
        // something is badly wrong...
        //
        // Yes this testing is also done in testPristineHandlers but
        // unfortunately:
        //  * we need to set up the handlers variable here, 
        //  * we don't want that to be set up incorrectly, as that can
        //    produce weird error messages in other tests, and
        //  * we can't rely on testPristineHandlers being the first
        //    test to run.
        // so we need to test things here too.
        assertNotNull("No Handlers defined for JDK14 logging", handlers);
        assertEquals("Unexpected number of handlers for JDK14 logging", 1, handlers.length);
        assertNotNull("Handler is null", handlers[0]);
        assertTrue("Handler not of expected type", handlers[0] instanceof TestHandler);
        handler = (TestHandler) handlers[0];
    }


    // Set up logger instance
    protected void setUpLogger(String name) throws Exception {
        logger = Logger.getLogger(name);
    }


    // Set up LogManager instance
    protected void setUpManager(String config) throws Exception {
        manager = LogManager.getLogManager();
        InputStream is =
            this.getClass().getClassLoader().getResourceAsStream(config);
        manager.readConfiguration(is);
        is.close();
    }


}
