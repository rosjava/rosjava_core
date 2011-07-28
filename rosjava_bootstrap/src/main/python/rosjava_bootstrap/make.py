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
import sys

import generate_android_properties
import generate_ros_properties
import roslib
import maven


def _usage():
    print 'make.py <package-name>'
    sys.exit(os.EX_USAGE)


def write_sorted_properties(properties, stream=sys.stdout):
    for key in sorted(properties):
        print >>stream, '%s=%s' % (key, properties[key])


def main(argv):
    if len(argv) != 2:
        _usage()
    package_name = argv[1]
    rospack = roslib.packages.ROSPackages()
    
    with open('dependencies.xml', 'w') as stream:
        maven.write_ant_maven_dependencies(rospack, package_name, stream)
        
    maven_depmap = maven.get_maven_dependencies(package_name, 'dependencies.xml')
    properties = generate_ros_properties.generate_properties(rospack, package_name, maven_depmap)
    with open('ros.properties', 'w') as stream:
        write_sorted_properties(properties, stream)
        
    properties = generate_android_properties.generate_properties(rospack, package_name)
    if properties is not None:
        with open('default.properties', 'w') as stream:
            write_sorted_properties(properties, stream)


if __name__ == '__main__':
    main(sys.argv)