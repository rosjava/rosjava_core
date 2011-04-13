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
package org.ros.internal.node.xmlrpc;

import org.apache.xmlrpc.XmlRpcException;

public class XmlRpcTimeoutException extends XmlRpcException {
  private static final long serialVersionUID = 4875266372372105081L;

  /**
   * Creates a new instance with the given error code and error message.
   */
  public XmlRpcTimeoutException(int pCode, String message) {
    super(pCode, message);
  }
}
