# set EXTRA_CMAKE_FLAGS in the including Makefile in order to add tweaks
CMAKE_FLAGS= -Wdev -DCMAKE_TOOLCHAIN_FILE=`rospack find rosbuild`/rostoolchain.cmake $(EXTRA_CMAKE_FLAGS)

# The all target does the heavy lifting, creating the build directory and
# invoking CMake
all: obtain-dependencies ant-properties eclipse-project eclipse-classpath msg-deps
	ant

PACKAGE_NAME=$(shell basename $(PWD))

obtain-dependencies:
	rosrun rosjava_bootstrap generate_dependencies_xml.py $(PACKAGE_NAME) > dependencies.xml

ant-properties:
	rosrun rosjava_bootstrap generate_ros_properties.py $(PACKAGE_NAME) > ros.properties
	rosrun rosjava_bootstrap generate_android_properties.py $(PACKAGE_NAME) > default.properties

eclipse-classpath:
	if [ ! -f .classpath ] ; then rosrun rosjava_bootstrap generate_eclipse_classpath.py $(PACKAGE_NAME) > .classpath ; touch .classpath-generated; fi

eclipse-project:
	if [ ! -f .project ] ; then rosrun rosjava_bootstrap generate_eclipse_project.py $(PACKAGE_NAME) > .project ; touch .project-generated; fi

# msg-deps builds both msgs and srvs
msg-deps:
	rosrun rosjava_bootstrap generate_msg_depends.py $(PACKAGE_NAME)

clean:
	-ant clean
	-if [ -f .project-generated ] ; then rm .project .project-generated; fi
	-if [ -f .classpath-generated ] ; then rm .classpath .classpath-generated; fi
	-if [ -f dependencies.xml ] ; then rm dependencies.xml; fi
	-rm ros.properties
	-rm default.properties

wipe-msgs:
	rosrun rosjava_bootstrap generate_msg_depends.py --wipe $(PACKAGE_NAME)

# All other targets are just passed through
test: all
	ant test

