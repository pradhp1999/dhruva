package com.cisco.dhruva.router;

import com.cisco.dhruva.Exception.DhruvaException;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;

public interface AppInterface {

  void handleRequest(IDhruvaMessage request) throws DhruvaException;

  void handleResponse(IDhruvaMessage response) throws DhruvaException;
}
