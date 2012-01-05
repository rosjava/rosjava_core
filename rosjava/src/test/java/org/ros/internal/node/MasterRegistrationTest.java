// Copyright 2011 Google Inc. All Rights Reserved.

package org.ros.internal.node;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ros.address.AdvertiseAddress;
import org.ros.address.BindAddress;
import org.ros.internal.message.old_style.MessageSerializer;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.client.Registrar;
import org.ros.internal.node.parameter.ParameterManager;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.server.SlaveServer;
import org.ros.internal.node.service.ServiceManager;
import org.ros.internal.node.topic.DefaultPublisher;
import org.ros.internal.node.topic.TopicDefinition;
import org.ros.internal.node.topic.TopicManager;
import org.ros.message.MessageDefinition;
import org.ros.namespace.GraphName;
import org.ros.node.topic.CountDownPublisherListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MasterRegistrationTest {

  private final TopicDefinition topicDefinition;
  private final MessageSerializer<org.ros.message.std_msgs.String> messageSerializer;

  private ExecutorService executorService;
  private MasterServer masterServer;
  private MasterClient masterClient;
  private Registrar masterRegistration;
  private TopicManager topicManager;
  private ServiceManager serviceManager;
  private ParameterManager parameterManager;
  private SlaveServer slaveServer;
  private DefaultPublisher<org.ros.message.std_msgs.String> publisher;
  private CountDownPublisherListener<org.ros.message.std_msgs.String> publisherListener;

  public MasterRegistrationTest() {
    topicDefinition =
        TopicDefinition.newFromTopicName(new GraphName("/topic"), MessageDefinition.newFromStrings(
            org.ros.message.std_msgs.String.__s_getDataType(),
            org.ros.message.std_msgs.String.__s_getMessageDefinition(),
            org.ros.message.std_msgs.String.__s_getMD5Sum()));
    messageSerializer = new MessageSerializer<org.ros.message.std_msgs.String>();
  }

  @Before
  public void setup() {
	executorService = Executors.newCachedThreadPool();
    masterServer = new MasterServer(BindAddress.newPrivate(), AdvertiseAddress.newPrivate());
    masterServer.start();
    masterClient = new MasterClient(masterServer.getUri());
    masterRegistration = new Registrar(masterClient, executorService);
    topicManager = new TopicManager();
    serviceManager = new ServiceManager();
    parameterManager = new ParameterManager();
    slaveServer =
        new SlaveServer(new GraphName("/node"), BindAddress.newPrivate(),
            AdvertiseAddress.newPrivate(), BindAddress.newPrivate(),
            AdvertiseAddress.newPrivate(), masterClient, topicManager, serviceManager,
            parameterManager, executorService);
    slaveServer.start();
    masterRegistration.start(slaveServer.toSlaveIdentifier());

    publisher =
        new DefaultPublisher<org.ros.message.std_msgs.String>(topicDefinition, messageSerializer,
            executorService);
    publisherListener = CountDownPublisherListener.newDefault();
    publisher.addListener(publisherListener);
  }

  @After
  public void tearDown() {
    masterRegistration.shutdown();
    masterServer.shutdown();
    executorService.shutdown();
  }

  @Test
  public void testRegisterPublisher() throws InterruptedException {
    masterRegistration.publisherAdded(publisher);
    assertTrue(publisherListener.awaitMasterRegistrationSuccess(1, TimeUnit.SECONDS));
  }

  @Test
  public void testRegisterPublisherRetries() throws InterruptedException {
    masterServer.shutdown();
    masterRegistration.setRetryDelay(100, TimeUnit.MILLISECONDS);
    masterRegistration.publisherAdded(publisher);
    // Restart the MasterServer on the same port (hopefully still available).
    masterServer =
        new MasterServer(BindAddress.newPrivate(masterServer.getAdvertiseAddress().getPort()),
            AdvertiseAddress.newPrivate());
    masterServer.start();
    assertTrue(publisherListener.awaitMasterRegistrationSuccess(1, TimeUnit.SECONDS));
  }
}
