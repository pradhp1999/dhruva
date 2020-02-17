// Copyright (c) 2004-2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipMime;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.*;

/**
 * Interface DsMimeBodyParser provides a single method to parse bodies of a particular content type.
 * Common parsers are provided by the stack, such as multipart/mixed and SDP parsers. The body of
 * the entity should be in parsed form after this method call.
 */
public interface DsMimeBodyParser {
  /**
   * Parses the body of the entity. The body of the entity should be in parsed form after this
   * method call.
   *
   * @param entity MIME entity whose body is unparsed
   * @throws DsSipParserException if there is parsing error.
   */
  public void parse(DsMimeEntity entity) throws DsSipParserException;
} // End of public interface DsMimeBodyParser
