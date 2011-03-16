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
package org.ros;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.ros.exceptions.RosInitException;
import org.ros.exceptions.RosNameException;
import org.ros.internal.loader.CommandLine;
import org.ros.internal.loader.EnvironmentVariables;
import org.ros.internal.namespace.GraphName;
import org.ros.namespace.NameResolver;
import org.ros.namespace.Namespace;

import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test CommandLineLoader.
 * 
 * @author kwc@willowgarage.com (Ken Conley)
 */
public class CommandLineLoaderTest extends TestCase {

  private Object defaultMasterUri;
  private String defaultRosRoot;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    defaultMasterUri = new URI("http://localhost:33133");
    defaultRosRoot = System.getProperty("user.dir");
  }

  @Test
  public void testCommandLineLoader() throws RosNameException, RosInitException {
    // Test with no args.
    CommandLineLoader loader = new CommandLineLoader(new String[] {});
    assertEquals(0, loader.getCleanCommandLineArgs().length);

    // Test with no remappings.
    String[] args = new String[] { "one two --three" };
    loader = new CommandLineLoader(args);
    Assert.assertArrayEquals(args, loader.getCleanCommandLineArgs());

    // Test with actual remappings. All of these are equivalent.
    String[] tests = { "--help one name:=/my/name -i foo:=/my/foo",
        "name:=/my/name --help one -i foo:=/my/foo", "name:=/my/name --help foo:=/my/foo one -i", };
    String[] expected = { "--help", "one", "-i" };
    for (String test : tests) {
      loader = new CommandLineLoader(test.split(" "));
      // Test command-line parsing
      String[] cleanArgs = loader.getCleanCommandLineArgs();
      Assert.assertArrayEquals(expected, cleanArgs);
    }
  }

  /**
   * Test createContext() with respect to reading of the environment
   * configuration, including command-line overrides. {@link NameResolver} is
   * tested separately.
   * 
   * @throws RosInitException
   * @throws RosNameException
   * @throws URISyntaxException
   * @throws UnknownHostException
   */
  @Test
  public void testCreateContextEnvironment() throws RosInitException, RosNameException,
      URISyntaxException, UnknownHostException {
    // Construct artificial environment. Test failure without required settings.
    // Failure: ROS_ROOT not set.
    String tmpDir = System.getProperty("java.io.tmpdir");
    String rosPackagePath = tmpDir + File.pathSeparator + defaultRosRoot;
    String[] rosPackagePathArray = new String[] { tmpDir, defaultRosRoot };
    Map<String, String> env = new HashMap<String, String>();
    CommandLineLoader loader = null;
    /*
     * In the future, ROS_ROOT may become required again (e.g. class loader).
     * 
    env.put(EnvironmentVariables.ROS_MASTER_URI, defaultMasterUri.toString());
    CommandLineLoader loader = new CommandLineLoader(new String[] {}, env);
    try {
      loader.createContext();
      fail("should have raised RosInitException: no ROS_ROOT");
    } catch (RosInitException e) {
    }
    
    */
    env = new HashMap<String, String>();
    env.put(EnvironmentVariables.ROS_ROOT, defaultRosRoot);
    loader = new CommandLineLoader(new String[] {}, env);
    try {
      loader.createContext();
      fail("should have raised RosInitException: no ROS_MASTER_URI");
    } catch (RosInitException e) {
    }

    // Construct artificial environment. Set required environment variables.
    env = getDefaultEnv();
    loader = new CommandLineLoader(new String[] {}, env);
    NodeContext nodeContext = loader.createContext();
    assertEquals(defaultMasterUri, nodeContext.getRosMasterUri());
    assertEquals(defaultRosRoot, nodeContext.getRosRoot());
    assertEquals(Namespace.GLOBAL_NS, nodeContext.getResolver().getNamespace());
    // Default is the hostname + FQDN.
    assertEquals(InetAddress.getLocalHost().getCanonicalHostName(), nodeContext.getHostName());

    // Construct artificial environment. Set optional environment variables.
    env = getDefaultEnv();
    env.put(EnvironmentVariables.ROS_IP, "192.168.0.1");
    env.put(EnvironmentVariables.ROS_NAMESPACE, "/foo/bar");
    env.put(EnvironmentVariables.ROS_PACKAGE_PATH, rosPackagePath);
    loader = new CommandLineLoader(new String[] {}, env);
    nodeContext = loader.createContext();

    assertEquals(defaultMasterUri, nodeContext.getRosMasterUri());
    assertEquals(defaultRosRoot, nodeContext.getRosRoot());
    assertEquals("192.168.0.1", nodeContext.getHostName());
    assertEquals("/foo/bar", nodeContext.getResolver().getNamespace());
    Assert.assertArrayEquals(rosPackagePathArray, nodeContext.getRosPackagePath());

    // Test ROS namespace resolution and canonicalization
    String canonical = new GraphName("/baz/bar").toString();
    env = getDefaultEnv();
    env.put(EnvironmentVariables.ROS_NAMESPACE, "baz/bar");
    loader = new CommandLineLoader(new String[] {}, env);
    nodeContext = loader.createContext();
    assertEquals(canonical, nodeContext.getResolver().getNamespace());
    env = getDefaultEnv();
    env.put(EnvironmentVariables.ROS_NAMESPACE, "baz/bar/");
    loader = new CommandLineLoader(new String[] {}, env);
    nodeContext = loader.createContext();
    assertEquals(canonical, nodeContext.getResolver().getNamespace());
  }

  @Test
  public void testCreateContextCommandLine() throws RosInitException, URISyntaxException,
      RosNameException {
    Map<String, String> env = getDefaultEnv();

    // Test ROS_MASTER_URI from command-line
    String[] args = { CommandLine.ROS_MASTER_URI + ":=http://override:22622" };
    NodeContext nodeContext = new CommandLineLoader(args, env).createContext();
    assertEquals(new URI("http://override:22622"), nodeContext.getRosMasterUri());

    // Test again with env var removed, make sure that it still behaves the
    // same.
    env.remove(EnvironmentVariables.ROS_MASTER_URI);
    nodeContext = new CommandLineLoader(args, env).createContext();
    assertEquals(new URI("http://override:22622"), nodeContext.getRosMasterUri());

    // Test ROS namespace resolution and canonicalization
    String canonical = new GraphName("/baz/bar").toString();
    env = getDefaultEnv();
    args = new String[] { CommandLine.ROS_NAMESPACE + ":=baz/bar" };
    nodeContext = new CommandLineLoader(args, env).createContext();
    assertEquals(canonical, nodeContext.getResolver().getNamespace());

    args = new String[] { CommandLine.ROS_NAMESPACE + ":=baz/bar/" };
    nodeContext = new CommandLineLoader(args, env).createContext();
    assertEquals(canonical, nodeContext.getResolver().getNamespace());

    // Verify precedence of command-line __ns over environment.
    env.put(EnvironmentVariables.ROS_NAMESPACE, "wrong/answer/");
    nodeContext = new CommandLineLoader(args, env).createContext();
    assertEquals(canonical, nodeContext.getResolver().getNamespace());

    // Verify address override.
    env = getDefaultEnv();
    args = new String[] { CommandLine.ROS_IP + ":=192.168.0.2" };
    nodeContext = new CommandLineLoader(args, env).createContext();
    assertEquals("192.168.0.2", nodeContext.getHostName());

    // Verify multiple options work together.
    env = getDefaultEnv();
    args = new String[] { CommandLine.ROS_NAMESPACE + ":=baz/bar/", "ignore",
        CommandLine.ROS_MASTER_URI + ":=http://override:22622", "--bad",
        CommandLine.ROS_IP + ":=192.168.0.2" };
    nodeContext = new CommandLineLoader(args, env).createContext();
    assertEquals(new URI("http://override:22622"), nodeContext.getRosMasterUri());
    assertEquals("192.168.0.2", nodeContext.getHostName());
    assertEquals(canonical, nodeContext.getResolver().getNamespace());
  }

  private HashMap<String, String> getDefaultEnv() {
    HashMap<String, String> env = new HashMap<String, String>();
    env.put(EnvironmentVariables.ROS_MASTER_URI, defaultMasterUri.toString());
    env.put(EnvironmentVariables.ROS_ROOT, defaultRosRoot);
    return env;
  }

  /**
   * Test the {@link NameResolver} created by createContext().
   * 
   * @throws RosInitException
   * @throws RosNameException
   * @throws URISyntaxException
   */
  @Test
  public void testCreateContextResolver() throws RosInitException, RosNameException,
      URISyntaxException {
    // Construct artificial environment. Set required environment variables.
    HashMap<String, String> env = getDefaultEnv();

    // Test with no args.
    CommandLineLoader loader = new CommandLineLoader(new String[] {}, env);
    NodeContext nodeContext = loader.createContext();
    nodeContext.getResolver().getRemappings();
    nodeContext = loader.createContext();

    // Test with no remappings.
    String[] commandLineArgs = new String[] { "foo", "--bar" };
    loader = new CommandLineLoader(commandLineArgs, env);
    nodeContext = loader.createContext();
    NameResolver resolver = nodeContext.getResolver();
    assertTrue(resolver.getRemappings().isEmpty());

    // test with actual remappings. All of these tests are equivalent.
    String[] tests = { "--help name:=/my/name -i foo:=/my/foo",
        "name:=/my/name --help -i foo:=/my/foo", "name:=/my/name foo:=/my/foo --help -i", };
    for (String test : tests) {
      commandLineArgs = test.split(" ");
      loader = new CommandLineLoader(commandLineArgs, env);
      nodeContext = loader.createContext();

      // Test that our remappings loaded.
      NameResolver r = nodeContext.getResolver();
      String n = r.resolveName("name");
      assertTrue(n.equals("/my/name"));
      assertTrue(r.resolveName("/name").equals("/name"));
      assertTrue(r.resolveName("foo").equals("/my/foo"));
      assertTrue(r.resolveName("/my/name").equals("/my/name"));
    }
  }
}
