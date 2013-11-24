Best practices
==============

rosjava is different than other ROS client libraries in many respects. As a
result, there are new best practices that should be followed while developing a
rosjava application.

Java package names
------------------

As usual, Java package names should start with a reversed domain name. In the
ROS ecosystem, the domain name should be followed by the ROS package name. For
example:

- org.ros.rosjava
- org.ros.rosjava_geometry

Only core packages (e.g. those in rosjava_core and android_core) should begin
with org.ros. A suitably unique choice for github based repos would be
the github url followed organization and repository/package name, e.g.

- com.github.rosjava.rosjava_extras

