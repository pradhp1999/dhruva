// Copyright (c) 2005-2009 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipMsgParser;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

/**
 * Provides the default construction of DsSipHeader objects. Users can create their own header types
 * and register them with DsSipMsgParser and then extend this class so that the Java User Agent will
 * automatically create these new header types when parsing a SIP message.
 *
 * <p>Users only need to extend the {@link #newInstance(int) newInstance(int headerId)} method,
 * since the method that take a header name either return an unknown header or resolve to the {@link
 * #newInstance(int) newInstance(int headerId)} method.
 *
 * <p>After extending this class, users must register an instance of the subclass with DsSipHeader
 * by calling the static method {@link DsSipHeader#setHeaderFactory(DsSipHeaderFactory)
 * DsSipHeader.setHeaderFactory(DsSipHeaderFactory)}.
 *
 * <p>When implementing {@link #newInstance(int) newInstance(int headerId)} in a subclass, the
 * method must first determine <code>headerId</code> is an extension header. If it is, then create a
 * new header of the appropriate type and return it, otherwise just do the following:
 *
 * <blockquote>
 *
 * <code>return super.newInstance(headerId);</code>
 *
 * </blockquote>
 */
public class DsSipHeaderFactory implements DsSipConstants {
  /** Header logging category. */
  private static Logger cat = DsLog4j.headerCat;

  /** Default constructor. */
  public DsSipHeaderFactory() {}

