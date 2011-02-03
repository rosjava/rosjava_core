/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/TestHttpConnectionManager.java,v 1.23 2004/07/17 18:58:33 mbecke Exp $
 * $Revision: 509320 $
 * $Date: 2007-02-19 20:52:43 +0100 (Mon, 19 Feb 2007) $
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
 */

package org.apache.commons.httpclient;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.apache.commons.httpclient.server.SimpleRequest;
import org.apache.commons.httpclient.server.SimpleResponse;

/**
 * Unit tests for {@link HttpConnectionManager}.
 *
 * @author Marc A. Saegesser
 * @version $Id: TestHttpConnectionManager.java 509320 2007-02-19 19:52:43Z mbecke $
 */
public class TestHttpConnectionManager extends HttpClientTestBase {

    // ------------------------------------------------------------ Constructor
    public TestHttpConnectionManager(String testName) throws IOException {
        super(testName);
    }

    // ------------------------------------------------------------------- Main
    public static void main(String args[]) {
        String[] testCaseName = { TestHttpConnectionManager.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestHttpConnectionManager.class);
    }


    // ----------------------------------------------------------- Test Methods

    /**
     * Test that the ConnectMethod correctly releases connections when
     * CONNECT fails.
     */
    public void testConnectMethodFailureRelease() throws Exception {
        
        MultiThreadedHttpConnectionManager mgr = new MultiThreadedHttpConnectionManager();
        mgr.getParams().setIntParameter(
            HttpConnectionManagerParams.MAX_TOTAL_CONNECTIONS, 1);
        client.setHttpConnectionManager(mgr);
        this.server.setHttpService(new RejectConnectService());
        
        // we're going to execute a connect method against the localhost, assuming
        // that CONNECT is not supported.  This should test the fakeResponse()
        // code on HttpMethodBase.
        client.getHostConfiguration().setProxy(server.getLocalAddress(), server.getLocalPort());
        // we must set the host to a secure destination or the CONNECT method
        // will not be used
        client.getHostConfiguration().setHost(
            "notARealHost", 
            1234, 
            new Protocol(
                "https", 
                (ProtocolSocketFactory)new FakeSecureProtocolSocketFactory(), 
                443)
        );
        
        GetMethod get = new GetMethod("/");
        try {
            assertTrue(client.executeMethod(get) != 200);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Error executing connect: " + e);
        }

        // this should calling releaseConnection() releases the connection
        try {
            get.releaseConnection();
            mgr.getConnectionWithTimeout(client.getHostConfiguration(), 1).releaseConnection();
        } catch (ConnectTimeoutException e1) {
            fail("Connection should have been available.");
        }
        
        get = new GetMethod("/");
        
        try {
            assertTrue(client.executeMethod(get) != 200);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Error executing connect: " + e);
        }

        // make sure reading the response fully releases the connection        
        try {
            get.getResponseBodyAsString();
            mgr.getConnectionWithTimeout(client.getHostConfiguration(), 1).releaseConnection();
        } catch (ConnectTimeoutException e1) {
            fail("Connection should have been available.");
        }     
        
        get = new GetMethod("/");
        
        try {
            assertTrue(client.executeMethod(get) != 200);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Error executing connect: " + e);
        }

        // make sure closing the output stream releases the connection        
        try {
            get.getResponseBodyAsStream().close();
            mgr.getConnectionWithTimeout(client.getHostConfiguration(), 1).releaseConnection();
        } catch (ConnectTimeoutException e) {
            fail("Connection should have been available.");
        } catch (IOException e) {
            e.printStackTrace();
            fail("Close connection failed: " + e);   
        }
    }

