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

import java.lang.Runnable;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;
import android.view.LayoutInflater;

import ros.android.util.MasterChecker;
import ros.android.util.RobotDescription;

/** Data class behind view of one item in the list of ROS Masters.
 * Gets created with a master URI and a local host name, then starts a
 * MasterChecker to look up robot name and type. */
public class MasterItem implements MasterChecker.RobotDescriptionReceiver, MasterChecker.FailureHandler {
  private MasterChecker checker_;
  private View view_;
  private RobotDescription desc_;
  private String connection_status_;
  private MasterChooserActivity parent_mca_;

  public MasterItem( RobotDescription desc, String my_hostname, MasterChooserActivity parent_mca ) {
    parent_mca_ = parent_mca;
    desc_ = desc;
    connection_status_ = "...";
    checker_ = new MasterChecker( my_hostname, this, this );
    checker_.beginChecking( desc_.master_uri_ );
  }

  public boolean isOk() {
    return connection_status_ == "ok";
  }

  @Override
  public void receive( RobotDescription robot_description ) {
    desc_.copyFrom( robot_description );
    connection_status_ = "ok";
    safePopulateView();
  }

  @Override
  public void handleFailure( String reason ) {
    connection_status_ = reason;
    safePopulateView();
  }

  public View getView( Context context, View convert_view, ViewGroup parent ) {
    View new_view;
    if (convert_view == null) {  // if it's not recycled, initialize some attributes
      LayoutInflater inflater =
        (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
      new_view = inflater.inflate( R.layout.master_item, null );
    } else {
      new_view = convert_view;
    }
    view_ = new_view;
    populateView();
    return new_view;
  }

  private void safePopulateView() {
    if( view_ != null )
    {
      final MasterChooserActivity mca = parent_mca_;

      view_.post( new Runnable() {
          @Override
          public void run() {
            populateView();
            mca.writeRobotList();
          }
        });
    }
  }

  private void populateView() {
    TextView tv;
    tv = (TextView) view_.findViewById( R.id.uri );
    tv.setText( desc_.master_uri_ );

    tv = (TextView) view_.findViewById( R.id.name );
    tv.setText( desc_.robot_name_ );

    tv = (TextView) view_.findViewById( R.id.status );
    tv.setText( connection_status_ );

    ImageView iv = (ImageView) view_.findViewById( R.id.robot_icon );
    if( desc_.robot_type_ == null )
    {
      iv.setImageResource( R.drawable.question_mark );
    }
    else if( desc_.robot_type_.equals( "pr2" ))
    {
      iv.setImageResource( R.drawable.pr2 );
    }
    else if( desc_.robot_type_.equals( "turtlebot" ))
    {
      iv.setImageResource( R.drawable.turtlebot );
    }
    else
    {
      iv.setImageResource( R.drawable.question_mark );
    }
  }
}
