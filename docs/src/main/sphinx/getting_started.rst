Getting started
===============

Creating a new Java package
---------------------------

The following steps will be wrapped up in package creation script in the near
future.

#. Use ``roscreate-pkg`` to create a new package. See
   :roswiki:`ROS/Tutorials/CreatingPackage`.
#. Remove the generated ``Makefile`` and ``CMakeLists.txt`` files.
#. Add a new build.gradle file (see :ref:`build-gradle-example`).
#. Put your your Java sources in ``src/main/java`` and your tests in ``src/test/java``.
#. Assuming you have already completed :doc:`building`, you can now call
   ``gradle build`` to build and test your package.

.. _build-gradle-example:

build.gradle example
~~~~~~~~~~~~~~~~~~~~

.. code-block:: groovy

  apply plugin: 'java'

  // The Maven plugin is only required if your package is used as a library.
  apply plugin: 'maven'

  // The Application plugin and mainClassName attribute are only required if
  // your package is used as a binary.
  apply plugin: 'application'
  mainClassName = 'org.ros.RosRun'

  sourceCompatibility = 1.6
  targetCompatibility = 1.6

  repositories {
    mavenLocal()
    maven {
      url 'http://robotbrains.hideho.org/nexus/content/groups/ros-public'
    }
  }

  version = 0.0.0-SNAPSHOT
  group = ros.my_stack

  dependencies {
    compile 'ros.rosjava_core:rosjava:0.0.0-SNAPSHOT'
  }

If you use the `Maven plugin`_, you may use ``gradle install`` to install your
package to your local .m2 cache and make it available to other rosjava packages
on your system.

If you use the `Application plugin`_, you may use ``gradle installApp`` to create
an executable wrapper for your package.

See the Gradle `Java tutorial`_ for more details.

.. _Maven plugin: http://gradle.org/docs/current/userguide/maven_plugin.html
.. _Application plugin: http://gradle.org/docs/current/userguide/application_plugin.html
.. _Java tutorial: http://gradle.org/docs/current/userguide/tutorial_java_projects.html

Creating nodes
--------------

Typically ROS nodes are synonymous with processes. In rosjava, however, nodes
are more like :roswiki:`nodelets` in that many nodes can run in a single
process, the Java VM.

Users, like yourself, do not create :javadoc:`org.ros.node.Node`\s. Instead,
programs are defined as implementations of :javadoc:`org.ros.node.NodeMain`
which are executed by the aptly named :javadoc:`org.ros.node.NodeMainExecutor`.

Let's consider the following simple :javadoc:`org.ros.node.NodeMain`
implementation::

  :linenos:

  package org.ros.tutorials.pubsub;

  import org.ros.concurrent.CancellableLoop;
  import org.ros.namespace.GraphName;
  import org.ros.node.Node;
  import org.ros.node.NodeMain;
  import org.ros.node.topic.Publisher;

  public class Talker implements NodeMain {

    @Override
    public GraphName getDefaultNodeName() {
      return new GraphName("rosjava_tutorial_pubsub/talker");
    }

    @Override
    public void onStart(final Node node) {
      final Publisher<std_msgs.String> publisher =
          node.newPublisher("chatter", std_msgs.String._TYPE);
      // This CancellableLoop will be canceled automatically when the Node shuts
      // down.
      node.executeCancellableLoop(new CancellableLoop() {
        private int sequenceNumber;

        @Override
        protected void setup() {
          sequenceNumber = 0;
        }

        @Override
        protected void loop() throws InterruptedException {
          std_msgs.String str = publisher.newMessage();
          str.setData("Hello world! " + sequenceNumber);
          publisher.publish(str);
          sequenceNumber++;
          Thread.sleep(1000);
        }
      });
    }

    @Override
    public void onShutdown(Node node) {
    }

    @Override
    public void onShutdownComplete(Node node) {
    }
  }

