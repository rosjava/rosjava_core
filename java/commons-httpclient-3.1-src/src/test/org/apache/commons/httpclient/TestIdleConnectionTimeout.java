/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/TestIdleConnectionTimeout.java,v 1.5 2004/11/07 12:31:42 olegk Exp $
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
package org.apache.commons.httpclient;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.util.IdleConnectionHandler;
import org.apache.commons.httpclient.util.IdleConnectionTimeoutThread;

/**
 */
public class TestIdleConnectionTimeout extends TestCase {
    /**
     * 
     */
    public TestIdleConnectionTimeout() {
        super();
    }
    /**
     * @param arg0
     */
    public TestIdleConnectionTimeout(String arg0) {
        super(arg0);
    }
    
    // ------------------------------------------------------- TestCase Methods

    public static Test suite() {
        return new TestSuite(TestIdleConnectionTimeout.class);
    }

    /**
     * Tests that the IdleConnectionHandler correctly closes connections.
     */
    public void testHandler() {
        
        TimeoutHttpConnection connection = new TimeoutHttpConnection();
        
        IdleConnectionHandler handler = new IdleConnectionHandler();
        
        handler.add(connection);
        
        synchronized(this) {
            try {
                this.wait(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        handler.closeIdleConnections(100);
        
        assertTrue("Connection not closed", connection.isClosed());

        connection.setClosed(false);
        
        handler.remove(connection);
        
        synchronized(this) {
            try {
                this.wait(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        handler.closeIdleConnections(100);
        
        assertFalse("Connection closed", connection.isClosed());
    }

    /**
     * Tests that the IdleConnectionTimeoutThread works correctly.
     */
    public void testTimeoutThread() {
        
        TimeoutHttpConnectionManager cm = new TimeoutHttpConnectionManager();        
        
        IdleConnectionTimeoutThread timeoutThread = new IdleConnectionTimeoutThread();
        timeoutThread.addConnectionManager(cm);
        timeoutThread.setTimeoutInterval(100);
        timeoutThread.start();
        
        synchronized(this) {
            try {
                this.wait(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        assertTrue("closeIdleConnections() not called", cm.closed);

        timeoutThread.removeConnectionManager(cm);
        cm.closed = false;
        
        synchronized(this) {
            try {
                this.wait(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        assertFalse("closeIdleConnections() called", cm.closed);
        
        timeoutThread.shutdown();
    }    
    
    private static class TimeoutHttpConnectionManager implements HttpConnectionManager {
        
        public boolean closed = false;
        
        public void closeIdleConnections(long idleTimeout) {
            this.closed = true;
        }

        /**
         * @deprecated
         */
        public HttpConnection getConnection(HostConfiguration hostConfiguration, long timeout)
            throws HttpException {
            return null;
        }

        public HttpConnection getConnection(HostConfiguration hostConfiguration) {
            return null;
        }

        public HttpConnection getConnectionWithTimeout(HostConfiguration hostConfiguration,
            long timeout) throws ConnectionPoolTimeoutException {
            return null;
        }

        public HttpConnectionManagerParams getParams() {
            return null;
        }

        public void releaseConnection(HttpConnection conn) {
        }

        public void setParams(HttpConnectionManagerParams params) {
        }
}
    
    private static class TimeoutHttpConnection extends HttpConnection {
        
        private boolean closed = false;;
        
        public TimeoutHttpConnection() {
            super("fake-host", 80);
        }
        
        /**
         * @return Returns the closed.
         */
        public boolean isClosed() {
            return closed;
        }
        /**
         * @param closed The closed to set.
         */
        public void setClosed(boolean closed) {
            this.closed = closed;
        }
        
        /* (non-Javadoc)
         * @see org.apache.commons.httpclient.HttpConnection#close()
         */
        public void close() {
            closed = true;
        }
    }
    
}
