// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

/** Defines various invalid reason numerical, string and byte string constants. */
public final class DsSipInvalidReasons {
  /** The Strings that the ints map to. */
  private static final String reasonArray[];

  /** The numerical constant indicating a valid SIP Message. */
  public static final int VALID = -1;
  /** The numerical constant indicating an invalid SIP Message with a general error condition. */
  public static final int GENERIC = 0;
  /** The numerical constant indicating an invalid SIP Message with an invalid mandatory header. */
  public static final int MANDATORY_HEADER_INVALID = 1;
  /** The numerical constant indicating an invalid SIP Message with an invalid body. */
  public static final int BODY_INVALID = 2;
  /** The numerical constant indicating an invalid SIP Message with a missing Via header. */
  public static final int MISSING_VIA_HEADER = 3;
  /** The numerical constant indicating an invalid SIP Message with a missing To header. */
  public static final int MISSING_TO_HEADER = 4;
  /** The numerical constant indicating an invalid SIP Message with a missing From header. */
  public static final int MISSING_FROM_HEADER = 5;
  /** The numerical constant indicating an invalid SIP Message with a missing CSeq header. */
  public static final int MISSING_CSEQ_HEADER = 6;
  /** The numerical constant indicating an invalid SIP Message with a missing Call-ID header. */
  public static final int MISSING_CALL_ID_HEADER = 7;
  /** The numerical constant indicating an invalid SIP Message with multiple To headers. */
  public static final int MULTIPLE_TO_HEADERS = 8;
  /** The numerical constant indicating an invalid SIP Message with with multiple From headers. */
  public static final int MULTIPLE_FROM_HEADERS = 9;
  /** The numerical constant indicating an invalid SIP Message with with multiple CSeq headers. */
  public static final int MULTIPLE_CSEQ_HEADERS = 10;
  /**
   * The numerical constant indicating an invalid SIP Message with with multiple Call-ID headers.
   */
  public static final int MULTIPLE_CALL_ID_HEADERS = 11;
  /**
   * The numerical constant indicating an invalid SIP Message where CSeq method name doesn't match
   * with the SIP message method.
   */
  public static final int CSEQ_MISMATCH = 12;
  /**
   * The numerical constant indicating an invalid SIP Message with a missing Content-Type header.
   */
  public static final int MISSING_CONTENT_TYPE = 13;
  /**
   * The numerical constant indicating an invalid Invite SIP Message with a missing Contact header.
   */
  public static final int INVITE_MISSING_CONTACT = 14;
  /**
   * The numerical constant indicating an invalid Register SIP Message where Request URI has a user
   * part.
   */
  public static final int REGISTER_HAS_USER = 15;
  /**
   * The numerical constant indicating an invalid SIP Message with no method name in the CSeq
   * header.
   */
  public static final int MISSING_CSEQ_METHOD = 16;
  /** The numerical constant indicating an invalid SIP Message with an invalid CSeq header. */
  public static final int INVALID_CSEQ_HEADER = 17;
  /**
   * The numerical constant indicating an invalid SIP Message with an invalid Content-Length header.
   */
  public static final int INVALID_CONTENT_LENGTH = 18;
  /**
   * The numerical constant indicating an invalid SIP Message with an invalid Content-Type header.
   */
  public static final int INVALID_CONTENT_TYPE = 19;
  /** The numerical constant indicating an invalid SIP Message with a malformed Request URI. */
  public static final int MALFORMED_REQUEST_URI = 20;
  /** The numerical constant indicating an invalid SIP Message with an invalid major version. */
  public static final int INVALID_MAJOR_VERSION = 21;
  /** The numerical constant indicating an invalid SIP Message where protocol is not SIP. */
  public static final int PROTOCOL_NOT_SIP = 22;
  /** The numerical constant indicating a NOTIFY is missing a Subscription-State header. */
  public static final int NOTIFY_MISSING_SUB_STATE = 23;
  /** The numerical constant representing the NOTIFY is missing the Event header. */
  public static final int NOTIFY_SUBSCRIBE_EVENT = 24;
  /** The numerical constant representing the message has a bad CSeq number. */
  public static final int INVALID_CSEQ_NUMBER = 25;
  /** The numerical constant indicating an invalid SIP Message with an invalid minor version. */
  public static final int INVALID_MINOR_VERSION = 26;
  /** The constant representing the total number of defined Invalid reasons. */
  private static final int SIZE = 27;

  static final String STR_GENERIC = "Bad Request";
  static final DsByteString BS_GENERIC = new DsByteString(STR_GENERIC);

  static final String STR_MANDATORY_HEADER_INVALID = "Missing Mandatory Header";
  static final DsByteString BS_MANDATORY_HEADER_INVALID =
      new DsByteString(STR_MANDATORY_HEADER_INVALID);

