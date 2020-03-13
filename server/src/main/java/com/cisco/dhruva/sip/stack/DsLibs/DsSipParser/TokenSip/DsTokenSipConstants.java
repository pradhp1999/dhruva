// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipConstants;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/** This class contains some constants used in DsSipObject library. */
@SuppressFBWarnings
public interface DsTokenSipConstants extends DsSipConstants {

  // fixed format SIP strings (highly optimized tokens)

  // todo this is a new one.  Check it out
  public static final byte TOKEN_SIP_PREFIX1 = (byte) 0x80;
  public static final byte TOKEN_SIP_PREFIX2 = (byte) 0x81;

  // High level tokens table
  public static final byte TOKEN_SIP_NULL = 0x00;
  public static final byte TOKEN_SIP_SIP_DICTIONARY_1 = 0x01;
  public static final byte TOKEN_SIP_SIP_DICTIONARY_2 = 0x02;
  public static final byte TOKEN_SIP_LOCAL_DICTIONARY = 0x03;
  public static final byte TOKEN_SIP_MESSAGE_DICTIONARY = 0x04;
  public static final byte TOKEN_SIP_UNSIGNED_SHORT = 0x05;
  public static final byte TOKEN_SIP_UNSIGNED_LONG = 0x06;
  public static final byte TOKEN_SIP_MESSAGE_BODY_CONTENT = 0x07;
  public static final byte TOKEN_SIP_KNOWN_HEADER = 0x08;
  public static final byte TOKEN_SIP_UNKNOWN_HEADER = 0x09;
  public static final byte TOKEN_SIP_CRLF = 0x0c;
  public static final byte TOKEN_SIP_CRLF_CRLF = 0x0d;
  public static final byte TOKEN_SIP_TWO_PIECE_USER_SEPERATOR = 0x0e;
  public static final byte TOKEN_SIP_HEX_STRING = 0x0f;
  public static final byte TOKEN_SIP_URI_LOW = 0x10;
  public static final byte TOKEN_SIP_URI_HIGH = 0x1f;
  public static final byte TOKEN_SIP_ASCII_CHAR_LOW = 0x20;
  public static final byte TOKEN_SIP_ASCII_CHAR_HIGH = 0x3f;
  public static final byte TOKEN_SIP_PARAMETER_LOW = 0x40;
  public static final byte TOKEN_SIP_PARAMETER_HIGH = 0x4f;

  public static final byte[] TOKEN_SIP_NULL_BYTES = {(byte) TOKEN_SIP_NULL};
  public static final byte[] TOKEN_SIP_SIP_DICTIONARY_1_BYTES = {(byte) TOKEN_SIP_SIP_DICTIONARY_1};
  public static final byte[] TOKEN_SIP_SIP_DICTIONARY_2_BYTES = {(byte) TOKEN_SIP_SIP_DICTIONARY_2};
  public static final byte[] TOKEN_SIP_LOCAL_DICTIONARY_BYTES = {(byte) TOKEN_SIP_LOCAL_DICTIONARY};
  public static final byte[] TOKEN_SIP_MESSAGE_DICTIONARY_BYTES = {
    (byte) TOKEN_SIP_MESSAGE_DICTIONARY
  };
  public static final byte[] TOKEN_SIP_UNSIGNED_SHORT_BYTES = {(byte) TOKEN_SIP_UNSIGNED_SHORT};
  public static final byte[] TOKEN_SIP_UNSIGNED_LONG_BYTES = {(byte) TOKEN_SIP_UNSIGNED_LONG};
  public static final byte[] TOKEN_SIP_MESSAGE_BODY_CONTENT_BYTES = {
    (byte) TOKEN_SIP_MESSAGE_BODY_CONTENT
  };
  public static final byte[] TOKEN_SIP_KNOWN_HEADER_BYTES = {(byte) TOKEN_SIP_KNOWN_HEADER};
  public static final byte[] TOKEN_SIP_UNKNOWN_HEADER_BYTES = {(byte) TOKEN_SIP_UNKNOWN_HEADER};
  public static final byte[] TOKEN_SIP_CRLF_BYTES = {(byte) TOKEN_SIP_CRLF};
  public static final byte[] TOKEN_SIP_CRLF_CRLF_BYTES = {(byte) TOKEN_SIP_CRLF_CRLF};
  public static final byte[] TOKEN_SIP_TWO_PIECE_USER_SEPERATOR_BYTES = {
    (byte) TOKEN_SIP_TWO_PIECE_USER_SEPERATOR
  };
  public static final byte[] TOKEN_SIP_HEX_STRING_BYTES = {(byte) TOKEN_SIP_HEX_STRING};
  public static final byte[] TOKEN_SIP_URI_LOW_BYTES = {(byte) TOKEN_SIP_URI_LOW};
  public static final byte[] TOKEN_SIP_URI_HIGH_BYTES = {(byte) TOKEN_SIP_URI_HIGH};
  public static final byte[] TOKEN_SIP_ASCII_CHAR_LOW_BYTES = {(byte) TOKEN_SIP_ASCII_CHAR_LOW};
  public static final byte[] TOKEN_SIP_ASCII_CHAR_HIGH_BYTES = {(byte) TOKEN_SIP_ASCII_CHAR_HIGH};
  public static final byte[] TOKEN_SIP_PARAMETER_LOW_BYTES = {(byte) TOKEN_SIP_PARAMETER_LOW};
  public static final byte[] TOKEN_SIP_PARAMETER_HIGH_BYTES = {(byte) TOKEN_SIP_PARAMETER_HIGH};

