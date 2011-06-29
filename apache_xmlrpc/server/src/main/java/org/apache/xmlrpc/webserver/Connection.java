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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.xmlrpc.common.ServerStreamConnection;
import org.apache.xmlrpc.common.XmlRpcHttpRequestConfig;
import org.apache.xmlrpc.common.XmlRpcNotAuthorizedException;
import org.apache.xmlrpc.server.XmlRpcHttpServerConfig;
import org.apache.xmlrpc.server.XmlRpcStreamServer;
import org.apache.xmlrpc.util.HttpUtil;
import org.apache.xmlrpc.util.LimitedInputStream;
import org.apache.xmlrpc.util.ThreadPool;



/** Handler for a single clients connection. This implementation
 * is able to do HTTP keepalive. In other words, it can serve
 * multiple requests via a single, physical connection.
 */
public class Connection implements ThreadPool.InterruptableTask, ServerStreamConnection {
    private static final String US_ASCII = "US-ASCII";
    private static final byte[] ctype = toHTTPBytes("Content-Type: text/xml\r\n");
    private static final byte[] clength = toHTTPBytes("Content-Length: ");
    private static final byte[] newline = toHTTPBytes("\r\n");
    private static final byte[] doubleNewline = toHTTPBytes("\r\n\r\n");
    private static final byte[] conkeep = toHTTPBytes("Connection: Keep-Alive\r\n");
    private static final byte[] conclose = toHTTPBytes("Connection: close\r\n");
    private static final byte[] ok = toHTTPBytes(" 200 OK\r\n");
    private static final byte[] serverName = toHTTPBytes("Server: Apache XML-RPC 1.0\r\n");
    private static final byte[] wwwAuthenticate = toHTTPBytes("WWW-Authenticate: Basic realm=XML-RPC\r\n");

    private static abstract class RequestException extends IOException {
        private static final long serialVersionUID = 2113732921468653309L;
        private final RequestData requestData;

        RequestException(RequestData pData, String pMessage) {
            super(pMessage);
            requestData = pData;
        }
        RequestData getRequestData() { return requestData; }
    }

    private static class BadEncodingException extends RequestException {
        private static final long serialVersionUID = -2674424938251521248L;
        BadEncodingException(RequestData pData, String pTransferEncoding) {
            super(pData, pTransferEncoding);
        }
    }

    private static class BadRequestException extends RequestException {
        private static final long serialVersionUID = 3257848779234554934L;
        BadRequestException(RequestData pData, String pTransferEncoding) {
            super(pData, pTransferEncoding);
        }
    }

    /** Returns the US-ASCII encoded byte representation of text for
     * HTTP use (as per section 2.2 of RFC 2068).
     */
    private static final byte[] toHTTPBytes(String text) {
        try {
            return text.getBytes(US_ASCII);
        } catch (UnsupportedEncodingException e) {
            throw new Error(e.getMessage() +
            ": HTTP requires US-ASCII encoding");
        }
    }

    private final WebServer webServer;
    private final Socket socket;
    private final InputStream input;
    private final OutputStream output;
    private final XmlRpcStreamServer server;
    private byte[] buffer;
    private Map headers;
    private RequestData requestData;
    private boolean shuttingDown;
    private boolean firstByte;

    /** Creates a new webserver connection on the given socket.
     * @param pWebServer The webserver maintaining this connection.
     * @param pServer The server being used to execute requests.
     * @param pSocket The server socket to handle; the <code>Connection</code>
     * is responsible for closing this socket.
     * @throws IOException
     */
    public Connection(WebServer pWebServer, XmlRpcStreamServer pServer, Socket pSocket)
            throws IOException {
        webServer = pWebServer;
        server = pServer;
        socket = pSocket;
        input = new BufferedInputStream(socket.getInputStream()){
            /** It may happen, that the XML parser invokes close().
             * Closing the input stream must not occur, because
             * that would close the whole socket. So we suppress it.
             */
            public void close() throws IOException {
            }
        };
        output = new BufferedOutputStream(socket.getOutputStream());
    }

