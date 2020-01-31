/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.config.network;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class NetworkConfig {

  private static final String UDP_EVENTLOOP_THREAD_COUNT = "dhruva.network.udpEventloopThreadCount";
  private static final Integer DEFAULT_UDP_EVENTLOOP_THREAD_COUNT = 1;

  @Autowired
  public NetworkConfig(Environment env) {
    this.env = env;
  }

  private final Environment env;

  public int UDPEventPoolThreadCount() {
    return env.getProperty(
        UDP_EVENTLOOP_THREAD_COUNT, Integer.class, DEFAULT_UDP_EVENTLOOP_THREAD_COUNT);
  }
}
