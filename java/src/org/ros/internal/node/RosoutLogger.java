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

import org.ros.Publisher;
import org.ros.internal.node.client.TimeProvider;

import org.apache.commons.logging.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

// TODO(damonkohler): This should be wrapped up in the Node already with a
// getter. That way things like getTopics() will fell natural in this class and
// we can get rid of setRosoutPublisher() which is probably error prone.
/**
 * Logger that logs to both an underlying Apache Commons Log as well as /rosout.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class RosoutLogger implements Log {

  private Publisher<org.ros.message.rosgraph_msgs.Log> rosoutPublisher;
  private final Log log;
  private final TimeProvider timeProvider;

  public RosoutLogger(Log log, TimeProvider timeProvider) {
    this.log = log;
    this.timeProvider = timeProvider;
  }

  public void setRosoutPublisher(Publisher<org.ros.message.rosgraph_msgs.Log> rosoutPublisher) {
    this.rosoutPublisher = rosoutPublisher;
  }

  private void publishToRosout(Object message, Throwable t) {
    final Writer result = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(result);
    t.printStackTrace(printWriter);
    publishToRosout(message.toString() + '\n' + printWriter.toString());
  }

  private void publishToRosout(Object message) {
    org.ros.message.rosgraph_msgs.Log m = new org.ros.message.rosgraph_msgs.Log();
    m.msg = message.toString();
    m.header.stamp = timeProvider.currentTime();
    m.topics = getTopics();
    rosoutPublisher.publish(m);
  }

  private ArrayList<String> getTopics() {
    // TODO implement. Should return list of topics that node is involved with.
    // This helps filter the rosoutconsole.
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
    if (log.isTraceEnabled() && rosoutPublisher != null) {
      publishToRosout(message);
    }
  }

  @Override
  public void trace(Object message, Throwable t) {
    log.trace(message, t);
    if (log.isTraceEnabled() && rosoutPublisher != null) {
      publishToRosout(message, t);
    }
  }

  @Override
  public void debug(Object message) {
    log.debug(message);
    if (log.isDebugEnabled() && rosoutPublisher != null) {
      publishToRosout(message);
    }
  }

  @Override
  public void debug(Object message, Throwable t) {
    log.debug(message, t);
    if (log.isDebugEnabled() && rosoutPublisher != null) {
      publishToRosout(message, t);
    }
  }

  @Override
  public void info(Object message) {
    log.info(message);
    if (log.isInfoEnabled() && rosoutPublisher != null) {
      publishToRosout(message);
    }
  }

  @Override
  public void info(Object message, Throwable t) {
    log.info(message, t);
    if (log.isInfoEnabled() && rosoutPublisher != null) {
      publishToRosout(message, t);
    }
  }

  @Override
  public void warn(Object message) {
    log.warn(message);
    if (log.isWarnEnabled() && rosoutPublisher != null) {
      publishToRosout(message);
    }
  }

  @Override
  public void warn(Object message, Throwable t) {
    log.warn(message, t);
    if (log.isWarnEnabled() && rosoutPublisher != null) {
      publishToRosout(message, t);
    }
  }

  @Override
  public void error(Object message) {
    log.error(message);
    if (log.isErrorEnabled() && rosoutPublisher != null) {
      publishToRosout(message);
    }
  }

  @Override
  public void error(Object message, Throwable t) {
    log.error(message, t);
    if (log.isErrorEnabled() && rosoutPublisher != null) {
      publishToRosout(message, t);
    }
  }

  @Override
  public void fatal(Object message) {
    log.fatal(message);
    if (log.isFatalEnabled() && rosoutPublisher != null) {
      publishToRosout(message);
    }
  }

  @Override
  public void fatal(Object message, Throwable t) {
    log.fatal(message, t);
    if (log.isFatalEnabled() && rosoutPublisher != null) {
      publishToRosout(message, t);
    }
  }

}
