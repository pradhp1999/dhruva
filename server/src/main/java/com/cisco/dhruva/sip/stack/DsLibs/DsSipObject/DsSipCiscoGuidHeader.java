// Copyright (c) 2005-2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import java.net.*;
import java.util.*;

/**
 * This class represents the Cisco-Guid header. The headers are generated from time stamp and some
 * pre-defined value. (Please see EDCS-422175 for details) It provides methods to build, access,
 * modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * CISCO-GUID   =  "Cisco-Guid":" field-field-field-field
 * field =  number
 * </pre> </code>
 */
public final class DsSipCiscoGuidHeader extends DsSipStringHeader {
  /** Header ID. */
  public static final byte sID = CISCO_GUID;

  private static long MASK32 = 0xffffffffL;
  private static long MASK28 = 0x0fffffffL;
  private static long VERSION = 0x10000000L;

  // MMA - 09.01.2005 - adding constants to be used to ensure proper lengths for GCID fields
  private static final String ZERO_FILLER = "0000000000"; // 'FIELD_LENGTH' zeros
  private static final int FIELD_LENGTH = 10;
  private static final int GCID_LENGTH = 43;
  private static final char GCID_FIELD_DELIMITER = '-';

  // MMA - 09.01.05 - changed the way the IP_HASH is cached, but not logic to determine
  private static final String GCID_FIELD_4;

  static {
    int hashCode;
    try {
      InetAddress add = InetAddress.getLocalHost();
      hashCode = add.hashCode();
      if (hashCode < 0) hashCode = -hashCode;
    } catch (Exception e) {
      // If we fail to get the IP_HASH based on local host, fall back to a pseudo random number
      hashCode = (new Random()).nextInt(Integer.MAX_VALUE);
      e.printStackTrace();
    }

    String temp = Integer.toString(hashCode, 10);
    int length = temp.length();
    if (length < FIELD_LENGTH) {
      temp = ZERO_FILLER.substring(length) + temp;
    }
    // no else: temp cannot exceed FIELD_LENGTH (10) because it is based on an int (signed, 32 bit)

    GCID_FIELD_4 = temp;
  }

  /** Default constructor. */
  public DsSipCiscoGuidHeader() {
    super();
  }

  /**
   * Constructs this header with the specified value.<br>
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   */
  public DsSipCiscoGuidHeader(byte[] value) {
    super(value);
  }

  /**
   * Constructs this header with the specified value.<br>
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   * @param offset the offset in the specified byte array, where from the value part, that needs to
   *     be parsed, starts.
   * @param count the total number of bytes, starting from the specified offset, that constitute the
   *     value part.
   */
  public DsSipCiscoGuidHeader(byte[] value, int offset, int count) {
    super(value, offset, count);
  }

  /**
   * Constructs this header with the specified value.<br>
   * The specified byte string <code>value</code> should be the value part (data after the colon) of
   * this header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   */
  public DsSipCiscoGuidHeader(DsByteString value) {
    super(value);
  }

  /**
   * The method returns the token which is the complete name of the header.
   *
   * @return The complete token name
   */
  public DsByteString getToken() {
    return BS_CISCO_GUID;
  }

  /**
   * The method Returns the token which is the compact name of the header.
   *
   * @return The compact token name
   */
  public DsByteString getCompactToken() {
    return BS_CISCO_GUID;
  }

  /**
   * Returns the header name plus ": " as a single byte string token. The header name will be in the
   * compact form if this header is set to be in compact form.
   *
   * @return the header name plus ": " as a single byte string token.
   */
  public final DsByteString getTokenC() {
    return BS_CISCO_GUID_TOKEN;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return CISCO_GUID;
  }

  /**
   * Generating a Cisco-Guid header. The value is composed by timestamp in a string form.
   *
   * @return an Cisco-Guid Header.
   */
  public static DsSipCiscoGuidHeader generateCiscoGuidHeader() {
    // MMA - 09.01.05
    //
    // Changed the way that this method builds up the GCID value
    // to ensure that it meets with the expectations for that value.
    // That is, each field is FIELD_LENGTH long, does not contain
    // negative values, etc.  Also converted to using StringBuffer
    // and strings (rather than multiple DsByteStrings) to minimize
    // overall resource usage.
    //
    // NOTE: the logic behind the values for the fields have *not* changed

    long time = System.currentTimeMillis();
    long f1 = time & MASK32;
    long f2 = (time >> 32) & MASK28 | VERSION;
    int f3 =
        (new Random())
            .nextInt(
                Integer.MAX_VALUE); // MMA - 08.31.05 - change so that we do not get negative values

    String sf1 = Long.toString(f1, 10);
    String sf2 = Long.toString(f2, 10);
    String sf3 = Integer.toString(f3, 10);

    StringBuffer gcidBuffer = new StringBuffer(GCID_LENGTH);
    int length = sf1.length();
    if (length < FIELD_LENGTH) {
      gcidBuffer.append(ZERO_FILLER.substring(length));
    }
    // no else: sf1 cannot exceed FIELD_LENGTH (10) based on the algorithm used above
    gcidBuffer.append(sf1);
    gcidBuffer.append(GCID_FIELD_DELIMITER);

    length = sf2.length();
    if (length < FIELD_LENGTH) {
      gcidBuffer.append(ZERO_FILLER.substring(length));
    }
    // no else: sf2 cannot exceed FIELD_LENGTH (10) based on the algorithm used above
    gcidBuffer.append(sf2);
    gcidBuffer.append(GCID_FIELD_DELIMITER);

    length = sf3.length();
    if (length < FIELD_LENGTH) {
      gcidBuffer.append(ZERO_FILLER.substring(length));
    }
    // no else: sf3 cannot exceed FIELD_LENGTH (10) because it is based on an int (signed, 32 bit)
    gcidBuffer.append(sf3);
    gcidBuffer.append(GCID_FIELD_DELIMITER);

    // GCID_FIELD_4 is guaranteed to be of the correct field length
    gcidBuffer.append(GCID_FIELD_4);

    return new DsSipCiscoGuidHeader(new DsByteString(gcidBuffer.toString()));
  }
}
