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

package org.ros.tutorials.pubsub;

import com.google.common.collect.Lists;

import android.app.Activity;
import android.os.Bundle;

import org.ros.NodeRunner;
import org.ros.RosCore;

import org.ros.rosjava.android.MessageCallable;
import org.ros.rosjava.android.views.RosTextView;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MainActivity extends Activity {

  private final NodeRunner nodeRunner;

  public MainActivity() {
    super();
    nodeRunner = NodeRunner.createDefault();
  }
  
  @Override
  protected void onPause() {
    super.onPause();
    finish();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    RosTextView<org.ros.message.std_msgs.String> rosTextView =
        (RosTextView<org.ros.message.std_msgs.String>) findViewById(R.id.text);
    rosTextView.setTopicName("/chatter");
    rosTextView.setMessageClass(org.ros.message.std_msgs.String.class);
    rosTextView
        .setMessageToStringCallable(new MessageCallable<String, org.ros.message.std_msgs.String>() {
          @Override
          public String call(org.ros.message.std_msgs.String message) {
            return message.data;
          }
        });
    try {
      // TODO(damonkohler): The master needs to be set via some sort of
      // NodeConfiguration builder.
      RosCore rosCore = new RosCore();
      nodeRunner.run(rosCore, Lists.newArrayList("RosCore", "__master:=foo"));
      rosCore.awaitStart();
      String uri = "__master:=" + rosCore.getUri().toString();
      nodeRunner.run(new Talker(), Lists.newArrayList("Talker", uri));
      nodeRunner.run(rosTextView, Lists.newArrayList("Listener", uri));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
