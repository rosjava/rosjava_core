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
package org.ros.internal.node.xmlrpc;

import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.client.AsyncCallback;

/**
 * <p>
 * A callback object that can wait up to a specified amount of time for the
 * XML-RPC response. Suggested use is as follows:
 * </p>
 * 
 * <pre>
 * // Wait for 10 seconds.
 * TimingOutCallback callback = new TimingOutCallback(10 * 1000);
 * XmlRpcClient client = new XmlRpcClient(url);
 * client.executeAsync(methodName, aVector, callback);
 * try {
 *   return callback.waitForResponse();
 * } catch (TimeoutException e) {
 *   System.out.println(&quot;No response from server.&quot;);
 * } catch (Exception e) {
 *   System.out.println(&quot;Server returned an error message.&quot;);
 * }
 * </pre>
 */
public class TimingOutCallback implements AsyncCallback {

  private final long timeout;
  private Object result;
  private Throwable error;
  private boolean responseSeen;

  /**
   * Waits the specified number of milliseconds for a response.
   */
  public TimingOutCallback(long pTimeout) {
    timeout = pTimeout;
  }

  /**
   * Called to wait for the response.
   * 
   * @throws InterruptedException
   *           The thread was interrupted.
   * @throws XmlRpcTimeoutException
   *           No response was received after waiting the specified time.
   * @throws Throwable
   *           An error was returned by the server.
   */
  public synchronized Object waitForResponse() throws Throwable {
    if (!responseSeen) {
      wait(timeout);
      if (!responseSeen) {
        throw new XmlRpcTimeoutException(0, "No response after waiting for " + timeout + " milliseconds.");
      }
    }
    if (error != null) {
      throw error;
    }
    return result;
  }

  @Override
  public synchronized void handleError(XmlRpcRequest pRequest, Throwable pError) {
    responseSeen = true;
    error = pError;
    notify();
  }

  @Override
  public synchronized void handleResult(XmlRpcRequest pRequest, Object pResult) {
    responseSeen = true;
    result = pResult;
    notify();
  }
}
