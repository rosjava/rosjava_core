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

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Thread executor for running queued connection jobs.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class ConnectionJobQueue {

  private LinkedBlockingDeque<Runnable> workQueue;
  private ThreadPoolExecutor jobExecutor;

  public ConnectionJobQueue() {
    // job queue currently for handling outbound XMLRPC connections.
    workQueue = new LinkedBlockingDeque<Runnable>();
    // tuning is currently arbitrary.
    jobExecutor = new ThreadPoolExecutor(1, 5, 2, TimeUnit.SECONDS, workQueue);
  }

  public void shutdown() {
    jobExecutor.shutdownNow();
  }

  public synchronized void addJob(Runnable job) {
    jobExecutor.execute(job);
  }
}
