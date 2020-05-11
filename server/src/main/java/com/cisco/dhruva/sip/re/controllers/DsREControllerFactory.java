/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.cisco.dhruva.sip.re.controllers;

import com.cisco.dhruva.sip.re.configs.DsControllerConfig;
import com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy.DsControllerFactoryInterface;
import com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy.DsControllerInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipServerTransaction;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRouteFixInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsURI;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsConfigManager;
import com.cisco.dhruva.util.log.Trace;

/**
 * This class loads the Route Engines XCL Script, and for each call to getController, generates and
 * returns a DsScriptController based on the XCL script.
 */
public class DsREControllerFactory implements DsControllerFactoryInterface, DsSipRouteFixInterface {

  protected static Trace Log = Trace.getTrace(DsREControllerFactory.class.getName());

  // the time before the actual timeout that you want to generate a cancel.
  // default 5 seconds
  public static final int REQUEST_TIMEOUT_OFFSET =
      Integer.parseInt(System.getProperty("REQUEST_TIMEOUT_OFFSET", "5000"));
  public static final int SEQ_REQUEST_TIMEOUT_DIVISIBLE =
      Integer.parseInt(System.getProperty("SEQ_REQUEST_TIMEOUT_DIVISIBLE", "2"));
  // protected XCL xcl = null;

  /** */
  public DsREControllerFactory() {
    // temporary!!!! for testing only !!!!!1
    //  	  DsControllerConfig.addLocalRouteDomain("63.113.45.222");
  }

  /*
   * Creates a DsScriptController object and returns it.  If the proxy is in
   * stateful mode, the stateful transaction is created here, and a 100 is sent
   * right away.  Otherwise the transaction will be created in the onNewRequest()
   * method of DsProxyController.
   *
   *@returns A new DsScriptController
   *@see DsScriptController
   */
  public DsControllerInterface getController(
      DsSipServerTransaction serverTransaction, DsSipRequest request) {

    // Grab a snaphot of the config that will be used for the rest of the transaction
    DsControllerConfig ourConfig = DsControllerConfig.getCurrent();

    DsProxyController controller = null;

    // TODO change this when chaning controller
    controller = new DsScriptController();

    int requestTimeout =
        DsConfigManager.getTimerValue(request.getNetwork(), DsSipConstants.serverTn);
    int sequentialSearchTimeout = requestTimeout / SEQ_REQUEST_TIMEOUT_DIVISIBLE;
    // Initialize the controller settings
    controller.init(
        ourConfig.getSearchType(),
        requestTimeout,
        sequentialSearchTimeout,
        ourConfig.getStateMode(),
        ourConfig.isRecursing(),
        ourConfig,
        ourConfig.getDefaultRetryAfterMilliSeconds(),
        null,
        ourConfig.getNextHopFailureAction());

    if (ourConfig.isStateful()
        && (request.getMethodID() != DsSipMessage.ACK)
        && (request.getMethodID() != DsSipMessage.CANCEL)) {
      // Will automatically create the right kind of transaction
      controller.createProxyTransaction(ourConfig.isStateful(), request, serverTransaction);
    }

    // Send a 100 if we are statefull.
    /* Old way
    if( ourConfig.isStateful() ) {
        DsSipResponse response = new DsSipResponse(DsSipResponseCode.DS_RESPONSE_TRYING,
                                                   request, null, null);
        try {
          DsProxyTransaction ourProxy = new DsProxyTransaction(controller, ourConfig, request);
          ourProxy.respond( response );
          controller.setProxyTransaction(ourProxy);
        }
        catch( DsException dse ) {
          if( Log.isEnabledFor(Level.WARN) )
            Log.warn( "Unalbe to send 100 in controller factory ", dse);
        }
    }  */

    if (Log.on && Log.isTraceEnabled()) Log.trace("Leaving getController in DsControllerFactory ");

    return controller;
  }

  // implements DsSipRouteFixInterface
  public boolean recognize(DsURI ruri, boolean isRequestURI) {
    if (Log.on && Log.isDebugEnabled())
      Log.debug("Entering recognize(" + ruri + ", " + isRequestURI + ')');
    DsControllerConfig ourConfig = DsControllerConfig.getCurrent();
    boolean b = ourConfig.recognize(ruri, isRequestURI);
    if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving recognize(), returning " + b);
    return b;
  }
}
