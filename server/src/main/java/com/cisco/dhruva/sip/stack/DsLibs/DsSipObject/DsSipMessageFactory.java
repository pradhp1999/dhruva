// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.*;
import java.io.IOException;

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

  /**
   * Parses and Creates a DsSipMessage from the specified <code>bytes</code> byte array.
   *
   * @param bytes the byte array that needs to be parsed as a SIP message.
   * @param offset the offset where from the message bytes start in the specified <code>bytes</code>
   *     byte array.
   * @param count the number of bytes beginning from the offset that needs to be considered while
   *     parsing the message.
   * @return the parsed SIP Message.
   * @throws DsSipParserException if there is an error while parsing the specified byte array as a
   *     SIP Message.
   * @throws DsSipParserListenerException if there is an error condition detected by the SIP Message
   *     listener, while parsing.
   */
  DsSipMessage createMessage(byte[] bytes, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException;

  /**
   * Parses and Creates a DsSipMessage from the specified <code>bytes</code> byte array. If the
   * specified flag <code>createKey</code> is <code>true</code> then the message key will also be
   * parsed while parsing the message. Similarly if the specified flag <code>validate</code> is
   * <code>true</code> then the parsed message will also be validated.
   *
   * @param bytes the byte array that needs to be parsed as a SIP message.
   * @param offset the offset where from the message bytes start in the specified <code>bytes</code>
   *     byte array.
   * @param count the number of bytes beginning from the offset that needs to be considered while
   *     parsing the message.
   * @param createKey if <code>true</code>, the message key will also be generated while parsing the
   *     message.
   * @param validate if <code>true</code> then the message will also be validated after parsing.
   * @return the parsed SIP Message.
   * @throws DsSipParserException if there is an error while parsing the specified byte array as a
   *     SIP Message.
   * @throws DsSipParserListenerException if there is an error condition detected by the SIP Message
   *     listener, while parsing.
   */
  DsSipMessage createMessage(
      byte[] bytes, int offset, int count, boolean createKey, boolean validate)
      throws DsSipParserListenerException, DsSipParserException;

  /**
   * Parses and Creates a DsSipMessage from the specified <code>in</code> input stream. If the
   * specified flag <code>createKey</code> is <code>true</code> then the message key will also be
   * parsed while parsing the message. Similarly if the specified flag <code>validate</code> is
   * <code>true</code> then the parsed message will also be validated.
   *
   * @param in the input stream where from bytes need to be read for parsing SIP message.
   * @param createKey if <code>true</code>, the message key will also be generated while parsing the
   *     message.
   * @param validate if <code>true</code> then the message will also be validated after parsing.
   * @return the parsed SIP Message.
   * @throws DsSipParserException if there is an error while parsing the specified byte array as a
   *     SIP Message.
   * @throws DsSipParserListenerException if there is an error condition detected by the SIP Message
   *     listener, while parsing.
   * @throws IOException if there is an error while reading from the specified input stream.
   */
  DsSipMessage createMessage(DsSipFrameStream in, boolean createKey, boolean validate)
      throws DsSipParserListenerException, DsSipParserException, IOException;
  /**
   * Parses and Creates a DsSipMessage from the specified <code>in</code> input stream.
   *
   * @param in the input stream where from bytes need to be read for parsing SIP message.
   * @return the parsed SIP Message.
   * @throws DsSipParserException if there is an error while parsing the specified byte array as a
   *     SIP Message.
   * @throws DsSipParserListenerException if there is an error condition detected by the SIP Message
   *     listener, while parsing.
   * @throws IOException if there is an error while reading from the specified input stream.
   */
  DsSipMessage createMessage(DsSipFrameStream in)
      throws DsSipParserListenerException, DsSipParserException, IOException;
}
