// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipParser;

/** Specifies the way that the parser informs listeners about headers. */
public interface DsSipHeaderListener {
  /**
   * A header has been found. Always called before headerFound().
   *
   * @param headerId the type of header that was found as defined in DsSipConstants
   * @return element listener or null to lazy parser header
   * @throws DsSipParserListenerException when the listener finds something wrong
   */
  DsSipElementListener headerBegin(int headerId) throws DsSipParserListenerException;

  /**
   * If headerId is UNKNOWN then offset points to beginning of the header name, otherwise, offset
   * points to beginning of the data. <br>
   * Not sure if this unknown behavior is still useful now that there is an unknownFound() method,
   * but we will keep it this way for now. <br>
   * If you wish to find the name and value of a UNKOWN header, return a non-NULL
   * DsSipElementListener from headerBegin. You will receive the name and value through the
   * unknownFound() method.
   *
   * @param headerId the type of header that was found as defined in DsSipConstants
   * @param buffer the byte array containing the header data
   * @param offset the start of the header data in the array
   * @param count the number of characters in the data array that belong to this header
   * @param isValid <code>true</code> if this header was parsed without exception; otherwise <code>
   *     false</code>
   * @throws DsSipParserListenerException when the listener finds something wrong
   */
  void headerFound(int headerId, byte[] buffer, int offset, int count, boolean isValid)
      throws DsSipParserListenerException;

  /**
   * Called when an unknown header is deeply parsed. Unlike the headerFound() method, both the name
   * and the value are returned.
   *
   * @param buffer the byte array containing the header data
   * @param nameOffset the start of the header name in the array
   * @param nameCount the number of characters in the header name
   * @param valueOffset the start of the header data in the array
   * @param valueCount the number of characters in the data array that belong to this header
   * @param isValid <code>true</code> if this header was parsed without exception; otherwise <code>
   *     false</code>
   * @throws DsSipParserListenerException when the listener finds something wrong
   */
  void unknownFound(
      byte[] buffer,
      int nameOffset,
      int nameCount,
      int valueOffset,
      int valueCount,
      boolean isValid)
      throws DsSipParserListenerException;
}
