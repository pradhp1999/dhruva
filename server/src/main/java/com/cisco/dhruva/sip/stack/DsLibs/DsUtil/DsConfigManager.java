// Copyright (c) 2005-2012, 2015 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipMFRClientTransactionInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessageValidator;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.Logger;
import java.lang.reflect.Field;
import org.slf4j.event.Level;

/**
 * The helper class which serves as a hook to configure, monitor and retrieve information from the
 * UA stack, needed through the SNMP, CLI or user code. This class provides for following
 * functionality in the stack scope:<br>
 *
 * <p>The configuration of the stack may also be controlled via the system properties documented in
 * this class.
 */
public final class DsConfigManager implements DsSipConstants {
  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.closeConnectionOnTimeout: &nbsp; &nbsp; Set to <code>true
   * </code> if you want to close a TCP/TLS connection after a SIP transaction timeout. Setting this
   * to <code>false</code> will keep the stack from closing connections prematurely when the timeout
   * is caused by a misbehaving SIP element rather than a true network issue, however it comes at
   * the cost of delaying the detection of true network issues.
   */
  public static final String PROP_CLOSE_CONNECTION_ON_TIMEOUT =
      "com.dynamicsoft.DsLibs.DsSipLlApi.closeConnectionOnTimeout";
  /**
   * <code>true</code>, as this is the way that the stack was behaving at the time this option was
   * added.
   */
  public static final boolean PROP_CLOSE_CONNECTION_ON_TIMEOUT_DEFAULT = true;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.maxUdpPacketSize: &nbsp; &nbsp; The maximum size UDP packet
   * that can be received by the stack. This will cause each UDP listener thread to allocated this
   * much memory, and hold it for the life of the thread.
   */
  public static final String PROP_MAX_UDP_PACKET_SIZE =
      "com.dynamicsoft.DsLibs.DsSipLlApi.maxUdpPacketSize";
  /** 64*1024 (the absolute maximum for any UDP packet). */
  public static final int PROP_MAX_UDP_PACKET_SIZE_DEFAULT = 64 * 1024;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.maxTcpMsgSize: &nbsp; &nbsp; The maximum size message that
   * can be received by the stack over TCP/TLS. This will cause each TCP/TLS listener thread to
   * potentially allocated this much memory, and hold it for the life of the thread.
   */
  public static final String PROP_MAX_TCP_MSG_SIZE =
      "com.dynamicsoft.DsLibs.DsSipLlApi.maxTcpMsgSize";
  /** 256*1024. */
  public static final int PROP_MAX_TCP_MSG_SIZE_DEFAULT = 256 * 1024;
  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.udpSendBufferSize: &nbsp; &nbsp; Default UDP send buffer
   * size.
   */
  public static final String PROP_UDP_SEND_BUFFER =
      "com.dynamicsoft.DsLibs.DsSipLlApi.udpSendBufferSize";
  /** -1 (use JDK default). */
  public static final int PROP_UDP_SEND_BUFFER_DEFAULT = -1;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.udpReceiveBufferSize:&nbsp; &nbsp; Default UDP receive buffer
   * size.
   */
  public static final String PROP_UDP_REC_BUFFER =
      "com.dynamicsoft.DsLibs.DsSipLlApi.udpReceiveBufferSize";
  /** -1 (use JDK default). */
  public static final int PROP_UDP_REC_BUFFER_DEFAULT = -1;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.tcpSendBufferSize: &nbsp; &nbsp; Default TCP send buffer
   * size.
   */
  public static final String PROP_TCP_SEND_BUFFER =
      "com.dynamicsoft.DsLibs.DsSipLlApi.tcpSendBufferSize";
  /** 16384. */
  public static final int PROP_TCP_SEND_BUFFER_DEFAULT = 16384;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.tcpReceiveBufferSize: &nbsp; &nbsp; Default TCP receive
   * buffer size.
   */
  public static final String PROP_TCP_REC_BUFFER =
      "com.dynamicsoft.DsLibs.DsSipLlApi.tcpReceiveBufferSize";
  /** 24576. */
  public static final int PROP_TCP_REC_BUFFER_DEFAULT = 24576;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.TCP_NODELAY: &nbsp; &nbsp; Use TCP no delay socket option.
   */
  public static final String PROP_TCP_NODELAY = "com.dynamicsoft.DsLibs.DsSipLlApi.TCP_NODELAY";
  /** true. */
  public static final boolean PROP_TCP_NODELAY_DEFAULT = true;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.DGRAM_CONNECTION_STRATEGY: &nbsp; &nbsp; Datagram Socket
   * connection strategy.
   */
  public static final String PROP_DGRAM_CONNECTION_STRATEGY =
      "com.dynamicsoft.DsLibs.DsSipLlApi.DGRAM_CONNECTION_STRATEGY";
  /** Use one datagram socket per endpoint(0). */
  public static final int PROP_DGRAM_CONNECTION_STRATEGY_DEFAULT = DsNetwork.DGRAM_PER_ENDPOINT;

