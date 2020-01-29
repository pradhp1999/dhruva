// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;

/**
 * This class represents a Date header as specified in RFC 3261. It provides methods to build,
 * access, modify, serialize and clone the header.
 *
 * <p>Case Sensitive.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Date         =  "Date:" SIP-date
 * SIP-date     =  rfc1123-date
 * rfc1123-date =  wkday "," SP date1 SP time SP "GMT"
 * date1        =  2DIGIT SP month SP 4DIGIT                   ; day month year (e.g., 02 Jun 1982)
 * time         =  2DIGIT ":" 2DIGIT ":" 2DIGIT                ; 00:00:00 - 23:59:59
 * wkday        =  "Mon" | "Tue" | "Wed" | "Thu" | "Fri" | "Sat" | "Sun"
 * month        =  "Jan" | "Feb" | "Mar" | "Apr" | "May" | "Jun" |
 *                 "Jul" | "Aug" | "Sep" | "Oct" | "Nov" | "Dec"
 * </pre> </code>
 */
public final class DsSipDateHeader extends DsSipDateOnlyHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_DATE;
  /** Header ID. */
  public static final byte sID = DATE;
  /** Compact header token. */
  public static final DsByteString sCompactToken = BS_DATE;

  /** Default Constructor. */
  public DsSipDateHeader() {
    super();
  }

  /**
   * Constructor used to set the date header with date in a string format.
   *
   * @param pDateString the date value.
   * @throws DsSipParserException if the date cannot be created.
   */
  public DsSipDateHeader(DsByteString pDateString) throws DsSipParserException {
    super();
    setDate(pDateString);
  }

  /**
   * Constructor used to set the date header with a DsDate.
   *
   * @param pDate the date value
   */
  public DsSipDateHeader(DsDate pDate) {
    super(pDate);
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
    return BS_DATE_TOKEN;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public int getHeaderID() {
    return DATE;
  }
}
