package ros.android;

import ros.android.utils.master.MasterChooser;

import android.app.Activity;
import android.os.Bundle;

public class RosAndroid extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        MasterChooser.getMaster();
    }
}