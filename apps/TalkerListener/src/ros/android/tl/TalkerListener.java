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

package ros.android.tl;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import org.ros.MessageListener;
import org.ros.Node;
import org.ros.Publisher;
import org.ros.exceptions.RosInitException;
import ros.android.activity.RosActivity;

/**
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 */
public class TalkerListener extends RosActivity {

  private Publisher<org.ros.message.std_msgs.String> pub;
  private Thread pubThread;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void onResume() {
    super.onResume();
    setContentView(R.layout.main);
    setText("loading");
  }

  @Override
  protected void onNodeDestroy(Node node) {
    if (pubThread != null) {
      pubThread.interrupt();
    }
    pubThread = null;
  }

  @Override
  protected void onNodeCreate(Node node) {
    try {
      Log.i("RosAndroid", "Setting up sub /chatter");
      node.createSubscriber("chatter", new MessageListener<org.ros.message.std_msgs.String>() {
        @Override
        public void onSuccess(final org.ros.message.std_msgs.String message) {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              setText(message.data);
            }
          });

        }

        @Override
        public void onFailure(Exception e) { 
        }
      }, org.ros.message.std_msgs.String.class);

      final org.ros.message.std_msgs.String message = new org.ros.message.std_msgs.String();
      message.data = "hello from " + getString(R.string.app_name);

      Log.i("RosAndroid", "Setting up pub on /android_chatter");
      pub = node.createPublisher("android_chatter", org.ros.message.std_msgs.String.class);
      pubThread = new Thread(new Runnable() {

        @Override
        public void run() {
          try {
            while (true) {
              pub.publish(message);
              Thread.sleep(100);
            }
          } catch (InterruptedException e) {
          }
        }
      });
      pubThread.start();

    } catch (RosInitException e) {
      setText(e.getMessage());
    }

  }

  private void setText(String text) {
    TextView t = (TextView) findViewById(R.id.text_view);
    t.setText(text);
  }

}