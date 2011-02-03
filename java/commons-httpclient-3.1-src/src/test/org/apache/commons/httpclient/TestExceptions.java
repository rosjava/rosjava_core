/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/TestExceptions.java,v 1.4 2004/03/25 20:37:20 olegk Exp $
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * 
 * @author <a href="mailto:laura@lwerner.org">Laura Werner</a>
 */
public class TestExceptions extends TestCase
{

    // ------------------------------------------------------------ Constructor
    public TestExceptions(String testName) {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestExceptions.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestExceptions.class);
    }

    /** Make sure that you can retrieve the "cause" from an HttpException */
    public void testGetCause() {
        
        Exception aCause = new IOException("the cause");
        
        try {
            throw new HttpException("http exception", aCause);
        }
        catch (HttpException e) {
            assertEquals("Retrieve cause from caught exception", e.getCause(), aCause);
        }
    }
    
    /** Make sure HttpConnection prints its stack trace to a PrintWriter properly */
    public void testStackTraceWriter() {
        
        Exception aCause = new IOException("initial exception");
        try {
            throw new HttpException("http exception", aCause);
        }
        catch (HttpException e) {
            // Get the stack trace printed into a string
            StringWriter stringWriter = new StringWriter();
            PrintWriter  writer = new PrintWriter(stringWriter);
            e.printStackTrace(writer);
            writer.flush();
            String stackTrace = stringWriter.toString();
            
            // Do some validation on what got printed
            validateStackTrace(e, stackTrace);
        }
    }
    
    /** Make sure HttpConnection prints its stack trace to a PrintStream properly */
    public void testStackTraceStream() {
        
        Exception aCause = new IOException("initial exception");
        try {
            throw new HttpException("http exception", aCause);
        }
        catch (HttpException e) {
            // Get the stack trace printed into a string
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            PrintStream  stream = new PrintStream(byteStream);
            e.printStackTrace(stream);
            stream.flush();
            String stackTrace = byteStream.toString();  // Assume default charset
            
            // Do some validation on what got printed
            validateStackTrace(e, stackTrace);
        }
    }
    
    /**
     * Make sure an HttpException stack trace has the right info in it.
     * This doesn't bother parsing the whole thing, just does some sanity checks.
     */
    private void validateStackTrace(HttpException exception, String stackTrace) {
        assertTrue("Starts with exception string", stackTrace.startsWith(exception.toString()));
        
        Throwable cause = exception.getCause();
        if (cause != null) {
            assertTrue("Contains 'cause'", stackTrace.toLowerCase().indexOf("cause") != -1);
            assertTrue("Contains cause.toString()", stackTrace.indexOf(cause.toString()) != -1);
        }
    }
}
