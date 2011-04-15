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

package ros.android.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.HashMap;

import org.ros.MessageListener;
import org.ros.Node;
import org.ros.Subscriber;
import org.ros.exceptions.RosInitException;
import org.ros.message.diagnostic_msgs.DiagnosticArray;
import org.ros.message.diagnostic_msgs.DiagnosticStatus;
import org.ros.message.diagnostic_msgs.KeyValue;

import ros.android.activity.R;

public class TurtlebotDashboard extends LinearLayout {
  private ImageButton diagnosticsButton;
  private ImageButton rosoutButton;
  private ImageButton modeButton;
  private Button[] breakerButtons = new Button[3];
  private ProgressBar robotBatteryBar;
  private ProgressBar laptopBatteryBar;
  private View robotChargingIndicator;
  private View laptopChargingIndicator;

  private Node node;
  private Subscriber<DiagnosticArray> diagnosticSubscriber;

  public TurtlebotDashboard(Context context) {
    super(context);
    Log.i("RosAndroid", "TurtlebotDashboard constructor.");
    inflateSelf(context);
  }

  public TurtlebotDashboard(Context context, AttributeSet attrs) {
    super(context, attrs);
    Log.i("RosAndroid", "TurtlebotDashboard constructor2.");
// TODO: make orientation settable in xml. (?)
//    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TurtlebotDashboard);
//    CharSequence s = a.getString(R.styleable.TurtlebotDashboard_orientation);
//    a.recycle();
    inflateSelf(context);
  }

  private void inflateSelf(Context context) {
    Log.i("RosAndroid", "TurtlebotDashboard inflateSelf().");
    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.turtlebot_dashboard, this);

    diagnosticsButton = (ImageButton) findViewById(R.id.diagnostics_button);
    rosoutButton = (ImageButton) findViewById(R.id.rosout_button);
    modeButton = (ImageButton) findViewById(R.id.mode_button);

    breakerButtons[0] = (Button) findViewById(R.id.breaker_1_button);
    breakerButtons[1] = (Button) findViewById(R.id.breaker_2_button);
    breakerButtons[2] = (Button) findViewById(R.id.breaker_3_button);

    robotBatteryBar = (ProgressBar) findViewById(R.id.robot_battery_bar);
    laptopBatteryBar = (ProgressBar) findViewById(R.id.laptop_battery_bar);

    robotChargingIndicator = (View) findViewById(R.id.robot_charging_indicator);
    laptopChargingIndicator = (View) findViewById(R.id.laptop_charging_indicator);

    // TODO: set all controls gray initially meaning no data has arrived.
  }

  /**
   * Set the ROS Node to use to get status data and connect it up.
   * Disconnects the previous node if there was one.  Call this with
   * null to disconnect.
   */
  public void setNode( Node node ) {
    Log.i("RosAndroid", "TurtlebotDashboard setNode().");
    if(node == this.node) {
      Log.i("RosAndroid", "TurtlebotDashboard setNode() duplicate.");
      return;
    }
    if(this.node != null) {
      disconnectNode();
    }
    this.node = node;
    if( this.node != null ) {
      try {
        connectNode();
      } catch( RosInitException ex ) {
        Log.e("RosAndroid", "TurtlebotDashboard: setNode() caught RosInitException: " + ex.getMessage());
        this.node = null;
      }
    } else {
      Log.i("RosAndroid", "TurtlebotDashboard setNode() new node is null.");
    }
  }

  public Node getNode() {
    return node;
  }

  private void connectNode() throws RosInitException {
    Log.i("RosAndroid", "TurtlebotDashboard connectNode().");

    diagnosticSubscriber = node.createSubscriber("diagnostics_agg", new MessageListener<DiagnosticArray>() {
        @Override
        public void onSuccess( final DiagnosticArray msg ) {
          TurtlebotDashboard.this.post( new Runnable() {
              @Override
              public void run() {
                TurtlebotDashboard.this.handleDiagnosticArray( msg );
              }
            });
        }
        @Override
        public void onFailure(Exception e) { 
        }
      }, DiagnosticArray.class);
  }

  private void disconnectNode() {
    Log.i("RosAndroid", "TurtlebotDashboard disconnectNode().");
    diagnosticSubscriber.cancel();
  }

  /**
   * Populate view with new diagnostic data.  This must be called in the UI thread.
   */
  private void handleDiagnosticArray(DiagnosticArray msg) {
    for(DiagnosticStatus status: msg.status) {
      if(status.name.equals("/Power System/Battery")) {
        populateBatteryFromStatus( robotBatteryBar, robotChargingIndicator, status );
      }
      if(status.name.equals("/Power System/Laptop Battery")) {
        populateBatteryFromStatus( laptopBatteryBar, laptopChargingIndicator, status );
      }
    }
  }

  private void populateBatteryFromStatus( ProgressBar bar, View charging_indicator, DiagnosticStatus status ) {
    HashMap<String,String> values = keyValueArrayToMap( status.values );
    try {
      float percent = 100 * Float.parseFloat(values.get("Charge (Ah)")) / Float.parseFloat(values.get("Capacity (Ah)"));
      bar.setProgress( (int) percent );
      // TODO: set color red/yellow/green based on level (maybe with level-set in XML)
    } catch( NumberFormatException ex ) {
      // TODO: make battery level gray
    } catch( ArithmeticException ex ) {
      // TODO: make battery level gray
    } catch( NullPointerException ex ) {
      // Do nothing: data wasn't there.
    }
    try {
      if(Float.parseFloat(values.get("Current (A)")) > 0) {
        charging_indicator.setVisibility(View.VISIBLE);
      } else {
        charging_indicator.setVisibility(View.INVISIBLE);
      }
    } catch( NumberFormatException ex ) {
    } catch( ArithmeticException ex ) {
    } catch( NullPointerException ex ) {
    }
  }

  private HashMap<String,String> keyValueArrayToMap(ArrayList<KeyValue> kvs) {
    HashMap<String,String> map = new HashMap<String,String>();
    for(KeyValue kv: kvs) {
      map.put(kv.key, kv.value);
    }
    return map;
  }
}
