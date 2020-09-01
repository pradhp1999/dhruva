package com.cisco.dhruva.common.dns;

import static java.util.Objects.requireNonNull;

import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

// DNS lookup behavior follows csb ParallelDnsResolver where lookups are always performed against
// DNS
// first, since the DNS layer is "source of truth" for records and already has caching in place that
// honors advertised TTL from servers.
//
// IF DNS lookup fails, we will then attempt to read from our local cache for last known good
// result.
//
public class DnsLookupImpl implements DnsLookup {

  private static final Logger log = DhruvaLoggerFactory.getLogger(DnsLookupImpl.class);

  private final SrvRecordCache srvCache;
  private final ARecordCache aCache;
  private final LookupFactory lookupFactory;

  public DnsLookupImpl(SrvRecordCache srvCache, ARecordCache aCache, LookupFactory lookupFactory) {
    this.srvCache = requireNonNull(srvCache, "srvCache");
    this.aCache = requireNonNull(aCache, "aCache");
    this.lookupFactory = requireNonNull(lookupFactory, "lookupFactory");
  }

  @Override
  public CompletableFuture<List<DNSSRVRecord>> lookupSRV(String srvString) {
    DnsLookupResult dnsLookupResult = doLookup(lookupFactory.createLookup(srvString, Type.SRV));
    CompletableFuture<List<DNSSRVRecord>> srvRecords = new CompletableFuture<>();
    srvRecords.complete(srvCache.lookup(srvString, dnsLookupResult));
    return srvRecords;
  }

  @Override
  public CompletableFuture<List<DNSARecord>> lookupA(String host) {
    DnsLookupResult dnsLookupResult = doLookup(lookupFactory.createLookup(host, Type.A));
    CompletableFuture<List<DNSARecord>> aRecords = new CompletableFuture<>();
    aRecords.complete(aCache.lookup(host, dnsLookupResult));
    return aRecords;
  }

  private static DnsLookupResult doLookup(Lookup lookup) {
    if (lookup != null) {
      Record[] records = lookup.run();
      return new DnsLookupResult(records, lookup.getResult(), lookup.getErrorString());
    }

    return new DnsLookupResult(null, null, null);
  }
}
