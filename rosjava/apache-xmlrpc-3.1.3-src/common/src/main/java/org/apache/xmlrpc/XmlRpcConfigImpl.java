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

import java.util.TimeZone;

import org.apache.xmlrpc.common.XmlRpcHttpConfig;


/** Default implementation of {@link org.apache.xmlrpc.XmlRpcConfig}.
 */
public abstract class XmlRpcConfigImpl implements XmlRpcConfig, XmlRpcHttpConfig {
	private boolean enabledForExtensions;
	private boolean contentLengthOptional;
	private String basicEncoding;
	private String encoding;
    private TimeZone timeZone = TimeZone.getDefault();

	public boolean isEnabledForExtensions() { return enabledForExtensions; }

	/** Sets, whether extensions are enabled. By default, the
	 * client or server is strictly compliant to the XML-RPC
	 * specification and extensions are disabled.
	 * @param pExtensions True to enable extensions, false otherwise.
	 */
	public void setEnabledForExtensions(boolean pExtensions) {
		enabledForExtensions = pExtensions;
	}

	/** Sets the encoding for basic authentication.
	 * @param pEncoding The encoding; may be null, in which case
	 * UTF-8 is choosen.
	 */
	public void setBasicEncoding(String pEncoding) {
		basicEncoding = pEncoding;
	}

	public String getBasicEncoding() { return basicEncoding; }

	/** Sets the requests encoding.
	 * @param pEncoding The requests encoding or null (default
	 * UTF-8).
	 */
	public void setEncoding(String pEncoding) {
		encoding = pEncoding;
	}

	public String getEncoding() { return encoding; }

	public boolean isContentLengthOptional() {
		return contentLengthOptional;
	}

	/** Sets, whether a "Content-Length" header may be
	 * omitted. The XML-RPC specification demands, that such
	 * a header be present.
	 * @param pContentLengthOptional True, if the content length may be omitted.
	 */
	public void setContentLengthOptional(boolean pContentLengthOptional) {
		contentLengthOptional = pContentLengthOptional;
	}

    public TimeZone getTimeZone() {
        return timeZone;
    }

    /** Returns the timezone, which is used to interpret date/time
     * values. Defaults to {@link TimeZone#getDefault()}.
     */
    public void setTimeZone(TimeZone pTimeZone) {
        timeZone = pTimeZone;
    }
}
