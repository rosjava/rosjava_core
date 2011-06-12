/*
 * Copyright 1999,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ws.commons.util.test;

import java.io.StringWriter;
import java.util.Arrays;

import org.apache.ws.commons.util.Base64;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import junit.framework.TestCase;


/** A test case for the Base64 encoder/decoder.
 */
public class Base64Test extends TestCase {
	/** Test for the base64 decoder/encoder.
	 * @throws Exception The test failed.
	 */
	public void testBase64() throws Exception {
		for (int i = 0;  i <= 256;  i++) {
			byte[] bytes = new byte[i];
			for (int j = 0;  j < i;  j++) {
				bytes[j] = (byte) j;
			}
			String s = Base64.encode(bytes);
			byte[] result = Base64.decode(s);
			assertTrue(Arrays.equals(bytes, result));
		}
	}

	/** Test for the base64 SAX encoder.
	 * @throws Exception The test failed.
	 */
	public void testSAXEncoder() throws Exception {
		for (int i = 0;  i <= 256;  i++) {
			byte[] bytes = new byte[i];
			for (int j = 0;  j < i;  j++) {
				bytes[j] = (byte) j;
			}
			final StringWriter sw = new StringWriter();
			ContentHandler ch = new DefaultHandler(){
				public void characters(char[] pChars, int pOffset, int pLen) throws SAXException {
					sw.write(pChars, pOffset, pLen);
				}
			};
			Base64.SAXEncoder encoder = new Base64.SAXEncoder(new char[4096], 0, null, ch);
			Base64.EncoderOutputStream eos = new Base64.EncoderOutputStream(encoder);
			eos.write(bytes);
			eos.close();
			String s = sw.toString();
			byte[] result = Base64.decode(s);
			assertTrue(Arrays.equals(bytes, result));
		}
	}
}
