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

import android.content.Intent;

import android.widget.Toast;

import android.content.DialogInterface;

import android.app.AlertDialog;

import android.hardware.Camera.PreviewCallback;

import android.graphics.ImageFormat;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * 
 */
public class RosCamera extends Activity implements PreviewCallback {
  private Preview mPreview;

  Camera mCamera;
  int cameraCurrentlyLocked;

  // The first rear facing camera
  int defaultCameraId;

  RosCameraNode cameraNode;

  String masterURI;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Hide the window title.
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

    // Create a RelativeLayout container that will hold a SurfaceView,
    // and set it as the content of our activity.
    mPreview = new Preview(this);
    setContentView(mPreview);
    // host loop back 10.0.2.2
    // device loop back 127.0.0.1

  }

  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    if (requestCode == 0) {
      if (resultCode == RESULT_OK) {
        String contents = intent.getStringExtra("SCAN_RESULT");
        String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
        masterURI = contents;
        setupNode();
        // Handle successful scan
      } else if (resultCode == RESULT_CANCELED) {
        Toast.makeText(getApplicationContext(), "No ROS Master URI found, Please try again.",
            Toast.LENGTH_LONG).show();
        setupNode();
      }
    }
  }

  private void launchUriSelector() {
    final CharSequence[] uriItems = { "From Barcode", "http://localhost:11311",
        "http://10.0.2.2:11311", };
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Pick a ROS Master URI");
    builder.setItems(uriItems, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int item) {
        if (item == 0) {
          Intent intent = new Intent("com.google.zxing.client.android.SCAN");
          intent.setPackage("com.google.zxing.client.android");
          intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
          startActivityForResult(intent, 0);
          Toast.makeText(getApplicationContext(), "Please Scan a QR Code with a ROS host URI.",
              Toast.LENGTH_LONG).show();
          masterURI = null;
        } else {          
          masterURI = (String) uriItems[item];
          setupNode();
        }

      }
    });
    AlertDialog alert = builder.create();
    alert.show();
  }

  @Override
  protected void onResume() {
    super.onResume();

    // Open the default i.e. the first rear facing camera.
    mCamera = Camera.open();
    cameraCurrentlyLocked = defaultCameraId;
    mPreview.setCamera(mCamera);

    setupPreview();
    if (masterURI == null) {
      launchUriSelector();
    }

  }

  @Override
  protected void onPause() {
    super.onPause();

    // Because the Camera object is a shared resource, it's very
    // important to release it when the activity is paused.
    if (mCamera != null) {
      mPreview.setCamera(null);
      mCamera.release();
      mCamera = null;
    }
    cameraNode = null;
  }

  void setupNode() {
    if (masterURI != null) {
      Toast.makeText(getApplicationContext(), "Using ROS Master on : " + masterURI,
          Toast.LENGTH_LONG).show();
      cameraNode = new RosCameraNode(masterURI, "android_camera");
    } else {
      launchUriSelector();
    }
  }

  private ArrayList<byte[]> previewBuffers;

  private void setupPreview() {
    if (previewBuffers == null) {
      previewBuffers = new ArrayList<byte[]>();
      Size sz = mCamera.getParameters().getPreviewSize();
      int format = mCamera.getParameters().getPreviewFormat();
      int bits_per_pixel = ImageFormat.getBitsPerPixel(format);
      int bytes_per_pixel = bits_per_pixel / 8;

      previewBuffers.add(new byte[bytes_per_pixel * sz.height * sz.width]);
    }
    for (byte[] x : previewBuffers) {
      mCamera.addCallbackBuffer(x);
    }
    mCamera.setPreviewCallbackWithBuffer(this);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    // Inflate our menu which can gather user input for switching camera
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.cameramenu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
    case R.id.switch_cam:

    default:
      return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onPreviewFrame(byte[] data, Camera camera) {
    if (cameraNode != null)
      cameraNode.onPreviewFrame(data, camera);
    camera.addCallbackBuffer(data);
  }

}

// ----------------------------------------------------------------------

