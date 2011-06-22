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
    
_ros_home = None
_rosjava_dir = None
_rosjava_jar = None
_msggen = None
_srvgen = None

def init():
    global _ros_home, _rosjava_dir, _msggen, _srvgen, _rosjava_jar
    _ros_home = roslib.rosenv.get_ros_home()
    _rosjava_dir = roslib.packages.get_pkg_dir('rosjava')
    _rosjava_jar = os.path.join(_rosjava_dir, 'dist', 'rosjava.jar')
    _msggen = os.path.join(_rosjava_dir, 'scripts', 'java_msgs.py')
    _srvgen = os.path.join(_rosjava_dir, 'scripts', 'java_srvs.py')
    
def msggen_source_path(pkg):
    return os.path.join(_ros_home, 'rosjava', 'gen', 'msg', pkg)

def srvgen_source_path(pkg):
    return os.path.join(_ros_home, 'rosjava', 'gen', 'srv', pkg)

def msggen_jar_path():
    return os.path.join(_ros_home, 'rosjava', 'lib')

def msggen_build_path(pkg):
    return os.path.join(_ros_home, 'rosjava', 'build', 'msg', pkg)

def srvgen_build_path(pkg):
    return os.path.join(_ros_home, 'rosjava', 'build', 'srv', pkg)

def get_msg_packages(rospack, package):
    depends = rospack.depends([package])[package]
    packages = []
    for pkg in depends:
        d = roslib.packages.get_pkg_subdir(pkg, roslib.packages.MSG_DIR, False)
        if d and os.path.isdir(d):
            packages.append(pkg)
    return packages

def get_srv_packages(rospack, package):
    depends = rospack.depends([package])[package]
    packages = []
    for pkg in depends:
        d = roslib.packages.get_pkg_subdir(pkg, roslib.packages.SRV_DIR, False)
        if d and os.path.isdir(d):
            packages.append(pkg)
    return packages

def generate_msg_source(package):
    source_path = msggen_source_path(package)
    command = [_msggen, '-o', source_path, package]
    subprocess.check_call(command)

def generate_srv_source(package):
    source_path = srvgen_source_path(package)
    command = [_srvgen, '-o', source_path, package]
    subprocess.check_call(command)
    
def build_msg(package):
    source_path = msggen_source_path(package)
    build_path = msggen_build_path(package)
    
    if not os.path.exists(build_path):
        os.makedirs(build_path)

    java_files = []
    for d, dirs, files in os.walk(source_path, topdown=True):
        java_files.extend([os.path.join(d, f) for f in files if f.endswith('.java')])
    
    command = ['javac', '-d', build_path, '-sourcepath', source_path, '-classpath', _rosjava_jar] + java_files
    subprocess.check_call(command)

def build_srv(package):
    source_path = msggen_source_path(package)
    build_path = srvgen_build_path(package)
    
    if not os.path.exists(build_path):
        os.makedirs(build_path)

    java_files = []
    for d, dirs, files in os.walk(source_path, topdown=True):
        java_files.extend([os.path.join(d, f) for f in files if f.endswith('.java')])
    
    command = ['javac', '-d', build_path, '-sourcepath', source_path, '-classpath', _rosjava_jar] + java_files
    subprocess.check_call(command)

def jar_file_path(package):
    jar_path = msggen_jar_path()
    jar_name = '%s.jar'%(package) #TODO: versioning
    return os.path.join(jar_path, jar_name)
    
def build_jar(package):
    jar_file = jar_file_path(package)
    msg_build_path = msggen_build_path(package)
    srv_build_path = srvgen_build_path(package)
    
    if not os.path.exists(os.path.dirname(jar_file)):
        os.makedirs(os.path.dirname(jar_file))

    # determine .class files to package by examining .java files
    class_files = []
    for d, dirs, files in os.walk(msg_build_path, topdown=True):
        class_files.extend([os.path.join(d, f) for f in files if f.endswith('.class')])
    for d, dirs, files in os.walk(srv_build_path, topdown=True):
        class_files.extend([os.path.join(d, f) for f in files if f.endswith('.class')])
        
    command = ['jar', 'cvf', jar_file] + class_files
    print "generating jar file %s"%(jar_file)
    subprocess.check_call(command)
    
def get_up_to_date():
    jar_path = msggen_jar_path()
    up_to_date = []
    if not os.path.exists(jar_path):
        return up_to_date
    for f in os.listdir(jar_path):
        if f.endswith('.jar'):
            up_to_date.append(f[:-4])
    return up_to_date
    
def wipe_msg_depends(package):
    init()
    
    rospack = roslib.packages.ROSPackages()
    msg_packages = get_msg_packages(rospack, package)
    srv_packages = get_srv_packages(rospack, package)

    to_delete = []
    
    for pkg in msg_packages:
        to_delete.append(msggen_source_path(pkg))
        to_delete.append(msggen_build_path(pkg))
    for pkg in srv_packages:
        to_delete.append(srvgen_source_path(pkg))
        to_delete.append(srvgen_build_path(pkg))
    for pkg in set(msg_packages)  | set(srv_packages):
        to_delete.append(jar_file_path(pkg))

    to_delete = [x for x in to_delete if os.path.exists(x)]
    for f in to_delete:
        print "deleting", f
        if os.path.isfile(f):
            os.remove(f)
        else:
            shutil.rmtree(f)
            
def generate_msg_depends(package):
    init()

    rospack = roslib.packages.ROSPackages()

    up_to_date = get_up_to_date()

    msg_packages = get_msg_packages(rospack, package)
    srv_packages = get_srv_packages(rospack, package)

    # subtract packages for which jar files already exist
    msg_packages = [p for p in msg_packages if not p in up_to_date]
    srv_packages = [p for p in srv_packages if not p in up_to_date]
    
    # generate .java
    for pkg in msg_packages:
        generate_msg_source(pkg)

    for pkg in srv_packages:
        generate_srv_source(pkg)

    # .java -> .class
    for pkg in msg_packages:
        build_msg(pkg)

    for pkg in srv_packages:
        build_srv(pkg)

    # .class -> .jar
    for pkg in set(msg_packages) | set(srv_packages):
        build_jar(pkg)

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
