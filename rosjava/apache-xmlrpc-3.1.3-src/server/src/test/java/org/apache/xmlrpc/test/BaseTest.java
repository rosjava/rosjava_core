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
import java.io.Serializable;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.common.XmlRpcExtensionException;
import org.apache.xmlrpc.common.XmlRpcInvocationException;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;


/** An abstract test case, to be implemented for the various
 * transport classes.
 */
public class BaseTest extends XmlRpcTestCase {

	/** The remote class being invoked by the test case.
	 */
	public static class Remote {
		/** Returns the argument, multiplied by two.
		 * @param pArg The argument being doubled.
		 * @return The argument, multiplied by two.
		 */
		public int byteParam(byte pArg) { return pArg*2; }
		/** Returns the argument, multiplied by two.
		 * @param pArg The argument being doubled.
		 * @return The argument, multiplied by two.
		 */
		public byte byteResult(byte pArg) { return (byte) (pArg*2); }
		/** Returns the argument, multiplied by two.
		 * @param pArg The argument being doubled.
		 * @return The argument, multiplied by two.
		 */
		public int shortParam(short pArg) { return pArg*2; }
		/** Returns the argument, multiplied by two.
		 * @param pArg The argument being doubled.
		 * @return The argument, multiplied by two.
		 */
		public short shortResult(short pArg) { return (short) (pArg*2); }
		/** Returns the argument, multiplied by two.
		 * @param pArg The argument being doubled.
		 * @return The argument, multiplied by two.
		 */
		public int intParam(int pArg) { return pArg*2; }
		/** Returns the argument, multiplied by two.
		 * @param pArg The argument being doubled.
		 * @return The argument, multiplied by two.
		 */
		public int longParam(long pArg) { return (int) (pArg*2); }
		/** Returns the argument, multiplied by two.
		 * @param pArg The argument being doubled.
		 * @return The argument, multiplied by two.
		 */
		public long longResult(long pArg) { return pArg*2; }
		/** Returns the argument, multiplied by two.
		 * @param pArg The argument being doubled.
		 * @return The argument, multiplied by two.
		 */
		public double floatParam(float pArg) { return pArg*2; }
		/** Returns the argument, multiplied by two.
		 * @param pArg The argument being doubled.
		 * @return The argument, multiplied by two.
		 */
		public float floatResult(float pArg) { return pArg*2; }
		/** Returns the argument, multiplied by two.
		 * @param pArg The argument being doubled.
		 * @return The argument, multiplied by two.
		 */
		public double doubleParam(double pArg) { return pArg*2; }
		/** Returns the argument, multiplied by two.
		 * @param pArg The argument being doubled.
		 * @return The argument, multiplied by two.
		 */
		public double doubleResult(double pArg) { return pArg*2; }
		/** Returns the argument, concatenated with itself.
		 * @param pArg The argument being concatenated.
		 * @return The argument, concatenated with itself.
		 */
		public String stringParam(String pArg) { return pArg+pArg; }
        /**
         * Throws a NullPointerException.
         */
        public Object throwNPE() {
            throw new NullPointerException();
        }
		/** Returns the argument, concatenated with itself.
		 * @param pArg The argument being concatenated.
		 * @return The argument, concatenated with itself.
		 */
		public String nullableStringParam(String pArg) {
			if (pArg == null) {
				pArg = "";
			}
			return pArg+pArg;
		}
		/** Returns the argument, concatenated with itself.
		 * @param pArg The argument being concatenated.
		 * @return The argument, concatenated with itself.
		 */
		public String nullableStringResult(String pArg) {
			if (pArg == null) {
				return null;
			}
			return pArg+pArg;
		}
		/** Returns the sum of the bytes in the given byte array.
		 * @param pArg The array of bytes being added.
		 * @return Sum over the bytes in the array.
		 */
		public int byteArrayParam(byte[] pArg) {
			int sum = 0;
			for (int i = 0;  i < pArg.length;  i++) {
				sum += pArg[i];
			}
			return sum;
		}
		/** Returns an array with the bytes 0..pArg.
		 * @param pArg Requestes byte array length.
		 * @return Byte array with 0..pArg.
		 */
		public byte[] byteArrayResult(int pArg) {
			byte[] result = new byte[pArg];
			for (int i = 0;  i < result.length;  i++) {
				result[i] = (byte) i;
			}
			return result;
		}
		/** Returns the sum over the objects in the array.
		 * @param pArg Object array being added
		 * @return Sum over the objects in the array
		 */
		public int objectArrayParam(Object[] pArg) {
			int sum = 0;
			for (int i = 0;  i < pArg.length;  i++) {
				if (pArg[i] instanceof Number) {
					sum += ((Number) pArg[i]).intValue();
				} else {
					sum += Integer.parseInt((String) pArg[i]);
				}
			}
			return sum;
		}
		/** Returns an array of integers with the values
		 * 0..pArg.
		 * @param pArg Requested array length.
		 * @return Array of integers with the values 0..pArg
		 */
		public Object[] objectArrayResult(int pArg) {
			Object[] result = new Object[pArg];
			for (int i = 0;  i < result.length;  i++) {
				result[i] = new Integer(i);
			}
			return result;
		}
		/** Returns a sum over the entries in the map. Each
		 * key is multiplied with its value.
		 * @param pArg The map being iterated.
		 * @return Sum of keys, multiplied by their values.
		 */
		public int mapParam(Map pArg) {
			int sum = 0;
			for (Iterator iter = pArg.entrySet().iterator();  iter.hasNext();  ) {
				Map.Entry entry = (Map.Entry) iter.next();
				String key = (String) entry.getKey();
				Integer value = (Integer) entry.getValue();
				sum += Integer.parseInt(key) * value.intValue();
			}
			return sum;
		}
		/** Returns a map with the stringified values 0..pArg as
		 * keys and the corresponding integers as values.
		 * @param pArg Requested map size.
		 * @return Map with the keys "0".."pArg" and
		 * 0..pArg as values.
		 */
		public Map mapResult(int pArg) {
			Map result = new HashMap();
			for (int i = 0;  i < pArg;  i++) {
				result.put(Integer.toString(i), new Integer(i));
			}
			return result;
		}
		/** Returns the sum of all "int" nodes in <code>pNode</code>.
		 * @param pNode The node being counted.
		 * @return The sum of the values of all "int" nodes.
		 */
		public int nodeParam(Node pNode) {
			if (pNode.getNodeType() != Node.DOCUMENT_NODE) {
				throw new IllegalStateException("Expected document node, got " + pNode);
			}
			Element e = ((Document) pNode).getDocumentElement();
			if (!ROOT_TAG.equals(e.getLocalName()) || !INT_URI.equals(e.getNamespaceURI())) {
				throw new IllegalStateException("Expected root element 'root', got "
												+ new QName(e.getNamespaceURI(), e.getLocalName()));
			}
			return count(pNode);
		}
		private int count(Node pNode) {
			if (INT_TAG.equals(pNode.getLocalName())  &&  INT_URI.equals(pNode.getNamespaceURI())) {
				StringBuffer sb = new StringBuffer();
				for (Node child = pNode.getFirstChild();  child != null;  child = child.getNextSibling()) {
					if (child.getNodeType() == Node.TEXT_NODE  ||  child.getNodeType() == Node.CDATA_SECTION_NODE) {
						sb.append(child.getNodeValue());
					}
				}
				return Integer.parseInt(sb.toString());
			} else {
				int result = 0;
				for (Node child = pNode.getFirstChild();  child != null;  child = child.getNextSibling()) {
					if (child.getNodeType() == Node.ELEMENT_NODE) {
						result += count(child);
					}
				}
				return result;
			}
		}

