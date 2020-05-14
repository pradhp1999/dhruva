/*
 * Copyright (c) 2001-2006 by cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.dhruva.sip.proxy;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import java.util.HashMap;

public class ResponseReasonCodeConstants {

  public static final int SUCCESS = 1;

  public static final int REDIRECT = 2;

  public static final int NO_LOCATIONS = 3;

  public static final int ICMP = 4;
  public static final int TIMEOUT = 5;
  public static final int UNREACHABLE = 6;

  public static final int UNKNOWN_SG = 7;
  public static final int DOWN = 8;
  public static final int EMPTY = 9;

  public static final int PROXY_ERROR = 10;
  public static final int CANCELLED = 11;

  public static final int SERVER_GROUP_TIMEOUT = 12;

  public static final int UNDEFINED = 100;

  public static final DsByteString UNREACHABLE_STR = new DsByteString("unreachable");
  public static final DsByteString ICMP_STR = new DsByteString("icmp");
  public static final DsByteString TIMEOUT_STR = new DsByteString("timeout");
  public static final DsByteString DOWN_STR = new DsByteString("down");
  public static final DsByteString UNKNOWN_SG_STR = new DsByteString("unknown sg");
  public static final DsByteString EMPTY_STR = new DsByteString("empty");
  public static final DsByteString SUCCESS_STR = new DsByteString("success");
  public static final DsByteString REDIRECT_STR = new DsByteString("redirect");
  public static final DsByteString PROXY_ERROR_STR = new DsByteString("proxy error");
  public static final DsByteString NO_LOCATIONS_STR = new DsByteString("no locations");
  public static final DsByteString CANCELLED_STR = new DsByteString("cancelled");
  public static final DsByteString UNDEFINED_STR = new DsByteString("undefined");

  public static final HashMap<Integer, DsByteString> INT_TO_STR_MAP = new HashMap<>(23);

  static {
    INT_TO_STR_MAP.put(UNREACHABLE, UNREACHABLE_STR);
    INT_TO_STR_MAP.put(ICMP, ICMP_STR);
    INT_TO_STR_MAP.put(TIMEOUT, TIMEOUT_STR);
    INT_TO_STR_MAP.put(DOWN, DOWN_STR);
    INT_TO_STR_MAP.put(UNKNOWN_SG, UNKNOWN_SG_STR);
    INT_TO_STR_MAP.put(EMPTY, EMPTY_STR);
    INT_TO_STR_MAP.put(SUCCESS, SUCCESS_STR);
    INT_TO_STR_MAP.put(REDIRECT, REDIRECT_STR);
    INT_TO_STR_MAP.put(PROXY_ERROR, PROXY_ERROR_STR);
    INT_TO_STR_MAP.put(NO_LOCATIONS, NO_LOCATIONS_STR);
    INT_TO_STR_MAP.put(CANCELLED, CANCELLED_STR);
    INT_TO_STR_MAP.put(UNDEFINED, UNDEFINED_STR);
    INT_TO_STR_MAP.put(SERVER_GROUP_TIMEOUT, TIMEOUT_STR);
  }

  public static int compareReasonCodes(int code1, int code2) {
    if (code1 < code2) return 1;
    else if (code1 == code2) return 0;
    else return -1;
  }

  public static DsByteString getReasonStrFromInt(int reasonCode) {
    return INT_TO_STR_MAP.get(reasonCode);
  }
}
