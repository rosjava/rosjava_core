package org.ros;

import java.util.concurrent.TimeUnit;

public interface ServiceServer<RequestType, ResponseType> {

  boolean isRegistered();

  void awaitRegistration() throws InterruptedException;

  boolean awaitRegistration(long timeout, TimeUnit unit) throws InterruptedException;

}