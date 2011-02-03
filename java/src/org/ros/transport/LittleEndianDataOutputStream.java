package org.ros.transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.google.common.base.Preconditions;

public class LittleEndianDataOutputStream extends OutputStream {

  private final DataOutputStream outputStream;

  public LittleEndianDataOutputStream(OutputStream outputStream) {
    this.outputStream = new DataOutputStream(outputStream);
  }

  @Override
  public void write(int value) throws IOException {
    outputStream.write(value);
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
}