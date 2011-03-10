package org.ros;

import static org.junit.Assert.*;

import org.ros.namespace.RosResolver;

import org.ros.exceptions.RosNameException;

import org.junit.Test;

public class RosContextTest {

  @Test
  public void testRosContext() {
    Context ctx = new Context();
    try {
      String myname = ctx.getResolver().resolveName("myname");
      assertTrue(myname.equals("/myname"));
    } catch (RosNameException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testInit() throws RosNameException {
    Context ctx = new Context();
    String args = "name:=/my/name foo:=/my/foo --help";
    String[] stripped = ctx.init(args.split(" "));
    assertTrue(stripped.length == 1);
    assertTrue(stripped[0].equals("--help"));
    RosResolver r = ctx.getResolver();
    String n = r.resolveName("name");
    System.out.print(n);
    assertTrue(n.equals("/my/name"));
    assertTrue(r.resolveName("/name").equals("/name"));
    assertTrue(r.resolveName("foo").equals("/my/foo"));
    assertTrue(r.resolveName("/my/name").equals("/my/name"));

  }

  @Test
  public void testGetResolver() throws RosNameException {
    Context ctx = new Context();
    Context ctx2 = new Context();
    String args = "name:=/my/name foo:=/my/foo --help";
    ctx.init(args.split(" "));
    RosResolver r = ctx2.getResolver(); // context 2 should be independent from
                                        // context 1.
    assertTrue(r.resolveName("name").equals("/name"));
  }

}
