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
package org.apache.xmlrpc.client;

import org.apache.xmlrpc.XmlRpcRequest;


/** A callback interface for an asynchronous XML-RPC call.
 * @since 3.0
 */
interface AsyncCallback {
    /** Call went ok, handle result.
     * @param pRequest The request being performed.
     * @param pResult The result object, which was returned by the server.
     */
    public void handleResult(XmlRpcRequest pRequest, Object pResult);

    /** Something went wrong, handle error.
     * @param pRequest The request being performed.
     * @param pError The error being thrown.
     */
    public void handleError(XmlRpcRequest pRequest, Throwable pError);
}
