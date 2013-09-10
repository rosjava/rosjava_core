cmake_minimum_required(VERSION 2.8.3)
project(rosjava_core)

find_package(catkin REQUIRED rosjava_tools)

catkin_rosjava_setup(uploadArchives)

catkin_package()