  /**
   * A factory to create headers of the correct subtype. If the headerId is not known then
   * DsSipUnknown header will be returned.
   *
   * @param headerId the header type to create the proper subtype from
   * @return a new DsSipHeader of the proper subclass.
   */
  public DsSipHeader newInstance(int headerId) {
    DsSipHeader header;

    // Optimize this - when we settle on a header list order, make sure that
    // these headers are in that order.  JVM will use the proper switch statement - jsm
    // The headers in this list must maintain the same order as they appear in DsSipConstants. - jsm
    switch (headerId) {
      case VIA:
        header = new DsSipViaHeader();
        break;
      case MAX_FORWARDS:
        header = new DsSipMaxForwardsHeader();
        break;
      case ROUTE:
        header = new DsSipRouteHeader();
        break;
      case RECORD_ROUTE:
        header = new DsSipRecordRouteHeader();
        break;
      case TO:
        header = new DsSipToHeader();
        break;
      case FROM:
        header = new DsSipFromHeader();
        break;
      case CSEQ:
        header = new DsSipCSeqHeader();
        break;
      case CALL_ID:
        header = new DsSipCallIdHeader();
        break;

      case CONTENT_LENGTH:
        header = new DsSipContentLengthHeader();
        break;
      case CONTACT:
        header = new DsSipContactHeader();
        break;
      case EXPIRES:
        header = new DsSipExpiresHeader();
        break;
      case PROXY_REQUIRE:
        header = new DsSipProxyRequireHeader();
        break;
      case REQUIRE:
        header = new DsSipRequireHeader();
        break;

      case SERVICE_AGENT_PHASE:
        header = new DsSipServiceAgentPhaseHeader();
        break;
      case SERVICE_AGENT_CONTEXT:
        header = new DsSipServiceAgentContextHeader();
        break;
      case SERVICE_AGENT_APPLICATION:
        header = new DsSipServiceAgentApplicationHeader();
        break;
      case SERVICE_AGENT_ROUTE:
        header = new DsSipServiceAgentRouteHeader();
        break;
      case REMOTE_PARTY_ID:
        header = new DsSipRemotePartyIdHeader();
        break;
      case EVENT:
        header = new DsSipEventHeader();
        break;
      case DIVERSION:
        header = new DsSipDiversionHeader();
        break;
      case P_ASSOCIATED_URI:
        header = new DsSipPAssociatedURIHeader();
        break;
      case P_CALLED_PARTY_ID:
        header = new DsSipPCalledPartyIdHeader();
        break;
      case SERVICE_ROUTE:
        header = new DsSipServiceRouteHeader();
        break;
      case P_ACCESS_NETWORK_INFO:
        header = new DsSipPAccessNetworkInfoHeader();
        break;
      case PRIVACY:
        header = new DsSipPrivacyHeader();
        break;

      case CONTENT_TYPE:
        header = new DsSipContentTypeHeader();
        break;

      case X_APPLICATION:
        header = new DsSipXApplicationHeader();
        break;
      case X_APPLICATION_CONTEXT:
        header = new DsSipXApplicationContextHeader();
        break;
      case X_FROM_OUTSIDE:
        header = new DsSipXFromOutsideHeader();
        break;
      case AS_PATH:
        header = new DsSipASPathHeader();
        break;
      case AS_RECORD_PATH:
        header = new DsSipASRecordPathHeader();
        break;
      case AE_COOKIE:
        header = new DsSipAECookieHeader();
        break;

      case SUBSCRIPTION_EXPIRES:
        header = new DsSipSubscriptionExpiresHeader();
        break;
      case ALLOW_EVENTS:
        header = new DsSipAllowEventsHeader();
        break;

      case PROXY_AUTHENTICATE:
        header = new DsSipProxyAuthenticateHeader();
        break;
      case PROXY_AUTHORIZATION:
        header = new DsSipProxyAuthorizationHeader();
        break;
      case AUTHORIZATION:
        header = new DsSipAuthorizationHeader();
        break;
      case AUTHENTICATION_INFO:
        header = new DsSipAuthenticationInfoHeader();
        break;
      case WWW_AUTHENTICATE:
        header = new DsSipWWWAuthenticateHeader();
        break;

      case ACCEPT:
        header = new DsSipAcceptHeader();
        break;
      case ACCEPT_ENCODING:
        header = new DsSipAcceptEncodingHeader();
        break;
      case ACCEPT_LANGUAGE:
        header = new DsSipAcceptLanguageHeader();
        break;
      case ALERT_INFO:
        header = new DsSipAlertInfoHeader();
        break;
      case ALLOW:
        header = new DsSipAllowHeader();
        break;
      case CALL_INFO:
        header = new DsSipCallInfoHeader();
        break;
      case CONTENT_DISPOSITION:
        header = new DsSipContentDispositionHeader();
        break;
      case CONTENT_ENCODING:
        header = new DsSipContentEncodingHeader();
        break;
      case CONTENT_LANGUAGE:
        header = new DsSipContentLanguageHeader();
        break;
      case DATE:
        header = new DsSipDateHeader();
        break;
      case ERROR_INFO:
        header = new DsSipErrorInfoHeader();
        break;
      case IN_REPLY_TO:
        header = new DsSipInReplyToHeader();
        break;
      case MIME_VERSION:
        header = new DsSipMIMEVersionHeader();
        break;
      case ORGANIZATION:
        header = new DsSipOrganizationHeader();
        break;
      case PRIORITY:
        header = new DsSipPriorityHeader();
        break;
      case RACK:
        header = new DsSipRAckHeader();
        break;
      case RETRY_AFTER:
        header = new DsSipRetryAfterHeader();
        break;
      case RSEQ:
        header = new DsSipRSeqHeader();
        break;
      case SERVER:
        header = new DsSipServerHeader();
        break;
      case SUBJECT:
        header = new DsSipSubjectHeader();
        break;
      case SUPPORTED:
        header = new DsSipSupportedHeader();
        break;
      case TIMESTAMP:
        header = new DsSipTimestampHeader();
        break;
      case UNSUPPORTED:
        header = new DsSipUnsupportedHeader();
        break;
      case USER_AGENT:
        header = new DsSipUserAgentHeader();
        break;
      case WARNING:
        header = new DsSipWarningHeader();
        break;
      case SESSION_EXPIRES:
        header = new DsSipSessionExpiresHeader();
        break;
      case TRANSLATE:
        header = new DsSipTranslateHeader();
        break;
      case CONTENT_VERSION:
        header = new DsSipContentVersionHeader();
        break;
      case REPLY_TO:
        header = new DsSipReplyToHeader();
        break;
      case SUBSCRIPTION_STATE:
        header = new DsSipSubscriptionStateHeader();
        break;
      case REFER_TO:
        header = new DsSipReferToHeader();
        break;
      case MIN_EXPIRES:
        header = new DsSipMinExpiresHeader();
        break;
      case PATH:
        header = new DsSipPathHeader();
        break;
      case P_ASSERTED_IDENTITY:
        header = new DsSipPAssertedIdentityHeader();
        break;
      case P_PREFERRED_IDENTITY:
        header = new DsSipPPreferredIdentityHeader();
        break;
      case REPLACES:
        header = new DsSipReplacesHeader();
        break;

        // per Edgar, no longer used by AE
        // needed to complete the ordered list - jsm
      case X_CONNECTION_INFO:
        header = new DsSipUnknownHeader(); /*header = new DsSipXConnectionInfoHeader();*/
        break;
      case P_DCS_LAES:
        header = new DsSipPDCSLAESHeader();
        break;
      case P_CHARGING_FUNCTION_ADDRESSES:
        header = new DsSipPChargingFunctionAddressesHeader();
        break;
      case P_CHARGING_VECTOR:
        header = new DsSipPChargingVectorHeader();
        break;
      case P_VISITED_NETWORK_ID:
        header = new DsSipPVisitedNetworkIDHeader();
        break;
      case REFERRED_BY:
        header = new DsSipReferredByHeader();
        break;

      case UNKNOWN_HEADER:
        header = new DsSipUnknownHeader();
        break;
        // CAFFEINE 2.0 DEVELOPMENT - (EDCS-304264) added feature support for JoinHeader,
        // ReplacesHeader
      case ACCEPT_CONTACT:
        header = new DsSipAcceptContactHeader();
        break;
      case REJECT_CONTACT:
        header = new DsSipRejectContactHeader();
        break;
      case REQUEST_DISPOSITION:
        header = new DsSipRequestDispositionHeader();
        break;
      case JOIN:
        header = new DsSipJoinHeader();
        break;
      case ETAG:
        header = new DsSipETagHeader();
        break;
      case IF_MATCH:
        header = new DsSipIfMatchHeader();
        break;
      case APP_INFO:
        header = new DsSipAppInfoHeader();
        break;
      case CONTENT_ID:
        header = new DsSipContentIdHeader();
        break;
      case CONTENT_DESCRIPTION:
        header = new DsSipContentDescriptionHeader();
        break;
      case CONTENT_TRANSFER_ENCODING:
        header = new DsSipContentTransferEncodingHeader();
        break;
      case CISCO_MAINTENANCE_MODE:
        header = new DsSipCiscoMaintenanceModeHeader();
        break;
      case CISCO_GUID:
        header = new DsSipCiscoGuidHeader();
        break;
        // MMA - 08.05.2005 - adding support for the Reason header (RFC 3326)
      case REASON_HEADER:
        header = new DsSipReasonHeader();
        break;
      case SUPPRESS_IF_MATCH:
        header = new DsSipSuppressIfMatchHeader();
        break;
      case TARGET_DIALOG:
        header = new DsSipTargetDialogHeader();
        break;
      case HISTORY_INFO:
        header = new DsSipHistoryInfoHeader();
        break;
      case X_CISCO_RAI:
        header = new DsSipXCiscoRaiHeader();
        break;
      default:
        if (cat.isEnabled(Level.WARN)) {
          cat.warn("Returning UNKNOWN header because of a bad index.");
        }

        header = new DsSipUnknownHeader();
        break;
    }

    return header;
  }

