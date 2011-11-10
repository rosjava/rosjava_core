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
import subprocess
import sys

import android
import eclipse
import generate_msg_depends
import roslib
import ros_properties
import maven


def _usage():
    print 'make.py <package-name> [clean]'
    sys.exit(os.EX_USAGE)


def _write_sorted_properties(properties, stream=sys.stdout):
    for key in sorted(properties):
        print >>stream, '%s=%s' % (key, properties[key])


def _remove(path):
    try:
        os.remove(path)
    except:
        print 'Failed to remove %r' % path


def _run(command, checked=True):
    print 'Executing command: %r' % command
    retcode = subprocess.call(command)
    if retcode and checked:
        sys.exit(retcode)


def build(rospack, package):
    with open('dependencies.xml', 'w') as stream:
        maven.write_ant_maven_dependencies(rospack, package, stream)

    maven_depmap = maven.get_maven_dependencies(package, 'dependencies.xml')

    properties = ros_properties.generate(rospack, package, maven_depmap)
    with open('ros.properties', 'w') as stream:
        _write_sorted_properties(properties, stream)

    properties = android.generate_properties(rospack, package)
    if properties is not None:
        with open('project.properties', 'w') as stream:
            _write_sorted_properties(properties, stream)

    generate_msg_depends.generate_msg_depends(package)

    with open('.classpath', 'w') as stream:
        eclipse.write_classpath(rospack, package, maven_depmap, stream)

    with open('.project', 'w') as stream:
        eclipse.write_project(package, stream)

    _run(['ant'])


def clean():
    _run(['ant', 'clean'], checked=False)
    _remove('dependencies.xml')
    _remove('ros.properties')
    _remove('default.properties')
    _remove('.classpath')
    _remove('.project')


def test():
    _run(['ant', 'test'])


def main(argv):
    if len(argv) < 2:
        _usage()
    package = argv[1]
    rospack = roslib.packages.ROSPackages()
    if len(argv) == 2:
        build(rospack, package)
    elif argv[2] == 'clean':
        clean()
    elif argv[2] == 'test':
        test()


if __name__ == '__main__':
    main(sys.argv)
