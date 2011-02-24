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

package org.ros.internal.transport;

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
public class ConnectionHeaderTest {
  
  @Test
  public void testEncodeAndDecodeHeader() throws IOException {
    Map<String, String> header = Maps.newHashMap();
    header.put("foo", "bar");
    byte[] encoded = ConnectionHeader.encode(header);
    Assert.assertEquals(header, ConnectionHeader.decode(encoded));
  }

  @Test
  public void testWriteAndReadHeader() throws IOException {
    Map<String, String> header = Maps.newHashMap();
    header.put("foo", "bar");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ConnectionHeader.writeHeader(header, out);
    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    Assert.assertEquals(header, ConnectionHeader.readHeader(in));
  }

}
