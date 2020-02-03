/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

/*
TODO:DHRUVA ,  Bringing empty class as it has stack dependencies

 */
public class DsNetwork implements Cloneable {

  public static final byte NONE = -1;

  public static DsNetwork getDefault() {
    return null;
  }

  public static DsNetwork getNetwork(byte m_network) {
    return null;
  }

  public byte getNumber() {
    return 0;
  }
}
