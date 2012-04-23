.. _installing:

Installing rosjava_core
=======================

These instructions assume that you have already installed ROS on your system.
See :roswiki:`ROS/Installation` if you need help installing ROS.

When choosing a release to install, please consider that:

* The *oldest* ROS release that rosjava has been tested with is: **Electric**
* The *newest* ROS release that rosjava has been tested with is: **Electric**

These instructions also assume you are using Ubuntu. However, the differences
between platforms should be minimal.

The recommend installation procedure is to use rosws. See the `rosws tutorial`_
for more information if you find the following quick start instructions to be
insufficient.

.. code-block:: bash

  sudo apt-get install python-setuptools
  easy_install --prefix ~/.local -U rosinstall
  export PATH=$PATH:~/.local/bin
  mkdir ~/my_workspace
  cd ~/my_workspace
  rosws init
  rosws merge /opt/ros/electric/.rosinstall
  rosws merge http://rosjava.googlecode.com/hg/.rosinstall
  rosws update

.. note:: The rosws tool will remind you as well, but don't forget to source
  the appropriate, newly generated setup script.

If you would like to build the rosjava_core documentation, you will also need Pygments 1.5+.

.. code-block:: bash

  easy_install --prefix ~/.local -U pygments

.. _rosws tutorial: http://www.ros.org/doc/api/rosinstall/html/rosws_tutorial.html

