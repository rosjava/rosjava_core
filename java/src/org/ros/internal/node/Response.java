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

package org.ros.internal.node;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author damonkohler@google.com (Damon Kohler)
 * 
 * @param <ResultType>
 */
public class Response<ResultType> {

  private final StatusCode statusCode;
  private final String statusMessage;
  private final ResultType result;

  public static <ResultType> Response<ResultType> createError(String message, ResultType value) {
    return new Response<ResultType>(StatusCode.ERROR, message, value);
  }

  public static <ResultType> Response<ResultType> createFailure(String message, ResultType value) {
    return new Response<ResultType>(StatusCode.FAILURE, message, value);
  }

  public static <ResultType> Response<ResultType> createSuccess(String message, ResultType value) {
    return new Response<ResultType>(StatusCode.SUCCESS, message, value);
  }

  public static <ResultType> Response<ResultType> fromList(List<Object> response,
      ResultFactory<ResultType> resultFactory) throws RemoteException {
    StatusCode statusCode = StatusCode.fromInt((Integer) response.get(0));
    String message = (String) response.get(1);
    if (statusCode != StatusCode.SUCCESS) {
      throw new RemoteException(statusCode, message);
    }
    return new Response<ResultType>(statusCode, message, resultFactory.create(response.get(2)));
  }

  public Response(int statusCode, String statusMessage, ResultType value) {
    this(StatusCode.fromInt(statusCode), statusMessage, value);
  }

  public Response(StatusCode statusCode, String statusMessage, ResultType value) {
    this.statusCode = statusCode;
    this.statusMessage = statusMessage;
    this.result = value;
  }

  public List<Object> toList() {
    return Lists.newArrayList(statusCode.toInt(), statusMessage, result == null ? "null" : result);
  }

  public StatusCode getStatusCode() {
    return statusCode;
  }

  public String getStatusMessage() {
    return statusMessage;
  }

  public ResultType getResult() {
    return result;
  }

  @Override
  public String toString() {
    return "Response<" + statusCode + ", " + statusMessage + ", " + result.toString() + ">";
  }

}
