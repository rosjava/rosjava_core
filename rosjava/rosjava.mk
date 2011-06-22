# set EXTRA_CMAKE_FLAGS in the including Makefile in order to add tweaks
CMAKE_FLAGS= -Wdev -DCMAKE_TOOLCHAIN_FILE=`rospack find rosbuild`/rostoolchain.cmake $(EXTRA_CMAKE_FLAGS)

# The all target does the heavy lifting, creating the build directory and
# invoking CMake
all: ant-properties msg-deps srv-deps
	ant

PACKAGE_NAME=$(shell basename $(PWD))

ant-properties:
	rosrun rosjava generate_properties.py $(PACKAGE_NAME) > ros.properties

msg-deps:
	echo "TODO"

srv-deps:
	echo "TODO"

clean:
	-ant clean

# All other targets are just passed through
test: all
	ant test

