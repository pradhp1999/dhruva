// Copyright (c) 2005-2010 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipHeaderInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import org.apache.logging.log4j.ThreadContext;

/**
 * Encapsulates the standard log4j library to provide for the logging functionality and in turn
 * defines and configures stack specific logging categories.
 */
public class DsLog4j {
  private static final String REMOTESTRING = ";remote=";
  private static final String SESSIONID_TEMPLATE = "SessionId";

  /**
   * Serves as the base logger for all the defined categories in the stack. The name of this base
   * logger is <b> com.dynamicsoft.DsLibs.DsUALibs </b> .
   */
  public static Logger baseCat = DhruvaLoggerFactory.getLogger("com.dynamicsoft.DsLibs.DsUALibs");

  /**
   * Specifies the logger where various informations like transactions in the transaction table, the
   * available transports and their properties, the timers, etc are logged. The name of this logger
   * is <b> com.dynamicsoft.DsLibs.DsUALibs.Dump </b> .
   */
  public static Logger dumpCat =
      DhruvaLoggerFactory.getLogger("com.dynamicsoft.DsLibs.DsUALibs.Dump");

  /**
   * Specifies the logger where various configuration settings of the stack are logged. The name of
   * this logger is <b> com.dynamicsoft.DsLibs.DsUALibs.Config </b> .
   */
  public static Logger configCat =
      DhruvaLoggerFactory.getLogger("com.dynamicsoft.DsLibs.DsUALibs.Config");
  /**
   * Specifies the logger where thread specific information is logged. This includes information
   * related to Queue Worker Threads and Timer Threads. The name of this logger is <b>
   * com.dynamicsoft.DsLibs.DsUALibs.DsUtil.Thread </b> .
   */
  public static Logger threadCat =
      DhruvaLoggerFactory.getLogger("com.dynamicsoft.DsLibs.DsUALibs.DsUtil.Thread");
  /**
   * Specifies the logger where network socket specific information is logged. The name of this
   * logger is <b> com.dynamicsoft.DsLibs.DsUALibs.DsUtil.Socket </b> .
   */
  public static Logger socketCat =
      DhruvaLoggerFactory.getLogger("com.dynamicsoft.DsLibs.DsUALibs.DsUtil.Socket");
  /**
   * Specifies the logger where SIP Headers specific information is logged. The name of this logger
   * is <b> com.dynamicsoft.DsLibs.DsUALibs.DsSipObject.Header </b> .
   */
  public static Logger headerCat =
      DhruvaLoggerFactory.getLogger("com.dynamicsoft.DsLibs.DsUALibs.DsSipObject.Header");
  /**
   * Specifies the logger where SIP Message specific information is logged. The name of this logger
   * is <b> com.dynamicsoft.DsLibs.DsUALibs.DsSipObject.Message </b> .
   */
  public static Logger messageCat =
      DhruvaLoggerFactory.getLogger("com.dynamicsoft.DsLibs.DsUALibs.DsSipObject.Message");
  /**
   * Specifies the logger where information regarding incoming and outgoing SIP messages through the
   * network is logged. The name of this logger is <b>
   * com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.Wire </b> .
   */
  public static Logger wireCat =
      DhruvaLoggerFactory.getLogger("com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.Wire");

  /** Special IN / OUT logger. */
  public static Logger inoutCat = DhruvaLoggerFactory.getLogger("SIP.INOUT");

  /**
   * Specifies the logger where information regarding connection associations is logged. The name of
   * this logger is <b> com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.ConnectionAssociation</b>
   */
  public static Logger connAssocCat =
      DhruvaLoggerFactory.getLogger(
          "com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.ConnectionAssociation");

  /**
   * Specifies the logger where information regarding the resolution of URLs to a set of network
   * addresses is logged. The name of this logger is <b>
   * com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.Resolver </b> .
   */
  public static Logger resolvCat =
      DhruvaLoggerFactory.getLogger("com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.Resolver");
  /**
   * Specifies the logger where information regarding the performance metrics is logged. The name of
   * this logger is <b> com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.Perf </b> .
   */
  public static Logger perfCat =
      DhruvaLoggerFactory.getLogger("com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.Perf");
  /**
   * Specifies the logger where information regarding the network connections is logged. The name of
   * this logger is <b> com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.Connection </b> .
   */
  public static Logger connectionCat =
      DhruvaLoggerFactory.getLogger("com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.Connection");
  /**
   * Specifies the logger where information regarding the network connections management is logged.
   * The name of this logger is <b> com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.ConnectionManagement
   * </b> .
   */
  public static Logger connectionMCat =
      DhruvaLoggerFactory.getLogger(
          "com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.ConnectionManagement");

