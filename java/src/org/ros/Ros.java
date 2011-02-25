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
   * @return The url of the master, with port(typically 11311).
   * @throws MalformedURLException
   */
  public static URL getMasterUri() throws MalformedURLException {

    return new URL("http://localhost:11311/");
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

}
