// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.net.InetAddress;
import org.slf4j.event.Level;

/**
 * Helper class that implements DsUnitOfWork and holds one unparsed SIP message. When process() is
 * called the Transaction Manager is called, which in turn calls the message parser. Two major
 * things are accomplished by the addition of this class. The Transaction Manager can now catch
 * parsing errors directly and handle them accordingly, and the parsing of TCP (stream) based
 * messages is not truly two stage. The framing happens and then an instance of an object from this
 * class is put onto the work queue for parsing, later.
 */
public class DsStunMessageBytes extends DsMessageBytes {
  // 0x0101  :  Binding Response
  // 2 byte length? (12x3 = 36 bytes)
  // trans id - last 16 bytes from request
  //
  // 0x0001: MAPPED-ADDRESS - 4 bytes + 8 bytes
  // 0x0004: SOURCE-ADDRESS - 4 bytes + 8 bytes
  // 0x0005: CHANGED-ADDRESS - 4 bytes + 8 bytes

  private static final int STUN_HEADER_LENGTH = 20;
  private static final byte ADDRESS_TYPE_LENGTH = (byte) 12;
  private static final byte ADDRESS_BODY_LENGTH = (byte) 8;
  private static final byte STUN_BODY_LENGTH = (byte) (ADDRESS_TYPE_LENGTH * 3);

  // private static final int BINDING_REQUEST_CODE = 0x0001;
  // private static final int BINDING_RESPONSE_CODE = 0x0101;
  // private static final int BINDING_ERROR_RESPONSE_CODE = 0x0111;
  // private static final int SHARED_SECRET_REQUEST_CODE = 0x0002;
  // private static final int SHARED_SECRET_RESPONSE_CODE = 0x0102;
  // private static final int SHARED_SECRET_ERROR_RESPONSE_CODE = 0x0112;

  private static final int TRANSACTION_ID_START = 4;
  private static final byte TRANSACTION_ID_LENGTH = (byte) 16;

  private static final byte IPv4 = 0x01;
  private static final int MAPPED_ADDRESS_START = STUN_HEADER_LENGTH;
  private static final int SOURCE_ADDRESS_START = MAPPED_ADDRESS_START + ADDRESS_TYPE_LENGTH;
  private static final int CHANGED_ADDRESS_START = SOURCE_ADDRESS_START + ADDRESS_TYPE_LENGTH;

  private static final byte MAPPED_ADDRESS = (byte) 0x01;
  private static final byte SOURCE_ADDRESS = (byte) 0x04;
  private static final byte CHANGED_ADDRESS = (byte) 0x05;

  private static final int TLV_HEADER_LENGTH = 4;
  private Logger logger = DhruvaLoggerFactory.getLogger(DsStunMessageBytes.class);

  /**
   * Constructor that takes the message and its binding information.
   *
   * @param bytes the message in its raw form
   * @param bi the binding information for this message
   */
  public DsStunMessageBytes(byte bytes[], DsBindingInfo bi) {
    super(bytes, bi);
  }

  public void process() {
    try {
      if (m_msgBytes[1] != 1) {
        // this is not a STUN Binding Request - log and ignore

        // log

        return;
      }

      byte[] respBytes = createResponseTemplate(m_msgBytes);

      // The STUN spec says to:
      //
      //     Several STUN attributes are defined.  The first is a MAPPED-ADDRESS
      //     attribute, which is an IP address and port.  It is always placed in
      //     the Binding Response, and it indicates the source IP address and port
      //     the server saw in the Binding Request.
      //
      // I believe this means to get the remote address and port out of the
      // Binding Info and echo it back, hence the other side will learn what port
      // the NAT has allocated. - jsm

      // respBytes[MAPPED_ADDRESS_START + TLV_HEADER_LENGTH] = N/A; - explicitly not used

      // all of these are IPv4, so set them all at once
      respBytes[MAPPED_ADDRESS_START + TLV_HEADER_LENGTH + 1] =
          respBytes[SOURCE_ADDRESS_START + TLV_HEADER_LENGTH + 1] =
              respBytes[CHANGED_ADDRESS_START + TLV_HEADER_LENGTH + 1] = IPv4;

      writePort(
          m_bindingInfo.getRemotePort(), respBytes, MAPPED_ADDRESS_START + TLV_HEADER_LENGTH + 2);
      writeIP(
          m_bindingInfo.getRemoteAddress(),
          respBytes,
          MAPPED_ADDRESS_START + TLV_HEADER_LENGTH + 4);

      // The STUN spec says:
      //
      //    The fifth attribute is the SOURCE-ADDRESS attribute.  It is only
      //    present in Binding Responses.  It indicates the source IP address and
      //    port where the response was sent from.  It is useful for detecting
      //    twice NAT configurations.
      writePort(
          m_bindingInfo.getLocalPort(), respBytes, SOURCE_ADDRESS_START + TLV_HEADER_LENGTH + 2);
      writeIP(
          m_bindingInfo.getLocalAddress(), respBytes, SOURCE_ADDRESS_START + TLV_HEADER_LENGTH + 4);

      // This seems to be the "other" IP and port that we are listening for STUN Requests on
      // For now - just HACK in the one port we are listening on, this one - jsm
      writePort(
          m_bindingInfo.getLocalPort(), respBytes, CHANGED_ADDRESS_START + TLV_HEADER_LENGTH + 2);
      writeIP(
          m_bindingInfo.getLocalAddress(),
          respBytes,
          CHANGED_ADDRESS_START + TLV_HEADER_LENGTH + 4);

      // if (DsLog4j.wireCat.isEnabledFor(Level.DEBUG))
      // {
      // DsLog4j.wireCat.log(Level.DEBUG,
      // "Sending STUN response:\n" +
      // DsString.toStunDebugString(respBytes) + "\n");
      // }
      sendResponse(respBytes);
    } catch (Exception e) {
      if (DsLog4j.connectionCat.isEnabled(Level.WARN)) {
        DsLog4j.connectionCat.warn("Exception building STUN response: ", e);
      }
    }
  }

