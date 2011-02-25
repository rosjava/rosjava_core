package org.ros;

import org.ros.message.Message;

/**
 * @author "Ethan Rublee ethan.rublee@gmail.com"
 *
 * @param <MessageT>
 */
public interface Callback<MessageT extends Message> {
  /**
   * @param m
   */
  void onRecieve(MessageT m);
}