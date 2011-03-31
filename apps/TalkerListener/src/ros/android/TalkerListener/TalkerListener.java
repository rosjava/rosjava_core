package ros.android.TalkerListener;

import android.util.Log;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import org.ros.MessageListener;
import org.ros.Node;
import org.ros.NodeContext;
import ros.android.BarcodeLoader;

import java.util.concurrent.LinkedBlockingDeque;

public class TalkerListener extends Activity {

  private void setText(String text) {
    TextView t = (TextView) findViewById(R.id.text_view);
    t.setText(text);
  }

  Node node;
//  
//  @Override
//  public Object onRetainNonConfigurationInstance() {
//      final LoadedPhoto[] list = new LoadedPhoto[numberOfPhotos];
//      keepPhotos(list);
//      return list;
//  }

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    if (node == null) {
      setText("loading");
      Log.i("RosAndroid","loading.... should only happen once");

      LinkedBlockingDeque<Integer> g = new LinkedBlockingDeque<Integer>();
      g.add(1);

      BarcodeLoader loader = new BarcodeLoader();
      NodeContext context;
      try {
        context = loader.createContext();
        Node node = new Node("listener", context);
        // final Log log = node.getLog();
        node.createSubscriber("chatter", new MessageListener<org.ros.message.std_msgs.String>() {
          @Override
          public void onNewMessage(final org.ros.message.std_msgs.String message) {
            Log.i("RosAndroid", "I heard: \"" + message.data + "\"");
            runOnUiThread(new Runnable() {

              @Override
              public void run() {
                setText(message.data);

              }
            });

          }
        }, org.ros.message.std_msgs.String.class);
      } catch (Exception e) {
        setText("failed to create node" + e.getMessage());
      }
    }
  }
}