  /** com.dynamicsoft.DsLibs.DsSipLlApi.DGRAM_TYPE: &nbsp; &nbsp; Datagram Socket type for UDP. */
  public static final String PROP_DGRAM_TYPE = "com.dynamicsoft.DsLibs.DsSipLlApi.DGRAM_TYPE";
  /** Use ICMP for Solaris and JDK otherwise for datagram socket type default. */
  public static final int PROP_DGRAM_TYPE_DEFAULT =
      System.getProperty("os.name").equalsIgnoreCase("SunOS")
          ? DsNetwork.DGRAM_ICMP
          : DsNetwork.DGRAM_DEFAULT;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.NETWORK_COMP_TYPE: &nbsp; &nbsp; Network Compression Type.
   */
  public static final String PROP_NETWORK_COMP_TYPE =
      "com.dynamicsoft.DsLibs.DsSipLlApi.NETWORK_COMP_TYPE";
  /** Use no compression by default */
  public static final int PROP_NETWORK_COMP_TYPE_DEFAULT = DsNetwork.NET_COMP_NONE;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.PROP_OUT_CONNECTION: &nbsp; &nbsp; Determines if new outgoing
   * connections can be opened through networks by default.
   */
  public static final String PROP_OUT_CONNECTION =
      "com.dynamicsoft.DsLibs.DsSipLlApi.OUT_CONNECTION";
  /** Allow outbound connection to be created on networks by default */
  public static final boolean PROP_OUT_CONNECTION_DEFAULT = true;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.PROP_ADD_CLIENT_SIDE_RPORT: &nbsp; &nbsp; Determines if rport
   * will be added to via sent in requests sent on networks by default.
   */
  public static final String PROP_ADD_CLIENT_SIDE_RPORT =
      "com.dynamicsoft.DsLibs.DsSipLlApi.PROP_ADD_CLIENT_SIDE_RPORT";
  /** Allow rport to be added to via headers on networks by default */
  public static final boolean PROP_ADD_CLIENT_SIDE_RPORT_DEFAULT = true;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.BranchID: &nbsp; &nbsp; Use the top Via branch parameter as
   * the transaction key.
   */
  public static final String PROP_KEY = "com.dynamicsoft.DsLibs.DsSipLlApi.BranchID";
  /** true. */
  public static final boolean PROP_KEY_DEFAULT = true;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.ShortKeys: &nbsp; &nbsp; When <code>true</code>, use a
   * shortened string as the branch parameter of the top Via.
   */
  public static final String PROP_SHORT_KEYS = "com.dynamicsoft.DsLibs.DsSipLlApi.ShortKeys";
  /** false. */
  public static final boolean PROP_SHORT_KEYS_DEFAULT = false;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.Cleanup: &nbsp; &nbsp; Use aggressive memory cleanup in the
   * transaction layer. When set to true the transactions can not be relied upon to hold onto the
   * orginal message and subsequent messages. These are discarded as soon as the transaction no
   * longer needs them. byte[] are held onto for retransmissions, rather than the entire message
   * object hierarchy.
   */
  public static final String PROP_CLEANUP = "com.dynamicsoft.DsLibs.DsSipLlApi.Cleanup";
  /** false. */
  public static final boolean PROP_CLEANUP_DEFAULT = false;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.DoNotDecrementMaxForwards: &nbsp; &nbsp; Do not decrement the
   * Max-Forwards in the stack.
   */
  public static final String PROP_MAXF =
      "com.dynamicsoft.DsLibs.DsSipLlApi.DoNotDecrementMaxForwards";
  /** false. */
  public static final boolean PROP_MAXF_DEFAULT = false;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.IncomingSocketTimeout: &nbsp; &nbsp; Set the timeout, in
   * seconds, for TCP, TLS & NAT UDP incoming socket connections.
   */
  public static final String PROP_INCOMING_SOCKET_TIMEOUT =
      "com.dynamicsoft.DsLibs.DsSipLlApi.IncomingSocketTimeout";
  /** 14400. */
  public static final int PROP_INCOMING_SOCKET_TIMEOUT_DEFAULT = 60 * 60 * 4;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.OutgoingSocketTimeout: &nbsp; &nbsp; Set the timeout, in
   * seconds, for TCP, TLS & NAT UDP outgoing socket connections.
   */
  public static final String PROP_OUTGOING_SOCKET_TIMEOUT =
      "com.dynamicsoft.DsLibs.DsSipLlApi.OutgoingSocketTimeout";
  /** 14400 */
  public static final int PROP_OUTGOING_SOCKET_TIMEOUT_DEFAULT = 60 * 60 * 4;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.SO_TIMEOUT: &nbsp; &nbsp; Set the SO_TIMEOUT, in
   * milliseconds, for TCP and TLS socket options. Using 0 means infinite timeout.
   */
  public static final String PROP_SO_TIMEOUT = "com.dynamicsoft.DsLibs.DsSipLlApi.SO_TIMEOUT";
  /** 0 - infinite timeout. */
  public static final int PROP_SO_TIMEOUT_DEFAULT = 0;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.TCP_CONN_TIMEOUT: &nbsp; &nbsp; Set the TCP_CONN_TIMEOUT, in
   * milliseconds, for TCP and TLS socket options. Using 0 means infinite timeout.
   */
  public static final String PROP_TCP_CONN_TIMEOUT =
      "com.dynamicsoft.DsLibs.DsSipLlApi.TCP_CONN_TIMEOUT";
  /** 0 - infinite timeout. */
  public static final int PROP_TCP_CONN_TIMEOUT_DEFAULT = 1000;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.rejectCMR3: &nbsp; &nbsp; Reject the CMR3 call , this is a
   * hack and must be removed.
   */
  public static final String PROP_REJECT_CMR3_MRS = "com.dynamicsoft.DsLibs.DsSipLlApi.rejectCMR3";
  /** 0. */
  public static final boolean PROP_REJECT_CMR3_MRS_DEFAULT = true;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.MTU: &nbsp; &nbsp; Set the known MTU size for the network.
   */
  public static final String PROP_MTU = "com.dynamicsoft.DsLibs.DsSipLlApi.MTU";
  /** 1500 (changed from Integer.MAX_VALUE, so this in on by default rather than off). */
  public static final int PROP_MTU_DEFAULT = 1500;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.simpleResolver: &nbsp; &nbsp; Do not use SRV or NAPTR to find
   * network elements.
   */
  public static final String PROP_SIMPLE_RESOLVER =
      "com.dynamicsoft.DsLibs.DsSipLlApi.simpleResolver";
  /** true. */
  public static final boolean PROP_SIMPLE_RESOLVER_DEFAULT = true;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.deterministicResolver: &nbsp; &nbsp; To use the Deterministic
   * SRV resolver or not. Only effective when not using the Simple Resolver.
   */
  public static final String PROP_DETERMINISTIC_RESOLVER =
      "com.dynamicsoft.DsLibs.DsSipLlApi.deterministicResolver";
  /** true. */
  public static final boolean PROP_DETERMINISTIC_RESOLVER_DEFAULT = true;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.useLocalDnsOnly: &nbsp; &nbsp; Enable/Disable the use of
   * local DNS SRV configuration file.
   */
  public static final String PROP_USE_LOCAL_DNS_ONLY =
      "com.dynamicsoft.DsLibs.DsSipLlApi.useLocalDnsOnly";
  /** false. */
  public static final boolean PROP_USE_LOCAL_DNS_ONLY_DEFAULT = false;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.convertDestinationRouteToIP: &nbsp; &nbsp; Enable/Disable
   * converting destination Route to IP.
   */
  public static final String PROP_DEFAULT_CONVERT_DESTINATION_ROUTE_TO_IP_ENABLED =
      "com.dynamicsoft.DsLibs.DsSipLlApi.convertDestinationRouteToIP";
  /** false. */
  public static final boolean PROP_DEFAULT_CONVERT_DESTINATION_ROUTE_TO_IP_ENABLED_DEFAULT = false;

  public static final String PROP_REMOVE_OWN_ROUTE_HEADER =
      "com.dynamicsoft.DsLibs.DsSipLlApi.removeOwnRouteHeader";

  public static final boolean PROP_REMOVE_OWN_ROUTE_HEADER_DEFAULT = false;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.useLocalDnsOnly: &nbsp; &nbsp; Enable/Disable the use of Dns
   * lookup Lync Federation.
   */
  public static final String PROP_DNS_LOOKUP_TLS_LYNC_FEDERATION_ENABLED =
      "com.dynamicsoft.DsLibs.DsSipLlApi.dnsLookupTLSLyncFederationEnabled";
  /** false. */
  public static final boolean PROP_DNS_LOOKUP_TLS_LYNC_FEDERATION_ENABLED_DEFAULT = false;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.enablePeerCertInfoHeader: &nbsp; &nbsp; Enable/Disable
   * Insertion of TLS cert info to the Initial Invite header.
   */
  public static final String PROP_PEER_CERT_INFO_HEADER_ENABLED =
      "com.dynamicsoft.DsLibs.DsSipLlApi.enablePeerCertInfoHeader";
  /** false. */
  public static final boolean PROP_PEER_CERT_INFO_HEADER_ENABLED_DEFAULT = false;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.failOnLRFix: &nbsp; &nbsp; Throw an exception when request
   * URI is recognized but route header is empty
   */
  public static final String PROP_FAIL_ON_LRFIX = "com.dynamicsoft.DsLibs.DsSipLlApi.failOnLRFix";
  /** true. */
  public static final boolean PROP_FAIL_ON_LRFIX_DEFAULT = true;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.useNAPTR: &nbsp; &nbsp; Use NAPTR records to find network
   * elements.
   */
  public static final String PROP_NAPTR = "com.dynamicsoft.DsLibs.DsSipLlApi.useNAPTR";
  /** false. */
  public static final boolean PROP_NAPTR_DEFAULT = false;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.localDomain: &nbsp; &nbsp; The local domain to append to
   * unresolvable host names. Use <code>null</code> to not try with a local domain added to host
   * names, and "" (the default) to use the domain name from the local host.
   */
  public static final String PROP_LOCAL_DOMAIN = "com.dynamicsoft.DsLibs.DsSipLlApi.localDomain";
  /** "". */
  public static final String PROP_LOCAL_DOMAIN_DEFAULT = "";

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.SmallMaps: &nbsp; &nbsp; Initialize internal tables for small
   * footprint applications.
   */
  public static final String PROP_SMALL_MAPS = "com.dynamicsoft.DsLibs.DsSipLlApi.SmallMaps";
  /** false. */
  public static final boolean PROP_SMALL_MAPS_DEFAULT = false;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.x200Terminated: &nbsp; &nbsp; Do not terminate INVITE
   * transactions on 200 response.
   */
  public static final String PROP_X200TERM = "com.dynamicsoft.DsLibs.DsSipLlApi.x200Terminated";
  /** false. */
  public static final boolean PROP_X200TERM_DEFAULT = false;

