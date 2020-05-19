/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.sip.DsPings;

import com.cisco.dhruva.sip.servergroups.SG;
import com.cisco.dhruva.util.log.Trace;
import java.util.HashSet;

/*
 * This class holds a set of 5xx response codes.  Any attempt to add a response
 * code outside of this range is ignored.  It also supports indexing of response
 * ranges.
 */

public class DsErrorResponseCodeSet implements Cloneable {

  protected HashSet responseCodes = null;
  protected final int LOWER_RESPONSE_CODE = SG.sgFailoverResponseCodeMin;
  protected final int UPPER_RESPONSE_CODE = SG.sgFailoverResponseCodeMax;
  protected static Trace Log = Trace.getTrace(DsErrorResponseCodeSet.class.getName());

  /*
   * Creates an empty set of error response codes
   */
  public DsErrorResponseCodeSet() {
    this(new HashSet(11));
  }

  private DsErrorResponseCodeSet(HashSet codes) {
    this.responseCodes = codes;
  }

  /*
   * Adds the specified error code to the set if it is a 5xx class error code.
   * Store the code at the specified index.  If there is something already at the
   * given index, then we overwrite it.
   */
  public boolean addErrorCode(Integer code) {

    // Save the code as a string so that the configuration can be viewed later
    return responseCodes.add(code);
  }

  /*
   * Removes the specified index from the error response code set.
   */
  public boolean removeErrorCode(Integer code) {

    return responseCodes.remove(code);
  }

  /*
   * Checks to see if the given code is in the current set.
   * @param code The status code to check on.
   * @returns True if the code is in the current set.
   */
  public boolean isValueInResponseCodeSet(int code) {
    Integer codeInteger = new Integer(code);
    return responseCodes.contains(codeInteger);
  }

  public Object clone() {
    return new DsErrorResponseCodeSet((HashSet) responseCodes.clone());
  }
}
