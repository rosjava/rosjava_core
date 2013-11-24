Overview
========

While rosjava is mostly feature complete, it is currently under active
development. Consider all APIs and documentation to be volatile.

`Javadoc <javadoc/index.html>`_ is used extensively and cross referenced from
this documentation.

Android friendly
----------------

One of the primary goals of rosjava is to bring ROS to Android. See the
android_core :ref:`android-core:getting-started` documentation for more
information.

Asynchronous
------------

Because ROS is heavily dependent on network communication, rosjava is
asynchronous. No attempt is made to hide asynchronous behavior behind a
synchronous API. As a result, the rosjava APIs may feel unfamiliar.

More threads, fewer processes
-----------------------------

You will not find a ``spin()`` method in rosjav in rosjava. Unlike other client
libraries, many rosjava nodes can run in a thread pool within a single JVM
process. In this way, rosjava nodes are similar to C++ nodlets. In the future,
rosjava nodes will support in memory communication in the same way that C++
nodelets do today.

