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
package org.apache.xmlrpc.util;

import java.io.InputStream;
import java.io.IOException;

/** A filtering {@link java.io.InputStream} for proper handling of
 * the <code>Content-Length</code> header: It guarantees to return
 * at most a given number of bytes.
 */
public class LimitedInputStream extends InputStream {
    // bytes remaining to be read from the input stream. This is
    // initialized from CONTENT_LENGTH (or getContentLength()).
    // This is used in order to correctly return a -1 when all the
    // data POSTed was read. If this is left to -1, content length is
    // assumed as unknown and the standard InputStream methods will be used
    private long available;
    private long markedAvailable;
    private InputStream in;

    /** Creates a new instance, reading from the given input stream
     * and returning at most the given number of bytes.
     * @param pIn Input stream being read.
     * @param pAvailable Number of bytes available in <code>pIn</code>.
     */
    public LimitedInputStream(InputStream pIn, int pAvailable) {
		in = pIn;
        available = pAvailable;
    }

    public int read() throws IOException {
        if (available > 0) {
            available--;
            return in.read();
        }
        return -1;
    }

    public int read(byte b[], int off, int len) throws IOException {
        if (available > 0) {
            if (len > available) {
                // shrink len
                len = (int) available;
            }
            int read = in.read(b, off, len);
            if (read == -1) {
                available = 0;
            } else {
                available -= read;
            }
            return read;
        }
        return -1;
    }

    public long skip(long n) throws IOException {
        long skip = in.skip(n);
        if (available > 0) {
            available -= skip;
        }
        return skip;
    }

    public void mark(int readlimit) {
        in.mark(readlimit);
        markedAvailable = available;
    }

    public void reset() throws IOException {
        in.reset();
        available = markedAvailable;
    }

    public boolean markSupported() {
        return true;
    }
}
