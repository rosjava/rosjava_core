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
import tempfile

import roslib.rosenv
import roslib.packages
import roslib.stacks

from generate_msg_depends import msg_jar_file_path, is_msg_pkg, is_srv_pkg

# XML tag for the rosjava manifest tag for path elements.
TAG_ROSJAVA_PATHELEMENT = 'rosjava-pathelement'

# XML tag for the rosjava manifest tag for source elements.
TAG_ROSJAVA_SRC = 'rosjava-src'

DEFAULT_SCOPE = 'compile'

BOOTSTRAP_PKG = 'rosjava_bootstrap'

DEPENDENCY_GENERATION_TARGET = 'get-dependencies'

DEPENDENCY_FILE_PROPERTY = 'dependency.file'

_bootstrap_pkg_dir = roslib.packages.get_pkg_dir(BOOTSTRAP_PKG)
_bootstrap_scripts_dir = os.path.join(_bootstrap_pkg_dir, 'scripts')

# See http://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#Dependency_Scope
SCOPE_MAP = {
    'compile': ['compile', 'runtime', 'test', 'all'],
    'runtime': ['runtime', 'test', 'all'],
    'test': ['test', 'all'],
    }

class UserError(Exception): pass

def usage():
    print "generate_properties.py <package-name>"
    sys.exit(os.EX_USAGE)

def get_full_maven_name(e):
    """
    Creates an entire Maven filename from an XML element containing
    groupId, artifactId, and version.
    """

    return "%s-%s.jar" % (e.attrs['artifactId'], e.attrs['version'])
    
def resolve_pathelements(pathelements):
    # TODO: potentially recognize keys like ROS_HOME
    return [os.path.abspath(p) for p in pathelements]

def walk_export_path(rospack, package, export_operator, package_operator, include_package=False, scope='all'):
    """
    Walk the entire set of dependencies for a package. Run the supplied
    lambda expressions on each export and each package.
    Export lambdas for a given package are all run before the package lambda.
    """
    depends = rospack.depends([package])[package]
    if include_package:
        depends.append(package)
    pathelements = []
    for pkg in depends:
        m = rospack.manifests[pkg]
        pkg_dir = roslib.packages.get_pkg_dir(pkg)
        for e in [x for x in m.exports if x.tag == TAG_ROSJAVA_PATHELEMENT]:
            try:
                # don't include this package's built resources
                if include_package and pkg == package and \
                       e.attrs.get('built', False):
                    continue
                else:
                    entry_scope = e.attrs.get('scope', DEFAULT_SCOPE)
                    if scope in SCOPE_MAP[entry_scope]:
                        export_operator(pkg, pkg_dir, e)
            except KeyError as ke:
                print >> sys.stderr, str(ke)
                print >> sys.stderr, "Invalid <%s> tag in package %s"%(tag, pkg)
        if package_operator:
            package_operator(pkg)

def get_specified_classpath(rospack, package, include_package, scope):
    """
    @param include_package: include library entries of self on path
    
    @param classpath_type: (optional, default 'all').  'compile',
    'runtime', 'test', or 'all'.  These classpath types are generated
    based on the scope of an export.  Exports have a default scope of
    'compile', which means they are part of all types of classpaths.
    For an exact mapping, see SCOPE_MAP.  The behavior of these
    scopes/classpath_types matches the Maven definition:
    
    http://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#Dependency_Scope

    Only gets the parts of the classpath which are not loaded by Maven.

    Returns list of dependencies.
    """
    pathelements = []

    def export_operator(pkg, pkg_dir, e):
        # If is a Maven artifact, create the entire name. Otherwise location
        # has all.
        if 'location' in e.attrs:
            location = e.attrs['location']
            if 'groupId' in e.attrs:
                fullname = get_full_maven_name(e)
                pathelements.append(os.path.join(pkg_dir, location, fullname))
            else:
                pathelements.append(os.path.join(pkg_dir, location))

    def package_operator(pkg): 
        if is_msg_pkg(pkg) or is_srv_pkg(pkg):
            pathelements.append(msg_jar_file_path(pkg))

    walk_export_path(rospack, package, export_operator, package_operator, include_package, scope)

    return resolve_pathelements(pathelements)

