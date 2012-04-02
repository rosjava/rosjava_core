// Copyright 2011 Google Inc. All Rights Reserved.

package org.ros.internal.node;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ros.RosCore;
import org.ros.RosTest;
import org.ros.namespace.GraphName;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.CountDownPublisherListener;
import org.ros.node.topic.Publisher;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MasterRegistrationTest extends RosTest {

  private CountDownPublisherListener<std_msgs.String> publisherListener;
  private Publisher<std_msgs.String> publisher;

  public static int pickFreePort() throws IOException {
    ServerSocket server = new ServerSocket(0);
    int port = server.getLocalPort();
    server.close();
    return port;
  }

  @Test
  public void testRegisterPublisher() throws InterruptedException {
    publisherListener = CountDownPublisherListener.newDefault();
    nodeMainExecutor.execute(new NodeMain() {
      @Override
      public void onStart(Node node) {
        publisher = node.newPublisher("/topic", std_msgs.String._TYPE);
        publisher.addListener(publisherListener);
      }

      @Override
      public void onShutdownComplete(Node node) {
      }

      @Override
      public void onShutdown(Node node) {
      }

      @Override
      public GraphName getDefaultNodeName() {
        return new GraphName("/node");
      }
    }, nodeConfiguration);
    assertTrue(publisherListener.awaitMasterRegistrationSuccess(1, TimeUnit.SECONDS));
    publisher.shutdown();
    assertTrue(publisherListener.awaitMasterUnregistrationSuccess(1, TimeUnit.SECONDS));
  }

  @Test
  public void testRegisterPublisherRetries() throws InterruptedException, IOException,
      URISyntaxException {
    int port = pickFreePort();
    final RosCore rosCore = RosCore.newPrivate(port);
    publisherListener = CountDownPublisherListener.newDefault();
    // We cannot use rosCore.getUri() here because it hasn't started yet.
    nodeConfiguration.setMasterUri(new URI("http://localhost:" + port));
    nodeMainExecutor.execute(new NodeMain() {
      @Override
      public void onStart(Node node) {
        ((DefaultNode) node).getRegistrar().setRetryDelay(100, TimeUnit.MILLISECONDS);
        publisher = node.newPublisher("/topic", std_msgs.String._TYPE);
        publisherListener = CountDownPublisherListener.newDefault();
        publisher.addListener(publisherListener);
        try {
          assertTrue(publisherListener.awaitMasterRegistrationFailure(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        rosCore.start();
      }

      @Override
      public void onShutdownComplete(Node node) {
      }

      @Override
      public void onShutdown(Node node) {
      }

      @Override
      public GraphName getDefaultNodeName() {
        return new GraphName("/node");
      }
    }, nodeConfiguration);
    rosCore.awaitStart();
    assertTrue(publisherListener.awaitMasterRegistrationSuccess(1, TimeUnit.SECONDS));
    publisher.shutdown();
    assertTrue(publisherListener.awaitMasterUnregistrationSuccess(1, TimeUnit.SECONDS));
  }
}