  static final String STR_BODY_INVALID = "Body Missing Bytes";
  static final DsByteString BS_BODY_INVALID = new DsByteString(STR_BODY_INVALID);

  static final String STR_MISSING_VIA_HEADER = "Missing Via Header";
  static final DsByteString BS_MISSING_VIA_HEADER = new DsByteString(STR_MISSING_VIA_HEADER);

  static final String STR_MISSING_TO_HEADER = "Missing To Header";
  static final DsByteString BS_MISSING_TO_HEADER = new DsByteString(STR_MISSING_TO_HEADER);

  static final String STR_MISSING_FROM_HEADER = "Missing From Header";
  static final DsByteString BS_MISSING_FROM_HEADER = new DsByteString(STR_MISSING_FROM_HEADER);

  static final String STR_MISSING_CSEQ_HEADER = "Missing CSeq Header";
  static final DsByteString BS_MISSING_CSEQ_HEADER = new DsByteString(STR_MISSING_CSEQ_HEADER);

  static final String STR_MISSING_CALL_ID_HEADER = "Missing Call-ID Header";
  static final DsByteString BS_MISSING_CALL_ID_HEADER =
      new DsByteString(STR_MISSING_CALL_ID_HEADER);

  static final String STR_MULTIPLE_TO_HEADERS = "Multiple To Headers";
  static final DsByteString BS_MULTIPLE_TO_HEADERS = new DsByteString(STR_MULTIPLE_TO_HEADERS);

  static final String STR_MULTIPLE_FROM_HEADERS = "Multiple From Headers";
  static final DsByteString BS_MULTIPLE_FROM_HEADERS = new DsByteString(STR_MULTIPLE_FROM_HEADERS);

  static final String STR_MULTIPLE_CSEQ_HEADERS = "Multiple CSeq Headers";
  static final DsByteString BS_MULTIPLE_CSEQ_HEADERS = new DsByteString(STR_MULTIPLE_CSEQ_HEADERS);

  static final String STR_MULTIPLE_CALL_ID_HEADERS = "Multiple Call-ID Headers";
  static final DsByteString BS_MULTIPLE_CALL_ID_HEADERS =
      new DsByteString(STR_MULTIPLE_CALL_ID_HEADERS);

  static final String STR_CSEQ_MISMATCH = "Method Type and CSeq Type Do Not Match";
  static final DsByteString BS_CSEQ_MISMATCH = new DsByteString(STR_CSEQ_MISMATCH);

  static final String STR_MISSING_CONTENT_TYPE = "Body Present But Missing Content-Type";
  static final DsByteString BS_MISSING_CONTENT_TYPE = new DsByteString(STR_MISSING_CONTENT_TYPE);

  static final String STR_INVITE_MISSING_CONTACT =
      "INVITE Message Missing Mandatory Contact Header";
  static final DsByteString BS_INVITE_MISSING_CONTACT =
      new DsByteString(STR_INVITE_MISSING_CONTACT);

  static final String STR_REGISTER_HAS_USER = "REGISTER Message Has User Part In Request-URI";
  static final DsByteString BS_REGISTER_HAS_USER = new DsByteString(STR_REGISTER_HAS_USER);

  static final String STR_MISSING_CSEQ_METHOD = "Method name is missing in the CSeq Header";
  static final DsByteString BS_MISSING_CSEQ_METHOD = new DsByteString(STR_MISSING_CSEQ_METHOD);

  static final String STR_INVALID_CSEQ_HEADER = "Invalid CSeq header";
  static final DsByteString BS_INVALID_CSEQ_HEADER = new DsByteString(STR_INVALID_CSEQ_HEADER);

  static final String STR_INVALID_CONTENT_LENGTH = "Invalid Content-Length header";
  static final DsByteString BS_INVALID_CONTENT_LENGTH =
      new DsByteString(STR_INVALID_CONTENT_LENGTH);

  static final String STR_INVALID_CONTENT_TYPE = "Invalid Content-Type header";
  static final DsByteString BS_INVALID_CONTENT_TYPE = new DsByteString(STR_INVALID_CONTENT_TYPE);

  static final String STR_MALFORMED_REQUEST_URI = "Malformed Request-URI";
  static final DsByteString BS_MALFORMED_REQUEST_URI = new DsByteString(STR_MALFORMED_REQUEST_URI);

  static final String STR_INVALID_MAJOR_VERSION = "Invalid Major SIP Version";
  static final DsByteString BS_INVALID_MAJOR_VERSION = new DsByteString(STR_INVALID_MAJOR_VERSION);

