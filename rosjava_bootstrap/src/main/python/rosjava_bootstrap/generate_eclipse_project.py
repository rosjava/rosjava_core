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

import android
import classpath
import maven
import roslib

# XML tag for the rosjava manifest tag for path elements.
TAG_ROSJAVA_PATHELEMENT = 'rosjava-pathelement'
# XML tag for the rosjava manifest tag for source elements.
TAG_ROSJAVA_SRC = 'rosjava-src'


def usage():
    print "generate_eclipse_project.py <package-name>"
    sys.exit(os.EX_USAGE)


def _get_source_paths(rospack, package):
    """
    @return: list of source paths. Source paths will be returned in the
    relative specification used in the ros manifest.xml file.
    @rtype: [str]
    """
    rospack.load_manifests([package])
    m = rospack.manifests[package]
    return [x.attrs['location'] for x in m.exports if x.tag == TAG_ROSJAVA_SRC]


def generate_classpath_file(rospack, package, maven_depmap, stream=sys.stdout):
    print >>stream, '<?xml version="1.0" encoding="UTF-8"?>\n<classpath>'
    # TODO(damonkohler): Move Eclipse .project file generation into this
    # script as well so that we can alter it for use with Android.
    if android.is_android_package(package):
        print >>stream, ('\t<classpathentry kind="con" '
                         'path="com.android.ide.eclipse.adt.ANDROID_FRAMEWORK"/>')
    for p in filter(None, _get_source_paths(rospack, package)):
        print >>stream, '\t<classpathentry kind="src" path="%s"/>' % (p)
    print >>stream, '\t<classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>'
    print >>stream, '\t<classpathentry kind="con" path="org.eclipse.jdt.junit.JUNIT_CONTAINER/4"/>'
    for p in filter(None, classpath.get_classpath(
            rospack, package, maven_depmap, include_package=True, scope='all').split(':')):
        print >>stream, '\t<classpathentry kind="lib" path="%s"/>' % (p)
    print >>stream, '\t<classpathentry kind="output" path="build"/>\n</classpath>'


def main(argv):
    if len(argv) != 2:
        usage()
    package = argv[1]
    rospack = roslib.packages.ROSPackages()
    maven_depmap = maven.get_maven_dependencies(package, 'dependencies.xml')
    generate_classpath_file(rospack, package, maven_depmap)


if __name__ == '__main__':
    try:
        main(sys.argv)
    except roslib.packages.InvalidROSPkgException as e:
        sys.stderr.write('ERROR: '+str(e)+'\n')
        sys.exit(1)
