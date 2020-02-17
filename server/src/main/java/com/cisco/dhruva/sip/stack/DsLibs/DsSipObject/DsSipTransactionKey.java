// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

/**
 * This class is used along with DsSipParserEventTee to create a key at the same time that we parse
 * an incoming message. A tee splits the events and delivers to both of the downstream listeners.
 *
 * <p>Here's how to use it with DsSipParserEventTee
 *
 * <p>
 *
 * <pre>
 *  DsSipMessageListenerFactory factory = ...;  // this is the factory that builds the
 *                                              // SIP Object
 *
 * DsSipTransactionKey key = new DsSipTransactionKey();    // an empty key
 *
 * DsSipParserEventTee tee = new DsSipParserEventTee(key, factory); // the T
 * DsSipMsgParser.parse(tee, bytes, offset, count);
 *
 * // now the message is built and the key is populated
 * </pre>
 */
public interface DsSipTransactionKey extends Cloneable {
  /**
   * The constant flag to turn on/off the debug traces for the concrete implementations the
   * DsSipTransactionKey.
   */
  public static final boolean DEBUG = false;

  /** Represents No mask value with all bits off that can be used to clear off all the flags. */
  public static final short NONE = (short) 0x0000;

  /**
   * The mask for the top Via header that tells whether the User, Host and Port components of the
   * top Via header should be used as part of the key.
   */
  public static final short USE_VIA = (short) 0x0001;

  /**
   * The mask for the Request URI that tells whether the User, Host and Port components of the
   * Request URI should be used as part of the key.
   */
  public static final short USE_URI = (short) 0x0002;

  /**
   * The mask that tells not to use CSeq method name in the CSeq header as part of the key, if the
   * message type is either ACK or CANCEL.
   */
  public static final short LOOKUP = (short) 0x0004;

  /**
   * The mask that tells to use the source IP address and Port of the incoming request as part of
   * the key.
   */
  public static final short INCOMING = (short) 0x0008;
  /**
   * The mask for the Tag parameter value in the To Header that tells whether the Tag parameter
   * value along with the User, Host and Port components of the To Header should be used as part of
   * the key.
   */
  public static final short USE_TO_TAG = (short) 0x0010;

  /**
   * The mask for the CSeq Method that tells whether the the CSeq Method name in the CSeq Header
   * should be used as part of the key.
   */
  public static final short USE_METHOD = (short) 0x0020;

  /** The mask with all bits set that can be used check which flags are on. */
  static final short MASK = (short) 0xffff;

  /** Constant error message string telling that CSeq Method is invalid. */
  static final String CSEQ_METHOD_INVALID = "Can't build transaction key: no CSeq method";

  /**
   * Constant error message string telling that the Magic Cookie value is invalid in the Via Branch
   * parameter value.
   */
  static final String MAGIC_COOKIE_INVALID = "Can't build transaction key: no Magic Cookie in VIA";

  /**
   * Returns the To Tag value, if present, otherwise returns null.
   *
   * @return the To Tag value, if present, otherwise returns null.
   */
  public DsByteString getToTag();

  /**
   * Returns the CSeq method name, if present, otherwise returns null.
   *
   * @return the CSeq method name, if present, otherwise returns null.
   */
  public DsByteString getCSeqMethod();

  /**
   * Returns the VIA branch value, if present, otherwise returns null.
   *
   * @return the VIA branch value, if present, otherwise returns null.
   */
  public DsByteString getViaBranch();

  /**
   * Sets the specified <code>context</code> as this key's context. A context defines what all
   * components of the SIP message should be considered while composing the transaction key. The
   * various contexts defined in DsSipTransactionKey interface are:
   *
   * <pre>
   * USE_URI      - Use the User, Host and Port components of the Request URI
   *                  as part of the key.
   * USE_VIA      - Use the User, Host and Port components of the top VIA header
   *                  as part of the key.
   * USE_METHOD   - Use the CSeq method name in the CSeq header as part of the
   *                  key.
   * USE_TO_TAG   - Use the Tag parameter value along with the User, Host and
   *                  Port components of the To header as part of the key.
   * LOOKUP       - Don't consider CSeq method name in the CSeq header as part
   *                  of the key, if the message type is either ACK or CANCEL.
   * INCOMING     - Use the source IP address and Port of the incoming request
   *                  as part of the key.
   * </pre>
   *
   * Any combination of these options can be specified to generate the required key. The
   * combinations can be specified by bitwise OR and/or bitwise AND operations (For example, USE_URI
   * | USE_VIA). <br>
   * In case of this transaction key, the valid options are USE_METHOD and INCOMING. Where as the
   * other options can be specified in case of Classic key as defined by {@link
   * DsSipClassicTransactionKey}. <br>
   * By default, the branch parameter value of the top VIA header is considered as part of this
   * default key. These default components will always be considered regardless of the context
   * options specified. The context options will specify the additional key components.
   *
   * @param context the context that needs to be set for this key.
   */
  public void setKeyContext(int context);

  /**
   * Returns a clone of this object.
   *
   * @return a clone of this object.
   */
  public Object clone();

  /**
   * Checks for the validity of the key. If there is an exception while validating the key, that
   * tells the key is not valid and should not be used.
   *
   * @throws DsSipKeyValidationException if there is an exception while validating the key.
   */
  void validate() throws DsSipKeyValidationException;

  /**
   * Sets the sent-by host address of the corresponding message to the specified <code>address
   * </code> byte array. Its the 'host' part in the sent-by component of the top VIA in the
   * corresponding message. This address along with the sent-by port number is used for incoming
   * messages in case of server transaction.
   *
   * @param address the source address of the corresponding message as a byte array.
   */
  public void setSourceAddress(byte[] address);

  /**
   * Sets the sent-by port number of the corresponding message to the specified <code>port</code>
   * number. Its the 'port' number in the sent-by component of the top VIA in the corresponding
   * message. This port number along with the sent-by address is used for incoming messages in case
   * of server transaction.
   *
   * @param port the source port number of the corresponding message.
   */
  public void setSourcePort(int port);

  /**
   * Tells whether the Via parts in this key are equal to that of the specified Key instance <code>
   * other</code>.
   *
   * @param other The key object whose Via components needs to be compared with this objects'.
   * @return <code>true</code> if the Via components are equal in both the keys, <code>false</code>
   *     otherwise.
   */
  public boolean viaEquals(Object other);
}
