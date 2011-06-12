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

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/** A {@link TypeSerializer} for integers.
 */
public class I4Serializer extends TypeSerializerImpl {
	/** Tag name of an int value.
	 */
	public static final String INT_TAG = "int";

	/** Tag name of an i4 value.
	 */
	public static final String I4_TAG = "i4";

	public void write(ContentHandler pHandler, Object pObject) throws SAXException {
		write(pHandler, I4_TAG, pObject.toString());
	}
}