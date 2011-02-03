/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/TestPostMethod.java,v 1.5 2004/12/12 10:02:38 olegk Exp $
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;

/**
 * Tests basic method functionality.
 *
 * @author Remy Maucherat
 * @author Rodney Waldhoff
 * 
 * @version $Id: TestPostParameterEncoding.java 480424 2006-11-29 05:56:49Z bayard $
 */
public class TestPostParameterEncoding extends TestCase {

    static final String NAME = "name", VALUE = "value";
    static final String NAME0 = "name0", VALUE0 = "value0";
    static final String NAME1 = "name1", VALUE1 = "value1";
    static final String NAME2 = "name2", VALUE2 = "value2";

    static final NameValuePair PAIR = new NameValuePair(NAME, VALUE);
    static final NameValuePair PAIR0 = new NameValuePair(NAME0, VALUE0);
    static final NameValuePair PAIR1 = new NameValuePair(NAME1, VALUE1);
    static final NameValuePair PAIR2 = new NameValuePair(NAME2, VALUE2);

    public TestPostParameterEncoding(final String testName) throws IOException {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestPostParameterEncoding.class);
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestPostParameterEncoding.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }
    
    private String getRequestAsString(RequestEntity entity) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        entity.writeRequest(bos);
        return new String(bos.toByteArray(), "UTF-8");
    }
    
    public void testPostParametersEncoding() throws Exception {
        PostMethod post = new PostMethod();
        post.setRequestBody(new NameValuePair[] { PAIR });
        assertEquals("name=value", getRequestAsString(post.getRequestEntity()));

        post.setRequestBody(new NameValuePair[]{ PAIR, PAIR1, PAIR2 });
        assertEquals("name=value&name1=value1&name2=value2", 
            getRequestAsString(post.getRequestEntity()));

        post.setRequestBody(new NameValuePair[]{ PAIR, PAIR1, PAIR2, new NameValuePair("hasSpace", "a b c d") });
        assertEquals("name=value&name1=value1&name2=value2&hasSpace=a+b+c+d",
            getRequestAsString(post.getRequestEntity()));

        post.setRequestBody(new NameValuePair[]{ new NameValuePair("escaping", ",.-\u00f6\u00e4\u00fc!+@#*&()=?:;}{[]$") });
        assertEquals("escaping=%2C.-%F6%E4%FC%21%2B%40%23*%26%28%29%3D%3F%3A%3B%7D%7B%5B%5D%24",
            getRequestAsString(post.getRequestEntity()));
        
    }

    public void testPostSetRequestBody() throws Exception {
        PostMethod post = new PostMethod("/foo");
        String body = "this+is+the+body";
        post.setRequestEntity(new StringRequestEntity(body, null, null));
        assertEquals(body, getRequestAsString(post.getRequestEntity()));
    }
    
}
