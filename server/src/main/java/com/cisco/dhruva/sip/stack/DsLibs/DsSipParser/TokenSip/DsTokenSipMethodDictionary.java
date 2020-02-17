// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import java.util.HashMap;

public class DsTokenSipMethodDictionary {
  static final DsByteString[] s_MethodDictionary = {
    new DsByteString("Response"),
    new DsByteString("Unknown"),
    new DsByteString("ACK"),
    new DsByteString("BYE"),
    new DsByteString("CANCEL"),
    new DsByteString("INFO"),
    new DsByteString("INVITE"),
    new DsByteString("MESSAGE"),
    new DsByteString("NOTIFY"),
    new DsByteString("OPTIONS"),
    new DsByteString("PRACK"),
    new DsByteString("PUBLISH"),
    new DsByteString("REFER"),
    new DsByteString("REGISTER"),
    new DsByteString("SUBSCRIBE"),
    new DsByteString("UPDATE")
  };

  public static final int INVALID = 0;
  public static final int UNKNOWN = 1;
  public static final int ACK = 2;
  public static final int BYE = 3;
  public static final int CANCEL = 4;
  public static final int INFO = 5;
  public static final int INVITE = 6;
  public static final int MESSAGE = 7;
  public static final int NOTIFY = 8;
  public static final int OPTIONS = 9;
  public static final int PRACK = 10;
  public static final int PUBLISH = 11;
  public static final int REFER = 12;
  public static final int REGISTER = 13;
  public static final int SUBSCRIBE = 14;
  public static final int UPDATE = 15;

  static final int[] sm_MethodMapping = new int[DsSipConstants.METHOD_NAMES_SIZE];

  static {
    sm_MethodMapping[DsSipConstants.UNKNOWN] = UNKNOWN;
    sm_MethodMapping[DsSipConstants.INVITE] = INVITE;
    sm_MethodMapping[DsSipConstants.ACK] = ACK;
    sm_MethodMapping[DsSipConstants.CANCEL] = CANCEL;
    sm_MethodMapping[DsSipConstants.BYE] = BYE;
    sm_MethodMapping[DsSipConstants.OPTIONS] = OPTIONS;
    sm_MethodMapping[DsSipConstants.REGISTER] = REGISTER;
    sm_MethodMapping[DsSipConstants.PRACK] = PRACK;
    sm_MethodMapping[DsSipConstants.INFO] = INFO;
    sm_MethodMapping[DsSipConstants.SUBSCRIBE] = SUBSCRIBE;
    sm_MethodMapping[DsSipConstants.NOTIFY] = NOTIFY;
    sm_MethodMapping[DsSipConstants.MESSAGE] = MESSAGE;
    sm_MethodMapping[DsSipConstants.REFER] = REFER;
    sm_MethodMapping[DsSipConstants.PING] = UNKNOWN;
    // sm_MethodMapping[DsSipConstants.UPDATE] =   UNKNOWN;
  }

  static final HashMap sm_MethodNameMap = new HashMap();

  static {
    sm_MethodNameMap.put(DsSipConstants.BS_ACK, new Integer(ACK));
    sm_MethodNameMap.put(DsSipConstants.BS_BYE, new Integer(BYE));
    sm_MethodNameMap.put(DsSipConstants.BS_CANCEL, new Integer(CANCEL));
    sm_MethodNameMap.put(DsSipConstants.BS_INFO, new Integer(INFO));
    sm_MethodNameMap.put(DsSipConstants.BS_INVITE, new Integer(INVITE));
    sm_MethodNameMap.put(DsSipConstants.BS_MESSAGE, new Integer(MESSAGE));
    sm_MethodNameMap.put(DsSipConstants.BS_NOTIFY, new Integer(NOTIFY));
    sm_MethodNameMap.put(DsSipConstants.BS_OPTIONS, new Integer(OPTIONS));
    sm_MethodNameMap.put(DsSipConstants.BS_PRACK, new Integer(PRACK));
    sm_MethodNameMap.put(new DsByteString("PUBLISH"), new Integer(PUBLISH));
    sm_MethodNameMap.put(DsSipConstants.BS_REFER, new Integer(REFER));
    sm_MethodNameMap.put(DsSipConstants.BS_REGISTER, new Integer(REGISTER));
    sm_MethodNameMap.put(DsSipConstants.BS_SUBSCRIBE, new Integer(SUBSCRIBE));
    sm_MethodNameMap.put(DsByteString.newInstance("UPDATE"), new Integer(UPDATE));
  }

  static final int s_MethodDictionaryMax = s_MethodDictionary.length;
  static final int s_MethodDictionaryMin = 2;

  /**
   * Retrieves the dictionary entry based on the shortcut index.
   *
   * @param methodIndex The method shortcut ID.
   * @return The dictionary value
   */
  public static final DsByteString getByShortcut(int methodIndex) {
    if (methodIndex >= s_MethodDictionaryMin && methodIndex <= s_MethodDictionaryMax) {
      return s_MethodDictionary[methodIndex];
    } else {
      return null;
    }
  }

  /**
   * Retrieves the static encoding from the method dictionary.
   *
   * @param method The name of the method to retrieve
   * @return The encoding method ID.
   */
  public static final int getEncoding(DsByteString method) {

    Integer index = (Integer) sm_MethodNameMap.get(method);

    if (index == null) {
      return UNKNOWN;
    } else {
      return index.intValue();
    }
  }

  /**
   * Retrieves the static encoding from the method dictionary.
   *
   * @param methodId The canonical ID (DsSipConstants) of the method to retrieve
   * @return The encoding method ID.
   */
  public static final int getEncoding(int methodId) {
    if (methodId > DsSipConstants.METHOD_NAMES_SIZE) {
      return UNKNOWN;
    } else {
      return sm_MethodMapping[methodId];
    }
  }
}
