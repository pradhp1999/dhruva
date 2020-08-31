package com.cisco.dhruva.common.dns;

import java.util.List;

public interface DnsLookup {
  List<DNSSRVRecord> lookupSRV(String lookup);

  List<DNSARecord> lookupA(String host);
}
