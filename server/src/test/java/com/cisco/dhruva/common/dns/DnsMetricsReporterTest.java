package com.cisco.dhruva.common.dns;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import com.cisco.dhruva.service.MetricService;
import java.io.IOException;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.xbill.DNS.*;

public class DnsMetricsReporterTest {

  private DnsLookup resolver;

  @Mock MetricService metricService;

  @InjectMocks DnsMetricsReporter dnsMetricsReporter;

  LookupFactory lookupFactory;
  Resolver xbillResolver;

  @BeforeTest
  public void setUp() {
    lookupFactory = mock(LookupFactory.class);

    xbillResolver = mock(Resolver.class);

    MockitoAnnotations.initMocks(this);

    resolver =
        DnsResolvers.newBuilder().lookupFactory(lookupFactory).metered(dnsMetricsReporter).build();
  }

  @Test
  public void testSrvQueryWithMetrics() throws Exception {
    String query = "thewebex101.";
    doNothing().when(metricService).sendDNSMetric(anyString(), anyString(), anyLong());
    setupResponseForSrvQuery(query, query, "node1.domain.", "node2.domain.");

    resolver.lookupSRV(query);
    ArgumentCaptor<String> argumentCaptor1 = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> argumentCaptor2 = ArgumentCaptor.forClass(String.class);
    verify(metricService)
        .sendDNSMetric(argumentCaptor1.capture(), argumentCaptor2.capture(), anyLong());

    String actualQuery = argumentCaptor1.getValue();
    Assert.assertEquals(query, actualQuery);

    String actualQueryType = argumentCaptor2.getValue();
    Assert.assertEquals("SRV", actualQueryType);
  }

  private void setupResponseForSrvQuery(String queryFqdn, String responseFqdn, String... results)
      throws IOException {
    when(lookupFactory.createLookup(queryFqdn, Type.SRV)).thenReturn(testLookupSrv(queryFqdn));
    when(xbillResolver.send(any(Message.class)))
        .thenReturn(messageWithNodes(responseFqdn, results));
  }

  private Lookup testLookupSrv(String thefqdn) throws TextParseException {
    Lookup result = new Lookup(thefqdn, Type.SRV);

    result.setResolver(xbillResolver);

    return result;
  }

  private Message messageWithNodes(String query, String[] names) throws TextParseException {
    Name queryName = Name.fromString(query);
    Record question = Record.newRecord(queryName, Type.SRV, DClass.IN);
    Message queryMessage = Message.newQuery(question);
    Message result = new Message();
    result.setHeader(queryMessage.getHeader());
    result.addRecord(question, Section.QUESTION);

    for (String name1 : names) {
      result.addRecord(
          new SRVRecord(queryName, DClass.IN, 1, 1, 1, 8080, Name.fromString(name1)),
          Section.ANSWER);
    }

    return result;
  }
}
