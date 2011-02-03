/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/java/org/apache/commons/httpclient/params/HttpClientParams.java,v 1.7 2004/05/13 04:01:22 mbecke Exp $
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

package org.apache.commons.httpclient.params;

/**
 * This class represents a collection of HTTP protocol parameters applicable to 
 * {@link org.apache.commons.httpclient.HttpClient instances of HttpClient}. 
 * Protocol parameters may be linked together to form a hierarchy. If a particular 
 * parameter value has not been explicitly defined in the collection itself, its 
 * value will be drawn from the parent collection of parameters.
 * 
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * 
 * @version $Revision: 480424 $
 * 
 * @since 3.0
 */
public class HttpClientParams extends HttpMethodParams {

    /**
     * Sets the timeout in milliseconds used when retrieving an 
     * {@link org.apache.commons.httpclient.HttpConnection HTTP connection} from the
     * {@link org.apache.commons.httpclient.HttpConnectionManager HTTP connection manager}.
     * <p>
     * This parameter expects a value of type {@link Long}.
     * </p>
     */ 
    public static final String CONNECTION_MANAGER_TIMEOUT = "http.connection-manager.timeout"; 

    /**
     * Defines the default 
     * {@link org.apache.commons.httpclient.HttpConnectionManager HTTP connection manager}
     * class.
     * <p>
     * This parameter expects a value of type {@link Class}.
     * </p>
     */ 
    public static final String CONNECTION_MANAGER_CLASS = "http.connection-manager.class"; 

    /**
     * Defines whether authentication should be attempted preemptively.
     * <p>
     * This parameter expects a value of type {@link Boolean}.
     * </p>
     */
    public static final String PREEMPTIVE_AUTHENTICATION = "http.authentication.preemptive";

    /**
     * Defines whether relative redirects should be rejected.
     * <p>
     * This parameter expects a value of type {@link Boolean}.
     * </p>
     */
    public static final String REJECT_RELATIVE_REDIRECT = "http.protocol.reject-relative-redirect"; 

    /** 
     * Defines the maximum number of redirects to be followed. 
     * The limit on number of redirects is intended to prevent infinite loops. 
     * <p>
     * This parameter expects a value of type {@link Integer}.
     * </p>
     */
    public static final String MAX_REDIRECTS = "http.protocol.max-redirects";

    /** 
     * Defines whether circular redirects (redirects to the same location) should be allowed. 
     * The HTTP spec is not sufficiently clear whether circular redirects are permitted, 
     * therefore optionally they can be enabled
     * <p>
     * This parameter expects a value of type {@link Boolean}.
     * </p>
     */
    public static final String ALLOW_CIRCULAR_REDIRECTS = "http.protocol.allow-circular-redirects";

    /**
     * Creates a new collection of parameters with the collection returned
     * by {@link #getDefaultParams()} as a parent. The collection will defer
     * to its parent for a default value if a particular parameter is not 
     * explicitly set in the collection itself.
     * 
     * @see #getDefaultParams()
     */
    public HttpClientParams() {
        super();
    }

    /**
     * Creates a new collection of parameters with the given parent. 
     * The collection will defer to its parent for a default value 
     * if a particular parameter is not explicitly set in the collection
     * itself.
     * 
     * @param defaults the parent collection to defer to, if a parameter
     * is not explictly set in the collection itself.
     *
     * @see #getDefaultParams()
     */
    public HttpClientParams(HttpParams defaults) {
        super(defaults);
    }

    /**
     * Returns the timeout in milliseconds used when retrieving an 
     * {@link org.apache.commons.httpclient.HttpConnection HTTP connection} from the
     * {@link org.apache.commons.httpclient.HttpConnectionManager HTTP connection manager}.
     * 
     * @return timeout in milliseconds.
     */ 
    public long getConnectionManagerTimeout() {
        return getLongParameter(CONNECTION_MANAGER_TIMEOUT, 0);
    }

    /**
     * Sets the timeout in milliseconds used when retrieving an 
     * {@link org.apache.commons.httpclient.HttpConnection HTTP connection} from the
     * {@link org.apache.commons.httpclient.HttpConnectionManager HTTP connection manager}.
     * 
     * @param timeout the timeout in milliseconds
     */ 
    public void setConnectionManagerTimeout(long timeout) {
        setLongParameter(CONNECTION_MANAGER_TIMEOUT, timeout);
    }

    /**
     * Returns the default 
     * {@link org.apache.commons.httpclient.HttpConnectionManager HTTP connection manager}
     * class.
     * @return {@link org.apache.commons.httpclient.HttpConnectionManager HTTP connection manager}
     * factory class.
     */ 
    public Class getConnectionManagerClass() {
        return (Class) getParameter(CONNECTION_MANAGER_CLASS);
    }

    /**
     * Sets {@link org.apache.commons.httpclient.HttpConnectionManager HTTP connection manager}
     * class to be used der default.
     * @param clazz 
     *  {@link org.apache.commons.httpclient.HttpConnectionManager HTTP connection manager}
     *  factory class.
     */ 
    public void setConnectionManagerClass(Class clazz) {
        setParameter(CONNECTION_MANAGER_CLASS, clazz);
    }
    
    /**
     * Returns <tt>true</tt> if authentication should be attempted preemptively, 
     * <tt>false</tt> otherwise.
     * 
     * @return <tt>true</tt> if authentication should be attempted preemptively,
     *   <tt>false</tt> otherwise.
     */
    public boolean isAuthenticationPreemptive() {
        return getBooleanParameter(PREEMPTIVE_AUTHENTICATION, false); 
    }

    /**
     * Sets whether authentication should be attempted preemptively.
     * 
     * @param value <tt>true</tt> if authentication should be attempted preemptively,
     *   <tt>false</tt> otherwise.
     */
    public void setAuthenticationPreemptive(boolean value) {
        setBooleanParameter(PREEMPTIVE_AUTHENTICATION, value); 
    }

    private static final String[] PROTOCOL_STRICTNESS_PARAMETERS = {
        REJECT_RELATIVE_REDIRECT,
        ALLOW_CIRCULAR_REDIRECTS
    };


    public void makeStrict() {
        super.makeStrict();
        setParameters(PROTOCOL_STRICTNESS_PARAMETERS, Boolean.TRUE);
    }


    public void makeLenient() {
        super.makeLenient();
        setParameters(PROTOCOL_STRICTNESS_PARAMETERS, Boolean.FALSE);
    }
}
