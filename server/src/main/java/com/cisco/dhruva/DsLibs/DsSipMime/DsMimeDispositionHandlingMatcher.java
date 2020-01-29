/*

 FILENAME:    DsMimeDispositionHandlingMatcher.java


 DESCRIPTION: Concrete class DsMimeDispositionHandlingMatcher (extending
              DsMimeStringMatcher) provides filtering capability based
              on Content-Disposition handling.

 MODULE:      DsMimeDispositionHandlingMatcher

 COPYRIGHT:

 Copyright (c) 2004 by Cisco Systems, Inc.
 All rights reserved.

*/

package com.cisco.dhruva.DsLibs.DsSipMime;

import com.cisco.dhruva.DsLibs.DsSipObject.*;
import java.util.*;

/**
 * Concrete class DsMimeDispositionHandlingMatcher (extending DsMimeStringMatcher) provides
 * filtering capability based on Content-Disposition handling. Methods in this class are NOT thread
 * safe.
 */
public class DsMimeDispositionHandlingMatcher extends DsMimeStringMatcher {
  /**
   * Constructor. Does case insensitive string comparison.
   *
   * @param dh disposition handling value
   */
  public DsMimeDispositionHandlingMatcher(DsByteString dh) {
    this(dh, true);
  }

  /**
   * Constructor
   *
   * @param dh disposition handling value
   * @param ignoreCase ignore case when comparing string values.
   */
  public DsMimeDispositionHandlingMatcher(DsByteString dh, boolean ignoreCase) {
    super(dh, ignoreCase);
  }

  protected DsByteString searchString(DsMimeEntity entity) {
    return (entity == null ? null : entity.getDispositionHandling());
  }

  /**
   * Sets the disposition handling value to match against. This method is provided mainly for
   * matcher reuse.
   *
   * @param dh disposition handling value
   */
  public void setHandling(DsByteString dh) {
    setValue(dh);
  }

  /**
   * Gets the disposition handling value to match against.
   *
   * @return disposition handling value
   */
  public DsByteString getHandling() {
    return getValue();
  }

  /**
   * Returns cached content disposition handling matcher. If there is no cache, a new
   * DsMimeDispositionHandlingMatcher will be created and stored in the cache.
   *
   * @param handling disposition handling
   * @return cached matcher
   */
  public static DsMimeDispositionHandlingMatcher getMatcher(DsByteString handling) {
    if (handling == null) return NULL_MATCHER;
    DsMimeDispositionHandlingMatcher matcher =
        (DsMimeDispositionHandlingMatcher) cache.get(handling);
    if (matcher == null) {
      matcher = new ReadOnlyDispositionHandlingMatcher(handling);
      cache.put(handling, matcher);
    }
    return matcher;
  }

  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (!(obj instanceof DsMimeDispositionHandlingMatcher)) return false;
    DsMimeDispositionHandlingMatcher m = (DsMimeDispositionHandlingMatcher) obj;
    if (ignoreCase != m.ignoreCase) return false;
    if (value == null) return (m.value == null);
    return (ignoreCase ? value.equalsIgnoreCase(m.value) : value.equals(m.value));
  }

  public String toString() {
    return super.toString(
        DISPOSITION_HANDLING_MATCHER_TO_STRING_FORMAT,
        new Object[] {value, (ignoreCase ? STRING_CASE_INSENSITIVE : STRING_CASE_SENSITIVE)});
  }

  /** stores cached content disposition handling matchers. */
  private static Hashtable cache = new Hashtable(11);
  /** null content disposition handling matcher. */
  private static DsMimeDispositionHandlingMatcher NULL_MATCHER =
      new ReadOnlyDispositionHandlingMatcher(null);

  /** toString format */
  protected static final String DISPOSITION_HANDLING_MATCHER_TO_STRING_FORMAT =
      "Matcher for content disposition handling parameter [{0}], case {1}ly.";

  /** Read-only matcher. */
  static class ReadOnlyDispositionHandlingMatcher extends DsMimeDispositionHandlingMatcher {
    public ReadOnlyDispositionHandlingMatcher(DsByteString ct) {
      super(ct);
    }

    public void setValue(DsByteString value) {
      throw new UnsupportedOperationException("This is a read-only matcher.");
    }
  }
} // End of public class DsMimeDispositionHandlingMatcher
