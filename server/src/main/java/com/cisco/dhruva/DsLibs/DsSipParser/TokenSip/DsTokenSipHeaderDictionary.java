// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipParser.TokenSip;

import com.cisco.dhruva.DsLibs.DsSipObject.*;

public class DsTokenSipHeaderDictionary {
  public static final int UNDEFINED = 0x00;

  public static int shortcutCount = 0;
  public static final int sm_Allow_Shortcut = 0xe0 + shortcutCount++;
  public static final int sm_AllowEvents_Shortcut = 0xe0 + shortcutCount++;
  public static final int sm_Authorization_Shortcut = 0xe0 + shortcutCount++;
  public static final int sm_Callid_Shortcut = 0xe0 + shortcutCount++;
  public static final int sm_Contact_Shortcut = 0xe0 + shortcutCount++;
  public static final int sm_Content_Length_Shortcut = 0xe0 + shortcutCount++;
  public static final int sm_Content_Type_Shortcut = 0xe0 + shortcutCount++;
  public static final int sm_CSeq_Shortcut = 0xe0 + shortcutCount++;
  public static final int sm_Event_Shortcut = 0xe0 + shortcutCount++;
  public static final int sm_Expires_Shortcut = 0xe0 + shortcutCount++;
  public static final int sm_From_Shortcut = 0xe0 + shortcutCount++;
  public static final int sm_In_Reply_To_Shortcut = 0xe0 + shortcutCount++;
  public static final int sm_Max_Forwards_Shortcut = 0xe0 + shortcutCount++;
  public static final int sm_Privacy_Shortcut = 0xe0 + shortcutCount++;
  public static final int sm_Proxy_Authenticate_Shortcut = 0xe0 + shortcutCount++;
  public static final int sm_Proxy_Authorization_Shortcut = 0xe0 + shortcutCount++;
  public static final int sm_RAck_Shortcut = 0xe0 + shortcutCount++;
  public static final int sm_Recipient_Shortcut = 0xe0 + shortcutCount++;
  public static final int sm_Record_Route_Shortcut = 0xe0 + shortcutCount++;
  public static final int sm_Reply_To_Shortcut = 0xe0 + shortcutCount++;
  public static final int sm_Require_Shortcut = 0xe0 + shortcutCount++;
  public static final int sm_Route_Shortcut = 0xe0 + shortcutCount++;
  public static final int sm_RSeq_Shortcut = 0xe0 + shortcutCount++;
  public static final int sm_Security_Client_Shortcut = 0xe0 + shortcutCount++;
  public static final int sm_Security_Server_Shortcut = 0xe0 + shortcutCount++;
  public static final int sm_Security_Verify_Shortcut = 0xe0 + shortcutCount++;
  public static final int sm_Supported_Shortcut = 0xe0 + shortcutCount++;
  public static final int sm_Timestamp_Shortcut = 0xe0 + shortcutCount++;
  public static final int sm_To_Shortcut = 0xe0 + shortcutCount++;
  public static final int sm_UserAgent_Shortcut = 0xe0 + shortcutCount++;
  public static final int sm_Via_Shortcut = 0xe0 + shortcutCount++;
  public static final int sm_WWW_Authenticate_Shortcut = 0xe0 + shortcutCount++;

  private static final int s_HeaderShortcutDictionaryMin = 0xe0;
  private static final int s_HeaderShortcutDictionaryMax =
      s_HeaderShortcutDictionaryMin + shortcutCount - 1;
  private static final int s_NumberOfHeaderShortcuts = shortcutCount;
  private static final byte s_HeaderShortcutDictionaryMinByte =
      (byte) s_HeaderShortcutDictionaryMin;
  private static final byte s_HeaderShortcutDictionaryMaxByte =
      (byte) s_HeaderShortcutDictionaryMax;

