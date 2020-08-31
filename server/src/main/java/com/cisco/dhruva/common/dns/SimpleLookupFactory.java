package com.cisco.dhruva.common.dns;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.TextParseException;

/** A LookupFactory that always returns new instances. */
public class SimpleLookupFactory implements LookupFactory {

  private final Resolver resolver;

  public SimpleLookupFactory() {
    this(null);
  }

  public SimpleLookupFactory(Resolver resolver) {
    this.resolver = resolver;
  }

  @Override
  public Lookup createLookup(String searchString, int type) {
    try {
      final Lookup lookup = new Lookup(searchString, type);
      if (resolver != null) {
        lookup.setResolver(resolver);
      }
      return lookup;
    } catch (TextParseException e) {
      // TODO Log error
      throw new DnsException("unable to create lookup for name: " + searchString, e);
    }
  }
}