  public static final byte TOKEN_SIP_FIXED_FORMAT_REQUEST_START_LINE = 0x60;
  public static final byte TOKEN_SIP_FIXED_FORMAT_RESPONSE_START_LINE = 0x61;
  public static final byte TOKEN_SIP_FIXED_FORMAT_INVITE_START_LINE = 0x62;

  public static final byte TOKEN_SIP_FIXED_FORMAT_HEADER_MIN = 0x63;
  public static final byte TOKEN_SIP_FIXED_FORMAT_HEADER_MAX = 0x6b;
  // todo 7f?

  public static final byte TOKEN_SIP_FIXED_FORMAT_VIA_HEADER = 0x63;
  public static final byte TOKEN_SIP_FIXED_FORMAT_TO_HEADER = 0x64;
  public static final byte TOKEN_SIP_FIXED_FORMAT_FROM_HEADER = 0x65;
  public static final byte TOKEN_SIP_FIXED_FORMAT_CSEQ_HEADER = 0x66;
  public static final byte TOKEN_SIP_FIXED_FORMAT_CALLID1_HEADER = 0x67;
  public static final byte TOKEN_SIP_FIXED_FORMAT_CALLID2_HEADER = 0x68;
  public static final byte TOKEN_SIP_FIXED_FORMAT_CONTACT_HEADER = 0x69;
  public static final byte TOKEN_SIP_FIXED_FORMAT_AUTHENTICATION_HEADER = 0x6a;
  public static final byte TOKEN_SIP_FIXED_FORMAT_AUTHORIZATION_HEADER = 0x6b;
  public static final byte TOKEN_SIP_FIXED_FORMAT_RECORD_ROUTE_HEADER = 0x6c;
  public static final byte TOKEN_SIP_FIXED_FORMAT_ROUTE_HEADER = 0x6d;

  public static final byte TOKEN_SIP_FIXED_FORMAT_AUTHORIZATION_VARIANT_HEADER = 0x6f;

  // version data static references.  Used for optimized event firing in message parsing
  public static final int s_ProtocolStart = 0;
  public static final int s_ProtocolLength = 3;
  public static final int s_VersionStart = BS_SIP_VERSION.indexOf('/') + 1;
  public static final int s_VersionLength = 3;
  public static final int s_MajorVersionStart = BS_SIP_VERSION.indexOf('/') + 1;
  public static final int s_MajorVersionLength = 1;
  public static final int s_MinorVersionStart = BS_SIP_VERSION.indexOf('.') + 1;
  public static final int s_MinorVersionLength = 1;
  public static final byte[] s_ProtocolBytes = BS_SIP_VERSION.toByteArray();

  public static final int s_UDP_START = 0;
  public static final int s_UDP_LENGTH = 3;
  public static final byte[] s_UDB_BYTES = ("UDP").getBytes();
  public static final byte[] s_ViaBranchNameStartBytes = ("branch=z9hG4bK").getBytes();
  public static final byte[] s_ViaBranchValueStartBytes = ("z9hG4bK").getBytes();

  // public static final int s_ViaBranchParamNameLength =
  // s_ViaBranchNameStartBytes.toString().indexOf('=');
  public static final int s_ViaBranchParamNameLength = 6;
  public static final byte[] s_ViaTokenNameStartBytes = ("tok=").getBytes();
  public static final DsByteString s_ViaTokenNameStart = new DsByteString("tok=");
  public static final DsByteString s_TokParamName = new DsByteString("tok");
  public static final byte[] s_URLTagStartBytes = ("tag=").getBytes();
  public static final byte[] s_ContactWildcardBytes = ("*").getBytes();