		/** Example of a Serializable instance.
		 */
        public static class CalendarWrapper implements Serializable {
            private static final long serialVersionUID = 8153663910532549627L;
            final Calendar cal;
            CalendarWrapper(Calendar pCalendar) {
                cal = pCalendar;
            }
        }

        /** Returns the calendar value in milliseconds.
		 * @param pCal Calendar object
		 * @return <code>pCal.getTime().getTime()</code>.
		 */
		public long serializableParam(CalendarWrapper pCal) {
			return pCal.cal.getTime().getTime();
		}

		/** Returns midnight of the following day.
		 */
        public Calendar calendarParam(Calendar pCal) {
            Calendar cal = (Calendar) pCal.clone();
            cal.add(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal;
        }

        /** Returns midnight of the following day.
         */
        public Date dateParam(Date pDate) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(pDate);
            return calendarParam(cal).getTime();
        }
    }

    protected XmlRpcHandlerMapping getHandlerMapping() throws IOException, XmlRpcException {
        return getHandlerMapping("BaseTest.properties");
	}

	/** Test, whether we can invoke a method, passing a byte value.
	 * @throws Exception The test failed.
	 */
	public void testByteParam() throws Exception {
		for (int i = 0;  i < providers.length;  i++) {
			testByteParam(providers[i]);
		}
	}

