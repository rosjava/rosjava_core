.. _getting_started:

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

  version = '0.0.0-SNAPSHOT'
  group = 'ros.my_stack'

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
are more like :roswiki:`nodelet`\s in that many nodes can run in a single
process, the Java VM.

Users, like yourself, do not create :javadoc:`org.ros.node.Node`\s. Instead,
programs are defined as implementations of :javadoc:`org.ros.node.NodeMain`
which are executed by the aptly named :javadoc:`org.ros.node.NodeMainExecutor`.

Let's consider the following mostly empty :javadoc:`org.ros.node.NodeMain`
implementation:

.. code-block:: java

  import org.ros.namespace.GraphName;
  import org.ros.node.Node;
  import org.ros.node.NodeMain;

  public class MyNode implements NodeMain {

    @Override
    public GraphName getDefaultNodeName() {
      return new GraphName("my_node");
    }

    @Override
    public void onStart(Node node) {
    }

    @Override
    public void onShutdown(Node node) {
    }

    @Override
    public void onShutdownComplete(Node node) {
    }
  }

The :javadoc:`org.ros.node.NodeMain#getDefaultNodeName()` method returns the
default name of the node. This name will be used unless a node name is
specified in the :javadoc:`org.ros.node.NodeConfiguration` (more on that
later). :javadoc:`org.ros.namespace.GraphName`\s are used throughout rosjava
when refering to nodes, topics, and parameters. Most methods which accept a
:javadoc:`org.ros.namespace.GraphName` will also accept a string for
convenience.

The :javadoc:`org.ros.node.NodeListener#onStart(org.ros.node.Node)` method is
the entry point for your program (or node). The :javadoc:`org.ros.node.Node`
parameter is the factory we use to build things like
:javadoc:`org.ros.topic.Publisher`\s and :javadoc:`org.ros.topic.Subscriber`\s.

The :javadoc:`org.ros.node.NodeListener#onShutdown(org.ros.node.Node)` method is
the first exit point for your program. It will be executed as soon as shutdown
is started (i.e. before all publishers, subscribers, etc. have been shutdown).
The shutdown of all created publishers, subscribers, etc. will be delayed until
this method returns or the shutdown timeout expires.

The :javadoc:`org.ros.node.NodeListener#onShutdownComplete(org.ros.node.Node)`
method is the final exit point for your program. It will be executed after all
publishers, subscribers, etc. have been shutdown. This is the preferred place
to handle clean up since it will not delay shutdown.

Publishers and subscribers
--------------------------

The following class (:javadoc:`org.ros.rosjava_tutorial_pubsub.Talker`) is
available from the rosjava_tutorial_pubsub package. In this example, we create
a publisher for the chatter topic. This should feel relatively familiar if
you're a ROS veteran. The :javadoc:`org.ros.topic.Publisher` publishes
``std_msgs.String`` messages to the ``/chatter`` topic.

.. literalinclude:: ../../../../rosjava_tutorial_pubsub/src/main/java/org/ros/rosjava_tutorial_pubsub/Talker.java
  :language: java
  :linenos:
  :lines: 17-
  :emphasize-lines: 27,37

Line 27 will probably feel unfamailiar even to ROS veterans. This is one
example of rosjava's asynchornous API. The intent of our
:javadoc:`org.ros.rosjava_tutorial_pubsub.Talker` class is to publish a hello
world message to anyone who will listen once per second. One way to accomplish
this is to publish the message and sleep in a loop. However, we don't want to
block the :javadoc:`org.ros.node.NodeListener#onStart(org.ros.node.Node)`
method. So, we create a :javadoc:`org.ros.concurrent.CancellableLoop` and ask
the :javadoc:`org.ros.node.Node` to execute it. The loop will be interrupted
automatically when the :javadoc:`org.ros.node.Node` exits.

On line 37 we create a new ``std_msgs.String`` message to publish using the
:javadoc:`org.ros.node.topic.Publisher#newMessage()` method. Messages in
rosjava cannot be instantiated directly. More on that later.

Now lets take a look at the :javadoc:`org.ros.rosjava_tutorial_pubsub.Listener`
class.

.. literalinclude:: ../../../../rosjava_tutorial_pubsub/src/main/java/org/ros/rosjava_tutorial_pubsub/Listener.java
  :language: java
  :linenos:
  :lines: 17-
  :emphasize-lines: 26

In line 26 we see another example of rosjava's asynchornous API. We can add as
many :javadoc:`org.ros.message.MessageListener`\s to our
:javadoc:`org.ros.node.topic.Subscriber` as we like. When a new message is
received, all of our :javadoc:`org.ros.message.MessageListener`\s will be
called with the incoming message as an argument to
:javadoc:`org.ros.message.MessageListener#onNewMessage(T)`.

Executing nodes
---------------

When packaging your application into jar, you can use :javadoc:`org.ros.RosRun`
as the main class. :javadoc:`org.ros.RosRun` provides a
:javadoc:`org.ros.node.NodeMainExecutor` and a command line interface that will
be familiar to ROS veterans. For example, the following steps will build and
execute the :javadoc:`org.ros.rosjava_tutorial_pubsub.Talker` and
:javadoc:`org.ros.rosjava_tutorial_pubsub.Listener` nodes in separate
processes:

