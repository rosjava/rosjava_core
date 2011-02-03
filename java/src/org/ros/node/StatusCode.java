package org.ros.node;

public enum StatusCode {
  ERROR(-1), FAILURE(0), SUCCESS(1);

  private final int intValue;

  private StatusCode(int value) {
    this.intValue = value;
  }

  public int toInt() {
    return intValue;
  }

  public static StatusCode fromInt(int intValue) {
    switch (intValue) {
    case -1:
      return ERROR;
    case 1:
      return SUCCESS;
    case 0:
    default:
      return FAILURE;
    }
  }
}
