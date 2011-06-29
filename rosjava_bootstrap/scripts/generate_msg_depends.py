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
import shutil
import subprocess

import roslib.rosenv
import roslib.packages
import roslib.stacks

def usage():
    print "generate_msg_depends.py <package-name>"
    sys.exit(os.EX_USAGE)
    
PKG = 'rosjava_bootstrap'

_ros_home = roslib.rosenv.get_ros_home()
_pkg_dir = roslib.packages.get_pkg_dir(PKG)
_bootstrap_jar = os.path.join(_pkg_dir, 'dist', 'rosjava-bootstrap.jar')
_build_file = os.path.join(_pkg_dir, 'scripts', 'build-msg.xml')
_properties_dir = os.path.join(_ros_home, 'rosjava', 'properties')
    
def msggen_source_path(pkg):
    return os.path.join(_ros_home, 'rosjava', 'gen', 'msg', pkg)

def srvgen_source_path(pkg):
    return os.path.join(_ros_home, 'rosjava', 'gen', 'srv', pkg)

def msggen_jar_path():
    return os.path.join(_ros_home, 'rosjava', 'lib')

def msg_jar_file_path(package):
    return os.path.join(_ros_home, 'rosjava', 'lib', '%s.jar'%package)

def is_msg_pkg(pkg):
    d = roslib.packages.get_pkg_subdir(pkg, roslib.packages.MSG_DIR, False)
    return bool(d and os.path.isdir(d))
    
def is_srv_pkg(pkg):
    d = roslib.packages.get_pkg_subdir(pkg, roslib.packages.SRV_DIR, False)
    return bool(d and os.path.isdir(d))

def get_msg_packages(rospack, package):
    depends = rospack.depends([package])[package]
    return [pkg for pkg in depends if is_msg_pkg(pkg)]

def get_srv_packages(rospack, package):
    depends = rospack.depends([package])[package]
    return [pkg for pkg in depends if is_srv_pkg(pkg)]

def get_up_to_date():
    jar_path = msggen_jar_path()
    up_to_date = []
    if not os.path.exists(jar_path):
        return up_to_date
    for f in os.listdir(jar_path):
        if f.endswith('.jar'):
            up_to_date.append(f[:-4])
    return up_to_date
    
def _generate_msgs(rospack, package, up_to_date):
    depends = get_msg_packages(rospack, package)
    unbuilt = [d for d in depends if not d in up_to_date]
    for u in unbuilt:
        _generate_msgs(rospack, u, up_to_date)
        
    # generate classpath, safe-encode
    classpath = get_msg_classpath(rospack, package)
    classpath = classpath.replace(':', '\:')
        
    # generate properties for ant
    properties_file = os.path.join(_properties_dir, 'build-%s.properties'%(package))
    if not os.path.exists(os.path.dirname(properties_file)):
        os.makedirs(os.path.dirname(properties_file))
    with open(properties_file, 'w') as f:
        f.write("""ros.package=%s
ros.home=%s
ros.classpath=%s"""%(package, _ros_home, classpath))

    # call ant to build everything
    # - add to up_to_date regardless to prevent infinite loop
    up_to_date.append(package)

    command = ['ant', '-f', _build_file,
               '-Dproperties=%s'%(properties_file)]
    subprocess.check_call(command)
    
def generate_msg_depends(package):
    rospack = roslib.packages.ROSPackages()

    up_to_date = get_up_to_date()

    msg_packages = get_msg_packages(rospack, package)
    srv_packages = get_srv_packages(rospack, package)

    # subtract packages for which jar files already exist
    msg_packages = [p for p in msg_packages if not p in up_to_date]
    srv_packages = [p for p in srv_packages if not p in up_to_date]
    
    # let ant do the rest
    for p in set(msg_packages + srv_packages):
        _generate_msgs(rospack, p, up_to_date)
    
def get_msg_classpath(rospack, package):
    def resolve_pathelements(pathelements):
        return [os.path.abspath(p) for p in pathelements]

    depends = rospack.depends([package])[package]
    pathelements = [_bootstrap_jar]
    for pkg in depends:
        if is_msg_pkg(pkg) or is_srv_pkg(pkg):
            pathelements.append(msg_jar_file_path(pkg))
    return os.pathsep.join(resolve_pathelements(pathelements))

def wipe_msg_depends(package):
    rospack = roslib.packages.ROSPackages()
    
    msg_packages = get_msg_packages(rospack, package)
    srv_packages = get_srv_packages(rospack, package)

    for p in set(msg_packages + srv_packages):
        # call ant to delete build artifacts
        command = ['ant', '-f', _build_file,
                   '-Dproperties=%s'%(properties_file),
                   'clean']
        subprocess.check_call(command)
    
from optparse import OptionParser
def generate_msg_depends_main(argv=None):
    parser = OptionParser(usage="usage: %prog [options] <package>", prog='generate_msg_depends.py')
    parser.add_option('--wipe', default=False, action="store_true", dest="wipe")
    if argv is None:
        argv = sys.argv[1:]
        
    options, args = parser.parse_args(argv)
    if len(args) != 1:
        parser.error("you may only specify one package argument")

    package = args[0]
    if options.wipe:
        wipe_msg_depends(package)
    else:
        generate_msg_depends(package)        
    
if __name__ == '__main__':
    generate_msg_depends_main()
