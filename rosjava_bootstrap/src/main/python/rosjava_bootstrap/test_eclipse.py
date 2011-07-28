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
import eclipse
import roslib
import StringIO

SAMPLE_PACKAGE = 'sample_package'


class TestEclipse(base_test_case.BaseTestCase):
    
    def test_get_source_paths(self):
        rospack = roslib.packages.ROSPackages()
        paths = eclipse._get_source_paths(rospack, SAMPLE_PACKAGE)
        self.assertEqual(['src/main/java'], paths)

    def test_write_classpath(self):
        rospack = roslib.packages.ROSPackages()
        stream = StringIO.StringIO()
        eclipse.write_classpath(rospack, SAMPLE_PACKAGE, {'compile': [], 'test': [], 'runtime': []},
                                stream)
        # TODO(damonkohler): Actually test the content of the generated classpath file.
        self.assertTrue(stream.getvalue())