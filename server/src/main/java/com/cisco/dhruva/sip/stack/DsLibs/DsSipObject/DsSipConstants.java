// Copyright (c) 2005-2009 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

/** This interface contains some constants used in DsSipObject library. */
public interface DsSipConstants {
  //////////////////////////////////////////////////////////////////////////////////////
  // Method Name IDs
  //////////////////////////////////////////////////////////////////////////////////////

  /** The constant for unknown SIP method name. */
  public static final int UNKNOWN = 0;
  /** The constant for INVITE method name. */
  public static final int INVITE = 1;
  /** The constant for ACK method name. */
  public static final int ACK = 2;
  /** The constant for CANCEL method name. */
  public static final int CANCEL = 3;
  /** The constant for BYE method name. */
  public static final int BYE = 4;
  /** The constant for OPTIONS method name. */
  public static final int OPTIONS = 5;
  /** The constant for REGISTER method name. */
  public static final int REGISTER = 6;
  /** The constant for PRACK method name. */
  public static final int PRACK = 7;
  /** The constant for INFO method name. */
  public static final int INFO = 8;
  /** The constant for SUBSCRIBE method name. */
  public static final int SUBSCRIBE = 9;
  /** The constant for NOTIFY method name. */
  public static final int NOTIFY = 10;
  /** The constant for MESSAGE method name. */
  public static final int MESSAGE = 11;
  /** The constant for REFER method name. */
  public static final int REFER = 12;
  /** The constant for PING method name. */
  public static final int PING = 13;
  /** The constant for UPDATE method name. */
  public static final int UPDATE = 14;

  /** The constant for PUBLISH method name. */
  public static final int PUBLISH = 15;
  /** The constant for the count of defined method name constants. */
  public static final int METHOD_NAMES_SIZE = 16;

  //////////////////////////////////////////////////////////////////////////////////////
  // Method Name Byte Strings
  //////////////////////////////////////////////////////////////////////////////////////

  /** The byte string constant for unknown method name. */
  public static final DsByteString BS_UNKNOWN = new DsByteString("UNKNOWN");
  /** The byte string constant for INVITE method name. */
  public static final DsByteString BS_INVITE = new DsByteString("INVITE");
  /** The byte string constant for ACK method name. */
  public static final DsByteString BS_ACK = new DsByteString("ACK");
  /** The byte string constant for CANCEL method name. */
  public static final DsByteString BS_CANCEL = new DsByteString("CANCEL");
  /** The byte string constant for BYE method name. */
  public static final DsByteString BS_BYE = new DsByteString("BYE");
  /** The byte string constant for OPTIONS method name. */
  public static final DsByteString BS_OPTIONS = new DsByteString("OPTIONS");
  /** The byte string constant for REGISTER method name. */
  public static final DsByteString BS_REGISTER = new DsByteString("REGISTER");
  /** The byte string constant for PRACK method name. */
  public static final DsByteString BS_PRACK = new DsByteString("PRACK");
  /** The byte string constant for INFO method name. */
  public static final DsByteString BS_INFO = new DsByteString("INFO");
  /** The byte string constant for SUBSCRIBE method name. */
  public static final DsByteString BS_SUBSCRIBE = new DsByteString("SUBSCRIBE");
  /** The byte string constant for NOTIFY method name. */
  public static final DsByteString BS_NOTIFY = new DsByteString("NOTIFY");
  /** The byte string constant for MESSAGE method name. */
  public static final DsByteString BS_MESSAGE = new DsByteString("MESSAGE");
  /** The byte string constant for REFER method name. */
  public static final DsByteString BS_REFER = new DsByteString("REFER");
  /** The byte string constant for PING method name. */
  public static final DsByteString BS_PING = new DsByteString("PING");
  /** The byte string constant for UPDATE method name. */
  public static final DsByteString BS_UPDATE = new DsByteString("UPDATE");
  /** The byte string constant for PUBLISH method name. */
  public static final DsByteString BS_PUBLISH = new DsByteString("PUBLISH");

  //////////////////////////////////////////////////////////////////////////////////////
  // Header Name IDs
  //////////////////////////////////////////////////////////////////////////////////////
  // no longer in SIP spec (As of bis 05)
  // ENCRYPTION
  // HIDE
  // RESPONSE_KEY

  // keep these constants in sync with the HEADER_NAMES array

  // I am in the process of ordering these headers
  // and grouping them into logical groups
  // I will remumber them when it is complete - jsm

  /** The header ID constant for Via header. */
  public static final byte VIA = 0;
  /** The header ID constant for Max-Forwards header. */
  public static final byte MAX_FORWARDS = 1;
  /** The header ID constant for Route header. */
  public static final byte ROUTE = 2;
  /** The header ID constant for Record-Route header. */
  public static final byte RECORD_ROUTE = 3;
  /** The header ID constant for To header. */
  public static final byte TO = 4;
  /** The header ID constant for From header. */
  public static final byte FROM = 5;
  /** The header ID constant for CSeq header. */
  public static final byte CSEQ = 6;
  /** The header ID constant for Call-Id header. */
  public static final byte CALL_ID = 7;

  /** The header ID constant for Content-Length header. */
  public static final byte CONTENT_LENGTH = 8;
  /** The header ID constant for Contact header. */
  public static final byte CONTACT = 9;
  /** The header ID constant for Expires header. */
  public static final byte EXPIRES = 10;
  /** The header ID constant for Proxy-Require header. */
  public static final byte PROXY_REQUIRE = 11;
  /** The header ID constant for Require header. */
  public static final byte REQUIRE = 12;

  /** The header ID constant for Service-Agent-Phase header. */
  public static final byte SERVICE_AGENT_PHASE = 13;
  /** The header ID constant for Service-Agent-Context header. */
  public static final byte SERVICE_AGENT_CONTEXT = 14;
  /** The header ID constant for Service-Agent-Application header. */
  public static final byte SERVICE_AGENT_APPLICATION = 15;
  /** The header ID constant for Service-Agent-Route header. */
  public static final byte SERVICE_AGENT_ROUTE = 16;
  /** The header ID constant for Remote-Party-ID header. */
  public static final byte REMOTE_PARTY_ID = 17;
  /** The header ID constant for Event header. */
  public static final byte EVENT = 18;
  /** The header ID constant for Diversion header. */
  public static final byte DIVERSION = 19;
  /** The header ID constant for P-Associated-URI header. */
  public static final byte P_ASSOCIATED_URI = 20;

  /** The header ID constant for P-Called-Party-ID header. */
  public static final byte P_CALLED_PARTY_ID = 21;
  /** The header ID constant for Service-Route header. */
  public static final byte SERVICE_ROUTE = 22;
  /** The header ID constant for P-Access-Network-Info header. */
  public static final byte P_ACCESS_NETWORK_INFO = 23;
  /** The header ID constant for Privacy header. */
  public static final byte PRIVACY = 24;

  /** Used to define the maximum array size of messages for headers. */
  public static final int ROUTE_ENGINE_HEADER_MAX = 24;

  /** The header ID constant for Content-Type header. */
  public static final byte CONTENT_TYPE = 25;

  /** Used to define the maximum array size of messages for headers. */
  public static final int PRESENCE_HEADER_MAX = 25;

  /** The header ID constant for X-Application header. */
  public static final byte X_APPLICATION = 26;
  /** The header ID constant for X-Application-Context header. */
  public static final byte X_APPLICATION_CONTEXT = 27;
  /** The header ID constant for X-From-Outside header. */
  public static final byte X_FROM_OUTSIDE = 28;
  /** The header ID constant for AS-Path header. */
  public static final byte AS_PATH = 29;
  /** The header ID constant for AS-Record-Path header. */
  public static final byte AS_RECORD_PATH = 30;
  /** The header ID constant for AE-Cookie header. */
  public static final byte AE_COOKIE = 31;

  /** Used to define the maximum array size of messages for headers. */
  public static final int APP_ENGINE_HEADER_MAX = 31;

  // mostly unsorted headers - jsm

  /** The header ID constant for Subscription-Expires header. */
  public static final byte SUBSCRIPTION_EXPIRES = 32;
  /** The header ID constant for Allow-Events header. */
  public static final byte ALLOW_EVENTS = 33;

  /** The header ID constant for Proxy-Authentication header. */
  public static final byte PROXY_AUTHENTICATE = 34;
  /** The header ID constant for Proxy-Authorization header. */
  public static final byte PROXY_AUTHORIZATION = 35;
  /** The header ID constant for Authorization header. */
  public static final byte AUTHORIZATION = 36;
  /** The header ID constant for Authentication_Info header. */
  public static final byte AUTHENTICATION_INFO = 37;
  /** The header ID constant for WWW-Authenticate header. */
  public static final byte WWW_AUTHENTICATE = 38;

  /** The header ID constant for Accept header. */
  public static final byte ACCEPT = 39;
  /** The header ID constant for Accept-Encoding header. */
  public static final byte ACCEPT_ENCODING = 40;
  /** The header ID constant for Accept-Language header. */
  public static final byte ACCEPT_LANGUAGE = 41;
  /** The header ID constant for Alert-Info header. */
  public static final byte ALERT_INFO = 42;
  /** The header ID constant for Allow header. */
  public static final byte ALLOW = 43;
  /** The header ID constant for Call-Info header. */
  public static final byte CALL_INFO = 44;
  /** The header ID constant for Content-Disposition header. */
  public static final byte CONTENT_DISPOSITION = 45;
  /** The header ID constant for Content-Encoding header. */
  public static final byte CONTENT_ENCODING = 46;
  /** The header ID constant for Content-Language header. */
  public static final byte CONTENT_LANGUAGE = 47;
  /** The header ID constant for Date header. */
  public static final byte DATE = 48;
  /** The header ID constant for Error-Info header. */
  public static final byte ERROR_INFO = 49;
  /** The header ID constant for In-Reply-To header. */
  public static final byte IN_REPLY_TO = 50;
  /** The header ID constant for Mime-Version header. */
  public static final byte MIME_VERSION = 51;
  /** The header ID constant for Organization header. */
  public static final byte ORGANIZATION = 52;
  /** The header ID constant for Priority header. */
  public static final byte PRIORITY = 53;
  /** The header ID constant for Rack header. */
  public static final byte RACK = 54;
  /** The header ID constant for Retry-After header. */
  public static final byte RETRY_AFTER = 55;
  /** The header ID constant for RSeq header. */
  public static final byte RSEQ = 56;
  /** The header ID constant for Server header. */
  public static final byte SERVER = 57;
  /** The header ID constant for Subject header. */
  public static final byte SUBJECT = 58;
  /** The header ID constant for Supported header. */
  public static final byte SUPPORTED = 59;
  /** The header ID constant for Timestamp header. */
  public static final byte TIMESTAMP = 60;
  /** The header ID constant for Unsupported header. */
  public static final byte UNSUPPORTED = 61;
  /** The header ID constant for User-Agent header. */
  public static final byte USER_AGENT = 62;
  /** The header ID constant for Warning header. */
  public static final byte WARNING = 63;
  /** The header ID constant for Session-Expires header. */
  public static final byte SESSION_EXPIRES = 64;
  /** The header ID constant for Translate header. */
  public static final byte TRANSLATE = 65;
  /** The header ID constant for Content-Version header. */
  public static final byte CONTENT_VERSION = 66;
  /** The header ID constant for Reply-To header. */
  public static final byte REPLY_TO = 67;
  /** The header ID constant for Subscription-State header. */
  public static final byte SUBSCRIPTION_STATE = 68;
  /** The header ID constant for Refer-To header. */
  public static final byte REFER_TO = 69;

  /** The header ID constant for Min-Expires header. */
  public static final byte MIN_EXPIRES = 70;

  /** The header ID constant for PATH header. */
  public static final byte PATH = 71;

  /** The header ID constant for P-Asserted-Identity header. */
  public static final byte P_ASSERTED_IDENTITY = 72;

  /** The header ID constant for P-Preferred-Identity header. */
  public static final byte P_PREFERRED_IDENTITY = 73;

  /** The header ID constant for Replaces header. (RFC 3891) */
  public static final byte REPLACES = 74;

  /** The header ID constant for X-Connection-Info header. */
  public static final byte X_CONNECTION_INFO = 75; // per Edgar,  no longer used by AE

  /** The header ID constant for P-DCS-LAES header. */
  public static final byte P_DCS_LAES = 76;

  /** The header ID constant for P-Charging-Function-Addresses header. */
  public static final byte P_CHARGING_FUNCTION_ADDRESSES = 77;

  /** The header ID constant for P-Charging-Vector header. */
  public static final byte P_CHARGING_VECTOR = 78;

  /** The header ID constant for P-Visited-Network-ID header. */
  public static final byte P_VISITED_NETWORK_ID = 79;

  /** The header ID constant for Referred-By header. */
  public static final byte REFERRED_BY = 80;

  // CAFFEINE 2.0 DEVELOPMENT
  /** The header ID constant for Accept-Contact header. */
  public static final byte ACCEPT_CONTACT = 81;

  /** The header ID constant for Reject-Contact header. */
  public static final byte REJECT_CONTACT = 82;

  /** The header ID constant for Request-Disposition header. */
  public static final byte REQUEST_DISPOSITION = 83;

  /** The header ID constant for Join header. */
  public static final byte JOIN = 84;

  /** The header ID constant for ETag header. */
  public static final byte ETAG = 85;

  /** The header ID constant for If-Match header. */
  public static final byte IF_MATCH = 86;

  /** The header ID constant for App-Info header. */
  public static final byte APP_INFO = 87;

  /** The header ID constant for Content-Id header. */
  public static final byte CONTENT_ID = 88;

  /** The header ID constant for Content-Description header. */
  public static final byte CONTENT_DESCRIPTION = 89;

  /** The header ID constant for Content-Transfer-Encoding header. */
  public static final byte CONTENT_TRANSFER_ENCODING = 90;

  /** The header ID constant for Cisco-Maintenance-Mode header. */
  public static final byte CISCO_MAINTENANCE_MODE = 91;

