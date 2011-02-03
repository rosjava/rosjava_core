/*
 * $Header$
 * $Revision: 480424 $
 * $Date: 2006-11-29 06:56:49 +0100 (Wed, 29 Nov 2006) $
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
 * [Additional notices, if required by prior licensing conditions]
 *
 */

package org.apache.commons.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.ControllerThreadSocketFactory;

/**
 *
 * Unit tests for {@link HttpConnection}.
 *
 * @author Sean C. Sullivan
 *
 * @version $Id: TestHttpConnection.java 480424 2006-11-29 05:56:49Z bayard $
 *
 */
public class TestHttpConnection extends HttpClientTestBase {
    
    // ------------------------------------------------------------ Constructor
    public TestHttpConnection(String testName) throws Exception {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestHttpConnection.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestHttpConnection.class);
    }


    // ----------------------------------------------------------- Test Methods

    public void testConstructThenClose() {
        this.server.setHttpService(new EchoService());
        HttpConnection conn = new HttpConnection(
                this.server.getLocalAddress(), this.server.getLocalPort());
        conn.close();
        assertTrue(!conn.isOpen());
    }

    public void testConnTimeoutRelease() {
        this.server.setHttpService(new EchoService());
        // create a custom protocol that will delay for 500 milliseconds
        Protocol testProtocol = new Protocol(
            "timeout",
            new DelayedProtocolSocketFactory(
                500, 
                Protocol.getProtocol("http").getSocketFactory()
            ),
            this.server.getLocalPort()
        );

        NoHostHttpConnectionManager connectionManager = new NoHostHttpConnectionManager();
        connectionManager.setConnection(
                new HttpConnection(
                        this.server.getLocalAddress(), this.server.getLocalPort(), testProtocol));
        this.client.setHttpConnectionManager(connectionManager);
        client.getHostConfiguration().setHost(
                this.server.getLocalAddress(), this.server.getLocalPort(), testProtocol);
        client.getHttpConnectionManager().getParams().setConnectionTimeout(1);
        
        try {
            GetMethod get = new GetMethod();
            client.executeMethod(get);
            fail("Should have timed out");
        } catch(IOException e) {
            /* should fail */
            assertTrue(e instanceof ConnectTimeoutException);
            assertTrue(connectionManager.isConnectionReleased());
        }
    }


    public void testConnTimeout() {

        // create a custom protocol that will delay for 500 milliseconds
        Protocol testProtocol = new Protocol(
            "timeout",
            new DelayedProtocolSocketFactory(
                500, 
                Protocol.getProtocol("http").getSocketFactory()
            ),
            this.server.getLocalPort()
        );

        HttpConnection conn = new HttpConnection(
                this.server.getLocalAddress(), this.server.getLocalPort(), testProtocol);
        // 1 ms is short enough to make this fail
        conn.getParams().setConnectionTimeout(1);
        try {
            conn.open();
            fail("Should have timed out");
        } catch(IOException e) {
            assertTrue(e instanceof ConnectTimeoutException);
            /* should fail */
        }
    }

    public void testForIllegalStateExceptions() {
        HttpConnection conn = new HttpConnection(
                this.server.getLocalAddress(), this.server.getLocalPort());
        try {
            OutputStream out = conn.getRequestOutputStream();
            fail("getRequestOutputStream did not throw the expected exception");
        }
        catch (IllegalStateException expected) {
            // this exception is expected
        }
        catch (IOException ex) {
            fail("getRequestOutputStream did not throw the expected exception");
        }

        try {
            OutputStream out = new ChunkedOutputStream(conn.getRequestOutputStream());
            fail("getRequestOutputStream(true) did not throw the expected exception");
        }
        catch (IllegalStateException expected) {
            // this exception is expected
        }
        catch (IOException ex) {
            fail("getRequestOutputStream(true) did not throw the expected exception");
        }

        try {
            InputStream in = conn.getResponseInputStream();
            fail("getResponseInputStream() did not throw the expected exception");
        }
        catch (IllegalStateException expected) {
            // this exception is expected
        }
        catch (IOException ex) {
            fail("getResponseInputStream() did not throw the expected exception");
        }

    }
    
    /**
     * A ProtocolSocketFactory that delays before creating a socket.
     */
    class DelayedProtocolSocketFactory implements ProtocolSocketFactory {
        
        private int delay;
        private ProtocolSocketFactory realFactory;
            
        public DelayedProtocolSocketFactory(int delay, ProtocolSocketFactory realFactory) {
            this.delay = delay;
            this.realFactory = realFactory;            
        }
                
        public Socket createSocket(
            String host,
            int port,
            InetAddress localAddress,
            int localPort
        ) throws IOException, UnknownHostException {
            
            synchronized (this) {
                try {
                    this.wait(delay);
                } catch (InterruptedException e) {}
            }
            return realFactory.createSocket(host, port, localAddress, localPort);
        }

        public Socket createSocket(
            final String host,
            final int port,
            final InetAddress localAddress,
            final int localPort,
            final HttpConnectionParams params
        ) throws IOException, UnknownHostException {
            
            if (params == null) {
                throw new IllegalArgumentException("Parameters may not be null");
            }
            int timeout = params.getConnectionTimeout();
            ControllerThreadSocketFactory.SocketTask task = new ControllerThreadSocketFactory.SocketTask() {
                public void doit() throws IOException {
                    synchronized (this) {
                        try {
                            this.wait(delay);
                        } catch (InterruptedException e) {}
                    }
                    setSocket(realFactory.createSocket(host, port, localAddress, localPort));
                }
            };
            return ControllerThreadSocketFactory.createSocket(task, timeout);
        }

        public Socket createSocket(String host, int port)
            throws IOException, UnknownHostException {
            synchronized (this) {
                try {
                    this.wait(delay);
                } catch (InterruptedException e) {}
            }
            return realFactory.createSocket(host, port);
        }

    }

}

