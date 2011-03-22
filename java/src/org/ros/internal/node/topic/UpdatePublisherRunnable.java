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
import org.ros.internal.node.RemoteException;
import org.ros.internal.node.client.SlaveClient;
import org.ros.internal.node.response.Response;
import org.ros.internal.transport.ProtocolDescription;
import org.ros.internal.transport.ProtocolNames;
import org.ros.message.Message;

import java.net.MalformedURLException;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
class UpdatePublisherRunnable<MessageType extends Message> implements Runnable {

  private static final Log log = LogFactory.getLog(UpdatePublisherRunnable.class);

  private final Subscriber<MessageType> subscriber;
  private final PublisherIdentifier publisherIdentifier;

  /**
   * @param subscriber
   * @param publisherIdentifier
   */
  public UpdatePublisherRunnable(Subscriber<MessageType> subscriber,
      PublisherIdentifier publisherIdentifier) {
    this.subscriber = subscriber;
    this.publisherIdentifier = publisherIdentifier;
  }

  @Override
  public void run() {
    SlaveClient slaveClient;
    try {
      slaveClient = new SlaveClient(publisherIdentifier.getNodeName(),
          publisherIdentifier.getSlaveUri());
      Response<ProtocolDescription> response = slaveClient.requestTopic(
          this.subscriber.getTopicName(), ProtocolNames.SUPPORTED);
      // TODO(kwc): all of this logic really belongs in a protocol handler
      // registry.
      ProtocolDescription selected = response.getResult();
      if (ProtocolNames.SUPPORTED.contains(selected.getName())) {
        subscriber.addPublisher(publisherIdentifier, selected.getAddress());
      } else {
        log.error("Publisher returned unsupported protocol selection: " + response);
      }
    } catch (MalformedURLException e) {
      log.error(e);
    } catch (RemoteException e) {
      // TODO(damonkohler): Retry logic is needed at the XML-RPC layer.
      log.error(e);
    }
  }
}
