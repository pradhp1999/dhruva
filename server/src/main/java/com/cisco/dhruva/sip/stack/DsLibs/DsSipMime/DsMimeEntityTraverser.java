/*

 FILENAME:    DsMimeEntityTraverser.java


 DESCRIPTION: Interface DsMimeEntityTraverser provides mechanism to traverse
              tree nodes of a MIME entity in a specified order, for example,
              pre-order or post-order. A traverser is normally only of interest
              to entities that contain multiple body parts.


 MODULE:      DsMimeEntityTraverser

 COPYRIGHT:

 Copyright (c) 2004 by Cisco Systems, Inc.
 All rights reserved.

*/

package com.cisco.dhruva.sip.stack.DsLibs.DsSipMime;

/**
 * Interface DsMimeEntityTraverser provides mechanism to traverse tree nodes of a MIME entity in a
 * specified order, for example, pre-order or post-order. A traverser is normally only of interest
 * to entities that contain multiple body parts. If a body is not an container, the single instance
 * of DsMimeNullTraverser should be returned.
 */
public interface DsMimeEntityTraverser {

  /**
   * Returns true if there are more entities to traverse.
   *
   * @return true if there are more entities to traverse.
   */
  public boolean hasNext();

  /**
   * Returns the next entity.
   *
   * @return the next entity.
   */
  public DsMimeEntity next();

  /**
   * Removes the current entity. This method can be called only once per call to next(). The
   * behavior of a traverser is unspecified if the underlying MIME body is modified while the
   * traversal is in progress in any way other than by calling this method.
   */
  public void remove();
} // End of public interface DsMimeEntityTraverser
