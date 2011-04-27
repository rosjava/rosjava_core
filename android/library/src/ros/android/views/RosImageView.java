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

package ros.android.views;

import com.google.common.base.Preconditions;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

import org.ros.MessageListener;
import org.ros.Node;
import org.ros.NodeConfiguration;
import org.ros.NodeMain;

/**
 * A camera node that publishes images and camera_info
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class RosImageView<T> extends ImageView implements NodeMain {

  private String topicName;
  private Class<T> messageClass;
  private MessageCallable<Bitmap, T> callable;
  private Node node;

  public RosImageView(Context context) {
    super(context);
  }

  public RosImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public RosImageView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public void setTopicName(String topicName) {
    this.topicName = topicName;
  }

  public void setMessageClass(Class<T> messageClass) {
    this.messageClass = messageClass;
  }
  
  public void setMessageToBitmapCallable(MessageCallable<Bitmap, T> callable) {
    this.callable = callable;
  }

  @Override
  public void run(NodeConfiguration nodeConfiguration) throws Exception {
    Preconditions.checkState(node == null);
    node = new Node("/anonymous", nodeConfiguration);
    node.createSubscriber(topicName, new MessageListener<T>() {
      @Override
      public void onNewMessage(final T message) {
        post(new Runnable() {
          @Override
          public void run() {
            setImageBitmap(callable.call(message));
          }
        });
      }
    }, messageClass);
  }

  public void stop() {
    Preconditions.checkNotNull(node);
    node.stop();
  }

}
