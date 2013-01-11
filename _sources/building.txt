.. _building:

Building rosjava_core
=====================

rosjava_core uses the `Gradle`_ build system. `rosmake`_ is not supported.

To build rosjava_core and install it to your local `Maven`_ repository, execute
the `gradle wrapper`_:

.. code-block:: bash

  roscd rosjava_core
  ./gradlew install

To build the documentation, you may execute the docs task:

.. code-block:: bash

  ./gradlew docs

To run the tests, you may execute the test task:

.. code-block:: bash

  ./gradlew test

To generate Eclipse project files, you may execute the eclipse:

.. code-block:: bash

  ./gradlew eclipse

.. _Gradle: http://www.gradle.org/
.. _rosmake: http://ros.org/wiki/rosmake/
.. _Maven: http://maven.apache.org/
.. _gradle wrapper: http://gradle.org/docs/current/userguide/gradle_wrapper.html

