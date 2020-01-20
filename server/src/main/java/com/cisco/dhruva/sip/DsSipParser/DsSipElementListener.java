package com.cisco.dhruva.sip.DsSipParser;

/**
 * Interface that defines the way that the parser reports events about headers.
 */
public interface DsSipElementListener
{
    /**
     * Always called before elementFound() or parameterFound().
     *
     * @param contextId the context id of the thing that is being parsed
     * @param elementId the element id that was found while parsing
     *
     * @throws DsSipParserListenerException when there is a problem with the data that was received
     *
     * @return element listener or null to lazy parser sub element
     */
    DsSipElementListener elementBegin(int contextId, int elementId) throws DsSipParserListenerException;

    /**
     * Called when and element is found inside of a context.
     *
     * @param contextId the context id of the thing that is being parsed
     * @param elementId the element id that was found while parsing
     * @param buffer the data where the element was found
     * @param offset the start of the element
     * @param count the length of the element
     * @param isValid <code>true</code> if there were no exceptions while parsing; otherwise <code>false</code>
     *
     * @throws DsSipParserListenerException when there is a problem with the data that was received
     */
    void elementFound(int contextId, int elementId, byte[] buffer, int offset, int count, boolean isValid)
            throws DsSipParserListenerException;

    /**
     * The parser found a generic parameter.  Only called when it is not a known parameter, for
     * any header or url, not just the current header.
     *
     * @param contextId the context id of the thing that is doing the parsing
     * @param buffer the data that was found, note that both the name and value are in the same byte[]
     * @param nameOffset the start of the name
     * @param nameCount the length of the name
     * @param valueOffset the start of the value
     * @param valueCount the length of the value
     *
     * @throws DsSipParserListenerException when there is a problem with the data that was received
     */
    void parameterFound(int contextId, byte[] buffer, int nameOffset, int nameCount,
                        int valueOffset, int valueCount) throws DsSipParserListenerException;
}


