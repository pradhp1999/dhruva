package com.cisco.dhruva.sip.DsSipParser;

import java.io.*;
import java.util.*;

import com.cisco.dhruva.sip.DsUtil.*;
import com.cisco.dhruva.sip.DsSipObject.*;


/**
 * Used to parse SIP messages.  This is where header parsers are registered as well as new
 * element IDs.
 */
public class DsSipMsgParser
{

    /**
     * Private default constructor. Disallow instance construction.
     */
    private DsSipMsgParser()
    {
    }

    /**
     * Parses a byte array and passes the events to the created listener.
     *
     * @param msgFactory the way to create the listener.
     * @param msg the byte array that contains the SIP message to parse.
     *
     * @return the created listener.
     *
     * @throws DsSipParserException if there is an exception while parsing.
     * @throws DsSipParserListenerException if the listener throws this exception.
     */
    public static DsSipMessageListener parse(DsSipMessageListenerFactory msgFactory, byte msg[])
            throws DsSipParserListenerException, DsSipParserException
    {
        return parse(msgFactory, msg, 0, msg.length);
    }

    /**
     * Parses a byte array and passes the events to the created listener.
     *
     * @param msgFactory the way to create the listener.
     * @param msg the byte array that contains the SIP message to parse.
     * @param offset the start of the SIP message in the array.
     * @param count the number of bytes in the SIP message.
     *
     * @return the created listener.
     *
     * @throws DsSipParserException if there is an exception while parsing.
     * @throws DsSipParserListenerException if the listener throws this exception.
     */
    public static DsSipMessageListener parse(DsSipMessageListenerFactory msgFactory, byte msg[], int offset, int count)
            throws DsSipParserListenerException, DsSipParserException
    {
        DsSipMessageListenerFactory impl = null;
        return (DsSipMessageListener) impl;
    }
}

