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
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.ws.commons.util.NamespaceContextImpl;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcClientRequestImpl;
import org.apache.xmlrpc.common.TypeFactory;
import org.apache.xmlrpc.common.TypeFactoryImpl;
import org.apache.xmlrpc.common.XmlRpcController;
import org.apache.xmlrpc.common.XmlRpcStreamConfig;
import org.apache.xmlrpc.parser.DateParser;
import org.apache.xmlrpc.parser.TypeParser;
import org.apache.xmlrpc.serializer.DateSerializer;
import org.apache.xmlrpc.serializer.TypeSerializer;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.xml.sax.SAXException;


/**
 * Test suite for working with custom types.
 */
public class CustomTypesTest extends XmlRpcTestCase {
    /**
     * Sample date converter
     */
    public static class DateConverter {
        /**
         * Adds one day to the given date.
         */
        public Date tomorrow(Date pDate) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(pDate);
            cal.add(Calendar.DAY_OF_MONTH, 1);
            return cal.getTime();
        }
    }
    
    protected XmlRpcHandlerMapping getHandlerMapping() throws IOException, XmlRpcException {
        PropertyHandlerMapping mapping = new PropertyHandlerMapping();
        mapping.addHandler("DateConverter", DateConverter.class);
        return mapping;
    }

    /** Tests using a custom date format.
     */
    public void testCustomDateFormat() throws Exception {
        for (int i = 0;  i < providers.length;  i++) {
            testCustomDateFormat(providers[i]);
        }
    }

    private TypeFactory getCustomDateTypeFactory(XmlRpcController pController, final Format pFormat) {
        return new TypeFactoryImpl(pController){
            private TypeSerializer dateSerializer = new DateSerializer(pFormat);

            public TypeParser getParser(XmlRpcStreamConfig pConfig, NamespaceContextImpl pContext, String pURI, String pLocalName) {
                if (DateSerializer.DATE_TAG.equals(pLocalName)) {
                    return new DateParser(pFormat);
                } else {
                    return super.getParser(pConfig, pContext, pURI, pLocalName);
                }
            }

            public TypeSerializer getSerializer(XmlRpcStreamConfig pConfig, Object pObject) throws SAXException {
                if (pObject instanceof Date) {
                    return dateSerializer;
                } else {
                    return super.getSerializer(pConfig, pObject);
                }
            }
            
        };
    }

    private void testCustomDateFormat(ClientProvider pProvider) throws Exception {
        final DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        XmlRpcClient client = pProvider.getClient();
        XmlRpcClientConfigImpl config = getConfig(pProvider);
        client.setConfig(config);
        TypeFactory typeFactory = getCustomDateTypeFactory(client, format);
        client.setTypeFactory(typeFactory);
        Calendar cal1 = Calendar.getInstance();
        XmlRpcRequest request = new XmlRpcClientRequestImpl(config, "DateConverter.tomorrow", new Object[]{cal1.getTime()});
        final String got = XmlRpcTestCase.writeRequest(client, request);
        final String expect = "<?xml version=\"1.0\" encoding=\"US-ASCII\"?>"
            + "<methodCall><methodName>DateConverter.tomorrow</methodName>"
            + "<params><param><value><dateTime.iso8601>" + format.format(cal1.getTime())
            + "</dateTime.iso8601></value></param></params></methodCall>";
        assertEquals(expect, got);
        
        XmlRpcServer server = pProvider.getServer();
        server.setTypeFactory(getCustomDateTypeFactory(server, format));
        Date date = (Date) client.execute(request);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date);
        cal1.add(Calendar.DAY_OF_MONTH, 1);
        assertEquals(cal1, cal2);
    }
}
