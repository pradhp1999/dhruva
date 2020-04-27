package com.cisco.dhruva.router;

import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import java.util.EventListener;

@FunctionalInterface
public interface MessageListener extends EventListener {

  /**
   * Invoked when a message is received for the proxy
   *
   * @param message the message that is received
   */
  void onMessage(IDhruvaMessage message);
}