    public void testGetConnection() {
        MultiThreadedHttpConnectionManager mgr = new MultiThreadedHttpConnectionManager();

        HostConfiguration hostConfiguration = new HostConfiguration();
        hostConfiguration.setHost("www.nosuchserver.com", 80, "http");

        // Create a new connection
        HttpConnection conn = mgr.getConnection(hostConfiguration);
        // Validate the connection properties
        assertEquals("Host", "www.nosuchserver.com", conn.getHost());
        assertEquals("Port", 80, conn.getPort());
        // Release the connection
        mgr.releaseConnection(conn);

        // Create a new connection
        hostConfiguration.setHost("www.nosuchserver.com", -1, "https");
        conn = mgr.getConnection(hostConfiguration);
        // Validate the connection properties
        assertEquals("Host", "www.nosuchserver.com", conn.getHost());
        assertEquals("Port", 443, conn.getPort());
        // Release the connection
        mgr.releaseConnection(conn);

        // Create a new connection
        hostConfiguration.setHost("www.nowhere.org", 8080, "http");
        conn = mgr.getConnection(hostConfiguration);
        // Validate the connection properties
        assertEquals("Host", "www.nowhere.org", conn.getHost());
        assertEquals("Port", 8080, conn.getPort());
        // Release the connection
        mgr.releaseConnection(conn);

    }

    public void testDroppedThread() throws Exception {

        this.server.setHttpService(new EchoService());

        MultiThreadedHttpConnectionManager mthcm = new MultiThreadedHttpConnectionManager();
        client.setHttpConnectionManager(mthcm);
        WeakReference wr = new WeakReference(mthcm);

        GetMethod method = new GetMethod("/");
        client.executeMethod(method);
        method.releaseConnection();

        mthcm = null;
        client = null;
        method = null;
        
        System.gc();

        // this sleep appears to be necessary in order to give the JVM
        // time to clean up the miscellaneous pointers to the connection manager
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            fail("shouldn't be interrupted.");
        }

