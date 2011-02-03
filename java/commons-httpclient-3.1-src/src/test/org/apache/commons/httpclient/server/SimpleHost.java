/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/server/SimpleHost.java,v 1.1 2004/11/13 12:21:28 olegk Exp $
 * $Revision: 510582 $
 * $Date: 2007-02-22 17:42:43 +0100 (Thu, 22 Feb 2007) $
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

/**
 * @author Oleg Kalnichevski
 */
public class SimpleHost implements Cloneable {

    private String hostname = null;

    private int port = -1;

    public SimpleHost(final String hostname, int port) {
        super();
        if (hostname == null) {
            throw new IllegalArgumentException("Host name may not be null");
        }
        if (port < 0) {
            throw new IllegalArgumentException("Port may not be negative");
        }
        this.hostname = hostname;
        this.port = port;
    }

    public SimpleHost (final SimpleHost httphost) {
        super();
        init(httphost);
    }

    private void init(final SimpleHost httphost) {
        this.hostname = httphost.hostname;
        this.port = httphost.port;
    }

    public Object clone() throws CloneNotSupportedException {
        SimpleHost copy = (SimpleHost) super.clone();
        copy.init(this);
        return copy;
    }    
    
    public String getHostName() {
        return this.hostname;
    }

    public int getPort() {
        return this.port;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer(50);        
        buffer.append(this.hostname);
        buffer.append(':');
        buffer.append(this.port);
        return buffer.toString();
    }    
    
    public boolean equals(final Object o) {
        
        if (o instanceof SimpleHost) {
            if (o == this) { 
                return true;
            }
            SimpleHost that = (SimpleHost) o;
            if (!this.hostname.equalsIgnoreCase(that.hostname)) {
                return false;
            }
            if (this.port != that.port) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.hostname.hashCode() + this.port;
    }

}
