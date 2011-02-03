/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/server/SimpleProxy.java,v 1.8 2004/12/11 22:35:26 olegk Exp $
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

import org.apache.commons.httpclient.Credentials;

/**
 * Simple server that registers default request handlers to act as a proxy.
 * 
 * @author Ortwin Glueck
 * @author Oleg Kalnichevski
 */
public class SimpleProxy extends SimpleHttpServer {
    
    private SimpleConnManager connmanager = null; 
    private HttpRequestHandlerChain stdchain = null;

    public SimpleProxy(int port) throws IOException {
        super(port);
        this.connmanager = new SimpleConnManager(); 
        this.stdchain = new HttpRequestHandlerChain();
        this.stdchain.appendHandler(new TransparentProxyRequestHandler());
        this.stdchain.appendHandler(new ProxyRequestHandler(this.connmanager));
        setRequestHandler(this.stdchain);
    }

    public SimpleProxy() throws IOException {
        this(0);
    }

    public void requireAuthentication(final Credentials creds, final String realm, boolean keepalive) {
        HttpRequestHandlerChain chain = new HttpRequestHandlerChain(this.stdchain); 
        chain.prependHandler(new ProxyAuthRequestHandler(creds ,realm, keepalive));
        setRequestHandler(chain);
    }

    public void requireAuthentication(final Credentials creds) {
        HttpRequestHandlerChain chain = new HttpRequestHandlerChain(this.stdchain); 
        chain.prependHandler(new ProxyAuthRequestHandler(creds));
        setRequestHandler(chain);
    }

    public void destroy() {
        super.destroy();
        this.connmanager.shutdown();
    }
    
    public void addHandler(final HttpRequestHandler handler) {
        this.stdchain.prependHandler(handler);
    }
    
}
