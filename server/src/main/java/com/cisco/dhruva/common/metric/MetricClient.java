/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.common.metric;

public interface MetricClient {

  public void sendMetric(Metric metric);
}
