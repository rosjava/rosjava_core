#!/usr/bin/env python
# Software License Agreement (BSD License)
#
# Copyright (c) 2011, Willow Garage, Inc.
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions
# are met:
#
#  * Redistributions of source code must retain the above copyright
#    notice, this list of conditions and the following disclaimer.
#  * Redistributions in binary form must reproduce the above
#    copyright notice, this list of conditions and the following
#    disclaimer in the documentation and/or other materials provided
#    with the distribution.
#  * Neither the name of Willow Garage, Inc. nor the names of its
#    contributors may be used to endorse or promote products derived
#    from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
# "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
# LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
# FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
# COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
# INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
# BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
# LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
# ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.
#
# Revision $Id: test_empty_service.py 3803 2009-02-11 02:04:39Z rob_wheeler $

PKG = 'rosjava'
NAME = 'testheader_passthrough'
import roslib; roslib.load_manifest(PKG)

from ros import rospy
from ros import rosjava_test_msgs
from ros import rostest

import sys
import time
import unittest

from rosjava_test_msgs.msg import TestHeader

class TestHeaderPassthrough(unittest.TestCase):
        
    def setUp(self):
        rospy.init_node(NAME)
        
        self.fixture_curr = None
        self.test_curr = None
        
        rospy.Subscriber('test_header_in', TestHeader, self.cb_from_fixture)
        rospy.Subscriber('test_header_out', TestHeader, self.cb_from_test)
        
    def cb_from_fixture(self, msg):
        self.fixture_curr = msg

    def cb_from_test(self, msg):
        self.test_curr = msg

    def test_testheader_passthrough(self):
        # 20 seconds to validate fixture
        timeout_t = time.time() + 20.
        print("waiting for 20 seconds for fixture to verify")
        while self.fixture_curr is None and \
                not rospy.is_shutdown() and \
                timeout_t > time.time():
            time.sleep(0.2)

        self.assertFalse(timeout_t < time.time(), "timeout exceeded")
        self.assertFalse(rospy.is_shutdown(), "node shutdown")            
        self.assertFalse(self.fixture_curr is None, "no data from fixture")
        self.assertEqual('/node0', self.fixture_curr.caller_id)
        self.assertEqual('', self.fixture_curr.orig_caller_id)

        # another 20 seconds to validate client
        timeout_t = time.time() + 20.
        print("waiting for 20 seconds for client to verify")
        while self.test_curr is None and \
                not rospy.is_shutdown() and \
                timeout_t > time.time():
            time.sleep(0.2)

        self.assertFalse(self.test_curr is None, "no data from test")
        self.assertEqual('/rosjava_node', self.test_curr.caller_id)
        self.assertEqual('/node0', self.test_curr.orig_caller_id)
        t = self.test_curr.header.stamp.to_sec()
        # be really generous here, just need to be in the ballpark.
        self.assertTrue(abs(time.time() - t) < 60.)

if __name__ == '__main__':
    import rostest
    rostest.run(PKG, NAME, TestHeaderPassthrough, sys.argv)
