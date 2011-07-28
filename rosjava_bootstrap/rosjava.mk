# set EXTRA_CMAKE_FLAGS in the including Makefile in order to add tweaks
CMAKE_FLAGS= -Wdev -DCMAKE_TOOLCHAIN_FILE=`rospack find rosbuild`/rostoolchain.cmake $(EXTRA_CMAKE_FLAGS)

PACKAGE_NAME=$(shell basename $(PWD))

all:
	rosrun rosjava_bootstrap make.py $(PACKAGE_NAME)

clean:
	-rosrun rosjava_bootstrap make.py $(PACKAGE_NAME) clean