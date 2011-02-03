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

import java.io.StringReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;

import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfig;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcClientRequestImpl;
import org.apache.xmlrpc.client.XmlRpcSunHttpTransportFactory;
import org.apache.xmlrpc.common.TypeFactoryImpl;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.parser.XmlRpcRequestParser;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


/** A test case for the various serializers.
 */
public class SerializerTest extends TestCase {
	private final XmlRpcClient client;

	/** Creates a new instance.
	 */
	public SerializerTest() {
		client = new XmlRpcClient();
		client.setTransportFactory(new XmlRpcSunHttpTransportFactory(client));
	}

	protected XmlRpcClientConfigImpl getConfig() {
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		return config;
	}

	protected XmlRpcStreamRequestConfig getExConfig() {
		XmlRpcClientConfigImpl config = getConfig();
		config.setEnabledForExtensions(true);
		return config;
	}

	protected String writeRequest(XmlRpcStreamRequestConfig pConfig, XmlRpcRequest pRequest)
			throws SAXException {
        client.setConfig((XmlRpcClientConfig) pConfig);
        return XmlRpcTestCase.writeRequest(client, pRequest);
	}

	/** Test serialization of a byte parameter.
	 * @throws Exception The test failed.
	 */
	public void testByteParam() throws Exception {
		XmlRpcStreamRequestConfig config = getExConfig();
		XmlRpcRequest request = new XmlRpcClientRequestImpl(config, "byteParam", new Object[]{new Byte((byte)3)});
		String got = writeRequest(config, request);
		String expect =
			"<?xml version=\"1.0\" encoding=\"US-ASCII\"?>"
			+ "<methodCall xmlns:ex=\"http://ws.apache.org/xmlrpc/namespaces/extensions\">"
			+ "<methodName>byteParam</methodName><params><param><value><ex:i1>3</ex:i1></value></param></params></methodCall>";
		assertEquals(expect, got);
	}

	/** Test serialization of an integer parameter.
	 * @throws Exception The test failed.
	 */
	public void testIntParam() throws Exception {
		XmlRpcStreamRequestConfig config = getConfig();
		XmlRpcRequest request = new XmlRpcClientRequestImpl(config, "intParam", new Object[]{new Integer(3)});
		String got = writeRequest(config, request);
		String expect =
			"<?xml version=\"1.0\" encoding=\"US-ASCII\"?>"
			+ "<methodCall>"
			+ "<methodName>intParam</methodName><params><param><value><i4>3</i4></value></param></params></methodCall>";
		assertEquals(expect, got);
	}

	/** Test serialization of a byte array.
	 * @throws Exception The test failed.
	 */
	public void testByteArrayParam() throws Exception {
		byte[] bytes = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
		XmlRpcStreamRequestConfig config = getConfig();
		XmlRpcRequest request = new XmlRpcClientRequestImpl(config, "byteArrayParam", new Object[]{bytes});
		String got = writeRequest(config, request);
		String expect =
			"<?xml version=\"1.0\" encoding=\"US-ASCII\"?>"
			+ "<methodCall>"
			+ "<methodName>byteArrayParam</methodName><params><param><value><base64>AAECAwQFBgcICQ==</base64></value></param></params></methodCall>";
		assertEquals(expect, got);
	}

	/** Test serialization of a map.
	 * @throws Exception The test failed.
	 */
	public void testMapParam() throws Exception {
		final Map map = new HashMap();
		map.put("2", new Integer(3));
		map.put("3", new Integer(5));
		final Object[] params = new Object[]{map};
		XmlRpcStreamRequestConfig config = getConfig();
		XmlRpcRequest request = new XmlRpcClientRequestImpl(config, "mapParam", params);
		String got = writeRequest(config, request);
		String expect =
			"<?xml version=\"1.0\" encoding=\"US-ASCII\"?>"
			+ "<methodCall><methodName>mapParam</methodName>"
			+ "<params><param><value><struct>"
			+ "<member><name>3</name><value><i4>5</i4></value></member>"
			+ "<member><name>2</name><value><i4>3</i4></value></member>"
			+ "</struct></value></param></params></methodCall>";
		assertEquals(expect, got);
	}

	/** Tests serialization of a calendar instance.
	 */
    public void testCalendarParam() throws Exception {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Calendar cal1 = Calendar.getInstance(tz);
        cal1.set(1933, 5, 12, 11, 7, 21);
        cal1.set(Calendar.MILLISECOND, 311);
        Calendar cal2 = Calendar.getInstance(TimeZone.getDefault());
        cal2.set(1933, 5, 12, 11, 7, 21);
        cal2.set(Calendar.MILLISECOND, 311);
        XmlRpcStreamRequestConfig config = getExConfig();
        XmlRpcRequest request = new XmlRpcClientRequestImpl(config, "dateParam", new Object[]{cal1, cal2.getTime()});
        String got = writeRequest(config, request);
        String expect =
            "<?xml version=\"1.0\" encoding=\"US-ASCII\"?>"
            + "<methodCall xmlns:ex=\"http://ws.apache.org/xmlrpc/namespaces/extensions\">"
            + "<methodName>dateParam</methodName><params>"
            + "<param><value><ex:dateTime>1933-06-12T11:07:21.311Z</ex:dateTime></value></param>"
            + "<param><value><dateTime.iso8601>19330612T11:07:21</dateTime.iso8601></value></param>"
            + "</params></methodCall>";
        assertEquals(expect, got);
    }

    /**
     * Test for XMLRPC-127: Is it possible to transmit a
     * map with integers as the keys?
     */
    public void testIntegerKeyMap() throws Exception {
        Map map = new HashMap();
        map.put(new Integer(1), "one");
        XmlRpcStreamRequestConfig config = getExConfig();
        XmlRpcRequest request = new XmlRpcClientRequestImpl(config, "integerKeyMap", new Object[]{map});
        String got = writeRequest(config, request);
        String expect =
            "<?xml version=\"1.0\" encoding=\"US-ASCII\"?>"
            + "<methodCall xmlns:ex=\"http://ws.apache.org/xmlrpc/namespaces/extensions\">"
            + "<methodName>integerKeyMap</methodName><params>"
            + "<param><value><struct><member>"
            +   "<name><value><i4>1</i4></value></name>"
            +   "<value>one</value></member>"
            + "</struct></value></param>"
            + "</params></methodCall>";
        assertEquals(expect, got);

        XmlRpcServer server = new XmlRpcServer();
        XmlRpcServerConfigImpl serverConfig = new XmlRpcServerConfigImpl();
        serverConfig.setEnabledForExtensions(true);
        server.setConfig(serverConfig);
        XmlRpcRequestParser parser = new XmlRpcRequestParser(serverConfig, new TypeFactoryImpl(server));
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setValidating(false);
        spf.setNamespaceAware(true);
        XMLReader xr = spf.newSAXParser().getXMLReader();
        xr.setContentHandler(parser);
        xr.parse(new InputSource(new StringReader(expect)));
        assertEquals("integerKeyMap", parser.getMethodName());
        List params = parser.getParams();
        assertEquals(1, params.size());
        Map paramMap = (Map) params.get(0);
        assertEquals(1, paramMap.size());
        assertEquals("one", paramMap.get(new Integer(1)));
    }
}
