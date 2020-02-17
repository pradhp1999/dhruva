// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

/** This interface provides APIs for generating nonce value for a SIP request. */
public interface DsSipNonceInterface {
  /**
   * Generates the nonce value as per the algorithm specified by rfc2617. <br>
   * <br>
   * nonce = time-stamp H(time-stamp ":" ETag ":" private-key) <br>
   * where time-stamp is a server-generated time or other non-repeating value, ETag is the value of
   * the HTTP ETag header associated with the requested entity, and private-key is data known only
   * to the server.
   *
   * @param timestamp the time-stamp tick that needs to be used in place of time-stamp in the above
   *     algorithm.
   * @return the nonce value as an un-hashed byte array of (time-stamp ":" ETag ":" private-key)
   */
  byte[] nonce(int timestamp);

  /**
   * Generates the nonce value for the specified SIP request as per the algorithm defined by the
   * "predictive nonce" draft for the HTTP Digest authentication scheme. The predictive nonce is
   * computed as:<br>
   * <br>
   * nonce = H(source-IP:&LTcanonicalization of headers to be protected&GT:round-time) <br>
   * where H is a suitable cryptographic hash function. <br>
   *
   * @param request the SIP request whose headers needs to be protected for message integrity
   * @param timestamp the round-time tick that needs to be used in place of round-time in the above
   *     algorithm.
   * @return the predictive nonce value as an un-hashed byte array of the value
   *     (source-IP:&LTcanonicalization of headers to be protected&GT:round-time)
   */
  byte[] predictiveNonce(DsSipRequest request, int timestamp);

  /**
   * Sets the option whether the CSeq method should be protected while calculating the predictive
   * nonce. The CSeq method will be included if this option is set to <code>true</code>, otherwise
   * the CSeq method will not be protected.
   *
   * @param protect if <code>true</code> then the CSeq method will be protected in the predictive
   *     nonce computation.
   */
  void setMethodProtection(boolean protect);

  /**
   * Tells whether the CSeq method will be protected while calculating the predictive nonce. The
   * CSeq method will be included if this option is set to <code>true</code>, otherwise the CSeq
   * method will not be protected.
   *
   * @return <code>true</code> if the CSeq method will be protected in the predictive nonce
   *     computation, <code>false</code> otherwise.
   */
  boolean isMethodProtection();

  /**
   * Sets the list of headers to be protected by predictive nonce computation. The specified array
   * should contain the constant header Ids, as defined in the {@link DsSipConstants} class, of the
   * headers that needs to be protected by predictive nonce computation. <br>
   * This header ids list is used in case of predictive nonce generation.
   *
   * @param hIds the array containing the integer constant header Ids as defined in the
   *     DsSipParserConstants class, of the headers that needs to be protected by predictive nonce
   *     computation
   */
  void setHeadersToProtect(int[] hIds);

  /**
   * Tells the list of headers that will be protected by predictive nonce computation. The returned
   * array contains the constant header Ids, as defined in the {@link DsSipConstants} class, of the
   * headers that will be protected by predictive nonce computation. <br>
   * This header ids list is used in case of predictive nonce generation.
   *
   * @return the array containing the integer constant header Ids as defined in the
   *     DsSipParserConstants class, of the headers that will be protected by predictive nonce
   *     computation
   */
  int[] getHeadersToProtect();

  /**
   * Sets the current time window. This time window is used in case of normal nonce generation. This
   * time window defines that after how long the generated nonce will be valid. It means with in
   * this time period, all parameters being same, generated nonce value will be same.
   *
   * @param timeInMinutes the time window in minutes
   */
  void setTimeWindow(int timeInMinutes);

  /**
   * Returns the current time window. This time window is used in case of normal nonce generation.
   * This time window defines that after how long the generated nonce will be valid. It means with
   * in this time period, all parameters being same, generated nonce value will be same.
   *
   * @return the time window in minutes
   */
  int getTimeWindow();

  /**
   * Sets the Client Nonce "cnonce" time window value in seconds. This time window defines that how
   * long the generated cnonce will be valid.
   *
   * @param timeInSeconds the cnonce time window in seconds.
   */
  void setCNTimeWindow(int timeInSeconds);

  /**
   * Returns the Client Nonce "cnonce" time window value in seconds. This time window defines that
   * how long the generated cnonce will be valid.
   *
   * @return the cnonce time window in seconds
   */
  int getCNTimeWindow();
}
