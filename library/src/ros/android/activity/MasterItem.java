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

package ros.android.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import ros.android.util.MasterChecker;
import ros.android.util.RobotDescription;

/**
 * Data class behind view of one item in the list of ROS Masters. Gets created
 * with a master URI and a local host name, then starts a MasterChecker to look
 * up robot name and type.
 */
public class MasterItem implements MasterChecker.RobotDescriptionReceiver,
    MasterChecker.FailureHandler {
  private MasterChecker checker;
  private View view;
  private RobotDescription description;
  private String connectionStatus;
  private MasterChooserActivity parentMca;

  public MasterItem(RobotDescription robotDescription, String myHostname,
      MasterChooserActivity parentMca) {
    this.parentMca = parentMca;
    this.description = robotDescription;
    connectionStatus = "...";
    checker = new MasterChecker(myHostname, this, this);
    checker.beginChecking(this.description.masterUri);
  }

  public boolean isOk() {
    return connectionStatus == "ok";
  }

  @Override
  public void receive(RobotDescription robotDescription) {
    description.copyFrom(robotDescription);
    connectionStatus = "ok";
    safePopulateView();
  }

  @Override
  public void handleFailure(String reason) {
    connectionStatus = reason;
    safePopulateView();
  }

  public View getView(Context context, View convert_view, ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    // Using convert_view here seems to cause the wrong view to show
    // up sometimes, so I'm always making new ones.
    view = inflater.inflate(R.layout.master_item, null);
    populateView();
    return view;
  }

  private void safePopulateView() {
    if (view != null) {
      final MasterChooserActivity mca = parentMca;

      view.post(new Runnable() {
        @Override
        public void run() {
          populateView();
          mca.writeRobotList();
        }
      });
    }
  }

  private void populateView() {
    boolean statusOk = connectionStatus.equals("ok");

    ProgressBar progress = (ProgressBar) view.findViewById(R.id.progress_circle);
    progress.setIndeterminate(true);
    progress.setVisibility(statusOk ? View.GONE : View.VISIBLE);

    TextView tv;
    tv = (TextView) view.findViewById(R.id.uri);
    tv.setText(description.masterUri);

    tv = (TextView) view.findViewById(R.id.name);
    tv.setText(description.robotName);

    tv = (TextView) view.findViewById(R.id.status);
    tv.setText(connectionStatus);

    ImageView iv = (ImageView) view.findViewById(R.id.robot_icon);
    iv.setVisibility(statusOk ? View.VISIBLE : View.GONE);
    if (description.robotType == null) {
      iv.setImageResource(R.drawable.question_mark);
    } else if (description.robotType.equals("pr2")) {
      iv.setImageResource(R.drawable.pr2);
    } else if (description.robotType.equals("turtlebot")) {
      iv.setImageResource(R.drawable.turtlebot);
    } else {
      iv.setImageResource(R.drawable.question_mark);
    }
  }
}
