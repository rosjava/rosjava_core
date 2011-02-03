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
package org.apache.xmlrpc.serializer;

import java.util.Iterator;
import java.util.Map;

import org.apache.xmlrpc.common.TypeFactory;
import org.apache.xmlrpc.common.XmlRpcStreamConfig;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


/** A {@link TypeSerializer} for maps.
 */
public class MapSerializer extends TypeSerializerImpl {
    /** Tag name of a maps struct tag.
     */
    public static final String STRUCT_TAG = "struct";

    /** Tag name of a maps member tag.
     */
    public static final String MEMBER_TAG = "member";

    /** Tag name of a maps members name tag.
     */
    public static final String NAME_TAG = "name";

    private final XmlRpcStreamConfig config;
	private final TypeFactory typeFactory;

    /** Creates a new instance.
	 * @param pTypeFactory The factory being used for creating serializers.
	 * @param pConfig The configuration being used for creating serializers.
	 */
	public MapSerializer(TypeFactory pTypeFactory, XmlRpcStreamConfig pConfig) {
		typeFactory = pTypeFactory;
		config = pConfig;
	}

    protected void writeEntry(ContentHandler pHandler, Object pKey, Object pValue) throws SAXException {
		pHandler.startElement("", MEMBER_TAG, MEMBER_TAG, ZERO_ATTRIBUTES);
		pHandler.startElement("", NAME_TAG, NAME_TAG, ZERO_ATTRIBUTES);
		if (config.isEnabledForExtensions()  &&  !(pKey instanceof String)) {
		    writeValue(pHandler, pKey);
        } else {
            String key = pKey.toString();
            pHandler.characters(key.toCharArray(), 0, key.length());
        }
		pHandler.endElement("", NAME_TAG, NAME_TAG);
		writeValue(pHandler, pValue);
		pHandler.endElement("", MEMBER_TAG, MEMBER_TAG);
	}

    private void writeValue(ContentHandler pHandler, Object pValue)
            throws SAXException {
        TypeSerializer ts = typeFactory.getSerializer(config, pValue);
		if (ts == null) {
			throw new SAXException("Unsupported Java type: " + pValue.getClass().getName());
		}
		ts.write(pHandler, pValue);
    }

    protected void writeData(ContentHandler pHandler, Object pData) throws SAXException {
		Map map = (Map) pData;
		for (Iterator iter = map.entrySet().iterator();  iter.hasNext();  ) {
			Map.Entry entry = (Map.Entry) iter.next();
			writeEntry(pHandler, entry.getKey(), entry.getValue());
		}
	}

    public void write(final ContentHandler pHandler, Object pObject) throws SAXException {
		pHandler.startElement("", VALUE_TAG, VALUE_TAG, ZERO_ATTRIBUTES);
		pHandler.startElement("", STRUCT_TAG, STRUCT_TAG, ZERO_ATTRIBUTES);
		writeData(pHandler, pObject);
		pHandler.endElement("", STRUCT_TAG, STRUCT_TAG);
		pHandler.endElement("", VALUE_TAG, VALUE_TAG);
	}
}