  /** The header ID constant for Cisco-Guid header. */
  public static final byte CISCO_GUID = 92;

  // MMA - 08.05.2005 - adding support for the Reason header (RFC 3326)
  /** The header ID constant for Reason header. */
  public static final byte REASON_HEADER = 93;

  /** The header ID constant for Suppress-If-Match header. */
  public static final byte SUPPRESS_IF_MATCH = 94;

  /** The header ID constant for Target-Dialog header. */
  public static final byte TARGET_DIALOG = 95;

  /** The header ID constant for History-Info header. */
  public static final byte HISTORY_INFO = 96;

  /** The header ID constant for X-cisco-rai header. */
  public static final byte X_CISCO_RAI = 97;

  /** The constant for the maximum number of known headers. */
  // IMPORTANT: kevmo 02.14.2006
  // IMPORTANT: MAX_KNOWN_HEADER = LAST_KNOW_HEADER (REASON_HEADER at this time);
  public static final int MAX_KNOWN_HEADER = 97;

  /** The general header ID constant for unknown header. */
  public static final byte UNKNOWN_HEADER = 98;

  // When adding a new header, make sure that you increment the numbers below as well - jsm

  // non-header types (context IDs)
  // still needs to be ordered, since it is in the array as
  // well as header names for debug purposes - mat revisit this - jsm
  /** The constant ID used for parser events for SIP URL. */
  public static final int SIP_URL_ID = 99;
  /** The constant ID used in parser events as context ID for TEL URL. */
  public static final int TEL_URL_ID = 100;
  // CAFFEINE 2.0 DEVELOPMENT
  /** The constant ID used for parser events for CID URL. */
  public static final int CID_URL_ID = 101;
  /**
   * The constant ID used in parser events as context ID for any URL other than SIP, TEL or CID
   * URLs.
   */
  public static final int UNKNOWN_URL_ID = 102;
  /** The constant ID used in parser events as context ID for Name Address. */
  public static final int NAME_ADDR_ID = 103;
  /** The constant ID used in parser events as context ID for entire Message. */
  public static final int ENTIRE_MSG_ID = 104;

  /** The constant for the total number of header/context IDs. */
  public static final int HEADER_NAMES_SIZE = 105;

  //////////////////////////////////////////////////////////////////////////////////////
  // Header Name Byte Strings
  //////////////////////////////////////////////////////////////////////////////////////

  /** The byte string constant for AE-Cookie header name. */
  public static final DsByteString BS_AE_COOKIE = new DsByteString("AE-Cookie");
  /** The byte string constant for Accept header name. */
  public static final DsByteString BS_ACCEPT = new DsByteString("Accept");
  // CAFFEINE 2.0 DEVELOPMENT
  /** The byte string constant for Accept-Contact header name. */
  public static final DsByteString BS_ACCEPT_CONTACT = new DsByteString("Accept-Contact");
  /** The byte string constant for Accept-Encoding header name. */
  public static final DsByteString BS_ACCEPT_ENCODING = new DsByteString("Accept-Encoding");
  /** The byte string constant for Accept-Language header name. */
  public static final DsByteString BS_ACCEPT_LANGUAGE = new DsByteString("Accept-Language");
  /** The byte string constant for Alert-Info header name. */
  public static final DsByteString BS_ALERT_INFO = new DsByteString("Alert-Info");
  /** The byte string constant for Allow header name. */
  public static final DsByteString BS_ALLOW = new DsByteString("Allow");
  /** The byte string constant for Allow-Events header name. */
  public static final DsByteString BS_ALLOW_EVENTS = new DsByteString("Allow-Events");
  /** The byte string constant for AS-Path header name. */
  public static final DsByteString BS_AS_PATH = new DsByteString("AS-Path");
  /** The byte string constant for As-Record-Path header name. */
  public static final DsByteString BS_AS_RECORD_PATH = new DsByteString("AS-Record-Path");
  /** The byte string constant for Authentication-Info header name. */
  public static final DsByteString BS_AUTHENTICATION_INFO = new DsByteString("Authentication-Info");
  /** The byte string constant for Authorization header name. */
  public static final DsByteString BS_AUTHORIZATION = new DsByteString("Authorization");

  /** The byte string constant for Call-ID header name. */
  public static final DsByteString BS_CALL_ID = new DsByteString("Call-ID");
  /** The byte string constant for Call-Info header name. */
  public static final DsByteString BS_CALL_INFO = new DsByteString("Call-Info");
  /** The byte string constant for Contact header name. */
  public static final DsByteString BS_CONTACT = new DsByteString("Contact");
  /** The byte string constant for Content-Disposition header name. */
  public static final DsByteString BS_CONTENT_DISPOSITION = new DsByteString("Content-Disposition");
  /** The byte string constant for Content-Encoding header name. */
  public static final DsByteString BS_CONTENT_ENCODING = new DsByteString("Content-Encoding");
  /** The byte string constant for Content-Language header name. */
  public static final DsByteString BS_CONTENT_LANGUAGE = new DsByteString("Content-Language");
  /** The byte string constant for Content-Length header name. */
  public static final DsByteString BS_CONTENT_LENGTH = new DsByteString("Content-Length");
  /** The byte string constant for Content-Type header name. */
  public static final DsByteString BS_CONTENT_TYPE = new DsByteString("Content-Type");
  /** The byte string constant for Content-Version header name. */
  public static final DsByteString BS_CONTENT_VERSION = new DsByteString("Content-Version");
  /** The byte string constant for CSeq header name. */
  public static final DsByteString BS_CSEQ = new DsByteString("CSeq");

  /** The byte string constant for Date header name. */
  public static final DsByteString BS_DATE = new DsByteString("Date");
  /** The byte string constant for Diversion header name. */
  public static final DsByteString BS_DIVERSION = new DsByteString("Diversion");

  /** The byte string constant for Error-Info header name. */
  public static final DsByteString BS_ERROR_INFO = new DsByteString("Error-Info");
  /** The byte string constant for Event header name. */
  public static final DsByteString BS_EVENT = new DsByteString("Event");
  /** The byte string constant for Expires header name. */
  public static final DsByteString BS_EXPIRES = new DsByteString("Expires");

  /** The byte string constant for From header name. */
  public static final DsByteString BS_FROM = new DsByteString("From");

  /** The byte string constant for In-Reply-To header name. */
  public static final DsByteString BS_IN_REPLY_TO = new DsByteString("In-Reply-To");

  /** The byte string constant for Max-Forwards header name. */
  public static final DsByteString BS_MAX_FORWARDS = new DsByteString("Max-Forwards");
  /** The byte string constant for Min-Expires header name. */
  public static final DsByteString BS_MIN_EXPIRES = new DsByteString("Min-Expires");
  /** The byte string constant for Mime-Version header name. */
  public static final DsByteString BS_MIME_VERSION = new DsByteString("MIME-Version");

  /** The byte string constant for Organization header name. */
  public static final DsByteString BS_ORGANIZATION = new DsByteString("Organization");

  /** The byte string constant for Path header name. */
  public static final DsByteString BS_PATH = new DsByteString("Path");
  /** The byte string constant for P-Access-Network-Info header name. */
  public static final DsByteString BS_P_ACCESS_NETWORK_INFO =
      new DsByteString("P-Access-Network-Info");
  /** The byte string constant for P-Asserted-Identity header name. */
  public static final DsByteString BS_P_ASSERTED_IDENTITY = new DsByteString("P-Asserted-Identity");
  /** The byte string constant for P-Associated-URI header name. */
  public static final DsByteString BS_P_ASSOCIATED_URI = new DsByteString("P-Associated-URI");
  /** The byte string constant for P-Called-Party-ID header name. */
  public static final DsByteString BS_P_CALLED_PARTY_ID = new DsByteString("P-Called-Party-ID");
  /** The byte string constant for P-DCS-LAES header name. */
  public static final DsByteString BS_P_DCS_LAES = new DsByteString("P-DCS-LAES");
  /** The byte string constant for P-Charging-Function-Addresses header name. */
  public static final DsByteString BS_P_CHARGING_FUNCTION_ADDRESSES =
      new DsByteString("P-Charging-Function-Addresses");
  /** The byte string constant for P-Charging-Vector header name. */
  public static final DsByteString BS_P_CHARGING_VECTOR = new DsByteString("P-Charging-Vector");
  /** The byte string constant for P-Visited-Network-ID header name. */
  public static final DsByteString BS_P_VISITED_NETWORK_ID =
      new DsByteString("P-Visited-Network-ID");
  /** The byte string constant for P-Preferred-Identity header name. */
  public static final DsByteString BS_P_PREFERRED_IDENTITY =
      new DsByteString("P-Preferred-Identity");
  /** The byte string constant for Priority header name. */
  public static final DsByteString BS_PRIORITY = new DsByteString("Priority");
  /** The byte string constant for "Privacy" header name. */
  public static final DsByteString BS_PRIVACY = new DsByteString("Privacy");
  /** The byte string constant for Proxy-Authenticate header name. */
  public static final DsByteString BS_PROXY_AUTHENTICATE = new DsByteString("Proxy-Authenticate");
  /** The byte string constant for Proxy-Authorization header name. */
  public static final DsByteString BS_PROXY_AUTHORIZATION = new DsByteString("Proxy-Authorization");
  /** The byte string constant for Proxy-Require header name. */
  public static final DsByteString BS_PROXY_REQUIRE = new DsByteString("Proxy-Require");

  /** The byte string constant for Rack header name. */
  public static final DsByteString BS_RACK = new DsByteString("RAck");
  /** The byte string constant for Record-Route header name. */
  public static final DsByteString BS_RECORD_ROUTE = new DsByteString("Record-Route");
  /** The byte string constant for Refer-To header name. */
  public static final DsByteString BS_REFER_TO = new DsByteString("Refer-To");
  /** The byte string constant for Referred-By header name. */
  public static final DsByteString BS_REFERRED_BY = new DsByteString("Referred-By");
  /** The byte string constant for Remote-Party-ID header name. */
  public static final DsByteString BS_REMOTE_PARTY_ID = new DsByteString("Remote-Party-ID");
  // CAFFEINE 2.0 DEVELOPMENT
  /** The byte string constant for Reject-Contact name. */
  public static final DsByteString BS_REJECT_CONTACT = new DsByteString("Reject-Contact");
  /** The byte string constant for Replaces header name. */
  public static final DsByteString BS_REPLACES = new DsByteString("Replaces");
  /** The byte string constant for Reply-To header name. */
  public static final DsByteString BS_REPLY_TO = new DsByteString("Reply-To");
  // CAFFEINE 2.0 DEVELOPMENT
  /** The byte string constant for Request-Dispositionheader name. */
  public static final DsByteString BS_REQUEST_DISPOSITION = new DsByteString("Request-Disposition");
  /** The byte string constant for Require header name. */
  public static final DsByteString BS_REQUIRE = new DsByteString("Require");
  /** The byte string constant for Retry-After header name. */
  public static final DsByteString BS_RETRY_AFTER = new DsByteString("Retry-After");
  /** The byte string constant for Route header name. */
  public static final DsByteString BS_ROUTE = new DsByteString("Route");
  /** The byte string constant for RSeq header name. */
  public static final DsByteString BS_RSEQ = new DsByteString("RSeq");

  /** The byte string constant for Service-Agent-Phase header name. */
  public static final DsByteString BS_SERVICE_AGENT_PHASE = new DsByteString("SE-Phase");
  /** The byte string constant for Service-Agent-Context header name. */
  public static final DsByteString BS_SERVICE_AGENT_CONTEXT = new DsByteString("SE-Context");
  /** The byte string constant for Service-Agent-Application header name. */
  public static final DsByteString BS_SERVICE_AGENT_APPLICATION =
      new DsByteString("SE-Application");
  /** The byte string constant for Service-Agent-Application header name. */
  public static final DsByteString BS_SERVICE_AGENT_ROUTE = new DsByteString("SE-Route");
  /** The byte string constant for Server header name. */
  public static final DsByteString BS_SERVER = new DsByteString("Server");
  /** The byte string constant for Service-Route header name. */
  public static final DsByteString BS_SERVICE_ROUTE = new DsByteString("Service-Route");
  /** The byte string constant for Session-Expires header name. */
  public static final DsByteString BS_SESSION_EXPIRES = new DsByteString("Session-Expires");
  /** The byte string constant for Session-ID header name. */
  public static final DsByteString BS_SESSION_ID = new DsByteString("Session-ID");
  /** The byte string constant for Subscription-Expires header name. */
  public static final DsByteString BS_SUBSCRIPTION_EXPIRES =
      new DsByteString("Subscription-Expires");
  /** The byte string constant for Subscription-State header name. */
  public static final DsByteString BS_SUBSCRIPTION_STATE = new DsByteString("Subscription-State");
  /** The byte string constant for Subject header name. */
  public static final DsByteString BS_SUBJECT = new DsByteString("Subject");
  /** The byte string constant for Supported header name. */
  public static final DsByteString BS_SUPPORTED = new DsByteString("Supported");

  /** The byte string constant for Timestamp header name. */
  public static final DsByteString BS_TIMESTAMP = new DsByteString("Timestamp");
  /** The byte string constant for Translate header name. */
  public static final DsByteString BS_TRANSLATE = new DsByteString("Translate");
  /** The byte string constant for To header name. */
  public static final DsByteString BS_TO = new DsByteString("To");

  /** The byte string constant for Unsupported header name. */
  public static final DsByteString BS_UNSUPPORTED = new DsByteString("Unsupported");
  /** The byte string constant for User-Agent header name. */
  public static final DsByteString BS_USER_AGENT = new DsByteString("User-Agent");

  /** The byte string constant for Via header name. */
  public static final DsByteString BS_VIA = new DsByteString("Via");

