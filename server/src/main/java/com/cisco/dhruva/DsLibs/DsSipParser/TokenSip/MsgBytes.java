// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipParser.TokenSip;

class MsgBytes {
  /** The byte array that contains the SIP message. */
  byte msg[];
  /** The start of the SIP message. */
  int offset;
  /** The number of bytes in the SIP message. */
  int count;
  /** The current position in the SIP message. */
  int i;

  /**
   * Constructor.
   *
   * @param msg the byte array that contains the SIP message.
   * @param offset the number of bytes in the SIP message.
   * @param count the number of bytes in the SIP message.
   */
  MsgBytes(byte msg[], int offset, int count) {
    this.msg = msg;
    this.i = this.offset = offset;
    this.count = count;
  }
}
