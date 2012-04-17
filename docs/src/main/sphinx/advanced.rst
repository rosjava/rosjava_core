Advanced topics
===============

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


