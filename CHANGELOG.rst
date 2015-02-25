=========
Changelog
=========

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

