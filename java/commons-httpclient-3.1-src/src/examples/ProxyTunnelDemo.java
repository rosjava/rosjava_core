import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;

import org.apache.commons.httpclient.ProxyClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;

/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//httpclient/src/examples/ProxyTunnelDemo.java,v 1.2 2004/06/12 22:47:23 olegk Exp $
 * $Revision: 480424 $
 * $Date: 2006-11-29 06:56:49 +0100 (Wed, 29 Nov 2006) $
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

/**
 * Example code for using {@link org.apache.commons.httpclient.ProxyClient}.
 * 
 * @author Oleg Kalnichevski
 * @author Michael Becke
 */
public class ProxyTunnelDemo {

    public static void main(String[] args) throws Exception {
        
        ProxyClient proxyclient = new ProxyClient();
        // set the host the proxy should create a connection to
        //
        // Note:  By default port 80 will be used. Some proxies only allow conections
        // to ports 443 and 8443.  This is because the HTTP CONNECT method was intented
        // to be used for tunneling HTTPS.
        proxyclient.getHostConfiguration().setHost("www.yahoo.com");
        // set the proxy host and port
        proxyclient.getHostConfiguration().setProxy("10.0.1.1", 3128);
        // set the proxy credentials, only necessary for authenticating proxies
        proxyclient.getState().setProxyCredentials(
            new AuthScope("10.0.1.1", 3128, null),
            new UsernamePasswordCredentials("proxy", "proxy"));
        
        // create the socket
        ProxyClient.ConnectResponse response = proxyclient.connect(); 
        
        if (response.getSocket() != null) {
            Socket socket = response.getSocket();
            try {
                // go ahead and do an HTTP GET using the socket
                Writer out = new OutputStreamWriter(
                    socket.getOutputStream(), "ISO-8859-1");
                out.write("GET http://www.yahoo.com/ HTTP/1.1\r\n");  
                out.write("Host: www.yahoo.com\r\n");  
                out.write("Agent: whatever\r\n");  
                out.write("\r\n");  
                out.flush();  
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), "ISO-8859-1"));
                String line = null;
                while ((line = in.readLine()) != null) {
                    System.out.println(line);
                }
            } finally {
                // be sure to close the socket when we're done
                socket.close(); 
            }
        } else {
            // the proxy connect was not successful, check connect method for reasons why
            System.out.println("Connect failed: " + response.getConnectMethod().getStatusLine());
            System.out.println(response.getConnectMethod().getResponseBodyAsString());
        }
    }
        
}
