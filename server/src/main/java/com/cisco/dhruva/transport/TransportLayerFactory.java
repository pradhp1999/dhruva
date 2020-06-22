/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport;

import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.service.MetricService;

public class TransportLayerFactory {

  private static TransportLayerFactory transportLayerFactory = new TransportLayerFactory();
  private static DhruvaTransportLayer dhruvaTransportLayer;
  private Object lock = new Object();

  public static TransportLayerFactory getInstance() {
    return transportLayerFactory;
  }

  public TransportLayer getTransportLayer(
      MessageForwarder messageForwarder,
      ExecutorService executorService,
      MetricService metricService) {
    if (dhruvaTransportLayer == null) {
      synchronized (lock) {
        if (dhruvaTransportLayer == null) {
          dhruvaTransportLayer =
              new DhruvaTransportLayer(messageForwarder, executorService, metricService);
        }
      }
    }
    return dhruvaTransportLayer;
  }
}
