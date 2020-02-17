// Copyright (c) 2005-2009 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

/** This class defines response codes and response reason phrases as specified in RFC 3261. */
public class DsSipResponseCode {
  /*
   * Constant definitons
   */

  // //////// 100's  /////////////////////////////////////
  // /////////////////////////////////////////////////////
  /** The numerical constant for Informational (1xx) response codes class. */
  public static final int DS_INFORMATIONAL = 1;
  /** The string constant for "Informational" string. */
  public static final String DS_STR_INFORMATIONAL = "Informational";
  /** The byte string constant for "Informational" string. */
  public static final DsByteString DS_BS_INFORMATIONAL = new DsByteString("Informational");
  /** The string constant for "Unknown 100 Class Response" string. */
  public static final String DS_STR_UNK_INFORMATIONAL = "Unknown 100 Class Response";
  /** The byte string constant for "Unknown 100 Class Response" string. */
  public static final DsByteString DS_BS_UNK_INFORMATIONAL =
      new DsByteString("Unknown 100 Class Response");

  /** The numerical constant for "100 - Trying" response code. */
  public static final int DS_RESPONSE_TRYING = 100;
  /** The string constant for "Trying" string. */
  public static final String DS_STR_RESPONSE_TRYING = "Trying";
  /** The byte string constant for "Trying" string. */
  public static final DsByteString DS_BS_RESPONSE_TRYING = new DsByteString("Trying");

  /** The numerical constant for "180 - Ringing" response code. */
  public static final int DS_RESPONSE_RINGING = 180;
  /** The string constant for "Ringing" string. */
  public static final String DS_STR_RESPONSE_RINGING = "Ringing";
  /** The byte string constant for "Ringing" string. */
  public static final DsByteString DS_BS_RESPONSE_RINGING = new DsByteString("Ringing");

  /** The numerical constant for "181 - Call Is Being Forwarded" response code. */
  public static final int DS_RESPONSE_CALL_IS_BEING_FORWARDED = 181;
  /** The string constant for "Call Is Being Forwarded" string. */
  public static final String DS_STR_RESPONSE_CALL_IS_BEING_FORWARDED = "Call Is Being Forwarded";
  /** The byte string constant for "Call Is Being Forwarded" string. */
  public static final DsByteString DS_BS_RESPONSE_CALL_IS_BEING_FORWARDED =
      new DsByteString("Call Is Being Forwarded");

  /** The numerical constant for "182 - Queued" response code. */
  public static final int DS_RESPONSE_QUEUED = 182;
  /** The string constant for "Queued" string. */
  public static final String DS_STR_RESPONSE_QUEUED = "Queued";
  /** The byte string constant for "Queued" string. */
  public static final DsByteString DS_BS_RESPONSE_QUEUED = new DsByteString("Queued");

  /**
   * The numerical constant for "183 - Session Progress" response code.
   *
   * @deprecated user the correctly spelled <code>DS_RESPONSE_SESSION_PROGRESS</code> instead.
   */
  public static final int DS_RESPONSE_SESSION_PROGRES = 183;
  // CAFFEINE 2.0 DEVELOPMENT - Add DS_SESSION_PROGRESS constant to DsSipResponseCode. The currently
  // used constant was
  //   probably a typo which missed the last 'S'. The old definition is still kept for backward
  // compatibility.
  public static final int DS_RESPONSE_SESSION_PROGRESS = 183;
  /** The string constant for "Session Progress" string. */
  public static final String DS_STR_SESSION_PROGRESS = "Session Progress";
  /** The byte string constant for "Session Progress" string. */
  public static final DsByteString DS_BS_SESSION_PROGRESS = new DsByteString("Session Progress");

  // //////// 200's  /////////////////////////////////////
  // /////////////////////////////////////////////////////
  /** The numerical constant for Success (2xx) response codes class. */
  public static final int DS_SUCCESS = 2;
  /** The string constant for "Success" string. */
  public static final String DS_STR_SUCCESS = "Success";
  /** The byte string constant for "Success" string. */
  public static final DsByteString DS_BS_SUCCESS = new DsByteString("Success");
  /** The string constant for "Unknown 200 Class Response" string. */
  public static final String DS_STR_UNK_SUCCESS = "Unknown 200 Class Response";
  /** The byte string constant for "Unknown 200 Class Response" string. */
  public static final DsByteString DS_BS_UNK_SUCCESS =
      new DsByteString("Unknown 200 Class Response");

  /** The numerical constant for "200 - Ok" response code. */
  public static final int DS_RESPONSE_OK = 200;
  /** The string constant for "Ok" string. */
  public static final String DS_STR_RESPONSE_OK = "Ok";
  /** The byte string constant for "Ok" string. */
  public static final DsByteString DS_BS_RESPONSE_OK = new DsByteString("Ok");

  /** The numerical constant for "202 - Accepted" response code. */
  public static final int DS_ACCEPTED = 202;
  /** The string constant for "Accepted" string. */
  public static final String DS_STR_ACCEPTED = "Accepted";
  /** The byte string constant for "Accepted" string. */
  public static final DsByteString DS_BS_ACCEPTED = new DsByteString("Accepted");

  /** The numerical constant for "204 - No Notification" response code. */
  public static final int DS_NO_NOTIFICATION = 204;
  /** The string constant for "No Notification" string. */
  public static final String DS_STR_NO_NOTIFICATION = "No Notification";
  /** The byte string constant for "Ok" string. */
  public static final DsByteString DS_BS_NO_NOTIFICATION = new DsByteString("No Notification");

  // //////// 300's  /////////////////////////////////////
  // /////////////////////////////////////////////////////
  /** The numerical constant for Redirection (3xx) response codes class. */
  public static final int DS_REDIRECTION = 3;
  /** The string constant for "Redirection" string. */
  public static final String DS_STR_REDIRECTION = "Redirection";
  /** The byte string constant for "Redirection" string. */
  public static final DsByteString DS_BS_REDIRECTION = new DsByteString("Redirection");
  /** The string constant for "Unknown 300 Class Response" string. */
  public static final String DS_STR_UNK_REDIRECTION = "Unknown 300 Class Response";
  /** The byte string constant for "Unknown 300 Class Response" string. */
  public static final DsByteString DS_BS_UNK_REDIRECTION =
      new DsByteString("Unknown 300 Class Response");

  /** The numerical constant for "300 - Multiple Choices" response code. */
  public static final int DS_RESPONSE_MULTIPLE_CHOICES = 300;
  /** The string constant for "Multiple Choices" string. */
  public static final String DS_STR_RESPONSE_MULTIPLE_CHOICES = "Multiple Choices";
  /** The byte string constant for "Multiple Choices" string. */
  public static final DsByteString DS_BS_RESPONSE_MULTIPLE_CHOICES =
      new DsByteString("Multiple Choices");

  /** The numerical constant for "301 - Moved Permanently" response code. */
  public static final int DS_RESPONSE_MOVED_PERMANENTLY = 301;
  /** The string constant for "Moved Permanently" string. */
  public static final String DS_STR_RESPONSE_MOVED_PERMANENTLY = "Moved Permanently";
  /** The byte string constant for "Moved Permanently" string. */
  public static final DsByteString DS_BS_RESPONSE_MOVED_PERMANENTLY =
      new DsByteString("Moved Permanently");

