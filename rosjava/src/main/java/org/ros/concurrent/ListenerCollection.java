package org.ros.concurrent;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ListenerCollection<T> {

  private final ExecutorService executorService;
  private final Collection<T> listeners;

  public interface SignalRunnable<U> {
    void run(U listener);
  }

  public ListenerCollection(ExecutorService executorService) {
    this.executorService = executorService;
    listeners = new CopyOnWriteArrayList<T>();
  }

  public ListenerCollection(Collection<T> listeners, ExecutorService executorService) {
    this(executorService);
    if (listeners != null) {
      addAll(listeners);
    }
  }

  public void add(T listener) {
    listeners.add(listener);
  }

  public void addAll(Collection<T> listeners) {
    this.listeners.addAll(listeners);
  }

  public void remove(T listener) {
    listeners.remove(listener);
  }

  public void clear() {
    listeners.clear();
  }

  /**
   * Signal all listeners.
   * 
   * <p>
   * Each listener is called in a separate thread.
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
   * Each listener is called in a separate thread.
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
