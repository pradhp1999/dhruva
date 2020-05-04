// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import java.io.*;
import java.security.SecureRandom;

/**
 * This class represents a CSeq header as specified in RFC 3261. It provides methods to build,
 * access, modify, serialize and clone the header.
 *
 * <p>Method names are case sensitive. New spec allows for extension methods.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * CSeq             =  "CSeq" ":" 1*DIGIT Method
 * Method           =  "INVITE" | "ACK" | "OPTIONS" | "BYE" | "CANCEL" | "REGISTER" | extension-method
 * extension-method =  token
 * </pre> </code>
 */
public final class DsSipCSeqHeader extends DsSipSeqMethodHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_CSEQ;
  /** Header ID. */
  public static final byte sID = CSEQ;
  /** Compact header token. */
  public static final DsByteString sCompactToken = BS_CSEQ;

  /** Random sequence number generator */
  private static final SecureRandom seqGenerator = new SecureRandom();

  public static final byte sFixedFormatHeaderId =
      DsTokenSipConstants.TOKEN_SIP_FIXED_FORMAT_CSEQ_HEADER;

  /** Default constructor. */
  public DsSipCSeqHeader() {
    super();
  }

  /**
   * Constructs this header with the specified method.
   *
   * @param method the method for this header.
   */
  public DsSipCSeqHeader(DsByteString method) {
    super(seqGenerator.nextInt(Integer.MAX_VALUE), method);
  }

  /**
   * Construct a SIP CSeq header given a sequence number and a method.
   *
   * @param number the sequence number.
   * @param method the method name.
   */
  public DsSipCSeqHeader(long number, DsByteString method) {
    super(number, method);
  }

  /**
   * Construct a SIP CSeq header given a sequence number and a method.
   *
   * @param number the sequence number.
   * @param method the method type.
   */
  public DsSipCSeqHeader(long number, int method) {
    super(number, method);
  }

  /**
   * Returns the token which is the name of the header.
   *
   * @return the token value.
   */
  public DsByteString getToken() {
    return sToken;
  }

  /**
   * Returns the token which is the compact name of the header.
   *
   * @return the compact token name.
   */
  public DsByteString getCompactToken() {
    return sCompactToken;
  }

  /**
   * Returns the header name plus ": " as a single byte string token. The header name will be in the
   * compact form if this header is set to be in compact form.
   *
   * @return the header name plus ": " as a single byte string token.
   */
  public final DsByteString getTokenC() {
    return BS_CSEQ_TOKEN;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return CSEQ;
  }

  /**
   * Returns a random sequence number.
   *
   * @return a random sequence number.
   */
  public static long reGenerate() {
    return seqGenerator.nextInt(Integer.MAX_VALUE);
  }

  public void writeEncodedHeaderName(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    out.write(sFixedFormatHeaderId);
  }
} // Ends DsSipCSeqHeader definition
