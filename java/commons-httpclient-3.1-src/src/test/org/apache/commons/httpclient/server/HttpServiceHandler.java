/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/server/HttpServiceHandler.java,v 1.9 2004/11/13 22:38:27 mbecke Exp $
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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpVersion;

/**
 * This request handler provides service interface similar to that of Servlet API.
 * 
 * @author Oleg Kalnichevski
 */
public class HttpServiceHandler implements HttpRequestHandler {

    private HttpService service = null;
    
    public HttpServiceHandler(final HttpService service) {
        super();
        if (service == null) {
            throw new IllegalArgumentException("Service may not be null");
        }
        this.service = service;
    }
    
    public boolean processRequest(
        final SimpleHttpServerConnection conn,
        final SimpleRequest request) throws IOException {
        if (conn == null) {
            throw new IllegalArgumentException("Connection may not be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("Request may not be null");
        }
        boolean complete = false;
        SimpleResponse response = new SimpleResponse();
        this.service.process(request, response);
        
        // Nake sure the request if fully consumed
        request.getBodyBytes();
        
        // Ensure there's a content type header
        if (!response.containsHeader("Content-Type")) {
            response.addHeader(new Header("Content-Type", "text/plain"));
        }
        
        // Ensure there's a content length or transfer encoding header
        if (!response.containsHeader("Content-Length") && !response.containsHeader("Transfer-Encoding")) {
            InputStream content = response.getBody();
            if (content != null) {
                long len = response.getContentLength();
                if (len < 0) {
                    if (response.getHttpVersion().lessEquals(HttpVersion.HTTP_1_0)) {
                        throw new IOException("Chunked encoding not supported for HTTP version " 
                                + response.getHttpVersion());
                    }
                    Header header = new Header("Transfer-Encoding", "chunked"); 
                    response.addHeader(header);                
                } else {
                    Header header = new Header("Content-Length", Long.toString(len)); 
                    response.setHeader(header);
                }
            } else {
                Header header = new Header("Content-Length", "0"); 
                response.addHeader(header);
            }
        }

        if (!response.containsHeader("Connection")) {
            // See if the the client explicitly handles connection persistence
            Header connheader = request.getFirstHeader("Connection");
            if (connheader != null) {
                if (connheader.getValue().equalsIgnoreCase("keep-alive")) {
                    Header header = new Header("Connection", "keep-alive"); 
                    response.addHeader(header);
                    conn.setKeepAlive(true);
                }
                if (connheader.getValue().equalsIgnoreCase("close")) {
                    Header header = new Header("Connection", "close"); 
                    response.addHeader(header);
                    conn.setKeepAlive(false);
                }
            } else {
                // Use protocol default connection policy
                if (response.getHttpVersion().greaterEquals(HttpVersion.HTTP_1_1)) {
                    conn.setKeepAlive(true);
                } else {
                    conn.setKeepAlive(false);
                }
            }
        }
        if ("HEAD".equalsIgnoreCase(request.getRequestLine().getMethod())) {
            // this is a head request, we don't want to send the actualy content
            response.setBody(null);
        }
        conn.writeResponse(response);
        return true;
    }
    
}
