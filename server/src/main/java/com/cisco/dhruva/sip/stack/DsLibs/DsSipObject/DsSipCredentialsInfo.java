// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

/** This interface represents challenge information for headers. */
public interface DsSipCredentialsInfo {
  /**
   * Generates the challenge string for the header.
   *
   * @param request the request to generate the credentials from.
   * @return the generated challenge string.
   */
  DsByteString generateCredentials(DsSipRequest request);

  /**
   * Validates the specified request as per this challenge info. The user credentials present in the
   * authorization header of the specified request are retrieved and validated against this
   * challenge info. The credentials can be either VALID, INVALID, STALE or MAX_TRY_OUT and is
   * denoted by the return value. Where the return values are: <br>
   * VALID - if the credentials are valid <br>
   * INVALID - if the credentials are not valid <br>
   * STALE - if the nonce value present in the credentials is stale. In other words, this nonce
   * value is no longer valid and new nonce value should be used. Also this means that the user
   * credentials may be valid but the time for what the nonce value was valid has expired. So in
   * this case, client can just resend the request with these credentials itself and may not need to
   * prompt the user to enter his/her user and password information.<br>
   * MAX_TRY_OUT - if the client has already tried for the maximum number of times that this server
   * was willing to tolerate. The server might response with 500 class response to this request and
   * possibly raise an alarm or notification to the server administrator for possible replay
   * attacks.<br>
   * The values STALE and MAX_TRY_OUT can be returned only in case of Digest Authentication scheme.
   *
   * @param request the request that needs to be validated against this challenge info.
   * @return a numeric value specifying whether the request is VALID, INVALID, STALE or MAX_TRY_OUT.
   */
  public short validate(DsSipRequest request);

  /**
   * Validates the specified request as per this challenge info. The user credentials present in the
   * specified authorization header <code>header</code> are validated against this challenge info.
   * The credentials can be either VALID, INVALID, STALE or MAX_TRY_OUT and is denoted by the return
   * value. Where the return values are: <br>
   * VALID - if the credentials are valid <br>
   * INVALID - if the credentials are not valid <br>
   * STALE - if the nonce value present in the credentials is stale. In other words, this nonce
   * value is no longer valid and new nonce value should be used. Also this means that the user
   * credentials may be valid but the time for what the nonce value was valid has expired. So in
   * this case, client can just resend the request with these credentials itself and may not need to
   * prompt the user to enter his/her user and password information.<br>
   * MAX_TRY_OUT - if the client has already tried for the maximum number of times that this server
   * was willing to tolerate. The server might response with 500 class response to this request and
   * possibly raise an alarm or notification to the server administrator for possible replay
   * attacks.<br>
   * The values STALE and MAX_TRY_OUT can be returned only in case of Digest Authentication scheme.
   *
   * @param request the request that needs to be validated against this challenge info
   * @param header the authorization header that needs to be validated against this challenge info.
   * @return a numeric value specifying whether the request is VALID, INVALID, STALE or MAX_TRY_OUT.
   */
  public short validate(DsSipRequest request, DsSipAuthorizationHeaderBase header);

  // qfang - 05.11.06 - CSCsd948585 client authentication enhancement
  /**
   * Retrieve realm this credential belong to
   *
   * @return DsByteString
   */
  public DsByteString getRealm();

  /**
   * Retrieve authentication type (e.g. "Basic" or "Digest")
   *
   * @return DsByteString
   */
  public DsByteString getType();
}