  /** The byte string constant for Warning header name. */
  public static final DsByteString BS_WARNING = new DsByteString("Warning");
  /** The byte string constant for WWW-Authenticate header name. */
  public static final DsByteString BS_WWW_AUTHENTICATE = new DsByteString("WWW-Authenticate");
  // CAFFEINE 2.0 DEVELOPMENT
  /** The byte string constant for JOIN header name. */
  public static final DsByteString BS_JOIN = new DsByteString("Join");
  /** The byte string constant for REPLACES header name. */
  public static final DsByteString BS_ETAG = new DsByteString("SIP-ETag");
  /** The byte string constant for If-Match header name. */
  public static final DsByteString BS_IF_MATCH = new DsByteString("Sip-If-Match");
  /** The byte string constant for Suppress-If-Match header name. */
  public static final DsByteString BS_SUPPRESS_IF_MATCH = new DsByteString("Suppress-If-Match");
  /** The byte string constant for Target-Dialog header name. */
  public static final DsByteString BS_TARGET_DIALOG = new DsByteString("Target-Dialog");
  /** The byte string constant for History-Info header name. */
  public static final DsByteString BS_HISTORY_INFO = new DsByteString("History-Info");
  /** The byte string constant for App-Info header name. */
  public static final DsByteString BS_APP_INFO = new DsByteString("App-Info");
  /** The byte string constant for Content-Id header name. */
  public static final DsByteString BS_CONTENT_ID = new DsByteString("Content-Id");
  /** The byte string constant for Content-Description header name. */
  public static final DsByteString BS_CONTENT_DESCRIPTION = new DsByteString("Content-Description");
  /** The byte string constant for Content-Transfer-Encoding header name. */
  public static final DsByteString BS_CONTENT_TRANSFER_ENCODING =
      new DsByteString("Content-Transfer-Encoding");

  // CAFFEINE 2.0 DEVELOPMENT - Cisco-Maintenance-Mode header support
  /** The byte string constant for Cisco-Maintenance-Mode header name. */
  public static final DsByteString BS_CISCO_MAINTENANCE_MODE =
      new DsByteString("Cisco-Maintenance-Mode");

  // CAFFEINE 2.0 DEVELOPMENT - CSCeg69327 GUID support in stack.
  /** The byte string constant for Cisco-Guid header name. */
  public static final DsByteString BS_CISCO_GUID = new DsByteString("Cisco-Guid");

  // MMA - 08.05.2005 - adding support for the Reason header (RFC 3326)
  /** The byte string constant for Reason header name. */
  public static final DsByteString BS_REASON_HEADER = new DsByteString("Reason");

  /** The byte string constant for P-RTP-Stat header name. */
  public static final DsByteString BS_P_RTP_STAT = new DsByteString("P-RTP-Stat");

  /** The byte string constant for X-Application header name. */
  public static final DsByteString BS_X_APPLICATION = new DsByteString("X-Application");
  /** The byte string constant for X-Application-Context header name. */
  public static final DsByteString BS_X_APPLICATION_CONTEXT =
      new DsByteString("X-Application-Context");
  /** The byte string constant for X-cisco-rai header name. */
  public static final DsByteString BS_X_CISCO_RAI = new DsByteString("X-cisco-rai");
  /** The byte string constant for X-From-Outside header name. */
  public static final DsByteString BS_X_FROM_OUTSIDE = new DsByteString("X-From-Outside");

  // per Edgar, no longer used by AE
  /** The byte string constant for X-Connection-Info header name. */
  public static final DsByteString BS_X_CONNECTION_INFO = new DsByteString("X-Connection-Info");

  /** The byte string constant for X-Cisco-Peer-Cert-Info header name. */
  public static final DsByteString X_CISCO_PEER_CERT_INFO_STRING =
      new DsByteString("X-Cisco-Peer-Cert-Info");

  /** The byte string constant for unknown header name. */
  public static final DsByteString BS_UNKNOWN_HEADER = new DsByteString("UNKNOWN_HEADER");

  //////////////////////////////////////////////////////////////////////////////////////
  // Header Name plus ": " Byte Strings
  //////////////////////////////////////////////////////////////////////////////////////

  /** The byte string constant for "AE-Cookie: " token. */
  public static final DsByteString BS_AE_COOKIE_TOKEN = new DsByteString("AE-Cookie: ");
  /** The byte string constant for "AS-Path: " token. */
  public static final DsByteString BS_AS_PATH_TOKEN = new DsByteString("AS-Path: ");
  /** The byte string constant for "AS-Record-Path: " token. */
  public static final DsByteString BS_AS_RECORD_PATH_TOKEN = new DsByteString("AS-Record-Path: ");
  /** The byte string constant for "Accept: " token. */
  public static final DsByteString BS_ACCEPT_TOKEN = new DsByteString("Accept: ");
  // CAFFEINE 2.0 DEVELOPMENT
  /** The byte string constant for "Accept-Contact: " token. */
  public static final DsByteString BS_ACCEPT_CONTACT_TOKEN = new DsByteString("Accept-Contact: ");
  /** The byte string constant for "Accept-Encoding: " token. */
  public static final DsByteString BS_ACCEPT_ENCODING_TOKEN = new DsByteString("Accept-Encoding: ");
  /** The byte string constant for "Accept-Language: " token. */
  public static final DsByteString BS_ACCEPT_LANGUAGE_TOKEN = new DsByteString("Accept-Language: ");
  /** The byte string constant for "Alert-Info: " token. */
  public static final DsByteString BS_ALERT_INFO_TOKEN = new DsByteString("Alert-Info: ");
  /** The byte string constant for "Allow: " token. */
  public static final DsByteString BS_ALLOW_TOKEN = new DsByteString("Allow: ");
  /** The byte string constant for "Allow-Events: " token. */
  public static final DsByteString BS_ALLOW_EVENTS_TOKEN = new DsByteString("Allow-Events: ");
  /** The byte string constant for "Authentication-Info: " token. */
  public static final DsByteString BS_AUTHENTICATION_INFO_TOKEN =
      new DsByteString("Authentication-Info: ");
  /** The byte string constant for "Authorization: " token. */
  public static final DsByteString BS_AUTHORIZATION_TOKEN = new DsByteString("Authorization: ");

  /** The byte string constant for "Call-ID: " token. */
  public static final DsByteString BS_CALL_ID_TOKEN = new DsByteString("Call-ID: ");
  /** The byte string constant for "Call-Info: " token. */
  public static final DsByteString BS_CALL_INFO_TOKEN = new DsByteString("Call-Info: ");
  /** The byte string constant for "Contact: " token. */
  public static final DsByteString BS_CONTACT_TOKEN = new DsByteString("Contact: ");
  /** The byte string constant for "Content-Disposition: " token. */
  public static final DsByteString BS_CONTENT_DISPOSITION_TOKEN =
      new DsByteString("Content-Disposition: ");
  /** The byte string constant for "Content-Encoding: " token. */
  public static final DsByteString BS_CONTENT_ENCODING_TOKEN =
      new DsByteString("Content-Encoding: ");
  /** The byte string constant for "Content-Language: " token. */
  public static final DsByteString BS_CONTENT_LANGUAGE_TOKEN =
      new DsByteString("Content-Language: ");
  /** The byte string constant for "Content-Length: " token. */
  public static final DsByteString BS_CONTENT_LENGTH_TOKEN = new DsByteString("Content-Length: ");
  /** The byte string constant for "Content-Type: " token. */
  public static final DsByteString BS_CONTENT_TYPE_TOKEN = new DsByteString("Content-Type: ");
  /** The byte string constant for "Content-Version: " token. */
  public static final DsByteString BS_CONTENT_VERSION_TOKEN = new DsByteString("Content-Version: ");
  /** The byte string constant for "CSeq: " token. */
  public static final DsByteString BS_CSEQ_TOKEN = new DsByteString("CSeq: ");

  /** The byte string constant for "Date: " token. */
  public static final DsByteString BS_DATE_TOKEN = new DsByteString("Date: ");
  /** The byte string constant for Diversion token. */
  public static final DsByteString BS_DIVERSION_TOKEN = new DsByteString("Diversion: ");

  /** The byte string constant for "Error-Info: " token. */
  public static final DsByteString BS_ERROR_INFO_TOKEN = new DsByteString("Error-Info: ");
  /** The byte string constant for "Event: " token. */
  public static final DsByteString BS_EVENT_TOKEN = new DsByteString("Event: ");
  /** The byte string constant for "Expires: " token. */
  public static final DsByteString BS_EXPIRES_TOKEN = new DsByteString("Expires: ");

  /** The byte string constant for "From: " token. */
  public static final DsByteString BS_FROM_TOKEN = new DsByteString("From: ");

  /** The byte string constant for "In-Reply-To: " token. */
  public static final DsByteString BS_IN_REPLY_TO_TOKEN = new DsByteString("In-Reply-To: ");

  /** The byte string constant for "Max-Forwards: " token. */
  public static final DsByteString BS_MAX_FORWARDS_TOKEN = new DsByteString("Max-Forwards: ");
  /** The byte string constant for "Min-Expires: " token. */
  public static final DsByteString BS_MIN_EXPIRES_TOKEN = new DsByteString("Min-Expires: ");
  /** The byte string constant for "MIME-Version: " token. */
  public static final DsByteString BS_MIME_VERSION_TOKEN = new DsByteString("MIME-Version: ");

  /** The byte string constant for "Organization: " token. */
  public static final DsByteString BS_ORGANIZATION_TOKEN = new DsByteString("Organization: ");

  /** The byte string constant for "Path: " token. */
  public static final DsByteString BS_PATH_TOKEN = new DsByteString("Path: ");
  /** The byte string constant for P-Access-Network-Info token. */
  public static final DsByteString BS_P_ACCESS_NETWORK_INFO_TOKEN =
      new DsByteString("P-Access-Network-Info: ");
  /** The byte string constant for "P-Asserted-Identity: " token. */
  public static final DsByteString BS_P_ASSERTED_IDENTITY_TOKEN =
      new DsByteString("P-Asserted-Identity: ");
  /** The byte string constant for P-Associated-URI token. */
  public static final DsByteString BS_P_ASSOCIATED_URI_TOKEN =
      new DsByteString("P-Associated-URI: ");
  /** The byte string constant for "P-Called-Party-ID: " token. */
  public static final DsByteString BS_P_CALLED_PARTY_ID_TOKEN =
      new DsByteString("P-Called-Party-ID: ");
  /** The byte string constant for "P-DCS-LAES: " token. */
  public static final DsByteString BS_P_DCS_LAES_TOKEN = new DsByteString("P-DCS-LAES: ");
  /** The byte string constant for "P-Charging-Function-Addresses: " token. */
  public static final DsByteString BS_P_CHARGING_FUNCTION_ADDRESSES_TOKEN =
      new DsByteString("P-Charging-Function-Addresses: ");
  /** The byte string constant for "P-Charging-Vector: " token. */
  public static final DsByteString BS_P_CHARGING_VECTOR_TOKEN =
      new DsByteString("P-Charging-Vector: ");
  /** The byte string constant for "P-Visited-Network-ID: " token. */
  public static final DsByteString BS_P_VISITED_NETWORK_ID_TOKEN =
      new DsByteString("P-Visited-Network-ID: ");
  /** The byte string constant for "P-Preferred-Identity: " token. */
  public static final DsByteString BS_P_PREFERRED_IDENTITY_TOKEN =
      new DsByteString("P-Preferred-Identity: ");
  /** The byte string constant for "Priority: " token. */
  public static final DsByteString BS_PRIORITY_TOKEN = new DsByteString("Priority: ");
  /** The byte string constant for "Privacy: " token. */
  public static final DsByteString BS_PRIVACY_TOKEN = new DsByteString("Privacy: ");
  /** The byte string constant for "Proxy-Authenticate: " token. */
  public static final DsByteString BS_PROXY_AUTHENTICATE_TOKEN =
      new DsByteString("Proxy-Authenticate: ");
  /** The byte string constant for "Proxy-Authorization: " token. */
  public static final DsByteString BS_PROXY_AUTHORIZATION_TOKEN =
      new DsByteString("Proxy-Authorization: ");
  /** The byte string constant for "Proxy-Require: " token. */
  public static final DsByteString BS_PROXY_REQUIRE_TOKEN = new DsByteString("Proxy-Require: ");

  /** The byte string constant for "RAck: " token. */
  public static final DsByteString BS_RACK_TOKEN = new DsByteString("RAck: ");
  /** The byte string constant for "Record-Route: " token. */
  public static final DsByteString BS_RECORD_ROUTE_TOKEN = new DsByteString("Record-Route: ");
  // CAFFEINE 2.0 DEVELOPMENT
  /** The byte string constant for "Reject-Contact: " token. */
  public static final DsByteString BS_REJECT_CONTACT_TOKEN = new DsByteString("Reject-Contact: ");
  /** The byte string constant for "Request-Disposition: " token. */
  public static final DsByteString BS_REQUEST_DISPOSITION_TOKEN =
      new DsByteString("Request-Disposition: ");
  /** The byte string constant for "Refer-To: " token. */
  public static final DsByteString BS_REFER_TO_TOKEN = new DsByteString("Refer-To: ");
  /** The byte string constant for "Referred-By: " token. */
  public static final DsByteString BS_REFERRED_BY_TOKEN = new DsByteString("Referred-By: ");
  /** The byte string constant for "Remote-Party-ID: " token. */
  public static final DsByteString BS_REMOTE_PARTY_ID_TOKEN = new DsByteString("Remote-Party-ID: ");
  /** The byte string constant for "Replaces: " token. */
  public static final DsByteString BS_REPLACES_TOKEN = new DsByteString("Replaces: ");
  /** The byte string constant for "Reply-To: " token. */
  public static final DsByteString BS_REPLY_TO_TOKEN = new DsByteString("Reply-To: ");
  /** The byte string constant for "Require: " token. */
  public static final DsByteString BS_REQUIRE_TOKEN = new DsByteString("Require: ");
  /** The byte string constant for "Retry-After: " token. */
  public static final DsByteString BS_RETRY_AFTER_TOKEN = new DsByteString("Retry-After: ");
  /** The byte string constant for "Route: " token. */
  public static final DsByteString BS_ROUTE_TOKEN = new DsByteString("Route: ");
  /** The byte string constant for "RSeq: " token. */
  public static final DsByteString BS_RSEQ_TOKEN = new DsByteString("RSeq: ");

