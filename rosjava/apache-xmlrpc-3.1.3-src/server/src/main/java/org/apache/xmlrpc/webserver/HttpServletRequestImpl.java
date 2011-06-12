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
package org.apache.xmlrpc.webserver;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.Socket;
import java.net.URLDecoder;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.xmlrpc.common.XmlRpcStreamConfig;
import org.apache.xmlrpc.util.HttpUtil;


/** Stub implementation of a {@link javax.servlet.http.HttpServletRequest}
 * with lots of unimplemented methods. I implemented only those, which
 * are required for testing the {@link org.apache.xmlrpc.webserver.XmlRpcServlet}.
 * Perhaps someone else is adding more at a later time?
 */
public class HttpServletRequestImpl implements HttpServletRequest {
	private final Socket socket;
	private final ServletInputStream istream;
	private ServletInputStream sistream;
	private BufferedReader reader;
	private boolean postParametersParsed;
	private String method;
	private String protocol;
	private String uri;
	private String queryString;
	private String httpVersion;
	private final Map headers = new HashMap();
	private final Map attributes = new HashMap();
	private Map parameters;
	private String characterEncoding;
	private int contentBytesRemaining = -1;

	/** Creates a new instance, which reads input from the given
	 * socket.
	 * @param pSocket The socket, to which the client is connected.
	 * @throws IOException Accessing the sockets input stream failed.
	 */
	public HttpServletRequestImpl(Socket pSocket) throws IOException {
		socket = pSocket;
		final InputStream bis = new BufferedInputStream(socket.getInputStream()){
    		/** It may happen, that the XML parser invokes close().
    		 * Closing the input stream must not occur, because
    		 * that would close the whole socket. So we suppress it.
    		 */
        	public void close() throws IOException {
        	}
        };
		istream = new ServletInputStream(){
			public int read() throws IOException {
				if (contentBytesRemaining == 0) {
					return -1;
				}
				int c = bis.read();
				if (c != -1  &&  contentBytesRemaining > 0) {
					--contentBytesRemaining;
				}
				return c;
			}
		};
	}

   /**
    * Read the header lines, one by one. Note, that the size of
         * the buffer is a limitation of the maximum header length!
         */
    public void readHttpHeaders()
      throws IOException, ServletWebServer.Exception {
        byte[] buffer = new byte[2048];
        String line = readLine(buffer);
        StringTokenizer tokens =
          line != null ? new StringTokenizer(line) : null;
        if (tokens == null || !tokens.hasMoreTokens()) {
            throw new ServletWebServer.Exception(400, "Bad Request", "Unable to parse requests first line (should" +
              " be 'METHOD uri HTTP/version', was empty.");
        }
        method = tokens.nextToken();
		if (!"POST".equalsIgnoreCase(method)) {
            throw new ServletWebServer.Exception(400, "Bad Request", "Expected 'POST' method, got " +
              method);
		}
		if (!tokens.hasMoreTokens()) {
            throw new ServletWebServer.Exception(400, "Bad Request", "Unable to parse requests first line (should" +
              " be 'METHOD uri HTTP/version', was: " + line);
		}
		String u = tokens.nextToken();
		int offset = u.indexOf('?');
		if (offset >= 0) {
			uri = u.substring(0, offset);
            queryString = u.substring(offset + 1);
		} else {
			uri = u;
			queryString = null;
		}
		if (tokens.hasMoreTokens()) {
			String v = tokens.nextToken().toUpperCase();
			if (tokens.hasMoreTokens()) {
                throw new ServletWebServer.Exception(400, "Bad Request", "Unable to parse requests first line (should" +
                  " be 'METHOD uri HTTP/version', was: " + line);
			} else {
				int index = v.indexOf('/');
				if (index == -1) {
                    throw new ServletWebServer.Exception(400, "Bad Request", "Unable to parse requests first line (should" +
                      " be 'METHOD uri HTTP/version', was: " + line);
				}
				protocol = v.substring(0, index).toUpperCase();
                httpVersion = v.substring(index + 1);
			}
		} else {
			httpVersion = "1.0";
			protocol = "HTTP";
		}
		for (;;) {
			line = HttpUtil.readLine(istream, buffer);
            if (line == null || line.length() == 0) {
				break;
			}
			int off = line.indexOf(':');
			if (off > 0) {
                addHeader(line.substring(0, off), line.substring(off + 1).trim());
			} else {
                throw new ServletWebServer.Exception(400, "Bad Request", "Unable to parse header line: " +
                  line);
			}
		}
		contentBytesRemaining = getIntHeader("content-length");
	}

