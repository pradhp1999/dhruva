/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.dhruva.sip.controller;

import com.cisco.dhruva.sip.proxy.DsProxyTransaction;
import com.cisco.dhruva.sip.proxy.Location;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsMessageLoggingInterface;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.util.ArrayList;

/*
 * This class enapsulates the funtionality of several commonly sent responses.
 */

public abstract class DsProxyResponseGenerator {

  /** our log object * */
  private static final Logger Log = DhruvaLoggerFactory.getLogger(DsProxyResponseGenerator.class);

  public static final String NL = System.getProperty("line.separator");

  public static DsSipResponse createRedirectResponse(ArrayList locations, DsSipRequest request)
      throws Exception {
    Log.debug("Entering createRedirectResponse()");
    DsSipHeaderList contactHeaders = new DsSipHeaderList();

    int size = locations.size();

    if (size > 0) {
      if (size == 1) {
        Location location = (Location) locations.get(0);
        DsURI uri = location.getURI();
        DsSipContactHeader contactHeader = new DsSipContactHeader(uri);
        contactHeader.setQvalue(location.getQValue());
        contactHeaders.addLast(contactHeader);
      } else {
        for (Object o : locations) {
          Location location = (Location) o;
          DsURI uri = location.getURI();
          DsSipContactHeader contactHeader = new DsSipContactHeader(uri);
          contactHeader.setQvalue(location.getQValue());
          contactHeaders.addLast(contactHeader);
        }
      }
    }
    Log.debug("Leaving createRedirectResponse()");
    return createRedirectResponse(contactHeaders, request);
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
    Log.debug("Entering sendRedirectResponse()");
    DsSipResponse response = createRedirectResponse(contactHeaders, request);

    // send the response.
    trans.respond(response);
    Log.debug("Leaving sendRedirectResponse()");
  }

  public static DsSipResponse createRedirectResponse(
      DsSipHeaderList contactHeaders,
      /*DsSipContactHeader contactHeader,*/
      DsSipRequest request)
      throws DsException {
    Log.debug("Entering createRedirectResponse()");
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

    Log.info("Created " + response.getStatusCode() + " response");

    DsSipToHeader toHeader = response.getToHeaderValidate();
    if (toHeader.getTag() == null) {
      toHeader.setTag(DsSipTag.generateTag());
    }

    // add redirect header to the response.
    response.addHeaders(contactHeaders);
    Log.debug("Leaving createRedirectResponse()");
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
    Log.debug("Entering sendServerInternalErrorResponse()");

    DsSipResponse response =
        new DsSipResponse(DsSipResponseCode.DS_RESPONSE_INTERNAL_SERVER_ERROR, request, null, null);
    response.setApplicationReason(DsMessageLoggingInterface.REASON_AUTO);
    DsSipToHeader toHeader = response.getToHeaderValidate();
    if (toHeader.getTag() == null) {
      toHeader.setTag(DsSipTag.generateTag());
    }

    trans.respond(response);
    Log.debug("Leaving sendServerInternalErrorResponse()");
  }

  public static DsSipResponse createNotFoundResponse(DsSipRequest request) throws DsException {
    Log.debug("Entering createNotFoundResponse()");
    DsSipResponse response =
        new DsSipResponse(DsSipResponseCode.DS_RESPONSE_NOT_FOUND, request, null, null);
    response.setApplicationReason(DsMessageLoggingInterface.REASON_AUTO);
    DsSipToHeader toHeader = response.getToHeaderValidate();
    if (toHeader.getTag() == null) {
      toHeader.setTag(DsSipTag.generateTag());
    }

    Log.debug("Leaving createNotFoundResponse()");

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
    Log.debug("Entering sendNotFoundResponse()");
    trans.respond(createNotFoundResponse(request));
    Log.debug("Leaving sendNotFoundResponse()");
  }

  /**
   * This is the utility method that sends trying response
   *
   * @param trans The proxy transaction that will be used to send the response.
   */
  public static void sendByteBasedTryingResponse(DsProxyTransaction trans) {
    Log.debug("Entering sendByteBasedTryingResponse()");
    trans.respond(null);
  }

  public static void sendTryingResponse(DsSipRequest request, DsProxyTransaction trans) {
    Log.debug("Entering sendTryingResponse()");
    DsSipResponse response =
        new DsSipResponse(DsSipResponseCode.DS_RESPONSE_TRYING, request, null, null);

    trans.respond(response);
  }

  public static void sendResponse(DsSipResponse response, DsProxyTransaction trans) {
    Log.debug("Entering sendResponse()");

    if (trans != null) {
      trans.respond(response);
      Log.debug("Sent response:" + NL + response.maskAndWrapSIPMessageToSingleLineOutput());
    } else {
      Log.warn("DsProxyTransaction was null!");
    }
  }

  /** Creates a response of the given type, and tags the To header. */
  public static DsSipResponse createResponse(int responseCode, DsSipRequest request)
      throws DsException {
    Log.debug("Entering createResponse()");
    DsSipResponse response = new DsSipResponse(responseCode, request, null, null);
    response.setApplicationReason(DsMessageLoggingInterface.REASON_AUTO);
    DsByteString tag = response.getToTag();
    Log.debug("To tag is " + tag);
    if (tag == null) {
      Log.debug("Generating To tag");
      DsSipToHeader toHeader = response.getToHeaderValidate();
      if (toHeader != null) toHeader.setTag(DsSipTag.generateTag());
    }
    return response;
  }
}
