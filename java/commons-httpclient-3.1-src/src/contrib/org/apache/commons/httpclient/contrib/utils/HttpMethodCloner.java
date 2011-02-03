/*
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
 * [Additional notices, if required by prior licensing conditions]
 *
 */

package org.apache.commons.httpclient.contrib.utils;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

/**
 * In this class are only methods to copy a HttpMethod: 
 * PUT, GET, POST,DELETE, TRACE, ...
 *
 * @author <a href="mailto:mathis@vtg.at">Thomas Mathis</a>
 * 
 * @deprecated
 * 
 * DISCLAIMER: HttpClient developers DO NOT actively support this component.
 * The component is provided as a reference material, which may be inappropriate
 * to be used without additional customization.
 */

public class HttpMethodCloner {

    private static void copyEntityEnclosingMethod(
      EntityEnclosingMethod m, EntityEnclosingMethod copy )
        throws java.io.IOException
     {
         copy.setRequestEntity(m.getRequestEntity());
     }
 
    private static void copyHttpMethodBase(
      HttpMethodBase m, HttpMethodBase copy) {
        try {
            copy.setParams((HttpMethodParams)m.getParams().clone());
        } catch (CloneNotSupportedException e) {
            // Should never happen
        }
    }

    /**
     * Clones a HttpMethod. <br>
     * <b>Attention:</b> You have to clone a method before it has 
     * been executed, because the URI can change if followRedirects 
     * is set to true.
     *
     * @param m the HttpMethod to clone
     *
     * @return the cloned HttpMethod, null if the HttpMethod could 
     * not be instantiated
     *
     * @throws java.io.IOException if the request body couldn't be read
     */
    public static HttpMethod clone(HttpMethod m) 
      throws java.io.IOException
    {
        HttpMethod copy = null;

        // copy the HttpMethod
        try {
            copy = (HttpMethod) m.getClass().newInstance();
        } catch (InstantiationException iEx) {
        } catch (IllegalAccessException iaEx) {
        }
        if ( copy == null ) {
            return null;
        }
        copy.setDoAuthentication(m.getDoAuthentication());
        copy.setFollowRedirects(m.getFollowRedirects());
        copy.setPath( m.getPath() );
        copy.setQueryString(m.getQueryString());

        // clone the headers
        Header[] h = m.getRequestHeaders();
        int size = (h == null) ? 0 : h.length;

        for (int i = 0; i < size; i++) {
            copy.setRequestHeader(
              new Header(h[i].getName(), h[i].getValue()));
        }
        copy.setStrictMode(m.isStrictMode());
        if (m instanceof HttpMethodBase) {
            copyHttpMethodBase(
              (HttpMethodBase)m, 
              (HttpMethodBase)copy);
        }
        if (m instanceof EntityEnclosingMethod) {
            copyEntityEnclosingMethod(
              (EntityEnclosingMethod)m,
              (EntityEnclosingMethod)copy);
        }
        return copy;
    }
}
