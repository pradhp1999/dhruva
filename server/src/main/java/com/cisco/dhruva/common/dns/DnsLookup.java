package com.cisco.dhruva.common.dns;

import com.cisco.dhruva.common.dns.dto.DNSARecord;
import com.cisco.dhruva.common.dns.dto.DNSSRVRecord;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface DnsLookup {
  CompletableFuture<List<DNSSRVRecord>> lookupSRV(String lookup)
      throws ExecutionException, InterruptedException;

  CompletableFuture<List<DNSARecord>> lookupA(String host)
      throws ExecutionException, InterruptedException;
}