  /** The numerical constant for "302 - Moved Temporarily" response code. */
  public static final int DS_RESPONSE_MOVED_TEMPORARILY = 302;
  /** The string constant for "Moved Temporarily" string. */
  public static final String DS_STR_RESPONSE_MOVED_TEMPORARILY = "Moved Temporarily";
  /** The byte string constant for "Moved Temporarily" string. */
  public static final DsByteString DS_BS_RESPONSE_MOVED_TEMPORARILY =
      new DsByteString("Moved Temporarily");

  /** The numerical constant for "303 - See Other" response code. */
  public static final int DS_RESPONSE_SEE_OTHER = 303;
  /** The string constant for "See Other" string. */
  public static final String DS_STR_RESPONSE_SEE_OTHER = "See Other";
  /** The byte string constant for "See Other" string. */
  public static final DsByteString DS_BS_RESPONSE_SEE_OTHER = new DsByteString("See Other");

  /** The numerical constant for "305 - Use Proxy" response code. */
  public static final int DS_RESPONSE_USE_PROXY = 305;
  /** The string constant for "Use Proxy" string. */
  public static final String DS_STR_RESPONSE_USE_PROXY = "Use Proxy";
  /** The byte string constant for "Use Proxy" string. */
  public static final DsByteString DS_BS_RESPONSE_USE_PROXY = new DsByteString("Use Proxy");

  /** The numerical constant for "380 - Alternative Service" response code. */
  public static final int DS_RESPONSE_ALTERNATIVE_SERVICE = 380;
  /** The string constant for "Alternative Service" string. */
  public static final String DS_STR_RESPONSE_ALTERNATIVE_SERVICE = "Alternative Service";
  /** The byte string constant for "Alternative Service" string. */
  public static final DsByteString DS_BS_RESPONSE_ALTERNATIVE_SERVICE =
      new DsByteString("Alternative Service");

  // //////// 400's  /////////////////////////////////////
  // /////////////////////////////////////////////////////
  /** The numerical constant for Client Error (4xx) response codes class. */
  public static final int DS_CLIENT_ERROR = 4;
  /** The string constant for "Client Error" string. */
  public static final String DS_STR_CLIENT_ERROR = "Client Error";
  /** The byte string constant for "Client Error" string. */
  public static final DsByteString DS_BS_CLIENT_ERROR = new DsByteString("Client Error");
  /** The string constant for "Unknown 400 Class Response" string. */
  public static final String DS_STR_UNK_CLIENT_ERROR = "Unknown 400 Class Response";
  /** The byte string constant for "Unknown 400 Class Response" string. */
  public static final DsByteString DS_BS_UNK_CLIENT_ERROR =
      new DsByteString("Unknown 400 Class Response");

  /** The numerical constant for "400 - Bad Request" response code. */
  public static final int DS_RESPONSE_BAD_REQUEST = 400;
  /** The string constant for "Bad Request" string. */
  public static final String DS_STR_RESPONSE_BAD_REQUEST = "Bad Request";
  /** The byte string constant for "Bad Request" string. */
  public static final DsByteString DS_BS_RESPONSE_BAD_REQUEST = new DsByteString("Bad Request");

  /** The numerical constant for "401 - Unauthorized" response code. */
  public static final int DS_RESPONSE_UNAUTHORIZED = 401;
  /** The string constant for "Unauthorized" string. */
  public static final String DS_STR_RESPONSE_UNAUTHORIZED = "Unauthorized";
  /** The byte string constant for "Unauthorized" string. */
  public static final DsByteString DS_BS_RESPONSE_UNAUTHORIZED = new DsByteString("Unauthorized");

  /** The numerical constant for "402 - Payment Required" response code. */
  public static final int DS_RESPONSE_PAYMENT_REQUIRED = 402;
  /** The string constant for "Payment Required" string. */
  public static final String DS_STR_RESPONSE_PAYMENT_REQUIRED = "Payment Required";
  /** The byte string constant for "Payment Required" string. */
  public static final DsByteString DS_BS_RESPONSE_PAYMENT_REQUIRED =
      new DsByteString("Payment Required");

  /** The numerical constant for "403 - Forbidden" response code. */
  public static final int DS_RESPONSE_FORBIDDEN = 403;
  /** The string constant for "Forbidden" string. */
  public static final String DS_STR_RESPONSE_FORBIDDEN = "Forbidden";
  /** The byte string constant for "Forbidden" string. */
  public static final DsByteString DS_BS_RESPONSE_FORBIDDEN = new DsByteString("Forbidden");

  /** The numerical constant for "404 - Not Found" response code. */
  public static final int DS_RESPONSE_NOT_FOUND = 404;
  /** The string constant for "Not Found" string. */
  public static final String DS_STR_RESPONSE_NOT_FOUND = "Not Found";
  /** The byte string constant for "Not Found" string. */
  public static final DsByteString DS_BS_RESPONSE_NOT_FOUND = new DsByteString("Not Found");

  /** The numerical constant for "405 - Method Not Allowed" response code. */
  public static final int DS_RESPONSE_METHOD_NOT_ALLOWED = 405;
  /** The string constant for "Method Not Allowed" string. */
  public static final String DS_STR_RESPONSE_METHOD_NOT_ALLOWED = "Method Not Allowed";
  /** The byte string constant for "Method Not Allowed" string. */
  public static final DsByteString DS_BS_RESPONSE_METHOD_NOT_ALLOWED =
      new DsByteString("Method Not Allowed");

  /** The numerical constant for "406 - Not Acceptable" response code. */
  public static final int DS_RESPONSE_NOT_ACCEPTABLE = 406;
  /** The string constant for "Not Acceptable" string. */
  public static final String DS_STR_RESPONSE_NOT_ACCEPTABLE = "Not Acceptable";
  /** The byte string constant for "Not Acceptable" string. */
  public static final DsByteString DS_BS_RESPONSE_NOT_ACCEPTABLE =
      new DsByteString("Not Acceptable");

  /** The numerical constant for "407 - Proxy Authentication Required" response code. */
  public static final int DS_RESPONSE_PROXY_AUTHENTICATION_REQUIRED = 407;
  /** The string constant for "Proxy Authentication Required" string. */
  public static final String DS_STR_RESPONSE_PROXY_AUTHENTICATION_REQUIRED =
      "Proxy Authentication Required";
  /** The byte string constant for "Proxy Authentication Required" string. */
  public static final DsByteString DS_BS_RESPONSE_PROXY_AUTHENTICATION_REQUIRED =
      new DsByteString("Proxy Authentication Required");

  /** The numerical constant for "408 - Request Timeout" response code. */
  public static final int DS_RESPONSE_REQUEST_TIMEOUT = 408;
  /** The string constant for "Request Timeout" string. */
  public static final String DS_STR_RESPONSE_REQUEST_TIMEOUT = "Request Timeout";
  /** The byte string constant for "Request Timeout" string. */
  public static final DsByteString DS_BS_RESPONSE_REQUEST_TIMEOUT =
      new DsByteString("Request Timeout");

