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

import org.ros.internal.node.service.ServiceServer;

import org.ros.internal.node.service.ServiceListener;

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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages topic and service registrations of a {@link SlaveServer} with the
 * {@link MasterServer}.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MasterRegistration implements TopicListener, ServiceListener {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(Node.class);

  private static final long DEFAULT_RETRY_DELAY = 5;
  private static final TimeUnit DEFAULT_RETRY_TIME_UNIT = TimeUnit.SECONDS;

  private final MasterClient masterClient;
  private final MasterRegistrationThread registrationThread;
  private final CompletionService<Response<?>> completionService;
  private final Map<Future<Response<?>>, Callable<Response<?>>> futures;
  private final ScheduledExecutorService retryExecutor;

  private long retryDelay;
  private TimeUnit retryTimeUnit;
  private SlaveIdentifier slaveIdentifier;

  // TODO(damonkohler): This flag isn't useful if the connection to the master
  // is flaky. We need a better indicator.
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
            log.error("Master registration failed and will be retried.");
            // Retry the registration task.
            final Callable<Response<?>> task = futures.get(response);
            futures.remove(response);
            retryExecutor.schedule(new Runnable() {
              @Override
              public void run() {
                submitCallable(task);
              }
            }, retryDelay, retryTimeUnit);
          }
        }
      } catch (InterruptedException e) {
        // Cancelable
      }
    }
  }

  public MasterRegistration(MasterClient masterClient) {
    this.masterClient = masterClient;
    setRetryDelay(DEFAULT_RETRY_DELAY, DEFAULT_RETRY_TIME_UNIT);
    completionService = new ExecutorCompletionService<Response<?>>(Executors.newCachedThreadPool());
    futures = Maps.newConcurrentMap();
    retryExecutor = Executors.newSingleThreadScheduledExecutor();
    registrationOk = false;
    registrationThread = new MasterRegistrationThread();
    if (DEBUG) {
      log.info("Remote URI: " + masterClient.getRemoteUri());
    }
  }

  public void setRetryDelay(long delay, TimeUnit unit) {
    retryDelay = delay;
    retryTimeUnit = unit;
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
      public Response<List<URI>> call() throws Exception {
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
      public Response<List<URI>> call() throws Exception {
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

  @Override
  public void serviceServerAdded(final ServiceServer<?, ?> serviceServer) {
    submitCallable(new Callable<Response<?>>() {
      @Override
      public Response<Void> call() throws Exception {
        Response<Void> response = masterClient.registerService(slaveIdentifier, serviceServer);
        serviceServer.signalRegistrationDone();
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
