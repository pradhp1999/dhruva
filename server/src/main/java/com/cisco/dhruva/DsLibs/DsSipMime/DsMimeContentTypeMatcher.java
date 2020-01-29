/*

 FILENAME:    DsMimeContentTypeMatcher.java


 DESCRIPTION: The concrete class DsMimeContentTypeMatcher (extending
              DsMimeStringMatcher) provides filtering capability based on
              Content-Type.

 MODULE:      DsMimeContentTypeMatcher

 AUTHOR:      JR Yang (jryang@cisco.com)
              Michael Zhou (xmzhou@cisco.com)

 COPYRIGHT:

 Copyright (c) 2004 by Cisco Systems, Inc.
 All rights reserved.

*/

package com.cisco.dhruva.DsLibs.DsSipMime;

import com.cisco.dhruva.DsLibs.DsSipObject.*;
import java.util.*;

/**
 * The concrete class DsMimeContentTypeMatcher (extending DsMimeStringMatcher) provides filtering
 * capability based on Content-Type. Methods in this class are NOT thread safe.
 */
public class DsMimeContentTypeMatcher extends DsMimeStringMatcher {
  /**
   * Constructor. Does case insensitvie string comparison.
   *
   * @param ct content type value in the form of type/subtype
   */
  public DsMimeContentTypeMatcher(DsByteString ct) {
    this(ct, true);
  }

  /**
   * Constructor
   *
   * @param ct content type value in the form of type/subtype.
   * @param ignoreCase ignore case when comparing string values.
   */
  public DsMimeContentTypeMatcher(DsByteString ct, boolean ignoreCase) {
    super(ct, ignoreCase);
  }

  protected DsByteString searchString(DsMimeEntity entity) {
    return (entity == null ? null : entity.getBodyType());
  }

  /**
   * Sets the Content-Type value to match against. This method is provided mainly for matcher reuse.
   *
   * @param ct Content-Type value in the form of type/subtype
   */
  public void setContentType(DsByteString ct) {
    setValue(ct);
  }

  /**
   * Gets the Content-Type value to match against.
   *
   * @return Content-Type value in the form of type/subtype
   */
  public DsByteString getContentType() {
    return getValue();
  }

  /**
   * Returns cached content type matcher. If there is no cache, a new DsMimeContentTypeMatcher will
   * be created and stored in the cache.
   *
   * @param type content type
   * @return cached matcher
   */
  public static DsMimeContentTypeMatcher getMatcher(DsByteString type) {
    if (type == null) return NULL_MATCHER;
    DsMimeContentTypeMatcher matcher = (DsMimeContentTypeMatcher) cache.get(type);
    if (matcher == null) {
      matcher = new ReadOnlyContentTypeMatcher(type);
      cache.put(type, matcher);
    }
    return matcher;
  }

  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (!(obj instanceof DsMimeContentTypeMatcher)) return false;
    DsMimeContentTypeMatcher m = (DsMimeContentTypeMatcher) obj;
    if (ignoreCase != m.ignoreCase) return false;
    if (value == null) return (m.value == null);
    return (ignoreCase ? value.equalsIgnoreCase(m.value) : value.equals(m.value));
  }

  public String toString() {
    return super.toString(
        CONTENT_TYPE_MATCHER_TO_STRING_FORMAT,
        new Object[] {value, (ignoreCase ? STRING_CASE_INSENSITIVE : STRING_CASE_SENSITIVE)});
  }

  /** stores cached content type matchers. */
  private static Hashtable cache = new Hashtable(11);
  /** null content type matcher. */
  private static final DsMimeContentTypeMatcher NULL_MATCHER = new ReadOnlyContentTypeMatcher(null);

  /** toString format */
  protected static final String CONTENT_TYPE_MATCHER_TO_STRING_FORMAT =
      "Matcher for content type [{0}], case {1}ly.";

  /** Read-only matcher. */
  static class ReadOnlyContentTypeMatcher extends DsMimeContentTypeMatcher {
    public ReadOnlyContentTypeMatcher(DsByteString ct) {
      super(ct);
    }

    public void setValue(DsByteString value) {
      throw new UnsupportedOperationException("This is a read-only matcher.");
    }
  }
} // End of public class DsMimeContentTypeMatcher
