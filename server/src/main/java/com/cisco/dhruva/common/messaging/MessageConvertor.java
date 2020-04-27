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

    requireNonNull(message, "fqdn");
    requireNonNull(context);

    IDhruvaMessage dhruvaMsg =
        RouteAppMessage.newBuilder()
            .withContext(context)
            .withPayload(MessageBody.fromPayloadData(message, type))
            .build();

    return dhruvaMsg;
  }

  public static DsSipMessage convertDhruvaMessageToSipMessage(IDhruvaMessage message) {
    MessageBody messageBody = message.getMessageBody();
    DsSipMessage msg = (DsSipMessage) messageBody.getPayloadData();
    return msg;
  }
}
