// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipParser;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipMime.DsMimeMessageListener;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.ByteBuffer;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsPerf;
import gnu.trove.TIntObjectHashMap;
import java.io.IOException;
import java.util.HashMap;

/**
 * Abstract base class for SIP message parser and MIME message parser. Holds common parsing methods
 * for both parsers.
 *
 * <p>Used to parse SIP messages. This is where header parsers are registered as well as new element
 * IDs.
 *
 * <p>This class was derived from DsSipMsgParser by the Caffeine team and had changed some methods,
 * variables from private to protected
 */
public abstract class DsMsgParserBase implements DsSipConstants {
  /** The maximum number of exceptions before parsing fails. */
  protected static final int MAX_EXCEPTIONS = 10;

  /** The number of characters in a SIP date. */
  protected static final int SIP_DATE_LENGTH = 29;
  /** The data structure that holds the header names. */
  protected static DsParseTreeNode parseTree[] = new DsParseTreeNode[128];

  //////////////////////////////////////////////////////////////////////////////
  // Method, Header and Element Names
  //////////////////////////////////////////////////////////////////////////////
  /** The names of the SIP methods indexed by method ID. */
  public static DsByteString[] METHOD_NAMES;
  /** The names of the SIP headers indexed by header ID. */
  public static DsByteString[] HEADER_NAMES;
  /**
   * The compact names of the SIP headers indexed by header ID. Contains the canonical header name
   * if a compact name does not exist.
   */
  public static DsByteString[] COMPACT_HEADER_NAMES;
  /**
   * The names of the SIP elements indexed by element ID. Only used for debug printing of elements.
   */
  public static DsByteString[] ELEMENT_NAMES;

  /** Mapping of header names to header IDs. */
  private static final HashMap headersMap = new HashMap(128);
  /** Mapping of method names to header IDs. */
  private static final HashMap methodsMap = new HashMap(32);
  /** Mapping of header IDs to parsers. */
  private static final TIntObjectHashMap parserMap = new TIntObjectHashMap();

  // Constants for DsPerf

  /** DsPerf constant. */
  protected static final int ENTIRE_PARSE;
  /** DsPerf constant. */
  protected static final int PARSE_START_LINE;
  /** DsPerf constant. */
  protected static final int PARSE_HEADERS;
  /** DsPerf constant. */
  protected static final int PARSE_VIA;
  /** DsPerf constant. */
  protected static final int PARSE_BODY;

  /** DsPerf constant. */
  protected static final int PARSE_INT;
  /** DsPerf constant. */
  protected static final int PARSE_LONG;
  /** DsPerf constant. */
  protected static final int PARSE_FLOAT;

  /** DsPerf constant. */
  protected static final int PARSE_SIP_URL_DATA;
  /** DsPerf constant. */
  protected static final int PARSE_TEL_URL_DATA;
  /** DsPerf constant. */
  protected static final int PARSE_CID_URL_DATA;

  /** DsPerf constant. */
  protected static final int FIRE_ELEMENT;
  /** DsPerf constant. */
  protected static final int FIRE_PARAMETER;
  /** DsPerf constant. */
  protected static final int PARSE_URL_HEADER;
  /** DsPerf constant. */
  protected static final int PARSE_NAME_ADDR;
  /** DsPerf constant. */
  protected static final int DEEP_PARSE_HEADERS;

  //////////////////////////////////////////////////////////////////////////////
  // Static block initializing Method, Header and Element Names
  //////////////////////////////////////////////////////////////////////////////

  static {
    METHOD_NAMES = new DsByteString[METHOD_NAMES_SIZE];
    HEADER_NAMES = new DsByteString[HEADER_NAMES_SIZE];
    COMPACT_HEADER_NAMES = new DsByteString[HEADER_NAMES_SIZE];
    ELEMENT_NAMES = new DsByteString[ELEMENT_NAMES_SIZE];

    // Method Names initialization
    METHOD_NAMES[ACK] = BS_ACK;
    METHOD_NAMES[BYE] = BS_BYE;
    METHOD_NAMES[CANCEL] = BS_CANCEL;
    METHOD_NAMES[INFO] = BS_INFO;
    METHOD_NAMES[INVITE] = BS_INVITE;
    METHOD_NAMES[MESSAGE] = BS_MESSAGE;
    METHOD_NAMES[NOTIFY] = BS_NOTIFY;
    METHOD_NAMES[OPTIONS] = BS_OPTIONS;
    METHOD_NAMES[PRACK] = BS_PRACK;
    METHOD_NAMES[REGISTER] = BS_REGISTER;
    METHOD_NAMES[SUBSCRIBE] = BS_SUBSCRIBE;
    METHOD_NAMES[REFER] = BS_REFER;
    METHOD_NAMES[PING] = BS_PING;
    METHOD_NAMES[UPDATE] = BS_UPDATE;
    METHOD_NAMES[PUBLISH] = BS_PUBLISH;
    METHOD_NAMES[UNKNOWN] = BS_UNKNOWN;

    // Header Names initialization
    HEADER_NAMES[ACCEPT] = BS_ACCEPT;
    HEADER_NAMES[ACCEPT_ENCODING] = BS_ACCEPT_ENCODING;
    HEADER_NAMES[ACCEPT_LANGUAGE] = BS_ACCEPT_LANGUAGE;
    HEADER_NAMES[ALERT_INFO] = BS_ALERT_INFO;
    HEADER_NAMES[ALLOW] = BS_ALLOW;
    HEADER_NAMES[AUTHENTICATION_INFO] = BS_AUTHENTICATION_INFO;
    HEADER_NAMES[AUTHORIZATION] = BS_AUTHORIZATION;
    HEADER_NAMES[CALL_ID] = BS_CALL_ID;
    HEADER_NAMES[CALL_INFO] = BS_CALL_INFO;
    HEADER_NAMES[CONTACT] = BS_CONTACT;
    HEADER_NAMES[CONTENT_DISPOSITION] = BS_CONTENT_DISPOSITION;
    HEADER_NAMES[CONTENT_ENCODING] = BS_CONTENT_ENCODING;
    HEADER_NAMES[CONTENT_LANGUAGE] = BS_CONTENT_LANGUAGE;
    HEADER_NAMES[CONTENT_LENGTH] = BS_CONTENT_LENGTH;
    HEADER_NAMES[CONTENT_TYPE] = BS_CONTENT_TYPE;
    HEADER_NAMES[CSEQ] = BS_CSEQ;
    HEADER_NAMES[DATE] = BS_DATE;
    HEADER_NAMES[ERROR_INFO] = BS_ERROR_INFO;
    HEADER_NAMES[EXPIRES] = BS_EXPIRES;
    HEADER_NAMES[FROM] = BS_FROM;
    HEADER_NAMES[IN_REPLY_TO] = BS_IN_REPLY_TO;
    HEADER_NAMES[MAX_FORWARDS] = BS_MAX_FORWARDS;
    HEADER_NAMES[MIME_VERSION] = BS_MIME_VERSION;
    HEADER_NAMES[ORGANIZATION] = BS_ORGANIZATION;
    HEADER_NAMES[PRIORITY] = BS_PRIORITY;
    HEADER_NAMES[PROXY_AUTHENTICATE] = BS_PROXY_AUTHENTICATE;
    HEADER_NAMES[PROXY_AUTHORIZATION] = BS_PROXY_AUTHORIZATION;
    HEADER_NAMES[PROXY_REQUIRE] = BS_PROXY_REQUIRE;
    HEADER_NAMES[RACK] = BS_RACK;
    HEADER_NAMES[RECORD_ROUTE] = BS_RECORD_ROUTE;
    HEADER_NAMES[REQUIRE] = BS_REQUIRE;
    HEADER_NAMES[RETRY_AFTER] = BS_RETRY_AFTER;
    HEADER_NAMES[ROUTE] = BS_ROUTE;
    HEADER_NAMES[RSEQ] = BS_RSEQ;
    HEADER_NAMES[SERVER] = BS_SERVER;
    HEADER_NAMES[SUBJECT] = BS_SUBJECT;
    HEADER_NAMES[SUPPORTED] = BS_SUPPORTED;
    HEADER_NAMES[TIMESTAMP] = BS_TIMESTAMP;
    HEADER_NAMES[TRANSLATE] = BS_TRANSLATE;
    HEADER_NAMES[TO] = BS_TO;
    HEADER_NAMES[UNSUPPORTED] = BS_UNSUPPORTED;
    HEADER_NAMES[USER_AGENT] = BS_USER_AGENT;
    HEADER_NAMES[VIA] = BS_VIA;
    HEADER_NAMES[WARNING] = BS_WARNING;
    HEADER_NAMES[WWW_AUTHENTICATE] = BS_WWW_AUTHENTICATE;
    HEADER_NAMES[SERVICE_AGENT_PHASE] = BS_SERVICE_AGENT_PHASE;
    HEADER_NAMES[SERVICE_AGENT_CONTEXT] = BS_SERVICE_AGENT_CONTEXT;
    HEADER_NAMES[SERVICE_AGENT_APPLICATION] = BS_SERVICE_AGENT_APPLICATION;
    HEADER_NAMES[SERVICE_AGENT_ROUTE] = BS_SERVICE_AGENT_ROUTE;
    HEADER_NAMES[SESSION_EXPIRES] = BS_SESSION_EXPIRES;
    HEADER_NAMES[REMOTE_PARTY_ID] = BS_REMOTE_PARTY_ID;
    HEADER_NAMES[EVENT] = BS_EVENT;
    HEADER_NAMES[SUBSCRIPTION_EXPIRES] = BS_SUBSCRIPTION_EXPIRES;
    HEADER_NAMES[SUBSCRIPTION_STATE] = BS_SUBSCRIPTION_STATE;
    HEADER_NAMES[ALLOW_EVENTS] = BS_ALLOW_EVENTS;
    HEADER_NAMES[CONTENT_VERSION] = BS_CONTENT_VERSION;
    HEADER_NAMES[AE_COOKIE] = BS_AE_COOKIE;
    HEADER_NAMES[X_APPLICATION] = BS_X_APPLICATION;
    HEADER_NAMES[X_APPLICATION_CONTEXT] = BS_X_APPLICATION_CONTEXT;
    HEADER_NAMES[X_FROM_OUTSIDE] = BS_X_FROM_OUTSIDE;
    // per Edgar, connection info header is no longer used by AE
    HEADER_NAMES[X_CONNECTION_INFO] = BS_X_CONNECTION_INFO;
    HEADER_NAMES[AS_PATH] = BS_AS_PATH;
    HEADER_NAMES[AS_RECORD_PATH] = BS_AS_RECORD_PATH;
    HEADER_NAMES[REPLY_TO] = BS_REPLY_TO;
    HEADER_NAMES[REFER_TO] = BS_REFER_TO;
    HEADER_NAMES[MIN_EXPIRES] = BS_MIN_EXPIRES;
    HEADER_NAMES[PATH] = BS_PATH;
    HEADER_NAMES[P_ASSERTED_IDENTITY] = BS_P_ASSERTED_IDENTITY;
    HEADER_NAMES[P_PREFERRED_IDENTITY] = BS_P_PREFERRED_IDENTITY;
    HEADER_NAMES[REPLACES] = BS_REPLACES;
    HEADER_NAMES[DIVERSION] = BS_DIVERSION;
    HEADER_NAMES[P_ASSOCIATED_URI] = BS_P_ASSOCIATED_URI;
    HEADER_NAMES[UNKNOWN_HEADER] = BS_UNKNOWN_HEADER;
    HEADER_NAMES[SIP_URL_ID] = BS_SIP_URL_ID;
    HEADER_NAMES[TEL_URL_ID] = BS_TEL_URL_ID;
    HEADER_NAMES[UNKNOWN_URL_ID] = BS_UNKNOWN_URL_ID;
    HEADER_NAMES[NAME_ADDR_ID] = BS_NAME_ADDR_ID;
    HEADER_NAMES[ENTIRE_MSG_ID] = BS_ENTIRE_MSG_ID;
    HEADER_NAMES[P_CALLED_PARTY_ID] = BS_P_CALLED_PARTY_ID;
    HEADER_NAMES[P_ACCESS_NETWORK_INFO] = BS_P_ACCESS_NETWORK_INFO;
    HEADER_NAMES[SERVICE_ROUTE] = BS_SERVICE_ROUTE;
    HEADER_NAMES[PRIVACY] = BS_PRIVACY;
    HEADER_NAMES[P_DCS_LAES] = BS_P_DCS_LAES;
    HEADER_NAMES[P_CHARGING_FUNCTION_ADDRESSES] = BS_P_CHARGING_FUNCTION_ADDRESSES;
    HEADER_NAMES[P_CHARGING_VECTOR] = BS_P_CHARGING_VECTOR;
    HEADER_NAMES[P_VISITED_NETWORK_ID] = BS_P_VISITED_NETWORK_ID;
    HEADER_NAMES[REFERRED_BY] = BS_REFERRED_BY;
    // CAFFEINE 2.0 DEVELOPMENT - Additional Header Names
    HEADER_NAMES[ACCEPT_CONTACT] = BS_ACCEPT_CONTACT;
    HEADER_NAMES[REJECT_CONTACT] = BS_REJECT_CONTACT;
    HEADER_NAMES[REQUEST_DISPOSITION] = BS_REQUEST_DISPOSITION;
    HEADER_NAMES[JOIN] = BS_JOIN;
    HEADER_NAMES[ETAG] = BS_ETAG;
    HEADER_NAMES[IF_MATCH] = BS_IF_MATCH;
    HEADER_NAMES[APP_INFO] = BS_APP_INFO;
    HEADER_NAMES[CONTENT_ID] = BS_CONTENT_ID;
    HEADER_NAMES[CONTENT_DESCRIPTION] = BS_CONTENT_DESCRIPTION;
    HEADER_NAMES[CONTENT_TRANSFER_ENCODING] = BS_CONTENT_TRANSFER_ENCODING;
    HEADER_NAMES[CISCO_MAINTENANCE_MODE] = BS_CISCO_MAINTENANCE_MODE;
    HEADER_NAMES[CISCO_GUID] = BS_CISCO_GUID;
    HEADER_NAMES[REASON_HEADER] =
        BS_REASON_HEADER; // MMA - 08.05.2005 - adding support for the Reason header (RFC 3326)
    HEADER_NAMES[SUPPRESS_IF_MATCH] = BS_SUPPRESS_IF_MATCH;
    HEADER_NAMES[TARGET_DIALOG] = BS_TARGET_DIALOG;
    HEADER_NAMES[HISTORY_INFO] = BS_HISTORY_INFO;
    HEADER_NAMES[X_CISCO_RAI] = BS_X_CISCO_RAI;
    HEADER_NAMES[CID_URL_ID] = BS_CID_URL_ID;

    // Compact Header Names initialization
    COMPACT_HEADER_NAMES[ACCEPT] = BS_ACCEPT;
    COMPACT_HEADER_NAMES[ACCEPT_ENCODING] = BS_ACCEPT_ENCODING;
    COMPACT_HEADER_NAMES[ACCEPT_LANGUAGE] = BS_ACCEPT_LANGUAGE;
    COMPACT_HEADER_NAMES[ALERT_INFO] = BS_ALERT_INFO;
    COMPACT_HEADER_NAMES[ALLOW] = BS_ALLOW;
    COMPACT_HEADER_NAMES[AUTHENTICATION_INFO] = BS_AUTHENTICATION_INFO;
    COMPACT_HEADER_NAMES[AUTHORIZATION] = BS_AUTHORIZATION;
    COMPACT_HEADER_NAMES[CALL_ID] = BS_CALL_ID_C;
    COMPACT_HEADER_NAMES[CALL_INFO] = BS_CALL_INFO;
    COMPACT_HEADER_NAMES[CONTACT] = BS_CONTACT_C;
    COMPACT_HEADER_NAMES[CONTENT_DISPOSITION] = BS_CONTENT_DISPOSITION;
    COMPACT_HEADER_NAMES[CONTENT_ENCODING] = BS_CONTENT_ENCODING_C;
    COMPACT_HEADER_NAMES[CONTENT_LANGUAGE] = BS_CONTENT_LANGUAGE;
    COMPACT_HEADER_NAMES[CONTENT_LENGTH] = BS_CONTENT_LENGTH_C;
    COMPACT_HEADER_NAMES[CONTENT_TYPE] = BS_CONTENT_TYPE_C;
    COMPACT_HEADER_NAMES[CSEQ] = BS_CSEQ;
    COMPACT_HEADER_NAMES[DATE] = BS_DATE;
    COMPACT_HEADER_NAMES[ERROR_INFO] = BS_ERROR_INFO;
    COMPACT_HEADER_NAMES[EXPIRES] = BS_EXPIRES;
    COMPACT_HEADER_NAMES[FROM] = BS_FROM_C;
    COMPACT_HEADER_NAMES[IN_REPLY_TO] = BS_IN_REPLY_TO;
    COMPACT_HEADER_NAMES[MAX_FORWARDS] = BS_MAX_FORWARDS;
    COMPACT_HEADER_NAMES[MIME_VERSION] = BS_MIME_VERSION;
    COMPACT_HEADER_NAMES[ORGANIZATION] = BS_ORGANIZATION;
    COMPACT_HEADER_NAMES[PRIORITY] = BS_PRIORITY;
    COMPACT_HEADER_NAMES[PROXY_AUTHENTICATE] = BS_PROXY_AUTHENTICATE;
    COMPACT_HEADER_NAMES[PROXY_AUTHORIZATION] = BS_PROXY_AUTHORIZATION;
    COMPACT_HEADER_NAMES[PROXY_REQUIRE] = BS_PROXY_REQUIRE;
    COMPACT_HEADER_NAMES[RACK] = BS_RACK;
    COMPACT_HEADER_NAMES[RECORD_ROUTE] = BS_RECORD_ROUTE;
    COMPACT_HEADER_NAMES[REQUIRE] = BS_REQUIRE;
    COMPACT_HEADER_NAMES[RETRY_AFTER] = BS_RETRY_AFTER;
    COMPACT_HEADER_NAMES[ROUTE] = BS_ROUTE;
    COMPACT_HEADER_NAMES[RSEQ] = BS_RSEQ;
    COMPACT_HEADER_NAMES[SERVER] = BS_SERVER;
    COMPACT_HEADER_NAMES[SUBJECT] = BS_SUBJECT_C;
    COMPACT_HEADER_NAMES[SUPPORTED] = BS_SUPPORTED_C;
    COMPACT_HEADER_NAMES[TIMESTAMP] = BS_TIMESTAMP;
    COMPACT_HEADER_NAMES[TRANSLATE] = BS_TRANSLATE;
    COMPACT_HEADER_NAMES[TO] = BS_TO_C;
    COMPACT_HEADER_NAMES[UNSUPPORTED] = BS_UNSUPPORTED;
    COMPACT_HEADER_NAMES[USER_AGENT] = BS_USER_AGENT;
    COMPACT_HEADER_NAMES[VIA] = BS_VIA_C;
    COMPACT_HEADER_NAMES[WARNING] = BS_WARNING;
    COMPACT_HEADER_NAMES[WWW_AUTHENTICATE] = BS_WWW_AUTHENTICATE;
    COMPACT_HEADER_NAMES[SERVICE_AGENT_PHASE] = BS_SERVICE_AGENT_PHASE;
    COMPACT_HEADER_NAMES[SERVICE_AGENT_CONTEXT] = BS_SERVICE_AGENT_CONTEXT;
    COMPACT_HEADER_NAMES[SERVICE_AGENT_APPLICATION] = BS_SERVICE_AGENT_APPLICATION;
    COMPACT_HEADER_NAMES[SERVICE_AGENT_ROUTE] = BS_SERVICE_AGENT_ROUTE;
    COMPACT_HEADER_NAMES[SESSION_EXPIRES] = BS_SESSION_EXPIRES_C;
    COMPACT_HEADER_NAMES[REMOTE_PARTY_ID] = BS_REMOTE_PARTY_ID;
    COMPACT_HEADER_NAMES[EVENT] = BS_EVENT_C;
    COMPACT_HEADER_NAMES[SUBSCRIPTION_EXPIRES] = BS_SUBSCRIPTION_EXPIRES;
    COMPACT_HEADER_NAMES[SUBSCRIPTION_STATE] = BS_SUBSCRIPTION_STATE;
    COMPACT_HEADER_NAMES[ALLOW_EVENTS] = BS_ALLOW_EVENTS_C;
    COMPACT_HEADER_NAMES[CONTENT_VERSION] = BS_CONTENT_VERSION;
    COMPACT_HEADER_NAMES[AE_COOKIE] = BS_AE_COOKIE;
    COMPACT_HEADER_NAMES[X_APPLICATION] = BS_X_APPLICATION;
    COMPACT_HEADER_NAMES[X_APPLICATION_CONTEXT] = BS_X_APPLICATION_CONTEXT;
    COMPACT_HEADER_NAMES[X_FROM_OUTSIDE] = BS_X_FROM_OUTSIDE;
    // per Edgar, connection info header is no longer used by AE
    COMPACT_HEADER_NAMES[X_CONNECTION_INFO] = BS_X_CONNECTION_INFO;
    COMPACT_HEADER_NAMES[AS_PATH] = BS_AS_PATH;
    COMPACT_HEADER_NAMES[AS_RECORD_PATH] = BS_AS_RECORD_PATH;
    COMPACT_HEADER_NAMES[REPLY_TO] = BS_REPLY_TO;
    COMPACT_HEADER_NAMES[REFER_TO] = BS_REFER_TO_C;
    COMPACT_HEADER_NAMES[MIN_EXPIRES] = BS_MIN_EXPIRES;
    COMPACT_HEADER_NAMES[PATH] = BS_PATH;
    COMPACT_HEADER_NAMES[P_ASSERTED_IDENTITY] = BS_P_ASSERTED_IDENTITY;
    COMPACT_HEADER_NAMES[P_PREFERRED_IDENTITY] = BS_P_PREFERRED_IDENTITY;
    COMPACT_HEADER_NAMES[REPLACES] = BS_REPLACES;
    COMPACT_HEADER_NAMES[DIVERSION] = BS_DIVERSION;
    COMPACT_HEADER_NAMES[P_ASSOCIATED_URI] = BS_P_ASSOCIATED_URI;
    COMPACT_HEADER_NAMES[UNKNOWN_HEADER] = BS_UNKNOWN_HEADER;
    COMPACT_HEADER_NAMES[SIP_URL_ID] = BS_SIP_URL_ID;
    COMPACT_HEADER_NAMES[TEL_URL_ID] = BS_TEL_URL_ID;
    COMPACT_HEADER_NAMES[UNKNOWN_URL_ID] = BS_UNKNOWN_URL_ID;
    COMPACT_HEADER_NAMES[NAME_ADDR_ID] = BS_NAME_ADDR_ID;
    COMPACT_HEADER_NAMES[ENTIRE_MSG_ID] = BS_ENTIRE_MSG_ID;
    COMPACT_HEADER_NAMES[P_CALLED_PARTY_ID] = BS_P_CALLED_PARTY_ID;
    COMPACT_HEADER_NAMES[P_ACCESS_NETWORK_INFO] = BS_P_ACCESS_NETWORK_INFO;
    COMPACT_HEADER_NAMES[SERVICE_ROUTE] = BS_SERVICE_ROUTE;
    COMPACT_HEADER_NAMES[PRIVACY] = BS_PRIVACY;
    COMPACT_HEADER_NAMES[P_DCS_LAES] = BS_P_DCS_LAES;
    COMPACT_HEADER_NAMES[P_CHARGING_FUNCTION_ADDRESSES] = BS_P_CHARGING_FUNCTION_ADDRESSES;
    COMPACT_HEADER_NAMES[P_CHARGING_VECTOR] = BS_P_CHARGING_VECTOR;
    COMPACT_HEADER_NAMES[P_VISITED_NETWORK_ID] = BS_P_VISITED_NETWORK_ID;
    COMPACT_HEADER_NAMES[REFERRED_BY] = BS_REFERRED_BY_C;

    // CAFFEINE 2.0 DEVELOPMENT - Additional Compact Header Names
    COMPACT_HEADER_NAMES[ACCEPT_CONTACT] = BS_ACCEPT_CONTACT_C;
    COMPACT_HEADER_NAMES[REJECT_CONTACT] = BS_REJECT_CONTACT_C;
    COMPACT_HEADER_NAMES[REQUEST_DISPOSITION] = BS_REQUEST_DISPOSITION_C;
    COMPACT_HEADER_NAMES[JOIN] = BS_JOIN;
    COMPACT_HEADER_NAMES[ETAG] = BS_ETAG;
    COMPACT_HEADER_NAMES[IF_MATCH] = BS_IF_MATCH;
    COMPACT_HEADER_NAMES[APP_INFO] = BS_APP_INFO;
    COMPACT_HEADER_NAMES[CONTENT_ID] = BS_CONTENT_ID;
    COMPACT_HEADER_NAMES[CONTENT_DESCRIPTION] = BS_CONTENT_DESCRIPTION;
    COMPACT_HEADER_NAMES[CONTENT_TRANSFER_ENCODING] = BS_CONTENT_TRANSFER_ENCODING;
    COMPACT_HEADER_NAMES[CISCO_MAINTENANCE_MODE] = BS_CISCO_MAINTENANCE_MODE;
    COMPACT_HEADER_NAMES[CISCO_GUID] = BS_CISCO_GUID;
    COMPACT_HEADER_NAMES[REASON_HEADER] =
        BS_REASON_HEADER; // MMA - 08.05.2005 - adding support for the Reason header (RFC 3326)
    COMPACT_HEADER_NAMES[CID_URL_ID] = BS_CID_URL_ID;
    COMPACT_HEADER_NAMES[SUPPRESS_IF_MATCH] = BS_SUPPRESS_IF_MATCH;
    COMPACT_HEADER_NAMES[TARGET_DIALOG] = BS_TARGET_DIALOG;
    COMPACT_HEADER_NAMES[HISTORY_INFO] = BS_HISTORY_INFO;
    COMPACT_HEADER_NAMES[X_CISCO_RAI] = BS_X_CISCO_RAI;

    // Element Names initialization
    ELEMENT_NAMES[ADDR_SPEC] = BSU_ADDR_SPEC;
    ELEMENT_NAMES[CALLID] = BSU_CALLID;
    ELEMENT_NAMES[CNONCE_VALUE] = BSU_CNONCE_VALUE;
    ELEMENT_NAMES[COMMENT] = BSU_COMMENT;
    ELEMENT_NAMES[CONTENT_CODING] = BSU_CONTENT_CODING;
    ELEMENT_NAMES[DELAY] = BSU_DELAY;
    ELEMENT_NAMES[DELTA_SECONDS] = BSU_DELTA_SECONDS;
    ELEMENT_NAMES[DISPLAY_NAME] = BSU_DISPLAY_NAME;
    ELEMENT_NAMES[DISPOSITION_TYPE] = BSU_DISPOSITION_TYPE;
    ELEMENT_NAMES[DISP_EXTENSION_TOKEN] = BSU_DISP_EXTENSION_TOKEN;
    ELEMENT_NAMES[HOST] = BSU_HOST;
    ELEMENT_NAMES[NONCE_VALUE] = BSU_NONCE_VALUE;
    ELEMENT_NAMES[OPTION_TAG] = BSU_OPTION_TAG;
    ELEMENT_NAMES[OTHER_HANDLING] = BSU_OTHER_HANDLING;
    ELEMENT_NAMES[OTHER_PRIORITY] = BSU_OTHER_PRIORITY;
    ELEMENT_NAMES[PASSWORD] = BSU_PASSWORD;
    ELEMENT_NAMES[PORT] = BSU_PORT;
    ELEMENT_NAMES[PRIMARY_TAG] = BSU_PRIMARY_TAG;
    ELEMENT_NAMES[TAG] = BSU_TAG;
    ELEMENT_NAMES[PRODUCT] = BSU_PRODUCT;
    ELEMENT_NAMES[PRODUCT_VERSION] = BSU_PRODUCT_VERSION;
    ELEMENT_NAMES[PROTOCOL_NAME] = BSU_PROTOCOL_NAME;
    ELEMENT_NAMES[PROTOCOL_VERSION] = BSU_PROTOCOL_VERSION;
    ELEMENT_NAMES[QOP_VALUE] = BSU_QOP_VALUE;
    ELEMENT_NAMES[QVALUE] = BSU_QVALUE;
    ELEMENT_NAMES[REALM_VALUE] = BSU_REALM_VALUE;
    ELEMENT_NAMES[SENT_BY] = BSU_SENT_BY;
    ELEMENT_NAMES[SENT_PROTOCOL] = BSU_SENT_PROTOCOL;
    ELEMENT_NAMES[SIP_DATE] = BSU_SIP_DATE;
    ELEMENT_NAMES[SIP_URL] = BSU_SIP_URL;
    ELEMENT_NAMES[SIPS_URL] = BSU_SIPS_URL;
    // CAFFEINE 2.0 DEVELOPMENT - Additional Element Name
    ELEMENT_NAMES[CID_URL] = BSU_CID_URL;
    ELEMENT_NAMES[SUBTAG] = BSU_SUBTAG;
    ELEMENT_NAMES[TRANSPORT] = BSU_TRANSPORT;
    ELEMENT_NAMES[TTL] = BSU_TTL;
    ELEMENT_NAMES[URI] = BSU_URI;
    ELEMENT_NAMES[USERNAME] = BSU_USERNAME;
    ELEMENT_NAMES[VIA_HIDDEN] = BSU_VIA_HIDDEN;
    ELEMENT_NAMES[WARN_AGENT] = BSU_WARN_AGENT;
    ELEMENT_NAMES[WARN_CODE] = BSU_WARN_CODE;
    ELEMENT_NAMES[WARN_TEXT] = BSU_WARN_TEXT;
    ELEMENT_NAMES[HTTP_URL] = BSU_HTTP_URL;
    ELEMENT_NAMES[TEL_URL] = BSU_TEL_URL;
    ELEMENT_NAMES[UNKNOWN_URL] = BSU_UNKNOWN_URL;
    ELEMENT_NAMES[CSEQ_METHOD] = BSU_CSEQ_METHOD;
    ELEMENT_NAMES[CSEQ_NUMBER] = BSU_CSEQ_NUMBER;
    ELEMENT_NAMES[WILDCARD] = BSU_WILDCARD;
    ELEMENT_NAMES[ACTION] = BSU_ACTION;
    ELEMENT_NAMES[EXPIRES_VALUE] = BSU_EXPIRES_VALUE;
    ELEMENT_NAMES[SINGLE_VALUE] = BSU_SINGLE_VALUE;
    ELEMENT_NAMES[MADDR] = BSU_MADDR;
    ELEMENT_NAMES[BRANCH] = BSU_BRANCH;
    ELEMENT_NAMES[HIDDEN] = BSU_HIDDEN;
    ELEMENT_NAMES[RECEIVED] = BSU_RECEIVED;
    ELEMENT_NAMES[USER] = BSU_USER;
    ELEMENT_NAMES[METHOD] = BSU_METHOD;
    ELEMENT_NAMES[HANDLING] = BSU_HANDLING;
    ELEMENT_NAMES[LANGUAGE_TAG] = BSU_LANGUAGE_TAG;
    ELEMENT_NAMES[LANGUAGE_SUBTAG] = BSU_LANGUAGE_SUBTAG;
    ELEMENT_NAMES[YEAR] = BSU_YEAR;
    ELEMENT_NAMES[MONTH] = BSU_MONTH;
    ELEMENT_NAMES[DAY_OF_WEEK] = BSU_DAY_OF_WEEK;
    ELEMENT_NAMES[DAY_OF_MONTH] = BSU_DAY_OF_MONTH;
    ELEMENT_NAMES[HOUR] = BSU_HOUR;
    ELEMENT_NAMES[MINUTE] = BSU_MINUTE;
    ELEMENT_NAMES[SECOND] = BSU_SECOND;
    ELEMENT_NAMES[GMT] = BSU_GMT;
    ELEMENT_NAMES[TIME_ZONE] = BSU_TIME_ZONE;
    ELEMENT_NAMES[TYPE] = BSU_TYPE;
    ELEMENT_NAMES[SUB_TYPE] = BSU_SUB_TYPE;
    ELEMENT_NAMES[TIMESTAMP_VALUE] = BSU_TIMESTAMP_VALUE;
    ELEMENT_NAMES[PURPOSE] = BSU_PURPOSE;
    ELEMENT_NAMES[DURATION] = BSU_DURATION;
    ELEMENT_NAMES[NC] = BSU_NC;
    ELEMENT_NAMES[QOP] = BSU_QOP;
    ELEMENT_NAMES[AUTH] = BSU_AUTH;
    ELEMENT_NAMES[REALM] = BSU_REALM;
    ELEMENT_NAMES[NONCE] = BSU_NONCE;
    ELEMENT_NAMES[STALE] = BSU_STALE;
    ELEMENT_NAMES[CNONCE] = BSU_CNONCE;
    ELEMENT_NAMES[DOMAIN] = BSU_DOMAIN;
    ELEMENT_NAMES[OPAQUE] = BSU_OPAQUE;
    ELEMENT_NAMES[RESPONSE] = BSU_RESPONSE;
    ELEMENT_NAMES[NEXTNONCE] = BSU_NEXTNONCE;
    ELEMENT_NAMES[ALGORITHM] = BSU_ALGORITHM;
    ELEMENT_NAMES[BASIC_COOKIE] = BSU_BASIC;
    ELEMENT_NAMES[PARTY] = BSU_PARTY;
    ELEMENT_NAMES[SCREEN] = BSU_SCREEN;
    ELEMENT_NAMES[ID_TYPE] = BSU_ID_TYPE;
    ELEMENT_NAMES[PRIVACY_ID] = BSU_PRIVACY;
    ELEMENT_NAMES[REFRESHER] = BSU_REFRESHER;
    ELEMENT_NAMES[RESPONSE_NUMBER] = BSU_RESPONSE_NUMBER;
    ELEMENT_NAMES[TEL_URL_NUMBER] = BSU_TEL_URL_NUMBER;
    ELEMENT_NAMES[TSP] = BSU_TSP;
    ELEMENT_NAMES[ISUB] = BSU_ISUB;
    ELEMENT_NAMES[POSTD] = BSU_POSTD;
    ELEMENT_NAMES[PHONE_CONTEXT] = BSU_PHONE_CONTEXT;
    ELEMENT_NAMES[EVENT_PACKAGE] = BSU_EVENT_PACKAGE;
    ELEMENT_NAMES[EVENT_SUB_PACKAGE] = BSU_EVENT_SUB_PACKAGE;
    ELEMENT_NAMES[HEADER_NAME] = BSU_HEADER_NAME;
    ELEMENT_NAMES[URI_SCHEME] = BSU_URI_SCHEME;
    ELEMENT_NAMES[URI_DATA] = BSU_URI_DATA;
    ELEMENT_NAMES[NAT] = BSU_NAT;
    ELEMENT_NAMES[NAME_ADDR] = BSU_NAME_ADDR;
    ELEMENT_NAMES[RECEIVED_PORT] = BSU_RECEIVED_PORT;
    ELEMENT_NAMES[FORKING_ID] = BSU_FORKING_ID;
    ELEMENT_NAMES[PARAMETERS] = BSU_PARAMETERS;
    ELEMENT_NAMES[LR] = BSU_LR;
    ELEMENT_NAMES[COMP] = BSU_COMP;
    ELEMENT_NAMES[ACTIVE] = BSU_ACTIVE;
    ELEMENT_NAMES[PENDING] = BSU_PENDING;
    ELEMENT_NAMES[TERMINATED] = BSU_TERMINATED;
    ELEMENT_NAMES[REASON] = BSU_REASON;
    ELEMENT_NAMES[RETRY_AFTER_VALUE] = BSU_RETRY_AFTER_VALUE;
    ELEMENT_NAMES[DEACTIVATED] = BSU_DEACTIVATED;
    ELEMENT_NAMES[PROBATION] = BSU_PROBATION;
    ELEMENT_NAMES[REJECTED] = BSU_REJECTED;
    ELEMENT_NAMES[TIMEOUT] = BSU_TIMEOUT;
    ELEMENT_NAMES[GIVEUP] = BSU_GIVEUP;
    ELEMENT_NAMES[NORESOURCE] = BSU_NORESOURCE;
    ELEMENT_NAMES[ID] = BSU_ID;

    // Initialize the (String to Integer) methods map
    for (int i = 0; i < METHOD_NAMES.length; i++) {
      Integer id = new Integer(i);
      methodsMap.put(METHOD_NAMES[i], id);
    }

    // Initialize the (String to Integer) headers map
    for (int i = 0; i <= MAX_KNOWN_HEADER; i++) {
      Integer id = new Integer(i);
      // Canonical
      headersMap.put(HEADER_NAMES[i], id);
      // Normalized
      headersMap.put(HEADER_NAMES[i].toUpperCase(), id);
    }
    // compact form - normalized
    headersMap.put(BS_ALLOW_EVENTS_C.toUpperCase(), new Integer(ALLOW_EVENTS));
    headersMap.put(BS_CALL_ID_C.toUpperCase(), new Integer(CALL_ID));
    headersMap.put(BS_CONTACT_C.toUpperCase(), new Integer(CONTACT));
    headersMap.put(BS_CONTENT_ENCODING_C.toUpperCase(), new Integer(CONTENT_ENCODING));
    headersMap.put(BS_CONTENT_LENGTH_C.toUpperCase(), new Integer(CONTENT_LENGTH));
    headersMap.put(BS_CONTENT_TYPE_C.toUpperCase(), new Integer(CONTENT_TYPE));
    headersMap.put(BS_EVENT_C.toUpperCase(), new Integer(EVENT));
    headersMap.put(BS_FROM_C.toUpperCase(), new Integer(FROM));
    headersMap.put(BS_SUBJECT_C.toUpperCase(), new Integer(SUBJECT));
    headersMap.put(BS_SUPPORTED_C.toUpperCase(), new Integer(SUPPORTED));
    headersMap.put(BS_TO_C.toUpperCase(), new Integer(TO));
    headersMap.put(BS_VIA_C.toUpperCase(), new Integer(VIA));
    headersMap.put(BS_SESSION_EXPIRES_C.toUpperCase(), new Integer(SESSION_EXPIRES));
    headersMap.put(BS_REFER_TO_C.toUpperCase(), new Integer(REFER_TO));
    // CAFFEINE 2.0 DEVELOPMENT - Additional compact form headers
    headersMap.put(BS_ACCEPT_CONTACT_C.toUpperCase(), new Integer(ACCEPT_CONTACT));
    headersMap.put(BS_REJECT_CONTACT_C.toUpperCase(), new Integer(REJECT_CONTACT));
    headersMap.put(BS_REQUEST_DISPOSITION_C.toUpperCase(), new Integer(REQUEST_DISPOSITION));
    headersMap.put(BS_REFERRED_BY_C.toUpperCase(), new Integer(REFERRED_BY));

    // compact form - canonical
    headersMap.put(BS_ALLOW_EVENTS_C, new Integer(ALLOW_EVENTS));
    headersMap.put(BS_CALL_ID_C, new Integer(CALL_ID));
    headersMap.put(BS_CONTACT_C, new Integer(CONTACT));
    headersMap.put(BS_CONTENT_ENCODING_C, new Integer(CONTENT_ENCODING));
    headersMap.put(BS_CONTENT_LENGTH_C, new Integer(CONTENT_LENGTH));
    headersMap.put(BS_CONTENT_TYPE_C, new Integer(CONTENT_TYPE));
    headersMap.put(BS_EVENT_C, new Integer(EVENT));
    headersMap.put(BS_FROM_C, new Integer(FROM));
    headersMap.put(BS_SUBJECT_C, new Integer(SUBJECT));
    headersMap.put(BS_SUPPORTED_C, new Integer(SUPPORTED));
    headersMap.put(BS_TO_C, new Integer(TO));
    headersMap.put(BS_VIA_C, new Integer(VIA));
    headersMap.put(BS_SESSION_EXPIRES_C, new Integer(SESSION_EXPIRES));
    headersMap.put(BS_REFER_TO_C, new Integer(REFER_TO));
    // CAFFEINE 2.0 DEVELOPMENT - Additional compact form headers
    headersMap.put(BS_ACCEPT_CONTACT_C, new Integer(ACCEPT_CONTACT));
    headersMap.put(BS_REJECT_CONTACT_C, new Integer(REJECT_CONTACT));
    headersMap.put(BS_REQUEST_DISPOSITION_C, new Integer(REQUEST_DISPOSITION));
    headersMap.put(BS_REFERRED_BY_C, new Integer(REFERRED_BY));

    // DsPerf register
    ENTIRE_PARSE = DsPerf.addType("\nEntire Msg Parse               ");
    PARSE_START_LINE = DsPerf.addType("  Parse Start Line             ");
    PARSE_HEADERS = DsPerf.addType("  Parse Headers                ");
    DEEP_PARSE_HEADERS = DsPerf.addType("    Deep Parse Headers         ");
    FIRE_ELEMENT = DsPerf.addType("    Fire Element               ");
    FIRE_PARAMETER = DsPerf.addType("    Fire Parameter             ");
    PARSE_VIA = DsPerf.addType("    Parse Via                  ");
    PARSE_BODY = DsPerf.addType("  Parse Body                   ");
    PARSE_INT = DsPerf.addType("Parse Int                      ");
    PARSE_LONG = DsPerf.addType("Parse Long                     ");
    PARSE_FLOAT = DsPerf.addType("Parse Float                    ");
    PARSE_SIP_URL_DATA = DsPerf.addType("Parse SIP URL Data             ");
    // CAFFEINE 2.0 DEVELOPMENT - Additional DsPerf data
    PARSE_CID_URL_DATA = DsPerf.addType("Parse CID URL Data             ");
    PARSE_TEL_URL_DATA = DsPerf.addType("Parse TEL URL Data             ");
    PARSE_URL_HEADER = DsPerf.addType("Parse URL Header               ");
    PARSE_NAME_ADDR = DsPerf.addType("Parse Name Addr                ");
  }

