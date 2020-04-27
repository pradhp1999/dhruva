package com.cisco.dhruva.common.messaging.models;

import java.io.Serializable;

public class MessageBody implements Serializable {

  final long serialVersionUID = 9215009530928977502L;

  private MessageBodyType bodyType;
  private Object valueData;

  private MessageBody() {}

  public static MessageBody fromPayloadData(Object value, MessageBodyType type) {
    if (value == null) {
      throw new IllegalArgumentException("Value data is null.");
    }

    MessageBody body = new MessageBody();
    body.bodyType = type;
    body.valueData = value;
    return body;
  }

  public Object getPayloadData() {
    return valueData;
  }

  public MessageBodyType getBodyType() {
    return bodyType;
  }
}
