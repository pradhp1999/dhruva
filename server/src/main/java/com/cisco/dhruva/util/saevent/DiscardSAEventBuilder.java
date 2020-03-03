package com.cisco.dhruva.util.saevent;

import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.Trace;

/** General logging category. */
public class DiscardSAEventBuilder {
  private static final Trace saEventLog = Trace.getTrace("com.cisco.CloudProxy.SAEvent");
  private static long lastNotificationSentForSipMessageTooLarge = 0;
  /** General logging category. */
  private static final Trace Log = Trace.getTrace(DiscardSAEventBuilder.class.getName());
  /**
   * Populates SAEvent and Alarm for too large Sip Message. Sample format for the Event sent to EMS
   * - Event Received - Transport Type, Remote Ip, remote Port, Local IP, Local Port , Packet Size
   */
  public static void tooLargeSipMessageSAEventAlarm(
      Transport transportType,
      String remoteIPAddress,
      int remotePort,
      String localIPAddress,
      int localPort,
      int size) {
    long currentTime = System.currentTimeMillis() / 1000;

    if ((currentTime - lastNotificationSentForSipMessageTooLarge) < 60) {
      if (Log.on && Log.isTraceEnabled()) {
        Log.trace("tooLargeSipMessageSAEventAlarm: Dropping Sip Message!");
      }
      return;
    }
    if (Log.on && Log.isTraceEnabled()) {
      Log.trace(
          "tooLargeSipMessageSAEventAlarm: Dropping Sip Message, Generate SAEVENT and Alarm.");
    }
    lastNotificationSentForSipMessageTooLarge = System.currentTimeMillis() / 1000;
    remoteIPAddress = remoteIPAddress.replaceAll("/", "");
    localIPAddress = localIPAddress.replaceAll("/", "");
    // REFACTOR

    //        try {
    //    		SIPMessageTooLargeDataParam dataParam = new SIPMessageTooLargeDataParam.Builder()
    //	    		.transportType(transportType)
    //	    		.remoteIPAddress(remoteIPAddress)
    //	    		.remotePort(remotePort)
    //	    		.localIPAddress(localIPAddress)
    //	    		.localPort(localPort)
    //	    		.packetSize(size)
    //	    		.build();
    //
    //	SIPSessionsMBeanImpl.getInstance().sendNotification(SAEventConstants.SIP_MESSAGE_TOO_LARGE,
    // dataParam);
    //
    //        } catch (Throwable t) {
    //        	if (Log.on && Log.isErrorEnabled())
    //                   Log.error("Error while creating and sending Too Large Sip message event
    // notification", t);
    //        }
  }
}
