/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
/* Generated by Together */

package com.cisco.dhruva.sip.proxy;

import com.cisco.dhruva.sip.controller.DsProxyResponseGenerator;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponseCode;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import com.cisco.dhruva.util.log.Trace;
import org.apache.logging.log4j.Level;

public class SimpleResponseAggregator implements ResponseAggregator {

  public static final String NL = System.getProperty("line.separator");

  private DsSipResponse bestResponse = null;
  private int bestReasonCode = ResponseReasonCodeConstants.UNDEFINED;
  private DsSipRequest request;

  private static final Logger Log = DhruvaLoggerFactory.getLogger(SimpleResponseAggregator.class);

  private boolean recursing = false;

  public SimpleResponseAggregator(DsSipRequest request)
  {
    this.request = request;
  }

  public SimpleResponseAggregator(DsSipRequest request, boolean recursing)
  {
    this(request);
    this.recursing = recursing;
  }

  public void gotFinalResponse(DsSipResponse response, int reasonCode)
  {
    updateBestResponse(response, reasonCode);
  }

  public DsSipResponse getBestResponse()
  {
    return bestResponse;
  }

  public DsSipRequest getRequest()
  {
    return request;
  }

  protected void updateBestResponse(DsSipResponse response, int reasonCode)
  {
    if (response!= null)
        {
            Log.debug("Entering updateBestResponse(" + response.maskAndWrapSIPMessageToSingleLineOutput() +
            ", " + reasonCode + ")");
        }

    // Ignore redirect responses if recursion is enabled since they will be
    // handled by the searcher.
    if ((response != null) && (response.getResponseClass() == DsSipResponseCode.DS_REDIRECTION) &&
        recursing)
    {

      Log.debug("Ignoring redirect response because recursion was enabled");
      Log.debug("Leaving updateBestResponse");

      return;
    }

    // Special case handling for 2xx and 6xx responses.  2xx responses should
    // always take precedence over other responses.  6xx responses should take
    // precedence over all other responses except 2xx responses.
    if (bestResponse == null)
    {
      // If we don't currently have a best response we can update the best
      // response with a 2xx or 6xx response immediately (without comparing the
      // reason codes).  Otherwise, we need to do reason code comparison to
      // figure out which response we're going to use.
      if ((response != null) &&
            ((response.getResponseClass() == DsSipResponseCode.DS_SUCCESS) ||
            (response.getResponseClass() == DsSipResponseCode.DS_GLOBAL_FAILURE)))
      {
        bestReasonCode = reasonCode;
        bestResponse = response;



        Log.debug("bestReasonCode updated to: " + bestReasonCode);
        Log.debug("bestResponse update to:" + NL + bestResponse.maskAndWrapSIPMessageToSingleLineOutput());
        Log.debug("Leaving updateBestResponse");


        return;
      }
    }
    else {
      // If we already have a 2xx response we don't need to do anything.
      // Subsequent 2xx responses will be propagated automatically.
      if (bestResponse.getResponseClass() == DsSipResponseCode.DS_SUCCESS) {

        Log.debug("Already had a 2xx response, not updating best response");
        Log.debug("Leaving updateBestResponse");

        return;
      }
      // If we don't have a 2xx response and the current response is either a
      // 2xx or a 6xx then it takes precedence over any other response we might
      // have received.
      else if ((response != null) &&
                ((response.getResponseClass() == DsSipResponseCode.DS_SUCCESS) ||
                (response.getResponseClass() == DsSipResponseCode.DS_GLOBAL_FAILURE))) {
        bestReasonCode = reasonCode;
        bestResponse = response;

        if (bestResponse != null)
        {
          Log.debug("bestReasonCode updated to: " + bestReasonCode);
          Log.debug("bestResponse update to:" + NL + bestResponse.maskAndWrapSIPMessageToSingleLineOutput());
          Log.debug("Leaving updateBestResponse");
        }
      }
      // If the current response is not a 2xx or 6xx and we already have a 6xx
      // response, then we don't need to do anything.
      else if (bestResponse.getResponseClass() == DsSipResponseCode.DS_GLOBAL_FAILURE) {

        Log.debug("Already had a 6xx response, not updating best response");
        Log.debug("Leaving updateBestResponse");

        return;
      }
    }

    // Compare the two reason codes.  The compare method returns 1 if the first
    // reason code is better than the second, 0 if they are equal, and -1 if
    // the second reason code is better than the first.
    int reasonCodeComparison = ResponseReasonCodeConstants.compareReasonCodes(reasonCode, bestReasonCode);

    // If this reason code is better than the current best reason code or the
    // response is a 200 response, update the reason code and the response
    if (reasonCodeComparison == 1)
    {
      bestReasonCode = reasonCode;
      bestResponse = response;

      if (bestResponse != null)
      {
        Log.debug("bestReasonCode updated to: " + bestReasonCode);
        Log.debug("bestResponse update to:" + NL + bestResponse.maskAndWrapSIPMessageToSingleLineOutput());
      }
    }
    // If the two reason codes are equal, we need to go to response code
    // comparison to figure out which we should use.
    else if (reasonCodeComparison == 0)
    {
      if (compareResponses(response, bestResponse) == 1)
      {
        bestReasonCode = reasonCode;
        bestResponse = response;

        if (bestResponse != null)
        {
          Log.debug("bestReasonCode updated to: " + bestReasonCode);
          Log.debug("bestResponse update to:" + NL + bestResponse.maskAndWrapSIPMessageToSingleLineOutput());
        }
      }
    }
    Log.debug("Leaving updateBestResponse");
  }

