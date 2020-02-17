/*

 FILENAME:    DsMimeNullTraverser.java


 DESCRIPTION: Implementing DsMimeEntityTraverser, this singleton class is
              conveniently used as the return value of traverser() method
              for non-container content types.

 MODULE:      DsMimeNullTraverser

 COPYRIGHT:

 Copyright (c) 2004 by Cisco Systems, Inc.
 All rights reserved.

*/

package com.cisco.dhruva.sip.stack.DsLibs.DsSipMime;

/**
 * Implementing DsMimeEntityTraverser, this singleton class is conveniently used as the return value
 * of traverser() method for non-container content types.
 */
public class DsMimeNullTraverser implements DsMimeEntityTraverser {
  /** Private constructor. */
  private DsMimeNullTraverser() {}

  /**
   * Returns the single instance of the NULL traverser.
   *
   * @return the single instance of the NULL traverser.
   */
  public static DsMimeNullTraverser getInstance() {
    return instance;
  }

  public boolean hasNext() {
    return false;
  }

  public DsMimeEntity next() {
    return null;
  }

  public void remove() {}

  /** single instance of the class */
  private static DsMimeNullTraverser instance = new DsMimeNullTraverser();
} // End of public interface DsMimeNullTraverser
