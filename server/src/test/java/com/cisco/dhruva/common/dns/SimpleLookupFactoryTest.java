package com.cisco.dhruva.common.dns;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;


public class SimpleLookupFactoryTest {
    SimpleLookupFactory factory;

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    Resolver xbillResolver;

    @Before
    public void setUp() {
        xbillResolver = mock(Resolver.class);
        factory = new SimpleLookupFactory(xbillResolver);
    }

    @Test
    public void shouldCreateLookups() {
    assertThat(factory.createLookup("some.domain.", Type.SRV), is(notNullValue()));
    }

    @Test
    public void shouldCreateNewLookupsEachTime() {
        Lookup first = factory.createLookup("some.other.name.", Type.SRV);
        Lookup second = factory.createLookup("some.other.name.", Type.SRV);

        assertThat(first == second, is(false));
    }

    @Test
    public void shouldRethrowXBillExceptions() {
        thrown.expect(DnsException.class);
        //thrown.expectCause(isA(TextParseException.class));

        factory.createLookup("bad\\1 name", Type.SRV);
    }
}
