package org.apache.commons.httpclient.contrib.ssl;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.protocol.Protocol;

/**
 * A kind of HostConfiguration that gets its Host from a factory. This is useful
 * for integrating a specialized Protocol or SocketFactory; for example, a
 * SecureSocketFactory that authenticates via SSL. Use
 * HttpClient.setHostConfiguration to install a HostConfigurationWithHostFactory
 * that contains the specialized HostFactory, Protocol or SocketFactory.
 * <p>
 * An alternative is to use Protocol.registerProtocol to register a specialized
 * Protocol. But that has drawbacks: it makes it hard to integrate modules (e.g.
 * web applications in a servlet container) with different strategies, because
 * they share the specialized Protocol (Protocol.PROTOCOLS is static). And it
 * can't support different Protocols for different hosts or ports (since the
 * host and port aren't parameters to Protocol.getProtocol).
 * 
 * @author John Kristian
 */
class HostConfigurationWithHostFactory extends HostConfiguration
{
    public HostConfigurationWithHostFactory(HttpHostFactory factory)
    {
        this.factory = factory;
    }

    private HostConfigurationWithHostFactory(HostConfigurationWithHostFactory that)
    {
        super(that);
        this.factory = that.factory;
    }

    private final HttpHostFactory factory;

    public Object clone()
    {
        return new HostConfigurationWithHostFactory(this);
    }

    private static final String DEFAULT_SCHEME = new String(HttpURL.DEFAULT_SCHEME);

    public void setHost(String host)
    {
        setHost(host, Protocol.getProtocol(DEFAULT_SCHEME).getDefaultPort());
    }

    public void setHost(final String host, int port)
    {
        setHost(host, port, DEFAULT_SCHEME);
    }

    public synchronized void setHost(String host, int port, String scheme)
    {
        setHost(factory.getHost(this, scheme, host, port));
    }

}