  /** The byte string constant for "SE-Application: " token. */
  public static final DsByteString BS_SERVICE_AGENT_APPLICATION_TOKEN =
      new DsByteString("SE-Application: ");
  /** The byte string constant for "SE-Context: " token. */
  public static final DsByteString BS_SERVICE_AGENT_CONTEXT_TOKEN =
      new DsByteString("SE-Context: ");
  /** The byte string constant for "SE-Phase: " token. */
  public static final DsByteString BS_SERVICE_AGENT_PHASE_TOKEN = new DsByteString("SE-Phase: ");
  /** The byte string constant for "SE-Route: " token. */
  public static final DsByteString BS_SERVICE_AGENT_ROUTE_TOKEN = new DsByteString("SE-Route: ");
  /** The byte string constant for "Server: " token. */
  public static final DsByteString BS_SERVER_TOKEN = new DsByteString("Server: ");
  /** The byte string constant for "Service-Route: " token. */
  public static final DsByteString BS_SERVICE_ROUTE_TOKEN = new DsByteString("Service-Route: ");
  /** The byte string constant for "Session-Expires: " token. */
  public static final DsByteString BS_SESSION_EXPIRES_TOKEN = new DsByteString("Session-Expires: ");
  /** The byte string constant for "Subject: " token. */
  public static final DsByteString BS_SUBJECT_TOKEN = new DsByteString("Subject: ");
  /** The byte string constant for "Subscription-Expires: " token. */
  public static final DsByteString BS_SUBSCRIPTION_EXPIRES_TOKEN =
      new DsByteString("Subscription-Expires: ");
  /** The byte string constant for "Subscription-State: " token. */
  public static final DsByteString BS_SUBSCRIPTION_STATE_TOKEN =
      new DsByteString("Subscription-State: ");
  /** The byte string constant for "Supported: " token. */
  public static final DsByteString BS_SUPPORTED_TOKEN = new DsByteString("Supported: ");

  /** The byte string constant for "Timestamp: " token. */
  public static final DsByteString BS_TIMESTAMP_TOKEN = new DsByteString("Timestamp: ");
  /** The byte string constant for "Translate: " token. */
  public static final DsByteString BS_TRANSLATE_TOKEN = new DsByteString("Translate: ");
  /** The byte string constant for "To: " token. */
  public static final DsByteString BS_TO_TOKEN = new DsByteString("To: ");

  /** The byte string constant for "Unsupported: " token. */
  public static final DsByteString BS_UNSUPPORTED_TOKEN = new DsByteString("Unsupported: ");
  /** The byte string constant for "User-Agent: " token. */
  public static final DsByteString BS_USER_AGENT_TOKEN = new DsByteString("User-Agent: ");

  /** The byte string constant for "Via: " token. */
  public static final DsByteString BS_VIA_TOKEN = new DsByteString("Via: ");

  /** The byte string constant for "Warning: " token. */
  public static final DsByteString BS_WARNING_TOKEN = new DsByteString("Warning: ");
  /** The byte string constant for "WWW-Authenticate: " token. */
  public static final DsByteString BS_WWW_AUTHENTICATE_TOKEN =
      new DsByteString("WWW-Authenticate: ");

  /** The byte string constant for "X-Application: " token. */
  public static final DsByteString BS_X_APPLICATION_TOKEN = new DsByteString("X-Application: ");
  /** The byte string constant for "X-Application-Context: " token. */
  public static final DsByteString BS_X_APPLICATION_CONTEXT_TOKEN =
      new DsByteString("X-Application-Context: ");
  /** The byte string constant for "X-cisco-rai: " token. */
  public static final DsByteString BS_X_CISCO_RAI_TOKEN = new DsByteString("X-cisco-rai: ");
  /** The byte string constant for "X-From-Outside: " token. */
  public static final DsByteString BS_X_FROM_OUTSIDE_TOKEN = new DsByteString("X-From-Outside: ");

  // per Edgar, no longer used by AE
  /** The byte string constant for "X-Connection-Info: " token. */
  public static final DsByteString BS_X_CONNECTION_INFO_TOKEN =
      new DsByteString("X-Connection-Info: ");

  // CAFFEINE 2.0 DEVELOPMENT - EDCS-304264 for join and replace header
  /** The byte string constant for "Join: " token. */
  public static final DsByteString BS_JOIN_TOKEN = new DsByteString("Join: ");

  /** The byte string constant for "ETag: " token. */
  public static final DsByteString BS_ETAG_TOKEN = new DsByteString("SIP-ETag: ");
  /** The byte string constant for "Suppress-If-Match: " token. */
  public static final DsByteString BS_SUPPRESS_IF_MATCH_TOKEN =
      new DsByteString("Suppress-If-Match: ");
  /** The byte string constant for "Target-Dialog: " token. */
  public static final DsByteString BS_TARGET_DIALOG_TOKEN = new DsByteString("Target-Dialog: ");
  /** The byte string constant for "History-Info: " token. */
  public static final DsByteString BS_HISTORY_INFO_TOKEN = new DsByteString("History-Info: ");
  /** The byte string constant for "If-Match: " token. */
  public static final DsByteString BS_IF_MATCH_TOKEN = new DsByteString("SIP-If-Match: ");

  /** The byte string constant for "App-Info: " header name. */
  public static final DsByteString BS_APP_INFO_TOKEN = new DsByteString("App-Info: ");

  /** The byte string constant for Content-Id: header name. */
  public static final DsByteString BS_CONTENT_ID_TOKEN = new DsByteString("Content-Id: ");
  /** The byte string constant for Content-Description: header name. */
  public static final DsByteString BS_CONTENT_DESCRIPTION_TOKEN =
      new DsByteString("Content-Description: ");
  /** The byte string constant for "Content-Transfer-Encoding: " token. */
  public static final DsByteString BS_CONTENT_TRANSFER_ENCODING_TOKEN =
      new DsByteString("Content-Transfer-Encoding: ");

  // CAFFEINE 2.0 DEVELOPMENT - Cisco-Maintenance-Mode header support
  /** The byte string constant for "Cisco-Maintenance-Mode: " header name. */
  public static final DsByteString BS_CISCO_MAINTENANCE_MODE_TOKEN =
      new DsByteString("Cisco-Maintenance-Mode: ");

  // CAFFEINE 2.0 DEVELOPMENT - CSCeg69327 GUID support in stack.
  /** The byte string constant for Cisco-Guid header name. */
  public static final DsByteString BS_CISCO_GUID_TOKEN = new DsByteString("Cisco-Guid: ");

  // MMA - 08.05.2005 - adding support for the Reason header (RFC 3326)
  /** The byte string constant for "Reason: " token. */
  public static final DsByteString BS_REASON_HEADER_TOKEN = new DsByteString("Reason: ");

  //////////////////////////////////////////////////////////////////////////////////////
  // Header Name Byte Strings (Compact form)
  //////////////////////////////////////////////////////////////////////////////////////

  /** The byte string constant for Allow-Events header compact name. */
  public static final DsByteString BS_ALLOW_EVENTS_C = new DsByteString("u");

  /** The byte string constant for Call-ID header compact name. */
  public static final DsByteString BS_CALL_ID_C = new DsByteString("i");
  /** The byte string constant for Contact header compact name. */
  public static final DsByteString BS_CONTACT_C = new DsByteString("m");
  /** The byte string constant for Content-Encoding header compact name. */
  public static final DsByteString BS_CONTENT_ENCODING_C = new DsByteString("e");
  /** The byte string constant for Content-Length header compact name. */
  public static final DsByteString BS_CONTENT_LENGTH_C = new DsByteString("l");
  /** The byte string constant for Content-Type header compact name. */
  public static final DsByteString BS_CONTENT_TYPE_C = new DsByteString("c");

  /** The byte string constant for Event header compact name. */
  public static final DsByteString BS_EVENT_C = new DsByteString("o");

  /** The byte string constant for From header compact name. */
  public static final DsByteString BS_FROM_C = new DsByteString("f");

  /** The byte string constant for Refer-To header compact name. */
  public static final DsByteString BS_REFER_TO_C = new DsByteString("r");

  /** The byte string constant for Referred-By header compact name. */
  public static final DsByteString BS_REFERRED_BY_C = new DsByteString("b");

  /** The byte string constant for Session-Expires header compact name. */
  public static final DsByteString BS_SESSION_EXPIRES_C = new DsByteString("x");
  /** The byte string constant for Subject header compact name. */
  public static final DsByteString BS_SUBJECT_C = new DsByteString("s");
  /** The byte string constant for Supported header compact name. */
  public static final DsByteString BS_SUPPORTED_C = new DsByteString("k");

  /** The byte string constant for To header compact name. */
  public static final DsByteString BS_TO_C = new DsByteString("t");

  /** The byte string constant for Via header compact name. */
  public static final DsByteString BS_VIA_C = new DsByteString("v");

  /** The byte string constant for norefersub tag in Require header . */
  public static final DsByteString BS_NOREFERSUB = new DsByteString("norefersub");

  // CAFFEINE 2.0 DEVELOPMENT
  /** The byte string constant for Accept-Contact header compact name. */
  public static final DsByteString BS_ACCEPT_CONTACT_C = new DsByteString("a");
  /** The byte string constant for Reject-Contact header compact name. */
  public static final DsByteString BS_REJECT_CONTACT_C = new DsByteString("j");
  /** The byte string constant for Request-Disposition header compact name. */
  public static final DsByteString BS_REQUEST_DISPOSITION_C = new DsByteString("d");
  /** The constant for the total number of compact header names. */
  public static final int COMPACT_HEADER_NAMES_SIZE = 18;

  //////////////////////////////////////////////////////////////////////////////////////
  // Header Name plus ": " Byte Strings (Compact form)
  //////////////////////////////////////////////////////////////////////////////////////

  /** The byte string constant for "u: " token. */
  public static final DsByteString BS_ALLOW_EVENTS_C_TOKEN = new DsByteString("u: ");

  /** The byte string constant for "i: " token. */
  public static final DsByteString BS_CALL_ID_C_TOKEN = new DsByteString("i: ");
  /** The byte string constant for "m: " token. */
  public static final DsByteString BS_CONTACT_C_TOKEN = new DsByteString("m: ");
  /** The byte string constant for "e: " token. */
  public static final DsByteString BS_CONTENT_ENCODING_C_TOKEN = new DsByteString("e: ");
  /** The byte string constant for "l: " token. */
  public static final DsByteString BS_CONTENT_LENGTH_C_TOKEN = new DsByteString("l: ");
  /** The byte string constant for "c: " token. */
  public static final DsByteString BS_CONTENT_TYPE_C_TOKEN = new DsByteString("c: ");

  /** The byte string constant for "o: " token. */
  public static final DsByteString BS_EVENT_C_TOKEN = new DsByteString("o: ");

  /** The byte string constant for "f: " token. */
  public static final DsByteString BS_FROM_C_TOKEN = new DsByteString("f: ");

  /** The byte string constant for "r: " token. */
  public static final DsByteString BS_REFER_TO_C_TOKEN = new DsByteString("r: ");

  /** The byte string constant for "b: " token. */
  public static final DsByteString BS_REFERRED_BY_C_TOKEN = new DsByteString("b: ");

  /** The byte string constant for "x: " token. */
  public static final DsByteString BS_SESSION_EXPIRES_C_TOKEN = new DsByteString("x: ");
  /** The byte string constant for "s: " token. */
  public static final DsByteString BS_SUBJECT_C_TOKEN = new DsByteString("s: ");
  /** The byte string constant for "k: " token. */
  public static final DsByteString BS_SUPPORTED_C_TOKEN = new DsByteString("k: ");

  /** The byte string constant for "t: " token. */
  public static final DsByteString BS_TO_C_TOKEN = new DsByteString("t: ");

  /** The byte string constant for "v: " token. */
  public static final DsByteString BS_VIA_C_TOKEN = new DsByteString("v: ");

  // CAFFEINE 2.0 DEVELOPMENT
  /** The byte string constant for "a: " token. */
  public static final DsByteString BS_ACCEPT_CONTACT_C_TOKEN = new DsByteString("a: ");
  /** The byte string constant for "j: " token. */
  public static final DsByteString BS_REJECT_CONTACT_C_TOKEN = new DsByteString("j: ");
  /** The byte string constant for "d: " token. */
  public static final DsByteString BS_REQUEST_DISPOSITION_C_TOKEN = new DsByteString("d: ");

  //////////////////////////////////////////////////////////////////////////////////////
  // Non-Header Context ID's Byte Strings
  //////////////////////////////////////////////////////////////////////////////////////

  /** The byte string constant for "ENTIRE_MSG_ID" token. */
  public static final DsByteString BS_ENTIRE_MSG_ID = new DsByteString("ENTIRE_MSG_ID");
  /** The byte string constant for "NAME_ADDR_ID" token. */
  public static final DsByteString BS_NAME_ADDR_ID = new DsByteString("NAME_ADDR_ID");
  /** The byte string constant for "SIP_URL_ID" token. */
  public static final DsByteString BS_SIP_URL_ID = new DsByteString("SIP_URL_ID");
  /** The byte string constant for "TEL_URL_ID" token. */
  public static final DsByteString BS_TEL_URL_ID = new DsByteString("TEL_URL_ID");
  // CAFFEINE 2.0 DEVELOPMENT
  /** The byte string constant for "CID_URL_ID" token. */
  public static final DsByteString BS_CID_URL_ID = new DsByteString("CID_URL_ID");
  /** The byte string constant for "UNKNOWN_URL_ID" token. */
  public static final DsByteString BS_UNKNOWN_URL_ID = new DsByteString("UNKNOWN_URL_ID");

  //////////////////////////////////////////////////////////////////////////////////////
  // Element Name Ids
  //////////////////////////////////////////////////////////////////////////////////////

