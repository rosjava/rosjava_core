Getting started
===============

Creating a new Java package
---------------------------

Creating nodes
--------------

Messages
--------

To import a specific message, you must first declare a dependency on that
message's package in your :roswiki:`Manifest` file and your build.gradle file.

Note that this redundancy will be removed in the near future.

This will add the message's jar file to your classpath.

Next, to use a message such as :rosmsg:`sensor_msgs/PointCloud2`, simply add an
import.  Rather than instantiate the message directly, however, it's preferred
that you use a :javadoc:`org.ros.message.MessageFactory` instead. This helps
allow the underlying message implementation to change in the future. ::

  import  org.ros.message.sensor_msgs.PointCloud;

  ...

  Node node;

  ...

  PointCloud2 msg = node.getMessageFactory()
      .newMessage("sensor_msgs/PointCloud");

If you want to use messages that you define:

- create a new package for those messages (e.g. my_msgs)
- add a dependency on the new package as described above
- ``rosrun rosjava_bootstrap install_generated_messages.py my_package``

Messages as BLOBs (Advanced)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

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
      .newMessageDeserializer("sensor_msgs/PointCloud")
          .deserialize(buffer);

Publishers and subscribers
--------------------------

Services
--------

Parameters
----------

rosjava offers full access to the ROS :roswiki:`Parameter Server`. The
:roswiki:`Parameter Server` is a shared dictionary of configuration parameters
accessible to all the nodes at runtime. It is meant to store configuration
parameters that are easy to inspect and modify.

Parameters are accessible via :javadoc:`org.ros.node.parameter.ParameterTree`\s
(provided by :javadoc:`org.ros.node.Node`\s). ::

  ParameterTree params = node.newParameterTree();

Accessing Parameters
~~~~~~~~~~~~~~~~~~~~

The :javadoc:`org.ros.node.parameter.ParameterTree` API allows you to set and
query lists, maps, and single objects of integers, strings and floats.

Unlike typical ROS :roswiki:`Client Libraries`, rosjava requires that the type
of the parameter be known when you retrieve it. If the actual parameter type
doesn't match the expected type, an exception will be thrown. ::

  boolean foo = params.getBoolean("/foo");
  int bar = params.getInteger("/bar", 42 /* default value */);
  double baz = params.getDouble("/foo/baz");

  params.set("/bloop", "Hello, world!");
  String helloWorld = params.getString("/bloop");

  List<Integer> numbers = params.getList("/numbers");
  Map<String, String> strings = params.getMap("/strings");

As with other ROS client libraries, it is possible to retrieve a subtree of
parameters. However, you will be responsible for casting the values to their
appropriate types. ::

  Map<String, Object> subtree = params.getMap("/subtree");

Using a ParameterListener
~~~~~~~~~~~~~~~~~~~~~~~~~

It is also possible to subscribe to a particular parameter using a
:javadoc:`org.ros.node.parameter.ParameterListener`. Note that this does not
work for parameter subtrees. ::

  params.addParameterListener("/foo/bar", new ParameterListener() {
    @Override
    public void onNewValue(Object value) {
      ...
    }
  });

Currently, ParameterListeners are not generic. Instead, you are responsible for casting value appropriately.

Logging
-------

The logging interface for rosjava is accessed through
:javadoc:`org.ros.node.Node` objects via the
:javadoc:`org.ros.node.Node#getLog()` method. This object returns an `Apache
Commons Log`_ object which handles the debug, info, error, warning, and fatal
logging outputs for ROS. ::

  node.getLog.debug("debug message");
  node.getLog.info(" informative message");

  node.getLog.warn("warning message");

  //report an error message
  node.getLog.error("error message");

  //error message with an exception
  //so that it can print the stack trace
  node.getLog.error("error message", e);

  node.fatal("message informing user of a fatal occurrence");

.. _Apache Commons Log: http://commons.apache.org/logging/commons-logging-1.1.1/apidocs/index.html

Exceptions
----------

Running nodes
-------------

