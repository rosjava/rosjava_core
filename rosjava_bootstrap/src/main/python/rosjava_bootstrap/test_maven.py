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
import maven
import roslib

SAMPLE_PACKAGE = 'sample_package'
SAMPLE_PACKAGE_DEPENDENCY = 'sample_package_dependency'


class TestMaven(base_test_case.BaseTestCase):
    
    def test_get_specified_classpath_dependencies_only(self):
        rospack = roslib.packages.ROSPackages()
        path = maven._get_specified_classpath(rospack, SAMPLE_PACKAGE, False, 'compile')
        self.assertEqual(2, len(path))
        basenames = [os.path.basename(x) for x in path]
        expected_basenames = [
                'com.domain.sample_dependency.with_location-0.0.0.jar',
                'com.domain.sample_dependency.built_with_location-0.0.0.jar',
                ]
        self.assertListEqual(expected_basenames, basenames)

    def test_get_specified_classpath(self):
        rospack = roslib.packages.ROSPackages()
        path = maven._get_specified_classpath(rospack, SAMPLE_PACKAGE, True, 'compile')
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
        path = maven.get_classpath(rospack, SAMPLE_PACKAGE, {'compile': []})
        jars = [os.path.basename(x) for x in path.split(':')]
        expected_jars = [
                'com.domain.sample_dependency.with_location-0.0.0.jar',
                'com.domain.sample_dependency.built_with_location-0.0.0.jar']
        self.assertListEqual(expected_jars, jars)
        
    def test_walk_export_path_dependencies_only(self):
        rospack = roslib.packages.ROSPackages()
        exports = []

        def export_operator(package, package_directory, export):
            exports.append(export.attrs)
            self.assertEqual(SAMPLE_PACKAGE_DEPENDENCY, package)

        def package_operator(package):
            self.assertEqual(SAMPLE_PACKAGE_DEPENDENCY, package)
        
        maven._map_package_dependencies(rospack, SAMPLE_PACKAGE, export_operator, package_operator)
        expected_exports = [
                {'version': '0.0.0', 'groupId': 'com.domain',
                 'artifactId': 'com.domain.sample_dependency'},
                {'version': '0.0.0', 'location': 'target/', 'groupId': 'com.domain',
                 'artifactId': 'com.domain.sample_dependency.with_location'},
                {'groupId': 'com.domain', 'location': 'target/', 'built': 'True','version': '0.0.0',
                 'artifactId': 'com.domain.sample_dependency.built_with_location'},
                ]
        self.assertListEqual(expected_exports, exports)

    def test_map_package_exports(self):
        rospack = roslib.packages.ROSPackages()
        exports = []

        def export_operator(package, package_directory, export):
            exports.append(export.attrs)
            self.assertTrue(package in (SAMPLE_PACKAGE, SAMPLE_PACKAGE_DEPENDENCY))

        def package_operator(package):
            self.assertTrue(package in (SAMPLE_PACKAGE, SAMPLE_PACKAGE_DEPENDENCY))

        maven._map_package_exports(rospack, SAMPLE_PACKAGE, export_operator)
        
        expected_exports = [
                {'version': '0.0.0', 'groupId': 'com.domain', 'artifactId': 'com.domain.sample'},
                {'version': '0.0.0', 'location': 'target/', 'groupId': 'com.domain',
                 'artifactId': 'com.domain.sample.with_location'},
                {'version': '0.0.0', 'location': 'target/', 'groupId': 'com.domain',
                 'built': 'True', 'artifactId': 'com.domain.sample.built_with_location'},
                ]
        self.assertListEqual(expected_exports, exports)
 
    def test_get_package_build_artifact(self):
        rospack = roslib.packages.ROSPackages()
        artifact = maven.get_package_build_artifact(rospack, SAMPLE_PACKAGE)
        self.assertEqual('target/com.domain.sample.built_with_location-0.0.0.jar', artifact)
        artifact = maven.get_package_build_artifact(rospack, SAMPLE_PACKAGE_DEPENDENCY)
        self.assertEqual('target/com.domain.sample_dependency.built_with_location-0.0.0.jar', artifact)