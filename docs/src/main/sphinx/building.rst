Building rosjava_core
=====================

rosjava_core uses the `Gradle`_ build system. `rosmake`_ is not supported.

To build rosjava_core, execute the `gradle wrapper`_:

.. code-block:: bash

  roscd rosjava_core
  ./gradlew install

If you do not want to build the documentation, you may exclude the docs:install task:

.. code-block:: bash

  ./gradlew install -x docs:install

To generate Eclipse project files:

.. code-block:: bash

  ./gradlew eclipse

.. _Gradle: http://www.gradle.org/
.. _rosmake: http://ros.org/wiki/rosmake/
.. _gradle wrapper: http://gradle.org/docs/current/userguide/gradle_wrapper.html

