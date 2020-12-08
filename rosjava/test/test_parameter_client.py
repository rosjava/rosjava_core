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
NAME = 'test_parameter_client'
import roslib; roslib.load_manifest(PKG)

import roslib.names

from ros import rospy
from ros import std_msgs
from ros import rostest

import sys
import time
import unittest

from std_msgs.msg import String, Bool, Int64, Float64
from rosjava_test_msgs.msg import Composite, CompositeA, CompositeB, TestArrays

class TestParameterClient(unittest.TestCase):
        
    def setUp(self):
        rospy.init_node(NAME)

        rospy.Subscriber('tilde', String, self.tilde_cb)
        rospy.Subscriber('string', String, self.string_cb)
        rospy.Subscriber('bool', Bool, self.bool_cb)
        rospy.Subscriber('list', TestArrays, self.list_cb)
        rospy.Subscriber('int', Int64, self.int_cb)
        rospy.Subscriber('float', Float64, self.float_cb)
        rospy.Subscriber('composite', Composite, self.composite_cb)
        
        self.list_msg = self.tilde_msg = self.string_msg = \
                        self.bool_msg = self.int_msg = self.float_msg = \
                        self.composite_msg = None
        self.tests = ['string', 'int', 'bool', 'float', 'composite', 'list']
        
    def string_cb(self, msg):
        self.string_msg = msg
    def int_cb(self, msg):
        self.int_msg = msg
    def bool_cb(self, msg):
        self.bool_msg = msg
    def float_cb(self, msg):
        self.float_msg = msg
    def composite_cb(self, msg):
        self.composite_msg = msg
    def list_cb(self, msg):
        self.list_msg = msg
    def tilde_cb(self, msg):
        self.tilde_msg = msg

    def test_parameter_client_read(self):
        timeout_t = time.time() + 20.
        print("waiting for 20 seconds for client to load")

        tests = self.tests
        msgs = [None]
        while any(1 for m in msgs if m is None) and \
                not rospy.is_shutdown() and \
                timeout_t > time.time():
            time.sleep(0.2)
            msgs = [getattr(self, t+'_msg') for t in tests]

        print("msgs: %s"%(msgs))
        
        self.assertFalse(timeout_t < time.time(), "timeout exceeded")
        self.assertFalse(rospy.is_shutdown(), "node shutdown")            
        self.assertFalse(any(1 for m in msgs if m is None), "did not receive all expected messages: "+str(msgs))

        ns = rospy.get_param('parameter_namespace')
        for t in tests:
            p_name = roslib.names.ns_join(ns, t)
            value = rospy.get_param(p_name, t)
            msg = getattr(self, "%s_msg"%(t))
            print("get param: %s"%(p_name))
            print("param value: %s"%(value))
            print("msg value: %s"%(msg))
            if t == 'composite':
                print("value", p_name, value)
                m = Composite(CompositeA(**value['a']), CompositeB(**value['b']))
                self.assertAlmostEqual(m.a.x, msg.a.x)
                self.assertAlmostEqual(m.a.y, msg.a.y)
                self.assertAlmostEqual(m.a.z, msg.a.z)
                self.assertAlmostEqual(m.a.w, msg.a.w)
                self.assertAlmostEqual(m.b.x, msg.b.x)
                self.assertAlmostEqual(m.b.y, msg.b.y)
                self.assertAlmostEqual(m.b.z, msg.b.z)
            elif t == 'list':
                self.assertEqual(list(value), list(msg.int32_array))
            elif t == 'float':
                self.assertAlmostEqual(value, msg.data)
            else:
                self.assertEqual(value, msg.data)

    def test_set_parameter(self):
        # make sure client copied each parameter correct
        ns_source = rospy.get_param('parameter_namespace')
        ns_target = rospy.get_param('target_namespace')
        tests = self.tests
        for t in tests:
            source_name = roslib.names.ns_join(ns_source, t)
            target_name = roslib.names.ns_join(ns_target, t)
            source_value = rospy.get_param(source_name)
            target_value = rospy.get_param(target_name)
            if t != 'float':
                self.assertEqual(source_value, target_value)
            else:
                self.assertAlmostEqual(source_value, target_value)
        
    def test_tilde_parameter(self):
        timeout_t = time.time() + 20.
        print("waiting for 20 seconds for client to load")
        while self.tilde_msg is None and \
                not rospy.is_shutdown() and \
                timeout_t > time.time():
            time.sleep(0.2)

        self.assertFalse(timeout_t < time.time(), "timeout exceeded")
        self.assertFalse(rospy.is_shutdown(), "node shutdown")            
        self.assertFalse(self.tilde_msg is None)
        
        self.assertEqual(rospy.get_param('param_client/tilde'), self.tilde_msg.data)

if __name__ == '__main__':
    import rostest
    rostest.run(PKG, NAME, TestParameterClient, sys.argv)