def get_classpath(rospack, package, maven_depmap, include_package=False, scope='all'):
    """
    @param include_package: include library entries of self on path
    
    @param maven_depmap: A map of lists for maven dependencies by scope.

    @param classpath_type: (optional, default 'all').  'compile',
    'runtime', 'test', or 'all'.  These classpath types are generated
    based on the scope of an export.  Exports have a default scope of
    'compile', which means they are part of all types of classpaths.
    For an exact mapping, see SCOPE_MAP.  The behavior of these
    scopes/classpath_types matches the Maven definition:
    
    http://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#Dependency_Scope

    Only gets the parts of the classpath which are not loaded by Maven.

    Returns list of dependencies.
    """
    paths = get_specified_classpath(rospack, package, include_package, scope)

    paths.extend(maven_depmap[scope])

    return os.pathsep.join(paths)

def get_eclipse_src_entries(package):
    """
    @return: list of source path locations.  Source paths will be
    returned in the relative specification used in the ros
    manifest.xml file.
    @rtype: [str]
    """
    rospack = roslib.packages.ROSPackages()
    depends = rospack.depends([package])[package]
    elements = []
        
    m = rospack.manifests[package]
    pkg_dir = roslib.packages.get_pkg_dir(package)
    for e in [x for x in m.exports if x.tag == TAG_ROSJAVA_SRC]:
        try:
            #elements.append(os.path.join(pkg_dir, e.attrs['location']))
            # use relative location as Eclipse does not seem to like fully resolved
            elements.append(e.attrs['location'])
        except KeyError:
            sys.stderr.write("Invalid <%s> tag in package %s"%(tag, pkg))
    return elements

_stack_version_cache = {}
def get_stack_version_cached(s):
    if s in _stack_version_cache:
        return _stack_version_cache[s]
    else:
        _stack_version_cache[s] = val = roslib.stacks.get_stack_version(s)
        return val

def is_android_library(package):
    m = roslib.manifest.load_manifest(package)
    return 'rosjava-android-lib' in [x.tag for x in m.exports]

def is_android_app(package):
    m = roslib.manifest.load_manifest(package)
    return 'rosjava-android-app' in [x.tag for x in m.exports]

def is_android_package(package):
    return is_android_app(package) or is_android_library(package)

def get_android_library_paths(package):
    m = roslib.manifest.load_manifest(package)
    return [x.attrs.get('path', '.') for x in m.exports if x.tag == 'rosjava-android-lib']

def get_package_version(package):
    # could optimize stack_of() calculation by maintaining a cache
    s = roslib.stacks.stack_of(package)
    return get_stack_version_cached(s)

def get_package_build_artifact(rospack, package):
    """
    Get what a given package builds.

    Returns None if didn't generate a Java artifact.
    """

    manifest = rospack.manifests[package]
    for e in [x for x in manifest.exports if x.tag == TAG_ROSJAVA_PATHELEMENT]:
        if e.attrs.get('built', False):
            if 'groupId' in e.attrs:
                full_filename = get_full_maven_name(e)
                location = e.attrs['location']
                # TODO(keith): Should always place in Maven repository.
                return os.path.join(location, full_filename)
            else:
                return e.attrs['location']

    return None