    /** Returns the connections request configuration by
     * merging the HTTP request headers and the servers configuration.
     * @return The connections request configuration.
     * @throws IOException Reading the request headers failed.
     */
    private RequestData getRequestConfig() throws IOException {
        requestData = new RequestData(this);
        if (headers != null) {
            headers.clear();
        }
        firstByte = true;
        XmlRpcHttpServerConfig serverConfig = (XmlRpcHttpServerConfig) server.getConfig();
        requestData.setBasicEncoding(serverConfig.getBasicEncoding());
        requestData.setContentLengthOptional(serverConfig.isContentLengthOptional());
        requestData.setEnabledForExtensions(serverConfig.isEnabledForExtensions());
        requestData.setEnabledForExceptions(serverConfig.isEnabledForExceptions());

        // reset user authentication
        String line = readLine();
        if (line == null  &&  firstByte) {
            return null;
        }
        // Netscape sends an extra \n\r after bodypart, swallow it
        if (line != null && line.length() == 0) {
            line = readLine();
            if (line == null  ||  line.length() == 0) {
                return null;
            }
        }

        // tokenize first line of HTTP request
        StringTokenizer tokens = new StringTokenizer(line);
        String method = tokens.nextToken();
        if (!"POST".equalsIgnoreCase(method)) {
            throw new BadRequestException(requestData, method);
        }
        requestData.setMethod(method);
        tokens.nextToken(); // Skip URI
        String httpVersion = tokens.nextToken();
        requestData.setHttpVersion(httpVersion);
        requestData.setKeepAlive(serverConfig.isKeepAliveEnabled()
                && WebServer.HTTP_11.equals(httpVersion));
        do {
            line = readLine();
            if (line != null) {
                String lineLower = line.toLowerCase();
                if (lineLower.startsWith("content-length:")) {
                    String cLength = line.substring("content-length:".length());
                    requestData.setContentLength(Integer.parseInt(cLength.trim()));
                } else if (lineLower.startsWith("connection:")) {
                    requestData.setKeepAlive(serverConfig.isKeepAliveEnabled()
                            &&  lineLower.indexOf("keep-alive") > -1);
                } else if (lineLower.startsWith("authorization:")) {
                    String credentials = line.substring("authorization:".length());
                    HttpUtil.parseAuthorization(requestData, credentials);
                } else if (lineLower.startsWith("transfer-encoding:")) {
                    String transferEncoding = line.substring("transfer-encoding:".length());
                    String nonIdentityEncoding = HttpUtil.getNonIdentityTransferEncoding(transferEncoding);
                    if (nonIdentityEncoding != null) {
                        throw new BadEncodingException(requestData, nonIdentityEncoding);
                    }
                }
            }
        }
        while (line != null && line.length() != 0);

        return requestData;
    }

    public void run() {
        try {
            for (int i = 0;  ;  i++) {
                RequestData data = getRequestConfig();
                if (data == null) {
                    break;
                }
                server.execute(data, this);
                output.flush();
                if (!data.isKeepAlive()  ||  !data.isSuccess()) {
                    break;
                }
            }
        } catch (RequestException e) {
            webServer.log(e.getClass().getName() + ": " + e.getMessage());
            try {
                writeErrorHeader(e.requestData, e, -1);
                output.flush();
            } catch (IOException e1) {
                /* Ignore me */
            }
        } catch (Throwable t) {
            if (!shuttingDown) {
                webServer.log(t);
            }
        } finally {
            try { output.close(); } catch (Throwable ignore) {}
            try { input.close(); } catch (Throwable ignore) {}
            try { socket.close(); } catch (Throwable ignore) {}
        }
    }

    private String readLine() throws IOException {
        if (buffer == null) {
            buffer = new byte[2048];
        }
        int next;
        int count = 0;
        for (;;) {
            try {
                next = input.read();
                firstByte = false;
            } catch (SocketException e) {
                if (firstByte) {
                    return null;
                } else {
                    throw e;
                }
            }
            if (next < 0 || next == '\n') {
                break;
            }
            if (next != '\r') {
                buffer[count++] = (byte) next;
            }
            if (count >= buffer.length) {
                throw new IOException("HTTP Header too long");
            }
        }
        return new String(buffer, 0, count, US_ASCII);
    }

    /** Writes the response header and the response to the
     * output stream.
     * @param pData The request data.
     * @param pBuffer The {@link ByteArrayOutputStream} holding the response.
     * @throws IOException Writing the response failed.
     */
    public void writeResponse(RequestData pData, OutputStream pBuffer)
            throws IOException {
        ByteArrayOutputStream response = (ByteArrayOutputStream) pBuffer;
        writeResponseHeader(pData, response.size());
        response.writeTo(output);
    }

