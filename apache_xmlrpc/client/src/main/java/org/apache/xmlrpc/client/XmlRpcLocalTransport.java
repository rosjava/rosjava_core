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
package org.apache.xmlrpc.client;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcConfig;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.TypeConverter;
import org.apache.xmlrpc.common.TypeConverterFactory;
import org.apache.xmlrpc.common.XmlRpcExtensionException;
import org.apache.xmlrpc.common.XmlRpcRequestProcessor;

/** The default implementation of a local transport.
 */
public class XmlRpcLocalTransport extends XmlRpcTransportImpl {
	/** Creates a new instance.
	 * @param pClient The client, which creates the transport.
	 */
	public XmlRpcLocalTransport(XmlRpcClient pClient) {
		super(pClient);
	}

	private boolean isExtensionType(Object pObject) {
		if (pObject == null) {
			return true;
		} else if (pObject instanceof Object[]) {
			Object[] objects = (Object[]) pObject;
			for (int i = 0;  i < objects.length;  i++) {
				if (isExtensionType(objects[i])) {
					return true;
				}
			}
			return false;
        } else if (pObject instanceof Collection) {
            for (Iterator iter = ((Collection) pObject).iterator();  iter.hasNext();  ) {
                if (isExtensionType(iter.next())) {
                    return true;
                }
            }
            return false;
		} else if (pObject instanceof Map) {
			Map map = (Map) pObject;
			for (Iterator iter = map.entrySet().iterator();  iter.hasNext();  ) {
				Map.Entry entry = (Map.Entry) iter.next();
				if (isExtensionType(entry.getKey())  ||  isExtensionType(entry.getValue())) {
					return true;
				}
			}
			return false;
		} else {
			return !(pObject instanceof Integer
                     ||  pObject instanceof Date
					 ||  pObject instanceof String
					 ||  pObject instanceof byte[]
					 ||  pObject instanceof Double);
		}
	}

	public Object sendRequest(XmlRpcRequest pRequest) throws XmlRpcException {
		XmlRpcConfig config = pRequest.getConfig();
		if (!config.isEnabledForExtensions()) {
			for (int i = 0;  i < pRequest.getParameterCount();  i++) {
				if (isExtensionType(pRequest.getParameter(i))) {
					throw new XmlRpcExtensionException("Parameter " + i + " has invalid type, if isEnabledForExtensions() == false");
				}
			}
		}
		final XmlRpcRequestProcessor server = ((XmlRpcLocalClientConfig) config).getXmlRpcServer();
        Object result;
		try {
			result = server.execute(pRequest);
        } catch (XmlRpcException t) {
            throw t;
		} catch (Throwable t) {
		    throw new XmlRpcClientException("Failed to invoke method " + pRequest.getMethodName()
		            + ": " + t.getMessage(), t);
		}
		if (!config.isEnabledForExtensions()) {
			if (isExtensionType(result)) {
				throw new XmlRpcExtensionException("Result has invalid type, if isEnabledForExtensions() == false");
			}
		}

		if (result == null) {
		    return null;
        }
        final TypeConverterFactory typeConverterFactory = server.getTypeConverterFactory();
        final TypeConverter typeConverter = typeConverterFactory.getTypeConverter(result.getClass());
        return typeConverter.backConvert(result);
	}
}