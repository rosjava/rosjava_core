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

import roslib; roslib.load_manifest('rosjava_bootstrap')

import os
import shutil
import subprocess
import sys
import tempfile

from rosjava_bootstrap import java_msgs
from rosjava_bootstrap import java_srvs

_DEPENDENCY_TAG = """
      <dependency>
        <groupId>ros</groupId>
        <artifactId>%s.%s</artifactId>
        <version>0.0.0-SNAPSHOT</version>
      </dependency>"""
_POM = """<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>ros</groupId>
  <artifactId>%(artifact_id)s</artifactId>
  <packaging>jar</packaging>
  <version>0.0.0-SNAPSHOT</version>
  <name>%(artifact_id)s</name>
  <url>http://maven.apache.org</url>
  <dependencies>
    <dependency>
      <groupId>ros.rosjava_core</groupId>
      <artifactId>rosjava_bootstrap</artifactId>
      <version>0.0.0-SNAPSHOT</version>
    </dependency>%(dependency_tags)s
  </dependencies>
</project>"""
_MESSAGE = 'message'
_SERVICE = 'service'

_installed_packages = set()


def _is_msg_package(package):
  d = roslib.packages.get_pkg_subdir(package, roslib.packages.MSG_DIR, False)
  return bool(d and os.path.isdir(d))


def _is_srv_package(package):
  d = roslib.packages.get_pkg_subdir(package, roslib.packages.SRV_DIR, False)
  return bool(d and os.path.isdir(d))


def _build_dependency_tags(rospack, package, artifact_type):
  dependency_tags = set()
  # Everything implicitly depends on std_msgs.
  if package != 'std_msgs':
    dependency_tags.add(_DEPENDENCY_TAG % (_MESSAGE, 'std_msgs'))
  for dependency in rospack.depends([package])[package]:
    if _is_msg_package(dependency):
      dependency_tags.add(_DEPENDENCY_TAG % (_MESSAGE, dependency))
    if _is_srv_package(dependency):
      dependency_tags.add(_DEPENDENCY_TAG % (_SERVICE, dependency))
  # Services implicitly depend upon the package's messages if they exist.
  if artifact_type == _SERVICE and _is_srv_package(package) and _is_msg_package(package):
    dependency_tags.add(_DEPENDENCY_TAG % (_MESSAGE, package))
  return dependency_tags


def _write_pom(rospack, package, artifact_type, directory):
  dependency_tags = _build_dependency_tags(rospack, package, artifact_type)
  with open(os.path.join(directory, 'pom.xml'), 'w') as pom:
    pom.write(_POM % {
        'artifact_id': '%s.%s' % (artifact_type, package),
        'dependency_tags': ''.join(dependency_tags),
        })


def _install_package(rospack, package, artifact_type):
  if (package, artifact_type) in _installed_packages:
    return
  sys.stdout.write('\nInstalling: %s.%s\n' % (artifact_type, package))
  directory = tempfile.mkdtemp()
  _write_pom(rospack, package, artifact_type, directory)
  message_directory = os.path.join(directory, 'src/main/java')
  if artifact_type == _MESSAGE:
    java_msgs.generate(package, output_dir=message_directory)
  if artifact_type == _SERVICE:
    java_srvs.generate(package, output_dir=message_directory)
  failed = subprocess.Popen(['cd %s; mvn install' % directory], shell=True).wait()
  if failed:
    sys.stderr.write('Failed!\nModule directory not cleaned up: %s\n' % directory)
    sys.exit(failed)
  else:
    shutil.rmtree(directory)
    _installed_packages.add((package, artifact_type))


def _install_dependencies(rospack, package):
  depends = rospack.depends([package])[package]
  for dependency in depends:
    _install_dependencies(rospack, dependency)
    if _is_msg_package(dependency):
      _install_package(rospack, dependency, _MESSAGE)
    if _is_srv_package(dependency):
      _install_package(rospack, dependency, _SERVICE)


if __name__ == '__main__':
  root_package = sys.argv[1]
  rospack = roslib.packages.ROSPackages()
  _install_dependencies(rospack, root_package)
  if _is_msg_package(root_package):
    _install_package(rospack, root_package, _MESSAGE)
  if _is_srv_package(root_package):
    _install_package(rospack, root_package, _SERVICE)

