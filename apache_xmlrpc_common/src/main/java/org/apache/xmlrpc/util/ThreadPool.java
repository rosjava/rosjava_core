/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.xmlrpc.util;

import java.util.ArrayList;
import java.util.List;


/** Simple thread pool. A task is executed by obtaining a thread from
 * the pool
 */
public class ThreadPool {
	/** The thread pool contains instances of {@link ThreadPool.Task}.
	 */
	public interface Task {
		/** Performs the task.
		 * @throws Throwable The task failed, and the worker thread won't be used again.
		 */
		void run() throws Throwable;
	}

    /** A task, which may be interrupted, if the pool is shutting down. 
     */
    public interface InterruptableTask extends Task {
        /** Interrupts the task.
         * @throws Throwable Shutting down the task failed.
         */
        void shutdown() throws Throwable;
    }

    private class Poolable {
        private volatile boolean shuttingDown;
        private Task task;
        private Thread thread;
        Poolable(ThreadGroup pGroup, int pNum) {
            thread = new Thread(pGroup, pGroup.getName() + "-" + pNum){
                public void run() {
                    while (!shuttingDown) {
                        final Task t = getTask();
                        if (t == null) {
                            try {
                                synchronized (this) {
                                    if (!shuttingDown  &&  getTask() == null) {
                                        wait();
                                    }
                                }
                            } catch (InterruptedException e) {
                                // Do nothing
                            }
                        } else {
                            try {
                                t.run();
                                resetTask();
                                repool(Poolable.this);
                            } catch (Throwable e) {
                                remove(Poolable.this);
                                Poolable.this.shutdown();
                                resetTask();
                            }
                        }
                    }
                }
            };
            thread.start();
        }
        synchronized void shutdown() {
            shuttingDown = true;
            final Task t = getTask();
            if (t != null  &&  t instanceof InterruptableTask) {
                try {
                    ((InterruptableTask) t).shutdown();
                } catch (Throwable th) {
                    // Ignore me
                }
            }
            task = null;
            synchronized (thread) {
                thread.notify();
            }
        }
        private Task getTask() {
            return task;
        }
        private void resetTask() {
            task = null;
        }
        void start(Task pTask) {
            task = pTask;
            synchronized (thread) {
                thread.notify();
            }
        }
    }

	private final ThreadGroup threadGroup;
	private final int maxSize;
	private final List waitingThreads = new ArrayList();
	private final List runningThreads = new ArrayList();
	private final List waitingTasks = new ArrayList();
	private int num;


	/** Creates a new instance.
	 * @param pMaxSize Maximum number of concurrent threads.
	 * @param pName Thread group name.
	 */
	public ThreadPool(int pMaxSize, String pName) {
		maxSize = pMaxSize;
		threadGroup = new ThreadGroup(pName);
	}

	private synchronized void remove(Poolable pPoolable) {
        runningThreads.remove(pPoolable);
        waitingThreads.remove(pPoolable);
	}

	void repool(Poolable pPoolable) {
	    boolean discarding = false;
	    Task task = null;
	    Poolable poolable = null;
	    synchronized (this) {
	        if (runningThreads.remove(pPoolable)) {
	            if (maxSize != 0  &&  runningThreads.size() + waitingThreads.size() >= maxSize) {
	                discarding = true;
	            } else {
	                waitingThreads.add(pPoolable);
	                if (waitingTasks.size() > 0) {
	                    task = (Task) waitingTasks.remove(waitingTasks.size() - 1);
	                    poolable = getPoolable(task, false);
	                }
	            }
	        } else {
	            discarding = true;
	        }
	        if (discarding) {
	            remove(pPoolable);
	        }
	    }
	    if (poolable != null) {
	        poolable.start(task);
	    }
	    if (discarding) {
	        pPoolable.shutdown();
	    }
	}

	/** Starts a task immediately.
	 * @param pTask The task being started.
	 * @return True, if the task could be started immediately. False, if
	 * the maxmimum number of concurrent tasks was exceeded. If so, you
	 * might consider to use the {@link #addTask(ThreadPool.Task)} method instead.
	 */
	public boolean startTask(Task pTask) {
	    final Poolable poolable = getPoolable(pTask, false);
	    if (poolable == null) {
	        return false;
	    }
	    poolable.start(pTask);
		return true;
	}

	private synchronized Poolable getPoolable(Task pTask, boolean pQueue) {
        if (maxSize != 0  &&  runningThreads.size() >= maxSize) {
            if (pQueue) {
                waitingTasks.add(pTask);
            }
            return null;
        }
        Poolable poolable;
        if (waitingThreads.size() > 0) {
            poolable = (Poolable) waitingThreads.remove(waitingThreads.size()-1);
        } else {
            poolable = new Poolable(threadGroup, num++);
        }
        runningThreads.add(poolable);
        return poolable;
	}
	
	/** Adds a task for immediate or deferred execution.
	 * @param pTask The task being added.
	 * @return True, if the task was started immediately. False, if
	 * the task will be executed later.
	 * @deprecated No longer in use.
	 */
	public boolean addTask(Task pTask) {
	    final Poolable poolable = getPoolable(pTask, true);
	    if (poolable != null) {
	        poolable.start(pTask);
	        return true;
	    }
	    return false;
	}

	/** Closes the pool.
	 */
	public synchronized void shutdown() {
        while (!waitingThreads.isEmpty()) {
            Poolable poolable = (Poolable) waitingThreads.remove(waitingThreads.size()-1);
            poolable.shutdown();
        }
        while (!runningThreads.isEmpty()) {
            Poolable poolable = (Poolable) runningThreads.remove(runningThreads.size()-1);
            poolable.shutdown();
        }
	}

	/** Returns the maximum number of concurrent threads.
	 * @return Maximum number of threads.
	 */
	public int getMaxThreads() { return maxSize; }

	/** Returns the number of threads, which have actually been created,
     * as opposed to the number of currently running threads.
	 */
    public synchronized int getNumThreads() { return num; }
}