	private void testByteParam(ClientProvider pProvider) throws Exception {
		final String methodName = "Remote.byteParam";
		final Object[] params = new Object[]{new Byte((byte) 3)};
		XmlRpcClient client = pProvider.getClient();
		Object result = client.execute(getExConfig(pProvider), methodName, params);
		assertEquals(new Integer(6), result);
		boolean ok = false;
		try {
			client.execute(getConfig(pProvider), methodName, params);
		} catch (XmlRpcExtensionException e) {
			ok = true;
		}
		assertTrue(ok);
	}

	/** Test, whether we can invoke a method, returning a byte.
	 * @throws Exception The test failed.
	 */
	public void testByteResult() throws Exception {
		for (int i = 0;  i < providers.length;  i++) {
			testByteResult(providers[i]);
		}
	}

	private void testByteResult(ClientProvider pProvider) throws Exception {
		final String methodName = "Remote.byteResult";
		final Object[] params = new Object[]{new Byte((byte) 3)};
		final XmlRpcClient client = pProvider.getClient();
		Object result = client.execute(getExConfig(pProvider), methodName, params);
		assertEquals(new Byte((byte) 6), result);
		boolean ok = false;
		try {
			client.execute(getConfig(pProvider), methodName, params);
		} catch (XmlRpcExtensionException e) {
			ok = true;
		}
		assertTrue(ok);
	}

	/** Test, whether we can invoke a method, passing a short value.
	 * @throws Exception The test failed.
	 */
	public void testShortParam() throws Exception {
		for (int i = 0;  i < providers.length;  i++) {
			testShortParam(providers[i]);
		}
	}

	private void testShortParam(ClientProvider pProvider) throws Exception {
		final String methodName = "Remote.shortParam";
		final Object[] params = new Object[]{new Short((short) 4)};
		final XmlRpcClient client = pProvider.getClient();
		Object result = client.execute(getExConfig(pProvider), methodName, params);
		assertEquals(new Integer(8), result);
		boolean ok = false;
		try {
			client.execute(getConfig(pProvider), methodName, params);
		} catch (XmlRpcExtensionException e) {
			ok = true;
		}
		assertTrue(ok);
	}

	/** Test, whether we can invoke a method, returning a short value.
	 * @throws Exception The test failed.
	 */
	public void testShortResult() throws Exception {
		for (int i = 0;  i < providers.length;  i++) {
			testShortResult(providers[i]);
		}
	}

	private void testShortResult(ClientProvider pProvider) throws Exception {
		final String methodName = "Remote.shortResult";
		final Object[] params = new Object[]{new Short((short) 4)};
		final XmlRpcClient client = pProvider.getClient();
		Object result = client.execute(getExConfig(pProvider), methodName, params);
		assertEquals(new Short((short) 8), result);
		boolean ok = false;
		try {
			client.execute(getConfig(pProvider), methodName, params);
		} catch (XmlRpcExtensionException e) {
			ok = true;
		}
		assertTrue(ok);
	}

