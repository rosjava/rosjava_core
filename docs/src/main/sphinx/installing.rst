.. _installing:

Installing rosjava_core
=======================

These instructions assume that you have already installed ROS on your system.
See :roswiki:`ROS/Installation` if you need help installing ROS.

These instructions also assume you are using Ubuntu. However, the differences
between platforms should be minimal.

The recommend installation procedure is to use rosws. See the `rosws tutorial`_
for more information if you find the following quick start instructions to be
insufficient.

.. code-block:: bash

  sudo apt-get install python-pip
  sudo pip install --upgrade rosinstall
  mkdir ~/my_workspace
  cd ~/my_workspace
  rosws init
  rosws merge /opt/ros/electric/.rosinstall
  rosws merge http://rosjava.googlecode.com/hg/.rosinstall
  rosws update
  source setup.bash

.. note:: You should source the correct setup script for your shell (e.g.
  setup.bash for Bash or setup.zsh for Z shell).

If you would like to build the rosjava_core documentation, you will also need
Pygments 1.5+ and Sphinx 1.1.3+.

.. code-block:: bash

  sudo pip install --upgrade sphinx Pygments

.. _rosws tutorial: http://www.ros.org/doc/api/rosinstall/html/rosws_tutorial.html

