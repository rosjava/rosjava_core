/*
 * $HeadURL: https://svn.apache.org/repos/asf/jakarta/httpcomponents/oac.hc3x/tags/HTTPCLIENT_3_1/src/contrib/org/apache/commons/httpclient/contrib/benchmark/BenchmarkWorker.java $
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
package org.apache.commons.httpclient.contrib.benchmark;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;

/**
 * <p>Benchmark worker that can execute an HTTP method given number of times</p>
 * 
 * @author <a href="mailto:oleg at ural.ru">Oleg Kalnichevski</a>
 *
 * @version $Revision: 480424 $
 */
public class BenchmarkWorker {

    private byte[] buffer = new byte[4096];
    private final int verbosity;
    private final HttpClient httpexecutor;
    
    public BenchmarkWorker(final HttpClient httpexecutor, int verbosity) {
        super();
        this.httpexecutor = httpexecutor;
        this.verbosity = verbosity;
    }
    
    public Stats execute(
            final HostConfiguration hostconf,
            final HttpMethod method, 
            int count,
            boolean keepalive) throws HttpException {
        Stats stats = new Stats();
        stats.start();
        for (int i = 0; i < count; i++) {
            try {
                this.httpexecutor.executeMethod(hostconf, method);
                if (this.verbosity >= 4) {
                    System.out.println(">> " + method.getName() + " " + 
                            method.getURI() + " " + method.getParams().getVersion());
                    Header[] headers = method.getRequestHeaders();
                    for (int h = 0; h < headers.length; h++) {
                        System.out.print(">> " + headers[h].toString());
                    }
                    System.out.println();
                }
                if (this.verbosity >= 3) {
                    System.out.println(method.getStatusLine().getStatusCode());
                }
                if (this.verbosity >= 4) {
                    System.out.println("<< " + method.getStatusLine().toString());
                    Header[] headers = method.getResponseHeaders();
                    for (int h = 0; h < headers.length; h++) {
                        System.out.print("<< " + headers[h].toString());
                    }
                    System.out.println();
                }
                InputStream instream = method.getResponseBodyAsStream();
                long contentlen = 0;
                if (instream != null) {
                    int l = 0;
                    while ((l = instream.read(this.buffer)) != -1) {
                        stats.incTotal(l);
                        contentlen += l;
                    }
                }
                stats.setContentLength(contentlen);
                stats.incSuccessCount();
            } catch (IOException ex) {
                stats.incFailureCount();
                if (this.verbosity >= 2) {
                    System.err.println("I/O error: " + ex.getMessage());
                }
            } finally {
                method.releaseConnection();
            }
            if (!keepalive) {
                this.httpexecutor.getHttpConnectionManager().closeIdleConnections(0);
            }
        }
        stats.finish();
        Header header = method.getResponseHeader("Server");
        if (header != null) {
            stats.setServerName(header.getValue());
        }
        return stats;
    }

}