  private static final byte[][] s_HeaderDictionary = {
    ("Accept").getBytes(),
    ("Accept-Contact").getBytes(),
    ("Accept-Encoding").getBytes(),
    ("Accept-Language").getBytes(),
    ("Alert-Info").getBytes(),
    ("Allow").getBytes(),
    ("Allow-Events").getBytes(),
    ("Authenticate").getBytes(),
    ("Authentication-Info").getBytes(),
    ("Authorization").getBytes(), // 10
    ("Call-ID").getBytes(),
    ("Call-Info").getBytes(),
    ("Conference").getBytes(),
    ("Contact").getBytes(),
    ("Content-Disposition").getBytes(),
    ("Content-Encoding").getBytes(),
    ("Content-Language").getBytes(),
    ("Content-Length").getBytes(),
    ("Content-Type").getBytes(),
    ("Content-Version").getBytes(),
    ("CSeq").getBytes(),
    ("Date").getBytes(),
    ("Encryption").getBytes(),
    ("Error-Info").getBytes(),
    ("Event").getBytes(),
    ("Expires").getBytes(),
    ("From").getBytes(),
    ("Hide").getBytes(),
    ("In-Reply-To").getBytes(),
    ("Max-Forwards").getBytes(),
    ("MIME-Version").getBytes(),
    ("Min-Expires").getBytes(),
    ("Min-SE").getBytes(),
    ("Organization").getBytes(),
    ("P-Access-Network-Info").getBytes(),
    ("P-Asserted-Identity").getBytes(),
    ("P-Associated-URI").getBytes(),
    ("P-Called-Party-ID").getBytes(),
    ("P-Charging-Function-Addresses").getBytes(),
    ("P-Charging-Vector").getBytes(),
    ("P-Media-Authorization").getBytes(),
    ("P-Preferred-Identity").getBytes(),
    ("P-Visited-Network-ID").getBytes(),
    ("Path").getBytes(),
    ("Priority").getBytes(),
    ("Privacy").getBytes(),
    ("Proxy-Authenticate").getBytes(),
    ("Proxy-Authorization").getBytes(),
    ("Proxy-Require").getBytes(),
    ("RAck").getBytes(),
    ("Reason").getBytes(),
    ("Recipient").getBytes(),
    ("Record-Route").getBytes(),
    ("Referred-By").getBytes(),
    ("Refer-To").getBytes(),
    ("Reject-Contact").getBytes(),
    ("Replaces").getBytes(),
    ("Reply-To").getBytes(),
    ("Request-Disposition").getBytes(),
    ("Require").getBytes(),
    ("Require-Contact").getBytes(),
    ("Response-Key").getBytes(),
    ("Retry-After").getBytes(),
    ("Route").getBytes(),
    ("RSeq").getBytes(),
    ("Security-Client").getBytes(),
    ("Security-Server").getBytes(),
    ("Security-Verify").getBytes(),
    ("Server").getBytes(),
    ("Service-Route").getBytes(),
    ("Session").getBytes(),
    ("Session-Expires").getBytes(),
    ("Subject").getBytes(),
    ("Subscription-Expires").getBytes(),
    ("Subscription-State").getBytes(),
    ("Supported").getBytes(),
    ("Timestamp").getBytes(),
    ("To").getBytes(),
    ("Unsupported").getBytes(),
    ("User-Agent").getBytes(),
    ("Via").getBytes(),
    ("Warning").getBytes(),
    ("WWW-Authenticate").getBytes()
  };

  private static final int[] m_HeaderTypeToShortcuts = new int[DsSipConstants.MAX_KNOWN_HEADER + 1];
  private static final int[] m_ShortcutsToHeaderType =
      new int[s_HeaderShortcutDictionaryMax - s_HeaderShortcutDictionaryMin + 1];
  private static final int[] m_KnownHeaders = new int[DsSipConstants.MAX_KNOWN_HEADER + 1];
  // maivu - 03-03-06 - make the size of m_KnownHeadersReverse equals the size of m_KnownHeaders
  private static final int[] m_KnownHeadersReverse = new int[DsSipConstants.MAX_KNOWN_HEADER + 1];