  /** The numerical constant for "409 - Conflict" response code. */
  public static final int DS_RESPONSE_CONFLICT = 409;
  /** The string constant for "Conflict" string. */
  public static final String DS_STR_RESPONSE_CONFLICT = "Conflict";
  /** The byte string constant for "Conflict" string. */
  public static final DsByteString DS_BS_RESPONSE_CONFLICT = new DsByteString("Conflict");

  /** The numerical constant for "410 - Gone" response code. */
  public static final int DS_RESPONSE_GONE = 410;
  /** The string constant for "Gone" string. */
  public static final String DS_STR_RESPONSE_GONE = "Gone";
  /** The byte string constant for "Gone" string. */
  public static final DsByteString DS_BS_RESPONSE_GONE = new DsByteString("Gone");

  /** The numerical constant for "411 - Length Required" response code. */
  public static final int DS_RESPONSE_LENGTH_REQUIRED = 411;
  /** The string constant for "Length Required" string. */
  public static final String DS_STR_RESPONSE_LENGTH_REQUIRED = "Length Required";
  /** The byte string constant for "Length Required" string. */
  public static final DsByteString DS_BS_RESPONSE_LENGTH_REQUIRED =
      new DsByteString("Length Required");

  // CAFFEINE 2.0 DEVELOPMENT - check for PUBLISH support in caffeine-1 also three junit testcase
  // files
  /** The numerical constant for "412 - Precondition Failed" response code. */
  public static final int DS_RESPONSE_PRECONDITION_FAILED = 412;
  /** The string constant for "Precondition Failed" string. */
  public static final String DS_STR_RESPONSE_PRECONDITION_FAILED = "Precondition Failed";
  /** The byte string constant for "Precondition Failed" string. */
  public static final DsByteString DS_BS_RESPONSE_PRECONDITION_FAILED =
      new DsByteString("Precondition Failed");

  /** The numerical constant for "413 - Message Body Too Large" response code. */
  public static final int DS_RESPONSE_REQUEST_MESSAGE_BODY_TOO_LARGE = 413;
  /** The string constant for "Message Body Too Large" string. */
  public static final String DS_STR_RESPONSE_REQUEST_MESSAGE_BODY_TOO_LARGE =
      "Message Body Too Large";
  /** The byte string constant for "Message Body Too Large" string. */
  public static final DsByteString DS_BS_RESPONSE_REQUEST_MESSAGE_BODY_TOO_LARGE =
      new DsByteString("Message Body Too Large");

  /** The numerical constant for "414 - Request URI Too Large" response code. */
  public static final int DS_RESPONSE_REQUEST_URI_TOO_LARGE = 414;
  /** The string constant for "Request URI Too Large" string. */
  public static final String DS_STR_RESPONSE_REQUEST_URI_TOO_LARGE = "Request URI Too Large";
  /** The byte string constant for "Request URI Too Large" string. */
  public static final DsByteString DS_BS_RESPONSE_REQUEST_URI_TOO_LARGE =
      new DsByteString("Request URI Too Large");

  /** The numerical constant for "415 - Unsupported Media Type" response code. */
  public static final int DS_RESPONSE_UNSUPPORTED_MEDIA_TYPE = 415;
  /** The string constant for "Unsupported Media Type" string. */
  public static final String DS_STR_RESPONSE_UNSUPPORTED_MEDIA_TYPE = "Unsupported Media Type";
  /** The byte string constant for "Unsupported Media Type" string. */
  public static final DsByteString DS_BS_RESPONSE_UNSUPPORTED_MEDIA_TYPE =
      new DsByteString("Unsupported Media Type");

  /** The numerical constant for "416 - Unsupported URI Scheme" response code. */
  public static final int DS_RESPONSE_UNSUPPORTED_URI_SCHEME = 416;
  /** The string constant for "Unsupported URI Scheme" string. */
  public static final String DS_STR_RESPONSE_UNSUPPORTED_URI_SCHEME = "Unsupported URI Scheme";
  /** The byte string constant for "Unsupported URI Scheme" string. */
  public static final DsByteString DS_BS_RESPONSE_UNSUPPORTED_URI_SCHEME =
      new DsByteString("Unsupported URI Scheme");

  /** The numerical constant for "420 - Bad Extension" response code. */
  public static final int DS_RESPONSE_BAD_EXTENSION = 420;
  /** The string constant for "Bad Extension" string. */
  public static final String DS_STR_RESPONSE_BAD_EXTENSION = "Bad Extension";
  /** The byte string constant for "Bad Extension" string. */
  public static final DsByteString DS_BS_RESPONSE_BAD_EXTENSION = new DsByteString("Bad Extension");

  /** The numerical constant for "423 - Interval Too Brief" response code. */
  public static final int DS_RESPONSE_REGISTRATION_TOO_BRIEF = 423;
  /** The string constant for "Interval Too Brief" string. */
  public static final String DS_STR_RESPONSE_REGISTRATION_TOO_BRIEF = "Interval Too Brief";
  /** The byte string constant for "Registration Too Brief" string. */
  public static final DsByteString DS_BS_RESPONSE_REGISTRATION_TOO_BRIEF =
      new DsByteString("Interval Too Brief");

  /** The numerical constant for "480 - Temporarily Not Available" response code. */
  public static final int DS_RESPONSE_TEMPORARILY_NOT_AVAILABLE = 480;
  /** The string constant for "Temporarily Not Available" string. */
  public static final String DS_STR_RESPONSE_TEMPORARILY_NOT_AVAILABLE =
      "Temporarily Not Available";
  /** The byte string constant for "Temporarily Not Available" string. */
  public static final DsByteString DS_BS_RESPONSE_TEMPORARILY_NOT_AVAILABLE =
      new DsByteString("Temporarily Not Available");

  /** The numerical constant for "481 - Call Leg/Transaction Does Not Exist" response code. */
  public static final int DS_RESPONSE_CALL_LEG_OR_TRANSACTION_DOES_NOT_EXIST = 481;
  /** The string constant for "Call Leg/Transaction Does Not Exist" string. */
  public static final String DS_STR_RESPONSE_CALL_LEG_OR_TRANSACTION_DOES_NOT_EXIST =
      "Call Leg/Transaction Does Not Exist";
  /** The byte string constant for "Call Leg/Transaction Does Not Exist" string. */
  public static final DsByteString DS_BS_RESPONSE_CALL_LEG_OR_TRANSACTION_DOES_NOT_EXIST =
      new DsByteString("Call Leg/Transaction Does Not Exist");

  /** The numerical constant for "482 - Loop Detected" response code. */
  public static final int DS_RESPONSE_LOOP_DETECTED = 482;
  /** The string constant for "Loop Detected" string. */
  public static final String DS_STR_RESPONSE_LOOP_DETECTED = "Loop Detected";
  /** The byte string constant for "Loop Detected" string. */
  public static final DsByteString DS_BS_RESPONSE_LOOP_DETECTED = new DsByteString("Loop Detected");