	protected String readLine(byte[] pBuffer) throws IOException {
		int res = istream.readLine(pBuffer, 0, pBuffer.length);
		if (res == -1) {
			return null;
		}
		if (res == pBuffer.length  &&  pBuffer[pBuffer.length] != '\n') {
			throw new ServletWebServer.Exception(400, "Bad Request",
												 "Maximum header size of " + pBuffer.length +
												 " characters exceeded.");
		}
		return new String(pBuffer, 0, res, "US-ASCII");
	}

	protected void addHeader(String pHeader, String pValue) {
		String key = pHeader.toLowerCase();
		addParameter(headers, key, pValue);
	}

	public String getAuthType() {
		String s = getHeader("Authorization");
		if (s == null) {
			return null;
		}
		StringTokenizer st = new StringTokenizer(s);
		if (st.hasMoreTokens()) {
			return st.nextToken().toUpperCase();
		} else {
			return null;
		}
	}

	public String getContextPath() { return ""; }

	public Cookie[] getCookies() { throw new IllegalStateException("Not implemented"); }
	public long getDateHeader(String arg0) { throw new IllegalStateException("Not implemented"); }

	public String getHeader(String pHeader) {
		String key = pHeader.toLowerCase();
		Object o = headers.get(key);
		if (o instanceof List) {
			o = ((List) o).get(0);
		}
		return (String) o;
	}

	public Enumeration getHeaderNames() {
		return Collections.enumeration(headers.keySet());
	}

	public Enumeration getHeaders(String pHeader) {
		String key = pHeader.toLowerCase();
		Object o = headers.get(key);
		List list;
		if (o instanceof List) {
			list = (List) o;
		} else {
			list = Collections.singletonList(o);
		}
		return Collections.enumeration(list);
	}

	public int getIntHeader(String pHeader) {
		String s = getHeader(pHeader);
		return s == null ? -1 : Integer.parseInt(s);
	}

	public String getMethod() { return method; }

	public String getPathInfo() { return null; }

	public String getPathTranslated() { return null; }

	public String getQueryString() { return queryString; }

	public String getRemoteUser() { throw new IllegalStateException("Not implemented"); }

	public String getRequestURI() { return uri; }

	public StringBuffer getRequestURL() {
		String scheme = getScheme().toLowerCase();
		StringBuffer sb = new StringBuffer(scheme);
		sb.append("://");
		String host = getHeader("host");
		if (host == null) {
			host = getLocalName();
			if (host == null) {
				host = getLocalAddr();
			}
		}
		int port = getLocalPort();
		int offset = host.indexOf(':');
		if (offset != -1) {
			host = host.substring(0, offset);
			try {
				port = Integer.parseInt(host.substring(offset+1));
			} catch (Exception e) {
			}
		}
		boolean isDefaultPort;
		if ("http".equalsIgnoreCase(scheme)) {
			isDefaultPort = port == 80;
		} else if ("https".equalsIgnoreCase(scheme)) {
			isDefaultPort = port == 443;
		} else {
			isDefaultPort = false;
		}
		if (!isDefaultPort) {
			sb.append(':');
			sb.append(port);
		}
		sb.append(getRequestURI());
		return sb;
	}

	public String getRequestedSessionId() { throw new IllegalStateException("Not implemented"); }

	public String getServletPath() { return uri; }