The :javadoc:`org.ros.node.NodeListener#onStart(org.ros.node.Node)` method
begins on line number 17.  As the name suggests, this is the entry point for
your program. The :javadoc:`org.ros.node.Node` parameter is the factory we use
to build things like :javadoc:`org.ros.topic.Publisher`\s and
:javadoc:`org.ros.topic.Subscriber`\s.

In this example, we create a publisher for the chatter topic. That should feel
relatively familiar if you're a ROS veteran. The
:javadoc:`org.ros.topic.Publisher` publishes ``std_msgs.String`` messages to
the ``chatter`` topic.

Line 22 will probably feel unfamailiar even to ROS veterans. This is one
example of rosjava's asynchornous API. The intent of our Talker class is to
publish a hello world message to anyone who will listen once per second. One
way to accomplish this is to publish the message and sleep in a loop. However,
we don't want to block the
:javadoc:`org.ros.node.NodeListener#onStart(org.ros.node.Node)` method. So, we
create a :javadoc:`org.ros.concurrent.CancellableLoop` and ask the
:javadoc:`org.ros.node.Node` to execute it. The loop will be interrupted
automatically when the :javadoc:`org.ros.node.Node` exits.

Executing nodes
---------------

When packaging your application into jar, you can use :javadoc:`org.ros.RosRun`
as the main class. :javadoc:`org.ros.RosRun` provides a
:javadoc:`org.ros.node.NodeMainExecutor` and a command line interface that will
be familiar to ROS veterans. For example, the following steps will build and
execute the :javadoc:`org.ros.rosjava_tutorial_pubsub.Talker` and
:javadoc:`org.ros.rosjava_tutorial_pubsub.Listener` nodes in separate
processes:

#. roscd rosjava_tutorial_pubsub
#. ../gradlew installApp
#. roscore &
#. ./build/rosjava_tutorial_pubsub/bin/rosjava_tutorial_pubsub org.ros.rosjava_tutorial_pubsub.Talker &
#. ./build/rosjava_tutorial_pubsub/bin/rosjava_tutorial_pubsub org.ros.rosjava_tutorial_pubsub.Listener

.. note:: The above example launches roscore and the Talker node in the background. You could instead launch each in a separate terminal.

.. note:: You may notice that rosrun cannot find the installed executable. This is a known issue that will be addressed in the future.

At this point, you should see the familiar "Hello, world!" messages start
appearing in your terminal. You can configure the executed nodes from the
command line in the same way you would any other ROS executable. For example,
the following commands will remap the default topic /chatter to /foo.

#. ./build/rosjava_tutorial_pubsub/bin/rosjava_tutorial_pubsub org.ros.rosjava_tutorial_pubsub.Talker chatter:=/foo &
#. ./build/rosjava_tutorial_pubsub/bin/rosjava_tutorial_pubsub org.ros.rosjava_tutorial_pubsub.Listener chatter:=/foo

See :roswiki:`Remapping%20Arguments` for more information.

As with all ROS nodes, rosjava nodes must connect to a :roswiki:`Master` in
order to function. However, unlike other client library implementations,
rosjava also provides its own :javadoc:`org.ros.RosCore` implementation. This
enables ROS to function in a pure Java installation. For example, you can
operate a complete ROS graph on a stock (i.e. non-rooted) Android phone.

Messages
--------

Messages are defined as interfaces. Since this makes it impossible to
instantiate the message directly, it's necessary to use a
:javadoc:`org.ros.message.MessageFactory`. This indirection allows the
underlying message implementation to change in the future. ::

  Node node;

  ...

  PointCloud2 msg = node.getTopicMessageFactory()
      .newMessage(sensor_msgs.PointCloud._TYPE);

If you want to use messages that you define:

- create a new package for those messages (e.g. my_msgs)
- ensure that my_msgs is in your ROS_PACKAGE_PATH (see :roswiki:`EnvironmentVariables`)
- reinstall rosjava_messages (see :doc:`building`)

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
      .newMessageDeserializer(sensor_msgs.PointCloud._TYPE)
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

