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
import unittest

import roslib


class BaseTestCase(unittest.TestCase):
    
    @classmethod
    def setUpClass(cls):
        package_directory = roslib.packages.get_pkg_dir('rosjava_bootstrap')
        resources_directory = os.path.join(
            package_directory, 'src', 'main', 'resources')
        os.environ['ROS_PACKAGE_PATH'] = (
            resources_directory + os.path.pathsep + os.environ['ROS_PACKAGE_PATH'])
        BaseTestCase._resources_directory = resources_directory
        
    def get_resources_directory(self):
        return BaseTestCase._resources_directory