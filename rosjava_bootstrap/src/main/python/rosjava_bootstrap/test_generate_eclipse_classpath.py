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
import generate_eclipse_classpath
import roslib
import StringIO

SAMPLE_PACKAGE = 'sample_package'


class TestGenerateEclipseClasspath(base_test_case.BaseTestCase):
    
    def test_get_source_paths(self):
        rospack = roslib.packages.ROSPackages()
        paths = generate_eclipse_classpath._get_source_paths(rospack, SAMPLE_PACKAGE)
        self.assertEqual(['src/main/java'], paths)

    def test_generate_classpath_file(self):
        rospack = roslib.packages.ROSPackages()
        stream = StringIO.StringIO()
        generate_eclipse_classpath.generate_classpath_file(
                rospack, SAMPLE_PACKAGE, {'all': []}, stream)
        # TODO(damonkohler): Actually test the content of the generated classpath file.
        self.assertTrue(stream.getvalue())