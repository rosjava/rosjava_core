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
import subprocess
import sys
import tempfile

import roslib

# See http://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#Dependency_Scope
SCOPE_MAP = {
    'compile': {'compile': 'compile', 'runtime': 'runtime'},
    'provided': {'compile': 'provided', 'runtime': 'provided'},
    'runtime': {'compile': 'runtime', 'runtime': 'runtime'},
    'test': {'compile': 'test', 'runtime': 'test'},
    }
DEFAULT_SCOPE = 'compile'

DEPENDENCY_FILE_PROPERTY = 'dependency.file'
DEPENDENCY_GENERATION_TARGET = 'get-dependencies'

# XML tag for the rosjava manifest tag for path elements.
TAG_ROSJAVA_PATHELEMENT = 'rosjava-pathelement'

BOOTSTRAP_PKG = 'rosjava_bootstrap'
BOOTSTRAP_PKG_DIR = roslib.packages.get_pkg_dir(BOOTSTRAP_PKG)
BOOTSTRAP_SCRIPTS_DIR = os.path.join(BOOTSTRAP_PKG_DIR, 'scripts')


def _identity_scope_transformation(dependency_scope, unused_current_scope):
    return dependency_scope
        
        
def _transtive_dependency_scope_transformation(dependency_scope, current_scope):
    scope_transformations = SCOPE_MAP[dependency_scope]
    return scope_transformations.get(current_scope)
 
 
def _map_exports(rospack, package, export_operator, scope_transformation, scope):
    rospack.load_manifests([package])
    m = rospack.manifests[package]    
    package_directory = roslib.packages.get_pkg_dir(package)
    for export in [x for x in m.exports if x.tag == TAG_ROSJAVA_PATHELEMENT]:
        if scope == scope_transformation(export.attrs.get('scope', DEFAULT_SCOPE), scope):
            export_operator(package, package_directory, export)


def map_package_exports(rospack, package, export_operator, scope=DEFAULT_SCOPE):
    _map_exports(rospack, package, export_operator, _identity_scope_transformation, scope)
    

def map_package_dependencies(rospack, package, export_operator, dependency_operator=None,
                             scope=DEFAULT_SCOPE):
    """
    Walk the entire set of dependencies for a package. Run the supplied
    lambda expressions on each export and each package.
    Export lambdas for a given package are all run before the package lambda.
    """
    depends = rospack.depends([package])[package]
    for dependency in depends:
        _map_exports(rospack, dependency, export_operator,
                     _transtive_dependency_scope_transformation, scope)
        if dependency_operator is not None:
            dependency_operator(dependency)


# TODO(damonkohler): Support multiple build artifacts?
def get_package_build_artifact(rospack, package):
    """
    Get what a given package builds.

    Returns None if didn't generate a Java artifact.
    """
    rospack.load_manifests([package])
    manifest = rospack.manifests[package]
    for e in [x for x in manifest.exports if x.tag == TAG_ROSJAVA_PATHELEMENT]:
        if e.attrs.get('built', False):
            if 'groupId' in e.attrs:
                full_filename = get_full_maven_name(e)
                # NOTE(damonkohler): If the rosjava-pathelement refers to
                # something that is built, then it must have a location
                # defined.
                location = e.attrs['location']
                # TODO(keith): Should always place in Maven repository.
                return os.path.join(location, full_filename)
            return e.attrs['location']


def get_full_maven_name(e):
    """
    Creates an entire Maven filename from an XML element containing
    groupId, artifactId, and version.
    """
    return '%s-%s.jar' % (e.attrs['artifactId'], e.attrs['version'])


def get_maven_dependencies(package, dependency_filename):
    """
    Run the dependency ant file and get all dependencies. Returns as a
    dictionary of lists keyed by scope.
    """
    full_dependency_filename = os.path.join(roslib.packages.get_pkg_dir(package),
                                            dependency_filename)

    # Get a temp file but doesn't need to stay open
    fd, name = tempfile.mkstemp()

    command = ['ant', '-f', full_dependency_filename,
               '-logger', 'org.apache.tools.ant.NoBannerLogger',
               '-D%s=%s'%(DEPENDENCY_FILE_PROPERTY, name),
               DEPENDENCY_GENERATION_TARGET]

    fnull = open(os.devnull)
    subprocess.check_call(command, stdout=fnull, stderr=fnull)
    fnull.close()

    f = os.fdopen(fd, "r")
    dependencies = f.read()
    f.close()

    os.remove(name)

    depmap = {}
    for line in dependencies.split():
        pair = line.split('::::')
        if pair[0] not in depmap:
            depmap[pair[0]] = []
        depmap[pair[0]].append(pair[1])

    for scope in ['compile', 'runtime', 'test']:
        depmap[scope] = depmap.get(scope, [])

    return depmap


