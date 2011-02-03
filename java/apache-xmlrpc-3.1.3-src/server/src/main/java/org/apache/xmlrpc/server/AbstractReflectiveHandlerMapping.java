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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcHandler;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.TypeConverterFactory;
import org.apache.xmlrpc.common.TypeConverterFactoryImpl;
import org.apache.xmlrpc.metadata.ReflectiveXmlRpcMetaDataHandler;
import org.apache.xmlrpc.metadata.Util;
import org.apache.xmlrpc.metadata.XmlRpcListableHandlerMapping;
import org.apache.xmlrpc.metadata.XmlRpcMetaDataHandler;
import org.apache.xmlrpc.server.RequestProcessorFactoryFactory.RequestProcessorFactory;


/** Abstract base class of handler mappings, which are
 * using reflection.
 */
public abstract class AbstractReflectiveHandlerMapping
		implements XmlRpcListableHandlerMapping {
	/** An object implementing this interface may be used
     * to validate user names and passwords.
     */
    public interface AuthenticationHandler {
        /** Returns, whether the user is authenticated and
         * authorized to perform the request.
         */
        boolean isAuthorized(XmlRpcRequest pRequest)
            throws XmlRpcException;
    }

    private TypeConverterFactory typeConverterFactory = new TypeConverterFactoryImpl();
    protected Map handlerMap = new HashMap();
    private AuthenticationHandler authenticationHandler;
    private RequestProcessorFactoryFactory requestProcessorFactoryFactory = new RequestProcessorFactoryFactory.RequestSpecificProcessorFactoryFactory();
    private boolean voidMethodEnabled;

    /**
     * Sets the mappings {@link TypeConverterFactory}.
     */
    public void setTypeConverterFactory(TypeConverterFactory pFactory) {
        typeConverterFactory = pFactory;
    }

    /**
     * Returns the mappings {@link TypeConverterFactory}.
     */
    public TypeConverterFactory getTypeConverterFactory() {
        return typeConverterFactory;
    }

    /** Sets the mappings {@link RequestProcessorFactoryFactory}. Note, that this doesn't
     * affect already registered handlers.
     */
    public void setRequestProcessorFactoryFactory(RequestProcessorFactoryFactory pFactory) {
        requestProcessorFactoryFactory = pFactory;
    }

    /** Returns the mappings {@link RequestProcessorFactoryFactory}.
     */
    public RequestProcessorFactoryFactory getRequestProcessorFactoryFactory() {
        return requestProcessorFactoryFactory;
    }

    /** Returns the authentication handler, if any, or null.
     */
    public AuthenticationHandler getAuthenticationHandler() {
        return authenticationHandler;
    }

    /** Sets the authentication handler, if any, or null.
     */
    public void setAuthenticationHandler(AuthenticationHandler pAuthenticationHandler) {
        authenticationHandler = pAuthenticationHandler;
    }

    protected boolean isHandlerMethod(Method pMethod) {
        if (!Modifier.isPublic(pMethod.getModifiers())) {
            return false;  // Ignore methods, which aren't public
        }
        if (Modifier.isStatic(pMethod.getModifiers())) {
            return false;  // Ignore methods, which are static
        }
        if (!isVoidMethodEnabled()  &&  pMethod.getReturnType() == void.class) {
            return false;  // Ignore void methods.
        }
        if (pMethod.getDeclaringClass() == Object.class) {
            return false;  // Ignore methods from Object.class
        }
        return true;
    }

    /** Searches for methods in the given class. For any valid
     * method, it creates an instance of {@link XmlRpcHandler}.
     * Valid methods are defined as follows:
     * <ul>
     *   <li>They must be public.</li>
     *   <li>They must not be static.</li>
     *   <li>The return type must not be void.</li>
     *   <li>The declaring class must not be
     *     {@link java.lang.Object}.</li>
     *   <li>If multiple methods with the same name exist,
     *     which meet the above conditins, then an attempt is
     *     made to identify a method with a matching signature.
     *     If such a method is found, then this method is
     *     invoked. If multiple such methods are found, then
     *     the first one is choosen. (This may be the case,
     *     for example, if there are methods with a similar
     *     signature, but varying subclasses.) Note, that
     *     there is no concept of the "most matching" method.
     *     If no matching method is found at all, then an
     *     exception is thrown.</li>
     * </ul>
     * @param pKey Suffix for building handler names. A dot and
     * the method name are being added.
     * @param pType The class being inspected.
     */
    protected void registerPublicMethods(String pKey,
    		Class pType) throws XmlRpcException {
    	Map map = new HashMap();
        Method[] methods = pType.getMethods();
        for (int i = 0;  i < methods.length;  i++) {
            final Method method = methods[i];
            if (!isHandlerMethod(method)) {
                continue;
            }
            String name = (pKey.length() > 0 ? pKey + "." : "") + method.getName();
            Method[] mArray;
            Method[] oldMArray = (Method[]) map.get(name);
            if (oldMArray == null) {
                mArray = new Method[]{method};
            } else {
                mArray = new Method[oldMArray.length+1];
                System.arraycopy(oldMArray, 0, mArray, 0, oldMArray.length);
                mArray[oldMArray.length] = method;
            }
            map.put(name, mArray);
        }

        for (Iterator iter = map.entrySet().iterator();  iter.hasNext();  ) {
            Map.Entry entry = (Map.Entry) iter.next();
            String name = (String) entry.getKey();
            Method[] mArray = (Method[]) entry.getValue();
            handlerMap.put(name, newXmlRpcHandler(pType, mArray));
        }
    }

    /** Creates a new instance of {@link XmlRpcHandler}.
     * @param pClass The class, which was inspected for handler
     * methods. This is used for error messages only. Typically,
     * it is the same than <pre>pInstance.getClass()</pre>.
     * @param pMethods The method being invoked.
     */
    protected XmlRpcHandler newXmlRpcHandler(final Class pClass,
            final Method[] pMethods) throws XmlRpcException {
    	String[][] sig = getSignature(pMethods);
    	String help = getMethodHelp(pClass, pMethods);
    	RequestProcessorFactory factory = requestProcessorFactoryFactory.getRequestProcessorFactory(pClass);
        if (sig == null  ||  help == null) {
    		return new ReflectiveXmlRpcHandler(this, typeConverterFactory,
                    pClass, factory, pMethods);
    	}
    	return new ReflectiveXmlRpcMetaDataHandler(this, typeConverterFactory,
                pClass, factory, pMethods, sig, help);
    }

    /** Creates a signature for the given method.
     */
    protected String[][] getSignature(Method[] pMethods) {
    	return Util.getSignature(pMethods);
    }

    /** Creates a help string for the given method, when applied
     * to the given class.
     */
    protected String getMethodHelp(Class pClass, Method[] pMethods) {
    	return Util.getMethodHelp(pClass, pMethods);
    }

    /** Returns the {@link XmlRpcHandler} with the given name.
     * @param pHandlerName The handlers name
     * @throws XmlRpcNoSuchHandlerException A handler with the given
     * name is unknown.
     */
    public XmlRpcHandler getHandler(String pHandlerName)
            throws XmlRpcNoSuchHandlerException, XmlRpcException {
        XmlRpcHandler result = (XmlRpcHandler) handlerMap.get(pHandlerName);
        if (result == null) {
            throw new XmlRpcNoSuchHandlerException("No such handler: " + pHandlerName);
        }
        return result;
    }

	public String[] getListMethods() throws XmlRpcException {
		List list = new ArrayList();
		for (Iterator iter = handlerMap.entrySet().iterator();
		     iter.hasNext();  ) {
			Map.Entry entry = (Map.Entry) iter.next();
			if (entry.getValue() instanceof XmlRpcMetaDataHandler) {
				list.add(entry.getKey());
			}
		}
		
		return (String[]) list.toArray(new String[list.size()]);
	}

	public String getMethodHelp(String pHandlerName) throws XmlRpcException {
		XmlRpcHandler h = getHandler(pHandlerName);
		if (h instanceof XmlRpcMetaDataHandler)
			return ((XmlRpcMetaDataHandler)h).getMethodHelp();
		throw new XmlRpcNoSuchHandlerException("No help available for method: "
				+ pHandlerName);
	}

	public String[][] getMethodSignature(String pHandlerName) throws XmlRpcException {
		XmlRpcHandler h = getHandler(pHandlerName);
		if (h instanceof XmlRpcMetaDataHandler)
			return ((XmlRpcMetaDataHandler)h).getSignatures();
		throw new XmlRpcNoSuchHandlerException("No metadata available for method: "
				+ pHandlerName);
	}

    /**
     * Returns, whether void methods are enabled. By default, null values
     * aren't supported by XML-RPC and void methods are in fact returning
     * null (at least from the perspective of reflection).
     */
    public boolean isVoidMethodEnabled() {
        return voidMethodEnabled;
    }

    /**
     * Sets, whether void methods are enabled. By default, null values
     * aren't supported by XML-RPC and void methods are in fact returning
     * null (at least from the perspective of reflection).
     */
    public void setVoidMethodEnabled(boolean pVoidMethodEnabled) {
        voidMethodEnabled = pVoidMethodEnabled;
    }
}
