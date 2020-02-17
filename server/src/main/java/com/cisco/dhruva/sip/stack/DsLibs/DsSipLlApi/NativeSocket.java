package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

public class NativeSocket {
  static {
    try {
      System.loadLibrary("cloudproxynativesocket");
    } catch (Exception ex) {
      throw ex;
    }
  }

  /*
   * To get the unsent message bytes in the give socket fd.
   */
  public native int getSocketWriteQueueCount(int fd);

  /*
   * sets the SO_SNDTIMEO of the give socket fd.
   */
  public native int setSendTimeout(int fd, int timeout);
}
