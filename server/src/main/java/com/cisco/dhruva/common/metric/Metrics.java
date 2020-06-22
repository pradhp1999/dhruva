/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.common.metric;

public final class Metrics {

  public enum MetricType {
    INFLUX
  }

  public static Metric newMetric(MetricType metricType) {
    if (metricType == MetricType.INFLUX) {
      return new InfluxMetric();
    }
    return null;
  }
}
