/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/java/org/apache/commons/httpclient/protocol/ControllerThreadSocketFactory.java,v 1.2 2004/04/18 23:51:38 jsdever Exp $
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

package org.apache.commons.httpclient.protocol;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.util.TimeoutController;

/**
 * This helper class is intedned to help work around the limitation of older Java versions
 * (older than 1.4) that prevents from specifying a connection timeout when creating a
 * socket. This factory executes a controller thread overssing the process of socket 
 * initialisation. If the socket constructor cannot be created within the specified time
 * limit, the controller terminates and throws an {@link ConnectTimeoutException} 
 * 
 * @author Ortwin Glueck
 * @author Oleg Kalnichevski
 * 
 * @since 3.0
 */
public final class ControllerThreadSocketFactory {

    private ControllerThreadSocketFactory() {
        super();
    }

    /**
     * This method spawns a controller thread overseeing the process of socket 
     * initialisation. If the socket constructor cannot be created within the specified time
     * limit, the controller terminates and throws an {@link ConnectTimeoutException}
     * 
     * @param host the host name/IP
     * @param port the port on the host
     * @param localAddress the local host name/IP to bind the socket to
     * @param localPort the port on the local machine
     * @param timeout the timeout value to be used in milliseconds. If the socket cannot be
     *        completed within the given time limit, it will be abandoned
     * 
     * @return a connected Socket
     * 
     * @throws IOException if an I/O error occurs while creating the socket
     * @throws UnknownHostException if the IP address of the host cannot be
     * determined
     * @throws ConnectTimeoutException if socket cannot be connected within the
     *  given time limit
     * 
     */
    public static Socket createSocket(
        final ProtocolSocketFactory socketfactory, 
        final String host,
        final int port,
        final InetAddress localAddress,
        final int localPort,
        int timeout)
     throws IOException, UnknownHostException, ConnectTimeoutException
    {
            SocketTask task = new SocketTask() {
                public void doit() throws IOException {
                    setSocket(socketfactory.createSocket(host, port, localAddress, localPort));
                }                 
            };
            try {
                TimeoutController.execute(task, timeout);
            } catch (TimeoutController.TimeoutException e) {
                throw new ConnectTimeoutException(
                    "The host did not accept the connection within timeout of " 
                    + timeout + " ms");
            }
            Socket socket = task.getSocket();
            if (task.exception != null) {
                throw task.exception;
            }
            return socket;
    }

    public static Socket createSocket(final SocketTask task, int timeout)
     throws IOException, UnknownHostException, ConnectTimeoutException
    {
            try {
                TimeoutController.execute(task, timeout);
            } catch (TimeoutController.TimeoutException e) {
                throw new ConnectTimeoutException(
                    "The host did not accept the connection within timeout of " 
                    + timeout + " ms");
            }
            Socket socket = task.getSocket();
            if (task.exception != null) {
                throw task.exception;
            }
            return socket;
    }

    /**
    * Helper class for wrapping socket based tasks.
    */
    public static abstract class SocketTask implements Runnable {
        /** The socket */
        private Socket socket;
        /** The exception */
        private IOException exception;

        /**
         * Set the socket.
         * @param newSocket The new socket.
         */
        protected void setSocket(final Socket newSocket) {
            socket = newSocket;
        }

        /**
         * Return the socket.
         * @return Socket The socket.
         */
        protected Socket getSocket() {
            return socket;
        }
        /**
         * Perform the logic.
         * @throws IOException If an IO problem occurs
         */
        public abstract void doit() throws IOException;

        /** Execute the logic in this object and keep track of any exceptions. */
        public void run() {
            try {
                doit();
            } catch (IOException e) {
                exception = e;
            }
        }
    }
}
