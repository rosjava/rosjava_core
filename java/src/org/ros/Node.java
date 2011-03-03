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
import org.ros.namespace.RosName;
import org.ros.namespace.RosNamespace;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 */
public class Node implements RosNamespace {
  /**
   * The node's context for name resolution and possibly other global
   * configuration issues (rosparam)
   */
  private final RosContext context;
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
   * The log of this node. This log has the node's name inserted in each
   * message, along with ROS standard logging conventions.
   */
  private RosLog log;
  private String masterUri;

  /**
   * @param name
   * @throws RosNameException
   */
  public Node(String name) throws RosNameException {
    this(name, Ros.getDefaultContext());
  }

  /**
   * @param name
   * @param context
   * @throws RosNameException
   */
  public Node(String name, RosContext context) throws RosNameException {
    Preconditions.checkNotNull(context);
    Preconditions.checkNotNull(name);
    this.context = context;
    rosName = new RosName(this.context.getResolver().resolveName(name));
    port = 0; // default port
    masterClient = null;
    slaveServer = null;
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

  /**
   * @param masterUri
   *          The uri of the rosmaster, typically "http://localhost:11311", or
   *          "http://remotehost.com:11311"
   * @param hostName
   *          the host
   * @throws RosInitException
   */
  public void init(String masterUri, String hostName) throws RosInitException {
    try {
      this.hostName = hostName;
      this.masterUri = masterUri;
      masterClient = new MasterClient(new URI(this.masterUri));
      slaveServer = new SlaveServer(rosName.toString(), masterClient, this.hostName, port);
      slaveServer.start();
      log().debug(
          "Successfully initiallized " + rosName.toString() + " with:\n\tmaster @ "
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
    String r = context.getResolver().resolveName(getName(), name);
    log.debug("Resolved name " + name + " as " + r);
    return r;
  }

}