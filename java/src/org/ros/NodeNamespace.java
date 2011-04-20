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

package org.ros;

import org.ros.exceptions.RosInitException;
import org.ros.internal.namespace.GraphName;
import org.ros.internal.node.service.ServiceClient;
import org.ros.internal.node.service.ServiceDefinition;
import org.ros.internal.node.service.ServiceIdentifier;
import org.ros.internal.node.service.ServiceResponseBuilder;
import org.ros.internal.node.service.ServiceServer;
import org.ros.message.Message;
import org.ros.namespace.NameResolver;
import org.ros.namespace.Namespace;
import org.ros.namespace.NodeNameResolver;

import java.net.MalformedURLException;
import java.net.URI;

/**
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class NodeNamespace implements Namespace {

  private Node node;
  private String namespace;

  public NodeNamespace(Node node, String namespace) {
    this.namespace = GraphName.canonicalizeName(namespace);
    this.node = node;
  }

  private String pushNameIntoNamespace(String name) {
    return NameResolver.join(this.namespace, name);
  }

  @Override
  public <MessageType extends Message> Publisher<MessageType> createPublisher(String topicName,
      Class<MessageType> messageClass) throws RosInitException {
    String pushedName = pushNameIntoNamespace(topicName);
    return node.createPublisher(pushedName, messageClass);
  }

  @Override
  public <MessageType> Subscriber<MessageType> createSubscriber(String topicName,
      MessageListener<MessageType> messageCallback, Class<MessageType> messageClass)
      throws RosInitException {
    String pushedName = pushNameIntoNamespace(topicName);
    return node.createSubscriber(pushedName, messageCallback, messageClass);
  }

  @Override
  public ParameterClient createParameterClient() {
    try {
      return ParameterClient.createFromNamespace(this);
    } catch (MalformedURLException e) {
      // Convert to unchecked exception as this really shouldn't happen as URL
      // is already validated.
      throw new RuntimeException("MalformedURLException should not have been thrown: " + e);
    }
  }

  @Override
  public String getName() {
    return namespace;
  }

  @Override
  public String resolveName(String name) {
    return getResolver().resolveName(name);
  }

  @Override
  public NameResolver getResolver() {
    NodeNameResolver resolver = node.getResolver();
    return resolver.createResolver(namespace);
  }

  @Override
  public <RequestType, ResponseType> ServiceServer createServiceServer(
      ServiceDefinition serviceDefinition,
      ServiceResponseBuilder<RequestType, ResponseType> responseBuilder) throws Exception {
    // TODO Auto-generated method stub
    throw new RuntimeException("not implemented");
  }

  @Override
  public <ResponseMessageType extends Message> ServiceClient<ResponseMessageType> createServiceClient(
      ServiceIdentifier serviceIdentifier, Class<ResponseMessageType> responseMessageClass) {
    // TODO(kwc) reimplement when new service API is available. This shows some
    // of the bad coupling that the service identifier/definition objects
    // creates.
    ServiceDefinition oldDefinition = serviceIdentifier.getServiceDefinition();
    String pushedName = NameResolver.join(new GraphName(namespace), oldDefinition.getName());
    ServiceDefinition pushedDefinition = new ServiceDefinition(new GraphName(pushedName),
        oldDefinition.getType(), oldDefinition.getMd5Checksum());
    ServiceIdentifier pushedIdentifier = new ServiceIdentifier(serviceIdentifier.getUri(),
        pushedDefinition);
    return node.createServiceClient(pushedIdentifier, responseMessageClass);
  }

  @Override
  public URI getMasterUri() {
    return node.getMasterUri();
  }

}
