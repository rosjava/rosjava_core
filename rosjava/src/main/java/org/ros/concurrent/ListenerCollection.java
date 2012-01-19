package org.ros.concurrent;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages a collection of listeners and makes it easy to execute a listener
 * callback on the entire collection.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ListenerCollection<T> {

  private final ScheduledExecutorService executorService;
  private final Collection<T> listeners;

  public interface SignalRunnable<U> {
    void run(U listener);
  }

  /**
   * @param executorService
   *          the {@link ScheduledExecutorService} to use when executing
   *          listener callbacks
   */
  public ListenerCollection(ScheduledExecutorService executorService) {
    this.executorService = executorService;
    listeners = new CopyOnWriteArrayList<T>();
  }

  /**
   * @param listeners
   *          an initial {@link Collection} of listeners to add
   * @param executorService
   *          the {@link ScheduledExecutorService} to use when executing
   *          listener callbacks
   */
  public ListenerCollection(Collection<T> listeners, ScheduledExecutorService executorService) {
    this(executorService);
    if (listeners != null) {
      addAll(listeners);
    }
  }

  /**
   * @param listener
   *          the listener to add
   */
  public void add(T listener) {
    listeners.add(listener);
  }

  /**
   * @param listeners
   *          a {@link Collection} of listeners to add
   */
  public void addAll(Collection<T> listeners) {
    this.listeners.addAll(listeners);
  }

  /**
   * @param listener
   *          the listener to remove
   */
  public void remove(T listener) {
    listeners.remove(listener);
  }

  /**
   * Removes all listeners.
   */
  public void clear() {
    listeners.clear();
  }

  /**
   * Signal all listeners.
   * 
   * <p>
   * Each {@link SignalRunnable} is executed in a separate thread.
   */
  public void signal(SignalRunnable<T> signalRunnable) {
    try {
      signal(signalRunnable, 0, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      // We don't wait for completion so we can ignore the unlikely
      // InterruptedException thrown by CountDownLatch.await().
    }
  }

  /**
   * Signal all listeners and wait for the all {@link SignalRunnable}s to
   * return.
   * 
   * <p>
   * Each {@link SignalRunnable} is executed in a separate thread.
   */
  public void signal(final SignalRunnable<T> signalRunnable, long timeout, TimeUnit unit)
      throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(listeners.size());
    for (final T listener : listeners) {
      executorService.execute(new Runnable() {
        @Override
        public void run() {
          signalRunnable.run(listener);
          latch.countDown();
        }
      });
    }
    latch.await(timeout, unit);
  }
}
