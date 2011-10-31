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

import com.google.common.annotations.VisibleForTesting;

import org.ros.time.TimeProvider;

import org.apache.commons.logging.Log;
import org.ros.node.topic.Publisher;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

/**
 * Logger that logs to both an underlying Apache Commons Log as well as /rosout.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class RosoutLogger implements Log {

  private final Log log;
  private final Publisher<org.ros.message.rosgraph_msgs.Log> publisher;
  private final TimeProvider timeProvider;

  RosoutLogger(Log log, Publisher<org.ros.message.rosgraph_msgs.Log> publisher,
      TimeProvider timeProvider) {
    this.log = log;
    this.publisher = publisher;
    this.timeProvider = timeProvider;
  }

  @VisibleForTesting
  Publisher<org.ros.message.rosgraph_msgs.Log> getPublisher() {
    return publisher;
  }

  private void publish(Object message, Throwable throwable) {
    PrintWriter printWriter = new PrintWriter(new StringWriter());
    throwable.printStackTrace(printWriter);
    publish(message.toString() + '\n' + printWriter.toString());
  }

  private void publish(Object message) {
    org.ros.message.rosgraph_msgs.Log m = new org.ros.message.rosgraph_msgs.Log();
    m.msg = message.toString();
    m.header.stamp = timeProvider.getCurrentTime();
    m.topics = getTopics();
    publisher.publish(m);
  }

  private ArrayList<String> getTopics() {
    // TODO(damonkohler): Should return list of all published and subscribed
    // topics for the node that created this logger. This helps filter the
    // rosoutconsole.
    return new ArrayList<String>();
  }

  @Override
  public boolean isDebugEnabled() {
    return log.isDebugEnabled();
  }

  @Override
  public boolean isErrorEnabled() {
    return log.isErrorEnabled();
  }

  @Override
  public boolean isFatalEnabled() {
    return log.isFatalEnabled();
  }

  @Override
  public boolean isInfoEnabled() {
    return log.isInfoEnabled();
  }

  @Override
  public boolean isTraceEnabled() {
    return log.isTraceEnabled();
  }

  @Override
  public boolean isWarnEnabled() {
    return log.isWarnEnabled();
  }

  @Override
  public void trace(Object message) {
    log.trace(message);
    if (log.isTraceEnabled() && publisher != null) {
      publish(message);
    }
  }

  @Override
  public void trace(Object message, Throwable t) {
    log.trace(message, t);
    if (log.isTraceEnabled() && publisher != null) {
      publish(message, t);
    }
  }

  @Override
  public void debug(Object message) {
    log.debug(message);
    if (log.isDebugEnabled() && publisher != null) {
      publish(message);
    }
  }

  @Override
  public void debug(Object message, Throwable t) {
    log.debug(message, t);
    if (log.isDebugEnabled() && publisher != null) {
      publish(message, t);
    }
  }

  @Override
  public void info(Object message) {
    log.info(message);
    if (log.isInfoEnabled() && publisher != null) {
      publish(message);
    }
  }

  @Override
  public void info(Object message, Throwable t) {
    log.info(message, t);
    if (log.isInfoEnabled() && publisher != null) {
      publish(message, t);
    }
  }

  @Override
  public void warn(Object message) {
    log.warn(message);
    if (log.isWarnEnabled() && publisher != null) {
      publish(message);
    }
  }

  @Override
  public void warn(Object message, Throwable t) {
    log.warn(message, t);
    if (log.isWarnEnabled() && publisher != null) {
      publish(message, t);
    }
  }

  @Override
  public void error(Object message) {
    log.error(message);
    if (log.isErrorEnabled() && publisher != null) {
      publish(message);
    }
  }

  @Override
  public void error(Object message, Throwable t) {
    log.error(message, t);
    if (log.isErrorEnabled() && publisher != null) {
      publish(message, t);
    }
  }

  @Override
  public void fatal(Object message) {
    log.fatal(message);
    if (log.isFatalEnabled() && publisher != null) {
      publish(message);
    }
  }

  @Override
  public void fatal(Object message, Throwable t) {
    log.fatal(message, t);
    if (log.isFatalEnabled() && publisher != null) {
      publish(message, t);
    }
  }

}
