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

import generate_android_properties
import generate_ros_properties
import generate_msg_depends
import generate_eclipse_classpath
import generate_eclipse_project
import roslib
import maven


def _usage():
    print 'make.py <package-name> [clean]'
    sys.exit(os.EX_USAGE)


def write_sorted_properties(properties, stream=sys.stdout):
    for key in sorted(properties):
        print >>stream, '%s=%s' % (key, properties[key])
        

def build(rospack, package):
    if os.path.exists('dependencies.xml'):
        print 'Skipping dependencies.xml generation.'
    else:
        with open('dependencies.xml', 'w') as stream:
            maven.write_ant_maven_dependencies(rospack, package, stream)
        
    maven_depmap = maven.get_maven_dependencies(package, 'dependencies.xml')
    
    if os.path.exists('ros.properties'):
        print 'Skipping ros.properties generation.'
    else:
        properties = generate_ros_properties.generate_properties(rospack, package, maven_depmap)
        with open('ros.properties', 'w') as stream:
            write_sorted_properties(properties, stream)
            
    if os.path.exists('default.properties'):
        print 'Skipping default.properties generation.'
    else:
        properties = generate_android_properties.generate_properties(rospack, package)
        if properties is not None:
            with open('default.properties', 'w') as stream:
                write_sorted_properties(properties, stream)
                
    generate_msg_depends.generate_msg_depends(package)
    
    if os.path.exists('.classpath'):
        print 'Skipping .classpath generation.'
    else:
        with open('.classpath', 'w') as stream:
            generate_eclipse_classpath.generate_classpath_file(rospack, package, maven_depmap, stream)

    if os.path.exists('.project'):
        print 'Skipping .project generation.'
    else:
        with open('.project', 'w') as stream:
            generate_eclipse_project.generate_eclipse_project(package, stream)
            
    subprocess.check_call(['ant'])
            
            
def _remove(path):
    try:
        os.remove(path)
    except:
        print 'Failed to remove %r' % path
            
            
def clean():
    subprocess.call(['ant', 'clean'])
    _remove('dependencies.xml')
    _remove('ros.properties')
    _remove('default.properties')
    _remove('.classpath')
    _remove('.project')

        
def main(argv):
    if len(argv) < 2:
        _usage()
    package = argv[1]
    rospack = roslib.packages.ROSPackages()
    if len(argv) == 2:
        build(rospack, package)
    elif argv[2] == 'clean':
        clean()
    

if __name__ == '__main__':
    main(sys.argv)