  // auth strings
  public static final byte[] s_DigestAuthUsernameParam = ("username=").getBytes();
  public static final int s_DigestAuthUsernameLength =
      (new DsByteString(s_DigestAuthUsernameParam)).indexOf('=');
  public static final byte[] s_DigestAuthRealmParam = ("realm=").getBytes();
  public static final int s_DigestAuthRealmLength =
      (new DsByteString(s_DigestAuthRealmParam).toString()).indexOf('=');
  public static final byte[] s_DigestAuthNonceParam = ("nonce=").getBytes();
  public static final int s_DigestAuthNonceLength =
      (new DsByteString(s_DigestAuthNonceParam).toString()).indexOf('=');
  public static final byte[] s_DigestAuthResponseParam = ("response=").getBytes();
  //    public static final int s_DigestAuthResponsePrefixLength = (new
  // DsByteString(s_DigestAuthResponseParam).toString()).indexOf('=');
  public static final int s_DigestAuthResponsePrefixLength = s_DigestAuthResponseParam.length;
  public static final int s_DigestAuthResponseLength = 16;
  public static final byte[] s_DigestAuthURIParam = ("uri=").getBytes();
  public static final int s_DigestAuthURILength =
      (new DsByteString(s_DigestAuthURIParam).toString()).indexOf('=');

  public static final byte[] s_DigestAuthenticationBytes = ("Digest").getBytes();

  public static final byte[] s_DigestAuthDomainParam = ("domain=").getBytes();
  public static final int s_DigestAuthDomainLength =
      (new DsByteString(s_DigestAuthDomainParam).toString()).indexOf('=');

  public static final byte[] s_AtSeparator = ("@").getBytes();

  public static final int GENERIC_CONTENT_MAX_SIZE = 255;

  public static final byte TOKEN_SIP_OPEN_URI = 0x08;
  public static final byte TOKEN_SIP_BRACKETED_URI = 0x09;
  public static final byte TOKEN_SIP_OPEN_URI_WITH_PORT = 0x0a;
  public static final byte TOKEN_SIP_BRACKETED_URI_WITH_PORT = 0x0b;

  public static final byte[] DS_TOKEN_SIP_SIP_SCHEME = ("sip").getBytes();
  public static final byte[] DS_TOKEN_SIP_TEL_SCHEME = ("tel").getBytes();
  public static final byte[] DS_TOKEN_SIP_INVITE_STARTLINE = ("INVITE").getBytes();

  public static final byte TOKEN_SIP_PARAM_MIN = 0x40;
  public static final byte TOKEN_SIP_PARAM_MAX = 0x4F;

  // name-addr/uri encodings
  public static final int TOKEN_SIP_INVITE_URI_DISPLAY_NAME = 0x01; // 0000000x
  public static final int TOKEN_SIP_INVITE_TEL_URI_SCHEME = 0x02; // 000000x0
  public static final int TOKEN_SIP_INVITE_URI_KNOWN_SCHEME = 0x04; // 00000x00
  public static final int TOKEN_SIP_INVITE_URI_PORT = 0x08; // 0000x000
  public static final int TOKEN_SIP_INVITE_URI_PARAMS = 0x10; // 000x0000
  public static final int TOKEN_SIP_INVITE_URI_TAG_PARAM = 0x20; // 00x00000
  public static final int TOKEN_SIP_INVITE_URI_TWO_PART_USER = 0x40; // 0x000000

  public static final byte TOKEN_SIP_END_OF_HEADERS = 0x0d; // CRLF CRLF

  // param encodings
  public static final int TOKEN_SIP_PARAM_PREPEND_SEMI = 0x00; // 0000000x
  public static final int TOKEN_SIP_PARAM_PREPEND_COMMA = 0x01; // 0000000x
  public static final int TOKEN_SIP_PARAM_PREPEND_QUESTION = 0x02; // 000000x0
  public static final int TOKEN_SIP_PARAM_PREPEND_NONE = 0x03; // 000000x0
  public static final int TOKEN_SIP_PARAM_NOT_QUOTED = 0x04; // 000000x0
  public static final int TOKEN_SIP_PARAM_QUOTED = 0x00; // 000000x0

  public static final byte[] TOKEN_SIP_LANGUAGE_SEPERATOR = ("-").getBytes();
  public static final byte[] TOKEN_SIP_EVENT_SEPERATOR = (".").getBytes();
  public static final byte[] TOKEN_SIP_EMPTY_TOKEN = ("").getBytes();

  public static final DsByteString TOKEN_SIP_PROXY_AUTHORIZATION_RESPONSE_PREFIX =
      new DsByteString("response");
  public static final DsByteString TOKEN_SIP_TWO_PART_USER_SEPERATOR = new DsByteString("%40");
}
