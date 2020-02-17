// Copyright (c) 2004-2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipMime;

/**
 * Base class of matchers which provide logically binary operations of matchers, such as
 * DsMimeAndMatcher and DsMimeOrMatcher. Methods in this class are NOT thread safe.
 */
public abstract class DsMimeBinaryMatcher implements DsMimeEntityMatcher {
  /**
   * Constructor.
   *
   * @param op1 operand 1
   * @param op2 operand 2
   * @throws IllegalArgumentException if either op1 or op2 is null
   */
  protected DsMimeBinaryMatcher(DsMimeEntityMatcher op1, DsMimeEntityMatcher op2) {
    this.op1 = op1;
    this.op2 = op2;
  }

  /**
   * Resets the operands. This method is provided mainly for matcher reuse.
   *
   * @param op1 operand 1
   * @param op2 operand 2
   * @throws IllegalArgumentException if either op1 or op2 is null
   */
  public void reset(DsMimeEntityMatcher op1, DsMimeEntityMatcher op2) {
    if (op1 == null || op2 == null)
      throw new IllegalArgumentException("One of the arguments is null.");
    this.op1 = op1;
    this.op2 = op2;
  }

  /** Operand 1. */
  protected DsMimeEntityMatcher op1;
  /** Operand 2. */
  protected DsMimeEntityMatcher op2;
} // End of public class DsMimeBinaryMatcher