  /**
   * Returns the method name from its integer constant id.
   *
   * @param id the integer id of the method.
   * @return the method name from its integer constant id.
   */
  public static DsByteString getMethod(int id) {
    if (id < 0 || id >= METHOD_NAMES.length) {
      id = UNKNOWN;
    }
    return METHOD_NAMES[id];
  }

  /**
   * Returns the method id for the specified <code>name</code> method name.
   *
   * @param name the method name.
   * @return the method id for the specified <code>name</code> method name.
   */
  public static int getMethod(DsByteString name) {
    // method names are case sensitive
    Integer id = (Integer) methodsMap.get(name);

    if (id != null) {
      return id.intValue();
    } else {
      return UNKNOWN;
    }
  }

  /**
   * Returns the method name as DsByteString object for the specified <code>name</code> method name.
   * If the method is one of the known methods then the constant DsByteString object, defined for
   * that method name, is returned, otherwise a new DsByteString object is created with the name
   * specified in the byte array.
   *
   * @param name the method name as byte array.
   * @param offset the offset where from the method name starts in the byte array.
   * @param count the number of bytes in the method name.
   * @return the method name as DsByteString object.
   */
  public static DsByteString toMethodBS(byte[] name, int offset, int count) {
    int id = getMethod(name, offset, count);
    if (id == UNKNOWN) {
      return new DsByteString(name, offset, count);
    }
    return getMethod(id);
  }

  /**
   * Returns the method id for the specified <code>data</code> method name.
   *
   * @param data the method name as byte array.
   * @param offset the offset where from the method name starts in the byte array.
   * @param count the number of bytes in the method name.
   * @return the method id for the specified method name.
   */
  public static int getMethod(byte[] data, int offset, int count) {
    int i = offset;

    // Method names are case sensitive - jsm
    switch (count) {
      case 3:
        if (data[i] == 'A' && data[i + 1] == 'C' && data[i + 2] == 'K') {
          return ACK;
        } else if (data[i] == 'B' && data[i + 1] == 'Y' && data[i + 2] == 'E') {
          return BYE;
        }
        break;
      case 4:
        if (data[i] == 'I' && data[i + 1] == 'N' && data[i + 2] == 'F' && data[i + 3] == 'O') {
          return INFO;
        } else if (data[i] == 'P'
            && data[i + 1] == 'I'
            && data[i + 2] == 'N'
            && data[i + 3] == 'G') {
          return PING;
        }
        break;
      case 5:
        if (data[i] == 'P'
            && data[i + 1] == 'R'
            && data[i + 2] == 'A'
            && data[i + 3] == 'C'
            && data[i + 4] == 'K') {
          return PRACK;
        } else if (data[i] == 'R'
            && data[i + 1] == 'E'
            && data[i + 2] == 'F'
            && data[i + 3] == 'E'
            && data[i + 4] == 'R') {
          return REFER;
        }
        break;
      case 6:
        if (data[i] == 'I'
            && data[i + 1] == 'N'
            && data[i + 2] == 'V'
            && data[i + 3] == 'I'
            && data[i + 4] == 'T'
            && data[i + 5] == 'E') {
          return INVITE;
        } else if (data[i] == 'N'
            && data[i + 1] == 'O'
            && data[i + 2] == 'T'
            && data[i + 3] == 'I'
            && data[i + 4] == 'F'
            && data[i + 5] == 'Y') {
          return NOTIFY;
        } else if (data[i] == 'C'
            && data[i + 1] == 'A'
            && data[i + 2] == 'N'
            && data[i + 3] == 'C'
            && data[i + 4] == 'E'
            && data[i + 5] == 'L') {
          return CANCEL;
        } else if (data[i] == 'U'
            && data[i + 1] == 'P'
            && data[i + 2] == 'D'
            && data[i + 3] == 'A'
            && data[i + 4] == 'T'
            && data[i + 5] == 'E') {
          return UPDATE;
        }
        break;
      case 7:
        if (data[i] == 'M'
            && data[i + 1] == 'E'
            && data[i + 2] == 'S'
            && data[i + 3] == 'S'
            && data[i + 4] == 'A'
            && data[i + 5] == 'G'
            && data[i + 6] == 'E') {
          return MESSAGE;
        } else if (data[i] == 'O'
            && data[i + 1] == 'P'
            && data[i + 2] == 'T'
            && data[i + 3] == 'I'
            && data[i + 4] == 'O'
            && data[i + 5] == 'N'
            && data[i + 6] == 'S') {
          return OPTIONS;
        } else if (data[i] == 'P'
            && data[i + 1] == 'U'
            && data[i + 2] == 'B'
            && data[i + 3] == 'L'
            && data[i + 4] == 'I'
            && data[i + 5] == 'S'
            && data[i + 6] == 'H') {
          return PUBLISH;
        }
        break;
      case 8:
        if (data[i] == 'R'
            && data[i + 1] == 'E'
            && data[i + 2] == 'G'
            && data[i + 3] == 'I'
            && data[i + 4] == 'S'
            && data[i + 5] == 'T'
            && data[i + 6] == 'E'
            && data[i + 7] == 'R') {
          return REGISTER;
        }
        break;
      case 9:
        if (data[i] == 'S'
            && data[i + 1] == 'U'
            && data[i + 2] == 'B'
            && data[i + 3] == 'S'
            && data[i + 4] == 'C'
            && data[i + 5] == 'R'
            && data[i + 6] == 'I'
            && data[i + 7] == 'B'
            && data[i + 8] == 'E') {
          return SUBSCRIBE;
        }
        break;
    }

    // check for user registered methods
    for (int j = METHOD_NAMES_SIZE; j < METHOD_NAMES.length; j++) {
      if (METHOD_NAMES[j].equals(data, offset, count)) {
        return j;
      }
    }

    return UNKNOWN;
  } // ends getMethod

  /**
   * Returns the canonical header name from its integer constant id.
   *
   * @param id the integer id of the header.
   * @return the canonical header name byte string for this header.
   */
  public static DsByteString getHeader(int id) {
    if (id < 0 || id >= HEADER_NAMES.length) {
      id = UNKNOWN_HEADER;
    }
    return HEADER_NAMES[id];
  }

  /**
   * Returns the header id for the specified <code>name</code> header name.
   *
   * @param name the header name.
   * @return the header id for the specified <code>name</code> header name.
   */
  public static int getHeader(DsByteString name) {
    Integer id = (Integer) headersMap.get(name);
    return (id != null)
        ? id.intValue() // got canonical
        : ((id = (Integer) headersMap.get(name.toUpperCase())) != null)
            ? id.intValue() // got Normalized
            : UNKNOWN_HEADER; // got nothing
  }

  /**
   * Returns the compact header name from its integer constant id. If no compact value is assigned
   * to this header, then its complete canonical name will be returned.
   *
   * @param id the integer id of the header.
   * @return the compact or canonical header name byte string for this header.
   */
  public static DsByteString getHeaderCompact(int id) {
    if (id < 0 || id > COMPACT_HEADER_NAMES.length) {
      id = UNKNOWN_HEADER;
    }
    return COMPACT_HEADER_NAMES[id];
  }

  //////////////////////////////////////////////////////////////////////////////
  static {
    // build the parse tree for header names
    // need to change from .length to last header
    for (int i = 0; i <= MAX_KNOWN_HEADER; i++) {
      switch (i) {
          // handle compact form
        case ALLOW_EVENTS:
          // CAFFEINE 2.0 DEVELOPMENT - Additional header support
        case ACCEPT_CONTACT:
        case CALL_ID:
        case CONTACT:
        case CONTENT_ENCODING:
        case CONTENT_LENGTH:
        case CONTENT_TYPE:
        case EVENT:
        case FROM:
        case REFER_TO:
        case REFERRED_BY:
          // CAFFEINE 2.0 DEVELOPMENT - Additional headers support
        case REJECT_CONTACT:
        case REQUEST_DISPOSITION:
        case SESSION_EXPIRES:
        case SUBJECT:
        case SUPPORTED:
        case TO:
        case VIA:
          addHeaderType(HEADER_NAMES[i], COMPACT_HEADER_NAMES[i], i);
          break;
        default:
          addHeaderType(HEADER_NAMES[i], null, i);
          break;
      }
    }
  }

  /**
   * Enables registration of user defined elements. Element IDs are used to inform the parser
   * listeners about what the parser found. If the parser that you are creating does not use the
   * pre-defined IDs, new ones can be created here.
   *
   * @param name the name of the ID (preferably in upper case), used only for debugging; it should
   *     be unique when compared to the other names in ELEMENT_NAMES.
   * @return the new element ID that represents the element <code>name</code>.
   */
  public static int registerElement(DsByteString name) {
    DsByteString[] newNames = new DsByteString[ELEMENT_NAMES.length + 1];
    System.arraycopy(ELEMENT_NAMES, 0, newNames, 0, ELEMENT_NAMES.length);
    newNames[newNames.length - 1] = name;
    ELEMENT_NAMES = newNames;

    return (ELEMENT_NAMES.length - 1);
  }

  /**
   * Enables registration of user defined headers. When parsing a SIP message and an instance of
   * this type of header is encountered, * <code>parser.parseHeader()</code> will be called.
   *
   * @param longName the cannonical form of the header name, must not be <code>null</code>
   * @param compactName the compact form of the header name, must not be <code>null</code>
   * @param isSingular pass <code>true</code> is this header can only appear once, or <code>false
   *     </code> if a list of these headers can appear in a message
   * @param parser the header parser to be used to parse this type of header
   * @return the integer header ID that will be used to refer to this type of header
   */
  public static int registerHeader(
      DsByteString longName,
      DsByteString compactName,
      boolean isSingular,
      DsSipHeaderParserInterface parser) {
    // we should probably make sure that the same header is not registered twice

    // add header to arrays
    DsByteString[] newHeaderNames = new DsByteString[HEADER_NAMES.length + 1];
    System.arraycopy(HEADER_NAMES, 0, newHeaderNames, 0, HEADER_NAMES.length);
    newHeaderNames[newHeaderNames.length - 1] = longName;
    HEADER_NAMES = newHeaderNames;

    // add compact header to arrays
    DsByteString[] newCompactHeaderNames = new DsByteString[COMPACT_HEADER_NAMES.length + 1];
    System.arraycopy(
        COMPACT_HEADER_NAMES, 0, newCompactHeaderNames, 0, COMPACT_HEADER_NAMES.length);
    if (compactName != null) {
      newCompactHeaderNames[newCompactHeaderNames.length - 1] = compactName;
      headersMap.put(compactName, new Integer(COMPACT_HEADER_NAMES.length - 1));
    } else {
      newCompactHeaderNames[newCompactHeaderNames.length - 1] = longName;
    }
    COMPACT_HEADER_NAMES = newCompactHeaderNames;

    headersMap.put(longName, new Integer(HEADER_NAMES.length - 1));

    // add header to parse tree
    addHeaderType(longName, compactName, HEADER_NAMES.length - 1);

    // add parser mapping to header id
    addParserMapping(HEADER_NAMES.length - 1, parser);

    // pass isSingular to DsSipMessage
    // ... figure out if we really need this or just isSingular is always false for ext headers

    // return new int value for this header
    return HEADER_NAMES.length - 1;
  }

  protected static void addParserMapping(int id, DsSipHeaderParserInterface parser) {
    parserMap.put(id, parser);
  }

  /**
   * Enables registration of used defined methods. When adding new method types to the JUA, <code>
   * DsSipDefaultMessageListenerFactory</code> must be extended and the requestBegin() method
   * overridden. All methods that are overridden there must be added to the message parser by
   * calling this method. This ensures that <code>DsSipMsgParser.getMethod()</code> returns the
   * correct value.
   *
   * @param name the case-sensitive name of the method to be registered, must not be <code>null
   *     </code>
   * @return the integer method ID that will be used to refer to this type of method
   */
  public static int registerMethod(DsByteString name) {
    // we should probably make sure that the same method is not registered twice

    DsByteString[] newMethodNames = new DsByteString[METHOD_NAMES.length + 1];
    System.arraycopy(METHOD_NAMES, 0, newMethodNames, 0, METHOD_NAMES.length);
    newMethodNames[newMethodNames.length - 1] = name;
    METHOD_NAMES = newMethodNames;

    methodsMap.put(name, new Integer(METHOD_NAMES.length - 1));

    return METHOD_NAMES.length - 1;
  }

