/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/server/ProxyRequestHandler.java,v 1.11 2004/12/11 22:35:26 olegk Exp $
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
import java.net.UnknownHostException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Ortwin Glueck
 * @author Oleg Kalnichevski
 */
public class ProxyRequestHandler implements HttpRequestHandler {

    private static final Log LOG = LogFactory.getLog(ProxyRequestHandler.class);

    private SimpleConnManager connmanager = null;
    
    public ProxyRequestHandler(final SimpleConnManager connmanager) {
        super();
        if (connmanager == null) {
            throw new IllegalArgumentException("Connection manager may not be null");
        }
        this.connmanager = connmanager;
    }
    
    /**
     * @see org.apache.commons.httpclient.server.HttpRequestHandler#processRequest(org.apache.commons.httpclient.server.SimpleHttpServerConnection)
     */
    public boolean processRequest(
        final SimpleHttpServerConnection conn,
        final SimpleRequest request) throws IOException
    {
        httpProxy(conn, request);
        return true;
    }

    private void httpProxy(
        final SimpleHttpServerConnection conn,
        final SimpleRequest request) throws IOException {

        RequestLine oldreqline = request.getRequestLine();
        URI uri = null;
        SimpleHost host = null;
        try {
            uri = new URI(oldreqline.getUri(), true);
            host = new SimpleHost(uri.getHost(), uri.getPort());
        } catch (URIException ex) {
            SimpleResponse response = ErrorResponse.getResponse(HttpStatus.SC_BAD_REQUEST);
            conn.writeResponse(response);
            return;
        }
        SimpleHttpServerConnection proxyconn = null;
        try {
            proxyconn = this.connmanager.openConnection(host);
        } catch (UnknownHostException e) {
            SimpleResponse response = ErrorResponse.getResponse(HttpStatus.SC_NOT_FOUND);
            conn.writeResponse(response);
            return;
        }
        try {
            proxyconn.setSocketTimeout(0);
            // Rewrite target url
            RequestLine newreqline = new RequestLine(
                    oldreqline.getMethod(), 
                    uri.getEscapedPath(), 
                    oldreqline.getHttpVersion()); 
            request.setRequestLine(newreqline);
            // Remove proxy-auth headers if present
            request.removeHeaders("Proxy-Authorization");
            // Manage connection persistence
            Header connheader = request.getFirstHeader("Proxy-Connection");
            if (connheader != null) {
                if (connheader.getValue().equalsIgnoreCase("close")) {
                    request.setHeader(new Header("Connection", "close"));
                }
            }
            request.removeHeaders("Proxy-Connection");
            
            proxyconn.writeRequest(request);
            
            SimpleResponse response = proxyconn.readResponse();
            if (response == null) {
                return;
            }
            response.setHeader(new Header("Via", "1.1 test (Test-Proxy)"));
            connheader = response.getFirstHeader("Connection");
            if (connheader != null) {
                String s = connheader.getValue(); 
                if (s.equalsIgnoreCase("close")) {
                    response.setHeader(new Header("Proxy-Connection", "close"));
                    conn.setKeepAlive(false);
                    proxyconn.setKeepAlive(false);
                    response.removeHeaders("Connection");
                }
                if (s.equalsIgnoreCase("keep-alive")) {
                    response.setHeader(new Header("Proxy-Connection", "keep-alive"));
                    conn.setKeepAlive(true);
                    proxyconn.setKeepAlive(true);
                    response.removeHeaders("Connection");
                }
            } else {
                // Use protocol default connection policy
                if (response.getHttpVersion().greaterEquals(HttpVersion.HTTP_1_1)) {
                    conn.setKeepAlive(true);
                    proxyconn.setKeepAlive(true);
                } else {
                    conn.setKeepAlive(false);
                    proxyconn.setKeepAlive(false);
                }
            }
            if ("HEAD".equalsIgnoreCase(request.getRequestLine().getMethod())) {
                // this is a head request, we don't want to send the actualy content
                response.setBody(null);
            }
            conn.writeResponse(response);

        } catch (HttpException e) {
            SimpleResponse response = ErrorResponse.getResponse(HttpStatus.SC_BAD_REQUEST);
            conn.writeResponse(response);
            proxyconn.setKeepAlive(false);
        } catch (IOException e) {
            LOG.warn(e.getMessage());
            proxyconn.setKeepAlive(false);
        } finally {
            this.connmanager.releaseConnection(host, proxyconn);
        }
    }
    
}