  /**
   * Trusted SAN entry CP This is a stop gap arrangement.Later this should be configurable as part
   * of each network defined
   */
  public static final String TRUSTED_DOMAINS = "com.dynamicsoft.DsLibs.DsUtil.trustedDomains";

  public static final String TRUSTED_DOMAINS_DEFAULT = "sip.webex.com";

  // CAFFEINE 2.0 (EDCS-295391) PRACK support
  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.100relSupport: &nbsp; &nbsp; To indicate if the 100rel is
   * "Required"/"Supported"/"Unsupported".
   */
  public static final String PROP_100REL_SUPPORT =
      "com.dynamicsoft.DsLibs.DsSipLlApi.100relSupport";
  /** DsSipConstants.UNSUPPORTED */
  public static final int PROP_100REL_SUPPORT_DEFAULT = UNSUPPORTED;

  /**
   * com.dynamicsoft.DsLibs.DsSipObject.HeaderPriLevel: &nbsp; &nbsp; The last header ID to include
   * in the array of headers in DsSipMessage.
   *
   * @see DsSipConstants
   */
  public static final String PROP_HEADER_PRI = "com.dynamicsoft.DsLibs.DsSipObject.HeaderPriLevel";

  /**
   * com.dynamicsoft.DsLibs.DsSipObject.localIP: &nbsp; &nbsp; The local IP to use to initialize the
   * default branch ID generator.
   */
  public static final String PROP_LOCAL_IP = "com.dynamicsoft.DsLibs.DsSipObject.localIP";

  /**
   * com.dynamicsoft.DsLibs.DsSipObject.txnUniquifier: &nbsp; &nbsp; The name of a header field to
   * use as part of the server transaction key.
   */
  public static final String PROP_TXN_UNIQUE = "com.dynamicsoft.DsLibs.DsSipObject.txnUniquifier";
  /** none. */

  /**
   * com.dynamicsoft.DsLibs.DsSipObject.emptyLR: &nbsp; &nbsp; Tells whether the "lr" parameter in
   * the SIP URL should be just a flag parameter or it should have value of "true" (like lr=true),
   * when present. The value of "false" for this property means that this parameter should appear
   * like "lr=true", otherwise just a flag parameter in the SIP URL. By default, this property value
   * is true.
   */
  public static final String PROP_EMPTY_LR = "com.dynamicsoft.DsLibs.DsSipObject.emptyLR";
  /** True. */
  public static final boolean PROP_EMPTY_LR_DEFAULT = true;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.emulate2543Responses: &nbsp; &nbsp; Tells if the sip
   * responses need to emulate RFC 2543.
   */
  public static final String PROP_EMULATE_RFC2543_RESPONSES =
      "com.dynamicsoft.DsLibs.DsSipLlApi.emulate2543Responses";
  /** false. */
  public static final boolean PROP_EMULATE_RFC2543_RESPONSES_DEFAULT = false;

  /** com.dynamicsoft.DsLibs.DsUtil.fipsProvider: &nbsp; &nbsp; The SSL FIPS provider. */
  public static final String PROP_FIPS_PROVIDER = "com.dynamicsoft.DsLibs.DsUtil.fipsProvider";

  public static final String PROP_FIPS_PROVIDER_DEFAULT = "BCFIPS";

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.DsEmptyMessageBytes.respondToEmptyMessages: &nbsp; &nbsp;
   * Send a response for all received empty messages.
   */
  public static final String PROP_RESPOND_TO_EMPTY_MESSAGES =
      "com.dynamicsoft.DsLibs.DsSipLlApi.DsEmptyMessageBytes.respondToEmptyMessages";
  /** true. */
  public static final boolean PROP_RESPOND_TO_EMPTY_MESSAGES_DEFAULT = true;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.DsSipServerLocator.debug: &nbsp; &nbsp; For Cisco use only.
   * Use debug data instead of DNS.
   */
  public static final String PROP_DNS_DEBUG =
      "com.dynamicsoft.DsLibs.DsSipLlApi.DsSipServerLocator.debug";
  /** false. */
  public static final boolean PROP_DNS_DEBUG_DEFAULT = false;

  /** com.dynamicsoft.DsLibs.DsSipLlApi.xtcp: &nbsp; &nbsp; For Cisco use only. */
  // CAFFEINE 2.0 add XTCP variable
  public static final String PROP_XTCP = "com.dynamicsoft.DsLibs.DsSipLlApi.xtcp";
  /** false. */
  public static final boolean PROP_XTCP_DEFAULT = false;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.xtcp.outQLength: &nbsp; &nbsp; For Cisco use only. The number
   * of SIP messages to hold once the TCP output buffer fills. This is purposely large to handle
   * server to server TCP/TLS communications. If you make this number small, then you need to make
   * sure that you are only using TCP/TLS on the fan-out side of the network. Otherwise, this number
   * should be set on a per DsNetwork basis.
   */
  public static final String PROP_XTCP_OUT_QLEN =
      "com.dynamicsoft.DsLibs.DsSipLlApi.xtcp.outQLength";
  /** 8000. */
  public static final int PROP_XTCP_OUT_QLEN_DEFAULT = 8000;

  // qfang - 11.27.06 - CSCsg64718 - manage unreachable destination
  /** Maxium number of seconds the unreachable destination entries are kept in the table */
  public static final String PROP_UNREACHABLE_DEST_TIMEOUT =
      "com.dynamicsoft.DsLibs.DsSipLlApi.unreachDestTimeout";
  /** 180. */
  public static final int PROP_UNREACHABLE_DEST_TIMEOUT_DEFAULT = 180;

  /*
   * Added new property for checking to use whether to Check DsUnreachableDestinationTable or not
   *
   * Part of defect fix for network fluctuation where CVP threads got exhausted
   *
   * CSCtz70393 Thread pool exhausted when all elements in srv group is down
   */

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.UseDsUnreachableDestinationTable: &nbsp; &nbsp; To handle
   * network fluctuations when CVP threads get exhausted.
   */
  public static final String PROP_USE_DSUNREACHABLE_DESTINATION_TABLE =
      "com.dynamicsoft.DsLibs.DsSipLlApi.UseDsUnreachableDestinationTable";
  /** false. */
  public static final boolean PROP_USE_DSUNREACHABLE_DESTINATION_TABLE_DEFAULT = false;

  /** com.dynamicsoft.JAIN: &nbsp; &nbsp; For Cisco use only. */
  public static final String PROP_JAIN = "com.dynamicsoft.JAIN";
  /** false. */
  public static final boolean PROP_JAIN_DEFAULT = false;

  /**
   * com.dynamicsoft.DsLibs.DsSipObject.noMaxForwards: &nbsp; &nbsp; For Cisco use only. This
   * property tells whether Max-Forwards should not be added explicitly by stack into SIP requests.
   * This is off by default and can be set by specifying the Java System Property
   * "com.dynamicsoft.DsLibs.DsSipObject.noMaxForwards" to 'true'.
   */
  public static final String PROP_NO_MAX_FORWARDS =
      "com.dynamicsoft.DsLibs.DsSipObject.noMaxForwards";
  /** false. */
  public static final boolean PROP_NO_MAX_FORWARDS_DEFAULT = false;

