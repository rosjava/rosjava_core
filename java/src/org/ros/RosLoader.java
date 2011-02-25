package org.ros;

/**
 * @author "Ethan Rublee ethan.rublee@gmail.com" Prototype class loader for
 *         finding RosMain's from manifests,launch files, etc.. This might be an
 *         internal thing.. just playing with it for now.
 */
public class RosLoader {
  /**
   * @param name
   *          The name of the class
   * @return an instance of a RosMain, may be run.
   * @throws ClassNotFoundException
   * @throws InstantiationException
   * @throws IllegalAccessException
   */
  public RosMain loadClass(String name) throws ClassNotFoundException, InstantiationException,
      IllegalAccessException {
    Class<?> clazz = getClass().getClassLoader().loadClass(name);

    return RosMain.class.cast(clazz.newInstance());
  }
}