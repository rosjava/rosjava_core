General
=======

*S: Explain key problem for us - multiple repo builds (relative package referencing, one shot builds). Added kazuto's patches.*

- System level maven cache based on rosdistro
- Use a root gradel file (build.gradle and settings.gradle)
- android doesn't have a maven cache
- use catkin android as fallback, try graven android plugin

*S: Branch development*

groovy, hydro (no devel suffixes)

*S: Package Structure*

*Q: How to sign the CLA for apache?*

SL4A, look for developers, individual and company CLA's. Digital signing. Can we get github to pop up a message when someone makes a pull request. Or at least have documentation.

*S: be very careful about java names, come up with some guidelines* 

Java
====

*Q: Should we use maven or bloom to package rosjava?*

Gradle target install app - have a closer look at this. Depending on options, feed that into bloom. All the jars should go into classpath and live in our rosdistro maven.

Or use Maven - dunno really yet.

*Q: If Maven, how is rosjava using the robotbrain maven?*

We should create a ros maven repo to replace robotbrain (done by swi guy)
Or distribute in debs.

*Q: If Maven, Can we isolate maven directories per rosdistro (e.g. in ~/.ros/groovy/.m2)?*

*Q: If bloom, how to handle dependencies?*

*S: Follow the style guide*

Android
=======

*S: Everyone use RosActivity so we can converge fixes in RosActivity*

Should we make sure this is in documentation.

*S: Handling RosActivity, NodeMain, OSGI...

*S: Explain what I'm doing with android_generate_properties and catkin_create_xxx.*

*Q: Hos is it pulling java dependencies?*

Just dumps the libraries (no maven for android)

*Q: Why is gradle so slow for android?*

Dexing.

*Q: Where should we put external android dependencies that aren't in mavencentral (e.g. snakeyaml)?*

Have a gradle script take the web download and install to the local maven cache (maybe a package to do this?)

*Q: Do folk generally use command line (gradle) or IDE (eclipse - incremental builds are much faster)*

Eclipse

*Q: Should we patch around the current android gradle framework or move to the new google android gradle plugin?*

Try it.

Gradle Android Plugin
=====================

* http://tools.android.com/tech-docs/new-build-system/user-guide

Don't know much about it yet. Some key points: it will replace adt. It will integrate with an ide.

Assigning
=========

*S: Where should be code (rosjava and rosandroid?)*

*S: People, damon on core code and patches, Daniel* on catkin, ....


Turtlebot/PR2 Apps
==================

*Q: This may branch - we're upgrading. What should we share?*

No