/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/TestStreams.java,v 1.19 2004/10/31 14:04:13 olegk Exp $
 * $Revision: 505890 $
 * $Date: 2007-02-11 12:25:25 +0100 (Sun, 11 Feb 2007) $
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
 * [Additional notices, if required by prior licensing conditions]
 *
 */

package org.apache.commons.httpclient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.util.EncodingUtil;


public class TestStreams extends TestCase {

    private static final String CONTENT_CHARSET = "ISO-8859-1";
    
    public TestStreams(String testName) {
        super(testName);
    }

    public void testChunkedInputStream() throws IOException {
        String correctInput = "10;key=\"value\r\nnewline\"\r\n1234567890123456\r\n5\r\n12345\r\n0\r\nFooter1: abcde\r\nFooter2: fghij\r\n";
        String correctResult = "123456789012345612345";
        HttpMethod method = new FakeHttpMethod();

        //Test for when buffer is larger than chunk size
        InputStream in = new ChunkedInputStream(new ByteArrayInputStream(
            EncodingUtil.getBytes(correctInput, CONTENT_CHARSET)), method);
        byte[] buffer = new byte[300];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len;
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
        String result = EncodingUtil.getString(out.toByteArray(), CONTENT_CHARSET);
        assertEquals(result, correctResult);
        Header footer = method.getResponseFooter("footer1");
        assertEquals(footer.getValue(), "abcde");
        footer = method.getResponseFooter("footer2");
        assertEquals(footer.getValue(), "fghij");

        method = new FakeHttpMethod();

        //Test for when buffer is smaller than chunk size.
        in = new ChunkedInputStream(new ByteArrayInputStream(
            EncodingUtil.getBytes(correctInput, CONTENT_CHARSET)), method);
        buffer = new byte[7];
        out = new ByteArrayOutputStream();
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
        result = EncodingUtil.getString(out.toByteArray(), CONTENT_CHARSET);
        assertEquals(result, correctResult);
        footer = method.getResponseFooter("footer1");
        assertEquals(footer.getValue(), "abcde");
        footer = method.getResponseFooter("footer2");
        assertEquals(footer.getValue(), "fghij");
    }

    public void testCorruptChunkedInputStream1() throws IOException {
        //missing \r\n at the end of the first chunk
        String corrupInput = "10;key=\"value\"\r\n123456789012345\r\n5\r\n12345\r\n0\r\nFooter1: abcde\r\nFooter2: fghij\r\n";
        HttpMethod method = new FakeHttpMethod();

        InputStream in = new ChunkedInputStream(new ByteArrayInputStream(
            EncodingUtil.getBytes(corrupInput, CONTENT_CHARSET)), method);
        byte[] buffer = new byte[300];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len;
        try {
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            fail("Should have thrown exception");
        } catch(IOException e) {
            /* expected exception */
        }
    }

    public void testEmptyChunkedInputStream() throws IOException {
        String input = "0\r\n";
        HttpMethod method = new FakeHttpMethod();

        InputStream in = new ChunkedInputStream(new ByteArrayInputStream(
            EncodingUtil.getBytes(input, CONTENT_CHARSET)), method);
        byte[] buffer = new byte[300];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len;
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
        assertEquals(0, out.size());
    }

    public void testContentLengthInputStream() throws IOException {
        String correct = "1234567890123456";
        InputStream in = new ContentLengthInputStream(new ByteArrayInputStream(
            EncodingUtil.getBytes(correct, CONTENT_CHARSET)), 10L);
        byte[] buffer = new byte[50];
        int len = in.read(buffer);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(buffer, 0, len);
        String result = EncodingUtil.getString(out.toByteArray(), CONTENT_CHARSET);
        assertEquals(result, "1234567890");
    }

    public void testContentLengthInputStreamSkip() throws IOException {
        InputStream in = new ContentLengthInputStream(new ByteArrayInputStream(new byte[20]), 10L);
        assertEquals(10, in.skip(10));
        assertTrue(in.read() == -1);

        in = new ContentLengthInputStream(new ByteArrayInputStream(new byte[20]), 10L);
        in.read();
        assertEquals(9, in.skip(10));
        assertTrue(in.read() == -1);

        in = new ContentLengthInputStream(new ByteArrayInputStream(new byte[20]), 2L);
        in.read();
        in.read();
        assertTrue(in.skip(10) <= 0);
        assertTrue(in.read() == -1);
    }

