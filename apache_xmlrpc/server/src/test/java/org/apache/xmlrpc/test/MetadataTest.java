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
import java.text.Collator;
import java.util.Arrays;
import java.util.Locale;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfig;
import org.apache.xmlrpc.metadata.XmlRpcSystemImpl;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;


/**
 * Test class for the introspection stuff.
 */
public class MetadataTest extends XmlRpcTestCase {
    protected XmlRpcHandlerMapping getHandlerMapping() throws IOException,
            XmlRpcException {
        PropertyHandlerMapping mapping = new PropertyHandlerMapping();
        mapping.addHandler("Adder", AuthenticationTest.AdderImpl.class);
        XmlRpcSystemImpl.addSystemHandler(mapping);
        return mapping;
    }

    /**
     * Test, whether the actual handlers are working.
     */
    public void testAdder() throws Exception {
        for (int i = 0;  i < providers.length;  i++) {
            testAdder(providers[i]);
        }
    }

    private void testAdder(ClientProvider pProvider) throws Exception {
        XmlRpcClient client = pProvider.getClient();
        XmlRpcClientConfig config = getConfig(pProvider);
        client.setConfig(config);
        Object o = client.execute("Adder.add", new Object[]{new Integer(3), new Integer(5)});
        assertEquals(new Integer(8), o);
    }

    /**
     * Test for system.listMethods.
     */
    public void testListMethods() throws Exception {
        for (int i = 0;  i < providers.length;  i++) {
            testListMethods(providers[i]);
        }
    }

    private void testListMethods(ClientProvider pProvider) throws Exception {
        XmlRpcClient client = pProvider.getClient();
        XmlRpcClientConfig config = getConfig(pProvider);
        client.setConfig(config);
        Object o = client.execute("system.listMethods", new Object[0]);
        Object[] methodList = (Object[]) o;
        Arrays.sort(methodList, Collator.getInstance(Locale.US));
        assertEquals(4, methodList.length);
        assertEquals("Adder.add", methodList[0]);
        assertEquals("system.listMethods", methodList[1]);
        assertEquals("system.methodHelp", methodList[2]);
        assertEquals("system.methodSignature", methodList[3]);
    }

    /**
     * Test for system.methodHelp.
     */
    public void testMethodHelp() throws Exception {
        for (int i = 0;  i < providers.length;  i++) {
            testMethodHelp(providers[i]);
        }
    }

    private void testMethodHelp(ClientProvider pProvider) throws Exception {
        XmlRpcClient client = pProvider.getClient();
        XmlRpcClientConfig config = getConfig(pProvider);
        client.setConfig(config);
        String help = (String) client.execute("system.methodHelp", new Object[]{"Adder.add"});
        assertEquals("Invokes the method org.apache.xmlrpc.test.AuthenticationTest$AdderImpl.add(int, int).", help);
    }

    /**
     * Test for system.methodSignature.
     */
    public void testMethodSignature() throws Exception {
        for (int i = 0;  i < providers.length;  i++) {
            testMethodSignature(providers[i]);
        }
    }

    private void testMethodSignature(ClientProvider pProvider) throws Exception {
        XmlRpcClient client = pProvider.getClient();
        XmlRpcClientConfig config = getConfig(pProvider);
        client.setConfig(config);
        Object[] signatures = (Object[]) client.execute("system.methodSignature", new Object[]{"Adder.add"});
        assertEquals(signatures.length, 1);
        Object[] signature = (Object[]) signatures[0];
        assertEquals(3, signature.length);
        assertEquals("int", signature[0]);
        assertEquals("int", signature[1]);
        assertEquals("int", signature[2]);
    }
}
