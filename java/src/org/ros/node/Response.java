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

package org.ros.node;

import com.google.common.collect.Lists;

import java.util.List;

public class Response<T> {
  
  private final StatusCode statusCode;
  private final String statusMessage;
  private final T value;
  
  public static <T> Response<T> CreateError(String message, T value) {
    return new Response<T>(StatusCode.ERROR, message, value);
  }
  
  public static <T> Response<T> CreateFailure(String message, T value) {
    return new Response<T>(StatusCode.FAILURE, message, value);
  }
  
  public static <T> Response<T> CreateSuccess(String message, T value) {
    return new Response<T>(StatusCode.SUCCESS, message, value);
  }
  
  public Response(int statusCode, String statusMessage, T value) {
    this(StatusCode.fromInt(statusCode), statusMessage, value);
  }

  public Response(StatusCode statusCode, String statusMessage, T value) {
    this.statusCode = statusCode;
    this.statusMessage = statusMessage;
    this.value = value;
  }
  
  public List<Object> toList() {
    return Lists.newArrayList(statusCode.toInt(), statusMessage, value == null ? "null" : value);
  }

  public StatusCode getStatusCode() {
    return statusCode;
  }

  public String getStatusMessage() {
    return statusMessage;
  }

  public T getValue() {
    return value;
  }
  
  @Override
  public String toString() {
    return "Response<" + statusCode + ", " + statusMessage + ", " + value.toString() + ">";
  }

  public static <T> Response<T> checkOk(Response<T> response) throws RemoteException {
    if (response.getStatusCode() != StatusCode.SUCCESS) {
      throw new RemoteException(response.toString());
    }
    return response;
  }

}