  /** The numerical constant for "483 - Too Many Hops" response code. */
  public static final int DS_RESPONSE_TOO_MANY_HOPS = 483;
  /** The string constant for "Too Many Hops" string. */
  public static final String DS_STR_RESPONSE_TOO_MANY_HOPS = "Too Many Hops";
  /** The byte string constant for "Too Many Hops" string. */
  public static final DsByteString DS_BS_RESPONSE_TOO_MANY_HOPS = new DsByteString("Too Many Hops");

  /** The numerical constant for "484 - Address Incomplete" response code. */
  public static final int DS_RESPONSE_ADDRESS_INCOMPLETE = 484;
  /** The string constant for "Address Incomplete" string. */
  public static final String DS_STR_RESPONSE_ADDRESS_INCOMPLETE = "Address Incomplete";
  /** The byte string constant for "Address Incomplete" string. */
  public static final DsByteString DS_BS_RESPONSE_ADDRESS_INCOMPLETE =
      new DsByteString("Address Incomplete");

  /** The numerical constant for "485 - Ambiguous" response code. */
  public static final int DS_RESPONSE_AMBIGUOUS = 485;
  /** The string constant for "Ambiguous" string. */
  public static final String DS_STR_RESPONSE_AMBIGUOUS = "Ambiguous";
  /** The byte string constant for "Ambiguous" string. */
  public static final DsByteString DS_BS_RESPONSE_AMBIGUOUS = new DsByteString("Ambiguous");

  /** The numerical constant for "486 - Busy Here" response code. */
  public static final int DS_RESPONSE_BUSY_HERE = 486;
  /** The string constant for "Busy Here" string. */
  public static final String DS_STR_RESPONSE_BUSY_HERE = "Busy Here";
  /** The byte string constant for "Busy Here" string. */
  public static final DsByteString DS_BS_RESPONSE_BUSY_HERE = new DsByteString("Busy Here");

  /** The numerical constant for "487 - Transaction Cancelled" response code. */
  public static final int DS_RESPONSE_TRANSACTION_CANCELLED = 487;
  /** The byte string constant for "487 - Transaction Cancelled" response code. */
  public static final int DS_RESPONSE_REQUEST_TERMINATED = 487;
  /** The string constant for "Transaction Cancelled" string. */
  public static final String DS_STR_RESPONSE_TRANSACTION_CANCELLED = "Request Terminated";
  /** The byte string constant for "Transaction Cancelled" string. */
  public static final DsByteString DS_BS_RESPONSE_TRANSACTION_CANCELLED =
      new DsByteString("Request Terminated");

  /** The numerical constant for "488 - Not Acceptable Here" response code. */
  public static final int DS_RESPONSE_NOT_ACCEPTABLE_HERE = 488;
  /** The string constant for "Not Acceptable Here" string. */
  public static final String DS_STR_RESPONSE_NOT_ACCEPTABLE_HERE = "Not Acceptable Here";
  /** The byte string constant for "Not Acceptable Here" string. */
  public static final DsByteString DS_BS_RESPONSE_NOT_ACCEPTABLE_HERE =
      new DsByteString("Not Acceptable Here");

  /** The numerical constant for "489 - Bad Event" response code. */
  public static final int DS_RESPONSE_BAD_EVENT = 489;
  /** The string constant for "Bad Event" string. */
  public static final String DS_STR_RESPONSE_BAD_EVENT = "Bad Event";
  /** The byte string constant for "Bad Event" string. */
  public static final DsByteString DS_BS_RESPONSE_BAD_EVENT = new DsByteString("Bad Event");

  /** The numerical constant for "491 - Request Pending" response code. */
  public static final int DS_RESPONSE_REQUEST_PENDING = 491;
  /** The string constant for "Request Pending" string. */
  public static final String DS_STR_RESPONSE_REQUEST_PENDING = "Request Pending";
  /** The byte string constant for "Request Pending" string. */
  public static final DsByteString DS_BS_RESPONSE_REQUEST_PENDING =
      new DsByteString("Request Pending");

  /** The numerical constant for "493 - Undecipherable" response code. */
  public static final int DS_RESPONSE_UNDECIPHERABLE = 493;
  /** The string constant for "Undecipherable" string. */
  public static final String DS_STR_RESPONSE_UNDECIPHERABLE = "Undecipherable";
  /** The byte string constant for "Undecipherable" string. */
  public static final DsByteString DS_BS_RESPONSE_UNDECIPHERABLE =
      new DsByteString("Undecipherable");

  // //////// 500's  /////////////////////////////////////
  // /////////////////////////////////////////////////////
  /** The numerical constant for Server Error (5xx) response codes class. */
  public static final int DS_SERVER_ERROR = 5;
  /** The string constant for "Server Error" string. */
  public static final String DS_STR_SERVER_ERROR = "Server Error";
  /** The byte string constant for "Server Error" string. */
  public static final DsByteString DS_BS_SERVER_ERROR = new DsByteString("Server Error");
  /** The string constant for "Unknown 500 Class Response" string. */
  public static final String DS_STR_UNK_SERVER_ERROR = "Unknown 500 Class Response";
  /** The byte string constant for "Unknown 500 Class Response" string. */
  public static final DsByteString DS_BS_UNK_SERVER_ERROR =
      new DsByteString("Unknown 500 Class Response");

  /** The numerical constant for "500 - Internal Server Error" response code. */
  public static final int DS_RESPONSE_INTERNAL_SERVER_ERROR = 500;
  /** The string constant for "Internal Server Error" string. */
  public static final String DS_STR_RESPONSE_INTERNAL_SERVER_ERROR = "Internal Server Error";
  /** The byte string constant for "Internal Server Error" string. */
  public static final DsByteString DS_BS_RESPONSE_INTERNAL_SERVER_ERROR =
      new DsByteString("Internal Server Error");

  /** The numerical constant for "501 - Not Implemented" response code. */
  public static final int DS_RESPONSE_NOT_IMPLEMENTED = 501;
  /** The string constant for "Not Implemented" string. */
  public static final String DS_STR_RESPONSE_NOT_IMPLEMENTED = "Not Implemented";
  /** The byte string constant for "Not Implemented" string. */
  public static final DsByteString DS_BS_RESPONSE_NOT_IMPLEMENTED =
      new DsByteString("Not Implemented");

  /** The numerical constant for "502 - Bad Gateway" response code. */
  public static final int DS_RESPONSE_BAD_GATEWAY = 502;
  /** The string constant for "Bad Gateway" string. */
  public static final String DS_STR_RESPONSE_BAD_GATEWAY = "Bad Gateway";
  /** The byte string constant for "Bad Gateway" string. */
  public static final DsByteString DS_BS_RESPONSE_BAD_GATEWAY = new DsByteString("Bad Gateway");

  /** The numerical constant for "503 - Service Unavailable" response code. */
  public static final int DS_RESPONSE_SERVICE_UNAVAILABLE = 503;
  /** The string constant for "Service Unavailable" string. */
  public static final String DS_STR_RESPONSE_SERVICE_UNAVAILABLE = "Service Unavailable";
  /** The byte string constant for "Service Unavailable" string. */
  public static final DsByteString DS_BS_RESPONSE_SERVICE_UNAVAILABLE =
      new DsByteString("Service Unavailable");

