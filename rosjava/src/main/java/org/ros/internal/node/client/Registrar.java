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

import com.google.common.base.Preconditions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.concurrent.RetryingExecutorService;
import org.ros.exception.RosRuntimeException;
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

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

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

  private final RetryingExecutorService retryingExecutorService;
  private final MasterClient masterClient;

  private SlaveIdentifier slaveIdentifier;

  /**
   * @param masterClient
   *          a {@link MasterClient} for communicating with the ROS master
   * @param executorService
   *          an {@link ExecutorService} to be used for all asynchronous
   *          operations
   */
  public Registrar(MasterClient masterClient, ExecutorService executorService) {
    this.masterClient = masterClient;
    retryingExecutorService = new RetryingExecutorService(executorService);
    if (DEBUG) {
      log.info("Master URI: " + masterClient.getRemoteUri());
    }
  }

  public void setRetryDelay(long delay, TimeUnit unit) {
    retryingExecutorService.setRetryDelay(delay, unit);
  }

  @Override
  public void publisherAdded(final DefaultPublisher<?> publisher) {
    if (DEBUG) {
      log.info("Registering publisher: " + publisher);
    }
    retryingExecutorService.submit(new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        Preconditions.checkNotNull(slaveIdentifier, "Registrar not started.");
        Response<List<URI>> response =
            masterClient.registerPublisher(publisher.toDefinition(slaveIdentifier));
        if (response.isSuccess()) {
          log.info("Publisher registered: " + publisher);
          publisher.signalOnMasterRegistrationSuccess();
          return false;
        } else {
          log.info(String.format("Publisher registration failed: %s: %s",
              response.getStatusMessage(), publisher));
          publisher.signalOnMasterRegistrationFailure();
          return true;
        }
      }
    });
  }

  @Override
  public void publisherRemoved(final DefaultPublisher<?> publisher) {
    if (DEBUG) {
      log.info("Unregistering publisher: " + publisher);
    }
    retryingExecutorService.submit(new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        Preconditions.checkNotNull(slaveIdentifier, "Registrar not started.");
        Response<Integer> response =
            masterClient.unregisterPublisher(publisher.toIdentifier(slaveIdentifier));
        if (response.isSuccess()) {
          log.info("Publisher unregistered: " + publisher);
          publisher.signalOnMasterUnregistrationSuccess();
          return false;
        } else {
          log.info(String.format("Publisher unregistration failed: %s: %s",
              response.getStatusMessage(), publisher));
          publisher.signalOnMasterUnregistrationFailure();
          return true;
        }
      }
    });
  }

  @Override
  public void subscriberAdded(final DefaultSubscriber<?> subscriber) {
    if (DEBUG) {
      log.info("Registering subscriber: " + subscriber);
    }
    retryingExecutorService.submit(new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        Preconditions.checkNotNull(slaveIdentifier, "Registrar not started.");
        Response<List<URI>> response = masterClient.registerSubscriber(slaveIdentifier, subscriber);
        if (response.isSuccess()) {
          log.info("Subscriber registered: " + subscriber);
          Collection<PublisherIdentifier> publishers =
              PublisherIdentifier.newCollectionFromUris(response.getResult(),
                  subscriber.getTopicDefinition());
          subscriber.updatePublishers(publishers);
          subscriber.signalOnMasterRegistrationSuccess();
          return false;
        } else {
          subscriber.signalOnMasterUnregistrationFailure();
          return true;
        }
      }
    });
  }

  @Override
  public void subscriberRemoved(final DefaultSubscriber<?> subscriber) {
    if (DEBUG) {
      log.info("Unregistering subscriber: " + subscriber);
    }
    retryingExecutorService.submit(new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        Preconditions.checkNotNull(slaveIdentifier, "Registrar not started.");
        Response<Integer> response = masterClient.unregisterSubscriber(slaveIdentifier, subscriber);
        if (response.isSuccess()) {
          log.info("Subscriber unregistered: " + subscriber);
          subscriber.signalOnMasterUnregistrationSuccess();
          return false;
        } else {
          subscriber.signalOnMasterUnregistrationFailure();
          return true;
        }
      }
    });
  }

  @Override
  public void serviceServerAdded(final DefaultServiceServer<?, ?> serviceServer) {
    if (DEBUG) {
      log.info("ServiceServer added: " + serviceServer);
    }
    retryingExecutorService.submit(new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        Preconditions.checkNotNull(slaveIdentifier, "Registrar not started.");
        Response<Void> response = masterClient.registerService(slaveIdentifier, serviceServer);
        if (response.isSuccess()) {
          serviceServer.signalRegistrationDone();
          return false;
        }
        return true;
      }
    });
  }

  public void start(SlaveIdentifier slaveIdentifier) {
    Preconditions.checkNotNull(slaveIdentifier);
    Preconditions.checkState(this.slaveIdentifier == null, "Registrar already started.");
    this.slaveIdentifier = slaveIdentifier;
  }

  public void shutdown() {
    try {
      retryingExecutorService.shutdown(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new RosRuntimeException(e);
    }
  }
}
