package com.cisco.dhruva.router;

import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;

public class AppMessageListenerAdaptor implements MessageListener {

  final MessageListener messageListener;

  public AppMessageListenerAdaptor(MessageListener messageListener) {
    this.messageListener = messageListener;
  }

  @Override
  public void onMessage(IDhruvaMessage message) {
    messageListener.onMessage(message);
  }

  @Override
  public String toString() {
    return messageListener.toString();
  }
}
