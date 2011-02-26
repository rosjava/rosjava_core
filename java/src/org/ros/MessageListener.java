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
package org.ros;

import org.ros.message.Message;



/** This simple callback interface is used by the
 * Subscriber whenever a new message is available.
 * 
 * @see Subscriber
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 *
 * @param <MessageType> The subscriber is specialized on a
 * particular message type, to alleviate ambiguity.
 */
public interface MessageListener<MessageType extends Message> {
  /**
   * @param m
   */
  void onNewMessage(MessageType m);
}