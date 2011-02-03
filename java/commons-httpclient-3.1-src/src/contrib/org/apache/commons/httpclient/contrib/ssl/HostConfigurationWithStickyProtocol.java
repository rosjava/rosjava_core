package org.apache.commons.httpclient.contrib.ssl;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpHost;
import org.apache.commons.httpclient.protocol.Protocol;

/**
 * A kind of HostConfiguration that can retain its Protocol when its host name
 * or port changes. HttpClient may clone its HostConfigurationWithStickyProtocol
 * and change the host URL, without changing the specialized Protocol.
 * <p>
 * This is useful for integrating a specialized Protocol or SocketFactory; for
 * example, a SecureSocketFactory that authenticates via SSL. Use
 * HttpClient.setHostConfiguration to install a
 * HostConfigurationWithStickyProtocol that contains the specialized Protocol or
 * SocketFactory.
 * <p>
 * An alternative is to use Protocol.registerProtocol to register a specialized
 * Protocol. But that has drawbacks: it makes it hard to integrate modules (e.g.
 * web applications in a servlet container) with different strategies, because
 * they share the specialized Protocol (Protocol.PROTOCOLS is static). Also, it
 * can't handle multiple socket factories for the same host and port, since the
 * URL path isn't a parameter to Protocol.getProtocol or socket factory methods.
 * 
 * @author John Kristian
 */
public class HostConfigurationWithStickyProtocol extends HostConfiguration
{
    public HostConfigurationWithStickyProtocol()
    {
    }

    public HostConfigurationWithStickyProtocol(HostConfiguration hostConfiguration)
    {
        super(hostConfiguration);
    }

    public Object clone()
    {
        return new HostConfigurationWithStickyProtocol(this);
    }

    public synchronized void setHost(String host, int port, String scheme)
    {
        setHost(new HttpHost(host, port, getNewProtocol(host, port, scheme)));
    }

    /**
     * Select a Protocol to be used for the given host, port and scheme. The
     * current Protocol may be selected, if appropriate. This method need not be
     * thread-safe; the caller must synchronize if necessary.
     * <p>
     * This implementation returns the current Protocol if it has the given
     * scheme; otherwise it returns the Protocol registered for that scheme.
     */
    protected Protocol getNewProtocol(String host, int port, String scheme)
    {
        final Protocol oldProtocol = getProtocol();
        if (oldProtocol != null) {
            final String oldScheme = oldProtocol.getScheme();
            if (oldScheme == scheme || (oldScheme != null && oldScheme.equalsIgnoreCase(scheme))) {
                // The old {rotocol has the desired scheme.
                return oldProtocol; // Retain it.
            }
        }
        return Protocol.getProtocol(scheme);
    }

}