  /**
   * com.dynamicsoft.DsLibs.DsSipObject.validateExtensions: &nbsp; &nbsp; For Cisco use only. This
   * property tells whether the messages specific to SIP extensions (non RFC 3261 specific) should
   * be validated.
   *
   * <p>For Example, <br>
   * It tells whether the SUBSCRIBE and NOTIFY requests should be checked for the presence of one
   * and only one Event Header, and also for the presence of Subscription-State Header in the NOTIFY
   * requests. If this property is set to true and the SUBSCRIBE/NOTIFY request doesn't contain
   * exactly one Event Header, for example, then "400 Bad Request" response would be sent by the
   * stack automatically. This is set by default and can be unset by specifying the Java System
   * Property "com.dynamicsoft.DsLibs.DsSipObject.validateExtensions" to 'false' or at run time
   * through {@link DsSipMessageValidator#setValidateExtensions(boolean) validate}.
   */
  public static final String PROP_VALIDATE_EXTENSIONS =
      "com.dynamicsoft.DsLibs.DsSipObject.validateExtensions";

  /** false. */
  public static final boolean PROP_VALIDATE_EXTENSIONS_DEFAULT = true;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.clientCallback: &nbsp; &nbsp; For Cisco use only. Specifies
   * the maximum number of worker threads for the Client Transaction Callback Queue. The default
   * value is specified in {@link #PROP_CLIENT_CB_WORKERS_DEFAULT}.
   */
  public static final String PROP_CLIENT_CB_WORKERS =
      "com.dynamicsoft.DsLibs.DsSipLlApi.clientCallback";

  /** 2. */
  public static final int PROP_CLIENT_CB_WORKERS_DEFAULT = 2;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.serverCallback: &nbsp; &nbsp; For Cisco use only. Specifies
   * the maximum number of worker threads for the Server Transaction Callback Queue. The default
   * value is specified in {@link #PROP_SERVER_CB_WORKERS_DEFAULT}.
   */
  public static final String PROP_SERVER_CB_WORKERS =
      "com.dynamicsoft.DsLibs.DsSipLlApi.serverCallback";

  /** 2. */
  public static final int PROP_SERVER_CB_WORKERS_DEFAULT = 2;

  /**
   * com.dynamicsoft.DsLibs.DsSipLlApi.autoResponseCancel: &nbsp; &nbsp; For Cisco use only. Tells
   * whether 487 response will be generated and sent on receiving CANCEL for a non-proxy server
   * transaction, while still in CALLING or PROCEEDING state. The default value is specified in
   * {@link #PROP_AUTO_RESPONSE_CANCEL_DEFAULT}.
   */
  public static final String PROP_AUTO_RESPONSE_CANCEL =
      "com.dynamicsoft.DsLibs.DsSipLlApi.autoResponseCancel";

  /** true. */
  public static final boolean PROP_AUTO_RESPONSE_CANCEL_DEFAULT = true;

  /** com.dynamicsoft.DsLibs.DsSipLlApi.ssl.handshake.timout: &nbsp; &nbsp; */
  public static final String PROP_SSL_HANDSHAKE_TIMEOUT =
      "com.dynamicsoft.DsLibs.DsSipLlApi.ssl.handshake.timout";
  /** 3000 milli seconds. */
  public static final int PROP_SSL_HANDSHAKE_TIMEOUT_DEFAULT = 3000;

  /** com.dynamicsoft.DsLibs.DsSipLlApi.connectionId.close: &nbsp; &nbsp; */
  public static final String PROP_CONNECTION_ID_CLOSE =
      "com.dynamicsoft.DsLibs.DsSipLlApi.connectionId.close";
  /** True. */
  public static final boolean PROP_CONNECTION_ID_CLOSE_DEFAULT = true;

  /** com.dynamicsoft.DsLibs.DsSipObject.nonce.timeWindow: &nbsp; &nbsp; */
  public static final String PROP_NONCE_TIME_WINDOW =
      "com.dynamicsoft.DsLibs.DsSipObject.nonce.timeWindow";
  /** 15. */
  public static final int PROP_NONCE_TIME_WINDOW_DEFAULT = 15;

  /** com.dynamicsoft.DsLibs.DsSipObject.cnonce.timeWindow: &nbsp; &nbsp; */
  public static final String PROP_CNONCE_TIME_WINDOW =
      "com.dynamicsoft.DsLibs.DsSipObject.cnonce.timeWindow";
  /** 1. */
  public static final int PROP_CNONCE_TIME_WINDOW_DEFAULT = 1;

  /**
   * com.dynamicsoft.DsLibs.DsSipObject.response.receive: &nbsp; &nbsp; when set to true then
   * received tag in the via is not removed if present.
   */
  public static final String PROP_ALLOW_RECEIVED_PARAM =
      "com.dynamicsoft.DsLibs.DsSipObject.response.receive";
  /** False. */
  public static final boolean PROP_ALLOW_RECEIVED_PARAM_DEFAULT = false;

  public static final String PROP_DO_A_QUERY_AFTER_SRV =
      "com.dynamicsoft.DsLibs.DsSipLlApi.doAQueryOnSRVFailure";
  public static final boolean PROP_DO_A_QUERY_AFTER_SRV_DEFAULT = false;

  /*
   * com.dynamicsoft.DsLibs.DsSipLlApi.createDnsServerGroup: &nbsp; &nbsp;
   * enables the setting of the server group creation from DNS lookup
   */
  public static final String PROP_CREATE_DNS_SERVER_GROUP =
      "com.dynamicsoft.DsLibs.DsSipLlApi.createDnsServerGroup";
  public static final boolean PROP_CREATE_DNS_SERVER_GROUP_DEFAULT = false;

  public static final String PROP_CREATE_CA_EVENTS =
      "com.dynamicsoft.DsLibs.DsSipLlApi.createCAEvents";
  public static final boolean PROP_CREATE_CA_EVENTS_DEFAULT = false;

  // KEVMO - 08.09.05 CSCsb53394 Add IP_HEADER TOS Value
  /** IP Header Value - INVALID */
  public static final int IPTOS_INVALID = -1;

  // add non-system property default values
  /** default value for compact header form usage for SIP headers */
  public static final boolean COMPACT_HEADER_USAGE_DEFAULT = false;

  /** default value for INVITE client trans retry count */
  public static final int INVITE_CLIENT_TRANS_RETRY_DEFAULT = 5;

  /** default value for non-INVITE client trans retry count */
  public static final int CLIENT_TRANS_RETRY_DEFAULT = 9;

  /** default value for non-INVITE client trans retry count */
  public static final int INVITE_SERVER_TRANS_RETRY_DEFAULT = 9;

  public static final String PROP_ENABLE_ERROR_AGGREGATOR =
      "com.cisco.DsLibs.DsSipProxy.Errors.DsProxyErrorAggregator";
  public static final boolean PROP_ENABLE_ERROR_AGGREGATOR_DEFAULT = false;

  /** DsSipRestoreInterfcae for retore a dialog upon receiving request */
  // GOGONG - 07.13.05 - Changing non-constant variable to lower case and make it private
  private static DsSipRestoreInterface restoreIf = null;

  /** Whether to use Cisco-Guid header or not. Default is false */
  // GOGONG - 07.13.05 - Changing non-constant variable to lower case and make it private
  private static boolean userCiscoGuidHeader = false;

  // /////////////////////////////////////////////////////
  //  private data

  private static Logger cat = DsLog4j.configCat;

  private static DsSipTransportLayer transportLayer;
  private static boolean handlingMultipleFinalResponses = false;

  // GOGONG - 07.13.05 CSCsc29805- Adding static member to specify default transport type as UDP
  private static Transport defaultOutgoingTransport = Transport.UDP;

  // GOGONG- 02.15.06 CSCsd32536 define static member to specify grace period to terminate expired
  // subscription.
  private static long gracePeriodOnTerminateSub = 0;

  // qfang - 05.11.06 - CSCsd948585 client authentication enhancement
  private static DsByteString localOutboundProxyRealm;

  // maivu - 11.01.06 - CSCsg22401 - default expiration for an INVITE or non-INVITE (64 seconds)
  public static final String PROP_DEFAULT_INVITE_EXPIRATION =
      "com.dynamicsoft.DsLibs.DsSipLlApi.m_defaultInviteExpiration";
  private static int defaultInviteExpiration = 64000;

  /**
   * Number of bytes of a SIP message to log when using the special IN/OUT logger. Default is 400.
   */
  private static int inOutLogMsgSize = 400;

  // Max size for any SIP Message
  public static final String PROP_MAX_SIZE_SIP_MESSAGE_POLICY =
      "com.dynamicsoft.DsLibs.DsSipLlApi.MaxSizeSipMessagePolicy";

  public static int maxSizeSipMessagePolicy = 50000;
  private static String globalDnsServerFailoverCodes = "502,503";

  /**
   * Get the grace period in milliseconds. The default value is 0. The grace period is a time to
   * hold termination of an expired subscription due to a minor delay.
   *
   * <p><b>See Also:</b> &nbsp;&nbsp;&nbsp;{@link
   * #setGracePeriodForExpiredSubscriptionTermination(long)}.
   *
   * @return the long value of grace period in millisecond.
   */
  // GOGONG 02.15.06 CSCsd32536 add new API to get grace period.
  public static long getGracePeriodForExpiredSubscriptionTermination() {
    return gracePeriodOnTerminateSub;
  }

  /**
   * Set the grace period in milliseconds before terminating an expired subscription due to a minor
   * delay. This API doesn't limit a max value, but it's up to user's best knowledge to choose the
   * fitting value. In general, the recommended value could be anything among 100 to 1000
   * milliseconds. The default value is 0.
   *
   * <p><b>See Also:</b> &nbsp;&nbsp;&nbsp;{@link
   * #getGracePeriodForExpiredSubscriptionTermination()}.
   *
   * @param graceTimeMillis the long value of the grace time in millisecond.
   */
  // GOGONG 02.15.06 CSCsd32536 add new API to set grace period.
  public static void setGracePeriodForExpiredSubscriptionTermination(long graceTimeMillis) {
    gracePeriodOnTerminateSub = graceTimeMillis;
  }

  /**
   * Used by both stack/application to get the default transport
   *
   * @return int
   */
  // GOGONG - 07.13.05 CSCsc29805
  public static Transport getDefaultOutgoingTransport() {
    return defaultOutgoingTransport;
  }

  /**
   * Used by application to reset the default transport type
   *
   * @param transport int
   */
  // GOGONG - 07.13.05 CSCsc29805
  public static void setDefaultOutgoingTransport(Transport transport) {
    defaultOutgoingTransport = transport;
  }

  /**
   * Sets type-of-service octet in the IP header for packets. As the underlying network
   * implementation may ignore this value applications should consider it a hint.
   *
   * <p>The tc <B>must</B> be in the range <code> 0 <= tc <=
   * 255</code> or an IllegalArgumentException will be thrown.
   *
   * <p>Notes:
   *
   * <p>for Internet Protocol v4 the value consists of an octet with precedence and TOS fields as
   * detailed in RFC 1349. The TOS field is bitset created by bitwise-or'ing values such the
   * following :-
   *
   * <p>
   *
   * <UL>
   *   <LI><CODE>IPTOS_LOWCOST (0x02)</CODE>
   *   <LI><CODE>IPTOS_RELIABILITY (0x04)</CODE>
   *   <LI><CODE>IPTOS_THROUGHPUT (0x08)</CODE>
   *   <LI><CODE>IPTOS_LOWDELAY (0x10)</CODE>
   * </UL>
   *
   * The last low order bit is always ignored as this corresponds to the MBZ (must be zero) bit.
   *
   * <p>Setting bits in the precedence field may result in a SocketException indicating that the
   * operation is not permitted. The SocketException will be thrown when the stack is started.
   *
   * <p>for Internet Protocol v6 <code>tc</code> is the value that would be placed into the
   * sin6_flowinfo field of the IP header.
   *
   * @param tc an <code>int</code> value for the bitset.
   * @throws IllegalArgument if there is an error setting the type-of-service
   *     <p>Example: 01100000 is desired number, tc value would be 96 01110000 is desired number, tc
   *     value would be 112
   */

  /**
   * Used by the stack to retrieve IP type of service value.
   *
   * @return the integer value of IP Type Of Service
   */
  // qfang - 05.11.06 - CSCsd948585 client authentication enhancement
  /**
   * Set local outbound proxy realm
   *
   * @param realm DsByteString local outbound proxy realm
   */
  public static void setLocalOutboundProxyRealm(DsByteString realm) {
    localOutboundProxyRealm = realm;
  }

  /**
   * Retrieve local outbound proxy realm
   *
   * @return DsByteString local outbound proxy realm
   */
  public static DsByteString getLocalOutboundProxyRealm() {
    return localOutboundProxyRealm;
  }

  //  maivu - 11.01.06 - CSCsg22401 Getter and Setter for default expiration of INVITE and
  // non-INVITE requests
  /**
   * Retrieve the default INVITE request expiration
   *
   * @return int default invitation expiration
   */
  public static int getDefaultInviteExpiration() {
    return defaultInviteExpiration;
  }

  /**
   * Set the default INVITE expiration. This timeout value will be used ONLY in the absence of the
   * Expires header in the INVITE.
   *
   * <p>The application is responsible to set this expiration value. If it is not set, the default
   * value will be 64s.
   *
   * @param expiration the expiration value
   */
  public static void setDefaultInviteExpiration(int expiration) {
    defaultInviteExpiration = expiration;
  }

  /**
   * Used by the stack to retrieve an int system property.
   *
   * @param name the property name.
   * @param defaultValue the default value.
   * @return the integer value of the specified property
   */
  public static int getProperty(String name, int defaultValue) {
    Integer i = Integer.getInteger(name);
    return (i == null) ? defaultValue : i.intValue();
  }

  /**
   * Used by the stack to retrieve an boolean system property.
   *
   * @param name the property name.
   * @param defaultValue the default value.
   * @return the boolean value of the specified property
   */
  public static boolean getProperty(String name, boolean defaultValue) {
    String prop = System.getProperty(name);
    return (prop == null) ? defaultValue : Boolean.valueOf(prop).booleanValue();
  }

  /**
   * Used by the stack to retrieve a system property.
   *
   * @param name the property name.
   * @return the String value of the specified property
   */
  public static String getProperty(String name) {
    return System.getProperty(name);
  }

  /**
   * Used by the stack to retrieve a system property.
   *
   * @param name the property name.
   * @param defaultValue the default value.
   * @return the String value of the specified property
   */
  public static String getProperty(String name, String defaultValue) {
    return System.getProperty(name, defaultValue);
  }

  /**
   * Turn caching of String and byte[] representations of integers on or off. Uses the last cache
   * size set.
   *
   * @param on <code>true</code> to enable the cache, <code>false</code> to turn it off
   */
  public static void useIntStrCache(boolean on) {
    if (on) {
      DsIntStrCache.on();
    } else {
      DsIntStrCache.off();
    }
  }

  /**
   * Gets the value for timers T1, T2, T3, T4 as specified in bis 07 and the max transaction
   * duration timer Tn.
   *
   * @param network the network whose timer values you want to get. If it is null, a timer value for
   *     default network is returned.
   * @param timerID the timer for which you want to get the value. Timer IDs are defined in
   *     DsSipConstants. Valid values are T1, T2, T3, T4, clientTn(for client only), serverTn(for
   *     server only) and Tn(for both client and server. Deprecated).
   * @return the value for the timer. -1 if the timerID is invalid
   */
  public static int getTimerValue(DsNetwork network, byte timerID) {
    if (network == null) return DsNetwork.getDefault().getSipTimers().getTimerValue(timerID);
    return network.getSipTimers().getTimerValue(timerID);
  }

  /**
   * This method just calls getTimerValue(null, timerID). i.e. it works on default network.
   *
   * @param timerID the timer for which you want to get the value. Timer IDs are defined in
   *     DsSipConstants. Valid values are T1, T2, T3, T4, clientTn(for client only), serverTn(for
   *     server only) and Tn(for both client and server. Deprecated).
   * @return the value for the timer. -1 if the timerID is invalid
   */
  public static int getTimerValue(byte timerID) {
    return getTimerValue(null, timerID);
  }

  /**
   * Sets the value for timers T1, T2, T3, T4 as specified in bis 07 and the max transaction
   * duration timer Tn.
   *
   * @param network the network whose timer values you want to set. If it is null, a timer value for
   *     default network is set.
   * @param timerID the timer for which you want to set the value. Timer IDs are defined in
   *     DsSipConstants. Valid values are T1, T2, T3, T4, clientTn(for client only), serverTn(for
   *     server only) and Tn(for both client and server. Deprecated).
   * @param timerValue the timer value in milliseconds.
   * @return the result of this set operation. <code>false</code> if timerID is invalid, <code>true
   *     </code> otherwise.
   */
  public static boolean setTimerValue(DsNetwork network, byte timerID, int timerValue) {
    if (network == null)
      return DsNetwork.getDefault().getSipTimers().setTimerValue(timerID, timerValue);
    return network.getSipTimers().setTimerValue(timerID, timerValue);
  }

  /**
   * This method just calls setTimerValue(null, timerID, timerValue). i.e. it works on the default
   * network.
   *
   * @param timerID the timer for which you want to set the value. Timer IDs are defined in
   *     DsSipConstants. Valid values are T1, T2, T3, T4, clientTn(for client only), serverTn(for
   *     server only) and Tn(for both client and server. Deprecated).
   * @param timerValue the timer value in milliseconds.
   * @return the result of this set operation. <code>false</code> if timerID is invalid, <code>true
   *     </code> otherwise.
   */
  public static boolean setTimerValue(byte timerID, int timerValue) {
    return setTimerValue(null, timerID, timerValue);
  }

  /**
   * Gets the retry count for retransmissions. For Invite client transaction, it gets the max number
   * of retransmission of INVITE. For non-Invite client transaction, it gets the max number of
   * retransmission of non-Invite request. For Invite server transaction, it gets the max number of
   * final response retransmissions.
   *
   * @param network the network whose retry counts you want to get. If it is null, a retry count for
   *     default network is returned.
   * @param transType the type of transaction. The valid values are defined in DsSipTimers and they
   *     are INVITE_CLIENT_TRANS, CLIENT_TRANS(for non-Invite) and INVITE_SERVER_TRANS.
   * @return the number of retransmissions for this transaction type. -1 if transaction type is
   *     invalid.
   */
  public static int getRetryCount(DsNetwork network, byte transType) {
    if (network == null) return DsNetwork.getDefault().getSipTimers().getRetryCount(transType);
    return network.getSipTimers().getRetryCount(transType);
  }

  /**
   * This method calls getRetryCount(null, transType). I.e. it works on default network.
   *
   * @param transType the type of transaction. The valid values are defined in DsSipTimers and they
   *     are INVITE_CLIENT_TRANS, CLIENT_TRANS(for non-Invite) and INVITE_SERVER_TRANS.
   * @return the number of retransmissions for this transaction type. -1 if transaction type is
   *     invalid.
   */
  public static int getRetryCount(byte transType) {
    return getRetryCount(null, transType);
  }

  /**
   * Sets the retry count for retransmissions. For Invite client transaction, it sets the max number
   * of retransmission of INVITE. For non-Invite client transaction, it sets the max number of
   * retransmission of non-Invite request. For Invite server transaction, it sets the max number of
   * final response retransmissions.
   *
   * @param network the network whose retry counts you want to set. If it is null, a retry count for
   *     default network is set.
   * @param transType the type of transaction. The valid values are defined in DsSipTimers and they
   *     are INVITE_CLIENT_TRANS, CLIENT_TRANS(for non-Invite) and INVITE_SERVER_TRANS.
   * @param retryCount the number of retransmissions.
   * @return the result of this set operation. <code>false</code> if transType is invalid and <code>
   *     true</code> otherwise.
   */
  public static boolean setRetryCount(DsNetwork network, byte transType, int retryCount) {
    if (network == null)
      return DsNetwork.getDefault().getSipTimers().setRetryCount(transType, (byte) retryCount);
    return network.getSipTimers().setRetryCount(transType, (byte) retryCount);
  }

  /**
   * This method calls setRetryCount(null, transType, retryCount). I.e. it works on default network.
   *
   * @param transType the type of transaction. The valid values are defined in DsSipTimers and they
   *     are INVITE_CLIENT_TRANS, CLIENT_TRANS(for non-Invite) and INVITE_SERVER_TRANS.
   * @param retryCount the number of retransmissions.
   * @return the result of this set operation. <code>false</code> if transType is invalid and <code>
   *     true</code> otherwise.
   */
  public static boolean setRetryCount(byte transType, int retryCount) {
    return setRetryCount(null, transType, retryCount);
  }

  /**
   * Returns the "transaction completion timeout" time, in millisecs, that is the default value for
   * all the transactions generated throughout the stack. The specified value exhibits the behavior
   * that how much time a transaction should wait for and keep on retransmitting the SIP message in
   * case of UDP, where retransmissions provide the mechanism for the reliability, before timing
   * out.
   *
   * <p>Default value for Invite client trans and non-Invite server trans is 32 secs and for Invite
   * server trans and non-Invite client trans it is 5 secs.
   *
   * @param network the network whose completion timeout you want to get. If it is null, a
   *     completion timeout for default network is returned.
   * @param transType the transaction type. its values are defined in DsSipConstants. Possible
   *     values are CLIENT_TRANS(for non-INVITE), INVITE_CLIENT_TRANS, SERVER_TRANS and
   *     INVITE_SERVER_TRANS.
   * @return the time taken before a transaction gets completed with a timeout.
   */
  public static int getTransactionCompleteTimeout(DsNetwork network, int transType) {
    DsNetwork nw = network;
    if (nw == null) nw = DsNetwork.getDefault();

    if (transType == CLIENT_TRANS || transType == INVITE_SERVER_TRANS) {
      return nw.getSipTimers().getTimerValue(T4);
    } else if (transType == INVITE_CLIENT_TRANS || transType == SERVER_TRANS) {
      return nw.getSipTimers().getTimerValue(TU2);
    }
    return -1;
  }

  /**
   * This method calls getTransactionCompleteTimeout(null, transType). i.e. it works on the default
   * network.
   *
   * @param transType the transaction type. its values are defined in DsSipConstants. Possible
   *     values are CLIENT_TRANS(for non-INVITE), INVITE_CLIENT_TRANS, SERVER_TRANS and
   *     INVITE_SERVER_TRANS.
   * @return the time taken before a transaction gets completed with a timeout.
   */
  public static int getTransactionCompleteTimeout(int transType) {
    return getTransactionCompleteTimeout(null, transType);
  }

  /**
   * Sets the "transaction completion timeout" time, in millisecs, that will be the default value
   * for all the transactions generated throughout the stack. The specified value exhibits the
   * behavior that how much time a transaction should wait for and keep on retransmitting the SIP
   * message in case of UDP, where retransmissions provide the mechanism for the reliability, before
   * timing out.
   *
   * <p>Default value for Invite server trans and non-Invite client trans is 5 secs and for
   * Non-Invite server trans and Invite client trans it is 32(64*T1) secs.
   *
   * <p>Note that this method will not work for Invite server trans and non-Invite client trans
   * since the value depends on T1.
   *
   * @param network the network whose completion timeout you want to set. If it is null, a
   *     completion timeout for default network is set.
   * @param transType the transaction type defined in DsSipConstants. Possible values are
   *     CLIENT_TRANS(for non-INVITE), INVITE_CLIENT_TRANS, SERVER_TRANS and INVITE_SERVER_TRANS.
   * @param millisecs the time(in millisecs) taken before a transaction gets completed with a
   *     timeout.
   */
  public static void setTransactionCompleteTimeout(
      DsNetwork network, byte transType, int millisecs) {
    DsNetwork nw = network;
    if (nw == null) nw = DsNetwork.getDefault();

    if (transType == CLIENT_TRANS || transType == INVITE_SERVER_TRANS) {
      nw.getSipTimers().setTimerValue(T4, millisecs);
    }
    // for others, the default value is 64*T1. But spec gives a value of 32s. Can be set here
    else if (transType == INVITE_CLIENT_TRANS || transType == SERVER_TRANS) {
      nw.getSipTimers().setTimerValue(TU2, millisecs);
    }
  }

  /**
   * This method calls setTransactionCompleteTimeout(null, transType, millisecs). i.e. it works on
   * the default network.
   *
   * @param transType the transaction type defined in DsSipConstants. Possible values are
   *     CLIENT_TRANS(for non-INVITE), INVITE_CLIENT_TRANS, SERVER_TRANS and INVITE_SERVER_TRANS.
   * @param millisecs the time(in millisecs) taken before a transaction gets completed with a
   *     timeout.
   */
  public static void setTransactionCompleteTimeout(byte transType, int millisecs) {
    setTransactionCompleteTimeout(null, transType, millisecs);
  }

  /**
   * Sets the maximum size of a keep alive message. In order to be a keep alive message, this
   * message must be strictly less than this number of bytes long.
   *
   * @param size the max size
   */
  public static void setMaxKeepAliveSize(int size) {
    // TODO
    // DsPacketListener.setMaxKeepAliveSize(size);
  }

  //    /**
  //     * Sets the threshold value for the number of TCP/TLS connections that are
  //     * supported by the existing Transport Layer. The threshold value signifies
  //     * that, the user should be notified once the active number of connection exceeds
  //     * this value, so that he/she can take act accordingly either by increasing the
  //     * max limit or should be ready to expect that the max limit may be reached soon.
  //     *
  //     * @param threshold the threshold number of TCP/TLS connections
  //     */
  //    public static void setThresholdTcpTlsConnections(int threshold)
  //    {
  //        if (transportLayer != null)
  //        {
  //// --            transportLayer.setThresholdConnections(threshold);
  //        }
  //    }
  //
  //    /**
  //     * Returns the threshold value for the number of TCP/TLS connections that are
  //     * supported by the existing Transport Layer. The threshold value signifies
  //     * that, the user should be notified once the active number of connection exceeds
  //     * this value, so that he/she can take act accordingly either by increasing the
  //     * max limit or should be ready to expect that the max limit may be reached soon.
  //     *
  //     * @return the threshold number of TCP/TLS connections
  //     */
  //    public static int getThresholdTcpTlsConnections()
  //    {
  //        int threshold = 0;
  //        if (transportLayer != null)
  //        {
  //// --            threshold = transportLayer.getThresholdConnections();
  //        }
  //        return threshold;
  //    }

  /**
   * Set the TransportLayer reference which will be used to retrieve/provide the necessary
   * information for SNMP traps.
   *
   * @param transLayer the Transport Layer that is used for transporting the messages
   */
  public static void setTransportLayer(DsSipTransportLayer transLayer) {
    transportLayer = transLayer;
  }

  /**
   * Returns boolean to indicate if multiple final response handling is enabled.
   *
   * @return boolean to indicate if multiple final response handling is enabled
   * @see DsSipMFRClientTransactionInterface
   */
  public static boolean handlingMultipleFinalResponses() {
    return handlingMultipleFinalResponses;
  }

  /**
   * Formats all the UA Stack specific properties, that have default values or explicitly set by the
   * application, to the standard output stream. The properties are formatted as name-value pairs
   * per line.
   *
   * @return the formatted string of all the UA Stack properties.
   */
  public static String formatProperties() {
    StringBuffer buffer = new StringBuffer();

    Field[] fields = DsConfigManager.class.getFields();
    if (null != fields) {
      String name = null;
      String value = null;
      for (int i = 0; i < fields.length; i++) {
        name = fields[i].getName();
        if (name.startsWith("PROP_") && !name.endsWith("_DEFAULT")) {

          try {
            name = fields[i].get(DsConfigManager.class).toString();
            buffer.append(name);
            buffer.append("\t");
            value = System.getProperty(name);
            if (null == value) {
              Field field = DsConfigManager.class.getField(fields[i].getName() + "_DEFAULT");
              value = field.get(DsConfigManager.class).toString();
            }
            buffer.append(value);
          } catch (Exception e) {
          }

          buffer.append("\r\n");
        }
      }
    }

    return buffer.toString();
  }

  /**
   * Get use Cisco-Guid header flag.
   *
   * @return use Cisco-Guid header flag.
   */
  public static boolean isUseCiscoGuidHeader() {
    // GOGONG - 07.13.05 - Changing non-constant variable to lower case
    return userCiscoGuidHeader;
  }
  // ////// DEPRECATED  API /////////////////////////////////
  /**
   * Returns the maximum number of times, the retransmission of INVITE requests will take place.
   *
   * @return the maximum number of times the INVITE requests will be retransmitted.
   * @deprecated <code> use getRetryCount(INVITE_CLIENT_TRANS) </code>
   */
  public static int getInviteRetryCount() {
    return getRetryCount(INVITE_CLIENT_TRANS);
  }

  /**
   * Returns the maximum number of times, the retransmission of Non-Invite requests will take place.
   *
   * @return the maximum number of times the Non-Invite requests will be retransmitted.
   * @deprecated use {@link #getRetryCount(byte) getRetryCount(CLIENT_TRANS)}.
   */
  public static int getRequestRetryCount() {
    return getRetryCount(CLIENT_TRANS);
  }

  /**
   * Sets the maximum number of times, the retransmission of Non-Invite requests should take place.
   *
   * @deprecated use {@link #setRetryCount(byte, int) setRetryCount(CLIENT_TRANS, count)}.
   * @param count the maximum number of times the Non-Invite requests should be retransmitted.
   */
  public static void setRequestRetryCount(int count) {
    setRetryCount(CLIENT_TRANS, count);
  }

  /**
   * Sets the maximum number of times, the retransmission of Invite requests should take place.
   *
   * @deprecated use setRetryCount(INVITE_CLIENT_TRANS, count)
   * @param count the maximum number of times the Invite requests should be retransmitted.
   */
  public static void setInviteRetryCount(int count) {
    setRetryCount(INVITE_CLIENT_TRANS, count);
  }

  /**
   * Sets the number of times a response to an INVITE will be retransmitted before receiving the
   * ACK.
   *
   * @deprecated use setRetryCount(INVITE_SERVER_TRANS, count)
   * @param count the number of times a response will be retransmitted
   */
  public static void setResponseRetryCount(int count) {
    setRetryCount(INVITE_SERVER_TRANS, count);
  }

  /**
   * Return the number of times a response to an INVITE will be retransmitted before receiving the
   * ACK.
   *
   * @deprecated use getRetryCount(INVITE_SERVER_TRANS)
   * @return the number of times a request will be retransmitted
   */
  public static int getResponseRetryCount() {
    return getRetryCount(INVITE_SERVER_TRANS);
  }

  /**
   * Returns the number of times the transactions are retried. A static counter is maintained to
   * keep track of the number of times the transactions are retried. Every time a transaction is
   * retried, this counter is incremented.
   *
   * @deprecated This method is deprecated since v5.0, as new API methods are added to provide more
   *     specific and detailed metrics for the SIP message flow. This method returns the total
   *     retransmissions occurred throughout the stack during its life time.
   * @return always returns 0
   */
  public static int getTransactionRetryCount() {
    return 0;
  }
  /**
   * Returns the delay time that is experienced before resending a transaction. This delay time
   * comes into picture in case of UDP as transport. Because in UDP the transport reliability is
   * provided by retransmitting the messages across the channel repeatedly in intervals. So the
   * Initial Retry Delay is the time period that occurs between the first message sent and the first
   * contiguous retry message. The default time delay is 500 milli seconds.
   *
   * @deprecated use getTimerValue(T1)
   * @return the time interval between the first message and the first contiguous retry message.
   */
  public static int getTransactionInitialRetryDelay() {
    return getTimerValue(T1);
  }
  /**
   * sets the delay time that should be experienced before resending a transaction. This delay time
   * comes into picture in case of UDP as transport. Because in UDP the transport reliability is
   * provided by retransmitting the messages across the channel repeatedly in intervals. So the
   * Initial Retry Delay is the time period that occurs between the first message sent and the first
   * contiguous retry message. The default time delay is 500 milli seconds.
   *
   * @deprecated use setTimerValue(T1, millisecs)
   * @param millisecs the time interval(in millisecs) between the first message and the first
   *     contiguous retry message.
   */
  public static void setTransactionInitialRetryDelay(int millisecs) {
    setTimerValue(T1, millisecs);
  }

  /**
   * Returns the "transaction completion timeout" time, in millisecs, that is the default value for
   * all the transactions generated throughout the stack. The specified value exhibits the behavior
   * that how much time a transaction should wait for and keep on retransmitting the SIP message in
   * case of UDP, where retransmissions provide the mechanism for the reliability, before timing
   * out.
   *
   * @deprecated bis 7 specifies different values for completion timeout according to transaction
   *     type. Use getTransactionCompleteTimeout(int transType) instead.
   * @return the time taken before a transaction gets completed with a timeout.
   */
  public static int getTransactionCompleteTimeout() {
    return getTransactionCompleteTimeout(INVITE_SERVER_TRANS);
  }

  /**
   * Sets the "transaction completion timeout" time, in millisecs, that will be the default value
   * for all the transactions generated throughout the stack. The specified value exhibits the
   * behavior that how much time a transaction should wait for and keep on retransmitting the SIP
   * message in case of UDP, where retransmissions provide the mechanism for the reliability, before
   * timing out.
   *
   * @deprecated bis 7 specifies different values for completion timeout according to transaction
   *     type. Use setTransactionCompleteTimeout(int transType, int millisecs) instead.
   * @param millisecs the time(in millisecs) taken before a transaction gets completed with a
   *     timeout.
   */
  public static void setTransactionCompleteTimeout(int millisecs) {
    setTransactionCompleteTimeout(INVITE_SERVER_TRANS, millisecs);
  }

  /**
   * Returns the maximum size of the Message Queue that contains the messages, along with the timer
   * events, to be processed.
   *
   * @return the maximum size of the Message Queue
   * @deprecated This method is deprecated since v5.0, and it always returns 0.
   */
  public static int getMessageQueueMaxSize() {
    return 0;
  }

  /**
   * Returns the current size of the Message Queue that contains the messages, along with the timer
   * events, to be processed.
   *
   * @return the current size of the Message Queue
   * @deprecated This method is deprecated since v5.0, and it always returns 0.
   */
  public static int getMessageQueueSize() {
    return 0;
  }

  // //////  END DEPRECATED  API     /////////////////////////////////

  /** Private default constructor to disallow the instantiation of this class by the end-user. */
  private DsConfigManager() {}

  /**
   * Set the time to wait, in seconds, before giving up when trying to establish a TCP/TLS
   * connection. 0 means infinite wait, and is the default behavior.
   *
   * @param seconds the number of seconds to wait before giving up on a call to connect, 0 is
   *     infinite
   */
  public static void setWaitForConnection(int seconds) {
    // TODO check if this is needed once we have TCP/TLS flow
  }

  /**
   * Gets the default for whether or not the Simple Resolver is used.
   *
   * @return <code>true</code> if the Simple Resolver is enabled by default
   */
  public static boolean getSimpleResolverDefault() {
    return DsNetworkProperties.getSimpleResolverDefault();
  }

  /**
   * Sets the default for whether or not the Simple Resolver is used. This Simple Resolver setting
   * is now settable per network. If you set it here it will overwrite this setting for all existing
   * and future networks.
   *
   * @param enabled if <code>true</code>, then the Simple Resolver will be enabled, disabled
   *     otherwise.
   */
  public static void setSimpleResolverDefault(boolean enabled) {
    DsNetwork network;
    for (byte b = Byte.MIN_VALUE; b <= Byte.MAX_VALUE; b++) {
      network = DsNetwork.getNetwork(b);
      if (network != null) {
        network.setSimpleResolver(enabled);

        if (DsLog4j.resolvCat.isEnabled(Level.DEBUG)) {
          DsLog4j.resolvCat.log(
              Level.DEBUG,
              "Setting Use Simple Resolver to " + enabled + " for network: " + network);
        }
      }

      // This stops the infinite loop when b wraps
      // Do not delete this even though it looks unncessary.  -stemayer
      if (b == Byte.MAX_VALUE) {
        break;
      }
    }

    DsNetworkProperties.setSimpleResolverDefault(enabled);
  }

  /**
   * Gets the number of bytes of a SIP message to log when using the special IN / OUT logger.
   *
   * @return the number of bytes of a SIP message to log, never negative
   */
  public static int getInOutLogMsgSize() {
    return inOutLogMsgSize;
  }

  /**
   * Sets the number of bytes of a SIP message to log when using the special IN / OUT logger. The
   * default size if 400.
   *
   * @param size the number of bytes of a SIP message to log
   * @throws IllegalArgumentException if size is negative
   */
  public static void setInOutLogMsgSize(int size) {
    if (size < 0) {
      throw new IllegalArgumentException("Size must be a positive int.");
    }

    inOutLogMsgSize = size;
  }

  /**
   * Set the time to wait, in seconds, before giving up when trying to establish a TCP/TLS
   * connection. 0 means infinite wait, and is the default behavior.
   *
   * @param seconds the number of seconds to wait before giving up on a call to connect, 0 is
   *     infinite
   */
  public static void setWaitForTLSHandshake(int seconds) {
    // TODO check if this is needed once we have TCP/TLS flow
  }

  /**
   * get the Max Size parameter for SIP Message Policy
   *
   * @return maxSize in bytes as integer
   */
  public static int getsipMessagePolicyMaxSize() {
    return maxSizeSipMessagePolicy;
  }

  /**
   * Sets the value of the sip Message Policy eg max Size.
   *
   * @param maxSize use to configure Maximum size for any Sip Message
   */
  public static void configureSipMessagePolicy(int maxSize) {
    maxSizeSipMessagePolicy = maxSize;
  }

  public static String getGlobalDnsServerFailoverCodes() {
    return globalDnsServerFailoverCodes;
  }

  // REFACTOR
  //  public static void setGlobalDnsServerFailoverCodes(String dnsServerFailoverCodes)
  //      throws Exception {
  //    globalDnsServerFailoverCodes =
  //
  // FailoverResponseCode.getInstance().setGlobalDnsServerFailoverCodes(dnsServerFailoverCodes);
  //  }
}
