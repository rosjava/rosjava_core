cmake_minimum_required(VERSION 2.8.3)
project(rosjava_core)

find_package(catkin REQUIRED)

catkin_package()

execute_process(
  COMMAND ./gradlew
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
)