def generate_ros_properties(package):
    maven_depmap = get_maven_dependencies(package, 'dependencies.xml')

    rospack = roslib.packages.ROSPackages()
    depends = rospack.depends([package])[package]
    artifact = get_package_build_artifact(rospack, package)

    generate_version = hasattr(roslib.stacks, 'get_stack_version')
    props = {'ros.home': roslib.rosenv.get_ros_home()}

    # Used for setting Android libraries of the Android libraries.
    package_dir = roslib.packages.get_pkg_dir(package)
    android_lib_id = 1

    # Add directory properties and Android libraries for every package we depend on.
    for p in depends:
        p_dir = roslib.packages.get_pkg_dir(p)
        props['ros.pkg.%s.dir' % (p)] = p_dir

        # Note: Android libraries require relative paths inorder to work correctly.
        #       Using an absolute path will cause mysterious error messages about
        #       not being able to find default.properties.
        rel_path = os.path.relpath(p_dir, package_dir)
        for l in get_android_library_paths(p):
            lib = os.path.join(rel_path, l)
            props['android.library.reference.%d' % (android_lib_id)] = lib
            android_lib_id += 1
        
        if generate_version:
            props['ros.pkg.%s.version'%(p)] = get_package_version(p)
        
    built_artifact = get_package_build_artifact(rospack, package)
    if built_artifact:
        props['ros.artifact.built'] = built_artifact

    props['ros.classpath'] = get_classpath(rospack, package, maven_depmap, scope='all').replace(':', '\:')
    props['ros.compile.classpath'] = get_classpath(rospack, package, maven_depmap, scope='compile').replace(':', '\:')
    props['ros.runtime.classpath'] = get_classpath(rospack, package, maven_depmap, scope='runtime').replace(':', '\:')
    props['ros.test.classpath'] = get_classpath(rospack, package, maven_depmap, scope='test').replace(':', '\:')

    # Re-encode for ant <fileset includes="${ros.jarfileset}">.  uses comma separator instead.
    for prop in ['ros.classpath', 'ros.compile.classpath', 'ros.runtime.classpath', 'ros.test.classpath']:
        props[prop.replace('classpath', 'jarfileset')] = props[prop].replace(':', ',')
    
    props['ros.test_results'] = os.path.join(roslib.rosenv.get_test_results_dir(), package)

    if is_android_package(package):
        props['sdk.dir'] = get_android_sdk_dir()
        # TODO: Should be attribute of the Android export.
        props['target'] = 'android-9'

    if is_android_library(package):
        props['android.library'] = 'true'
    
    keys = props.keys()
    for k in sorted(keys):
        sys.stdout.write('%s=%s\n'%(k, props[k]))

def get_android_sdk_dir():
    """
    @return: location of Android SDK
    @raise UserError: if android is not installed
    """
    import which
    location = which.which('android')
    if not location:
        raise UserError("android tool is not in your command path.  Install the android sdk and add the tools directory to your path.")
    else:
        # SDK dir is two levels up in the path
        return os.path.dirname(os.path.dirname(location))

def is_android_package(package):
    m = roslib.manifest.load_manifest(package)
    return 'android' in [x.tag for x in m.exports]

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
        depmap[scope] = depmap.get(scope, list())

    depmap['all'] = depmap['test']

    return depmap
    
def generate_eclipse_classpath_file(package):
    maven_depmap = get_maven_dependencies(package, 'dependencies.xml')

    rospack = roslib.packages.ROSPackages()
    sys.stdout.write("""<?xml version="1.0" encoding="UTF-8"?>
<classpath>
""")
    # TODO(damonkohler): Move Eclipse .project file generation into this
    # script as well so that we can alter it for use with Android.
    if is_android_package(package):
        sys.stdout.write('\t<classpathentry kind="con" path="com.android.ide.eclipse.adt.ANDROID_FRAMEWORK"/>\n')
    for p in get_eclipse_src_entries(package):
        if p:
            sys.stdout.write('\t<classpathentry kind="src" path="%s"/>\n'%(p))
    sys.stdout.write("""\t<classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>
	<classpathentry kind="con" path="org.eclipse.jdt.junit.JUNIT_CONTAINER/4"/>
""")
    for p in get_classpath(rospack, package, maven_depmap, include_package=True, scope='all').split(':'):
        if p:
            sys.stdout.write('\t<classpathentry kind="lib" path="%s"/>\n'%(p))

    sys.stdout.write("""\t<classpath kind="output" path="build"/>
</classpath>
""")