  /** Protected default constructor. Disallow instance construction. */
  protected DsMsgParserBase() {}

  /**
   * Parses a header from a SIP message.
   *
   * @param headerListener where to report the results of parsed header.
   * @param mb the message bytes that contains the SIP message to parse.
   * @return <code>true</code> if there are more headers to parse.
   * @throws DsSipParserException if there is an exception while parsing.
   * @throws DsSipParserListenerException if the listener throws this exception.
   */
  protected static boolean parseHeader(DsSipHeaderListener headerListener, MsgBytes mb)
      throws DsSipParserListenerException, DsSipParserException {
    byte[] msg = mb.msg;

    int type = UNKNOWN_HEADER;
    int start = mb.i;

    try {
      switch (msg[mb.i]) {
        case '\r':
          mb.i++; // next char must be \n - fall through (empty header)
        case '\n':
          mb.i++;
          return false; // no more headers to parse
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      // the final EOL was missing (separator between headers and body)
      return false; // no more headers to parse
    }

    // get first node

    byte ch = msg[mb.i++];
    // character known to be ASCII - optimize - jsm
    // optimize further by & with (256-32)
    if (ch <= 'Z' && ch >= 'A') {
      ch += 32; // 'a' - 'A' (97 - 65)
    }

    DsParseTreeNode next = parseTree[ch];

    // try to match header token
    while (next != null) {
      // possible end node
      if (next.match >= 0) {
        lws(mb);
        if (msg[mb.i] == ':') {
          type = next.match;
          mb.i++;
          break; // found
        }
      } else if (msg[mb.i] == ':') {
        break;
      }

      // next = next.next(msg[mb.i++]);

      // BEGIN - manual inlining of above line of code
      byte ich = msg[mb.i++];

      // int index = next.indexOf(ich);

      // BEGIN - second level inline
      int index = -1;

      if (next.nextChars != null) {
        // character known to be ASCII - optimize - jsm
        if (ich <= 'Z' && ich >= 'A') {
          ich += 32; // 'a' - 'A' (97 - 65)
        }

        for (int i = 0; i < next.nextChars.length; i++) {
          if (ich == next.nextChars[i]) {
            index = i;
            break;
          }
        }
      }

      // END - second level inline

      if (index == -1) {
        next = null;
      } else {
        next = next.next[index];
      }

      // END - manual inlining of above line of code
    }

    int unknownStart = start;

    // special case for unknown headers, must find the rest of the name and the ":"
    if (type == UNKNOWN_HEADER) {
      int end = mb.i;
      boolean foundColon = (msg[mb.i] == ':');

      if (foundColon) {
        mb.i++;
      }

      while (!foundColon) {
        switch (msg[mb.i]) {
          case ':':
            {
              end = mb.i;
              foundColon = true;
              mb.i++;
              break;
            }
          case ' ':
          case '\t':
          case '\r':
            {
              end = mb.i;
              lws(mb);
              break;
            }
          case '\n':
            {
              end = mb.i;
              lws(mb);
              if (mb.i == end) // fix for infinite loop - may need to revisit
              {
                // addException(new DsSipParserException("Malformed SIP message", -1, "", false));

                // exceptions.setSipMessage(currentMessage); // centralize

                // throw exceptions;
                // return false; // ERROR - no more headers to parse
                // CAFFEINE 2.0 DEVELOPMENT
                // Bugid: CSCef82808 Via header without host won't be detected when doing deep
                // parsing
                // Unlike the default case below, we can not pass this one without warning

                // CAFFEINE 3.1 DEVELOPMENT - Codenomicon test
                // make sure that we move to the next byte
                ++mb.i;

                throw generateDsSipParserException(
                    headerListener, "Unknown header found", UNKNOWN_HEADER, msg, start, msg.length);
              }
              break;
            }
          default:
            {
              mb.i++;
              if (mb.i >= (mb.offset + mb.count)) {
                // make sure we don't go passed the end of the array
                // this only happens with certain malformed messages
                // addException(new DsSipParserException("Malformed SIP message", -1, "", false));

                // exceptions.setSipMessage(currentMessage); // centralize

                // throw exceptions;
                return false; // ERROR - no more headers to parse
              }
              break;
            }
        }
      }
    }

    lws(mb);
    start = mb.i;

    int headerCount = 0;

    // lazy pass over the header, find the logical EOL

    if (msg[mb.i]
        != '\n') // hack for possible bad lws() and to handle blank headers - revisit this - jsm
    {
      while (true) {
        if (msg[mb.i++] == '\n') {
          // GOGONG 09/22/05 CSCsb92399: already reached end of header. no more to parse
          if ((mb.i >= msg.length) || !(msg[mb.i] == ' ' || msg[mb.i] == '\t')) {
            if (msg[mb.i - 2] == '\r') {
              headerCount = mb.i - start - 2;
            } else {
              headerCount = mb.i - start - 1;
            }
            break;
          }
        }
      }
    } else {
      ++mb.i;
    }

    if (type != UNKNOWN_HEADER) {
      parseHeader(headerListener, type, msg, start, headerCount);
    } else {
      int unknownCount = start - unknownStart;
      parseHeader(headerListener, type, msg, unknownStart, headerCount + unknownCount);
    }

    // GOGONG 09/22/05 CSCsb92399 will return false when no more to parse
    return (mb.i < msg.length);
  }

  /**
   * Parses a SIP header in the form name:value.
   *
   * @param headerListener where to report the results of parsed header.
   * @param data the byte array that contains the SIP message to parse.
   * @param offset the start of the SIP message in the array.
   * @param count the number of bytes in the SIP message.
   * @throws DsSipParserException if there is an exception while parsing.
   * @throws DsSipParserListenerException if the listener throws this exception.
   */
  public static void parseHeader(
      DsSipHeaderListener headerListener, byte data[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    int type = UNKNOWN_HEADER;

    int end = offset + count;
    int index = lws(data, offset, end);
    int start = index;

    // get first node

    byte ch = data[index++];
    // character known to be ASCII - optimize - jsm
    // optimize further by & with (256-32)
    if (ch <= 'Z' && ch >= 'A') {
      ch += 32; // 'a' - 'A' (97 - 65)
    }

    DsParseTreeNode next = parseTree[ch];

    // try to match header token
    while (next != null) {
      // possible end node
      if (next.match >= 0) {
        index = lws(data, index, end);
        if (data[index] == ':') {
          type = next.match;
          index++;
          break; // found
        }
      } else if (data[index] == ':') {
        break;
      }

      // next = next.next(data[index++]);

      // BEGIN - manual inlining of above line of code
      byte ich = data[index++];

      // int index = next.indexOf(ich);

      // BEGIN - second level inline
      int inlineIndex = -1;

      if (next.nextChars != null) {
        // character known to be ASCII - optimize - jsm
        if (ich <= 'Z' && ich >= 'A') {
          ich += 32; // 'a' - 'A' (97 - 65)
        }

        for (int i = 0; i < next.nextChars.length; i++) {
          if (ich == next.nextChars[i]) {
            inlineIndex = i;
            break;
          }
        }
      }

      // END - second level inline

      if (inlineIndex == -1) {
        next = null;
      } else {
        next = next.next[inlineIndex];
      }

      // END - manual inlining of above line of code
    }

    int unknownStart = start;

    // special case for unknown headers, must find the rest of the name and the ":"
    if (type == UNKNOWN_HEADER) {
      int innerEnd = index;
      boolean foundColon = (data[index] == ':');

      if (foundColon) {
        index++;
      }

      while (!foundColon) {
        switch (data[index]) {
          case ':':
            {
              innerEnd = index;
              foundColon = true;
              index++;
              break;
            }
          case ' ':
          case '\t':
          case '\r':
            {
              innerEnd = index;
              index = lws(data, index, end);
              break;
            }
          case '\n':
            {
              innerEnd = index;
              index = lws(data, index, end);
              if (index == innerEnd) // fix for infinite loop - may need to revisit
              {
                // addException(new DsSipParserException("Malformed SIP message", -1, "", false));

                // exceptions.setSipMessage(currentMessage); // centralize

                // throw exceptions;
                return; // ERROR - no more headers to parse
              }
              break;
            }
          default:
            {
              index++;
              if (index >= end) {
                // make sure we don't go passed the end of the array
                // this only happens with certain malformed messages
                // addException(new DsSipParserException("Malformed SIP message", -1, "", false));

                // exceptions.setSipMessage(currentMessage); // centralize

                // throw exceptions;
                return; // ERROR - no more headers to parse
              }
              break;
            }
        }
      }
    }

    index = lws(data, index, end);
    start = index;

    int headerCount = end - start;

    // lazy pass over the header, find the logical EOL
    ch = data[end - 1];
    if (ch <= ' ') // trailing WS
    {
      int last = end - 2;
      headerCount--;
      ch = data[last];
      while (last > start && ch <= ' ') {
        headerCount--;
        ch = data[--last];
      }
    }

    if (type != UNKNOWN_HEADER) {
      parseHeader(headerListener, type, data, start, headerCount);
    } else {
      int unknownCount = start - unknownStart;
      parseHeader(headerListener, type, data, unknownStart, headerCount + unknownCount);
    }
  }

  /**
   * Parses a SIP header value. Note that the "name:" portion of the header must not exist.
   *
   * @param headerListener where to report the results of parsed header.
   * @param headerType the header ID that the data is from.
   * @param data the byte array that contains the SIP message to parse.
   * @param offset the start of the SIP message in the array.
   * @param count the number of bytes in the SIP message.
   * @throws DsSipParserException if there is an exception while parsing.
   * @throws DsSipParserListenerException if the listener throws this exception.
   */
  public static void parseHeader(
      DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    if (DsPerf.ON) DsPerf.start(DEEP_PARSE_HEADERS);

    // the order of this switch statement must remain in sync with the order of DsSipConstants - jsm
    switch (headerType) {
      case VIA:
        parseVia(headerListener, headerType, data, offset, count);
        break;
      case MAX_FORWARDS:
        parseTokenListWithParams(headerListener, headerType, data, offset, count);
        break;
      case ROUTE:
      case RECORD_ROUTE:
        parseUrlHeader(headerListener, headerType, data, offset, count);
        break;
      case TO:
      case FROM:
        parseUrlHeader(headerListener, headerType, data, offset, count);
        break;
      case CSEQ:
        parseCSeq(headerListener, headerType, data, offset, count);
        break;
      case CALL_ID:
        parseStringHeader(headerListener, headerType, data, offset, count);
        break;
      case CONTENT_LENGTH:
        parseTokenListWithParams(headerListener, headerType, data, offset, count);
        break;
      case CONTACT:
        parseUrlHeader(headerListener, headerType, data, offset, count);
        break;
      case EXPIRES:
        parseDateOrLong(headerListener, headerType, data, offset, count);
        break;
      case PROXY_REQUIRE:
      case REQUIRE:
        parseTokenListWithParams(headerListener, headerType, data, offset, count);
        break;
      case SERVICE_AGENT_PHASE:
      case SERVICE_AGENT_CONTEXT:
      case SERVICE_AGENT_APPLICATION:
        parseTokenListWithParams(headerListener, headerType, data, offset, count);
        break;
      case SERVICE_AGENT_ROUTE:
      case REMOTE_PARTY_ID:
        parseUrlHeader(headerListener, headerType, data, offset, count);
        break;
      case EVENT:
        parseTokenListWithParams(headerListener, headerType, data, offset, count);
        break;
      case DIVERSION:
        // CAFFEINE 2.0 parse the DIVERSION header
        parseUrlHeader(headerListener, headerType, data, offset, count);
        break;
      case P_ASSOCIATED_URI:
      case P_CALLED_PARTY_ID:
      case SERVICE_ROUTE:
        parseUrlHeader(headerListener, headerType, data, offset, count);
        break;
      case P_ACCESS_NETWORK_INFO:
        parseTokenListWithParams(headerListener, headerType, data, offset, count);
        break;
      case PRIVACY:
        parsePrivacy(headerListener, headerType, data, offset, count);
        break;
      case CONTENT_TYPE:
        parseMediaTypeWithParams(headerListener, headerType, data, offset, count);
        break;
      case AE_COOKIE:
      case X_APPLICATION:
      case X_APPLICATION_CONTEXT:
      case X_FROM_OUTSIDE:
      case AS_PATH:
      case AS_RECORD_PATH:
        parseStringHeader(headerListener, headerType, data, offset, count);
        break;
      case SUBSCRIPTION_EXPIRES:
        parseDateOrLong(headerListener, headerType, data, offset, count);
        break;
      case ALLOW_EVENTS:
        parseTokenListWithParams(headerListener, headerType, data, offset, count);
        break;
      case PROXY_AUTHENTICATE:
      case PROXY_AUTHORIZATION:
      case AUTHORIZATION:
        parseAuth(headerListener, headerType, data, offset, count);
        break;
      case AUTHENTICATION_INFO:
        parseAuthParams(headerListener, headerType, data, offset, count);
        break;
      case WWW_AUTHENTICATE:
        parseAuth(headerListener, headerType, data, offset, count);
        break;
      case ACCEPT:
        parseMediaTypeWithParams(headerListener, headerType, data, offset, count);
        break;
      case ACCEPT_ENCODING:
      case ACCEPT_LANGUAGE:
        parseTokenListWithParams(headerListener, headerType, data, offset, count);
        break;
      case ALERT_INFO:
        parseUrlHeader(headerListener, headerType, data, offset, count);
        break;
      case ALLOW:
        parseTokenListWithParams(headerListener, headerType, data, offset, count);
        break;
      case CALL_INFO:
        parseUrlHeader(headerListener, headerType, data, offset, count);
        break;
        // CAFFEINE 2.0 DEVELOPMENT - Additional parsed headers support
      case CONTENT_ID:
        parseEnclosedStringHeader(headerListener, headerType, data, offset, count);
        break;
      case CONTENT_DESCRIPTION:
        parseStringHeader(headerListener, headerType, data, offset, count);
        break;
      case CONTENT_DISPOSITION:
      case CONTENT_ENCODING:
        // CAFFEINE 2.0 DEVELOPMENT - Additional parsed header support
      case CONTENT_TRANSFER_ENCODING:
      case CONTENT_LANGUAGE:
        parseTokenListWithParams(headerListener, headerType, data, offset, count);
        break;
      case DATE:
        parseDate(headerListener, headerType, data, offset, count);
        break;
      case ERROR_INFO:
        parseUrlHeader(headerListener, headerType, data, offset, count);
        break;
      case IN_REPLY_TO:
      case MIME_VERSION:
        parseTokenListWithParams(headerListener, headerType, data, offset, count);
        break;
      case ORGANIZATION:
      case PRIORITY:
        parseStringHeader(headerListener, headerType, data, offset, count);
        break;
      case RACK:
        parseRAck(headerListener, headerType, data, offset, count);
        break;
      case RETRY_AFTER:
        parseDateOrLong(headerListener, headerType, data, offset, count);
        break;
      case RSEQ:
        parseTokenListWithParams(headerListener, headerType, data, offset, count);
        break;
      case SERVER:
        parseServerUserAgent(headerListener, headerType, data, offset, count);
        break;
      case SUBJECT:
        parseStringHeader(headerListener, headerType, data, offset, count);
        break;
      case SUPPORTED:
        parseTokenListWithParams(headerListener, headerType, data, offset, count);
        break;
      case TIMESTAMP:
        parseTimestamp(headerListener, headerType, data, offset, count);
        break;
      case UNSUPPORTED:
        parseTokenListWithParams(headerListener, headerType, data, offset, count);
        break;
      case USER_AGENT:
        parseServerUserAgent(headerListener, headerType, data, offset, count);
        break;
      case WARNING:
        parseWarning(headerListener, headerType, data, offset, count);
        break;
      case SESSION_EXPIRES:
        parseDateOrLong(headerListener, headerType, data, offset, count);
        break;
      case TRANSLATE:
        parseUrlHeader(headerListener, headerType, data, offset, count);
        break;
      case CONTENT_VERSION:
        parseTokenListWithParams(headerListener, headerType, data, offset, count);
        break;
      case REPLY_TO:
        parseUrlHeader(headerListener, headerType, data, offset, count);
        break;
      case SUBSCRIPTION_STATE:
        parseTokenListWithParams(headerListener, headerType, data, offset, count);
        break;
      case REFER_TO:
        parseUrlHeader(headerListener, headerType, data, offset, count);
        break;
      case MIN_EXPIRES:
        parseDateOrLong(headerListener, headerType, data, offset, count);
        break;
      case PATH:
      case P_ASSERTED_IDENTITY:
      case P_PREFERRED_IDENTITY:
        parseUrlHeader(headerListener, headerType, data, offset, count);
        break;
      case REPLACES:
        parseTokenListWithParams(headerListener, headerType, data, offset, count);
        break;
        // per Edgar, connection info header is no longer used by AE
      case X_CONNECTION_INFO:
        parseStringHeader(headerListener, headerType, data, offset, count);
        break;

      case P_DCS_LAES:
        parseTokenListWithParams(headerListener, headerType, data, offset, count);
        break;
      case P_CHARGING_FUNCTION_ADDRESSES:
      case P_CHARGING_VECTOR:
        parseParams(headerListener, headerType, data, offset, count);
        break;
      case P_VISITED_NETWORK_ID:
        parseWordListWithParams(headerListener, headerType, data, offset, count);
        break;
      case REFERRED_BY:
        parseUrlHeader(headerListener, headerType, data, offset, count);
        break;
        // CAFFEINE 2.0 DEVELOPMENT - Additional parsed headers support
      case ACCEPT_CONTACT:
      case REJECT_CONTACT:
        parseTokenListWithParams(headerListener, headerType, data, offset, count);
        break;
      case REQUEST_DISPOSITION:
        parseTokenListWithParams(headerListener, headerType, data, offset, count);
        break;
      case JOIN:
        parseTokenListWithParams(headerListener, headerType, data, offset, count);
        break;
      case APP_INFO:
        parseUrlHeader(headerListener, headerType, data, offset, count);
        break;
      case ETAG:
      case IF_MATCH:
        parseStringHeader(headerListener, headerType, data, offset, count);
        break;
      case CISCO_MAINTENANCE_MODE:
        parseTokenListWithParamsWithOptionalURI(headerListener, headerType, data, offset, count);
        break;
      case CISCO_GUID:
        parseStringHeader(headerListener, headerType, data, offset, count);
        break;
        // MMA - 08.05.2005 - adding support for the Reason header (RFC 3326)
      case REASON_HEADER:
        parseTokenListWithParams(headerListener, headerType, data, offset, count);
        break;
      case SUPPRESS_IF_MATCH:
        parseStringHeader(headerListener, headerType, data, offset, count);
        break;
      case TARGET_DIALOG:
        parseTokenListWithParams(headerListener, headerType, data, offset, count);
        break;
      case HISTORY_INFO:
        parseUrlHeader(headerListener, headerType, data, offset, count);
        break;
      case X_CISCO_RAI:
        parseTokenListWithParams(headerListener, headerType, data, offset, count);
        break;
      case UNKNOWN_HEADER:
        parseUnknownHeader(headerListener, headerType, data, offset, count);
        break;
      default:
        DsSipHeaderParserInterface parser = (DsSipHeaderParserInterface) parserMap.get(headerType);
        if (parser != null) {
          parser.parseHeader(headerListener, headerType, data, offset, count);
        } else {
          // should probably warn here? - jsm
          headerListener.headerBegin(headerType);
          headerListener.headerFound(headerType, data, offset, count, true);
        }
        break;
    }
    if (DsPerf.ON) DsPerf.stop(DEEP_PARSE_HEADERS);
  }

  /**
   * Reports the body of a SIP message to the listener.
   *
   * @param sipMsg where to report the results.
   * @param mb the message bytes that contains the SIP message to parse.
   * @throws DsSipParserListenerException if the listener throws this exception.
   */
  protected static void parseBody(DsMimeMessageListener sipMsg, MsgBytes mb)
      throws DsSipParserListenerException {
    if (DsPerf.ON) DsPerf.start(PARSE_BODY);

    // need more validation with Content-Length, but this will work for sunny day for now - jsm
    boolean isValid = true; // hack - always true for now - jsm

    // CAFFEINE 2.0 DEVELOPMENT - bug in original code: int count = mb.count - mb.i;
    int count = mb.offset + mb.count - mb.i;

    if (count > 0) {
      sipMsg.messageFound(mb.msg, mb.i, count, isValid);
    } else {
      sipMsg.messageFound(DsByteString.BS_EMPTY_STRING.data(), 0, 0, isValid);
    }

    if (DsPerf.ON) DsPerf.stop(PARSE_BODY);
  }

  /**
   * Moves the index to the next non-WS char. If the current char is not WS, the this method just
   * returns. If it is, then it moves to the next non-WS char or to the end if the last char is WS.
   *
   * @param mb the message bytes that contains the SIP message to parse.
   */
  protected static void lws(MsgBytes mb) {
    byte ch = mb.msg[mb.i];
    while (ch <= ' ' && ch != '\n') {
      if (++mb.i < mb.count) {
        ch = mb.msg[mb.i];
      } else {
        return;
      }
    }

    if (ch == '\n') {
      if (mb.i + 1 >= mb.count) {
        return;
      }

      ch = mb.msg[mb.i + 1];
      if (ch == ' ' || ch == '\t') {
        mb.i += 2;
        lws(mb);
      }
    }
  }

  /**
   * Moves the index to the next non-WS char. If the current char is not WS, the this method just
   * returns. If it is, then it moves to the next non-WS char or to the end if the last char is WS.
   *
   * @param data the byte array that contains the SIP message to parse.
   * @param offset the start of the SIP message in the array.
   * @param count the number of bytes in the SIP message.
   * @return the index of the next non-WS char.
   */
  protected static int lws(byte[] data, int index, int end) {
    if (index < end) {
      byte ch = data[index];
      while (ch <= ' ' && ch != '\n') {
        if (++index < end) {
          ch = data[index];
        } else {
          return index; // end
        }
      }

      // check this section - jsm (seems to work so far)
      // looks like we can go out of bounds - check more
      if (ch == '\n') {
        if (index + 1 >= end) {
          return end;
        }

        ch = data[index + 1];
        if (ch == ' ' || ch == '\t') {
          index += 2;
          return lws(data, index, end);
        }
      }
    }

    return index;
  }

  /**
   * Adds a header to the parse tree node data structure, for recognizing headers.
   *
   * @param name the canonical form of the header name.
   * @param compactName the compact form of the header name, use <code>null</code> is none exists.
   * @param code the header ID.
   */
  protected static void addHeaderType(DsByteString name, DsByteString compactName, int code) {
    addBranch(name, code);

    if (compactName != null) {
      addBranch(compactName, code);
    }
  }

  /**
   * Adds a header to the parse tree node data structure, for recognizing headers.
   *
   * @param name the header name.
   * @param code the header ID.
   */
  protected static void addBranch(DsByteString name, int code) {
    byte ch = name.charAt(0);

    if (ch <= 'Z' && ch >= 'A') {
      ch += 32; // 'a' - 'A' (97 - 65)
    }

    DsParseTreeNode node = parseTree[ch];

    if (node == null) {
      // this branch not started yet
      // start new branch
      node = new DsParseTreeNode();

      // add to the tree
      parseTree[ch] = node;
    }

    DsParseTreeNode next = node;

    // walk through the string (start at 1, compact form handled above)
    for (int j = 1; j < name.length(); j++) {
      ch = name.charAt(j);

      next.addChar(ch);

      next = next.next(ch);
    }

    next.match = code;

    return;
  }

  /**
   * Helper method to notify a listener than an element has been found. The listener may be asked
   * for sub-element listeners and then deeper parsing can take place.
   *
   * @param element the element listener to notify
   * @param contextId the context to fire this event in
   * @param buffer the buffer where the element can be found
   * @param offset the index that the element starts at
   * @param count the number of bytes in the found element
   * @throws DsSipParserListenerException if it is thrown by one of the listeners
   */
  protected static void fireElement(
      DsSipElementListener element,
      int contextId,
      int elementId,
      byte[] buffer,
      int offset,
      int count)
      throws DsSipParserListenerException {
    if (DsPerf.ON) DsPerf.start(FIRE_ELEMENT);

    if (element != null) {
      DsSipElementListener subElement = null;

      // first check and see if this may need to be parsed deeper
      switch (elementId) {
        case SIP_URL:
        case SIPS_URL:
        case TEL_URL:
        case HTTP_URL:
          // CAFFEINE 2.0 DEVELOPMENT - Additional header support
        case CID_URL:
        case UNKNOWN_URL:
        case SIP_DATE:
          // we do not parse Expires SIP_DATEs, since they are just 3600 anyway. - jsm
          if (contextId != EXPIRES) {
            subElement = element.elementBegin(contextId, elementId);
          }
          break;
        case SINGLE_VALUE:
          if (contextId == CONTENT_LANGUAGE
              || contextId == ACCEPT_LANGUAGE
              || contextId == SIP_DATE
              || contextId == EVENT
              || contextId == ALLOW_EVENTS) {
            subElement = element.elementBegin(contextId, elementId);
          }
        default:
          break;
      }

      boolean isElementValid = true;

      try {
        // maybe move this below the call to elementFound? - jsm
        if (subElement != null) // needs to be parsed as well
        {
          // find the right parser for this type and parse it
          switch (elementId) {
            case SIP_URL:
            case SIPS_URL:
              parseSipUrl(subElement, buffer, offset, count);
              break;
            case TEL_URL:
              parseTelUrl(subElement, buffer, offset, count);
              break;
              // CAFFEINE 2.0 DEVELOPMENT - Additional parsed header support
            case CID_URL:
              parseCidUrl(subElement, buffer, offset, count);
              break;
            case HTTP_URL:
            case UNKNOWN_URL:
              parseUnknownUrl(subElement, buffer, offset, count);
              break;
            default:
              break;
          }

          // make this a switch statement
          if ((contextId == CONTENT_LANGUAGE || contextId == ACCEPT_LANGUAGE)
              && elementId == SINGLE_VALUE) {
            // need to parse the language
            parseLanguage(subElement, contextId, buffer, offset, count);
          }

          if (((contextId == DATE) && elementId == SINGLE_VALUE) || (elementId == SIP_DATE)) {
            // need to parse the SIP-Date
            parseSipDate(subElement, contextId, buffer, offset, count);
          }

          if ((contextId == EVENT || contextId == ALLOW_EVENTS) && elementId == SINGLE_VALUE) {
            // need to parse the event package and sub package
            parseEventPackage(subElement, contextId, buffer, offset, count);
          }
        }
      } catch (DsSipParserListenerException e) {
        throw e;
      } catch (Exception e) {
        isElementValid = false;
      }

      element.elementFound(contextId, elementId, buffer, offset, count, isElementValid);
    }

    if (DsPerf.ON) DsPerf.stop(FIRE_ELEMENT);
  }

  protected static void fireParameter(
      DsSipElementListener element,
      int contextId,
      byte[] buffer,
      int nameOffset,
      int nameCount,
      int valueOffset,
      int valueCount)
      throws DsSipParserListenerException {
    if (DsPerf.ON) DsPerf.start(FIRE_PARAMETER);

    // fix any missed flag parameter value
    if (valueOffset < 0 || valueCount < 0) {
      valueOffset = 0;
      valueCount = 0;
    }

    element.parameterFound(contextId, buffer, nameOffset, nameCount, valueOffset, valueCount);

    if (DsPerf.ON) DsPerf.stop(FIRE_PARAMETER);
  }

  // To
  // From
  // Contact
  // Route
  // Record-Route
  // Alert-Info (allow 0 or more)
  // Call-Info (allow 0 or more)
  // Error-Info (allow 0 or more)
  // more?
  protected static void parseUrlHeader(
      DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    if (DsPerf.ON) DsPerf.start(PARSE_URL_HEADER);

    try {
      int index = offset;
      int end = offset + count;

      while (true) {
        int startHeader = index;
        DsSipElementListener header = headerListener.headerBegin(headerType);
        if (header == null) {
          headerListener.headerFound(headerType, data, startHeader, end - startHeader, true);
          if (DsPerf.ON) DsPerf.stop(PARSE_URL_HEADER);
          return; // done - lazy parse only
        }

        index = lws(data, index, end);

        // handle empty case
        if (index == end) {
          headerListener.headerFound(headerType, data, startHeader, end - startHeader, true);
          if (DsPerf.ON) DsPerf.stop(PARSE_URL_HEADER);
          return;
        }

        // handle empty element - ",,"
        if (data[index] == ',') {
          // ignore this empty element
          index++;
          continue;
        }

        int start = index;
        int endDispName = index;
        int startUrl = index;

        int urlStart = 0;
        int urlCount = 0;
        boolean foundDispName = false;

        boolean foundComma = false;
        int commaIndex = -1;

        byte ch = data[index];

        // short cut for quoted string -> NameAddr
        if (ch == '*') // star for Contact only
        {
          // check for contact header type? - jsm
          fireElement(header, headerType, WILDCARD, data, index, 1);
        } else if (ch == '"') {
          index++; // move past first '"'
          index = readToCloseQuote(data, index, end);

          fireElement(header, headerType, DISPLAY_NAME, data, start, index - start);

          while (data[index++] != '<') // find start of NameAddr
          {
            // just find the <
          }
          startUrl = index; // points to first char in url

          while (data[index++] != '>') // find end of NameAddr
          {
            // just find the >
          }
          int endNameAddr = index - 1; // index points one char past >

          urlStart = startUrl;
          urlCount = endNameAddr - startUrl;
        } else {
          boolean done = false;
          while (!done && index < end) {
            // The very first char is not LWS - handled above
            // So, finding LWS before finding a ':' indicates the end of a name addr token
            // ':' found is handled below, that is the trivial case (only a URL, not a name addr)
            switch (data[index++]) {
              case ' ':
              case '\t':
              case '\r':
              case '\n':
                endDispName = index - 1;
                // any leading LWS is skipped - so this must be a name addr
                while (index < end && (ch = data[index++]) != '<') {
                  // multiple tokens allowed as DispName
                  if (ch > ' ') {
                    // This will be the last non <, non LWS char
                    endDispName = index;
                  }
                }

                if (ch != '<') {
                  throw generateDsSipParserException(
                      headerListener,
                      "Illegal: No '<' found in name-addr",
                      headerType,
                      data,
                      offset,
                      count);
                }

                // already trimmed LWS - except between tokens

                foundDispName = true;
                fireElement(header, headerType, DISPLAY_NAME, data, start, endDispName - start);

                // fall through to '<' since this is now the ch
              case '<':
                // If this is true, then we did not fall through from above and
                // need to set the display name, since there was no WS
                if (!foundDispName) {
                  foundDispName = true;
                  fireElement(header, headerType, DISPLAY_NAME, data, start, index - start - 1);
                }
                /*
                                                else
                                                {
                                                    // no display name but there are <>, so send and empty DISPLAY_NAME event
                                                    // so that the user code knows to set <> true
                                                    fireElement(header, headerType, DISPLAY_NAME, data, 0, 0);
                                                }
                */

                startUrl = index; // points to first char in url

                while (index < end && data[index++] != '>') // find end of NameAddr
                {
                  // just find the >
                }

                if (data[index - 1] != '>') {
                  throw generateDsSipParserException(
                      headerListener,
                      "Illegal: No '>' found in name-addr",
                      headerType,
                      data,
                      offset,
                      count);
                }

                int endNameAddr = index - 1; // index points one char past >, so go back 1

                urlStart = startUrl;
                urlCount = endNameAddr - startUrl;

                done = true; // move on to parameters
                break;
              case ':':
                // Name addr does not handle null display names
                // dispNameStr = "";

                // if we see the ':' here - it is just a url, not a name addr
                ch = data[index++];
                while (index < end && ch > ' ' && ch != ';' && ch != ',') {
                  ch = data[index++];
                }

                if (ch == ',') {
                  commaIndex = index - 1;
                  foundComma = true;
                }

                urlStart = start;

                if (index == end && ch != '\n') {
                  urlCount = index - start;
                } else {
                  urlCount = index - start - 1;
                }

                done = true; // move on to parameters
            }
          }

          if (!done) {
            throw generateDsSipParserException(
                headerListener,
                "Maiformed Url Header: Display Name with no URL",
                headerType,
                data,
                offset,
                count);
          }
        }

        if (ch != '*') {
          fireElement(
              header, headerType, getUrlType(data, urlStart, urlCount), data, urlStart, urlCount);
        }

        index = lws(data, index, end);

        if (index < end && data[index] == ',') {
          headerListener.headerFound(headerType, data, startHeader, index - startHeader, true);

          // move past the comma
          index++;

          continue;
        } else if (foundComma) // we must have moved passed the comma at this point
        {
          headerListener.headerFound(headerType, data, startHeader, commaIndex - startHeader, true);

          continue;
        }

        // ready to look for params
        boolean inName = true;

        boolean foundName = false;
        boolean foundValue = false;
        int nameCount = -1;
        int valueCount = -1;

        if (ch != ';') // already found ';' above
        {
          while (index < end && data[index++] != ';') // find the first ';' if it exists
          {}
        }

        // trim any LWS that might exists after the ';'
        index = lws(data, index, end);

        int startName = index;
        int startValue = -1;

        while (index < end && !foundComma) // still stuff to check for?
        {
          switch (data[index++]) {
            case ';':
              // begin new param
              // end name or value
              if (inName) {
                if (!foundName) // name not set yet
                {
                  foundName = true;
                  nameCount = index - startName - 1;
                }
              } else {
                inName = true;
                if (!foundValue) // value not set yet
                {
                  foundValue = true;
                  valueCount = index - startValue - 1;
                }
              }
              // inName is always true at this point

              fireParameter(header, headerType, data, startName, nameCount, startValue, valueCount);

              index = lws(data, index, end);

              startName = index;
              nameCount = -1;
              valueCount = -1;
              foundName = false;
              foundValue = false;

              break;
            case '=':
              // end name
              // begin value
              if (!foundName) // name not set yet
              {
                foundName = true;

                nameCount = index - startName - 1;
              }
              inName = false; // switch to value

              index = lws(data, index, end);

              startValue = index;
              break;
            case '"':
              // quotes allowed in values only
              if (!inName) {
                index = readToCloseQuote(data, index, end);

                // if we are at the end, the value will get created below - don't create it twice
                if (index != end) {
                  foundValue = true;
                  valueCount = index - startValue;
                }
              } else {
                throw generateDsSipParserException(
                    headerListener,
                    "Illegal: '\"' found in parameter name",
                    headerType,
                    data,
                    offset,
                    count);
              }
              break;
            case ',':
              foundComma = true;
              // end name or value
              if (inName && !foundName) {
                foundName = true;
                nameCount = index - startName - 1;
              } else if (!inName && !foundValue) {
                foundValue = true;
                valueCount = index - startValue - 1;
              }

              if (!foundValue) {
                fireParameter(header, headerType, data, startName, nameCount, 0, 0);
              } else {
                fireParameter(
                    header, headerType, data, startName, nameCount, startValue, valueCount);
              }
              break;
            case ' ':
            case '\t':
            case '\r':
            case '\n':
              // end name or value
              if (inName && !foundName) {
                foundName = true;

                nameCount = index - startName - 1;
              } else if (!inName && !foundValue) {
                foundValue = true;

                valueCount = index - startValue - 1;
              }

              // or just skip if already ended

              break;
          }

          // The last case that the switch statement cannot handle
          // We must get the last string (name or value here)
          // Too hard to do outside of loop, since we may or maynot get into the loop
          if (index == end) {
            if (inName) {
              if (!foundName) {
                foundName = true;

                nameCount = index - startName;
              }

              fireParameter(header, headerType, data, startName, nameCount, 0, 0);
            } else {
              if (!foundValue) {
                foundValue = true;

                valueCount = index - startValue;
              }

              fireParameter(header, headerType, data, startName, nameCount, startValue, valueCount);
            }
          }
        }

        headerListener.headerFound(headerType, data, startHeader, index - startHeader, true);
        // headerListener.headerFound(headerType, data, offset, count, true);

        if (foundComma == false) // no more headers to parse
        {
          if (DsPerf.ON) DsPerf.stop(PARSE_URL_HEADER);
          return;
        }
      }
    } catch (DsSipParserException e) {
      throw e;
    } catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(headerListener, e, headerType, data, offset, count);
    }
  }

  /**
   * Parses a set of parameters, separated by commas or semicolons. Reports the results to the
   * listener.
   *
   * @param element where to report the results.
   * @param headerType the type of header that these parameters came from.
   * @param data the parameters.
   * @param offset the index that the parameters start at.
   * @param count the number of bytes in the parameters.
   * @throws DsSipParserException if there is an exception while parsing.
   * @throws DsSipParserListenerException if the listener throws this exception.
   */
  public static void parseParameters(
      DsSipElementListener element, int headerType, byte data[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      if (element == null) {
        return; // done - lazy parse only
      }

      int end = offset + count;
      int index = lws(data, offset, end);

      // ready to look for params
      boolean inName = true;

      boolean foundName = false;
      boolean foundValue = false;
      int nameCount = -1;
      int valueCount = -1;

      int startName = index;
      int startValue = -1;

      while (index < end) // still stuff to check for?
      {
        switch (data[index++]) {
          case ';':
          case ',': // allow for ',' as well for the Challenge Info and Credentials Info
            // begin new param
            // end name or value
            if (inName) {
              if (!foundName) // name not set yet
              {
                foundName = true;
                nameCount = index - startName - 1;
              }
            } else {
              inName = true;
              if (!foundValue) // value not set yet
              {
                foundValue = true;
                valueCount = index - startValue - 1;
              }
            }
            // inName is always true at this point

            fireParameter(element, headerType, data, startName, nameCount, startValue, valueCount);

            index = lws(data, index, end);

            startName = index;
            nameCount = -1;
            valueCount = -1;
            foundName = false;
            foundValue = false;

            break;
          case '=':
            // end name
            // begin value
            if (!foundName) // name not set yet
            {
              foundName = true;

              nameCount = index - startName - 1;
            }
            inName = false; // switch to value

            index = lws(data, index, end);

            startValue = index;
            break;
          case '"':
            // quotes allowed in values only
            if (!inName) {
              index = readToCloseQuote(data, index, end);

              // if we are at the end, the value will get created below - don't create it twice
              if (index != end) {
                foundValue = true;
                valueCount = index - startValue;
              }
            } else {
              throw generateDsSipParserException(
                  element,
                  "Illegal: '\"' found in parameter name",
                  headerType,
                  PARAMETERS,
                  data,
                  offset,
                  count);
            }
            break;
          case ' ':
          case '\t':
          case '\r':
          case '\n':
            // end name or value
            if (inName && !foundName) {
              foundName = true;

              nameCount = index - startName - 1;
            } else if (!inName && !foundValue) {
              foundValue = true;

              valueCount = index - startValue - 1;
            }

            // or just skip if already ended

            break;
        }

        // The last case that the switch statement cannot handle
        // We must get the last string (name or value here)
        // Too hard to do outside of loop, since we may or maynot get into the loop
        if (index == end) {
          if (inName) {
            if (!foundName) {
              foundName = true;

              nameCount = index - startName;
            }

            fireParameter(element, headerType, data, startName, nameCount, 0, 0);
          } else {
            if (!foundValue) {
              foundValue = true;

              valueCount = index - startValue;
            }

            fireParameter(element, headerType, data, startName, nameCount, startValue, valueCount);
          }
        }
      }
    } catch (DsSipParserException e) {
      throw e;
    } catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(element, e, headerType, PARAMETERS, data, offset, count);
    }
  }

