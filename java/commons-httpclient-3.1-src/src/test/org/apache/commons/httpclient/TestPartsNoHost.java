/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/TestPartsNoHost.java,v 1.7 2004/02/22 18:08:49 olegk Exp $
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
 * [Additional notices, if required by prior licensing conditions]
 *
 */

package org.apache.commons.httpclient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.StringPart;

/**
 * @author <a href="mailto:adrian@ephox.com">Adrian Sutton</a>
 * @version $Revision: 480424 $ $Date: 2006-11-29 06:56:49 +0100 (Wed, 29 Nov 2006) $
 */
public class TestPartsNoHost extends TestCase {

    static final String PART_DATA = "This is the part data.";
    static final String NAME = "name";

    // ------------------------------------------------------------ Constructor

    public TestPartsNoHost(String testName) {
        super(testName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestPartsNoHost.class);
    }

    // ----------------------------------------------------------------- Tests

    public void testFilePartResendsFileData() throws Exception {
        File file = createTempTestFile();
        FilePart part = new FilePart(NAME, file);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        part.send(stream);
        String resp1 = stream.toString();
        stream = new ByteArrayOutputStream();
        part.send(stream);
        String resp2 = stream.toString();
        file.delete();
        assertEquals(resp1, resp2);
    }

    public void testStringPartResendsData() throws Exception {
        StringPart part = new StringPart(NAME, PART_DATA);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        part.send(stream);
        String resp1 = stream.toString();
        stream = new ByteArrayOutputStream();
        part.send(stream);
        String resp2 = stream.toString();
        assertEquals(resp1, resp2);
    }

    public void testFilePartNullFileResendsData() throws Exception {
        FilePart part = new FilePart(NAME, "emptyfile.ext", null);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        part.send(stream);
        String resp1 = stream.toString();
        stream = new ByteArrayOutputStream();
        part.send(stream);
        String resp2 = stream.toString();
        assertEquals(resp1, resp2);
    }


    /** Writes PART_DATA out to a temporary file and returns the file it
     * was written to.
     * @return the File object representing the file the data was
     * written to.
     */
    private File createTempTestFile() throws IOException {
        File file = File.createTempFile("FilePartTest", ".txt");
        PrintWriter out = new PrintWriter(new FileWriter(file));
        out.println(PART_DATA);
        out.flush();
        out.close();
        return file;
    }
}
