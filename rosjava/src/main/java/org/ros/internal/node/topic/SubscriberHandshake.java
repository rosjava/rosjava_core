/*
 * Copyright (C) 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.internal.node.topic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.internal.transport.ClientHandshake;
import org.ros.internal.transport.ConnectionHeaderFields;

import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class SubscriberHandshake implements ClientHandshake {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(SubscriberHandshake.class);

  private final Map<String, String> outgoingHeader;
  
  private String errorMessage;

  public SubscriberHandshake(Map<String, String> outgoingHeader) {
    this.outgoingHeader = outgoingHeader;
  }

  @Override
  public boolean handshake(Map<String, String> incomingHeader) {
    if (DEBUG) {
      log.info("Outgoing subscriber connection header: " + outgoingHeader);
      log.info("Incoming publisher connection header: " + incomingHeader);
    }
    errorMessage = incomingHeader.get(ConnectionHeaderFields.ERROR);
    if (errorMessage != null) {
      return false;
    }
    if (!incomingHeader.get(ConnectionHeaderFields.TYPE).equals(
        outgoingHeader.get(ConnectionHeaderFields.TYPE))) {
      errorMessage = "Message types don't match.";
      return false;
    }
    if (!incomingHeader.get(ConnectionHeaderFields.MD5_CHECKSUM).equals(
        outgoingHeader.get(ConnectionHeaderFields.MD5_CHECKSUM))) {
      errorMessage = "Checksums don't match.";
      return false;
    }
    return true;
  }

  @Override
  public Map<String, String> getOutgoingHeader() {
    return outgoingHeader;
  }
  
  @Override
  public String getErrorMessage() {
    return errorMessage;
  }
}
