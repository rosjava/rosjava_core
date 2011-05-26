# There's an NPE bug that makes it necessary to split this up in the case of an empty project.
function update {
  android update project --target 11 --path $1
  android update project --target 11 --path $1 --library ../../library
}

update "RosCamera"
update "Teleop"
update "image_transport"
update "orientation_publisher"
update "pan_tilt_camera"
update "pubsub"
