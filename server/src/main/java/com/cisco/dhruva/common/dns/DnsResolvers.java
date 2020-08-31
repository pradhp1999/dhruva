package com.cisco.dhruva.common.dns;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.cisco.dhruva.common.dns.metrics.DnsReporter;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.List;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;

/** Provides builders for configuring and instantiating DNS resolver */
public final class DnsResolvers {

  private static final int DEFAULT_DNS_TIMEOUT_SECONDS = 120;
  private static final int DEFAULT_RETENTION_DURATION_HOURS = 2;
  private static final int DEFAULT_CACHE_SIZE = 1000;

  public static DnsResolverBuilder newBuilder() {
    return new DnsResolverBuilder();
  }

  public static final class DnsResolverBuilder {

    private final DnsReporter reporter;
    private final boolean cacheLookups;
    private final long dnsLookupTimeoutMillis;
    private final long retentionDurationMillis;
    private final List<String> servers;

    private DnsResolverBuilder() {
      this(
          null,
          false,
          SECONDS.toMillis(DEFAULT_DNS_TIMEOUT_SECONDS),
          HOURS.toMillis(DEFAULT_RETENTION_DURATION_HOURS),
          null);
    }

    private DnsResolverBuilder(
        DnsReporter reporter,
        boolean cacheLookups,
        long dnsLookupTimeoutMillis,
        long retentionDurationMillis,
        List<String> servers) {
      this.reporter = reporter;
      this.cacheLookups = cacheLookups;
      this.dnsLookupTimeoutMillis = dnsLookupTimeoutMillis;
      this.retentionDurationMillis = retentionDurationMillis;
      this.servers = servers; // DNS servers
    }

    public DnsLookup build() {
      Resolver resolver;
      try {
        resolver = new SimpleResolver();

        // Configure the Resolver to use our timeouts.
        final Duration timeoutDuration = Duration.ofMillis(60000);
        // resolver.setTimeout(timeoutDuration);
        //              Lookup.getDefaultResolver().setTimeout(timeoutDuration);
      } catch (UnknownHostException e) {
        throw new RuntimeException(e);
      }
      // final Duration timeoutDuration = Duration.ofMillis(60000);
      // resolver.setTimeout(timeoutDuration);
      // Lookup.getDefaultResolver().setTimeout(timeoutDuration);

      LookupFactory lookupFactory = new SimpleLookupFactory(resolver);

      SrvRecordCache srvRecordCache = new SrvRecordCache(DEFAULT_CACHE_SIZE);
      ARecordCache aRecordCache = new ARecordCache(DEFAULT_CACHE_SIZE);

      DnsLookup result = new DnsLookupImpl(srvRecordCache, aRecordCache, lookupFactory);

      if (reporter != null) {
        result = new MeteredDnsSrvResolver(result, reporter);
      }

      return result;
    }

    // This is used for stats and metrics
    public DnsResolverBuilder metered(DnsReporter reporter) {
      return new DnsResolverBuilder(
          reporter, cacheLookups, dnsLookupTimeoutMillis, retentionDurationMillis, servers);
    }

    public DnsResolverBuilder dnsLookupTimeoutMillis(long dnsLookupTimeoutMillis) {
      return new DnsResolverBuilder(
          reporter, cacheLookups, dnsLookupTimeoutMillis, retentionDurationMillis, servers);
    }

    public DnsResolverBuilder retentionDurationMillis(long retentionDurationMillis) {
      return new DnsResolverBuilder(
          reporter, cacheLookups, dnsLookupTimeoutMillis, retentionDurationMillis, servers);
    }

    public DnsResolverBuilder servers(List<String> servers) {
      return new DnsResolverBuilder(
          reporter, cacheLookups, dnsLookupTimeoutMillis, retentionDurationMillis, servers);
    }
  }

  // Builder
  private DnsResolvers() {}
}
