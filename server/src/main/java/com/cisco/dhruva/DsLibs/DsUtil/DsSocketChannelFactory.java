package com.cisco.dhruva.DsLibs.DsUtil;

import java.io.IOException;
import java.nio.channels.*;

public class DsSocketChannelFactory {

  private static SocketChannel socketChannel;

  public static SocketChannel getSocketChannel() throws IOException {
    if (socketChannel != null) {
      return socketChannel;
    } else {
      return SocketChannel.open();
    }
  }

  public static void setSocketChannel(SocketChannel socketChannel) {
    DsSocketChannelFactory.socketChannel = socketChannel;
  }

  public static void resetSocket() {
    socketChannel = null;
  }
}
