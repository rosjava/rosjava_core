# set EXTRA_CMAKE_FLAGS in the including Makefile in order to add tweaks
CMAKE_FLAGS= -Wdev -DCMAKE_TOOLCHAIN_FILE=`rospack find rosbuild`/rostoolchain.cmake $(EXTRA_CMAKE_FLAGS)

PACKAGE_NAME=$(shell basename $(PWD))

all:
	rosrun rosjava_bootstrap generate_dependencies_xml.py $(PACKAGE_NAME) dependencies.xml
	rosrun rosjava_bootstrap generate_ros_properties.py $(PACKAGE_NAME) ros.properties
	rosrun rosjava_bootstrap generate_android_properties.py $(PACKAGE_NAME) > default.properties
	rosrun rosjava_bootstrap generate_msg_depends.py $(PACKAGE_NAME)
	if [ ! -f .classpath ] ; then rosrun rosjava_bootstrap generate_eclipse_classpath.py $(PACKAGE_NAME) > .classpath ; touch .classpath-generated; fi
	if [ ! -f .project ] ; then rosrun rosjava_bootstrap generate_eclipse_project.py $(PACKAGE_NAME) > .project ; touch .project-generated; fi
	ant

clean:
	-ant clean
	-if [ -f .project-generated ] ; then rm .project .project-generated; fi
	-if [ -f .classpath-generated ] ; then rm .classpath .classpath-generated; fi
	-if [ -f dependencies.xml ] ; then rm dependencies.xml; fi
	-rm ros.properties
	-rm default.properties

wipe-msgs:
	rosrun rosjava_bootstrap generate_msg_depends.py --wipe $(PACKAGE_NAME)
