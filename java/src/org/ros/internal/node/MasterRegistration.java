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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.response.Response;
import org.ros.internal.node.server.MasterServer;
import org.ros.internal.node.server.SlaveIdentifier;
import org.ros.internal.node.server.SlaveServer;
import org.ros.internal.node.topic.Publisher;
import org.ros.internal.node.topic.PublisherIdentifier;
import org.ros.internal.node.topic.Subscriber;
import org.ros.internal.node.topic.TopicListener;
import org.ros.internal.node.xmlrpc.XmlRpcTimeoutException;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Manages topic and service registrations of a {@link SlaveServer} with the
 * {@link MasterServer}.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MasterRegistration implements TopicListener, UncaughtExceptionHandler {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(Node.class);

  class MasterRegistrationThread extends Thread {
    @Override
    public void run() {
      try {
        // It would be consolidate the number of threads we are using. The only
        // necessity here is the ability to keep retrying jobs from this queue.
        while (!Thread.currentThread().isInterrupted()) {
          if (registrationQueue.isEmpty()) {
            Thread.sleep(100);
          } else {
            RegistrationJob job = registrationQueue.peek();
            if (job.run()) {
              registrationQueue.remove();
              setMasterRegistrationOk(true);
            } else {
              setMasterRegistrationOk(false);
            }
          }
        }
      } catch (InterruptedException e) {
        // Cancel-able
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
  }

  abstract class RegistrationJob {

    public abstract void doJob() throws RemoteException, XmlRpcTimeoutException,
        MalformedURLException;

    public boolean run() throws MalformedURLException {
      try {
        doJob();
      } catch (XmlRpcTimeoutException e) {
        log.error("Timeout communication with master.", e);
        return false;
      } catch (RemoteException e) {
        log.error("Remote exception from master.", e);
        return false;
      } catch (UndeclaredThrowableException e) {
        // Artifact of Java reflection API and the Apache XML-RPC library.
        throw new RuntimeException(e);
      }
      return true;
    }
  }

  class PublisherRegistrationJob extends RegistrationJob {

    private Publisher<?> publisher;

    public PublisherRegistrationJob(Publisher<?> publisher) {
      this.publisher = publisher;
    }

    @Override
    public void doJob() throws RemoteException, XmlRpcTimeoutException {
      masterClient.registerPublisher(publisher.toPublisherIdentifier(slaveIdentifier));
      publisher.signalRegistrationDone();
    }
  }

  class SubcriberRegistrationJob extends RegistrationJob {

    private Subscriber<?> subscriber;

    public SubcriberRegistrationJob(Subscriber<?> subscriber) {
      this.subscriber = subscriber;
    }

    @Override
    public void doJob() throws RemoteException, XmlRpcTimeoutException {
      Response<List<URI>> response;
      response = masterClient.registerSubscriber(slaveIdentifier, subscriber);
      List<PublisherIdentifier> publishers =
          SlaveServer.buildPublisherIdentifierList(response.getResult(),
              subscriber.getTopicDefinition());
      subscriber.updatePublishers(publishers);
      subscriber.signalRegistrationDone();
    }

  }

  private final ConcurrentLinkedQueue<RegistrationJob> registrationQueue;
  private final MasterClient masterClient;
  private SlaveIdentifier slaveIdentifier;
  private boolean registrationOk;
  private final MasterRegistrationThread registrationThread;
  private Throwable masterRegistrationError;

  public MasterRegistration(MasterClient masterClient) {
    this.masterClient = masterClient;
    if (DEBUG) {
      log.info("Remote URI: " + masterClient.getRemoteUri());
    }
    registrationOk = false;
    registrationQueue = new ConcurrentLinkedQueue<RegistrationJob>();
    registrationThread = new MasterRegistrationThread();
    registrationThread.setUncaughtExceptionHandler(this);
  }

  public int getPendingSize() {
    return registrationQueue.size();
  }

  public boolean isMasterRegistrationOk() {
    return registrationOk;
  }

  public void setMasterRegistrationOk(boolean registrationOk) {
    this.registrationOk = registrationOk;
  }

  @Override
  public void publisherAdded(String topicName, Publisher<?> publisher) {
    registrationQueue.add(new PublisherRegistrationJob(publisher));
  }

  @Override
  public void subscriberAdded(String topicName, Subscriber<?> subscriber) {
    registrationQueue.add(new SubcriberRegistrationJob(subscriber));
  }

  public void shutdown() {
    registrationThread.interrupt();
  }

  public void start(SlaveIdentifier slaveIdentifier) {
    if (slaveIdentifier == null) {
      throw new NullPointerException();
    }
    if (this.slaveIdentifier != null) {
      throw new IllegalStateException("cannot call start() more than once");
    }
    this.slaveIdentifier = slaveIdentifier;
    registrationThread.start();
  }

  @Override
  public void uncaughtException(Thread thread, Throwable throwable) {
    setMasterRegistrationOk(false);
    setMasterRegistrationError(throwable);
  }

  public Throwable getMasterRegistrationError() {
    return this.masterRegistrationError;
  }

  private void setMasterRegistrationError(Throwable throwable) {
    this.masterRegistrationError = throwable;
  }
}
