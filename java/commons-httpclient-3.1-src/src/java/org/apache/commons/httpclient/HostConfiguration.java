/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/java/org/apache/commons/httpclient/HostConfiguration.java,v 1.23 2005/01/14 21:16:40 olegk Exp $
 * $Revision: 510585 $
 * $Date: 2007-02-22 17:52:16 +0100 (Thu, 22 Feb 2007) $
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

import org.apache.commons.httpclient.params.HostParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.util.LangUtils;

import java.net.InetAddress;

/**
 * Holds all of the variables needed to describe an HTTP connection to a host.  This includes 
 * remote host, port and protocol, proxy host and port, local address, and virtual host.
 * 
 * @author <a href="mailto:becke@u.washington.edu">Michael Becke</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @author Laura Werner
 * 
 * @since 2.0 
 */
public class HostConfiguration implements Cloneable {

    /**
     * A value to represent any host configuration, instead of using something like
     * <code>null</code>. This value should be treated as immutable and only used in 
     * lookups and other such places to represent "any" host config.
     */
    public static final HostConfiguration ANY_HOST_CONFIGURATION = new HostConfiguration();

    /** The host to use. */
    private HttpHost host = null;

    /** The host name of the proxy server */
    private ProxyHost proxyHost = null;

    /** The local address to use when creating the socket, or null to use the default */
    private InetAddress localAddress = null;

    /** Parameters specific to this host */
    private HostParams params = new HostParams();

    /**
     * Constructor for HostConfiguration.
     */
    public HostConfiguration() {
        super();
    }
    
    /**
     * Copy constructor for HostConfiguration
     * 
     * @param hostConfiguration the hostConfiguration to copy
     */
    public HostConfiguration (final HostConfiguration hostConfiguration) {
        init(hostConfiguration);        
    }

    private void init(final HostConfiguration hostConfiguration) {
        // wrap all of the assignments in a synchronized block to avoid
        // having to negotiate the monitor for each method call
        synchronized (hostConfiguration) {
            try {
                if (hostConfiguration.host != null) {
                    this.host = (HttpHost) hostConfiguration.host.clone();
                } else {
                    this.host = null;
                }
                if (hostConfiguration.proxyHost != null) {
                    this.proxyHost = (ProxyHost) hostConfiguration.proxyHost.clone();
                } else {
                    this.proxyHost = null;
                }
                this.localAddress = hostConfiguration.getLocalAddress();
                this.params = (HostParams)hostConfiguration.getParams().clone();
            } catch (CloneNotSupportedException e) {
                throw new IllegalArgumentException("Host configuration could not be cloned");
            }
        }        
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        HostConfiguration copy;
        try {
            copy = (HostConfiguration) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException("Host configuration could not be cloned");
        }
        copy.init(this);
        return copy;
    }    
    
    /**
     * @see java.lang.Object#toString()
     */
    public synchronized String toString() {
        
        boolean appendComma = false;
        StringBuffer b = new StringBuffer(50);        
        b.append("HostConfiguration[");
        
        if (this.host != null) {
            appendComma = true;
            b.append("host=").append(this.host);
        }
        if (this.proxyHost != null) {
            if (appendComma) {
                b.append(", ");
            } else {
                appendComma = true;
            }
            b.append("proxyHost=").append(this.proxyHost);
        }
        if (this.localAddress != null) {
            if (appendComma) {
                b.append(", ");
            } else {
                appendComma = true;
            }
            b.append("localAddress=").append(this.localAddress);
            if (appendComma) {
                b.append(", ");
            } else {
                appendComma = true;
            }
            b.append("params=").append(this.params);
        }
        b.append("]");
        return b.toString();
    }    
    
