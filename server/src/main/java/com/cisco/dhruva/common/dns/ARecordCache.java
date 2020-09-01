package com.cisco.dhruva.common.dns;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Record;

public class ARecordCache extends AbstractDnsCache<DNSARecord> {

  public ARecordCache(int maxCacheSize, long retentionTimeMillis) {
    super(maxCacheSize, retentionTimeMillis);
  }

  @Override
  protected List<DNSARecord> getRecords(Record[] records) {
    return Arrays.stream(records)
        .filter(r -> filterOnType(r, ARecord.class))
        .map(r -> (ARecord) r)
        .map(
            r ->
                new DNSARecord(r.getName().toString(), r.getTTL(), r.getAddress().getHostAddress()))
        .collect(Collectors.toList());
  }
}
