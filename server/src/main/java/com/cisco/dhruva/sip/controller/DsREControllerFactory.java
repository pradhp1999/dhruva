/*
 * Copyright (c) 2001-2006 by cisco Systems, Inc.
 * All rights reserved.
 */
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

// FILENAME:	DsControllerFactory.java
//
// MODULE:	DsSipController
//
// COPYRIGHT:
// ============== copyright 2000 dynamicsoft Inc. =================
// ==================== all rights reserved =======================
// /////////////////////////////////////////////////////////////////

package com.cisco.dhruva.sip.controller;

import com.cisco.dhruva.adaptor.ProxyAdaptorFactory;
import com.cisco.dhruva.adaptor.ProxyAdaptorFactoryInterface;
import com.cisco.dhruva.config.sip.controller.DsControllerConfig;
import com.cisco.dhruva.loadbalancer.LBRepositoryHolder;
import com.cisco.dhruva.router.AppInterface;
import com.cisco.dhruva.sip.proxy.DsControllerFactoryInterface;
import com.cisco.dhruva.sip.proxy.DsControllerInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipServerTransaction;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsConfigManager;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.util.Arrays;
import java.util.HashMap;

/** This class returns the right Adaptor layer to communicate with App returns a DsAppController */
public class DsREControllerFactory implements DsControllerFactoryInterface, DsSipRouteFixInterface {

  protected static Logger Log = DhruvaLoggerFactory.getLogger(DsREControllerFactory.class);

  // the time before the actual timeout that you want to generate a cancel.
  // default 5 seconds
  public static final int REQUEST_TIMEOUT_OFFSET =
      Integer.parseInt(System.getProperty("REQUEST_TIMEOUT_OFFSET", "5000"));
  public static final int SEQ_REQUEST_TIMEOUT_DIVISIBLE =
      Integer.parseInt(System.getProperty("SEQ_REQUEST_TIMEOUT_DIVISIBLE", "2"));

  /** */
  public DsREControllerFactory() {
    // temporary!!!! for testing only !!!!!1
  }

  /*
   * Creates a DsAppController object and returns it.  If the proxy is in
   * stateful mode, the stateful transaction is created here, and a 100 is sent
   * right away.  Otherwise the transaction will be created in the onNewRequest()
   * method of DsProxyController.
   *
   *@returns A new DsAppController
   *@see DsScriptController
   */
  public DsControllerInterface getController(
          DsSipServerTransaction serverTransaction,
          DsSipRequest request,
          ProxyAdaptorFactoryInterface pf,
          AppInterface app) {

    Log.debug(
        "Entering getController in DsControllerFactory {}"
            + Arrays.toString(request.getSessionId()));

    // Grab a snaphot of the config that will be used for the rest of the transaction
    DsControllerConfig ourConfig = DsControllerConfig.getCurrent();

    DsProxyController controller = new DsAppController(pf, app);

    int requestTimeout =
        DsConfigManager.getTimerValue(request.getNetwork(), DsSipConstants.serverTn);
    int sequentialSearchTimeout = requestTimeout / SEQ_REQUEST_TIMEOUT_DIVISIBLE;

    // MEETPASS
    LBRepositoryHolder sgConfig = HashMap::new;
    // Initialize the controller settings
    controller.init(
        ourConfig.getSearchType(),
        requestTimeout,
        sequentialSearchTimeout,
        ourConfig.getStateMode(),
        ourConfig.isRecursing(),
        ourConfig,
        ourConfig.getDefaultRetryAfterMilliSeconds(),
        sgConfig,
        ourConfig.getNextHopFailureAction());

    if (ourConfig.isStateful()
        && (request.getMethodID() != DsSipMessage.ACK)
        && (request.getMethodID() != DsSipMessage.CANCEL)) {
      // Will automatically create the right kind of transaction
      controller.createProxyTransaction(true, request, serverTransaction);
    }

    return controller;
  }

  // implements DsSipRouteFixInterface
  public boolean recognize(DsURI ruri, boolean isRequestURI) {
    Log.debug("Entering recognize(" + ruri + ", " + isRequestURI + ')');
    DsControllerConfig ourConfig = DsControllerConfig.getCurrent();
    return ourConfig.recognize(ruri, isRequestURI);
  }
}
