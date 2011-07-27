#!/usr/bin/env python

# Software License Agreement (BSD License)
#
# Copyright (c) 2011, Willow Garage, Inc.
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions
# are met:
#
#  * Redistributions of source code must retain the above copyright
#    notice, this list of conditions and the following disclaimer.
#  * Redistributions in binary form must reproduce the above
#    copyright notice, this list of conditions and the following
#    disclaimer in the documentation and/or other materials provided
#    with the distribution.
#  * Neither the name of Willow Garage, Inc. nor the names of its
#    contributors may be used to endorse or promote products derived
#    from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
# "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
# LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
# FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
# COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
# INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
# BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
# LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
# ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.

import os
import sys

import classpath
import maven
import roslib


_stack_of_cache = {}
_stack_version_cache = {}
def get_package_version(package, stack_of_cache=None, stack_version_cache=None):
    if stack_of_cache is None:
        stack_of_cache = _stack_of_cache
    if stack_version_cache is None:
        stack_version_cache = _stack_version_cache
    stack_of_cache[package] = stack = stack_of_cache.get(package) or roslib.stacks.stack_of(package)
    stack_version_cache[stack] = version = (
            stack_version_cache.get(stack) or
            roslib.stacks.get_stack_version(stack)) #@UndefinedVariable
    return version


def generate_properties(rospack, package, maven_depmap):
    depends = rospack.depends([package])[package]
    properties = {'ros.home': roslib.rosenv.get_ros_home()}

    # Add directory properties for every package we depend on.
    for dependency in depends:
        dependency_directory = roslib.packages.get_pkg_dir(dependency)
        properties['ros.pkg.%s.dir' % (dependency)] = dependency_directory
        if hasattr(roslib.stacks, 'get_stack_version'):
            properties['ros.pkg.%s.version' % (dependency)] = get_package_version(dependency)

    built_artifact = maven.get_package_build_artifact(rospack, package)
    if built_artifact:
        properties['ros.artifact.built'] = built_artifact

    properties['ros.compile.classpath'] = classpath.get_classpath(
            rospack, package, maven_depmap, scope='compile')
    properties['ros.runtime.classpath'] = classpath.get_classpath(
            rospack, package, maven_depmap, scope='runtime')
    properties['ros.test.classpath'] = classpath.get_classpath(
            rospack, package, maven_depmap, scope='test')

    # Re-encode for ant <fileset includes="${ros.jarfileset}">.  uses comma separator instead.
    for prop in ['ros.compile', 'ros.runtime', 'ros.test']:
        properties[prop + '.jarfileset'] = properties[prop + '.classpath'].replace(':', ',')

    properties['ros.test_results'] = os.path.join(roslib.rosenv.get_test_results_dir(), package)
    return properties


def print_sorted_properties(properties, stream=sys.stdout):
    for key in sorted(properties):
        print >>stream, '%s=%s' % (key, properties[key])


def _usage():
    print 'generate_ros_properties.py <package-name>'
    sys.exit(os.EX_USAGE)


def main(argv):
    if len(argv) != 2:
        _usage()
    package = argv[1]
    rospack = roslib.packages.ROSPackages()
    maven_depmap = maven.get_maven_dependencies(package, 'dependencies.xml')
    properties = generate_properties(rospack, package, maven_depmap)
    print_sorted_properties(properties)


if __name__ == '__main__':
    try:
        main(sys.argv)
    except roslib.packages.InvalidROSPkgException as e:
        print >>sys.stderr, 'ERROR: %s' % str(e)
        sys.exit(1)