  /**
   * Specifies the base logger for all the state machine logging events. The name of this logger is
   * <b> com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.LlSM </b> .
   */
  public static Logger LlSMCat =
      DhruvaLoggerFactory.getLogger("com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.LlSM");

  /**
   * Specifies the logger for all the client state machine logging events. The name of this logger
   * is <b> com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.LlSM.client </b> .
   */
  public static Logger LlSMClientCat =
      DhruvaLoggerFactory.getLogger("com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.LlSM.client");
  /**
   * Specifies the logger for the state switching logging events in the client state machine. The
   * name of this logger is <b> com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.LlSM.client.SwitchState
   * </b> .
   */
  public static Logger LlSMClientSwitchStateCat =
      DhruvaLoggerFactory.getLogger(
          "com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.LlSM.client.SwitchState");
  /**
   * Specifies the logger for the timers logging events in the client state machine. The name of
   * this logger is <b> com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.LlSM.client.Timers </b> .
   */
  public static Logger LlSMClientTimersCat =
      DhruvaLoggerFactory.getLogger(
          "com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.LlSM.client.Timers");
  /**
   * Specifies the logger for the user callback logging events in the client state machine. The name
   * of this logger is <b> com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.LlSM.client.UserCB </b> .
   */
  public static Logger LlSMClientUserCBCat =
      DhruvaLoggerFactory.getLogger(
          "com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.LlSM.client.UserCB");

  /**
   * Specifies the logger for all the server state machine logging events. The name of this logger
   * is <b> com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.LlSM.server </b> .
   */
  public static Logger LlSMServerCat =
      DhruvaLoggerFactory.getLogger("com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.LlSM.server");
  /**
   * Specifies the logger for the state switching logging events in the server state machine. The
   * name of this logger is <b> com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.LlSM.server.SwitchState
   * </b> .
   */
  public static Logger LlSMServerSwitchStateCat =
      DhruvaLoggerFactory.getLogger(
          "com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.LlSM.server.SwitchState");
  /**
   * Specifies the logger for the timers logging events in the server state machine. The name of
   * this logger is <b> com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.LlSM.server.Timers </b> .
   */
  public static Logger LlSMServerTimersCat =
      DhruvaLoggerFactory.getLogger(
          "com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.LlSM.server.Timers");
  /**
   * Specifies the logger for the user callback logging events in the server state machine. The name
   * of this logger is <b> com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.LlSM.server.UserCB </b> .
   */
  public static Logger LlSMServerUserCBCat =
      DhruvaLoggerFactory.getLogger(
          "com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.LlSM.server.UserCB");

  /**
   * Specifies the logger for the transaction key logging events. The name of this logger is <b>
   * com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.TransactionKey </b> .
   */
  public static Logger transKeyCat =
      DhruvaLoggerFactory.getLogger("com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.TransactionKey");

  /**
   * Specifies the base logger for all the transaction management logging events. The name of this
   * logger is <b> com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.TransactionManagement </b> .
   */
  public static Logger transMCat =
      DhruvaLoggerFactory.getLogger(
          "com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.TransactionManagement");
  /**
   * Specifies the logger for all the Request specific transaction management logging events. The
   * name of this logger is <b>
   * com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.TransactionManagement.Request </b> .
   */
  public static Logger transMReqCat =
      DhruvaLoggerFactory.getLogger(
          "com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.TransactionManagement.Request");
  /**
   * Specifies the logger for only the Cancel specific transaction management logging events. The
   * name of this logger is <b>
   * com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.TransactionManagement.Cancel </b> .
   */
  public static Logger transMCancelCat =
      DhruvaLoggerFactory.getLogger(
          "com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.TransactionManagement.Cancel");

  // CAFFEINE 2.0 (EDCS29531) PRACK Support
  /**
   * Specifies the logger for only the Prack specific transaction management logging events. The
   * name of this logger is <b>
   * com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.TransactionManagement.Prack </b> .
   */
  public static Logger transMPrackCat =
      DhruvaLoggerFactory.getLogger(
          "com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.TransactionManagement.Prack");
  /**
   * Specifies the logger for only the Ack specific transaction management logging events. The name
   * of this logger is <b> com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.TransactionManagement.Ack </b>
   * .
   */
  public static Logger transMAckCat =
      DhruvaLoggerFactory.getLogger(
          "com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.TransactionManagement.Ack");
  /**
   * Specifies the logger for all the Response specific transaction management logging events. The
   * name of this logger is <b>
   * com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.TransactionManagement.Response </b> .
   */
  public static Logger transMRespCat =
      DhruvaLoggerFactory.getLogger(
          "com.dynamicsoft.DsLibs.DsUALibs.DsSipLlApi.TransactionManagement.Response");

