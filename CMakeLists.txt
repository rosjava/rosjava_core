cmake_minimum_required(VERSION 2.8.3)
project(rosjava_core)

find_package(catkin REQUIRED rosjava_tools)

catkin_rosjava_setup()

catkin_package()

install(DIRECTORY ${CATKIN_DEVEL_PREFIX}/${CATKIN_GLOBAL_MAVEN_DESTINATION}/ros/rosjava_core/ 
        DESTINATION ${CATKIN_GLOBAL_MAVEN_DESTINATION}/ros/rosjava_core)