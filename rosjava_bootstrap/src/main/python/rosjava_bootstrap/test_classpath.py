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
import classpath
import roslib

SAMPLE_PACKAGE = 'sample_package'


class TestClasspath(base_test_case.BaseTestCase):

    def test_get_specified_classpath_dependencies_only(self):
        rospack = roslib.packages.ROSPackages()
        path = classpath._get_specified_classpath(rospack, SAMPLE_PACKAGE, False, 'compile')
        self.assertEqual(2, len(path))
        basenames = [os.path.basename(x) for x in path]
        expected_basenames = [
                'com.domain.sample_dependency.with_location-0.0.0.jar',
                'com.domain.sample_dependency.built_with_location-0.0.0.jar',
                ]
        self.assertListEqual(expected_basenames, basenames)

    def test_get_specified_classpath(self):
        rospack = roslib.packages.ROSPackages()
        path = classpath._get_specified_classpath(rospack, SAMPLE_PACKAGE, True, 'compile')
        self.assertEqual(3, len(path))
        basenames = [os.path.basename(x) for x in path]
        expected_basenames = [
                'com.domain.sample.with_location-0.0.0.jar',
                'com.domain.sample_dependency.with_location-0.0.0.jar',
                'com.domain.sample_dependency.built_with_location-0.0.0.jar',
                ]
        self.assertListEqual(expected_basenames, basenames)

    def test_get_classpath(self):
        rospack = roslib.packages.ROSPackages()
        path = classpath.get_classpath(rospack, SAMPLE_PACKAGE, {'compile': []})
        jars = [os.path.basename(x) for x in path.split(':')]
        expected_jars = [
                'com.domain.sample_dependency.with_location-0.0.0.jar',
                'com.domain.sample_dependency.built_with_location-0.0.0.jar']
        self.assertListEqual(expected_jars, jars)