cmake_minimum_required(VERSION 2.4.6)
include($ENV{ROS_ROOT}/core/rosbuild/rosbuild.cmake)
rosbuild_init()
# for now we're just using the rosmake integration to run the integration tests
# This will remain the case until we figure out to properly integrate Java and
# rosmake.

# integration tests
rosbuild_add_rostest(test/slave_api.rostest)
rosbuild_add_rostest(test/slave_api_simtime.rostest)
rosbuild_add_rostest(test/string_passthrough.rostest)
rosbuild_add_rostest(test/int64_passthrough.rostest)
