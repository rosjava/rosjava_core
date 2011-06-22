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

import roslib.rosenv
import roslib.packages
import roslib.stacks

from generate_msg_depends import msg_jar_file_path, is_msg_pkg, is_srv_pkg

def usage():
    print "generate_properties.py <package-name>"
    sys.exit(os.EX_USAGE)
    
def resolve_pathelements(pathelements):
    # TODO: potentially recognize keys like ROS_HOME
    return [os.path.abspath(p) for p in pathelements]

def get_classpath(package):
    rospack = roslib.packages.ROSPackages()
    depends = rospack.depends([package])[package]
    pathelements = []
    for pkg in depends:
        m = rospack.manifests[pkg]
        pkg_dir = roslib.packages.get_pkg_dir(pkg)
        for e in [x for x in m.exports if x.tag == 'rosjava-pathelement']:
            try:
                pathelements.append(os.path.join(pkg_dir, e.attrs['location']))
            except KeyError:
                print >> sys.stderr, "Invalid <rosjava-pathelement> tag in package %s"%(pkg)
        if is_msg_pkg(pkg) or is_srv_pkg(pkg):
            pathelements.append(msg_jar_file_path(pkg))
    return os.pathsep.join(resolve_pathelements(pathelements))

def generate_properties_main(argv=None):
    if argv is None:
        argv = sys.argv
    if len(argv) != 2:
        usage()
    package = argv[1]
    print 'ros.home=%s'%(roslib.rosenv.get_ros_home())
    print 'ros.classpath=%s'%(get_classpath(package).replace(':', '\:'))
    
if __name__ == '__main__':
    generate_properties_main()