	/** Test, whether we can invoke a method, passing an
	 * integer value.
	 * @throws Exception The test failed.
	 */
	public void testIntParam() throws Exception {
		for (int i = 0;  i < providers.length;  i++) {
			testIntParam(providers[i]);
		}
	}

	private void testIntParam(ClientProvider pProvider) throws Exception {
		final String methodName = "Remote.intParam";
		final Object[] params = new Object[]{new Integer(5)};
		final XmlRpcClient client = pProvider.getClient();
		Object result = client.execute(getConfig(pProvider), methodName, params);
		assertEquals(new Integer(10), result);
		result = client.execute(getExConfig(pProvider), methodName, params);
		assertEquals(new Integer(10), result);
	}

	/** Test, whether we can invoke a method, passing a long value.
	 * @throws Exception The test failed.
	 */
	public void testLongParam() throws Exception {
		for (int i = 0;  i < providers.length;  i++) {
			testLongParam(providers[i]);
		}
	}

	private void testLongParam(ClientProvider pProvider) throws Exception {
		final String methodName = "Remote.longParam";
		final Object[] params = new Object[]{new Long(6L)};
		final XmlRpcClient client = pProvider.getClient();
		Object result = client.execute(getExConfig(pProvider), methodName, params);
		assertEquals(new Integer(12), result);
		boolean ok = false;
		try {
			client.execute(getConfig(pProvider), methodName, params);
		} catch (XmlRpcExtensionException e) {
			ok = true;
		}
		assertTrue(ok);
	}

	/** Test, whether we can invoke a method, returning a long value.
	 * @throws Exception The test failed.
	 */
	public void testLongResult() throws Exception {
		for (int i = 0;  i < providers.length;  i++) {
			testLongResult(providers[i]);
		}
	}

	private void testLongResult(ClientProvider pProvider) throws Exception {
		final String methodName = "Remote.longResult";
		final Object[] params = new Object[]{new Long(6L)};
		final XmlRpcClient client = pProvider.getClient();
		Object result = client.execute(getExConfig(pProvider), methodName, params);
		assertEquals(new Long(12L), result);
		boolean ok = false;
		try {
			client.execute(getConfig(pProvider), methodName, params);
		} catch (XmlRpcExtensionException e) {
			ok = true;
		}
		assertTrue(ok);
	}

	/** Test, whether we can invoke a method, passing a
	 * string value.
	 * @throws Exception The test failed.
	 */
	public void testStringParam() throws Exception {
		for (int i = 0;  i < providers.length;  i++) {
			testStringParam(providers[i]);
		}
	}

	private void testStringParam(ClientProvider pProvider) throws Exception {
		final String methodName = "Remote.stringParam";
		final Object[] params = new Object[]{"abc"};
		final XmlRpcClient client = pProvider.getClient();
		Object result = client.execute(getConfig(pProvider), methodName, params);
		assertEquals("abcabc", result);
		result = client.execute(getExConfig(pProvider), methodName, params);
		assertEquals("abcabc", result);
	}

	/** Test, whether we can invoke a method, passing a
	 * string value or null.
	 * @throws Exception The test failed.
	 */
	public void testNullableStringParam() throws Exception {
		for (int i = 0;  i < providers.length;  i++) {
			testNullableStringParam(providers[i]);
		}
	}

	private void testNullableStringParam(ClientProvider pProvider) throws Exception {
		final String methodName = "Remote.nullableStringParam";
		final Object[] params = new Object[]{"abc"};
		final XmlRpcClient client = pProvider.getClient();
		Object result = client.execute(getConfig(pProvider), methodName, params);
		assertEquals("abcabc", result);
		result = client.execute(getExConfig(pProvider), methodName, params);
		assertEquals("abcabc", result);
		final Object[] nullParams = new Object[]{null};
		result = client.execute(getExConfig(pProvider), methodName, nullParams);
		assertEquals("", result);
		boolean ok = false;
		try {
			client.execute(getConfig(pProvider), methodName, nullParams);
		} catch (XmlRpcExtensionException e) {
			ok = true;
		}
		assertTrue(ok);
	}

