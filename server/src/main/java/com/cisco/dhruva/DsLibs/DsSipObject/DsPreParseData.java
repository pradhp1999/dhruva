// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

/**
 * A class that holds data from an early parse of the message. One of the main points is to be able
 * to retrieve the key from the Via header.
 */
public final class DsPreParseData {
  private DsByteString m_key;
  private DsByteString m_responseCode;
  private DsByteString m_method;

  /** Constructs and empty object. */
  public DsPreParseData() {}

  /**
   * Set the key found in the message.
   *
   * @param buffer the byte[] that contains the key
   * @param offset the start of the key
   * @param count the number of characters in the key
   * @see #getKey
   */
  public void setKey(byte[] buffer, int offset, int count) {
    m_key = new DsByteString(buffer, offset, count);
  }

  /**
   * Set the response code found in the message.
   *
   * @param buffer the byte[] that contains the response code
   * @param offset the start of the response code
   * @param count the number of characters in the response code
   * @see #getResponseCode
   */
  public void setResponseCode(byte[] buffer, int offset, int count) {
    m_responseCode = new DsByteString(buffer, offset, count);
  }

  /**
   * Set the method found in the message.
   *
   * @param buffer the byte[] that contains the method
   * @param offset the start of the method
   * @param count the number of characters in the method
   * @see #getMethod
   */
  public void setMethod(byte[] buffer, int offset, int count) {
    m_method = new DsByteString(buffer, offset, count);
  }

  /**
   * Get the key found in the message.
   *
   * @return the key for this message, or <code>null</code> if not found
   * @see #setKey
   */
  public DsByteString getKey() {
    return m_key;
  }

  /**
   * Get the response code found in the message.
   *
   * @return the response code for this message, or <code>null</code> if is was not a response
   * @see #setResponseCode
   */
  public DsByteString getResponseCode() {
    return m_responseCode;
  }

  /**
   * Get the method found in the message.
   *
   * @return the method for this message, or <code>null</code> if is was not a request
   * @see #setMethod
   */
  public DsByteString getMethod() {
    return m_method;
  }

  /**
   * Find out if the message that was pre-parsed was a request or a response.
   *
   * @return <code>true</code> if is was a request, or <code>false</code> if is was a response
   */
  public boolean isRequest() {
    return (m_method != null);
  }
}
