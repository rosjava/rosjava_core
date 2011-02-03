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

public class Description implements XmlSerializable {
  
  static final String DESCRIPTION_TAG = "description";
  static final String BRIEF_ATTRIBUTE = "brief";
  
  private String description;
  private String brief;
  
  public Description(String description) {
    Preconditions.checkNotNull(description);
    this.description = description;
    this.brief = "";
  }
  
  public Description(String description, String brief) {
    this(description);
    Preconditions.checkNotNull(brief);
    this.brief = brief;
  }
  
  public Description(Element element) {
    Preconditions.checkArgument(element.getNodeName().equals(DESCRIPTION_TAG));
    this.description = element.getFirstChild().getNodeValue();
    this.brief = element.getAttribute(BRIEF_ATTRIBUTE);
  }
  
  public static boolean checkNodeName(Node node) {
    return node.getNodeName().equals(DESCRIPTION_TAG);
  }
  
  @Override
  public Element toElement(Document doc) {
    Element element = doc.createElement(DESCRIPTION_TAG);
    element.appendChild(doc.createTextNode(description));
    if (brief.length() != 0) {
      element.setAttribute(BRIEF_ATTRIBUTE, brief);
    }
    return element;
  }
  
}