  // we will need to delete some of these and add some new ones. - jsm
  // element names
  /** Element ID. */
  public static final int ADDR_SPEC = 0;
  /** Element ID. */
  public static final int CALLID = 1;
  /** Element ID. */
  public static final int CNONCE_VALUE = 2;
  /** Element ID. */
  public static final int COMMENT = 3;

  // optimized for Via elementFound() switch
  /** Element ID. */
  public static final int HOST = 4;
  /** Element ID. */
  public static final int PORT = 5;
  /** Element ID. */
  public static final int PROTOCOL_NAME = 6;
  /** Element ID. */
  public static final int PROTOCOL_VERSION = 7;
  /** Element ID. */
  public static final int TRANSPORT = 8;

  /** Element ID. */
  public static final int CONTENT_CODING = 9;
  /** Element ID. */
  public static final int DELAY = 10;
  /** Element ID. */
  public static final int DELTA_SECONDS = 11;
  /** Element ID. */
  public static final int DISPLAY_NAME = 12;
  /** Element ID. */
  public static final int DISPOSITION_TYPE = 13;
  /** Element ID. */
  public static final int DISP_EXTENSION_TOKEN = 14;
  /** Element ID. */
  public static final int HANDLING = 15;
  /** Element ID. */
  public static final int NONCE_VALUE = 16;
  /** Element ID. */
  public static final int OPTION_TAG = 17;
  /** Element ID. */
  public static final int OTHER_HANDLING = 18;
  /** Element ID. */
  public static final int OTHER_PRIORITY = 19;
  /** Element ID. */
  public static final int PASSWORD = 20;
  /** Element ID. */
  public static final int PRIMARY_TAG = 21;
  /** Element ID. */
  public static final int TAG = 22;
  /** Element ID. */
  public static final int PRODUCT = 23;
  /** Element ID. */
  public static final int PRIVACY_ID = 24;
  /** Element ID. */
  public static final int QOP_VALUE = 25;
  /** Element ID. */
  public static final int QVALUE = 26;
  /** Element ID. */
  public static final int REALM_VALUE = 27;
  /** Element ID. */
  public static final int SENT_BY = 28;
  /** Element ID. */
  public static final int SENT_PROTOCOL = 29;
  /** Element ID. */
  public static final int SIP_DATE = 30;
  /** Element ID. */
  public static final int SUBTAG = 31;

  /** Element ID. */
  public static final int URI = 32;
  /** Element ID. */
  public static final int USERNAME = 33;
  /** Element ID. */
  public static final int VIA_HIDDEN = 34;
  /** Element ID. */
  public static final int WARN_AGENT = 35;
  /** Element ID. */
  public static final int WARN_CODE = 36;
  /** Element ID. */
  public static final int WARN_TEXT = 37;

  /** Element ID. */
  public static final int SIP_URL = 38;
  /** Element ID. */
  public static final int SIPS_URL = 39;
  /** Element ID. */
  public static final int TEL_URL = 40;
  /** Element ID. */
  public static final int HTTP_URL = 41;
  /** Element ID. */
  public static final int UNKNOWN_URL = 42;
  /** Element ID. */
  public static final int CSEQ_METHOD = 43;
  /** Element ID. */
  public static final int CSEQ_NUMBER = 44;
  /** Element ID. */
  public static final int ACTION = 45;
  /** Element ID. */
  public static final int EXPIRES_VALUE = 46;
  /** Element ID. */
  public static final int SINGLE_VALUE = 47;
  /** Element ID. */
  public static final int MADDR = 48;
  /** Element ID. */
  public static final int BRANCH = 49;
  /** Element ID. */
  public static final int HIDDEN = 50;
  /** Element ID. */
  public static final int RECEIVED = 51;
  /** Element ID. */
  public static final int USER = 52;
  /** Element ID. */
  public static final int METHOD = 53;
  /** Element ID. */
  public static final int LANGUAGE_TAG = 54;
  /** Element ID. */
  public static final int LANGUAGE_SUBTAG = 55;

  /** Element ID. */
  public static final int YEAR = 56;
  /** Element ID. */
  public static final int MONTH = 57;
  /** Element ID. */
  public static final int DAY_OF_WEEK = 58;
  /** Element ID. */
  public static final int DAY_OF_MONTH = 59;
  /** Element ID. */
  public static final int HOUR = 60;
  /** Element ID. */
  public static final int MINUTE = 61;
  /** Element ID. */
  public static final int SECOND = 62;
  /** Element ID. */
  public static final int GMT = 63;
  /** Element ID. */
  public static final int TIME_ZONE = 64;
  /** Element ID. */
  public static final int TYPE = 65;
  /** Element ID. */
  public static final int SUB_TYPE = 66;
  /** Element ID. */
  public static final int TIMESTAMP_VALUE = 67;
  /** Element ID. */
  public static final int PURPOSE = 68;
  /** Element ID. */
  public static final int DURATION = 69;

  /** Element ID. */
  public static final int NC = 70;
  /** Element ID. */
  public static final int QOP = 71;
  /** Element ID. */
  public static final int AUTH = 72;
  /** Element ID. */
  public static final int REALM = 73;
  /** Element ID. */
  public static final int NONCE = 74;
  /** Element ID. */
  public static final int STALE = 75;
  /** Element ID. */
  public static final int CNONCE = 76;
  /** Element ID. */
  public static final int DOMAIN = 77;
  /** Element ID. */
  public static final int OPAQUE = 78;
  /** Element ID. */
  public static final int RESPONSE = 79;
  /** Element ID. */
  public static final int NEXTNONCE = 80;
  /** Element ID. */
  public static final int ALGORITHM = 81;
  /** Element ID. */
  public static final int BASIC_COOKIE = 82;

  /** Element ID. */
  public static final int PARTY = 83;
  /** Element ID. */
  public static final int SCREEN = 84;
  /** Element ID. */
  public static final int ID_TYPE = 85;
  /** Element ID. */
  public static final int PRODUCT_VERSION = 86;

  /** Element ID. */
  public static final int REFRESHER = 87;

  /** Element ID. */
  public static final int RESPONSE_NUMBER = 88;
  /** Element ID. */
  public static final int TEL_URL_NUMBER = 89;
  /** Element ID. */
  public static final int TSP = 90;
  /** Element ID. */
  public static final int ISUB = 91;
  /** Element ID. */
  public static final int POSTD = 92;
  /** Element ID. */
  public static final int PHONE_CONTEXT = 93;

  /** Element ID. */
  public static final int EVENT_PACKAGE = 94;
  /** Element ID. */
  public static final int EVENT_SUB_PACKAGE = 95;

  /** Element ID. */
  public static final int HEADER_NAME = 96;
  /** Element ID. */
  public static final int URI_SCHEME = 97;
  /** Element ID. */
  public static final int URI_DATA = 98;
  /** Element ID. */
  public static final int NAT = 99;

  /** Element ID. */
  public static final int NAME_ADDR = 100;

  // known parameters that I have seen at bakeoffs
  /** Element ID. */
  public static final int RECEIVED_PORT = 101;
  /** Element ID. */
  public static final int FORKING_ID = 102;
  /** Element ID. */
  public static final int PARAMETERS = 103;
  /** Element ID. */
  public static final int WILDCARD = 104;

  /** Element ID. */
  public static final int LR = 105;
  /** Element ID. */
  public static final int COMP = 106;

  /** Element ID. */
  public static final int TTL = 107;
  /** Element ID. */
  public static final int ACTIVE = 108;
  /** Element ID. */
  public static final int PENDING = 109;
  /** Element ID. */
  public static final int TERMINATED = 110;
  /** Element ID. */
  public static final int REASON = 111;
  /** Element ID. */
  public static final int RETRY_AFTER_VALUE = 112;
  /** Element ID. */
  public static final int DEACTIVATED = 113;
  /** Element ID. */
  public static final int PROBATION = 114;
  /** Element ID. */
  public static final int REJECTED = 115;
  /** Element ID. */
  public static final int TIMEOUT = 116;
  /** Element ID. */
  public static final int GIVEUP = 117;
  /** Element ID. */
  public static final int NORESOURCE = 118;
  /** Element ID. */
  public static final int ID = 119;
  // CAFFEINE 2.0 DEVELOPMENT
  /** Element ID. */
  public static final int CID_URL = 120;

  /** The number of Element IDs. */
  public static final int ELEMENT_NAMES_SIZE = 121; // must be properly sized now - jsm

  //////////////////////////////////////////////////////////////////////////////////////
  // Element Name Byte Strings
  //////////////////////////////////////////////////////////////////////////////////////

