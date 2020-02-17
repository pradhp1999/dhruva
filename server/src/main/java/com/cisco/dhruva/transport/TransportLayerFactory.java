/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport;

public class TransportLayerFactory {

  private static TransportLayerFactory transportLayerFactory = new TransportLayerFactory();
  private static DhruvaTransportLayer dhruvaTransportLayer;
  private Object lock = new Object();

  public static TransportLayerFactory getInstance() {
    return transportLayerFactory;
  }

  public TransportLayer getTransportLayer(MessageForwarder messageForwarder) {
    if (dhruvaTransportLayer == null) {
      synchronized (lock) {
        if (dhruvaTransportLayer == null) {
          dhruvaTransportLayer = new DhruvaTransportLayer(messageForwarder);
        }
      }
    }
    return dhruvaTransportLayer;
  }
}
