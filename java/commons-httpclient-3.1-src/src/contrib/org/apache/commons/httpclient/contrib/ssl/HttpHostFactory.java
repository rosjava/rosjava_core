package org.apache.commons.httpclient.contrib.ssl;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpHost;
import org.apache.commons.httpclient.HttpsURL;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

/** A source of HttpHosts. */
public class HttpHostFactory
{
    /** The default factory. */
    public static final HttpHostFactory DEFAULT = new HttpHostFactory(null, // httpProtocol
            new Protocol(new String(HttpsURL.DEFAULT_SCHEME),
                    (ProtocolSocketFactory) new EasySSLProtocolSocketFactory(),
                    HttpsURL.DEFAULT_PORT));

    public HttpHostFactory(Protocol httpProtocol, Protocol httpsProtocol)
    {
        this.httpProtocol = httpProtocol;
        this.httpsProtocol = httpsProtocol;
    }

    protected final Protocol httpProtocol;

    protected final Protocol httpsProtocol;

    /** Get a host for the given parameters. This method need not be thread-safe. */
    public HttpHost getHost(HostConfiguration old, String scheme, String host, int port)
    {
        return new HttpHost(host, port, getProtocol(old, scheme, host, port));
    }

    /**
     * Get a Protocol for the given parameters. The default implementation
     * selects a protocol based only on the scheme. Subclasses can do fancier
     * things, such as select SSL parameters based on the host or port. This
     * method must not return null.
     */
    protected Protocol getProtocol(HostConfiguration old, String scheme, String host, int port)
    {
        final Protocol oldProtocol = old.getProtocol();
        if (oldProtocol != null) {
            final String oldScheme = oldProtocol.getScheme();
            if (oldScheme == scheme || (oldScheme != null && oldScheme.equalsIgnoreCase(scheme))) {
                // The old protocol has the desired scheme.
                return oldProtocol; // Retain it.
            }
        }
        Protocol newProtocol = (scheme != null && scheme.toLowerCase().endsWith("s")) ? httpsProtocol
                : httpProtocol;
        if (newProtocol == null) {
            newProtocol = Protocol.getProtocol(scheme);
        }
        return newProtocol;
    }

}
