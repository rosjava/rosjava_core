package org.ros.android.app_chooser;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import android.view.View;

import org.ros.message.app_manager.App;
import org.ros.Node;
import org.ros.app_manager.AppManager;
import org.ros.app_manager.AppManagerNotAvailableException;
import org.ros.exceptions.RosInitException;

import ros.android.activity.RosActivity;
import ros.android.util.RobotDescription;

/** Show a grid of applications that a given robot is capable of, and
 * launch whichever is chosen. */
public class AppChooser extends RosActivity
{
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void onResume() {
    super.onResume();
    updateList();
  }

  private void updateList() {
    setContentView(R.layout.main);
    final ArrayList<App> apps = getAppList();
    GridView gridview = (GridView) findViewById(R.id.gridview);
    gridview.setAdapter(new AppAdapter(this, apps));

    gridview.setOnItemClickListener(new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
          AppLauncher.launch( AppChooser.this, apps.get( position ));
        }
      });
  }

  public void chooseNewMasterClicked( View view ) {
    chooseNewMaster();
  }

  public ArrayList<App> getAppList() {
    try {
      RobotDescription robot = getCurrentRobot();
      AppManager app_man = new AppManager( getNode(), robot.robot_name_ );
      return app_man.getAvailableApps();
    }
    catch( AppManagerNotAvailableException ex )
    {
      setStatus("AppManager not available");
      return null;
    }
    catch( RosInitException ex )
    {
      setStatus("Ros init exception: " + ex.getMessage());
      return null;
    }
  }

  private void setStatus( String status_message ) {
    TextView status_view = (TextView) findViewById(R.id.status_view);
    if( status_view != null )
    {
      status_view.setText( status_message );
    }
  }
}
