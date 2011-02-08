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

package org.ros.topic.server;

import java.net.URL;

import org.ros.node.server.SlaveDescription;
import org.ros.topic.TopicDescription;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class PublisherDescription {

  private final SlaveDescription slaveDescription;
  private final TopicDescription topicDescription;

  public PublisherDescription(SlaveDescription slaveDescription, TopicDescription topicDescription) {
    this.slaveDescription = slaveDescription;
    this.topicDescription = topicDescription;
  }

  public SlaveDescription getSlaveDescription() {
    return slaveDescription;
  }

  public String getNodeName() {
    return slaveDescription.getName();
  }

  public URL getSlaveUrl() {
    return slaveDescription.getUrl();
  }

  public String getTopicName() {
    return topicDescription.getName();
  }

  @Override
  public String toString() {
    return "PublisherDescription<" + slaveDescription.toString() + ", "
        + topicDescription.toString() + ">";
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((slaveDescription == null) ? 0 : slaveDescription.hashCode());
    result = prime * result + ((topicDescription == null) ? 0 : topicDescription.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    PublisherDescription other = (PublisherDescription) obj;
    if (slaveDescription == null) {
      if (other.slaveDescription != null) return false;
    } else if (!slaveDescription.equals(other.slaveDescription)) return false;
    if (topicDescription == null) {
      if (other.topicDescription != null) return false;
    } else if (!topicDescription.equals(other.topicDescription)) return false;
    return true;
  }

}
