package com.cisco.dhruva.common.dns;

import com.cisco.dhruva.common.dns.dto.DNSARecord;
import com.cisco.dhruva.common.dns.dto.DNSSRVRecord;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import okhttp3.Dns;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.xbill.DNS.*;

import javax.validation.constraints.AssertTrue;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.isA;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class DnsResolversTest {
  private DnsLookup resolver;
  @Rule
  public ExpectedException thrown = ExpectedException.none();


  LookupFactory lookupFactory;
  Resolver xbillResolver;



  @BeforeTest
  public void setUp() {
    lookupFactory = mock(LookupFactory.class);

//    SrvRecordCache srvCache = new SrvRecordCache(1000, 50000);
//    ARecordCache aCache = new ARecordCache(1000, 50000);

    xbillResolver = mock(Resolver.class);

    resolver = DnsResolvers.
            newBuilder().
            lookupFactory(lookupFactory).
            build();
  }

  @Test
  public void checkResultsValidSrvQuery() throws Exception {
    try {

      String queryFqdn = "thefqdn.";

      setupResponseForSrvQuery(queryFqdn, queryFqdn, "node1.domain.", "node2.domain.");

      CompletableFuture<List<DNSSRVRecord>> future = resolver.lookupSRV(queryFqdn);
      List<DNSSRVRecord> records = future.get();
      Assert.assertFalse(records.isEmpty());
    } catch (DnsException e) {
      Assert.fail();
    } catch (InterruptedException | ExecutionException e) {
      Assert.fail();
      e.printStackTrace();
    }
  }

  @Test(expectedExceptions = {ExecutionException.class})
  public void checkResultsInValidSrvQuery() throws Exception {
    thrown.expectCause(isA(DnsException.class));

    String fqdn = "thefqdn.";

    when(lookupFactory.createLookup(fqdn, Type.SRV)).thenReturn(testLookupSrv(fqdn));
    when(xbillResolver.send(any(Message.class))).thenReturn(messageWithRCode(fqdn, Rcode.NXDOMAIN));

    CompletableFuture<List<DNSSRVRecord>> f =
            resolver.lookupSRV(fqdn);
    List<DNSSRVRecord> dnssrvRecords = f.get();
  }

  @Test(expectedExceptions = {ExecutionException.class})
  public void shouldFailForBadHostNames() throws Exception {
    thrown.expectCause(isA(DnsException.class));

    String fqdn = "thefqdn.";

    when(lookupFactory.createLookup(fqdn, Type.SRV)).thenReturn(testLookupSrv(fqdn));
    when(xbillResolver.send(any(Message.class))).thenReturn(messageWithRCode(fqdn, Rcode.NXDOMAIN));


    CompletableFuture<List<DNSSRVRecord>> f = resolver.lookupSRV("_sips._spamhost.tcp.com");
    List<DNSSRVRecord> dnssrvRecords = f.get();
  }

  @Test
  public void checkResultsValidAQuery() throws Exception{
    try {
      String host = "thehost.";
      setupResponseForAQuery(host, host, "node1.domain.", "node2.domain.");
      CompletableFuture<List<DNSARecord>> future = resolver.lookupA(host);
      List<DNSARecord> records = future.get();
      Assert.assertFalse(records.isEmpty());
    } catch (DnsException e) {
      Assert.fail();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void checkResultsInValidAQuery() throws Exception{
    try {

      String host = "thehost.";
      when(lookupFactory.createLookup(host, Type.A)).thenReturn(testLookupA(host));
      when(xbillResolver.send(any(Message.class))).thenReturn(messageWithRCode(host, Rcode.NXDOMAIN));
      CompletableFuture<List<DNSARecord>> future = resolver.lookupA(host);
      List<DNSARecord> records = future.get();
      Assert.assertFalse(records.isEmpty());
    } catch (DnsException e) {
      Assert.fail();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }

  private void setupResponseForSrvQuery(String queryFqdn, String responseFqdn, String... results)
          throws IOException {
    when(lookupFactory.createLookup(queryFqdn, Type.SRV)).thenReturn(testLookupSrv(queryFqdn));
    when(xbillResolver.send(any(Message.class)))
            .thenReturn(messageWithNodes(responseFqdn, results));
  }
  private void setupResponseForAQuery(String queryFqdn, String responseFqdn, String... results)
          throws IOException {
    when(lookupFactory.createLookup(queryFqdn, Type.A)).thenReturn(testLookupA(queryFqdn));
    when(xbillResolver.send(any(Message.class)))
            .thenReturn(messageWithNodes(responseFqdn, results));
  }

  private Message messageWithRCode(String query, int rcode) throws TextParseException {
    Name queryName = Name.fromString(query);
    Record question = Record.newRecord(queryName, Type.SRV, DClass.IN);
    Message queryMessage = Message.newQuery(question);
    Message result = new Message();
    result.setHeader(queryMessage.getHeader());
    result.addRecord(question, Section.QUESTION);

    result.getHeader().setRcode(rcode);

    return result;
  }
  private Lookup testLookupSrv(String thefqdn) throws TextParseException {
    Lookup result = new Lookup(thefqdn, Type.SRV);

    result.setResolver(xbillResolver);

    return result;
  }
  private Lookup testLookupA(String thefqdn) throws TextParseException {
    Lookup result = new Lookup(thefqdn, Type.A);

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
