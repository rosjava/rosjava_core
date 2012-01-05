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

package org.ros.internal.message.new_style;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.ros.message.MessageDefinition;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageDefinitionTest {

  @Test
  public void testMd5Checksum() {
    org.ros.message.rosgraph_msgs.Log message = new org.ros.message.rosgraph_msgs.Log();
    MessageDefinition definition =
        MessageDefinition.newFromStrings(message.getDataType(), message.getMessageDefinition(),
            message.getMD5Sum());
    assertEquals(message.getMD5Sum(), definition.getMd5Checksum());
  }

}
