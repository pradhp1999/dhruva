package com.cisco.dhruva.sip.DsSipObject;

import com.cisco.dhruva.sip.DsSipParser.*;

/**
 * This interface defines a contract for a SIP message factory that should provide the said APIs for
 * constructing SIP messages either from a byte array or from an input stream.
 */
public interface DsSipMessageFactory {
  /**
   * Parses and Creates a DsSipMessage from the specified <code>bytes</code> byte array.
   *
   * @param bytes the byte array that needs to be parsed as a SIP message.
   * @return the parsed SIP Message.
   * @throws DsSipParserException if there is an error while parsing the specified byte array as a
   *     SIP Message.
   * @throws DsSipParserListenerException if there is an error condition detected by the SIP Message
   *     listener, while parsing.
   */
  DsSipMessage createMessage(byte[] bytes)
      throws DsSipParserListenerException, DsSipParserException;
}
