# This is here until ROS totally built using mvn

mvn install:install-file -DgroupId=org.ros.rosjava -DartifactId=org.ros.rosjava.actionlib -Dversion=0.0.0 -Dpackaging=jar -Dfile=$HOME/.ros/rosjava/lib/org.ros.rosjava.actionlib-0.0.0.jar
mvn install:install-file -DgroupId=org.ros.rosjava -DartifactId=org.ros.rosjava.actionlib_msgs -Dversion=0.0.0 -Dpackaging=jar -Dfile=$HOME/.ros/rosjava/lib/org.ros.rosjava.actionlib_msgs-0.0.0.jar
mvn install:install-file -DgroupId=org.ros.rosjava -DartifactId=org.ros.rosjava.actionlib_tutorials -Dversion=0.0.0 -Dpackaging=jar -Dfile=$HOME/.ros/rosjava/lib/org.ros.rosjava.actionlib_tutorials-0.0.0.jar
mvn install:install-file -DgroupId=org.ros.rosjava -DartifactId=org.ros.rosjava.roscpp -Dversion=0.0.0 -Dpackaging=jar -Dfile=$HOME/.ros/rosjava/lib/org.ros.rosjava.roscpp-0.0.0.jar
mvn install:install-file -DgroupId=org.ros.rosjava -DartifactId=org.ros.rosjava.rosgraph_msgs -Dversion=0.0.0 -Dpackaging=jar -Dfile=$HOME/.ros/rosjava/lib/org.ros.rosjava.rosgraph_msgs-0.0.0.jar
mvn install:install-file -DgroupId=org.ros.rosjava -DartifactId=org.ros.rosjava.std_msgs -Dversion=0.0.0 -Dpackaging=jar -Dfile=$HOME/.ros/rosjava/lib/org.ros.rosjava.std_msgs-0.0.0.jar
mvn install:install-file -DgroupId=org.ros.rosjava -DartifactId=org.ros.rosjava.test_ros -Dversion=0.0.0 -Dpackaging=jar -Dfile=$HOME/.ros/rosjava/lib/org.ros.rosjava.test_ros-0.0.0.jar

mvn install:install-file -DgroupId=org.ros.rosjava -DartifactId=org.ros.rosjava.apache-xmlrpc -Dversion=3.1.3 -Dpackaging=jar -Dfile=../apache_xmlrpc/target/org.ros.rosjava.apache-xmlrpc-3.1.3.jar
mvn install:install-file -DgroupId=org.ros.rosjava -DartifactId=org.ros.rosjava.ws-commons-util -Dversion=1.0.2 -Dpackaging=jar -Dfile=../apache_commons_util/target/org.ros.rosjava.ws-commons-util-1.0.2.jar
