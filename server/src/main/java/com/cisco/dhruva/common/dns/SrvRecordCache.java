package com.cisco.dhruva.common.dns;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;

public class SrvRecordCache extends AbstractDnsCache<DNSSRVRecord> {

  public SrvRecordCache(long maxCacheSize) {
    super(maxCacheSize);
  }

  @Override
  protected List<DNSSRVRecord> getRecords(Record[] records) {
    return Arrays.stream(records)
        .filter(r -> filterOnType(r, SRVRecord.class))
        .map(r -> (SRVRecord) r)
        .map(
            r ->
                new DNSSRVRecord(
                    r.getName().toString(),
                    r.getTTL(),
                    r.getPriority(),
                    r.getWeight(),
                    r.getPort(),
                    r.getTarget().toString()))
        .collect(Collectors.toList());
  }
}
