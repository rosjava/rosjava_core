Installing rosjava_core
=======================

rosjava_core should be installed using rosws. See the `rosws tutorial`_ for more information.

.. _rosws tutorial: http://www.ros.org/doc/api/rosinstall/html/rosws_tutorial.html

The following instructions configure rosjava_core for electric on Ubuntu.

#. sudo apt-get install easy_install
#. easy_install --prefix ~/.local -U rosintall
#. cd ~/my_ros_workspace
#. rosws init
#. rosws merge /opt/ros/electric/.rosinstall
#. rosws merge http://rosjava.googlecode.com/hg/.rosinstall

Don't forget to source the appropriate new setup shell script.