        Object connectionManager = wr.get();
        assertNull("connectionManager should be null", connectionManager);
    }    
    
    public void testWriteRequestReleaseConnection() {

        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.getParams().setDefaultMaxConnectionsPerHost(1);

        client.setHttpConnectionManager(connectionManager);
        
        GetMethod get = new GetMethod("/") {
            protected boolean writeRequestBody(HttpState state, HttpConnection conn)
                throws IOException, HttpException {
                throw new IOException("Oh no!!");
            }
        };
        
        try {
            client.executeMethod(get);
            fail("An exception should have occurred.");
        } catch (HttpException e) {
            e.printStackTrace();
            fail("HttpException should not have occurred: " + e);
        } catch (IOException e) {
            // expected
        }
        
        try {
            connectionManager.getConnectionWithTimeout(client.getHostConfiguration(), 1);
        } catch (ConnectTimeoutException e) {
            e.printStackTrace();
            fail("Connection was not released: " + e);
        }
        
    }
    
    public void testReleaseConnection() {

        this.server.setHttpService(new EchoService());

        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.getParams().setDefaultMaxConnectionsPerHost(1);

        client.setHttpConnectionManager(connectionManager);
        // we shouldn't have to wait if a connection is available
        client.getParams().setConnectionManagerTimeout(1);

        GetMethod getMethod = new GetMethod("/");

        try {
            client.executeMethod(getMethod);
        } catch (Exception e) {
            fail("error reading from server: " + e);
        }

        try {
            // this should fail quickly since the connection has not been released
            client.executeMethod(getMethod);
            fail("a httpConnection should not be available");
        } catch (ConnectTimeoutException e) {            
        } catch (HttpException e) {
            fail("error reading from server; " + e);
        } catch (IOException e) {
            e.printStackTrace();
            fail("error reading from server; " + e);
        }

        // this should release the connection
        getMethod.releaseConnection();

        getMethod = new GetMethod("/");

        try {
            // this should fail quickly if the connection has not been released
            client.executeMethod(getMethod);
        } catch (HttpException e) {
            fail("httpConnection does not appear to have been released: " + e);
        } catch (IOException e) {
            fail("error reading from server; " + e);
        }

    }

    /**
     * Makes sure that a connection gets released after the content of the body
     * is read.
     */
    public void testResponseAutoRelease() throws Exception  {

        this.server.setHttpService(new EchoService());

        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.getParams().setDefaultMaxConnectionsPerHost(1);

        client.setHttpConnectionManager(connectionManager);
        // we shouldn't have to wait if a connection is available
        client.getParams().setConnectionManagerTimeout( 1 );

        GetMethod getMethod = new GetMethod("/");

        try {
            client.executeMethod(getMethod);
        } catch (Exception e) {
            fail("error reading from server: " + e);
        }
        
        // this should release the connection
        getMethod.getResponseBody();

        getMethod = new GetMethod("/");

        try {
            // this should fail quickly if the connection has not been released
            client.executeMethod(getMethod);
        } catch (HttpException e) {
            fail("httpConnection does not appear to have been released: " + e);
        } catch (IOException e) {
            fail("error reading from server; " + e);
        }

    }
    
    /**
     * Tests the MultiThreadedHttpConnectionManager's ability to reclaim unused 
     * connections.
     */
    public void testConnectionReclaiming() {
        
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.getParams().setDefaultMaxConnectionsPerHost(1);
        connectionManager.getParams().setMaxTotalConnections(1);

        HostConfiguration host1 = new HostConfiguration();
        host1.setHost("host1", -1, "http");

        HostConfiguration host2 = new HostConfiguration();
        host2.setHost("host2", -1, "http");

        HttpConnection connection = connectionManager.getConnection(host1);
        // now release this connection
        connection.releaseConnection();
        connection = null;
        
        try {
            // the connection from host1 should be reclaimed
            connection = connectionManager.getConnectionWithTimeout(host2, 100);
        } catch (ConnectTimeoutException e) {
            e.printStackTrace();
            fail("a httpConnection should have been available: " + e);
        }        
    }
    
    /**
     * Tests that {@link MultiThreadedHttpConnectionManager#shutdownAll()} closes all resources
     * and makes all connection mangers unusable.
     */
    public void testShutdownAll() {

        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.getParams().setDefaultMaxConnectionsPerHost(1);
        connectionManager.getParams().setMaxTotalConnections(1);

        HostConfiguration host1 = new HostConfiguration();
        host1.setHost("host1", -1, "http");

        // hold on to the only connection
        HttpConnection connection = connectionManager.getConnection(host1);

        // wait for a connection on another thread
        GetConnectionThread getConn = new GetConnectionThread(host1, connectionManager, 0);
        getConn.start();
        
        MultiThreadedHttpConnectionManager.shutdownAll();
        
        // now release this connection, this should close the connection, but have no other effect
        connection.releaseConnection();
        connection = null;
        
        try {
            getConn.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // this thread should have caught an exception without getting a connection
        assertNull("Not connection should have been checked out", getConn.getConnection());
        assertNotNull("There should have been an exception", getConn.getException());
        
        try {
            connectionManager.getConnection(host1);
            fail("An exception should have occurred");
        } catch (Exception e) {
            // this is expected
        }
    }
        
    /**
     * Tests that {@link MultiThreadedHttpConnectionManager#shutdown()} closes all resources
     * and makes the connection manger unusable.
     */
    public void testShutdown() {

        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.getParams().setDefaultMaxConnectionsPerHost(1);
        connectionManager.getParams().setMaxTotalConnections(1);

        HostConfiguration host1 = new HostConfiguration();
        host1.setHost("host1", -1, "http");

        // hold on to the only connection
        HttpConnection connection = connectionManager.getConnection(host1);

        // wait for a connection on another thread
        GetConnectionThread getConn = new GetConnectionThread(host1, connectionManager, 0);
        getConn.start();
        
        connectionManager.shutdown();
        
        // now release this connection, this should close the connection, but have no other effect
        connection.releaseConnection();
        connection = null;
        
        try {
            getConn.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // this thread should have caught an exception without getting a connection
        assertNull("Not connection should have been checked out", getConn.getConnection());
        assertNotNull("There should have been an exception", getConn.getException());
        
        try {
            connectionManager.getConnection(host1);
            fail("An exception should have occurred");
        } catch (Exception e) {
            // this is expected
        }
    }
    
    /**
     * Tests the MultiThreadedHttpConnectionManager's ability to restrict the maximum number 
     * of connections.
     */    
    public void testMaxConnections() {
        
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.getParams().setDefaultMaxConnectionsPerHost(1);
        connectionManager.getParams().setMaxTotalConnections(2);

        HostConfiguration host1 = new HostConfiguration();
        host1.setHost("host1", -1, "http");

        HostConfiguration host2 = new HostConfiguration();
        host2.setHost("host2", -1, "http");

        HttpConnection connection1 = connectionManager.getConnection(host1);
        HttpConnection connection2 = connectionManager.getConnection(host2);
    
        try {
            // this should fail quickly since the connection has not been released
            connectionManager.getConnectionWithTimeout(host2, 100);
            fail("ConnectionPoolTimeoutException should not be available");
        } catch (ConnectionPoolTimeoutException e) {
            // this should throw an exception
        }
        
        // release one of the connections
        connection2.releaseConnection();
        connection2 = null;
        
        try {
            // there should be a connection available now
            connection2 = connectionManager.getConnectionWithTimeout(host2, 100);
        } catch (ConnectionPoolTimeoutException e) {
            e.printStackTrace();
            fail("a httpConnection should have been available: " + e);
        }
    }    

    /**
     * Tests the MultiThreadedHttpConnectionManager's ability to restrict the maximum number 
     * of connections per host.
     */    
    public void testMaxConnectionsPerHost() throws Exception {
        
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.getParams().setDefaultMaxConnectionsPerHost(1);
        connectionManager.getParams().setMaxTotalConnections(100);

        HostConfiguration host1 = new HostConfiguration();
        host1.setHost("host1", -1, "http");

        HostConfiguration host2 = new HostConfiguration();
        host2.setHost("host2", -1, "http");

        HostConfiguration host3 = new HostConfiguration();
        host3.setHost("host3", -1, "http");

        connectionManager.getParams().setMaxConnectionsPerHost(host1, 3);
        connectionManager.getParams().setMaxConnectionsPerHost(host2, 2);

        // Host1
        HttpConnection connection1 = connectionManager.getConnectionWithTimeout(host1, 1000);
        HttpConnection connection2 = connectionManager.getConnectionWithTimeout(host1, 1000);
        HttpConnection connection3 = connectionManager.getConnectionWithTimeout(host1, 1000);
        try {
            // this should fail quickly since the connection has not been released
            connectionManager.getConnectionWithTimeout(host1, 100);
            fail("ConnectionPoolTimeoutException should not be available");
        } catch (ConnectionPoolTimeoutException e) {
            // expected
        }
        
        // Host2
        connection1 = connectionManager.getConnectionWithTimeout(host2, 1000);
        connection2 = connectionManager.getConnectionWithTimeout(host2, 1000);
        try {
            // this should fail quickly since the connection has not been released
            connectionManager.getConnectionWithTimeout(host2, 100);
            fail("ConnectionPoolTimeoutException should not be available");
        } catch (ConnectionPoolTimeoutException e) {
            // expected
        }

        // Host3 (should use the default per host value)
        connection1 = connectionManager.getConnectionWithTimeout(host3, 1000);
        try {
            // this should fail quickly since the connection has not been released
            connectionManager.getConnectionWithTimeout(host3, 100);
            fail("ConnectionPoolTimeoutException should not be available");
        } catch (ConnectionPoolTimeoutException e) {
            // expected
        }
    }    

    public void testHostReusePreference() {
        
        final MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.getParams().setDefaultMaxConnectionsPerHost(1);
        connectionManager.getParams().setMaxTotalConnections(1);

        final HostConfiguration host1 = new HostConfiguration();
        host1.setHost("host1", -1, "http");

        final HostConfiguration host2 = new HostConfiguration();
        host2.setHost("host2", -1, "http");

        HttpConnection connection = connectionManager.getConnection(host1);

        GetConnectionThread getHost1 = new GetConnectionThread(host1, connectionManager, 200);
        GetConnectionThread getHost2 = new GetConnectionThread(host2, connectionManager, 200);
        
        getHost2.start();
        getHost1.start();
        
        // give the threads some time to startup
        try {
            Thread.sleep(100);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
            
        // after the connection to host1 is released it should be given to getHost1
        connection.releaseConnection();
        connection = null;

        try {
            getHost1.join();
            getHost2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertNotSame(
            "Connection should have been given to someone", 
            getHost1.getConnection(),
            getHost2.getConnection()
        );        
        assertNotNull("Connection should have been given to host1", getHost1.getConnection());
        assertNull("Connection should NOT have been given to host2", getHost2.getConnection());
        
    } 
    
    public void testMaxConnectionsPerServer() {
     
        this.server.setHttpService(new EchoService());

        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.getParams().setDefaultMaxConnectionsPerHost(1);

        client.setHttpConnectionManager(connectionManager);
        // we shouldn't have to wait if a connection is available
        client.getParams().setConnectionManagerTimeout( 1 );

        GetMethod getMethod = new GetMethod("/");

        try {
            client.executeMethod(getMethod);
        } catch (Exception e) {
            fail("error reading from server: " + e);
        }

        GetMethod getMethod2 = new GetMethod("/");

        try {
            // this should fail quickly since the connection has not been released
            client.executeMethod(getMethod2);
            fail("a httpConnection should not be available");
        } catch (ConnectTimeoutException e) {
        } catch (HttpException e) {
            fail("error reading from server; " + e);
        } catch (IOException e) {
            fail("error reading from server; " + e);
        }
                
    }
    
    public void testDeleteClosedConnections() {
        
        MultiThreadedHttpConnectionManager manager = new MultiThreadedHttpConnectionManager();
        
        HttpConnection conn = manager.getConnection(client.getHostConfiguration());
        
        assertEquals("connectionsInPool", manager.getConnectionsInPool(), 1);
        assertEquals("connectionsInPool(host)", manager.getConnectionsInPool(client.getHostConfiguration()), 1);
        
        conn.close();
        conn.releaseConnection();

        assertEquals("connectionsInPool", manager.getConnectionsInPool(), 1);
        assertEquals("connectionsInPool(host)", manager.getConnectionsInPool(client.getHostConfiguration()), 1);

        manager.deleteClosedConnections();
        
        assertEquals("connectionsInPool", manager.getConnectionsInPool(), 0);
        assertEquals("connectionsInPool(host)", manager.getConnectionsInPool(client.getHostConfiguration()), 0);
    }
    
    /**
     * Tests that thread waiting in the MultiThreadedHttpConnectionManager can be 
     * interrupted.
     */
    public void testWaitingThreadInterrupted() {

        this.server.setHttpService(new EchoService());

        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.getParams().setIntParameter(
            HttpConnectionManagerParams.MAX_TOTAL_CONNECTIONS, 1);

        HostConfiguration host1 = new HostConfiguration();
        host1.setHost("host1", -1, "http");

        // hold on to the only connection
        HttpConnection connection = connectionManager.getConnection(host1);

        // wait for a connection on another thread
        GetConnectionThread getConn = new GetConnectionThread(host1, connectionManager, 1000);
        getConn.start();
        
        // give the thread a chance to block
        synchronized (this) {
            try {
                this.wait(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        // interrupt the thread, this should cancel waiting with a RuntimeException
        getConn.interrupt();
        
        try {
            getConn.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // make sure the correct exception was thrown
        assertTrue(getConn.exception != null);
        assertEquals(getConn.exception.getClass(), IllegalThreadStateException.class);
        
        // make sure the connection manager is still working
        connection.releaseConnection();
        try {
            connectionManager.getConnectionWithTimeout(host1, 10);
        } catch (ConnectionPoolTimeoutException e) {
            fail("Connection not available");
        }
    }
    
    public void testReclaimUnusedConnection() {

        this.server.setHttpService(new EchoService());

        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.getParams().setIntParameter(
            HttpConnectionManagerParams.MAX_TOTAL_CONNECTIONS, 1);

        client.setHttpConnectionManager(connectionManager);
        // we shouldn't have to wait if a connection is available
        client.getParams().setConnectionManagerTimeout( 30000 );

        GetMethod getMethod = new GetMethod("/");

        try {
            client.executeMethod(getMethod);
        } catch (Exception e) {
            fail("error reading from server: " + e);
        }

        getMethod = new GetMethod("/");
        
        Runtime.getRuntime().gc();

        try {
            // we didn't explicitly release the connection, but it should be 
            // reclaimed by the garbage collector, we hope:)
            client.executeMethod(getMethod);
        } catch (HttpException e) {
            fail("httpConnection does not appear to have been reclaimed by the GC: " + e);
        } catch (IOException e) {
            fail("error reading from server; " + e);
        }

    }
    
    public void testGetFromMultipleThreads() {
        
        this.server.setHttpService(new EchoService());

        client.setHttpConnectionManager(new MultiThreadedHttpConnectionManager());
        ExecuteMethodThread[] threads = new ExecuteMethodThread[10];
        
        for (int i = 0; i < threads.length; i++) {
            GetMethod method = new GetMethod("/");
            method.setFollowRedirects(true);
            
            threads[i] = new ExecuteMethodThread(method, client);
            threads[i].start();
        }
        
        for (int i = 0; i < threads.length; i++) {
            try {
                // wait until this thread finishes. we'll give it 10 seconds,
                // but it shouldn't take that long
                threads[i].join(10000);
            } catch (InterruptedException e) {
            }
            // make sure an exception did not occur
            Exception e = threads[i].getException();
            if (e != null) {
                fail("An error occured in the get: " + e);
            }
            // we should have a 200 status
            assertEquals(threads[i].getMethod().getStatusCode(), HttpStatus.SC_OK);
        }
    }

    public void testTimeout() {
        MultiThreadedHttpConnectionManager mgr = new MultiThreadedHttpConnectionManager();
        mgr.getParams().setDefaultMaxConnectionsPerHost(2);
        
        try{
            HostConfiguration hostConfig = new HostConfiguration();
            hostConfig.setHost("www.nosuchserver.com", 80, "http");
            
            HttpConnection conn1 = mgr.getConnection(hostConfig);
            HttpConnection conn2 = mgr.getConnection(hostConfig);
            
            HttpConnection conn3 = mgr.getConnectionWithTimeout(hostConfig, 1000);
            fail("Expected an HttpException.");
            
        }catch(ConnectTimeoutException e){
            //Expected result
        }
    }
    
    static class FakeSecureProtocolSocketFactory implements SecureProtocolSocketFactory {
        
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
            throws IOException, UnknownHostException {
            throw new IllegalStateException("createSocket() should never have been called.");
        }
        
        public Socket createSocket(String host, int port)
            throws IOException, UnknownHostException {
            throw new IllegalStateException("createSocket() should never have been called.");
        }
        
        public Socket createSocket(String host, int port, InetAddress clientHost, int clientPort)
            throws IOException, UnknownHostException {
            throw new IllegalStateException("createSocket() should never have been called.");
        }
        
        public Socket createSocket(String host, int port, InetAddress clientHost, int clientPort, 
            HttpConnectionParams params)
            throws IOException, UnknownHostException {
            throw new IllegalStateException("createSocket() should never have been called.");
        }
    }
    
    static class RejectConnectService extends EchoService {
        public boolean process(SimpleRequest request, SimpleResponse response)
                throws IOException {
            if (request.getRequestLine().getMethod().equalsIgnoreCase("CONNECT")) {
                response.setStatusLine(request.getRequestLine().getHttpVersion(), HttpStatus.SC_METHOD_NOT_ALLOWED);
                response.setHeader(new Header("Connection", "close"));
                return true;
            } else {
                return super.process(request, response);
            }
        }
    }
    
    static class GetConnectionThread extends Thread {
        
        private HostConfiguration hostConfiguration;
        private MultiThreadedHttpConnectionManager connectionManager;
        private HttpConnection connection;
        private long timeout;
        private Exception exception;
        
        public GetConnectionThread(
            HostConfiguration hostConfiguration, 
            MultiThreadedHttpConnectionManager connectionManager,
            long timeout
        ) {
            this.hostConfiguration = hostConfiguration;
            this.connectionManager = connectionManager; 
            this.timeout = timeout;
        }
        
        public void run() {
            try {
                connection = connectionManager.getConnectionWithTimeout(hostConfiguration, timeout);
            } catch (Exception e) {
                this.exception = e;
            }            
        }
        
        public Exception getException() {
            return exception;
        }
        
        public HttpConnection getConnection() {
            return connection;
        }

    }
    
}

