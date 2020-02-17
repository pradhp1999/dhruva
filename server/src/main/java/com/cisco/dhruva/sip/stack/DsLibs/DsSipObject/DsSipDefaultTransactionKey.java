// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.
package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipElementListener;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipHeaderListener;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipMessageListener;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipMessageListenerFactory;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipMsgParser;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsIntStrCache;
import java.io.IOException;
import java.io.Serializable;

/** Defines the transaction key as specified in RFC 3261. */
public final class DsSipDefaultTransactionKey
    implements DsSipTransactionKey,
        DsSipConstants,
        DsSipMessageListener,
        DsSipMessageListenerFactory,
        DsSipHeaderListener,
        DsSipElementListener,
        Serializable {
  // /////////////////////////////////////////////////////
  // ////// Static Data  /////////////////////////////////
  // /////////////////////////////////////////////////////

  private static final int MULT = 31;

  // /////////////////////////////////////////////////////
  // ////// Instance Data   //////////////////////////////
  // /////////////////////////////////////////////////////

  // //////////////////////////////////////////////////////////////////////
  private byte[] m_baBranch; // 4
  private DsByteString m_strMethod; // 4
  private byte[] m_baAddress; // 4
  private byte[] m_baToTag; // 4
  private boolean m_bIncoming; // 1/8
  private boolean m_bLookup; // 1/8
  private int m_hashCode; // 4
  private int m_iPort; // 4
  private boolean m_bTopVia;
  // //////////////////////////////////////////////////////////////////////
  // TOTOAL = 25 bytes  :)

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
   * Returns the To Tag value, if present, otherwise returns null.
   *
   * @return the To Tag value, if present, otherwise returns null.
   */
  public DsByteString getToTag() {
    return (null == m_baToTag) ? null : new DsByteString(m_baToTag, 0, m_baToTag.length);
  }

  /**
   * Returns the CSeq method name, if present, otherwise returns null.
   *
   * @return the CSeq method name, if present, otherwise returns null.
   */
  public DsByteString getCSeqMethod() {
    return m_strMethod;
  }

  /**
   * Returns the VIA branch value, if present, otherwise returns null.
   *
   * @return the VIA branch value, if present, otherwise returns null.
   */
  public DsByteString getViaBranch() {
    return (null == m_baBranch)
        ? null
        : (m_baBranch.length == 0)
            ? DsByteString.BS_EMPTY_STRING
            : new DsByteString(m_baBranch, 0, m_baBranch.length);
  }

  /**
   * Sets the CSeq method to the specified <code>method</code>.
   *
   * @param method the CSeq method name that needs to be set for this key
   */
  public void setCSeqMethod(DsByteString method) {
    m_strMethod = method;
  }

  /**
   * Sets the VIA branch value to the specified <code>branch</code> value.
   *
   * @param branch the VIA branch value that needs to be set for this key
   */
  public void setViaBranch(byte[] branch) {
    m_baBranch = branch;
  }

  /**
   * Returns the source address of the corresponding message as a byte array. Its the 'host' part in
   * the sent-by component of the top VIA in the corresponding message. This address along with the
   * sent-by port number is used for incoming messages in case of server transaction.
   *
   * @return the source address of the corresponding message as a byte array.
   */
  public byte[] getSourceAddress() {
    return m_baAddress;
  }

  /**
   * Returns the source port number of the corresponding message. Its the 'port' part in the sent-by
   * component of the top VIA in the corresponding message. This port number along with the sent-by
   * host is used for incoming messages in case of server transaction.
   *
   * @return the source port number of the corresponding message.
   */
  public int getSourcePort() {
    return m_iPort;
  }

  /**
   * Sets the sent-by host address of the corresponding message to the specified <code>address
   * </code> byte array. Its the 'host' part in the sent-by component of the top VIA in the
   * corresponding message. This address along with the sent-by port number is used for incoming
   * messages in case of server transaction.
   *
   * @param address the source address of the corresponding message as a byte array.
   */
  public void setSourceAddress(byte[] address) {
    m_baAddress = address;
  }

  /**
   * Sets the sent-by port number of the corresponding message to the specified <code>port</code>
   * number. Its the 'port' number in the sent-by component of the top VIA in the corresponding
   * message. This port number along with the sent-by address is used for incoming messages in case
   * of server transaction.
   *
   * @param port the source port number of the corresponding message.
   */
  public void setSourcePort(int port) {
    m_iPort = port;
  }

  /**
   * Returns the string representation of the various components contained in this key. This is
   * useful for debugging purposes.
   *
   * @return the string representation of the various components contained in this key.
   */
  void dump() {
    System.out.println("ToTag: " + getToTag());
    System.out.println("CSeqMethod: " + getCSeqMethod());
    System.out.println("ViaBranch: " + getViaBranch());
    System.out.println("Source Address: " + getSourceAddress());
    System.out.println("Source Port: " + m_iPort);
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

    DsSipDefaultTransactionKey comparator = null;
    try {
      comparator = (DsSipDefaultTransactionKey) other;
    } catch (ClassCastException cce) {
      return false;
    }
    int len0 = 0, len1 = 0;
    byte[] b0 = m_baBranch, b1 = comparator.m_baBranch;

    // Compare the via branch
    len0 = (b0 == null) ? 0 : b0.length;
    len1 = (b1 == null) ? 0 : b1.length;

    if (len0 != len1) return false;
    for (int i = 0; i < len0; i++) {
      if (b0[i] != b1[i]) return false;
    }

    if (!m_bLookup) {
      // reference equality is OK here since we intern for these two methods
      //  see DsByteString.newInstance
      // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
      if (m_strMethod == BS_CANCEL || m_strMethod == BS_ACK || m_strMethod == BS_PRACK) {
        if (comparator.m_strMethod != m_strMethod) return false;
        // if (!DsByteString.equals(m_strMethod, comparator.m_strMethod)) return false;
      }
    }

    // If this key is for incoming SIP message, compare source
    // address and port also
    if (m_bIncoming) {
      // Compare Source addrress
      b0 = m_baAddress;
      b1 = comparator.m_baAddress;
      len0 = (b0 == null) ? 0 : b0.length;
      len1 = (b1 == null) ? 0 : b1.length;

      if (len0 != len1) return false;
      for (int i = 0; i < len0; i++) {
        if (b0[i] != b1[i]) return false;
      }
      // Compare Source port
      if (m_iPort != comparator.m_iPort) return false;
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
      int len = 0;
      byte[] b = m_baBranch;

      // the via branch
      len = (b == null) ? 0 : b.length;
      for (int i = 0; i < len; i++) {
        hash = MULT * hash + b[i];
      }

      if (!m_bLookup) {
        // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
        if (m_strMethod == BS_CANCEL || m_strMethod == BS_ACK || m_strMethod == BS_PRACK) {
          hash = MULT * hash + m_strMethod.hashCode();
        }
      }

      // If this key is for incoming SIP message, consider source
      // address and port also
      if (m_bIncoming) {
        // Source addrress
        b = m_baAddress;
        len = (b == null) ? 0 : b.length;
        for (int i = 0; i < len; i++) {
          hash = MULT * hash + b[i];
        }
        // Source port
        hash = MULT * hash + m_iPort;
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
    String buffer2String = null;
    try (ByteBuffer buffer = ByteBuffer.newInstance()) {

      // the via branch
      if (m_baBranch != null) {
        buffer.write(m_baBranch);
        buffer.write(B_COMMA);
      }

      if (!m_bLookup) {
        // reference equals is ok here since these will be intern'ed
        // CAFFEINE 2.0 DEVELOPMENT - PRACK Support
        if (m_strMethod == BS_ACK || m_strMethod == BS_CANCEL || m_strMethod == BS_PRACK) {
          buffer.write(m_strMethod.data(), m_strMethod.offset(), m_strMethod.length());
          buffer.write(B_COMMA);
        }
      }

      // If this key is for incoming SIP message, consider source
      // address and port also
      if (m_bIncoming) {
        // Source addrress
        if (m_baAddress != null) {
          buffer.write(m_baAddress);
          buffer.write(B_COMMA);
        }
        // Source port
        buffer.write(DsIntStrCache.intToBytes(m_iPort));
      }
      buffer2String = buffer.toString();
    } catch (IOException e) {

    }
    return buffer2String;
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
   * In case of this transaction key, the valid options are USE_METHOD and INCOMING. Where as the
   * other options can be specified in case of Classic key as defined by {@link
   * DsSipClassicTransactionKey}. <br>
   * By default, the branch parameter value of the top VIA header is considered as part of this
   * default key. These default components will always be considered regardless of the context
   * options specified. The context options will specify the additional key components.
   *
   * @param context the context that needs to be set for this key.
   */
  public void setKeyContext(int context) {
    boolean incoming = (context & INCOMING) > 0, lookup = (context & LOOKUP) > 0;
    if (incoming == m_bIncoming && m_bLookup == lookup) {
      return;
    }
    m_bIncoming = incoming;
    m_bLookup = lookup;
    m_hashCode = 0;
  }

  //
  //    public int getKeyContext()
  //    {
  //        int context = 0;
  //        if(m_bLookup) context |= LOOKUP;
  //        if(m_bIncoming) context |= INCOMING;
  //        return context;
  //    }
  //
  //

  /**
   * Tells whether the Via parts in this key are equal to that of the specified Key instance <code>
   * other</code>.
   *
   * @param other The key object whose Via components needs to be compared with this objects'.
   * @return <code>true</code> if the Via components are equal in both the keys, <code>false</code>
   *     otherwise.
   */
  public boolean viaEquals(Object other) {
    if (this == other) return true;
    if (null == other) return false;
    DsSipDefaultTransactionKey comparator = null;
    try {
      comparator = (DsSipDefaultTransactionKey) other;
    } catch (ClassCastException cce) {
      return false;
    }

    int len0 = 0, len1 = 0;
    byte[] b0 = m_baBranch, b1 = comparator.m_baBranch;

    // Compare the via branch
    len0 = (b0 == null) ? 0 : b0.length;
    len1 = (b1 == null) ? 0 : b1.length;

    if (len0 != len1) return false;
    for (int i = 0; i < len0; i++) {
      if (b0[i] != b1[i]) return false;
    }
    return true;
  }

  // /////////////////////////////////////////////////////////////////////////////////
  // /////////////////////  Listener Interfaces   ////////////////////////////////////
  // /////////////////////////////////////////////////////////////////////////////////

  public final DsSipElementListener headerBegin(int headerId) {
    if (DEBUG) System.out.println("headerBegin = [" + DsSipMsgParser.HEADER_NAMES[headerId] + "]");
    if (DEBUG) System.out.println();
    switch (headerId) {
      case VIA:
        if (!m_bTopVia) {
          m_bTopVia = true;
          return this;
        }
        break;
      case TO:
      case CSEQ:
        return this;
    }
    return null;
  }

  public final DsSipElementListener requestURIBegin(
      byte[] buffer, int schemeOffset, int schemeCount) {
    if (DEBUG)
      System.out.println(
          "requestURIBegin - scheme = ["
              + DsByteString.newString(buffer, schemeOffset, schemeCount)
              + "]");
    if (DEBUG) System.out.println();
    return null;
  }

  public void requestURIFound(byte[] buffer, int offset, int count, boolean valid) {}

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

  public final DsSipMessageListener requestBegin(
      byte[] buffer, int methodOffset, int methodCount, boolean isEncoded) {
    if (DEBUG)
      System.out.println(
          "requestBegin - method = ["
              + DsByteString.newString(buffer, methodOffset, methodCount)
              + "]");
    if (DEBUG) System.out.println();
    return this;
  }

  public final DsSipMessageListener responseBegin(
      byte[] buffer, int code, int reasonOffset, int reasonCount, boolean isEncoded) {
    if (DEBUG)
      System.out.println(
          "responseBegin - reason = ["
              + DsByteString.newString(buffer, reasonOffset, reasonCount)
              + "]");
    if (DEBUG) System.out.println("responseBegin code = [" + code + "]");
    if (DEBUG) System.out.println();
    return this;
  }

  public final DsSipElementListener elementBegin(int contextId, int elementId) {

    if (DEBUG)
      System.out.println(
          "elementBegin - contextId = ["
              + contextId
              + "]["
              + DsSipMsgParser.HEADER_NAMES[contextId]
              + "]");
    if (DEBUG)
      System.out.println(
          "elementBegin - elementId = ["
              + elementId
              + "]["
              + DsSipMsgParser.ELEMENT_NAMES[elementId]
              + "]");
    if (DEBUG) System.out.println();
    return null;
  }

  public final void elementFound(
      int contextId, int elementId, byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {
    if (DEBUG)
      System.out.println(
          "elementFound - contextId = ["
              + contextId
              + "]["
              + DsSipMsgParser.HEADER_NAMES[contextId]
              + "]");
    if (DEBUG)
      System.out.println(
          "elementFound - elementId = ["
              + elementId
              + "]["
              + DsSipMsgParser.ELEMENT_NAMES[elementId]
              + "]");
    if (DEBUG)
      System.out.println("elementFound - value [offset, count] = [" + offset + ", " + count + "]");
    if (DEBUG)
      System.out.println(
          "elementFound - value = [" + DsByteString.newString(buffer, offset, count) + "]");
    if (DEBUG) System.out.println();
    switch (contextId) {
      case (CSEQ):
        switch (elementId) {
          case CSEQ_METHOD:
            m_strMethod = DsByteString.newInstance(buffer, offset, count);
            break;
        }
        break;
      case (VIA):
        switch (elementId) {
          case HOST:
            m_baAddress = null;
            if (count > 0) {
              m_baAddress = new byte[count];
              System.arraycopy(buffer, offset, m_baAddress, 0, count);
            }
            break;
          case PORT:
            m_iPort = 0;
            try {
              m_iPort = DsSipMsgParser.parseInt(buffer, offset, count);
            } catch (NumberFormatException nfe) {
              throw new DsSipParserListenerException(
                  "Exception while constructing the numerical value: ", nfe);
            }
            break;
        }
        break;
    } // _switch
  }

  public void unknownFound(
      byte[] buffer,
      int nameOffset,
      int nameCount,
      int valueOffset,
      int valueCount,
      boolean isValid)
      throws DsSipParserListenerException {
    // only useful for unknown headers - ignore
  }

  public void parameterFound(
      int contextId, byte[] buffer, int nameOffset, int nameCount, int valueOffset, int valueCount)
      throws DsSipParserListenerException {
    switch (contextId) {
      case VIA:
        {
          DsByteString value = DsByteString.newLower(buffer, nameOffset, nameCount);
          if (value == BS_BRANCH) {
            // Check the presence of Magic Cookie
            if (valueCount > MAGIC_COOKIE_COUNT
                && BS_MAGIC_COOKIE.equals(buffer, valueOffset, MAGIC_COOKIE_COUNT)) {
              // Magic Cookie is present
              m_baBranch = new byte[valueCount];
              System.arraycopy(buffer, valueOffset, m_baBranch, 0, valueCount);
            }
            /*-- now we check with m_bTopVia
                                    else
                                    {
                                        // No Magic Cookie is present
                                        m_baBranch = DsByteString.EMPTY_BYTES; // to flag that we already checked the top VIA
                                    }
            --*/
          }
        }
        break;
      case (TO):
        {
          DsByteString value = DsByteString.newLower(buffer, nameOffset, nameCount);
          if (value == BS_TAG) {
            if (valueCount > 0) {
              m_baToTag = new byte[valueCount];
              System.arraycopy(buffer, valueOffset, m_baToTag, 0, valueCount);
            }
          }
        }
        break;
    }
  }

  public void headerFound(int headerId, byte[] buffer, int offset, int count, boolean isValid)
      throws DsSipParserListenerException {}

  /*--
  //-- Its not in any of the praser listener interfaces.
      public void unknownFound(byte[] buffer, int nameOffset, int nameCount,
                                  int valueOffset, int valueCount)
              throws DsSipParserListenerException
      {}
  --*/
  public DsSipHeaderListener getHeaderListener() {
    return this;
  }

  public void messageFound(byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {
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
    if (null == m_baBranch || m_baBranch.length < 1) {
      throw new DsSipKeyValidationException(MAGIC_COOKIE_INVALID);
    }

    if (null == m_strMethod) {
      throw new DsSipKeyValidationException(CSEQ_METHOD_INVALID);
    }
  }

  // unit test for keys
  //    public final static void main( String args[]) throws Exception
  //    {
  //
  //        //                                   |
  //        //                                   v
  //        // User A        Proxy 1          Proxy 2          User B
  //        // |                |                |                |
  //        // |   INVITE F1    |                |                |
  //        // |--------------->|                |                |
  //        // |     407 F2     |                |                |
  //        // |<---------------|                |                |
  //        // |     ACK F3     |                |                |
  //        // |--------------->|                |                |
  //        // |   INVITE F4    |                |                |
  //        // |--------------->|   INVITE F5    |                |
  //        // |    (100) F6    |--------------->|   INVITE F7    |
  //        // |<---------------|    (100) F8    |--------------->|
  //        // |                |<---------------|                |
  //        // |                |                |     180 F9     |
  //        // |                |    180 F10     |<---------------|
  //        // |     180 F11    |<---------------|                |
  //        // |<---------------|                |     200 F12    |
  //        // |                |    200 F13     |<---------------|
  //        // |     200 F14    |<---------------|                |
  //        // |<---------------|                |                |
  //        // |     ACK F15    |                |                |
  //        // |--------------->|    ACK F16     |                |
  //        // |                |--------------->|     ACK F17    |
  //        // |                |                |--------------->|
  //        // |                Both Way RTP Media                |
  //        // |<================================================>|
  //        // |                |                |     BYE F18    |
  //        // |                |    BYE F19     |<---------------|
  //        // |     BYE F20    |<---------------|                |
  //        // |<---------------|                |                |
  //        // |     200 F21    |                |                |
  //        // |--------------->|     200 F22    |                |
  //        // |                |--------------->|     200 F23    |
  //        // |                |                |--------------->|
  //        // |                |                |                |
  //        //
  //
  ////        System.out.println("Transaction Key Test COPY is set to: " + COPY);
  //
  //
  //        byte[] F5 =    ("INVITE sip:UserB@there.com SIP/2.0\r\n" +
  //                        "Via: SIP/2.0/UDP ss1.wcom.com:5060;branch=z9hG4bK.2d4790.1\r\n" +
  //                        "Via: SIP/2.0/UDP here.com:5060\r\n" +
  //                        "Record-Route: <sip:UserB@there.com;maddr=ss1.wcom.com>\r\n" +
  //                        "From: BigGuy <sip:UserA@here.com>\r\n" +
  //                        "To: LittleGuy <sip:UserB@there.com>\r\n" +
  //                        "Call-ID: 12345601@here.com\r\n" +
  //                        "CSeq: 1 INVITE\r\n" +
  //                        "Contact: <sip:UserA@100.101.102.103>\r\n" +
  //                        "Content-Type: application/sdp\r\n" +
  //                        "Content-Length: 147\r\n" +
  //                        "\r\n" +
  //                        "v=0\r\n" +
  //                        "o=UserA 2890844526 2890844526 IN IP4 here.com\r\n" +
  //                        "s=Session SDP\r\n" +
  //                        "c=IN IP4 100.101.102.103\r\n" +
  //                        "t=0 0\r\n" +
  //                        "m=audio 49172 RTP/AVP 0\r\n" +
  //                        "a=rtpmap:0 PCMU/8000\r\n").getBytes();
  //
  //        DsSipMessage F5msg = DsSipMessage.createMessage(F5, true, true);
  //        F5msg.setKeyContext(  DsSipTransactionKey.INCOMING
  //                              |  DsSipTransactionKey.USE_VIA
  //                              |  DsSipTransactionKey.USE_URI);
  //        //  F7 =  clone(F5), add via, createKey
  //
  //        DsSipMessage F7msg = (DsSipMessage) F5msg.clone();
  //        DsSipHeaderString via   = new DsSipHeaderString("SIP/2.0/UDP
  // ss2.wcom.com:5060;branch=z9hG4bK.721e418c4.1");
  //        via.setHeaderID((byte)DsSipConstants.VIA);
  //        via.setToken(DsSipConstants.BS_VIA);
  //        F7msg.addHeader(via, true, false);
  //        F7msg.createKey();
  //
  //        byte[] F9 =    ("SIP/2.0 180 Ringing\r\n" +
  //                        "Via: SIP/2.0/UDP ss2.wcom.com:5060;branch=z9hG4bK.721e418c4.1\r\n" +
  //                        "Via: SIP/2.0/UDP ss1.wcom.com:5060;branch=z9hG4bK.2d4790.1\r\n" +
  //                        "Via: SIP/2.0/UDP here.com:5060\r\n" +
  //                        "From: BigGuy <sip:UserA@here.com>\r\n" +
  //                        "To: LittleGuy <sip:UserB@there.com>;tag=314159\r\n" +
  //                        "Call-ID: 12345601@here.com\r\n" +
  //                        "CSeq: 1 INVITE\r\n" +
  //                        "Content-Length: 0\r\n\r\n").getBytes();
  //
  //        // match to F7
  //        DsSipMessage F9msg = DsSipMessage.createMessage(F9, true, true);
  //
  //
  //        myAssert(F9msg.equals(F7msg), "F9msg.equals(F7msg)");
  //        myAssert(F7msg.equals(F9msg), "F7msg.equals(F9msg)");
  //
  //        myAssert(F7msg.hashCode() == F9msg.hashCode(), "F7msg.hashCode() == F9msg.hashCode()"
  //                        + "[" + F7msg.hashCode() + "," + F9msg.hashCode() + "]");
  //
  //
  //        byte[] F12 =   ("SIP/2.0 200 OK\r\n" +
  //                        "Via: SIP/2.0/UDP ss2.wcom.com:5060;branch=z9hG4bK.721e418c4.1\r\n" +
  //                        "Via: SIP/2.0/UDP ss1.wcom.com:5060;branch=z9hG4bK.2d4790.1\r\n" +
  //                        "Via: SIP/2.0/UDP here.com:5060\r\n" +
  //                        "Record-Route: <sip:UserB@there.com;maddr=ss2.wcom.com>,\r\n" +
  //                        "  <sip:UserB@there.com;maddr=ss1.wcom.com>\r\n" +
  //                        "From: BigGuy <sip:UserA@here.com>\r\n" +
  //                        "To: LittleGuy <sip:UserB@there.com>;tag=314159\r\n" +
  //                        "Call-ID: 12345601@here.com\r\n" +
  //                        "CSeq: 1 INVITE\r\n" +
  //                        "Contact: <sip:UserB@110.111.112.113>\r\n" +
  //                        "Content-Type: application/sdp\r\n" +
  //                        "Content-Length: 147\r\n" +
  //                        "\r\n" +
  //                        "v=0\r\n" +
  //                        "o=UserB 2890844527 2890844527 IN IP4 there.com\r\n" +
  //                        "s=Session SDP\r\n" +
  //                        "c=IN IP4 110.111.112.113\r\n" +
  //                        "t=0 0\r\n" +
  //                        "m=audio 3456 RTP/AVP 0\r\n" +
  //                        "a=rtpmap:0 PCMU/8000\r\n").getBytes();
  //
  //        // match to F7
  //        DsSipMessage F12msg = DsSipMessage.createMessage(F12, true, true);
  //
  //
  //        myAssert(F12msg.equals(F7msg), "F12msg.equals(F7msg)");
  //        myAssert(F7msg.equals(F12msg), "F7msg.equals(F12msg)");
  //        myAssert(F12msg.hashCode() == F7msg.hashCode(), "F12msg.hashCode() ==
  // F7msg.hashCode()");
  //
  //        byte[] F16 =   ("ACK sip:UserB@there.com SIP/2.0\r\n" +
  //                        "Via: SIP/2.0/UDP ss1.wcom.com:5060;branch=z9hG4bK.2d4790.1\r\n" +
  //                        "Via: SIP/2.0/UDP here.com:5060\r\n" +
  //                        "Route: <sip:UserB@110.111.112.113>\r\n" +
  //                        "From: BigGuy <sip:UserA@here.com>\r\n" +
  //                        "To: LittleGuy <sip:UserB@there.com>;tag=314159\r\n" +
  //                        "Call-ID: 12345601@here.com\r\n" +
  //                        "CSeq: 1 ACK\r\n" +
  //                        "Content-Length: 0\r\n\r\n").getBytes();
  //        //  match F5
  //        DsSipMessage F16msg = DsSipMessage.createMessage(F16, true, true);
  //
  //        F16msg.setKeyContext( DsSipTransactionKey.INCOMING
  //                              | DsSipTransactionKey.USE_VIA
  //                              | DsSipTransactionKey.LOOKUP
  //                              | DsSipTransactionKey.USE_URI);
  //
  //        myAssert(F16msg.equals(F5msg), "F16msg.equals(F5msg)");
  //        myAssert(F5msg.equals(F16msg), "F5msg.equals(F16msg)");
  //        myAssert(F5msg.hashCode() ==  F16msg.hashCode(), "F5msg.hashCode() ==
  // F16msg.hashCode()");
  //
  //
  //        byte[] F18 =   ("BYE sip:UserA@here.com SIP/2.0\r\n" +
  //                        "Via: SIP/2.0/UDP there.com:5060\r\n" +
  //                        "Route: <sip:UserA@here.com;maddr=ss1.wcom.com>,\r\n" +
  //                        "  <sip:UserA@100.101.102.103>\r\n" +
  //                        "From: LittleGuy <sip:UserB@there.com>;tag=314159\r\n" +
  //                        "To: BigGuy <sip:UserA@here.com>\r\n" +
  //                        "Call-ID: 12345601@here.com\r\n" +
  //                        "CSeq: 1 BYE\r\n" +
  //                        "Content-Length: 0\r\n\r\n").getBytes();
  //
  //        // F19 = clone(F18), add via, createKey
  //        DsSipMessage F18msg = DsSipMessage.createMessage(F18, true, true);
  //
  //        F18msg.setKeyContext(  DsSipTransactionKey.INCOMING
  //                               |  DsSipTransactionKey.USE_VIA
  //                               |  DsSipTransactionKey.USE_URI);
  //
  //        DsSipMessage F19msg = (DsSipMessage) F18msg.clone();
  //        via   = new DsSipHeaderString("SIP/2.0/UDP
  // ss2.wcom.com:5060;branch=z9hG4bK.721e418c4.1");
  //        via.setHeaderID((byte)DsSipConstants.VIA);
  //        via.setToken(DsSipConstants.BS_VIA);
  //        F19msg.addHeader(via, true, false);
  //        F19msg.createKey();
  //
  //        byte[] F22 =   ("SIP/2.0 200 OK\r\n" +
  //                        "Via: SIP/2.0/UDP ss2.wcom.com:5060;branch=z9hG4bK.721e418c4.1\r\n" +
  //                        "Via: SIP/2.0/UDP there.com:5060\r\n" +
  //                        "From: LittleGuy <sip:UserB@there.com>;tag=314159\r\n" +
  //                        "To: BigGuy <sip:UserA@here.com>\r\n" +
  //                        "Call-ID: 12345601@here.com\r\n" +
  //                        "CSeq: 1 BYE\r\n" +
  //                        "Content-Length: 0\r\n\r\n").getBytes();
  //        // match to F19
  //        DsSipMessage F22msg = DsSipMessage.createMessage(F22, true, true);
  //
  //
  //        myAssert(F19msg.equals(F22msg), "F19msg.equals(F22msg)");
  //        myAssert(F22msg.equals(F19msg), "F22msg.equals(F19msg)");
  //        myAssert(F22msg.hashCode() == F19msg.hashCode(), "F22msg.hashCode() ==
  // F19msg.hashCode()");
  //
  //
  //
  //        //   beyond the flow, test CANCEL
  //        DsSipMessage F5cancel = new DsSipCancelMessage((DsSipRequest)F5msg);
  //        via   = new DsSipHeaderString("SIP/2.0/UDP ss1.wcom.com:5060;branch=z9hG4bK.2d4790.1");
  //        via.setHeaderID((byte)DsSipConstants.VIA);
  //        via.setToken(DsSipConstants.BS_VIA);
  //        F5cancel.addHeader(via, true, false);
  //        // turn it into an incoming CANCEL message
  //        F5cancel = DsSipMessage.createMessage(F5cancel.toByteString().data(), true, true);
  //
  //        F5cancel.setKeyContext( DsSipTransactionKey.INCOMING
  //                              | DsSipTransactionKey.LOOKUP
  //                              | DsSipTransactionKey.USE_VIA
  //                              | DsSipTransactionKey.USE_URI);
  //
  //
  //        myAssert(F5cancel.equals(F5msg), "F5cancel.equals(F5msg)");
  //        myAssert(F5msg.equals(F5cancel), "F5msg.equals(F5cancel)");
  //        myAssert(F5msg.hashCode() ==  F5cancel.hashCode(), "F5msg.hashCode() ==
  // F5cancel.hashCode()");
  //
  //    }

  /*
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
  */
}
