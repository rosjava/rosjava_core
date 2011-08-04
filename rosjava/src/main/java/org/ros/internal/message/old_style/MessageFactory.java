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

package org.ros.internal.message.old_style;

import org.ros.exception.RosRuntimeException;
import org.ros.message.Message;

import com.google.common.base.Preconditions;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MessageFactory implements org.ros.message.MessageFactory {

  /**
   * Java package prefix for root package for messages.
   */
  private static final String ROS_MESSAGE_CLASS_PACKAGE_PREFIX = "org.ros.message";

  /**
   * Java package prefix for root package for services.
   */
  private static final String ROS_SERVICE_CLASS_PACKAGE_PREFIX = "org.ros.service";

  @SuppressWarnings("unchecked")
  @Override
  public <T> T newMessage(String messageType) {
    try {
      return (T) loadMessageClass(messageType, ROS_MESSAGE_CLASS_PACKAGE_PREFIX).newInstance();
    } catch (Exception e) {
      throw new RosRuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T newServiceResponse(String serviceType) {
    try {
      return (T) loadMessageClass(serviceType + "$Response", ROS_SERVICE_CLASS_PACKAGE_PREFIX)
          .newInstance();
    } catch (Exception e) {
      throw new RosRuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T newServiceRequest(String serviceType) {
    try {
      return (T) loadMessageClass(serviceType + "$Request", ROS_SERVICE_CLASS_PACKAGE_PREFIX)
          .newInstance();
    } catch (Exception e) {
      throw new RosRuntimeException(e);
    }
  }

  /**
   * Get the class for a given message type.
   * 
   * @param messageType
   *          The string giving "ros package"/"message name", e.g. std_msgs/Time
   * @param packagePath
   *          Path to the package which contain messages.
   * 
   * @return The class.
   * 
   * @throws RosRuntimeException
   *           No class representing that name or the class is not accessible.
   */
  @SuppressWarnings("unchecked")
  private static Class<Message> loadMessageClass(String messageType, String packagePath) {
    Preconditions.checkArgument(messageType.split("/").length == 2);
    try {
      return (Class<Message>) MessageFactory.class.getClassLoader().loadClass(
          packagePath + "." + messageType.replace('/', '.'));
    } catch (Exception e) {
      throw new RosRuntimeException(e);
    }
  }
}
