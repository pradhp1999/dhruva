// Copyright (c) 2004-2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipMime;

/**
 * Concrete class DsMimeAndMatcher (extending DsMimeBinaryMatcher) provides logical OR operation of
 * two matchers. This can be used to construct complex matchers when looking for body parts. Methods
 * in this class are NOT thread safe.
 */
public class DsMimeOrMatcher extends DsMimeBinaryMatcher {
  /**
   * Constructor.
   *
   * @param op1 operand 1
   * @param op2 operand 2
   * @throws IllegalArgumentException if either op1 or op2 is null
   */
  public DsMimeOrMatcher(DsMimeEntityMatcher op1, DsMimeEntityMatcher op2) {
    super(op1, op2);
  }

  /**
   * Returns true if the entity matches either criterion specified by op1 and op2
   *
   * @param entity mime entity
   * @return true if the entity matches either criterion specified by op1 and op2
   */
  public boolean matches(DsMimeEntity entity) {
    return (op1.matches(entity) || op2.matches(entity));
  }
} // End of public class DsMimeOrMatcher
