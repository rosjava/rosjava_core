Building rosjava_core
=====================

rosjava_core uses the `Gradle`_ build system. `rosmake`_ is not supported.

To build rosjava_core, execute the `gradle wrapper`_:

.. _Gradle: http://www.gradle.org/
.. _rosmake: http://ros.org/wiki/rosmake/
.. _gradle wrapper: http://gradle.org/docs/current/userguide/gradle_wrapper.html

#. roscd rosjava_core
#. ./gradlew install

To generate Eclipse project files:

#. ./gradlew eclipse

