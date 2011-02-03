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

import java.io.IOException;
import org.apache.commons.httpclient.methods.PostMethod;

/**
 * HTTP POST methid intended to simulate side-effects of 
 * interaction with non-compiant HTTP servers or proxies
 * 
 * @author Oleg Kalnichevski
 */

public class NoncompliantPostMethod extends PostMethod {

    public NoncompliantPostMethod(){
        super();
    }

    public NoncompliantPostMethod(String uri) {
        super(uri);
    }

    /**
     * NoncompliantPostMethod class skips "Expect: 100-continue"
     * header when sending request headers to an HTTP server.
     * 
     * <p>
     * That makes the server expect the request body to follow 
     * immediately after the request head. The HTTP server does not 
     * send status code 100 expected by the client. The client should 
     * be able to recover gracefully by sending the request body 
     * after a defined timeout without having received "continue"
     * code.
     * </p>
     */
    protected void writeRequestHeaders(HttpState state, HttpConnection conn)
        throws IOException, HttpException {
        addRequestHeaders(state, conn);
        Header[] headers = getRequestHeaders();
        for (int i = 0; i < headers.length; i++) {
            Header header = headers[i];
            // Write all the headers but "Expect"
            if (!header.getName().equalsIgnoreCase("Expect") ) {
                conn.print(header.toExternalForm(), "US-ASCII");
            }
        }
    }

}
