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
package org.apache.xmlrpc;

import java.io.PrintStream;
import java.io.PrintWriter;

/** This exception is thrown by the XmlRpcClient, if an invocation of the
 * remote method failed. Failure may have two reasons: The invocation
 * failed on the remote side (for example, an exception was thrown within
 * the server) or the communication with the server failed. The latter
 * is indicated by throwing an instance of
 * {@link org.apache.xmlrpc.client.XmlRpcClientException}.
 */
public class XmlRpcException extends Exception {
	private static final long serialVersionUID = 3258693217049325618L;

	/** The fault code of the exception. For servers based on this library, this
     * will always be 0. (If there are predefined error codes, they should be in
     * the XML-RPC spec.)
     */
    public final int code;

	/** If the transport was able to catch a remote exception
	 * (as is the case, if the local transport is used or if extensions
	 * are enabled and the server returned a serialized exception),
	 * then this field contains the trapped exception.
	 */
	public final Throwable linkedException;

    /** Creates a new instance with the given error code and error message.
     * @param pCode Error code.
     * @param pMessage Detail message.
     */
    public XmlRpcException(int pCode, String pMessage) {
		this(pCode, pMessage, null);
    }

    /** Creates a new instance with the given error message
     * and cause.
     * @param pMessage Detail message.
     * @param pLinkedException The errors cause.
     */
    public XmlRpcException(String pMessage, Throwable pLinkedException) {
		this(0, pMessage, pLinkedException);
    }

    /** Creates a new instance with the given error message
     * and error code 0.
     * @param pMessage Detail message.
     */
    public XmlRpcException(String pMessage) {
		this(0, pMessage, null);
    }

    /** Creates a new instance with the given error code, error message
     * and cause.
     * @param pCode Error code.
     * @param pMessage Detail message.
     * @param pLinkedException The errors cause.
     */
    public XmlRpcException(int pCode, String pMessage, Throwable pLinkedException) {
		super(pMessage);
		code = pCode;
		linkedException = pLinkedException;
    }

	public void printStackTrace(PrintStream pStream) {
		super.printStackTrace(pStream);
		if (linkedException != null) {
			pStream.println("Caused by:");
			linkedException.printStackTrace(pStream);
		}
	}

	public void printStackTrace(PrintWriter pWriter) {
		super.printStackTrace(pWriter);
		if (linkedException != null) {
			pWriter.println("Caused by:");
			linkedException.printStackTrace(pWriter);
		}
	}

	public Throwable getCause() {
	    return linkedException;
    }
}
