package com.cisco.dhruva.common.dns;

import com.cisco.dhruva.common.dns.dto.DNSARecord;
import com.cisco.dhruva.common.dns.dto.DNSSRVRecord;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class DnsResolversTest {
  private DnsLookup resolver;

  @BeforeTest
  public void setUp() {
    resolver = DnsResolvers.newBuilder().build();
  }

  @Test
  public void checkResultsValidSrvQuery() {
    try {
      CompletableFuture<List<DNSSRVRecord>> future = resolver.lookupSRV("_sip._tcp.webex.com");
      List<DNSSRVRecord> records = future.get();
      Assert.assertFalse(records.isEmpty());
    } catch (DnsException e) {
      Assert.fail();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }

  @Test(expectedExceptions = {DnsException.class})
  public void checkResultsInValidSrvQuery() throws DnsException {
    resolver.lookupSRV("_sip._qazwsx.webex.com");
  }

  @Test(expectedExceptions = {DnsException.class})
  public void shouldFailForBadHostNames() {
    resolver.lookupSRV("_sips._spamhost.tcp.com");
  }

  @Test
  public void checkResultsValidAQuery() {
    try {
      CompletableFuture<List<DNSARecord>> future = resolver.lookupA("geo-pri-1.cmr.webex.com");
      List<DNSARecord> records = future.get();
      Assert.assertFalse(records.isEmpty());
    } catch (DnsException e) {
      Assert.fail();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }
}
