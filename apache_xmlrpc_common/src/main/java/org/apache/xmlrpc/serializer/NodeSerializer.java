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

import org.apache.ws.commons.serialize.DOMSerializer;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


/** The node serializer is serializing a DOM node.
 */
public class NodeSerializer extends ExtSerializer {
	private static final DOMSerializer ser = new DOMSerializer();
	static {
		ser.setStartingDocument(false);
	}

	/** The local name of a dom tag.
	 */
	public static final String DOM_TAG = "dom";

	protected String getTagName() { return DOM_TAG; }

	protected void serialize(ContentHandler pHandler, Object pObject) throws SAXException {
		ser.serialize((Node) pObject, pHandler);
	}
}
