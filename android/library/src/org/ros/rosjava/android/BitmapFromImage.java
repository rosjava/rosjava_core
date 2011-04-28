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

package org.ros.rosjava.android;

import com.google.common.base.Preconditions;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.ros.message.sensor_msgs.Image;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class BitmapFromImage implements MessageCallable<Bitmap, Image> {

  @Override
  public Bitmap call(Image message) {
    Preconditions.checkArgument(message.encoding.equals("rgb8"));
    Bitmap bitmap =
        Bitmap.createBitmap((int) message.width, (int) message.height, Bitmap.Config.ARGB_8888);
    for (int x = 0; x < message.width; x++) {
      for (int y = 0; y < message.height; y++) {
        byte red = message.data[(int) (y * message.step + 3 * x)];
        byte green = message.data[(int) (y * message.step + 3 * x + 1)];
        byte blue = message.data[(int) (y * message.step + 3 * x + 2)];
        bitmap.setPixel(x, y, Color.argb(255, red & 0xFF, green & 0xFF, blue & 0xFF));
      }
    }
    return bitmap;
  }

}
