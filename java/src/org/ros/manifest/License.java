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

package org.ros.manifest;

import com.google.common.base.Preconditions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.net.MalformedURLException;
import java.net.URL;

public class License implements XmlSerializable {
  
  private static final String URL_ATTRIBUTE = "url";
  private static final String LICENSE_TAG = "license";
  private final String license;
  private final URL url;
  
  public License(String license, URL url) {
    Preconditions.checkNotNull(license);
    Preconditions.checkNotNull(url);
    this.license = license;
    this.url = url;
  }
  
  public License(Element element) throws MalformedURLException {
    Preconditions.checkArgument(element.getNodeName().equals(LICENSE_TAG));
    this.license = element.getFirstChild().getNodeValue();
    this.url = new URL(element.getAttribute(URL_ATTRIBUTE));
  }
  
  public static boolean checkNodeName(Node node) {
    return node.getNodeName().equals(LICENSE_TAG);
  }

  @Override
  public Element toElement(Document doc) {
    Element element = doc.createElement(LICENSE_TAG);
    element.appendChild(doc.createTextNode(license));
    if (url != null) {
      element.setAttribute(URL_ATTRIBUTE, url.toString());
    }
    return element;
  }

}