    public void testChunkedConsitance() throws IOException {
        String input = "76126;27823abcd;:q38a-\nkjc\rk%1ad\tkh/asdui\r\njkh+?\\suweb";
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        OutputStream out = new ChunkedOutputStream(buffer);
        out.write(EncodingUtil.getBytes(input, CONTENT_CHARSET));
        out.close();
        buffer.close();
        InputStream in = new ChunkedInputStream(new ByteArrayInputStream(buffer.toByteArray()), new GetMethod());

        byte[] d = new byte[10];
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        int len = 0;
        while ((len = in.read(d)) > 0) {
            result.write(d, 0, len);
        }

        String output = EncodingUtil.getString(result.toByteArray(), CONTENT_CHARSET);
        assertEquals(input, output);
    }

    public void testChunkedOutputStream() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ChunkedOutputStream out = new ChunkedOutputStream(buffer, 2);
        out.write('1');  
        out.write('2');  
        out.write('3');  
        out.write('4');  
        out.finish();
        out.close();
        
        byte [] rawdata =  buffer.toByteArray();
        
        assertEquals(19, rawdata.length);
        assertEquals('2', rawdata[0]);
        assertEquals('\r', rawdata[1]);
        assertEquals('\n', rawdata[2]);
        assertEquals('1', rawdata[3]);
        assertEquals('2', rawdata[4]);
        assertEquals('\r', rawdata[5]);
        assertEquals('\n', rawdata[6]);
        assertEquals('2', rawdata[7]);
        assertEquals('\r', rawdata[8]);
        assertEquals('\n', rawdata[9]);
        assertEquals('3', rawdata[10]);
        assertEquals('4', rawdata[11]);
        assertEquals('\r', rawdata[12]);
        assertEquals('\n', rawdata[13]);
        assertEquals('0', rawdata[14]);
        assertEquals('\r', rawdata[15]);
        assertEquals('\n', rawdata[16]);
        assertEquals('\r', rawdata[17]);
        assertEquals('\n', rawdata[18]);
    }

    public void testChunkedOutputStreamLargeChunk() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ChunkedOutputStream out = new ChunkedOutputStream(buffer, 2);
        out.write(new byte[] {'1', '2', '3', '4'});
        out.finish();
        out.close();
        
        byte [] rawdata =  buffer.toByteArray();
        
        assertEquals(14, rawdata.length);
        assertEquals('4', rawdata[0]);
        assertEquals('\r', rawdata[1]);
        assertEquals('\n', rawdata[2]);
        assertEquals('1', rawdata[3]);
        assertEquals('2', rawdata[4]);
        assertEquals('3', rawdata[5]);
        assertEquals('4', rawdata[6]);
        assertEquals('\r', rawdata[7]);
        assertEquals('\n', rawdata[8]);
        assertEquals('0', rawdata[9]);
        assertEquals('\r', rawdata[10]);
        assertEquals('\n', rawdata[11]);
        assertEquals('\r', rawdata[12]);
        assertEquals('\n', rawdata[13]);
    }

    public void testChunkedOutputStreamSmallChunk() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ChunkedOutputStream out = new ChunkedOutputStream(buffer, 2);
        out.write('1');  
        out.finish();
        out.close();
        
        byte [] rawdata =  buffer.toByteArray();
        
        assertEquals(11, rawdata.length);
        assertEquals('1', rawdata[0]);
        assertEquals('\r', rawdata[1]);
        assertEquals('\n', rawdata[2]);
        assertEquals('1', rawdata[3]);
        assertEquals('\r', rawdata[4]);
        assertEquals('\n', rawdata[5]);
        assertEquals('0', rawdata[6]);
        assertEquals('\r', rawdata[7]);
        assertEquals('\n', rawdata[8]);
        assertEquals('\r', rawdata[9]);
        assertEquals('\n', rawdata[10]);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestStreams.class);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestStreams.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }


    public void testAutoCloseInputStream() throws IOException {
        // The purpose of this test is to check EOF handling of ACIS with
        // respect to exceptions being thrown. Putting it on top of a
        // plain ByteArrayInputStream won't do, since BAIS can't be closed.
        ByteArrayInputStream bais =
            new ByteArrayInputStream("whatever".getBytes());
        InputStream fbais = new java.io.FilterInputStream(bais) {
                private boolean closed = false;
                public void close() throws IOException {
                    closed = true;
                    super.close();
                }
                public int available() throws IOException {
                    if (closed)
                        throw new IOException("closed");
                    return super.available();
                }
            };

        AutoCloseInputStream acis = new AutoCloseInputStream(fbais, null);
        byte[] data = new byte[16];
        int count = 0;
        while (count >= 0) {
            count = acis.read(data);
        }
        // We're at EOF. The underlying stream should be closed,
        // but the ACIS itself not.
        try {
            fbais.available();
            fail("underlying stream not auto-closed");
        } catch (IOException x) {
            // expected, pis should be closed
        }

        // don't want to see an exception being thrown here
        acis.available();

        acis.close();
        try {
            acis.available();
            fail("auto-close stream not closed");
        } catch (IOException x) {
            // expected, acis should be closed
        }
    }
}

