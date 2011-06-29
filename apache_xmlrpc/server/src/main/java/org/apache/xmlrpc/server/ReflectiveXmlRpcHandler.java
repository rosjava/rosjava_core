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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcHandler;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.TypeConverter;
import org.apache.xmlrpc.common.TypeConverterFactory;
import org.apache.xmlrpc.common.XmlRpcInvocationException;
import org.apache.xmlrpc.common.XmlRpcNotAuthorizedException;
import org.apache.xmlrpc.metadata.Util;
import org.apache.xmlrpc.server.AbstractReflectiveHandlerMapping.AuthenticationHandler;
import org.apache.xmlrpc.server.RequestProcessorFactoryFactory.RequestProcessorFactory;


/** Default implementation of {@link XmlRpcHandler}.
 */
public class ReflectiveXmlRpcHandler implements XmlRpcHandler {
    private static class MethodData {
        final Method method;
        final TypeConverter[] typeConverters;
        MethodData(Method pMethod, TypeConverterFactory pTypeConverterFactory) {
            method = pMethod;
            Class[] paramClasses = method.getParameterTypes();
            typeConverters = new TypeConverter[paramClasses.length];
            for (int i = 0;  i < paramClasses.length;  i++) {
                typeConverters[i] = pTypeConverterFactory.getTypeConverter(paramClasses[i]);
            }
        }
    }
    private final AbstractReflectiveHandlerMapping mapping;
	private final MethodData[] methods;
    private final Class clazz;
    private final RequestProcessorFactory requestProcessorFactory;

	/** Creates a new instance.
	 * @param pMapping The mapping, which creates this handler.
	 * @param pClass The class, which has been inspected to create
	 * this handler. Typically, this will be the same as
	 * <pre>pInstance.getClass()</pre>. It is used for diagnostic
	 * messages only.
	 * @param pMethods The method, which will be invoked for
	 * executing the handler. 
	 */
	public ReflectiveXmlRpcHandler(AbstractReflectiveHandlerMapping pMapping,
                TypeConverterFactory pTypeConverterFactory,
                Class pClass, RequestProcessorFactory pFactory, Method[] pMethods) {
		mapping = pMapping;
        clazz = pClass;
		methods = new MethodData[pMethods.length];
        requestProcessorFactory = pFactory;
        for (int i = 0;  i < methods.length;  i++) {
            methods[i] = new MethodData(pMethods[i], pTypeConverterFactory); 
        }
	}

    private Object getInstance(XmlRpcRequest pRequest) throws XmlRpcException {
        return requestProcessorFactory.getRequestProcessor(pRequest);
    }

	public Object execute(XmlRpcRequest pRequest) throws XmlRpcException {
	    AuthenticationHandler authHandler = mapping.getAuthenticationHandler();
	    if (authHandler != null  &&  !authHandler.isAuthorized(pRequest)) {
	        throw new XmlRpcNotAuthorizedException("Not authorized");
	    }
	    Object[] args = new Object[pRequest.getParameterCount()];
	    for (int j = 0;  j < args.length;  j++) {
	        args[j] = pRequest.getParameter(j);
	    }
	    Object instance = getInstance(pRequest);
	    for (int i = 0;  i < methods.length;  i++) {
            MethodData methodData = methods[i];
            TypeConverter[] converters = methodData.typeConverters;
            if (args.length == converters.length) {
                boolean matching = true;
                for (int j = 0;  j < args.length;  j++) {
                    if (!converters[j].isConvertable(args[j])) {
                        matching = false;
                        break;
                    }
                }
                if (matching) {
                    for (int j = 0;  j < args.length;  j++) {
                        args[j] = converters[j].convert(args[j]);
                    }
                    return invoke(instance, methodData.method, args);
                }
            }
	    }
	    throw new XmlRpcException("No method " + pRequest.getMethodName() + " matching arguments: " + Util.getSignature(args));
    }

    private Object invoke(Object pInstance, Method pMethod, Object[] pArgs) throws XmlRpcException {
        try {
	        return pMethod.invoke(pInstance, pArgs);
	    } catch (IllegalAccessException e) {
	        throw new XmlRpcException("Illegal access to method "
	                                  + pMethod.getName() + " in class "
	                                  + clazz.getName(), e);
	    } catch (IllegalArgumentException e) {
	        throw new XmlRpcException("Illegal argument for method "
	                                  + pMethod.getName() + " in class "
	                                  + clazz.getName(), e);
	    } catch (InvocationTargetException e) {
	        Throwable t = e.getTargetException();
            if (t instanceof XmlRpcException) {
                throw (XmlRpcException) t;
            }
	        throw new XmlRpcInvocationException("Failed to invoke method "
	                                  + pMethod.getName() + " in class "
	                                  + clazz.getName() + ": "
	                                  + t.getMessage(), t);
	    }
	}
}
