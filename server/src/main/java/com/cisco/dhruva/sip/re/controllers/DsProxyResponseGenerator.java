/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.sip.re.controllers;

/*
 * This class enapsulates the funtionality of several commonly sent responses.
 */

import com.cisco.dhruva.sip.re.search.Location;
import com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy.DsProxyTransaction;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipContactHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipHeaderList;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipReasonHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponseCode;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTag;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipToHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsURI;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsMessageLoggingInterface;
import com.cisco.dhruva.util.log.Trace;
import java.util.ArrayList;
import java.util.Iterator;

public abstract class DsProxyResponseGenerator {

  /** our log object * */
  private static final Trace Log = Trace.getTrace(DsProxyResponseGenerator.class.getName());

  public static final String NL = System.getProperty("line.separator");

  public static DsSipResponse createRedirectResponse(ArrayList locations, DsSipRequest request)
      throws Exception {
    if (Log.on && Log.isDebugEnabled()) Log.debug("Entering createRedirectResponse()");
    DsSipHeaderList contactHeaders = new DsSipHeaderList();

    int size = locations.size();

    if (size > 0) {
      if (size == 1) {
        Location location = (com.cisco.dhruva.sip.re.search.Location) locations.get(0);
        DsURI uri = location.getURI();
        DsSipContactHeader contactHeader = new DsSipContactHeader(uri);
        contactHeader.setQvalue(location.getQValue());
        contactHeaders.addLast(contactHeader);
      } else {
        for (Iterator i = locations.iterator(); i.hasNext(); ) {
          Location location = (Location) i.next();
          DsURI uri = location.getURI();
          DsSipContactHeader contactHeader = new DsSipContactHeader(uri);
          contactHeader.setQvalue(location.getQValue());
          contactHeaders.addLast(contactHeader);
        }
      }
    }
    if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving createRedirectResponse()");
    return createRedirectResponse(contactHeaders, request);
  }

  /*
   * Assumes that the vector of locations passed in is non-null
   */
  public static void sendRedirectResponse(
      ArrayList locations, DsSipRequest request, DsProxyTransaction trans) throws Exception {
    if (Log.on && Log.isDebugEnabled()) Log.debug("Entering sendRedirectResponse()");
    DsSipHeaderList contactHeaders = new DsSipHeaderList();

    int size = locations.size();

    if (size > 0) {
      if (size == 1) {
        Location location = (Location) locations.get(0);
        DsURI uri = location.getURI();
        DsSipContactHeader contactHeader = new DsSipContactHeader(uri);
        contactHeaders.addLast(contactHeader);
      } else {
        for (Iterator i = locations.iterator(); i.hasNext(); ) {
          Location location = (Location) i.next();
          DsURI uri = location.getURI();
          DsSipContactHeader contactHeader = new DsSipContactHeader(uri);
          contactHeaders.addLast(contactHeader);
        }
      }
    }

    sendRedirectResponse(contactHeaders, request, trans);
    if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving sendRedirectResponse()");
  }

  /**
   * This is a utility method that sends a redirect Response depending on the number of contact
   * elements in the contact header. If there are multiple contacts, then a 300 will be generated,
   * otherwise a 302 will be generate.
   *
   * @param contactHeaders <CODE>DsSipContactHeader</CODE> object containg the contact elemnts.
   * @param request The request original request that this response messeage is responding to.
   * @param trans The proxy transaction that will be used to send the response.
   */
  public static void sendRedirectResponse(
      DsSipHeaderList contactHeaders,
      /*DsSipContactHeader contactHeader,*/
      DsSipRequest request,
      DsProxyTransaction trans)
      throws Exception {
    if (Log.on && Log.isDebugEnabled()) Log.debug("Entering sendRedirectResponse()");
    DsSipResponse response = createRedirectResponse(contactHeaders, request);

    // send the response.
    trans.respond(response);
    if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving sendRedirectResponse()");
  }

  public static DsSipResponse createRedirectResponse(
      DsSipHeaderList contactHeaders,
      /*DsSipContactHeader contactHeader,*/
      DsSipRequest request)
      throws DsException {
    if (Log.on && Log.isDebugEnabled()) Log.debug("Entering createRedirectResponse()");
    DsSipResponse response = null;
    // added by BJ
    DsSipContactHeader contactHeader = (DsSipContactHeader) contactHeaders.getFirst();
    // check to see if the contactHeader contains more than
    // one element. If yes create redirect header with
    // response code-MULTIPLE_CHOICES ( # 300 )
    //
    // NOTE: Ideally contactHeader should never be null.
    if ((contactHeader != null) && (contactHeaders.size() > 1)) {
      response =
          new DsSipResponse(DsSipResponseCode.DS_RESPONSE_MULTIPLE_CHOICES, request, null, null);
    } else {
      // if one or less contact create redirect header with response
      // type-MOVED_TEMPORARILY ( #302)
      response =
          new DsSipResponse(DsSipResponseCode.DS_RESPONSE_MOVED_TEMPORARILY, request, null, null);
    }

    response.setApplicationReason(DsMessageLoggingInterface.REASON_AUTO);

    if (Log.on && Log.isInfoEnabled())
      Log.info("Created " + response.getStatusCode() + " response");

    DsSipToHeader toHeader = response.getToHeaderValidate();
    if (toHeader.getTag() == null) {
      toHeader.setTag(DsSipTag.generateTag());
    }

    // add redirect header to the response.
    response.addHeaders(contactHeaders);
    if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving createRedirectResponse()");
    return response;
  }

