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
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.util.ArrayList;

import org.ros.Node;

import ros.android.activity.R;

public class TurtlebotDashboard extends LinearLayout {
  private Node node;

  private ImageButton diagnosticsButton;
  private ImageButton rosoutButton;
  private ImageButton modeButton;

  private Button[] breakerButtons = new Button[3];

  private ProgressBar robotBatteryBar;
  private ProgressBar laptopBatteryBar;

  public TurtlebotDashboard(Context context) {
    super(context);
    inflateSelf(context);
  }

  public TurtlebotDashboard(Context context, AttributeSet attrs) {
    super(context, attrs);
// TODO: make orientation settable in xml.
//    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TurtlebotDashboard);
//    CharSequence s = a.getString(R.styleable.TurtlebotDashboard_orientation);
//    a.recycle();
    inflateSelf(context);
  }

  private void inflateSelf(Context context) {
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

    robotBatteryBar.setProgress(50);
    laptopBatteryBar.setProgress(75);
  }

  public void setNode( Node node ) {
    this.node = node;
  }
}