	/** Test, whether we can invoke a method, returning a
	 * string value or null.
	 * @throws Exception The test failed.
	 */
	public void testNullableStringResult() throws Exception {
		for (int i = 0;  i < providers.length;  i++) {
			testNullableStringResult(providers[i]);
		}
	}

	private void testNullableStringResult(ClientProvider pProvider) throws Exception {
		final String methodName = "Remote.nullableStringResult";
		final Object[] params = new Object[]{"abc"};
		final XmlRpcClient client = pProvider.getClient();
		Object result = client.execute(getConfig(pProvider), methodName, params);
		assertEquals("abcabc", result);
		result = client.execute(getExConfig(pProvider), methodName, params);
		assertEquals("abcabc", result);
		final Object[] nullParams = new Object[]{null};
		result = client.execute(getExConfig(pProvider), methodName, nullParams);
		assertEquals(null, result);
		boolean ok = false;
		try {
			client.execute(getConfig(pProvider), methodName, nullParams);
		} catch (XmlRpcExtensionException e) {
			ok = true;
		}
		assertTrue(ok);
	}

	/** Test, whether we can invoke a method, passing a float value.
	 * @throws Exception The test failed.
	 */
	public void testFloatParam() throws Exception {
		for (int i = 0;  i < providers.length;  i++) {
			testFloatParam(providers[i]);
		}
	}

	private void testFloatParam(ClientProvider pProvider) throws Exception {
		final String methodName = "Remote.floatParam";
		final Object[] params = new Object[]{new Float(0.4)};
		final XmlRpcClient client = pProvider.getClient();
		Object result = client.execute(getExConfig(pProvider), methodName, params);
		assertEquals(8, Math.round(((Double) result).doubleValue()*10));
		boolean ok = false;
		try {
			client.execute(getConfig(pProvider), methodName, params);
		} catch (XmlRpcExtensionException e) {
			ok = true;
		}
		assertTrue(ok);
	}

	/** Test, whether we can invoke a method, returning a float value.
	 * @throws Exception The test failed.
	 */
	public void testFloatResult() throws Exception {
		for (int i = 0;  i < providers.length;  i++) {
			testFloatResult(providers[i]);
		}
	}

	private void testFloatResult(ClientProvider pProvider) throws Exception {
		final String methodName = "Remote.floatResult";
		final Object[] params = new Object[]{new Float(0.4)};
		final XmlRpcClient client = pProvider.getClient();
		Object result = client.execute(getExConfig(pProvider), methodName, params);
		assertEquals(new Float(0.8), result);
		boolean ok = false;
		try {
			client.execute(getConfig(pProvider), methodName, params);
		} catch (XmlRpcExtensionException e) {
			ok = true;
		}
		assertTrue(ok);
	}

	/** Test, whether we can invoke a method, passing a
	 * double value.
	 * @throws Exception The test failed.
	 */
	public void testDoubleParam() throws Exception {
		for (int i = 0;  i < providers.length;  i++) {
			testDoubleParam(providers[i]);
		}
	}

	private void testDoubleParam(ClientProvider pProvider) throws Exception {
		final String methodName = "Remote.doubleParam";
		final Object[] params = new Object[]{new Double(0.6)};
		final XmlRpcClient client = pProvider.getClient();
		Object result = client.execute(getConfig(pProvider), methodName, params);
		assertEquals(new Double(1.2), result);
		result = client.execute(getExConfig(pProvider), methodName, params);
		assertEquals(new Double(1.2), result);
	}

