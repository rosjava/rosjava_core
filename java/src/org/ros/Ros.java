package org.ros;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.xmlrpc.XmlRpcException;
import org.ros.Ros.Subscriber.Callback;
import org.ros.internal.node.RemoteException;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.server.SlaveServer;
import org.ros.internal.topic.MessageDefinition;
import org.ros.internal.topic.TopicDefinition;
import org.ros.message.Message;

public class Ros {
  /**
   * 
   * @author erublee
   * 
   * @param <MessageT>
   */
  public static class Publisher<MessageT extends Message> {

    Publisher(String topic_name, Class<MessageT> clazz) {
      this.topic_name = topic_name;
      this.clazz = clazz;
    }

    public void publish(MessageT m) {
      publisher.publish(m);
    }

    void start() {
      try {
        TopicDefinition topicDefinition;
        topicDefinition = new TopicDefinition(topic_name,
            MessageDefinition.createFromMessage((Message) clazz.newInstance()));
        publisher = new org.ros.internal.topic.Publisher(topicDefinition, ros.getHostName(), 0);
        publisher.start();
      } catch (IOException e) {
        e.printStackTrace();
      } catch (InstantiationException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      } catch (IllegalAccessException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
    }

    org.ros.internal.topic.Publisher publisher;
    String topic_name;
    // deal with type erasure for generics
    Class<MessageT> clazz;
  }

  public static class Subscriber<MessageT extends Message> {
    public static interface Callback<MessageT extends Message> {
      void onRecieve(MessageT m);
    }
  }

  /**
   * Interface for the ros namespace spec.
   * 
   */
  public static interface Namespace {

    /**
     * @param <MessageT>
     *          The message type to create the publisher for
     * @param topic_name
     *          the topic name, will be pushed down under this namespace unless
     *          '/' is prepended
     * @param clazz
     *          the Class object used to deal with type eraser
     * @return A handle to a publisher that may be used to publish messages of
     *         type MessageT
     */
    public <MessageT extends Message> Publisher<MessageT> createPublisher(String topic_name,
        Class<MessageT> clazz);

    /**
     * 
     * @param <MessageT>
     *          The message type to create the Subscriber for
     * @param topic_name
     *          the topic name, global assumed?//TODO verify
     * @param clazz
     *          the Class object used to deal with type eraser
     * @return A handle to a Subscriber that may be used to subscribe messages
     *         of type MessageT
     */
    public <MessageT extends Message> Subscriber<MessageT> createSubscriber(String topic_name,
        Subscriber.Callback<MessageT> callback, Class<MessageT> clazz);

    public String getName();

    public String resolveName(String name);
  }

  /**
   * 
   * @author erublee
   * 
   */
  public static class Node implements Namespace {
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
    }

    /**
     * This starts up a connection with the master and gets the juices flowing
     */
    public void init() {
      master = new MasterClient(ros.getMaterUri());
      slave = new SlaveServer(name, master, ros.getHostName(), port);
      try {
        slave.start();
      } catch (XmlRpcException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    /**
     * Spin for one update cycle? Maybe these are unnecessary for rosjava?
     */
    void spinOnce() {
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      // TODO spin once, process events?
      // slave.processEvents();
    }

    /**
     * spin indefinitely and process events
     */
    void spin() {
      while (true)
        spinOnce(); // TODO update rate?
    }

    @Override
    public <MessageT extends Message> Publisher<MessageT> createPublisher(String topic_name,
        Class<MessageT> clazz) {
      Publisher<MessageT> pub = new Publisher<MessageT>(resolveName(topic_name), clazz);
      pub.start();
      try {
        slave.addPublisher(pub.publisher);
      } catch (MalformedURLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (RemoteException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      return pub;
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
        Callback<MessageT> callback, Class<MessageT> clazz) {
      // TODO Auto-generated method stub
      return null;
    }

    public boolean isShutdown() {
      // TODO Auto-generated method stub
      return false;
    }

  }

  /**
   * Alias class for some global convenience functions ?
   * 
   * @author erublee
   * 
   */
  public static class ros {
    /**
     * Get the master uri, maybe from environment or else where?
     * 
     * @return
     */
    public static URL getMaterUri() {
      try {
        return new URL("http://localhost:11311/");
      } catch (MalformedURLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        return null;
      }
    }

    /**
     * Finds the environment's host name, will look in TODO ROS_HOSTNAME
     * 
     * @return the undecorated hostname, e.g. 'localhost'
     */
    public static String getHostName() {
      // TODO better resolution? from env
      return "localhost";
    }

    protected static void logi(String string) {
      // TODO ros logging
      System.out.println(string);
    }

    public static boolean isShutdown() {
      // TODO Auto-generated method stub
      return false;
    }
  }

}
