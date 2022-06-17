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

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.xmlrpc.XmlRpcException;


/**
 * A handler mapping based on a property file. The property file
 * contains a set of properties. The property key is taken as the
 * handler name. The property value is taken as the name of a
 * class being instantiated. For any non-void, non-static, and
 * public method in the class, an entry in the handler map is
 * generated. A typical use would be, to specify interface names
 * as the property keys and implementations as the values.
 */
public final class PropertyHandlerMapping extends AbstractReflectiveHandlerMapping {
    /**
     * Reads handler definitions from a resource file.
     * @param pClassLoader The class loader being used to load
     *   handler classes.
     * @param pResource The resource being used, for example
     *   "org/apache/xmlrpc/webserver/XmlRpcServlet.properties"
     * @throws IOException Loading the property file failed.
     * @throws XmlRpcException Initializing the handlers failed.
     */
    public void load(ClassLoader pClassLoader, String pResource)
            throws IOException, XmlRpcException {
        URL url = pClassLoader.getResource(pResource);
        if (url == null) {
            throw new IOException("Unable to locate resource " + pResource);
        }
        load(pClassLoader, url);
    }
    
    /**
     * Reads handler definitions from a property file.
     * @param pClassLoader The class loader being used to load
     *   handler classes.
     * @param pURL The URL from which to load the property file
     * @throws IOException Loading the property file failed.
     * @throws XmlRpcException Initializing the handlers failed.
     */
    public void load(ClassLoader pClassLoader, URL pURL) throws IOException, XmlRpcException {
        Properties props = new Properties();
        props.load(pURL.openStream());
        load(pClassLoader, props);
    }

    /**
     * Reads handler definitions from an existing Map.
     * @param pClassLoader The class loader being used to load
     *   handler classes.
     * @param pMap The existing Map to read from
     * @throws XmlRpcException Initializing the handlers failed.
     */
    public void load(ClassLoader pClassLoader, Map pMap) throws XmlRpcException {
        for (Iterator iter = pMap.entrySet().iterator();  iter.hasNext();  ) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            Class c = newHandlerClass(pClassLoader, value);
            registerPublicMethods(key, c);
        }
    }

    protected Class newHandlerClass(ClassLoader pClassLoader, String pClassName)
            throws XmlRpcException {
        final Class c;
        try {
            c = pClassLoader.loadClass(pClassName);
        } catch (ClassNotFoundException e) {
            throw new XmlRpcException("Unable to load class: " + pClassName, e);
        }
        if (c == null) {
            throw new XmlRpcException(0, "Loading class " + pClassName + " returned null.");
        }
        return c;
    }

    /** Adds handlers for the given object to the mapping.
     * The handlers are build by invoking
     * {@link #registerPublicMethods(String, Class)}.
     * @param pKey The class key, which is passed
     * to {@link #registerPublicMethods(String, Class)}.
     * @param pClass Class, which is responsible for handling the request.
     */
    public void addHandler(String pKey, Class pClass) throws XmlRpcException {
        registerPublicMethods(pKey, pClass);
    }

    /** Removes all handlers with the given class key.
     */
    public void removeHandler(String pKey) {
        for (Iterator i = handlerMap.keySet().iterator(); i.hasNext();) {
            String k = (String)i.next();
            if (k.startsWith(pKey)) i.remove();
        }
    }
}