	public HttpSession getSession() { throw new IllegalStateException("Not implemented"); }

	public HttpSession getSession(boolean pCreate) { throw new IllegalStateException("Not implemented"); }

	public Principal getUserPrincipal() { throw new IllegalStateException("Not implemented"); }

	public boolean isRequestedSessionIdFromCookie() { throw new IllegalStateException("Not implemented"); }

	public boolean isRequestedSessionIdFromURL() { throw new IllegalStateException("Not implemented"); }

	public boolean isRequestedSessionIdFromUrl() { throw new IllegalStateException("Not implemented"); }

	public boolean isRequestedSessionIdValid() { throw new IllegalStateException("Not implemented"); }

	public boolean isUserInRole(String pRole) { throw new IllegalStateException("Not implemented"); }

	public Object getAttribute(String pKey) { return attributes.get(pKey); }

	public Enumeration getAttributeNames() { return Collections.enumeration(attributes.keySet()); }

	public String getCharacterEncoding() {
		if (characterEncoding == null) {
			String contentType = getHeader("content-type");
			if (contentType != null) {
				for (StringTokenizer st = new StringTokenizer(contentType, ";");  st.hasMoreTokens();  ) {
					String s = st.nextToken().trim();
					if (s.toLowerCase().startsWith("charset=")) {
						return s.substring("charset=".length()).trim();
					}
				}
			}
			return null;
		} else {
			return characterEncoding;
		}
	}

	public void setCharacterEncoding(String pEncoding) { characterEncoding = pEncoding; }

