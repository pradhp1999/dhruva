/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.util;

import static com.cisco.dhruva.util.log.event.Event.DIRECTION.OUT;

import com.cisco.dhruva.service.MetricService;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipTransactionManager;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.util.log.event.Event;
import com.cisco.dhruva.util.log.event.Event.DIRECTION;
import com.cisco.dhruva.util.log.event.Event.MESSAGE_TYPE;
import java.util.GregorianCalendar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LMAUtil {

  private static MetricService metricService;

  @Autowired
  public LMAUtil(MetricService metricService) {
    LMAUtil.metricService = metricService;
  }

  public static void emitEventAndMetrics(
      DsSipMessage message, DsBindingInfo dsBindingInfo, DIRECTION direction) {

    MESSAGE_TYPE sipMessageType;
    String sipMethod;
    String requestUri;
    boolean midDialog = false;
    if (message.isRequest()) {
      sipMessageType = MESSAGE_TYPE.REQUEST;
      sipMethod = ((DsSipRequest) message).getMethod().toString();
      requestUri = ((DsSipRequest) message).getURI().toString();
      midDialog = message.isMidCall();

    } else {
      sipMessageType = MESSAGE_TYPE.RESPONSE;
      sipMethod = String.valueOf(((DsSipResponse) message).getStatusCode());
      requestUri = ((DsSipResponse) message).getReasonPhrase().toString();
      midDialog =
          DsSipTransactionManager.getTransactionManager()
              .getClientTransaction((DsSipResponse) message)
              .map(dsSipClientTransaction -> dsSipClientTransaction.getRequest())
              .map(dsSipRequest -> dsSipRequest.isMidCall())
              .orElse(false);
    }

    long dhruvaProcessingDelayInMillis = 0;
    if (direction == OUT && !message.isInternallyGenerated()) {
      dhruvaProcessingDelayInMillis =
          new GregorianCalendar().getTimeInMillis() - message.getTimestamp().getTimeInMillis();
    }

    Event.emitMessageEvent(
        dsBindingInfo,
        message,
        direction,
        sipMessageType,
        sipMethod,
        requestUri,
        midDialog,
        dhruvaProcessingDelayInMillis);

    metricService.sendSipMessageMetric(
        sipMethod,
        message.getCallId().toString(),
        message.getCSeqMethod().toString(),
        sipMessageType,
        message.getConnectionTransport(),
        direction,
        midDialog,
        message.isInternallyGenerated(),
        dhruvaProcessingDelayInMillis);
  }
}