def write_maven_dependencies_group(f, rospack, package, scope):
    """Write out a maven <dependencies> element in the file for the given scope"""
    f.write('  <artifact:dependencies filesetId="dependency.fileset.%s">' %
            scope)
    f.write('<artifact:remoteRepository id="org.ros.release" url="https://robotbrains.hideho.org/nexus/content/groups/ros-public" />\n')

    def export_operator(pkg, pkg_dir, e):
        # TODO(khughes): Nuke location once in Maven repository
        if 'groupId' in e.attrs and not 'location' in e.attrs:
            f.write("""
    <artifact:dependency groupId="%s" artifactId="%s" version="%s" />
"""%(
                e.attrs['groupId'], e.attrs['artifactId'],
                e.attrs['version']
                ))

    walk_export_path(rospack, package, 
                     export_operator, None,
                     True, scope)

    f.write("  </artifact:dependencies>\n")

def generate_ant_maven_dependencies(package):
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

    rospack = roslib.packages.ROSPackages()

    sys.stdout.write("""<?xml version="1.0"?>
<project name="dependencies" basedir="."  xmlns:artifact="antlib:org.apache.maven.artifact.ant">
  <taskdef resource="net/sf/antcontrib/antlib.xml"/>

  <path id="maven-ant-tasks.classpath" path="%s/maven-ant-tasks-2.1.3.jar" />
  <typedef resource="org/apache/maven/artifact/ant/antlib.xml"
           uri="antlib:org.apache.maven.artifact.ant"
           classpathref="maven-ant-tasks.classpath" />

"""% _bootstrap_scripts_dir)

    sys.stdout.write("""  <artifact:dependencies filesetId="dependency.osgi">
    <artifact:remoteRepository id="org.ros.release" url="https://robotbrains.hideho.org/nexus/content/groups/ros-public" />
    <artifact:dependency groupId="biz.aQute" artifactId="bnd" version="0.0.384" />
  </artifact:dependencies>

  <path id="classpath.osgi">
    <fileset refid="dependency.osgi" />
  </path>

  <taskdef resource="aQute/bnd/ant/taskdef.properties" classpathref="classpath.osgi" />
""")


    write_maven_dependencies_group(sys.stdout, rospack, package, 'compile')
    write_maven_dependencies_group(sys.stdout, rospack, package, 'test')
    write_maven_dependencies_group(sys.stdout, rospack, package, 'runtime')

    sys.stdout.write("""

  <target name="%s">
    <for param="file">
      <path>
        <fileset refid="dependency.fileset.compile"/>
      </path>
      <sequential>
	<echo file="${%s}" append="true">compile::::@{file}
</echo>
      </sequential>
    </for>
    <for param="file">
      <path>
        <fileset refid="dependency.fileset.runtime"/>
      </path>
      <sequential>
	<echo file="${%s}" append="true">runtime::::@{file}
</echo>
      </sequential>
    </for>
    <for param="file">
      <path>
        <fileset refid="dependency.fileset.test"/>
      </path>
      <sequential>
	<echo file="${%s}" append="true">test::::@{file}
</echo>
      </sequential>
    </for>
  </target>
</project>
"""%(DEPENDENCY_GENERATION_TARGET, 
     DEPENDENCY_FILE_PROPERTY, DEPENDENCY_FILE_PROPERTY, 
     DEPENDENCY_FILE_PROPERTY))
    
    
def generate_properties_main(argv=None):
    if argv is None:
        argv = sys.argv
    use_eclipse = False
    use_dependencies = False
    if '--eclipse' in argv:
        use_eclipse = True
        argv = [a for a in argv if a != '--eclipse']
    if '--dependencies' in argv:
        use_dependencies = True
        argv = [a for a in argv if a != '--dependencies']
    if len(argv) != 2:
        usage()
    package = argv[1]
    if use_eclipse:
        generate_eclipse_classpath_file(package)
    elif use_dependencies:
        generate_ant_maven_dependencies(package)
    else:
        generate_ros_properties(package)
    
if __name__ == '__main__':
    try:
        generate_properties_main()
    except roslib.packages.InvalidROSPkgException as e:
        sys.stderr.write('ERROR: '+str(e)+'\n')
        sys.exit(1)
    except UserError as ue:
        sys.stderr.write(str(ue)+'\n')
        sys.exit(1)