  /**
   * A factory to create headers of the correct subtype. If the headerId is not known then
   * DsSipUnknown header will be returned. With this version of newInstance(), the name of the
   * header will be populated in the unknown header.
   *
   * <p>Note that you should use the int version of this method where possible. This will save a
   * lookup, but use this one if you would have to do the lookup anyway or for unknown headers.
   *
   * @param headerName the name of the header to create the proper subtype from
   * @return a new DsSipHeader of the proper subclass.
   * @see #newInstance(int)
   */
  public final DsSipHeader newInstance(DsByteString headerName) {
    DsSipHeader header;

    int headerId = DsSipMsgParser.getHeader(headerName);

    if (headerId == UNKNOWN_HEADER) {
      header = new DsSipUnknownHeader();
      ((DsSipUnknownHeader) header).setName(headerName);
    } else {
      header = newInstance(headerId);
    }

    return header;
  }

  /**
   * A factory to create headers of the correct subtype. If the headerId is not known then
   * DsSipUnknown header will be returned. With this version of newInstance(), the name of the
   * header will be populated in the unknown header.
   *
   * <p>Note that you should use the int version of this method where possible. This will save a
   * lookup, but use this one if you would have to do the lookup anyway or for unknown headers.
   *
   * @param id the ID of the header to create.
   * @param name the name of the header to create the proper subtype from
   * @return a new DsSipHeader of the proper subclass.
   * @see #newInstance(int)
   */
  public final DsSipHeader newInstance(int id, DsByteString name) {
    if (id == UNKNOWN_HEADER) {
      return new DsSipUnknownHeader(name, null);
    } else {
      return newInstance(id);
    }
  }
}
