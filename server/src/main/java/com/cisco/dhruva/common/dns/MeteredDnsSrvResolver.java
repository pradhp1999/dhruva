package com.cisco.dhruva.common.dns;

import static java.util.Objects.requireNonNull;

import com.cisco.dhruva.common.dns.metrics.DnsReporter;
import com.cisco.dhruva.common.dns.metrics.DnsTimingContext;
import java.util.List;

/** Tracks metrics for DnsSrvResolver calls. */
class MeteredDnsSrvResolver implements DnsLookup {
  private final DnsLookup delegate;
  private final DnsReporter reporter;

  MeteredDnsSrvResolver(DnsLookup delegate, DnsReporter reporter) {
    this.delegate = requireNonNull(delegate, "delegate");
    this.reporter = requireNonNull(reporter, "reporter");
  }

  @Override
  public List<DNSSRVRecord> lookupSRV(String fqdn) throws DnsException {

    final DnsTimingContext resolveTimer = reporter.resolveTimer();

    final List<DNSSRVRecord> result;

    try {
      result = delegate.lookupSRV(fqdn);
    } catch (DnsException error) {
      reporter.reportFailure(error);
      throw error;
    } finally {
      resolveTimer.stop();
    }

    if (result.isEmpty()) {
      reporter.reportEmpty();
    }

    return result;
  }

  @Override
  public List<DNSARecord> lookupA(String host) {
    final DnsTimingContext resolveTimer = reporter.resolveTimer();

    final List<DNSARecord> result;

    try {
      result = delegate.lookupA(host);
    } catch (DnsException error) {
      reporter.reportFailure(error);
      throw error;
    } finally {
      resolveTimer.stop();
    }

    if (result.isEmpty()) {
      reporter.reportEmpty();
    }

    return result;
  }
}
