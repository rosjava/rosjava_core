/*
 * $HeadURL: https://svn.apache.org/repos/asf/jakarta/httpcomponents/oac.hc3x/tags/HTTPCLIENT_3_1/src/contrib/org/apache/commons/httpclient/contrib/benchmark/HttpBenchmark.java $
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

import java.io.File;
import java.net.URL;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

/**
 * <p>A simple HTTP benchmark tool, which implements a subset of AB (Apache Benchmark) interface</p>
 * 
 * @author <a href="mailto:oleg at ural.ru">Oleg Kalnichevski</a>
 *
 * @version $Revision: 480424 $
 */
public class HttpBenchmark {

    private static HttpClient createRequestExecutor() {
        HttpClient httpclient = new HttpClient();
        httpclient.getParams().setVersion(HttpVersion.HTTP_1_1);
        httpclient.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, false);
        httpclient.getHttpConnectionManager().getParams().setStaleCheckingEnabled(false);
        return httpclient;
    }
    
    public static void main(String[] args) throws Exception {

        Option iopt = new Option("i", false, "Do HEAD requests instead of GET.");
        iopt.setRequired(false);
        
        Option kopt = new Option("k", false, "Enable the HTTP KeepAlive feature, " +
                "i.e., perform multiple requests within one HTTP session. " +
                "Default is no KeepAlive");
        kopt.setRequired(false);
        
        Option nopt = new Option("n", true, "Number of requests to perform for the " +
                "benchmarking session. The default is to just perform a single " +
                "request which usually leads to non-representative benchmarking " +
                "results.");
        nopt.setRequired(false);
        nopt.setArgName("requests");
        
        Option popt = new Option("p", true, "File containing data to POST.");
        popt.setRequired(false);
        popt.setArgName("POST-file");

        Option Topt = new Option("T", true, "Content-type header to use for POST data.");
        Topt.setRequired(false);
        Topt.setArgName("content-type");

        Option vopt = new Option("v", true, "Set verbosity level - 4 and above prints " +
                "information on headers, 3 and above prints response codes (404, 200, " +
                "etc.), 2 and above prints warnings and info.");
        vopt.setRequired(false);
        vopt.setArgName("verbosity");

        Option hopt = new Option("h", false, "Display usage information.");
        nopt.setRequired(false);
        
        Options options = new Options();
        options.addOption(iopt);
        options.addOption(kopt);
        options.addOption(nopt);
        options.addOption(popt);
        options.addOption(Topt);
        options.addOption(vopt);
        options.addOption(hopt);

        if (args.length == 0) {
            showUsage(options);
            System.exit(1);
        }
        
        CommandLineParser parser = new PosixParser();
        CommandLine cmd = parser.parse(options, args);
        
        if (cmd.hasOption('h')) {
            showUsage(options);
            System.exit(1);
        }
        
        int verbosity = 0;
        if(cmd.hasOption('v')) {
            String s = cmd.getOptionValue('v');
            try {
                verbosity = Integer.parseInt(s);
            } catch (NumberFormatException ex) {
                System.err.println("Invalid verbosity level: " + s);
                showUsage(options);
                System.exit(-1);
            }
        }
        
        boolean keepAlive = false;
        if(cmd.hasOption('k')) {
            keepAlive = true;
        }
        
        int num = 1;
        if(cmd.hasOption('n')) {
            String s = cmd.getOptionValue('n');
            try {
                num = Integer.parseInt(s);
            } catch (NumberFormatException ex) {
                System.err.println("Invalid number of requests: " + s);
                showUsage(options);
                System.exit(-1);
            }
        }
        
        args = cmd.getArgs();
        if (args.length != 1) {
            showUsage(options);
            System.exit(-1);
        }
        // Parse the target url 
        URL url = new URL(args[0]); 
        
        // Prepare host configuration
        HostConfiguration hostconf = new HostConfiguration();
        hostconf.setHost(
                url.getHost(), 
                url.getPort(), 
                url.getProtocol());
        
        // Prepare request
        HttpMethod method = null;
        if (cmd.hasOption('p')) {
            PostMethod httppost = new PostMethod(url.getPath());
            File file = new File(cmd.getOptionValue('p'));
            if (!file.exists()) {
                System.err.println("File not found: " + file);
                System.exit(-1);
            }
            String contenttype = null;
            if (cmd.hasOption('T')) {
                contenttype = cmd.getOptionValue('T'); 
            }
            FileRequestEntity entity = new FileRequestEntity(file, contenttype);
            httppost.setRequestEntity(entity);
            if (file.length() > 100000) {
                httppost.setContentChunked(true);
            }
            method = httppost;
        } else if (cmd.hasOption('i')) {
            HeadMethod httphead = new HeadMethod(url.getPath());
            method = httphead;
        } else {
            GetMethod httpget = new GetMethod(url.getPath());
            method = httpget;
        }
        if (!keepAlive) {
            method.addRequestHeader("Connection", "close");
        }
        
        // Prepare request executor
        HttpClient executor = createRequestExecutor();
        BenchmarkWorker worker = new BenchmarkWorker(executor, verbosity);
        
        // Execute
        Stats stats = worker.execute(hostconf, method, num, keepAlive);
        
        // Show the results
        float totalTimeSec = (float)stats.getDuration() / 1000;
        float reqsPerSec = (float)stats.getSuccessCount() / totalTimeSec; 
        float timePerReqMs = (float)stats.getDuration() / (float)stats.getSuccessCount(); 
        
        System.out.print("Server Software:\t");
        System.out.println(stats.getServerName());
        System.out.print("Server Hostname:\t");
        System.out.println(hostconf.getHost());
        System.out.print("Server Port:\t\t");
        if (hostconf.getPort() > 0) {
            System.out.println(hostconf.getPort());
        } else {
            System.out.println(hostconf.getProtocol().getDefaultPort());
        }
        System.out.println();
        System.out.print("Document Path:\t\t");
        System.out.println(method.getURI());
        System.out.print("Document Length:\t");
        System.out.print(stats.getContentLength());
        System.out.println(" bytes");
        System.out.println();
        System.out.print("Time taken for tests:\t");
        System.out.print(totalTimeSec);
        System.out.println(" seconds");
        System.out.print("Complete requests:\t");
        System.out.println(stats.getSuccessCount());
        System.out.print("Failed requests:\t");
        System.out.println(stats.getFailureCount());
        System.out.print("Content transferred:\t");
        System.out.print(stats.getTotal());
        System.out.println(" bytes");
        System.out.print("Requests per second:\t");
        System.out.print(reqsPerSec);
        System.out.println(" [#/sec] (mean)");
        System.out.print("Time per request:\t");
        System.out.print(timePerReqMs);
        System.out.println(" [ms] (mean)");
    }

    private static void showUsage(final Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("HttpBenchmark [options] [http://]hostname[:port]/path", options);
    }
    
}
