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
import org.ros.internal.node.RemoteException;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.server.SlaveServer;
import org.ros.logging.RosLog;
import org.ros.message.Message;
import org.ros.message.Time;
import org.ros.namespace.Namespace;
import org.ros.namespace.Resolver;
import org.ros.namespace.RosName;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 */
public class Node implements Namespace {
  /** The node's namespace name. */
  private final RosName rosName;
  private String hostName;
  /** Port on which the slave server will be initialized on. */
  private final int port;
  /** The master client, used for communicating with an existing master. */
  private MasterClient masterClient;
  /** ... */
  private SlaveServer slaveServer;

  /**
   * The log of this node. This log has the node_name inserted in each message,
   * along with ROS standard logging conventions.
   */
  private RosLog log;

  /**
   * Create a node, using the command line args which will be mined for ros
   * specific tags.
   * 
   * @param argv
   *          arg parsing
   * @param name
   *          the name, as in namespace of the node
   * @throws RosNameException
   */
  public Node(String argv[], String name) throws RosNameException {
    this(name);
  }

  public Node(String name) throws RosNameException {
    RosName tname = new RosName(name);
    if (!tname.isGlobal()) {
      rosName = new RosName("/" + name); // FIXME Resolve node name from
                                         // args remappings or pushdown,
                                         // what have you.
    } else {
      rosName = tname;
    }

    port = 0; // default port
    masterClient = null;
    slaveServer = null;
    // FIXME arg parsing
    log = new RosLog(rosName.toString());
  }

  @Override
  public <MessageType extends Message> Publisher<MessageType> createPublisher(String topic_name,
      Class<MessageType> clazz) throws RosInitException, RosNameException {
    try {
      Preconditions.checkNotNull(masterClient);
      Preconditions.checkNotNull(slaveServer);
      Publisher<MessageType> pub = new Publisher<MessageType>(resolveName(topic_name), clazz);
      pub.start(hostName);
      slaveServer.addPublisher(pub.publisher);
      return pub;
    } catch (IOException e) {
      throw new RosInitException(e);
    } catch (InstantiationException e) {
      throw new RosInitException(e);
    } catch (IllegalAccessException e) {
      throw new RosInitException(e);
    } catch (RemoteException e) {
      throw new RosInitException(e);
    } catch (URISyntaxException e) {
      throw new RosInitException(e);
    }
  }

  @Override
  public <MessageType extends Message> Subscriber<MessageType> createSubscriber(String topic_name,
      final MessageListener<MessageType> callback, Class<MessageType> clazz)
      throws RosInitException, RosNameException {

    try {
      Subscriber<MessageType> sub = new Subscriber<MessageType>(getName(), resolveName(topic_name),
          clazz);
      sub.init(slaveServer, callback);
      return sub;
    } catch (InstantiationException e) {
      throw new RosInitException(e);
    } catch (IllegalAccessException e) {
      throw new RosInitException(e);
    } catch (IOException e) {
      throw new RosInitException(e);
    } catch (URISyntaxException e) {
      throw new RosInitException(e);
    }

  }

  /**
   * @return The current time of the system, using rostime.
   */
  public Time currentTime() {
    // TODO: need to add in rostime implementation for simulated time in the
    // event that wallclock is not being used
    return Time.fromMillis(System.currentTimeMillis());
  }

  @Override
  public String getName() {
    return rosName.toString();
  }

  /**
   * This starts up a connection with the master and gets the juices flowing
   * 
   * @throws RosInitException
   */
  public void init() throws RosInitException {

    try {
      init(Ros.getMasterUri().toString(), Ros.getHostName());
    } catch (URISyntaxException e) {
      throw new RosInitException(e);
    }
  }

  public void init(String masteruri, String hostname) throws RosInitException {
    try {
      this.hostName = hostname;
      masterClient = new MasterClient(new URI(masteruri));
      slaveServer = new SlaveServer(rosName.toString(), masterClient, this.hostName, port);
      slaveServer.start();
      log().debug(
          "Successfully initiallized " + rosName.toString() + " with:\n\tmaster @"
              + masterClient.getRemoteUri().toString() + "\n\tListening on port: "
              + slaveServer.getUri().toString());
    } catch (IOException e) {
      throw new RosInitException(e);
    } catch (XmlRpcException e) {
      throw new RosInitException(e);
    } catch (URISyntaxException e) {
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
    Resolver resolver = Resolver.getDefault();
    String r = resolver.resolveName(getName(), name);
    log.debug("Resolved name " + name + " as " + r);
    return r;
  }

}