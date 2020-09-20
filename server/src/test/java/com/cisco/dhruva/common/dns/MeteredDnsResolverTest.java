package com.cisco.dhruva.common.dns;



import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.never;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cisco.dhruva.common.dns.dto.DNSARecord;
import com.cisco.dhruva.common.dns.dto.DNSSRVRecord;
import com.cisco.dhruva.common.dns.metrics.DnsReporter;
import com.cisco.dhruva.common.dns.metrics.DnsTimingContext;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSRVWrapper;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class MeteredDnsResolverTest {
    private static final String FQDN = "hellowebex";
    private static final DnsException EXCEPTION = new DnsException(1,
            FQDN,
            DnsErrorCode.ERROR_DNS_HOST_NOT_FOUND);
    private static final Error ERROR = new Error();

    @SuppressWarnings("unchecked")
    private static final List<DNSSRVRecord> EMPTY_SRV = mock(List.class);
    @SuppressWarnings("unchecked")
    private static final List<DNSSRVRecord> NOT_EMPTY_SRV = mock(List.class);

    @SuppressWarnings("unchecked")
    private static final List<DNSARecord> EMPTY_A = mock(List.class);
    @SuppressWarnings("unchecked")
    private static final List<DNSARecord> NOT_EMPTY_A = mock(List.class);

    static {
        when(EMPTY_SRV.isEmpty()).thenReturn(true);
        when(NOT_EMPTY_SRV.isEmpty()).thenReturn(false);
        when(EMPTY_A.isEmpty()).thenReturn(true);
        when(NOT_EMPTY_A.isEmpty()).thenReturn(false);
    }

    private DnsLookup delegate;
    private DnsReporter reporter;
    private DnsTimingContext timingReporter;

    private DnsLookup resolver;

    @BeforeMethod
    public void before() {
        delegate = mock(DnsLookup.class);
        reporter = mock(DnsReporter.class);
        timingReporter = mock(DnsTimingContext.class);

        resolver = new MeteredDnsResolver(delegate, reporter);

        when(reporter.resolveTimer()).thenReturn(timingReporter);
    }

    @AfterMethod
    public void after() {
        verify(reporter).resolveTimer();
        verify(timingReporter).stop();
    }

    @Test
    public void shouldCountSuccessfulForSrv() throws Exception {
        when(delegate.lookupSRV(FQDN)).thenReturn(CompletableFuture.completedFuture(NOT_EMPTY_SRV));

        resolver.lookupSRV(FQDN);

        verify(reporter, never()).reportEmpty();
        verify(reporter, never()).reportFailure(EXCEPTION);
    }

    @Test
    public void shouldCountSuccessfulForA() throws Exception {
        when(delegate.lookupA(FQDN)).thenReturn(CompletableFuture.completedFuture(NOT_EMPTY_A));

        resolver.lookupA(FQDN);

        verify(reporter, never()).reportEmpty();
        verify(reporter, never()).reportFailure(EXCEPTION);
    }

    @Test
    public void shouldReportEmpty() throws Exception {
        when(delegate.lookupSRV(FQDN)).thenReturn(CompletableFuture.completedFuture(EMPTY_SRV));

        resolver.lookupSRV(FQDN);

        verify(reporter).reportEmpty();
        verify(reporter, never()).reportFailure(EXCEPTION);
    }

    @Test
    public void shouldReportExceptionForSrv() throws Exception {
        when(delegate.lookupSRV(FQDN)).thenThrow(EXCEPTION);

        try {
            CompletableFuture<List<DNSSRVRecord>> f =
                    new CompletableFuture<>();
            f = resolver.lookupSRV(FQDN);
            List<DNSSRVRecord> dnssrvRecords = f.get();
            Assert.fail();
        } catch(DnsException ignored) {

        }

        verify(reporter, never()).reportEmpty();
        verify(reporter).reportFailure(EXCEPTION);
    }

    @Test
    public void shouldReportExceptionForA() throws Exception {
        when(delegate.lookupA(FQDN)).thenThrow(EXCEPTION);

        try {
            CompletableFuture<List<DNSARecord>> f =
                    new CompletableFuture<>();
            f = resolver.lookupA(FQDN);
            List<DNSARecord> dnssrvRecords = f.get();
            Assert.fail();
        } catch(DnsException ignored) {

        }

        verify(reporter, never()).reportEmpty();
        verify(reporter).reportFailure(EXCEPTION);
    }

    @Test
    public void shouldNotReportError() throws Exception {
        when(delegate.lookupSRV(FQDN)).thenThrow(ERROR);

        try {
            CompletableFuture<List<DNSSRVRecord>> f =
                    new CompletableFuture<>();
            f = resolver.lookupSRV(FQDN);
            List<DNSSRVRecord> dnssrvRecords = f.get();
            Assert.fail();
        } catch(Error e) {
            Assert.assertEquals(ERROR, e);
        }

        verify(reporter, never()).reportEmpty();
        verify(reporter, never()).reportFailure(EXCEPTION);
    }
}
