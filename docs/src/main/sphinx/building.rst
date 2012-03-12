Building rosjava_core
=====================

rosjava_core uses the `Gradle`_ build system. `rosmake`_ is not supported.

To build rosjava_core, execute the `gradle wrapper`_:

.. _Gradle: http://www.gradle.com/
.. _rosmake: http://ros.org/wiki/rosmake/
.. _gradle wrapper: http://gradle.org/docs/current/userguide/gradle_wrapper.html

Note that the build process currently involves extra steps that will be folded
into Gradle tasks or otherwise eliminated.

#. `Install Maven 3 <http://maven.apache.org/download.html>`_.
#. roscd rosjava_core
#. ./gradlew rosjava_bootstrap:install
#. rosrun rosjava_bootstrap install_generated_modules.py rosjava
#. ./gradlew rosjava:install

Then, for each rosjava_core package you're interested in (e.g. foo):

#. rosrun rosjava_bootstrap install_generated_modules.py foo
#. ./gradlew foo:install

To generate Eclipse project files:

#. ./gradlew foo:eclipse

