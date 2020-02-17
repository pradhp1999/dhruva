// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import org.apache.logging.log4j.Level;

/** */
public class DsEmptyMessageBytes extends DsMessageBytes {
  /** The CRLF message to send back to the sender. */
  private static byte[] m_emptyResponse = {(byte) '\r', (byte) '\n'};

  private static boolean m_sendResponse =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_RESPOND_TO_EMPTY_MESSAGES,
          DsConfigManager.PROP_RESPOND_TO_EMPTY_MESSAGES_DEFAULT);

  /**
   * Constructor that takes the message and its binding information.
   *
   * @param bytes the message in its raw form (should be 0 length)
   * @param bi the binding information for this message
   */
  public DsEmptyMessageBytes(byte bytes[], DsBindingInfo bi) {
    super(bytes, bi);
  }

  public void process() {
    sendResponse();
  }

  public void run() {
    process();
  }

  public void abort() {}

  private void sendResponse() {
    try {
      DsSipTransportLayer tl = DsSipTransactionManager.getTransportLayer();
      DsNetwork network = m_bindingInfo.getNetwork();
      if (network == null) {
        network = DsNetwork.getDefault();
        if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
          DsLog4j.connectionCat.debug(
              "Using Default Network to send Empty Response due to null network in binding info");
        }
      }
      DsConnection conn =
          tl.getConnection(
              network,
              m_bindingInfo.getLocalAddress(),
              m_bindingInfo.getLocalPort(),
              m_bindingInfo.getRemoteAddress(),
              m_bindingInfo.getRemotePort(),
              m_bindingInfo.getTransport(),
              true);

      // update the timestamp corresponding to this UDP connection.
      // required for persistence connections to work.
      if (DsLog4j.wireCat.isEnabled(Level.INFO)) {
        DsLog4j.wireCat.log(
            Level.INFO, "update time stamp for connection with binding info = " + m_bindingInfo);
      }

      ((DsAbstractConnection) conn).updateTimeStamp();
      if (m_sendResponse) {
        conn.send(m_emptyResponse);
      }
    } catch (Exception e) {
      if (DsLog4j.connectionCat.isEnabled(Level.WARN)) {
        DsLog4j.connectionCat.warn("Exception sending Empty response: ", e);
      }
    }
  }
}
