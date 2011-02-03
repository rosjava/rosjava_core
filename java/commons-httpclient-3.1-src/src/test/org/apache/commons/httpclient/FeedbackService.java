/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/test/org/apache/commons/httpclient/FeedbackService.java,v 1.1 2004/11/20 17:56:39 olegk Exp $
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
package org.apache.commons.httpclient;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.httpclient.server.HttpService;
import org.apache.commons.httpclient.server.RequestLine;
import org.apache.commons.httpclient.server.SimpleRequest;
import org.apache.commons.httpclient.server.SimpleResponse;


public class FeedbackService implements HttpService {

    public FeedbackService() {
        super();
    }

    public boolean process(final SimpleRequest request, final SimpleResponse response)
        throws IOException
    {
        RequestLine requestline = request.getRequestLine();
        HttpVersion httpversion = requestline.getHttpVersion();

        StringWriter buffer = new StringWriter(100);
        buffer.write("Method type: ");
        buffer.write(requestline.getMethod());
        buffer.write("\r\n");
        buffer.write("Requested resource: ");
        buffer.write(requestline.getUri());
        buffer.write("\r\n");
        buffer.write("Protocol version: ");
        buffer.write(httpversion.toString());
        buffer.write("\r\n");
        
        String requestbody = request.getBodyString();
        if (requestbody != null && !requestbody.equals("")) {
            buffer.write("\r\n");
            buffer.write("Request body: ");
            buffer.write(requestbody);
            buffer.write("\r\n");
        }
        
        response.setStatusLine(httpversion, HttpStatus.SC_OK);
        response.setBodyString(buffer.toString());
        return true;
    }
}