  /**
   * Compares two SIP responses to determine which response is better.  Note
   * that this method treats 6xx (Global Failure) responses as the worst
   * possible responses rather than choosing them over 3xx, 4xx, and 5xx
   * responses.  Null responses are treated as equals, and a non-null response
   * is treated as better than a null response.
   *
   * @param response1 The first response to be compared.
   * @param response2 The second response to be compared.
   * @return 1 if response1 is better than response2, -1 if response2 if better
   * than response1, and 0 if the two responses are equal.
   */
  public static int compareResponses(DsSipResponse response1,
                                     DsSipResponse response2)
  {
    // Null responses are equal
    if ((response1 == null) && (response2 == null))
      return 0;
    // Non-null response is better than a null response
    else if ((response1 != null) && (response2 == null))
      return 1;
    else if ((response2 != null) && (response1 == null))
      return -1;
    // If both responses are non-null, use the status code to make a
    // determination
    else if (response1.getStatusCode() < response2.getStatusCode())
      return 1;
    else
      return -1;
  }

  public int getBestResponseReasonCode()
  {
    Log.debug("returning bestReasonCode=" + bestReasonCode);
    return bestReasonCode;
  }

  public void onProxyFailure(int reasonCode)
  {
    // 408 Response generation removed for 09/13 wireless build.
    // This may affect wireline servers so we need to change this
    // soon - JPS
    // Aggregation logic for failure reasonCodes goes here
    DsSipResponse _response = null;
    switch (reasonCode)
    {
      case ResponseReasonCodeConstants.TIMEOUT:
        try {
          _response = DsProxyResponseGenerator.createResponse(DsSipResponseCode.DS_RESPONSE_REQUEST_TIMEOUT,
                                                              getRequest());
        }
        catch (DsException e) {
          Log.error("Error using DsProxyResponseGenerator to create response", e);
          _response = new DsSipResponse(DsSipResponseCode.DS_RESPONSE_REQUEST_TIMEOUT, getRequest(), null, null);
        }
        updateBestResponse(_response, ResponseReasonCodeConstants.TIMEOUT);
        break;
      default:
        updateBestResponse(null, reasonCode);
        break;
    }
  }
}
