=========
Changelog
=========

0.3.5 (2017-07-17)
------------------
* Fix remapping member variable not set if argument is not null
* Adding the capability to remove a messageListener from a subscriber.
* Avoid flooding log with error messages on NTP sync failure
* Fix to avoid logging the same error multiple times for NtpTimeProvider.
* Fix: avoid exceptions on node shutdown
* Fix to avoid spurious exceptions on node shutdown if there's an active topic.
* Contributors: Juan Ignacio Ubeira, Julian Cerruti, Perrine Aguiar

0.3.4 (2017-05-22)
------------------
* Implement parameter server's searchParam
* fixed nullptr triggered by default node factory
* Contributors: Julian Cerruti, Leroy Ruegemer

0.3.3 (2017-04-17)
------------------
* Added fix to remove Publishers and Subscribers from topicParticipantManager on node shutdown. 
* Added fix to remove listeners from DefaultPublisher on shutdown.
* Contributors: Dan Ambrosio, Julian Cerruti

0.3.2 (2017-03-06)
------------------
* Adds fix for shutting down DefaultNodeMainExecutor ListenerGroup to prevent leak in android when activities are destroyed.
* Added ability to remove listener from ListenerGroup to fix android_core issue `#254 <https://github.com/rosjava/rosjava_core/issues/254>`_.

0.3.1 (2017-02-22)
------------------
* NativeNodeMain upgraded to upstream error codes to the application.
* Parameter Node added to new Helper module.
* publishMavenJavaPublicationToMavenRepository -> publish
* Gradle 2.2.1 -> 2.14.1
* Contributors: Juan Ignacio Ubeira, Julian Cerruti

0.3.0 (2016-12-13)
------------------
* Updates for Kinetic release.
* NativeNodeMain for C++ node integration.

0.2.1 (2015-02-25)
------------------
* allow setting of the talker topic name in pubsub tutorial.
* A more robust ntp provider.
* gradle 1.12 -> 2.2.1
* Add a comment explaining the disabling of SNI.
* Fix SSL connection errors with Java 1.7.
* Adds APIs to check the status of a service connection.
  Fixes bug that caused a disconnected service to be reused.
  Fixes bug in DefaultSubscriber that used the wrong class for logging.
* Changes FrameTransformTree to use GraphName instead of FrameName but still support tf2.
* Removed listAllInetAddress private method.
  Also renamed getAllInetAddressByName to getAllInetAddressesByName.
* Adds newNonLoopbackForNetworkInterface
  This is needed to allow the user to specify the ROS Hostname.
  An example application can: Run the ROS application through a VPN
  connection. The user would like to use the tunnel interface
  to creates the nodes and master.
* Contributors: Damon Kohler, Daniel Stonier, Lucas Chiesa, corot, damonkohler

0.1.6 (2013-11-01)
------------------
* Constrain open ranged dependencies.

0.1.5 (2013-10-31)
------------------
* use ROS_MAVEN_REPOSITORY

0.1.4 (2013-10-25)
------------------
* official maven style open ended dependencies.

0.1.3 (2013-09-23)
------------------
* use updated ros gradle plugins with maven-publish for publishing.

0.1.2 (2013-09-17)
------------------
* missing sensor_msgs dependency added.
* gradle wrapper -> 1.7

0.1.1 (2013-09-13)
------------------
* first official hydro release
* tf_msgs -> tf2_msgs upgrade
* fix hydro transform issues (frame naming policy)
* message generation code moved out
* message generation moved out
* using gradle plugins to eliminate copied build logic
* uses the github rosjava/rosjava_maven_repo for pulling external dependencies
* uses local maven repositories embedded in the ros workspaces (share/maven)
* cmake installation rules - deb building starting
* test_ros messages -> rosjava_test_msgs

