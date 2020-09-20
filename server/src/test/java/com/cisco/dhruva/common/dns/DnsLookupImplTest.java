package com.cisco.dhruva.common.dns;


import com.cisco.dhruva.common.dns.dto.DNSSRVRecord;

import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.xbill.DNS.*;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;

import org.xbill.DNS.DClass;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.Section;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

public class DnsLookupImplTest {
    DnsLookup resolver;

    LookupFactory lookupFactory;
    Resolver xbillResolver;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeTest
    public void setUp() {
        lookupFactory = mock(LookupFactory.class);

        SrvRecordCache srvCache = new SrvRecordCache(1000, 50000);
        ARecordCache aCache = new ARecordCache(1000, 50000);

        resolver = new DnsLookupImpl(srvCache, aCache, lookupFactory);

        xbillResolver = mock(Resolver.class);
    }

    @AfterTest
    public void tearDown() {
        Lookup.refreshDefault();
    }

    @Test
    public void shouldReturnResultsFromLookup() throws Exception {
        String fqdn = "thefqdn.";
        String[] resultNodes = new String[] { "node1.domain.", "node2.domain." };

        setupResponseForQuery(fqdn, fqdn, resultNodes);

        CompletableFuture<List<DNSSRVRecord>> f = resolver.lookupSRV(fqdn);
        List<DNSSRVRecord> actual  = f.get();

        Set<String> nodeNames = actual.stream().map(DNSSRVRecord::getTarget).collect(Collectors.toSet());

        assertThat(nodeNames, containsInAnyOrder(resultNodes));
    }

    @Test
    public void shouldIndicateCauseFromXBillIfLookupFails() throws Exception {
        thrown.expect(DnsException.class);
        thrown.expectMessage("response does not match query");

        String fqdn = "thefqdn.";
        setupResponseForQuery(fqdn, "somethingelse.", "node1.domain.", "node2.domain.");

        resolver.lookupSRV(fqdn);
    }

    @Test
    public void shouldIndicateNameIfLookupFails() throws Exception {
        thrown.expect(DnsException.class);
        thrown.expectMessage("thefqdn.");

        String fqdn = "thefqdn.";
        setupResponseForQuery(fqdn, "somethingelse.", "node1.domain.", "node2.domain.");

        resolver.lookupSRV(fqdn);
    }

    @Test(expectedExceptions = { ExecutionException.class })
    public void shouldReturnEmptyForHostNotFound() throws Exception {
        String fqdn = "thefqdn.";

        when(lookupFactory.createLookup(fqdn, Type.SRV)).thenReturn(testLookup(fqdn));
        when(xbillResolver.send(any(Message.class))).thenReturn(messageWithRCode(fqdn, Rcode.NXDOMAIN));

        CompletableFuture<List<DNSSRVRecord>> f = resolver.lookupSRV(fqdn);
        List<DNSSRVRecord> actual = f.get();
        //assertThat(f.get().isEmpty(), is(true));
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

    private void setupResponseForQuery(String queryFqdn, String responseFqdn, String... results)
            throws IOException {
        when(lookupFactory.createLookup(queryFqdn, Type.SRV)).thenReturn(testLookup(queryFqdn));
        when(xbillResolver.send(any(Message.class)))
                .thenReturn(messageWithNodes(responseFqdn, results));
    }

    private Lookup testLookup(String thefqdn) throws TextParseException {
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
