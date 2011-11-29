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

package org.ros.internal.node.client;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.internal.node.response.Response;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.server.SlaveIdentifier;
import org.ros.internal.node.server.SlaveServer;
import org.ros.internal.node.service.DefaultServiceServer;
import org.ros.internal.node.service.ServiceListener;
import org.ros.internal.node.topic.DefaultPublisher;
import org.ros.internal.node.topic.DefaultSubscriber;
import org.ros.internal.node.topic.PublisherIdentifier;
import org.ros.internal.node.topic.TopicListener;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * Manages topic, and service registrations of a {@link SlaveServer} with the
 * {@link MasterServer}.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class Registrar implements TopicListener, ServiceListener {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(Registrar.class);

  private static final long DEFAULT_RETRY_DELAY = 5;
  private static final TimeUnit DEFAULT_RETRY_TIME_UNIT = TimeUnit.SECONDS;

  private final MasterClient masterClient;
  private final MasterRegistrationThread registrationThread;
  private final CompletionService<Response<?>> completionService;
  private final Map<Future<Response<?>>, Callable<Response<?>>> futures;
  private final ScheduledExecutorService retryExecutor;
  private final ExecutorService executorService;
  
  private long retryDelay;
  private TimeUnit retryTimeUnit;
  private SlaveIdentifier slaveIdentifier;

  class MasterRegistrationThread extends Thread {
    @Override
    public void run() {
      try {
        while (!Thread.currentThread().isInterrupted()) {
          Future<Response<?>> response = completionService.take();
          try {
            if (response.get().isSuccess()) {
              futures.remove(response);
            }
          } catch (ExecutionException e) {
            log.warn("Master registration failed and will be retried.", e);
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

  /**
   * @param masterClient
   *          A client to the ROS master server.
   * @param executorService
   *          An {@link ExecutorService} to be used for all async operations.
   */
  public Registrar(MasterClient masterClient, ExecutorService executorService) {
    this.masterClient = masterClient;
    this.executorService = executorService;
    
    setRetryDelay(DEFAULT_RETRY_DELAY, DEFAULT_RETRY_TIME_UNIT);
    completionService = new ExecutorCompletionService<Response<?>>(executorService);
    futures = Maps.newConcurrentMap();
    retryExecutor = Executors.newSingleThreadScheduledExecutor();
    registrationThread = new MasterRegistrationThread();
    if (DEBUG) {
      log.info("Remote URI: " + masterClient.getRemoteUri());
    }
  }

  public void setRetryDelay(long delay, TimeUnit unit) {
    retryDelay = delay;
    retryTimeUnit = unit;
  }

  /**
   * Get the number of pending async registration requests to the master.
   * 
   * @return The number of pending async registration requests to the master.
   */
  public int getPendingSize() {
    return futures.size();
  }

  /**
   * Submit a task to the completion service.
   * 
   * @param task
   */
  private void submitCallable(Callable<Response<?>> task) {
    Future<Response<?>> future = completionService.submit(task);
    futures.put(future, task);
  }

  @Override
  public void publisherAdded(final DefaultPublisher<?> publisher) {
    submitCallable(new Callable<Response<?>>() {
      @Override
      public Response<List<URI>> call() throws Exception {
        Preconditions.checkNotNull(slaveIdentifier, "Registrar not started.");
        Response<List<URI>> response =
            masterClient.registerPublisher(publisher.toPublisherIdentifier(slaveIdentifier));
        publisher.signalRegistrationDone();
        return response;
      }
    });
  }

  @Override
  public void subscriberAdded(final DefaultSubscriber<?> subscriber) {
    submitCallable(new Callable<Response<?>>() {
      @Override
      public Response<List<URI>> call() throws Exception {
        Preconditions.checkNotNull(slaveIdentifier, "Registrar not started.");
        Response<List<URI>> response = masterClient.registerSubscriber(slaveIdentifier, subscriber);
        Collection<PublisherIdentifier> publishers =
            PublisherIdentifier.newCollectionFromUris(response.getResult(),
                subscriber.getTopicDefinition());
        subscriber.updatePublishers(publishers);
        subscriber.signalRegistrationDone();
        return response;
      }
    });
  }

  @Override
  public void serviceServerAdded(final DefaultServiceServer<?, ?> serviceServer) {
    submitCallable(new Callable<Response<?>>() {
      @Override
      public Response<Void> call() throws Exception {
        Preconditions.checkNotNull(slaveIdentifier, "Registrar not started.");
        Response<Void> response = masterClient.registerService(slaveIdentifier, serviceServer);
        serviceServer.signalRegistrationDone();
        return response;
      }
    });
  }

  public void start(SlaveIdentifier slaveIdentifier) {
    Preconditions.checkNotNull(slaveIdentifier);
    Preconditions.checkState(this.slaveIdentifier == null, "Registrar already started.");
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
