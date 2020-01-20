package com.cisco.dhruva.sip.DsSipParser;

/**
 * Specifies the way that the parser notifies about events while parsing a SIP message.
 */
public interface DsSipMessageListener
{
    /**
     * Notify the message listener that the beginning of the Request-URI was found.
     * Always called first for requests, not called for responses.
     *
     * @param buffer holds the URL scheme
     * @param schemeOffset the start of the URL scheme name
     * @param schemeCount the length of the URL scheme name
     *
     * @return a DsSipElementListener to notify of the parsing on the Request-URI or
     *         <code>null</code> to lazy parse the URI
     *
     * @throws DsSipParserListenerException when there is a problem with the data that was received
     */
    DsSipElementListener requestURIBegin(byte[] buffer, int schemeOffset, int schemeCount)
            throws DsSipParserListenerException;

    /**
     * Notify the message listener that the end of the Request-URI was found, and what it is.
     * Always called first for requests (even if it was lazy parsed), not called for responses.
     *
     * @param buffer holds the URL scheme and data
     * @param offset the start of the Request-URI
     * @param count the length of the Request-URI
     * @param isValid <code>true</code> if the Request-URI of the message is valid; otherwise <code>false</code>
     *
     * @throws DsSipParserListenerException when there is a problem with the data that was received
     */
    void requestURIFound(byte[] buffer, int offset, int count, boolean isValid) throws DsSipParserListenerException;

    /* CAFFEINE 2.0 DEVELOPMENT - Methods below are moved to DsSipMimeMessageListener.java
    DsSipHeaderListener getHeaderListener();
    void messageFound(byte[] buffer, int offset, int count, boolean isValid) throws DsSipParserListenerException;
     */

    /**
     * Called for both responses and requests, when the protocol and version is determined.
     * These are reported all at once for performance considerations.
     *
     * @param buffer holds the protocol and version information
     * @param protocolOffset the start of the protocol
     * @param protocolCount the length of the protocol
     * @param majorOffset the start of the major version
     * @param majorCount the length of the major version
     * @param minorOffset the start of the minor version
     * @param minorCount the length of the minor version
     * @param valid <code>true</code> if the protocol and version of the message are valid; otherwise <code>false</code>
     *
     * @throws DsSipParserListenerException when there is a problem with the data that was received
     */
    void protocolFound(byte[] buffer, int protocolOffset, int protocolCount,
                       int majorOffset, int majorCount,
                       int minorOffset, int minorCount,
                       boolean valid) throws DsSipParserListenerException;

    /**
     * Only called when a message body is found in the headers section of a Request-URI.
     *
     * @param buffer the body of the message
     * @param offset the start of the body
     * @param count the length of the body
     *
     * @throws DsSipParserListenerException when there is a problem with the data that was received
     */
    void bodyFoundInRequestURI(byte[] buffer, int offset, int count) throws DsSipParserListenerException;
}
