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

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.metadata.Util;


/**
 * <p>The request processor is the object, which is actually performing
 * the request. There is nothing magic about the request processor:
 * It may very well be a POJO. The {@link RequestProcessorFactoryFactory}
 * is passed to the {@link AbstractReflectiveHandlerMapping} at startup.
 * The mapping uses this factory to create instances of
 * {@link RequestProcessorFactory}, which are used to initialize
 * the {@link ReflectiveXmlRpcHandler}. The handler in turn uses its
 * factory to create the actual request processor when a request comes
 * in.</p>
 * <p>However, the question arises, when and how the request processor
 * is created and whether it needs request specific initialization.
 * The {@link RequestProcessorFactoryFactory} is an object, which makes
 * that logic pluggable. Unfortunately, we aren't done with a single
 * factory: We even need a factory for factories. The rationale is
 * best explained by looking at the different use cases and how to
 * implement them.</p>
 * <p>The default {@link RequestProcessorFactoryFactory} is the
 * {@link RequestSpecificProcessorFactoryFactory}. It creates a new
 * processor instance for any request. In other words, it allows the
 * request processor to have some state. This is fine, if the request
 * processor is a lightweight object or needs request specific
 * initialization. In this case, the actual request processor is
 * created and invoked when
 * calling {@link RequestProcessorFactory#getRequestProcessor(XmlRpcRequest)}.</p>
 * <p>An alternative implementation is the
 * {@link StatelessProcessorFactoryFactory}, which may be used to
 * create stateless request processors. Stateless request processors
 * are typically heavyweight objects, which have an expensive
 * initialization phase. The processor factory, which is created by
 * {@link #getRequestProcessorFactory(Class pClass)} contains an
 * initialized singleton, which is returned by
 * {@link RequestProcessorFactory#getRequestProcessor(XmlRpcRequest)}.</p>
 * <p>Other alternatives might be a
 * {@link RequestProcessorFactoryFactory}, which maintains a pool
 * of {@link RequestProcessorFactory} instances. The instances are
 * configured by calling
 * {@link RequestProcessorFactory#getRequestProcessor(XmlRpcRequest)}.</p>
 */
public interface RequestProcessorFactoryFactory {
    /**
     * This is the factory for request processors. This factory is itself
     * created by a call to
     * {@link RequestProcessorFactoryFactory#getRequestProcessorFactory(Class)}.
     */
    public interface RequestProcessorFactory {
        /**
         * This method is invoked for any request in order to create and
         * configure the request processor. The returned object is an
         * instance of the class parameter in
         * {@link RequestProcessorFactoryFactory#getRequestProcessorFactory(Class)}.
         */
        public Object getRequestProcessor(XmlRpcRequest pRequest) throws XmlRpcException;
    }

    /**
     * This method is invoked at startup. It creates a factory for instances of
     * <code>pClass</code>.
     */
    public RequestProcessorFactory getRequestProcessorFactory(Class pClass) throws XmlRpcException;

    /**
     * This is the default implementation of {@link RequestProcessorFactoryFactory}.
     * A new instance is created and initialized for any request. The instance may
     * be configured by overwriting {@link #getRequestProcessor(Class, XmlRpcRequest)}.
     */
    public static class RequestSpecificProcessorFactoryFactory
            implements RequestProcessorFactoryFactory {
        /**
         * Subclasses may override this method for request specific configuration.
         * A typical subclass will look like this:
         * <pre>
         *   public class MyRequestProcessorFactoryFactory
         *           extends RequestProcessorFactoryFactory {
         *       protected Object getRequestProcessor(Class pClass, XmlRpcRequest pRequest) {
         *           Object result = super.getRequestProcessor(pClass, pRequest);
         *           // Configure the object here
         *           ...
         *           return result;
         *       }
         *   }
         * </pre>
         * @param pRequest The request object.
         */
        protected Object getRequestProcessor(Class pClass, XmlRpcRequest pRequest) throws XmlRpcException {
            return Util.newInstance(pClass);
        }
        public RequestProcessorFactory getRequestProcessorFactory(final Class pClass) throws XmlRpcException {
            return new RequestProcessorFactory(){
                public Object getRequestProcessor(XmlRpcRequest pRequest) throws XmlRpcException {
                    return RequestSpecificProcessorFactoryFactory.this.getRequestProcessor(pClass, pRequest);
                }
            };
        }
    }

    /**
     * This is an alternative implementation of {@link RequestProcessorFactoryFactory}.
     * It creates stateless request processors, which are able to process concurrent
     * requests without request specific initialization.
     */
    public static class StatelessProcessorFactoryFactory
            implements RequestProcessorFactoryFactory {
        /**
         * Subclasses may override this method for class specific configuration. Note,
         * that this method will be called at startup only! A typical subclass will
         * look like this:
         * <pre>
         *   public class MyRequestProcessorFactoryFactory
         *           extends StatelessProcessorFactoryFactory {
         *       protected Object getRequestProcessor(Class pClass) {
         *           Object result = super.getRequestProcessor(pClass);
         *           // Configure the object here
         *           ...
         *           return result;
         *       }
         *   }
         * </pre>
         */
        protected Object getRequestProcessor(Class pClass) throws XmlRpcException {
            return Util.newInstance(pClass);
        }
        public RequestProcessorFactory getRequestProcessorFactory(Class pClass)
                throws XmlRpcException {
            final Object processor = getRequestProcessor(pClass);
            return new RequestProcessorFactory(){
                public Object getRequestProcessor(XmlRpcRequest pRequest) throws XmlRpcException {
                    return processor;
                }
            };
        }
    }
}