    /**
     * Tests if the host configuration equals the configuration set on the
     * connection. True only if the host, port, protocol, local address and virtual address
     * are equal.  If no host configuration has been set false will be returned.
     * 
     * @param connection the connection to test against
     * @return <code>true</code> if the connection's host information equals that of this
     * configuration
     * 
     * @see #proxyEquals(HttpConnection)
     */
    public synchronized boolean hostEquals(final HttpConnection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection may not be null");
        }
        if (this.host != null) {
            if (!this.host.getHostName().equalsIgnoreCase(connection.getHost())) {
                return false;
            }
            if (this.host.getPort() != connection.getPort()) {
                return false;
            }
            if (!this.host.getProtocol().equals(connection.getProtocol())) {
                return false;
            }
            if (this.localAddress != null) {
                if (!this.localAddress.equals(connection.getLocalAddress())) {
                    return false;
                }
            } else {
                if (connection.getLocalAddress() != null) {
                    return false; 
                }
            }
            return true;
        } else {
            return false;   
        }
    }

    /**
     * Tests if the proxy configuration equals the configuration set on the
     * connection. True only if the proxyHost and proxyPort are equal.
     *
     * @param connection the connection to test against
     * @return <code>true</code> if the connection's proxy information equals that of this
     * configuration
     *
     * @see #hostEquals(HttpConnection)
     */
    public synchronized boolean proxyEquals(final HttpConnection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection may not be null");
        }
        if (this.proxyHost != null) {
            return
                this.proxyHost.getHostName().equalsIgnoreCase(connection.getProxyHost())
                && this.proxyHost.getPort() == connection.getProxyPort();
        } else {
            return connection.getProxyHost() == null;
        }
    }    
    
    /**
     * Returns true if the host is set.
     * @return <code>true</code> if the host is set.
     * 
     * @deprecated no longer used
     */
    public synchronized boolean isHostSet() {
        return this.host != null;
    }

    /**
     * Sets the given host
     * 
     * @param host the host
     */
    public synchronized void setHost(final HttpHost host) {
        this.host = host;
    }
    
    /**
     * Sets the given host, port and protocol
     * 
     * @param host the host(IP or DNS name)
     * @param port The port
     * @param protocol The protocol.
     */
    public synchronized void setHost(final String host, int port, final String protocol) {
        this.host = new HttpHost(host, port, Protocol.getProtocol(protocol));
    }
    
    /**
     * Sets the given host, virtual host, port and protocol.
     * 
     * @param host the host(IP or DNS name)
     * @param virtualHost the virtual host name or <code>null</code>
     * @param port the host port or -1 to use protocol default
     * @param protocol the protocol
     * 
     * @deprecated #setHost(String, int, Protocol)
     */
    public synchronized void setHost(final String host, final String virtualHost, int port, 
        final Protocol protocol) {
        setHost(host, port, protocol);
        this.params.setVirtualHost(virtualHost);
    }

    /**
     * Sets the given host, port and protocol.
     *   
     * @param host the host(IP or DNS name)
     * @param port The port
     * @param protocol the protocol
     */
    public synchronized void setHost(final String host, int port, final Protocol protocol) {
        if (host == null) {
            throw new IllegalArgumentException("host must not be null");   
        }
        if (protocol == null) {
            throw new IllegalArgumentException("protocol must not be null");   
        }
        this.host = new HttpHost(host, port, protocol);
    }

    /**
     * Sets the given host and port.  Uses the default protocol "http".
     * 
     * @param host the host(IP or DNS name)
     * @param port The port
     */
    public synchronized void setHost(final String host, int port) {
        setHost(host, port, Protocol.getProtocol("http"));
    }
    
    /**
     * Set the given host. Uses the default protocol("http") and its port.
     * 
     * @param host The host(IP or DNS name).
     */
    public synchronized void setHost(final String host) {
        Protocol defaultProtocol = Protocol.getProtocol("http"); 
        setHost(host, defaultProtocol.getDefaultPort(), defaultProtocol);
    }
    
    /**
     * Sets the protocol, host and port from the given URI.
     * @param uri the URI.
     */
    public synchronized void setHost(final URI uri) {
        try {
            setHost(uri.getHost(), uri.getPort(), uri.getScheme());
        } catch (URIException e) {
            throw new IllegalArgumentException(e.toString());
        }
    }

    /**
     * Return the host url.
     * 
     * @return The host url.
     */
    public synchronized String getHostURL() {
        if (this.host == null) {
            throw new IllegalStateException("Host must be set to create a host URL");   
        } else {
            return this.host.toURI();
        }
    }

    /**
     * Returns the host.
     * 
     * @return the host(IP or DNS name), or <code>null</code> if not set
     * 
     * @see #isHostSet()
     */
    public synchronized String getHost() {
        if (this.host != null) {
            return this.host.getHostName();
        } else {
            return null;
        }
    }

    /**
     * Returns the virtual host.
     * 
     * @return the virtual host name, or <code>null</code> if not set
     * 
     * @deprecated use HostParams
     */
    public synchronized String getVirtualHost() {
        return this.params.getVirtualHost();
    }

    /**
     * Returns the port.
     * 
     * @return the host port, or <code>-1</code> if not set
     * 
     * @see #isHostSet()
     */
    public synchronized int getPort() {
        if (this.host != null) {
            return this.host.getPort();
        } else {
            return -1;
        }
    }

    /**
     * Returns the protocol.
     * @return The protocol.
     */
    public synchronized Protocol getProtocol() {
        if (this.host != null) {
            return this.host.getProtocol();
        } else {
            return null;
        }
    }

    /**
     * Tests if the proxy host/port have been set.
     * 
     * @return <code>true</code> if a proxy server has been set.
     * 
     * @see #setProxy(String, int)
     * 
     * @deprecated no longer used
     */    
    public synchronized boolean isProxySet() {
        return this.proxyHost != null;   
    }

    /**
     * Sets the given proxy host
     * 
     * @param proxyHost the proxy host
     */
    public synchronized void setProxyHost(final ProxyHost proxyHost) {
        this.proxyHost = proxyHost;
    }
    
    /**
     * Set the proxy settings.
     * @param proxyHost The proxy host
     * @param proxyPort The proxy port
     */
    public synchronized void setProxy(final String proxyHost, int proxyPort) {
        this.proxyHost = new ProxyHost(proxyHost, proxyPort); 
    }

    /**
     * Returns the proxyHost.
     * 
     * @return the proxy host, or <code>null</code> if not set
     * 
     * @see #isProxySet()
     */
    public synchronized String getProxyHost() {
        if (this.proxyHost != null) {
            return this.proxyHost.getHostName();
        } else {
            return null;
        }
    }

    /**
     * Returns the proxyPort.
     * 
     * @return the proxy port, or <code>-1</code> if not set
     * 
     * @see #isProxySet()
     */
    public synchronized int getProxyPort() {
        if (this.proxyHost != null) {
            return this.proxyHost.getPort();
        } else {
            return -1;
        }
    }

    /**
     * Set the local address to be used when creating connections.
     * If this is unset, the default address will be used.
     * This is useful for specifying the interface to use on multi-homed or clustered systems.
     * 
     * @param localAddress the local address to use
     */
    
    public synchronized void setLocalAddress(InetAddress localAddress) {
        this.localAddress = localAddress;
    }

    /**
     * Return the local address to be used when creating connections.
     * If this is unset, the default address should be used.
     * 
     * @return the local address to be used when creating Sockets, or <code>null</code>
     */
    
    public synchronized InetAddress getLocalAddress() {
        return this.localAddress;
    }
    
    /**
     * Returns {@link HostParams HTTP protocol parameters} associated with this host.
     *
     * @return HTTP parameters.
     *
     * @since 3.0
     */
    public HostParams getParams() {
        return this.params;
    }

    /**
     * Assigns {@link HostParams HTTP protocol parameters} specific to this host.
     * 
     * @since 3.0
     * 
     * @see HostParams
     */
    public void setParams(final HostParams params) {
        if (params == null) {
            throw new IllegalArgumentException("Parameters may not be null");
        }
        this.params = params;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public synchronized boolean equals(final Object o) {
        if (o instanceof HostConfiguration) {
            // shortcut if we're comparing with ourselves
            if (o == this) { 
                return true;
            }
            HostConfiguration that = (HostConfiguration) o;
            return LangUtils.equals(this.host, that.host)
                && LangUtils.equals(this.proxyHost, that.proxyHost)
                && LangUtils.equals(this.localAddress, that.localAddress);
        } else {
            return false;
        }
        
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public synchronized int hashCode() {
        int hash = LangUtils.HASH_SEED;
        hash = LangUtils.hashCode(hash, this.host);
        hash = LangUtils.hashCode(hash, this.proxyHost);
        hash = LangUtils.hashCode(hash, this.localAddress);
        return hash;
    }

}
