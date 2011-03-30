package ros.android.utils.master;

import android.os.Environment;

import java.io.File;

public class MasterChooser {
  
  public static String getMaster()
  {
    File sdcard = Environment.getExternalStorageDirectory();
    return "localhost";
  }
}
