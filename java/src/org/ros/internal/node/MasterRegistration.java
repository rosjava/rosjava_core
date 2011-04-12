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
import org.apache.xmlrpc.client.TimingOutCallback.TimeoutException;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.response.Response;
import org.ros.internal.node.server.SlaveIdentifier;
import org.ros.internal.node.server.SlaveServer;
import org.ros.internal.node.topic.Publisher;
import org.ros.internal.node.topic.PublisherIdentifier;
import org.ros.internal.node.topic.Subscriber;
import org.ros.internal.node.topic.TopicListener;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Manages topic and service registrations of a {@link SlaveServer} with the
 * {@link MasterServer}.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class MasterRegistration implements TopicListener {

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
      }
    }
  }

  abstract class RegistrationJob {

    public abstract void doJob() throws RemoteException, TimeoutException;

    public boolean run() {
      try {
        doJob();
      } catch (TimeoutException e) {
        log.error("timeout communication with master", e);
        return false;
      } catch (RemoteException e) {
        log.error("remote exception from master", e);
        return false;
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
    public void doJob() throws RemoteException, TimeoutException {
      try {
        masterClient.registerPublisher(publisher.toPublisherIdentifier(slaveIdentifier));
      } catch (URISyntaxException e) {
        // convert to RuntimeException as this generally can't happen.
        throw new RuntimeException(e);
      }
    }
  }

  class SubcriberRegistrationJob extends RegistrationJob {

    private Subscriber<?> subscriber;

    public SubcriberRegistrationJob(Subscriber<?> subscriber) {
      this.subscriber = subscriber;
    }

    @Override
    public void doJob() throws RemoteException, TimeoutException {
      Response<List<URI>> response;
      try {
        response = masterClient.registerSubscriber(slaveIdentifier, subscriber);
        List<PublisherIdentifier> publishers = SlaveServer.buildPublisherIdentifierList(
            response.getResult(), subscriber.getTopicDefinition());
        subscriber.updatePublishers(publishers);
      } catch (URISyntaxException e) {
        // convert to RuntimeException as this generally can't happen.
        throw new RuntimeException(e);
      }
    }

  }

  private final ConcurrentLinkedQueue<RegistrationJob> registrationQueue;
  private final MasterClient masterClient;
  private SlaveIdentifier slaveIdentifier;
  private boolean registrationOk;
  private final MasterRegistrationThread registrationThread;

  public MasterRegistration(MasterClient masterClient) {
    this.masterClient = masterClient;
    registrationOk = false;
    registrationQueue = new ConcurrentLinkedQueue<RegistrationJob>();
    registrationThread = new MasterRegistrationThread();
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
    slaveIdentifier = null;
  }

  public void start(SlaveIdentifier slaveIdentifier) {
    if (this.slaveIdentifier != null) {
       throw new IllegalStateException("cannot call start() more than once");
    }
    this.slaveIdentifier = slaveIdentifier;
    registrationThread.start();
  }
}