	/** Test, whether we can invoke a method, returning a
	 * double value.
	 * @throws Exception The test failed.
	 */
	public void testDoubleResult() throws Exception {
		for (int i = 0;  i < providers.length;  i++) {
			testDoubleResult(providers[i]);
		}
	}

	private void testDoubleResult(ClientProvider pProvider) throws Exception {
		final String methodName = "Remote.doubleResult";
		final Object[] params = new Object[]{new Double(0.6)};
		final XmlRpcClient client = pProvider.getClient();
		Object result = client.execute(getConfig(pProvider), methodName, params);
		assertEquals(new Double(1.2), result);
		result = client.execute(getExConfig(pProvider), methodName, params);
		assertEquals(new Double(1.2), result);
	}

	/** Test, whether we can invoke a method, passing a
	 * byte array.
	 * @throws Exception The test failed.
	 */
	public void testByteArrayParam() throws Exception {
		for (int i = 0;  i < providers.length;  i++) {
			testByteArrayParam(providers[i]);
		}
	}

	private void testByteArrayParam(ClientProvider pProvider) throws Exception {
		final byte[] bytes = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
		final String methodName = "Remote.byteArrayParam";
		final Object[] params = new Object[]{bytes};
		final XmlRpcClient client = pProvider.getClient();
		Object result = client.execute(getConfig(pProvider), methodName, params);
		assertEquals(new Integer(0+1+2+3+4+5+6+7+8+9), result);
		result = client.execute(getExConfig(pProvider), methodName, params);
		assertEquals(new Integer(0+1+2+3+4+5+6+7+8+9), result);
	}

	/** Test, whether we can invoke a method, returning a
	 * byte array.
	 * @throws Exception The test failed.
	 */
	public void testByteArrayResult() throws Exception {
		for (int i = 0;  i < providers.length;  i++) {
			testByteArrayResult(providers[i]);
		}
	}

	private void testByteArrayResult(ClientProvider pProvider) throws Exception {
		final byte[] bytes = new byte[]{0, 1, 2, 3, 4, 5, 6, 7};
		final String methodName = "Remote.byteArrayResult";
		final Object[] params = new Object[]{new Integer(8)};
		final XmlRpcClient client = pProvider.getClient();
		Object result = client.execute(getConfig(pProvider), methodName, params);
		assertTrue(Arrays.equals(bytes, (byte[]) result));
		result = client.execute(getExConfig(pProvider), methodName, params);
		assertTrue(Arrays.equals(bytes, (byte[]) result));
	}

	/** Test, whether we can invoke a method, passing an
	 * object array.
	 * @throws Exception The test failed.
	 */
	public void testObjectArrayParam() throws Exception {
		for (int i = 0;  i < providers.length;  i++) {
			testObjectArrayParam(providers[i]);
		}
	}

	private void testObjectArrayParam(ClientProvider pProvider) throws Exception {
		final Object[] objects = new Object[]{new Byte((byte) 1), new Short((short) 2),
											  new Integer(3), new Long(4), "5"};
		final String methodName = "Remote.objectArrayParam";
		final Object[] params = new Object[]{objects};
		final XmlRpcClient client = pProvider.getClient();
		boolean ok = false;
		try {
			client.execute(getConfig(pProvider), methodName, params);
		} catch (XmlRpcExtensionException e) {
			ok = true;
		}
		assertTrue(ok);
		Object result = client.execute(getExConfig(pProvider), methodName, params);
		assertEquals(new Integer(15), result);
	}

	/** Test, whether we can invoke a method, returning an
	 * object array.
	 * @throws Exception The test failed.
	 */
	public void testObjectArrayResult() throws Exception {
		for (int i = 0;  i < providers.length;  i++) {
			testObjectArrayResult(providers[i]);
		}
	}

