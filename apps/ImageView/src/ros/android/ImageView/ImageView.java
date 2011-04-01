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

package ros.android.ImageView;

import android.os.Bundle;
import android.util.Log;
import org.ros.Node;
import org.ros.Publisher;
import org.ros.exceptions.RosInitException;
import ros.android.activity.RosActivity;
import ros.android.views.ImageSub;

/**
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 */
public class ImageView extends RosActivity {

  private Publisher<org.ros.message.std_msgs.String> pub;
  private Thread pubThread;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    try {
      Node node = getNode();
      
      ImageSub imageSub = (ImageSub) findViewById(R.id.image);
      imageSub.init(node, "/camera/rgb/image_color/compressed");

    } catch (RosInitException e) {
      Log.e("ImageView",e.getMessage());
    }

  }


}