package com.cisco.dhruva.sip.DsSipParser;

/** Specifies the way that the parser obtains message listeners. */
public interface DsSipMessageListenerFactory {
  /**
   * Returns a message listener for requests.
   *
   * @param buffer the byte array containing the method name
   * @param methodOffset the beginning of the method name
   * @param methodCount the number of bytes in the method name
   * @param isEncoded <code>true</code> if the message is encoded
   * @return the listener or <code>null</code> to ignore the request
   * @throws DsSipParserListenerException if the listener decides there is a problem
   */
  DsSipMessageListener requestBegin(
      byte[] buffer, int methodOffset, int methodCount, boolean isEncoded)
      throws DsSipParserListenerException;

  /**
   * Returns a message listener for responses.
   *
   * @param buffer the byte array containing the response code
   * @param code the response code
   * @param reasonOffset the beginning of the reason string
   * @param reasonCount the number of bytes in the reason string
   * @param isEncoded <code>true</code> if the message is encoded
   * @return the listener or <code>null</code> to ignore the response
   * @throws DsSipParserListenerException if the listener decides there is a problem
   */
  DsSipMessageListener responseBegin(
      byte[] buffer, int code, int reasonOffset, int reasonCount, boolean isEncoded)
      throws DsSipParserListenerException;
}
