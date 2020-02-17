package com.cisco.dhruva.sip.stack.DsLibs.DsSipMime;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.*;

/**
 * DsMimeMessageListener specifies the way that the parser notifies about events while parsing a
 * MIME message.
 */
public interface DsMimeMessageListener {

  /**
   * Gets the header listener for this message.
   *
   * @return the header listener for this message or <code>null</code> if not interested in headers.
   */
  DsSipHeaderListener getHeaderListener();

  /**
   * This is called at the end of a message, whether or not there's a body and is always called
   * last. The body is returned in buffer. If there is no body present then buffer =
   * DsByteString.BS_EMPTY_STRING.data() and offset and count = 0.
   *
   * <p>If the message is invalid, then an exception will be thrown by the original call to parse().
   *
   * @param buffer the body of the message or DsSipConstants.EMPTY_BODY
   * @param offset the start of the body
   * @param count the length of the body
   * @param isValid <code>true</code> if the body of the message is valid; otherwise <code>false
   *     </code> (body has too few bytes)
   * @throws DsSipParserListenerException when there is a problem with the data that was received
   */
  void messageFound(byte[] buffer, int offset, int count, boolean isValid)
      throws DsSipParserListenerException;
}
