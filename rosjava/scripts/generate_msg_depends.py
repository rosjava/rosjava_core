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
    return os.path.join(_ros_home, 'rosjava', 'msg_gen', pkg)

def srvgen_source_path(pkg):
    return os.path.join(_ros_home, 'rosjava', 'srv_gen', pkg)

def msggen_jar_path():
    return os.path.join(_ros_home, 'rosjava', 'lib')

def msggen_build_path():
    return os.path.join(_ros_home, 'rosjava', 'build')

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
    build_path = msggen_build_path()
    
    if not os.path.exists(build_path):
        os.makedirs(build_path)

    java_files = []
    for d, dirs, files in os.walk(source_path, topdown=True):
        java_files.extend([os.path.join(d, f) for f in files if f.endswith('.java')])
    
    command = ['javac', '-d', build_path, '-sourcepath', source_path, '-classpath', _rosjava_jar] + java_files
    subprocess.check_call(command)

def build_srv(package):
    source_path = msggen_source_path(package)
    build_path = msggen_build_path()
    
    if not os.path.exists(build_path):
        os.makedirs(build_path)

    java_files = []
    for d, dirs, files in os.walk(source_path, topdown=True):
        java_files.extend([os.path.join(d, f) for f in files if f.endswith('.java')])
    
    command = ['javac', '-d', build_path, '-sourcepath', source_path, '-classpath', _rosjava_jar] + java_files
    subprocess.check_call(command)

def build_jar(package):
    jar_path = msggen_jar_path()
    jar_name = '%s.jar'%(package) #TODO: versioning
    jar_file = os.path.join(jar_path, jar_name)
    build_path = msggen_build_path()
    source_path = msggen_source_path(package)
    
    if not os.path.exists(jar_path):
        os.makedirs(jar_path)

    # determine .class files to package by examining .java files
    java_files = []
    for d, dirs, files in os.walk(source_path, topdown=True):
        java_files.extend([os.path.join(d, f) for f in files if f.endswith('.java')])
    print "source_path", source_path
    print "JAVA_FILES", java_files
        
    class_files = []
    for f in java_files:
        class_file = f[:-5] + '.class'
        class_file = build_path + class_file[len(source_path):]
        class_files.append(class_file)
        
    command = ['jar', 'cvf', jar_file] + class_files
    print "JAR", command
    subprocess.check_call(command)
    
def get_up_to_date():
    jar_path = msggen_jar_path()
    up_to_date = []
    for f in os.listdir(jar_path):
        if f.endswith('.jar'):
            up_to_date.append(f[:-4])
    return up_to_date
    
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

def generate_msg_depends_main(argv=None):
    if argv is None:
        argv = sys.argv
    if len(argv) != 2:
        usage()

    generate_msg_depends()
    
if __name__ == '__main__':
    generate_msg_depends_main()
