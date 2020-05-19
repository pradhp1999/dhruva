package com.cisco.dhruva.common.messaging;

import static java.util.Objects.requireNonNull;

import com.cisco.dhruva.common.context.ExecutionContext;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.common.messaging.models.MessageBody;
import com.cisco.dhruva.common.messaging.models.MessageBodyType;
import com.cisco.dhruva.common.messaging.models.RouteAppMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;

public class MessageConvertor {

  public static IDhruvaMessage convertSipMessageToDhruvaMessage(
      DsSipMessage message, MessageBodyType type, ExecutionContext context) {

    requireNonNull(message, "sip message should not be null");
    requireNonNull(context);

    return RouteAppMessage.newBuilder()
        .withContext(context)
        .withPayload(MessageBody.fromPayloadData(message, type))
        .build();
  }

  public static DsSipMessage convertDhruvaMessageToSipMessage(IDhruvaMessage message) {
    requireNonNull(message, "dhruva message cannot be null");
    MessageBody messageBody = message.getMessageBody();
    return (DsSipMessage) messageBody.getPayloadData();
  }
}
