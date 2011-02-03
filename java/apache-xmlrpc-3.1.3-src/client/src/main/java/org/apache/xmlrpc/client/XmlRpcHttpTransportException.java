package org.apache.xmlrpc.client;

import org.apache.xmlrpc.XmlRpcException;

/**
 * Exception thrown if the HTTP status code sent by the server
 * indicates that the request could not be processed. In
 * general, the 400 and 500 level HTTP status codes will
 * result in an XmlRpcHttpTransportException being thrown.
 */
public class XmlRpcHttpTransportException extends XmlRpcException {
    private static final long serialVersionUID = -6933992871198450027L;

    private final int status;
    private final String statusMessage;
    
    /**
     * Creates a new instance with the specified HTTP status code
     * and HTTP status message.
     * @param pCode The HTTP status code
     * @param pMessage The HTTP status message returned by the HTTP server
     */
    public XmlRpcHttpTransportException(int pCode, String pMessage) {
        this(pCode, pMessage, "HTTP server returned unexpected status: " + pMessage);
    }
    
    /**
     * Construct a new XmlRpcHttpTransportException with the specified HTTP status code, 
     * HTTP status message, and exception message.
     * @param httpStatusCode the HTTP status code
     * @param httpStatusMessage the HTTP status message returned by the HTTP server
     * @param message the exception message.
     */
    public XmlRpcHttpTransportException(int httpStatusCode, String httpStatusMessage, String message) {
        super( message );
        this.status = httpStatusCode;
        this.statusMessage = httpStatusMessage;
    }
    
    /**
     * Get the HTTP status code that resulted in this exception.
     * @return the HTTP status code that resulted in this exception.
     */
    public int getStatusCode()
    {
        return status;
    }
    
    /**
     * Get the status message returned by the HTTP server.
     * @return the status message returned by the HTTP server.
     */
    public String getStatusMessage()
    {
        return statusMessage;
    }
}
