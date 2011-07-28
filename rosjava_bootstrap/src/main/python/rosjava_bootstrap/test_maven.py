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

import base_test_case
import maven
import roslib

SAMPLE_PACKAGE = 'sample_package'
SAMPLE_PACKAGE_DEPENDENCY = 'sample_package_dependency'


class TestMaven(base_test_case.BaseTestCase):

    def test_walk_export_path_dependencies_only(self):
        rospack = roslib.packages.ROSPackages()
        exports = []

        def export_operator(package, package_directory, export):
            exports.append(export.attrs)
            self.assertEqual(SAMPLE_PACKAGE_DEPENDENCY, package)

        def package_operator(package):
            self.assertEqual(SAMPLE_PACKAGE_DEPENDENCY, package)
        
        maven.walk_export_path(rospack, SAMPLE_PACKAGE, export_operator, package_operator,
                               include_package=False)
        expected_exports = [
                {'version': '0.0.0', 'groupId': 'com.domain',
                 'artifactId': 'com.domain.sample_dependency'},
                {'version': '0.0.0', 'location': 'target/', 'groupId': 'com.domain',
                 'artifactId': 'com.domain.sample_dependency.with_location'},
                {'groupId': 'com.domain', 'location': 'target/', 'built': 'True','version': '0.0.0',
                 'artifactId': 'com.domain.sample_dependency.built_with_location'},
                ]
        self.assertListEqual(expected_exports, exports)

    def test_walk_export_path(self):
        rospack = roslib.packages.ROSPackages()
        exports = []

        def export_operator(package, package_directory, export):
            exports.append(export.attrs)
            self.assertTrue(package in (SAMPLE_PACKAGE, SAMPLE_PACKAGE_DEPENDENCY))

        def package_operator(package):
            self.assertTrue(package in (SAMPLE_PACKAGE, SAMPLE_PACKAGE_DEPENDENCY))

        maven.walk_export_path(rospack, SAMPLE_PACKAGE, export_operator, package_operator,
                               include_package=True)
        expected_exports = [
                {'version': '0.0.0', 'groupId': 'com.domain',
                 'artifactId': 'com.domain.sample_dependency'},
                {'version': '0.0.0', 'location': 'target/', 'groupId': 'com.domain',
                 'artifactId': 'com.domain.sample_dependency.with_location'},
                {'groupId': 'com.domain', 'location': 'target/', 'built': 'True',
                 'version': '0.0.0',
                 'artifactId': 'com.domain.sample_dependency.built_with_location'},
                {'version': '0.0.0', 'groupId': 'com.domain', 'artifactId': 'com.domain.sample'},
                {'version': '0.0.0', 'location': 'target/', 'groupId': 'com.domain',
                 'artifactId': 'com.domain.sample.with_location'},
                ]
        self.assertListEqual(expected_exports, exports)
 
    def test_get_package_build_artifact(self):
        rospack = roslib.packages.ROSPackages()
        artifact = maven.get_package_build_artifact(rospack, SAMPLE_PACKAGE)
        self.assertEqual('target/com.domain.sample.built_with_location-0.0.0.jar', artifact)
        artifact = maven.get_package_build_artifact(rospack, SAMPLE_PACKAGE_DEPENDENCY)
        self.assertEqual('target/com.domain.sample_dependency.built_with_location-0.0.0.jar', artifact)