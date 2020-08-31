package com.cisco.dhruva.common.dns;

import java.util.List;
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
      List<DNSSRVRecord> records = resolver.lookupSRV("_sip._tcp.webex.com");
      Assert.assertFalse(records.isEmpty());
    } catch (DnsException e) {
      Assert.fail();
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
}
