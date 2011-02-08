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

package org.ros.transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class LittleEndianDataInputStream extends InputStream {
  
  private final DataInputStream inputStream;
  
  public LittleEndianDataInputStream(InputStream inputStream) {
    this.inputStream = new DataInputStream(inputStream);
  }

  @Override
  public int read() throws IOException {
    return inputStream.read();
  }
  
  public String readAsciiString(int size) throws IOException {
    byte[] field = readByteArray(size);
    return new String(field, "US-ASCII");
  }

  public byte[] readByteArray(int size) throws IOException {
    byte[] buffer = new byte[size];
    int bytesRead = 0;
    while (bytesRead < size) {
      bytesRead += inputStream.read(buffer, bytesRead, size - bytesRead);
    }
    return buffer;
  }

  public int readInt() throws IOException {
    byte[] littleEndian = readByteArray(4);
    int unsignedResult = (littleEndian[3]) << 24 | (littleEndian[2] & 0xff) << 16
        | (littleEndian[1] & 0xff) << 8 | (littleEndian[0] & 0xff);
    if (unsignedResult < 0) {
      throw new IllegalStateException();
    }
    return unsignedResult;
  }
}