	public int getContentLength() {
		try {
			return getIntHeader("content-length");
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	public String getContentType() { return getHeader("content-type"); }

	public ServletInputStream getInputStream() throws IOException {
		if (reader == null) {
			if (sistream == null) {
				if (postParametersParsed) {
					throw new IllegalStateException("The method getInputStream() must not be called, after POST parameters have been parsed.");
				}
				sistream = istream;
			}
			return sistream;
		} else {
			throw new IllegalStateException("The method getReader() has already been invoked.");
		}
	}

	public Locale getLocale() { throw new IllegalStateException("Not implemented"); }

	public Enumeration getLocales() { throw new IllegalStateException("Not implemented"); }

	private void addParameter(Map pParams, String pKey, String pValue) {
		Object o = pParams.get(pKey);
		if (o == null) {
			pParams.put(pKey, pValue);
		} else {
			List list;
			if (o instanceof String) {
				list = new ArrayList();
				list.add(o);
				pParams.put(pKey, list);
			} else {
				list = (List) o;
			}
			list.add(pParams);
		}
	}

	private void parseQueryString(Map pParams, String pQueryString, String pEncoding) throws UnsupportedEncodingException {
		for (StringTokenizer st = new StringTokenizer(pQueryString, "&");  st.hasMoreTokens();  ) {
			String s = st.nextToken();
			parseParameter(pParams, s, pEncoding);
		}
	}

	private void parseParameter(Map pParams, String pParam, String pEncoding) throws UnsupportedEncodingException {
		if (pParam.length() == 0) {
			return;
		}
		int offset = pParam.indexOf('=');
		final String name, value;
		if (offset == -1) {
			name = pParam;
			value = "";
		} else {
			name = pParam.substring(0, offset);
			value = pParam.substring(offset+1);
		}
		addParameter(pParams, URLDecoder.decode(name, pEncoding), URLDecoder.decode(value, pEncoding));
	}

	private void parsePostData(Map pParams, InputStream pStream, String pEncoding) throws IOException {
		Reader r = new InputStreamReader(pStream, "US-ASCII");
		StringBuffer sb = new StringBuffer();
		for (;;) {
			int c = r.read();
			if (c == -1  ||  c == '&') {
				parseParameter(pParams, sb.toString(), pEncoding);
				if (c == -1) {
					break;
				} else {
					sb.setLength(0);
				}
			} else {
				sb.append((char) c);
			}
		}
	}

	protected void parseParameters() {
		if (parameters != null) {
			return;
		}
		String encoding = getCharacterEncoding();
		if (encoding == null) {
			encoding = XmlRpcStreamConfig.UTF8_ENCODING;
		}
		Map params = new HashMap();
		String s = getQueryString();
		if (s != null) {
			try {
				parseQueryString(params, s, encoding);
			} catch (IOException e) {
				throw new UndeclaredThrowableException(e);
			}
		}
		if ("POST".equals(getMethod())  &&
			"application/x-www-form-urlencoded".equals(getContentType())) {
			if (sistream != null  ||  reader != null) {
				throw new IllegalStateException("POST parameters cannot be parsed, after"
												+ " getInputStream(), or getReader(),"
												+ " have been called.");
			}
			postParametersParsed = true;
			try {
				parsePostData(params, istream, encoding);
			} catch (IOException e) {
				throw new UndeclaredThrowableException(e);
			}
		}
		parameters = params;
	}

	public String getParameter(String pName) {
		parseParameters();
		Object o = parameters.get(pName);
		if (o instanceof List) {
			o = ((List) o).get(0);
		}
		return (String) o;
	}

	public Map getParameterMap() {
		parseParameters();
		final Map result = new HashMap();
		for (final Iterator iter = parameters.entrySet().iterator();  iter.hasNext();  ) {
			final Map.Entry entry = (Map.Entry) iter.next();
			final String name = (String) entry.getKey();
			final Object o = entry.getValue();
			final String[] array;
			if (o instanceof String) {
				array = new String[]{(String) o};
			} else if (o instanceof List) {
				final List list = (List) o;
				array = (String[]) list.toArray(new String[list.size()]);
			} else {
				throw new IllegalStateException("Invalid object: " + o.getClass().getName());
			}
			result.put(name, array);
		}
		return Collections.unmodifiableMap(result);
	}

	public Enumeration getParameterNames() {
		parseParameters();
		return Collections.enumeration(parameters.keySet());
	}

	public String[] getParameterValues(String pName) {
		parseParameters();
		Object o = parameters.get(pName);
		if (o instanceof String) {
			return new String[]{(String) o};
		} else {
			List list = (List) o;
			return (String[]) list.toArray(new String[list.size()]);
		}
	}

	public String getProtocol() { return protocol; }

	public BufferedReader getReader() throws IOException {
		if (sistream == null) {
			if (reader == null) {
				if (postParametersParsed) {
					throw new IllegalStateException("The method getReader() must not be called, after POST parameters have been parsed.");
				}
				String encoding = getCharacterEncoding();
				if (encoding == null) {
					encoding = "UTF8";
				}
				reader = new BufferedReader(new InputStreamReader(istream, encoding));
			}
			return reader;
		} else {
			throw new IllegalStateException("The methods getInputStream(), and getReader(), are mutually exclusive.");
		}
	}

	public String getRealPath(String pPath) { throw new IllegalStateException("Not implemented."); }

	public String getLocalAddr() { return socket.getLocalAddress().getHostAddress(); }

	public String getLocalName() { return socket.getLocalAddress().getHostName(); }

	public int getLocalPort() { return socket.getLocalPort(); }

	public String getRemoteAddr() { return socket.getInetAddress().getHostAddress(); }

	public String getRemoteHost() { return socket.getInetAddress().getHostName(); }

	public int getRemotePort() { return socket.getPort(); }

	public RequestDispatcher getRequestDispatcher(String pUri) {
		throw new IllegalStateException("Not implemented");
	}

	public String getScheme() { return "http"; }

	public String getServerName() { return socket.getLocalAddress().getHostName(); }

	public int getServerPort() { return socket.getLocalPort(); }

	public boolean isSecure() { return false; }

	public void removeAttribute(String pKey) {
		attributes.remove(pKey);
	}

	public void setAttribute(String pKey, Object pValue) {
		attributes.put(pKey, pValue);
	}

	protected String getHttpVersion() { return httpVersion; }
}
