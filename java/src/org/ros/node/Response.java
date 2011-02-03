package org.ros.node;

import java.util.List;


import com.google.common.collect.Lists;

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

}
