/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.util.log.event;


import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;

public class Event {

  public static String DIRECTION = "direction";
  public static String REMOTEIP = "remoteIp";
  public static String REMOTEPORT = "remotePort";
  public static String LOCALIP = "localIp";
  public static String LOCALPORT = "localPort";
  public static String IN = "IN";
  public static String OUT = "OUT";

  private static Logger logger = DhruvaLoggerFactory.getLogger(Event.class);

  public enum EventType {
    CONNECTION, SIPMESSAGE
  }

  public enum EventSubType {
    UDPCONNECTION(EventType.CONNECTION), TCPCONNECTION(EventType.CONNECTION), TLSCONNECTION(
        EventType.CONNECTION);
    private EventType eventType;

    EventSubType(EventType eventType) {
      this.eventType = eventType;
    }

    public EventType getEventType() {
      return this.eventType;
    }
  }


  public static void emitMessageEvent(DsBindingInfo messageBindingInfo, DsSipMessage message,
      String direction) {
    String sipMessageType, sipMethod, requestUri;
    if (message.isRequest()) {
      sipMessageType = "request";
      sipMethod = ((DsSipRequest) message).getMethod().toString();
      requestUri = ((DsSipRequest) message).getURI().toString();

    } else {
      sipMessageType = "response";
      sipMethod = String.valueOf(((DsSipResponse) message).getStatusCode());
      requestUri = ((DsSipResponse) message).getReasonPhrase().toString();
    }

    Map<String,String> messageInfoMap = Maps.newHashMap(ImmutableMap.of("sipMessageType", sipMessageType,
        "sipMethod", sipMethod, "cseqMethod", message.getCSeqMethod().toString(), "requestUri",
        requestUri,
        Event.REMOTEIP, messageBindingInfo.getRemoteAddressStr()));
    messageInfoMap.put(Event.REMOTEPORT, String.valueOf(messageBindingInfo.getRemotePort()));
    messageInfoMap.put(Event.DIRECTION, direction);
    messageInfoMap.put(Event.LOCALIP,messageBindingInfo.getLocalAddress().getHostAddress());
    messageInfoMap.put(Event.LOCALPORT,String.valueOf(messageBindingInfo.getLocalPort()));

    logger.emitEvent(EventType.SIPMESSAGE, Optional.empty(), message.toString(), Optional.of(messageInfoMap));
  }


}