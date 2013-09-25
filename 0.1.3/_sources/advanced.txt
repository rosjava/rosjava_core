Advanced topics
===============

Listeners
---------

Because rosjava provides a primarily asynchronous API, many classes which allow
you to provide event listeners. For example,
:javadoc:`org.ros.node.topic.PublisherListener`\s allow you to react to
lifecycle events of a :javadoc:`org.ros.node.topic.Publisher`. The snippet
below adds a :javadoc:`org.ros.node.topic.PublisherListener` that will log a
warning message if the :javadoc:`org.ros.node.topic.Publisher` fails to
register with the master. ::

  Node node;
  Publisher<std_msgs.String> publisher;

  ...

  publisher.addListener(new DefaultPublisherListener() {
    @Override
    public void onMasterRegistrationFailure(Publisher<std_msgs.String> registrant) {
      node.getLog().warn("Publisher failed to register: " + registrant);
    }
  });

Messages as BLOBs
-----------------

If you need to deserialize a ROS message BLOB, it is important to remember that
Java is a big endian virtual machine. When supplying the ``ByteBuffer`` to the
:javadoc:`org.ros.message.MessageDeserializer`, make sure that order is set to
little endian. ::

  Node node;
  byte[] messageData;

  ...

  ByteBuffer buffer = ByteBuffer.wrap(messageData);
  buffer.order(ByteOrder.LITTLE_ENDIAN);
  PointCloud2 msg = node.getMessageSerializationFactory()
      .newMessageDeserializer(sensor_msgs.PointCloud._TYPE)
          .deserialize(buffer);