  /** The byte string constant for "ADDR_SPEC" token. */
  public static final DsByteString BSU_ADDR_SPEC = new DsByteString("ADDR_SPEC");
  /** The byte string constant for "BASIC" token. */
  public static final DsByteString BSU_BASIC = new DsByteString("BASIC");
  /** The byte string constant for "CALLID" token. */
  public static final DsByteString BSU_CALLID = new DsByteString("CALLID");
  /** The byte string constant for "CNONCE_VALUE" token. */
  public static final DsByteString BSU_CNONCE_VALUE = new DsByteString("CNONCE_VALUE");
  /** The byte string constant for "COMMENT" token. */
  public static final DsByteString BSU_COMMENT = new DsByteString("COMMENT");
  /** The byte string constant for "CONTENT_CODING" token. */
  public static final DsByteString BSU_CONTENT_CODING = new DsByteString("CONTENT_CODING");
  /** The byte string constant for "DELAY" token. */
  public static final DsByteString BSU_DELAY = new DsByteString("DELAY");
  /** The byte string constant for "DELTA_SECONDS" token. */
  public static final DsByteString BSU_DELTA_SECONDS = new DsByteString("DELTA_SECONDS");
  /** The byte string constant for "DISPLAY_NAME" token. */
  public static final DsByteString BSU_DISPLAY_NAME = new DsByteString("DISPLAY_NAME");
  /** The byte string constant for "DISPOSITION_TYPE" token. */
  public static final DsByteString BSU_DISPOSITION_TYPE = new DsByteString("DISPOSITION_TYPE");
  /** The byte string constant for "DISP_EXTENSION_TOKEN" token. */
  public static final DsByteString BSU_DISP_EXTENSION_TOKEN =
      new DsByteString("DISP_EXTENSION_TOKEN");
  /** The byte string constant for "HOST" token. */
  public static final DsByteString BSU_HOST = new DsByteString("HOST");
  /** The byte string constant for "NONCE_VALUE" token. */
  public static final DsByteString BSU_NONCE_VALUE = new DsByteString("NONCE_VALUE");
  /** The byte string constant for "OPTION_TAG" token. */
  public static final DsByteString BSU_OPTION_TAG = new DsByteString("OPTION_TAG");
  /** The byte string constant for "OTHER_HANDLING" token. */
  public static final DsByteString BSU_OTHER_HANDLING = new DsByteString("OTHER_HANDLING");
  /** The byte string constant for "OTHER_PRIORITY" token. */
  public static final DsByteString BSU_OTHER_PRIORITY = new DsByteString("OTHER_PRIORITY");
  /** The byte string constant for "PASSWORD" token. */
  public static final DsByteString BSU_PASSWORD = new DsByteString("PASSWORD");
  /** The byte string constant for "PORT" token. */
  public static final DsByteString BSU_PORT = new DsByteString("PORT");
  /** The byte string constant for "PRIMARY_TAG" token. */
  public static final DsByteString BSU_PRIMARY_TAG = new DsByteString("PRIMARY_TAG");
  /** The byte string constant for "TAG" token. */
  public static final DsByteString BSU_TAG = new DsByteString("TAG");
  /** The byte string constant for "PRODUCT" token. */
  public static final DsByteString BSU_PRODUCT = new DsByteString("PRODUCT");
  /** The byte string constant for "PRODUCT_VERSION" token. */
  public static final DsByteString BSU_PRODUCT_VERSION = new DsByteString("PRODUCT_VERSION");
  /** The byte string constant for "PROTOCOL_NAME" token. */
  public static final DsByteString BSU_PROTOCOL_NAME = new DsByteString("PROTOCOL_NAME");
  /** The byte string constant for "PROTOCOL_VERSION" token. */
  public static final DsByteString BSU_PROTOCOL_VERSION = new DsByteString("PROTOCOL_VERSION");
  /** The byte string constant for "QOP_VALUE" token. */
  public static final DsByteString BSU_QOP_VALUE = new DsByteString("QOP_VALUE");
  /** The byte string constant for "QVALUE" token. */
  public static final DsByteString BSU_QVALUE = new DsByteString("QVALUE");
  /** The byte string constant for "REALM_VALUE" token. */
  public static final DsByteString BSU_REALM_VALUE = new DsByteString("REALM_VALUE");
  /** The byte string constant for "SENT_BY" token. */
  public static final DsByteString BSU_SENT_BY = new DsByteString("SENT_BY");
  /** The byte string constant for "SENT_PROTOCOL" token. */
  public static final DsByteString BSU_SENT_PROTOCOL = new DsByteString("SENT_PROTOCOL");
  /** The byte string constant for "SIP_DATE" token. */
  public static final DsByteString BSU_SIP_DATE = new DsByteString("SIP_DATE");
  /** The byte string constant for "SIP_URL" token. */
  public static final DsByteString BSU_SIP_URL = new DsByteString("SIP_URL");
  /** The byte string constant for "SIPS_URL" token. */
  public static final DsByteString BSU_SIPS_URL = new DsByteString("SIPS_URL");
  /** The byte string constant for "SUBTAG" token. */
  public static final DsByteString BSU_SUBTAG = new DsByteString("SUBTAG");
  /** The byte string constant for "TRANSPORT" token. */
  public static final DsByteString BSU_TRANSPORT = new DsByteString("TRANSPORT");
  /** The byte string constant for "TTL" token. */
  public static final DsByteString BSU_TTL = new DsByteString("TTL");
  /** The byte string constant for "URI" token. */
  public static final DsByteString BSU_URI = new DsByteString("URI");
  /** The byte string constant for "USERNAME" token. */
  public static final DsByteString BSU_USERNAME = new DsByteString("USERNAME");
  /** The byte string constant for "VIA_HIDDEN" token. */
  public static final DsByteString BSU_VIA_HIDDEN = new DsByteString("VIA_HIDDEN");
  /** The byte string constant for "WARN_AGENT" token. */
  public static final DsByteString BSU_WARN_AGENT = new DsByteString("WARN_AGENT");
  /** The byte string constant for "WARN_CODE" token. */
  public static final DsByteString BSU_WARN_CODE = new DsByteString("WARN_CODE");
  /** The byte string constant for "WARN_TEXT" token. */
  public static final DsByteString BSU_WARN_TEXT = new DsByteString("WARN_TEXT");
  /** The byte string constant for "HTTP_URL" token. */
  public static final DsByteString BSU_HTTP_URL = new DsByteString("HTTP_URL");
  /** The byte string constant for "TEL_URL" token. */
  public static final DsByteString BSU_TEL_URL = new DsByteString("TEL_URL");
  // CAFFEINE 2.0 DEVELOPMENT - Support Content-ID
  /** The byte string constant for "CID_URL" token. */
  public static final DsByteString BSU_CID_URL = new DsByteString("CID_URL");
  /** The byte string constant for "UNKNOWN_URL" token. */
  public static final DsByteString BSU_UNKNOWN_URL = new DsByteString("UNKNOWN_URL");
  /** The byte string constant for "CSEQ_METHOD" token. */
  public static final DsByteString BSU_CSEQ_METHOD = new DsByteString("CSEQ_METHOD");
  /** The byte string constant for "CSEQ_NUMBER" token. */
  public static final DsByteString BSU_CSEQ_NUMBER = new DsByteString("CSEQ_NUMBER");
  /** The byte string constant for "WILDCARD" token. */
  public static final DsByteString BSU_WILDCARD = new DsByteString("WILDCARD");
  /** The byte string constant for "ACTION" token. */
  public static final DsByteString BSU_ACTION = new DsByteString("ACTION");
  /** The byte string constant for "EXPIRES_VALUE" token. */
  public static final DsByteString BSU_EXPIRES_VALUE = new DsByteString("EXPIRES_VALUE");
  /** The byte string constant for "SINGLE_VALUE" token. */
  public static final DsByteString BSU_SINGLE_VALUE = new DsByteString("SINGLE_VALUE");
  /** The byte string constant for "MADDR" token. */
  public static final DsByteString BSU_MADDR = new DsByteString("MADDR");
  /** The byte string constant for "BRANCH" token. */
  public static final DsByteString BSU_BRANCH = new DsByteString("BRANCH");
  /** The byte string constant for "HIDDEN" token. */
  public static final DsByteString BSU_HIDDEN = new DsByteString("HIDDEN");
  /** The byte string constant for "RECEIVED" token. */
  public static final DsByteString BSU_RECEIVED = new DsByteString("RECEIVED");
  /** The byte string constant for "USER" token. */
  public static final DsByteString BSU_USER = new DsByteString("USER");
  /** The byte string constant for "METHOD" token. */
  public static final DsByteString BSU_METHOD = new DsByteString("METHOD");
  /** The byte string constant for "HANDLING" token. */
  public static final DsByteString BSU_HANDLING = new DsByteString("HANDLING");
  /** The byte string constant for "LANGUAGE_TAG" token. */
  public static final DsByteString BSU_LANGUAGE_TAG = new DsByteString("LANGUAGE_TAG");
  /** The byte string constant for "LANGUAGE_SUBTAG" token. */
  public static final DsByteString BSU_LANGUAGE_SUBTAG = new DsByteString("LANGUAGE_SUBTAG");
  /** The byte string constant for "YEAR" token. */
  public static final DsByteString BSU_YEAR = new DsByteString("YEAR");
  /** The byte string constant for "MONTH" token. */
  public static final DsByteString BSU_MONTH = new DsByteString("MONTH");
  /** The byte string constant for "DAY_OF_WEEK" token. */
  public static final DsByteString BSU_DAY_OF_WEEK = new DsByteString("DAY_OF_WEEK");
  /** The byte string constant for "DAY_OF_MONTH" token. */
  public static final DsByteString BSU_DAY_OF_MONTH = new DsByteString("DAY_OF_MONTH");
  /** The byte string constant for "HOUR" token. */
  public static final DsByteString BSU_HOUR = new DsByteString("HOUR");
  /** The byte string constant for "MINUTE" token. */
  public static final DsByteString BSU_MINUTE = new DsByteString("MINUTE");
  /** The byte string constant for "SECOND" token. */
  public static final DsByteString BSU_SECOND = new DsByteString("SECOND");
  /** The byte string constant for "GMT" token. */
  public static final DsByteString BSU_GMT = new DsByteString("GMT");
  /** The byte string constant for "TIME_ZONE" token. */
  public static final DsByteString BSU_TIME_ZONE = new DsByteString("TIME_ZONE");
  /** The byte string constant for "TYPE" token. */
  public static final DsByteString BSU_TYPE = new DsByteString("TYPE");
  /** The byte string constant for "SUB_TYPE" token. */
  public static final DsByteString BSU_SUB_TYPE = new DsByteString("SUB_TYPE");
  /** The byte string constant for "TIMESTAMP_VALUE" token. */
  public static final DsByteString BSU_TIMESTAMP_VALUE = new DsByteString("TIMESTAMP_VALUE");
  /** The byte string constant for "PURPOSE" token. */
  public static final DsByteString BSU_PURPOSE = new DsByteString("PURPOSE");
  /** The byte string constant for "DURATION" token. */
  public static final DsByteString BSU_DURATION = new DsByteString("DURATION");
  /** The byte string constant for "NC" token. */
  public static final DsByteString BSU_NC = new DsByteString("NC");
  /** The byte string constant for "QOP" token. */
  public static final DsByteString BSU_QOP = new DsByteString("QOP");
  /** The byte string constant for "AUTH" token. */
  public static final DsByteString BSU_AUTH = new DsByteString("AUTH");
  /** The byte string constant for "REALM" token. */
  public static final DsByteString BSU_REALM = new DsByteString("REALM");
  /** The byte string constant for "NONCE" token. */
  public static final DsByteString BSU_NONCE = new DsByteString("NONCE");
  /** The byte string constant for "STALE" token. */
  public static final DsByteString BSU_STALE = new DsByteString("STALE");
  /** The byte string constant for "CNONCE" token. */
  public static final DsByteString BSU_CNONCE = new DsByteString("CNONCE");
  /** The byte string constant for "DOMAIN" token. */
  public static final DsByteString BSU_DOMAIN = new DsByteString("DOMAIN");
  /** The byte string constant for "OPAQUE" token. */
  public static final DsByteString BSU_OPAQUE = new DsByteString("OPAQUE");
  /** The byte string constant for "RESPONSE" token. */
  public static final DsByteString BSU_RESPONSE = new DsByteString("RESPONSE");
  /** The byte string constant for "NEXTNONCE" token. */
  public static final DsByteString BSU_NEXTNONCE = new DsByteString("NEXTNONCE");
  /** The byte string constant for "ALGORITHM" token. */
  public static final DsByteString BSU_ALGORITHM = new DsByteString("ALGORITHM");
  /** The byte string constant for "PARTY" token. */
  public static final DsByteString BSU_PARTY = new DsByteString("PARTY");
  /** The byte string constant for "SCREEN" token. */
  public static final DsByteString BSU_SCREEN = new DsByteString("SCREEN");
  /** The byte string constant for "ID_TYPE" token. */
  public static final DsByteString BSU_ID_TYPE = new DsByteString("ID_TYPE");
  /** The byte string constant for "PRIVACY" token. */
  public static final DsByteString BSU_PRIVACY = new DsByteString("PRIVACY");
  /** The byte string constant for "REFRESHER" token. */
  public static final DsByteString BSU_REFRESHER = new DsByteString("REFRESHER");
  /** The byte string constant for "RESPONSE_NUMBER" token. */
  public static final DsByteString BSU_RESPONSE_NUMBER = new DsByteString("RESPONSE_NUMBER");
  /** The byte string constant for "TEL_URL_NUMBER" token. */
  public static final DsByteString BSU_TEL_URL_NUMBER = new DsByteString("TEL_URL_NUMBER");
  /** The byte string constant for "TSP" token. */
  public static final DsByteString BSU_TSP = new DsByteString("TSP");
  /** The byte string constant for "ISUB" token. */
  public static final DsByteString BSU_ISUB = new DsByteString("ISUB");
  /** The byte string constant for "POSTD" token. */
  public static final DsByteString BSU_POSTD = new DsByteString("POSTD");
  /** The byte string constant for "PHONE_CONTEXT" token. */
  public static final DsByteString BSU_PHONE_CONTEXT = new DsByteString("PHONE_CONTEXT");
  /** The byte string constant for "EVENT_PACKAGE" token. */
  public static final DsByteString BSU_EVENT_PACKAGE = new DsByteString("EVENT_PACKAGE");
  /** The byte string constant for "EVENT_SUB_PACKAGE" token. */
  public static final DsByteString BSU_EVENT_SUB_PACKAGE = new DsByteString("EVENT_SUB_PACKAGE");
  /** The byte string constant for "HEADER_NAME" token. */
  public static final DsByteString BSU_HEADER_NAME = new DsByteString("HEADER_NAME");
  /** The byte string constant for "URI_SCHEME" token. */
  public static final DsByteString BSU_URI_SCHEME = new DsByteString("URI_SCHEME");
  /** The byte string constant for "URI_DATA" token. */
  public static final DsByteString BSU_URI_DATA = new DsByteString("URI_DATA");
  /** The byte string constant for "NAT" token. */
  public static final DsByteString BSU_NAT = new DsByteString("NAT");
  /** The byte string constant for "NAME_ADDR" token. */
  public static final DsByteString BSU_NAME_ADDR = new DsByteString("NAME_ADDR");
  /** The byte string constant for "RECEIVED_PORT" token. */
  public static final DsByteString BSU_RECEIVED_PORT = new DsByteString("RECEIVED_PORT");
  /** The byte string constant for "FORKING_ID" token. */
  public static final DsByteString BSU_FORKING_ID = new DsByteString("FORKING_ID");
  /** The byte string constant for "PARAMETERS" token. */
  public static final DsByteString BSU_PARAMETERS = new DsByteString("PARAMETERS");
  /** The byte string constant for "LR" token. */
  public static final DsByteString BSU_LR = new DsByteString("LR");
  /** The byte string constant for "COMP" token. */
  public static final DsByteString BSU_COMP = new DsByteString("COMP");
  /** The byte string constant for "ACTIVE" token. */
  public static final DsByteString BSU_ACTIVE = new DsByteString("ACTIVE");
  /** The byte string constant for "PENDING" token. */
  public static final DsByteString BSU_PENDING = new DsByteString("PENDING");
  /** The byte string constant for "TERMINATED" token. */
  public static final DsByteString BSU_TERMINATED = new DsByteString("TERMINATED");
  /** The byte string constant for "REASON" token. */
  public static final DsByteString BSU_REASON = new DsByteString("REASON");
  /** The byte string constant for "RETRY_AFTER_VALUE" token. */
  public static final DsByteString BSU_RETRY_AFTER_VALUE = new DsByteString("RETRY_AFTER_VALUE");
  /** The byte string constant for "DEACTIVATED" token. */
  public static final DsByteString BSU_DEACTIVATED = new DsByteString("DEACTIVATED");
  /** The byte string constant for "PROBATION" token. */
  public static final DsByteString BSU_PROBATION = new DsByteString("PROBATION");
  /** The byte string constant for "REJECTED" token. */
  public static final DsByteString BSU_REJECTED = new DsByteString("REJECTED");
  /** The byte string constant for "TIMEOUT" token. */
  public static final DsByteString BSU_TIMEOUT = new DsByteString("TIMEOUT");
  /** The byte string constant for "GIVEUP" token. */
  public static final DsByteString BSU_GIVEUP = new DsByteString("GIVEUP");
  /** The byte string constant for "NORESOURCE" token. */
  public static final DsByteString BSU_NORESOURCE = new DsByteString("NORESOURCE");
  /** The byte string constant for "ID" token. */
  public static final DsByteString BSU_ID = new DsByteString("ID");
  /** The byte string constant for "PINT" token. */
  public static final DsByteString BSU_PINT = new DsByteString("PINT");
  /** The byte string constant for "SYSTEM" token. */
  public static final DsByteString BSU_SYSTEM = new DsByteString("SYSTEM");
  /** The byte string constant for "CPU" token. */
  public static final DsByteString BSU_CPU = new DsByteString("CPU");
  /** The byte string constant for "MEM" token. */
  public static final DsByteString BSU_MEM = new DsByteString("MEM");
  /** The byte string constant for "DS0" token. */
  public static final DsByteString BSU_DS0 = new DsByteString("DS0");
  /** The byte string constant for "DSP" token. */
  public static final DsByteString BSU_DSP = new DsByteString("DSP");
  /** The byte string constant for "MB" token. */
  public static final DsByteString BSU_MB = new DsByteString("MB");

  //////////////////////////////////////////////////////////////////////////////////////
  // Constant Bytes that we may be using frequently
  //////////////////////////////////////////////////////////////////////////////////////