  /**
   * Utility function that sends Internal Server Error response
   *
   * @param request The request original request that this response messeage is responding to.
   * @param trans The stateful proxy transaction that will be used to send the response.
   */
  public static void sendServerInternalErrorResponse(DsSipRequest request, DsProxyTransaction trans)
      throws DsException {
    if (Log.on && Log.isDebugEnabled()) Log.debug("Entering sendServerInternalErrorResponse()");

    DsSipResponse response =
        new DsSipResponse(DsSipResponseCode.DS_RESPONSE_INTERNAL_SERVER_ERROR, request, null, null);
    response.setApplicationReason(DsMessageLoggingInterface.REASON_AUTO);
    DsSipToHeader toHeader = response.getToHeaderValidate();
    if (toHeader.getTag() == null) {
      toHeader.setTag(DsSipTag.generateTag());
    }

    trans.respond(response);
    if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving sendServerInternalErrorResponse()");
  }

  public static DsSipResponse createNotFoundResponse(DsSipRequest request) throws DsException {
    if (Log.on && Log.isDebugEnabled()) Log.debug("Entering createNotFoundResponse()");
    DsSipResponse response =
        new DsSipResponse(DsSipResponseCode.DS_RESPONSE_NOT_FOUND, request, null, null);
    response.setApplicationReason(DsMessageLoggingInterface.REASON_AUTO);
    DsSipToHeader toHeader = response.getToHeaderValidate();
    if (toHeader.getTag() == null) {
      toHeader.setTag(DsSipTag.generateTag());
    }

    if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving createNotFoundResponse()");

    return response;
  }

  /**
   * Utility function that sends 404 responses. Note, you must use a stateful transaction to send
   * the response.
   *
   * @param request The request original request that this response messeage is responding to.
   * @param trans The stateful proxy transaction that will be used to send the response.
   */
  public static void sendNotFoundResponse(DsSipRequest request, DsProxyTransaction trans)
      throws DsException {
    if (Log.on && Log.isDebugEnabled()) Log.debug("Entering sendNotFoundResponse()");
    trans.respond(createNotFoundResponse(request));
    if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving sendNotFoundResponse()");
  }

  /**
   * This is the utility method that sends trying response
   *
   * @param trans The proxy transaction that will be used to send the response.
   */
  public static void sendByteBasedTryingResponse(DsProxyTransaction trans) {
    trans.respond(null);
  }

  public static void sendTryingResponse(DsSipRequest request, DsProxyTransaction trans) {
    if (Log.on && Log.isDebugEnabled()) Log.debug("Entering sendTryingResponse()");
    DsSipResponse response =
        new DsSipResponse(DsSipResponseCode.DS_RESPONSE_TRYING, request, null, null);

    trans.respond(response);

    if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving sendTryingResponse()");
  }

  public static void sendResponse(DsSipResponse response, DsProxyTransaction trans) {
    if (Log.on && Log.isTraceEnabled()) Log.trace("Entering sendResponse()");

    if (trans != null) {
      trans.respond(response);
      if (Log.on && Log.isDebugEnabled()) {
        Log.debug("Sent response:" + NL + response.maskAndWrapSIPMessageToSingleLineOutput());
      }
    } else {
      Log.warn("DsProxyTransaction was null!");
    }

    if (Log.on && Log.isTraceEnabled()) Log.trace("Leaving sendResponse()");
  }

  /** Creates a response of the given type, and tags the To header. */
  public static DsSipResponse createResponse(int responseCode, DsSipRequest request)
      throws DsException {
    if (Log.on && Log.isDebugEnabled()) Log.debug("Entering createResponse()");
    DsSipResponse response = new DsSipResponse(responseCode, request, null, null);
    response.setApplicationReason(DsMessageLoggingInterface.REASON_AUTO);
    DsByteString tag = response.getToTag();
    if (Log.on && Log.isDebugEnabled()) Log.debug("To tag is " + tag);
    if (tag == null) {
      if (Log.on && Log.isDebugEnabled()) Log.debug("Generating To tag");
      DsSipToHeader toHeader = response.getToHeaderValidate();
      if (toHeader != null) toHeader.setTag(DsSipTag.generateTag());
    }
    if (Log.on && Log.isDebugEnabled()) Log.debug("Leaving createResponse()");
    return response;
  }

  /**
   * {@link DsProxyResponseGenerator#addReasonHeader(DsSipResponse)} is called when CP goes
   * maintenance mode<br>
   * This add's Reason header for CP maintenance mode<br>
   * Reason: SIP; cause=901; text="CP in Maintenance Mode"
   *
   * @param response
   */
  public static void addMaintenanceReasonHeader(DsSipResponse response) {
    if (response == null || response.getStatusCode() != 503) {
      Log.warn(
          "Maintenance mode Reason header added when CP generates 503 Service Unavaiable, but found : "
              + response);
      return;
    }
    DsSipReasonHeader reasonHeader = new DsSipReasonHeader();
    reasonHeader.setProtocol(DsSipConstants.BS_SIP);
    reasonHeader.setText(DsSipConstants.MAINTENANCE_MODE_REASON_PHRASE);
    reasonHeader.setCause(DsSipConstants.MAINTENANCE_MODE_REASON_CAUSE);
    response.addHeader(reasonHeader);
  }
}
