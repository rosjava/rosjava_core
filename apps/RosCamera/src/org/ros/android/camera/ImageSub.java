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
package org.ros.android.camera;

import android.content.Context;
import android.widget.ImageView;
import org.ros.MessageListener;
import org.ros.Node;
import org.ros.exceptions.RosInitException;
import org.ros.message.sensor_msgs.Image;

/**
 * A camera node that publishes images and camera_info
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * 
 */
public class ImageSub extends ImageView implements MessageListener<Image> {
  private org.ros.Subscriber<Image> imageSub;

  public ImageSub(Context ctx) {
    super(ctx);
  }

  public void init(Node node) throws RosInitException {
    imageSub = node.createSubscriber(node.resolveName("image"), this, Image.class);
  }

  public void stop() {
    imageSub.cancel();
  }

  @Override
  public void onNewMessage(Image message) {
    
  }

}
