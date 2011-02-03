package org.ros.node.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.xmlrpc.XmlRpcException;
import org.ros.node.xmlrpc.SlaveImpl;
import org.ros.topic.Publisher;
import org.ros.topic.Subscriber;
import org.ros.topic.SubscriberDescription;
import org.ros.transport.ProtocolDescription;
import org.ros.transport.TcpRosDescription;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

public class Slave extends Node {

  private final Map<String, Publisher> publishers;

  private URL masterUrl;

  public Slave() {
    publishers = Maps.newConcurrentMap();
  }

  public void start(int port) throws XmlRpcException, IOException {
    super.start(port, org.ros.node.xmlrpc.SlaveImpl.class, new SlaveImpl(this));
  }

  public void addPublisher(Publisher publisher) {
    String topic = publisher.getTopicName();
    publishers.put(topic, publisher);
  }

  public void addSubscriber(Subscriber subscriber) {
    throw new UnsupportedOperationException();
  }

  public List<Object> getBusStats(String callerId) {
    throw new UnsupportedOperationException();
  }

  public List<Object> getBusInfo(String callerId) {
    throw new UnsupportedOperationException();
  }

  public void setMasterUri(URL url) {
    masterUrl = url;
  }

  public URL getMasterUri(String callerId) throws MalformedURLException {
    return masterUrl;
  }

  public void shutdown(String callerId, String message) {
    super.shutdown();
  }

  public List<Object> getPid(String callerId) {
    throw new UnsupportedOperationException();
  }

  public List<SubscriberDescription> getSubscriptions() {
    throw new UnsupportedOperationException();
  }

  public List<Publisher> getPublications() {
    return ImmutableList.copyOf(publishers.values());
  }

  public List<Object> paramUpdate(String callerId, String parameterKey, String parameterValue) {
    throw new UnsupportedOperationException();
  }

  public List<Object> publisherUpdate(String callerId, String topic, Collection<String> publishers) {
    throw new UnsupportedOperationException();
  }

  public ProtocolDescription requestTopic(String topic, Set<String> protocols) {
    Preconditions.checkArgument(publishers.containsKey(topic));
    // TODO(damonkohler): Pull out list of supported protocols.
    Preconditions.checkArgument(protocols.contains("TCPROS"));
    return new TcpRosDescription(publishers.get(topic).getAddress());
  }
}
