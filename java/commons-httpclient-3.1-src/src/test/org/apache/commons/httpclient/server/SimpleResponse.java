/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/server/SimpleResponse.java,v 1.8 2004/11/13 12:21:28 olegk Exp $
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import org.apache.commons.httpclient.ChunkedInputStream;
import org.apache.commons.httpclient.ContentLengthInputStream;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.HeaderGroup;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.StatusLine;

/**
 * A generic HTTP response.
 * 
 * @author Christian Kohlschuetter
 * @author Oleg Kalnichevski
 */
public class SimpleResponse {
    
    public static final String DEFAULT_CONTENT_CHARSET = "ISO-8859-1";
    
    private HttpVersion ver = HttpVersion.HTTP_1_1;
    private int statuscode = HttpStatus.SC_OK;
    private String phrase = HttpStatus.getStatusText(HttpStatus.SC_OK);
    private HeaderGroup headers = new HeaderGroup();
    private InputStream entity = null;

    public SimpleResponse() {
        super();
    }

    public SimpleResponse(
            final StatusLine statusline, 
            final Header[] headers, 
            final InputStream content) 
            throws IOException {
        super();
        if (statusline == null) {
            throw new IllegalArgumentException("Status line may not be null");
        }
        setStatusLine(HttpVersion.parse(statusline.getHttpVersion()),
                statusline.getStatusCode(), statusline.getReasonPhrase());
        setHeaders(headers);
        if (content != null) {
            InputStream in = content;
            Header contentLength = this.headers.getFirstHeader("Content-Length");
            Header transferEncoding = this.headers.getFirstHeader("Transfer-Encoding");

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


    public void setStatusLine(final HttpVersion ver, int statuscode, final String phrase) {
        if (ver == null) {
            throw new IllegalArgumentException("HTTP version may not be null");
        }
        if (statuscode <= 0) {
            throw new IllegalArgumentException("Status code may not be negative or zero");
        }
        this.ver = ver;
        this.statuscode = statuscode;
        if (phrase != null) {
            this.phrase = phrase;
        } else {
            this.phrase = HttpStatus.getStatusText(statuscode);
        }
    }

    public void setStatusLine(final HttpVersion ver, int statuscode) {
        setStatusLine(ver, statuscode, null);
    }

    public String getPhrase() {
        return this.phrase;
    }

    public int getStatuscode() {
        return this.statuscode;
    }

    public HttpVersion getHttpVersion() {
        return this.ver;
    }

    public String getStatusLine() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(this.ver);
        buffer.append(' ');
        buffer.append(this.statuscode);
        if (this.phrase != null) {
            buffer.append(' ');
            buffer.append(this.phrase);
        }
        return buffer.toString();
    }

    public boolean containsHeader(final String name) {
        return this.headers.containsHeader(name);
    }

    public Header[] getHeaders() {
        return this.headers.getAllHeaders();
    }

    public Header getFirstHeader(final String name) {
        return this.headers.getFirstHeader(name);
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

    public void setHeaders(final Header[] headers) {
        if (headers == null) {
            return;
        }
        this.headers.setHeaders(headers);
    }

    public Iterator getHeaderIterator() {
        return this.headers.getIterator();
    }
    
    public String getCharset() {
        String charset = DEFAULT_CONTENT_CHARSET;
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
        return charset;
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
    
    public void setBodyString(final String string) {
        if (string != null) {
            byte[] raw = null;
            try {
                raw = string.getBytes(DEFAULT_CONTENT_CHARSET);
            } catch (UnsupportedEncodingException e) {
                raw = string.getBytes();
            }
            this.entity = new ByteArrayInputStream(raw);
            if (!containsHeader("Content-Type")) {
                setHeader(new Header("Content-Type", "text/plain"));
            }
            setHeader(new Header("Content-Length", Long.toString(raw.length)));
        } else {
            this.entity = null;
        }
    }
    
    public void setBody(final InputStream instream) {
        this.entity = instream;
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
