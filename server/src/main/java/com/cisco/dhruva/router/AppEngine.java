package com.cisco.dhruva.router;

import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;

public class AppEngine {

  private AbstractAppSession session;

  public AppEngine(AbstractAppSession session) {
    this.session = session;
  }

  public void start() {
    // Initialize
  }

  public void handleMessage(IDhruvaMessage message) {}
}