  /** The numerical constant for "504 - Gateway Timeout" response code. */
  public static final int DS_RESPONSE_GATEWAY_TIMEOUT = 504;
  /** The string constant for "Gateway Timeout" string. */
  public static final String DS_STR_RESPONSE_GATEWAY_TIMEOUT = "Gateway Timeout";
  /** The byte string constant for "Gateway Timeout" string. */
  public static final DsByteString DS_BS_RESPONSE_GATEWAY_TIMEOUT =
      new DsByteString("Gateway Timeout");

  /** The numerical constant for "505 - SIP Version Not Supported" response code. */
  public static final int DS_RESPONSE_SIP_VERSION_NOT_SUPPORTED = 505;
  /** The string constant for "SIP Version Not Supported" string. */
  public static final String DS_STR_RESPONSE_SIP_VERSION_NOT_SUPPORTED =
      "SIP Version Not Supported";
  /** The byte string constant for "SIP Version Not Supported" string. */
  public static final DsByteString DS_BS_RESPONSE_SIP_VERSION_NOT_SUPPORTED =
      new DsByteString("SIP Version Not Supported");

  // //////// 600's  /////////////////////////////////////
  // /////////////////////////////////////////////////////
  /** The numerical constant for Global Failure (6xx) response codes class. */
  public static final int DS_GLOBAL_FAILURE = 6;
  /** The string constant for "Global Failure" string. */
  public static final String DS_STR_GLOBAL_FAILURE = "Global Failure";
  /** The byte string constant for "Global Failure" string. */
  public static final DsByteString DS_BS_GLOBAL_FAILURE = new DsByteString("Global Failure");
  /** The string constant for "Unknown 600 Class Response" string. */
  public static final String DS_STR_UNK_GLOBAL_FAILURE = "Unknown 600 Class Response";
  /** The byte string constant for "Unknown 600 Class Response" string. */
  public static final DsByteString DS_BS_UNK_GLOBAL_FAILURE =
      new DsByteString("Unknown 600 Class Response");

  /** The numerical constant for "600 - Busy Everywhere" response code. */
  public static final int DS_RESPONSE_BUSY_EVERYWHERE = 600;
  /** The string constant for "Busy Everywhere" string. */
  public static final String DS_STR_RESPONSE_BUSY_EVERYWHERE = "Busy Everywhere";
  /** The byte string constant for "Busy Everywhere" string. */
  public static final DsByteString DS_BS_RESPONSE_BUSY_EVERYWHERE =
      new DsByteString("Busy Everywhere");

  /** The numerical constant for "603 - Decline" response code. */
  public static final int DS_RESPONSE_DECLINE = 603;
  /** The string constant for "Decline" string. */
  public static final String DS_STR_RESPONSE_DECLINE = "Decline";
  /** The byte string constant for "Decline" string. */
  public static final DsByteString DS_BS_RESPONSE_DECLINE = new DsByteString("Decline");

  /** The numerical constant for "604 - Does Not Exist Anywhere" response code. */
  public static final int DS_RESPONSE_DOES_NOT_EXIST_ANYWHERE = 604;
  /** The string constant for "Does Not Exist Anywhere" string. */
  public static final String DS_STR_RESPONSE_DOES_NOT_EXIST_ANYWHERE = "Does Not Exist Anywhere";
  /** The byte string constant for "Does Not Exist Anywhere" string. */
  public static final DsByteString DS_BS_RESPONSE_DOES_NOT_EXIST_ANYWHERE =
      new DsByteString("Does Not Exist Anywhere");

  /** The numerical constant for "606 - Not Acceptable" response code. */
  public static final int DS_RESPONSE_GLOBAL_NOT_ACCEPTABLE = 606;
  /** The string constant for "Not Acceptable" string. */
  public static final String DS_STR_RESPONSE_GLOBAL_NOT_ACCEPTABLE = "Not Acceptable";
  /** The byte string constant for "Not Acceptable" string. */
  public static final DsByteString DS_BS_RESPONSE_GLOBAL_NOT_ACCEPTABLE =
      new DsByteString("Not Acceptable");

  /** The string constant for "Unknown Response" string. */
  public static final String DS_STR_UNK_RESPONSE = "Unknown Response";
  /** The byte string constant for "Unknown Response" string. */
  public static final DsByteString DS_BS_UNK_RESPONSE = new DsByteString("Unknown Response");

  public static final int MIN_RESPONSE_CODE = 100;
  public static final int MAX_RESPONSE_CODE = 606;

  /** The default constructor. */
  public DsSipResponseCode() {}

  /**
   * Validate the response code in the sip message
   *
   * @param respCode the response code
   * @return true if the respCode is in range
   */
  public static boolean isRespCodeInRange(int respCode) {
    return (respCode >= MIN_RESPONSE_CODE && respCode <= MAX_RESPONSE_CODE);
  }

