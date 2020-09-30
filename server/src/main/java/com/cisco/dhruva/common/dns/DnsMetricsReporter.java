package com.cisco.dhruva.common.dns;

import com.cisco.dhruva.common.dns.metrics.DnsReporter;
import com.cisco.dhruva.common.dns.metrics.DnsTimingContext;
import com.cisco.dhruva.service.MetricService;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DnsMetricsReporter implements DnsReporter {

  @Autowired public MetricService metricsService;

  private static final Logger log = DhruvaLoggerFactory.getLogger(DnsLookupImpl.class);

  @Override
  public DnsTimingContext resolveTimer() {
    return new DnsTimingContext() {
      private final long start = System.currentTimeMillis();

      @Override
      public void stop(String query, String queryType) {
        final long now = System.currentTimeMillis();
        final long diff = now - start;
        metricsService.sendDNSMetric(query, queryType, diff);
      }
    };
  }

  @Override
  public void reportFailure(String query, String queryType, Throwable error) {
    log.error(
        "error while resolving query {} type {} exception cause {}",
        query,
        queryType,
        error.getCause());
  }

  @Override
  public void reportEmpty(String query, String queryType) {
    log.error("got empty records while resolving query {} type {} ", query, queryType);
  }
}
