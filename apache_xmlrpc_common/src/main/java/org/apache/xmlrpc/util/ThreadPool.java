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


/**
 * Simple thread pool. A task is executed by obtaining a thread from
 * the pool
 */
public final class ThreadPool {
    private final ThreadGroup threadGroup;
    private final int maxSize;
    private final List<Poolable> waitingThreads = new ArrayList();
    private final List<Poolable> runningThreads = new ArrayList();
    private final List<Task> waitingTasks = new ArrayList();
    private int num;

    /**
     * The thread pool contains instances of {@link ThreadPool.Task}.
     */
    public interface Task {
        /**
         * Performs the task.
         *
         * @throws Throwable The task failed, and the worker thread won't be used again.
         */
        void run() throws Throwable;
    }

    /**
     * A task, which may be interrupted, if the pool is shutting down.
     */
    public interface InterruptableTask extends Task {
        /**
         * Interrupts the task.
         *
         * @throws Throwable Shutting down the task failed.
         */
        void shutdown() throws Throwable;
    }

    private final class Poolable {
        private volatile boolean shuttingDown;
        private Task task;
        private final Thread thread;

        Poolable(final ThreadGroup pGroup, int pNum) {
            this.thread = new Thread(pGroup, pGroup.getName() + "-" + pNum) {

                public final void run() {
                    while (!Poolable.this.shuttingDown) {
                        final Task task = getTask();
                        if (task == null) {
                            try {
                                synchronized (this) {
                                    if (!Poolable.this.shuttingDown && getTask() == null) {
                                        wait();
                                    }
                                }
                            } catch (InterruptedException e) {
                                // Do nothing
                            }
                        } else {
                            try {
                                task.run();
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
            this.shuttingDown = true;
            final Task t = getTask();
            if (t != null && t instanceof InterruptableTask) {
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

        private final Task getTask() {
            return this.task;
        }

        private final void resetTask() {
            this.task = null;
        }

        final void start(Task pTask) {
            this.task = pTask;
            synchronized (thread) {
                this.thread.notify();
            }
        }
    }


    /**
     * Creates a new instance.
     *
     * @param pMaxSize Maximum number of concurrent threads.
     * @param pName    Thread group name.
     */
    public ThreadPool(int pMaxSize, String pName) {
        this.maxSize = pMaxSize;
        this.threadGroup = new ThreadGroup(pName);
    }

    private final synchronized void remove(final Poolable pPoolable) {
        this.runningThreads.remove(pPoolable);
        this.waitingThreads.remove(pPoolable);
    }

    final void repool(Poolable pPoolable) {
        boolean discarding = false;
        Task task = null;
        Poolable poolable = null;
        synchronized (this) {
            if (runningThreads.remove(pPoolable)) {
                if (maxSize != 0 && this.runningThreads.size() + this.waitingThreads.size() >= maxSize) {
                    discarding = true;
                } else {
                    waitingThreads.add(pPoolable);
                    if (waitingTasks.size() > 0) {
                        task =  this.waitingTasks.remove(this.waitingTasks.size() - 1);
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

    /**
     * Starts a task immediately.
     *
     * @param pTask The task being started.
     *
     * @return True, if the task could be started immediately. False, if
     * the maximum number of concurrent tasks was exceeded.
     */
    public final boolean startTask(Task pTask) {
        final Poolable poolable = getPoolable(pTask, false);
        if (poolable == null) {
            return false;
        }
        poolable.start(pTask);
        return true;
    }

    private final synchronized Poolable getPoolable(final Task pTask, final boolean pQueue) {
        if (this.maxSize != 0 && this.runningThreads.size() >= this.maxSize) {
            if (pQueue) {
                this.waitingTasks.add(pTask);
            }
            return null;
        }
        final Poolable poolable;
        if (!this.waitingThreads.isEmpty()) {
            poolable = this.waitingThreads.remove(waitingThreads.size() - 1);
        } else {
            poolable = new Poolable(threadGroup, num++);
        }
        this.runningThreads.add(poolable);
        return poolable;
    }


    /**
     * Closes the pool.
     */
    public synchronized void shutdown() {
        for (final Poolable poolable : this.waitingThreads) {
            poolable.shutdown();
        }
        this.waitingThreads.clear();
        for (final Poolable poolable : this.runningThreads) {
            poolable.shutdown();
        }
        this.runningThreads.clear();
    }

    /**
     * Returns the maximum number of concurrent threads.
     *
     * @return Maximum number of threads.
     */
    public final int getMaxThreads() {
        return this.maxSize;
    }

    /**
     * Returns the number of threads, which have actually been created,
     * as opposed to the number of currently running threads.
     */
    public final synchronized int getNumThreads() {
        return this.num;
    }
}
