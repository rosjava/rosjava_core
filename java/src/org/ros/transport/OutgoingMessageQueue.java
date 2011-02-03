package org.ros.transport;

import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ros.communication.Message;

import com.google.common.collect.Lists;

public class OutgoingMessageQueue {

  private static final boolean DEBUG = false;
  private static final Log log = LogFactory.getLog(OutgoingMessageQueue.class);
  
  private final Collection<LittleEndianDataOutputStream> streams;
  private final BlockingQueue<Message> messages;
  private final MessageSendingThread thread;

  private final class MessageSendingThread extends Thread {
    @Override
    public void run() {
      try {
        while (!Thread.currentThread().isInterrupted()) {
          sendMessage(messages.take());
        }
      } catch (InterruptedException e) {
        // Cancelable
      }
    }

    public void cancel() {
      interrupt();
    }
  }

  public OutgoingMessageQueue() {
    streams = Lists.newArrayList();
    messages = new LinkedBlockingQueue<Message>();
    thread = new MessageSendingThread();
  }
  
  public void add(Message message) {
    messages.add(message);
  }

  public void shutdown() {
    thread.cancel();
  }

  public void start() {
    thread.start();
  }

  public void addSocket(Socket socket) throws IOException {
    LittleEndianDataOutputStream out = new LittleEndianDataOutputStream(socket.getOutputStream());
    streams.add(out);
  }

  private void sendMessage(Message message) {
    byte[] data = message.serialize(0 /* unused seq */);
    int size = message.serializationLength();
    Iterator<LittleEndianDataOutputStream> iterator = streams.iterator();
    while (iterator.hasNext()) {
      LittleEndianDataOutputStream out = iterator.next();
      try {
        out.writeInt(size);
        out.write(data);
        out.flush();
      } catch (IOException e) {
        if (DEBUG) {
          log.info("Connection died.", e);
        }
        iterator.remove();
      }
    }
  }
}
