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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.xmlrpc.server.XmlRpcStreamServer;
import org.apache.xmlrpc.util.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
 * <p>The {@link WebServer} is a minimal HTTP server, that might be used
 * as an embedded web server.</p>
 * <p>Use of the {@link WebServer} has grown very popular amongst users
 * of Apache XML-RPC. Why this is the case, can hardly be explained,
 * because the {@link WebServer} is at best a workaround, compared to
 * full blown servlet engines like Tomcat or Jetty. For example, under
 * heavy load it will almost definitely be slower than a real servlet
 * engine, because it does neither support proper keepalive (multiple
 * requests per physical connection) nor chunked mode (in other words,
 * it cannot stream requests).</p>
 * <p>If you still insist in using the {@link WebServer}, it is
 * recommended to use its subclass, the {@link ServletWebServer} instead,
 * which offers a minimal subset of the servlet API. In other words,
 * you keep yourself the option to migrate to a real servlet engine
 * later.</p>
 * <p>Use of the {@link WebServer} goes roughly like this: First of all,
 * create a property file (for example "MyHandlers.properties") and
 * add it to your jar file. The property keys are handler names and
 * the property values are the handler classes. Once that is done,
 * create an instance of WebServer:
 * <pre>
 *   final int port = 8088;
 *   final String propertyFile = "MyHandler.properties";
 *
 *   PropertyHandlerMapping mapping = new PropertyHandlerMapping();
 *   ClassLoader cl = Thread.currentThread().getContextClassLoader();
 *   mapping.load(cl, propertyFile);
 *   WebServer webServer = new WebServer(port);
 *   XmlRpcServerConfigImpl config = new XmlRpcServerConfigImpl();
 *   XmlRpcServer server = webServer.getXmlRpcServer();
 *   server.setConfig(config);
 *   server.setHandlerMapping(mapping);
 *   webServer.start();
 * </pre>
 */
