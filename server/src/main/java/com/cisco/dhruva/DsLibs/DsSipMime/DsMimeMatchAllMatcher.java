/*

 FILENAME:    DsMimeMatchAllMatcher.java


 DESCRIPTION: The concrete class DsMimeMatchAllMatcher (a singleton) implements
              interface DsMimeEntityMatcher and always returns true for method matches().

 MODULE:      DsMimeMatchAllMatcher

 COPYRIGHT:

 Copyright (c) 2004 by Cisco Systems, Inc.
 All rights reserved.

*/

package com.cisco.dhruva.DsLibs.DsSipMime;

/**
 * The concrete class DsMimeMatchAllMatcher (a singleton) implements interface DsMimeEntityMatcher
 * and always returns true for method matches(). This essentially matches all entities. It is
 * provided by the stack to avoid null pointer checking for matcher and should be used if the
 * application code passes null as the matcher argument for acceptVisitor() method call.
 */
public class DsMimeMatchAllMatcher implements DsMimeEntityMatcher {

  /** Private constructor for the Singleton pattern */
  private DsMimeMatchAllMatcher() {
    // empty
  }

  /** Returns the only instance of this type of matchers */
  public static DsMimeMatchAllMatcher getInstance() {
    return instance;
  }

  /** Always returns true. */
  public boolean matches(DsMimeEntity entity) {
    return true;
  }

  /** single instance of the matcher. */
  private static DsMimeMatchAllMatcher instance = new DsMimeMatchAllMatcher();
} // End of public class DsMimeMatchAllMatcher
