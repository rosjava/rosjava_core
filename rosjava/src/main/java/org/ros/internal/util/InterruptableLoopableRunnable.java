/**
 * 
 */
package org.ros.internal.util;

import com.google.common.base.Preconditions;


/**
 * A {@link Runnable} which can be used in a {@link java.util.concurrent.ExecutorService}.
 * It is interruptable.
 *
 * @author Keith M. Hughes
 * @since Nov 22, 2011
 */
public abstract class InterruptableLoopableRunnable implements Runnable {
	/**
	 * True if the code has been run once, false otherwise.
	 */
	private boolean ranOnce = false;
	
	/**
	 * The thread the code will be running in.
	 */
	private Thread thread;
	
	@Override
	public void run() {
		// Make sure we can only run this thing once.
		synchronized (this) {
			Preconditions.checkState(!ranOnce);
			ranOnce = true;
			thread = Thread.currentThread();
		}
		
		try {
			while (!thread.isInterrupted()) {
				doLoopBody();
			}
		} catch (InterruptedException e) {
			handleInterruptedException();
		} finally {
			thread = null;
		}
	}
	
	/**
	 * The actual method which should be run.
	 */
	public abstract void doLoopBody() throws InterruptedException;
	
	/**
	 * An interrupted exception was thrown.
	 */
	public void handleInterruptedException() {
		// Default is do nothing.
	}
	
	/**
	 * Cancel the code from running.
	 */
	public synchronized void cancel() {
	  if (thread != null) {
	    thread.interrupt();
	  }
	}
	
	/**
	 * Cancel the code from running.
	 */
	public synchronized boolean isRunning() {
		return thread != null && !thread.isInterrupted();
	}

}
