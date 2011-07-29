#! /usr/bin/env python

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
#

__authors__ = 'astambler@willowgarage.com (Adam Stambler), damonkohler@google.com (Damon Kohler)'

import os
import sys
import subprocess

import maven
import roslib


def _usage():
    print """
This python script runs rosjava based jars and bootstraps the classpath
for the node by looking at its package manifest.

rosrun rosjava_bootstrap run.py <pkg> <node_class>  [args ... ]
"""
    sys.exit(os.EX_USAGE)


def _build_command(rospack, maven_depmap, package, node_class, args):
    classpath = maven.get_classpath(rospack, package, maven_depmap, scope='runtime',
                                    include_package=True)
    command = ['java', '-classpath', classpath, 'org.ros.RosRun', node_class]
    command.extend(args)
    return command


def main(argv):
    if len(argv) < 3:
        _usage()
    package = sys.argv[1]
    node_class = sys.argv[2]
    args = sys.argv[3:]
    rospack = roslib.packages.ROSPackages()
    maven_depmap = maven.get_maven_dependencies(package, 'dependencies.xml')
    command = _build_command(rospack, maven_depmap, package, node_class, args) 
    print 'Executing command: %r' % command
    return_code = 1
    try:
        return_code = subprocess.call(command)
    except KeyboardInterrupt:
        pass
    finally:
        sys.exit(return_code)
    
    
if __name__ == '__main__':
    main(sys.argv)