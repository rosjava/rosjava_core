#!/usr/bin/python
#
# Copyright (C) 2012 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License. You may obtain a copy of
# the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.

import threading

import roslib; roslib.load_manifest('rosjava_benchmarks')
import rospy

import tf.msg as tf_msgs
import std_msgs.msg as std_msgs


class PubsubBenchmark(object):

  def __init__(self):
    self.count = itertools.count()
    self.time = rospy.Time.now()
    self.lock = threading.Lock()

  def callback(self, _):
    with self.lock:
      next(self.count)

  def run(self):
    tf_publisher = rospy.Publisher('tf', tf_msgs.tfMessage)
    status_publisher = rospy.Publisher('status', std_msgs.String)
    rospy.Subscriber('tf', tf_msgs.tfMessage, self.callback)
    while not rospy.is_shutdown():
      tf_publisher.publish(tf_msgs.tfMessage())
      if (rospy.Time.now() - self.time) > rospy.Duration(5):
        status_publisher.publish(std_msgs.String('%.2f Hz' % (next(self.count) / 5.0)))
        with self.lock:
          self.count = itertools.count()
          self.time = rospy.Time.now()


if __name__ == '__main__':
  try:
    rospy.init_node('benchmark')
    PubsubBenchmark().run()
  except rospy.ROSInterruptException:
    pass
