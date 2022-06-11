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
package org.apache.xmlrpc.parser;

import org.apache.xmlrpc.XmlRpcException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;


/**
 * A parser for serializable objects.
 */
public final class SerializableParser extends ByteArrayParser {
    public final Object getResult() throws XmlRpcException {
        try {
            final byte[] res = (byte[]) super.getResult();
            final ByteArrayInputStream bais = new ByteArrayInputStream(res);
            final ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (IOException e) {
            throw new XmlRpcException("Failed to read result object: " + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new XmlRpcException("Failed to load class for result object: " + e.getMessage(), e);
        }
    }
}
