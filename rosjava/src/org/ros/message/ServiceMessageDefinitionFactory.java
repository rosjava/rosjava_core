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

package org.ros.message;

import com.google.common.base.Preconditions;

import org.ros.internal.message.ServiceMessageDefinition;

public class ServiceMessageDefinitionFactory {

  @SuppressWarnings("unchecked")
  public static ServiceMessageDefinition createFromString(String serviceType) {
    Preconditions.checkArgument(serviceType.split("/").length == 2);
    Class<Service<?, ?>> serviceClass;
    Service<?, ?> service;
    try {
      serviceClass =
          (Class<Service<?, ?>>) Class.forName("org.ros.message.srv."
              + serviceType.replace('/', '.'));
      service = serviceClass.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return new ServiceMessageDefinition(service.getDataType(), service.getMD5Sum());
  }

}
