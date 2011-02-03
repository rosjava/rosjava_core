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
package org.apache.xmlrpc.common;

import org.apache.xmlrpc.XmlRpcException;


/**
 * This exception is thrown, if the server catches an exception, which
 * is thrown by the handler.
 */
public class XmlRpcInvocationException extends XmlRpcException {
    private static final long serialVersionUID = 7439737967784966169L;

    /**
     * Creates a new instance with the given error code, error message
     * and cause.
     */
    public XmlRpcInvocationException(int pCode, String pMessage, Throwable pLinkedException) {
        super(pCode, pMessage, pLinkedException);
    }

    /**
     * Creates a new instance with the given error message and cause.
     */
    public XmlRpcInvocationException(String pMessage, Throwable pLinkedException) {
        super(pMessage, pLinkedException);
    }
}
