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

package org.ros.internal.node.client;

import com.google.common.collect.Lists;

import org.ros.namespace.GraphName;


import org.ros.internal.node.response.IntegerResultFactory;
import org.ros.internal.node.response.ProtocolDescriptionResultFactory;
import org.ros.internal.node.response.Response;
import org.ros.internal.node.response.TopicDefinitionListResultFactory;
import org.ros.internal.node.response.UriResultFactory;
import org.ros.internal.node.response.VoidResultFactory;
import org.ros.internal.node.topic.TopicDefinition;
import org.ros.internal.transport.ProtocolDescription;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class SlaveClient extends NodeClient<org.ros.internal.node.xmlrpc.Slave> {

  private final GraphName nodeName;

  public SlaveClient(GraphName nodeName, URI uri) {
    super(uri, org.ros.internal.node.xmlrpc.Slave.class);
    this.nodeName = nodeName;
  }

  public List<Object> getBusStats() {
    throw new UnsupportedOperationException();
  }

  public List<Object> getBusInfo() {
    throw new UnsupportedOperationException();
  }

  public Response<URI> getMasterUri() {
    return Response.fromListChecked(node.getMasterUri(nodeName.toString()), new UriResultFactory());
  }

  public List<Object> shutdown(String message) {
    throw new UnsupportedOperationException();
  }

  public Response<Integer> getPid() {
    return Response.fromListChecked(node.getPid(nodeName.toString()), new IntegerResultFactory());
  }

  public Response<List<TopicDefinition>> getSubscriptions() {
    return Response.fromListChecked(node.getSubscriptions(nodeName.toString()),
        new TopicDefinitionListResultFactory());
  }

  public Response<List<TopicDefinition>> getPublications() {
    return Response.fromListChecked(node.getPublications(nodeName.toString()),
        new TopicDefinitionListResultFactory());
  }

  public Response<Void> paramUpdate(String parameterKey, Boolean parameterValue) {
    return Response.fromListChecked(
        node.paramUpdate(nodeName.toString(), parameterKey, parameterValue),
        new VoidResultFactory());
  }

  public Response<Void> paramUpdate(String parameterKey, Character parameterValue) {
    return Response.fromListChecked(
        node.paramUpdate(nodeName.toString(), parameterKey, parameterValue),
        new VoidResultFactory());
  }

  public Response<Void> paramUpdate(String parameterKey, Byte parameterValue) {
    return Response.fromListChecked(
        node.paramUpdate(nodeName.toString(), parameterKey, parameterValue),
        new VoidResultFactory());
  }

  public Response<Void> paramUpdate(String parameterKey, Short parameterValue) {
    return Response.fromListChecked(
        node.paramUpdate(nodeName.toString(), parameterKey, parameterValue),
        new VoidResultFactory());
  }

  public Response<Void> paramUpdate(String parameterKey, Integer parameterValue) {
    return Response.fromListChecked(
        node.paramUpdate(nodeName.toString(), parameterKey, parameterValue),
        new VoidResultFactory());
  }

  public Response<Void> paramUpdate(String parameterKey, Double parameterValue) {
    return Response.fromListChecked(
        node.paramUpdate(nodeName.toString(), parameterKey, parameterValue),
        new VoidResultFactory());
  }

  public Response<Void> paramUpdate(String parameterKey, String parameterValue) {
    return Response.fromListChecked(
        node.paramUpdate(nodeName.toString(), parameterKey, parameterValue),
        new VoidResultFactory());
  }

  public Response<Void> paramUpdate(String parameterKey, List<?> parameterValue) {
    return Response.fromListChecked(
        node.paramUpdate(nodeName.toString(), parameterKey, parameterValue),
        new VoidResultFactory());
  }

  public Response<Void> paramUpdate(String parameterKey, Vector<?> parameterValue) {
    return Response.fromListChecked(
        node.paramUpdate(nodeName.toString(), parameterKey, parameterValue),
        new VoidResultFactory());
  }

  public Response<Void> paramUpdate(String parameterKey, Map<?, ?> parameterValue) {
    return Response.fromListChecked(
        node.paramUpdate(nodeName.toString(), parameterKey, parameterValue),
        new VoidResultFactory());
  }

  public Response<Void> publisherUpdate(String topic, Collection<URI> publisherUris) {
    List<String> publishers = Lists.newArrayList();
    for (URI uri : publisherUris) {
      publishers.add(uri.toString());
    }
    return Response.fromListChecked(
        node.publisherUpdate(nodeName.toString(), topic, publishers.toArray()),
        new VoidResultFactory());
  }

  public Response<ProtocolDescription> requestTopic(String topic,
      Collection<String> requestedProtocols) {
    return Response.fromListChecked(node.requestTopic(nodeName.toString(), topic,
        new Object[][] { requestedProtocols.toArray() }), new ProtocolDescriptionResultFactory());
  }

}
