/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/server/SimpleHttpServer.java,v 1.15 2004/12/11 22:35:26 olegk Exp $
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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A simple, but extensible HTTP server, mostly for testing purposes.
 * 
 * @author Christian Kohlschuetter
 * @author Oleg Kalnichevski
 */
public class SimpleHttpServer implements Runnable {
    private static final Log LOG = LogFactory.getLog(SimpleHttpServer.class);
    
    private String testname = "Simple test";
    private long count = 0;
    private ServerSocket listener = null;
    private Thread t;
    private ThreadGroup tg;
    private boolean stopped = false;

    private SimpleConnSet connections = new SimpleConnSet();

    private HttpRequestHandler requestHandler = null;

    /**
     * Creates a new HTTP server instance, using an arbitrary free TCP port
     * 
     * @throws IOException  if anything goes wrong during initialization
     */
    public SimpleHttpServer() throws IOException {
        this(null, 0);
    }

    /**
     * Creates a new HTTP server instance, using the specified socket
     * factory and the TCP port
     * 
     * @param   port    Desired TCP port
     * @throws IOException  if anything goes wrong during initialization
     */
    public SimpleHttpServer(SimpleSocketFactory socketfactory, int port) 
        throws IOException {
        if (socketfactory == null) {
            socketfactory = new SimplePlainSocketFactory();
        }
        listener = socketfactory.createServerSocket(port);
        if(LOG.isDebugEnabled()) {
            LOG.debug("Starting test HTTP server on port " + getLocalPort());
        }
        tg = new ThreadGroup("SimpleHttpServer thread group");
        t = new Thread(tg, this, "SimpleHttpServer listener");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Creates a new HTTP server instance, using the specified TCP port
     * 
     * @param   port    Desired TCP port
     * @throws IOException  if anything goes wrong during initialization
     */
    public SimpleHttpServer(int port) throws IOException {
        this(null, port);
    }

    public String getTestname() {
        return this.testname;
    }

    public void setTestname(final String testname) {
        this.testname = testname;
    }
    
    /**
     * Returns the TCP port that this HTTP server instance is bound to.
     *
     * @return  TCP port, or -1 if not running
     */
    public int getLocalPort() {
        return listener.getLocalPort();
    }
    
    /**
     * Returns the IP address that this HTTP server instance is bound to.
     * @return String representation of the IP address or <code>null</code> if not running
     */
    public String getLocalAddress() {
        InetAddress address = listener.getInetAddress();
        // Ugly work-around for older JDKs
        byte[] octets = address.getAddress();
        if ((octets[0] == 0) 
         && (octets[1] == 0) 
         && (octets[2] == 0) 
         && (octets[3] == 0)) {
            return "localhost"; 
        } else {
            return address.getHostAddress();
        }
    }

    /**
     * Checks if this HTTP server instance is running.
     * 
     * @return  true/false
     */
    public boolean isRunning() {
        if(t == null) {
            return false;
        }
        return t.isAlive();
    }

    /**
     * Stops this HTTP server instance.
     */
    public synchronized void destroy() {
        if (stopped) {
            return;
        }

        this.stopped = true;
        if(LOG.isDebugEnabled()) {
            LOG.debug("Stopping test HTTP server on port " + getLocalPort());
        }
        tg.interrupt();
        
        if (listener != null) {
            try {
                listener.close();
            } catch(IOException e) {
                
            }
        }
        this.connections.shutdown();
    }

    /**
     * Returns the currently used HttpRequestHandler by this SimpleHttpServer
     * 
     * @return The used HttpRequestHandler, or null.
     */
    public HttpRequestHandler getRequestHandler() {
        return requestHandler;
    }

    /**
     * Sets the HttpRequestHandler to be used for this SimpleHttpServer.
     * 
     * @param rh    Request handler to be used, or null to disable.
     */
    public void setRequestHandler(HttpRequestHandler rh) {
        this.requestHandler = rh;
    }

    public void setHttpService(HttpService service) {
        setRequestHandler(new HttpServiceHandler(service));
    }

    public void run() {
        try {
            while (!this.stopped && !Thread.interrupted()) {
                Socket socket = listener.accept();
                try {
                    if (this.requestHandler == null) {
                        socket.close();
                        break;
                    }
                    SimpleHttpServerConnection conn = new SimpleHttpServerConnection(socket); 
                    this.connections.addConnection(conn);

                    Thread t = new SimpleConnectionThread(
                            tg,
                            this.testname + " thread " + this.count,
                            conn, 
                            this.connections,
                            this.requestHandler);
                    t.setDaemon(true);
                    t.start();
                } catch (IOException e) {
                    LOG.error("I/O error: " + e.getMessage());
                }
                this.count++;
                Thread.sleep(100);
            }
        } catch (InterruptedException accept) {
        } catch (IOException e) {
            if (!stopped) {
                LOG.error("I/O error: " + e.getMessage());
            }
        } finally {
            destroy();
        }
    }
}
