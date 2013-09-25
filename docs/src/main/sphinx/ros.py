# Copyright (C) 2012 Google Inc.
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

from docutils import nodes


def make_roswiki_link(name, rawtext, text, lineno, inliner, options={}, content=[]):
  refuri = 'http://wiki.ros.org/' + text
  node = nodes.reference(rawtext, text, refuri=refuri, **options)
  return [node], []


def make_rosmsg_link(name, rawtext, text, lineno, inliner, options={}, content=[]):
  package, message = text.split('/', 1)
  refuri = 'http://ros.org/doc/api/%s/html/msg/%s.html' % (package, message)
  node = nodes.reference(rawtext, text, refuri=refuri, **options)
  return [node], []


def make_rossrv_link(name, rawtext, text, lineno, inliner, options={}, content=[]):
  package, message = text.split('/', 1)
  refuri = 'http://ros.org/doc/api/%s/html/srv/%s.html' % (package, message)
  node = nodes.reference(rawtext, text, refuri=refuri, **options)
  return [node], []


def setup(app):
  app.add_role('roswiki', make_roswiki_link)
  app.add_role('rosmsg', make_rosmsg_link)
  app.add_role('rossrv', make_rossrv_link)