  /**
   * Retrieves the response phrase for a given response code.
   *
   * @param responseCode the response code.
   * @return the response phrase for the given response code.
   */
  public static final String getReasonPhrase(int responseCode) {

    switch (responseCode) {
        // //////// 100's  /////////////////////////////////////
        // /////////////////////////////////////////////////////
      case (DS_INFORMATIONAL):
        return DS_STR_INFORMATIONAL;

      case (DS_RESPONSE_TRYING):
        return DS_STR_RESPONSE_TRYING;

      case (DS_RESPONSE_RINGING):
        return DS_STR_RESPONSE_RINGING;

      case (DS_RESPONSE_CALL_IS_BEING_FORWARDED):
        return DS_STR_RESPONSE_CALL_IS_BEING_FORWARDED;

      case (DS_RESPONSE_QUEUED):
        return DS_STR_RESPONSE_QUEUED;

      case (DS_RESPONSE_SESSION_PROGRESS):
        return DS_STR_SESSION_PROGRESS;

        // //////// 200's  /////////////////////////////////////
        // /////////////////////////////////////////////////////
      case (DS_SUCCESS):
        return DS_STR_SUCCESS;

      case (DS_RESPONSE_OK):
        return DS_STR_RESPONSE_OK;

      case (DS_ACCEPTED):
        return DS_STR_ACCEPTED;

      case (DS_NO_NOTIFICATION):
        return DS_STR_NO_NOTIFICATION;

        // //////// 300's  /////////////////////////////////////
        // /////////////////////////////////////////////////////
      case (DS_REDIRECTION):
        return DS_STR_REDIRECTION;

      case (DS_RESPONSE_MULTIPLE_CHOICES):
        return DS_STR_RESPONSE_MULTIPLE_CHOICES;

      case (DS_RESPONSE_MOVED_PERMANENTLY):
        return DS_STR_RESPONSE_MOVED_PERMANENTLY;

      case (DS_RESPONSE_MOVED_TEMPORARILY):
        return DS_STR_RESPONSE_MOVED_TEMPORARILY;

      case (DS_RESPONSE_SEE_OTHER):
        return DS_STR_RESPONSE_SEE_OTHER;

      case (DS_RESPONSE_USE_PROXY):
        return DS_STR_RESPONSE_USE_PROXY;

      case (DS_RESPONSE_ALTERNATIVE_SERVICE):
        return DS_STR_RESPONSE_ALTERNATIVE_SERVICE;

        // //////// 400's  /////////////////////////////////////
        // /////////////////////////////////////////////////////
      case (DS_CLIENT_ERROR):
        return DS_STR_CLIENT_ERROR;

      case (DS_RESPONSE_BAD_REQUEST):
        return DS_STR_RESPONSE_BAD_REQUEST;

      case (DS_RESPONSE_UNAUTHORIZED):
        return DS_STR_RESPONSE_UNAUTHORIZED;

      case (DS_RESPONSE_PAYMENT_REQUIRED):
        return DS_STR_RESPONSE_PAYMENT_REQUIRED;

      case (DS_RESPONSE_FORBIDDEN):
        return DS_STR_RESPONSE_FORBIDDEN;

      case (DS_RESPONSE_NOT_FOUND):
        return DS_STR_RESPONSE_NOT_FOUND;

      case (DS_RESPONSE_METHOD_NOT_ALLOWED):
        return DS_STR_RESPONSE_METHOD_NOT_ALLOWED;

      case (DS_RESPONSE_NOT_ACCEPTABLE):
        return DS_STR_RESPONSE_NOT_ACCEPTABLE;

      case (DS_RESPONSE_PROXY_AUTHENTICATION_REQUIRED):
        return DS_STR_RESPONSE_PROXY_AUTHENTICATION_REQUIRED;

      case (DS_RESPONSE_REQUEST_TIMEOUT):
        return DS_STR_RESPONSE_REQUEST_TIMEOUT;

      case (DS_RESPONSE_CONFLICT):
        return DS_STR_RESPONSE_CONFLICT;

      case (DS_RESPONSE_GONE):
        return DS_STR_RESPONSE_GONE;

      case (DS_RESPONSE_LENGTH_REQUIRED):
        return DS_STR_RESPONSE_LENGTH_REQUIRED;

        // CAFFEINE 2.0 DEVELOPMENT - return string and DsByteString on precondition failed
        //    in getReasonPhrase and getBsReasonPhrase calls
      case (DS_RESPONSE_PRECONDITION_FAILED):
        return DS_STR_RESPONSE_PRECONDITION_FAILED;

      case (DS_RESPONSE_REQUEST_MESSAGE_BODY_TOO_LARGE):
        return DS_STR_RESPONSE_REQUEST_MESSAGE_BODY_TOO_LARGE;

      case (DS_RESPONSE_REQUEST_URI_TOO_LARGE):
        return DS_STR_RESPONSE_REQUEST_URI_TOO_LARGE;

      case (DS_RESPONSE_UNSUPPORTED_MEDIA_TYPE):
        return DS_STR_RESPONSE_UNSUPPORTED_MEDIA_TYPE;

      case (DS_RESPONSE_UNSUPPORTED_URI_SCHEME):
        return DS_STR_RESPONSE_UNSUPPORTED_URI_SCHEME;

      case (DS_RESPONSE_BAD_EXTENSION):
        return DS_STR_RESPONSE_BAD_EXTENSION;

      case (DS_RESPONSE_REGISTRATION_TOO_BRIEF):
        return DS_STR_RESPONSE_REGISTRATION_TOO_BRIEF;

      case (DS_RESPONSE_TEMPORARILY_NOT_AVAILABLE):
        return DS_STR_RESPONSE_TEMPORARILY_NOT_AVAILABLE;

      case (DS_RESPONSE_CALL_LEG_OR_TRANSACTION_DOES_NOT_EXIST):
        return DS_STR_RESPONSE_CALL_LEG_OR_TRANSACTION_DOES_NOT_EXIST;

      case (DS_RESPONSE_LOOP_DETECTED):
        return DS_STR_RESPONSE_LOOP_DETECTED;

      case (DS_RESPONSE_TOO_MANY_HOPS):
        return DS_STR_RESPONSE_TOO_MANY_HOPS;

      case (DS_RESPONSE_ADDRESS_INCOMPLETE):
        return DS_STR_RESPONSE_ADDRESS_INCOMPLETE;

      case (DS_RESPONSE_AMBIGUOUS):
        return DS_STR_RESPONSE_AMBIGUOUS;

      case (DS_RESPONSE_BUSY_HERE):
        return DS_STR_RESPONSE_BUSY_HERE;

      case (DS_RESPONSE_TRANSACTION_CANCELLED):
        return DS_STR_RESPONSE_TRANSACTION_CANCELLED;

      case (DS_RESPONSE_NOT_ACCEPTABLE_HERE):
        return DS_STR_RESPONSE_NOT_ACCEPTABLE_HERE;

      case (DS_RESPONSE_BAD_EVENT):
        return DS_STR_RESPONSE_BAD_EVENT;

      case (DS_RESPONSE_REQUEST_PENDING):
        return DS_STR_RESPONSE_REQUEST_PENDING;

      case (DS_RESPONSE_UNDECIPHERABLE):
        return DS_STR_RESPONSE_UNDECIPHERABLE;

        // //////// 500's  /////////////////////////////////////
        // /////////////////////////////////////////////////////
      case (DS_SERVER_ERROR):
        return DS_STR_SERVER_ERROR;

      case (DS_RESPONSE_INTERNAL_SERVER_ERROR):
        return DS_STR_RESPONSE_INTERNAL_SERVER_ERROR;

      case (DS_RESPONSE_NOT_IMPLEMENTED):
        return DS_STR_RESPONSE_NOT_IMPLEMENTED;

      case (DS_RESPONSE_BAD_GATEWAY):
        return DS_STR_RESPONSE_BAD_GATEWAY;

      case (DS_RESPONSE_SERVICE_UNAVAILABLE):
        return DS_STR_RESPONSE_SERVICE_UNAVAILABLE;

      case (DS_RESPONSE_GATEWAY_TIMEOUT):
        return DS_STR_RESPONSE_GATEWAY_TIMEOUT;

      case (DS_RESPONSE_SIP_VERSION_NOT_SUPPORTED):
        return DS_STR_RESPONSE_SIP_VERSION_NOT_SUPPORTED;

        // //////// 600's  /////////////////////////////////////
        // /////////////////////////////////////////////////////
      case (DS_GLOBAL_FAILURE):
        return DS_STR_GLOBAL_FAILURE;

      case (DS_RESPONSE_BUSY_EVERYWHERE):
        return DS_STR_RESPONSE_BUSY_EVERYWHERE;

      case (DS_RESPONSE_DECLINE):
        return DS_STR_RESPONSE_DECLINE;

      case (DS_RESPONSE_DOES_NOT_EXIST_ANYWHERE):
        return DS_STR_RESPONSE_DOES_NOT_EXIST_ANYWHERE;

      case (DS_RESPONSE_GLOBAL_NOT_ACCEPTABLE):
        return DS_STR_RESPONSE_GLOBAL_NOT_ACCEPTABLE;

      default:
        switch (responseCode / 100) {
          case DS_INFORMATIONAL:
            return DS_STR_UNK_INFORMATIONAL;

          case DS_SUCCESS:
            return DS_STR_UNK_SUCCESS;

          case DS_REDIRECTION:
            return DS_STR_UNK_REDIRECTION;

          case DS_CLIENT_ERROR:
            return DS_STR_UNK_CLIENT_ERROR;

          case DS_SERVER_ERROR:
            return DS_STR_UNK_SERVER_ERROR;

          case DS_GLOBAL_FAILURE:
            return DS_STR_UNK_GLOBAL_FAILURE;

          default:
            return DS_STR_UNK_RESPONSE;
        }
    }
  }

