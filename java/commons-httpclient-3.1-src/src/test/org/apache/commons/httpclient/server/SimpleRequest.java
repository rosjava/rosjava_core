/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/server/SimpleRequest.java,v 1.3 2004/11/13 12:21:28 olegk Exp $
 * $Revision: 480424 $
 * $Date: 2006-11-29 06:56:49 +0100 (Wed, 29 Nov 2006) $
 *
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.commons.httpclient.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.commons.httpclient.ChunkedInputStream;
import org.apache.commons.httpclient.ContentLengthInputStream;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.HeaderGroup;
import org.apache.commons.httpclient.NameValuePair;

/**
 * A generic HTTP request.
 * 
 * @author Oleg Kalnichevski
 */
public class SimpleRequest {
    
    public static final String DEFAULT_CONTENT_CHARSET = "ISO-8859-1";
    
    private RequestLine requestLine = null;
    private HeaderGroup headers = new HeaderGroup();
    private InputStream entity = null;

    public SimpleRequest() {
        super();
    }

    public SimpleRequest(
        final RequestLine requestLine,
        final Header[] headers,
        final InputStream content) throws IOException
    {
        super();
        if (requestLine == null) {
            throw new IllegalArgumentException("Request line may not be null");
        }
        this.requestLine = requestLine;
        if (headers != null) {
            this.headers.setHeaders(headers);
        }
        if (content != null) {
            // only PUT and POST have content
            String methodname = requestLine.getMethod(); 
            if ("POST".equalsIgnoreCase(methodname) || "PUT".equalsIgnoreCase(methodname)) {
                Header contentLength = this.headers.getFirstHeader("Content-Length");
                Header transferEncoding = this.headers.getFirstHeader("Transfer-Encoding");
                InputStream in = content;
                if (transferEncoding != null) {
                    if (transferEncoding.getValue().indexOf("chunked") != -1) {
                        in = new ChunkedInputStream(in);
                    }
                } else if (contentLength != null) {
                    long len = getContentLength();
                    if (len >= 0) {
                        in = new ContentLengthInputStream(in, len);
                    }
                }
                this.entity = in;
            }
        }
    }

    public SimpleRequest(final RequestLine requestLine, final Header[] headers)
        throws IOException {
        this(requestLine, headers, null);
    }
    
    public RequestLine getRequestLine() {
        return this.requestLine;
    }

    public void setRequestLine(final RequestLine requestline) {
        if (requestline == null) {
            throw new IllegalArgumentException("Request line may not be null");
        }
        this.requestLine = requestline;
    }

    public boolean containsHeader(final String name) {
        return this.headers.containsHeader(name);
    }

    public Header[] getHeaders() {
        return this.headers.getAllHeaders();
    }

    public Header getFirstHeader(final String s) {
        return this.headers.getFirstHeader(s);
    }

    public void removeHeaders(final String s) {
        if (s == null) {
            return;
        }
        Header[] headers = this.headers.getHeaders(s);
        for (int i = 0; i < headers.length; i++) {
            this.headers.removeHeader(headers[i]);
        }
    }

    public void addHeader(final Header header) {
        if (header == null) {
            return;
        }
        this.headers.addHeader(header);
    }

    public void setHeader(final Header header) {
        if (header == null) {
            return;
        }
        removeHeaders(header.getName());
        addHeader(header);
    }

    public Iterator getHeaderIterator() {
        return this.headers.getIterator();
    }

    public String getContentType() {
        Header contenttype = this.headers.getFirstHeader("Content-Type");
        if (contenttype != null) {
            return contenttype.getValue(); 
        } else {
            return "text/plain"; 
        }
    }
    
    public String getCharset() {
        String charset = null;
        Header contenttype = this.headers.getFirstHeader("Content-Type");
        if (contenttype != null) {
            HeaderElement values[] = contenttype.getElements();
            if (values.length == 1) {
                NameValuePair param = values[0].getParameterByName("charset");
                if (param != null) {
                    charset = param.getValue();
                }
            }
        }
        if (charset != null) {
            return charset;
        } else {
            return DEFAULT_CONTENT_CHARSET;
        }
    }
    
    public long getContentLength() {
        Header contentLength = this.headers.getFirstHeader("Content-Length");
        if (contentLength != null) {
            try {
                return Long.parseLong(contentLength.getValue());
            } catch (NumberFormatException e) {
                return -1;
            }
        } else {
            return -1;
        }
    }
    
    public InputStream getBody() {
        return this.entity;
    }
    
    public byte[] getBodyBytes() throws IOException {
        InputStream in = getBody();
        if (in != null) {
            byte[] tmp = new byte[4096];
            int bytesRead = 0;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream(1024);
            while ((bytesRead = in.read(tmp)) != -1) {
                buffer.write(tmp, 0, bytesRead);
            }
            return buffer.toByteArray();
        } else {
            return null;
        }
    }
    
    public String getBodyString() throws IOException {
        byte[] raw = getBodyBytes();
        if (raw != null) {
            return new String(raw, getCharset());
        } else {
            return null;
        }
    }
}
