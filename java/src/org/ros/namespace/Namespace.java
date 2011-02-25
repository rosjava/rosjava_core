package org.ros.namespace;

import org.ros.Callback;

import org.ros.Publisher;
import org.ros.Subscriber;
import org.ros.exceptions.RosInitException;
import org.ros.message.Message;

/**
 * Interface for the ROS namespace spec.
 * 
 */
public interface Namespace {

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
   * @throws RosInitException 
   */
  public <MessageT extends Message> Publisher<MessageT> createPublisher(String topic_name,
      Class<MessageT> clazz) throws RosInitException;

  /**
   * 
   * @param <MessageT>
   *          The message type to create the Subscriber for.
   * @param topic_name
   *          The topic name to be subscribed to. This may be "bar" "/foo/bar"
   *          "~my" and will be auto resolved.
   * @param callback
   *          The callback to be registered to this subscription. This will be
   *          called asynchronously any time that a message is published on the
   *          topic.
   * @param clazz
   *          The class of the message type that is being published on the
   *          topic.
   * @return A handle to a Subscriber that may be used to subscribe messages of
   *         type MessageT.
   * @throws RosInitException
   *           The subscriber may fail if the Ros system has not been
   *           initialized or other wackyness. TODO specify exceptions that
   *           might be thrown here.
   */
  public <MessageT extends Message> Subscriber<MessageT> createSubscriber(String topic_name,
      Callback<MessageT> callback, Class<MessageT> clazz) throws RosInitException;

  /**
   * @return The fully resolved name of this namespace, e.g. "/foo/bar/boop".
   */
  public String getName();

  /**
   * Resolve the given name, using ROS conventions, into a full ROS namespace
   * name. Will be relative to the current namespace unless prepended by a "/"
   * 
   * @param name
   *          The name to resolve.
   * @return Fully resolved ros namespace name.
   */
  public String resolveName(String name);
}