package com.cisco.dhruva.router;

import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;

public interface AppInterface {

  void handleRequest(IDhruvaMessage request);

  void handleResponse(IDhruvaMessage response);
}
