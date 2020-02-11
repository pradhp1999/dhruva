/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport;

import java.util.Arrays;
import java.util.Optional;

public enum Transport {
  NONE(0),
  UDP(1),
  TCP(2),
  MULTICAST(3),
  TLS(4),
  SCTP(5);

  private int value;

  Transport(int transport) {
    this.value = transport;
  }

  public static Optional<Transport> valueOf(int value) {
    return Arrays.stream(values())
        .filter(tarnsport -> tarnsport.value == value)
        .findFirst();
  }
}
