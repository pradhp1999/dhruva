// Copyright (c) 2005-2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;
import org.apache.logging.log4j.Level;

/**
 * This class is used along with DsSipParserEventTee to create a key at the same time that we parse
 * an incoming message. A tee splits the events and delivers to both of the downstream listeners.
 *
 * <p>Here's how to use it with DsSipParserEventTee
 *
 * <p>
 *
 * <pre>
 * DsSipMessageListenerFactory factory = ...;  // this is the factory that builds the
 *                                             // SIP Object
 *
 * DsSipClassicTransactionKey key = new DsSipClassicTransactionKey();    // an empty key
 *
 * DsSipParserEventTee tee = new DsSipParserEventTee(key, factory); // the T
 * DsSipMsgParser.parse(tee, bytes, offset, count);
 *
 * // now the message is built and the key is populated
 * </pre>
 */
public final class DsSipClassicTransactionKey
    implements DsSipTransactionKey,
        DsSipConstants,
        DsSipMessageListener,
        DsSipMessageListenerFactory,
        DsSipHeaderListener,
        DsSipElementListener,
        Serializable {
  // /////////////////////////////////////////////////////
  // ////// Static Data   ////////////////////////////////
  // /////////////////////////////////////////////////////

  // Here I just need something to distinguish btw
  //  to from and request URI, so I'll use
  //  SIP_URL_ID.
  private static final int REQUEST_URI = SIP_URL_ID;

  //  make a copy of the bytes or use the
  //   message bytes
  private static final boolean COPY = true;

  /** Error message string constant. */
  static final String FROM_INVALID =
      "Can't build transaction key: no From data (user/host/port/uridata)";
  /** Error message string constant. */
  static final String TO_INVALID =
      "Can't build transaction key: no To data (user/host/port/uridata)";
  /** Error message string constant. */
  static final String CALLID_INVALID = "Can't build transaction key: no Call-Id";
  /** Error message string constant. */
  static final String CSEQ_INVALID = "Can't build transaction key: no CSeq number";
  /** Error message string constant. */
  static final String VIA_INVALID = "Can't build transaction key: no Via data (host/port/branch)";
  /** Error message string constant. */
  static final String RURI_INVALID =
      "Can't build transaction key: no RURI data (user/host/port/uridata)";

  private static final int UNDEFINED = -47;
  private static int UNIQUIFIER;
  private static final DsByteString UNIQUIFIER_STR;

  static {
    String uniq = DsConfigManager.getProperty(DsConfigManager.PROP_TXN_UNIQUE);
    if (uniq != null) {
      if (DsLog4j.transKeyCat.isEnabled(Level.INFO)) {
        DsLog4j.transKeyCat.log(Level.INFO, "user code set key uniquifier to " + uniq);
      }
      UNIQUIFIER_STR = new DsByteString(uniq);
      UNIQUIFIER = DsSipMsgParser.getHeader(UNIQUIFIER_STR);
    } else {
      UNIQUIFIER_STR = null;
      UNIQUIFIER = UNDEFINED;
    }
  }

  private static ThreadLocal tlKeyBuffer = new ThreadLocal();

  private static ByteArrayOutputStream getBuffer() {
    ByteArrayOutputStream str = (ByteArrayOutputStream) tlKeyBuffer.get();
    if (str == null) {
      str = new ByteArrayOutputStream(160); // big enough for a key
      //  it will grow if needed
      tlKeyBuffer.set(str);
    }
    return str;
  }

  // /////////// key data

  /** To Tag index. */
  protected static final int TO_TAG = 0;
  /** To User index. */
  protected static final int TO_USER = 2;
  /** Offset index of the basic elements. */
  protected static final int K_BASE_OFFSET = TO_USER;
  /** To Host index. */
  protected static final int TO_HOST = 4;
  /** To Port index. */
  protected static final int TO_PORT = 6;
  /** From User index. */
  protected static final int FROM_USER = 8;
  /** From Host index. */
  protected static final int FROM_HOST = 10;
  /** From Port index. */
  protected static final int FROM_PORT = 12;
  /** From Port index. */
  protected static final int FROM_TAG = 14;
  /** Call ID index. */
  protected static final int CALL_ID = 16;
  /** CSeq Number index. */
  protected static final int CSEQ_NUMBER = 18;

  /** Basic elements index count. */
  protected static final int K_BASE_COUNT = 20;

  /** Via Host index. */
  protected static final int VIA_HOST = 20;
  /** Via offset index. */
  protected static final int K_VIA_OFFSET = VIA_HOST;
  /** Via Port index. */
  protected static final int VIA_PORT = 22;
  /** Via Branch index. */
  protected static final int VIA_BRANCH = 24;

  /** Via index count. */
  protected static final int K_VIA_COUNT = 6;

  /** Request URI User index. */
  protected static final int RURI_USER = 26;
  /** Request URI Offset index. */
  protected static final int K_RURI_OFFSET = RURI_USER;
  /** Request URI Host index. */
  protected static final int RURI_HOST = 28;
  /** Request URI Port index. */
  protected static final int RURI_PORT = 30;

  /** Request URI index count. */
  protected static final int K_RURI_COUNT = 6;

  /** Key Uniquifier index. */
  protected static final int UNIQ = 32;

  // /////////// end key data

  /** CSeq Method index. */
  protected static final int CSEQ_METHOD = 34;

  /** Number of indices. */
  protected static final int N_POINTERS = 36;

  /** Prime multiplier to be used for hash code generation. */
  private static final int MULT = 31;

  // /////////////////////////////////////////////////////
  // ////// Instance Data   //////////////////////////////
  // /////////////////////////////////////////////////////

  // //////////////////////////////////////////////////////////////////////

  private byte[] m_messageBytes; // 4
  private int[] m_keyData = new int[N_POINTERS]; // 68 + 4
  private boolean m_keyURI = true; // 1/8
  private boolean m_keyToTag = false; // 1/8
  private boolean m_keyVia = true; // 1/8
  private boolean m_isRequest = true; // 1/8
  private boolean m_incoming = false; // 1/8
  private boolean m_isLookup = false; // 1/8
  private int m_current; // 2
  private int m_hashCode; // 4
  private byte m_method; // 1
  // //////////////////////////////////////////////////////////////////////

  /**
   * Returns a clone of this object.
   *
   * @return a clone of this object.
   */
  public Object clone() {
    Object cl = null;
    try {
      cl = super.clone();
    } catch (CloneNotSupportedException ex) {
    }
    return cl;
  }

  /**
   * Returns the key uniquifier, if present in this key, otherwise returns null. Note: The returned
   * DsByteString object is constructed on every invocation of this method.
   *
   * @return the key uniquifier, if present in this key, otherwise returns <code>null</code>.
   */
  public DsByteString getUniquifier() {
    if (m_keyData[UNIQ] == 0) return null;
    return new DsByteString(m_messageBytes, m_keyData[UNIQ], m_keyData[UNIQ + 1]);
  }

  /**
   * Returns the To Tag value, if found while constructing this key, otherwise returns null. Note:
   * The returned DsByteString object is constructed on every invocation of this method.
   *
   * @return the To Tag value, if found while constructing this key, otherwise returns <code>null
   *     </code>.
   */
  public DsByteString getToTag() {
    if (m_keyData[TO_TAG] == 0) return null;
    return new DsByteString(m_messageBytes, m_keyData[TO_TAG], m_keyData[TO_TAG + 1]);
  }

  /**
   * Returns the To User value, if found while constructing this key, otherwise returns null. Note:
   * The returned DsByteString object is constructed on every invocation of this method.
   *
   * @return the To User value, if found while constructing this key, otherwise returns <code>null
   *     </code>.
   */
  public DsByteString getToUser() {
    if (m_keyData[TO_USER] == 0) return null;
    return new DsByteString(m_messageBytes, m_keyData[TO_USER], m_keyData[TO_USER + 1]);
  }

  /**
   * Returns the To Host value, if found while constructing this key, otherwise returns null. Note:
   * The returned DsByteString object is constructed on every invocation of this method.
   *
   * @return the To Host value, if found while constructing this key, otherwise returns <code>null
   *     </code>.
   */
  public DsByteString getToHost() {
    if (m_keyData[TO_HOST] == 0) return null;
    return new DsByteString(m_messageBytes, m_keyData[TO_HOST], m_keyData[TO_HOST + 1]);
  }

  /**
   * Returns the To Port value, if found while constructing this key, otherwise returns null. Note:
   * The returned DsByteString object is constructed on every invocation of this method.
   *
   * @return the To Port value, if found while constructing this key, otherwise returns <code>null
   *     </code>.
   */
  public DsByteString getToPort() {
    if (m_keyData[TO_PORT] == 0) return null;
    return new DsByteString(m_messageBytes, m_keyData[TO_PORT], m_keyData[TO_PORT + 1]);
  }

  /**
   * Returns the From User value, if found while constructing this key, otherwise returns null.
   * Note: The returned DsByteString object is constructed on every invocation of this method.
   *
   * @return the From User value, if found while constructing this key, otherwise returns <code>null
   *     </code>.
   */
  public DsByteString getFromUser() {
    if (m_keyData[FROM_USER] == 0) return null;
    return new DsByteString(m_messageBytes, m_keyData[FROM_USER], m_keyData[FROM_USER + 1]);
  }

  /**
   * Returns the From Host value, if found while constructing this key, otherwise returns null.
   * Note: The returned DsByteString object is constructed on every invocation of this method.
   *
   * @return the From Host value, if found while constructing this key, otherwise returns <code>null
   *     </code>.
   */
  public DsByteString getFromHost() {
    if (m_keyData[FROM_HOST] == 0) return null;
    return new DsByteString(m_messageBytes, m_keyData[FROM_HOST], m_keyData[FROM_HOST + 1]);
  }

  /**
   * Returns the From Port value, if found while constructing this key, otherwise returns null.
   * Note: The returned DsByteString object is constructed on every invocation of this method.
   *
   * @return the From Port value, if found while constructing this key, otherwise returns <code>null
   *     </code>.
   */
  public DsByteString getFromPort() {
    if (m_keyData[FROM_PORT] == 0) return null;
    return new DsByteString(m_messageBytes, m_keyData[FROM_PORT], m_keyData[FROM_PORT + 1]);
  }

  /**
   * Returns the CallID value, if found while constructing this key, otherwise returns null. Note:
   * The returned DsByteString object is constructed on every invocation of this method.
   *
   * @return the CallID value, if found while constructing this key, otherwise returns <code>null
   *     </code>.
   */
  public DsByteString getCallId() {
    if (m_keyData[CALL_ID] == 0) return null;
    return new DsByteString(m_messageBytes, m_keyData[CALL_ID], m_keyData[CALL_ID + 1]);
  }

  /**
   * Returns the CSeq Number value, if found while constructing this key, otherwise returns null.
   * Note: The returned DsByteString object is constructed on every invocation of this method.
   *
   * @return the CSeq Number value, if found while constructing this key, otherwise returns <code>
   *     null</code>.
   */
  public DsByteString getCSeqNumber() {
    if (m_keyData[CSEQ_NUMBER] == 0) return null;
    return new DsByteString(m_messageBytes, m_keyData[CSEQ_NUMBER], m_keyData[CSEQ_NUMBER + 1]);
  }

  /**
   * Returns the CSeq Method value, if found while constructing this key, otherwise returns null.
   * Note: The returned DsByteString object is constructed on every invocation of this method.
   *
   * @return the CSeq Method value, if found while constructing this key, otherwise returns <code>
   *     null</code>.
   */
  public DsByteString getCSeqMethod() {
    if (m_keyData[CSEQ_METHOD] == 0) return null;
    return new DsByteString(m_messageBytes, m_keyData[CSEQ_METHOD], m_keyData[CSEQ_METHOD + 1]);
  }

  /**
   * Returns the Via Host value, if found while constructing this key, otherwise returns null. Note:
   * The returned DsByteString object is constructed on every invocation of this method.
   *
   * @return the Via Host value, if found while constructing this key, otherwise returns <code>null
   *     </code>.
   */
  public DsByteString getViaHost() {
    if (m_keyData[VIA_HOST] == 0) return null;
    return new DsByteString(m_messageBytes, m_keyData[VIA_HOST], m_keyData[VIA_HOST + 1]);
  }

  /**
   * Returns the Via Port value, if found while constructing this key, otherwise returns null. Note:
   * The returned DsByteString object is constructed on every invocation of this method.
   *
   * @return the Via Port value, if found while constructing this key, otherwise returns <code>null
   *     </code>.
   */
  public DsByteString getViaPort() {
    if (m_keyData[VIA_PORT] == 0) return null;
    return new DsByteString(m_messageBytes, m_keyData[VIA_PORT], m_keyData[VIA_PORT + 1]);
  }

  /**
   * Returns the Via Branch value, if found while constructing this key, otherwise returns null.
   * Note: The returned DsByteString object is constructed on every invocation of this method.
   *
   * @return the Via Branch value, if found while constructing this key, otherwise returns <code>
   *     null</code>.
   */
  public DsByteString getViaBranch() {
    if (m_keyData[VIA_BRANCH] == 0) return null;
    return new DsByteString(m_messageBytes, m_keyData[VIA_BRANCH], m_keyData[VIA_BRANCH + 1]);
  }

  /**
   * Returns the Request URI User value, if found while constructing this key, otherwise returns
   * null. Note: The returned DsByteString object is constructed on every invocation of this method.
   *
   * @return the Request URI User value, if found while constructing this key, otherwise returns
   *     <code>null</code>.
   */
  public DsByteString getURIUser() {
    if (m_keyData[RURI_USER] == 0) return null;
    return new DsByteString(m_messageBytes, m_keyData[RURI_USER], m_keyData[RURI_USER + 1]);
  }

  /**
   * Returns the Request URI Host value, if found while constructing this key, otherwise returns
   * null. Note: The returned DsByteString object is constructed on every invocation of this method.
   *
   * @return the Request URI Host value, if found while constructing this key, otherwise returns
   *     <code>null</code>.
   */
  public DsByteString getURIHost() {
    if (m_keyData[RURI_HOST] == 0) return null;
    return new DsByteString(m_messageBytes, m_keyData[RURI_HOST], m_keyData[RURI_HOST + 1]);
  }

  /**
   * Returns the Request URI Port value, if found while constructing this key, otherwise returns
   * null.
   *
   * <p>Note: The returned DsByteString object is constructed on every invocation of this method.
   *
   * @return the Request URI Port value, if found while constructing this key, otherwise returns
   *     <code>null</code>.
   */
  public DsByteString getURIPort() {
    if (m_keyData[RURI_PORT] == 0) return null;
    return new DsByteString(m_messageBytes, m_keyData[RURI_PORT], m_keyData[RURI_PORT + 1]);
  }

  /**
   * Tells whether the Via parts in this key are equal to that of the specified classic Key instance
   * <code>other</code>.
   *
   * @param other The classic key object whose Via components needs to be compared with this
   *     objects'.
   * @return <code>true</code> if the Via components are equal in both the keys, <code>false</code>
   *     otherwise.
   */
  public boolean viaEquals(Object other) {
    if (this == other) return true;

    int offset0 = 0, len0 = 0, offset1 = 0, len1 = 0;

    DsSipClassicTransactionKey comparator = (DsSipClassicTransactionKey) other;
    for (int i = K_VIA_OFFSET; i < K_VIA_OFFSET + K_VIA_COUNT; i += 2) {
      offset0 = (int) m_keyData[i];
      len0 = (int) m_keyData[i + 1];
      offset1 = (int) comparator.m_keyData[i];
      len1 = (int) comparator.m_keyData[i + 1];
      if ((len0 == 0 && len1 == 0)
          || ((i == VIA_PORT)
              && ((len0 == 0 || isDefault(m_messageBytes, offset0, len0))
                  && (len1 == 0 || isDefault(comparator.m_messageBytes, offset1, len1))))) {
        continue;
      }
      if (len0 != len1) {
        return false;
      }
      for (int j = 0; j < len0; ++j) {
        if (m_messageBytes[offset0 + j] != comparator.m_messageBytes[offset1 + j]) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Returns the string representation of the various components contained in this key. This is
   * useful for debugging purposes.
   *
   * @return the string representation of the various components contained in this key.
   */
  public String dump() {
    return "m_keyURI :   "
        + m_keyURI
        + "\n"
        + "m_keyToTag : "
        + m_keyToTag
        + "\n"
        + "m_keyVia :   "
        + m_keyVia
        + "\n"
        + "m_isRequest :"
        + m_isRequest
        + "\n"
        + "m_incoming : "
        + m_incoming
        + "\n"
        + "m_isLookup : "
        + m_isLookup
        + "\n"
        + "ToTag:       "
        + getToTag()
        + "\n"
        + "ToUser:      "
        + getToUser()
        + "\n"
        + "ToHost:      "
        + getToHost()
        + "\n"
        + "ToPort:      "
        + getToPort()
        + "\n"
        + "FromUser:    "
        + getFromUser()
        + "\n"
        + "FromHost:    "
        + getFromHost()
        + "\n"
        + "FromPort:    "
        + getFromPort()
        + "\n"
        + "CallId:      "
        + getCallId()
        + "\n"
        + "CSeqNumber:  "
        + getCSeqNumber()
        + "\n"
        + "CSeqMethod:  "
        + getCSeqMethod()
        + "\n"
        + "ViaHost:     "
        + getViaHost()
        + "\n"
        + "ViaPort:     "
        + getViaPort()
        + "\n"
        + "ViaBranch:   "
        + getViaBranch()
        + "\n"
        + "URIUser:     "
        + getURIUser()
        + "\n"
        + "URIHost:     "
        + getURIHost()
        + "\n"
        + "URIPort:     "
        + getURIPort()
        + "\n"
        + "Uniquifier ("
        + UNIQUIFIER_STR
        + "): "
        + getUniquifier()
        + "\n";
  }

  /**
   * Compares this key with the specified key <code>other</code> and tells whether both the keys are
   * equivalent.
   *
   * @param other the other key that needs to be compared with this key for equality check.
   * @return <code>true</code> if both the keys are equivalent, <code>false</code> otherwise.
   */
  public boolean equals(Object other) {
    if (other == null) return false;
    if (this == other) return true;

    int offset0 = 0, len0 = 0, offset1 = 0, len1 = 0;

    DsSipClassicTransactionKey comparator = (DsSipClassicTransactionKey) other;
    for (int i = m_keyToTag ? TO_TAG : K_BASE_OFFSET; i < K_BASE_COUNT; i += 2) {
      offset0 = (int) m_keyData[i];
      len0 = (int) m_keyData[i + 1];
      offset1 = (int) comparator.m_keyData[i];
      len1 = (int) comparator.m_keyData[i + 1];

      if ((len0 == 0 && len1 == 0)
          || ((i == TO_PORT || i == FROM_PORT)
              && ((len0 == 0 || isDefault(m_messageBytes, offset0, len0))
                  && (len1 == 0 || isDefault(comparator.m_messageBytes, offset1, len1))))) {
        continue;
      }

      if (len0 != len1) {
        return false;
      }
      for (int j = 0; j < len0; ++j) {
        if (m_messageBytes[offset0 + j] != comparator.m_messageBytes[offset1 + j]) {
          return false;
        }
      }
    }

    if (m_keyVia) {
      for (int i = K_VIA_OFFSET; i < K_VIA_OFFSET + K_VIA_COUNT; i += 2) {
        offset0 = (int) m_keyData[i];
        len0 = (int) m_keyData[i + 1];
        offset1 = (int) comparator.m_keyData[i];
        len1 = (int) comparator.m_keyData[i + 1];
        if ((len0 == 0 && len1 == 0)
            || ((i == VIA_PORT)
                && ((len0 == 0 || isDefault(m_messageBytes, offset0, len0))
                    && (len1 == 0 || isDefault(comparator.m_messageBytes, offset1, len1))))) {

          continue;
        }
        if (len0 != len1) {
          return false;
        }
        for (int j = 0; j < len0; ++j) {
          if (m_messageBytes[offset0 + j] != comparator.m_messageBytes[offset1 + j]) {
            return false;
          }
        }
      }
    }

    if (m_keyURI) {
      for (int i = K_RURI_OFFSET; i < K_RURI_OFFSET + K_RURI_COUNT; i += 2) {
        offset0 = (int) m_keyData[i];
        len0 = (int) m_keyData[i + 1];
        offset1 = (int) comparator.m_keyData[i];
        len1 = (int) comparator.m_keyData[i + 1];
        if ((len0 == 0 && len1 == 0)
            || ((i == RURI_PORT)
                && ((len0 == 0 || isDefault(m_messageBytes, offset0, len0))
                    && (len1 == 0 || isDefault(comparator.m_messageBytes, offset1, len1))))) {
          continue;
        }
        if (len0 != len1) {
          return false;
        }
        for (int j = 0; j < len0; ++j) {
          if (m_messageBytes[offset0 + j] != comparator.m_messageBytes[offset1 + j]) {
            return false;
          }
        }
      }
    }

    if (m_incoming && (UNIQUIFIER != UNDEFINED)) {
      offset0 = (int) m_keyData[UNIQ];
      len0 = (int) m_keyData[UNIQ + 1];
      offset1 = (int) comparator.m_keyData[UNIQ];
      len1 = (int) comparator.m_keyData[UNIQ + 1];
      if (len0 != len1) {
        return false;
      }
      for (int j = 0; j < len0; ++j) {
        if (m_messageBytes[offset0 + j] != comparator.m_messageBytes[offset1 + j]) {
          return false;
        }
      }
    }

    if (!m_isLookup) {
      // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
      if (m_method == ACK || m_method == CANCEL || m_method == PRACK) {
        if (m_method != comparator.m_method) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Returns the hash code for this key.
   *
   * @return The hash code for this key.
   */
  public int hashCode() {
    int hash = m_hashCode;
    if (hash == 0) {
      int i = m_keyToTag ? TO_TAG : K_BASE_OFFSET;
      int offset, len;
      for (; i < K_BASE_COUNT; i += 2) {
        offset = (int) m_keyData[i];
        len = (int) m_keyData[i + 1];
        if (len == 0
            || ((i == TO_PORT || i == FROM_PORT) && isDefault(m_messageBytes, offset, len))) {
          continue;
        }
        for (int j = offset; j < offset + len; ++j) {
          hash = MULT * hash + m_messageBytes[j];
        }
      }
      if (m_keyVia) {
        // via host
        i = K_VIA_OFFSET;
        for (i = K_VIA_OFFSET; i < K_VIA_OFFSET + K_VIA_COUNT; i += 2) {
          offset = (int) m_keyData[i];
          len = (int) m_keyData[i + 1];
          if (len == 0 || (i == VIA_PORT && isDefault(m_messageBytes, offset, len))) {
            continue;
          }
          for (int j = offset; j < offset + len; ++j) {
            hash = MULT * hash + m_messageBytes[j];
          }
        }
      }

      if (m_keyURI) {
        for (i = K_RURI_OFFSET; i < K_RURI_OFFSET + K_RURI_COUNT; i += 2) {
          offset = (int) m_keyData[i];
          len = (int) m_keyData[i + 1];
          if (len == 0 || (i == RURI_PORT && isDefault(m_messageBytes, offset, len))) {
            continue;
          }
          for (int j = offset; j < offset + len; ++j) {
            hash = MULT * hash + m_messageBytes[j];
          }
        }
      }

      if (m_incoming) {
        offset = (int) m_keyData[UNIQ];
        len = (int) m_keyData[UNIQ + 1];
        for (int j = offset; j < offset + len; ++j) {
          hash = MULT * hash + m_messageBytes[j];
        }
      }

      if (!m_isLookup) {
        // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
        if (m_method == ACK || m_method == CANCEL || m_method == PRACK) {
          hash = MULT * hash + m_method;
        }
      }
      m_hashCode = hash;
    }

    return m_hashCode;
  }

  /**
   * Returns a readable string representation of this key.
   *
   * @return a readable string representation of this key.
   */
  public String toString() {
    // this could be done a lot more optimally, but I am not sure that it matters - jsm

    String ret_value = "";

    for (int i = m_keyToTag ? TO_TAG : K_BASE_OFFSET; i < K_BASE_COUNT; i += 2) {
      ret_value += DsByteString.newString(m_messageBytes, m_keyData[i], m_keyData[i + 1]) + ",";
    }
    if (m_keyVia) {
      for (int i = K_VIA_OFFSET; i < K_VIA_OFFSET + K_VIA_COUNT; i += 2) {
        ret_value += DsByteString.newString(m_messageBytes, m_keyData[i], m_keyData[i + 1]) + ",";
      }
    }

    if (m_keyURI) {
      for (int i = K_RURI_OFFSET; i < K_RURI_OFFSET + K_RURI_COUNT; i += 2) {
        ret_value += DsByteString.newString(m_messageBytes, m_keyData[i], m_keyData[i + 1]) + ",";
      }
    }

    if (m_incoming) {
      ret_value +=
          DsByteString.newString(m_messageBytes, m_keyData[UNIQ], m_keyData[UNIQ + 1]) + ",";
    }

    if (!m_isLookup) {
      // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
      if (m_method == ACK || m_method == CANCEL || m_method == PRACK) {
        ret_value += Byte.toString(m_method);
      }
    }
    return ret_value;
  }

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
   * In case of this transaction key, the valid options are USE_URI, USE_VIA, USE_TO_TAG and LOOKUP.
   * Where as the other options can be specified in case of default key as defined by {@link
   * DsSipDefaultTransactionKey}. <br>
   * By default, the User, Host and Port components of the top To and From headers, the value of
   * Call-ID header and the CSeq number in the CSeq header are considered as part of this classic
   * key. These default components will always be considered regardless of the context options
   * specified. The context options will specify the additional key components.
   *
   * @param context the context that needs to be set for this key.
   */
  public void setKeyContext(int context) {
    boolean useVia = (USE_VIA & context) > 0,
        useURI = (USE_URI & context) > 0,
        isLookup = (LOOKUP & context) > 0,
        toTag = (USE_TO_TAG & context) > 0,
        incoming = (INCOMING & context) > 0;

    if (m_keyVia == useVia
        && m_keyURI == useURI
        && m_isLookup == isLookup
        && m_keyToTag == toTag
        && m_incoming == incoming) {
      return;
    }
    m_keyVia = useVia;
    m_keyURI = useURI;
    m_isLookup = isLookup;
    m_keyToTag = toTag;
    m_incoming = incoming;
    m_hashCode = 0;
  }
  //
  //    public int getKeyContext()
  //    {
  //        int context = 0;
  //        if(m_keyVia)   context |= USE_VIA;
  //        if(m_keyURI)   context |= USE_URI;
  //        if(m_isLookup) context |= LOOKUP;
  //        if(m_keyToTag) context |= USE_TO_TAG;
  //        if(m_incoming) context |= INCOMING;
  //        return context;
  //    }
  //

  /**
   * Sets the sent-by host address of the corresponding message to the specified <code>address
   * </code> byte array. Its the 'host' part in the sent-by component of the top VIA in the
   * corresponding message. This address along with the sent-by port number is used for incoming
   * messages in case of server transaction. Note: In this case it is nope operation as its not
   * required in the Classic key.
   *
   * @param address the source address of the corresponding message as a byte array.
   */
  public void setSourceAddress(byte[] address) {}
  /**
   * Sets the sent-by port number of the corresponding message to the specified <code>port</code>
   * number. Its the 'port' number in the sent-by component of the top VIA in the corresponding
   * message. This port number along with the sent-by address is used for incoming messages in case
   * of server transaction. Note: In this case it is nope operation as its not required in the
   * Classic key.
   *
   * @param port the source port number of the corresponding message.
   */
  public void setSourcePort(int port) {}

  // /////////////////////////////////////////////////////////////////////////////////
  // /////////////////////  Listener Interfaces   ////////////////////////////////////
  // /////////////////////////////////////////////////////////////////////////////////

  /*
   * javadoc inherited
   */
  public final DsSipElementListener headerBegin(int headerId) {
    if (DEBUG) {
      System.out.println("headerBegin = [" + DsSipMsgParser.HEADER_NAMES[headerId] + "]");
      System.out.println();
    }

    m_current = headerId;
    switch (headerId) {
      case VIA:
        if (m_keyData[K_VIA_OFFSET + 1] == 0) {
          return this;
        }
        break;
      case TO:
      case FROM:
      case DsSipConstants.CALL_ID:
      case CSEQ:
        return this;
    }
    return null;
  }

  /*
   * javadoc inherited
   */
  public final DsSipElementListener requestURIBegin(
      byte[] buffer, int schemeOffset, int schemeCount) {
    if (DEBUG) {
      System.out.println(
          "requestURIBegin - scheme = ["
              + DsByteString.newString(buffer, schemeOffset, schemeCount)
              + "]");
      System.out.println();
    }

    m_current = REQUEST_URI;
    return this;
  }

  /*
   * javadoc inherited
   */
  public void requestURIFound(byte[] buffer, int offset, int count, boolean valid) {}

  /*
   * javadoc inherited
   */
  public void protocolFound(
      byte[] buffer,
      int protocolOffset,
      int protocolCount,
      int majorOffset,
      int majorCount,
      int minorOffset,
      int minorCount,
      boolean valid)
      throws DsSipParserListenerException {}

  /*
   * javadoc inherited
   */
  public final DsSipMessageListener requestBegin(
      byte[] buffer, int methodOffset, int methodCount, boolean isEncoded) {
    if (DEBUG) {
      System.out.println(
          "requestBegin - method = ["
              + DsByteString.newString(buffer, methodOffset, methodCount)
              + "]");
      System.out.println();
    }

    if (COPY) getBuffer().reset();
    if (COPY) getBuffer().write(0); // dummy byte since offset 0 means unset to us
    m_isRequest = true;
    m_messageBytes = COPY ? null : buffer;
    return this;
  }

  /*
   * javadoc inherited
   */
  public final DsSipMessageListener responseBegin(
      byte[] buffer, int code, int reasonOffset, int reasonCount, boolean isEncoded) {
    if (DEBUG) {
      System.out.println(
          "responseBegin - reason = ["
              + DsByteString.newString(buffer, reasonOffset, reasonCount)
              + "]");
      System.out.println("responseBegin code = [" + code + "]");
      System.out.println();
    }

    if (COPY) getBuffer().reset();
    if (COPY) getBuffer().write(0); // dummy byte since offset 0 means unset to us
    m_isRequest = false;
    m_keyURI = false;
    m_messageBytes = COPY ? null : buffer;
    return this;
  }

  /*
   * javadoc inherited
   */
  public final DsSipElementListener elementBegin(int contextId, int elementId) {

    if (DEBUG) {
      System.out.println(
          "elementBegin - contextId = ["
              + contextId
              + "]["
              + DsSipMsgParser.HEADER_NAMES[contextId]
              + "]");
      System.out.println(
          "elementBegin - elementId = ["
              + elementId
              + "]["
              + DsSipMsgParser.ELEMENT_NAMES[elementId]
              + "]");
      System.out.println();
    }

    switch (contextId) {
      case SIP_URL_ID:
      case UNKNOWN_URL_ID:
        return this;
      case TO:
      case FROM:
        switch (elementId) {
          case UNKNOWN_URL:
          case SIP_URL:
          case HTTP_URL:
          case TEL_URL:
            return this;
        }
    }
    return null;
  }

  /*
   * javadoc inherited
   */
  public final void elementFound(
      int contextId, int elementId, byte[] buffer, int offset, int count, boolean valid) {
    if (DEBUG) {
      System.out.println(
          "elementFound - contextId = ["
              + contextId
              + "]["
              + DsSipMsgParser.HEADER_NAMES[contextId]
              + "]");
      System.out.println(
          "elementFound - elementId = ["
              + elementId
              + "]["
              + DsSipMsgParser.ELEMENT_NAMES[elementId]
              + "]");
      System.out.println("elementFound - value [offset, count] = [" + offset + ", " + count + "]");
      System.out.println(
          "elementFound - value = [" + DsByteString.newString(buffer, offset, count) + "]");
      System.out.println();
    }

    switch (contextId) {
      case DsSipConstants.CALL_ID:
        m_keyData[CALL_ID] = COPY ? getBuffer().size() : offset;
        m_keyData[CALL_ID + 1] = count;
        if (COPY) getBuffer().write(buffer, offset, count);
        break;
      case (CSEQ):
        switch (elementId) {
          case DsSipConstants.CSEQ_NUMBER:
            m_keyData[CSEQ_NUMBER] = COPY ? getBuffer().size() : offset;
            m_keyData[CSEQ_NUMBER + 1] = count;
            if (COPY) getBuffer().write(buffer, offset, count);
            break;
          case DsSipConstants.CSEQ_METHOD:
            m_keyData[CSEQ_METHOD] = COPY ? getBuffer().size() : offset;
            m_keyData[CSEQ_METHOD + 1] = count;
            if (COPY) getBuffer().write(buffer, offset, count);
            m_method = (byte) DsSipMsgParser.getMethod(new DsByteString(buffer, offset, count));
            break;
        }
        break;
      case UNKNOWN_URL_ID:
        switch (m_current) {
          case REQUEST_URI:
            if (elementId == URI_DATA) {
              m_keyData[RURI_USER] = COPY ? getBuffer().size() : offset;
              m_keyData[RURI_USER + 1] = count;
              if (COPY) getBuffer().write(buffer, offset, count);
            }
            break;
          case TO:
            if (elementId == URI_DATA) {
              m_keyData[TO_USER] = COPY ? getBuffer().size() : offset;
              m_keyData[TO_USER + 1] = count;
              if (COPY) getBuffer().write(buffer, offset, count);
            }
            break;
          case FROM:
            if (elementId == URI_DATA) {
              m_keyData[FROM_USER] = COPY ? getBuffer().size() : offset;
              m_keyData[FROM_USER + 1] = count;
              if (COPY) getBuffer().write(buffer, offset, count);
            }
            break;
        }
        break;
      case TEL_URL_ID:
        switch (m_current) {
          case REQUEST_URI:
            if (elementId == TEL_URL_NUMBER) {
              m_keyData[RURI_USER] = COPY ? getBuffer().size() : offset;
              m_keyData[RURI_USER + 1] = count;
              if (COPY) getBuffer().write(buffer, offset, count);
            }
            break;
          case TO:
            if (elementId == TEL_URL_NUMBER) {
              m_keyData[TO_USER] = COPY ? getBuffer().size() : offset;
              m_keyData[TO_USER + 1] = count;
              if (COPY) getBuffer().write(buffer, offset, count);
            }
            break;
          case FROM:
            if (elementId == TEL_URL_NUMBER) {
              m_keyData[FROM_USER] = COPY ? getBuffer().size() : offset;
              m_keyData[FROM_USER + 1] = count;
              if (COPY) getBuffer().write(buffer, offset, count);
            }
            break;
        }
        break;
      case SIP_URL_ID:
        switch (m_current) {
          case REQUEST_URI:
            switch (elementId) {
              case USERNAME:
                m_keyData[RURI_USER] = COPY ? getBuffer().size() : offset;
                m_keyData[RURI_USER + 1] = count;
                if (COPY) getBuffer().write(buffer, offset, count);
                break;
              case HOST:
                m_keyData[RURI_HOST] = COPY ? getBuffer().size() : offset;
                m_keyData[RURI_HOST + 1] = count;
                if (COPY) getBuffer().write(buffer, offset, count);
                break;
              case PORT:
                m_keyData[RURI_PORT] = COPY ? getBuffer().size() : offset;
                m_keyData[RURI_PORT + 1] = count;
                if (COPY) getBuffer().write(buffer, offset, count);
                break;
            }
            break;
          case TO:
            switch (elementId) {
              case USERNAME:
                m_keyData[TO_USER] = COPY ? getBuffer().size() : offset;
                m_keyData[TO_USER + 1] = count;
                if (COPY) getBuffer().write(buffer, offset, count);
                break;
              case HOST:
                m_keyData[TO_HOST] = COPY ? getBuffer().size() : offset;
                m_keyData[TO_HOST + 1] = count;
                if (COPY) getBuffer().write(buffer, offset, count);
                break;
              case PORT:
                m_keyData[TO_PORT] = COPY ? getBuffer().size() : offset;
                m_keyData[TO_PORT + 1] = count;
                if (COPY) getBuffer().write(buffer, offset, count);
                break;
            }
            break;
          case FROM:
            switch (elementId) {
              case USERNAME:
                m_keyData[FROM_USER] = COPY ? getBuffer().size() : offset;
                m_keyData[FROM_USER + 1] = count;
                if (COPY) getBuffer().write(buffer, offset, count);
                break;
              case HOST:
                m_keyData[FROM_HOST] = COPY ? getBuffer().size() : offset;
                m_keyData[FROM_HOST + 1] = count;
                if (COPY) getBuffer().write(buffer, offset, count);
                break;
              case PORT:
                m_keyData[FROM_PORT] = COPY ? getBuffer().size() : offset;
                m_keyData[FROM_PORT + 1] = count;
                if (COPY) getBuffer().write(buffer, offset, count);
                break;
            }
            break;
        }
        break;
      case VIA:
        switch (elementId) {
          case PORT:
            m_keyData[VIA_PORT] = COPY ? getBuffer().size() : offset;
            m_keyData[VIA_PORT + 1] = count;
            if (COPY) getBuffer().write(buffer, offset, count);
            break;
          case HOST:
            m_keyData[VIA_HOST] = COPY ? getBuffer().size() : offset;
            m_keyData[VIA_HOST + 1] = count;
            if (COPY) getBuffer().write(buffer, offset, count);
            break;
        }
        break;
    }
  }

  /*
   * javadoc inherited
   */
  public void unknownFound(
      byte[] buffer,
      int nameOffset,
      int nameCount,
      int valueOffset,
      int valueCount,
      boolean isValid)
      throws DsSipParserListenerException {
    if ((UNIQUIFIER == UNKNOWN_HEADER)
        && (UNIQUIFIER_STR != null)
        && (UNIQUIFIER_STR.equals(buffer, nameOffset, nameCount))) {
      m_keyData[UNIQ] = COPY ? getBuffer().size() : valueOffset;
      m_keyData[UNIQ + 1] = valueCount;
      if (COPY) getBuffer().write(buffer, valueOffset, valueCount);
    }
  }

  /*
   * javadoc inherited
   */
  public void parameterFound(
      int contextId, byte[] buffer, int nameOffset, int nameCount, int valueOffset, int valueCount)
      throws DsSipParserListenerException {
    switch (contextId) {
      case VIA:
        {
          DsByteString value = DsByteString.newLower(buffer, nameOffset, nameCount);
          if (value == BS_BRANCH) {
            m_keyData[VIA_BRANCH] = COPY ? getBuffer().size() : valueOffset;
            m_keyData[VIA_BRANCH + 1] = valueCount;
            if (COPY) getBuffer().write(buffer, valueOffset, valueCount);
          }
        }
        break;
      case (TO):
        {
          DsByteString value = DsByteString.newLower(buffer, nameOffset, nameCount);
          if (value == BS_TAG) {
            m_keyData[TO_TAG] = COPY ? getBuffer().size() : valueOffset;
            m_keyData[TO_TAG + 1] = valueCount;
            if (COPY) getBuffer().write(buffer, valueOffset, valueCount);
          }
        }
        break;
      case (FROM):
        {
          DsByteString value = DsByteString.newLower(buffer, nameOffset, nameCount);
          if (value == BS_TAG) {
            m_keyData[FROM_TAG] = COPY ? getBuffer().size() : valueOffset;
            m_keyData[FROM_TAG + 1] = valueCount;
            if (COPY) getBuffer().write(buffer, valueOffset, valueCount);
          }
        }
        break;
    }
  }

  /*
   * javadoc inherited
   */
  public void headerFound(int headerId, byte[] buffer, int offset, int count, boolean isValid)
      throws DsSipParserListenerException {
    if (headerId == UNIQUIFIER) {
      m_keyData[UNIQ] = COPY ? getBuffer().size() : offset;
      m_keyData[UNIQ + 1] = count;
      if (COPY) getBuffer().write(buffer, offset, count);
    }
  }

  /*
   * javadoc inherited
   */
  public DsSipHeaderListener getHeaderListener() {
    return this;
  }

  /*
   * javadoc inherited
   */
  public void messageFound(byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {
    m_messageBytes = COPY ? getBuffer().toByteArray() : m_messageBytes;
    validate();
  }

  /*
   * javadoc inherited
   */
  public void bodyFoundInRequestURI(byte[] buffer, int offset, int count)
      throws DsSipParserListenerException {}

  /**
   * Checks for the validity of the key. If there is an exception while validating the key, that
   * tells the key is not valid and should not be used.
   *
   * @throws DsSipKeyValidationException if there is an exception while validating the key.
   */
  public void validate() throws DsSipKeyValidationException {
    if ((m_keyData[TO_USER] == 0) && (m_keyData[TO_HOST] == 0) && (m_keyData[TO_PORT] == 0)) {
      throw new DsSipKeyValidationException(TO_INVALID);
    }

    if ((m_keyData[FROM_USER] == 0) && (m_keyData[FROM_HOST] == 0) && (m_keyData[FROM_PORT] == 0)) {
      throw new DsSipKeyValidationException(FROM_INVALID);
    }

    if (m_keyData[CALL_ID] == 0) {
      throw new DsSipKeyValidationException(CALLID_INVALID);
    }

    if (m_keyData[CSEQ_NUMBER] == 0) {
      throw new DsSipKeyValidationException(CSEQ_INVALID);
    }

    if ((m_keyData[VIA_HOST] == 0) && (m_keyData[VIA_PORT] == 0) && (m_keyData[VIA_BRANCH] == 0)) {
      throw new DsSipKeyValidationException(VIA_INVALID);
    }

    if (((m_keyData[RURI_USER] == 0) && (m_keyData[RURI_HOST] == 0) && (m_keyData[RURI_PORT] == 0))
        && m_isRequest) {
      throw new DsSipKeyValidationException(RURI_INVALID);
    }
  }

  private static boolean isDefault(byte[] buffer, int offset, int count) {
    if (count == 4
        && buffer[offset] == '5'
        && buffer[offset + 1] == '0'
        && buffer[offset + 2] == '6'
        && buffer[offset + 3] == '0') return true;
    return false;
  }

  /*--
      public final static void main( String args[]) throws Exception
      {

          //byte[] F5 =  ("REGISTER sip:spcs.com;comp=sigcomp SIP/2.0\r\n" +
          byte[] F5 =  ("REGISTER sip:bar.com SIP/2.0\r\n" +
              "Via: SIP/2.0/UDP 63.113.45.227:3305;branch=z9hG4bK3;comp=sigcomp\r\n" +
              "Max-Forwards: 70\r\n" +
              "To: sip:bjenkins@spcs.com\r\n" +
              "From: sip:bjenkins@spcs.com\r\n" +
              "Call-ID: 10240753208974@63.113.45.227\r\n" +
              "CSeq: 4 REGISTER\r\n" +
              "Content-Length: 0\r\n" +
              "Remote-Party-ID: <sip:bjenkins@spcs.com>;id-type=user;screen=yes\r\n" +
              "Expires: 7200\r\n" +
              "Contact: <sip:whatever@63.113.45.227:3305;comp=sigcomp>\r\n\r\n").getBytes();
          DsSipMessage F5msg = DsSipMessage.createMessage(F5, true, true);

      }

      // ////////////////////// test  ////////////////////////////////


      // unit test for keys
      public final static void main2( String args[]) throws Exception
      {

          //                                   |
          //                                   v
          // User A        Proxy 1          Proxy 2          User B
          // |                |                |                |
          // |   INVITE F1    |                |                |
          // |--------------->|                |                |
          // |     407 F2     |                |                |
          // |<---------------|                |                |
          // |     ACK F3     |                |                |
          // |--------------->|                |                |
          // |   INVITE F4    |                |                |
          // |--------------->|   INVITE F5    |                |
          // |    (100) F6    |--------------->|   INVITE F7    |
          // |<---------------|    (100) F8    |--------------->|
          // |                |<---------------|                |
          // |                |                |     180 F9     |
          // |                |    180 F10     |<---------------|
          // |     180 F11    |<---------------|                |
          // |<---------------|                |     200 F12    |
          // |                |    200 F13     |<---------------|
          // |     200 F14    |<---------------|                |
          // |<---------------|                |                |
          // |     ACK F15    |                |                |
          // |--------------->|    ACK F16     |                |
          // |                |--------------->|     ACK F17    |
          // |                |                |--------------->|
          // |                Both Way RTP Media                |
          // |<================================================>|
          // |                |                |     BYE F18    |
          // |                |    BYE F19     |<---------------|
          // |     BYE F20    |<---------------|                |
          // |<---------------|                |                |
          // |     200 F21    |                |                |
          // |--------------->|     200 F22    |                |
          // |                |--------------->|     200 F23    |
          // |                |                |--------------->|
          // |                |                |                |
          //

          System.out.println("Transaction Key Test COPY is set to: " + COPY);


          byte[] F5 =    ("INVITE sip:UserB@there.com SIP/2.0\r\n" +
                          "Via: SIP/2.0/UDP ss1.wcom.com:5060;branch=2d4790.1\r\n" +
                          "Via: SIP/2.0/UDP here.com:5060\r\n" +
                          "Record-Route: <sip:UserB@there.com;maddr=ss1.wcom.com>\r\n" +
                          "From: BigGuy <sip:UserA@here.com>\r\n" +
                          "To: LittleGuy <sip:UserB@there.com>\r\n" +
                          "Call-ID: 12345601@here.com\r\n" +
                          "CSeq: 1 INVITE\r\n" +
                          "Contact: <sip:UserA@100.101.102.103>\r\n" +
                          "Content-Type: application/sdp\r\n" +
                          "Content-Length: 147\r\n" +
                          "\r\n" +
                          "v=0\r\n" +
                          "o=UserA 2890844526 2890844526 IN IP4 here.com\r\n" +
                          "s=Session SDP\r\n" +
                          "c=IN IP4 100.101.102.103\r\n" +
                          "t=0 0\r\n" +
                          "m=audio 49172 RTP/AVP 0\r\n" +
                          "a=rtpmap:0 PCMU/8000\r\n").getBytes();

          DsSipMessage F5msg = DsSipMessage.createMessage(F5, true, true);

          F5msg.setKeyContext(  DsSipTransactionKey.INCOMING
                                |  DsSipTransactionKey.USE_VIA
                                |  DsSipTransactionKey.USE_URI);

          //  F7 =  clone(F5), add via, createKey

          DsSipMessage F7msg = (DsSipMessage) F5msg.clone();
          DsSipHeaderString via   = new DsSipHeaderString("SIP/2.0/UDP ss2.wcom.com:5060;branch=721e418c4.1");
          via.setHeaderID((byte)DsSipConstants.VIA);
          via.setToken(DsSipConstants.BS_VIA);
          F7msg.addHeader(via, true, false);
          F7msg.createKey();

          byte[] F9 =    ("SIP/2.0 180 Ringing\r\n" +
                          "Via: SIP/2.0/UDP ss2.wcom.com:5060;branch=721e418c4.1\r\n" +
                          "Via: SIP/2.0/UDP ss1.wcom.com:5060;branch=2d4790.1\r\n" +
                          "Via: SIP/2.0/UDP here.com:5060\r\n" +
                          "From: BigGuy <sip:UserA@here.com>\r\n" +
                          "To: LittleGuy <sip:UserB@there.com>;tag=314159\r\n" +
                          "Call-ID: 12345601@here.com\r\n" +
                          "CSeq: 1 INVITE\r\n" +
                          "Content-Length: 0\r\n\r\n").getBytes();

          // match to F7
          DsSipMessage F9msg = DsSipMessage.createMessage(F9, true, true);


          myAssert(F9msg.equals(F7msg), "F9msg.equals(F7msg)");
          myAssert(F7msg.equals(F9msg), "F7msg.equals(F9msg)");
          myAssert(F7msg.hashCode() == F9msg.hashCode(), "F7msg.hashCode() == F9msg.hashCode()");


          byte[] F12 =   ("SIP/2.0 200 OK\r\n" +
                          "Via: SIP/2.0/UDP ss2.wcom.com:5060;branch=721e418c4.1\r\n" +
                          "Via: SIP/2.0/UDP ss1.wcom.com:5060;branch=2d4790.1\r\n" +
                          "Via: SIP/2.0/UDP here.com:5060\r\n" +
                          "Record-Route: <sip:UserB@there.com;maddr=ss2.wcom.com>,\r\n" +
                          "  <sip:UserB@there.com;maddr=ss1.wcom.com>\r\n" +
                          "From: BigGuy <sip:UserA@here.com>\r\n" +
                          "To: LittleGuy <sip:UserB@there.com>;tag=314159\r\n" +
                          "Call-ID: 12345601@here.com\r\n" +
                          "CSeq: 1 INVITE\r\n" +
                          "Contact: <sip:UserB@110.111.112.113>\r\n" +
                          "Content-Type: application/sdp\r\n" +
                          "Content-Length: 147\r\n" +
                          "\r\n" +
                          "v=0\r\n" +
                          "o=UserB 2890844527 2890844527 IN IP4 there.com\r\n" +
                          "s=Session SDP\r\n" +
                          "c=IN IP4 110.111.112.113\r\n" +
                          "t=0 0\r\n" +
                          "m=audio 3456 RTP/AVP 0\r\n" +
                          "a=rtpmap:0 PCMU/8000\r\n").getBytes();

          // match to F7
          DsSipMessage F12msg = DsSipMessage.createMessage(F12, true, true);


          myAssert(F12msg.equals(F7msg), "F12msg.equals(F7msg)");
          myAssert(F7msg.equals(F12msg), "F7msg.equals(F12msg)");
          myAssert(F12msg.hashCode() == F7msg.hashCode(), "F12msg.hashCode() == F7msg.hashCode()");

          byte[] F16 =   ("ACK sip:UserB@there.com SIP/2.0\r\n" +
                          "Via: SIP/2.0/UDP ss1.wcom.com:5060;branch=2d4790.1\r\n" +
                          "Via: SIP/2.0/UDP here.com:5060\r\n" +
                          "Route: <sip:UserB@110.111.112.113>\r\n" +
                          "From: BigGuy <sip:UserA@here.com>\r\n" +
                          "To: LittleGuy <sip:UserB@there.com>;tag=314159\r\n" +
                          "Call-ID: 12345601@here.com\r\n" +
                          "CSeq: 1 ACK\r\n" +
                          "Content-Length: 0\r\n\r\n").getBytes();
          //  match F5
          DsSipMessage F16msg = DsSipMessage.createMessage(F16, true, true);


          F16msg.setKeyContext( DsSipTransactionKey.INCOMING
                                | DsSipTransactionKey.USE_VIA
                                | DsSipTransactionKey.LOOKUP
                                | DsSipTransactionKey.USE_URI);


          myAssert(F16msg.equals(F5msg), "F16msg.equals(F5msg)");
          myAssert(F5msg.equals(F16msg), "F5msg.equals(F16msg)");
          myAssert(F5msg.hashCode() ==  F16msg.hashCode(), "F5msg.hashCode() ==  F16msg.hashCode()");


          byte[] F18 =   ("BYE sip:UserA@here.com SIP/2.0\r\n" +
                          "Via: SIP/2.0/UDP there.com:5060\r\n" +
                          "Route: <sip:UserA@here.com;maddr=ss1.wcom.com>,\r\n" +
                          "  <sip:UserA@100.101.102.103>\r\n" +
                          "From: LittleGuy <sip:UserB@there.com>;tag=314159\r\n" +
                          "To: BigGuy <sip:UserA@here.com>\r\n" +
                          "Call-ID: 12345601@here.com\r\n" +
                          "CSeq: 1 BYE\r\n" +
                          "Content-Length: 0\r\n\r\n").getBytes();

          // F19 = clone(F18), add via, createKey
          DsSipMessage F18msg = DsSipMessage.createMessage(F18, true, true);

          F18msg.setKeyContext(  DsSipTransactionKey.INCOMING
                                 |  DsSipTransactionKey.USE_VIA
                                 |  DsSipTransactionKey.USE_URI);

          DsSipMessage F19msg = (DsSipMessage) F18msg.clone();
          via   = new DsSipHeaderString("SIP/2.0/UDP ss2.wcom.com:5060;branch=721e418c4.1");
          via.setHeaderID((byte)DsSipConstants.VIA);
          via.setToken(DsSipConstants.BS_VIA);
          F19msg.addHeader(via, true, false);
          F19msg.createKey();

          byte[] F22 =   ("SIP/2.0 200 OK\r\n" +
                          "Via: SIP/2.0/UDP ss2.wcom.com:5060;branch=721e418c4.1\r\n" +
                          "Via: SIP/2.0/UDP there.com:5060\r\n" +
                          "From: LittleGuy <sip:UserB@there.com>;tag=314159\r\n" +
                          "To: BigGuy <sip:UserA@here.com>\r\n" +
                          "Call-ID: 12345601@here.com\r\n" +
                          "CSeq: 1 BYE\r\n" +
                          "Content-Length: 0\r\n\r\n").getBytes();
          // match to F19
          DsSipMessage F22msg = DsSipMessage.createMessage(F22, true, true);


          myAssert(F19msg.equals(F22msg), "F19msg.equals(F22msg)");
          myAssert(F22msg.equals(F19msg), "F22msg.equals(F19msg)");
          myAssert(F22msg.hashCode() == F19msg.hashCode(), "F22msg.hashCode() == F19msg.hashCode()");



          //   beyond the flow, test CANCEL
          DsSipMessage F5cancel = new DsSipCancelMessage((DsSipRequest)F5msg);
          via   = new DsSipHeaderString("SIP/2.0/UDP ss1.wcom.com:5060;branch=2d4790.1");
          via.setHeaderID((byte)DsSipConstants.VIA);
          via.setToken(DsSipConstants.BS_VIA);
          F5cancel.addHeader(via, true, false);
          // turn it into an incoming CANCEL message
          F5cancel = DsSipMessage.createMessage(F5cancel.toByteString().data(), true, true);


          // match F5
          F5cancel.setKeyContext( DsSipTransactionKey.INCOMING
                                | DsSipTransactionKey.LOOKUP
                                | DsSipTransactionKey.USE_VIA
                                | DsSipTransactionKey.USE_URI);


          myAssert(F5cancel.equals(F5msg), "F5cancel.equals(F5msg)");
          myAssert(F5msg.equals(F5cancel), "F5msg.equals(F5cancel)");
          myAssert(F5msg.hashCode() ==  F5cancel.hashCode(), "F5msg.hashCode() ==  F5cancel.hashCode()");

      }


      static void myAssert(boolean b, String str)
      {
          if(!b)
          {
              System.out.println("!!!myAssertion failed: " + str);
              Thread.currentThread().dumpStack();
              System.exit(-1);
          }
          else
          {
              System.out.println("myAssertion passed: " + str);
          }
      }
  --*/
}
