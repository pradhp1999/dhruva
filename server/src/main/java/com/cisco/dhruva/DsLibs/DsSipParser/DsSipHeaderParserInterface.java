// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipParser;

/** Specifies the way the that message parser tells a header parser to parse a header. */
public interface DsSipHeaderParserInterface {
  /**
   * Parses the data and notifies the listener when components of the header are recognized. <code>
   * data</code> will not contain terminating end of line characters.
   *
   * @param headerListener the listener to be notified of the recognized components of the header
   * @param headerType the integer representation of the type of header being parsed
   * @param data the byte array that contains the data to be parsed
   * @param offset the starting point in <code>data</code> of this array
   * @param count the number of bytes in <code>data</code> that are part of this header
   * @throws DsSipParserListenerException when <code>headerListener</code> has an exception, this is
   *     a way for it to pass exceptions through to the caller of parseHeader
   * @throws DsSipParserException when the parser itself finds an exception with the header being
   *     parsed
   */
  public void parseHeader(
      DsSipHeaderListener headerListener, int headerType, byte[] data, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException;
}
