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

import org.apache.xmlrpc.XmlRpcConfigImpl;


/** Default implementation of a request configuration.
 */
public class XmlRpcHttpRequestConfigImpl extends XmlRpcConfigImpl implements
		XmlRpcHttpRequestConfig {
	private boolean gzipCompressing;
	private boolean gzipRequesting;
	private String basicUserName;
	private String basicPassword;
    private int    connectionTimeout = 0;
    private int    replyTimeout = 0;
    private boolean enabledForExceptions;
    
	/** Sets, whether gzip compression is being used for
	 * transmitting the request.
	 * @param pCompressing True for enabling gzip compression,
	 * false otherwise.
	 * @see #setGzipRequesting(boolean)
	 */
	public void setGzipCompressing(boolean pCompressing) {
		gzipCompressing = pCompressing;
	}

	public boolean isGzipCompressing() {
		return gzipCompressing;
	}

	/** Sets, whether gzip compression is requested for the
	 * response.
	 * @param pRequesting True for requesting gzip compression,
	 * false otherwise.
	 * @see #setGzipCompressing(boolean)
	 */
	public void setGzipRequesting(boolean pRequesting) {
		gzipRequesting = pRequesting;
	}

	public boolean isGzipRequesting() {
		return gzipRequesting;
	}

	/** Sets the user name for basic authentication.
	 * @param pUser The user name.
	 */
	public void setBasicUserName(String pUser) {
		basicUserName = pUser;
	}

	public String getBasicUserName() { return basicUserName; }

	/** Sets the password for basic authentication.
	 * @param pPassword The password.
	 */
	public void setBasicPassword(String pPassword) {
		basicPassword = pPassword;
	}

	public String getBasicPassword() { return basicPassword; }

    /** Set the connection timeout in milliseconds.
     * @param pTimeout connection timeout, 0 to disable it
     */
    public void setConnectionTimeout(int pTimeout) {
        connectionTimeout = pTimeout;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /** Set the reply timeout in milliseconds.
     * @param pTimeout reply timeout, 0 to disable it
     */
    public void setReplyTimeout(int pTimeout) {
        replyTimeout = pTimeout;
    }

    public int getReplyTimeout() {
        return replyTimeout;
    }

    /** Sets, whether the response should contain a "faultCause" element
     * in case of errors. The "faultCause" is an exception, which the
     * server has trapped and written into a byte stream as a serializable
     * object.
     */
    public void setEnabledForExceptions(boolean pEnabledForExceptions) {
        enabledForExceptions = pEnabledForExceptions;
    }

    public boolean isEnabledForExceptions() {
        return enabledForExceptions;
    }
}
