package com.cisco.dhruva.sip.cac;

/** Created by IntelliJ IDEA. User: rrachuma Date: 12/9/10 Time: 3:01 PM */
public enum SessionStateType {
  INITIAL(0, "INITIAL"),
  PROVISIONAL(1, "PROVISIONAL"),
  ACCEPTED(2, "ACCEPTED"),
  REDIRECTED(3, "REDIRECTED"),
  FAILED(4, "FAILED"),
  TIMED_OUT(5, "TIMED OUT"),
  TERMINATED(6, "TERMINATED"),
  FORCED_REMOVE(7, "FORCED REMOVE");

  private int stateIndex;
  private String stateStr;

  SessionStateType(int index, String str) {
    stateIndex = index;
    stateStr = str;
  }

  public int getStateAsInt() {
    return stateIndex;
  }

  public String getStateAsStr() {
    return stateStr;
  }
}
