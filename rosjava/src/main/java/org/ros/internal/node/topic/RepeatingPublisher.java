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

package org.ros.internal.node.topic;

import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.internal.util.InterruptableLoopableRunnable;
import org.ros.node.topic.Publisher;

import com.google.common.base.Preconditions;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class RepeatingPublisher<MessageType> {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(RepeatingPublisher.class);

  private final Publisher<MessageType> publisher;
  private final MessageType message;
  private final int frequency;
  private final RepeatingPublisherThread runnable;
  
  /**
   * Executor used to run the repeating publisher.
   */
  private final ExecutorService executorService;

  private final class RepeatingPublisherThread extends InterruptableLoopableRunnable {
    @Override
    public void doLoopBody() throws InterruptedException {
      publisher.publish(message);
      if (DEBUG) {
        log.info("Published message: " + message);
      }
      Thread.sleep((long) (1000f / frequency));
    }
  }

  /**
   * @param publisher
   * @param message
   * @param frequency
   *          the frequency of publication in Hz
   */
  public RepeatingPublisher(Publisher<MessageType> publisher, MessageType message, int frequency,
	  ExecutorService executorService) {
    this.publisher = publisher;
    this.message = message;
    this.frequency = frequency;
    this.executorService = executorService;
    runnable = new RepeatingPublisherThread();
  }

  public void start() {
    Preconditions.checkState(!runnable.isRunning());
    executorService.execute(runnable);
  }

  public void cancel() {
    Preconditions.checkState(runnable.isRunning());
    runnable.cancel();
  }

}
