/*
 * Software License Agreement (BSD License)
 * 
 * Copyright (c) 2011, Willow Garage, Inc. All rights reserved. Redistribution
 * and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. * Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. * Neither the name of Willow Garage, Inc. nor the
 * names of its contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.ros.app_manager;

import org.ros.ServiceResponseListener;
import org.ros.message.Message;

/**
 * Message listener implementation that enables blocking-style execution of
 * service calls. This implementation is intended for services as it only
 * accepts a single message. It can be used with topics if the use case is
 * similar (wait until a single message received).
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 * 
 * @param <T>
 */
public class BasicAppManagerCallback<T extends Message> implements ServiceResponseListener<T> {

  private T response;
  private AppManagerException failure;

  public BasicAppManagerCallback() {
    response = null;
  }

  @Override
  public void onSuccess(T message) {
    response = message;
  }

  public T getResponse() {
    return response;
  }

  @Override
  public void onFailure(Exception e) {
    this.failure = new AppManagerException(e);
  }

  public T waitForResponse(long timeout) throws TimeoutException, AppManagerException {
    // TODO: need equivalent of node.ok()
    long timeoutT = System.currentTimeMillis() + timeout;
    while (response == null && failure == null && System.currentTimeMillis() < timeoutT) {
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
      }
    }
    if (failure != null) {
      // re-throw
      throw failure;
    }
    if (response == null) {
      throw new TimeoutException();
    }
    return response;
  }

  public static class TimeoutException extends Exception {

    private static final long serialVersionUID = 1L;

  }
}
