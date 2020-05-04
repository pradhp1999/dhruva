/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.dhruva.sip.controller;


import com.cisco.dhruva.sip.proxy.DsControllerInterface;
import com.cisco.dhruva.sip.proxy.DsProxyServerTransaction;
import com.cisco.dhruva.sip.proxy.DsProxyStatelessTransaction;
import com.cisco.dhruva.sip.proxy.DsProxyTransaction;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipClientTransactionImpl;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipServerTransaction;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsTrackingException;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import com.cisco.dhruva.util.log.Trace;
import org.apache.logging.log4j.Level;


/**
 * DsScriptController handles request and response callbacks from the low-level
 * proxy core using the DsControllerInterface.  For each callback it
 * invokes the appropriate XCL request and response processing logic as
 * required.
 *
 * @author Mitch Rappard
 *         <p/>
 *         Copyright 2001 dynamicsoft, inc.
 *         All rights reserved
 */
public class DsScriptController extends DsProxyController implements DsControllerInterface
{

    protected XCLRepository xclRepository = null;
    protected XCLExecutionObject exObj = null;
    protected static Logger Log =
            DhruvaLoggerFactory.getLogger(DsScriptController.class);

    /**
     * Instantiates a new DsScriptController object which uses the specified
     * XCL script to determine how request and response callbacks will be
     * processed.
     *
     * @param xclRepository The XCL script to be used for request and response processing.
     */
    public DsScriptController(XCLRepository xclRepository)
    {
        this.xclRepository = xclRepository;
    }

    /**
     * The first method invoked by ProxyManager right after it has retreived a
     * controller from the controller factory (this happens when it receives
     * a new request).
     * The implementation of this method MUST create
     * a DsProxyTransaction object and return it to the
     * ProxyManager
     *
     * @param request The incoming request that trigered this method
     * @return ProxyStatelessTransaction
     */
    public DsProxyStatelessTransaction onNewRequest(DsSipServerTransaction serverTrans, DsSipRequest request)
    {
        if (Log.on && Log.isTraceEnabled()) Log.trace("Entering onNewRequest in ScriptController");

        //Create the proxy transaction if it wasn't created in the factory
        DsProxyStatelessTransaction trans = super.onNewRequest(serverTrans, request);

        if (respondedOnNewRequest)
        {
            // request has been responded and no need to continue
            return ourProxy;
        }

        //check for failoverstateful mode. in that case we have already
        //checked and proxied to in the onNewRequest of DsProxyController
        //so just return ourProxy overhere to avoid going into the execution below.
        if (stateMode == DsControllerConfig.FAILOVER_STATEFUL)
        {
            DsFailOverStatefulWrapper failOverWrapper = getFailOverStatefulWrapper(request);
            if (failOverWrapper != null)
            {
                return trans;
            }
        }

        //Determine the direction of this message now
        DsBindingInfo bi = request.getBindingInfo();
        ListenIf listenIf = (ListenIf) DsControllerConfig.getCurrent().getInterface(bi.getLocalAddress(),
                                                                                    bi.getTransport(),
                                                                                    bi.getLocalPort());
        if (Log.on && Log.isDebugEnabled())
            Log.debug("Matched incoming request to listen interface: " + listenIf);

        //TODO remove below default
        String direction = DsControllerConfig.LISTEN_INTERNAL;
        if (listenIf != null)
            direction = listenIf.getDirection();

        //Make the call back to the XCL tree
        try
        {
            // If the XCL script is going to be used to determine the direction
            // of the message rather than the route header, or there is no route
            // header in the message, use XCL to make a routing decision
            if (xclRepository.getProcessRouteHeader() || request.getHeader(DsSipConstants.ROUTE) == null
                    || !DsSipClientTransactionImpl.isMidDialogRequest(request)) {
                //request.setProcessRoute(false);

                XCL xcl = xclRepository.getRootXCL();

                //Create an execution object which will hold the execution environment variables
                //for the duration of this transaction.
                //XCLExecutionObject exObj = new XCLExecutionObject( request, callProcConfig );
                exObj = new XCLExecutionObject(new Location(request.getURI()), request, this,
                                               xclRepository, timeToTry,
                                               sequentialSearchTimeout, this.searchType,
                                               isRecursing, direction);

                // Start collecting instrumentation data for Incoming Request - XCL
                DsRePerfManager.start(DsRePerfManager.INCOMING_REQUEST_XCL);

                xcl.onNewRequest(exObj);

                // Stop collecting instrumentation data for Incoming Request - XCL
                DsRePerfManager.stop(DsRePerfManager.INCOMING_REQUEST_XCL);
            }
            else
            {
                // We don't want XCL to process this request, so we by bypass it
                // by making a call directly to the proxy core.  Note that failover
                // will not be possible for this branch.  Nothng more will be done until
                // the core makes the onBestResponse callback

                if (Log.on && Log.isDebugEnabled())
                    Log.debug("Skipping XCL and sending to the URL in the " +
                            " Route header");

                Location loc = new Location(request.getURI());
                loc.setProcessRoute(true);
                this.usingRouteHeader = true;

                this.proxyTo(loc, ourRequest, null);
            }
        }
        catch (Throwable e)
        {
            // Error Logging
            if (Log.on && Log.isEnabled(Level.ERROR))
                Log.error("onNewRequest() - Execution error while executing XCL script: " + e.getMessage(), e);
            //Set the request var in the proxy controller so it can send the response
            ourRequest = request;
            //Try to send a 500
            sendFailureResponse(DsSipResponseCode.DS_RESPONSE_INTERNAL_SERVER_ERROR);
            return ourProxy;
        }

        if (Log.on && Log.isTraceEnabled()) Log.trace("Leaving onNewRequest in ScriptController");

        //return trans;
        // when in stateless mode if a response is generated then the state for that transaction is changed to stateful
        // so the "trans" is pointing to the initial stateless transaction, this would have changed after NewRequest()
        // if it resulted in a response. so returning the stateless transaction "trans" when we have converted into
        // statefull would create problems. so returning ourProxy which is the actual transaction thet is being used.
        return ourProxy;

    }

