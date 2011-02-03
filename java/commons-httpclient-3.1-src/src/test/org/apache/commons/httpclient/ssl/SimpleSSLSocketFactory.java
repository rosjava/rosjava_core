/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/ssl/SimpleSSLSocketFactory.java,v 1.1 2004/12/11 22:35:26 olegk Exp $
 * $Revision: 514390 $
 * $Date: 2007-03-04 13:37:15 +0100 (Sun, 04 Mar 2007) $
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

package org.apache.commons.httpclient.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.URL;
import java.security.KeyStore;

import javax.net.ServerSocketFactory;

import org.apache.commons.httpclient.server.SimpleSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.net.ssl.KeyManager;
import com.sun.net.ssl.KeyManagerFactory;
import com.sun.net.ssl.SSLContext;

/**
 * Defines a SSL socket factory
 * 
 * @author Oleg Kalnichevski
 */
public class SimpleSSLSocketFactory implements SimpleSocketFactory {
    
    private static final Log LOG = LogFactory.getLog(SimpleSocketFactory.class);

    private static SSLContext SSLCONTEXT = null;
    
    private static SSLContext createSSLContext() {
        try {
            ClassLoader cl = SimpleSocketFactory.class.getClassLoader();
            URL url = cl.getResource("org/apache/commons/httpclient/ssl/simpleserver.keystore");
            KeyStore keystore  = KeyStore.getInstance("jks");
            InputStream is = null;
            try {
                is = url.openStream();
                keystore.load(is, "nopassword".toCharArray());
            } finally {
                if (is != null) is.close();
            }
            KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
            kmfactory.init(keystore, "nopassword".toCharArray());
            KeyManager[] keymanagers = kmfactory.getKeyManagers(); 
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(keymanagers, null, null);
            return sslcontext;
        } catch (Exception ex) {
            // this is not the way a sane exception handling should be done
            // but for our simple HTTP testing framework this will suffice
            LOG.error(ex.getMessage(), ex);
            throw new IllegalStateException(ex.getMessage());
        }
    
    }
    
    private static SSLContext getSSLContext() {
        if (SSLCONTEXT == null) {
            SSLCONTEXT = createSSLContext();
        }
        return SSLCONTEXT;
    }
    
    public SimpleSSLSocketFactory() {
        super();
    }
    
    public ServerSocket createServerSocket(int port) throws IOException {
        ServerSocketFactory socketfactory = getSSLContext().getServerSocketFactory();
        return socketfactory.createServerSocket(port);
    }
    
}
