/*

 FILENAME:    DsMimeEntityVisitor.java


 DESCRIPTION: Interface DsMimeEntityVisitor is implemented by the
              application code to retrieve information out of MIME
              content. User code will supply concrete implementation
              of visitor and entity matcher for a visitation.


 MODULE:      DsMimeEntityVisitor

 COPYRIGHT:

 Copyright (c) 2004 by Cisco Systems, Inc.
 All rights reserved.

*/

package com.cisco.dhruva.DsLibs.DsSipMime;

/**
 * Interface DsMimeEntityVisitor is implemented by the application code to retrieve information out
 * of MIME content. User code will supply concrete implementation of visitor and entity matcher for
 * a visitation.
 *
 * <p>It defines only one method visit() which is called by the stack when a particular MIME entity
 * is found to match the criteria for this visitation. It also defines constants (as return code of
 * method visit()) for application to instruct the stack how to proceed when method visit() returns.
 */
public interface DsMimeEntityVisitor {

  /** Constant: continue this visitation. */
  public static final int CONTINUE_VISIT = 0;

  /** Constant: stop this visitation. */
  public static final int STOP_VISIT = 1;

  /**
   * Callback method invoked when a MIME entity of interest is found.
   *
   * @param entity the MIME entity that matches supplied criteria
   * @return the code to instruct stack how to proceed
   */
  public int visit(DsMimeEntity entity);
} // End of public interface DsMimeEntityVisitor
