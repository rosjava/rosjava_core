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

public class Review implements XmlSerializable {

  private static final String REVIEW_TAG = "review";
  private static final String NOTES_ATTRIBUTE = "notes";
  private static final String STATUS_ATTRIBUTE = "status";
  
  private final String status;
  private final String notes;

  public Review(String status, String notes) {
    Preconditions.checkNotNull(status);
    Preconditions.checkNotNull(notes);
    this.status = status;
    this.notes = notes;
  }
  
  public Review(Element element) {  
    Preconditions.checkArgument(element.getNodeName().equals(REVIEW_TAG));
    this.status = element.getAttribute(STATUS_ATTRIBUTE);
    this.notes = element.getAttribute(NOTES_ATTRIBUTE);
  }
  
  public static boolean checkNode(Node node) {
    return node.getNodeName().equals(REVIEW_TAG);
  }

  @Override
  public Element toElement(Document doc) {
    Element review = doc.createElement(REVIEW_TAG);
    review.setAttribute(STATUS_ATTRIBUTE, status);
    review.setAttribute(NOTES_ATTRIBUTE, notes);
    return review;
  }

}
