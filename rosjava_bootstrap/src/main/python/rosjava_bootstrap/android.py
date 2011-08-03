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

import roslib
import which


class AndroidError(Exception):
    pass


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


def get_android_target(package):
    m = roslib.manifest.load_manifest(package)
    for x in [x for x in m.exports if x.tag.startswith('rosjava-android')]:
      target = x.attrs.get('target')
      if target is not None:
        return target
    # TODO(damonkohler): Use a more sensible default.
    return 'android-9'


def get_android_sdk_dir():
    """
    @return: location of Android SDK
    @raise UserError: if android is not installed
    """
    location = which.which('android')
    if not location:
        raise AndroidError('The android tool is not in your command path. '
                           'Install the Android SDK and add the tools directory to your path.')
    # SDK dir is two levels up in the path.
    return os.path.dirname(os.path.dirname(location))


def generate_properties(rospack, package):
    if not is_android_package(package):
        return
    # Used for setting Android libraries of the Android libraries.
    package_dir = roslib.packages.get_pkg_dir(package)
    android_lib_id = 1
    props = {
            'sdk.dir': get_android_sdk_dir(),
            # TODO: Should be attribute of the Android export.
            'target': get_android_target(package),
            }
    # Add directory properties and Android libraries for every package we depend on.
    for p in rospack.depends([package])[package]:
        p_dir = roslib.packages.get_pkg_dir(p)
        # Note: Android libraries require relative paths inorder to work correctly.
        #       Using an absolute path will cause mysterious error messages about
        #       not being able to find default.properties.
        rel_path = os.path.relpath(p_dir, package_dir)
        for l in get_android_library_paths(p):
            lib = os.path.join(rel_path, l)
            props['android.library.reference.%d' % (android_lib_id)] = lib
            android_lib_id += 1
    if is_android_library(package):
        props['android.library'] = 'true'
    return props