	private void testObjectArrayResult(ClientProvider pProvider) throws Exception {
		final Object[] objects = new Object[]{new Integer(0), new Integer(1),
											  new Integer(2), new Integer(3)};
		final String methodName = "Remote.objectArrayResult";
		final Object[] params = new Object[]{new Integer(4)};
		final XmlRpcClient client = pProvider.getClient();
		Object result = client.execute(getConfig(pProvider), methodName, params);
		assertTrue(Arrays.equals(objects, (Object[]) result));
		result = client.execute(getExConfig(pProvider), methodName, params);
		assertTrue(Arrays.equals(objects, (Object[]) result));
	}

	/** Test, whether we can invoke a method, passing a map.
	 * @throws Exception The test failed.
	 */
	public void testMapParam() throws Exception {
		for (int i = 0;  i < providers.length;  i++) {
			testMapParam(providers[i]);
		}
	}

	private void testMapParam(ClientProvider pProvider) throws Exception {
		final Map map = new HashMap();
		map.put("2", new Integer(3));
		map.put("3", new Integer(5));
		final String methodName = "Remote.mapParam";
		final Object[] params = new Object[]{map};
		final XmlRpcClient client = pProvider.getClient();
		Object result = client.execute(getConfig(pProvider), methodName, params);
		assertEquals(new Integer(21), result);
		result = client.execute(getExConfig(pProvider), methodName, params);
		assertEquals(new Integer(21), result);
	}

	private void checkMap(Map pResult) {
		assertEquals(4, pResult.size());
		assertEquals(new Integer(0), pResult.get("0"));
		assertEquals(new Integer(1), pResult.get("1"));
		assertEquals(new Integer(2), pResult.get("2"));
		assertEquals(new Integer(3), pResult.get("3"));
	}

	/** Test, whether we can invoke a method, returning a map.
	 * @throws Exception The test failed.
	 */
	public void testMapResult() throws Exception {
		for (int i = 0;  i < providers.length;  i++) {
			testMapResult(providers[i]);
		}
	}

	private void testMapResult(ClientProvider pProvider) throws Exception {
		final String methodName = "Remote.mapResult";
		final Object[] params = new Object[]{new Integer(4)};
		final XmlRpcClient client = pProvider.getClient();
		Object result = client.execute(getConfig(pProvider), methodName, params);
		checkMap((Map) result);
		result = client.execute(getExConfig(pProvider), methodName, params);
		checkMap((Map) result);
	}

	/** Test, whether we can invoke a method, passing a DOM
	 * node as parameter.
	 * @throws Exception The test failed.
	 */
	public void testNodeParam() throws Exception {
		for (int i = 0;  i < providers.length;  i++) {
			testNodeParam(providers[i]);
		}
	}

	private static final String ROOT_TAG = "root";
	private static final String INT_TAG = "int";
	private static final String INT_URI = "http://ws.apache.org/xmlrpc/namespaces/testNodeParam";

	private void testNodeParam(ClientProvider pProvider) throws Exception {
		final String xml =
			"<" + ROOT_TAG + " xmlns='" + INT_URI +"'>" +
			"  <" + INT_TAG + ">1</" + INT_TAG + ">" +
			"  <" + INT_TAG + ">2</" + INT_TAG + ">" +
			"  <" + INT_TAG + ">3</" + INT_TAG + ">" +
			"  <" + INT_TAG + ">4</" + INT_TAG + ">" +
			"  <" + INT_TAG + ">5</" + INT_TAG + ">" +
			"</" + ROOT_TAG + ">";
		final String methodName = "Remote.nodeParam";
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(false);
		dbf.setNamespaceAware(true);
		Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
		final Object[] params = new Object[]{doc};
		final XmlRpcClient client = pProvider.getClient();
		Object result = client.execute(getExConfig(pProvider), methodName, params);
		assertEquals(new Integer(1+2+3+4+5), result);
		boolean ok = false;
		try {
			client.execute(getConfig(pProvider), methodName, params);
		} catch (XmlRpcExtensionException e) {
			ok = true;
		}
		assertTrue(ok);
	}

