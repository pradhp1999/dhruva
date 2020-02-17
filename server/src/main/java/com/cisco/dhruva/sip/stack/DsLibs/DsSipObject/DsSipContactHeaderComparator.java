/*------------------------------------------------------------------
 * Comparator function for DsSipContactHeader objects.
 *
 * July 2003, kimle
 *
 * Copyright (c) 2003 by cisco Systems, Inc.
 * All rights reserved.
 *------------------------------------------------------------------
 */
package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import java.util.Comparator;

/**
 * This class represents a comparator for DsSipContactHeader objects. Comparison is based on
 * qvalues.
 */
public class DsSipContactHeaderComparator implements Comparator {

  public int compare(Object o1, Object o2) {

    DsSipContactHeader a = (DsSipContactHeader) o1;
    DsSipContactHeader b = (DsSipContactHeader) o2;

    float first = a.getQvalue();
    float second = b.getQvalue();

    if (Float.compare(second, first) == 0) {
      if (a.hasFeature(DsSipAcceptContactHeader.SCORE)
          && b.hasFeature(DsSipAcceptContactHeader.SCORE)) {

        Float floatA = new Float(a.getFeature(DsSipAcceptContactHeader.SCORE).toString());
        Float floatB = new Float(b.getFeature(DsSipAcceptContactHeader.SCORE).toString());

        float firstA = floatA.floatValue();
        float secondA = floatB.floatValue();

        return Float.compare(secondA, firstA);
      }
      if (a.hasFeature(DsSipAcceptContactHeader.SCORE)) {
        return 1;
      }
      if (a.hasFeature(DsSipAcceptContactHeader.SCORE)) {
        return -1;
      }
    }

    return Float.compare(second, first);
  }
}
