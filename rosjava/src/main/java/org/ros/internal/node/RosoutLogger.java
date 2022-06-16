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

import com.google.common.base.Preconditions;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ros.Topics;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import rosgraph_msgs.Log;

import java.util.function.Consumer;

/**
 * Logger that logs to both an underlying {@link org.slf4j.Logger} as well as /rosout.
 * The graph name of the node is added as {@link Marker} after the connection.
 *
 * @author kwc@willowgarage.com (Ken Conley)
 * @author damonkohler@google.com (Damon Kohler)
 */
final class RosoutLogger implements org.ros.node.RosLog {

    private ConnectedNode connectedNode;
    private Publisher<rosgraph_msgs.Log> publisher;
    private final Logger logger;
    private Marker marker = null;

    final void connect(final ConnectedNode connectedNode) {
        Preconditions.checkNotNull(connectedNode, "default node should not be null");
        this.connectedNode = connectedNode;
        this.publisher = this.connectedNode.newPublisher(Topics.ROSOUT, rosgraph_msgs.Log._TYPE);
        this.marker = MarkerFactory.getMarker(connectedNode.getName().toString());
    }



    /**
     * Starts logging disconnected from ROS, using only {@link Logger}
     * It needs to be connected using
     */
    public RosoutLogger() {
        final String loggerName = RosoutLogger.class.getCanonicalName();
        this.logger = LoggerFactory.getLogger(loggerName);
    }

    /**
     * Starts logging connected to ROS, if usePublisher is not null it is applied
     *
     * @param defaultNode
     * @param usePublisher
     */
    RosoutLogger(final ConnectedNode connectedNode, final Consumer<Publisher<Log>> usePublisher) {
        this();
        this.connect(connectedNode);
        if (usePublisher != null) {
            usePublisher.accept(this.publisher);
        }
    }

    private final void publish(final byte level, final String message, final Throwable throwable) {
        final String throwableStack = ExceptionUtils.getStackTrace(throwable);
        this.publish(level, message + '\n' + throwableStack);
    }

    private final void publish(final byte level, final String message) {

        final rosgraph_msgs.Log logMessage = this.publisher.newMessage();
        logMessage.getHeader().setStamp(this.connectedNode.getCurrentTime());
        logMessage.setLevel(level);
        logMessage.setName(this.connectedNode.getName().toString());
        logMessage.setMsg(message);


        // TODO(damonkohler): Should update the topics field with a list of all
        // published and subscribed topics for the node that created thisthis.logger.
        // This helps filter the rosoutconsole.
        this.publisher.publish(logMessage);
    }


    @Override
    public final String getName() {
        return this.logger.getName();
    }

    @Override
    public final boolean isTraceEnabled() {
        return this.logger.isTraceEnabled();
    }

    @Override
    public final boolean isDebugEnabled() {
        return this.logger.isDebugEnabled();
    }


    @Override
    public final boolean isErrorEnabled() {
        return this.logger.isErrorEnabled();
    }


    @Override
    public final boolean isInfoEnabled() {
        return this.logger.isInfoEnabled();
    }


    @Override
    public final boolean isWarnEnabled() {
        return this.logger.isWarnEnabled();
    }


    @Override
    public final void trace(final String message) {

        if (this.marker == null) {
            this.logger.trace(message);
        } else {
            this.logger.trace(this.marker, message);
        }

        if (this.logger.isTraceEnabled() && this.publisher != null) {
            this.publish(Log.DEBUG, message);
        }
    }

    @Override
    public final void trace(final String message, final Throwable t) {
        if (this.marker == null) {
            this.logger.trace(message, t);
        } else {
            this.logger.trace(this.marker, message, t);
        }
        if (this.logger.isTraceEnabled() && this.publisher != null) {
            this.publish(rosgraph_msgs.Log.DEBUG, message, t);
        }
    }

    @Override
    public final void debug(final String message) {
        if (this.marker == null) {
            this.logger.debug(message);
        } else {
            this.logger.debug(this.marker, message);
        }
        if (this.logger.isDebugEnabled() && this.publisher != null) {
            this.publish(rosgraph_msgs.Log.DEBUG, message);
        }
    }

    @Override
    public final void debug(final String message, final Throwable t) {
        if (this.marker == null) {
            this.logger.debug(message, t);
        } else {
            this.logger.debug(this.marker, message, t);
        }
        if (this.logger.isDebugEnabled() && this.publisher != null) {
            this.publish(rosgraph_msgs.Log.DEBUG, message, t);
        }
    }

    @Override
    public final void info(final String message) {
        if (this.marker == null) {
            this.logger.info(message);
        } else {
            this.logger.info(this.marker, message);
        }
        if (this.logger.isInfoEnabled() && this.publisher != null) {
            this.publish(rosgraph_msgs.Log.INFO, message);
        }
    }

    @Override
    public final void info(final String message, final Throwable t) {
        if (this.marker == null) {
            this.logger.info(message, t);
        } else {
            this.logger.info(this.marker, message, t);
        }
        if (this.logger.isInfoEnabled() && this.publisher != null) {
            this.publish(rosgraph_msgs.Log.INFO, message, t);
        }
    }

    @Override
    public final void warn(final String message) {
        if (this.marker == null) {
            this.logger.warn(message);
        } else {
            this.logger.warn(this.marker, message);
        }
        if (this.logger.isWarnEnabled() && this.publisher != null) {
            this.publish(rosgraph_msgs.Log.WARN, message);
        }
    }

    @Override
    public final void warn(final String message, final Throwable t) {
        if (this.marker == null) {
            this.logger.warn(message, t);
        } else {
            this.logger.warn(this.marker, message, t);
        }
        if (this.logger.isWarnEnabled() && this.publisher != null) {
            this.publish(rosgraph_msgs.Log.WARN, message, t);
        }
    }

    @Override
    public final void error(final String message) {
        if (this.marker == null) {
            this.logger.error(message);
        } else {
            this.logger.error(this.marker, message);
        }
        if (this.logger.isErrorEnabled() && this.publisher != null) {
            this.publish(rosgraph_msgs.Log.ERROR, message);
        }
    }

    @Override
    public final void error(final String message, final Throwable t) {
        if (this.marker == null) {
            this.logger.error(message, t);
        } else {
            this.logger.error(this.marker, message, t);
        }
        if (this.logger.isErrorEnabled() && this.publisher != null) {
            this.publish(rosgraph_msgs.Log.ERROR, message, t);
        }
    }

    @Override
    public final void fatal(final String message) {
        if (this.marker == null) {
            this.logger.error(message);
        } else {
            this.logger.error(this.marker, message);
        }
        if (this.logger.isErrorEnabled() && this.publisher != null) {
            this.publish(Log.FATAL, message);
        }
    }

    @Override
    public final void fatal(final String message, final Throwable t) {
        if (this.marker == null) {
            this.logger.error(message, t);
        } else {
            this.logger.error(this.marker, message, t);
        }
        if (this.logger.isErrorEnabled() && this.publisher != null) {
            this.publish(Log.FATAL, message, t);
        }
    }

}