    /** Writes the response header to the output stream.	 * 
     * @param pData The request data
     * @param pContentLength The content length, if known, or -1.
     * @throws IOException Writing the response failed.
     */
    public void writeResponseHeader(RequestData pData, int pContentLength)
            throws IOException {
        output.write(toHTTPBytes(pData.getHttpVersion()));
        output.write(ok);
        output.write(serverName);
        output.write(pData.isKeepAlive() ? conkeep : conclose);
        output.write(ctype);
        if (headers != null) {
            for (Iterator iter = headers.entrySet().iterator();  iter.hasNext();  ) {
                Map.Entry entry = (Map.Entry) iter.next();
                String header = (String) entry.getKey();
                String value = (String) entry.getValue();
                output.write(toHTTPBytes(header + ": " + value + "\r\n"));
            }
        }
        if (pContentLength != -1) {
            output.write(clength);
            output.write(toHTTPBytes(Integer.toString(pContentLength)));
            output.write(doubleNewline);
        } else {
            output.write(newline);
        }
        pData.setSuccess(true);
    }

    /** Writes an error response to the output stream.
     * @param pData The request data.
     * @param pError The error being reported.
     * @param pStream The {@link ByteArrayOutputStream} with the error response.
     * @throws IOException Writing the response failed.
     */
    public void writeError(RequestData pData, Throwable pError, ByteArrayOutputStream pStream)
            throws IOException {
        writeErrorHeader(pData, pError, pStream.size());
        pStream.writeTo(output);
        output.flush();
    }

    /** Writes an error responses headers to the output stream.
     * @param pData The request data.
     * @param pError The error being reported.
     * @param pContentLength The response length, if known, or -1.
     * @throws IOException Writing the response failed.
     */
    public void writeErrorHeader(RequestData pData, Throwable pError, int pContentLength)
            throws IOException {
        if (pError instanceof BadRequestException) {
            final byte[] content = toHTTPBytes("Method " + pData.getMethod()
                    + " not implemented (try POST)\r\n");
            output.write(toHTTPBytes(pData.getHttpVersion()));
            output.write(toHTTPBytes(" 400 Bad Request"));
            output.write(newline);
            output.write(serverName);
            writeContentLengthHeader(content.length);
            output.write(newline);
            output.write(content);
        } else if (pError instanceof BadEncodingException) {
            final byte[] content = toHTTPBytes("The Transfer-Encoding " + pError.getMessage()
                    + " is not implemented.\r\n");
            output.write(toHTTPBytes(pData.getHttpVersion()));
            output.write(toHTTPBytes(" 501 Not Implemented"));
            output.write(newline);
            output.write(serverName);
            writeContentLengthHeader(content.length);
            output.write(newline);
            output.write(content);
        } else if (pError instanceof XmlRpcNotAuthorizedException) {
            final byte[] content = toHTTPBytes("Method " + pData.getMethod()
                    + " requires a " + "valid user name and password.\r\n");
            output.write(toHTTPBytes(pData.getHttpVersion()));
            output.write(toHTTPBytes(" 401 Unauthorized"));
            output.write(newline);
            output.write(serverName);
            writeContentLengthHeader(content.length);
            output.write(wwwAuthenticate);
            output.write(newline);
            output.write(content);
        } else {
            output.write(toHTTPBytes(pData.getHttpVersion()));
            output.write(ok);
            output.write(serverName);
            output.write(conclose);
            output.write(ctype);
            writeContentLengthHeader(pContentLength);
            output.write(newline);
        }
    }

    private void writeContentLengthHeader(int pContentLength) throws IOException {
        if (pContentLength == -1) {
            return;
        }
        output.write(clength);
        output.write(toHTTPBytes(Integer.toString(pContentLength)));
        output.write(newline);
    }

    /** Sets a response header value.
     */
    public void setResponseHeader(String pHeader, String pValue) {
        headers.put(pHeader, pValue);
    }


    public OutputStream newOutputStream() throws IOException {
        boolean useContentLength;
        useContentLength = !requestData.isEnabledForExtensions()
            ||  !((XmlRpcHttpRequestConfig) requestData).isContentLengthOptional();
        if (useContentLength) {
            return new ByteArrayOutputStream();
        } else {
            return output;
        }
    }

    public InputStream newInputStream() throws IOException {
        int contentLength = requestData.getContentLength();
        if (contentLength == -1) {
            return input;
        } else {
            return new LimitedInputStream(input, contentLength);
        }
    }

    public void close() throws IOException {
    }

    public void shutdown() throws Throwable {
        shuttingDown = true;
        socket.close();
    }
}
