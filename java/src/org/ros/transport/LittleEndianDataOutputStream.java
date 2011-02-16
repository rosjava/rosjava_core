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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.google.common.base.Preconditions;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class LittleEndianDataOutputStream extends OutputStream {

  private final DataOutputStream outputStream;

  public LittleEndianDataOutputStream(OutputStream outputStream) {
    this.outputStream = new DataOutputStream(outputStream);
  }

  @Override
  public void write(int value) throws IOException {
    outputStream.write(value);
  }
  
  public void writeField(byte[] field) throws IOException {
    writeInt(field.length);
    outputStream.write(field);
  }
  
  public void writeInt(int value) throws IOException {
    Preconditions.checkArgument(value > 0);
    byte[] buffer = new byte[4];
    buffer[0] = (byte) value;
    buffer[1] = (byte) (value >> 8);
    buffer[2] = (byte) (value >> 16);
    buffer[3] = (byte) (value >> 24);
    outputStream.write(buffer);
  }

  public void writeBytes(String value) throws IOException {
    outputStream.writeBytes(value);
  }
  
  /* (non-Javadoc)
   * @see java.io.OutputStream#close()
   */
  @Override
  public void close() throws IOException {
    super.close();
    outputStream.close();
  }
  
  /* (non-Javadoc)
   * @see java.io.OutputStream#flush()
   */
  @Override
  public void flush() throws IOException {
    super.flush();
    outputStream.flush();
  }
  
}