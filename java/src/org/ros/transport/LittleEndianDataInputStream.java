package org.ros.transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

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
