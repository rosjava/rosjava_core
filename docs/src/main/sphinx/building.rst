Building rosjava_core
=====================

rosjava_core uses the `Gradle`_ build system. To build rosjava_core, execute the `gradle wrapper`_:

.. _Gradle: http://www.gradle.com/
.. _gradle wrapper: http://gradle.org/docs/current/userguide/gradle_wrapper.html

Note that the build process currently involves extra steps that will be folded into Gradle tasks.

#. roscd rosjava_core
#. ./gradlew rosjava_bootstrap:install
#. rosrun rosjava_bootstrap install_generated_modules.py rosjava
#. ./gradlew rosjava:install

Then, for each rosjava_core package you're interested in (e.g. foo):

#. rosrun rosjava_bootstrap install_generated_modules.py foo
#. ./gradlew foo:install

See the rosjava `Javadoc <javadoc/index.html>`_ to learn more about the API.

Note that package level documentation is in progress.