/**
 * A simple wrapper around a Camera and a SurfaceView that renders a centered
 * preview of the Camera to the surface. We need to center the SurfaceView
 * because not all devices have cameras that support preview sizes at the same
 * aspect ratio as the device's display.
 */
class Preview extends ViewGroup implements SurfaceHolder.Callback {
  private final String TAG = "Preview";

  SurfaceView mSurfaceView;
  SurfaceHolder mHolder;
  Size mPreviewSize;
  List<Size> mSupportedPreviewSizes;
  Camera mCamera;

  Preview(Context context) {
    super(context);

    mSurfaceView = new SurfaceView(context);
    addView(mSurfaceView);

    // Install a SurfaceHolder.Callback so we get notified when the
    // underlying surface is created and destroyed.
    mHolder = mSurfaceView.getHolder();
    mHolder.addCallback(this);
    mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
  }

  public void setCamera(Camera camera) {
    mCamera = camera;
    if (mCamera != null) {
      mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
      requestLayout();
    }
  }

  public void switchCamera(Camera camera) {
    setCamera(camera);
    try {
      camera.setPreviewDisplay(mHolder);
    } catch (IOException exception) {
      Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
    }
    Camera.Parameters parameters = camera.getParameters();
    parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
    requestLayout();

    camera.setParameters(parameters);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // We purposely disregard child measurements because act as a
    // wrapper to a SurfaceView that centers the camera preview instead
    // of stretching it.
    final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
    final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
    setMeasuredDimension(width, height);

    if (mSupportedPreviewSizes != null) {
      mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
    }
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    if (changed && getChildCount() > 0) {
      final View child = getChildAt(0);

      final int width = r - l;
      final int height = b - t;

      int previewWidth = width;
      int previewHeight = height;
      if (mPreviewSize != null) {
        previewWidth = mPreviewSize.width;
        previewHeight = mPreviewSize.height;
      }

      // Center the child SurfaceView within the parent.
      if (width * previewHeight > height * previewWidth) {
        final int scaledChildWidth = previewWidth * height / previewHeight;
        child.layout((width - scaledChildWidth) / 2, 0, (width + scaledChildWidth) / 2, height);
      } else {
        final int scaledChildHeight = previewHeight * width / previewWidth;
        child.layout(0, (height - scaledChildHeight) / 2, width, (height + scaledChildHeight) / 2);
      }
    }
  }

  public void surfaceCreated(SurfaceHolder holder) {
    // The Surface has been created, acquire the camera and tell it where
    // to draw.
    try {
      if (mCamera != null) {
        mCamera.setPreviewDisplay(holder);
      }
    } catch (IOException exception) {
      Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
    }
  }

  public void surfaceDestroyed(SurfaceHolder holder) {
    // Surface will be destroyed when we return, so stop the preview.
    if (mCamera != null) {
      mCamera.stopPreview();
    }
  }

  private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
    final double ASPECT_TOLERANCE = 0.1;
    double targetRatio = (double) w / h;
    if (sizes == null)
      return null;

    Size optimalSize = null;
    double minDiff = Double.MAX_VALUE;

    int targetHeight = h;

    // Try to find an size match aspect ratio and size
    for (Size size : sizes) {
      double ratio = (double) size.width / size.height;
      if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
        continue;
      if (Math.abs(size.height - targetHeight) < minDiff) {
        optimalSize = size;
        minDiff = Math.abs(size.height - targetHeight);
      }
    }

    // Cannot find the one match the aspect ratio, ignore the requirement
    if (optimalSize == null) {
      minDiff = Double.MAX_VALUE;
      for (Size size : sizes) {
        if (Math.abs(size.height - targetHeight) < minDiff) {
          optimalSize = size;
          minDiff = Math.abs(size.height - targetHeight);
        }
      }
    }
    return optimalSize;
  }

  public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    // Now that the size is known, set up the camera parameters and begin
    // the preview.
    Camera.Parameters parameters = mCamera.getParameters();
    parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
    requestLayout();

    mCamera.setParameters(parameters);
    mCamera.startPreview();
  }
}