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
package org.apache.xmlrpc.metadata;

import java.lang.reflect.Method;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.common.TypeConverterFactory;
import org.apache.xmlrpc.server.AbstractReflectiveHandlerMapping;
import org.apache.xmlrpc.server.ReflectiveXmlRpcHandler;
import org.apache.xmlrpc.server.RequestProcessorFactoryFactory.RequestProcessorFactory;


/** Default implementation of {@link XmlRpcMetaDataHandler}.
 */
public final class ReflectiveXmlRpcMetaDataHandler extends ReflectiveXmlRpcHandler
		implements XmlRpcMetaDataHandler {
	private final String[][] signatures;
	private final String methodHelp;

	/** Creates a new instance.
	 * @param pMapping The mapping, which creates this handler.
	 * @param pClass The class, which has been inspected to create
	 * this handler. Typically, this will be the same as
	 * <pre>pInstance.getClass()</pre>. It is used for diagnostic
	 * messages only.
	 * @param pMethods The method, which will be invoked for
	 * executing the handler. 
	 * @param pSignatures The signature, which will be returned by
	 * {@link #getSignatures()}.
	 * @param pMethodHelp The help string, which will be returned
	 * by {@link #getMethodHelp()}.
	 */
	public ReflectiveXmlRpcMetaDataHandler(AbstractReflectiveHandlerMapping pMapping,
                TypeConverterFactory pTypeConverterFactory,
			    Class pClass, RequestProcessorFactory pFactory, Method[] pMethods,
			    String[][] pSignatures, String pMethodHelp) {
		super(pMapping, pTypeConverterFactory, pClass, pFactory, pMethods);
		signatures = pSignatures;
		methodHelp = pMethodHelp;
	}

	public String[][] getSignatures() throws XmlRpcException {
		return signatures;
	}

	public String getMethodHelp() throws XmlRpcException {
		return methodHelp;
	}
}
