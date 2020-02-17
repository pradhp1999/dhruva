// Copyright (c) 2004-2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipMime;

/**
 * The concrete class DsMimeNotMatcher (extending DsMimeUnaryMatcher) provides logical NOT operation
 * of a matcher. This This can be used to construct complex matchers when looking for body parts.
 * Methods in this class are NOT thread safe.
 */
public class DsMimeNotMatcher extends DsMimeUnaryMatcher {
  /**
   * Constructor
   *
   * @param op operand
   * @throws IllegalArgumentException is op is null
   */
  public DsMimeNotMatcher(DsMimeEntityMatcher op) {
    super(op);
  }

  /**
   * Returns true if the entity does NOT match the criterion specified by op.
   *
   * @param entity mime entity
   * @return true if the entity does NOT match the criterion specified by op.
   */
  public boolean matches(DsMimeEntity entity) {
    return (!op.matches(entity));
  }
} // End of public class DsMimeNotMatcher