  /**
   * Retrieves the response phrase for a given response code.
   *
   * @param responseCode the response code.
   * @return the response phrase for the given response code.
   */
  public static final DsByteString getBSReasonPhrase(int responseCode) {
    switch (responseCode) {
        // //////// 100's  /////////////////////////////////////
        // /////////////////////////////////////////////////////
      case (DS_INFORMATIONAL):
        return DS_BS_INFORMATIONAL;

      case (DS_RESPONSE_TRYING):
        return DS_BS_RESPONSE_TRYING;

      case (DS_RESPONSE_RINGING):
        return DS_BS_RESPONSE_RINGING;

      case (DS_RESPONSE_CALL_IS_BEING_FORWARDED):
        return DS_BS_RESPONSE_CALL_IS_BEING_FORWARDED;

      case (DS_RESPONSE_QUEUED):
        return DS_BS_RESPONSE_QUEUED;

      case (DS_RESPONSE_SESSION_PROGRESS):
        return DS_BS_SESSION_PROGRESS;

        // //////// 200's  /////////////////////////////////////
        // /////////////////////////////////////////////////////
      case (DS_SUCCESS):
        return DS_BS_SUCCESS;

      case (DS_RESPONSE_OK):
        return DS_BS_RESPONSE_OK;

      case (DS_ACCEPTED):
        return DS_BS_ACCEPTED;

      case (DS_NO_NOTIFICATION):
        return DS_BS_NO_NOTIFICATION;

        // //////// 300's  /////////////////////////////////////
        // /////////////////////////////////////////////////////
      case (DS_REDIRECTION):
        return DS_BS_REDIRECTION;

      case (DS_RESPONSE_MULTIPLE_CHOICES):
        return DS_BS_RESPONSE_MULTIPLE_CHOICES;

      case (DS_RESPONSE_MOVED_PERMANENTLY):
        return DS_BS_RESPONSE_MOVED_PERMANENTLY;

      case (DS_RESPONSE_MOVED_TEMPORARILY):
        return DS_BS_RESPONSE_MOVED_TEMPORARILY;

      case (DS_RESPONSE_SEE_OTHER):
        return DS_BS_RESPONSE_SEE_OTHER;

      case (DS_RESPONSE_USE_PROXY):
        return DS_BS_RESPONSE_USE_PROXY;

      case (DS_RESPONSE_ALTERNATIVE_SERVICE):
        return DS_BS_RESPONSE_ALTERNATIVE_SERVICE;

        // //////// 400's  /////////////////////////////////////
        // /////////////////////////////////////////////////////
      case (DS_CLIENT_ERROR):
        return DS_BS_CLIENT_ERROR;

      case (DS_RESPONSE_BAD_REQUEST):
        return DS_BS_RESPONSE_BAD_REQUEST;

      case (DS_RESPONSE_UNAUTHORIZED):
        return DS_BS_RESPONSE_UNAUTHORIZED;

      case (DS_RESPONSE_PAYMENT_REQUIRED):
        return DS_BS_RESPONSE_PAYMENT_REQUIRED;

      case (DS_RESPONSE_FORBIDDEN):
        return DS_BS_RESPONSE_FORBIDDEN;

      case (DS_RESPONSE_NOT_FOUND):
        return DS_BS_RESPONSE_NOT_FOUND;

      case (DS_RESPONSE_METHOD_NOT_ALLOWED):
        return DS_BS_RESPONSE_METHOD_NOT_ALLOWED;

      case (DS_RESPONSE_NOT_ACCEPTABLE):
        return DS_BS_RESPONSE_NOT_ACCEPTABLE;

      case (DS_RESPONSE_PROXY_AUTHENTICATION_REQUIRED):
        return DS_BS_RESPONSE_PROXY_AUTHENTICATION_REQUIRED;

      case (DS_RESPONSE_REQUEST_TIMEOUT):
        return DS_BS_RESPONSE_REQUEST_TIMEOUT;

      case (DS_RESPONSE_CONFLICT):
        return DS_BS_RESPONSE_CONFLICT;

      case (DS_RESPONSE_GONE):
        return DS_BS_RESPONSE_GONE;

      case (DS_RESPONSE_LENGTH_REQUIRED):
        return DS_BS_RESPONSE_LENGTH_REQUIRED;

        // CAFFEINE 2.0 DEVELOPMENT - return string and DsByteString on precondition failed
        //    in getReasonPhrase and getBsReasonPhrase calls
      case (DS_RESPONSE_PRECONDITION_FAILED):
        return DS_BS_RESPONSE_PRECONDITION_FAILED;

      case (DS_RESPONSE_REQUEST_MESSAGE_BODY_TOO_LARGE):
        return DS_BS_RESPONSE_REQUEST_MESSAGE_BODY_TOO_LARGE;

      case (DS_RESPONSE_REQUEST_URI_TOO_LARGE):
        return DS_BS_RESPONSE_REQUEST_URI_TOO_LARGE;

      case (DS_RESPONSE_UNSUPPORTED_MEDIA_TYPE):
        return DS_BS_RESPONSE_UNSUPPORTED_MEDIA_TYPE;

      case (DS_RESPONSE_UNSUPPORTED_URI_SCHEME):
        return DS_BS_RESPONSE_UNSUPPORTED_URI_SCHEME;

      case (DS_RESPONSE_BAD_EXTENSION):
        return DS_BS_RESPONSE_BAD_EXTENSION;

      case (DS_RESPONSE_REGISTRATION_TOO_BRIEF):
        return DS_BS_RESPONSE_REGISTRATION_TOO_BRIEF;

      case (DS_RESPONSE_TEMPORARILY_NOT_AVAILABLE):
        return DS_BS_RESPONSE_TEMPORARILY_NOT_AVAILABLE;

      case (DS_RESPONSE_CALL_LEG_OR_TRANSACTION_DOES_NOT_EXIST):
        return DS_BS_RESPONSE_CALL_LEG_OR_TRANSACTION_DOES_NOT_EXIST;

      case (DS_RESPONSE_LOOP_DETECTED):
        return DS_BS_RESPONSE_LOOP_DETECTED;

      case (DS_RESPONSE_TOO_MANY_HOPS):
        return DS_BS_RESPONSE_TOO_MANY_HOPS;

      case (DS_RESPONSE_ADDRESS_INCOMPLETE):
        return DS_BS_RESPONSE_ADDRESS_INCOMPLETE;

      case (DS_RESPONSE_AMBIGUOUS):
        return DS_BS_RESPONSE_AMBIGUOUS;

      case (DS_RESPONSE_BUSY_HERE):
        return DS_BS_RESPONSE_BUSY_HERE;

      case (DS_RESPONSE_TRANSACTION_CANCELLED):
        return DS_BS_RESPONSE_TRANSACTION_CANCELLED;

      case (DS_RESPONSE_NOT_ACCEPTABLE_HERE):
        return DS_BS_RESPONSE_NOT_ACCEPTABLE_HERE;

      case (DS_RESPONSE_BAD_EVENT):
        return DS_BS_RESPONSE_BAD_EVENT;

      case (DS_RESPONSE_REQUEST_PENDING):
        return DS_BS_RESPONSE_REQUEST_PENDING;

      case (DS_RESPONSE_UNDECIPHERABLE):
        return DS_BS_RESPONSE_UNDECIPHERABLE;

        // //////// 500's  /////////////////////////////////////
        // /////////////////////////////////////////////////////
      case (DS_SERVER_ERROR):
        return DS_BS_SERVER_ERROR;

      case (DS_RESPONSE_INTERNAL_SERVER_ERROR):
        return DS_BS_RESPONSE_INTERNAL_SERVER_ERROR;

      case (DS_RESPONSE_NOT_IMPLEMENTED):
        return DS_BS_RESPONSE_NOT_IMPLEMENTED;

      case (DS_RESPONSE_BAD_GATEWAY):
        return DS_BS_RESPONSE_BAD_GATEWAY;

      case (DS_RESPONSE_SERVICE_UNAVAILABLE):
        return DS_BS_RESPONSE_SERVICE_UNAVAILABLE;

      case (DS_RESPONSE_GATEWAY_TIMEOUT):
        return DS_BS_RESPONSE_GATEWAY_TIMEOUT;

      case (DS_RESPONSE_SIP_VERSION_NOT_SUPPORTED):
        return DS_BS_RESPONSE_SIP_VERSION_NOT_SUPPORTED;

        // //////// 600's  /////////////////////////////////////
        // /////////////////////////////////////////////////////
      case (DS_GLOBAL_FAILURE):
        return DS_BS_GLOBAL_FAILURE;

      case (DS_RESPONSE_BUSY_EVERYWHERE):
        return DS_BS_RESPONSE_BUSY_EVERYWHERE;

      case (DS_RESPONSE_DECLINE):
        return DS_BS_RESPONSE_DECLINE;

      case (DS_RESPONSE_DOES_NOT_EXIST_ANYWHERE):
        return DS_BS_RESPONSE_DOES_NOT_EXIST_ANYWHERE;

      case (DS_RESPONSE_GLOBAL_NOT_ACCEPTABLE):
        return DS_BS_RESPONSE_GLOBAL_NOT_ACCEPTABLE;

      default:
        switch (responseCode / 100) {
          case DS_INFORMATIONAL:
            return DS_BS_UNK_INFORMATIONAL;

          case DS_SUCCESS:
            return DS_BS_UNK_SUCCESS;

          case DS_REDIRECTION:
            return DS_BS_UNK_REDIRECTION;

          case DS_CLIENT_ERROR:
            return DS_BS_UNK_CLIENT_ERROR;

          case DS_SERVER_ERROR:
            return DS_BS_UNK_SERVER_ERROR;

          case DS_GLOBAL_FAILURE:
            return DS_BS_UNK_GLOBAL_FAILURE;

          default:
            return DS_BS_UNK_RESPONSE;
        }
    }
  }
}

