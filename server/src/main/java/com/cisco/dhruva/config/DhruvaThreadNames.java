/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.config;

public class DhruvaThreadNames {

  private static final String UDP_EVENTLOOP_THREAD_NAME = " UDP_EventLoop_Netty_Thread";

  public static String getUdpEventloopThreadName() {
    return UDP_EVENTLOOP_THREAD_NAME;
  }
}
