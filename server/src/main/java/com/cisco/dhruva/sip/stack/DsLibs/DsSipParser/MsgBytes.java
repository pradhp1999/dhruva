package com.cisco.dhruva.sip.stack.DsLibs.DsSipParser;

/**
 * Internal wrapper class for holding the current message bytes and the indexes associated with it.
 */
public class MsgBytes {
  /** The byte array that contains the SIP message. */
  public byte msg[];
  /** The start of the SIP message. */
  public int offset;
  /** The number of bytes in the SIP message. */
  public int count;
  /** The current position in the SIP message. */
  public int i;

  /**
   * Constructor.
   *
   * @param msg the byte array that contains the SIP message.
   * @param offset the number of bytes in the SIP message.
   * @param count the number of bytes in the SIP message.
   */
  public MsgBytes(byte msg[], int offset, int count) {
    this.msg = msg;
    this.i = this.offset = offset;
    this.count = count;
  }
}