  /**
   * Specifies the logger for all the Mid-Level call management logging events. The name of this
   * logger is <b> com.dynamicsoft.DsLibs.DsUALibs.DsSipMlApi.CallManagement </b> .
   */
  public static Logger callMCat =
      DhruvaLoggerFactory.getLogger("com.dynamicsoft.DsLibs.DsUALibs.DsSipMlApi.CallManagement");
  /**
   * Specifies the logger for all the Mid-Level call state logging events. The name of this logger
   * is <b> com.dynamicsoft.DsLibs.DsUALibs.DsSipMlApi.callState </b> .
   */
  public static Logger callStateCat =
      DhruvaLoggerFactory.getLogger("com.dynamicsoft.DsLibs.DsUALibs.DsSipMlApi.callState");
  /**
   * Specifies the logger for the Registration specific logging events in the Mid-Level API. The
   * name of this logger is <b> com.dynamicsoft.DsLibs.DsUALibs.DsSipMlApi.Registration </b> .
   */
  public static Logger registrationCat =
      DhruvaLoggerFactory.getLogger("com.dynamicsoft.DsLibs.DsUALibs.DsSipMlApi.Registration");
  /*-- Not used, and I don't think we have any plans for High-Level APIs.
      public static Logger HlCallCat = Logger.getLogger("com.dynamicsoft.DsLibs.DsUALibs.DsSipHlApi.HlCall");
      public static Logger HlCallMCat = Logger.getLogger("com.dynamicsoft.DsLibs.DsUALibs.DsSipHlApi.HlCallManagement");
  --*/

  /**
   * Specifies the logger for the general exceptions logging events in the stack. The name of this
   * logger is <b> com.dynamicsoft.DsLibs.DsUALibs.Exception </b> .
   */
  public static Logger exceptionCat =
      DhruvaLoggerFactory.getLogger("com.dynamicsoft.DsLibs.DsUALibs.Exception");

  /**
   * Specifies the logger for the authentication specific logging events. The name of this logger is
   * <b> com.dynamicsoft.DsLibs.DsUALibs.Authentication </b> .
   */
  public static Logger authCat =
      DhruvaLoggerFactory.getLogger("com.dynamicsoft.DsLibs.DsUALibs.Authentication");

  /**
   * Specifies the logger for the Events Package specific logging events. The name of this logger is
   * <b> com.dynamicsoft.DsLibs.DsUALibs.DsSipEvents.events </b> .
   */
  public static Logger eventsCat =
      DhruvaLoggerFactory.getLogger("com.dynamicsoft.DsLibs.DsUALibs.DsSipEvents.events");

  /**
   * Specifies the logger for the Message Statistics Package specific logging events. The name of
   * this logger is <b> com.dynamicsoft.DsLibs.DsUALibs.DsUtil.DsMessageStatistics </b> .
   */

  // CAFFEINE 2.0 MessageStatis changes add refer count, mime count
  public static Logger msgStatsCat =
      DhruvaLoggerFactory.getLogger("com.dynamicsoft.DsLibs.DsUALibs.DsUtil.DsMessageStatistics");

  /**
   * Specifies the logger for the Refer Package specific logging events. The name of this logger is
   * <b> com.dynamicsoft.DsLibs.DsUALibs.DsSipRefer.refer</b> .
   */
  public static Logger referCat =
      DhruvaLoggerFactory.getLogger("com.dynamicsoft.DsLibs.DsUALibs.DsSipRefer.refer");

  /**
   * Specifies the logger for the MIME Package specific logging events. The name of this logger is
   * <b> com.dynamicsoft.DsLibs.DsUALibs.DsSipMime.mime</b> .
   */
  public static Logger mimeCat =
      DhruvaLoggerFactory.getLogger("com.dynamicsoft.DsLibs.DsUALibs.DsSipMime.mime");

