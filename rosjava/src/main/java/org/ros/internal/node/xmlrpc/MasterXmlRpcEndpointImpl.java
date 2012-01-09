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

import org.ros.exception.RosRuntimeException;
import org.ros.internal.node.response.Response;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.server.ParameterServer;
import org.ros.internal.node.server.SlaveIdentifier;
import org.ros.internal.node.service.ServiceIdentifier;
import org.ros.internal.node.topic.PublisherIdentifier;
import org.ros.internal.node.topic.SubscriberIdentifier;
import org.ros.internal.node.topic.TopicIdentifier;
import org.ros.namespace.GraphName;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A combined XML RPC endpoint for the master and parameter servers.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MasterXmlRpcEndpointImpl implements MasterXmlRpcEndpoint,
    ParameterServerXmlRpcEndpoint {

  private final MasterServer master;
  private final ParameterServer parameterServer;

  public MasterXmlRpcEndpointImpl(MasterServer master) {
    this.master = master;
    parameterServer = new ParameterServer();
  }

  @Override
  public List<Object> getPublishedTopics(String callerId, String subgraph) {
    return Response.newSuccess("current topics",
        master.getPublishedTopics(new GraphName(callerId), new GraphName(subgraph))).toList();
  }

  @Override
  public List<Object> getTopicTypes(String callerId) {
    return Response.newSuccess("topic types", master.getTopicTypes(new GraphName(callerId)))
        .toList();
  }

  @Override
  public List<Object> getSystemState(String callerId) {
    return Response.newSuccess("current system state", master.getSystemState()).toList();
  }

  @Override
  public List<Object> getUri(String callerId) {
    return Response.newSuccess("Success", master.getUri().toString()).toList();
  }

  @Override
  public List<Object> lookupNode(String callerId, String nodeName) {
    SlaveIdentifier identifier = master.lookupNode(new GraphName(nodeName));
    if (identifier != null) {
      return Response.newSuccess("Success", identifier.getUri().toString()).toList();
    } else {
      return Response.newError("No such node", null).toList();
    }
  }

  @Override
  public List<Object> lookupService(String callerId, String service) {
    ServiceIdentifier identifier = master.lookupService(new GraphName(service));
    if (identifier != null) {
      return Response.newSuccess("Success", identifier.getUri().toString()).toList();
    }
    return Response.newError("No such service.", null).toList();
  }

  @Override
  public List<Object> registerPublisher(String callerId, String topic, String topicType,
      String callerApi) {
    SlaveIdentifier slaveIdentifier = SlaveIdentifier.newFromStrings(callerId, callerApi);
    PublisherIdentifier publisherIdentifier =
        new PublisherIdentifier(slaveIdentifier, new TopicIdentifier(new GraphName(topic)));
    List<SubscriberIdentifier> subscribers =
        master.registerPublisher(publisherIdentifier, topicType);
    List<String> urls = Lists.newArrayList();
    for (SubscriberIdentifier subscriberIdentifier : subscribers) {
      urls.add(subscriberIdentifier.getUri().toString());
    }
    return Response.newSuccess("Success", urls).toList();
  }

  @Override
  public List<Object> unregisterPublisher(String callerId, String topicName, String callerApi) {
    PublisherIdentifier publisherIdentifier =
        PublisherIdentifier.newFromStrings(callerId, callerApi, topicName);
    return Response.newSuccess("Success", master.unregisterPublisher(publisherIdentifier)).toList();
  }

  @Override
  public List<Object> registerSubscriber(String callerId, String topicName, String topicType,
      String callerApi) {
    List<PublisherIdentifier> publishers =
        master.registerSubscriber(
            SubscriberIdentifier.newFromStrings(callerId, callerApi, topicName), topicType);
    List<String> urls = Lists.newArrayList();
    for (PublisherIdentifier publisherIdentifier : publishers) {
      urls.add(publisherIdentifier.getSlaveUri().toString());
    }
    return Response.newSuccess("Success", urls).toList();
  }

  @Override
  public List<Object> unregisterSubscriber(String callerId, String topicName, String callerApi) {
    SubscriberIdentifier subscriberIdentifier =
        SubscriberIdentifier.newFromStrings(callerId, callerApi, topicName);
    return Response.newSuccess("Success", master.unregisterSubscriber(subscriberIdentifier))
        .toList();
  }

  @Override
  public List<Object> registerService(String callerId, String serviceName, String serviceApi,
      String callerApi) {
    ServiceIdentifier serviceIdentifier;
    try {
      serviceIdentifier = new ServiceIdentifier(new GraphName(serviceName), new URI(serviceApi));
    } catch (URISyntaxException e) {
      throw new RosRuntimeException(e);
    }
    master.registerService(serviceIdentifier);
    return Response.newSuccess("Success", 0).toList();
  }

  @Override
  public List<Object> unregisterService(String callerId, String serviceName, String serviceApi) {
    ServiceIdentifier serviceIdentifier;
    try {
      serviceIdentifier = new ServiceIdentifier(new GraphName(serviceName), new URI(serviceApi));
    } catch (URISyntaxException e) {
      throw new RosRuntimeException(e);
    }
    int result = master.unregisterService(serviceIdentifier) ? 1 : 0;
    return Response.newSuccess("Success", result).toList();
  }

  @Override
  public List<Object> setParam(String callerId, String key, Boolean value) {
    parameterServer.set(new GraphName(key), value);
    return Response.newSuccess("Success", null).toList();
  }

  @Override
  public List<Object> setParam(String callerId, String key, Integer value) {
    parameterServer.set(new GraphName(key), value);
    return Response.newSuccess("Success", null).toList();
  }

  @Override
  public List<Object> setParam(String callerId, String key, Double value) {
    parameterServer.set(new GraphName(key), value);
    return Response.newSuccess("Success", null).toList();
  }

  @Override
  public List<Object> setParam(String callerId, String key, String value) {
    parameterServer.set(new GraphName(key), value);
    return Response.newSuccess("Success", null).toList();
  }

  @Override
  public List<Object> setParam(String callerId, String key, List<?> value) {
    parameterServer.set(new GraphName(key), value);
    return Response.newSuccess("Success", null).toList();
  }

  @Override
  public List<Object> setParam(String callerId, String key, Map<?, ?> value) {
    parameterServer.set(new GraphName(key), value);
    return Response.newSuccess("Success", null).toList();
  }

  @Override
  public List<Object> getParam(String callerId, String key) {
    Object value = parameterServer.get(new GraphName(key));
    if (value == null) {
      return Response.newError("Parameter \"" + key + "\" is not set.", null).toList();
    }
    return Response.newSuccess("Success", value).toList();
  }

  @Override
  public List<Object> searchParam(String callerId, String key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Object> subscribeParam(String callerId, String callerApi, String key) {
    parameterServer.subscribe(new GraphName(key),
        SlaveIdentifier.newFromStrings(callerId, callerApi));
    Object value = parameterServer.get(new GraphName(key));
    if (value == null) {
      // Must return an empty map as the value of an unset parameter.
      value = new HashMap<String, Object>();
    }
    return Response.newSuccess("Success", value).toList();
  }

  @Override
  public List<Object> unsubscribeParam(String callerId, String callerApi, String key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Object> deleteParam(String callerId, String key) {
    parameterServer.delete(new GraphName(key));
    return Response.newSuccess("Success", null).toList();
  }

  @Override
  public List<Object> hasParam(String callerId, String key) {
    return Response.newSuccess("Success", parameterServer.has(new GraphName(key))).toList();
  }

  @Override
  public List<Object> getParamNames(String callerId) {
    Collection<GraphName> names = parameterServer.getNames();
    List<String> stringNames = Lists.newArrayList();
    for (GraphName name : names) {
      stringNames.add(name.toString());
    }
    return Response.newSuccess("Success", stringNames).toList();
  }
}