//  public static String getReasonPhrase(int responseCode)
//     {
//         if (responseCode > 99)
//         {
//             int responseCodeClass = responseCode / 100;

//             switch (responseCodeClass)
//             {

//                 case 1:
//                     switch (responseCode)
//                     {

//                         case 100:
//                             return "Trying";

//                         case 180:
//                             return "Ringing";

//                         case 181:
//                             return "Call Is Being Forwarded";

//                         case 182:
//                             return "Queued";

//                         default:
//                             return "Unknown 100 Class Response";
//                     }

//                 case 2:
//                     switch (responseCode)
//                     {

//                         case 200:
//                             return "Ok";

//                         case 202:
//                             return "Accepted";

//                         default:
//                             return "Unknown 200 Class Response";
//                     }

//                 case 3:
//                     switch (responseCode)
//                     {

//                         case 300:
//                             return "Multiple Choices";

//                         case 301:
//                             return "Moved Permanently";

//                         case 302:
//                             return "Moved Temporarily";

//                         case 303:
//                             return "See Other";

//                         case 305:
//                             return "Use Proxy";

//                         case 380:
//                             return "Alternative Service";

//                         default:
//                             return "Unknown 300 Class Response";
//                     }

//                 case 4:
//                     switch (responseCode)
//                     {

//                         case 400:
//                             return "Bad Request";

//                         case 401:
//                             return "Unauthorized";

//                         case 402:
//                             return "Payment Required";

//                         case 403:
//                             return "Forbidden";

//                         case 404:
//                             return "Not Found";

//                         case 405:
//                             return "Method Not Allowed";

//                         case 406:
//                             return "Not Acceptable";

//                         case 407:
//                             return "Proxy Authentication Required";

//                         case 408:
//                             return "Request Timeout";

//                         case 409:
//                             return "Conflict";

//                         case 410:
//                             return "Gone";

//                         case 411:
//                             return "Length Required";

//                         case 413:
//                             return "Request  Message Body Too Large";

//                         case 414:
//                             return "Request-URI Too Large";

//                         case 415:
//                             return "Unsupported Media Type";

//                         case 420:
//                             return "Bad Extension";

//                         case 480:
//                             return "Temporarily not available";

//                         case 481:
//                             return "Call Leg/Transaction Does Not Exist";

//                         case 482:
//                             return "Loop Detected";

//                         case 483:
//                             return "Too Many Hops";

//                         case 484:
//                             return "Address Incomplete";

//                         case 485:
//                             return "Ambiguous";

//                         case 486:
//                             return "Busy Here";

//                         case 487:
//                             return "Transaction Cancelled";

//                         case 489:
//                             return "Bad Event";

//                         default:
//                             return "Unknown 400 Class Response";
//                     }

//                 case 5:
//                     switch (responseCode)
//                     {

//                         case 500:
//                             return "Internal Server Error";

//                         case 501:
//                             return "Not Implemented";

//                         case 502:
//                             return "Bad Gateway";

//                         case 503:
//                             return "Service Unavailable";

//                         case 504:
//                             return "Gateway Timeout";

//                         case 505:
//                             return "SIP Version not supported";

//                         default:
//                             return "Unknown 500 Class Response";
//                     }

//                 case 6:
//                     switch (responseCode)
//                     {

//                         case 600:
//                             return "Busy Everywhere";

//                         case 603:
//                             return "Decline";

//                         case 604:
//                             return "Does not exist anywhere";

//                         case 606:
//                             return "Not Acceptable";

//                         default:
//                             return "Unknown 600 Class Response";

//                     }

//                 default:
//                     return "Unknown Response";
//             }
//         }
//         else
//         {
//             switch (responseCode)
//             {

//                 case 1:
//                     return "Informational";

//                 case 2:
//                     return "Success";

//                 case 3:
//                     return "Redirection";

//                 case 4:
//                     return "Client-Error";

//                 case 5:
//                     return "Server-Error";

//                 case 6:
//                     return "Global-Failure";

//                 default:
//                     return "Unknown Response";
//             }
//         }
//     }