  /**
   * Specifies the logger for offer/answer specific logging events. The name of this logger is <b>
   * com.dynamicsoft.DsLibs.DsUALibs.DsSipDialog.OfferAnswer</b> .
   */
  public static Logger oaCat =
      DhruvaLoggerFactory.getLogger("com.dynamicsoft.DsLibs.DsUALibs.DsSipDialog.OfferAnswer");

  /**
   * Specifies the logger for SAEvent specific logging Events The name of this logger is <b>
   * com.cisco.dhruva.util.saevent.SAEvent</b> .
   */
  public static Logger saEventTraceLog =
      DhruvaLoggerFactory.getLogger("com.cisco.dhruva.util.saevent.SAEvent");

  /**
   * Use the special IN / OUT logger to log a SIP message. Only the first
   * DsConfigManager.getInOutLogMsgSize() bytes of the SIP message are logger, after the CRLFs are
   * replaced with a single space.
   *
   * @param inbound <code>true</code> for an IN message and <code>false</code> for an OUT message
   * @param info the binding info for the SIP message, must not be <code>null</code>
   * @param data the SIP message being logged
   */
  public static void logInOutMessage(boolean inbound, DsBindingInfo info, byte[] data) {
    logInOutMessage(
        inbound,
        DsSipTransportType.getTypeAsByteString(info.getTransport().ordinal()).toString(),
        info.getRemoteAddress().getHostAddress(),
        info.getRemotePort(),
        info.getLocalAddress().getHostAddress(),
        info.getLocalPort(),
        data);
  }

  /**
   * Use the special IN / OUT logger to log a SIP message. Only the first
   * DsConfigManager.getInOutLogMsgSize() bytes of the SIP message are logger, after the CRLFs are
   * replaced with a single space.
   *
   * @param inbound <code>true</code> for an IN message and <code>false</code> for an OUT message
   * @param transport the transport used
   * @param remoteaddr address of the remote side
   * @param remoteport the remote port
   * @param localaddr address of the local side
   * @param localport the local port
   * @param data the SIP message being logged
   */
  public static void logInOutMessage(
      boolean inbound,
      String transport,
      String remoteaddr,
      int remoteport,
      String localaddr,
      int localport,
      byte[] data) {
    int numBytes = DsConfigManager.getInOutLogMsgSize();

    if (numBytes > data.length) {
      numBytes = data.length;
    }

    // Remove carriage returns and only display the first DsConfigManager.getInOutLogMsgSize() bytes
    // of the message
    String pStr = DsByteString.newString(data, 0, numBytes).replaceAll("\r\n", "|");
    StringBuffer sb = new StringBuffer(numBytes + 200);
    if (inbound) {
      sb.append("IN ")
          .append(" ")
          .append(transport)
          .append(" [")
          .append(remoteaddr)
          .append(":")
          .append(remoteport)
          .append("] >> [")
          .append(localaddr)
          .append(":")
          .append(localport)
          .append("] ")
          .append(pStr);
    } else {
      sb.append("OUT")
          .append(" ")
          .append(transport)
          .append(" [")
          .append(localaddr)
          .append(":")
          .append(localport)
          .append("] >> [")
          .append(remoteaddr)
          .append(":")
          .append(remoteport)
          .append("] ")
          .append(pStr);
    }

    DsLog4j.inoutCat.debug(DsSipMessage.maskAndWrapSIPMessageToSingleLineOutput(sb.toString()));
  }

  /**
   * Logs the session ID in MDC, this can be accessed by specifying a pattern
   *
   * @param message
   */
  public static boolean logSessionId(DsSipMessage message) {
    boolean logSuccess = false;
    if (message != null) {
      DsSipHeaderInterface sessionIDHeader = message.getHeader(new DsByteString("Session-ID"));
      if (sessionIDHeader != null) {

        DsByteString sessionIdHeaderValue = sessionIDHeader.getValue();
        if (sessionIdHeaderValue != null) {
          String sessionId = sessionIdHeaderValue.toString();
          if (sessionId.contains(REMOTESTRING)) {
            String[] tempString = sessionId.split(REMOTESTRING);
            if (tempString.length >= 2) {
              ThreadContext.put(SESSIONID_TEMPLATE, tempString[0] + "/" + tempString[1]);
              logSuccess = true;
            } else if (tempString.length == 1) {
              ThreadContext.put(SESSIONID_TEMPLATE, tempString[0]);
              logSuccess = true;
            }
          } else {
            ThreadContext.put(SESSIONID_TEMPLATE, sessionId);
            logSuccess = true;
          }
        }
      }
    }
    return logSuccess;
  }
} // class DsLog4j
