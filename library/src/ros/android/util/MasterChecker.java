/*
 * Copyright (c) 2011, Willow Garage, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Willow Garage, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package ros.android.util;

import org.ros.Node;
import org.ros.NodeContext;
import org.ros.ParameterClient;
import org.ros.exceptions.RosInitException;

import java.util.Date;
import java.util.Random;
import java.lang.Thread;
import java.lang.InterruptedException;

import android.util.Log;

/** Threaded ROS-master checker.  Runs a thread which checks for a
 * valid ROS master and sends back a RobotDescription (with robot name
 * and type) on success or a failure reason on failure. */
public class MasterChecker {
  public interface RobotDescriptionReceiver {
    /** Called on success with a description of the robot that got checked. */
    void receive( RobotDescription robot_description );
  }
  public interface FailureHandler {
    /** Called on failure with a short description of why it failed, like "exception" or "timeout". */
    void handleFailure( String reason );
  }

  private CheckerThread thread_;
  private RobotDescriptionReceiver found_master_callback_;
  private FailureHandler failure_callback_;
  private String my_host_name_;

  /** Constructor.  Should not take any time. */
  public MasterChecker( String my_host_name, RobotDescriptionReceiver found_master_callback ) {
    this( my_host_name, found_master_callback, null );
  }

  /** Constructor.  Should not take any time. */
  public MasterChecker( String my_host_name, RobotDescriptionReceiver found_master_callback, FailureHandler failure_callback ) {
    my_host_name_ = my_host_name;
    found_master_callback_ = found_master_callback;
    failure_callback_ = failure_callback;
  }

  /** Start the checker thread with the given master URI.  If the
   * thread is already running, kill it first and then start anew.
   * Returns immediately. */
  public void beginChecking( String master_uri ) {
    stopChecking();
    thread_ = new CheckerThread( my_host_name_, found_master_callback_, failure_callback_ );
    thread_.robot_description_.master_uri_ = master_uri;
    thread_.robot_description_.robot_name_ = null;
    thread_.robot_description_.robot_type_ = null;
    thread_.robot_description_.time_last_seen_ = null;
    thread_.start();
  }

  /** Stop the checker thread. */
  public void stopChecking() {
    if( thread_ != null && thread_.isAlive() )
    {
      thread_.interrupt();
    }
  }

  private class CheckerThread extends Thread {
    private RobotDescriptionReceiver found_master_callback_;
    private FailureHandler failure_callback_;
    private String my_host_name_;
    public RobotDescription robot_description_;

    public CheckerThread( String my_host_name, RobotDescriptionReceiver found_master_callback, FailureHandler failure_callback ) {
      robot_description_ = new RobotDescription();
      my_host_name_ = my_host_name;
      found_master_callback_ = found_master_callback;
      failure_callback_ = failure_callback;

      setDaemon( true ); // don't require callers to explicitly kill all the old checker threads.
      setUncaughtExceptionHandler( new Thread.UncaughtExceptionHandler() {
          public void uncaughtException( Thread thread, Throwable ex ) {
            failure_callback_.handleFailure( "exception" );
          }
        } );
    }

    public void run() {
      try
      {
        Node node = new Node( "master_checker_" + new Random().nextInt(),
                              MasterChooser.createContext( robot_description_.master_uri_, my_host_name_ ));
        ParameterClient param_client = node.createParameterClient();
        robot_description_.robot_name_ = (String) param_client.getParam( "robot/name" );
        robot_description_.robot_type_ = (String) param_client.getParam( "robot/type" );
        robot_description_.time_last_seen_ = new Date(); // current time.
        found_master_callback_.receive( robot_description_ );
      }
      catch( Exception ex )
      {
        Log.e( "RosAndroid",
               "Exception while creating node in MasterChecker for master URI " + robot_description_.master_uri_ +
               " with my_host_name = " + my_host_name_ );
        failure_callback_.handleFailure( "exception" );
      }
    }
  }
}
