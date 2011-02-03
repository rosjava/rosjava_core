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
package org.apache.commons.httpclient;

import org.apache.commons.httpclient.methods.HeadMethod;

/**
 * HTTP GET methid intended to simulate side-effects of 
 * interaction with non-compiant HTTP servers or proxies
 * 
 * @author Oleg Kalnichevski
 */

public class NoncompliantHeadMethod extends HeadMethod {

    public NoncompliantHeadMethod(){
        super();
    }

    public NoncompliantHeadMethod(String uri) {
        super(uri);
    }

    /**
     * Expect HTTP HEAD but perform HTTP GET instead in order to 
     * simulate the behaviour of a non-compliant HTTP server sending
     * body content in response to HTTP HEAD request 
     *  
     */
    public String getName() {
        return "GET";
    }

}
