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

import android.graphics.Color;

import android.util.Log;

import org.ros.message.sensor_msgs.Image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.widget.ImageView;
import org.ros.MessageListener;
import org.ros.Node;
import org.ros.exceptions.RosInitException;
import org.ros.message.sensor_msgs.CompressedImage;

/**
 * A camera node that publishes images and camera_info
 * 
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 */
public class SensorImageView extends ImageView implements MessageListener<CompressedImage>,
    Runnable {
  private org.ros.Subscriber<CompressedImage> imageSub;

  public SensorImageView(Context ctx) {
    super(ctx);
  }

  public SensorImageView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public SensorImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void init(Node node, String topic) throws RosInitException {
    imageSub = node.createSubscriber(topic, this, CompressedImage.class);
  }

  public void stop() {
    if( imageSub != null ) {
      imageSub.cancel();
    }
    imageSub = null;
  }

  Bitmap bitmap;

  @Override
  public void onSuccess(CompressedImage message) {
    bitmap = BitmapFactory.decodeByteArray(message.data, 0, message.data.length);
    post(this);
  }

  @Override
  public void onFailure(Exception e) {
    // TODO Auto-generated method stub
  }

  /**
   * FIXME make this switchable.
   * 
   * @param message
   */
  public void decodeNonCompressed(Image message) {
    // FIXME support more encodings.
    if (!message.encoding.equals("rgb8")) {
      Log.e("RosAndroid", "Unsopported encoding : " + message.encoding);
      return;
    }
    // if the bitmap is a different size or null allocate it.
    if (bitmap == null || bitmap.getWidth() != message.width
        || bitmap.getHeight() != message.height) {
      bitmap = Bitmap.createBitmap((int) message.width, (int) message.height,
          Bitmap.Config.ARGB_8888);
    }
    // copy the message data into the bitmap.
    for (int x = 0; x < message.width; x++) {
      for (int y = 0; y < message.height; y++) {
        byte red = message.data[(int) (y * message.step + x)];
        byte green = message.data[(int) (y * message.step + x + 1)];
        byte blue = message.data[(int) (y * message.step + x + 2)];
        bitmap.setPixel(x, y, Color.argb(1, blue, green, red));
      }
    }
  }

  @Override
  public void run() {
    setImageBitmap(bitmap);
  }

}
