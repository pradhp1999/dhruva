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

  private static final int DEFAULT_DNS_TIMEOUT_SECONDS = 5;
  private static final int DEFAULT_RETENTION_DURATION_HOURS = 24;
  private static final int DEFAULT_CACHE_SIZE = 1_000;

  public static DnsResolverBuilder newBuilder() {
    return new DnsResolverBuilder();
  }

  public static final class DnsResolverBuilder {

    private final DnsReporter reporter;
    private final Resolver resolver;
    private final long dnsLookupTimeoutMillis;
    private final long retentionDurationMillis;
    private final long cacheSize;
    private final List<String> servers;

    private DnsResolverBuilder() {
      this(
          null,
          null,
          DEFAULT_CACHE_SIZE,
          SECONDS.toMillis(DEFAULT_DNS_TIMEOUT_SECONDS),
          HOURS.toMillis(DEFAULT_RETENTION_DURATION_HOURS),
          null);
    }

    private DnsResolverBuilder(
        DnsReporter reporter,
        Resolver resolver,
        long cacheSize,
        long dnsLookupTimeoutMillis,
        long retentionDurationMillis,
        List<String> servers) {
      this.reporter = reporter;
      this.resolver = resolver;
      this.dnsLookupTimeoutMillis = dnsLookupTimeoutMillis;
      this.retentionDurationMillis = retentionDurationMillis;
      this.servers = servers; // DNS servers
      this.cacheSize = cacheSize;
    }

    public DnsLookup build() {
      LookupFactory lookupFactory;
      if (resolver == null) {
        Resolver resolver;
        try {
          resolver = new SimpleResolver();
          final Duration timeoutDuration = Duration.ofMillis(dnsLookupTimeoutMillis);
          resolver.setTimeout(timeoutDuration);

          lookupFactory = new SimpleLookupFactory(resolver);
        } catch (UnknownHostException e) {
          throw new RuntimeException(e);
        }
      } else {
        lookupFactory = new SimpleLookupFactory(this.resolver);
      }

      SrvRecordCache srvRecordCache =
          new SrvRecordCache(DEFAULT_CACHE_SIZE, retentionDurationMillis);
      ARecordCache aRecordCache = new ARecordCache(DEFAULT_CACHE_SIZE, retentionDurationMillis);

      DnsLookup result = new DnsLookupImpl(srvRecordCache, aRecordCache, lookupFactory);

      if (reporter != null) {
        result = new MeteredDnsSrvResolver(result, reporter);
      }

      return result;
    }

    // This is used for stats and metrics
    public DnsResolverBuilder metered(DnsReporter reporter) {
      return new DnsResolverBuilder(
          reporter, resolver, cacheSize, dnsLookupTimeoutMillis, retentionDurationMillis, servers);
    }

    public DnsResolverBuilder dnsLookupTimeoutMillis(long dnsLookupTimeoutMillis) {
      return new DnsResolverBuilder(
          reporter, resolver, cacheSize, dnsLookupTimeoutMillis, retentionDurationMillis, servers);
    }

    public DnsResolverBuilder retentionDurationMillis(long retentionDurationMillis) {
      return new DnsResolverBuilder(
          reporter, resolver, cacheSize, dnsLookupTimeoutMillis, retentionDurationMillis, servers);
    }

    public DnsResolverBuilder servers(List<String> servers) {
      return new DnsResolverBuilder(
          reporter, resolver, cacheSize, dnsLookupTimeoutMillis, retentionDurationMillis, servers);
    }
  }

  // Builder
  private DnsResolvers() {}
}
