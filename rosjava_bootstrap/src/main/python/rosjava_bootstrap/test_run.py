#!/usr/bin/python

# Copyright (C) 2011 Google Inc.
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

__author__ = 'damonkohler@google.com (Damon Kohler)'

import os

import base_test_case
import roslib
import run


class TestRun(base_test_case.BaseTestCase):
    
    def test_build_command(self):
        rospack = roslib.packages.ROSPackages()
        maven_depmap = {'runtime': []}
        command = run._build_command(rospack, maven_depmap, 'sample_package', 'org.ros.FooNode',
                                     ('some', 'args'))
        expected_command = [
                'java',
                '-classpath', 
                'com.domain.sample.with_location-0.0.0.jar',
                'com.domain.sample.built_with_location-0.0.0.jar',
                'com.domain.sample_dependency.with_location-0.0.0.jar',
                'com.domain.sample_dependency.built_with_location-0.0.0.jar',
                'org.ros.RosRun',
                'org.ros.FooNode',
                'some',
                'args']
        self.assertListEqual(expected_command[:2], command[:2])
        self.assertListEqual(expected_command[2:6],
                             [os.path.basename(x) for x in command[2].split(':')])
        self.assertListEqual(expected_command[6:], command[3:])
