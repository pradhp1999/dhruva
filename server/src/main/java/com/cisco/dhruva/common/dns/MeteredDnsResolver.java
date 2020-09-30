package com.cisco.dhruva.common.dns;

import static java.util.Objects.requireNonNull;

import com.cisco.dhruva.common.dns.dto.DNSARecord;
import com.cisco.dhruva.common.dns.dto.DNSSRVRecord;
import com.cisco.dhruva.common.dns.metrics.DnsReporter;
import com.cisco.dhruva.common.dns.metrics.DnsTimingContext;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.xbill.DNS.Type;

/** Tracks metrics for DnsSrvResolver calls. */
class MeteredDnsResolver implements DnsLookup {
  private final DnsLookup delegate;
  private final DnsReporter reporter;

  MeteredDnsResolver(DnsLookup delegate, DnsReporter reporter) {
    this.delegate = requireNonNull(delegate, "delegate");
    this.reporter = requireNonNull(reporter, "reporter");
  }

  @Override
  public CompletableFuture<List<DNSSRVRecord>> lookupSRV(String fqdn) {

    final DnsTimingContext resolveTimer = reporter.resolveTimer();

    final List<DNSSRVRecord> result;
    CompletableFuture<List<DNSSRVRecord>> f1 = new CompletableFuture<>();

    try {
      CompletableFuture<List<DNSSRVRecord>> f2 = delegate.lookupSRV(fqdn);
      result = f2.get();
      f1.complete(result);
      if (result.isEmpty()) {
        reporter.reportEmpty(fqdn, "SRV");
      }
    } catch (DnsException error) {
      reporter.reportFailure(fqdn, "SRV", error);
      throw error;
    } catch (InterruptedException | ExecutionException error) {
      reporter.reportFailure(fqdn, "SRV", error);
      throw new DnsException(Type.SRV, fqdn, DnsErrorCode.ERROR_DNS_INTERNAL_ERROR);
    } finally {
      resolveTimer.stop(fqdn, "SRV");
    }

    return f1;
  }

  @Override
  public CompletableFuture<List<DNSARecord>> lookupA(String host) {

    final DnsTimingContext resolveTimer = reporter.resolveTimer();

    final List<DNSARecord> result;
    CompletableFuture<List<DNSARecord>> f1 = new CompletableFuture<>();

    try {
      CompletableFuture<List<DNSARecord>> f2 = delegate.lookupA(host);
      result = f2.get();
      f1.complete(result);
      if (result.isEmpty()) {
        reporter.reportEmpty(host, "A");
      }
    } catch (DnsException error) {
      reporter.reportFailure(host, "A", error);
      throw error;
    } catch (InterruptedException | ExecutionException error) {
      reporter.reportFailure(host, "A", error);
      throw new DnsException(Type.A, host, DnsErrorCode.ERROR_DNS_INTERNAL_ERROR);
    } finally {
      resolveTimer.stop(host, "A");
    }

    return f1;
  }
}
