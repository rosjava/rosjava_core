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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
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
public class ImageSub extends ImageView implements MessageListener<Image>, Runnable {
  private org.ros.Subscriber<Image> imageSub;

  public ImageSub(Context ctx) {
    super(ctx);
    // bitmap = Bitmap.createBitmap(width, height, config)
  }

  public void init(Node node) throws RosInitException {
    imageSub = node.createSubscriber(node.resolveName("image"), this, Image.class);
  }

  public void stop() {
    imageSub.cancel();
  }

  Bitmap bitmap;

  @Override
  public void onNewMessage(Image message) {
    // FIXME support more encodings.
    if (message.encoding != "rgb8") {
      Log.e("RosAndroid", "Unsopported encoding : " + message.encoding);
      return;
    }
    //if the bitmap is a different size or null allocate it.
    if (bitmap == null || bitmap.getWidth() != message.width
        || bitmap.getHeight() != message.height) {
      bitmap = Bitmap.createBitmap((int) message.width, (int) message.height,
          Bitmap.Config.ARGB_8888);
    }
    
    //copy the message data into the bitmap.
    for (int x = 0; x < message.width; x++) {
      for (int y = 0; y < message.height; y++) {
        byte red = message.data[(int) (y * message.step + x)];
        byte green = message.data[(int) (y * message.step + x + 1)];
        byte blue = message.data[(int) (y * message.step + x + 2)];
        bitmap.setPixel(x, y, Color.argb(1, red, green, blue));
      }
    }
    post(this);

  }

  @Override
  public void run() {
    setImageBitmap(bitmap);

  }

}
