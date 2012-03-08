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

import os

from docutils import nodes, utils


def make_javadoc_link(name, rawtext, text, lineno, inliner, options={}, content=[]):
  env = inliner.document.settings.env
  javadoc_root = env.config.javadoc_root
  class_part, method_part = (text.split('#', 1) + [''])[:2]
  refuri = os.path.join(javadoc_root, '%s.html#%s' % (class_part.replace('.', '/'), method_part))
  node = nodes.reference(rawtext, utils.unescape(text), refuri=refuri, **options)
  return [node], []


def setup(app):
  app.add_config_value('javadoc_root', None, 'env')
  app.add_role('javadoc', make_javadoc_link)