  private void sendResponse(byte[] data) {
    try {
      DsSipTransportLayer tl = DsSipTransactionManager.getTransportLayer();

      // here we get the connection using a listening point for sending data
      DsConnection conn =
          tl.getConnection(
              m_bindingInfo.getNetwork(),
              m_bindingInfo.getLocalAddress(),
              m_bindingInfo.getLocalPort(),
              m_bindingInfo.getRemoteAddress(),
              m_bindingInfo.getRemotePort(),
              m_bindingInfo.getTransport(),
              true);

      // update the timestamp corresponding to this UDP connection.
      // required for persistence connections to work.
      if (DsLog4j.wireCat.isEnabled(Level.DEBUG)) {
        DsLog4j.wireCat.log(
            Level.DEBUG,
            "STUN - update time stamp for connection with binding info = " + m_bindingInfo);
      }

      ((DsAbstractConnection) conn).updateTimeStamp();
      ((DsAbstractConnection) conn).sendSync(data);
      logger.info("Sent Stun messages {} ", new String(data));
    } catch (Exception e) {
      if (DsLog4j.connectionCat.isEnabled(Level.WARN)) {
        DsLog4j.connectionCat.warn("Exception sending STUN response: ", e);
      }
    }
  }

  private static void writeIP(InetAddress addr, byte[] bytes, int index) {
    System.arraycopy(addr.getAddress(), 0, bytes, index, 4);
  }

  private static void writePort(int port, byte[] bytes, int index) {
    short shortPort = (short) port;

    bytes[index] = (byte) (shortPort >> 8);
    bytes[index + 1] = (byte) (shortPort & 0x00ff);
  }

  private static byte[] createResponseTemplate(byte[] msgBytes) {
    byte[] respBytes = new byte[STUN_HEADER_LENGTH + STUN_BODY_LENGTH];

    // 2 byte type
    respBytes[0] = respBytes[1] = 1;

    // 2 byte length
    // respBytes[2] = 0; - already 0
    respBytes[3] = STUN_BODY_LENGTH;

    System.arraycopy(
        msgBytes, TRANSACTION_ID_START, respBytes, TRANSACTION_ID_START, TRANSACTION_ID_LENGTH);

    // these are already 0 at array init time
    // respBytes[MAPPED_ADDRESS_START] = 0;
    // respBytes[MAPPED_ADDRESS_START + 2] = 0;
    // respBytes[SOURCE_ADDRESS_START] = 0;
    // respBytes[SOURCE_ADDRESS_START + 2] = 0;
    // respBytes[CHANGED_ADDRESS_START] = 0;
    // respBytes[CHANGED_ADDRESS_START + 2] = 0;

    respBytes[MAPPED_ADDRESS_START + 1] = MAPPED_ADDRESS;
    respBytes[SOURCE_ADDRESS_START + 1] = SOURCE_ADDRESS;
    respBytes[CHANGED_ADDRESS_START + 1] = CHANGED_ADDRESS;

    // set these all at once since they are all the same value
    respBytes[MAPPED_ADDRESS_START + 3] =
        respBytes[SOURCE_ADDRESS_START + 3] =
            respBytes[CHANGED_ADDRESS_START + 3] = ADDRESS_BODY_LENGTH;

    return respBytes;
  }

  public void run() {
    process();
  }

  public void abort() {}
}
