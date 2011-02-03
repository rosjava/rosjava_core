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

import com.google.common.collect.Maps;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.Map.Entry;

public class Export implements XmlSerializable {
  
  private final String name;
  private final String value;
  private final Map<String, String> attributes;
  
  public Export(String name, String value) {
    this.name = name;
    this.value = value;
    attributes = Maps.newHashMap();
  }
  
  public void addAttribute(String name, String value) {
    attributes.put(name, value);
  }

  @Override
  public Element toElement(Document doc) {
    Element element = doc.createElement(name);
    for (Entry<String, String> entry : attributes.entrySet()) {
      element.setAttribute(entry.getKey(), entry.getValue());
    }
    element.appendChild(doc.createTextNode(value));
    return element;
  }

}