  static final String STR_INVALID_MINOR_VERSION = "Invalid Minor SIP Version";
  static final DsByteString BS_INVALID_MINOR_VERSION = new DsByteString(STR_INVALID_MINOR_VERSION);

  static final String STR_PROTOCOL_NOT_SIP = "Protocol Not SIP";
  static final DsByteString BS_PROTOCOL_NOT_SIP = new DsByteString(STR_PROTOCOL_NOT_SIP);

  static final String STR_NOTIFY_MISSING_SUB_STATE = "NOTIFY missing Subscription-State header";
  static final DsByteString BS_NOTIFY_MISSING_SUB_STATE =
      new DsByteString(STR_NOTIFY_MISSING_SUB_STATE);

  static final String STR_NOTIFY_SUBSCRIBE_EVENT =
      "NOTIFY and SUBSCRIBE requests must have exactly 1 Event header";
  static final DsByteString BS_NOTIFY_SUBSCRIBE_EVENT =
      new DsByteString(STR_NOTIFY_SUBSCRIBE_EVENT);

  static final String STR_INVALID_CSEQ_NUMBER = "Invalid CSeq Number";
  static final DsByteString BS_INVALID_CSEQ_NUMBER = new DsByteString(STR_INVALID_CSEQ_NUMBER);

  static {
    reasonArray = new String[SIZE];

    reasonArray[GENERIC] = STR_GENERIC;
    reasonArray[MANDATORY_HEADER_INVALID] = STR_MANDATORY_HEADER_INVALID;
    reasonArray[BODY_INVALID] = STR_BODY_INVALID;
    reasonArray[MISSING_VIA_HEADER] = STR_MISSING_VIA_HEADER;
    reasonArray[MISSING_TO_HEADER] = STR_MISSING_TO_HEADER;
    reasonArray[MISSING_FROM_HEADER] = STR_MISSING_FROM_HEADER;
    reasonArray[MISSING_CSEQ_HEADER] = STR_MISSING_CSEQ_HEADER;
    reasonArray[MISSING_CALL_ID_HEADER] = STR_MISSING_CALL_ID_HEADER;
    reasonArray[MULTIPLE_TO_HEADERS] = STR_MULTIPLE_TO_HEADERS;
    reasonArray[MULTIPLE_FROM_HEADERS] = STR_MULTIPLE_FROM_HEADERS;
    reasonArray[MULTIPLE_CSEQ_HEADERS] = STR_MULTIPLE_CSEQ_HEADERS;
    reasonArray[MULTIPLE_CALL_ID_HEADERS] = STR_MULTIPLE_CALL_ID_HEADERS;
    reasonArray[CSEQ_MISMATCH] = STR_CSEQ_MISMATCH;
    reasonArray[MISSING_CONTENT_TYPE] = STR_MISSING_CONTENT_TYPE;
    reasonArray[INVITE_MISSING_CONTACT] = STR_INVITE_MISSING_CONTACT;
    reasonArray[REGISTER_HAS_USER] = STR_REGISTER_HAS_USER;
    reasonArray[MISSING_CSEQ_METHOD] = STR_MISSING_CSEQ_METHOD;
    reasonArray[INVALID_CSEQ_HEADER] = STR_INVALID_CSEQ_HEADER;
    reasonArray[INVALID_CONTENT_LENGTH] = STR_INVALID_CONTENT_LENGTH;
    reasonArray[INVALID_CONTENT_TYPE] = STR_INVALID_CONTENT_TYPE;
    reasonArray[MALFORMED_REQUEST_URI] = STR_MALFORMED_REQUEST_URI;
    reasonArray[PROTOCOL_NOT_SIP] = STR_PROTOCOL_NOT_SIP;
    reasonArray[NOTIFY_MISSING_SUB_STATE] = STR_NOTIFY_MISSING_SUB_STATE;
    reasonArray[NOTIFY_SUBSCRIBE_EVENT] = STR_NOTIFY_SUBSCRIBE_EVENT;
    reasonArray[INVALID_CSEQ_NUMBER] = STR_INVALID_CSEQ_NUMBER;
    reasonArray[INVALID_MAJOR_VERSION] = STR_INVALID_MAJOR_VERSION;
    reasonArray[INVALID_MINOR_VERSION] = STR_INVALID_MINOR_VERSION;
  }

  // static methods only - no need to construct this object
  private DsSipInvalidReasons() {}

  /**
   * Translate a reason code from an int to a String intended for the user, as the reason phase.
   *
   * @param reason the reason returned from DsSipMessage.isValidWithReason()
   * @return a String intended for the user as the reason phrase of a 400 response
   */
  public static String getReason(int reason) {
    if (reason < 0 || reason >= SIZE) {
      reason = 0;
    }

    return reasonArray[reason];
  }
}
