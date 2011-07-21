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

DEFAULT_SCOPE = 'compile'

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
    
def resolve_pathelements(pathelements):
    # TODO: potentially recognize keys like ROS_HOME
    return [os.path.abspath(p) for p in pathelements]

def get_classpath(rospack, package, include_package=False, include_built =False, scope='all'):
    """
    @param include_package: include library entries of self on path
    
    @param classpath_type: (optional, default 'all').  'compile',
    'runtime', 'test', or 'all'.  These classpath types are generated
    based on the scope of an export.  Exports have a default scope of
    'compile', which means they are part of all types of classpaths.
    For an exact mapping, see SCOPE_MAP.  The behavior of these
    scopes/classpath_types matches the Maven definition:
    
    http://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#Dependency_Scope
    """
    depends = rospack.depends([package])[package]
    if include_package:
        depends.append(package)
    pathelements = []
    tag = 'rosjava-pathelement'
    for pkg in depends:
        m = rospack.manifests[pkg]
        pkg_dir = roslib.packages.get_pkg_dir(pkg)
        for e in [x for x in m.exports if x.tag == tag]:
            try:
                # don't include this package's built resources
                if not include_built and include_package and pkg == package and \
                       e.attrs.get('built', False):
                    continue
                
                location = os.path.join(pkg_dir, e.attrs['location'])
                entry_scope = e.attrs.get('scope', DEFAULT_SCOPE)
                if scope in SCOPE_MAP[entry_scope]:
                    pathelements.append(location)
            except KeyError as ke:
                print >> sys.stderr, str(ke)
                print >> sys.stderr, "Invalid <%s> tag in package %s"%(tag, pkg)
        if is_msg_pkg(pkg) or is_srv_pkg(pkg):
            pathelements.append(msg_jar_file_path(pkg))
    return os.pathsep.join(resolve_pathelements(pathelements))

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
    tag = 'rosjava-src'
        
    m = rospack.manifests[package]
    pkg_dir = roslib.packages.get_pkg_dir(package)
    for e in [x for x in m.exports if x.tag == tag]:
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

def get_package_version(package):
    # could optimize stack_of() calculation by maintaining a cache
    s = roslib.stacks.stack_of(package)
    return get_stack_version_cached(s)
    
def generate_ros_properties(package):
    rospack = roslib.packages.ROSPackages()
    depends = rospack.depends([package])[package]

    generate_version = hasattr(roslib.stacks, 'get_stack_version')
    
    props = {}
    props['ros.home'] = roslib.rosenv.get_ros_home()

    # add dir props for every package we depend on
    for p in depends:
        props['ros.pkg.%s.dir'%(p)] = roslib.packages.get_pkg_dir(p)
        if generate_version:
            props['ros.pkg.%s.version'%(p)] = get_package_version(p)
        
    props['ros.classpath'] = get_classpath(rospack, package, scope='all').replace(':', '\:')
    props['ros.compile.classpath'] = get_classpath(rospack, package, scope='compile').replace(':', '\:')
    props['ros.runtime.classpath'] = get_classpath(rospack, package, scope='runtime').replace(':', '\:')
    props['ros.test.classpath'] = get_classpath(rospack, package, scope='test').replace(':', '\:')

    # re-encode for ant <fileset includes="${ros.jarfileset}">.  uses comma separator instead.
    for prop in ['ros.classpath', 'ros.compile.classpath', 'ros.runtime.classpath', 'ros.test.classpath']:
        props[prop.replace('classpath', 'jarfileset')] = props[prop].replace(':', ',')
    
    props['ros.test_results'] = os.path.join(roslib.rosenv.get_test_results_dir(), package)

    if is_android_package(package):
        props['android.sdk.dir'] = get_android_sdk_dir()
        # TODO: should be attribute of android export
        props['target']='android-9'
        props['android.library']='true'
    
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
    
def generate_properties_main(argv=None):
    if argv is None:
        argv = sys.argv
    use_eclipse = False
    if '--eclipse' in argv:
        use_eclipse = True
        argv = [a for a in argv if a != '--eclipse']
    if len(argv) != 2:
        usage()
    package = argv[1]
    if use_eclipse:
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
        for p in get_classpath(rospack, package, include_package=True, scope='all').split(':'):
            if p:
                sys.stdout.write('\t<classpathentry kind="lib" path="%s"/>\n'%(p))
        sys.stdout.write("""\t<classpathentry kind="output" path="build"/>
</classpath>
""")
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
