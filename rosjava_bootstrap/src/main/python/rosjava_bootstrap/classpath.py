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

from generate_msg_depends import msg_jar_file_path, is_msg_pkg, is_srv_pkg
import maven


def _get_specified_classpath(rospack, package, include_package, scope):
    """
    @param include_package: include library entries of self on path

    @param scope: 'compile', 'runtime', 'test', or 'all'.  These classpath
    types are generated based on the scope of an export.  Exports have a
    default scope of 'compile', which means they are part of all types of
    classpaths.  For an exact mapping, see SCOPE_MAP.  The behavior of these
    scopes/classpath_types matches the Maven definition:

    http://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#Dependency_Scope

    Only gets the parts of the classpath which are not loaded by Maven.

    Returns list of dependencies.
    """
    path_elements = []

    def export_operator(pkg, pkg_dir, e):
        # If is a Maven artifact, create the entire name. Otherwise location has all.
        if 'location' in e.attrs:
            location = e.attrs['location']
            if 'groupId' in e.attrs:
                fullname = maven.get_full_maven_name(e)
                path_elements.append(os.path.join(pkg_dir, location, fullname))
            else:
                path_elements.append(os.path.join(pkg_dir, location))

    def package_operator(pkg):
        if is_msg_pkg(pkg) or is_srv_pkg(pkg):
            path_elements.append(msg_jar_file_path(pkg))

    maven.walk_export_path(rospack, package, export_operator, package_operator, include_package,
                           scope)
    return [os.path.abspath(path) for path in path_elements]


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
    paths = _get_specified_classpath(rospack, package, include_package, scope)
    paths.extend(maven_depmap[scope])
    return os.pathsep.join(paths)
