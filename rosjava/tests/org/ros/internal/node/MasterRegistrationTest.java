// Copyright 2011 Google Inc. All Rights Reserved.

package org.ros.internal.node;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ros.internal.message.MessageDefinition;
import org.ros.internal.namespace.DefaultGraphName;
import org.ros.internal.node.address.AdvertiseAddress;
import org.ros.internal.node.address.BindAddress;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.parameter.ParameterManager;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.server.SlaveServer;
import org.ros.internal.node.service.ServiceManager;
import org.ros.internal.node.topic.Publisher;
import org.ros.internal.node.topic.TopicDefinition;
import org.ros.internal.node.topic.TopicManager;
import org.ros.message.MessageSerializer;

import java.util.concurrent.TimeUnit;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MasterRegistrationTest {

  private final TopicDefinition topicDefinition;
  private final MessageSerializer<org.ros.message.std_msgs.String> messageSerializer;

  private MasterServer masterServer;
  private MasterClient masterClient;
  private MasterRegistration masterRegistration;
  private TopicManager topicManager;
  private ServiceManager serviceManager;
  private ParameterManager parameterManager;
  private SlaveServer slaveServer;
  private Publisher<org.ros.message.std_msgs.String> publisher;

  public MasterRegistrationTest() {
    topicDefinition =
        TopicDefinition.create(new DefaultGraphName("/topic"), MessageDefinition.create(
            org.ros.message.std_msgs.String.__s_getDataType(),
            org.ros.message.std_msgs.String.__s_getMessageDefinition(),
            org.ros.message.std_msgs.String.__s_getMD5Sum()));
    messageSerializer = new MessageSerializer<org.ros.message.std_msgs.String>();
  }

  @Before
  public void setup() {
    masterServer = new MasterServer(BindAddress.createPrivate(0), AdvertiseAddress.createPrivate());
    masterServer.start();
    masterClient = new MasterClient(masterServer.getUri());
    masterRegistration = new MasterRegistration(masterClient);
    topicManager = new TopicManager();
    serviceManager = new ServiceManager();
    parameterManager = new ParameterManager();
    slaveServer =
        new SlaveServer(new DefaultGraphName("/node"), BindAddress.createPrivate(0),
            AdvertiseAddress.createPrivate(), BindAddress.createPrivate(0),
            AdvertiseAddress.createPrivate(), masterClient, topicManager, serviceManager,
            parameterManager);
    slaveServer.start();
    masterRegistration.start(slaveServer.toSlaveIdentifier());
    publisher = new Publisher<org.ros.message.std_msgs.String>(topicDefinition, messageSerializer);
  }

  @After
  public void tearDown() {
    masterRegistration.shutdown();
    masterServer.shutdown();
  }

  @Test
  public void testRegisterPublisher() throws InterruptedException {
    masterRegistration.publisherAdded(publisher);
    assertTrue(publisher.awaitRegistration(1, TimeUnit.SECONDS));
  }

  @Test
  public void testRegisterPublisherRetries() throws InterruptedException {
    masterServer.shutdown();
    masterRegistration.setRetryDelay(100, TimeUnit.MILLISECONDS);
    masterRegistration.publisherAdded(publisher);
    // Restart the MasterServer on the same port (hopefully still available).
    masterServer =
        new MasterServer(BindAddress.createPrivate(masterServer.getAdvertiseAddress().getPort()),
            AdvertiseAddress.createPrivate());
    masterServer.start();
    assertTrue(publisher.awaitRegistration(1, TimeUnit.SECONDS));
  }

}
