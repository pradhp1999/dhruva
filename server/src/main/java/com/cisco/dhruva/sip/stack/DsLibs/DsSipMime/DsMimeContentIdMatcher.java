/*

 FILENAME:    DsMimeContentIdMatcher.java


 DESCRIPTION: The concrete class DsMimeContentIdMatcher (extending
              DsMimeStringMatcher) provides filtering capability based on
              Content-Id header value.

 MODULE:      DsMimeContentIdMatcher

 AUTHOR:      JR Yang (jryang@cisco.com)
              Michael Zhou (xmzhou@cisco.com)

 COPYRIGHT:

 Copyright (c) 2004 by Cisco Systems, Inc.
 All rights reserved.

*/

package com.cisco.dhruva.sip.stack.DsLibs.DsSipMime;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;

/**
 * The concrete class DsMimeContentIdMatcher (extending DsMimeStringMatcher) provides filtering
 * capability based on Content-Id header value. Methods in this class are NOT thread safe.
 */
public class DsMimeContentIdMatcher extends DsMimeStringMatcher {

  /**
   * Constructor. Does case sensitive string comparison.
   *
   * @param cid content id value to look for
   */
  public DsMimeContentIdMatcher(DsByteString cid) {
    this(cid, false);
  }

  /**
   * Constructor
   *
   * @param cid content id value to look for
   * @param ignoreCase ignore case when comparing string values
   */
  public DsMimeContentIdMatcher(DsByteString cid, boolean ignoreCase) {
    super(cid, ignoreCase);
  }

  protected DsByteString searchString(DsMimeEntity entity) {
    return (entity == null ? null : entity.getContentId());
  }

  /**
   * Sets the Content-Id value to match against. This method is provided mainly for matcher reuse.
   *
   * @param cid Content-Id value
   */
  public void setContentId(DsByteString cid) {
    setValue(cid);
  }

  /**
   * Gets the Content-Id value to match against.
   *
   * @return Content-Id value
   */
  public DsByteString getContentId() {
    return getValue();
  }

  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (!(obj instanceof DsMimeContentIdMatcher)) return false;
    DsMimeContentIdMatcher m = (DsMimeContentIdMatcher) obj;
    if (ignoreCase != m.ignoreCase) return false;
    if (value == null) return (m.value == null);
    return (ignoreCase ? value.equalsIgnoreCase(m.value) : value.equals(m.value));
  }

  public String toString() {
    return super.toString(
        CONTENT_ID_MATCHER_TO_STRING_FORMAT,
        new Object[] {value, (ignoreCase ? STRING_CASE_INSENSITIVE : STRING_CASE_SENSITIVE)});
  }

  /** toString format */
  protected static final String CONTENT_ID_MATCHER_TO_STRING_FORMAT =
      "Matcher for content ID [{0}], case {1}ly.";
} // End of public class DsMimeContentIdMatcher
