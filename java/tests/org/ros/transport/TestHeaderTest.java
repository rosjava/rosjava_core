// Copyright 2011 Google Inc. All Rights Reserved.

package org.ros.transport;

import com.google.common.collect.Maps;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class TestHeaderTest {
  
  @Test
  public void testEncodeAndDecodeHeader() throws IOException {
    Map<String, String> header = Maps.newHashMap();
    header.put("foo", "bar");
    byte[] encoded = Header.encode(header);
    Assert.assertEquals(header, Header.decode(encoded));
  }

  @Test
  public void testWriteAndReadHeader() throws IOException {
    Map<String, String> header = Maps.newHashMap();
    header.put("foo", "bar");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Header.writeHeader(header, out);
    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    Assert.assertEquals(header, Header.readHeader(in));
  }

}
