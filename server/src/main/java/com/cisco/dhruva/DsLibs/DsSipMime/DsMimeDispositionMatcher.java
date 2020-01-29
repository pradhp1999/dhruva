/*

 FILENAME:    DsMimeDispositionMatcher.java


 DESCRIPTION: The concrete class DsMimeDispositionMatcher (extending
              DsMimeStringMatcher) provides filtering capability based
              on Content-Disposition.

 MODULE:      DsMimeDispositionMatcher

 COPYRIGHT:

 Copyright (c) 2004 by Cisco Systems, Inc.
 All rights reserved.

*/

package com.cisco.dhruva.DsLibs.DsSipMime;

import com.cisco.dhruva.DsLibs.DsSipObject.*;
import java.util.*;

/**
 * The concrete class DsMimeDispositionMatcher (extending DsMimeStringMatcher) provides filtering
 * capability based on Content-Disposition. Methods in this class are NOT thread safe.
 */
public class DsMimeDispositionMatcher extends DsMimeStringMatcher {
  /**
   * Constructor. Does case insensitive string comparison.
   *
   * @param cd content disposition value
   */
  public DsMimeDispositionMatcher(DsByteString cd) {
    this(cd, true);
  }

  /**
   * Constructor
   *
   * @param cd content disposition value
   * @param ignoreCase ignore case when comparing string values.
   */
  public DsMimeDispositionMatcher(DsByteString cd, boolean ignoreCase) {
    super(cd, ignoreCase);
  }

  protected DsByteString searchString(DsMimeEntity entity) {
    return (entity == null ? null : entity.getDispositionType());
  }

  /**
   * Sets the Content-Disposition value to match against. This method is provided mainly for matcher
   * reuse.
   *
   * @param ct Content-Disposition value
   */
  public void setDisposition(DsByteString cd) {
    setValue(cd);
  }

  /**
   * Gets the Content-Disposition value to match against.
   *
   * @return Content-Disposition value
   */
  public DsByteString getDisposition() {
    return getValue();
  }

  /**
   * Returns cached content disposition matcher. If there is no cache, a new
   * DsMimeDispositionMatcher will be created and stored in the cache.
   *
   * @param disposition disposition type
   * @return cached matcher
   */
  public static DsMimeDispositionMatcher getMatcher(DsByteString disposition) {
    if (disposition == null) return NULL_MATCHER;
    DsMimeDispositionMatcher matcher = (DsMimeDispositionMatcher) cache.get(disposition);
    if (matcher == null) {
      matcher = new ReadOnlyDispositionMatcher(disposition);
      cache.put(disposition, matcher);
    }
    return matcher;
  }

  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (!(obj instanceof DsMimeDispositionMatcher)) return false;
    DsMimeDispositionMatcher m = (DsMimeDispositionMatcher) obj;
    if (ignoreCase != m.ignoreCase) return false;
    if (value == null) return (m.value == null);
    return (ignoreCase ? value.equalsIgnoreCase(m.value) : value.equals(m.value));
  }

  public String toString() {
    return super.toString(
        DISPOSITION_MATCHER_TO_STRING_FORMAT,
        new Object[] {value, (ignoreCase ? STRING_CASE_INSENSITIVE : STRING_CASE_SENSITIVE)});
  }

  /** stores cached content disposition handling matchers. */
  private static Hashtable cache = new Hashtable(11);
  /** null content disposition matcher. */
  private static DsMimeDispositionMatcher NULL_MATCHER = new ReadOnlyDispositionMatcher(null);

  /** toString format */
  protected static final String DISPOSITION_MATCHER_TO_STRING_FORMAT =
      "Matcher for content disposition type [{0}], case {1}ly.";

  /** Read-only matcher. */
  static class ReadOnlyDispositionMatcher extends DsMimeDispositionMatcher {
    public ReadOnlyDispositionMatcher(DsByteString ct) {
      super(ct);
    }

    public void setValue(DsByteString value) {
      throw new UnsupportedOperationException("This is a read-only matcher.");
    }
  }
} // End of public class DsMimeDispositionMatcher
