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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.ros.message.sensor_msgs.CompressedImage;
import org.ros.rosjava.android.views.MessageCallable;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class BitmapFromCompressedImage implements MessageCallable<Bitmap, CompressedImage> {

  @Override
  public Bitmap call(CompressedImage message) {
    return BitmapFactory.decodeByteArray(message.data, 0, message.data.length);
  }

}
