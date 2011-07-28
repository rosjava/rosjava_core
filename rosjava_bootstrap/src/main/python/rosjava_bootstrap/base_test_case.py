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
import pprint
import unittest

import resources


class BaseTestCase(unittest.TestCase):
    
    @classmethod
    def setUpClass(cls):
        resources_directory = resources.get_resources_directory()
        os.environ['ROS_PACKAGE_PATH'] = (
            resources_directory + os.path.pathsep + os.environ['ROS_PACKAGE_PATH'])
        
    def assertListEqual(self, first, second):
        for i, e in enumerate(first):
            if i >= len(second):
                pprint.pprint(first)
                pprint.pprint(second)
                self.fail('len(first) != len(second)')
            if e != second[i]:
                self.fail('At index %d, %r != %r' % (i, e, second[i]))
        if len(first) != len(second):
            pprint.pprint(first)
            pprint.pprint(second)
            self.fail('len(first) != len(second)')
