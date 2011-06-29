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
package org.apache.xmlrpc.webserver;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;


/** Default implementation of a servlet output stream.
 * Handles output of HTTP headers.
 */
class ServletOutputStreamImpl extends ServletOutputStream {
	private final OutputStream target;
	private final HttpServletResponseImpl res;
	private final byte[] buffer = new byte[HttpServletResponseImpl.BUFFER_SIZE];
	private int bufferOffset;
	private boolean closed;
	private boolean committed;

	ServletOutputStreamImpl(OutputStream pTarget, HttpServletResponseImpl pResponse) {
		target = pTarget;
		res = pResponse;
	}

	public void write(int b) throws IOException {
		if (closed) {
			throw new IOException("This output stream is already closed.");
		}
		if (bufferOffset == buffer.length) {
			flush();
		}
		buffer[bufferOffset++] = (byte) b;
	}

	public void write(byte[] pChars, int pOffset, int pLen) throws IOException {
		if (closed) {
			throw new IOException("This output stream is already closed.");
		}
		while (pLen-- > 0) {
			if (bufferOffset == buffer.length) {
				flush();
			}
			buffer[bufferOffset++] = pChars[pOffset++];
		}
	}

	private void flush(boolean pClosing) throws IOException {
		if (!committed) {
			committed = true;
			String headers = res.getHttpHeaders(pClosing ? new Integer(bufferOffset) : null);
			target.write(headers.getBytes("US-ASCII"));
		}
		if (bufferOffset > 0) {
			target.write(buffer, 0, bufferOffset);
			bufferOffset = 0;
		}
	}

	public void close() throws IOException {
		if (!closed) {
			flush(true);
			closed = true;
			target.close();
		}
	}

	public void flush() throws IOException {
		if (closed) {
			throw new IOException("This output stream is already closed.");
		}
		flush(false);
		target.flush();
	}

	void reset() {
		if (committed) {
			throw new IllegalStateException("The response is already committed. A reset cannot be performed.");
		}
	}

	boolean isCommitted() {
		return committed;
	}
}
