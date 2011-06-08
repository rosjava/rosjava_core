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

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.response.Response;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.server.SlaveIdentifier;
import org.ros.internal.node.server.SlaveServer;
import org.ros.internal.node.topic.Publisher;
import org.ros.internal.node.topic.PublisherDefinition;
import org.ros.internal.node.topic.Subscriber;
import org.ros.internal.node.topic.TopicListener;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Manages topic and service registrations of a {@link SlaveServer} with the
 * {@link MasterServer}.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MasterRegistration implements TopicListener {

  private static final boolean DEBUG = false;
  static final Log log = LogFactory.getLog(Node.class);

  private final MasterClient masterClient;
  private final MasterRegistrationThread registrationThread;

  private final CompletionService<Response<?>> completionService;
  private final Map<Future<Response<?>>, Callable<Response<?>>> futures;

  SlaveIdentifier slaveIdentifier;
  private boolean registrationOk;

  class MasterRegistrationThread extends Thread {
    @Override
    public void run() {
      try {
        while (!Thread.currentThread().isInterrupted()) {
          Future<Response<?>> response = completionService.take();
          try {
            if (response.get().isSuccess()) {
              registrationOk = true;
              futures.remove(response);
            }
          } catch (ExecutionException e) {
            registrationOk = false;
            // Retry the registration task.
            Callable<Response<?>> task = futures.get(response);
            futures.remove(response);
            completionService.submit(task);
          }
        }
      } catch (InterruptedException e) {
        // Cancel-able
      }
    }
  }

  public MasterRegistration(MasterClient masterClient) {
    this.masterClient = masterClient;
    if (DEBUG) {
      log.info("Remote URI: " + masterClient.getRemoteUri());
    }
    completionService = new ExecutorCompletionService<Response<?>>(Executors.newCachedThreadPool());
    futures = Maps.newConcurrentMap();
    registrationOk = false;
    registrationThread = new MasterRegistrationThread();
  }

  public int getPendingSize() {
    return futures.size();
  }

  public boolean isMasterRegistrationOk() {
    return registrationOk;
  }

  private void submitCallable(Callable<Response<?>> task) {
    Future<Response<?>> future = completionService.submit(task);
    futures.put(future, task);
  }

  @Override
  public void publisherAdded(final Publisher<?> publisher) {
    submitCallable(new Callable<Response<?>>() {
      @Override
      public Response<?> call() throws Exception {
        Response<List<URI>> response =
            masterClient.registerPublisher(publisher.toPublisherIdentifier(slaveIdentifier));
        publisher.signalRegistrationDone();
        return response;
      }
    });
  }

  @Override
  public void subscriberAdded(final Subscriber<?> subscriber) {
    submitCallable(new Callable<Response<?>>() {
      @Override
      public Response<?> call() throws Exception {
        Response<List<URI>> response = masterClient.registerSubscriber(slaveIdentifier, subscriber);
        List<PublisherDefinition> publishers =
            SlaveServer.buildPublisherIdentifierList(response.getResult(),
                subscriber.getTopicDefinition());
        subscriber.updatePublishers(publishers);
        subscriber.signalRegistrationDone();
        return response;
      }
    });
  }

  public void start(SlaveIdentifier slaveIdentifier) {
    Preconditions.checkNotNull(slaveIdentifier);
    Preconditions.checkState(this.slaveIdentifier == null, "Already started.");
    this.slaveIdentifier = slaveIdentifier;
    registrationThread.start();
  }

  public void shutdown() {
    registrationThread.interrupt();
    synchronized (futures) {
      for (Future<Response<?>> future : futures.keySet()) {
        future.cancel(true);
      }
    }
  }

}
