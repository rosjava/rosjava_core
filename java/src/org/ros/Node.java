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

import com.google.common.base.Preconditions;

import org.apache.xmlrpc.XmlRpcException;
import org.ros.exceptions.RosInitException;
import org.ros.exceptions.RosNameException;
import org.ros.internal.namespace.GraphName;
import org.ros.internal.node.RemoteException;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.server.SlaveIdentifier;
import org.ros.internal.node.server.SlaveServer;
import org.ros.internal.topic.MessageDefinition;
import org.ros.internal.topic.PubSubFactory;
import org.ros.internal.topic.TopicDefinition;
import org.ros.logging.RosLog;
import org.ros.message.Message;
import org.ros.message.Time;
import org.ros.namespace.NameResolver;
import org.ros.namespace.Namespace;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 */
public class Node implements Namespace {
  /**
   * The node's context for name resolution and possibly other global
   * configuration issues (rosparam)
   */
  private final NodeContext context;
  /** The node's namespace name. */
  private final GraphName nodeName;
  /** Port on which the slave server will be initialized on. */
  private final int port;
  /** The master client, used for communicating with an existing master. */
  private MasterClient masterClient;
  /** ... */
  private SlaveServer slaveServer;
  /** Factory for generating internal Publisher and Subscriber instances. */
  private PubSubFactory pubSubFactory;

  /**
   * The log of this node. This log has the node's name inserted in each
   * message, along with ROS standard logging conventions.
   */
  private RosLog log;
  private boolean initialized;
  private Executor executor;

  /**
   * @param name
   * @param context
   * @throws RosNameException
   */
  public Node(String name, NodeContext context) throws RosNameException {
    Preconditions.checkNotNull(context);
    Preconditions.checkNotNull(name);
    this.context = context;
    nodeName = new GraphName(this.context.getResolver().resolveName(name));
    port = 0; // default port
    masterClient = null;
    slaveServer = null;
    log = new RosLog(nodeName.toString());
  }

  @Override
  public <MessageType extends Message> Publisher<MessageType> createPublisher(String topic_name,
      Class<MessageType> clazz) throws RosInitException, RosNameException {
    if (!initialized) {
      // kwc: this is not a permanent constraint. In the future, with more state
      // tracking, it is possible to allow Publisher handles to be created
      // before node.init().
      throw new RosInitException("Please call node.init()");
    }
    try {
      Preconditions.checkNotNull(masterClient);
      Preconditions.checkNotNull(slaveServer);
      Publisher<MessageType> pub = new Publisher<MessageType>(resolveName(topic_name), clazz);

      if (context.getHostName().equals("localhost") || context.getHostName().startsWith("127.0.0.")) {
        // If we are advertising as localhost, explicitly bind to loopback-only.
        // NOTE: technically 127.0.0.0/8 is loopback, not 127.0.0.1/24.
        pub.start(new InetSocketAddress(InetAddress.getByName("localhost"), context.getTcpRosPort()));
      } else {
        pub.start(new InetSocketAddress(context.getTcpRosPort()));
      }
      slaveServer.addPublisher(pub.publisher);
      return pub;
    } catch (IOException e) {
      throw new RosInitException(e);
    } catch (InstantiationException e) {
      throw new RosInitException(e);
    } catch (IllegalAccessException e) {
      throw new RosInitException(e);
    } catch (URISyntaxException e) {
      throw new RosInitException(e);
    } catch (RemoteException e) {
      throw new RosInitException(e);
    }
  }

  @Override
  public <MessageType extends Message> Subscriber<MessageType> createSubscriber(String topicName,
      final MessageListener<MessageType> callback, Class<MessageType> messageClass)
      throws RosInitException, RosNameException {

    if (!initialized) {
      // kwc: this is not a permanent constraint. In the future, with more state
      // tracking, it is possible to allow Publisher handles to be created
      // before node.init().
      throw new RosInitException("Please call node.init()");
    }

    try {
      Message m = messageClass.newInstance();
      String resolvedTopicName = resolveName(topicName);
      TopicDefinition topicDefinition = new TopicDefinition(resolvedTopicName,
          MessageDefinition.createFromMessage(m));

      org.ros.internal.topic.Subscriber<MessageType> subscriberImpl = pubSubFactory
          .createSubscriber(slaveServer, topicDefinition, messageClass);

      // Add the callback to the impl.
      subscriberImpl.addMessageListener(callback);
      // Create the user-facing Subscriber handle. This is little more than a
      // lightweight wrapper around the internal implementation so that we can
      // track callback references.
      Subscriber<MessageType> subscriber = new Subscriber<MessageType>(resolvedTopicName, callback,
          messageClass, subscriberImpl);
      return subscriber;

    } catch (IOException e) {
      throw new RosInitException(e);
    } catch (URISyntaxException e) {
      throw new RosInitException(e);
    } catch (RemoteException e) {
      throw new RosInitException(e);
    } catch (InstantiationException e) {
      throw new RosInitException(e);
    } catch (IllegalAccessException e) {
      throw new RosInitException(e);
    }

  }

  /**
   * @return The current time of the system, using rostime.
   */
  public Time currentTime() {
    // TODO: need to add in rostime (/Clock) implementation for simulated time
    // in the event that wallclock is not being used
    return Time.fromMillis(System.currentTimeMillis());
  }

  @Override
  public String getName() {
    return nodeName.toString();
  }

  /**
   * This starts up a connection with the master.
   * 
   * @throws RosInitException
   */
  public void init() throws RosInitException {
    try {
      masterClient = new MasterClient(context.getRosMasterUri());

      // Start up XML-RPC Slave server.
      SlaveIdentifier slaveIdentifier;
      try {
        slaveServer = new SlaveServer(nodeName.toString(), masterClient, context.getHostName(),
            port);
        slaveServer.start();
        slaveIdentifier = slaveServer.toSlaveIdentifier();
        log().debug(
            "Successfully initialized " + nodeName.toString() + " with:\n\tmaster @ "
                + masterClient.getRemoteUri().toString() + "\n\tListening on port: "
                + slaveServer.getUri().toString());
      } catch (MalformedURLException e) {
        throw new RosInitException("invalid ROS slave URI");
      } catch (URISyntaxException e) {
        throw new RosInitException("invalid ROS slave URI");
      }

      // Create factory and job queue for generating publisher/subscriber impls.
      executor = Executors.newCachedThreadPool();
      pubSubFactory = new PubSubFactory(slaveIdentifier, executor);

      initialized = true;
    } catch (IOException e) {
      throw new RosInitException(e);
    } catch (XmlRpcException e) {
      throw new RosInitException(e);
    }
  }

  /**
   * @return This is this nodes logger, and may be used to pump messages to
   *         rosout.
   */
  public RosLog log() {
    return log;
  }

  @Override
  public String resolveName(String name) throws RosNameException {
    NameResolver resolver = context.getResolver();
    String r = resolver.resolveName(getName(), name);
    log.debug("Resolved name " + name + " as " + r);
    return r;
  }

  public void shutdown() {
    // todo unregister all publishers, subscribers, etc.
    slaveServer.shutdown();
  }

}