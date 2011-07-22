/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.internal.manifest;

import com.google.common.base.Preconditions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.net.MalformedURLException;
import java.net.URL;

public class VersionControl implements XmlSerializable {

  private static final String VERSION_CONTROL_TAG = "versioncontrol";
  private static final String URL_ATTRIBUTE = "url";
  private static final String TYPE_ATTRIBUTE = "type";
  
  private final String type;
  private final URL url;

  public VersionControl(String type, URL url) {
    Preconditions.checkNotNull(type);
    Preconditions.checkArgument(type.length() != 0);
    Preconditions.checkNotNull(url);
    this.type = type;
    this.url = url;
  }

  public VersionControl(Element element) throws MalformedURLException {
    Preconditions.checkArgument(element.getNodeName().equals(VERSION_CONTROL_TAG));
    this.type = element.getAttribute(TYPE_ATTRIBUTE);
    this.url = new URL(element.getAttribute(URL_ATTRIBUTE));
  }
  
  public static boolean checkNode(Node node) {
    return node.getNodeName().equals(VERSION_CONTROL_TAG);
  }

  @Override
  public Element toElement(Document doc) {
    Element element = doc.createElement(VERSION_CONTROL_TAG);
    element.setAttribute(TYPE_ATTRIBUTE, type);
    element.setAttribute(URL_ATTRIBUTE, url.toString());
    return element;
  }

}
