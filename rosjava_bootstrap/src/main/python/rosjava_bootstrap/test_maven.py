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

    def test_walk_export_path(self):
        rospack = roslib.packages.ROSPackages()

        def export_operator(package, package_directory, export):
            pass

        def package_operator(package):
            pass

        maven.walk_export_path(rospack, SAMPLE_PACKAGE, export_operator, package_operator)

    def test_get_package_build_artifact(self):
        rospack = roslib.packages.ROSPackages()
        artifact = maven.get_package_build_artifact(rospack, SAMPLE_PACKAGE)
        self.assertEqual('target/com.domain.sample.built_with_location-0.0.0.jar', artifact)
        artifact = maven.get_package_build_artifact(rospack, SAMPLE_PACKAGE_DEPENDENCY)
        self.assertEqual('target/com.domain.sample_dependency.built_with_location-0.0.0.jar', artifact)