package org.ros.topic;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.communication.Message;
import org.ros.transport.Header;
import org.ros.transport.HeaderFields;
import org.ros.transport.OutgoingMessageQueue;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public abstract class Publisher extends Topic {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(Publisher.class);

  private final OutgoingMessageQueue out;
  private final ImmutableMap<String, String> header;
  private final List<SubscriberDescription> subscribers;

  public Publisher(TopicDescription topicDescription, String hostname) {
    super(topicDescription, hostname);
    out = new OutgoingMessageQueue();
    header = new ImmutableMap.Builder<String, String>()
        .put(HeaderFields.TYPE, topicDescription.getMessageType())
        .put(HeaderFields.MD5_CHECKSUM, topicDescription.getMd5Checksum())
        .build();
    subscribers = Lists.newArrayList();
  }

  @Override
  public void start(int port) throws IOException {
    super.start(port);
    out.start();
  }

  @Override
  public void shutdown() {
    super.shutdown();
    out.shutdown();
  }

  protected void publish(Message message) {
    out.add(message);
  }

  protected void onNewConnection(Socket socket) {
    try {
      handshake(socket);
      Preconditions.checkState(socket.isConnected());
      out.addSocket(socket);
    } catch (IOException e) {
      log.error("Failed to accept connection.", e);
    }
  }

  private void handshake(Socket socket) throws IOException {
    Map<String, String> incomingHeader = Header.readHeader(socket.getInputStream());
    if (DEBUG) {
      log.info("Incoming handshake header: " + incomingHeader);
      log.info("Expected handshake header: " + header); 
    }
    Preconditions.checkState(incomingHeader.get(HeaderFields.TYPE).equals(
        header.get(HeaderFields.TYPE)));
    Preconditions.checkState(incomingHeader.get(HeaderFields.MD5_CHECKSUM).equals(
        header.get(HeaderFields.MD5_CHECKSUM)));
    SubscriberDescription subscriber = SubscriberDescription.CreateFromHeader(incomingHeader);
    subscribers.add(subscriber);
    Header.writeHeader(header, socket.getOutputStream());
  }
}
