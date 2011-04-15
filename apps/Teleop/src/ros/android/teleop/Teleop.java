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

package ros.android.teleop;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import org.ros.MessageListener;
import org.ros.Node;
import org.ros.Publisher;
import org.ros.Subscriber;
import org.ros.exceptions.RosInitException;
import org.ros.message.Message;
import org.ros.message.app_manager.AppStatus;
import org.ros.message.geometry_msgs.Twist;
import org.ros.namespace.Namespace;
import org.ros.service.app_manager.StartApp;
import ros.android.activity.RosAppActivity;
import ros.android.views.SensorImageView;

/**
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class Teleop extends RosAppActivity implements OnTouchListener {
  private Publisher<Twist> twistPub;
  private SensorImageView imageView;
  private Thread pubThread;
  private boolean deadman;
  private Twist touchCmdMessage;
  private float motionY;
  private float motionX;
  private Subscriber<AppStatus> statusSub;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
    setContentView(R.layout.main);

    View mainView = findViewById(R.id.image);
    mainView.setOnTouchListener(this);

    imageView = (SensorImageView) findViewById(R.id.image);
    // imageView.setOnTouchListener(this);
    touchCmdMessage = new Twist();
  }

  @Override
  protected void onNodeDestroy(Node node) {
    deadman = false;
    if (twistPub != null) {
      twistPub.shutdown();
      twistPub = null;
    }
    if (imageView != null) {
      imageView.stop();
      imageView = null;
    }
    if (statusSub != null) {
      statusSub.cancel();
      statusSub = null;
    }
    if (pubThread != null) {
      pubThread.interrupt();
      pubThread = null;
    }
    super.onNodeDestroy(node);
  }

  private <T extends Message> void createPublisherThread(final Publisher<T> pub, final T message,
      final int rate) {
    pubThread = new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          while (true) {
            pub.publish(message);
            Thread.sleep(1000 / rate);
          }
        } catch (InterruptedException e) {
        }
      }
    });
    Log.i("Teleop", "started pub thread");
    pubThread.start();
  }

  private void initRos() {
    try {
      Log.i("Teleop", "getNode()");
      Node node = getNode();
      Namespace appNamespace = getAppNamespace(node);
      imageView = (SensorImageView) findViewById(R.id.image);
      Log.i("Teleop", "init imageView");
      imageView.init(node, appNamespace.resolveName("camera/rgb/image_color/compressed"));
      imageView.post(new Runnable() {

        @Override
        public void run() {
          imageView.setSelected(true);
        }
      });
      Log.i("Teleop", "init twistPub");
      twistPub = appNamespace.createPublisher("turtlebot_node/cmd_vel", Twist.class);
      createPublisherThread(twistPub, touchCmdMessage, 10);
    } catch (RosInitException e) {
      Log.e("Teleop", e.getMessage());
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    Toast.makeText(Teleop.this, "starting app", Toast.LENGTH_LONG).show();
  }

  @Override
  protected void onNodeCreate(Node node) {
    Log.i("Teleop", "startAppFuture");
    super.onNodeCreate(node);
    startApp();
  }

  private void startApp() {
    appManager.startApp("turtlebot_teleop/android_teleop",
        new MessageListener<StartApp.Response>() {
          @Override
          public void onSuccess(StartApp.Response message) {
            initRos();
            // TODO(kwc): add status code for app already running
            /*
             * if (message.started) { safeToastStatus("started"); initRos(); }
             * else { safeToastStatus(message.message); }
             */
          }

          @Override
          public void onFailure(Exception e) {
            safeToastStatus("Failed: " + e.getMessage());
          }
        });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.teleop_switch, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // TODO: REMOVE
    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onTouch(View arg0, MotionEvent motionEvent) {
    int action = motionEvent.getAction();
    if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
      deadman = true;

      motionX = (motionEvent.getX() - (arg0.getWidth() / 2)) / (arg0.getWidth());
      motionY = (motionEvent.getY() - (arg0.getHeight() / 2)) / (arg0.getHeight());

      touchCmdMessage.linear.x = -motionY;
      touchCmdMessage.linear.y = 0;
      touchCmdMessage.linear.z = 0;
      touchCmdMessage.angular.x = 0;
      touchCmdMessage.angular.y = 0;
      touchCmdMessage.angular.z = -2 * motionX;

    } else {
      deadman = false;
      touchCmdMessage.linear.x = 0;
      touchCmdMessage.linear.y = 0;
      touchCmdMessage.linear.z = 0;
      touchCmdMessage.angular.x = 0;
      touchCmdMessage.angular.y = 0;
      touchCmdMessage.angular.z = 0;
    }
    return true;
  }

  private void safeToastStatus(final String message) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(Teleop.this, message, Toast.LENGTH_SHORT).show();
      }
    });
  }

}