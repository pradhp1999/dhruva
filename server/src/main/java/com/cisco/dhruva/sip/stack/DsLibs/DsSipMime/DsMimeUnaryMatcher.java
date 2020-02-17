// Copyright (c) 2004-2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipMime;

/**
 * Base class of matchers which provide logically unary operations of matchers, such as
 * DsMimeNotMatcher. Methods in this class are NOT thread safe.
 */
public abstract class DsMimeUnaryMatcher implements DsMimeEntityMatcher {

  /**
   * Constructor.
   *
   * @param op operand
   * @throws IllegalArgumentException if op is null
   */
  protected DsMimeUnaryMatcher(DsMimeEntityMatcher op) {
    reset(op);
  }

  /**
   * Resets the operand. This method is provided mainly for matcher reuse.
   *
   * @param op operand
   * @throws IllegalArgumentException if op is null
   */
  public void reset(DsMimeEntityMatcher op) {
    if (op == null) throw new IllegalArgumentException("Matcher arguments is null.");
    this.op = op;
  }

  /** Operand. */
  protected DsMimeEntityMatcher op;
} // End of public class DsMimeUnaryMatcher