def _write_maven_dependencies_group(rospack, package, scope, stream):
    """Write out a maven <dependencies> element in the file for the given scope"""
    print >>stream, '  <artifact:dependencies filesetId="dependency.fileset.%s">' % scope
    print >>stream, ('    <artifact:remoteRepository id="org.ros.release" '
                'url="http://robotbrains.hideho.org/nexus/content/groups/ros-public" />')

    def export_operator(pkg, pkg_dir, e):
        # TODO(khughes): Nuke location once in Maven repository
        if 'groupId' in e.attrs and not 'location' in e.attrs:
            print >>stream, ('    <artifact:dependency groupId="%(groupId)s" '
                             'artifactId="%(artifactId)s" version="%(version)s" />' % e.attrs)
            
    def wrapped_export_operator(p, d, export):
        if export.attrs.get('built', False):
            return
        export_operator(p, d, export)
        
    map_package_exports(rospack, package, wrapped_export_operator, scope)    
    map_package_dependencies(rospack, package, export_operator, scope=scope)
    print >>stream, '  </artifact:dependencies>'


def write_ant_maven_dependencies(rospack, package, stream=sys.stdout):
    """
    Generate an Ant file which will get all dependencies needed via a Maven
    repository and provide both a classpath for ant builds and a file of
    dependencies to be consumed by this script.

    ant -f file -Ddependency.file=fname get-dependencies

    will create a file fname with 1 line per dependency. This file is appended
    to so should be empty every time.

    The fileset id dependency.fileset is available for such things as classpaths
    for a build.
    """
    print >>stream, ("""<?xml version="1.0"?>
<project name="dependencies" basedir="."  xmlns:artifact="antlib:org.apache.maven.artifact.ant"
      xmlns:ac="antlib:net.sf.antcontrib"
>
  <taskdef resource="net/sf/antcontrib/antlib.xml"/>

  <path id="maven-ant-tasks.classpath" path="%s/maven-ant-tasks-2.1.3.jar" />
  <typedef resource="org/apache/maven/artifact/ant/antlib.xml"
           uri="antlib:org.apache.maven.artifact.ant"
           classpathref="maven-ant-tasks.classpath" />
  <typedef resource="net/sf/antcontrib/antlib.xml"
           uri="antlib:net.sf.antcontrib"
           classpath="%s/ant-contrib-1.0b3.jar"/>
""" % (BOOTSTRAP_SCRIPTS_DIR, BOOTSTRAP_SCRIPTS_DIR))

    print >>stream, """  <artifact:dependencies filesetId="dependency.osgi">
    <artifact:remoteRepository id="org.ros.release" url="http://robotbrains.hideho.org/nexus/content/groups/ros-public" />
    <artifact:dependency groupId="biz.aQute" artifactId="bnd" version="0.0.384" />
  </artifact:dependencies>

  <path id="classpath.osgi">
    <fileset refid="dependency.osgi" />
  </path>

  <taskdef resource="aQute/bnd/ant/taskdef.properties" classpathref="classpath.osgi" />"""

    _write_maven_dependencies_group(rospack, package, 'compile', stream)
    _write_maven_dependencies_group(rospack, package, 'test', stream)
    _write_maven_dependencies_group(rospack, package, 'runtime', stream)

    print >>stream, ("""

  <target name="%s">
    <ac:for param="file">
      <path>
        <fileset refid="dependency.fileset.compile"/>
      </path>
      <sequential>
	<echo file="${%s}" append="true">compile::::@{file}
</echo>
      </sequential>
    </ac:for>
    <ac:for param="file">
      <path>
        <fileset refid="dependency.fileset.runtime"/>
      </path>
      <sequential>
	<echo file="${%s}" append="true">runtime::::@{file}
</echo>
      </sequential>
    </ac:for>
    <ac:for param="file">
      <path>
        <fileset refid="dependency.fileset.test"/>
      </path>
      <sequential>
	<echo file="${%s}" append="true">test::::@{file}
</echo>
      </sequential>
    </ac:for>
  </target>
</project>""" % (DEPENDENCY_GENERATION_TARGET, DEPENDENCY_FILE_PROPERTY, DEPENDENCY_FILE_PROPERTY,
                 DEPENDENCY_FILE_PROPERTY))

