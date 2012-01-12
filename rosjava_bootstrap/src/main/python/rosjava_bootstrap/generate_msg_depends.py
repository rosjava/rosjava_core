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
from optparse import OptionParser

import roslib
   
BOOTSTRAP_PKG = 'rosjava_bootstrap'

_ros_home = roslib.rosenv.get_ros_home()
_bootstrap_pkg_dir = roslib.packages.get_pkg_dir(BOOTSTRAP_PKG)
_scripts_dir = os.path.join(_bootstrap_pkg_dir, 'scripts')
# TODO(keith): need to calculate this when we have versions
_bootstrap_jar = os.path.join(_bootstrap_pkg_dir, 'target', 'org.ros.rosjava.rosjava_bootstrap-0.0.0.jar')
_build_file = os.path.join(_bootstrap_pkg_dir, 'scripts', 'build-msg.xml')
_properties_dir = os.path.join(_ros_home, 'rosjava', 'properties')
_parent_pom = os.path.join(_bootstrap_pkg_dir, '..', 'pom.xml')
_maven_dir = os.path.join(_ros_home, 'rosjava', 'maven')
    
def msggen_source_path(pkg):
    return os.path.join(_ros_home, 'rosjava', 'gen', 'msg', pkg)

def srvgen_source_path(pkg):
    return os.path.join(_ros_home, 'rosjava', 'gen', 'srv', pkg)

def msggen_jar_path():
    return os.path.join(_ros_home, 'rosjava', 'lib')

def msg_jar_file_path(package):
    # TODO(keith): once versioning jars need to get this info from package
    return os.path.join(_ros_home, 'rosjava', 'lib', 'org.ros.rosjava.%s-0.0.0.jar'%package)

def is_msg_pkg(pkg):
    d = roslib.packages.get_pkg_subdir(pkg, roslib.packages.MSG_DIR, False)
    return bool(d and os.path.isdir(d))
    
def is_srv_pkg(pkg):
    d = roslib.packages.get_pkg_subdir(pkg, roslib.packages.SRV_DIR, False)
    return bool(d and os.path.isdir(d))

def get_msg_packages(rospack, package):
    depends = rospack.depends([package])[package]
    # temporary workaround until ROS 1.5.2 and common_msgs 1.6.0 is released
    if package in ['rosgraph_msgs', 'actionlib_msgs'] and not 'std_msgs' in depends:
        depends.append('std_msgs')
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

def run_ant(properties):
    # generate properties for ant
    properties_file = os.path.join(_properties_dir, 
        'build-%s.properties'%(properties['ros.package']))
    if not os.path.exists(os.path.dirname(properties_file)):
        os.makedirs(os.path.dirname(properties_file))

    with open(properties_file, 'w') as f:
        for property, value in properties.iteritems():
            f.write('%s=%s\n'%(property,value))

    command = ['ant', '-f', _build_file,
               '-Dproperties=%s'%(properties_file)]

    command.append('maven-install')

    subprocess.check_call(command)
    
def add_osgi_properties(properties, dependencies):
    """Add to the properties a set of properties for OSGi"""

    osgi_imports = ['org.ros.message']
    for dependency in dependencies:
        osgi_imports.append('org.ros.message.%s'%(dependency))

    imports = ','.join(osgi_imports)
    properties['ros.osgi.imports'] = imports 
    properties['ros.osgi.exports'] = 'org.ros.message.%s;uses:="%s"'%(
        properties['ros.package'], imports)
    
