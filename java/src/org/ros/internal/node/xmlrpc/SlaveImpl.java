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

package org.ros.internal.node.xmlrpc;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.internal.node.response.Response;
import org.ros.internal.node.response.StatusCode;
import org.ros.internal.node.server.ServerException;
import org.ros.internal.node.server.SlaveServer;
import org.ros.internal.node.topic.Publisher;
import org.ros.internal.node.topic.Subscriber;
import org.ros.internal.transport.ProtocolDescription;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SlaveImpl implements Slave {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(SlaveImpl.class);

  private final SlaveServer slave;

  public SlaveImpl(SlaveServer slave) {
    this.slave = slave;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ros.node.xmlrpc.Slave#getBusStats(java.lang.String)
   */
  @Override
  public List<Object> getBusStats(String callerId) {
    return slave.getBusStats(callerId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ros.node.xmlrpc.Slave#getBusInfo(java.lang.String)
   */
  @Override
  public List<Object> getBusInfo(String callerId) {
    List<Object> busInfo = slave.getBusInfo(callerId);
    return Response.createSuccess("bus info", busInfo).toList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ros.node.xmlrpc.Slave#getMasterUri(java.lang.String)
   */
  @Override
  public List<Object> getMasterUri(String callerId) {
    URI uri = slave.getMasterUri(callerId);
    return new Response<String>(StatusCode.SUCCESS, "", uri.toString()).toList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ros.node.xmlrpc.Slave#shutdown(java.lang.String, java.lang.String)
   */
  @Override
  public List<Object> shutdown(String callerId, String message) {
    slave.shutdown(callerId, message);
    return Response.createSuccess("Shutdown successful.", null).toList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ros.node.xmlrpc.Slave#getPid(java.lang.String)
   */
  @Override
  public List<Object> getPid(String callerId) {
    try {
      int pid = slave.getPid(callerId);
      return Response.createSuccess("pid: " + pid, pid).toList();
    } catch (UnsupportedOperationException e) {
      return Response.createFailure("cannot retrieve pid on this platform", -1).toList();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ros.node.xmlrpc.Slave#getSubscriptions(java.lang.String)
   */
  @Override
  public List<Object> getSubscriptions(String callerId) {
    List<Subscriber<?>> subscribers = slave.getSubscriptions();
    List<List<String>> subscriptions = Lists.newArrayList();
    for (Subscriber<?> subscriber : subscribers) {
      subscriptions.add(subscriber.getTopicDefinitionAsList());
    }
    return Response.createSuccess("Success", subscriptions).toList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ros.node.xmlrpc.Slave#getPublications(java.lang.String)
   */
  @Override
  public List<Object> getPublications(String callerId) {
    List<Publisher<?>> publishers = slave.getPublications();
    List<List<String>> publications = Lists.newArrayList();
    for (Publisher<?> publisher : publishers) {
      publications.add(publisher.getTopicDefinitionAsList());
    }
    return Response.createSuccess("Success", publications).toList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ros.node.xmlrpc.Slave#paramUpdate(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @Override
  public List<Object> paramUpdate(String callerId, String parameterKey, String parameterValue) {
    // TODO(kwc) implement parameter subscriptions (low priority)
    return Response.createError("paramUpdate is not supported", 0).toList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ros.node.xmlrpc.Slave#publisherUpdate(java.lang.String,
   * java.lang.String, java.lang.Object[])
   */
  @Override
  public List<Object> publisherUpdate(String callerId, String topic, Object[] publishers) {
    try {
      ArrayList<URI> publisherUris = new ArrayList<URI>(publishers.length);
      for (Object publisher : publishers) {
        publisherUris.add(new URI(publisher.toString()));
      }
      //TODO(kwc): remove when publisherUpdate is implemented
      slave.publisherUpdate(callerId, topic, publisherUris);
      return Response.createFailure("publisherUpdate implementation in progress", 0).toList();
    } catch (URISyntaxException e) {
      return Response.createError("invalid URI sent in update", 0).toList();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ros.node.xmlrpc.Slave#requestTopic(java.lang.String,
   * java.lang.String, java.lang.Object[])
   */
  @Override
  public List<Object> requestTopic(String callerId, String topic, Object[] protocols) {
    Set<String> requestedProtocols = Sets.newHashSet();
    for (int i = 0; i < protocols.length; i++) {
      requestedProtocols.add((String) ((Object[]) protocols[i])[0]);
    }
    ProtocolDescription protocol;
    try {
      protocol = slave.requestTopic(topic, requestedProtocols);
    } catch (ServerException e) {
      return Response.createError(e.getMessage(), null).toList();
    }
    List<Object> response = Response.createSuccess(protocol.toString(), protocol.toList()).toList();
    if (DEBUG) {
      log.info("requestTopic(" + topic + ", " + requestedProtocols + ") response: "
          + response.toString());
    }
    return response;
  }

}
