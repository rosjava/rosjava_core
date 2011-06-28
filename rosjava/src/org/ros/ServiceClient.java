package org.ros;


import java.net.URI;

public interface ServiceClient<RequestType, ResponseType> {

  void connect(URI uri);

  void shutdown();

  /**
   * @param request
   */
  void call(RequestType request, ServiceResponseListener<ResponseType> listener);

}