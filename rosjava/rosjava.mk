# set EXTRA_CMAKE_FLAGS in the including Makefile in order to add tweaks
CMAKE_FLAGS= -Wdev -DCMAKE_TOOLCHAIN_FILE=`rospack find rosbuild`/rostoolchain.cmake $(EXTRA_CMAKE_FLAGS)

# The all target does the heavy lifting, creating the build directory and
# invoking CMake
all: ant-properties eclipse-project msg-deps
	ant

PACKAGE_NAME=$(shell basename $(PWD))

ant-properties:
	rosrun rosjava generate_properties.py $(PACKAGE_NAME) > ros.properties

eclipse-project:
	rosrun rosjava generate_properties.py --eclipse $(PACKAGE_NAME) > .project

# msg-deps builds both msgs and srvs
msg-deps:
	rosrun rosjava generate_msg_depends.py $(PACKAGE_NAME)

clean:
	-rm ros.properties
	-ant clean

wipe-msgs:
	rosrun rosjava generate_msg_depends.py --wipe $(PACKAGE_NAME)

# All other targets are just passed through
test: all
	ant test

