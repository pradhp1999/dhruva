// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipElementListener;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipHeaderListener;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipMessageListener;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipMessageListenerFactory;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipMsgParser;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsIntStrCache;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Defines a light weight SIP Response which contains the SIP response bytes. This class can be used
 * in a case where the incoming response needs to be forwarded to the next hop and only requires the
 * top Via header to be removed before forwarding. It proves to be an efficient way of forwarding
 * responses in such situations.
 */
public final class DsSipResponseBytes
    implements DsSipMessageListener,
        DsSipMessageListenerFactory,
        DsSipHeaderListener,
        DsSipElementListener {

  // warning:  these variable names conflict with the ones in DsSipConstants

  private static final int REASON = 0; // indices for the important parts of the response
  private static final int CONTACT = 2;
  private static final int RECORD_ROUTE = 4;
  private static final int CSEQ_METHOD = 6;
  // -------------------------------------------
  private static final int N_POINTERS = 8;

  private DsBindingInfo m_bindingInfo; // a copy of the binding info that the response
  //   was delivered over
  private int m_statusCode; // the message's response code

  private byte[] m_messageBytes; // the message's rewritten bytes
  private int[] m_messageIndex; // index to..

  private byte m_viaCount = 0; // keep track of our state in the parse
  private int m_addrSource;

  private DsByteString m_2viaHost; // addressing information from SECOND Via Header
  private DsByteString m_2viaTransport;
  private int m_2viaPort = 0;

  private DsByteString m_viaHost; // addressing information from FIRST Via Header
  private DsByteString m_viaTransport;
  private int m_viaPort = 0;

  private DsByteString m_topVia;

  private static ThreadLocal tlKeyBuffer = new ThreadLocal();

  private static ByteArrayOutputStream getBuffer() {
    ByteArrayOutputStream str = (ByteArrayOutputStream) tlKeyBuffer.get();
    if (str == null) {
      str = new ByteArrayOutputStream(512); // big enough:  it will grow if needed
      tlKeyBuffer.set(str);
    }
    return str;
  }

  private DsSipTransactionKey m_key;

  /** Constructs this instance with null buffer. */
  public DsSipResponseBytes() {
    this(null, 0, 0, 0); // these will get set when the parser calls with events
  }

  /**
   * Constructs this instance with the response bytes set to the specified buffer starting from the
   * <code>offset</code> up to the specified <code>count</code> number of bytes. The response code
   * is set to the specified <code>code</code>.
   *
   * @param buffer the response bytes buffer
   * @param offset the offset in the specified buffer where from the actual responses bytes starts
   * @param count the number of bytes starting from the offset that constitute the response bytes.
   * @param code the response code.
   */
  public DsSipResponseBytes(byte[] buffer, int code, int offset, int count) {
    // m_messageBytes = buffer;
    // m_messageIndex[REASON] =   offset;
    // m_messageIndex[REASON+1] = count;
    m_messageIndex = new int[N_POINTERS];
    getBuffer().reset();

    if (buffer != null) {
      m_messageBytes = buffer;

      m_statusCode = code;

      byte[] code_bytes = DsIntStrCache.intToBytes(code);
      try {
        ByteArrayOutputStream bos = getBuffer();
        DsSipConstants.BS_SIP_VERSION_SPACE.write(bos);
        bos.write(code_bytes, 0, code_bytes.length);
        bos.write(' ');

        m_messageIndex[REASON] = bos.size();
        m_messageIndex[REASON + 1] = count;

        bos.write(buffer, offset, count);
        DsSipConstants.BS_EOH.write(bos);
      } catch (IOException ioe) {
        // ioe.printStackTrace();
      }
      m_statusCode = code;
    }
  }

  /**
   * Returns the response class of the response code in this response.
   *
   * @return the response class
   */
  public final int getResponseClass() {
    return (m_statusCode / 100);
  }

  /**
   * Returns the response status code.
   *
   * @return the status code.
   */
  public final int getStatusCode() {
    return m_statusCode;
  }

  /**
   * Returns the reason phrase.
   *
   * @return the reason phrase
   */
  public DsByteString getReasonPhrase() {
    if (m_messageIndex[REASON] == 0) return null;
    return new DsByteString(m_messageBytes, m_messageIndex[REASON], m_messageIndex[REASON + 1]);
  }

  /**
   * Returns the CSeq Method name.
   *
   * @return the CSeq method name.
   */
  public final DsByteString getCSeqMethod() {
    if (m_messageIndex[CSEQ_METHOD] == 0) return null;
    return new DsByteString(
        m_messageBytes, m_messageIndex[CSEQ_METHOD], m_messageIndex[CSEQ_METHOD + 1]);
  }

  /**
   * Returns the top contact header string.
   *
   * @return the top contact header string.
   */
  public DsByteString getTopContactString() {
    if (m_messageIndex[CONTACT] == 0) return null;
    return new DsByteString(m_messageBytes, m_messageIndex[CONTACT], m_messageIndex[CONTACT + 1]);
  }

  /**
   * Returns the top contact header.
   *
   * @return the top contact header.
   * @throws DsSipParserListenerException if there is an error while parsing the top contact header.
   * @throws DsSipParserException if there is an error while parsing the top contact header.
   */
  public DsSipContactHeader getTopContactHeader()
      throws DsSipParserListenerException, DsSipParserException {
    if (m_messageIndex[CONTACT] != 0) {
      return (DsSipContactHeader)
          DsSipHeader.createHeader(
              DsSipConstants.CONTACT,
              m_messageBytes,
              m_messageIndex[CONTACT],
              m_messageIndex[CONTACT + 1]);
    }
    return null;
  }

  /**
   * Returns the CSeq Method Id in this response.
   *
   * @return the CSeq Method Id in this response.
   */
  public final int getCSeqMethodID() {
    return DsSipMsgParser.getMethod(getCSeqMethod());
  }

  /**
   * Returns the host part of the second top Via header in this response.
   *
   * @return the host part of the second top Via header in this response.
   */
  public DsByteString get2ndViaHost() {
    return m_2viaHost;
  }

  /**
   * Returns the port number of the second top Via header in this response.
   *
   * @return the port number of the second top Via header in this response.
   */
  public int get2ndViaPort() {
    return m_2viaPort;
  }

  /**
   * Returns the transport value of the second top Via header in this response.
   *
   * @return the transport value of the second top Via header in this response.
   */
  public DsByteString get2ndViaTransport() {
    return m_2viaTransport;
  }

  /**
   * Returns the host part of the top Via header in this response.
   *
   * @return the host part of the top Via header in this response.
   */
  public DsByteString getViaHost() {
    return m_viaHost;
  }

  /**
   * Returns the port number of the top Via header in this response.
   *
   * @return the port number of the top Via header in this response.
   */
  public int getViaPort() {
    return m_viaPort;
  }

  /**
   * Returns the transport value of the top Via header in this response.
   *
   * @return the transport value of the top Via header in this response.
   */
  public DsByteString getViaTransport() {
    return m_viaTransport;
  }

  /**
   * Tells if there are multiple Via headers in this response.
   *
   * @return <code>true</code> if the response has nore than one Via header, otherwise returns
   *     <code>false</code>.
   */
  public boolean hasMultipleVias() {
    return m_viaCount > 1;
  }

  /**
   * Returns the binding information for the connection that this response was received over.
   *
   * @return the binding information for the connection that this response was received over.
   */
  public DsBindingInfo getBindingInfo() {
    return m_bindingInfo;
  }

  /**
   * Sets the binding information for the connection that this response was received over.
   *
   * @param info the new binding info for this response.
   */
  public void setBindingInfo(DsBindingInfo info) {
    m_bindingInfo = info;
  }

  /**
   * Parses this response bytes into a DsSipResponse object. If the specified flag <code>replaceVia
   * </code> is <code>false</code> then the top Via header, if any, will be removed.
   *
   * @param replaceVia if <code>false</code> then the top Via header will be removed from the
   *     returned DsSipResponse object.
   * @return the DsSipResponse object constructed from the response bytes.
   * @throws DsSipParserListenerException if there is an error while parsing the response bytes into
   *     DsSipResponse object.
   * @throws DsSipParserException if there is an error while parsing the response bytes into
   *     DsSipResponse object.
   */
  public DsSipResponse toSipResponse(boolean replaceVia)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipResponse r = (DsSipResponse) DsSipMessage.createMessage(m_messageBytes);
    if (replaceVia) {
      r.addHeader(new DsSipHeaderString(DsSipConstants.VIA, DsSipConstants.BS_VIA, m_topVia), true);
    }
    return r;
  }

  /**
   * Parses this response bytes into a DsSipResponse object with the top Via header,if any, removed.
   *
   * @return the DsSipResponse object constructed from the response bytes.
   * @throws DsSipParserListenerException if there is an error while parsing the response bytes into
   *     DsSipResponse object.
   * @throws DsSipParserException if there is an error while parsing the response bytes into
   *     DsSipResponse object.
   */
  public DsSipResponse toSipResponse() throws DsSipParserListenerException, DsSipParserException {
    return (DsSipResponse) DsSipMessage.createMessage(m_messageBytes);
  }

  /**
   * Returns the response bytes with the top Via header removed.
   *
   * @return the response bytes with the top Via header removed.
   */
  public byte[] toByteArray() {
    return m_messageBytes;
  }

  /**
   * Returns the response bytes as byte string with the top Via header removed.
   *
   * @return the response bytes as byte string with the top Via header removed.
   */
  public DsByteString toByteString() {
    return new DsByteString(m_messageBytes);
  }

  /**
   * Sets the key context.
   *
   * @param context the new key context.
   */
  public final void setKeyContext(int context) {
    if (m_key != null) {
      m_key.setKeyContext(context);
    } else {
      // for incoming messages, there should ALWAYS be key
      //  bytes
    }
  }

  /**
   * set the key after the message has been parsed.
   *
   * @param key the transaction key for this response message.
   */
  void setKey(DsSipTransactionKey key) {
    m_key = key;
  }

  /**
   * Returns the transaction key for this response message.
   *
   * @return the transaction key for this response message.
   */
  public final DsSipTransactionKey getKey() {
    return m_key;
  }

  // /////////////////////////////////////////////////
  // //////// Listener implementations  //////////////
  // /////////////////////////////////////////////////

  /*
   *  pull out the Via header parameters
   */
  public void elementFound(
      int contextId, int elementId, byte[] buffer, int offset, int count, boolean valid) {
    if (contextId != DsSipConstants.VIA) return;

    if (m_viaCount == 1) {
      if (elementId == DsSipConstants.TRANSPORT) {
        m_viaTransport = new DsByteString(buffer, offset, count);
      } else if (elementId == DsSipConstants.HOST) {
        m_viaHost = new DsByteString(buffer, offset, count);
        m_addrSource = elementId;
      } else if (elementId == DsSipConstants.PORT) {
        m_viaPort = DsSipMsgParser.parseInt(buffer, offset, count);
      }
    } else // must be the second Via
    {
      if (elementId == DsSipConstants.TRANSPORT) {
        m_2viaTransport = new DsByteString(buffer, offset, count);
      } else if (elementId == DsSipConstants.HOST) {
        m_2viaHost = new DsByteString(buffer, offset, count);
        m_addrSource = elementId;
      } else if (elementId == DsSipConstants.PORT) {
        m_2viaPort = DsSipMsgParser.parseInt(buffer, offset, count);
      }
    }
  }

  /*
   *  pull out the Via header parameters
   */
  public void parameterFound(
      int contextId, byte[] buffer, int nameOffset, int nameCount, int valueOffset, int valueCount)
      throws DsSipParserListenerException {
    if (contextId != DsSipConstants.VIA) return;
    if (m_viaCount == 1) {
      // here we should be validating the top via header
      DsByteString value = DsByteString.newLower(buffer, nameOffset, nameCount);
      if (value == DsSipConstants.BS_MADDR) {
        // in the proxy, I don't think maddr or received are used to
        //  validate that the top via is us
      } else if (value == DsSipConstants.BS_RECEIVED) {
        // in the proxy, I don't think maddr or received are used to
        //  validate that the top via is us
      }
    } else // must be the second Via
    {
      DsByteString value = DsByteString.newLower(buffer, nameOffset, nameCount);
      if (value == DsSipConstants.BS_MADDR) {
        m_2viaHost = new DsByteString(buffer, valueOffset, valueCount);
        m_addrSource = DsSipConstants.MADDR;
      } else if (value == DsSipConstants.BS_RECEIVED) {
        // don't let received override maddr
        if (m_addrSource == DsSipConstants.HOST) {
          m_2viaHost = new DsByteString(buffer, valueOffset, valueCount);
        }
        m_addrSource = DsSipConstants.RECEIVED;
      }
    }
  }

  /*
   * deep parse the first and second Via headers
   */
  public DsSipElementListener headerBegin(int headerId) {
    if (headerId == DsSipConstants.VIA) {
      ++m_viaCount;
      if (m_viaCount > 2) {
        return null;
      } else {
        return this;
      }
    }

    // -- only 200's? (we need to route the ACK)
    if (headerId == DsSipConstants.CONTACT
        && (m_messageIndex[CONTACT] == 0)
        && (m_statusCode / 100 == DsSipResponseCode.DS_SUCCESS)) {
      return this;
    }

    return null;
  }

  /*
   *  write all of the headers except the top Via header
   *  to the buffer
   */
  public void headerFound(int headerId, byte[] buffer, int offset, int count, boolean isValid) {
    // remove (don't write out) the top Via header
    if (headerId == DsSipConstants.VIA && m_viaCount == 1) {
      m_topVia = new DsByteString(buffer, offset, count);
      m_topVia.copyData();
      return;
    }

    ByteArrayOutputStream bos = getBuffer();

    DsByteString name = DsSipMsgParser.HEADER_NAMES[headerId];
    try {
      name.write(bos);
      bos.write(':');
      if (headerId == DsSipConstants.CONTACT) {
        m_messageIndex[CONTACT] = bos.size();
        m_messageIndex[CONTACT + 1] = count;
      }
      bos.write(buffer, offset, count);
      DsSipConstants.BS_EOH.write(bos);
    } catch (IOException exc) {
      // exc.printStackTrace();
    }
  }

  /*
   *  write the body to the buffer
   */
  public void messageFound(byte[] buffer, int offset, int count, boolean valid) {
    // write the empty header or the body of the message
    ByteArrayOutputStream bos = getBuffer();
    try {
      if (buffer != null && count != 0) {
        DsSipConstants.BS_EOH.write(bos);
        bos.write(buffer, offset, count);
      } else {
        DsSipConstants.BS_EOH.write(bos);
      }
    } catch (IOException ioe) {
    }
    m_messageBytes = bos.toByteArray();
  }

  /*
   * javadoc inherited
   */
  public void bodyFoundInRequestURI(byte[] buffer, int offset, int count)
      throws DsSipParserListenerException {
    // this might be an issue, but I do not think this can get called for a response.
  }

  /*
   *  write the start line  to the buffer
   */
  public DsSipMessageListener responseBegin(
      byte[] buffer, int code, int reasonOffset, int reasonCount, boolean isEncoded) {
    getBuffer().reset();

    m_messageBytes = buffer;

    m_statusCode = code;

    byte[] code_bytes = DsIntStrCache.intToBytes(code);

    try {
      ByteArrayOutputStream bos = getBuffer();
      DsSipConstants.BS_SIP_VERSION_SPACE.write(bos);
      bos.write(code_bytes, 0, code_bytes.length);
      bos.write(' ');
      m_messageIndex[REASON] = bos.size();
      m_messageIndex[REASON + 1] = reasonCount;
      bos.write(buffer, reasonOffset, reasonCount);
      DsSipConstants.BS_EOH.write(bos);
    } catch (IOException ioe) {
      // ioe.printStackTrace();
    }
    return this;
  }

  /**
   * The string representation of various elements in this object. It should be used for debug
   * purposes only.
   */
  public String toString() {
    return "Message: \n"
        + DsByteString.newString(m_messageBytes)
        + "\n"
        + "----------------\n"
        + "viaHost:      "
        + m_2viaHost
        + "\n"
        + "viaTransport: "
        + m_2viaTransport
        + "\n"
        + "viaPort:      "
        + m_2viaPort
        + "\n";
  }

  /*
  // proxy calls this to validate the the top via header is correct
  public boolean isListenInterface(String host, int port, int transport)
  {
      Enumeration sockets = getListenKeys();
       DsTransportLayer.ListenKey socket;
      InetAddress addr;

      while (sockets.hasMoreElements())
      {
            try
          {
               socket = (DsTransportLayer.ListenKey)sockets.nextElement();
              addr = InetAddress.getByName(host);
              if (socket.equals(new DsTransportLayer.ListenKey(addr, port, transport)))
              {
                    if (DsLog4j.connectionCat.isEnabledFor(Level.INFO))
                        DsLog4j.connectionCat.info("Possible loop on local interface: "+ socket);
                    return true;
              }
            }
            catch (Exception e)
          {
                          if (DsLog4j.connectionCat.isEnabledFor(Level.ERROR))
              DsLog4j.connectionCat.error("Exception in isListenInterface: ", e);
          }
      }
      return false;
  }
  */

  // /////////////////////////////////////////////////////////////////////
  // /////////////////////////////////////////////////////////////////////
  /*--
      public static final void main(String[] args) throws Exception
      {


          byte[] m0 =    ("SIP/2.0 180 Ringing\r\n" +
                          "Via: SIP/2.0/UDP ss2.wcom.com:5060;branch=721e418c4.1\r\n" +
                          "Via: SIP/2.0/UDP ss1.wcom.com:5060;branch=2d4790.1\r\n" +
                          "Via: SIP/2.0/UDP here.com:5060\r\n" +
                          "From: BigGuy <sip:UserA@here.com>\r\n" +
                          "To: LittleGuy <sip:UserB@there.com>;tag=314159\r\n" +
                          "Call-ID: 12345601@here.com\r\n" +
                          "CSeq: 1 INVITE\r\n" +
                          "Content-Length: 0\r\n\r\n").getBytes();

          byte[] m1 =    ("SIP/2.0 180 Ringing\r\n" +
                          "Via: SIP/2.0/UDP ss2.wcom.com:5060;branch=721e418c4.1,SIP/2.0/UDP ss1.wcom.com:5060;branch=2d4790.1\r\n" +
                          "Via: SIP/2.0/UDP here.com:5060\r\n" +
                          "From: BigGuy <sip:UserA@here.com>\r\n" +
                          "To: LittleGuy <sip:UserB@there.com>;tag=314159\r\n" +
                          "Call-ID: 12345601@here.com\r\n" +
                          "CSeq: 1 INVITE\r\n" +
                          "\r\n" +
                          "xxxxxxxxxxxxxxx\r\n" +
                          "yyyyyyyyyyyyyyy").getBytes();


          DsSipResponseBytes resp = new DsSipResponseBytes();
          DsSipMsgParser.parse(resp, m1, 0, m1.length);

          System.out.println("Input:");
          System.out.println("=====================");
          System.out.println(DsByteString.newString(m1));
          System.out.println("Output: resp.toSipResponse(true)");
          System.out.println("=====================");
          System.out.println(resp.toSipResponse(true));

          System.out.println("Output: resp.toSipResponse()");
          System.out.println("=====================");
          System.out.println(resp.toSipResponse());


      }
  --*/

  // /////////////////////////////////////////////////////////////////////////////////////////////////
  // /////////////////////////////////////////////////////////////////  Stubbed out
  // //////////////////
  // /////////////////////////////////////////////////////////////////////////////////////////////////
  public void requestURIFound(byte[] buffer, int offset, int count, boolean isValid)
      throws DsSipParserListenerException {}

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

  public void unknownFound(
      byte[] buffer,
      int nameOffset,
      int nameCount,
      int valueOffset,
      int valueCount,
      boolean isValid)
      throws DsSipParserListenerException {}

  public DsSipElementListener elementBegin(int contextId, int elementId)
      throws DsSipParserListenerException {
    return null;
  }

  public DsSipMessageListener requestBegin(
      byte[] buffer, int methodOffset, int methodCount, boolean isEncoded)
      throws DsSipParserListenerException {
    return null;
  }

  public DsSipHeaderListener getHeaderListener() {
    return this;
  }

  public DsSipElementListener requestURIBegin(byte[] buffer, int schemeOffset, int schemeCount)
      throws DsSipParserListenerException {
    return null;
  }
}
