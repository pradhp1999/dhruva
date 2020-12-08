package com.cisco.dhruva.router;

import com.cisco.dhruva.Exception.DhruvaException;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;

// @Service
public class AppSession extends AbstractAppSession {

  private static final String sipRoutingType = "routing.sip.type";

  // @Autowired private Environment environment;

  public AppSession() {
    super();
  }

  //  public String getSIPRoutingType() {
  //    String val = environment.getProperty(sipRoutingType, String.class);
  //    assert val != null;
  //    return val;
  //  }

  @Override
  public void handleRequest(IDhruvaMessage request) throws DhruvaException {
    super.handleRequest(request);
  }

  @Override
  public void handleResponse(IDhruvaMessage response) throws DhruvaException {
    super.handleResponse(response);
  }
}