  /*
   * The constant bytes. All the Constant names that start with 'B_',
   * represents byte constant.
   */
  /** The constant for '&' byte. */
  public static final byte B_AMPERSAND = (byte) '&';
  /** The constant for '@' byte. */
  public static final byte B_AT = (byte) '@';
  /** The constant for '\' byte. */
  public static final byte B_BSLASH = (byte) '\\';
  /** The constant for ':' byte. */
  public static final byte B_COLON = (byte) ':';
  /** The constant for ',' byte. */
  public static final byte B_COMMA = (byte) ',';
  /** The constant for '=' byte. */
  public static final byte B_EQUAL = (byte) '=';
  /** The constant for '-' byte. */
  public static final byte B_HIPHEN = (byte) '-';
  /** The constant for '(' byte. */
  public static final byte B_LBRACE = (byte) '(';
  /** The constant for '<' byte. */
  public static final byte B_LABRACE = (byte) '<';
  /** The constant for '\n' newline byte. */
  public static final byte B_NEWLINE = (byte) '\n';
  /** The constant for '+' byte. */
  public static final byte B_PLUS = (byte) '+';
  /** The constant for '%' byte. */
  public static final byte B_PERCENT = (byte) '%';
  /** The constant for '.' byte. */
  public static final byte B_PERIOD = (byte) '.';
  /** The constant for '?' byte. */
  public static final byte B_QUESTION = (byte) '?';
  /** The constant for '"' byte. */
  public static final byte B_QUOTE = (byte) '\"';
  /** The constant for '>' byte. */
  public static final byte B_RABRACE = (byte) '>';
  /** The constant for ')' byte. */
  public static final byte B_RBRACE = (byte) ')';
  /** The constant for '\r' carriage return byte. */
  public static final byte B_RETURN = (byte) '\r';
  /** The constant for ';' byte. */
  public static final byte B_SEMI = (byte) ';';
  /** The constant for '/' byte. */
  public static final byte B_SLASH = (byte) '/';
  /** The constant for ' ' blank space byte. */
  public static final byte B_SPACE = (byte) ' ';
  /** The constant for '*' byte. */
  public static final byte B_WILDCARD = (byte) '*';
  /** The constant for '[' byte. */
  public static final byte B_OPEN_BRACKET = (byte) '[';
  /** The constant for ']' byte. */
  public static final byte B_CLOSE_BRACKET = (byte) ']';

  /** The constant for byte array with '*' as the only byte element. */
  public static final byte[] BA_WILDCARD = {(byte) '*'};

  //////////////////////////////////////////////////////////////////////////////////////
  // Constant Byte strings for convenience
  //////////////////////////////////////////////////////////////////////////////////////

  /** The byte string constant for "&" token. */
  public static final DsByteString BS_AMPERSAND = new DsByteString("&");
  /** The byte string constant for "@" token. */
  public static final DsByteString BS_AT = new DsByteString("@");
  /** The byte string constant for "-" token. */
  public static final DsByteString BS_MINUS = new DsByteString("-");
  /** The byte string constant for "" token (Blank byte string). */
  public static final DsByteString BS_BLANK = new DsByteString("");
  /** The byte string constant for ":" token. */
  public static final DsByteString BS_COLON = new DsByteString(":");
  /** The byte string constant for ": " token. */
  public static final DsByteString BS_COLON_SPACE = new DsByteString(": ");
  /** The byte string constant for "," token. */
  public static final DsByteString BS_COMMA = new DsByteString(",");
  /** The byte string constant for "\r\n" token. */
  public static final DsByteString BS_EOH = new DsByteString("\r\n");
  /** The byte string constant for "\r\n\r\n" token. */
  public static final DsByteString BS_EOH_EOH = new DsByteString("\r\n\r\n");
  /** The byte string constant for "=" token. */
  public static final DsByteString BS_EQUAL = new DsByteString("=");
  /** The byte string constant for "sip" token. */
  public static final DsByteString BS_LSIP = new DsByteString("sip");
  /** The byte string constant for "sips" token. */
  public static final DsByteString BS_SIPS = new DsByteString("sips");
  /** The byte string constant for "\n" token (New Line). */
  public static final DsByteString BS_NEWLINE = new DsByteString("\n");
  /** The byte string constant for "%" token. */
  public static final DsByteString BS_PERCENT = new DsByteString("%");
  /** The byte string constant for "q" token. */
  public static final DsByteString BS_PERIOD = new DsByteString(".");
  /** The byte string constant for "q" token. */
  public static final DsByteString BS_Q = new DsByteString("q");
  /** The byte string constant for "\"" token. */
  public static final DsByteString BS_QUOTES = new DsByteString("\"");
  /** The byte string constant for "?" token. */
  public static final DsByteString BS_QUESTION = new DsByteString("?");
  /** The byte string constant for ";q=" token. */
  public static final DsByteString BS_QVALUE = new DsByteString(";q=");
  /** The byte string constant for "\r" token Carriage Return). */
  public static final DsByteString BS_RETURN = new DsByteString("\r");
  /** The byte string constant for "/" token. */
  public static final DsByteString BS_SLASH = new DsByteString("/");
  /** The byte string constant for "*" token. */
  public static final DsByteString BS_WILDCARD = new DsByteString("*");
  /** The byte string constant for ";" token. */
  public static final DsByteString BS_SEMI = new DsByteString(";");
  /** The byte string constant for "<" token. */
  public static final DsByteString BS_LABRACE = new DsByteString("<");
  /** The byte string constant for ">" token. */
  public static final DsByteString BS_RABRACE = new DsByteString(">");
  /** The byte string constant for "(" token. */
  public static final DsByteString BS_LBRACE = new DsByteString("(");
  /** The byte string constant for ")" token. */
  public static final DsByteString BS_RBRACE = new DsByteString(")");
  /** The byte string constant for " " token (Blank Space). */
  public static final DsByteString BS_SPACE = new DsByteString(" ");
  /** The byte string constant for "SIP" token. */
  public static final DsByteString BS_SIP = new DsByteString("SIP");
  /** The byte string constant for "SIP/2.0" token. */
  public static final DsByteString BS_SIP_VERSION = new DsByteString("SIP/2.0");
  /** The byte string constant for "SIP/2.0 " token. */
  public static final DsByteString BS_SIP_VERSION_SPACE = new DsByteString("SIP/2.0 ");
  /** The byte string constant for "2.0" token. */
  public static final DsByteString BS_VERSION = new DsByteString("2.0");

  /** The byte string constant for "sdp" token. */
  public static final DsByteString BS_SDP = new DsByteString("sdp");
  /** The byte string constant for "text" token. */
  public static final DsByteString BS_TEXT = new DsByteString("text");
  /** The byte string constant for "html" token. */
  public static final DsByteString BS_HTML = new DsByteString("html");
  /** The byte string constant for "plain" token. */
  public static final DsByteString BS_PLAIN = new DsByteString("plain");
  /** The byte string constant for "application" token. */
  public static final DsByteString BS_APPLICATION = new DsByteString("application");
  /** The byte string constant for "text/plain" token. */
  public static final DsByteString BS_TEXT_PLAIN = new DsByteString("text/plain");

  // Parameter directives/names
  /** The byte string constant for "action" token. */
  public static final DsByteString BS_ACTION = new DsByteString("action");
  /** The byte string constant for "proxy" token. */
  public static final DsByteString BS_ACTION_PROXY = new DsByteString("proxy");
  /** The byte string constant for "redirect" token. */
  public static final DsByteString BS_ACTION_REDIRECT = new DsByteString("redirect");
  /** The byte string constant for "algorithm" token. */
  public static final DsByteString BS_ALGORITHM = new DsByteString("algorithm");
  /** The byte string constant for "auth" token. */
  public static final DsByteString BS_AUTH = new DsByteString("auth");
  /** The byte string constant for "auth-int" token. */
  public static final DsByteString BS_AUTH_INT = new DsByteString("auth-int");
  /** The byte string constant for "branch" token. */
  public static final DsByteString BS_BRANCH = new DsByteString("branch");
  /** The byte string constant for "Basic" token. */
  public static final DsByteString BS_BASIC = new DsByteString("Basic");
  /** The byte string constant for "cic" token. */
  public static final DsByteString BS_CIC = new DsByteString("cic");
  /** The byte string constant for "cnonce" token. */
  public static final DsByteString BS_CNONCE = new DsByteString("cnonce");
  /** The byte string constant for "Digest" token. */
  public static final DsByteString BS_DIGEST = new DsByteString("Digest");
  /** The byte string constant for "domain" token. */
  public static final DsByteString BS_DOMAIN = new DsByteString("domain");
  /** The byte string constant for "duration" token. */
  public static final DsByteString BS_DURATION = new DsByteString("duration");
  /** The byte string constant for "early-only" token. */
  public static final DsByteString BS_EARLY_ONLY = new DsByteString("early-only");
  /** The byte string constant for "expires" token. */
  public static final DsByteString BSL_EXPIRES = new DsByteString("expires");
  /** The byte string constant for "expires" token. */
  public static final DsByteString BS_EXPIRES_VALUE = new DsByteString("expires");
  /** The byte string constant for "from-tag" token. */
  public static final DsByteString BS_FROM_TAG = new DsByteString("from-tag");
  /** The byte string constant for "handling" token. */
  public static final DsByteString BS_HANDLING = new DsByteString("handling");
  /** The byte string constant for "hidden" token. */
  public static final DsByteString BS_HIDDEN = new DsByteString("hidden");
  /** The byte string constant for "id-type" token. */
  public static final DsByteString BS_ID_TYPE = new DsByteString("id-type");
  /** The byte string constant for "ip" token. */
  public static final DsByteString BS_IP = new DsByteString("ip");
  /** The byte string constant for "forkingID" token. */
  public static final DsByteString BS_FORKING_ID = new DsByteString("forkingID");
  /** The byte string constant for "maddr" token. */
  public static final DsByteString BS_MADDR = new DsByteString("maddr");
  /** The byte string constant for "lr" token. */
  public static final DsByteString BS_LR = new DsByteString("lr");
  /** The byte string constant for "comp" token. */
  public static final DsByteString BS_COMP = new DsByteString("comp");
  /** The byte string constant for "sigcomp" token. */
  public static final DsByteString BS_SIGCOMP = new DsByteString("sigcomp");
  /** The byte string constant for "method" token. */
  public static final DsByteString BS_METHOD = new DsByteString("method");
  /** The byte string constant for "nat" token. */
  public static final DsByteString BS_NAT = new DsByteString("nat");
  /** The byte string constant for "npdi" token. */
  public static final DsByteString BS_NPDI = new DsByteString("npdi");
  /** The byte string constant for "nextnonce" token. */
  public static final DsByteString BS_NEXTNONCE = new DsByteString("nextnonce");
  /** The byte string constant for "nc" token. */
  public static final DsByteString BS_NC = new DsByteString("nc");
  /** The byte string constant for "nonce" token. */
  public static final DsByteString BS_NONCE = new DsByteString("nonce");
  /** The byte string constant for "opaque" token. */
  public static final DsByteString BS_OPAQUE = new DsByteString("opaque");
  /** The byte string constant for "optional" token. */
  public static final DsByteString BS_OPTIONAL = new DsByteString("optional");
  /** The byte string constant for "party" token. */
  public static final DsByteString BS_PARTY = new DsByteString("party");
  /** The byte string constant for "privacy" token. */
  public static final DsByteString BSL_PRIVACY = new DsByteString("privacy");
  /** The byte string constant for "purpose" token. */
  public static final DsByteString BS_PURPOSE = new DsByteString("purpose");
  /** The byte string constant for "phone" token. */
  public static final DsByteString BS_PHONE = new DsByteString("phone");
  /** The byte string constant for "qop" token. */
  public static final DsByteString BS_QOP = new DsByteString("qop");
  /** The byte string constant for "realm" token. */
  public static final DsByteString BS_REALM = new DsByteString("realm");
  /** The byte string constant for "received" token. */
  public static final DsByteString BS_RECEIVED = new DsByteString("received");
  /** The byte string constant for "received-port" token. */
  public static final DsByteString BS_RECEIVED_PORT = new DsByteString("received-port");
  /** The byte string constant for "rport" token. */
  public static final DsByteString BS_RPORT = new DsByteString("rport");
  /** The byte string constant for "required" token. */
  public static final DsByteString BS_REQUIRED = new DsByteString("required");
  /** The byte string constant for "response" token. */
  public static final DsByteString BS_RESPONSE = new DsByteString("response");
  /** The byte string constant for "render" token. */
  public static final DsByteString BS_RENDER = new DsByteString("render");
  /** The byte string constant for "rn" token. */
  public static final DsByteString BS_RN = new DsByteString("rn");
  /** The byte string constant for "screen" token. */
  public static final DsByteString BS_SCREEN = new DsByteString("screen");
  /** The byte string constant for "session" token. */
  public static final DsByteString BS_SESSION = new DsByteString("session");
  /** The byte string constant for "stale" token. */
  public static final DsByteString BS_STALE = new DsByteString("stale");
  /** The byte string constant for "tag" token. */
  public static final DsByteString BS_TAG = new DsByteString("tag");
  /** The byte string constant for "to-tag" token. */
  public static final DsByteString BS_TO_TAG = new DsByteString("to-tag");
  /** The byte string constant for "transport" token. */
  public static final DsByteString BS_TRANSPORT = new DsByteString("transport");
  /** The byte string constant for "uri" token. */
  public static final DsByteString BS_URI = new DsByteString("uri");
  /** The byte string constant for "user" token. */
  public static final DsByteString BS_USER = new DsByteString("user");
  /** The byte string constant for "username" token. */
  public static final DsByteString BS_USERNAME = new DsByteString("username");
  /** The byte string constant for "true" token. */
  public static final DsByteString BS_TRUE = new DsByteString("true");
  // CAFFEINE 2.0 DEVELOPMENT - Add new parameter
  /** The byte string constant for "false" token. */
  public static final DsByteString BS_FALSE = new DsByteString("false");
  /** The byte string constant for "refresher" token. */
  public static final DsByteString BS_REFRESHER = new DsByteString("refresher");
  /** The byte string constant for "tsp" token. */
  public static final DsByteString BS_TSP = new DsByteString("tsp");
  /** The byte string constant for "ttl" token. */
  public static final DsByteString BS_TTL = new DsByteString("ttl");
  /** The byte string constant for "isub" token. */
  public static final DsByteString BS_ISUB = new DsByteString("isub");
  /** The byte string constant for "postd" token. */
  public static final DsByteString BS_POSTD = new DsByteString("postd");
  /** The byte string constant for "phone-context" token. */
  public static final DsByteString BS_PHONE_CONTEXT = new DsByteString("phone-context");
  /** The byte string constant for "Content-Length: 0\r\n" token. */
  public static final DsByteString BS_ZERO_CONTENT_LENGTH_EOH =
      new DsByteString("Content-Length: 0\r\n");
  /** The byte string constant for "SIP/2.0 100 Trying\r\n" token. */
  public static final DsByteString BS_100_TRYING = new DsByteString("SIP/2.0 100 Trying\r\n");
  /** The byte string constant for "active" token. */
  public static final DsByteString BS_ACTIVE = new DsByteString("active");
  /** The byte string constant for "pending" token. */
  public static final DsByteString BS_PENDING = new DsByteString("pending");
  /** The byte string constant for "terminated" token. */
  public static final DsByteString BS_TERMINATED = new DsByteString("terminated");
  /** The byte string constant for "reason" token. */
  public static final DsByteString BS_REASON = new DsByteString("reason");
  /** The byte string constant for "retry-after" token. */
  public static final DsByteString BS_RETRY_AFTER_VALUE = new DsByteString("retry-after");
  /** The byte string constant for "retry-after" token. */
  public static final DsByteString BSL_RETRY_AFTER = new DsByteString("retry-after");
  /** The byte string constant for "deactivated" token. */
  public static final DsByteString BS_DEACTIVATED = new DsByteString("deactivated");
  /** The byte string constant for "probation" token. */
  public static final DsByteString BS_PROBATION = new DsByteString("probation");
  /** The byte string constant for "rejected" token. */
  public static final DsByteString BS_REJECTED = new DsByteString("rejected");
  /** The byte string constant for "timeout" token. */
  public static final DsByteString BS_TIMEOUT = new DsByteString("timeout");
  /** The byte string constant for "giveup" token. */
  public static final DsByteString BS_GIVEUP = new DsByteString("giveup");
  /** The byte string constant for "noresource" token. */
  public static final DsByteString BS_NORESOURCE = new DsByteString("noresource");
  /** The byte string constant for "id" token. */
  public static final DsByteString BS_ID = new DsByteString("id");
  /** The byte string constant for "pint" token. */
  public static final DsByteString BS_PINT = new DsByteString("pint");
  /** The byte string constant for "replaces" token. */
  public static final DsByteString BSL_REPLACES = new DsByteString("replaces");
  /** The byte string constant for "away" token. */
  public static final DsByteString BS_AWAY = new DsByteString("away");
  /** The byte string constant for "tel" token. */
  // kevmo 11.03.05 CSCsc33810 move the tel to this class from
  // DsURI and make the constant consists to each other
  public static final DsByteString BS_TEL = new DsByteString("tel");
  /** The byte string constant for "counter" token. */
  public static final DsByteString BS_COUNTER = new DsByteString("counter");
  /** The byte string constant for "deflection" token. */
  public static final DsByteString BS_DEFLECTION = new DsByteString("deflection");
  /** The byte string constant for "do-not-disturb" token. */
  public static final DsByteString BS_DO_NOT_DISTURB = new DsByteString("do-not-disturb");
  /** The byte string constant for "follow-me" token. */
  public static final DsByteString BS_FOLLOW_ME = new DsByteString("follow-me");
  /** The byte string constant for "full" token. */
  public static final DsByteString BS_FULL = new DsByteString("full");
  /** The byte string constant for "limit" token. */
  public static final DsByteString BS_LIMIT = new DsByteString("limit");
  /** The byte string constant for "name" token. */
  public static final DsByteString BS_NAME = new DsByteString("name");
  /** The byte string constant for "no" token. */
  public static final DsByteString BS_NO = new DsByteString("no");
  /** The byte string constant for "no-answer" token. */
  public static final DsByteString BS_NO_ANSWER = new DsByteString("no-answer");
  /** The byte string constant for "off" token. */
  public static final DsByteString BS_OFF = new DsByteString("off");
  /** The byte string constant for "out-of-service" token. */
  public static final DsByteString BS_OUT_OF_SERVICE = new DsByteString("out-of-service");
  /** The byte string constant for "time-of-day" token. */
  public static final DsByteString BS_TIME_OF_DAY = new DsByteString("time-of-day");
  /** The byte string constant for "unavailable" token. */
  public static final DsByteString BS_UNAVAILABLE = new DsByteString("unavailable");
  /** The byte string constant for "unconditional" token. */
  public static final DsByteString BS_UNCONDITIONAL = new DsByteString("unconditional");
  /** The byte string constant for "unknown" token. */
  public static final DsByteString BSL_UNKNOWN = new DsByteString("unknown");
  /** The byte string constant for "user-busy" token. */
  public static final DsByteString BS_USER_BUSY = new DsByteString("user-busy");
  /** The byte string constant for "yes" token. */
  public static final DsByteString BS_YES = new DsByteString("yes");
  /** The byte string constant for "body" token. */
  public static final DsByteString BS_BODY = new DsByteString("body");
  // CAFFEINE 2.0 DEVELOPMENT - Adding new parameters
  /**
   * The byte string constant for "100rel" option tag in Require / Supported / Unsupported headers.
   */
  public static final DsByteString BS_100REL = new DsByteString("100rel");
  /** The byte string constant for "app-name" token. */
  public static final DsByteString BS_APP_NAME = new DsByteString("app-name");
  /** The byte string constant for "boundary" token. */
  public static final DsByteString BS_BOUNDARY = new DsByteString("boundary");
  // MMA - 08.05.2005 - adding additional parameter names/tokens in support of the Reason header
  // (RFC 3326)
  /** The byte string constant for "cause" token. */
  public static final DsByteString BS_CAUSE = new DsByteString("cause");
  /** The byte string constant for "index" token. */
  public static final DsByteString BS_INDEX = new DsByteString("index");