	/** Test, whether we can invoke a method, passing an instance of
	 * {@link java.io.Serializable} as a parameter.
	 * @throws Exception The test failed.
	 */
	public void testSerializableParam() throws Exception {
		for (int i = 0;  i < providers.length;  i++) {
			testSerializableParam(providers[i]);
		}
	}

	private void testSerializableParam(ClientProvider pProvider) throws Exception {
		final String methodName = "Remote.serializableParam";
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		cal.set(2005, 5, 23, 8, 4, 0);
		cal.set(Calendar.MILLISECOND, 5);
		final Object[] params = new Object[]{new Remote.CalendarWrapper(cal)};
		final XmlRpcClient client = pProvider.getClient();
		Object result = client.execute(getExConfig(pProvider), methodName, params);
		assertEquals(new Long(cal.getTime().getTime()), result);
		boolean ok = false;
		try {
			client.execute(getConfig(pProvider), methodName, params);
		} catch (XmlRpcExtensionException e) {
			ok = true;
		}
		assertTrue(ok);
	}

	/** Tests, whether we can invoke a method, passing an instance of
     * {@link Calendar} as a parameter.
     * @throws Exception The test failed.
	 */
	public void testCalendarParam() throws Exception {
	    for (int i = 0;  i < providers.length;  i++) {
	        testCalendarParam(providers[i]);
        }
    }

	private void testCalendarParam(ClientProvider pProvider) throws Exception {
	    final String methodName = "Remote.calendarParam";
        Calendar cal1 = newCalendarParam();
        Calendar cal2 = newCalendarResult();
        final Object[] params = new Object[]{cal1};
        final XmlRpcClient client = pProvider.getClient();
        Object result = client.execute(getExConfig(pProvider), methodName, params);
        assertEquals(cal2.getTime(), ((Calendar) result).getTime());
        boolean ok = false;
        try {
            client.execute(getConfig(pProvider), methodName, params);
        } catch (XmlRpcExtensionException e) {
            ok = true;
        }
        assertTrue(ok);
    }

    private Calendar newCalendarResult() {
        Calendar cal2 = Calendar.getInstance(TimeZone.getDefault());
        cal2.set(2005, 5, 24, 0, 0, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        return cal2;
    }

    private Calendar newCalendarParam() {
        Calendar cal1 = Calendar.getInstance(TimeZone.getDefault());
        cal1.set(2005, 5, 23, 8, 4, 0);
        cal1.set(Calendar.MILLISECOND, 5);
        return cal1;
    }

    /** Tests, whether we can invoke a method, passing an instance of
     * {@link Date} as a parameter.
     * @throws Exception The test failed.
     */
    public void testDateParam() throws Exception {
        for (int i = 0;  i < providers.length;  i++) {
            testDateParam(providers[i]);
        }
    }

    private void testDateParam(ClientProvider pProvider) throws Exception {
        final String methodName = "Remote.dateParam";
        Date date1 = newCalendarParam().getTime();
        Calendar cal2 = newCalendarResult();
        final Object[] params = new Object[]{date1};
        final XmlRpcClient client = pProvider.getClient();
        Object result = client.execute(getExConfig(pProvider), methodName, params);
        assertEquals(cal2.getTime(), result);
        result = client.execute(getConfig(pProvider), methodName, params);
        assertEquals(cal2.getTime(), result);
    }

    /**
     * Tests, whether a NullPointerException, thrown by the server, can be
     * trapped by the client.
     */
    public void testCatchNPE() throws Exception {
        for (int i = 0;  i < providers.length;  i++) {
            testCatchNPE(providers[i]);
        }
    }

    private void testCatchNPE(ClientProvider pProvider) throws Exception {
        final XmlRpcClient client = pProvider.getClient();
        final String methodName = "Remote.throwNPE";
        try {
            client.execute(getExConfig(pProvider), methodName, (Object[]) null); 
        } catch (XmlRpcInvocationException e) {
            if (!(e.getCause() instanceof NullPointerException)) {
                throw e;
            }
        }
    }
}
