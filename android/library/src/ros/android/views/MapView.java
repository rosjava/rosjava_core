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

package ros.android.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import org.ros.MessageListener;
import org.ros.Node;
import org.ros.Subscriber;
import org.ros.exceptions.RosInitException;
import org.ros.message.nav_msgs.OccupancyGrid;
import org.ros.message.nav_msgs.MapMetaData;

public class MapView extends ImageView {
  private Subscriber<OccupancyGrid> mapSubscriber;
  private Node node;
  private Bitmap bitmap;

  public MapView(Context ctx) {
    super(ctx);
  }

  public MapView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public MapView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  /**
   * Set the ROS Node to use to get status data and connect it up. Disconnects
   * the previous node if there was one. Call this with null to disconnect.
   */
  public void setNode(Node node) {
    if (node == this.node) {
      return;
    }
    if (this.node != null) {
      disconnectNode();
    }
    this.node = node;
    if (this.node != null) {
      try {
        connectNode();
      } catch (RosInitException ex) {
        Log.e("RosAndroid",
              "MapView: setNode() caught RosInitException: " + ex.getMessage());
        this.node = null;
      }
    } else {
      Log.i("RosAndroid", "MapView setNode() new node is null.");
    }
  }

  public Node getNode() {
    return node;
  }
  
  private void connectNode() throws RosInitException {
    Log.i("RosAndroid", "MapView connectNode().");

    mapSubscriber =
        node.createSubscriber("map", new MessageListener<OccupancyGrid>() {
          @Override
          public void onNewMessage(final OccupancyGrid msg) {
            MapView.this.post(new Runnable() {
              @Override
              public void run() {
                MapView.this.handleMap(msg);
              }
            });
          }
        }, OccupancyGrid.class);
  }

  private void disconnectNode() {
    mapSubscriber.cancel();
  }

  /**
   * Populate view with new map data. This must be called in the UI
   * thread.
   */
  private void handleMap(OccupancyGrid msg) {
    Log.i("RosAndroid", "MapView.handleMap()");
    if( bitmap != null && (bitmap.getWidth() != (int)msg.info.width || bitmap.getHeight() != (int)msg.info.height)) {
      bitmap.recycle();
      bitmap = null;
    }
    if( bitmap == null ) {
      bitmap = Bitmap.createBitmap((int)msg.info.width, (int)msg.info.height, Bitmap.Config.RGB_565);
    }

    // copy the map data into the bitmap.
    int data_i = 0;
    for (int y = 0; y < msg.info.height; y++) {
      for (int x = 0; x < msg.info.width; x++) {
        int cell = (int) msg.data[data_i];
        data_i++;
        int red = 128;
        int green = 128;
        int blue = 128;
        switch(cell) {
        case 100:
          red = 255;
          green = 255;
          blue = 255;
          break;
        case 0:
          red = 0;
          green = 0;
          blue = 0;
          break;
        }
        bitmap.setPixel(x, y, Color.rgb(blue, green, red));
      }
    }
    setImageBitmap(bitmap);
  }
}


