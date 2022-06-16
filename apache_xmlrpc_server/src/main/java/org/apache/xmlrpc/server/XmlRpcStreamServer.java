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
package org.apache.xmlrpc.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.XmlRpcRequestConfig;
import org.apache.xmlrpc.common.ServerStreamConnection;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.common.XmlRpcStreamRequestProcessor;
import org.apache.xmlrpc.parser.XmlRpcRequestParser;
import org.apache.xmlrpc.serializer.DefaultXMLWriterFactory;
import org.apache.xmlrpc.serializer.XmlRpcWriter;
import org.apache.xmlrpc.serializer.XmlWriterFactory;
import org.apache.xmlrpc.util.SAXParsers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


/** Extension of {@link XmlRpcServer} with support for reading
 * requests from a stream and writing the response to another
 * stream.
 */
public abstract class XmlRpcStreamServer extends XmlRpcServer
		implements XmlRpcStreamRequestProcessor {
	private static final Log log = LogFactory.getLog(XmlRpcStreamServer.class);
	private XmlWriterFactory writerFactory = new DefaultXMLWriterFactory();
	private static final Logger logger = LoggerFactory.getLogger(XmlRpcStreamServer.class);;

	
	protected XmlRpcRequest getRequest(final XmlRpcStreamRequestConfig pConfig,
									   InputStream pStream) throws XmlRpcException {
		final XmlRpcRequestParser parser = new XmlRpcRequestParser(pConfig, getTypeFactory());
		final XMLReader xr = SAXParsers.newXMLReader();
		xr.setContentHandler(parser);
		try {
			xr.parse(new InputSource(pStream));
		} catch (SAXException e) {
			Exception ex = e.getException();
			if (ex != null  &&  ex instanceof XmlRpcException) {
				throw (XmlRpcException) ex;
			}
			throw new XmlRpcException("Failed to parse XML-RPC request: " + e.getMessage(), e);
		} catch (IOException e) {
			throw new XmlRpcException("Failed to read XML-RPC request: " + e.getMessage(), e);
		}
		final List params = parser.getParams();
		return new XmlRpcRequest(){
			public XmlRpcRequestConfig getConfig() { return pConfig; }
			public String getMethodName() { return parser.getMethodName(); }
			public int getParameterCount() { return params == null ? 0 : params.size(); }
			public Object getParameter(int pIndex) { return params.get(pIndex); }
		};
	}

	protected XmlRpcWriter getXmlRpcWriter(XmlRpcStreamRequestConfig pConfig,
										   OutputStream pStream)
			throws XmlRpcException {
		ContentHandler w = getXMLWriterFactory().getXmlWriter(pConfig, pStream);
		return new XmlRpcWriter(pConfig, w, getTypeFactory());
	}

	protected void writeResponse(XmlRpcStreamRequestConfig pConfig, OutputStream pStream,
								 Object pResult) throws XmlRpcException {
		try {
			getXmlRpcWriter(pConfig, pStream).write(pConfig, pResult);
		} catch (SAXException e) {
			throw new XmlRpcException("Failed to write XML-RPC response: " + e.getMessage(), e);
		}
	}

	/**
     * This method allows to convert the error into another error. For example, this
     * may be an error, which could be deserialized by the client.
	 */
    protected Throwable convertThrowable(Throwable pError) {
        return pError;
    }

    protected void writeError(XmlRpcStreamRequestConfig pConfig, OutputStream pStream,
							  Throwable pError)
			throws XmlRpcException {
        final Throwable error = convertThrowable(pError);
        final int code;
		final String message;
		if (error instanceof XmlRpcException) {
			XmlRpcException ex = (XmlRpcException) error;
			code = ex.code;
		} else {
			code = 0;
		}
		message = error.getMessage();
		try {
			getXmlRpcWriter(pConfig, pStream).write(pConfig, code, message, error);
		} catch (SAXException e) {
			throw new XmlRpcException("Failed to write XML-RPC response: " + e.getMessage(), e);
		}
	}

	/** Sets the XML Writer factory.
	 * @param pFactory The XML Writer factory.
	 */
	public void setXMLWriterFactory(XmlWriterFactory pFactory) {
		writerFactory = pFactory;
	}

	/** Returns the XML Writer factory.
	 * @return The XML Writer factory.
	 */
	public XmlWriterFactory getXMLWriterFactory() {
		return writerFactory;
	}

	protected InputStream getInputStream(XmlRpcStreamRequestConfig pConfig,
										 ServerStreamConnection pConnection) throws IOException {
		InputStream istream = pConnection.newInputStream();
		if (pConfig.isEnabledForExtensions()  &&  pConfig.isGzipCompressing()) {
			istream = new GZIPInputStream(istream);
		}
		return istream;
	}

	/** Called to prepare the output stream. Typically used for enabling
	 * compression, or similar filters.
	 * @param pConnection The connection object.
	 */
	protected OutputStream getOutputStream(ServerStreamConnection pConnection,
										   XmlRpcStreamRequestConfig pConfig, OutputStream pStream) throws IOException {
		if (pConfig.isEnabledForExtensions()  &&  pConfig.isGzipRequesting()) {
			return new GZIPOutputStream(pStream);
		} else {
			return pStream;
		}
	}

	/** Called to prepare the output stream, if content length is
	 * required.
	 * @param pConfig The configuration object.
	 * @param pSize The requests size.
	 */
	protected OutputStream getOutputStream(XmlRpcStreamRequestConfig pConfig,
										   ServerStreamConnection pConnection,
										   int pSize) throws IOException {
	    return pConnection.newOutputStream();
	}

	/** Returns, whether the requests content length is required.
	 * @param pConfig The configuration object.
	 */
	protected boolean isContentLengthRequired(XmlRpcStreamRequestConfig pConfig) {
		return false;
	}

	/** Returns, whether the 
	/** Processes a "connection". The "connection" is an opaque object, which is
	 * being handled by the subclasses.
	 * @param pConfig The request configuration.
	 * @param pConnection The "connection" being processed.
	 * @throws XmlRpcException Processing the request failed.
	 */
	public void execute(XmlRpcStreamRequestConfig pConfig,
						ServerStreamConnection pConnection)
			throws XmlRpcException {
		log.debug("execute: ->");
		try {
			Object result;
			Throwable error;
			InputStream istream = null;
			try {
				istream = getInputStream(pConfig, pConnection);
				XmlRpcRequest request = getRequest(pConfig, istream);
				if (request.getMethodName().equals("system.multicall")) {
					result = executeMulticall(request);
				} else {
					result = execute(request);
				}
				istream.close();
				istream = null;
				error = null;
				log.debug("execute: Request performed successfully");
			} catch (Throwable t) {
				logError(t);
				result = null;
				error = t;
			} finally {
				if (istream != null) { try { istream.close(); } catch (Throwable ignore) {} }
			}
			boolean contentLengthRequired = isContentLengthRequired(pConfig);
			ByteArrayOutputStream baos;
			OutputStream ostream;
			if (contentLengthRequired) {
				baos = new ByteArrayOutputStream();
				ostream = baos;
			} else {
				baos = null;
				ostream = pConnection.newOutputStream();
			}
			ostream = getOutputStream(pConnection, pConfig, ostream);
			try {
				if (error == null) {
					writeResponse(pConfig, ostream, result);
				} else {
					writeError(pConfig, ostream, error);
				}
				ostream.close();
				ostream = null;
			} finally {
				if (ostream != null) { try { ostream.close(); } catch (Throwable ignore) {} }
			}
			if (baos != null) {
				OutputStream dest = getOutputStream(pConfig, pConnection, baos.size());
				try {
					baos.writeTo(dest);
					dest.close();
					dest = null;
				} finally {
					if (dest != null) { try { dest.close(); } catch (Throwable ignore) {} }
				}
			}
            pConnection.close();
			pConnection = null;
		} catch (IOException e) {
			throw new XmlRpcException("I/O error while processing request: "
					+ e.getMessage(), e);
		} finally {
			if (pConnection != null) { try { pConnection.close(); } catch (Throwable ignore) {} }
		}
		log.debug("execute: <-");
	}
	
	private Object[] executeMulticall(final XmlRpcRequest pRequest) {
		if (pRequest.getParameterCount() != 1)
			return null;

		Object[] reqs = (Object[]) pRequest.getParameter(0); // call requests
		ArrayList<Object> results = new ArrayList<Object>(); // call results
		final XmlRpcRequestConfig pConfig = pRequest.getConfig();
		// TODO: make concurrent calls?
		for (int i = 0; i < reqs.length; i++) {
			Object result = null;
			try {
				@SuppressWarnings("unchecked")
				HashMap<String, Object> req = (HashMap<String, Object>) reqs[i];
				final String methodName = (String) req.get("methodName");
				final Object[] params = (Object[]) req.get("params");
				result = execute(new XmlRpcRequest() {
					@Override
					public XmlRpcRequestConfig getConfig() {
						return pConfig;
					}

					@Override
					public String getMethodName() {
						return methodName;
					}

					@Override
					public int getParameterCount() {
						return params == null ? 0 : params.length;
					}

					@Override
					public Object getParameter(int pIndex) {
						return params[pIndex];
					}
				});
			} catch (Throwable t) {
				logError(t);
				// TODO: should this return an XmlRpc fault?
				result = null;
			}
			results.add(result);
		}
		Object[] retobj = new Object[] { results };
		return retobj;
	}

    protected void logError(Throwable t) {
        final String msg = t.getMessage() == null ? t.getClass().getName() : t.getMessage();
		logger.debug(msg, t);
    }


}
