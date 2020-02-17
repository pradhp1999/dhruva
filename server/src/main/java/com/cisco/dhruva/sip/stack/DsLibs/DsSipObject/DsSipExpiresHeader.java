// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsDate;

/**
 * This class represents a Expires header as specified in RFC 3261. It provides methods to build,
 * access, modify, serialize and clone the header.
 *
 * <p>Case Sensitive.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Expires       =  "Expires" ":" ( SIP-date | delta-seconds )
 * SIP-date      =  rfc1123-date
 * rfc1123-date  =  wkday "," SP date1 SP time SP "GMT"
 * date1         =  2DIGIT SP month SP 4DIGIT                   ; day month year (e.g., 02 Jun 1982)
 * time          =  2DIGIT ":" 2DIGIT ":" 2DIGIT                ; 00:00:00 - 23:59:59
 * wkday         =  "Mon" | "Tue" | "Wed" | "Thu" | "Fri" | "Sat" | "Sun"
 * month         =  "Jan" | "Feb" | "Mar" | "Apr" | "May" | "Jun" |
 *                  "Jul" | "Aug" | "Sep" | "Oct" | "Nov" | "Dec"
 * delta-seconds =  1*DIGIT
 * </pre> </code>
 */
public final class DsSipExpiresHeader extends DsSipDateOrDeltaHeader {
  /** 3600, the delta seconds default */
  private static final int DEFAULT_DELTA_SECONDS = 3600;

  /** Header token. */
  public static final DsByteString sToken = BS_EXPIRES;
  /** Header ID. */
  public static final byte sID = EXPIRES;
  /** Compact header token. */
  public static final DsByteString sCompactToken = BS_EXPIRES;

  /** Default constructor. */
  public DsSipExpiresHeader() {
    super();
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header
   * and constructs this header.<br>
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   * If there is an exception during parsing phase, it will set the invalid flag of this header and
   * retain the various components that it already parsed. One should check the valid flag before
   * retrieving the various components of this header.
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   * @throws DsSipParserException if there is an error while parsing the specified value into this
   *     header.
   * @throws DsSipParserListenerException if there is an error condition detected by this header as
   *     a Parser Listener, while parsing.
   */
  public DsSipExpiresHeader(byte[] value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value, 0, value.length);
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header
   * and constructs this header.<br>
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   * If there is an exception during parsing phase, it will set the invalid flag of this header and
   * retain the various components that it already parsed. One should check the valid flag before
   * retrieving the various components of this header.
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   * @param offset the offset in the specified byte array, where from the value part, that needs to
   *     be parsed, starts.
   * @param count the total number of bytes, starting from the specified offset, that constitute the
   *     value part.
   * @throws DsSipParserException if there is an error while parsing the specified value into this
   *     header.
   * @throws DsSipParserListenerException if there is an error condition detected by this header as
   *     a Parser Listener, while parsing.
   */
  public DsSipExpiresHeader(byte[] value, int offset, int count)
      throws DsSipParserException, DsSipParserListenerException {
    /*
     CAFFEINE 2.0 bug fix - CSCef03455 It is the initialization sequence problem.
     The origianl super() calling will eventually call down to the child and set child's private date member.
    */
    parse(value, offset, count);
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header
   * and constructs this header.<br>
   * The specified byte string <code>value</code> should be the value part (data after the colon) of
   * this header.<br>
   * If there is an exception during parsing phase, it will set the invalid flag of this header and
   * retain the various components that it already parsed. One should check the valid flag before
   * retrieving the various components of this header.
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   * @throws DsSipParserException if there is an error while parsing the specified value into this
   *     header.
   * @throws DsSipParserListenerException if there is an error condition detected by this header as
   *     a Parser Listener, while parsing.
   */
  public DsSipExpiresHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Constructs this header with the specified <code>deltaSeconds</code> value.
   *
   * @param deltaSeconds the delta seconds that needs to be set for this header.
   */
  public DsSipExpiresHeader(long deltaSeconds) {
    super(deltaSeconds);
  }

  /**
   * Constructs this header with the specified <code>date</code> value. It will set the delta
   * seconds value based on the time difference between this specified <code>date</code> and the
   * current time in seconds, i.e (delta seconds = date - current time). If the time difference is
   * in negative then delta seconds will be set to 0.
   *
   * @param date the date that needs to be set for this header.
   */
  public DsSipExpiresHeader(DsDate date) {
    super(date);
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
    return BS_EXPIRES_TOKEN;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return EXPIRES;
  }

  /**
   * Sets the delta seconds value for this header to the specified value only if the specified
   * <code>deltaSeconds</code> value is >= 0.
   *
   * @param deltaSeconds the delta seconds that needs to be set for this header.
   */
  public void setDeltaSeconds(long deltaSeconds) {
    if (deltaSeconds < 0) {
      return;
    }
    m_lDeltaSeconds = deltaSeconds;
  }

  //    public static void main(String[] args)
  //    {
  //        try
  //        {
  //            byte[] bytes = read();
  //            DsSipExpiresHeader header = new DsSipExpiresHeader(bytes);
  //            System.out.println();
  //            System.out.println("<<<<<<<<<<<<<<<<< HEADER >>>>>>>>>>>>>>>>>>>>");
  //            System.out.println();
  //            header.write(System.out);
  //            System.out.println();
  //            System.out.println();
  //            System.out.println("<<<<<<<<<<<<<<<<< CLONE >>>>>>>>>>>>>>>>>>>>");
  //            System.out.println();
  //            DsSipExpiresHeader clone = (DsSipExpiresHeader) header.clone();
  //            clone.write(System.out);
  //            System.out.println();
  //            System.out.println();
  //            System.out.println("<<<<<<<<<<<<<<<<< (HEADER == CLONE) = "
  //                                                    + header.equals(clone)
  //                                                    +" >>>>>>>>>>>>>>>>>>>>");
  //            System.out.println();
  //            System.out.println();
  //            System.out.println("<<<<<<<<<<<<<<<<< (CLONE == HEADER) = "
  //                                                    + clone.equals(header)
  //                                                    +" >>>>>>>>>>>>>>>>>>>>");
  //            System.out.println();
  //        }
  //        catch(Exception e)
  //        {
  //            e.printStackTrace();
  //        }
  //    }// Ends main()
}
