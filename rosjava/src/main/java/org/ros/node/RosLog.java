package org.ros.node;

import org.slf4j.Marker;

/**
 * A subset of {@link org.slf4j.Logger} that allows logging in a {@link org.slf4j.Logger} and published in {@link org.ros.Topics#ROSOUT}
 * Created at 2022-06-15 on 12:43
 *
 * @author Spyros Koukas -
 */
public interface RosLog {
    String getName();

    boolean isTraceEnabled();

    boolean isDebugEnabled();


    boolean isErrorEnabled();


    boolean isInfoEnabled();


    boolean isWarnEnabled();


    void trace(String message);

    void trace(String message, Throwable t);

    void debug(String message);

    void debug(String message, Throwable t);

    void info(String message);

    void info(String message, Throwable t);

    void warn(String message);

    void warn(String message, Throwable t);

    void error(String message);

    void error(String message, Throwable t);

    void fatal(String message);

    void fatal(String message, Throwable t);




}
