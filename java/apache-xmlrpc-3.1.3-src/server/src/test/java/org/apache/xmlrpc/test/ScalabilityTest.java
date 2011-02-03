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
package org.apache.xmlrpc.test;

import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
import org.apache.xmlrpc.util.ThreadPool;
import org.apache.xmlrpc.webserver.ServletWebServer;
import org.apache.xmlrpc.webserver.WebServer;
import org.apache.xmlrpc.webserver.XmlRpcServlet;

import junit.framework.TestCase;


/**
 * Tests the frameworks scalability.
 */
public class ScalabilityTest extends TestCase {
    /**
     * Primitive handler class
     */
    public static class Adder {
        /**
         * Returns the sum of the numbers p1 and p2.
         */
        public int add(int p1, int p2) {
            return p1 + p2;
        }
    }

    private class MyServletWebServer extends ServletWebServer {
        protected ThreadPool pool;
        MyServletWebServer(HttpServlet pServlet, int pPort)
                throws ServletException {
            super(pServlet, pPort);
        }
        public ThreadPool newThreadPool(){
            pool = new ThreadPool(getXmlRpcServer().getMaxThreads(), "XML-RPC"){
            };
            return pool;
        }
        int getNumThreads() {
            return pool.getNumThreads();
        }
    }

    private class MyWebServer extends WebServer {
        protected ThreadPool pool;
        MyWebServer(int pPort) {
            super(pPort);
        }
        public ThreadPool newThreadPool(){
            pool = new ThreadPool(getXmlRpcServer().getMaxThreads(), "XML-RPC"){
            };
            return pool;
        }
        int getNumThreads() {
            return pool.getNumThreads();
        }
    }
    
    private static final int BASE = 1;
    private static final Integer THREE = new Integer(3);
    private static final Integer FIVE = new Integer(5);
    private static final Integer EIGHT = new Integer(8);
    private XmlRpcServlet servlet;
    private MyServletWebServer server;
    private MyWebServer webServer;

    private XmlRpcHandlerMapping newXmlRpcHandlerMapping() throws XmlRpcException {
        PropertyHandlerMapping mapping = new PropertyHandlerMapping();
        mapping.addHandler("Adder", Adder.class);
        return mapping;
    }

    private void initServletWebServer() throws Exception {
        servlet = new XmlRpcServlet(){
            private static final long serialVersionUID = -2040521497373327817L;
            protected XmlRpcHandlerMapping newXmlRpcHandlerMapping()
                    throws XmlRpcException {
                return ScalabilityTest.this.newXmlRpcHandlerMapping();

            }
            
        };
        server = new MyServletWebServer(servlet, 0);
        server.getXmlRpcServer().setMaxThreads(25);
        server.start();
    }

    private void shutdownServletWebServer() {
        server.shutdown();
    }

    private void initWebServer() throws Exception {
        webServer = new MyWebServer(0);
        webServer.getXmlRpcServer().setHandlerMapping(newXmlRpcHandlerMapping());
        webServer.getXmlRpcServer().setMaxThreads(25);
        webServer.start();
    }

    private void shutdownWebServer() {
        webServer.shutdown();
    }

    /**
     * Runs the test with a single client.
     */
    public void testSingleClient() throws Exception {
        initServletWebServer();
        boolean ok = false;
        try {
            long now = System.currentTimeMillis();
            servlet.getXmlRpcServletServer().setMaxThreads(1);
            new Client(100*BASE, server.getPort()).run();
            System.out.println("Single client: " + (System.currentTimeMillis()-now) + ", " + server.getNumThreads());
            shutdownServletWebServer();
            ok = true;
        } finally {
            if (!ok) { try { shutdownServletWebServer(); } catch (Throwable t) {} }
        }
    }

    /**
     * Runs the web server test with a single client.
     */
    public void testSingleWebServerClient() throws Exception {
        initWebServer();
        boolean ok = false;
        try {
            long now = System.currentTimeMillis();
            webServer.getXmlRpcServer().setMaxThreads(1);
            new Client(100*BASE, webServer.getPort()).run();
            System.out.println("Single client: " + (System.currentTimeMillis()-now) + ", " + webServer.getNumThreads());
            shutdownWebServer();
            ok = true;
        } finally {
            if (!ok) { try { shutdownWebServer(); } catch (Throwable t) {} }
        }
    }

    private static class Client implements Runnable {
        private final int iterations;
        private final int port;
        Client(int pIterations, int pPort) {
            iterations = pIterations;
            port = pPort;
        }
        public void run() {
            try {
                XmlRpcClient client = new XmlRpcClient();
                XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
                config.setServerURL(new URL("http://127.0.0.1:" + port + "/"));
                client.setConfig(config);
                for (int i = 0;  i < iterations;  i++) {
                    assertEquals(EIGHT, client.execute("Adder.add", new Object[]{THREE, FIVE}));
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    /**
     * Runs the test with ten clients.
     */
    public void testTenClient() throws Exception {
        initServletWebServer();
        boolean ok = false;
        try {
            final Thread[] threads = new Thread[10];
            servlet.getXmlRpcServletServer().setMaxThreads(10);
            long now = System.currentTimeMillis();
            for (int i = 0;  i < threads.length;  i++) {
                threads[i] = new Thread(new Client(10*BASE, server.getPort()));
                threads[i].start();
            }
            for (int i = 0;  i < threads.length;  i++) {
                threads[i].join();
            }
            System.out.println("Ten clients: " + (System.currentTimeMillis() - now) + ", " + server.getNumThreads());
            shutdownServletWebServer();
            ok = false;
        } finally {
            if (!ok) { try { shutdownServletWebServer(); } catch (Throwable t) {} }
        }
    }
}