  static {
    for (int x = 0; x < DsSipConstants.MAX_KNOWN_HEADER; x++) {
      m_HeaderTypeToShortcuts[x] = UNDEFINED;
      m_KnownHeaders[x] = UNDEFINED;
      m_KnownHeadersReverse[x] = UNDEFINED;
    }

    for (int x = 0; x < m_KnownHeadersReverse.length; x++) {
      m_KnownHeadersReverse[x] = UNDEFINED;
    }

    m_HeaderTypeToShortcuts[DsSipAllowHeader.sID] = sm_Allow_Shortcut;
    m_HeaderTypeToShortcuts[DsSipAllowEventsHeader.sID] = sm_AllowEvents_Shortcut;
    m_HeaderTypeToShortcuts[DsSipAuthorizationHeader.sID] = sm_Authorization_Shortcut;
    m_HeaderTypeToShortcuts[DsSipCallIdHeader.sID] = sm_Callid_Shortcut;
    m_HeaderTypeToShortcuts[DsSipContactHeader.sID] = sm_Contact_Shortcut;
    m_HeaderTypeToShortcuts[DsSipContentLengthHeader.sID] = sm_Content_Length_Shortcut;
    m_HeaderTypeToShortcuts[DsSipContentTypeHeader.sID] = sm_Content_Type_Shortcut;
    m_HeaderTypeToShortcuts[DsSipCSeqHeader.sID] = sm_CSeq_Shortcut;
    m_HeaderTypeToShortcuts[DsSipEventHeader.sID] = sm_Event_Shortcut;
    m_HeaderTypeToShortcuts[DsSipExpiresHeader.sID] = sm_Expires_Shortcut;
    m_HeaderTypeToShortcuts[DsSipFromHeader.sID] = sm_From_Shortcut;
    m_HeaderTypeToShortcuts[DsSipInReplyToHeader.sID] = sm_In_Reply_To_Shortcut;
    m_HeaderTypeToShortcuts[DsSipMaxForwardsHeader.sID] = sm_Max_Forwards_Shortcut;
    m_HeaderTypeToShortcuts[DsSipPrivacyHeader.sID] = sm_Privacy_Shortcut;
    m_HeaderTypeToShortcuts[DsSipProxyAuthenticateHeader.sID] = sm_Proxy_Authenticate_Shortcut;
    m_HeaderTypeToShortcuts[DsSipProxyAuthorizationHeader.sID] = sm_Proxy_Authorization_Shortcut;
    m_HeaderTypeToShortcuts[DsSipRAckHeader.sID] = sm_RAck_Shortcut;
    // m_HeaderTypeToShortcuts[DsSipRecipientHeader.sID] = sm_Recipient_Shortcut;
    // todo what's the deal with recipient
    m_HeaderTypeToShortcuts[DsSipRecordRouteHeader.sID] = sm_Record_Route_Shortcut;
    m_HeaderTypeToShortcuts[DsSipReplyToHeader.sID] = sm_Reply_To_Shortcut;
    m_HeaderTypeToShortcuts[DsSipRequireHeader.sID] = sm_Require_Shortcut;
    m_HeaderTypeToShortcuts[DsSipRouteHeader.sID] = sm_Route_Shortcut;
    m_HeaderTypeToShortcuts[DsSipRSeqHeader.sID] = sm_RSeq_Shortcut;
    // m_HeaderTypeToShortcuts[DsSipSecurityClientHeader.sID] = sm_Security_Client_Shortcut;
    // m_HeaderTypeToShortcuts[DsSipSecurityServerHeader.sID] = sm_Security_Server_Shortcut;
    // m_HeaderTypeToShortcuts[DsSipSecurityVerifyHeader.sID] = sm_Security_Verify_Shortcut;
    m_HeaderTypeToShortcuts[DsSipSupportedHeader.sID] = sm_Supported_Shortcut;
    m_HeaderTypeToShortcuts[DsSipTimestampHeader.sID] = sm_Timestamp_Shortcut;
    m_HeaderTypeToShortcuts[DsSipToHeader.sID] = sm_To_Shortcut;
    m_HeaderTypeToShortcuts[DsSipUserAgentHeader.sID] = sm_UserAgent_Shortcut;
    m_HeaderTypeToShortcuts[DsSipViaHeader.sID] = sm_Via_Shortcut;
    m_HeaderTypeToShortcuts[DsSipWWWAuthenticateHeader.sID] = sm_WWW_Authenticate_Shortcut;

    m_ShortcutsToHeaderType[sm_Allow_Shortcut - s_HeaderShortcutDictionaryMin] =
        DsSipAllowHeader.sID;
    m_ShortcutsToHeaderType[sm_AllowEvents_Shortcut - s_HeaderShortcutDictionaryMin] =
        DsSipAllowEventsHeader.sID;
    m_ShortcutsToHeaderType[sm_Authorization_Shortcut - s_HeaderShortcutDictionaryMin] =
        DsSipAuthorizationHeader.sID;
    m_ShortcutsToHeaderType[sm_Callid_Shortcut - s_HeaderShortcutDictionaryMin] =
        DsSipCallIdHeader.sID;
    m_ShortcutsToHeaderType[sm_Contact_Shortcut - s_HeaderShortcutDictionaryMin] =
        DsSipContactHeader.sID;
    m_ShortcutsToHeaderType[sm_Content_Length_Shortcut - s_HeaderShortcutDictionaryMin] =
        DsSipContentLengthHeader.sID;
    m_ShortcutsToHeaderType[sm_Content_Type_Shortcut - s_HeaderShortcutDictionaryMin] =
        DsSipContentTypeHeader.sID;
    m_ShortcutsToHeaderType[sm_CSeq_Shortcut - s_HeaderShortcutDictionaryMin] = DsSipCSeqHeader.sID;
    m_ShortcutsToHeaderType[sm_Event_Shortcut - s_HeaderShortcutDictionaryMin] =
        DsSipEventHeader.sID;
    m_ShortcutsToHeaderType[sm_Expires_Shortcut - s_HeaderShortcutDictionaryMin] =
        DsSipExpiresHeader.sID;
    m_ShortcutsToHeaderType[sm_From_Shortcut - s_HeaderShortcutDictionaryMin] = DsSipFromHeader.sID;
    m_ShortcutsToHeaderType[sm_In_Reply_To_Shortcut - s_HeaderShortcutDictionaryMin] =
        DsSipInReplyToHeader.sID;
    m_ShortcutsToHeaderType[sm_Max_Forwards_Shortcut - s_HeaderShortcutDictionaryMin] =
        DsSipMaxForwardsHeader.sID;
    m_ShortcutsToHeaderType[sm_Privacy_Shortcut - s_HeaderShortcutDictionaryMin] =
        DsSipConstants.UNKNOWN_HEADER;
    m_ShortcutsToHeaderType[sm_Proxy_Authenticate_Shortcut - s_HeaderShortcutDictionaryMin] =
        DsSipProxyAuthenticateHeader.sID;
    m_ShortcutsToHeaderType[sm_Proxy_Authorization_Shortcut - s_HeaderShortcutDictionaryMin] =
        DsSipProxyAuthorizationHeader.sID;
    m_ShortcutsToHeaderType[sm_RAck_Shortcut - s_HeaderShortcutDictionaryMin] = DsSipRAckHeader.sID;
    m_ShortcutsToHeaderType[sm_Recipient_Shortcut - s_HeaderShortcutDictionaryMin] =
        DsSipConstants.UNKNOWN_HEADER;
    m_ShortcutsToHeaderType[sm_Record_Route_Shortcut - s_HeaderShortcutDictionaryMin] =
        DsSipRecordRouteHeader.sID;
    m_ShortcutsToHeaderType[sm_Reply_To_Shortcut - s_HeaderShortcutDictionaryMin] =
        DsSipReplyToHeader.sID;
    m_ShortcutsToHeaderType[sm_Require_Shortcut - s_HeaderShortcutDictionaryMin] =
        DsSipRequireHeader.sID;
    m_ShortcutsToHeaderType[sm_Route_Shortcut - s_HeaderShortcutDictionaryMin] =
        DsSipRouteHeader.sID;
    m_ShortcutsToHeaderType[sm_RSeq_Shortcut - s_HeaderShortcutDictionaryMin] = DsSipRSeqHeader.sID;
    m_ShortcutsToHeaderType[sm_Security_Client_Shortcut - s_HeaderShortcutDictionaryMin] =
        DsSipConstants.UNKNOWN_HEADER;
    m_ShortcutsToHeaderType[sm_Security_Server_Shortcut - s_HeaderShortcutDictionaryMin] =
        DsSipConstants.UNKNOWN_HEADER;
    m_ShortcutsToHeaderType[sm_Security_Verify_Shortcut - s_HeaderShortcutDictionaryMin] =
        DsSipConstants.UNKNOWN_HEADER;
    m_ShortcutsToHeaderType[sm_Supported_Shortcut - s_HeaderShortcutDictionaryMin] =
        DsSipSupportedHeader.sID;
    m_ShortcutsToHeaderType[sm_Timestamp_Shortcut - s_HeaderShortcutDictionaryMin] =
        DsSipTimestampHeader.sID;
    m_ShortcutsToHeaderType[sm_To_Shortcut - s_HeaderShortcutDictionaryMin] = DsSipToHeader.sID;
    m_ShortcutsToHeaderType[sm_UserAgent_Shortcut - s_HeaderShortcutDictionaryMin] =
        DsSipUserAgentHeader.sID;
    m_ShortcutsToHeaderType[sm_Via_Shortcut - s_HeaderShortcutDictionaryMin] = DsSipViaHeader.sID;
    m_ShortcutsToHeaderType[sm_WWW_Authenticate_Shortcut - s_HeaderShortcutDictionaryMin] =
        DsSipWWWAuthenticateHeader.sID;

    // todo commented out those that have no matching header.  Check em out.
    m_KnownHeaders[DsSipAcceptHeader.sID] = 1;
    // m_KnownHeaders[DsSipAcceptContactHeader.sID] = 2;
    m_KnownHeaders[DsSipAcceptEncodingHeader.sID] = 3;
    m_KnownHeaders[DsSipAcceptLanguageHeader.sID] = 4;
    m_KnownHeaders[DsSipAlertInfoHeader.sID] = 5;
    m_KnownHeaders[DsSipAllowHeader.sID] = 6;
    m_KnownHeaders[DsSipAllowEventsHeader.sID] = 7;
    m_KnownHeaders[DsSipAuthenticationInfoHeader.sID] = 8;
    m_KnownHeaders[DsSipAuthenticationInfoHeader.sID] = 9;
    m_KnownHeaders[DsSipAuthorizationHeader.sID] = 10;
    m_KnownHeaders[DsSipCallIdHeader.sID] = 11;
    m_KnownHeaders[DsSipCallInfoHeader.sID] = 12;
    // m_KnownHeaders[DsSipConferenceHeader.sID] = 13;
    m_KnownHeaders[DsSipContactHeader.sID] = 14;
    m_KnownHeaders[DsSipContentDispositionHeader.sID] = 15;
    m_KnownHeaders[DsSipContentEncodingHeader.sID] = 16;
    m_KnownHeaders[DsSipContentLanguageHeader.sID] = 17;
    m_KnownHeaders[DsSipContentLengthHeader.sID] = 18;
    m_KnownHeaders[DsSipContentTypeHeader.sID] = 19;
    m_KnownHeaders[DsSipContentVersionHeader.sID] = 20;
    m_KnownHeaders[DsSipCSeqHeader.sID] = 21;
    m_KnownHeaders[DsSipDateHeader.sID] = 22;
    // m_KnownHeaders[DsSipEncryptionHeader.sID] = 23;
    m_KnownHeaders[DsSipErrorInfoHeader.sID] = 24;
    m_KnownHeaders[DsSipEventHeader.sID] = 25;
    m_KnownHeaders[DsSipExpiresHeader.sID] = 26;
    m_KnownHeaders[DsSipFromHeader.sID] = 27;
    // m_KnownHeaders[DsSipHideHeader.sID] = 28;
    m_KnownHeaders[DsSipInReplyToHeader.sID] = 29;
    m_KnownHeaders[DsSipMaxForwardsHeader.sID] = 30;
    m_KnownHeaders[DsSipMIMEVersionHeader.sID] = 31;
    // m_KnownHeaders[DsSipMinExpiresHeader.sID] = 32;
    // m_KnownHeaders[DsSipMinSEHeader.sID] = 33;
    m_KnownHeaders[DsSipOrganizationHeader.sID] = 34;

    m_KnownHeaders[DsSipPAccessNetworkInfoHeader.sID] = 35;
    m_KnownHeaders[DsSipPAssertedIdentityHeader.sID] = 36;
    m_KnownHeaders[DsSipPAssociatedURIHeader.sID] = 37;
    // m_KnownHeaders[DsSipPCalledPartyIDHeader.sID] = 38;
    m_KnownHeaders[DsSipPChargingFunctionAddressesHeader.sID] = 39;
    m_KnownHeaders[DsSipPChargingVectorHeader.sID] = 40;
    // m_KnownHeaders[DsSipPMediaAuthorizationHeader.sID] = 41;
    m_KnownHeaders[DsSipPPreferredIdentityHeader.sID] = 42;
    m_KnownHeaders[DsSipPVisitedNetworkIDHeader.sID] = 43;

    m_KnownHeaders[DsSipPathHeader.sID] = 44;
    m_KnownHeaders[DsSipPriorityHeader.sID] = 45;
    m_KnownHeaders[DsSipPrivacyHeader.sID] = 46;
    m_KnownHeaders[DsSipProxyAuthenticateHeader.sID] = 47;
    m_KnownHeaders[DsSipProxyAuthorizationHeader.sID] = 48;
    m_KnownHeaders[DsSipProxyRequireHeader.sID] = 49;
    m_KnownHeaders[DsSipRAckHeader.sID] = 50;
    // m_KnownHeaders[DsSipReasonHeader.sID] = 51;
    // m_KnownHeaders[DsSipRecipientHeader.sID] = 52;
    m_KnownHeaders[DsSipRecordRouteHeader.sID] = 53;
    m_KnownHeaders[DsSipReferredByHeader.sID] = 54;
    m_KnownHeaders[DsSipReferToHeader.sID] = 55;
    // m_KnownHeaders[DsSipRejectContactHeader.sID] = 56;
    // m_KnownHeaders[DsSipReplacesHeader.sID] = 57;
    m_KnownHeaders[DsSipReplyToHeader.sID] = 58;
    // m_KnownHeaders[DsSipRequestDispositionHeader.sID] = 59;
    m_KnownHeaders[DsSipRequireHeader.sID] = 60;
    // m_KnownHeaders[DsSipRequireContactHeader.sID] = 61;
    // m_KnownHeaders[DsSipResponseKeyHeader.sID] = 62;
    m_KnownHeaders[DsSipRetryAfterHeader.sID] = 63;
    m_KnownHeaders[DsSipRouteHeader.sID] = 64;
    m_KnownHeaders[DsSipRSeqHeader.sID] = 65;
    // m_KnownHeaders[DsSipSecurityClientHeader.sID] = 66;
    // m_KnownHeaders[DsSipSecurityServerHeader.sID] = 67;
    // m_KnownHeaders[DsSipSecurityVerifyHeader.sID] = 68;
    m_KnownHeaders[DsSipServerHeader.sID] = 69;
    m_KnownHeaders[DsSipServiceRouteHeader.sID] = 70;
    // m_KnownHeaders[DsSipSessionHeader.sID] = 71;
    m_KnownHeaders[DsSipSessionExpiresHeader.sID] = 72;
    m_KnownHeaders[DsSipSubjectHeader.sID] = 73;
    m_KnownHeaders[DsSipSubscriptionExpiresHeader.sID] = 74;
    m_KnownHeaders[DsSipSubscriptionStateHeader.sID] = 75;
    m_KnownHeaders[DsSipSupportedHeader.sID] = 76;
    m_KnownHeaders[DsSipTimestampHeader.sID] = 77;
    m_KnownHeaders[DsSipToHeader.sID] = 78;
    m_KnownHeaders[DsSipUnsupportedHeader.sID] = 79;
    m_KnownHeaders[DsSipUserAgentHeader.sID] = 80;
    m_KnownHeaders[DsSipViaHeader.sID] = 81;
    m_KnownHeaders[DsSipWarningHeader.sID] = 82;
    m_KnownHeaders[DsSipWWWAuthenticateHeader.sID] = 83;

    // reverse lookups
    m_KnownHeadersReverse[m_KnownHeaders[DsSipAcceptHeader.sID]] = DsSipAcceptHeader.sID;
    // m_KnownHeadersReverse[//m_KnownHeaders[DsSipAcceptContactHeader.sID]] =
    // DsSipAcceptContactHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipAcceptEncodingHeader.sID]] =
        DsSipAcceptEncodingHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipAcceptLanguageHeader.sID]] =
        DsSipAcceptLanguageHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipAlertInfoHeader.sID]] = DsSipAlertInfoHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipAllowHeader.sID]] = DsSipAllowHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipAllowEventsHeader.sID]] = DsSipAllowEventsHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipAuthenticationInfoHeader.sID]] =
        DsSipAuthenticationInfoHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipAuthenticationInfoHeader.sID]] =
        DsSipAuthenticationInfoHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipAuthorizationHeader.sID]] =
        DsSipAuthorizationHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipCallIdHeader.sID]] = DsSipCallIdHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipCallInfoHeader.sID]] = DsSipCallInfoHeader.sID;
    // m_KnownHeadersReverse[//m_KnownHeaders[DsSipConferenceHeader.sID]] =
    // DsSipConferenceHeader.sID]
    m_KnownHeadersReverse[m_KnownHeaders[DsSipContactHeader.sID]] = DsSipContactHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipContentDispositionHeader.sID]] =
        DsSipContentDispositionHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipContentEncodingHeader.sID]] =
        DsSipContentEncodingHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipContentLanguageHeader.sID]] =
        DsSipContentLanguageHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipContentLengthHeader.sID]] =
        DsSipContentLengthHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipContentTypeHeader.sID]] = DsSipContentTypeHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipContentVersionHeader.sID]] =
        DsSipContentVersionHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipCSeqHeader.sID]] = DsSipCSeqHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipDateHeader.sID]] = DsSipDateHeader.sID;
    // m_KnownHeadersReverse[//m_KnownHeaders[DsSipEncryptionHeader.sID]] =
    // DsSipEncryptionHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipErrorInfoHeader.sID]] = DsSipErrorInfoHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipEventHeader.sID]] = DsSipEventHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipExpiresHeader.sID]] = DsSipExpiresHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipFromHeader.sID]] = DsSipFromHeader.sID;
    // m_KnownHeadersReverse[//m_KnownHeaders[DsSipHideHeader.sID]] = DsSipHideHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipInReplyToHeader.sID]] = DsSipInReplyToHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipMaxForwardsHeader.sID]] = DsSipMaxForwardsHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipMIMEVersionHeader.sID]] = DsSipMIMEVersionHeader.sID;
    // m_KnownHeadersReverse[//m_KnownHeaders[DsSipMinExpiresHeader.sID]] =
    // DsSipMinExpiresHeader.sID;
    // m_KnownHeadersReverse[//m_KnownHeaders[DsSipMinSEHeader.sID]] = DsSipMinSEHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipOrganizationHeader.sID]] =
        DsSipOrganizationHeader.sID;

    m_KnownHeadersReverse[m_KnownHeaders[DsSipPAccessNetworkInfoHeader.sID]] =
        DsSipPAccessNetworkInfoHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipPAssertedIdentityHeader.sID]] =
        DsSipPAssertedIdentityHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipPAssociatedURIHeader.sID]] =
        DsSipPAssociatedURIHeader.sID;
    // m_KnownHeadersReverse[m_KnownHeaders[DsSipPCalledPartyIDHeader.sID]] =
    // DsSipPCalledPartyIDHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipPChargingFunctionAddressesHeader.sID]] =
        DsSipPChargingFunctionAddressesHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipPChargingVectorHeader.sID]] =
        DsSipPChargingVectorHeader.sID;
    // m_KnownHeadersReverse[m_KnownHeaders[DsSipPMediaAuthorizationHeader.sID]] =
    // DsSipPMediaAuthorizationHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipPPreferredIdentityHeader.sID]] =
        DsSipPPreferredIdentityHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipPVisitedNetworkIDHeader.sID]] =
        DsSipPVisitedNetworkIDHeader.sID;

    m_KnownHeadersReverse[m_KnownHeaders[DsSipPathHeader.sID]] = DsSipPathHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipPriorityHeader.sID]] = DsSipPriorityHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipPrivacyHeader.sID]] = DsSipPrivacyHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipProxyAuthenticateHeader.sID]] =
        DsSipProxyAuthenticateHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipProxyAuthorizationHeader.sID]] =
        DsSipProxyAuthorizationHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipProxyRequireHeader.sID]] =
        DsSipProxyRequireHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipRAckHeader.sID]] = DsSipRAckHeader.sID;
    // m_KnownHeadersReverse[//m_KnownHeaders[DsSipReasonHeader.sID]] = DsSipReasonHeader.sID;
    // m_KnownHeadersReverse[//m_KnownHeaders[DsSipRecipientHeader.sID]] = DsSipRecipientHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipRecordRouteHeader.sID]] = DsSipRecordRouteHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipReferredByHeader.sID]] = DsSipReferredByHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipReferToHeader.sID]] = DsSipReferToHeader.sID;
    // m_KnownHeadersReverse[//m_KnownHeaders[DsSipRejectContactHeader.sID]] =
    // DsSipRejectContactHeader.sID;
    // m_KnownHeadersReverse[//m_KnownHeaders[DsSipReplacesHeader.sID]] = DsSipReplacesHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipReplyToHeader.sID]] = DsSipReplyToHeader.sID;
    // m_KnownHeadersReverse[//m_KnownHeaders[DsSipRequestDispositionHeader.sID]] =
    // DsSipRequestDispositionHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipRequireHeader.sID]] = DsSipRequireHeader.sID;
    // m_KnownHeadersReverse[//m_KnownHeaders[DsSipRequireContactHeader.sID]] =
    // DsSipRequireContactHeader.sID;
    // m_KnownHeadersReverse[//m_KnownHeaders[DsSipResponseKeyHeader.sID]] =
    // DsSipResponseKeyHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipRetryAfterHeader.sID]] = DsSipRetryAfterHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipRouteHeader.sID]] = DsSipRouteHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipRSeqHeader.sID]] = DsSipRSeqHeader.sID;
    // m_KnownHeadersReverse[//m_KnownHeaders[DsSipSecurityClientHeader.sID]] =
    // DsSipSecurityClientHeader.sID;
    // m_KnownHeadersReverse[//m_KnownHeaders[DsSipSecurityServerHeader.sID]] =
    // DsSipSecurityServerHeader.sID;
    // m_KnownHeadersReverse[//m_KnownHeaders[DsSipSecurityVerifyHeader.sID]] =
    // DsSipSecurityVerifyHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipServerHeader.sID]] = DsSipServerHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipServiceRouteHeader.sID]] =
        DsSipServiceRouteHeader.sID;
    // m_KnownHeadersReverse[//m_KnownHeaders[DsSipSessionHeader.sID]] = DsSipSessionHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipSessionExpiresHeader.sID]] =
        DsSipSessionExpiresHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipSubjectHeader.sID]] = DsSipSubjectHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipSubscriptionExpiresHeader.sID]] =
        DsSipSubscriptionExpiresHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipSubscriptionStateHeader.sID]] =
        DsSipSubscriptionStateHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipSupportedHeader.sID]] = DsSipSupportedHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipTimestampHeader.sID]] = DsSipTimestampHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipToHeader.sID]] = DsSipToHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipUnsupportedHeader.sID]] = DsSipUnsupportedHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipUserAgentHeader.sID]] = DsSipUserAgentHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipViaHeader.sID]] = DsSipViaHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipWarningHeader.sID]] = DsSipWarningHeader.sID;
    m_KnownHeadersReverse[m_KnownHeaders[DsSipWWWAuthenticateHeader.sID]] =
        DsSipWWWAuthenticateHeader.sID;
  }

  public static final byte[] getEncoding(int headerId) {
    int index = m_HeaderTypeToShortcuts[headerId];

    if (index != UNDEFINED) {
      // return DsByteString.valueOf(index).toByteArray();
      byte[] result = {(byte) index};
      return result;
    } else {
      index = m_KnownHeaders[headerId];
      if (index != UNDEFINED) {
        byte[] result = new byte[2];
        result[0] = DsTokenSipConstants.TOKEN_SIP_KNOWN_HEADER;
        result[1] = (byte) index;
        return result;
      } else {
        return null;
      }
    }
  }

  /**
   * Retrieves the dictionary entry based on the shortcut index. The dictionary entry is the
   * standard header ID or UNKNOWN_HEADER
   *
   * @param encoding The encoded header ID.
   * @return The dictionary value
   */
  public static final int getShortcutHeaderId(int encoding) {
    if ((encoding < s_HeaderShortcutDictionaryMaxByte)
        && (encoding > s_HeaderShortcutDictionaryMinByte)) {
      return m_ShortcutsToHeaderType[s_NumberOfHeaderShortcuts + encoding];
    }
    return DsSipConstants.UNKNOWN_HEADER;
  }

  public static final int getHeaderId(int encoding) {
    try {
      return m_KnownHeadersReverse[encoding];
    } catch (ArrayIndexOutOfBoundsException e) {
      return UNDEFINED;
    }
  }

  // used for debugging
  public static final byte[] getHeaderName(int headerId) {
    return s_HeaderDictionary[m_KnownHeaders[headerId] - 1];
  }

  /**
   * Checks if the header is a fixed-format type header.
   *
   * @param headerId The encoded header ID.
   * @return
   */
  public static final boolean isFixedFormatUriHeader(int headerId) {
    switch (headerId) {
      case DsSipConstants.TO:
      case DsSipConstants.FROM:
      case DsSipConstants.CONTACT:
      case DsSipConstants.RECORD_ROUTE:
      case DsSipConstants.ROUTE:
        return true;
      default:
        return false;
    }
  }

  /**
   * Checks if the header is in the tokenized SIP spec "blocked" list.
   *
   * @param headerId The encoded header ID.
   * @return
   */
  public static final boolean isBlockedHeader(int headerId) {
    switch (headerId) {
        // General headers that we choose to block
      case DsSipConstants.USER_AGENT:
      case DsSipConstants.MAX_FORWARDS:
      case DsSipConstants.CONTENT_LENGTH:
      case DsSipConstants.REMOTE_PARTY_ID:
      case DsSipConstants.CONTENT_TYPE:

        // We also block privacy from going to the client
      case DsSipConstants.PRIVACY:

        // Headers that are known to the JUA, but no to Token Sip
      case DsSipConstants.AE_COOKIE:
      case DsSipConstants.AS_PATH:
      case DsSipConstants.AS_RECORD_PATH:
      case DsSipConstants.SERVICE_AGENT_ROUTE:
      case DsSipConstants.TRANSLATE:
      case DsSipConstants.X_APPLICATION:
      case DsSipConstants.X_APPLICATION_CONTEXT:
      case DsSipConstants.X_CONNECTION_INFO:
      case DsSipConstants.X_FROM_OUTSIDE:
      case DsSipConstants.DIVERSION:
      case DsSipConstants.SERVICE_AGENT_APPLICATION:
      case DsSipConstants.SERVICE_AGENT_CONTEXT:
      case DsSipConstants.SERVICE_AGENT_PHASE:
      case DsSipConstants.SERVICE_ROUTE:

        // and block all of the "P-" headers that we know about.
      case DsSipConstants.P_ASSOCIATED_URI:
      case DsSipConstants.P_CALLED_PARTY_ID:
      case DsSipConstants.P_ACCESS_NETWORK_INFO:
      case DsSipConstants.P_ASSERTED_IDENTITY:
      case DsSipConstants.P_PREFERRED_IDENTITY:
      case DsSipConstants.P_DCS_LAES:
      case DsSipConstants.P_CHARGING_FUNCTION_ADDRESSES:
      case DsSipConstants.P_CHARGING_VECTOR:
      case DsSipConstants.P_VISITED_NETWORK_ID:
        return true;

      default:
        return false;
    }
  }
}
