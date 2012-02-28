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

import java.util.List;

import org.apache.xmlrpc.common.TypeFactory;
import org.apache.xmlrpc.common.XmlRpcStreamConfig;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


/** A {@link TypeSerializer} for lists.
 */
public class ListSerializer extends ObjectArraySerializer {
	/** Creates a new instance.
	 * @param pTypeFactory The factory being used for creating serializers.
	 * @param pConfig The configuration being used for creating serializers.
	 */
	public ListSerializer(TypeFactory pTypeFactory, XmlRpcStreamConfig pConfig) {
		super(pTypeFactory, pConfig);
	}
	protected void writeData(ContentHandler pHandler, Object pObject) throws SAXException {
		List data = (List) pObject;
		for (int i = 0;  i < data.size();  i++) {
			writeObject(pHandler, data.get(i));
		}
	}
}