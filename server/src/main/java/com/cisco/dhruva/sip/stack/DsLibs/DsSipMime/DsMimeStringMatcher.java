/*

 FILENAME:    DsMimeStringMatcher.java


 DESCRIPTION: The abstract class DsMimeStringMatcher (implementing
              DsMimeEntityMatcher) provides a template for matchers
              who look for a matching string in the entity. It is
              the base class of DsMimeContentIdMatcher, DsMimeContentTypeMatcher,
              DsMimeDispositionMatcher and DsMimeDispositionHandlingMatcher.

 MODULE:      DsMimeStringMatcher

 AUTHOR:      Michael Zhou (xmzhou@cisco.com)

 COPYRIGHT:

 Copyright (c) 2004 by Cisco Systems, Inc.
 All rights reserved.

*/

package com.cisco.dhruva.sip.stack.DsLibs.DsSipMime;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import java.text.*;

/**
 * The abstract class DsMimeStringMatcher (implementing DsMimeEntityMatcher) provides a template for
 * matchers who look for a matching string in the entity. It is the base class of
 * DsMimeContentIdMatcher, DsMimeContentTypeMatcher, DsMimeDispositionMatcher and
 * DsMimeDispositionHandlingMatcher. Methods in this class are NOT thread safe.
 */
public abstract class DsMimeStringMatcher implements DsMimeEntityMatcher {
  /**
   * Constructor. Same as DsMimeStringMatcher(value, false), which does case sensitive string
   * comparison.
   *
   * @param value string value to look for
   */
  public DsMimeStringMatcher(DsByteString value) {
    this(value, false);
  }

  /**
   * Constructor
   *
   * @param value string value to look for
   * @param ignoreCase ignore case when comparing string values
   */
  public DsMimeStringMatcher(DsByteString value, boolean ignoreCase) {
    this.value = value;
    setIgnoreCase(ignoreCase);
  }

  /**
   * Returns true if the entity's has a matching string. Subclasses of this class must provide
   * concrete implementation of searchString() method to instruct the stack where to search for the
   * matching string.
   *
   * @param entity mime entity
   * @return true if the entity's content id value matches cid.
   */
  public boolean matches(DsMimeEntity entity) {
    if (entity != null) // entity should always be non-null.
    {
      DsByteString v = searchString(entity);
      return ((value == null)
          ? (v == null)
          : (ignoreCase ? value.equalsIgnoreCase(v) : value.equals(v)));
    }
    return false;
  }

  /**
   * Returns the string to be searched and matched against getValue().
   *
   * @param entity MIME entity
   * @return the string to search for
   */
  protected abstract DsByteString searchString(DsMimeEntity entity);

  /**
   * Sets the string value to match against. This method is provided mainly for matcher reuse.
   *
   * @param value string value
   */
  public void setValue(DsByteString value) {
    this.value = value;
  }

  /**
   * Gets the string value to match against.
   *
   * @return String value
   */
  public DsByteString getValue() {
    return value;
  }

  /**
   * Sets the ignoreCase flag when matching string values. True should passed in if this matcher
   * does case insensitive string matching.
   *
   * @param ignoreCase ignore case when comparing string values
   */
  public void setIgnoreCase(boolean ignoreCase) {
    this.ignoreCase = ignoreCase;
  }

  /**
   * Gets the ignoreCase flag. True is returned if this matcher does case insensitive string
   * matching.
   *
   * @return ignoreCase value
   */
  public boolean getIgnoreCase() {
    return ignoreCase;
  }

  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (!(obj instanceof DsMimeStringMatcher)) return false;
    DsMimeStringMatcher m = (DsMimeStringMatcher) obj;
    return (ignoreCase == m.ignoreCase && DsByteString.equals(value, m.value));
  }

  public String toString() {
    return toString(
        STRING_MATCHER_TO_STRING_FORMAT,
        new Object[] {value, (ignoreCase ? STRING_CASE_INSENSITIVE : STRING_CASE_SENSITIVE)});
  }

  protected String toString(String format, Object[] objs) {
    return MessageFormat.format(format, objs);
  }

  /** String value */
  protected DsByteString value = null;
  /** Case sensitivity of value comparison */
  protected boolean ignoreCase = false;

  /** toString format */
  protected static final String STRING_MATCHER_TO_STRING_FORMAT =
      "Matcher for string value [{0}], case {1}ly.";
  /** case sensitive */
  protected static final String STRING_CASE_SENSITIVE = "sensitive";
  /** case insensitive */
  protected static final String STRING_CASE_INSENSITIVE = "insensitive";
} // End of public class DsMimeStringMatcher
