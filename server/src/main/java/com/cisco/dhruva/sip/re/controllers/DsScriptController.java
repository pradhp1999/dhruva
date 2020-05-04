/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.sip.re.controllers;

import com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy.DsControllerInterface;
import com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy.DsProxyServerTransaction;
import com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy.DsProxyStatelessTransaction;
import com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy.DsProxyTransaction;
import com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy.DsViaHandler;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipServerTransaction;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipTransactionManager;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipTransportLayer;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsUdpConnection;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipAckMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipCancelMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipViaHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipDictionary;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.Trace;
import java.io.IOException;
import java.net.InetAddress;

/**
 * DsScriptController handles request and response callbacks from the low-level proxy core using the
 * DsControllerInterface. For each callback it invokes the appropriate XCL request and response
 * processing logic as required.
 *
 * @author Mitch Rappard
 *     <p>Copyright 2001 dynamicsoft, inc. All rights reserved
 */
public class DsScriptController extends DsProxyController implements DsControllerInterface {

  protected static Trace Log = Trace.getTrace(DsScriptController.class.getName());
  private int n_branch;

  /**
   * The first method invoked by ProxyManager right after it has retreived a controller from the
   * controller factory (this happens when it receives a new request). The implementation of this
   * method MUST create a DsProxyTransaction object and return it to the ProxyManager
   *
   * @param request The incoming request that trigered this method
   * @return ProxyStatelessTransaction
   */
  public DsProxyStatelessTransaction onNewRequest(
      DsSipServerTransaction serverTrans, DsSipRequest request) {

    DsSipTransportLayer dsTransportLayer= DsSipTransactionManager.getTransportLayer();
    try {

      DsByteString branch = DsViaHandler.getInstance().getBranchID(++n_branch,
          request);

      DsSipViaHeader via = new DsSipViaHeader(DsByteString.newInstance("127.0.0.1"), 5060,
          Transport.UDP);


      via.setBranch(branch);

      if (request.shouldCompress()) {
        via.setComp(DsSipConstants.BS_SIGCOMP);
      }
      else
      {
        DsTokenSipDictionary tokDic = request.shouldEncode();
        if (null != tokDic)
          via.setParameter(DsTokenSipConstants.s_TokParamName, tokDic.getName());
      }
      request.addHeader(via, true, false);


      DsUdpConnection dsUdpConnection= (DsUdpConnection) dsTransportLayer.getConnection(request.getNetwork(), InetAddress
          .getByName("0.0.0.0"),5070,InetAddress.getByName("127.0.0.1"),5065, Transport.UDP);
      dsUdpConnection.send(request.toByteArray());
    } catch (DsException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /** Send the cancel to the XCL script for processing. */
  public void onCancel(
      DsProxyTransaction proxy, DsProxyServerTransaction trans, DsSipCancelMessage cancel) {}

  /** Send the ack to the XCL script for processing. */
  public void onAck(
      DsProxyTransaction proxy, DsProxyServerTransaction trans, DsSipAckMessage ack) {}

  public void onBestResponse(DsProxyTransaction proxy, DsSipResponse response) {}
}
