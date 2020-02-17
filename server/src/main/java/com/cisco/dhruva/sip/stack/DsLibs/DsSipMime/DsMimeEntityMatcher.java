/*

 FILENAME:    DsMimeEntityMatcher.java


 DESCRIPTION: Interface DsMimeEntityMatcher is implemented by the
              application code and passed in during each visitation
              to filter out the entities of no interest to applications.


 MODULE:      DsMimeEntityMatcher

 COPYRIGHT:

 Copyright (c) 2004 by Cisco Systems, Inc.
 All rights reserved.

*/

package com.cisco.dhruva.sip.stack.DsLibs.DsSipMime;

/**
 * Interface DsMimeEntityMatcher is implemented by the application code and passed in during each
 * visitation to filter out the entities of no interest to application.
 */
public interface DsMimeEntityMatcher {

  /**
   * Returns true if the entity is of interest to the application.
   *
   * @param entity the entity to examine for
   * @return true if the entity is of interest to the application
   */
  public boolean matches(DsMimeEntity entity);
} // End of public interface DsMimeEntityMatcher