  /** The byte string constant for "IEEE-802.11a" token. */
  public static final DsByteString BS_IEEE_802_11A = new DsByteString("IEEE-802.11a");
  /** The byte string constant for "IEEE-802.11b" token. */
  public static final DsByteString BS_IEEE_802_11B = new DsByteString("IEEE-802.11b");
  /** The byte string constant for "3GPP-GERAN" token. */
  public static final DsByteString BS_3GPP_GERAN = new DsByteString("3GPP-GERAN");
  /** The byte string constant for "3GPP-UTRAN-FDD" token. */
  public static final DsByteString BS_3GPP_UTRAN_FDD = new DsByteString("3GPP-UTRAN-FDD");
  /** The byte string constant for "3GPP-UTRAN-TDD" token. */
  public static final DsByteString BS_3GPP_UTRAN_TDD = new DsByteString("3GPP-UTRAN-TDD");
  /** The byte string constant for "3GPP-CDMA2000" token. */
  public static final DsByteString BS_3GPP_CDMA2000 = new DsByteString("3GPP-CDMA2000");
  /** The byte string constant for "cgi-3gpp" token. */
  public static final DsByteString BS_CGI_3GPP = new DsByteString("cgi-3gpp");
  /** The byte string constant for "utran-cell-id-3gpp" token. */
  public static final DsByteString BS_UTRAN_CELL_ID_3GPP = new DsByteString("utran-cell-id-3gpp");

  /** The byte string constant for "critical" token. */
  public static final DsByteString BS_CRITICAL = new DsByteString("critical");
  /** The byte string constant for "none" token. */
  public static final DsByteString BS_NONE = new DsByteString("none");
  /** The byte string constant for "header" token. */
  public static final DsByteString BS_HEADER = new DsByteString("header");

  /** The byte string constant for "content" token. */
  public static final DsByteString BS_CONTENT = new DsByteString("content");
  /** The byte string constant for "key" token. */
  public static final DsByteString BS_KEY = new DsByteString("key");
  /** The byte string constant for "bcid" token. */
  public static final DsByteString BS_BCID = new DsByteString("bcid");
  /** The byte string constant for "cccid" token. */
  public static final DsByteString BS_CCCID = new DsByteString("cccid");

  /** The byte string constant for "ccf" token. */
  public static final DsByteString BS_CCF = new DsByteString("ccf");
  /** The byte string constant for "ecf" token. */
  public static final DsByteString BS_ECF = new DsByteString("ecf");

  /** The byte string constant for "icid-value" token. */
  public static final DsByteString BS_ICID_VALUE = new DsByteString("icid-value");
  /** The byte string constant for "icid-generated-at" token. */
  public static final DsByteString BS_ICID_GENERATED_AT = new DsByteString("icid-generated-at");
  /** The byte string constant for "orig-ioi" token. */
  public static final DsByteString BS_ORIG_IOI = new DsByteString("orig-ioi");
  /** The byte string constant for "term-ioi" token. */
  public static final DsByteString BS_TERM_IOI = new DsByteString("term-ioi");

  /** The byte string constant for "cid" token. */
  public static final DsByteString BS_CID = new DsByteString("cid");

  /** The byte string constant for "system" token. */
  public static final DsByteString BS_SYSTEM = new DsByteString("system");
  /** The byte string constant for "cpu" token. */
  public static final DsByteString BS_CPU = new DsByteString("cpu");
  /** The byte string constant for "mem" token. */
  public static final DsByteString BS_MEM = new DsByteString("mem");
  /** The byte string constant for "ds0" token. */
  public static final DsByteString BS_DS0 = new DsByteString("ds0");
  /** The byte string constant for "dsp" token. */
  public static final DsByteString BS_DSP = new DsByteString("dsp");
  /** The byte string constant for "mb" token. */
  public static final DsByteString BS_MB = new DsByteString("mb");
  /** The byte string constant for "available" token. */
  public static final DsByteString BS_AVAILABLE = new DsByteString("available");
  /** The byte string constant for "almost-out-of-resource" token. */
  public static final DsByteString BS_ALMOST_OUT_OF_RESOURCE =
      new DsByteString("almost-out-of-resource");
  /** The byte string constant for "total" token. */
  public static final DsByteString BS_TOTAL = new DsByteString("total");
  /** The byte string constant for "used" token. */
  public static final DsByteString BS_USED = new DsByteString("used");

  // Set the P-RTP-Stat header parameters.
  /** The byte string constant for "PS" token. */
  public static final String MEDIA_PACKETS_SENT_COUNT = "PS";
  /** The byte string constant for "OS" token. */
  public static final String MEDIA_PACKETS_SENT_OCTET_COUNT = "OS";
  /** The byte string constant for "PR" token. */
  public static final String MEDIA_PACKETS_RECEIVED_COUNT = "PR";
  /** The byte string constant for "OR" token. */
  public static final String MEDIA_PACKETS_RECEIVED_OCTET_COUNT = "OR";
  /** The byte string constant for "PL" token. */
  public static final String MEDIA_PACKETS_LOSS_COUNT = "PL";
  /** The byte string constant for "JI" token. */
  public static final String MEDIA_PACKETS_JITTER = "JI";
  /** The byte string constant for "LA" token. */
  public static final String MEDIA_PACKETS_ROUND_TRIP_DELAY = "LA";
  /** The byte string constant for "DU" token. */
  public static final String MEDIA_DURATION_IN_SECONDS = "DU";

  //////////////////////////////////////////////////////////////////////////////////////////
  ////    for DsSipTimers implementation use    //////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////

  // timer names
  /** The numerical constant for T1 timer. */
  public static final byte T1 = (byte) 0;
  /** The numerical constant for T2 timer. */
  public static final byte T2 = (byte) 1;
  /** The numerical constant for T3 timer. */
  public static final byte T3 = (byte) 2;
  /** The numerical constant for T4 timer. */
  public static final byte T4 = (byte) 3;
  /** TU1: user defined for edge proxy x state. */
  public static final byte TU1 = (byte) 4;
  /** TU2: user defined for INVITE_CLIENT_TRANS and SERVER_TRANS completion timeout. */
  public static final byte TU2 = (byte) 5;
  /** TU3: timer for "delayed" provisional response. */
  public static final byte TU3 = (byte) 6;
  /** The numerical constant for Client Transaction Tn timer. */
  public static final byte clientTn = (byte) 7;
  /** The numerical constant for Server Transaction Tn timer. */
  public static final byte serverTn = (byte) 8;
  // Note: Tn must be the last entry in this definition list for variable Tx
  // Tn has been deprecated. Use either clientTn or serverTn instead.
  /**
   * The numerical constant for Tn timer.
   *
   * @deprecated use {@link #clientTn} for Client Transaction or use {@link #serverTn} for Server
   *     Transaction.
   */
  public static final byte Tn = (byte) 9;

  // retry count names
  /** The numerical constant name for the Invite Client Transaction retry count. */
  public static final byte INVITE_CLIENT_TRANS = (byte) 0;
  /** The numerical constant name for the Non-Invite Client Transaction retry count. */
  public static final byte CLIENT_TRANS = (byte) 1;
  /** The numerical constant name for the Invite Server Transaction retry count. */
  public static final byte INVITE_SERVER_TRANS = (byte) 2;
  // Note: SERVER_TRANS must be the last entry in this definition list for retry count names
  /** The numerical constant name for the Non-Invite Server Transaction retry count. */
  public static final byte SERVER_TRANS = (byte) 3;

  //////////////////////////////////////////////////////////////////////////////////////////
  ////    end of DsSipTimers implementation use    //////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////////////

  /**
   * The Magic Cookie string that may be present in the branch parameter of VIA header in the SIP
   * message.
   */
  public static final DsByteString BS_MAGIC_COOKIE = new DsByteString("z9hG4bK");

  /** The number of characters in the Magic Cookie. */
  public static final int MAGIC_COOKIE_COUNT = 7;

  /** The byte string constant for "MD5" token. */
  public static final DsByteString BS_MD5 = new DsByteString("MD5");

  /** The byte string constant for "MD5-sess" token. */
  public static final DsByteString BS_MD5_SESS = new DsByteString("MD5-sess");

  /** Constant string that represents the encoding UTF-8. */
  public static final String UTF8 = "UTF-8";

  /** Constant string that represents the MIN FORWARDS Value. */
  public static final int MAX_FORWARDS_MIN_VALUE = 0;

  /** Constant string that represents the MAX FORWARDS Value. */
  public static final int MAX_FORWARDS_MAX_VALUE = 255;
}
