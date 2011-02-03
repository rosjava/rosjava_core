/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/java/org/apache/commons/httpclient/protocol/ReflectionSocketFactory.java,v 1.4 2004/12/21 23:15:21 olegk Exp $
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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.httpclient.ConnectTimeoutException;

/**
 * This helper class uses refelction in order to execute Socket methods
 * available in Java 1.4 and above  
 * 
 * @author Oleg Kalnichevski
 * 
 * @since 3.0
 */
public final class ReflectionSocketFactory {

    private static boolean REFLECTION_FAILED = false;
    
    private static Constructor INETSOCKETADDRESS_CONSTRUCTOR = null;
    private static Method SOCKETCONNECT_METHOD = null;
    private static Method SOCKETBIND_METHOD = null;
    private static Class SOCKETTIMEOUTEXCEPTION_CLASS = null;

    private ReflectionSocketFactory() {
        super();
    }

    /**
     * This method attempts to execute Socket method available since Java 1.4
     * using reflection. If the methods are not available or could not be executed
     * <tt>null</tt> is returned
     *   
     * @param socketfactoryName name of the socket factory class
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
        final String socketfactoryName, 
        final String host,
        final int port,
        final InetAddress localAddress,
        final int localPort,
        int timeout)
     throws IOException, UnknownHostException, ConnectTimeoutException
    {
        if (REFLECTION_FAILED) {
            //This is known to have failed before. Do not try it again
            return null;
        }
        // This code uses reflection to essentially do the following:
        //
        //  SocketFactory socketFactory = Class.forName(socketfactoryName).getDefault();
        //  Socket socket = socketFactory.createSocket();
        //  SocketAddress localaddr = new InetSocketAddress(localAddress, localPort);
        //  SocketAddress remoteaddr = new InetSocketAddress(host, port);
        //  socket.bind(localaddr);
        //  socket.connect(remoteaddr, timeout);
        //  return socket;
        try {
            Class socketfactoryClass = Class.forName(socketfactoryName);
            Method method = socketfactoryClass.getMethod("getDefault", 
                new Class[] {});
            Object socketfactory = method.invoke(null, 
                new Object[] {});
            method = socketfactoryClass.getMethod("createSocket", 
                new Class[] {});
            Socket socket = (Socket) method.invoke(socketfactory, new Object[] {});
            
            if (INETSOCKETADDRESS_CONSTRUCTOR == null) {
                Class addressClass = Class.forName("java.net.InetSocketAddress");
                INETSOCKETADDRESS_CONSTRUCTOR = addressClass.getConstructor(
                    new Class[] { InetAddress.class, Integer.TYPE });
            }
                
            Object remoteaddr = INETSOCKETADDRESS_CONSTRUCTOR.newInstance(
                new Object[] { InetAddress.getByName(host), new Integer(port)});

            Object localaddr = INETSOCKETADDRESS_CONSTRUCTOR.newInstance(
                    new Object[] { localAddress, new Integer(localPort)});

            if (SOCKETCONNECT_METHOD == null) {
                SOCKETCONNECT_METHOD = Socket.class.getMethod("connect", 
                    new Class[] {Class.forName("java.net.SocketAddress"), Integer.TYPE});
            }

            if (SOCKETBIND_METHOD == null) {
                SOCKETBIND_METHOD = Socket.class.getMethod("bind", 
                    new Class[] {Class.forName("java.net.SocketAddress")});
            }
            SOCKETBIND_METHOD.invoke(socket, new Object[] { localaddr});
            SOCKETCONNECT_METHOD.invoke(socket, new Object[] { remoteaddr, new Integer(timeout)});
            return socket;
        }
        catch (InvocationTargetException e) {
            Throwable cause = e.getTargetException(); 
            if (SOCKETTIMEOUTEXCEPTION_CLASS == null) {
                try {
                    SOCKETTIMEOUTEXCEPTION_CLASS = Class.forName("java.net.SocketTimeoutException");
                } catch (ClassNotFoundException ex) {
                    // At this point this should never happen. Really.
                    REFLECTION_FAILED = true;
                    return null;
                }
            }
            if (SOCKETTIMEOUTEXCEPTION_CLASS.isInstance(cause)) {
                throw new ConnectTimeoutException(
                    "The host did not accept the connection within timeout of " 
                    + timeout + " ms", cause);
            }
            if (cause instanceof IOException) {
                throw (IOException)cause;
            }
            return null;
        }
        catch (Exception e) {
            REFLECTION_FAILED = true;
            return null;
        }
    }
}
