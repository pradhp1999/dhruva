package com.cisco.dhruva.sip.proxy;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;


/**
 * A very simple interface that aggregates responses from multiple
 * branches (after forking) to determine what the best response to
 * forward upstream is
 */
public interface ResponseAggregator {

  /**
   * Let the aggregatot know that a response was received
   */
  public void gotFinalResponse(DsSipResponse response, int reasonCode);

  /**
   * Let the aggregatot know that a branch timed out

  public void onRequestTimeout(DsSipResponse response);     */

  /**
   * Ask the aggregatot what's the best response received so far is.
   */
  public DsSipResponse getBestResponse();

 /**
   * Ask the aggregator what's the  response code for the best response received so far is.
   */
  public int getBestResponseReasonCode();

/**
   * Let the aggregator know the proxy failure reason code
   */
  public void onProxyFailure(int reasonCode);


}
