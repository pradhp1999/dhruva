/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.util.log.event;

import static com.cisco.dhruva.util.log.event.Event.DIRECTION.OUT;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;

public class Event {

  private static final String ISMIDDIALOG = "isMidDialog";
  private static final String DHRUVA_PROCESSING_DELAY_IN_MILLIS = "dhruvaProcessingDelayInMillis";
  public static String DIRECTION = "direction";
  public static String REMOTEIP = "remoteIp";
  public static String REMOTEPORT = "remotePort";
  public static String LOCALIP = "localIp";
  public static String LOCALPORT = "localPort";

  private static Logger logger = DhruvaLoggerFactory.getLogger(Event.class);

  public enum EventType {
    CONNECTION,
    SIPMESSAGE
  }

  public enum MESSAGE_TYPE {
    REQUEST,
    RESPONSE
  }

  public enum DIRECTION {
    IN,
    OUT
  }

  public enum EventSubType {
    UDPCONNECTION(EventType.CONNECTION),
    TCPCONNECTION(EventType.CONNECTION),
    TLSCONNECTION(EventType.CONNECTION);
    private EventType eventType;

    EventSubType(EventType eventType) {
      this.eventType = eventType;
    }

    public EventType getEventType() {
      return this.eventType;
    }
  }

  public static void emitMessageEvent(
      DsBindingInfo messageBindingInfo,
      DsSipMessage message,
      DIRECTION direction,
      MESSAGE_TYPE sipMessageType,
      String sipMethod,
      String requestUri,
      boolean isMidDialog,
      long dhruvaProcessingDelayInMillis) {
    Map<String, String> messageInfoMap =
        Maps.newHashMap(
            ImmutableMap.of(
                "sipMessageType",
                sipMessageType.name(),
                "sipMethod",
                sipMethod,
                "cseqMethod",
                message.getCSeqMethod().toString(),
                "requestUri",
                requestUri,
                Event.REMOTEIP,
                messageBindingInfo.getRemoteAddressStr()));
    messageInfoMap.put(Event.REMOTEPORT, String.valueOf(messageBindingInfo.getRemotePort()));
    messageInfoMap.put(Event.DIRECTION, direction.name());
    messageInfoMap.put(Event.LOCALIP, messageBindingInfo.getLocalAddress().getHostAddress());
    messageInfoMap.put(Event.LOCALPORT, String.valueOf(messageBindingInfo.getLocalPort()));
    messageInfoMap.put(Event.ISMIDDIALOG, String.valueOf(isMidDialog));
    if (direction == OUT) {
      messageInfoMap.put(
          Event.DHRUVA_PROCESSING_DELAY_IN_MILLIS, String.valueOf(dhruvaProcessingDelayInMillis));
    }

    logger.emitEvent(EventType.SIPMESSAGE, null, message.toString(), messageInfoMap);
  }
}
