/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.common.metric;

import com.cisco.wx2.metrics.InfluxPoint;
import com.cisco.wx2.server.InfluxDBClientHelper;
import java.time.Instant;
import javax.inject.Inject;

public class InfluxClient implements MetricClient {

  @Inject private InfluxDBClientHelper influxDBClientHelper;

  @Override
  public void sendMetric(Metric metric) {
    metric.timestamp(Instant.now());
    influxDBClientHelper.writePointAsync((InfluxPoint) metric.get());
  }
}
