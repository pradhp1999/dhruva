package com.cisco.dhruva.adaptor;

import com.cisco.dhruva.Exception.DhruvaException;
import com.cisco.dhruva.sip.proxy.controller.DsControllerInterface;
import com.cisco.dhruva.sip.proxy.controller.DsProxyController;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.util.SIPRequestBuilder;
import org.mockito.Mockito;
import org.mockito.Mockito.*;
import org.testng.annotations.Test;

public class ProxyAdaptorTest {

  @Test
  public void testHandleRequestFlow()
      throws DsSipParserListenerException, DsSipParserException, DhruvaException {
    ProxyAdaptorFactory f = new ProxyAdaptorFactory();
    DsControllerInterface controller = Mockito.mock(DsProxyController.class);
    AppAdaptorInterface adaptor = f.getProxyAdaptor(controller);
    DsSipRequest request =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));

    adaptor.handleRequest(request);
  }
}
