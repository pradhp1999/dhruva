package com.cisco.dhruva.adaptor;

import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.sip.proxy.Location;

public interface ProxyResponseInterface {

  void onSuccessResponse(Location location, IDhruvaMessage response, int responseCode);
}
