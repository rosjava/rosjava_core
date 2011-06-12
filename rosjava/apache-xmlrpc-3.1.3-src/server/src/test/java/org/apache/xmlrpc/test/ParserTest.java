/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.xmlrpc.test;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.UndeclaredThrowableException;
import java.text.ParseException;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.common.XmlRpcHttpRequestConfigImpl;
import org.apache.xmlrpc.common.XmlRpcStreamConfig;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.parser.DateParser;
import org.apache.xmlrpc.parser.XmlRpcRequestParser;
import org.apache.xmlrpc.parser.XmlRpcResponseParser;
import org.apache.xmlrpc.util.SAXParsers;
import org.apache.xmlrpc.util.XmlRpcDateTimeFormat;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;


/** Test for the various parsers.
 */
public class ParserTest extends TestCase {
	private Object parseResponse(final String s) throws XmlRpcException, IOException, SAXException {
		return parseResponse(new InputSource(new StringReader(s)));
	}

    private Object parseResponse(InputSource isource) throws XmlRpcException,
            IOException, SAXException {
        XmlRpcStreamRequestConfig config = new XmlRpcClientConfigImpl();
		XmlRpcClient client = new XmlRpcClient();
		XmlRpcResponseParser parser = new XmlRpcResponseParser(config, client.getTypeFactory());
		XMLReader xr = SAXParsers.newXMLReader();
		xr.setContentHandler(parser);
        xr.parse(isource);
		Object o = parser.getResult();
		return o;
    }

    private XmlRpcRequestParser parseRequest(final String s) throws XmlRpcException, IOException, SAXException {
        XmlRpcStreamConfig config = new XmlRpcHttpRequestConfigImpl();
        XmlRpcClient client = new XmlRpcClient();
        XmlRpcRequestParser parser = new XmlRpcRequestParser(config, client.getTypeFactory());
        XMLReader xr = SAXParsers.newXMLReader();
        xr.setContentHandler(parser);
        xr.parse(new InputSource(new StringReader(s)));
        return parser;
    }

	/** Tests, whether strings can be parsed with,
	 * or without, the "string" tag.
	 */
	public void testStringType() throws Exception {
		final String[] strings = new String[]{
			"3", "<string>3</string>",
			"  <string>3</string>  "
		};
		for (int i = 0;  i < strings.length;  i++) {
			final String s =
				"<?xml version='1.0' encoding='UTF-8'?>\n"
				+ "<methodResponse><params><param>\n"
				+ "<value>" + strings[i] + "</value></param>\n"
				+ "</params></methodResponse>\n";
			Object o = parseResponse(s);
			assertEquals("3", o);
		}
	}

	/** Tests, whether nested arrays can be parsed.
	 */
	public void testNestedObjectArrays() throws Exception {
		final String s =
			"<?xml version='1.0' encoding='UTF-8'?>\n"
			+ "<methodResponse><params><param>\n"
			+ "<value><array><data><value><array>\n"
			+ "<data><value>array</value>\n"
			+ "<value>string</value></data></array>\n"
			+ "</value></data></array></value></param>\n"
			+ "</params></methodResponse>\n";
		Object o = parseResponse(s);
		assertTrue(o instanceof Object[]);
		Object[] outer = (Object[]) o;
		assertEquals(1, outer.length);
		o = outer[0];
		assertTrue(o instanceof Object[]);
		Object[] inner = (Object[]) o;
		assertEquals(2, inner.length);
		assertEquals("array", inner[0]);
		assertEquals("string", inner[1]);
	}

	/**
     * Tests, whether a request may omit the <params> tag.
	 */
    public void testOptionalParams() throws Exception {
        final String s1 = "<methodResponse/>";
        Object o1 = parseResponse(s1);
        assertNull(o1);

        final String s2 = "<methodResponse><params/></methodResponse>";
        Object o2 = parseResponse(s2);
        assertNull(o2);

        final String s3 = "<methodCall><methodName>foo</methodName></methodCall>";
        XmlRpcRequestParser p3 = parseRequest(s3);
        assertEquals("foo", p3.getMethodName());
        assertNull(p3.getParams());

        final String s4 = "<methodCall><methodName>bar</methodName><params/></methodCall>";
        XmlRpcRequestParser p4 = parseRequest(s4);
        assertEquals("bar", p4.getMethodName());
        assertNotNull(p4.getParams());
        assertTrue(p4.getParams().size() == 0);
    }

    /**
     * Test for XMLRPC-140.
     */
    public void testXMLRPC140() throws Exception {
        DateParser parser = new DateParser(new XmlRpcDateTimeFormat(){
            private static final long serialVersionUID = 0L;
            protected TimeZone getTimeZone() {
                return TimeZone.getDefault();
            }
        }){
            public void setResult(Object pObject){
                try {
                    super.setResult((String) pObject);
                } catch (SAXException e) {
                    throw new UndeclaredThrowableException(e);
                }
            }
        };
        try {
            parser.setResult("20070316T162808Z");
            fail("Expected exception");
        } catch (UndeclaredThrowableException e) {
            SAXParseException spe = (SAXParseException) e.getUndeclaredThrowable();
            ParseException pe = (ParseException) spe.getException();
            assertEquals(11, pe.getErrorOffset());
        }
    }
}
