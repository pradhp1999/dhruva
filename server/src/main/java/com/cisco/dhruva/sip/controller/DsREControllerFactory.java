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

import com.cisco.dhruva.sip.proxy.DsControllerFactoryInterface;
import com.cisco.dhruva.sip.proxy.DsControllerInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipServerTransaction;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsConfigManager;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import com.cisco.dhruva.util.log.Trace;
import org.apache.logging.log4j.Level;

// our packages

/**
 * This class loads the Route Engines XCL Script, and for each call to getController,
 * generates and returns a DsScriptController based on the XCL script.
 */

public class DsREControllerFactory implements DsControllerFactoryInterface, DsSipRouteFixInterface
{

    protected static Logger Log = DhruvaLoggerFactory.getLogger(DsREControllerFactory.class);


    // the time before the actual timeout that you want to generate a cancel.
    // default 5 seconds
    public static final int REQUEST_TIMEOUT_OFFSET = Integer.parseInt(System.getProperty("REQUEST_TIMEOUT_OFFSET","5000"));
    public static final int SEQ_REQUEST_TIMEOUT_DIVISIBLE = Integer.parseInt(System.getProperty("SEQ_REQUEST_TIMEOUT_DIVISIBLE","2"));
    //protected XCL xcl = null;

    /**
     */
    public DsREControllerFactory()
    {
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
  public DsControllerInterface getController(DsSipServerTransaction serverTransaction, DsSipRequest request) {

    Log.debug( "Entering getController in DsControllerFactory ");

    //Grab a snaphot of the config that will be used for the rest of the transaction
    DsControllerConfig  ourConfig = DsControllerConfig.getCurrent();

    //Create the controller, passing it the XCL that was loaded in our constructor
    CallProcessingConfig cpConfig = CallProcessingConfig.getInstance();
    DsProxyController controller = null;
    if((ourConfig.isEdgeProxyMode() || ourConfig.isNatMediaSet() ||
           ourConfig.isFirewallMode() ) ) {

      if (Log.on && Log.isDebugEnabled()) {
        if (Log.isEnabled(Level.DEBUG))
          Log.debug( "Creating new DsFirewallScriptController in the controller factory");
      }

      controller = new DsFirewallScriptController(  cpConfig.getXCLRepository(),
                                                    ourConfig );
    }
    else {
      if (Log.on && Log.isDebugEnabled()) {
        if (Log.isEnabled(Level.DEBUG))
        Log.debug( "Creating new DsScriptController in the controller factory");
      }

      controller = new DsScriptController(cpConfig.getXCLRepository());
    }

    int requestTimeout = DsConfigManager.getTimerValue(request.getNetwork(), DsSipConstants.serverTn);
    int sequentialSearchTimeout = requestTimeout/SEQ_REQUEST_TIMEOUT_DIVISIBLE;
    //Initialize the controller settings
    controller.init(ourConfig.getSearchType(), requestTimeout,
                    sequentialSearchTimeout,
                    ourConfig.getStateMode(), ourConfig.isRecursing(),
                    ourConfig,
                    ourConfig.getDefaultRetryAfterMilliSeconds(),
                    cpConfig,
                    ourConfig.getNextHopFailureAction());
    /*
    controller.init(ourConfig.getSearchType(), (int)ourConfig.getRequestTimeout(),
                    ourConfig.getSequentialSearchTimeout(),
                    ourConfig.getStateMode(), ourConfig.isRecursing(),
                    ourConfig,
                    ourConfig.getDefaultRetryAfterMilliSeconds(),
                    cpConfig,
                    ourConfig.getNextHopFailureAction());
    */

    if ( ourConfig.isStateful() &&
       (request.getMethodID() != DsSipMessage.ACK) &&
       (request.getMethodID() != DsSipMessage.CANCEL) )
    {
      //Will automatically create the right kind of transaction
      controller.createProxyTransaction( ourConfig.isStateful(), request, serverTransaction );
    }


    Log.debug( "Leaving getController in DsControllerFactory ");

    return controller;

  }

  // implements DsSipRouteFixInterface 
  public boolean recognize(DsURI ruri, boolean isRequestURI) {
    Log.debug("Entering recognize(" + ruri  + ", " + isRequestURI + ')');
    DsControllerConfig  ourConfig = DsControllerConfig.getCurrent();
    boolean b = ourConfig.recognize(ruri, isRequestURI);
    Log.debug("Leaving recognize(), returning " + b);
    return b;
  }
}


