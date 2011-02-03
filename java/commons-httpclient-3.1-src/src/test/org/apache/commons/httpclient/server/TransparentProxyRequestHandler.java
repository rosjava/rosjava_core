/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/server/TransparentProxyRequestHandler.java,v 1.7 2004/12/11 22:35:26 olegk Exp $
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
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpVersion;

/**
 * This request handler can handle the CONNECT method. It does nothing for any
 * other HTTP methods.
 * 
 * @author Ortwin Glueck
 */
public class TransparentProxyRequestHandler implements HttpRequestHandler {

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.httpclient.server.HttpRequestHandler#processRequest(org.apache.commons.httpclient.server.SimpleHttpServerConnection)
     */
    public boolean processRequest(
        final SimpleHttpServerConnection conn,
        final SimpleRequest request) throws IOException
    {

        RequestLine line = request.getRequestLine();
        HttpVersion ver = line.getHttpVersion();
        String method = line.getMethod();
        if (!"CONNECT".equalsIgnoreCase(method)) {
            return false;
        }
        Socket targetSocket = null;
        try {
            targetSocket = connect(line.getUri());
        } catch (IOException e) {
            SimpleResponse response = new SimpleResponse();
            response.setStatusLine(ver, HttpStatus.SC_NOT_FOUND);
            response.setHeader(new Header("Server", "test proxy"));
            response.setBodyString("Cannot connect to " + line.getUri());
            conn.writeResponse(response);
            return true;
        }
        SimpleResponse response = new SimpleResponse();
        response.setHeader(new Header("Server", "test proxy"));
        response.setStatusLine(ver, HttpStatus.SC_OK, "Connection established");
        conn.writeResponse(response);
        
        SimpleHttpServerConnection target = new SimpleHttpServerConnection(targetSocket); 
        pump(conn, target);
        return true;
    }

    private void pump(final SimpleHttpServerConnection source, final SimpleHttpServerConnection target)
        throws IOException {

        source.setSocketTimeout(100);
        target.setSocketTimeout(100);

        InputStream sourceIn = source.getInputStream();
        OutputStream sourceOut = source.getOutputStream();
        InputStream targetIn = target.getInputStream();
        OutputStream targetOut = target.getOutputStream();
        
        byte[] tmp = new byte[1024];
        int l;
        for (;;) {
            if (!source.isOpen() || !target.isOpen()) { 
                break;
            }
            try {
                l = sourceIn.read(tmp);
                if (l == -1) {
                    break;
                }
                targetOut.write(tmp, 0, l);
            } catch (InterruptedIOException ignore) {
                if (Thread.interrupted()) {
                    break;
                }
            }
            try {
                l = targetIn.read(tmp);
                if (l == -1) {
                    break;
                }
                sourceOut.write(tmp, 0, l);
            } catch (InterruptedIOException ignore) {
                if (Thread.interrupted()) {
                    break;
                }
            }
        }
    }
    
    private static Socket connect(final String host) throws IOException {
        String hostname = null; 
        int port; 
        int i = host.indexOf(':');
        if (i != -1) {
            hostname = host.substring(0, i);
            try {
                port = Integer.parseInt(host.substring(i + 1));
            } catch (NumberFormatException ex) {
                throw new IOException("Invalid host address: " + host);
            }
        } else {
            hostname = host;
            port = 80;
        }
        return new Socket(hostname, port);        
    }
    
}