def prepare_maven(properties, dependencies):
    """Create an ant fragment to be included with maven info.
Modifies the properties list."""

    maven_ant_file = os.path.join(_maven_dir, 'maven-dependencies-%s.xml'%(properties['ros.package']))
    if not os.path.exists(os.path.dirname(maven_ant_file)):
        os.makedirs(os.path.dirname(maven_ant_file))

    properties['ros.maven.ant.include'] = maven_ant_file

    # String formating in python doesn't like . as a separator
    file_contents = ["""<?xml version="1.0"?>
<project name="maven" default="maven-install" basedir="."  xmlns:artifact="antlib:org.apache.maven.artifact.ant">
  <path id="maven-ant-tasks.classpath" path="%s/maven-ant-tasks-2.1.3.jar" />
  <typedef resource="org/apache/maven/artifact/ant/antlib.xml"
           uri="antlib:org.apache.maven.artifact.ant"
           classpathref="maven-ant-tasks.classpath" />

<artifact:pom id="mypom" groupId="org.ros" artifactId="org.ros.rosjava.%s" version="0.0.0" packaging="jar">
<dependency groupId="org.ros" artifactId="org.ros.rosjava.bootstrap" version="0.0.0" />
"""%(properties['ros.bootstrap.scripts.dir'], properties['ros.package'])]

    for dependency in dependencies:
        file_contents.append('<dependency groupId="org.ros" artifactId="org.ros.rosjava.%s" version="0.0.0" />'
                             %(dependency))

    file_contents.append("""</artifact:pom>
  <target name="maven-bug-workaround">
    <!-- For bug in plugin that ignores in-memory poms for install -->
    <artifact:writepom pomRefId="mypom" file="${base}/maven/${ros.package}-pom.xml" />
    <artifact:pom id="mypom.workaround" file="${base}/maven/${ros.package}-pom.xml" />
  </target>
  <target name="maven-install" depends="jar,maven-bug-workaround">
    <artifact:install file="${jar}" pomRefId="mypom.workaround" />
  </target>

  <target name="maven-deploy" depends="jar,maven-bug-workaround">
    <artifact:deploy file="${jar}" pomRefId="mypom.workaround">
      <remoteRepository url="file:///www/repository"/>
    </artifact:deploy>
  </target>
</project>
""")

    with open(maven_ant_file, 'w') as f:
        f.write('\n'.join(file_contents))
    
def _generate_msgs(rospack, package, up_to_date):
    depends = get_msg_packages(rospack, package)
    unbuilt = [d for d in depends if not d in up_to_date]
    for u in unbuilt:
        _generate_msgs(rospack, u, up_to_date)
        
    # generate classpath, safe-encode
    dependencies = get_all_msg_dependencies(rospack, package)
    classpath = get_msg_classpath(rospack, package)
    artifact_built = msg_jar_file_path(package)

    # Map for all properties
    properties = {'ros.package': package,
                  'ros.artifact.built': artifact_built,
                  'ros.home': _ros_home,
                  'ros.bootstrap.scripts.dir': _scripts_dir,
                  'ros.compile.classpath': classpath,
                  'ros.gen.msg.dir':
                      '%s/rosjava/gen/msg/%s'%(_ros_home,package)}

    # call ant to build everything
    # - add to up_to_date regardless to prevent infinite loop
    up_to_date.append(package)

    add_osgi_properties(properties, dependencies)
    prepare_maven(properties, dependencies)
    run_ant(properties)

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

def get_all_msg_dependencies(rospack, package):
    """gets all msg package dependencies"""
    depends = rospack.depends([package])[package]
    # have to include std_msgs because of Header
    if 'std_msgs' not in depends:
        depends.append('std_msgs')
    return [pkg for pkg in depends if is_msg_pkg(pkg) or is_srv_pkg(pkg)]
        
def get_msg_classpath(rospack, package):
    depends = get_all_msg_dependencies(rospack, package)
    pathelements = [_bootstrap_jar]
    for pkg in depends:
        pathelements.append(msg_jar_file_path(pkg))
    return os.pathsep.join([os.path.abspath(p) for p in pathelements])


def wipe_msg_depends(package):
    rospack = roslib.packages.ROSPackages()
    
    msg_packages = get_msg_packages(rospack, package)
    srv_packages = get_srv_packages(rospack, package)

    properties_file = os.path.join(_properties_dir, 'build-%s.properties'%(package))
    for p in set(msg_packages + srv_packages):
        # call ant to delete build artifacts
        command = ['ant', '-f', _build_file,
                   '-Dproperties=%s' % (properties_file),
                   'clean']
        subprocess.check_call(command)
    
    
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
