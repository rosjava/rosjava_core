package org.ros;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Alias class for some global convenience functions ?
 * 
 * @author erublee
 * 
 */
public class Ros {

    /**
     * Get the master uri, maybe from environment or else where?
     * 
     * @return
     */
    public static URL getMasterUri() {
      try {
        return new URL("http://localhost:11311/");
      } catch (MalformedURLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        return null;
      }
    }

    /**
     * Finds the environment's host name, will look in TODO ROS_HOSTNAME
     * 
     * @return the undecorated hostname, e.g. 'localhost'
     */
    public static String getHostName() {
      // TODO better resolution? from env
      return "localhost";
    }

    protected static void logi(String string) {
      // TODO ros logging
      System.out.println(string);
    }

    public static boolean isShutdown() {
      // TODO Auto-generated method stub
      return false;
    }

}
