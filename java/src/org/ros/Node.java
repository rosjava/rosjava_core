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

import org.ros.message.Time;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.ros.exceptions.RosInitException;
import org.ros.internal.node.RemoteException;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.server.SlaveServer;
import org.ros.message.Message;
import org.ros.namespace.Namespace;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author "Ethan Rublee ethan.rublee@gmail.com"
 * 
 */
public class Node implements Namespace {
  private MasterClient master = null;
  private SlaveServer slave = null;

  private String name = "node";
  private int port = 0; // default port

  /**
   * 
   * @param argv
   *          arg parsing
   * @param name
   *          the name, as in namespace of the node
   */
  public Node(String argv[], String name) {
    this.name = name;
    log = LogFactory.getLog(this.name);
  }

  /**
   * This starts up a connection with the master and gets the juices flowing
   * 
   * @throws RosInitException
   * @throws URISyntaxException 
   */
  public void init() throws RosInitException {
    try {
      master = new MasterClient(Ros.getMasterUri());
      slave = new SlaveServer(name, master, Ros.getHostName(), port);
      slave.start();
    } catch (IOException e) {
      throw new RosInitException(e.getMessage());
    } catch (XmlRpcException e) {
      throw new RosInitException(e.getMessage());
    } catch (URISyntaxException e) {
      throw new RosInitException(e.getMessage());
    }
  }

  @Override
  public <MessageT extends Message> Publisher<MessageT> createPublisher(String topic_name,
      Class<MessageT> clazz) throws RosInitException {
    try {
      Publisher<MessageT> pub = new Publisher<MessageT>(resolveName(topic_name), clazz);
      pub.start();
      slave.addPublisher(pub.publisher);
      return pub;
    } catch (IOException e) {
      throw new RosInitException(e.getMessage());
    } catch (InstantiationException e) {
      throw new RosInitException(e.getMessage());
    } catch (IllegalAccessException e) {
      throw new RosInitException(e.getMessage());
    } catch (RemoteException e) {
      throw new RosInitException(e.getMessage());
    } catch (URISyntaxException e) {
      throw new RosInitException(e.getMessage());
    }
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String resolveName(String name) {
    // TODO make better
    return this.name + "/" + name;
  }

  @Override
  public <MessageT extends Message> Subscriber<MessageT> createSubscriber(String topic_name,
      final Callback<MessageT> callback, Class<MessageT> clazz) throws RosInitException {

    try {
      Subscriber<MessageT> sub = new Subscriber<MessageT>(getName(), resolveName(topic_name), clazz);
      sub.init(slave, callback);
      return sub;
    } catch (InstantiationException e) {
      throw new RosInitException(e.getMessage());
    } catch (IllegalAccessException e) {
      throw new RosInitException(e.getMessage());
    } catch (IOException e) {
      throw new RosInitException(e.getMessage());
    } catch (URISyntaxException e) {
      throw new RosInitException(e.getMessage());
    }

  }

  /**
   * @param message
   */
  public void logDebug(Object message) {
    log.debug(message);
  }

  /**
   * @param message
   */
  public void logWarn(Object message) {
    log.warn(message);
  }

  /**
   * @param message
   */
  public void logInfo(Object message) {
    log.info(message);
  }

  /**
   * @param message
   */
  public void logError(Object message) {
    log.warn(message);
  }

  /**
   * @param message
   */
  public void logFatal(Object message) {
    log.fatal(message);
  }

  private Log log;

  public Time currentTime() {
    //TODO: need to add in rostime implementation for simulated time in the event that wallclock is not being used
    return Time.fromMillis(System.currentTimeMillis());
  }

}