    /**
     * Send the cancel to the XCL script for processing.
     */
    public void onCancel(DsProxyTransaction proxy,
                         DsProxyServerTransaction trans,
                         DsSipCancelMessage cancel) throws DsException
    {
        if (Log.on && Log.isDebugEnabled())
            Log.debug("Entering onCancel()");

        gotCancel = true;

        //Notify the execution object about the CANCEL
        if(exObj == null ) {
        	throw new DsTrackingException(TrackingExceptions.NULLPOINTEREXCEPTION, "XCLExecutionObject is null to Cancel for message :"
        + cancel.maskAndWrapSIPMessageToSingleLineOutput());
        }else {
        		exObj.onCancel(cancel);
        }
        if (Log.on && Log.isDebugEnabled())
            Log.debug("Leaving onCancel()");


    }

    /**
     * Send the ack to the XCL script for processing.
     */
    public void onAck(DsProxyTransaction proxy,
                      DsProxyServerTransaction trans,
                      DsSipAckMessage ack)
    {
        if (Log.on && Log.isDebugEnabled())
            Log.debug("Entering onAck()");

        // Start recording instrumentation data for Incoming ACK/200 - Controller
        DsRePerfManager.start(DsRePerfManager.INCOMING_ACK200_CONTROLLER);

        //Notify the execution object about the ACK
        if(exObj != null)
            exObj.onAck(ack);

        // Stop recording instrumentation data for Incoming ACK/200 - Controller
        DsRePerfManager.stop(DsRePerfManager.INCOMING_ACK200_CONTROLLER);

        if (Log.on && Log.isDebugEnabled())
            Log.debug("Leaving onAck()");
    }

    public void onBestResponse(DsProxyTransaction proxy, DsSipResponse response)
    {

        if (Log.on && Log.isTraceEnabled())
            Log.trace("Entering onBestResponse()");

        // Start recording instrumentation data for Incoming Best Response - Controller
        DsRePerfManager.start(DsRePerfManager.INCOMING_BEST_RESPONSE_CONTROLLER);

        // If we sent to a route header without invoking XCL, then we will use the
        // best response that the core has.
        if (usingRouteHeader)
        {

            if (Log.on && Log.isDebugEnabled() && response != null)
            {
                Log.debug("We must have sent to a route header or gotten a cancel, responding " +
                        " with the core's best respnonse: \n" + response.maskAndWrapSIPMessageToSingleLineOutput());
            }

            //Forward the best response upstream
            proxy.respond();
        }

        // Stop recording instrumentation data for Incoming Best Response - Controller
        DsRePerfManager.stop(DsRePerfManager.INCOMING_BEST_RESPONSE_CONTROLLER);

        if (Log.on && Log.isTraceEnabled())
            Log.trace("Leaving onBestResponse()");
    }
}