public class WebServer implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebServer.class);

    private class AddressMatcher {
        private final int pattern[];

        AddressMatcher(String pAddress) {
            try {
                pattern = new int[4];
                StringTokenizer st = new StringTokenizer(pAddress, ".");
                if (st.countTokens() != 4) {
                    throw new IllegalArgumentException();
                }
                for (int i = 0; i < 4; i++) {
                    String next = st.nextToken();
                    if ("*".equals(next)) {
                        pattern[i] = 256;
                    } else {
                        /* Note: *Not* pattern[i] = Integer.parseInt(next);
                         * See XMLRPC-145
                         */
                        pattern[i] = (byte) Integer.parseInt(next);
                    }
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("\"" + pAddress
                        + "\" does not represent a valid IP address");
            }
        }

        boolean matches(byte[] pAddress) {
            for (int i = 0; i < 4; i++) {
                if (pattern[i] > 255) {
                    continue; // Wildcard
                }
                if (pattern[i] != pAddress[i]) {
                    return false;
                }
            }
            return true;
        }
    }

    protected ServerSocket serverSocket;
    private Thread listener;
    private ThreadPool pool;
    protected final List accept = new ArrayList();
    protected final List deny = new ArrayList();
    protected final XmlRpcStreamServer server = newXmlRpcStreamServer();

    protected XmlRpcStreamServer newXmlRpcStreamServer() {
        return new ConnectionServer();
    }

    // Inputs to setupServerSocket()
    private InetAddress address;
    private int port;

    private boolean paranoid;

    static final String HTTP_11 = "HTTP/1.1";

    /**
     * Creates a web server at the specified port number.
     *
     * @param pPort Port number; 0 for a random port, choosen by the
     *              operating system.
     */
    public WebServer(int pPort) {
        this(pPort, null);
    }

    /**
     * Creates a web server at the specified port number and IP address.
     *
     * @param pPort Port number; 0 for a random port, choosen by the
     *              operating system.
     * @param pAddr Local IP address; null for all available IP addresses.
     */
    public WebServer(int pPort, InetAddress pAddr) {
        address = pAddr;
        port = pPort;
    }

    /**
     * Factory method to manufacture the server socket.  Useful as a
     * hook method for subclasses to override when they desire
     * different flavor of socket (i.e. a <code>SSLServerSocket</code>).
     *
     * @param pPort   Port number; 0 for a random port, choosen by the operating
     *                system.
     * @param backlog
     * @param addr    If <code>null</code>, binds to
     *                <code>INADDR_ANY</code>, meaning that all network interfaces on
     *                a multi-homed host will be listening.
     *
     * @throws IOException Error creating listener socket.
     */
    protected ServerSocket createServerSocket(int pPort, int backlog, InetAddress addr)
            throws IOException {
        return new ServerSocket(pPort, backlog, addr);
    }

    /**
     * Initializes this server's listener socket with the specified
     * attributes, assuring that a socket timeout has been set.  The
     * {@link #createServerSocket(int, int, InetAddress)} method can
     * be overridden to change the flavor of socket used.
     *
     * @see #createServerSocket(int, int, InetAddress)
     */
    private synchronized void setupServerSocket(int backlog) throws IOException {
        // Since we can't reliably set SO_REUSEADDR until JDK 1.4 is
        // the standard, try to (re-)open the server socket several
        // times.  Some OSes (Linux and Solaris, for example), hold on
        // to listener sockets for a brief period of time for security
        // reasons before relinquishing their hold.
        for (int i = 1; ; i++) {
            try {
                serverSocket = createServerSocket(port, backlog, address);
                // A socket timeout must be set.
                if (serverSocket.getSoTimeout() <= 0) {
                    serverSocket.setSoTimeout(4096);
                }
                return;
            } catch (BindException e) {
                if (i == 10) {
                    throw e;
                } else {
                    long waitUntil = System.currentTimeMillis() + 1000;
                    for (; ; ) {
                        long l = waitUntil - System.currentTimeMillis();
                        if (l > 0) {
                            try {
                                Thread.sleep(l);
                            } catch (InterruptedException ex) {
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Spawns a new thread which binds this server to the port it's
     * configured to accept connections on.
     *
     * @throws IOException Binding the server socket failed.
     * @see #run()
     */
    public void start() throws IOException {
        setupServerSocket(50);

        // The listener reference is released upon shutdown().
        if (listener == null) {
            listener = new Thread(this, "XML-RPC Weblistener");
            // Not marked as daemon thread since run directly via main().
            listener.start();
        }
    }

    /**
     * Switch client filtering on/off.
     *
     * @param pParanoid True to enable filtering, false otherwise.
     *
     * @see #acceptClient(java.lang.String)
     * @see #denyClient(java.lang.String)
     */
    public void setParanoid(boolean pParanoid) {
        paranoid = pParanoid;
    }

    /**
     * Returns the client filtering state.
     *
     * @return True, if client filtering is enabled, false otherwise.
     *
     * @see #acceptClient(java.lang.String)
     * @see #denyClient(java.lang.String)
     */
    protected boolean isParanoid() {
        return paranoid;
    }

    /**
     * Add an IP address to the list of accepted clients. The parameter can
     * contain '*' as wildcard character, e.g. "192.168.*.*". You must call
     * setParanoid(true) in order for this to have any effect.
     *
     * @param pAddress The IP address being enabled.
     *
     * @throws IllegalArgumentException Parsing the address failed.
     * @see #denyClient(java.lang.String)
     * @see #setParanoid(boolean)
     */
    public void acceptClient(String pAddress) {
        accept.add(new AddressMatcher(pAddress));
    }

    /**
     * Add an IP address to the list of denied clients. The parameter can
     * contain '*' as wildcard character, e.g. "192.168.*.*". You must call
     * setParanoid(true) in order for this to have any effect.
     *
     * @param pAddress The IP address being disabled.
     *
     * @throws IllegalArgumentException Parsing the address failed.
     * @see #acceptClient(java.lang.String)
     * @see #setParanoid(boolean)
     */
    public void denyClient(String pAddress) {
        deny.add(new AddressMatcher(pAddress));
    }

    /**
     * Checks incoming connections to see if they should be allowed.
     * If not in paranoid mode, always returns true.
     *
     * @param s The socket to inspect.
     *
     * @return Whether the connection should be allowed.
     */
    protected boolean allowConnection(Socket s) {
        if (!paranoid) {
            return true;
        }

        int l = deny.size();
        byte addr[] = s.getInetAddress().getAddress();
        for (int i = 0; i < l; i++) {
            AddressMatcher match = (AddressMatcher) deny.get(i);
            if (match.matches(addr)) {
                return false;
            }
        }
        l = accept.size();
        for (int i = 0; i < l; i++) {
            AddressMatcher match = (AddressMatcher) accept.get(i);
            if (match.matches(addr)) {
                return true;
            }
        }
        return false;
    }

    protected ThreadPool.Task newTask(WebServer pServer, XmlRpcStreamServer pXmlRpcServer,
                                      Socket pSocket) throws IOException {
        return new Connection(pServer, pXmlRpcServer, pSocket);
    }

    /**
     * Listens for client requests until stopped.  Call {@link
     * #start()} to invoke this method, and {@link #shutdown()} to
     * break out of it.
     *
     * @throws RuntimeException Generally caused by either an
     *                          <code>UnknownHostException</code> or <code>BindException</code>
     *                          with the vanilla web server.
     * @see #start()
     * @see #shutdown()
     */
    public void run() {
        pool = newThreadPool();
        try {
            while (listener != null) {
                try {
                    Socket socket = serverSocket.accept();
                    try {
                        socket.setTcpNoDelay(true);
                    } catch (SocketException socketOptEx) {
                        if (LOGGER.isErrorEnabled()) {
                            LOGGER.error(ExceptionUtils.getStackTrace(socketOptEx));
                        }
                    }

                    try {
                        if (allowConnection(socket)) {
                            // set read timeout to 30 seconds
                            socket.setSoTimeout(30000);
                            final ThreadPool.Task task = newTask(this, server, socket);
                            if (pool.startTask(task)) {
                                socket = null;
                            } else {

                                LOGGER.error("Maximum load of " + pool.getMaxThreads()+ " exceeded, rejecting client");
                            }
                        }
                    } finally {
                        if (socket != null) {
                            try {
                                socket.close();
                            } catch (Throwable ignore) {
                            }
                        }
                    }
                } catch (InterruptedIOException checkState) {
                    // Timeout while waiting for a client (from
                    // SO_TIMEOUT)...try again if still listening.
                } catch (Throwable t) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error(ExceptionUtils.getStackTrace(t));
                    }
                }
            }
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error(ExceptionUtils.getStackTrace(e));
                    }
                }
            }

            // Shutdown our Runner-based threads
            pool.shutdown();
        }
    }

    protected ThreadPool newThreadPool() {
        return new ThreadPool(server.getMaxThreads(), "XML-RPC");
    }

    /**
     * Stop listening on the server port.  Shutting down our {@link
     * #listener} effectively breaks it out of its {@link #run()}
     * loop.
     *
     * @see #run()
     */
    public synchronized void shutdown() {
        // Stop accepting client connections
        if (listener != null) {
            Thread l = listener;
            listener = null;
            l.interrupt();
            if (pool != null) {
                pool.shutdown();
            }
        }
    }

    /**
     * Returns the port, on which the web server is running.
     * This method may be invoked after {@link #start()} only.
     *
     * @return Servers port number
     */
    public int getPort() {
        return serverSocket.getLocalPort();
    }



     /**
     * Returns the {@link org.apache.xmlrpc.server.XmlRpcServer}.
     *
     * @return The server object.
     */
    public XmlRpcStreamServer getXmlRpcServer() {
        return server;
    }
}
