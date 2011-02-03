/*
 * $HeadURL: https://svn.apache.org/repos/asf/jakarta/httpcomponents/oac.hc3x/tags/HTTPCLIENT_3_1/src/test/org/apache/commons/httpclient/TestParameterParser.java $
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
 */

package org.apache.commons.httpclient;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.List;

import org.apache.commons.httpclient.util.ParameterParser;

/**
 * Unit tests for {@link ParameterParser}.
 *
 * @author <a href="mailto:oleg at ural.ru">Oleg Kalnichevski</a>
 */
public class TestParameterParser extends TestCase {

    // ------------------------------------------------------------ Constructor
    public TestParameterParser(String testName) {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestParameterParser.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestParameterParser.class);
    }

    public void testParsing() {
        String s = 
          "test; test1 =  stuff   ; test2 =  \"stuff; stuff\"; test3=\"stuff";
        ParameterParser  parser = new ParameterParser();
        List params = parser.parse(s, ';');
        assertEquals("test", ((NameValuePair)params.get(0)).getName());
        assertEquals(null, ((NameValuePair)params.get(0)).getValue());
        assertEquals("test1", ((NameValuePair)params.get(1)).getName());
        assertEquals("stuff", ((NameValuePair)params.get(1)).getValue());
        assertEquals("test2", ((NameValuePair)params.get(2)).getName());
        assertEquals("stuff; stuff", ((NameValuePair)params.get(2)).getValue());
        assertEquals("test3", ((NameValuePair)params.get(3)).getName());
        assertEquals("\"stuff", ((NameValuePair)params.get(3)).getValue());

        s = "  test  , test1=stuff   ,  , test2=, test3, ";
        params = parser.parse(s, ',');
        assertEquals("test", ((NameValuePair)params.get(0)).getName());
        assertEquals(null, ((NameValuePair)params.get(0)).getValue());
        assertEquals("test1", ((NameValuePair)params.get(1)).getName());
        assertEquals("stuff", ((NameValuePair)params.get(1)).getValue());
        assertEquals("test2", ((NameValuePair)params.get(2)).getName());
        assertEquals("", ((NameValuePair)params.get(2)).getValue());
        assertEquals("test3", ((NameValuePair)params.get(3)).getName());
        assertEquals(null, ((NameValuePair)params.get(3)).getValue());

        s = "  test";
        params = parser.parse(s, ';');
        assertEquals("test", ((NameValuePair)params.get(0)).getName());
        assertEquals(null, ((NameValuePair)params.get(0)).getValue());

        s = "  ";
        params = parser.parse(s, ';');
        assertEquals(0, params.size());

        s = " = stuff ";
        params = parser.parse(s, ';');
        assertEquals(1, params.size());
        assertEquals("", ((NameValuePair)params.get(0)).getName());
        assertEquals("stuff", ((NameValuePair)params.get(0)).getValue());
    }
    
    public void testParsingEscapedChars() {
        String s = "param = \"stuff\\\"; more stuff\"";
        ParameterParser parser = new ParameterParser();
        List params = parser.parse(s, ';');
        assertEquals(1, params.size());
        assertEquals("param", 
                ((NameValuePair)params.get(0)).getName());
        assertEquals("stuff\\\"; more stuff", 
                ((NameValuePair)params.get(0)).getValue());

        s = "param = \"stuff\\\\\"; anotherparam";
        params = parser.parse(s, ';');
        assertEquals(2, params.size());
        assertEquals("param", 
                ((NameValuePair)params.get(0)).getName());
        assertEquals("stuff\\\\", 
                ((NameValuePair)params.get(0)).getValue());
        assertEquals("anotherparam", 
                ((NameValuePair)params.get(1)).getName());
        assertNull(
                ((NameValuePair)params.get(1)).getValue());
    }
    
    public void testParsingBlankParams() {
        String s =  "test; test1 =  ; test2 = \"\"";
        ParameterParser  parser = new ParameterParser();
        List params = parser.parse(s, ';');
        assertEquals("test", ((NameValuePair)params.get(0)).getName());
        assertEquals(null, ((NameValuePair)params.get(0)).getValue());
        assertEquals("test1", ((NameValuePair)params.get(1)).getName());
        assertEquals("", ((NameValuePair)params.get(1)).getValue());
        assertEquals("test2", ((NameValuePair)params.get(2)).getName());
        assertEquals("", ((NameValuePair)params.get(2)).getValue());
    }
}