  // combine with TokenList?
  // Call-ID
  // Organization
  // Priority
  // Subject
  // more?
  protected static void parseStringHeader(
      DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      DsSipElementListener header = headerListener.headerBegin(headerType);
      if (header == null) {
        headerListener.headerFound(headerType, data, offset, count, true);
        return; // done - lazy parse only
      }

      if (count == 0) {
        fireElement(header, headerType, SINGLE_VALUE, data, offset, 0);

        headerListener.headerFound(headerType, data, offset, count, true);
        return;
      }

      int end = count + offset;
      int start = offset;

      // skip any leading white space
      while (start < end && data[start++] <= ' ') {}

      if (start == end) {
        if (data[start - 1] <= ' ') {
          // empty header
          fireElement(header, headerType, SINGLE_VALUE, data, offset, 0);

          headerListener.headerFound(headerType, data, offset, count, true);
          return;
        }
      }

      start--;
      // start now points to the first char in the actual value

      end--;
      while (start < end && data[end] <= ' ') {
        end--;
      }
      // end now points to the last char in the actual value

      if (start == end) {
        fireElement(header, headerType, SINGLE_VALUE, data, start, 1);
      } else {
        fireElement(header, headerType, SINGLE_VALUE, data, start, end - start + 1);
      }

      headerListener.headerFound(headerType, data, offset, count, true);
    }
    // catch (DsSipParserException e)
    // {
    // throw e;
    // }
    catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(headerListener, e, headerType, data, offset, count);
    }
  }

  // CAFFEINE 2.0 DEVELOPMENT - parse the Content-ID header value
  protected static void parseEnclosedStringHeader(
      DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      DsSipElementListener header = headerListener.headerBegin(headerType);
      if (header == null) {
        headerListener.headerFound(headerType, data, offset, count, true);
        return; // done - lazy parse only
      }

      if (count == 0) {
        fireElement(header, headerType, SINGLE_VALUE, data, offset, 0);

        headerListener.headerFound(headerType, data, offset, count, true);
        return;
      }

      int end = count + offset;
      int start = offset;

      // skip any leading white space
      while (start < end && data[start++] <= ' ') {}

      if (start == end) {
        if (data[start - 1] <= ' ') {
          // empty header
          fireElement(header, headerType, SINGLE_VALUE, data, offset, 0);

          headerListener.headerFound(headerType, data, offset, count, true);
          return;
        }
      }

      if (data[start - 1] != '<') {
        throw generateDsSipParserException(
            headerListener, "No '<' found in the header", headerType, data, offset, count);
      }

      // skip any leading white space after the '<'
      while (start < end && data[start++] <= ' ') {}

      if (start == end) {
        if (data[start - 1] <= ' ') {
          throw generateDsSipParserException(
              headerListener,
              "No char found after '<' in the header",
              headerType,
              data,
              offset,
              count);
        }
      }

      start--;
      // start now points to the first char (after '<') in the actual value

      end--;
      while (start < end && data[end] <= ' ') {
        end--;
      }
      // end now points to the last char in the actual value, which should be '>'

      if (data[end] != '>') {
        throw generateDsSipParserException(
            headerListener, "No '>' found in the header", headerType, data, offset, count);
      }

      end--; // Skip '>'
      while (start < end && data[end] <= ' ') {
        end--;
      }
      // end now points to the last char in the actual value

      // Store the element (setValue()), which has the header (such as, Content-ID) value,
      // minue '<', '>', and unnecessary white spaces
      fireElement(header, headerType, URI_DATA, data, start, end - start + 1);

      headerListener.headerFound(headerType, data, offset, count, true);
    } catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(headerListener, e, headerType, data, offset, count);
    }
  }

  protected static void parseRAck(
      DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      DsSipElementListener header = headerListener.headerBegin(headerType);
      if (header == null) {
        headerListener.headerFound(headerType, data, offset, count, true);
        return; // done - lazy parse only
      }

      // end points 1 past the last slot, treat it like .length
      int end = count + offset;
      int index = lws(data, offset, end);
      int start = index;

      // find the first token, this is the response number
      while (index < end && data[index++] > ' ') {}

      if (index == end && data[index - 1] > ' ') {
        fireElement(header, headerType, RESPONSE_NUMBER, data, start, index - start);
      } else {
        fireElement(header, headerType, RESPONSE_NUMBER, data, start, index - start - 1);
      }

      // start and index now point to the first char of the second token, the CSeq number
      start = index = lws(data, index, end);

      // find the CSeq number
      while (index < end && data[index++] > ' ') {}

      if (index == end && data[index - 1] > ' ') {
        fireElement(header, headerType, CSEQ_NUMBER, data, start, index - start);
      } else {
        fireElement(header, headerType, CSEQ_NUMBER, data, start, index - start - 1);
      }

      // start and index now point to the first char of the third token, the Method name
      start = index = lws(data, index, end);

      // find the Method name
      while (index < end && data[index++] > ' ') {}

      if (index == end && data[index - 1] > ' ') {
        fireElement(header, headerType, CSEQ_METHOD, data, start, index - start);
      } else {
        fireElement(header, headerType, CSEQ_METHOD, data, start, index - start - 1);
      }

      headerListener.headerFound(headerType, data, offset, count, true);
    }
    // catch (DsSipParserException e)
    // {
    // throw e;
    // }
    catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(headerListener, e, headerType, data, offset, count);
    }
  }

  protected static void parseCSeq(
      DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      DsSipElementListener header = headerListener.headerBegin(headerType);
      if (header == null) {
        headerListener.headerFound(headerType, data, offset, count, true);
        return; // done - lazy parse only
      }

      int end = count + offset;
      int index = lws(data, offset, end);

      // start now points to the first char in the number
      int start = index;

      while (index < end && data[index++] > ' ') {}

      fireElement(header, headerType, CSEQ_NUMBER, data, start, index - start - 1);

      // start now points to the first char in the method
      start = index = lws(data, index, end);

      while (index < end && data[index++] > ' ') {}

      if (index == end && data[index - 1] > ' ') {
        fireElement(header, headerType, CSEQ_METHOD, data, start, index - start);
      } else {
        fireElement(header, headerType, CSEQ_METHOD, data, start, index - start - 1);
      }

      headerListener.headerFound(headerType, data, offset, count, true);
    }
    // catch (DsSipParserException e)
    // {
    // throw e;
    // }
    catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(headerListener, e, headerType, data, offset, count);
    }
  }

  protected static void parseWarning(
      DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      int index = offset;
      int end = offset + count;

      while (true) {
        int startHeader = index;
        DsSipElementListener header = headerListener.headerBegin(headerType);
        if (header == null) {
          headerListener.headerFound(headerType, data, startHeader, end - startHeader, true);
          return; // done - lazy parse only
        }

        // move to first non-WS char
        index = lws(data, index, end);

        // handle empty case
        if (index == end) {
          headerListener.headerFound(headerType, data, startHeader, end - startHeader, true);
          return;
        }

        // handle empty element - ",,"
        if (data[index] == ',') {
          // ignore this empty element
          index++;
          continue;
        }

        // mark the start of the data
        int start = index;

        byte ch = data[index++];
        while (index < end && ch != ' ') {
          // just find the SP

          ch = data[index++];
        }

        fireElement(header, headerType, WARN_CODE, data, start, index - start - 1);

        start = index;
        ch = data[index];
        boolean foundHost = false;

        // Add support for IPv6 - addr enclosed in [ ].
        boolean isIPv6 = false;
        if (ch == '[') {
          isIPv6 = true;
          index = readToCloseBracket(data, index, end);

          // skip the [
          // will trim the ] below
          start++;
        }

        while (index < end && ch != ' ') {
          if (ch == ':') // host with a port
          {
            foundHost = true;
            if (isIPv6) {
              // extra -1 for ]
              fireElement(header, headerType, HOST, data, start, index - start - 2);
            } else {
              fireElement(header, headerType, HOST, data, start, index - start - 1);
            }
            start = index;
            while (index < end && data[index++] != ' ') {}

            fireElement(header, headerType, PORT, data, start, index - start - 1);
            break;
          }

          ch = data[index++];
        }

        if (!foundHost) {
          if (isIPv6) {
            // extra -1 for ]
            fireElement(header, headerType, HOST, data, start, index - start - 2);
          } else {
            fireElement(header, headerType, HOST, data, start, index - start - 1);
          }
        }

        start = index;
        ch = data[index++];

        if (ch != '"') // exception
        {
          // throw exception
        }

        index = readToCloseQuote(data, index, end);

        fireElement(header, headerType, WARN_TEXT, data, start, index - start);

        index = lws(data, index, end);

        if (index == end) {
          headerListener.headerFound(headerType, data, startHeader, end - startHeader, true);
          return; // done parsing
        }

        headerListener.headerFound(headerType, data, startHeader, index - startHeader, true);

        if (data[index] == ',') {
          index++;
        } else {
          return;
        }
      }
    }
    // catch (DsSipParserException e)
    // {
    // throw e;
    // }
    catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(headerListener, e, headerType, data, offset, count);
    }
  }

  protected static void parseVia(
      DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    if (DsPerf.ON) DsPerf.start(PARSE_VIA);

    try {
      int index = offset;
      int end = offset + count;
      // CAFFEINE 2.0 DEVELOPMENT
      // Bugid: CSCef82808 Via header without host won't be detected when doing deep parsing
      int elementCount = 0;

      int start;
      byte ch;

      // loop through all of the Via headers
      // we will return from this method when all parsing is complete
      while (true) {
        start = index = lws(data, index, end);

        int startHeader = index;
        DsSipElementListener header = headerListener.headerBegin(headerType);
        if (header == null) {
          headerListener.headerFound(headerType, data, startHeader, end - startHeader, true);
          if (DsPerf.ON) DsPerf.stop(PARSE_VIA);
          return; // done - lazy parse only
        }

        // The first thing is the sent-protocol
        // Optimized - check for the most common case first
        if (data[index] == 'S'
            && data[index + 1] == 'I'
            && data[index + 2] == 'P'
            && data[index + 3] == '/'
            && data[index + 4] == '2'
            && data[index + 5] == '.'
            && data[index + 6] == '0'
            && data[index + 7] == '/') {
          // should these just be defaulted? - jsm
          // if not, then we should just find all of these generically - jsm
          fireElement(header, headerType, PROTOCOL_NAME, data, start, 3);
          fireElement(header, headerType, PROTOCOL_VERSION, data, start + 4, 3);

          ch = data[index + 11];

          if (data[index + 8] == 'U'
              && data[index + 9] == 'D'
              && data[index + 10] == 'P'
              && ch <= ' ') {
            index = lws(data, index + 12, end);

            fireElement(header, headerType, TRANSPORT, data, start + 8, 3);
          } else if (data[index + 8] == 'T'
              && data[index + 9] == 'C'
              && data[index + 10] == 'P'
              && ch <= ' ') {
            index = lws(data, index + 12, end);

            fireElement(header, headerType, TRANSPORT, data, start + 8, 3);
          } else if (data[index + 8] == 'T'
              && data[index + 9] == 'L'
              && data[index + 10] == 'S'
              && ch <= ' ') {
            index = lws(data, index + 12, end);

            fireElement(header, headerType, TRANSPORT, data, start + 8, 3);
          } else {
            // well, the protocol name is not one of the big three
            start = index = lws(data, index + 8, end);

            ch = data[index];
            while (ch > ' ') {
              ch = data[++index];
            }

            fireElement(header, headerType, TRANSPORT, data, start, index - start);

            index = lws(data, index, end);
          }
        } else {
          // This is probably very infrequent.  No short cuts here, just parse token/token/token

          // leave index at the first non-WS char after sent-protocol

          start = index;

          boolean foundName = false;
          while ((ch = data[index++]) != '/') {
            if (ch <= ' ') {
              foundName = true;

              fireElement(header, headerType, PROTOCOL_NAME, data, start, index - start - 1);

              while (data[index++] != '/') {
                // just find the '/' - w/o looking for WS
              }
              break;
            }
          }

          if (!foundName) {
            fireElement(header, headerType, PROTOCOL_NAME, data, start, index - start - 1);
          }

          start = index = lws(data, index, end);

          boolean foundVersion = false;
          while ((ch = data[index++]) != '/') {
            if (ch <= ' ') {
              foundVersion = true;

              fireElement(header, headerType, PROTOCOL_VERSION, data, start, index - start - 1);

              while (data[index++] != '/') {
                // just find the '/' - w/o looking for WS
              }
              break;
            }
          }

          if (!foundVersion) {
            fireElement(header, headerType, PROTOCOL_VERSION, data, start, index - start - 1);
          }

          start = index = lws(data, index, end);

          while (true) {
            ch = data[index++];

            if (ch <= ' ') {
              fireElement(header, headerType, TRANSPORT, data, start, index - start - 1);

              break;
            }
          }

          // move to first non-WS char
          index = lws(data, index, end);
        }

        // Now comes the host and port
        start = index;
        int colonIndex = -1;

        // keep in mind that "host LWS : LWS port" is legal
        // not like urls that can't have spaces

        // find the first separator after the host/[port]
        ch = data[index++];

        // Add support for IPv6 - addr enclosed in [ ].
        boolean isIPv6 = false;
        if (ch == '[') {
          isIPv6 = true;
          index = readToCloseBracket(data, index, end);

          // skip the [
          // will trim the ] below
          start++;
        }

        while (index < end
            && // end of data
            ch > ' '
            && ch != ','
            && // start of new header
            ch != ';'
            && // start of parameters
            ch != '(') // start of comment
        {
          if (ch == ':') {
            colonIndex = index - 1;
          }
          ch = data[index++];
        }

        int lastIndex = index - 1;
        byte lastCh = data[lastIndex];
        boolean foundHostPort = false; // set if they are found in first if

        // this could be LWS before or after the : - let's check
        if (lastCh <= ' ') // LWS
        {
          if (colonIndex == -1) // no colon found yet
          {
            foundHostPort = true;

            // CAFFEINE 2.0 DEVELOPMENT
            // Bugid: CSCef82808 Via header without host won't be detected when doing deep parsing
            elementCount = index - start - 1;
            if (isIPv6) {
              --elementCount;
            }
            if (elementCount > 0) // make sure that we do have something for Host
            {
              fireElement(header, headerType, HOST, data, start, elementCount);
            } else {
              throw generateDsSipParserException(
                  headerListener, "No Host found in Via header", headerType, data, offset, count);
            }

            index = lws(data, index, end);

            if (index < end && data[index] == ':') // there is a port
            {
              index++;

              // move to the start of the port
              index = lws(data, index, end);
              start = index;

              ch = data[index++];
              while (index < end
                  && // end of data
                  ch > ' '
                  && ch != ','
                  && // start of new header
                  ch != ';'
                  && // start of parameters
                  ch != '(') // start of comment
              {
                ch = data[index++];
              }

              lastIndex = index - 1;
              lastCh = data[lastIndex];

              if (index == end && lastCh >= '0' && lastCh <= '9') {
                fireElement(header, headerType, PORT, data, start, index - start);
              } else {
                // CAFFEINE 2.0 DEVELOPMENT
                // Bugid: CSCef82808 Via header without host won't be detected when doing deep
                // parsing
                elementCount = index - start - 1;
                if (elementCount > 0) // make sure that we do have something for Port
                {
                  fireElement(header, headerType, PORT, data, start, elementCount);
                } else {
                  throw generateDsSipParserException(
                      headerListener,
                      "No Port found in Via header after ':'",
                      headerType,
                      data,
                      offset,
                      count);
                }
              }
            }
            // else // just extra WS at the end
            // {
            // }
          } else if (colonIndex == index - 2) // LWS after : but none before it
          {
            foundHostPort = true;

            // CAFFEINE 2.0 DEVELOPMENT
            // Bugid: CSCef82808 Via header without host won't be detected when doing deep parsing
            elementCount = colonIndex - start;
            if (isIPv6) {
              --elementCount;
            }
            if (elementCount > 0) // make sure that we do have something for Host
            {
              fireElement(header, headerType, HOST, data, start, elementCount);
            } else {
              throw generateDsSipParserException(
                  headerListener, "No Host found in Via header", headerType, data, offset, count);
            }

            // move to the start of the port
            index = lws(data, index, end);
            start = index;

            ch = data[index++];
            while (index < end
                && // end of data
                ch > ' '
                && ch != ','
                && // start of new header
                ch != ';'
                && // start of parameters
                ch != '(') // start of comment
            {
              ch = data[index++];
            }

            lastIndex = index - 1;
            lastCh = data[lastIndex];

            if (index == end && lastCh >= '0' && lastCh <= '9') {
              fireElement(header, headerType, PORT, data, start, index - start);
            } else {
              // CAFFEINE 2.0 DEVELOPMENT
              // Bugid: CSCef82808 Via header without host won't be detected when doing deep parsing
              elementCount = index - start - 1;
              if (elementCount > 0) // make sure that we do have something for Port
              {
                fireElement(header, headerType, PORT, data, start, elementCount);
              } else {
                throw generateDsSipParserException(
                    headerListener,
                    "No Port found in Via header after ':'",
                    headerType,
                    data,
                    offset,
                    count);
              }
            }
          }
        }

        if (!foundHostPort) {
          // no LWS around : if we get here (if it even exists)

          // we now either have just a host or a host and a port
          // index points to the first WS char or separator after this (possible combined) token
          if (colonIndex == -1) {
            // this is the easy case, no port found, the entire string is the host token
            if (index == end && lastCh != '\n') {
              if (isIPv6) {
                fireElement(header, headerType, HOST, data, start, index - start - 1);
              } else {
                fireElement(header, headerType, HOST, data, start, index - start);
              }
            } else {
              // CAFFEINE 2.0 DEVELOPMENT
              // Bugid: CSCef82808 Via header without host won't be detected when doing deep parsing
              elementCount = index - start - 1;
              if (isIPv6) {
                --elementCount;
              }
              if (elementCount > 0) // make sure that we do have something for Host
              {
                fireElement(header, headerType, HOST, data, start, elementCount);
              } else {
                throw generateDsSipParserException(
                    headerListener, "No Host found in Via header", headerType, data, offset, count);
              }
            }
          } else {
            // we found a host and a port
            // CAFFEINE 2.0 DEVELOPMENT
            // Bugid: CSCef82808 Via header without host won't be detected when doing deep parsing
            elementCount = colonIndex - start;
            if (isIPv6) {
              --elementCount;
            }
            if (elementCount > 0) // make sure that we do have something for Host
            {
              fireElement(header, headerType, HOST, data, start, elementCount);
            } else {
              throw generateDsSipParserException(
                  headerListener, "No Host found in Via header", headerType, data, offset, count);
            }

            if (index == end && lastCh >= '0' && lastCh <= '9') {
              fireElement(header, headerType, PORT, data, colonIndex + 1, index - colonIndex - 1);
            } else {
              // CAFFEINE 2.0 DEVELOPMENT
              // Bugid: CSCef82808 Via header without host won't be detected when doing deep parsing
              elementCount = index - colonIndex - 2;
              if (elementCount > 0) // make sure that we do have something for Port
              {
                fireElement(header, headerType, PORT, data, colonIndex + 1, elementCount);
              } else {
                throw generateDsSipParserException(
                    headerListener,
                    "No Port found in Via header after ':'",
                    headerType,
                    data,
                    offset,
                    count);
              }
            }
          }
        }

        if (lastCh <= ' ') {
          // if we ended host port search with WS, then move to the next non-WS char
          index = lws(data, index, end);

          // save the lastCh again - since lws() might have moved to another char
          // check the increment here
          if (index < end) {
            lastCh = data[index++];
          } else {
            lastCh = (byte) '\n';
          }
        }

        // now we either have a
        //     ',' - end of header, start new one
        //     ';' - start of parameters
        //     '(' - comment

        // if we found a ',' - that is the end of this header
        // add it to the list and go back to the top of the loop and start parsing the next header
        if (lastCh == (byte) ',') {
          headerListener.headerFound(headerType, data, startHeader, lastIndex - startHeader, true);

          continue; // start parsing the next header
        }

        int commaIndex = -1;
        if (lastCh == (byte) ';') // found a ';' when looking for host port
        {
          // ready to look for params
          boolean inName = true;

          boolean foundName = false;
          boolean foundValue = false;
          int nameCount = -1;
          int valueCount = -1;

          // trim any LWS that might exists after the ';'
          index = lws(data, index, end);

          int startName = index;
          int startValue = -1;

          boolean done = false;
          while (index < end && !done) // still stuff to check for?
          {
            switch (data[index++]) {
              case ',': // new header
                commaIndex = index - 1;
              case '(': // found comment
                lastCh = data[index - 1];
                done = true;
                // fall through and do the same thing as you do for a ';'
              case ';':
                // begin new param
                // end name or value
                if (inName) {
                  if (!foundName) // name not set yet
                  {
                    foundName = true;
                    nameCount = index - startName - 1;
                  }
                } else {
                  inName = true;
                  if (!foundValue) // value not set yet
                  {
                    foundValue = true;
                    valueCount = index - startValue - 1;
                  }
                }
                // inName is always true at this point

                fireParameter(
                    header, headerType, data, startName, nameCount, startValue, valueCount);

                index = lws(data, index, end);

                startName = index;
                nameCount = -1;
                valueCount = -1;
                foundName = false;
                foundValue = false;

                break;
              case '=':
                // end name
                // begin value
                if (!foundName) // name not set yet
                {
                  foundName = true;

                  nameCount = index - startName - 1;
                }
                inName = false; // switch to value

                index = lws(data, index, end);

                startValue = index;
                break;
              case '"':
                // quotes allowed in values only
                if (!inName) {
                  index = readToCloseQuote(data, index, end);

                  // if we are at the end, the value will get created below - don't create it twice
                  if (index != end) {
                    foundValue = true;
                    valueCount = index - startValue;
                  }
                } else {
                  throw generateDsSipParserException(
                      headerListener,
                      "Illegal: '\"' found in parameter name",
                      headerType,
                      data,
                      offset,
                      count);
                }
                break;
              case ' ':
              case '\t':
              case '\r':
              case '\n':
                // end name or value
                if (inName && !foundName) {
                  foundName = true;

                  nameCount = index - startName - 1;
                } else if (!inName && !foundValue) {
                  foundValue = true;

                  valueCount = index - startValue - 1;
                }

                // or just skip if already ended

                break;
            }

            // The last case that the switch statement cannot handle
            // We must get the last string (name or value here)
            // Too hard to do outside of loop, since we may or maynot get into the loop
            if (index == end) {
              if (inName) {
                if (!foundName) {
                  foundName = true;

                  nameCount = index - startName;
                }

                fireParameter(header, headerType, data, startName, nameCount, 0, 0);
              } else {
                if (!foundValue) {
                  foundValue = true;

                  valueCount = index - startValue;
                }

                fireParameter(
                    header, headerType, data, startName, nameCount, startValue, valueCount);
              }
            }
          }
        }

        // after getting the parameters or maybe just the host port we might be looking at a ')' or
        // a ','

        index = lws(data, index, end);

        // since we found a ',' - that is the end of this header
        // go back to the top of the loop and start parsing the next header
        if (lastCh == ',') {
          headerListener.headerFound(headerType, data, startHeader, commaIndex - startHeader, true);

          if (data[index] == ',') {
            // move past the comma
            index++;
          }

          continue; // start parsing the next header
        }

        // found a comment
        if (lastCh == '(') {
          int startComment = index - 1;

          // find the end of the comment
          index = readToCloseParen(data, index, end);

          fireElement(header, headerType, COMMENT, data, startComment, index - startComment);
        }

        // now there may be another header, look for ','

        index = lws(data, index, end);

        // since we found a ',' - that is the end of this header
        // go back to the top of the loop and start parsing the next header
        if (index < end && data[index] == ',') {
          headerListener.headerFound(headerType, data, startHeader, index - startHeader, true);

          // move past the comma
          index++;

          continue; // start parsing the next header
        }

        // if we get here, then there are no more headers to parse
        headerListener.headerFound(headerType, data, startHeader, index - startHeader, true);

        if (DsPerf.ON) DsPerf.stop(PARSE_VIA);
        return; // done parsing
      }
    }
    // catch (DsSipParserException e)
    // {
    // throw e;
    // }
    catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(headerListener, e, headerType, data, offset, count);
    }
  }

  protected static void parseUnknownHeader(
      DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      int end = offset + count;
      int index = lws(data, offset, end);
      int start = index;
      int backup = 0; // Handle WS before ':'

      while (index < end && data[index++] != ':') {
        // just find the colon
        if (data[index - 1] <= ' ') {
          ++backup;
        }
      }

      if (index == end) {
        throw generateDsSipParserException(
            headerListener, "No ':' in unknown header name", headerType, data, offset, count);
      }

      int nameStart = start;
      int nameCount = index - start - 1 - backup;

      // Trim WS from the tail of the header and from the front
      start = lws(data, index, end);
      index = end - 1;

      if (index == start || index + 1 == start) // 1 char header with 'ch'\n or 2 char with '\r'\n
      {
        if (data[index] <= ' ') // no data, just WS
        {
          headerListener.unknownFound(data, nameStart, nameCount, start, 0, true);
        } else {
          headerListener.unknownFound(data, nameStart, nameCount, start, 1, true);
        }
      } else {
        // from the end find the first non-WS char, that is the last char we are interested in
        while (index > start && data[index--] <= ' ') {}

        // we are normally 1 char past the last char, now we are 1 char before it, hence + 2

        // use this instead of fireElement
        headerListener.unknownFound(data, nameStart, nameCount, start, index - start + 2, true);
      }
    } catch (DsSipParserException e) {
      throw e;
    } catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(headerListener, e, headerType, data, offset, count);
    }
  }

  // Authentication-Info
  // Merge this with parseAuth at some point, since this code is really shared.  But, I need to fix
  // this NOW. - jsm
  static void parseAuthParams(
      DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      int index = offset;
      int end = offset + count;

      int startHeader = index;
      DsSipElementListener header = headerListener.headerBegin(headerType);
      if (header == null) {
        headerListener.headerFound(headerType, data, startHeader, end - startHeader, true);
        return; // done - lazy parse only
      }

      index = lws(data, index, end);

      // Params are different here, they start with WS and then are separated by ','
      byte ch;

      boolean inName = true;

      boolean foundName = false;
      boolean foundValue = false;
      int nameCount = -1;
      int valueCount = -1;

      int startName = index;
      int startValue = -1;

      boolean moreHeaders = false;
      while (index < end && !moreHeaders) // still stuff to check for?
      {
        switch (data[index++]) {
          case ',':
            // begin new param
            // end name or value
            if (inName) {
              if (!foundName) // name not set yet
              {
                foundName = true;
                nameCount = index - startName - 1;
              }
            } else {
              inName = true;
              if (!foundValue) // value not set yet
              {
                foundValue = true;
                valueCount = index - startValue - 1;
              }
            }
            // inName is always true at this point

            fireParameter(header, headerType, data, startName, nameCount, startValue, valueCount);

            index = lws(data, index, end);

            startName = index;
            nameCount = -1;
            valueCount = -1;
            foundName = false;
            foundValue = false;

            // since the grammar allows for comma separates list of comma separated lists,
            // we need to look ahead every time we find a ',' to see if there are more parameters
            // for this header or if there is a new header

            // first char of next token
            int lookAheadIndex = lws(data, index, end);
            ch = data[lookAheadIndex];

            // find out if there is WS
            // if there is then there is more work to do to make a determination
            // if an '=' or a ',' is found then we know there are more parameters
            while (lookAheadIndex < end && ch > ' ' && ch != '=' && ch != ',') {
              ch = data[lookAheadIndex++];
            }

            if (ch != '=' && ch != ',') // check next non-WS char
            {
              lookAheadIndex = lws(data, lookAheadIndex, end);

              ch = data[lookAheadIndex];

              if (ch != '=' && ch != ',') // if this is true, then we have a new header
              {
                moreHeaders = true;
              }
            }

            break;
          case '=':
            // end name
            // begin value
            if (!foundName) // name not set yet
            {
              foundName = true;

              nameCount = index - startName - 1;
            }
            inName = false; // switch to value

            index = lws(data, index, end);

            startValue = index;
            break;
          case '"':
            // quotes allowed in values only
            if (!inName) {
              index = readToCloseQuote(data, index, end);

              // if we are at the end, the value will get created below - don't create it twice
              if (index != end) {
                foundValue = true;
                valueCount = index - startValue;
              }
            } else {
              throw generateDsSipParserException(
                  headerListener,
                  "Illegal: '\"' found in parameter name",
                  headerType,
                  data,
                  offset,
                  count);
            }
            break;
          case ' ':
          case '\t':
          case '\r':
          case '\n':
            // end name or value
            if (inName && !foundName) {
              foundName = true;

              nameCount = index - startName - 1;
            } else if (!inName && !foundValue) {
              foundValue = true;

              valueCount = index - startValue - 1;
            }

            // or just skip if already ended

            break;
        }

        // The last case that the switch statement cannot handle
        // We must get the last string (name or value here)
        // Too hard to do outside of loop, since we may or maynot get into the loop
        if (index == end) {
          if (inName) {
            if (!foundName) {
              foundName = true;

              nameCount = index - startName;
            }

            fireParameter(header, headerType, data, startName, nameCount, 0, 0);
          } else {
            if (!foundValue) {
              foundValue = true;

              valueCount = index - startValue;
            }

            fireParameter(header, headerType, data, startName, nameCount, startValue, valueCount);
          }
        }
      }

      headerListener.headerFound(headerType, data, startHeader, index - startHeader, true);
    } catch (DsSipParserException e) {
      throw e;
    } catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(headerListener, e, headerType, data, offset, count);
    }
  }

  // Authorization
  // Proxy-Authorization
  // Proxy-Authenticate
  // WWW-Authenticate
  protected static void parseAuth(
      DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      int index = offset;
      int end = offset + count;

      while (true) {
        int startHeader = index;
        DsSipElementListener header = headerListener.headerBegin(headerType);
        if (header == null) {
          headerListener.headerFound(headerType, data, startHeader, end - startHeader, true);
          return; // done - lazy parse only
        }

        index = lws(data, index, end);

        // handle empty case
        if (index == end) {
          headerListener.headerFound(headerType, data, startHeader, end - startHeader, true);
          return;
        }

        // handle empty element - ",,"
        if (data[index] == ',') {
          // ignore this empty element
          index++;
          continue;
        }

        int start = index;

        int commaIndex = -1;
        byte ch;

        // find the WS separator for the token
        while (index < end && data[index++] > ' ') {}

        // index == end?

        // should be "Digest"
        fireElement(header, headerType, SINGLE_VALUE, data, start, index - start - 1);

        index = lws(data, index, end);

        // check to see if this is "Basic"
        if (index - start >= 5
            && data[start] == 'B'
            && data[start + 1] == 'a'
            && data[start + 2] == 's'
            && data[start + 3] == 'i'
            && data[start + 4] == 'c') {
          start = index;

          while (true) {
            ch = data[index++];
            if (ch <= ' ') {
              fireElement(header, headerType, BASIC_COOKIE, data, start, index - start - 1);

              // check for more headers
              index = lws(data, index, end);
              if (index >= end) {
                // no more headers
                headerListener.headerFound(headerType, data, startHeader, end - startHeader, true);
                return;
              } else if (data[index] == ',') {
                // there is another header
                headerListener.headerFound(
                    headerType, data, startHeader, index - startHeader - 1, true);
                index++;
                break;
              } else {
                throw generateDsSipParserException(
                    headerListener,
                    "Illegal: Extra characters found after Basic Cookie",
                    headerType,
                    data,
                    offset,
                    count);
              }
            } else if (ch == ',') {
              fireElement(header, headerType, BASIC_COOKIE, data, start, index - start - 1);

              // there is another header
              headerListener.headerFound(
                  headerType, data, startHeader, index - startHeader - 1, true);
              break;
            } else if (index == end) {
              fireElement(header, headerType, BASIC_COOKIE, data, start, index - start);

              // no more headers
              headerListener.headerFound(headerType, data, startHeader, end - startHeader, true);
              return;
            }
          }

          continue;
        }

        // if we get here, it is Digest or other, but not Basic.

        // Ready to look for params
        // Params are different here, they start with WS and then are separated by ','
        boolean inName = true;

        boolean foundName = false;
        boolean foundValue = false;
        int nameCount = -1;
        int valueCount = -1;

        int startName = index;
        int startValue = -1;

        boolean moreHeaders = false;
        while (index < end && !moreHeaders) // still stuff to check for?
        {
          switch (data[index++]) {
            case ',':
              commaIndex = index - 1;

              // begin new param
              // end name or value
              if (inName) {
                if (!foundName) // name not set yet
                {
                  foundName = true;
                  nameCount = index - startName - 1;
                }
              } else {
                inName = true;
                if (!foundValue) // value not set yet
                {
                  foundValue = true;
                  valueCount = index - startValue - 1;
                }
              }
              // inName is always true at this point

              fireParameter(header, headerType, data, startName, nameCount, startValue, valueCount);

              index = lws(data, index, end);

              startName = index;
              nameCount = -1;
              valueCount = -1;
              foundName = false;
              foundValue = false;

              // since the grammar allows for comma separates list of comma separated lists,
              // we need to look ahead every time we find a ',' to see if there are more parameters
              // for this header or if there is a new header

              // first char of next token
              int lookAheadIndex = lws(data, index, end);
              ch = data[lookAheadIndex];

              // find out if there is WS
              // if there is then there is more work to do to make a determination
              // if an '=' or a ',' is found then we know there are more parameters
              while (lookAheadIndex < end && ch > ' ' && ch != '=' && ch != ',') {
                ch = data[lookAheadIndex++];
              }

              if (ch != '=' && ch != ',') // check next non-WS char
              {
                lookAheadIndex = lws(data, lookAheadIndex, end);

                ch = data[lookAheadIndex];

                if (ch != '=' && ch != ',') // if this is true, then we have a new header
                {
                  moreHeaders = true;
                }
              }

              break;
            case '=':
              // end name
              // begin value
              if (!foundName) // name not set yet
              {
                foundName = true;

                nameCount = index - startName - 1;
              }
              inName = false; // switch to value

              index = lws(data, index, end);

              startValue = index;
              break;
            case '"':
              // quotes allowed in values only
              if (!inName) {
                index = readToCloseQuote(data, index, end);

                // if we are at the end, the value will get created below - don't create it twice
                if (index != end) {
                  foundValue = true;
                  valueCount = index - startValue;
                }
              } else {
                throw generateDsSipParserException(
                    headerListener,
                    "Illegal: '\"' found in parameter name",
                    headerType,
                    data,
                    offset,
                    count);
              }
              break;
            case ' ':
            case '\t':
            case '\r':
            case '\n':
              // end name or value
              if (inName && !foundName) {
                foundName = true;

                nameCount = index - startName - 1;
              } else if (!inName && !foundValue) {
                foundValue = true;

                valueCount = index - startValue - 1;
              }

              // or just skip if already ended

              break;
          }

          // The last case that the switch statement cannot handle
          // We must get the last string (name or value here)
          // Too hard to do outside of loop, since we may or maynot get into the loop
          if (index == end) {
            if (inName) {
              if (!foundName) {
                foundName = true;

                nameCount = index - startName;
              }

              fireParameter(header, headerType, data, startName, nameCount, 0, 0);
            } else {
              if (!foundValue) {
                foundValue = true;

                valueCount = index - startValue;
              }

              fireParameter(header, headerType, data, startName, nameCount, startValue, valueCount);
            }
          }
        }

        if (moreHeaders) {
          headerListener.headerFound(headerType, data, startHeader, commaIndex - startHeader, true);
        } else {
          headerListener.headerFound(headerType, data, startHeader, index - startHeader, true);
          return;
        }
      }
    } catch (DsSipParserException e) {
      throw e;
    } catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(headerListener, e, headerType, data, offset, count);
    }
  }

  static void parsePrivacy(
      DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      DsSipElementListener header = headerListener.headerBegin(headerType);
      if (header == null) {
        headerListener.headerFound(headerType, data, offset, count, true);
        return; // done - lazy parse only
      }

      int end = count + offset;
      int index = lws(data, offset, end);
      int startHeader = index;

      while (index < end) {
        // start now points to the first char of the next token
        int start = index;

        byte ch = data[index];

        // find the separator for the token
        while (index < end && ch > ' ' && ch != ';') {
          ch = data[index++];
        }

        if (index == end && ch > ' ' && ch != ';') {
          fireElement(header, headerType, SINGLE_VALUE, data, start, index - start);
        } else {
          fireElement(header, headerType, SINGLE_VALUE, data, start, index - start - 1);
        }

        // if WS is the separator then move to the next non-WS char to make decisions below
        if (ch <= ' ') // WS
        {
          index = lws(data, index, end);
          if (index < end) {
            ch = data[index++];
          }
        }

        // there are more tokens to parse
        while (ch == ';') {
          if (index < end && data[index] == ';') {
            ch = data[index++];
          }
          index = lws(data, index, end);
          ch = data[index];
        }
      }
      headerListener.headerFound(headerType, data, startHeader, index - startHeader, true);
    }
    // catch (DsSipParserException e)
    // {
    // throw e;
    // }
    catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(headerListener, e, headerType, data, offset, count);
    }
  }

  // Accept-Encoding
  // Accept-Language - language can be deeply parsed later
  // Content-Disposition
  // Content-Language - language can be deeply parsed later
  // Service-Agent-Phase
  // Service-Agent-Context
  // Service-Agent-Application
  // Event
  // Replaces
  // more
  // also - token headers w/o params
  protected static void parseTokenListWithParams(
      DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      int index = offset;
      int end = offset + count;

      while (true) {
        int startHeader = index;
        DsSipElementListener header = headerListener.headerBegin(headerType);
        if (header == null) {
          headerListener.headerFound(headerType, data, startHeader, end - startHeader, true);
          return; // done - lazy parse only
        }

        index = lws(data, index, end);

        // handle empty case
        if (index == end) {
          headerListener.headerFound(headerType, data, startHeader, end - startHeader, true);
          return;
        }

        // handle empty element - ",,"
        if (data[index] == ',') {
          // ignore this empty element
          index++;
          continue;
        }

        int start = index;

        int commaIndex = -1;

        byte ch = data[index];

        // find the separator for the token
        while (index < end && ch > ' ' && ch != ';' && ch != ',') {
          ch = data[index++];
        }

        if (index == end && ch > ' ' && ch != ';' && ch != ',') {
          fireElement(header, headerType, SINGLE_VALUE, data, start, index - start);
        } else {
          fireElement(header, headerType, SINGLE_VALUE, data, start, index - start - 1);
        }

        // if WS is the separator then move to the next non-WS char to make decisions below
        if (ch <= ' ') // WS
        {
          index = lws(data, index, end);
          if (index < end) {
            ch = data[index++];
          }
        }

        // there are parameters to parse
        if (ch == ';') {
          // ready to look for params
          boolean inName = true;

          boolean foundName = false;
          boolean foundValue = false;
          int nameCount = -1;
          int valueCount = -1;

          // trim any LWS that might exists after the ';'
          index = lws(data, index, end);

          int startName = index;
          int startValue = -1;

          boolean done = false;
          while (index < end && !done) // still stuff to check for?
          {
            switch (data[index++]) {
              case ';':
                // begin new param
                // end name or value
                if (inName) {
                  if (!foundName) // name not set yet
                  {
                    foundName = true;
                    nameCount = index - startName - 1;
                  }
                } else {
                  inName = true;
                  if (!foundValue) // value not set yet
                  {
                    foundValue = true;
                    valueCount = index - startValue - 1;
                  }
                }
                // inName is always true at this point

                fireParameter(
                    header, headerType, data, startName, nameCount, startValue, valueCount);

                index = lws(data, index, end);

                startName = index;
                nameCount = -1;
                valueCount = -1;
                foundName = false;
                foundValue = false;

                break;
              case '=':
                // end name
                // begin value
                if (!foundName) // name not set yet
                {
                  foundName = true;

                  nameCount = index - startName - 1;
                }
                inName = false; // switch to value

                index = lws(data, index, end);

                startValue = index;
                break;
              case '"':
                // quotes allowed in values only
                if (!inName) {
                  index = readToCloseQuote(data, index, end);

                  // if we are at the end, the value will get created below - don't create it twice
                  if (index != end) {
                    foundValue = true;
                    valueCount = index - startValue;
                  }
                } else {
                  throw generateDsSipParserException(
                      headerListener,
                      "Illegal: '\"' found in parameter name",
                      headerType,
                      data,
                      offset,
                      count);
                }
                break;
              case ',':
                done = true;
                ch = (byte) ',';
                // end name or value
                if (inName && !foundName) {
                  foundName = true;
                  nameCount = index - startName - 1;
                } else if (!inName && !foundValue) {
                  foundValue = true;
                  valueCount = index - startValue - 1;
                }

                if (!foundValue) {
                  fireParameter(header, headerType, data, startName, nameCount, 0, 0);
                } else {
                  fireParameter(
                      header, headerType, data, startName, nameCount, startValue, valueCount);
                }
                break;
              case ' ':
              case '\t':
              case '\r':
              case '\n':
                // end name or value
                if (inName && !foundName) {
                  foundName = true;

                  nameCount = index - startName - 1;
                } else if (!inName && !foundValue) {
                  foundValue = true;

                  valueCount = index - startValue - 1;
                }

                // or just skip if already ended

                break;
            }

            // The last case that the switch statement cannot handle
            // We must get the last string (name or value here)
            // Too hard to do outside of loop, since we may or maynot get into the loop
            if (index == end) {
              if (inName) {
                if (!foundName) {
                  foundName = true;

                  nameCount = index - startName;
                }

                fireParameter(header, headerType, data, startName, nameCount, 0, 0);
              } else {
                if (!foundValue) {
                  foundValue = true;

                  valueCount = index - startValue;
                }

                fireParameter(
                    header, headerType, data, startName, nameCount, startValue, valueCount);
              }
            }
          }
        }

        if (ch == ',') {
          commaIndex = index - 1;
          headerListener.headerFound(headerType, data, startHeader, commaIndex - startHeader, true);
        } else {
          headerListener.headerFound(headerType, data, startHeader, index - startHeader, true);
          return;
        }
      }
    } catch (DsSipParserException e) {
      throw e;
    } catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(headerListener, e, headerType, data, offset, count);
    }
  }

  // CAFFEINE 2.0 DEVELOPMENT - Cisco-Maintenance-Mode Support
  // similar to parseTokenListWithParams, but may or may not have URI
  protected static void parseTokenListWithParamsWithOptionalURI(
      DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      int index = offset;
      int end = offset + count;

      while (true) {
        int startHeader = index;
        DsSipElementListener header = headerListener.headerBegin(headerType);
        if (header == null) {
          headerListener.headerFound(headerType, data, startHeader, end - startHeader, true);
          return; // done - lazy parse only
        }

        index = lws(data, index, end);

        // handle empty case
        if (index == end) {
          headerListener.headerFound(headerType, data, startHeader, end - startHeader, true);
          return;
        }

        // handle empty element - ",,"
        if (data[index] == ',') {
          // ignore this empty element
          index++;
          continue;
        }

        int start = index;

        int commaIndex = -1;

        byte ch = data[index];

        // find the separator for the token
        while (index < end && ch > ' ' && ch != ';' && ch != ',') {
          ch = data[index++];
        }

        if (index == end && ch > ' ' && ch != ';' && ch != ',') {
          fireElement(header, headerType, DISPLAY_NAME, data, start, index - start);
        } else {
          fireElement(header, headerType, DISPLAY_NAME, data, start, index - start - 1);
        }

        // todo: what'er the values in fireElement
        if (ch != ';') // find optional URI <...>
        {
          boolean noUri = false;
          while (data[index++] != '<') // find start of NameAddr
          {
            // just find the <
            if (data[index] == ';') {
              noUri = true;
              break;
            }
          }
          int startUrl = index; // points to first char in url
          if (!noUri) {
            while (data[index++] != '>') { // find end of NameAddr
              // just find the >
            }
            int urlCount = index - 1 - startUrl; // index points one char past >
            fireElement(header, headerType, UNKNOWN_URL, data, startUrl, urlCount);
          }
        }

        // if WS is the separator then move to the next non-WS char to make decisions below
        if (ch <= ' ') // WS
        {
          index = lws(data, index, end);
          if (index < end) {
            ch = data[index++];
          }
        }

        // there are parameters to parse
        if (ch == ';') {
          // ready to look for params
          boolean inName = true;

          boolean foundName = false;
          boolean foundValue = false;
          int nameCount = -1;
          int valueCount = -1;

          // trim any LWS that might exists after the ';'
          index = lws(data, index, end);

          int startName = index;
          int startValue = -1;

          boolean done = false;
          while (index < end && !done) // still stuff to check for?
          {
            switch (data[index++]) {
              case ';':
                // begin new param
                // end name or value
                if (inName) {
                  if (!foundName) // name not set yet
                  {
                    foundName = true;
                    nameCount = index - startName - 1;
                  }
                } else {
                  inName = true;
                  if (!foundValue) // value not set yet
                  {
                    foundValue = true;
                    valueCount = index - startValue - 1;
                  }
                }
                // inName is always true at this point

                fireParameter(
                    header, headerType, data, startName, nameCount, startValue, valueCount);

                index = lws(data, index, end);

                startName = index;
                nameCount = -1;
                valueCount = -1;
                foundName = false;
                foundValue = false;

                break;
              case '=':
                // end name
                // begin value
                if (!foundName) // name not set yet
                {
                  foundName = true;

                  nameCount = index - startName - 1;
                }
                inName = false; // switch to value

                index = lws(data, index, end);

                startValue = index;
                break;
              case '"':
                // quotes allowed in values only
                if (!inName) {
                  index = readToCloseQuote(data, index, end);

                  // if we are at the end, the value will get created below - don't create it twice
                  if (index != end) {
                    foundValue = true;
                    valueCount = index - startValue;
                  }
                } else {
                  throw generateDsSipParserException(
                      headerListener,
                      "Illegal: '\"' found in parameter name",
                      headerType,
                      data,
                      offset,
                      count);
                }
                break;
              case ',':
                done = true;
                ch = (byte) ',';
                // end name or value
                if (inName && !foundName) {
                  foundName = true;
                  nameCount = index - startName - 1;
                } else if (!inName && !foundValue) {
                  foundValue = true;
                  valueCount = index - startValue - 1;
                }

                if (!foundValue) {
                  fireParameter(header, headerType, data, startName, nameCount, 0, 0);
                } else {
                  fireParameter(
                      header, headerType, data, startName, nameCount, startValue, valueCount);
                }
                break;
              case ' ':
              case '\t':
              case '\r':
              case '\n':
                // end name or value
                if (inName && !foundName) {
                  foundName = true;

                  nameCount = index - startName - 1;
                } else if (!inName && !foundValue) {
                  foundValue = true;

                  valueCount = index - startValue - 1;
                }

                // or just skip if already ended

                break;
            }

            // The last case that the switch statement cannot handle
            // We must get the last string (name or value here)
            // Too hard to do outside of loop, since we may or maynot get into the loop
            if (index == end) {
              if (inName) {
                if (!foundName) {
                  foundName = true;

                  nameCount = index - startName;
                }

                fireParameter(header, headerType, data, startName, nameCount, 0, 0);
              } else {
                if (!foundValue) {
                  foundValue = true;

                  valueCount = index - startValue;
                }

                fireParameter(
                    header, headerType, data, startName, nameCount, startValue, valueCount);
              }
            }
          }
        }

        if (ch == ',') {
          commaIndex = index - 1;
          headerListener.headerFound(headerType, data, startHeader, commaIndex - startHeader, true);
        } else {
          headerListener.headerFound(headerType, data, startHeader, index - startHeader, true);
          return;
        }
      }
    } catch (DsSipParserException e) {
      throw e;
    } catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(headerListener, e, headerType, data, offset, count);
    }
  }

  // parase headers without token
  // P-Charging-Function-Addresses
  // P-Charging-Vector
  static void parseParams(
      DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      int index = offset;
      int end = offset + count;

      int startHeader = index;
      DsSipElementListener header = headerListener.headerBegin(headerType);
      if (header == null) {
        headerListener.headerFound(headerType, data, startHeader, end - startHeader, true);
        return; // done - lazy parse only
      }

      index = lws(data, index, end);

      // handle empty case
      if (index == end) {
        headerListener.headerFound(headerType, data, startHeader, end - startHeader, true);
        return;
      }

      // ready to look for params
      boolean inName = true;

      boolean foundName = false;
      boolean foundValue = false;
      int nameCount = -1;
      int valueCount = -1;

      int startName = index;
      int startValue = -1;

      boolean done = false;
      while (index < end && !done) // still stuff to check for?
      {
        switch (data[index++]) {
          case ';':
            // begin new param
            // end name or value
            if (inName) {
              if (!foundName) // name not set yet
              {
                foundName = true;
                nameCount = index - startName - 1;
              }
            } else {
              inName = true;
              if (!foundValue) // value not set yet
              {
                foundValue = true;
                valueCount = index - startValue - 1;
              }
            }
            // inName is always true at this point

            fireParameter(header, headerType, data, startName, nameCount, startValue, valueCount);

            index = lws(data, index, end);

            startName = index;
            nameCount = -1;
            valueCount = -1;
            foundName = false;
            foundValue = false;

            break;
          case '=':
            // end name
            // begin value
            if (!foundName) // name not set yet
            {
              foundName = true;

              nameCount = index - startName - 1;
            }
            inName = false; // switch to value

            index = lws(data, index, end);

            startValue = index;
            break;
          case '"':
            // quotes allowed in values only
            if (!inName) {
              index = readToCloseQuote(data, index, end);

              // if we are at the end, the value will get created below - don't create it twice
              if (index != end) {
                foundValue = true;
                valueCount = index - startValue;
              }
            } else {
              throw generateDsSipParserException(
                  headerListener,
                  "Illegal: '\"' found in parameter name",
                  headerType,
                  data,
                  offset,
                  count);
            }
            break;
          case ' ':
          case '\t':
          case '\r':
          case '\n':
            // end name or value
            if (inName && !foundName) {
              foundName = true;

              nameCount = index - startName - 1;
            } else if (!inName && !foundValue) {
              foundValue = true;

              valueCount = index - startValue - 1;
            }

            // or just skip if already ended

            break;
        }

        // The last case that the switch statement cannot handle
        // We must get the last string (name or value here)
        // Too hard to do outside of loop, since we may or maynot get into the loop
        if (index == end) {
          if (inName) {
            if (!foundName) {
              foundName = true;

              nameCount = index - startName;
            }

            fireParameter(header, headerType, data, startName, nameCount, 0, 0);
          } else {
            if (!foundValue) {
              foundValue = true;

              valueCount = index - startValue;
            }

            fireParameter(header, headerType, data, startName, nameCount, startValue, valueCount);
          }
        }
      }

      headerListener.headerFound(headerType, data, startHeader, index - startHeader, true);
      return;
    } catch (DsSipParserException e) {
      throw e;
    } catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(headerListener, e, headerType, data, offset, count);
    }
  }

  // P-Visited-Network-ID
  static void parseWordListWithParams(
      DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      int index = offset;
      int end = offset + count;

      while (true) {
        int startHeader = index;
        DsSipElementListener header = headerListener.headerBegin(headerType);
        if (header == null) {
          headerListener.headerFound(headerType, data, startHeader, end - startHeader, true);
          return; // done - lazy parse only
        }

        index = lws(data, index, end);

        // handle empty case
        if (index == end) {
          headerListener.headerFound(headerType, data, startHeader, end - startHeader, true);
          return;
        }

        // handle empty element - ",,"
        if (data[index] == ',') {
          // ignore this empty element
          index++;
          continue;
        }

        int start = index;

        int commaIndex = -1;

        byte ch = data[index];

        // handle token or quoted-string
        if (ch == '"') {
          index++; // move past first '"'
          index = readToCloseQuote(data, index, end);

          fireElement(header, headerType, SINGLE_VALUE, data, start, index - start);
          if (index < end) {
            ch = data[index++];
          } else {
            headerListener.headerFound(headerType, data, startHeader, index - startHeader, true);
            return;
          }
        } else // token
        {
          // find the separator for the token
          while (index < end && ch > ' ' && ch != ';' && ch != ',') {
            ch = data[index++];
          }

          if (index == end && ch > ' ' && ch != ';' && ch != ',') {
            fireElement(header, headerType, SINGLE_VALUE, data, start, index - start);
          } else {
            fireElement(header, headerType, SINGLE_VALUE, data, start, index - start - 1);
          }
        } // end of token or quoted-string

        // if WS is the separator then move to the next non-WS char to make decisions below
        if (ch <= ' ') // WS
        {
          index = lws(data, index, end);
          if (index < end) {
            ch = data[index++];
          }
        }

        // there are parameters to parse
        if (ch == ';') {
          // ready to look for params
          boolean inName = true;

          boolean foundName = false;
          boolean foundValue = false;
          int nameCount = -1;
          int valueCount = -1;

          // trim any LWS that might exists after the ';'
          index = lws(data, index, end);

          int startName = index;
          int startValue = -1;

          boolean done = false;
          while (index < end && !done) // still stuff to check for?
          {
            switch (data[index++]) {
              case ';':
                // begin new param
                // end name or value
                if (inName) {
                  if (!foundName) // name not set yet
                  {
                    foundName = true;
                    nameCount = index - startName - 1;
                  }
                } else {
                  inName = true;
                  if (!foundValue) // value not set yet
                  {
                    foundValue = true;
                    valueCount = index - startValue - 1;
                  }
                }
                // inName is always true at this point

                fireParameter(
                    header, headerType, data, startName, nameCount, startValue, valueCount);

                index = lws(data, index, end);

                startName = index;
                nameCount = -1;
                valueCount = -1;
                foundName = false;
                foundValue = false;

                break;
              case '=':
                // end name
                // begin value
                if (!foundName) // name not set yet
                {
                  foundName = true;

                  nameCount = index - startName - 1;
                }
                inName = false; // switch to value

                index = lws(data, index, end);

                startValue = index;
                break;
              case '"':
                // quotes allowed in values only
                if (!inName) {
                  index = readToCloseQuote(data, index, end);

                  // if we are at the end, the value will get created below - don't create it twice
                  if (index != end) {
                    foundValue = true;
                    valueCount = index - startValue;
                  }
                } else {
                  throw generateDsSipParserException(
                      headerListener,
                      "Illegal: '\"' found in parameter name",
                      headerType,
                      data,
                      offset,
                      count);
                }
                break;
              case ',':
                done = true;
                ch = (byte) ',';
                // end name or value
                if (inName && !foundName) {
                  foundName = true;
                  nameCount = index - startName - 1;
                } else if (!inName && !foundValue) {
                  foundValue = true;
                  valueCount = index - startValue - 1;
                }

                if (!foundValue) {
                  fireParameter(header, headerType, data, startName, nameCount, 0, 0);
                } else {
                  fireParameter(
                      header, headerType, data, startName, nameCount, startValue, valueCount);
                }
                break;
              case ' ':
              case '\t':
              case '\r':
              case '\n':
                // end name or value
                if (inName && !foundName) {
                  foundName = true;

                  nameCount = index - startName - 1;
                } else if (!inName && !foundValue) {
                  foundValue = true;

                  valueCount = index - startValue - 1;
                }

                // or just skip if already ended

                break;
            }

            // The last case that the switch statement cannot handle
            // We must get the last string (name or value here)
            // Too hard to do outside of loop, since we may or maynot get into the loop
            if (index == end) {
              if (inName) {
                if (!foundName) {
                  foundName = true;

                  nameCount = index - startName;
                }

                fireParameter(header, headerType, data, startName, nameCount, 0, 0);
              } else {
                if (!foundValue) {
                  foundValue = true;

                  valueCount = index - startValue;
                }

                fireParameter(
                    header, headerType, data, startName, nameCount, startValue, valueCount);
              }
            }
          }
        }

        if (ch == ',') {
          commaIndex = index - 1;
          headerListener.headerFound(headerType, data, startHeader, commaIndex - startHeader, true);
        } else {
          headerListener.headerFound(headerType, data, startHeader, index - startHeader, true);
          return;
        }
      }
    } catch (DsSipParserException e) {
      throw e;
    } catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(headerListener, e, headerType, data, offset, count);
    }
  }

  // Timestamp
  protected static void parseTimestamp(
      DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      DsSipElementListener header = headerListener.headerBegin(headerType);
      if (header == null) {
        headerListener.headerFound(headerType, data, offset, count, true);
        return; // done - lazy parse only
      }

      // end points 1 past the last slot, treat it like .length
      int end = count + offset;
      int index = lws(data, offset, end);
      int start = index;

      // skip any leading white space
      while (index < end && data[index++] > ' ') {}

      if (index == end && data[index - 1] > ' ') {
        fireElement(header, headerType, SINGLE_VALUE, data, start, index - start);
      } else {
        fireElement(header, headerType, SINGLE_VALUE, data, start, index - start - 1);
      }

      start = index = lws(data, index, end);

      if (index < end) {
        while (index < end && data[index++] > ' ') {}

        if (index == end && data[index - 1] > ' ') {
          fireElement(header, headerType, DELAY, data, start, index - start);
        } else {
          fireElement(header, headerType, DELAY, data, start, index - start - 1);
        }
      }

      headerListener.headerFound(headerType, data, offset, count, true);
    }
    // catch (DsSipParserException e)
    // {
    // throw e;
    // }
    catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(headerListener, e, headerType, data, offset, count);
    }
  }

  // Server
  // User-Agent
  protected static void parseServerUserAgent(
      DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      DsSipElementListener header = headerListener.headerBegin(headerType);
      if (header == null) {
        headerListener.headerFound(headerType, data, offset, count, true);
        return; // done - lazy parse only
      }

      int end = count + offset;
      int index = lws(data, offset, end);
      int elementId = TYPE;

      while (true) {
        index = lws(data, index, end);

        if (index >= end) {
          headerListener.headerFound(headerType, data, offset, count, true);
          return;
        }

        int start = index;

        if (data[index] == '(') // comment
        {
          index++;
          index = readToCloseParen(data, index, end);

          fireElement(header, headerType, COMMENT, data, start, index - start);

          elementId = TYPE;

          continue;
        } else // type or type/subtype
        {
          // find the separator for the type
          boolean done = false;
          int endIndex = end;
          while (!done && index < end) {
            switch (data[index]) {
              case ' ':
              case '\t':
              case '\r':
              case '\n':
                done = true;
                endIndex = index;
                // move to first non-WS char
                index = lws(data, index, end);
                break;
              case '(':
              case '/':
                done = true;
                endIndex = index;
                break;
            }

            if (!done) {
              index++;
            }
          }

          // endIndex now points to the actual last char

          fireElement(header, headerType, elementId, data, start, endIndex - start);

          if (index == end || data[index] == '(') {
            continue;
          }

          if (data[index] == '/') // found a sub type
          {
            elementId = SUB_TYPE;
            index++;
            continue;
          }

          elementId = TYPE;
        }
      }
    }
    // catch (DsSipParserException e)
    // {
    // throw e;
    // }
    catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(headerListener, e, headerType, data, offset, count);
    }
  }

  // Accept
  // Content-Type
  protected static void parseMediaTypeWithParams(
      DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      int index = offset;
      int end = offset + count;

      while (true) {
        int startHeader = index;
        DsSipElementListener header = headerListener.headerBegin(headerType);
        if (header == null) {
          headerListener.headerFound(headerType, data, startHeader, end - startHeader, true);
          return; // done - lazy parse only
        }

        index = lws(data, index, end);

        // handle empty case
        if (index == end) {
          headerListener.headerFound(headerType, data, startHeader, end - startHeader, true);
          return;
        }

        // handle empty element - ",,"
        if (data[index] == ',') {
          // ignore this empty element
          index++;
          continue;
        }

        int start = index;

        int commaIndex = -1;

        index = lws(data, index, end);

        byte ch = data[index];

        // find the separator for the type
        while (index < end && ch > ' ' && ch != '/') {
          ch = data[index++];
        }

        fireElement(header, headerType, TYPE, data, start, index - start - 1);

        // just in case we stopped on WS, not a '/', find the '/'
        while (index < end && ch != '/') {
          ch = data[index++];
        }

        index = lws(data, index, end);
        start = index;

        // find the separator for the sub-type
        while (index < end && ch > ' ' && ch != ',' && ch != ';') {
          ch = data[index++];
        }

        if (index == end && ch > ' ' && ch != ',' && ch != ';') {
          fireElement(header, headerType, SUB_TYPE, data, start, index - start);
        } else {
          fireElement(header, headerType, SUB_TYPE, data, start, index - start - 1);
        }

        // if WS is the separator then move to the next non-WS char to make decisions below
        if (ch <= ' ') // WS
        {
          index = lws(data, index, end);
          if (index < end) {
            ch = data[index++];
          }
        }

        // there are parameters to parse
        if (ch == ';') {
          // ready to look for params
          boolean inName = true;

          boolean foundName = false;
          boolean foundValue = false;
          int nameCount = -1;
          int valueCount = -1;

          // trim any LWS that might exists after the ';'
          index = lws(data, index, end);

          int startName = index;
          int startValue = -1;

          boolean done = false;
          while (index < end && !done) // still stuff to check for?
          {
            switch (data[index++]) {
              case ';':
                // begin new param
                // end name or value
                if (inName) {
                  if (!foundName) // name not set yet
                  {
                    foundName = true;
                    nameCount = index - startName - 1;
                  }
                } else {
                  inName = true;
                  if (!foundValue) // value not set yet
                  {
                    foundValue = true;
                    valueCount = index - startValue - 1;
                  }
                }
                // inName is always true at this point

                fireParameter(
                    header, headerType, data, startName, nameCount, startValue, valueCount);

                index = lws(data, index, end);

                startName = index;
                nameCount = -1;
                valueCount = -1;
                foundName = false;
                foundValue = false;

                break;
              case '=':
                // end name
                // begin value
                if (!foundName) // name not set yet
                {
                  foundName = true;

                  nameCount = index - startName - 1;
                }
                inName = false; // switch to value

                index = lws(data, index, end);

                startValue = index;
                break;
              case '"':
                // quotes allowed in values only
                if (!inName) {
                  index = readToCloseQuote(data, index, end);

                  // if we are at the end, the value will get created below - don't create it twice
                  if (index != end) {
                    foundValue = true;
                    valueCount = index - startValue;
                  }
                } else {
                  throw generateDsSipParserException(
                      headerListener,
                      "Illegal: '\"' found in parameter name",
                      headerType,
                      data,
                      offset,
                      count);
                }
                break;
              case ',':
                done = true;
                ch = (byte) ',';
                // end name or value
                if (inName && !foundName) {
                  foundName = true;
                  nameCount = index - startName - 1;
                } else if (!inName && !foundValue) {
                  foundValue = true;
                  valueCount = index - startValue - 1;
                }

                if (!foundValue) {
                  fireParameter(header, headerType, data, startName, nameCount, 0, 0);
                } else {
                  fireParameter(
                      header, headerType, data, startName, nameCount, startValue, valueCount);
                }
                break;
              case ' ':
              case '\t':
              case '\r':
              case '\n':
                // end name or value
                if (inName && !foundName) {
                  foundName = true;

                  nameCount = index - startName - 1;
                } else if (!inName && !foundValue) {
                  foundValue = true;

                  valueCount = index - startValue - 1;
                }

                // or just skip if already ended

                break;
            }

            // The last case that the switch statement cannot handle
            // We must get the last string (name or value here)
            // Too hard to do outside of loop, since we may or maynot get into the loop
            if (index == end) {
              if (inName) {
                if (!foundName) {
                  foundName = true;

                  nameCount = index - startName;
                }

                fireParameter(header, headerType, data, startName, nameCount, 0, 0);
              } else {
                if (!foundValue) {
                  foundValue = true;

                  valueCount = index - startValue;
                }

                fireParameter(
                    header, headerType, data, startName, nameCount, startValue, valueCount);
              }
            }
          }
        }

        if (ch == ',') {
          commaIndex = index - 1;
          headerListener.headerFound(headerType, data, startHeader, commaIndex - startHeader, true);
        } else {
          headerListener.headerFound(headerType, data, startHeader, index - startHeader, true);
          return;
        }
      }
    } catch (DsSipParserException e) {
      throw e;
    } catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(headerListener, e, headerType, data, offset, count);
    }
  }

  // Date
  protected static void parseDate(
      DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      DsSipElementListener header = headerListener.headerBegin(headerType);
      if (header == null) {
        headerListener.headerFound(headerType, data, offset, count, true);
        return; // done - lazy parse only
      }

      int end = count + offset - 1;
      int index = lws(data, offset, end);
      int start = index;

      if (start + SIP_DATE_LENGTH > end) {
        // throw exception
      }

      fireElement(header, headerType, SINGLE_VALUE, data, start, SIP_DATE_LENGTH);

      headerListener.headerFound(headerType, data, offset, count, true);
    }
    // catch (DsSipParserException e)
    // {
    // throw e;
    // }
    catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(headerListener, e, headerType, data, offset, count);
    }
  }

  protected static void parseEventPackage(
      DsSipElementListener element, int headerType, byte[] data, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      if (element == null) {
        return; // done - lazy parse only
      }

      int end = offset + count;
      int index = lws(data, offset, end);
      int start = index;

      int elementType = EVENT_PACKAGE;

      while (index <= end) {
        while (index < end && data[index++] != '.') {}

        if (index < end) {
          fireElement(element, headerType, elementType, data, start, index - start - 1);
        } else {
          fireElement(element, headerType, elementType, data, start, index - start);
          return;
        }

        start = index;

        // ch = data[index++];
        elementType = EVENT_SUB_PACKAGE;
      }
    }
    // catch (DsSipParserException e)
    // {
    // throw e;
    // }
    catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(element, e, headerType, SINGLE_VALUE, data, offset, count);
    }
  }

  // called from Date, Retry-After, Expires and expires parameter

  // Sat, 13 Nov 2001 23:29:00 GMT
  // 01234567890123456789012345678
  //          1         2
  protected static void parseSipDate(
      DsSipElementListener element, int headerType, byte[] data, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      if (element == null) {
        return; // done - lazy parse only
      }

      // check count? - jsm

      int start = offset;

      fireElement(element, headerType, DAY_OF_WEEK, data, start, 3);

      // next comes ", " - ignore

      // next comes 2 digit day of month
      fireElement(element, headerType, DAY_OF_MONTH, data, start + 5, 2);

      // the next three chars are the month
      fireElement(element, headerType, MONTH, data, start + 8, 3);

      // index 11 is a space and 12-15 are the 4 digit year
      fireElement(element, headerType, YEAR, data, start + 12, 4);

      // index 16 is a space and 17-18 are the 2 digit hour
      fireElement(element, headerType, HOUR, data, start + 17, 2);

      // index 19 is a ':' and 20-21 are the 2 digit minute
      fireElement(element, headerType, MINUTE, data, start + 20, 2);

      // index 22 is a ':' and 23-24 are the 2 digit second
      fireElement(element, headerType, SECOND, data, start + 23, 2);

      // 26-28 must be "GMT"
      if (data[start + 26] == 'G' && data[start + 27] == 'M' && data[start + 28] == 'T') {
        // assume this as default? - jsm
        fireElement(element, headerType, TIME_ZONE, data, start + 26, 3);
      } else {
        // throw exception...
      }
    }
    // catch (DsSipParserException e)
    // {
    // throw e;
    // }
    catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(element, e, headerType, SIP_DATE, data, offset, count);
    }
  }

  //        Retry-After  =  "Retry-After" HCOLON
  //                        ( SIP-date | delta-seconds )
  //                        [ comment ] *( SEMI retry-param )
  //        retry-param  =  "duration" EQUAL delta-seconds |
  //                        generic-param

  // Retry-After
  // Expires - really simpler, but fits the BNF for Retry-After
  // todo - TR: I made this method public so the tokenized parser wouldn't need to rewrite this same
  // code.
  // The new parser methods should remove this need once this code is merged back.
  public static void parseDateOrLong(
      DsSipHeaderListener headerListener, int headerType, byte[] data, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      DsSipElementListener header = headerListener.headerBegin(headerType);
      if (header == null) {
        headerListener.headerFound(headerType, data, offset, count, true);
        return; // done - lazy parse only
      }

      // end points 1 past the last slot, treat it like .length
      int end = count + offset;
      int index = lws(data, offset, end);
      int start = index;

      byte ch = data[index]; // first char tells us if it is a SIP-date or a long

      if (ch >= '0' && ch <= '9') // delta-seconds - long
      {
        while (index < end && ch > ' ' && ch != ';' && ch != '(') {
          ch = data[index++];
        }

        if (index == end && (ch >= '0' && ch <= '9')) {
          fireElement(header, headerType, DELTA_SECONDS, data, start, index - start);
        } else {
          fireElement(header, headerType, DELTA_SECONDS, data, start, index - start - 1);
        }
      } else // SIP-date
      {
        if ((index + SIP_DATE_LENGTH) > end) {
          throw generateDsSipParserException(
              headerListener, "Malformed date", headerType, data, index, end - index);
        }

        fireElement(header, headerType, SIP_DATE, data, start, SIP_DATE_LENGTH);

        index += SIP_DATE_LENGTH;

        if (index >= end) {
          headerListener.headerFound(headerType, data, offset, count, true);
          return;
        }
        ch = data[index++];
      }

      // index now points to the first char past the end of the first element (delta-seconds or
      // SIP-Date)

      if (index >= end) {
        headerListener.headerFound(headerType, data, offset, count, true);
        return;
      }

      if (ch <= ' ') {
        index = lws(data, index, end);

        if (index == end) {
          headerListener.headerFound(headerType, data, offset, count, true);
          return;
        }

        ch = data[index++];
      }

      if (ch == '(') // start of comment
      {
        int startComment = index - 1;

        // find the end of the comment
        index = readToCloseParen(data, index, end);

        fireElement(header, headerType, COMMENT, data, startComment, index - startComment);

        index = lws(data, index, end);

        if (index < end) {
          ch = data[index++];
        }
      }

      if (index == end) {
        headerListener.headerFound(headerType, data, offset, count, true);
        return;
      }

      if (ch == ';') // start of parameters
      {
        // ready to look for params
        boolean inName = true;

        boolean foundName = false;
        boolean foundValue = false;
        int nameCount = -1;
        int valueCount = -1;

        // trim any LWS that might exists after the ';'
        index = lws(data, index, end);

        int startName = index;
        int startValue = -1;

        while (index < end) // still stuff to check for?
        {
          switch (data[index++]) {
            case ';':
              // begin new param
              // end name or value
              if (inName) {
                if (!foundName) // name not set yet
                {
                  foundName = true;
                  nameCount = index - startName - 1;
                }
              } else {
                inName = true;
                if (!foundValue) // value not set yet
                {
                  foundValue = true;
                  valueCount = index - startValue - 1;
                }
              }
              // inName is always true at this point

              fireParameter(header, headerType, data, startName, nameCount, startValue, valueCount);

              index = lws(data, index, end);

              startName = index;
              nameCount = -1;
              valueCount = -1;
              foundName = false;
              foundValue = false;

              break;
            case '=':
              // end name
              // begin value
              if (!foundName) // name not set yet
              {
                foundName = true;

                nameCount = index - startName - 1;
              }
              inName = false; // switch to value

              index = lws(data, index, end);

              startValue = index;
              break;
            case '"':
              // quotes allowed in values only
              if (!inName) {
                index = readToCloseQuote(data, index, end);

                // if we are at the end, the value will get created below - don't create it twice
                if (index != end) {
                  foundValue = true;
                  valueCount = index - startValue;
                }
              } else {
                throw generateDsSipParserException(
                    headerListener,
                    "Illegal: '\"' found in parameter name",
                    headerType,
                    data,
                    offset,
                    count);
              }
              break;
            case ' ':
            case '\t':
            case '\r':
            case '\n':
              // end name or value
              if (inName && !foundName) {
                foundName = true;

                nameCount = index - startName - 1;
              } else if (!inName && !foundValue) {
                foundValue = true;

                valueCount = index - startValue - 1;
              }

              // or just skip if already ended

              break;
          }

          // The last case that the switch statement cannot handle
          // We must get the last string (name or value here)
          // Too hard to do outside of loop, since we may or maynot get into the loop
          if (index == end) {
            if (inName) {
              if (!foundName) {
                foundName = true;

                nameCount = index - startName;
              }

              fireParameter(header, headerType, data, startName, nameCount, 0, 0);
            } else {
              if (!foundValue) {
                foundValue = true;

                valueCount = index - startValue;
              }

              fireParameter(header, headerType, data, startName, nameCount, startValue, valueCount);
            }
          }
        }
      }

      headerListener.headerFound(headerType, data, offset, count, true);
    } catch (DsSipParserException e) {
      throw e;
    } catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(headerListener, e, headerType, data, offset, count);
    }
  }

  // called from Accept-Language and Content-Language.
  protected static void parseLanguage(
      DsSipElementListener element, int headerType, byte[] data, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      if (element == null) {
        return; // done - lazy parse only
      }

      int end = offset + count;
      int index = offset;
      int start;

      byte ch;
      int elementId = LANGUAGE_TAG;

      while (true) {
        start = index;
        ch = data[index++];
        // check if WS is allowed or not - jsm
        while (index < end && ch > ' ' && ch != '-') {
          ch = data[index++];
        }

        if (index == end) {
          fireElement(element, headerType, elementId, data, start, index - start);
          return;
        } else {
          fireElement(element, headerType, elementId, data, start, index - start - 1);

          // the rest are subtags
          elementId = LANGUAGE_SUBTAG;
        }

        if (ch != '-') {
          return; // only continue if there is another subtag
        }
      }
    }
    // catch (DsSipParserException e)
    // {
    // throw e;
    // }
    catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(element, e, headerType, SINGLE_VALUE, data, offset, count);
    }
  }

  protected static int getUrlType(byte[] data, int offset, int count) {
    if (count > 4
        && (data[offset] == 's' || data[offset] == 'S')
        && (data[offset + 1] == 'i' || data[offset + 1] == 'I')
        && (data[offset + 2] == 'p' || data[offset + 2] == 'P')
        && (data[offset + 3] == ':')) {
      return SIP_URL;
    } else if (count > 5
        && (data[offset] == 's' || data[offset] == 'S')
        && (data[offset + 1] == 'i' || data[offset + 1] == 'I')
        && (data[offset + 2] == 'p' || data[offset + 2] == 'P')
        && (data[offset + 3] == 's' || data[offset + 3] == 'S')
        && (data[offset + 4] == ':')) {
      return SIPS_URL;
    } else if (count > 4
        && (data[offset] == 't' || data[offset] == 'T')
        && (data[offset + 1] == 'e' || data[offset + 1] == 'E')
        && (data[offset + 2] == 'l' || data[offset + 2] == 'L')
        && (data[offset + 3] == ':')) {
      return TEL_URL;
    }
    // CAFFEINE 2.0 DEVELOPMENT - Content-ID Support
    else if (count > 4
        && (data[offset] == 'c' || data[offset] == 'C')
        && (data[offset + 1] == 'i' || data[offset + 1] == 'I')
        && (data[offset + 2] == 'd' || data[offset + 2] == 'D')
        && (data[offset + 3] == ':')) {
      return CID_URL;
    } else if (count > 5
        && (data[offset] == 'h' || data[offset] == 'H')
        && (data[offset + 1] == 't' || data[offset + 1] == 'T')
        && (data[offset + 2] == 't' || data[offset + 2] == 'T')
        && (data[offset + 3] == 'p' || data[offset + 3] == 'P')
        && (data[offset + 4] == ':')) {
      return HTTP_URL;
    } else // unknown url type
    {
      return UNKNOWN_URL;
    }
  }

  protected static int readToCloseParen(byte[] data, int index, int end) {
    byte ch;
    int parenCount = 1; // already read the first one
    while (index < end) {
      ch = data[index++];
      if (ch == ')') // found
      {
        parenCount--;
        if (parenCount == 0) {
          return index; // char past the ')'
        }
      } else if (ch == '(') // nested paren
      {
        parenCount++;
      } else if (ch == '\\') // escape - ignore next char
      {
        index++;
      }
    }

    // should never get here
    return end;
  }

  /**
   * Parses an unknown URL. Reports the results to the listener. This parser fires events for the
   * following element IDs:
   *
   * <blockquote>
   *
   * URI_SCHEME<br>
   * URI_DATA<br>
   *
   * </blockquote>
   *
   * @param element where to report the results.
   * @param data the URL.
   * @param offset the index that the URL starts at.
   * @param count the number of bytes in the URL.
   * @throws DsSipParserException if there is an exception while parsing.
   * @throws DsSipParserListenerException if the listener throws this exception.
   */
  public static void parseUnknownUrl(
      DsSipElementListener element, byte data[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      if (element == null) {
        return; // done - lazy parse only
      }

      int end = offset + count;
      int index = lws(data, offset, end);
      int start = index;

      // should we make sure that this is not a known url? - jsm

      // just find the colon
      while (index < end && data[index++] != ':') {}

      if (index == end) {
        // throw exception - no data
        return; // for now - jsm
      }

      fireElement(element, UNKNOWN_URL_ID, URI_SCHEME, data, start, index - start - 1);
      fireElement(element, UNKNOWN_URL_ID, URI_DATA, data, index, end - index);
    }
    // catch (DsSipParserException e)
    // {
    // throw e;
    // }
    catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(
          element, e, UNKNOWN_URL_ID, UNKNOWN_URL, data, offset, count);
    }
  }

  // CAFFEINE 2.0 DEVELOPMENT
  /**
   * Parses an entire Cid URL, including the scheme. Reports the results to the listener. This
   * parser fires events for the following element IDs:
   *
   * <blockquote>
   *
   * URI_SCHEME<br>
   * URI_DATA<br>
   *
   * </blockquote>
   *
   * @param element where to report the results.
   * @param data the URL.
   * @param offset the index that the URL starts at.
   * @param count the number of bytes in the URL.
   * @throws DsSipParserException if there is an exception while parsing.
   * @throws DsSipParserListenerException if the listener throws this exception.
   */
  public static void parseCidUrl(DsSipElementListener element, byte data[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      if (element == null) {
        return; // done - lazy parse only
      }

      int end = offset + count;
      int index = lws(data, offset, end);

      // just find the colon
      while (index < end && data[index++] != ':') {}

      if (index == end) {
        return;
      }

      boolean isCidUri = false;

      // make sure this is a Cid URL (case insensitive)
      if (data[index] == 'c' || data[index] == 'C') {
        if (data[index + 1] == 'i' || data[index + 1] == 'I') {
          if (data[index + 2] == 'd' || data[index + 2] == 'D') {
            if (data[index + 3] == ':') {
              isCidUri = true;
            }
          }
        }
      }

      if (!isCidUri) {
        throw generateDsSipParserException(
            element, "Not a cid URI", CID_URL_ID, CID_URL, data, offset, count);
      }

      fireElement(element, CID_URL_ID, URI_SCHEME, data, offset, 3);
      fireElement(element, CID_URL_ID, URI_DATA, data, offset + 4, count - 4);
    } catch (Exception e) {
      throw generateDsSipParserException(element, e, CID_URL_ID, CID_URL, data, offset, count);
    }
  }

  /**
   * Parses an entire Tel URL, including the scheme. Reports the results to the listener. This
   * parser fires events for the following element IDs:
   *
   * <blockquote>
   *
   * URI_SCHEME<br>
   *
   * </blockquote>
   *
   * @param element where to report the results.
   * @param data the URL.
   * @param offset the index that the URL starts at.
   * @param count the number of bytes in the URL.
   * @throws DsSipParserException if there is an exception while parsing.
   * @throws DsSipParserListenerException if the listener throws this exception.
   */
  public static void parseTelUrl(DsSipElementListener element, byte data[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      if (element == null) {
        return; // done - lazy parse only
      }

      boolean isTelUri = false;

      // make sure this is a SIP URL (case insensitive)
      if (data[offset] == 't' || data[offset] == 'T') {
        if (data[offset + 1] == 'e' || data[offset + 1] == 'E') {
          if (data[offset + 2] == 'l' || data[offset + 2] == 'L') {
            if (data[offset + 3] == ':') {
              isTelUri = true;
            }
          }
        }
      }

      if (!isTelUri) {
        throw generateDsSipParserException(
            element, "Not a tel URI", TEL_URL_ID, TEL_URL, data, offset, count);
      }

      fireElement(element, TEL_URL_ID, URI_SCHEME, data, offset, 3);

      // broken up into scheme and data now
      parseTelUrlData(element, data, offset + 4, count - 4);
    } catch (DsSipParserException e) {
      throw e;
    } catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(element, e, TEL_URL_ID, TEL_URL, data, offset, count);
    }
  }

  /**
   * Parses a Tel URL's data portion. Reports the results to the listener. This parser fires events
   * for the following element IDs:
   *
   * <blockquote>
   *
   * TEL_URL_NUMBER<br>
   * <br>
   *
   * </blockquote>
   *
   * @param element where to report the results.
   * @param data the URL.
   * @param offset the index that the URL starts at.
   * @param count the number of bytes in the URL.
   * @throws DsSipParserException if there is an exception while parsing.
   * @throws DsSipParserListenerException if the listener throws this exception.
   */
  public static void parseTelUrlData(
      DsSipElementListener element, byte data[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    if (DsPerf.ON) DsPerf.start(PARSE_TEL_URL_DATA);

    try {
      int end = offset + count;
      int index = offset;
      int start = index;

      // check for global vs. local number
      // if (data[index] == '+') // global phone number
      // {
      // index++;
      // }

      // now, we either get to the end or find parameters
      // that becomes the end of the phone number
      while (index < end && data[index++] != ';') {}

      if (index == end) {
        fireElement(element, TEL_URL_ID, TEL_URL_NUMBER, data, start, index - start);
        if (DsPerf.ON) DsPerf.stop(PARSE_TEL_URL_DATA);
        return;
      }

      fireElement(element, TEL_URL_ID, TEL_URL_NUMBER, data, start, index - start - 1);

      // ready for parameters
      int startName = index;
      int startValue = -1;

      int nameCount;
      int valueCount;

      // keep looking for params until EOF
      //   = end of name - look for value
      //      ; next param

      while (index < end) {
        switch (data[index++]) {
          case ' ': // EOF
          case '\t': // EOF
          case '\r': // EOF
          case '\n': // EOF
            nameCount = index - startName - 1;
            fireParameter(element, TEL_URL_ID, data, startName, nameCount, 0, 0);
            if (DsPerf.ON) DsPerf.stop(PARSE_TEL_URL_DATA);
            return;
          case '=': // end of name - start of value
            nameCount = index - startName - 1;

            boolean innerDone = false;
            startValue = index;
            while (!innerDone && index < end) {
              switch (data[index++]) {
                case ' ': // EOF
                case '\t': // EOF
                case '\r': // EOF
                case '\n': // EOF
                  valueCount = index - startValue - 1;
                  fireParameter(
                      element, TEL_URL_ID, data, startName, nameCount, startValue, valueCount);
                  if (DsPerf.ON) DsPerf.stop(PARSE_TEL_URL_DATA);
                  return;
                case ';': // next param
                  innerDone = true;
                  valueCount = index - startValue - 1;
                  fireParameter(
                      element, TEL_URL_ID, data, startName, nameCount, startValue, valueCount);
                  startName = index;
                  break;
                case '"': // quoted string - allowed, unlike SIP URL
                  innerDone = true;
                  index = readToCloseQuote(data, index, end);

                  // if we are at the end, the value will get created below - don't create it twice
                  if (index != end) {
                    valueCount = index - startValue - 1;
                    fireParameter(
                        element, TEL_URL_ID, data, startName, nameCount, startValue, valueCount);
                  }
                  break;
              }
            }

            if (index == end) {
              valueCount = index - startValue;
              fireParameter(
                  element, TEL_URL_ID, data, startName, nameCount, startValue, valueCount);
              if (DsPerf.ON) DsPerf.stop(PARSE_TEL_URL_DATA);
              return;
            }
            break;
          case ';': // end of name - start of new parameter - no value
            // no need to check for known types since they all have values
            nameCount = index - startName - 1;
            fireParameter(element, TEL_URL_ID, data, startName, nameCount, 0, 0);
            startName = index;
            break;
        }
      }

      if (index == end) {
        nameCount = index - startName;
        fireParameter(element, TEL_URL_ID, data, startName, nameCount, 0, 0);
        if (DsPerf.ON) DsPerf.stop(PARSE_TEL_URL_DATA);
        return;
      }
    }
    // catch (DsSipParserException e)
    // {
    // throw e;
    // }
    // CAFFEINE 2.0 DEVELOPMENT - commented out.  Will generate and throw DsSipParserException
    // below.
    //        catch (DsSipParserListenerException e)
    //        {
    //            throw e;
    //        }
    catch (Exception e) {
      throw generateDsSipParserException(element, e, TEL_URL_ID, TEL_URL, data, offset, count);
    }
  }

  /**
   * Parses a Name Address. Reports the results to the listener. This parser fires events for the
   * following element IDs:
   *
   * <blockquote>
   *
   * DISPLAY_NAME<br>
   * URI<br>
   *
   * </blockquote>
   *
   * @param element where to report the results.
   * @param data the Name Address.
   * @param offset the index that the Name Address starts at.
   * @param count the number of bytes in the Name Address.
   * @throws DsSipParserException if there is an exception while parsing.
   * @throws DsSipParserListenerException if the listener throws this exception.
   */
  public static void parseNameAddr(DsSipElementListener element, byte data[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    if (DsPerf.ON) DsPerf.start(PARSE_NAME_ADDR);

    try {
      if (element == null) {
        if (DsPerf.ON) DsPerf.stop(PARSE_NAME_ADDR);
        return; // done - lazy parse only
      }

      int end = offset + count;
      int index = lws(data, offset, end);
      int start = index;

      byte ch = data[index];
      if (ch != '<') // find the display name - exclude trailing WS
      {
        if (ch == '"') // quoted string
        {
          index++;
          index = readToCloseQuote(data, index, end);

          fireElement(element, NAME_ADDR_ID, DISPLAY_NAME, data, start, index - start);

          while (index < end && data[index++] != '<') // find the start of the url
          {}

          start = index;
        } else // one or more tokens - or just a url
        {
          ch = data[index++];
          while (index < end && ch != '<') // find the start of the url
          {
            if (ch == ':') // just a url, no display name or <>
            {
              // no display name but there are <>, so send and empty DISPLAY_NAME event
              // so that the user code knows to set <> true
              fireElement(element, NAME_ADDR_ID, DISPLAY_NAME, data, 0, 0);
              fireElement(
                  element, NAME_ADDR_ID, URI, data, offset, count); // handle erroneous WS? - jsm

              if (DsPerf.ON) DsPerf.stop(PARSE_NAME_ADDR);
              return;
            }

            ch = data[index++];
          }

          int endOfTokens = index - 2; // char before the '<' was found
          ch = data[endOfTokens];
          while (endOfTokens > start && ch <= ' ') {
            ch = data[--endOfTokens];
          }

          fireElement(element, NAME_ADDR_ID, DISPLAY_NAME, data, start, endOfTokens - start + 1);

          start = index;
        }
      } else {
        start = index + 1;

        // no display name but there are <>, so send and empty DISPLAY_NAME event
        // so that the user code knows to set <> true
        fireElement(element, NAME_ADDR_ID, DISPLAY_NAME, data, 0, 0);
      }

      // start now points to the first char of the url, just passed the opening '<'

      // find the char before the ending '>' and we are done

      index = end - 1;
      while (index > start && data[index--] != '>') {}

      // check for exception?

      fireElement(element, NAME_ADDR_ID, URI, data, start, index - start + 1);

      if (DsPerf.ON) DsPerf.stop(PARSE_NAME_ADDR);
    }
    // catch (DsSipParserException e)
    // {
    // throw e;
    // }
    catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(element, e, NAME_ADDR_ID, NAME_ADDR, data, offset, count);
    }
  }

  /**
   * Parses an entire SIP URL, including the scheme. Reports the results to the listener. This
   * parser fires events for the following element IDs:
   *
   * <blockquote>
   *
   * URI_SCHEME<br>
   * USERNAME<br>
   * PASSWORD<br>
   * HOST<br>
   * PORT<br>
   *
   * </blockquote>
   *
   * @param element where to report the results.
   * @param data the URL.
   * @param offset the index that the URL starts at.
   * @param count the number of bytes in the URL.
   * @throws DsSipParserException if there is an exception while parsing.
   * @throws DsSipParserListenerException if the listener throws this exception.
   */
  public static void parseSipUrl(DsSipElementListener element, byte data[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      parseSipUrl(null, (DsSipHeaderListener) element, element, data, offset, count);
    } catch (ClassCastException e) {
      // should never happen, since element is always a SIP URL and that is a DsSipHeaderListener
      parseSipUrl(null, null, element, data, offset, count);
    }
  }

  /**
   * Parses an entire SIP URL, including the scheme. Reports the results to the listener. This
   * parser fires events for the following element IDs:
   *
   * <blockquote>
   *
   * URI_SCHEME<br>
   * USERNAME<br>
   * PASSWORD<br>
   * HOST<br>
   * PORT<br>
   *
   * </blockquote>
   *
   * @param msgListener where to report the body if it exists as a header parameter, <code>null
   *     </code> OK
   * @param element where to report the results.
   * @param data the URL.
   * @param offset the index that the URL starts at.
   * @param count the number of bytes in the URL.
   * @throws DsSipParserException if there is an exception while parsing.
   * @throws DsSipParserListenerException if the listener throws this exception.
   */
  public static void parseSipUrl(
      DsSipMessageListener msgListener,
      DsSipElementListener element,
      byte data[],
      int offset,
      int count)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      parseSipUrl(msgListener, (DsSipHeaderListener) element, element, data, offset, count);
    } catch (ClassCastException e) {
      // should never happen, since element is always a SIP URL and that is a DsSipHeaderListener
      parseSipUrl(msgListener, null, element, data, offset, count);
    }
  }

  /**
   * Parses an entire SIP URL, including the scheme. Reports the results to the listener. This
   * parser fires events for the following element IDs:
   *
   * <blockquote>
   *
   * URI_SCHEME<br>
   * USERNAME<br>
   * PASSWORD<br>
   * HOST<br>
   * PORT<br>
   *
   * </blockquote>
   *
   * @param hdrListener where to report the results when headers are found in the URL, <code>null
   *     </code> OK.
   * @param element where to report the results.
   * @param data the URL.
   * @param offset the index that the URL starts at.
   * @param count the number of bytes in the URL.
   * @throws DsSipParserException if there is an exception while parsing.
   * @throws DsSipParserListenerException if the listener throws this exception.
   */
  public static void parseSipUrl(
      DsSipHeaderListener hdrListener,
      DsSipElementListener element,
      byte data[],
      int offset,
      int count)
      throws DsSipParserListenerException, DsSipParserException {
    parseSipUrl(null, hdrListener, element, data, offset, count);
  }

  /**
   * Parses an entire SIP URL, including the scheme. Reports the results to the listener. This
   * parser fires events for the following element IDs:
   *
   * <blockquote>
   *
   * URI_SCHEME<br>
   * USERNAME<br>
   * PASSWORD<br>
   * HOST<br>
   * PORT<br>
   *
   * </blockquote>
   *
   * @param msgListener where to report the body if it exists as a header parameter, <code>null
   *     </code> OK.
   * @param hdrListener where to report the results when headers are found in the URL, <code>null
   *     </code> OK.
   * @param element where to report the results.
   * @param data the URL.
   * @param offset the index that the URL starts at.
   * @param count the number of bytes in the URL.
   * @throws DsSipParserException if there is an exception while parsing.
   * @throws DsSipParserListenerException if the listener throws this exception.
   */
  public static void parseSipUrl(
      DsSipMessageListener msgListener,
      DsSipHeaderListener hdrListener,
      DsSipElementListener element,
      byte data[],
      int offset,
      int count)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      if (element == null) {
        return; // done - lazy parse only
      }

      boolean isSipUri = false;
      int schemeCount = 3;

      // make sure this is a SIP URL (case insensitive)
      if (data[offset] == 's' || data[offset] == 'S') {
        if (data[offset + 1] == 'i' || data[offset + 1] == 'I') {
          if (data[offset + 2] == 'p' || data[offset + 2] == 'P') {
            if (data[offset + 3] == ':') {
              isSipUri = true;
            } else if ((data[offset + 3] == 's' || data[offset + 3] == 'S')
                && data[offset + 4] == ':') {
              isSipUri = true;
              schemeCount = 4;
            }
          }
        }
      }

      if (!isSipUri) {
        throw generateDsSipParserException(
            element, "Not a sip URI", SIP_URL_ID, SIP_URL, data, offset, count);
      }

      fireElement(element, SIP_URL_ID, URI_SCHEME, data, offset, schemeCount);

      // broken up into scheme and data now
      parseSipUrlData(
          msgListener,
          hdrListener,
          element,
          data,
          offset + schemeCount + 1,
          count - schemeCount - 1);
    } catch (DsSipParserException e) {
      throw e;
    } catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(element, e, SIP_URL_ID, SIP_URL, data, offset, count);
    }
  }

  /**
   * Parses a SIP URL's data portion. Reports the results to the listener. This parser fires events
   * for the following element IDs:
   *
   * <blockquote>
   *
   * USERNAME<br>
   * PASSWORD<br>
   * HOST<br>
   * PORT<br>
   *
   * </blockquote>
   *
   * @param element where to report the results.
   * @param data the URL.
   * @param offset the index that the URL starts at.
   * @param count the number of bytes in the URL.
   * @throws DsSipParserException if there is an exception while parsing.
   * @throws DsSipParserListenerException if the listener throws this exception.
   */
  public static void parseSipUrlData(
      DsSipElementListener element, byte data[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    parseSipUrlData(null, element, data, offset, count);
  }

  /**
   * Parses a SIP URL's data portion. Reports the results to the listener. This parser fires events
   * for the following element IDs:
   *
   * <blockquote>
   *
   * USERNAME<br>
   * PASSWORD<br>
   * HOST<br>
   * PORT<br>
   *
   * </blockquote>
   *
   * @param hdrListener where to report the results when headers are found in the URL, <code>null
   *     </code> OK.
   * @param element where to report the results.
   * @param data the URL.
   * @param offset the index that the URL starts at.
   * @param count the number of bytes in the URL.
   * @throws DsSipParserException if there is an exception while parsing.
   * @throws DsSipParserListenerException if the listener throws this exception.
   */
  public static void parseSipUrlData(
      DsSipHeaderListener hdrListener,
      DsSipElementListener element,
      byte data[],
      int offset,
      int count)
      throws DsSipParserListenerException, DsSipParserException {
    parseSipUrlData(null, hdrListener, element, data, offset, count);
  }

  /**
   * Parses a SIP URL's data portion. Reports the results to the listener. This parser fires events
   * for the following element IDs:
   *
   * <blockquote>
   *
   * USERNAME<br>
   * PASSWORD<br>
   * HOST<br>
   * PORT<br>
   *
   * </blockquote>
   *
   * @param msgListener where to report the message body when the special "body" header is found in
   *     the URL, <code>null</code> OK.
   * @param hdrListener where to report the results when headers are found in the URL, <code>null
   *     </code> OK.
   * @param element where to report the results.
   * @param data the URL.
   * @param offset the index that the URL starts at.
   * @param count the number of bytes in the URL.
   * @throws DsSipParserException if there is an exception while parsing.
   * @throws DsSipParserListenerException if the listener throws this exception.
   */
  public static void parseSipUrlData(
      DsSipMessageListener msgListener,
      DsSipHeaderListener hdrListener,
      DsSipElementListener element,
      byte data[],
      int offset,
      int count)
      throws DsSipParserListenerException, DsSipParserException {
    if (DsPerf.ON) DsPerf.start(PARSE_SIP_URL_DATA);

    try {
      int end = offset + count;
      int index = offset;
      int start = index;

      // search for the '@', if it is there then there is a user part, else skip right to host
      int atIndex = -1;
      for (int i = index; i < end; i++) {
        if (data[i] == '@') {
          atIndex = i;
          break;
        }
      }

      if (atIndex != -1) // @ exists, thus user part exists
      {
        // search for : or @
        //    : means passwd
        //    @ means just username
        for (; index < atIndex; index++) {
          if (data[index] == ':') {
            // found the :
            //   before : is the user name
            //   after the : is the password
            fireElement(element, SIP_URL_ID, USERNAME, data, start, index - start);
            fireElement(element, SIP_URL_ID, PASSWORD, data, index + 1, atIndex - (index + 1));

            break;
          }
        }

        // make sure that index points to the first char after the @
        // since this is the same place that index will point if there is no '@'

        // if there is no at, it is already at offset + 4, the first char after the scheme :
        if (index == atIndex) // : not found - username only
        {
          fireElement(element, SIP_URL_ID, USERNAME, data, start, index - start);

          index++;
        } else {
          index = atIndex + 1; // move the index 1 char past '@'
        }
      }

      // find the host name
      // the host name ends at : ? ;
      //   : get port
      //   ; get params
      //   ? get headers
      //   WS - be nice and handle this
      // Added [] to handle IPv6

      byte ch;
      start = index;
      boolean done = false;
      boolean isIPv6 = false;
      while (!done && index < end) {
        switch (data[index++]) {
          case '[': // start IPv6 host
            isIPv6 = true;
            index = readToCloseBracket(data, index, end);

            // skip the [
            // will trim the ] below
            start++;

            break;
            // no white space allowed in URLs
          case ' ': // EOF
          case '\t': // EOF
          case '\r': // EOF
          case '\n': // EOF
            if (isIPv6) {
              // extra -1 to trim ]
              fireElement(element, SIP_URL_ID, HOST, data, start, index - start - 2);
            } else {
              fireElement(element, SIP_URL_ID, HOST, data, start, index - start - 1);
            }
            if (DsPerf.ON) DsPerf.stop(PARSE_SIP_URL_DATA);
            return;
          case ':': // port
            if (isIPv6) {
              // extra -1 to trim ]
              fireElement(element, SIP_URL_ID, HOST, data, start, index - start - 2);
            } else {
              fireElement(element, SIP_URL_ID, HOST, data, start, index - start - 1);
            }

            // find port 0-9
            start = index;
            ch = data[start];
            while (ch >= '0' && ch <= '9' && index < end) {
              ch = data[index++];
            }

            if (index == end && ch >= '0' && ch <= '9') // ends with port
            {
              fireElement(element, SIP_URL_ID, PORT, data, start, index - start);
              if (DsPerf.ON) DsPerf.stop(PARSE_SIP_URL_DATA);
              return;
            } else {
              fireElement(element, SIP_URL_ID, PORT, data, start, index - start - 1);

              if (ch <= ' ') {
                if (DsPerf.ON) DsPerf.stop(PARSE_SIP_URL_DATA);
                return;
              }
            }

            done = true;
            break;
          case ';': // parameters
          case '?': // headers
            if (isIPv6) {
              // extra -1 to trim ]
              fireElement(element, SIP_URL_ID, HOST, data, start, index - start - 2);
            } else {
              fireElement(element, SIP_URL_ID, HOST, data, start, index - start - 1);
            }

            done = true;
            break;
        }
      }

      if (index == end) // ends with host name or host name : port
      {
        if (isIPv6) {
          // extra -1 to trim ]
          fireElement(element, SIP_URL_ID, HOST, data, start, index - start - 1);
        } else {
          fireElement(element, SIP_URL_ID, HOST, data, start, index - start);
        }

        if (DsPerf.ON) DsPerf.stop(PARSE_SIP_URL_DATA);
        return;
      }

      int startName = index;
      int startValue = -1;

      int nameCount;
      int valueCount;

      ch = data[index - 1]; // prev char - either ; for parameters or ? for headers
      if (ch == ';') // start of parameters
      {
        // keep looking for params until EOF or ? for headers
        //   = end of name - look for value
        //      ? start headers
        //      ; next param

        done = false;
        while (!done && index < end) {
          switch (data[index++]) {
            case ' ': // EOF
            case '\t': // EOF
            case '\r': // EOF
            case '\n': // EOF
              nameCount = index - startName - 1;
              fireParameter(element, SIP_URL_ID, data, startName, nameCount, 0, 0);
              if (DsPerf.ON) DsPerf.stop(PARSE_SIP_URL_DATA);
              return;
            case '=': // end of name - start of value
              nameCount = index - startName - 1;

              boolean innerDone = false;
              startValue = index;
              while (!innerDone && index < end) {
                switch (data[index++]) {
                  case ' ': // EOF
                  case '\t': // EOF
                  case '\r': // EOF
                  case '\n': // EOF
                    valueCount = index - startValue - 1;
                    fireParameter(
                        element, SIP_URL_ID, data, startName, nameCount, startValue, valueCount);
                    if (DsPerf.ON) DsPerf.stop(PARSE_SIP_URL_DATA);
                    return;
                  case '?': // start headers
                    done = true;
                  case ';': // next param
                    innerDone = true;
                    valueCount = index - startValue - 1;
                    fireParameter(
                        element, SIP_URL_ID, data, startName, nameCount, startValue, valueCount);
                    startName = index;
                    break;
                }
              }

              if (index == end) {
                valueCount = index - startValue;
                fireParameter(
                    element, SIP_URL_ID, data, startName, nameCount, startValue, valueCount);
                if (DsPerf.ON) DsPerf.stop(PARSE_SIP_URL_DATA);
                return;
              }
              break;
            case '?': // end of name - start of headers - no value
              done = true;
            case ';': // end of name - start of new parameter - no value
              // no need to check for known types since they all have values
              nameCount = index - startName - 1;
              fireParameter(element, SIP_URL_ID, data, startName, nameCount, 0, 0);
              startName = index;
              break;
          }
        }

        if (index == end) {
          nameCount = index - startName;
          fireParameter(element, SIP_URL_ID, data, startName, nameCount, 0, 0);
          if (DsPerf.ON) DsPerf.stop(PARSE_SIP_URL_DATA);
          return;
        }
      }

      // allows optional header values - jsm

      // need a way to treat headers not as parameters, this is wrong - jsm
      ch = data[index - 1]; // prev char - needs to be ? if we get here
      if (ch == '?') // start of headers
      {
        if (hdrListener == null) {
          // Since headers are only allowed in request URIs, and do not make sense elsewhere,
          // throw an exception.  Without a hdrListener, there is no one to tell about these events,
          // at this point, this URI is invalid
          throw generateDsSipParserException(
              element,
              "Illegal: Unexpected header found in SIP URL.",
              SIP_URL_ID,
              SIP_URL,
              data,
              offset,
              count);
        }

        startName = index;

        // keep looking for params until EOF or & for next header
        //   = end of name - look for next
        //      & next header

        done = false;
        while (!done && index < end) {
          switch (data[index++]) {
            case ' ': // EOF
            case '\t': // EOF
            case '\r': // EOF
            case '\n': // EOF
              nameCount = index - startName - 1;

              if (checkForBody(data, startName, nameCount)) {
                // no body, ignore
              } else {
                // empty header, but still need to parse
                parseHeaderFromUrl(hdrListener, data, startName, nameCount, 0, 0);
              }

              if (DsPerf.ON) DsPerf.stop(PARSE_SIP_URL_DATA);
              return;
            case '=': // end of name - start of value
              nameCount = index - startName - 1;

              boolean innerDone = false;
              startValue = index;
              while (!innerDone && index < end) {
                switch (data[index++]) {
                  case ' ': // EOF
                  case '\t': // EOF
                  case '\r': // EOF
                  case '\n': // EOF
                    valueCount = index - startValue - 1;

                    if (checkForBody(data, startName, nameCount)) {
                      if (msgListener != null) {
                        msgListener.bodyFoundInRequestURI(data, startValue, valueCount);
                      }
                    } else {
                      // end of headers, parse this last one
                      parseHeaderFromUrl(
                          hdrListener, data, startName, nameCount, startValue, valueCount);
                    }

                    if (DsPerf.ON) DsPerf.stop(PARSE_SIP_URL_DATA);
                    return;
                  case '&': // next param
                    valueCount = index - startValue - 1;

                    if (checkForBody(data, startName, nameCount)) {
                      if (msgListener != null) {
                        msgListener.bodyFoundInRequestURI(data, startValue, valueCount);
                      }
                    } else {
                      // end of header, parse it
                      parseHeaderFromUrl(
                          hdrListener, data, startName, nameCount, startValue, valueCount);
                    }

                    innerDone = true;
                    startName = index;
                    break;
                }

                if (index == end) {
                  valueCount = index - startValue;

                  if (checkForBody(data, startName, nameCount)) {
                    if (msgListener != null) {
                      msgListener.bodyFoundInRequestURI(data, startValue, valueCount);
                    }
                  } else {
                    // last char was part of a header value, parse this last header
                    parseHeaderFromUrl(
                        hdrListener, data, startName, nameCount, startValue, valueCount);
                  }

                  if (DsPerf.ON) DsPerf.stop(PARSE_SIP_URL_DATA);
                  return;
                }
              }
              break;
            case '&': // end of name - start of new header - no value
              nameCount = index - startName - 1;

              if (checkForBody(data, startName, nameCount)) {
                // no body, ignore
              } else {
                // end of header with no value, still need to parse it
                parseHeaderFromUrl(hdrListener, data, startName, nameCount, 0, 0);
              }

              startName = index;
              break;
          }
        }

        if (index == end) {
          nameCount = index - startName - 1;

          // last char was part of a header name, parse this last empty header

          if (checkForBody(data, startName, nameCount)) {
            // no body, ignore
          } else {
            parseHeaderFromUrl(hdrListener, data, startName, nameCount, 0, 0);
            // fireParameter(element, SIP_URL_ID, data, startName, nameCount, 0, 0);
          }

          if (DsPerf.ON) DsPerf.stop(PARSE_SIP_URL_DATA);
          return;
        }
      }
    } catch (DsSipParserException e) {
      throw e;
    } catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(element, e, SIP_URL_ID, SIP_URL, data, offset, count);
    }
  }

  private static boolean checkForBody(byte[] data, int startName, int nameCount) {
    if (nameCount != 4) {
      return false;
    }

    if ((data[startName] == 'b' || data[startName] == 'B')
        && (data[startName + 1] == 'o' || data[startName + 1] == 'O')
        && (data[startName + 2] == 'd' || data[startName + 2] == 'D')
        && (data[startName + 3] == 'y' || data[startName + 3] == 'Y')) {
      return true;
    }

    return false;
  }

  protected static void parseHeaderFromUrl(
      DsSipHeaderListener hdrListener,
      byte[] data,
      int startName,
      int nameCount,
      int startValue,
      int valueCount)
      throws DsSipParserListenerException, DsSipParserException {

    try (ByteBuffer buf = ByteBuffer.newInstance(128)) {

      buf.write(data, startName, nameCount);
      buf.write(':');

      int start = startValue;
      int count = 0;
      byte escapedChar;
      for (int i = 0; i < valueCount; i++) {
        if (data[startValue + i] != '%') {
          count++;
        } else {
          // found an escaped char

          // first copy, the data so far
          buf.write(data, start, count);

          escapedChar =
              (byte) ((hexVal(data[startValue + i + 1]) * 16) + hexVal(data[startValue + i + 2]));
          buf.write(escapedChar);

          // skip over this escaped char
          i += 2; // i gets incremented again at the top of the loop
          start = startValue + i + 1;
          count = 0;
        }
      }

      buf.write(data, start, count);

      byte[] hdrBytes = buf.toByteArray();
      parseHeader(hdrListener, hdrBytes, 0, hdrBytes.length);
    } catch (IOException ie) {

    }
  }

  protected static byte hexVal(byte b) {
    if (b >= '0' && b <= '9') {
      return (byte) (b - '0');
    }

    if (b >= 'A' && b <= 'F') {
      return (byte) ((b - 'A') + 10);
    }

    if (b >= 'a' && b <= 'f') {
      return (byte) ((b - 'a') + 10);
    }

    // should throw an exception - jsm
    return -1;
  }

  /**
   * A fast wat to get an integer value from a string.
   *
   * @param val the string to parse.
   * @return the integer value represented by val.
   * @throws NumberFormatException if the number is malformed.
   */
  public static int parseInt(DsByteString val) {
    return parseInt(val.data(), val.offset(), val.length());
  }

  /**
   * A fast wat to get an long value from a string.
   *
   * @param val the string to parse.
   * @return the long value represented by val.
   * @throws NumberFormatException if the number is malformed.
   */
  public static long parseLong(DsByteString val) {
    return parseLong(val.data(), val.offset(), val.length());
  }

  /**
   * A fast wat to get an float value from a string.
   *
   * @param val the string to parse.
   * @return the float value represented by val.
   * @throws NumberFormatException if the number is malformed.
   */
  public static float parseFloat(DsByteString val) {
    return parseFloat(val.data(), val.offset(), val.length());
  }

  /**
   * A fast wat to get an float value from a string.
   *
   * @param data the byte array to parse.
   * @param offset the start of the number in the byte array.
   * @param count the number of bytes in the number to parse.
   * @return the float value represented by val.
   * @throws NumberFormatException if the number is malformed.
   */
  public static float parseFloat(byte data[], int offset, int count) {
    if (DsPerf.ON) DsPerf.start(PARSE_FLOAT);

    if (count == 1) {
      if (data[offset] == '.') {
        if (DsPerf.ON) DsPerf.stop(PARSE_FLOAT);
        return 0;
      }

      if (data[offset] <= '9' && data[offset] >= '0') {
        if (DsPerf.ON) DsPerf.stop(PARSE_FLOAT);
        return data[offset] - '0';
      } else // exception
      {
        throw new NumberFormatException(
            "Tried to parse non-float value: '" + (char) data[offset] + "'");
      }
    }

    int end = offset + count - 1;
    boolean hasDecimal = false;
    for (int i = offset; i <= end; i++) {
      if (data[i] == '.') {
        hasDecimal = true;
        break;
      }
    }

    if (hasDecimal) {
      float val = 0;
      int index = end;

      // keep working until the end or the decimal point is found
      while (index >= offset && (data[index] <= '9' && data[index] >= '0')) {
        val += (data[index] - '0');
        val /= 10.0;
        index--;
      }

      if (data[index] != '.') {
        throw new NumberFormatException("Expecting '.' and got: '" + (char) data[index] + "'");
      }

      index--;

      long factor = 1;

      // keep working until the end or all digits are found
      while (index >= offset && (data[index] <= '9' && data[index] >= '0')) {
        val += (factor * (data[index] - '0'));
        factor *= 10;
        index--;
      }

      if (index == offset && data[index] == '-') {
        val *= -1;
      }

      if (DsPerf.ON) DsPerf.stop(PARSE_FLOAT);
      return val;
    } else // no decimal point
    {
      if (DsPerf.ON) DsPerf.stop(PARSE_FLOAT);
      return parseLong(data, offset, count);
    }
  }

  /**
   * A fast wat to get an long value from a string.
   *
   * @param data the byte array to parse.
   * @param offset the start of the number in the byte array.
   * @param count the number of bytes in the number to parse.
   * @return the long value represented by val.
   * @throws NumberFormatException if the number is malformed.
   */
  public static long parseLong(byte data[], int offset, int count) {
    if (DsPerf.ON) DsPerf.start(PARSE_LONG);

    byte ch;
    long val = 0;
    long factor = 1;
    int end = offset + count - 1;

    for (int i = end; i >= offset; i--) {
      ch = data[i];
      if (ch >= '0' && ch <= '9') {
        val += (factor * (ch - '0'));
        factor *= 10;
      } else if ((i == offset) && ((ch == '-') || (ch == '+'))) {
        if (ch == '-') {
          if (DsPerf.ON) DsPerf.stop(PARSE_LONG);
          return (val * -1);
        }

        if (DsPerf.ON) DsPerf.stop(PARSE_LONG);
        return val;
      } else {
        throw new NumberFormatException("Tried to parse non-integer value: '" + (char) ch + "'");
      }
    }

    if (DsPerf.ON) DsPerf.stop(PARSE_LONG);
    return val;
  }

  /**
   * A fast wat to get an int value from a string.
   *
   * @param data the byte array to parse.
   * @param offset the start of the number in the byte array.
   * @param count the number of bytes in the number to parse.
   * @return the int value represented by val.
   * @throws NumberFormatException if the number is malformed.
   */
  public static int parseInt(byte data[], int offset, int count) {
    if (DsPerf.ON) DsPerf.start(PARSE_INT);

    byte ch;
    int val = 0;
    int factor = 1;
    int end = offset + count - 1;

    // CAFFEINE 2.0 DEVELOPMENT
    //   Bugid: CSCef82808 Via header without host won't be detected when doing deep parsing
    if (end <= 0 && end > data.length) return val; // to prevent ArrayIndexOutOfBoundsException

    for (int i = end; i >= offset; i--) {
      ch = data[i];
      if (ch >= '0' && ch <= '9') {
        val += (factor * (ch - '0'));
        factor *= 10;
      } else if ((i == offset) && ((ch == '-') || (ch == '+'))) {
        if (ch == '-') {
          if (DsPerf.ON) DsPerf.stop(PARSE_INT);
          return (val * -1);
        }

        if (DsPerf.ON) DsPerf.stop(PARSE_INT);
        return val;
      } else {
        throw new NumberFormatException("Tried to parse non-integer value: '" + (char) ch + "'");
      }
    }

    if (DsPerf.ON) DsPerf.stop(PARSE_INT);
    return val;
  }

  protected static int readToCloseQuote(byte[] data, int index, int end) {
    byte ch;
    while (index < end) {
      ch = data[index++];
      if (ch == '"') // found
      {
        return index; // char past the '"'
      }

      if (ch == '\\') // escape - ignore next char
      {
        index++;
      }
    }

    // should never get here
    return end;
  }

  protected static int readToCloseBracket(byte[] data, int index, int end) {
    byte ch;
    while (index < end) {
      ch = data[index++];
      if (ch == ']') // found
      {
        return index; // char past the ']'
      }
    }

    // should never get here
    return end;
  }

  /**
   * Generate a DsSipParserException and add the header to header list.
   *
   * @param element the listener to pass the elementFound() event to
   * @param msg the message to include in the exception
   * @param contextId the context (header, URI, etc) that was being parsed
   * @param elementId the element that was being parsed
   * @param data the data being parsed
   * @param offset the start of the data in the array
   * @param count the number of chars in the array that belong to this value
   * @return the DsSipParserException generated by this method
   */
  public static DsSipParserException generateDsSipParserException(
      DsSipElementListener element,
      String msg,
      int contextId,
      int elementId,
      byte[] data,
      int offset,
      int count) {
    boolean validData = isDataValid(data, offset, count);

    DsSipParserException newE;

    if (validData) {
      newE = new DsSipParserException(msg, contextId, new DsByteString(data, offset, count));
    } else // data is null or offset/count goes out of bounds, just use ""
    {
      newE = new DsSipParserException(msg, contextId, DsByteString.BS_EMPTY_STRING);
    }

    notifyElementListener(element, contextId, elementId, data, offset, count, validData);

    return newE;
  }

  /**
   * Generate a DsSipParserException and add the header to header list.
   *
   * @param element the listener to pass the elementFound() event to
   * @param e the exception that caused this exception to be generated
   * @param contextId the context (header, URI, etc) that was being parsed
   * @param elementId the element that was being parsed
   * @param data the data being parsed
   * @param offset the start of the data in the array
   * @param count the number of chars in the array that belong to this value
   * @return the DsSipParserException generated by this method
   */
  public static DsSipParserException generateDsSipParserException(
      DsSipElementListener element,
      Exception e,
      int contextId,
      int elementId,
      byte[] data,
      int offset,
      int count) {
    boolean validData = isDataValid(data, offset, count);

    DsSipParserException newE;

    if (validData) {
      newE = new DsSipParserException(e, contextId, new DsByteString(data, offset, count));
    } else // data is null or offset/count goes out of bounds, just use ""
    {
      newE = new DsSipParserException(e, contextId, DsByteString.BS_EMPTY_STRING);
    }

    notifyElementListener(element, contextId, elementId, data, offset, count, validData);

    return newE;
  }

  /**
   * Generate a DsSipParserException and add the header to header list.
   *
   * @param headerListener the listener to pass the headerFound() event to
   * @param msg the message to include in the exception
   * @param contextId the context (header, URI, etc) that was being parsed
   * @param data the data being parsed
   * @param offset the start of the data in the array
   * @param count the number of chars in the array that belong to this value
   * @return the DsSipParserException generated by this method
   */
  public static DsSipParserException generateDsSipParserException(
      DsSipHeaderListener headerListener,
      String msg,
      int contextId,
      byte[] data,
      int offset,
      int count) {
    boolean validData = isDataValid(data, offset, count);

    DsSipParserException newE;

    if (validData) {
      newE = new DsSipParserException(msg, contextId, new DsByteString(data, offset, count));
    } else // data is null or offset/count goes out of bounds, just use ""
    {
      newE = new DsSipParserException(msg, contextId, DsByteString.BS_EMPTY_STRING);
    }

    notifyHeaderListener(headerListener, contextId, data, offset, count, validData);

    return newE;
  }

  /**
   * Generate a DsSipParserException and add the header to header list.
   *
   * @param headerListener the listener to pass the headerFound() event to
   * @param e the exception that caused this exception to be generated
   * @param contextId the context (header, URI, etc) that was being parsed
   * @param data the data being parsed
   * @param offset the start of the data in the array
   * @param count the number of chars in the array that belong to this value
   * @return the DsSipParserException generated by this method
   */
  public static DsSipParserException generateDsSipParserException(
      DsSipHeaderListener headerListener,
      Exception e,
      int contextId,
      byte[] data,
      int offset,
      int count) {
    boolean validData = isDataValid(data, offset, count);

    DsSipParserException newE;

    if (validData) {
      newE = new DsSipParserException(e, contextId, new DsByteString(data, offset, count));
    } else // data is null or offset/count goes out of bounds, just use ""
    {
      newE = new DsSipParserException(e, contextId, DsByteString.BS_EMPTY_STRING);
    }

    notifyHeaderListener(headerListener, contextId, data, offset, count, validData);

    return newE;
  }

  /**
   * Generate a DsSipParserException and add the header to header list.
   *
   * @param messageListener the listener to pass the headerFound() event to
   * @param e the exception that caused this exception to be generated
   * @param data the data being parsed
   * @param offset the start of the data in the array
   * @param count the number of chars in the array that belong to this value
   * @return the DsSipParserException generated by this method
   */
  // CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to DsMimeMessageListener
  public static DsSipParserException generateDsSipParserException(
      DsMimeMessageListener messageListener, Exception e, byte[] data, int offset, int count) {
    boolean validData = isDataValid(data, offset, count);

    DsSipParserException newE;

    if (validData) {
      newE = new DsSipParserException(e, ENTIRE_MSG_ID, new DsByteString(data, offset, count));
    } else // data is null or offset/count goes out of bounds, just use ""
    {
      newE = new DsSipParserException(e, ENTIRE_MSG_ID, DsByteString.BS_EMPTY_STRING);
    }

    notifyMessageListener(messageListener, data, offset, count, validData);

    return newE;
  }

  protected static boolean isDataValid(byte[] data, int offset, int count) {
    return (data != null
        && offset >= 0
        && offset < data.length
        && count >= 0
        && (offset + count) <= data.length);
  }

  protected static void notifyMessageListener(
      DsMimeMessageListener messageListener,
      byte[] data,
      int offset,
      int count,
      boolean validData) {
    try {
      if (messageListener != null) {
        if (validData) {
          messageListener.messageFound(data, offset, count, false);
        } else if (data != null) {
          messageListener.messageFound(data, 0, data.length, false);
        } else {
          messageListener.messageFound(new byte[0], 0, 0, false);
        }
      }
    } catch (Exception ex) {
      // ignore
    }
  }

  protected static void notifyHeaderListener(
      DsSipHeaderListener headerListener,
      int contextId,
      byte[] data,
      int offset,
      int count,
      boolean validData) {
    try {
      if (headerListener != null) {
        if (validData) {
          headerListener.headerFound(contextId, data, offset, count, false);
        } else if (data != null) {
          headerListener.headerFound(contextId, data, 0, data.length, false);
        } else {
          headerListener.headerFound(contextId, new byte[0], 0, 0, false);
        }
      }
    } catch (Exception ex) {
      // ignore
    }
  }

  protected static void notifyElementListener(
      DsSipElementListener element,
      int contextId,
      int elementId,
      byte[] data,
      int offset,
      int count,
      boolean validData) {
    try {
      if (element != null) {
        if (validData) {
          element.elementFound(contextId, elementId, data, offset, count, false);
        } else if (data != null) {
          element.elementFound(contextId, elementId, data, 0, data.length, false);
        } else {
          element.elementFound(contextId, elementId, new byte[0], 0, 0, false);
        }
      }
    } catch (Exception ex) {
      // ignore
    }
  }
}