.. code-block:: bash

  roscd rosjava_tutorial_pubsub
  ../gradlew installApp
  roscore &
  ./build/rosjava_tutorial_pubsub/bin/rosjava_tutorial_pubsub org.ros.rosjava_tutorial_pubsub.Talker &
  ./build/rosjava_tutorial_pubsub/bin/rosjava_tutorial_pubsub org.ros.rosjava_tutorial_pubsub.Listener

.. note:: The above example launches roscore and the Talker node in the
  background. You could instead launch each in a separate terminal. Also, you
  may notice that rosrun cannot find the installed executable. This is a known
  issue that will be addressed in the future.

At this point, you should see the familiar "Hello, world!" messages start
appearing in your terminal. You can also echo the topic using the
:roswiki:`rostopic` command line tool:

.. code-block:: bash

  rostopic echo chatter

You can configure the executed nodes from the command line in the same way you
would any other ROS executable. For example, the following commands will remap
the default topic /chatter to /foo.

.. code-block:: bash

  ./build/rosjava_tutorial_pubsub/bin/rosjava_tutorial_pubsub org.ros.rosjava_tutorial_pubsub.Talker chatter:=/foo &
  ./build/rosjava_tutorial_pubsub/bin/rosjava_tutorial_pubsub org.ros.rosjava_tutorial_pubsub.Listener chatter:=/foo

See :roswiki:`Remapping%20Arguments` for more information on passing arguments
to ROS executables.

.. note:: Only the arguments described in :roswiki:`Remapping%20Arguments` are
  supported. Support for arbitrary command line arguments (i.e. argv) will be
  added in the future.

Services
--------

The following class (:javadoc:`org.ros.rosjava_tutorial_services.Server`) is
available from the rosjava_tutorial_services package. In this example, we
create a :javadoc:`org.ros.node.service.ServiceServer` for the
``test_ros.AddTwoInts`` service. This should feel relatively familiar if you're
a ROS veteran.

.. literalinclude:: ../../../../rosjava_tutorial_services/src/main/java/org/ros/rosjava_tutorial_services/Server.java
  :language: java
  :linenos:
  :lines: 17-
  :emphasize-lines: 28

The :javadoc:`org.ros.node.service.ServiceResponseBuilder` is called
asynchronously for each incoming request. On line 28 we modify the response
output parameter to contain the sum of the two integers in the request. The
response will be sent once the
:javadoc:`org.ros.node.service.ServiceResponseBuilder#build(T, S)` returns.

Now lets take a look at the :javadoc:`org.ros.rosjava_tutorial_services.Client`
class.

.. literalinclude:: ../../../../rosjava_tutorial_services/src/main/java/org/ros/rosjava_tutorial_services/Client.java
  :language: java
  :linenos:
  :lines: 17-
  :emphasize-lines: 35-46

On lines 35-46 we see another example of rosjava's asynchornous API. When the
response is received, our
:javadoc:`org.ros.node.service.ServiceResponseListener` will be called with the
incoming response as an argument to
:javadoc:`org.ros.node.service.ServiceResponseListener#onSuccess(T)`. In the
event that the server thows a :javadoc:`org.ros.exception.ServiceException`
while building the response,
:javadoc:`org.ros.node.service.ServiceResponseListener#onFailure(RemoteException)`
will be called. The :javadoc:`org.ros.exception.RemoteException` will contain
the error message from the server.

Building and executing these nodes works in the same manner as described above:

.. code-block:: bash

  roscd rosjava_tutorial_services
  ../gradlew installApp
  roscore &
  ./build/rosjava_tutorial_services/bin/rosjava_tutorial_services org.ros.rosjava_tutorial_services.Server &
  ./build/rosjava_tutorial_services/bin/rosjava_tutorial_services org.ros.rosjava_tutorial_services.Client

At this point, you should see the log message "2 + 2 = 4" appear in your
terminal. You can also access the service using the :roswiki:`rosservice`
command line tool:

.. code-block:: bash

  rosservice add_two_ints 2 2

Just as before, you can configure the executed nodes from the command line in
the same way you would any other ROS executable. See
:roswiki:`Remapping%20Arguments` for more information on passing arguments to
ROS executables.

Messages
--------

Messages are defined as interfaces. Since this makes it impossible to
instantiate the message directly, it's necessary to use a
:javadoc:`org.ros.message.MessageFactory` or helper methods such as
:javadoc:`org.ros.node.topic.Publisher#newMessage()`. This indirection allows
the underlying message implementation to change in the future. ::

  Node node;

  ...

  PointCloud2 msg = node.getTopicMessageFactory()
      .newMessage(sensor_msgs.PointCloud._TYPE);

If you want to use messages that you define:

- create a new package for those messages (e.g. my_msgs)
- ensure that my_msgs is in your ROS_PACKAGE_PATH (see :roswiki:`EnvironmentVariables`)
- reinstall rosjava_messages (see :doc:`building`)

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

Currently, ParameterListeners are not generic. Instead, you are responsible for
casting the value appropriately.

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

  // report an error message
  node.getLog.error("error message");

  // error message with an exception
  // so that it can print the stack trace
  node.getLog.error("error message", e);

  node.fatal("message informing user of a fatal occurrence");

.. _Apache Commons Log: http://commons.apache.org/logging/commons-logging-1.1.1/apidocs/index.html

