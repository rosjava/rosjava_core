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

import android
import roslib


def generate_properties(rospack, package):
    if not android.is_android_package(package):
        return
    
    # Used for setting Android libraries of the Android libraries.
    package_dir = roslib.packages.get_pkg_dir(package)
    android_lib_id = 1

    props = {
            'sdk.dir': android.get_android_sdk_dir(),
            # TODO: Should be attribute of the Android export.
            'target': 'android-9'
            }
    
    # Add directory properties and Android libraries for every package we depend on.
    for p in rospack.depends([package])[package]:
        p_dir = roslib.packages.get_pkg_dir(p)
        # Note: Android libraries require relative paths inorder to work correctly.
        #       Using an absolute path will cause mysterious error messages about
        #       not being able to find default.properties.
        rel_path = os.path.relpath(p_dir, package_dir)
        for l in android.get_android_library_paths(p):
            lib = os.path.join(rel_path, l)
            props['android.library.reference.%d' % (android_lib_id)] = lib
            android_lib_id += 1
            
    if android.is_android_library(package):
        props['android.library'] = 'true'
        
    return props