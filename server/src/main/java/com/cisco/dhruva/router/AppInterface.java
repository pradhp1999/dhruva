package com.cisco.dhruva.router;

import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;

public interface AppInterface {

  public void handleRequest(IDhruvaMessage request);

  public void handleResponse(IDhruvaMessage response);
}
