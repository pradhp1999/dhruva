package com.cisco.dhruva.config.sip;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import java.util.*;

public abstract class RE {

  public static final String dsReAccounting = "dsReAccounting";
  public static final String dsReAccountingDefault = "off";
  public static final String dsReAccounting_on = "on";
  public static final int index_dsReAccounting_on = 0;
  public static final String dsReAccounting_off = "off";
  public static final int index_dsReAccounting_off = 1;
  public static final String dsReAccountingClientSide = "dsReAccountingClientSide";
  public static final String dsReAccountingClientSideDefault = "off";
  public static final String dsReAccountingClientSide_on = "on";
  public static final int index_dsReAccountingClientSide_on = 0;
  public static final String dsReAccountingClientSide_off = "off";
  public static final int index_dsReAccountingClientSide_off = 1;
  public static final String dsReAccountingServerSide = "dsReAccountingServerSide";
  public static final String dsReAccountingServerSideDefault = "off";
  public static final String dsReAccountingServerSide_on = "on";
  public static final int index_dsReAccountingServerSide_on = 0;
  public static final String dsReAccountingServerSide_off = "off";
  public static final int index_dsReAccountingServerSide_off = 1;
  public static final String dsReCommit = "dsReCommit";
  public static final String dsReCompactHeaderForm = "dsReCompactHeaderForm";
  public static final String dsReCompactHeaderFormDefault = "off";
  public static final String dsReCompactHeaderForm_on = "on";
  public static final int index_dsReCompactHeaderForm_on = 0;
  public static final String dsReCompactHeaderForm_off = "off";
  public static final int index_dsReCompactHeaderForm_off = 1;
  public static final String dsReConnectionBasedSelection = "dsReConnectionBasedSelection";
  public static final String dsReConnectionBasedSelectionDefault = "off";
  public static final String dsReConnectionBasedSelection_on = "on";
  public static final int index_dsReConnectionBasedSelection_on = 0;
  public static final String dsReConnectionBasedSelection_off = "off";
  public static final int index_dsReConnectionBasedSelection_off = 1;
  public static final String dsReConnectionCacheSize = "dsReConnectionCacheSize";
  public static final int dsReConnectionCacheSizeDefault = 10000;
  public static final int dsReConnectionCacheSizeMin = 1;
  public static final String dsReDefaultContactExpires = "dsReDefaultContactExpires";
  public static final int dsReDefaultContactExpiresDefault = 3600;
  public static final int dsReDefaultContactExpiresMin = 60;
  public static final String dsReDefaultQValue = "dsReDefaultQValue";
  public static final float dsReDefaultQValueDefault = 1.0f;
  public static final float dsReDefaultQValueMin = 0.0f;
  public static final float dsReDefaultQValueMax = 1.0f;
  public static final String dsReEnumServerStatus = "dsReEnumServerStatus";
  public static final String dsReFirewallMode = "dsReFirewallMode";
  public static final String dsReFirewallModeDefault = "off";
  public static final String dsReFirewallMode_on = "on";
  public static final int index_dsReFirewallMode_on = 0;
  public static final String dsReFirewallMode_off = "off";
  public static final int index_dsReFirewallMode_off = 1;
  public static final String dsReMaxContactExpires = "dsReMaxContactExpires";
  public static final int dsReMaxContactExpiresDefault = 3600;
  public static final int dsReMaxContactExpiresMin = 60;
  public static final String dsReMaxIterationDepth = "dsReMaxIterationDepth";
  public static final int dsReMaxIterationDepthDefault = 10;
  public static final int dsReMaxIterationDepthMin = 0;
  public static final String dsReMaxMediaDescriptions = "dsReMaxMediaDescriptions";
  public static final int dsReMaxMediaDescriptionsDefault = 3;
  public static final int dsReMaxMediaDescriptionsMin = 1;
  public static final String dsReMinContactExpires = "dsReMinContactExpires";
  public static final int dsReMinContactExpiresDefault = 500;
  public static final int dsReMinContactExpiresMin = 60;
  public static final String dsReNatMedia = "dsReNatMedia";
  public static final String dsReNatMediaDefault = "off";
  public static final String dsReNatMedia_on = "on";
  public static final int index_dsReNatMedia_on = 0;
  public static final String dsReNatMedia_off = "off";
  public static final int index_dsReNatMedia_off = 1;
  public static final String dsReNatMedia_internal = "internal";
  public static final int index_dsReNatMedia_internal = 2;
  public static final String dsReOnFirewallFailure = "dsReOnFirewallFailure";
  public static final String dsReOnFirewallFailureDefault = "pass";
  public static final String dsReOnFirewallFailure_pass = "pass";
  public static final int index_dsReOnFirewallFailure_pass = 0;
  public static final String dsReOnFirewallFailure_reject = "reject";
  public static final int index_dsReOnFirewallFailure_reject = 1;
  public static final String dsReOnNextHopFailure = "dsReOnNextHopFailure";
  public static final String dsReOnNextHopFailureDefault = "failover";
  public static final String dsReOnNextHopFailure_drop = "drop";
  public static final int index_dsReOnNextHopFailure_drop = 0;
  public static final String dsReOnNextHopFailure_failover = "failover";
  public static final int index_dsReOnNextHopFailure_failover = 1;
  public static final String dsRePerformanceData = "dsRePerformanceData";
  public static final String dsRePerformanceData_on = "on";
  public static final int index_dsRePerformanceData_on = 0;
  public static final String dsRePerformanceData_off = "off";
  public static final int index_dsRePerformanceData_off = 1;
  public static final String dsRePerformanceData_print = "print";
  public static final int index_dsRePerformanceData_print = 2;
  public static final String dsRePerformanceData_reset = "reset";
  public static final int index_dsRePerformanceData_reset = 3;
  public static final String dsReQos = "dsReQos";
  public static final String dsReQosDefault = "off";
  public static final String dsReQos_on = "on";
  public static final int index_dsReQos_on = 0;
  public static final String dsReQos_off = "off";
  public static final int index_dsReQos_off = 1;
  public static final String dsReQosSgElementStatus = "dsReQosSgElementStatus";
  public static final String dsReRadiusSgElementStatus = "dsReRadiusSgElementStatus";
  public static final String dsReRecurseOnRedirect = "dsReRecurseOnRedirect";
  public static final String dsReRecurseOnRedirectDefault = "off";
  public static final String dsReRecurseOnRedirect_on = "on";
  public static final int index_dsReRecurseOnRedirect_on = 0;
  public static final String dsReRecurseOnRedirect_off = "off";
  public static final int index_dsReRecurseOnRedirect_off = 1;
  public static final String dsReRollback = "dsReRollback";
  public static final String dsReRollback_previous = "previous";
  public static final int index_dsReRollback_previous = 0;
  public static final String dsReRollback_changes = "changes";
  public static final int index_dsReRollback_changes = 1;
  public static final String dsReRouteFile = "dsReRouteFile";
  public static final String dsReSend100 = "dsReSend100";
  public static final String dsReSend100Default = "off";
  public static final String dsReSend100_on = "on";
  public static final int index_dsReSend100_on = 0;
  public static final String dsReSend100_off = "off";
  public static final int index_dsReSend100_off = 1;
  public static final String dsReSend305 = "dsReSend305";
  public static final String dsReSend305Default = "off";
  public static final String dsReSend305_on = "on";
  public static final int index_dsReSend305_on = 0;
  public static final String dsReSend305_reject_secondary = "reject-secondary";
  public static final int index_dsReSend305_reject_secondary = 1;
  public static final String dsReSend305_off = "off";
  public static final int index_dsReSend305_off = 2;
  public static final String dsReShowRoutes = "dsReShowRoutes";
  public static final String dsReStateMode = "dsReStateMode";
  public static final String dsReStateModeDefault = "stateful";
  public static final String dsReStateMode_stateful = "stateful";
  public static final int index_dsReStateMode_stateful = 0;
  public static final String dsReStateMode_stateless = "stateless";
  public static final int index_dsReStateMode_stateless = 1;
  public static final String dsReStateMode_failover_stateful = "failover-stateful";
  public static final int index_dsReStateMode_failover_stateful = 2;
  public static final String dsReXCLDebug = "dsReXCLDebug";
  public static final String dsReXCLTest = "dsReXCLTest";
  public static final String dsReAccountingSipHdr = "dsReAccountingSipHdr";
  public static final String reAccountingSipHdrName = "reAccountingSipHdrName";
  public static final String reAccountingSipHdrSource = "reAccountingSipHdrSource";
  public static final String reAccountingSipHdrSourceDefault = "RESP";
  public static final String reAccountingSipHdrSource_REQ = "REQ";
  public static final int index_reAccountingSipHdrSource_REQ = 0;
  public static final String reAccountingSipHdrSource_RESP = "RESP";
  public static final int index_reAccountingSipHdrSource_RESP = 1;
  public static final String reAccountingSipHdrRowStatus = "reAccountingSipHdrRowStatus";
  public static final String dsReAddServiceRoute = "dsReAddServiceRoute";
  public static final String reAddServiceRouteDirection = "reAddServiceRouteDirection";
  public static final String reAddServiceRouteAddress = "reAddServiceRouteAddress";
  public static final String reAddServiceRouteTransport = "reAddServiceRouteTransport";
  public static final String reAddServiceRouteTransport_UDP = "UDP";
  public static final int index_reAddServiceRouteTransport_UDP =
      DsSipTransportType.getTypeAsInt(reAddServiceRouteTransport_UDP);
  public static final String reAddServiceRouteTransport_TCP = "TCP";
  public static final int index_reAddServiceRouteTransport_TCP =
      DsSipTransportType.getTypeAsInt(reAddServiceRouteTransport_TCP);
  public static final String reAddServiceRouteTransport_TLS = "TLS";
  public static final int index_reAddServiceRouteTransport_TLS =
      DsSipTransportType.getTypeAsInt(reAddServiceRouteTransport_TLS);
  public static final String reAddServiceRouteSequence = "reAddServiceRouteSequence";
  public static final int reAddServiceRouteSequenceMin = 1;
  public static final int reAddServiceRouteSequenceMax = 10;
  public static final String reAddServiceRoutePort = "reAddServiceRoutePort";
  public static final int reAddServiceRoutePortMin = 0;
  public static final int reAddServiceRoutePortMax = 65535;
  public static final String reAddServiceRouteParams = "reAddServiceRouteParams";
  public static final String reAddServiceRouteRowStatus = "reAddServiceRouteRowStatus";
  public static final String dsReApplicationConfig = "dsReApplicationConfig";
  public static final String reApplicationName = "reApplicationName";
  public static final String reApplicationSg = "reApplicationSg";
  public static final String reReturnApplicationSg = "reReturnApplicationSg";
  public static final String reApplicationResource = "reApplicationResource";
  public static final String reApplicationConfigRowStatus = "reApplicationConfigRowStatus";
  public static final String dsReEnumRoot = "dsReEnumRoot";
  public static final String reEnumRootName = "reEnumRootName";
  public static final String reEnumRootTimeout = "reEnumRootTimeout";
  public static final int reEnumRootTimeoutDefault = 1000;
  public static final int reEnumRootTimeoutMin = 0;
  public static final String reEnumRootAttempts = "reEnumRootAttempts";
  public static final int reEnumRootAttemptsDefault = 1;
  public static final int reEnumRootAttemptsMin = 0;
  public static final String reEnumRootRecurse = "reEnumRootRecurse";
  public static final String reEnumRootRecurseDefault = "false";
  public static final String reEnumRootRecurse_true = "true";
  public static final int index_reEnumRootRecurse_true = 0;
  public static final String reEnumRootRecurse_false = "false";
  public static final int index_reEnumRootRecurse_false = 1;
  public static final String reEnumRootExponentialBackOff = "reEnumRootExponentialBackOff";
  public static final String reEnumRootExponentialBackOffDefault = "false";
  public static final String reEnumRootExponentialBackOff_true = "true";
  public static final int index_reEnumRootExponentialBackOff_true = 0;
  public static final String reEnumRootExponentialBackOff_false = "false";
  public static final int index_reEnumRootExponentialBackOff_false = 1;
  public static final String reEnumRootRowStatus = "reEnumRootRowStatus";
  public static final String dsReEnumServer = "dsReEnumServer";
  public static final String reEnumServerRoot = "reEnumServerRoot";
  public static final String reEnumServerIp = "reEnumServerIp";
  public static final String reEnumServerPort = "reEnumServerPort";
  public static final int reEnumServerPortDefault = 53;
  public static final int reEnumServerPortMin = 1;
  public static final int reEnumServerPortMax = 65535;
  public static final String reEnumServerQValue = "reEnumServerQValue";
  public static final float reEnumServerQValueDefault = 1.0f;
  public static final float reEnumServerQValueMin = 0.0f;
  public static final float reEnumServerQValueMax = 1.0f;
  public static final String reEnumServerRowStatus = "reEnumServerRowStatus";
  public static final String dsReFirewallGroup = "dsReFirewallGroup";
  public static final String reFirewallGroupGroupID = "reFirewallGroupGroupID";
  public static final int reFirewallGroupGroupIDMin = 1;
  public static final String reFirewallGroupDomain = "reFirewallGroupDomain";
  public static final String reFirewallGroupDomainDefault = "default";
  public static final String reFirewallGroupDomain_default = "default";
  public static final int index_reFirewallGroupDomain_default = 0;
  public static final String reFirewallGroupRowStatus = "reFirewallGroupRowStatus";
  public static final String dsReFirewallQueue = "dsReFirewallQueue";
  public static final String reFirewallQueueSize = "reFirewallQueueSize";
  public static final int reFirewallQueueSizeDefault = 300;
  public static final int reFirewallQueueSizeMin = 50;
  public static final String reFirewallQueueBusyReset = "reFirewallQueueBusyReset";
  public static final int reFirewallQueueBusyResetDefault = 80;
  public static final int reFirewallQueueBusyResetMin = 1;
  public static final int reFirewallQueueBusyResetMax = 100;
  public static final String reFirewallQueueMaxThreads = "reFirewallQueueMaxThreads";
  public static final int reFirewallQueueMaxThreadsDefault = 12;
  public static final int reFirewallQueueMaxThreadsMin = 1;
  public static final String reFirewallQueueRowStatus = "reFirewallQueueRowStatus";
  public static final String reFirewallQueueRowStatusDefault = "on";
  public static final String reFirewallQueueRowStatus_on = "on";
  public static final int index_reFirewallQueueRowStatus_on = 0;
  public static final String reFirewallQueueRowStatus_off = "off";
  public static final int index_reFirewallQueueRowStatus_off = 1;
  public static final String dsReFirewallTimeout = "dsReFirewallTimeout";
  public static final String reFirewallTimeoutSessionTimeout = "reFirewallTimeoutSessionTimeout";
  public static final int reFirewallTimeoutSessionTimeoutDefault = 1800;
  public static final int reFirewallTimeoutSessionTimeoutMin = 0;
  public static final String reFirewallTimeoutSessionClose = "reFirewallTimeoutSessionClose";
  public static final int reFirewallTimeoutSessionCloseDefault = 1;
  public static final String reFirewallTimeoutRowStatus = "reFirewallTimeoutRowStatus";
  public static final String reFirewallTimeoutRowStatusDefault = "on";
  public static final String reFirewallTimeoutRowStatus_on = "on";
  public static final int index_reFirewallTimeoutRowStatus_on = 0;
  public static final String reFirewallTimeoutRowStatus_off = "off";
  public static final int index_reFirewallTimeoutRowStatus_off = 1;
  public static final String dsReListen = "dsReListen";
  public static final String reListenInterface = "reListenInterface";
  public static final String reListenDirection = "reListenDirection";
  public static final String reListenPort = "reListenPort";
  public static final int reListenPortDefault = 5060;
  public static final int reListenPortMin = 0;
  public static final int reListenPortMax = 65535;
  public static final String reListenTransport = "reListenTransport";
  public static final String reListenTransportDefault = "UDP";
  public static final String reListenTransport_UDP = "UDP";
  public static final int index_reListenTransport_UDP =
      DsSipTransportType.getTypeAsInt(reListenTransport_UDP);
  public static final String reListenTransport_TCP = "TCP";
  public static final int index_reListenTransport_TCP =
      DsSipTransportType.getTypeAsInt(reListenTransport_TCP);
  public static final String reListenTransport_TLS = "TLS";
  public static final int index_reListenTransport_TLS =
      DsSipTransportType.getTypeAsInt(reListenTransport_TLS);
  public static final String reListenRowStatus = "reListenRowStatus";
  public static final String dsReLoadXCL = "dsReLoadXCL";
  public static final String reLoadXCLScriptName = "reLoadXCLScriptName";
  public static final String reLoadXCLScriptFile = "reLoadXCLScriptFile";
  public static final String reLoadXCLRowStatus = "reLoadXCLRowStatus";
  public static final String dsReMask = "dsReMask";
  public static final String reMaskDirection = "reMaskDirection";
  public static final String reMaskHeader = "reMaskHeader";
  public static final String reMaskHeaderDefault = "via";
  public static final String reMaskHeader_via = "via";
  public static final int index_reMaskHeader_via = 0;
  public static final String reMaskRowStatus = "reMaskRowStatus";
  public static final String dsReMaxPinholeBandwidth = "dsReMaxPinholeBandwidth";
  public static final String reMaxPinholeBandwidthBits = "reMaxPinholeBandwidthBits";
  public static final long reMaxPinholeBandwidthBitsDefault = 1200;
  public static final long reMaxPinholeBandwidthBitsMin = 0;
  public static final String reMaxPinholeBandwidthCodec = "reMaxPinholeBandwidthCodec";
  public static final String reMaxPinholeBandwidthRowStatus = "reMaxPinholeBandwidthRowStatus";
  public static final String dsReModifyServiceRoute = "dsReModifyServiceRoute";
  public static final String reModifyServiceRouteDirection = "reModifyServiceRouteDirection";
  public static final String reModifyServiceRouteAddress = "reModifyServiceRouteAddress";
  public static final String reModifyServiceRouteTransport = "reModifyServiceRouteTransport";
  public static final String reModifyServiceRouteTransport_UDP = "UDP";
  public static final int index_reModifyServiceRouteTransport_UDP =
      DsSipTransportType.getTypeAsInt(reModifyServiceRouteTransport_UDP);
  public static final String reModifyServiceRouteTransport_TCP = "TCP";
  public static final int index_reModifyServiceRouteTransport_TCP =
      DsSipTransportType.getTypeAsInt(reModifyServiceRouteTransport_TCP);
  public static final String reModifyServiceRouteTransport_TLS = "TLS";
  public static final int index_reModifyServiceRouteTransport_TLS =
      DsSipTransportType.getTypeAsInt(reModifyServiceRouteTransport_TLS);
  public static final String reModifyServiceRoutePort = "reModifyServiceRoutePort";
  public static final int reModifyServiceRoutePortMin = 0;
  public static final int reModifyServiceRoutePortMax = 65535;
  public static final String reModifyServiceRouteRowStatus = "reModifyServiceRouteRowStatus";
  public static final String dsReModuleTrigger = "dsReModuleTrigger";
  public static final String reModuleTriggerModuleName = "reModuleTriggerModuleName";
  public static final String reModuleTriggerModuleName_authentication = "authentication";
  public static final int index_reModuleTriggerModuleName_authentication = 0;
  public static final String reModuleTriggerModuleName_default_route = "default-route";
  public static final int index_reModuleTriggerModuleName_default_route = 1;
  public static final String reModuleTriggerModuleName_filter_in_request = "filter-in-request";
  public static final int index_reModuleTriggerModuleName_filter_in_request = 2;
  public static final String reModuleTriggerModuleName_filter_out_request = "filter-out-request";
  public static final int index_reModuleTriggerModuleName_filter_out_request = 3;
  public static final String reModuleTriggerModuleName_filter_out_response = "filter-out-response";
  public static final int index_reModuleTriggerModuleName_filter_out_response = 4;
  public static final String reModuleTriggerModuleName_identity_assertion = "identity-assertion";
  public static final int index_reModuleTriggerModuleName_identity_assertion = 5;
  public static final String reModuleTriggerModuleName_network_direction = "network-direction";
  public static final int index_reModuleTriggerModuleName_network_direction = 6;
  public static final String reModuleTriggerModuleName_privacy = "privacy";
  public static final int index_reModuleTriggerModuleName_privacy = 7;
  public static final String reModuleTriggerModuleName_record_route_param = "record-route-param";
  public static final int index_reModuleTriggerModuleName_record_route_param = 8;
  public static final String reModuleTriggerModuleName_client_type = "client-type";
  public static final int index_reModuleTriggerModuleName_client_type = 9;
  public static final String reModuleTriggerModuleName_pcmm_orig_req = "pcmm-orig-req";
  public static final int index_reModuleTriggerModuleName_pcmm_orig_req = 10;
  public static final String reModuleTriggerModuleName_pcmm_orig_resp = "pcmm-orig-resp";
  public static final int index_reModuleTriggerModuleName_pcmm_orig_resp = 11;
  public static final String reModuleTriggerModuleName_pcmm_params = "pcmm-params";
  public static final int index_reModuleTriggerModuleName_pcmm_params = 12;
  public static final String reModuleTriggerModuleName_pcmm_term_req = "pcmm-term-req";
  public static final int index_reModuleTriggerModuleName_pcmm_term_req = 13;
  public static final String reModuleTriggerModuleName_pcmm_term_resp = "pcmm-term-resp";
  public static final int index_reModuleTriggerModuleName_pcmm_term_resp = 14;
  public static final String reModuleTriggerModuleName_routing = "routing";
  public static final int index_reModuleTriggerModuleName_routing = 15;
  public static final String reModuleTriggerModuleName_routing_classification =
      "routing-classification";
  public static final int index_reModuleTriggerModuleName_routing_classification = 16;
  public static final String reModuleTriggerModuleName_post_normalize = "post-normalize";
  public static final int index_reModuleTriggerModuleName_post_normalize = 17;
  public static final String reModuleTriggerModuleName_pre_normalize = "pre-normalize";
  public static final int index_reModuleTriggerModuleName_pre_normalize = 18;
  public static final String reModuleTriggerModuleName_radius_server_request =
      "radius-server-request";
  public static final int index_reModuleTriggerModuleName_radius_server_request = 19;
  public static final String reModuleTriggerModuleName_radius_server_response =
      "radius-server-response";
  public static final int index_reModuleTriggerModuleName_radius_server_response = 20;
  public static final String reModuleTriggerModuleName_radius_client_request =
      "radius-client-request";
  public static final int index_reModuleTriggerModuleName_radius_client_request = 21;
  public static final String reModuleTriggerModuleName_radius_client_response =
      "radius-client-response";
  public static final int index_reModuleTriggerModuleName_radius_client_response = 22;
  public static final String reModuleTriggerModuleName_N11 = "N11";
  public static final int index_reModuleTriggerModuleName_N11 = 23;
  public static final String reModuleTriggerModuleName_privacy_in = "privacy-in";
  public static final int index_reModuleTriggerModuleName_privacy_in = 24;
  public static final String reModuleTriggerModuleName_privacy_out = "privacy-out";
  public static final int index_reModuleTriggerModuleName_privacy_out = 25;
  public static final String reModuleTriggerSequenceNo = "reModuleTriggerSequenceNo";
  public static final int reModuleTriggerSequenceNoMin = 0;
  public static final String reModuleTriggerTriggerName = "reModuleTriggerTriggerName";
  public static final String reModuleTriggerTriggerNameDefault = "default";
  public static final String reModuleTriggerTriggerName_default = "default";
  public static final int index_reModuleTriggerTriggerName_default = 0;
  public static final String reModuleTriggerAction = "reModuleTriggerAction";
  public static final String reModuleTriggerAction_add = "add";
  public static final int index_reModuleTriggerAction_add = 0;
  public static final String reModuleTriggerAction_set = "set";
  public static final int index_reModuleTriggerAction_set = 1;
  public static final String reModuleTriggerAction_reserve = "reserve";
  public static final int index_reModuleTriggerAction_reserve = 2;
  public static final String reModuleTriggerAction_release = "release";
  public static final int index_reModuleTriggerAction_release = 3;
  public static final String reModuleTriggerAction_cancel = "cancel";
  public static final int index_reModuleTriggerAction_cancel = 4;
  public static final String reModuleTriggerAction_commit = "commit";
  public static final int index_reModuleTriggerAction_commit = 5;
  public static final String reModuleTriggerAction_assert_pass_through = "assert-pass-through";
  public static final int index_reModuleTriggerAction_assert_pass_through = 6;
  public static final String reModuleTriggerAction_assert_reject = "assert-reject";
  public static final int index_reModuleTriggerAction_assert_reject = 7;
  public static final String reModuleTriggerAction_auth_pass_through = "auth-pass-through";
  public static final int index_reModuleTriggerAction_auth_pass_through = 8;
  public static final String reModuleTriggerAction_auth_reject = "auth-reject";
  public static final int index_reModuleTriggerAction_auth_reject = 9;
  public static final String reModuleTriggerAction_call_type = "call-type";
  public static final int index_reModuleTriggerAction_call_type = 10;
  public static final String reModuleTriggerAction_dsedge_auth = "dsedge_auth";
  public static final int index_reModuleTriggerAction_dsedge_auth = 11;
  public static final String reModuleTriggerAction_dsedge_outbound = "dsedge_outbound";
  public static final int index_reModuleTriggerAction_dsedge_outbound = 12;
  public static final String reModuleTriggerAction_identity_default = "identity-default";
  public static final int index_reModuleTriggerAction_identity_default = 13;
  public static final String reModuleTriggerAction_none = "none";
  public static final int index_reModuleTriggerAction_none = 14;
  public static final String reModuleTriggerAction_privacy_assert = "privacy-assert";
  public static final int index_reModuleTriggerAction_privacy_assert = 15;
  public static final String reModuleTriggerAction_privacy_none = "privacy-none";
  public static final int index_reModuleTriggerAction_privacy_none = 16;
  public static final String reModuleTriggerAction_privacy_service = "privacy-service";
  public static final int index_reModuleTriggerAction_privacy_service = 17;
  public static final String reModuleTriggerAction_remove_header = "remove-header";
  public static final int index_reModuleTriggerAction_remove_header = 18;
  public static final String reModuleTriggerAction_undetermined = "undetermined";
  public static final int index_reModuleTriggerAction_undetermined = 19;
  public static final String reModuleTriggerAction_sticky = "sticky";
  public static final int index_reModuleTriggerAction_sticky = 20;
  public static final String reModuleTriggerAction_non_sticky = "non-sticky";
  public static final int index_reModuleTriggerAction_non_sticky = 21;
  public static final String reModuleTriggerAction_911 = "911";
  public static final int index_reModuleTriggerAction_911 = 22;
  public static final String reModuleTriggerAction_911_esrn_cli = "911-esrn-cli";
  public static final int index_reModuleTriggerAction_911_esrn_cli = 23;
  public static final String reModuleTriggerAction_911_esrn_das = "911-esrn-das";
  public static final int index_reModuleTriggerAction_911_esrn_das = 24;
  public static final String reModuleTriggerAction_app_dispatch = "app-dispatch";
  public static final int index_reModuleTriggerAction_app_dispatch = 25;
  public static final String reModuleTriggerAction_radius_start = "radius-start";
  public static final int index_reModuleTriggerAction_radius_start = 26;
  public static final String reModuleTriggerAction_radius_stop = "radius-stop";
  public static final int index_reModuleTriggerAction_radius_stop = 27;
  public static final String reModuleTriggerAction_radius_stop_all_attributes =
      "radius-stop-all-attributes";
  public static final int index_reModuleTriggerAction_radius_stop_all_attributes = 28;
  public static final String reModuleTriggerAction_routing_classification =
      "routing-classification";
  public static final int index_reModuleTriggerAction_routing_classification = 29;
  public static final String reModuleTriggerAction_radius_interim = "radius-interim";
  public static final int index_reModuleTriggerAction_radius_interim = 30;
  public static final String reModuleTriggerActionParam = "reModuleTriggerActionParam";
  public static final String reModuleTriggerPolicy = "reModuleTriggerPolicy";
  public static final String reModuleTriggerRowStatus = "reModuleTriggerRowStatus";
  public static final String dsReNetscreenFirewall = "dsReNetscreenFirewall";
  public static final String reNetscreenFirewallIP = "reNetscreenFirewallIP";
  public static final String reNetscreenFirewallPort = "reNetscreenFirewallPort";
  public static final int reNetscreenFirewallPortMin = 1;
  public static final int reNetscreenFirewallPortMax = 65535;
  public static final String reNetscreenFirewallGroupID = "reNetscreenFirewallGroupID";
  public static final String reNetscreenFirewallRowStatus = "reNetscreenFirewallRowStatus";
  public static final String dsReNetwork = "dsReNetwork";
  public static final String reNetworkDirection = "reNetworkDirection";
  public static final String reNetworkType = "reNetworkType";
  public static final String reNetworkTypeDefault = "standard";
  public static final String reNetworkType_standard = "standard";
  public static final int index_reNetworkType_standard = 0;
  public static final String reNetworkType_icmp = "icmp";
  public static final int index_reNetworkType_icmp = 1;
  public static final String reNetworkType_noicmp = "noicmp";
  public static final int index_reNetworkType_noicmp = 2;
  public static final String reNetworkType_nat = "nat";
  public static final int index_reNetworkType_nat = 3;
  public static final String reNetworkType_sigcomp = "sigcomp";
  public static final int index_reNetworkType_sigcomp = 4;
  public static final String reNetworkType_toksip = "toksip";
  public static final int index_reNetworkType_toksip = 5;
  public static final String reNetworkConnection = "reNetworkConnection";
  public static final String reNetworkConnectionDefault = "true";
  public static final String reNetworkConnection_true = "true";
  public static final int index_reNetworkConnection_true = 0;
  public static final String reNetworkConnection_false = "false";
  public static final int index_reNetworkConnection_false = 1;
  public static final String reNetworkRowStatus = "reNetworkRowStatus";
  public static final String dsReNormalizationPolicy = "dsReNormalizationPolicy";
  public static final String reNormalizationPolicy = "reNormalizationPolicy";
  public static final String reNormalizationSequence = "reNormalizationSequence";
  public static final int reNormalizationSequenceDefault = 10;
  public static final int reNormalizationSequenceMin = 1;
  public static final int reNormalizationSequenceMax = 1000;
  public static final String reNormalizationHeader = "reNormalizationHeader";
  public static final String reNormalizationAction = "reNormalizationAction";
  public static final String reNormalizationAction_add = "add";
  public static final int index_reNormalizationAction_add = 0;
  public static final String reNormalizationAction_remove = "remove";
  public static final int index_reNormalizationAction_remove = 1;
  public static final String reNormalizationAction_remove_all = "remove-all";
  public static final int index_reNormalizationAction_remove_all = 2;
  public static final String reNormalizationAction_remove_all_except = "remove-all-except";
  public static final int index_reNormalizationAction_remove_all_except = 3;
  public static final String reNormalizationAction_replace = "replace";
  public static final int index_reNormalizationAction_replace = 4;
  public static final String reNormalizationAction_dest_uri_to_sip = "dest-uri-to-sip";
  public static final int index_reNormalizationAction_dest_uri_to_sip = 5;
  public static final String reNormalizationAction_dest_uri_to_tel = "dest-uri-to-tel";
  public static final int index_reNormalizationAction_dest_uri_to_tel = 6;
  public static final String reNormalizationMsgType = "reNormalizationMsgType";
  public static final String reNormalizationMsgTypeDefault = "request";
  public static final String reNormalizationMsgType_request = "request";
  public static final int index_reNormalizationMsgType_request = 0;
  public static final String reNormalizationMsgType_response = "response";
  public static final int index_reNormalizationMsgType_response = 1;
  public static final String reNormalizationMsgType_both = "both";
  public static final int index_reNormalizationMsgType_both = 2;
  public static final String reNormalizationMethod = "reNormalizationMethod";
  public static final String reNormalizationHdrInstance = "reNormalizationHdrInstance";
  public static final String reNormalizationHdrInstanceDefault = "all";
  public static final String reNormalizationHdrInstance_first = "first";
  public static final int index_reNormalizationHdrInstance_first = 0;
  public static final String reNormalizationHdrInstance_last = "last";
  public static final int index_reNormalizationHdrInstance_last = 1;
  public static final String reNormalizationHdrInstance_all = "all";
  public static final int index_reNormalizationHdrInstance_all = 2;
  public static final String reNormalizationParameter = "reNormalizationParameter";
  public static final String reNormalizationField = "reNormalizationField";
  public static final String reNormalizationField_hdr_parameter = "hdr-parameter";
  public static final int index_reNormalizationField_hdr_parameter = 0;
  public static final String reNormalizationField_uri_parameter = "uri-parameter";
  public static final int index_reNormalizationField_uri_parameter = 1;
  public static final String reNormalizationField_uri = "uri";
  public static final int index_reNormalizationField_uri = 2;
  public static final String reNormalizationActionParam = "reNormalizationActionParam";
  public static final String reNormalizationRegexMatch = "reNormalizationRegexMatch";
  public static final String reNormalizationRegexReplace = "reNormalizationRegexReplace";
  public static final String reNormalizationPolicyRowStatus = "reNormalizationPolicyRowStatus";
  public static final String dsRePath = "dsRePath";
  public static final String rePathAddress = "rePathAddress";
  public static final String rePathTransport = "rePathTransport";
  public static final String rePathTransport_UDP = "UDP";
  public static final int index_rePathTransport_UDP =
      DsSipTransportType.getTypeAsInt(rePathTransport_UDP);
  public static final String rePathTransport_TCP = "TCP";
  public static final int index_rePathTransport_TCP =
      DsSipTransportType.getTypeAsInt(rePathTransport_TCP);
  public static final String rePathTransport_TLS = "TLS";
  public static final int index_rePathTransport_TLS =
      DsSipTransportType.getTypeAsInt(rePathTransport_TLS);
  public static final String rePathDirection = "rePathDirection";
  public static final String rePathPort = "rePathPort";
  public static final int rePathPortMin = 0;
  public static final int rePathPortMax = 65535;
  public static final String rePathRowStatus = "rePathRowStatus";
  public static final String dsRePopId = "dsRePopId";
  public static final String rePrimaryPopId = "rePrimaryPopId";
  public static final String rePopIdRowStatus = "rePopIdRowStatus";
  public static final String rePopIdRowStatusDefault = "on";
  public static final String rePopIdRowStatus_on = "on";
  public static final int index_rePopIdRowStatus_on = 0;
  public static final String rePopIdRowStatus_off = "off";
  public static final int index_rePopIdRowStatus_off = 1;
  public static final String dsRePopNames = "dsRePopNames";
  public static final String rePopNamesName = "rePopNamesName";
  public static final String rePopNamesElementName = "rePopNamesElementName";
  public static final String rePopNamesRowStatus = "rePopNamesRowStatus";
  public static final String dsReQosSg = "dsReQosSg";
  public static final String reQosSgName = "reQosSgName";
  public static final String reQosSgRowStatus = "reQosSgRowStatus";
  public static final String dsReQosSgElement = "dsReQosSgElement";
  public static final String reQosSgElementSgName = "reQosSgElementSgName";
  public static final String reQosSgElementUri = "reQosSgElementUri";
  public static final String reQosSgElementConnectionPoolSize = "reQosSgElementConnectionPoolSize";
  public static final int reQosSgElementConnectionPoolSizeDefault = 20;
  public static final int reQosSgElementConnectionPoolSizeMin = 1;
  public static final int reQosSgElementConnectionPoolSizeMax = 1000;
  public static final String reQosSgElementSgReference = "reQosSgElementSgReference";
  public static final String reQosSgElementPipelineSize = "reQosSgElementPipelineSize";
  public static final int reQosSgElementPipelineSizeDefault = 25;
  public static final int reQosSgElementPipelineSizeMin = 1;
  public static final int reQosSgElementPipelineSizeMax = 25;
  public static final String reQosSgElementTimeout = "reQosSgElementTimeout";
  public static final int reQosSgElementTimeoutDefault = 1000;
  public static final int reQosSgElementTimeoutMin = 0;
  public static final String reQosSgElementQValue = "reQosSgElementQValue";
  public static final float reQosSgElementQValueDefault = 1.0f;
  public static final float reQosSgElementQValueMin = 0.0f;
  public static final float reQosSgElementQValueMax = 1.0f;
  public static final String reQosSgElementRowStatus = "reQosSgElementRowStatus";
  public static final String dsReRadiusSg = "dsReRadiusSg";
  public static final String reRadiusSgName = "reRadiusSgName";
  public static final String reRadiusSgSourceIP = "reRadiusSgSourceIP";
  public static final String reRadiusSgRowStatus = "reRadiusSgRowStatus";
  public static final String dsReRadiusSgElement = "dsReRadiusSgElement";
  public static final String reRadiusSgElementSgName = "reRadiusSgElementSgName";
  public static final String reRadiusSgElementHost = "reRadiusSgElementHost";
  public static final String reRadiusSgElementPort = "reRadiusSgElementPort";
  public static final int reRadiusSgElementPortDefault = 1813;
  public static final int reRadiusSgElementPortMin = 0;
  public static final int reRadiusSgElementPortMax = 65535;
  public static final String reRadiusSgElementSharedSecret = "reRadiusSgElementSharedSecret";
  public static final String reRadiusSgElementSgReference = "reRadiusSgElementSgReference";
  public static final String reRadiusSgElementRetxCount = "reRadiusSgElementRetxCount";
  public static final int reRadiusSgElementRetxCountDefault = 3;
  public static final int reRadiusSgElementRetxCountMin = 0;
  public static final String reRadiusSgElementRetxTimeout = "reRadiusSgElementRetxTimeout";
  public static final int reRadiusSgElementRetxTimeoutDefault = 500;
  public static final int reRadiusSgElementRetxTimeoutMin = 0;
  public static final String reRadiusSgElementQValue = "reRadiusSgElementQValue";
  public static final float reRadiusSgElementQValueDefault = 1.0f;
  public static final float reRadiusSgElementQValueMin = 0.0f;
  public static final float reRadiusSgElementQValueMax = 1.0f;
  public static final String reRadiusSgElementRowStatus = "reRadiusSgElementRowStatus";
  public static final String dsReRecordRoute = "dsReRecordRoute";
  public static final String reRecordRouteHost = "reRecordRouteHost";
  public static final String reRecordRouteTransport = "reRecordRouteTransport";
  public static final String reRecordRouteTransportDefault = "UDP";
  public static final String reRecordRouteTransport_UDP = "UDP";
  public static final int index_reRecordRouteTransport_UDP =
      DsSipTransportType.getTypeAsInt(reRecordRouteTransport_UDP);
  public static final String reRecordRouteTransport_TCP = "TCP";
  public static final int index_reRecordRouteTransport_TCP =
      DsSipTransportType.getTypeAsInt(reRecordRouteTransport_TCP);
  public static final String reRecordRouteTransport_TLS = "TLS";
  public static final int index_reRecordRouteTransport_TLS =
      DsSipTransportType.getTypeAsInt(reRecordRouteTransport_TLS);
  public static final String reRecordRouteDirection = "reRecordRouteDirection";
  public static final String reRecordRoutePort = "reRecordRoutePort";
  public static final int reRecordRoutePortMin = 1;
  public static final int reRecordRoutePortMax = 65535;
  public static final String reRecordRouteRowStatus = "reRecordRouteRowStatus";
  public static final String dsReRetransmitCount = "dsReRetransmitCount";
  public static final String reRetransmitCountType = "reRetransmitCountType";
  public static final String reRetransmitCountType_INVITE_CLIENT = "INVITE_CLIENT";
  public static final int index_reRetransmitCountType_INVITE_CLIENT = 0;
  public static final String reRetransmitCountType_CLIENT = "CLIENT";
  public static final int index_reRetransmitCountType_CLIENT = 1;
  public static final String reRetransmitCountType_INVITE_SERVER = "INVITE_SERVER";
  public static final int index_reRetransmitCountType_INVITE_SERVER = 2;
  public static final String reRetransmitCountValue = "reRetransmitCountValue";
  public static final int reRetransmitCountValueMin = 0;
  public static final int reRetransmitCountValueMax = 127;
  public static final String reRetransmitCountNetwork = "reRetransmitCountNetwork";
  public static final String reRetransmitCountRowStatus = "reRetransmitCountRowStatus";
  public static final String dsReRetransmitTimer = "dsReRetransmitTimer";
  public static final String reRetransmitTimerType = "reRetransmitTimerType";
  public static final String reRetransmitTimerType_T1 = "T1";
  public static final int index_reRetransmitTimerType_T1 = 0;
  public static final String reRetransmitTimerType_T2 = "T2";
  public static final int index_reRetransmitTimerType_T2 = 1;
  public static final String reRetransmitTimerType_T4 = "T4";
  public static final int index_reRetransmitTimerType_T4 = 2;
  public static final String reRetransmitTimerType_TU1 = "TU1";
  public static final int index_reRetransmitTimerType_TU1 = 3;
  public static final String reRetransmitTimerType_TU2 = "TU2";
  public static final int index_reRetransmitTimerType_TU2 = 4;
  public static final String reRetransmitTimerType_TU3 = "TU3";
  public static final int index_reRetransmitTimerType_TU3 = 5;
  public static final String reRetransmitTimerType_serverTn = "serverTn";
  public static final int index_reRetransmitTimerType_serverTn = 6;
  public static final String reRetransmitTimerType_clientTn = "clientTn";
  public static final int index_reRetransmitTimerType_clientTn = 7;
  public static final String reRetransmitTimerValue = "reRetransmitTimerValue";
  public static final int reRetransmitTimerValueMin = 1;
  public static final int reRetransmitTimerValueMax = 2147483647;
  public static final String reRetransmitTimerNetwork = "reRetransmitTimerNetwork";
  public static final String reRetransmitTimerRowStatus = "reRetransmitTimerRowStatus";
  public static final String dsReRoute = "dsReRoute";
  public static final String reRouteTable = "reRouteTable";
  public static final String reRouteKey = "reRouteKey";
  public static final String reRouteOutput = "reRouteOutput";
  public static final String reRouteServerGroup = "reRouteServerGroup";
  public static final String reRouteVariables = "reRouteVariables";
  public static final String reRoutePolicyAdvance = "reRoutePolicyAdvance";
  public static final String reRouteGroup = "reRouteGroup";
  public static final String reRouteRowStatus = "reRouteRowStatus";
  public static final String dsReRouteElement = "dsReRouteElement";
  public static final String reRouteElementGroupName = "reRouteElementGroupName";
  public static final String reRouteElementRuri = "reRouteElementRuri";
  public static final String reRouteElementRoute = "reRouteElementRoute";
  public static final String reRouteElementNetwork = "reRouteElementNetwork";
  public static final String reRouteElementFailover = "reRouteElementFailover";
  public static final String reRouteElementFailoverDefault = "502,503";
  public static final String reRouteElementQValue = "reRouteElementQValue";
  public static final float reRouteElementQValueDefault = 1.0f;
  public static final float reRouteElementQValueMin = 0.0f;
  public static final float reRouteElementQValueMax = 1.0f;
  public static final String reRouteElementWeight = "reRouteElementWeight";
  public static final String reRouteElementTPolicy = "reRouteElementTPolicy";
  public static final String reRouteElementRowStatus = "reRouteElementRowStatus";
  public static final String MRS_DOMAIN_URI = "mrsDomainURI";
  public static final String ROUTE_CHOICE = "routeChoice";
  public static final String LOOKUP_BODY = "lookupBody";
  public static final String dsReRouteGroup = "dsReRouteGroup";
  public static final String reRouteGroupName = "reRouteGroupName";
  public static final String reRouteGroupTimeOfDayFlag = "reRouteGroupTimeOfDayFlag";
  public static final String reRouteGroupWeightBasedLoadBalancingFlag =
      "reRouteGroupWeightBasedLoadBalancingFlag";
  public static final String reRouteGroupRowStatus = "reRouteGroupRowStatus";
  public static final String dsReRoutePolicy = "dsReRoutePolicy";
  public static final String reRoutePolicy = "reRoutePolicy";
  public static final String reRouteSequence = "reRouteSequence";
  public static final int reRouteSequenceDefault = 10;
  public static final int reRouteSequenceMin = 1;
  public static final int reRouteSequenceMax = 1000;
  public static final String reRouteEnum = "reRouteEnum";
  public static final String reRouteEnum_y = "y";
  public static final int index_reRouteEnum_y = 0;
  public static final String reRouteEnum_yes = "yes";
  public static final int index_reRouteEnum_yes = 1;
  public static final String reRouteLookup = "reRouteLookup";
  public static final String reRouteLookup_y = "y";
  public static final int index_reRouteLookup_y = 0;
  public static final String reRouteLookup_yes = "yes";
  public static final int index_reRouteLookup_yes = 1;
  public static final String reRouteNrsMap = "reRouteNrsMap";
  public static final String reRouteNrsMap_y = "y";
  public static final int index_reRouteNrsMap_y = 0;
  public static final String reRouteNrsMap_yes = "yes";
  public static final int index_reRouteNrsMap_yes = 1;
  public static final String reRouteEnumTable = "reRouteEnumTable";
  public static final String reRouteLookupTable = "reRouteLookupTable";
  public static final String reRouteNrsMapTable = "reRouteNrsMapTable";
  public static final String reRouteEnumField = "reRouteEnumField";
  public static final String reRouteEnumField_ruri = "ruri";
  public static final int index_reRouteEnumField_ruri = 0;
  public static final String reRouteEnumField_ruri_phone = "ruri_phone";
  public static final int index_reRouteEnumField_ruri_phone = 1;
  public static final String reRouteEnumField_ruri_lrn = "ruri_lrn";
  public static final int index_reRouteEnumField_ruri_lrn = 2;
  public static final String reRouteEnumField_ruri_cic = "ruri_cic";
  public static final int index_reRouteEnumField_ruri_cic = 3;
  public static final String reRouteEnumField_diversion = "diversion";
  public static final int index_reRouteEnumField_diversion = 4;
  public static final String reRouteEnumField_diversion_phone = "diversion_phone";
  public static final int index_reRouteEnumField_diversion_phone = 5;
  public static final String reRouteEnumField_diversion_lrn = "diversion_lrn";
  public static final int index_reRouteEnumField_diversion_lrn = 6;
  public static final String reRouteEnumField_diversion_cic = "diversion_cic";
  public static final int index_reRouteEnumField_diversion_cic = 7;
  public static final String reRouteEnumField_paid = "paid";
  public static final int index_reRouteEnumField_paid = 8;
  public static final String reRouteEnumField_paid_phone = "paid_phone";
  public static final int index_reRouteEnumField_paid_phone = 9;
  public static final String reRouteEnumField_paid_lrn = "paid_lrn";
  public static final int index_reRouteEnumField_paid_lrn = 10;
  public static final String reRouteEnumField_paid_cic = "paid_cic";
  public static final int index_reRouteEnumField_paid_cic = 11;
  public static final String reRouteEnumField_rpid = "rpid";
  public static final int index_reRouteEnumField_rpid = 12;
  public static final String reRouteEnumField_rpid_phone = "rpid_phone";
  public static final int index_reRouteEnumField_rpid_phone = 13;
  public static final String reRouteEnumField_rpid_lrn = "rpid_lrn";
  public static final int index_reRouteEnumField_rpid_lrn = 14;
  public static final String reRouteEnumField_rpid_cic = "rpid_cic";
  public static final int index_reRouteEnumField_rpid_cic = 15;
  public static final String reRouteEnumField_from = "from";
  public static final int index_reRouteEnumField_from = 16;
  public static final String reRouteEnumField_from_phone = "from_phone";
  public static final int index_reRouteEnumField_from_phone = 17;
  public static final String reRouteEnumField_from_lrn = "from_lrn";
  public static final int index_reRouteEnumField_from_lrn = 18;
  public static final String reRouteEnumField_from_cic = "from_cic";
  public static final int index_reRouteEnumField_from_cic = 19;
  public static final String reRouteEnumField_originating_uri = "originating_uri";
  public static final int index_reRouteEnumField_originating_uri = 20;
  public static final String reRouteEnumField_ruri_routing_number = "ruri_routing_number";
  public static final int index_reRouteEnumField_ruri_routing_number = 21;
  public static final String reRouteEnumField_diversion_routing_number = "diversion_routing_number";
  public static final int index_reRouteEnumField_diversion_routing_number = 22;
  public static final String reRouteEnumField_paid_routing_number = "paid_routing_number";
  public static final int index_reRouteEnumField_paid_routing_number = 23;
  public static final String reRouteEnumField_rpid_routing_number = "rpid_routing_number";
  public static final int index_reRouteEnumField_rpid_routing_number = 24;
  public static final String reRouteEnumField_from_routing_number = "from_routing_number";
  public static final int index_reRouteEnumField_from_routing_number = 25;
  public static final String reRouteLookupField = "reRouteLookupField";
  public static final String reRouteLookupField_ruri = "ruri";
  public static final int index_reRouteLookupField_ruri = 0;
  public static final String reRouteLookupField_ruri_phone = "ruri_phone";
  public static final int index_reRouteLookupField_ruri_phone = 1;
  public static final String reRouteLookupField_ruri_user = "ruri_user";
  public static final String reRouteLookupField_ruri_lrn = "ruri_lrn";
  public static final int index_reRouteLookupField_ruri_lrn = 2;
  public static final String reRouteLookupField_ruri_cic = "ruri_cic";
  public static final int index_reRouteLookupField_ruri_cic = 3;
  public static final String reRouteLookupField_ruri_host = "ruri_host";
  public static final int index_reRouteLookupField_ruri_host = 4;
  public static final String reRouteLookupField_diversion = "diversion";
  public static final int index_reRouteLookupField_diversion = 5;
  public static final String reRouteLookupField_diversion_phone = "diversion_phone";
  public static final int index_reRouteLookupField_diversion_phone = 6;
  public static final String reRouteLookupField_diversion_user = "diversion_user";
  public static final String reRouteLookupField_diversion_lrn = "diversion_lrn";
  public static final int index_reRouteLookupField_diversion_lrn = 7;
  public static final String reRouteLookupField_diversion_cic = "diversion_cic";
  public static final int index_reRouteLookupField_diversion_cic = 8;
  public static final String reRouteLookupField_diversion_host = "diversion_host";
  public static final int index_reRouteLookupField_diversion_host = 9;
  public static final String reRouteLookupField_paid = "paid";
  public static final int index_reRouteLookupField_paid = 10;
  public static final String reRouteLookupField_paid_phone = "paid_phone";
  public static final int index_reRouteLookupField_paid_phone = 11;
  public static final String reRouteLookupField_paid_user = "paid_user";
  public static final String reRouteLookupField_paid_lrn = "paid_lrn";
  public static final int index_reRouteLookupField_paid_lrn = 12;
  public static final String reRouteLookupField_paid_cic = "paid_cic";
  public static final int index_reRouteLookupField_paid_cic = 13;
  public static final String reRouteLookupField_paid_host = "paid_host";
  public static final int index_reRouteLookupField_paid_host = 14;
  public static final String reRouteLookupField_rpid = "rpid";
  public static final int index_reRouteLookupField_rpid = 15;
  public static final String reRouteLookupField_rpid_phone = "rpid_phone";
  public static final int index_reRouteLookupField_rpid_phone = 16;
  public static final String reRouteLookupField_rpid_user = "rpid_user";
  public static final String reRouteLookupField_rpid_lrn = "rpid_lrn";
  public static final int index_reRouteLookupField_rpid_lrn = 17;
  public static final String reRouteLookupField_rpid_cic = "rpid_cic";
  public static final int index_reRouteLookupField_rpid_cic = 18;
  public static final String reRouteLookupField_rpid_host = "rpid_host";
  public static final int index_reRouteLookupField_rpid_host = 19;
  public static final String reRouteLookupField_from = "from";
  public static final int index_reRouteLookupField_from = 20;
  public static final String reRouteLookupField_from_phone = "from_phone";
  public static final int index_reRouteLookupField_from_phone = 21;
  public static final String reRouteLookupField_from_user = "from_user";
  public static final String reRouteLookupField_from_lrn = "from_lrn";
  public static final int index_reRouteLookupField_from_lrn = 22;
  public static final String reRouteLookupField_from_cic = "from_cic";
  public static final int index_reRouteLookupField_from_cic = 23;
  public static final String reRouteLookupField_from_host = "from_host";
  public static final int index_reRouteLookupField_from_host = 24;
  public static final String reRouteLookupField_originating_uri = "originating_uri";
  public static final int index_reRouteLookupField_originating_uri = 25;
  public static final String reRouteLookupField_ruri_routing_number = "ruri_routing_number";
  public static final int index_reRouteLookupField_ruri_routing_number = 26;
  public static final String reRouteLookupField_diversion_routing_number =
      "diversion_routing_number";
  public static final int index_reRouteLookupField_diversion_routing_number = 27;
  public static final String reRouteLookupField_paid_routing_number = "paid_routing_number";
  public static final int index_reRouteLookupField_paid_routing_number = 28;
  public static final String reRouteLookupField_rpid_routing_number = "rpid_routing_number";
  public static final int index_reRouteLookupField_rpid_routing_number = 29;
  public static final String reRouteLookupField_from_routing_number = "from_routing_number";
  public static final int index_reRouteLookupField_from_routing_number = 30;
  public static final String reRouteLookupField_uri_param = "uri_param";
  public static final int index_reRouteLookupField_uri_param = 31;
  public static final String reRouteLookupField_in_network = "in_network";
  public static final int index_reRouteLookupField_in_network = 32;
  public static final String reRouteLookupField_local_ip = "local_ip";
  public static final int index_reRouteLookupField_local_ip = 33;
  public static final String reRouteLookupField_local_port = "local_port";
  public static final int index_reRouteLookupField_local_port = 34;
  public static final String reRouteLookupField_remote_ip = "remote_ip";
  public static final int index_reRouteLookupField_remote_ip = 35;
  public static final String reRouteLookupField_remote_port = "remote_port";
  public static final int index_reRouteLookupField_remote_port = 36;
  public static final String reRouteLookupField_local_ip_port = "local_ip_port";
  public static final int index_reRouteLookupField_local_ip_port = 37;
  public static final String reRouteLookupField_remote_ip_port = "remote_ip_port";
  public static final int index_reRouteLookupField_remote_ip_port = 38;
  public static final String reRouteLookupField_routing_classification = "routing_classification";
  public static final int index_reRouteLookupField_routing_classification = 39;
  public static final String reRouteLookupField_ruri_host_port = "ruri_host_port";
  public static final int index_reRouteLookupField_ruri_host_port = 40;
  public static final String reRouteLookupField_from_host_port = "from_host_port";
  public static final int index_reRouteLookupField_from_host_port = 41;
  public static final String reRouteLookupField_paid_host_port = "paid_host_port";
  public static final int index_reRouteLookupField_paid_host_port = 42;
  public static final String reRouteLookupField_rpid_host_port = "rpid_host_port";
  public static final int index_reRouteLookupField_rpid_host_port = 43;
  public static final String reRouteLookupField_diversion_host_port = "diversion_host_port";
  public static final int index_reRouteLookupField_diversion_host_port = 44;
  public static final String reRouteLookupField_to = "to";
  public static final String reRouteLookupField_to_phone = "to_phone";
  public static final String reRouteLookupField_to_user = "to_user";
  public static final String reRouteLookupField_to_lrn = "to_lrn";
  public static final String reRouteLookupField_to_cic = "to_cic";
  public static final String reRouteLookupField_to_host = "to_host";
  public static final String reRouteLookupField_to_host_port = "to_host_port";
  public static final String reRouteLookupField_to_routing_number = "to_routing_number";

  public static final String reRouteNrsMapField = "reRouteNrsMapField";
  public static final String reRouteNrsMapField_ruri = "ruri";
  public static final int index_reRouteNrsMapField_ruri = 0;
  public static final String reRouteNrsMapField_ruri_phone = "ruri_phone";
  public static final int index_reRouteNrsMapField_ruri_phone = 1;
  public static final String reRouteNrsMapField_ruri_lrn = "ruri_lrn";
  public static final int index_reRouteNrsMapField_ruri_lrn = 2;
  public static final String reRouteNrsMapField_ruri_cic = "ruri_cic";
  public static final int index_reRouteNrsMapField_ruri_cic = 3;
  public static final String reRouteNrsMapField_diversion = "diversion";
  public static final int index_reRouteNrsMapField_diversion = 4;
  public static final String reRouteNrsMapField_diversion_phone = "diversion_phone";
  public static final int index_reRouteNrsMapField_diversion_phone = 5;
  public static final String reRouteNrsMapField_diversion_lrn = "diversion_lrn";
  public static final int index_reRouteNrsMapField_diversion_lrn = 6;
  public static final String reRouteNrsMapField_diversion_cic = "diversion_cic";
  public static final int index_reRouteNrsMapField_diversion_cic = 7;
  public static final String reRouteNrsMapField_paid = "paid";
  public static final int index_reRouteNrsMapField_paid = 8;
  public static final String reRouteNrsMapField_paid_phone = "paid_phone";
  public static final int index_reRouteNrsMapField_paid_phone = 9;
  public static final String reRouteNrsMapField_paid_lrn = "paid_lrn";
  public static final int index_reRouteNrsMapField_paid_lrn = 10;
  public static final String reRouteNrsMapField_paid_cic = "paid_cic";
  public static final int index_reRouteNrsMapField_paid_cic = 11;
  public static final String reRouteNrsMapField_rpid = "rpid";
  public static final int index_reRouteNrsMapField_rpid = 12;
  public static final String reRouteNrsMapField_rpid_phone = "rpid_phone";
  public static final int index_reRouteNrsMapField_rpid_phone = 13;
  public static final String reRouteNrsMapField_rpid_lrn = "rpid_lrn";
  public static final int index_reRouteNrsMapField_rpid_lrn = 14;
  public static final String reRouteNrsMapField_rpid_cic = "rpid_cic";
  public static final int index_reRouteNrsMapField_rpid_cic = 15;
  public static final String reRouteNrsMapField_from = "from";
  public static final int index_reRouteNrsMapField_from = 16;
  public static final String reRouteNrsMapField_from_phone = "from_phone";
  public static final int index_reRouteNrsMapField_from_phone = 17;
  public static final String reRouteNrsMapField_from_lrn = "from_lrn";
  public static final int index_reRouteNrsMapField_from_lrn = 18;
  public static final String reRouteNrsMapField_from_cic = "from_cic";
  public static final int index_reRouteNrsMapField_from_cic = 19;
  public static final String reRouteNrsMapField_originating_uri = "originating_uri";
  public static final int index_reRouteNrsMapField_originating_uri = 20;
  public static final String reRouteNrsMapField_ruri_routing_number = "ruri_routing_number";
  public static final int index_reRouteNrsMapField_ruri_routing_number = 21;
  public static final String reRouteNrsMapField_diversion_routing_number =
      "diversion_routing_number";
  public static final int index_reRouteNrsMapField_diversion_routing_number = 22;
  public static final String reRouteNrsMapField_paid_routing_number = "paid_routing_number";
  public static final int index_reRouteNrsMapField_paid_routing_number = 23;
  public static final String reRouteNrsMapField_rpid_routing_number = "rpid_routing_number";
  public static final int index_reRouteNrsMapField_rpid_routing_number = 24;
  public static final String reRouteNrsMapField_from_routing_number = "from_routing_number";
  public static final int index_reRouteNrsMapField_from_routing_number = 25;
  public static final String reRouteEnumRoot = "reRouteEnumRoot";
  public static final String reRouteEnumVoidResponse = "reRouteEnumVoidResponse";
  public static final int reRouteEnumVoidResponseMin = 300;
  public static final int reRouteEnumVoidResponseMax = 700;
  public static final String reRouteLookupRule = "reRouteLookupRule";
  public static final String reRouteLookupRule_exact = "exact";
  public static final int index_reRouteLookupRule_exact = 0;
  public static final String reRouteLookupRule_fixed = "fixed";
  public static final int index_reRouteLookupRule_fixed = 1;
  public static final String reRouteLookupRule_prefix = "prefix";
  public static final int index_reRouteLookupRule_prefix = 2;
  public static final String reRouteLookupRule_subdomain = "subdomain";
  public static final int index_reRouteLookupRule_subdomain = 3;
  public static final String reRouteLookupRule_subnet = "subnet";
  public static final int index_reRouteLookupRule_subnet = 4;
  public static final String reRouteLookupLength = "reRouteLookupLength";
  public static final int reRouteLookupLengthMin = 1;
  public static final int reRouteLookupLengthMax = 50;
  public static final String reRouteLookupKeyModifier = "reRouteLookupKeyModifier";
  public static final String reRouteLookupParamValue = "reRouteLookupParamValue";
  public static final String reRoutePolicyPolicyAdvance = "reRoutePolicyPolicyAdvance";
  public static final String reRoutePolicyRowStatus = "reRoutePolicyRowStatus";
  public static final String dsReRoutingPolicy = "dsReRoutingPolicy";
  public static final String reRoutingPolicy = "reRoutingPolicy";
  public static final String reRoutingSequence = "reRoutingSequence";
  public static final int reRoutingSequenceDefault = 10;
  public static final int reRoutingSequenceMin = 1;
  public static final int reRoutingSequenceMax = 1000;
  public static final String reRoutingEnum = "reRoutingEnum";
  public static final String reRoutingEnum_y = "y";
  public static final int index_reRoutingEnum_y = 0;
  public static final String reRoutingEnum_yes = "yes";
  public static final int index_reRoutingEnum_yes = 1;
  public static final String reRoutingLookup = "reRoutingLookup";
  public static final String reRoutingLookup_y = "y";
  public static final int index_reRoutingLookup_y = 0;
  public static final String reRoutingLookup_yes = "yes";
  public static final int index_reRoutingLookup_yes = 1;
  public static final String reRoutingNrsMap = "reRoutingNrsMap";
  public static final String reRoutingNrsMap_y = "y";
  public static final int index_reRoutingNrsMap_y = 0;
  public static final String reRoutingNrsMap_yes = "yes";
  public static final int index_reRoutingNrsMap_yes = 1;
  public static final String reRoutingEnumTable = "reRoutingEnumTable";
  public static final String reRoutingLookupTable = "reRoutingLookupTable";
  public static final String reRoutingNrsMapTable = "reRoutingNrsMapTable";
  public static final String reRoutingEnumField = "reRoutingEnumField";
  public static final String reRoutingEnumField_ruri_phone = "ruri_phone";
  public static final int index_reRoutingEnumField_ruri_phone = 0;
  public static final String reRoutingLookupField = "reRoutingLookupField";
  public static final String reRoutingLookupField_ruri_phone = "ruri_phone";
  public static final int index_reRoutingLookupField_ruri_phone = 0;
  public static final String reRoutingLookupField_ruri_lrn = "ruri_lrn";
  public static final int index_reRoutingLookupField_ruri_lrn = 1;
  public static final String reRoutingLookupField_ruri_host = "ruri_host";
  public static final int index_reRoutingLookupField_ruri_host = 2;
  public static final String reRoutingLookupField_ruri_cic = "ruri_cic";
  public static final int index_reRoutingLookupField_ruri_cic = 3;
  public static final String reRoutingLookupField_paid_phone = "paid_phone";
  public static final int index_reRoutingLookupField_paid_phone = 4;
  public static final String reRoutingLookupField_diversion_phone = "diversion_phone";
  public static final int index_reRoutingLookupField_diversion_phone = 5;
  public static final String reRoutingNrsMapField = "reRoutingNrsMapField";
  public static final String reRoutingNrsMapField_ruri = "ruri";
  public static final int index_reRoutingNrsMapField_ruri = 0;
  public static final String reRoutingEnumRoot = "reRoutingEnumRoot";
  public static final String reRoutingEnumVoidResponse = "reRoutingEnumVoidResponse";
  public static final int reRoutingEnumVoidResponseMin = 300;
  public static final int reRoutingEnumVoidResponseMax = 700;
  public static final String reRoutingLookupRule = "reRoutingLookupRule";
  public static final String reRoutingLookupRule_exact = "exact";
  public static final int index_reRoutingLookupRule_exact = 0;
  public static final String reRoutingLookupRule_fixed = "fixed";
  public static final int index_reRoutingLookupRule_fixed = 1;
  public static final String reRoutingLookupRule_prefix = "prefix";
  public static final int index_reRoutingLookupRule_prefix = 2;
  public static final String reRoutingLookupRule_subdomain = "subdomain";
  public static final int index_reRoutingLookupRule_subdomain = 3;
  public static final String reRoutingLookupRule_subnet = "subnet";
  public static final int index_reRoutingLookupRule_subnet = 4;
  public static final String reRoutingLookupLength = "reRoutingLookupLength";
  public static final int reRoutingLookupLengthMin = 1;
  public static final int reRoutingLookupLengthMax = 50;
  public static final String reRoutingLookupKeyModifier = "reRoutingLookupKeyModifier";
  public static final String reRoutingLookupKeyModifier_ci = "ci";
  public static final int index_reRoutingLookupKeyModifier_ci = 0;
  public static final String reRoutingLookupKeyModifier_ignore_plus = "ignore_plus";
  public static final int index_reRoutingLookupKeyModifier_ignore_plus = 1;
  public static final String reRoutingLookupKeyModifier_tel = "tel";
  public static final int index_reRoutingLookupKeyModifier_tel = 2;
  public static final String reRoutingPolicyRowStatus = "reRoutingPolicyRowStatus";
  public static final String dsReSaveRoutes = "dsReSaveRoutes";
  public static final String reSaveRoutesFilename = "reSaveRoutesFilename";
  public static final String reSaveRoutesTable = "reSaveRoutesTable";
  public static final String reSaveRoutesRowStatus = "reSaveRoutesRowStatus";
  public static final String dsReSaveServerGroups = "dsReSaveServerGroups";
  public static final String reSaveServerGroupsFilename = "reSaveServerGroupsFilename";
  public static final String reSaveServerGroupsSG = "reSaveServerGroupsSG";
  public static final String reSaveServerGroupsRowStatus = "reSaveServerGroupsRowStatus";
  public static final String dsReSetRootXCL = "dsReSetRootXCL";
  public static final String reSetRootXCLScriptName = "reSetRootXCLScriptName";
  public static final String reSetRootXCLRoute = "reSetRootXCLRoute";
  public static final String reSetRootXCLRouteDefault = "no";
  public static final String reSetRootXCLRoute_yes = "yes";
  public static final int index_reSetRootXCLRoute_yes = 0;
  public static final String reSetRootXCLRoute_no = "no";
  public static final int index_reSetRootXCLRoute_no = 1;
  public static final String dsReSg = "dsReSg";
  public static final String reSgName = "reSgName";
  public static final String reSgNetwork = "reSgNetwork";
  public static final String reSgLbType = "reSgLbType";
  public static final String reSgLbTypeDefault = "global";
  public static final String reSgLbType_global = "global";
  public static final int index_reSgLbType_global = 0;
  public static final String reSgLbType_highest_q = "highest-q";
  public static final int index_reSgLbType_highest_q = 1;
  public static final String reSgLbType_request_uri = "request-uri";
  public static final int index_reSgLbType_request_uri = 2;
  public static final String reSgLbType_call_id = "call-id";
  public static final int index_reSgLbType_call_id = 3;
  public static final String reSgLbType_to_uri = "to-uri";
  public static final int index_reSgLbType_to_uri = 4;
  public static final String reSgPing = "reSgPing";
  public static final String reSgPingDefault = "on";
  public static final String reSgPing_on = "on";
  public static final int index_reSgPing_on = 0;
  public static final String reSgPing_off = "off";
  public static final int index_reSgPing_off = 1;
  public static final String reSgRowStatus = "reSgRowStatus";
  public static final String dsReSgElement = "dsReSgElement";
  public static final String reSgElementSgName = "reSgElementSgName";
  public static final String reSgElementSgReference = "reSgElementSgReference";
  public static final String reSgElementHost = "reSgElementHost";
  public static final String reSgElementPort = "reSgElementPort";
  public static final int reSgElementPortDefault = 5060;
  public static final int reSgElementPortMin = 0;
  public static final int reSgElementPortMax = 65535;
  public static final String reSgElementTransport = "reSgElementTransport";
  public static final String reSgElementTransportDefault = "UDP";
  public static final String reSgElementTransport_UDP = "UDP";
  public static final int index_reSgElementTransport_UDP =
      DsSipTransportType.getTypeAsInt(reSgElementTransport_UDP);
  public static final String reSgElementTransport_TCP = "TCP";
  public static final int index_reSgElementTransport_TCP =
      DsSipTransportType.getTypeAsInt(reSgElementTransport_TCP);
  public static final String reSgElementTransport_TLS = "TLS";
  public static final int index_reSgElementTransport_TLS =
      DsSipTransportType.getTypeAsInt(reSgElementTransport_TLS);
  public static final String reSgElementQValue = "reSgElementQValue";
  public static final float reSgElementQValueDefault = 1.0f;
  public static final float reSgElementQValueMin = 0.0f;
  public static final float reSgElementQValueMax = 1.0f;
  public static final String reSgElementWeight = "reSgElementWeight";
  public static final String reSgElementRowStatus = "reSgElementRowStatus";
  public static final String dsReShutdown = "dsReShutdown";
  public static final String reShutdownResponse = "reShutdownResponse";
  public static final String reShutdownResponse_redirect = "redirect";
  public static final int index_reShutdownResponse_redirect = 0;
  public static final String reShutdownResponse_reject = "reject";
  public static final int index_reShutdownResponse_reject = 1;
  public static final String reShutdownTime = "reShutdownTime";
  public static final int reShutdownTimeMin = 0;
  public static final String reShutdownRetryAfter = "reShutdownRetryAfter";
  public static final int reShutdownRetryAfterDefault = 0;
  public static final int reShutdownRetryAfterMin = 0;
  public static final String reShutdownRedirectHost = "reShutdownRedirectHost";
  public static final String reShutdownRedirectPort = "reShutdownRedirectPort";
  public static final int reShutdownRedirectPortDefault = 5060;
  public static final int reShutdownRedirectPortMin = 0;
  public static final int reShutdownRedirectPortMax = 65535;
  public static final String reShutdownRedirectTransport = "reShutdownRedirectTransport";
  public static final String reShutdownRedirectTransportDefault = "UDP";
  public static final String reShutdownRedirectTransport_UDP = "UDP";
  public static final int index_reShutdownRedirectTransport_UDP =
      DsSipTransportType.getTypeAsInt(reShutdownRedirectTransport_UDP);
  public static final String reShutdownRedirectTransport_TCP = "TCP";
  public static final int index_reShutdownRedirectTransport_TCP =
      DsSipTransportType.getTypeAsInt(reShutdownRedirectTransport_TCP);
  public static final String reShutdownRedirectTransport_TLS = "TLS";
  public static final int index_reShutdownRedirectTransport_TLS =
      DsSipTransportType.getTypeAsInt(reShutdownRedirectTransport_TLS);
  public static final String reShutdownOnOrOff = "reShutdownOnOrOff";
  public static final String reShutdownOnOrOffDefault = "on";
  public static final String reShutdownOnOrOff_on = "on";
  public static final int index_reShutdownOnOrOff_on = 0;
  public static final String reShutdownOnOrOff_off = "off";
  public static final int index_reShutdownOnOrOff_off = 1;
  public static final String dsReTable = "dsReTable";
  public static final String reTableName = "reTableName";
  public static final String reTableRowStatus = "reTableRowStatus";
  public static final String dsReTime = "dsReTime";
  public static final String reTimeTable = "reTimeTable";
  public static final String reTimeQValue = "reTimeQValue";
  public static final float reTimeQValueMin = 0.0f;
  public static final float reTimeQValueMax = 1.0f;
  public static final String reTimeOutput = "reTimeOutput";
  public static final String reTimeDtStart = "reTimeDtStart";
  public static final String reTimeDtEnd = "reTimeDtEnd";
  public static final String reTimeDuration = "reTimeDuration";
  public static final String reTimeFreq = "reTimeFreq";
  public static final String reTimeFreq_daily = "daily";
  public static final int index_reTimeFreq_daily = 0;
  public static final String reTimeFreq_weekly = "weekly";
  public static final int index_reTimeFreq_weekly = 1;
  public static final String reTimeFreq_monthly = "monthly";
  public static final int index_reTimeFreq_monthly = 2;
  public static final String reTimeFreq_yearly = "yearly";
  public static final int index_reTimeFreq_yearly = 3;
  public static final String reTimeInterval = "reTimeInterval";
  public static final int reTimeIntervalDefault = 1;
  public static final int reTimeIntervalMin = 1;
  public static final int reTimeIntervalMax = 9999999;
  public static final String reTimeUntil = "reTimeUntil";
  public static final String reTimeByDay = "reTimeByDay";
  public static final String reTimeByMonth = "reTimeByMonth";
  public static final String reTimeByMonthDay = "reTimeByMonthDay";
  public static final String reTimeByYearDay = "reTimeByYearDay";
  public static final String reTimeByWeekNo = "reTimeByWeekNo";
  public static final String reTimeWkst = "reTimeWkst";
  public static final String reTimeWkstDefault = "MO";
  public static final String reTimeWkst_MO = "MO";
  public static final int index_reTimeWkst_MO = 0;
  public static final String reTimeWkst_TU = "TU";
  public static final int index_reTimeWkst_TU = 1;
  public static final String reTimeWkst_WE = "WE";
  public static final int index_reTimeWkst_WE = 2;
  public static final String reTimeWkst_TH = "TH";
  public static final int index_reTimeWkst_TH = 3;
  public static final String reTimeWkst_FR = "FR";
  public static final int index_reTimeWkst_FR = 4;
  public static final String reTimeWkst_SA = "SA";
  public static final int index_reTimeWkst_SA = 5;
  public static final String reTimeWkst_SU = "SU";
  public static final int index_reTimeWkst_SU = 6;
  public static final String reTimeServerGroup = "reTimeServerGroup";
  public static final String reTimeVariables = "reTimeVariables";
  public static final String reTimeRowStatus = "reTimeRowStatus";
  public static final String dsReTimeTable = "dsReTimeTable";
  public static final String reTimeTableName = "reTimeTableName";
  public static final String reTimeTableRowStatus = "reTimeTableRowStatus";
  public static final String dsReTriggerCondition = "dsReTriggerCondition";
  public static final String reTriggerTriggerName = "reTriggerTriggerName";
  public static final String reTriggerSequenceNo = "reTriggerSequenceNo";
  public static final int reTriggerSequenceNoMin = 0;
  public static final String reTriggerRequestMethod = "reTriggerRequestMethod";
  public static final String reTriggerMsgType = "reTriggerMsgType";
  public static final String reTriggerMsgType_request = "request";
  public static final int index_reTriggerMsgType_request = 0;
  public static final String reTriggerMsgType_response = "response";
  public static final int index_reTriggerMsgType_response = 1;
  public static final String reTriggerResponseCode = "reTriggerResponseCode";
  public static final String reTriggerRequestUri = "reTriggerRequestUri";
  public static final String reTriggerRequestScheme = "reTriggerRequestScheme";
  public static final String reTriggerRequestScheme_sip = "sip";
  public static final int index_reTriggerRequestScheme_sip = 0;
  public static final String reTriggerRequestScheme_tel = "tel";
  public static final int index_reTriggerRequestScheme_tel = 1;
  public static final String reTriggerRequestUser = "reTriggerRequestUser";
  public static final String reTriggerRequestHost = "reTriggerRequestHost";
  public static final String reTriggerRequestPort = "reTriggerRequestPort";
  public static final String reTriggerRequestParam = "reTriggerRequestParam";
  public static final String reTriggerRouteUri = "reTriggerRouteUri";
  public static final String reTriggerRouteScheme = "reTriggerRouteScheme";
  public static final String reTriggerRouteUser = "reTriggerRouteUser";
  public static final String reTriggerRouteHost = "reTriggerRouteHost";
  public static final String reTriggerRoutePort = "reTriggerRoutePort";
  public static final String reTriggerRouteUriParam = "reTriggerRouteUriParam";
  public static final String reTriggerRouteHdrParam = "reTriggerRouteHdrParam";
  public static final String reTriggerLocalIp = "reTriggerLocalIp";
  public static final String reTriggerLocalPort = "reTriggerLocalPort";
  public static final String reTriggerRemoteIP = "reTriggerRemoteIP";
  public static final String reTriggerRemotePort = "reTriggerRemotePort";
  public static final String reTriggerInNetwork = "reTriggerInNetwork";
  public static final String reTriggerOutNetwork = "reTriggerOutNetwork";
  public static final String reTriggerProtocol = "reTriggerProtocol";
  public static final String reTriggerProtocol_UDP = "UDP";
  public static final int index_reTriggerProtocol_UDP =
      DsSipTransportType.getTypeAsInt(reTriggerProtocol_UDP);
  public static final String reTriggerProtocol_TCP = "TCP";
  public static final int index_reTriggerProtocol_TCP =
      DsSipTransportType.getTypeAsInt(reTriggerProtocol_TCP);
  public static final String reTriggerProtocol_TLS = "TLS";
  public static final int index_reTriggerProtocol_TLS =
      DsSipTransportType.getTypeAsInt(reTriggerProtocol_TLS);
  public static final String reTriggerSubjectAltName = "reTriggerSubjectAltName";
  public static final String reTriggerUserAgentHdr = "reTriggerUserAgentHdr";
  public static final String reTriggerRowStatus = "reTriggerRowStatus";
  public static final String dsReVia = "dsReVia";
  public static final String reViaAddress = "reViaAddress";
  public static final String reViaTransport = "reViaTransport";
  public static final String reViaTransportDefault = "UDP";
  public static final String reViaTransport_UDP = "UDP";
  public static final int index_reViaTransport_UDP =
      DsSipTransportType.getTypeAsInt(reViaTransport_UDP);
  public static final String reViaTransport_TCP = "TCP";
  public static final int index_reViaTransport_TCP =
      DsSipTransportType.getTypeAsInt(reViaTransport_TCP);
  public static final String reViaTransport_TLS = "TLS";
  public static final int index_reViaTransport_TLS =
      DsSipTransportType.getTypeAsInt(reViaTransport_TLS);
  public static final String reViaDirection = "reViaDirection";
  public static final String reViaPort = "reViaPort";
  public static final int reViaPortDefault = 5060;
  public static final int reViaPortMin = 0;
  public static final int reViaPortMax = 65535;
  public static final String reViaSrcPort = "reViaSrcPort";
  public static final int reViaSrcPortMin = 0;
  public static final int reViaSrcPortMax = 65535;
  public static final String reViaSrcAddress = "reViaSrcAddress";
  public static final String reViaRowStatus = "reViaRowStatus";
  public static final String dsReVlanIdentification = "dsReVlanIdentification";
  public static final String reVlanIdentificationID = "reVlanIdentificationID";
  public static final String reVlanIdentificationDomain = "reVlanIdentificationDomain";
  public static final String reVlanIdentificationDomainDefault = "default";
  public static final String reVlanIdentificationDomain_default = "default";
  public static final int index_reVlanIdentificationDomain_default = 0;
  public static final String reVlanIdentificationRowStatus = "reVlanIdentificationRowStatus";
  public static final String dsReXCLCLIParams = "dsReXCLCLIParams";
  public static final String reXCLCLIParamsName = "reXCLCLIParamsName";
  public static final String reXCLCLIParamsValue = "reXCLCLIParamsValue";
  public static final String reXCLCLIParamsRowStatus = "reXCLCLIParamsRowStatus";
  public static final String dsReXclModule = "dsReXclModule";
  public static final String reXclModuleFile = "reXclModuleFile";
  public static final String reXclModuleRowStatus = "reXclModuleRowStatus";
  public static final String dsReXclQueue = "dsReXclQueue";
  public static final String reXclQueueSize = "reXclQueueSize";
  public static final int reXclQueueSizeDefault = 2000;
  public static final int reXclQueueSizeMin = 50;
  public static final int reXclQueueSizeMax = 1000000;
  public static final String reXclQueueThreadCount = "reXclQueueThreadCount";
  public static final int reXclQueueThreadCountDefault = 2;
  public static final String reXclQueueStatus = "reXclQueueStatus";
  public static final String reXclQueueStatusDefault = "on";
  public static final String reXclQueueStatus_on = "on";
  public static final int index_reXclQueueStatus_on = 0;
  public static final String reXclQueueStatus_off = "off";
  public static final int index_reXclQueueStatus_off = 1;
  public static final String enumTrapDomainUnreachableClear = "enumTrapDomainUnreachableClear";
  public static final String enumDomainUnreachableClearName = "enumDomainUnreachableClearName";
  public static final String enumDomainUnreachableClearMessage =
      "enumDomainUnreachableClearMessage";
  public static final String qosTrapServerUnreachableClear = "qosTrapServerUnreachableClear";
  public static final String qosServerUnreachableClearIP = "qosServerUnreachableClearIP";
  public static final String qosServerUnreachableClearPort = "qosServerUnreachableClearPort";
  public static final String qosServerUnreachableClearServerGroup =
      "qosServerUnreachableClearServerGroup";
  public static final String qosServerUnreachableClearMessage = "qosServerUnreachableClearMessage";
  public static final String qosTrapServerUnreachable = "qosTrapServerUnreachable";
  public static final String qosServerUnreachableIP = "qosServerUnreachableIP";
  public static final String qosServerUnreachablePort = "qosServerUnreachablePort";
  public static final String qosServerUnreachableServerGroup = "qosServerUnreachableServerGroup";
  public static final String qosServerUnreachableMessage = "qosServerUnreachableMessage";
  public static final String enumTrapServerUnreachableClear = "enumTrapServerUnreachableClear";
  public static final String enumServerUnreachableClearIP = "enumServerUnreachableClearIP";
  public static final String enumServerUnreachableClearPort = "enumServerUnreachableClearPort";
  public static final String enumServerUnreachableClearDomain = "enumServerUnreachableClearDomain";
  public static final String enumServerUnreachableClearMessage =
      "enumServerUnreachableClearMessage";
  public static final String radiusTrapServerUnreachable = "radiusTrapServerUnreachable";
  public static final String radiusServerUnreachableIP = "radiusServerUnreachableIP";
  public static final String radiusServerUnreachablePort = "radiusServerUnreachablePort";
  public static final String radiusServerUnreachableServerGroup =
      "radiusServerUnreachableServerGroup";
  public static final String radiusServerUnreachableMessage = "radiusServerUnreachableMessage";
  public static final String enumTrapServerUnreachable = "enumTrapServerUnreachable";
  public static final String enumServerUnreachableIP = "enumServerUnreachableIP";
  public static final String enumServerUnreachablePort = "enumServerUnreachablePort";
  public static final String enumServerUnreachableDomain = "enumServerUnreachableDomain";
  public static final String enumServerUnreachableMessage = "enumServerUnreachableMessage";
  public static final String sipTrapReLicenseExpire = "sipTrapReLicenseExpire";
  public static final String reLicenseExpireMessage = "reLicenseExpireMessage";
  public static final String radiusTrapServerGroupUnreachableClear =
      "radiusTrapServerGroupUnreachableClear";
  public static final String radiusServerGroupUnreachableClearName =
      "radiusServerGroupUnreachableClearName";
  public static final String radiusServerGroupUnreachableClearMessage =
      "radiusServerGroupUnreachableClearMessage";
  public static final String enumTrapDomainUnreachable = "enumTrapDomainUnreachable";
  public static final String enumDomainUnreachableName = "enumDomainUnreachableName";
  public static final String enumDomainUnreachableMessage = "enumDomainUnreachableMessage";
  public static final String radiusTrapServerUnreachableClear = "radiusTrapServerUnreachableClear";
  public static final String radiusServerUnreachableClearIP = "radiusServerUnreachableClearIP";
  public static final String radiusServerUnreachableClearPort = "radiusServerUnreachableClearPort";
  public static final String radiusServerUnreachableClearServerGroup =
      "radiusServerUnreachableClearServerGroup";
  public static final String radiusServerUnreachableClearMessage =
      "radiusServerUnreachableClearMessage";
  public static final String radiusTrapServerGroupUnreachable = "radiusTrapServerGroupUnreachable";
  public static final String radiusServerGroupUnreachableName = "radiusServerGroupUnreachableName";
  public static final String radiusServerGroupUnreachableMessage =
      "radiusServerGroupUnreachableMessage";
  public static final String sipTrapReUnknownSipMethod = "sipTrapReUnknownSipMethod";
  public static final String reUnknownSipMethodName = "reUnknownSipMethodName";
  public static final String reUnknownSipMethodMessage = "reUnknownSipMethodMessage";
  public static final String qosTrapServerGroupUnreachableClear =
      "qosTrapServerGroupUnreachableClear";
  public static final String qosServerGroupUnreachableClearName =
      "qosServerGroupUnreachableClearName";
  public static final String qosServerGroupUnreachableClearMessage =
      "qosServerGroupUnreachableClearMessage";
  public static final String qosTrapServerGroupUnreachable = "qosTrapServerGroupUnreachable";
  public static final String qosServerGroupUnreachableName = "qosServerGroupUnreachableName";
  public static final String qosServerGroupUnreachableMessage = "qosServerGroupUnreachableMessage";

  private static final HashSet allStrings = new HashSet(78);

  private static final HashSet versionableParams = new HashSet();

  private static final HashMap validValues_reRetransmitTimerType = new HashMap(8);
  private static final HashMap validValues_reRoutingLookupRule = new HashMap(5);
  private static final HashMap validValues_dsReAccounting = new HashMap(2);
  private static final HashMap validValues_reRoutingLookup = new HashMap(2);
  private static final HashMap validValues_reRouteNrsMap = new HashMap(2);
  private static final HashMap validValues_reXclQueueStatus = new HashMap(2);
  private static final HashMap validValues_reTriggerMsgType = new HashMap(2);
  private static final HashMap validValues_reEnumRootExponentialBackOff = new HashMap(2);
  private static final HashMap validValues_reMaskHeader = new HashMap(1);
  private static final HashMap validValues_reTriggerProtocol = new HashMap(3);
  private static final HashMap validValues_reRouteEnum = new HashMap(2);
  private static final HashMap validValues_reNormalizationMsgType = new HashMap(3);
  private static final HashMap validValues_reFirewallGroupDomain = new HashMap(2);
  private static final HashMap validValues_reNetworkType = new HashMap(6);
  private static final HashMap validValues_rePathTransport = new HashMap(3);
  private static final HashMap validValues_dsReFirewallMode = new HashMap(2);
  private static final HashMap validValues_reNormalizationAction = new HashMap(7);
  private static final HashMap validValues_reListenTransport = new HashMap(3);
  private static final HashMap validValues_reRouteLookupField = new HashMap(40);
  private static final HashMap validValues_reRetransmitCountType = new HashMap(3);
  private static final HashMap validValues_reVlanIdentificationDomain = new HashMap(2);
  private static final HashMap validValues_dsReOnFirewallFailure = new HashMap(2);
  private static final HashMap validValues_dsReRecurseOnRedirect = new HashMap(2);
  private static final HashMap validValues_reTriggerRequestScheme = new HashMap(2);
  private static final HashMap validValues_reFirewallQueueRowStatus = new HashMap(2);
  private static final HashMap validValues_reSgLbType = new HashMap(5);
  private static final HashMap validValues_dsReCompactHeaderForm = new HashMap(2);
  private static final HashMap validValues_reFirewallTimeoutRowStatus = new HashMap(2);
  private static final HashMap validValues_dsReNatMedia = new HashMap(3);
  private static final HashMap validValues_reShutdownResponse = new HashMap(2);
  private static final HashMap validValues_reAddServiceRouteTransport = new HashMap(3);
  private static final HashMap validValues_reRoutingLookupField = new HashMap(6);
  private static final HashMap validValues_dsReRollback = new HashMap(3);
  private static final HashMap validValues_reRoutingEnumField = new HashMap(1);
  private static final HashMap validValues_reNormalizationHdrInstance = new HashMap(3);
  private static final HashMap validValues_reRouteEnumField = new HashMap(26);
  private static final HashMap validValues_reRouteLookup = new HashMap(2);
  private static final HashMap validValues_reNormalizationField = new HashMap(3);
  private static final HashMap validValues_dsRePerformanceData = new HashMap(4);
  private static final HashMap validValues_reModifyServiceRouteTransport = new HashMap(3);
  private static final HashMap validValues_reShutdownRedirectTransport = new HashMap(3);
  private static final HashMap validValues_reRoutingEnum = new HashMap(2);
  private static final HashMap validValues_reModuleTriggerModuleName = new HashMap(25);
  private static final HashMap validValues_reAccountingSipHdrSource = new HashMap(2);
  private static final HashMap validValues_dsReConnectionBasedSelection = new HashMap(2);
  private static final HashMap validValues_reRouteLookupRule = new HashMap(5);
  private static final HashMap validValues_reRoutingNrsMap = new HashMap(2);
  private static final HashMap validValues_reModuleTriggerTriggerName = new HashMap(2);
  private static final HashMap validValues_dsReOnNextHopFailure = new HashMap(2);
  private static final HashMap validValues_dsReAccountingServerSide = new HashMap(2);
  private static final HashMap validValues_reRecordRouteTransport = new HashMap(3);
  private static final HashMap validValues_reModuleTriggerAction = new HashMap(31);
  private static final HashMap validValues_reTimeWkst = new HashMap(7);
  private static final HashMap validValues_dsReAccountingClientSide = new HashMap(2);
  private static final HashMap validValues_reViaTransport = new HashMap(3);
  private static final HashMap validValues_rePopIdRowStatus = new HashMap(2);
  private static final HashMap validValues_reRouteNrsMapField = new HashMap(26);
  private static final HashMap validValues_reNetworkConnection = new HashMap(2);
  private static final HashMap validValues_reTimeFreq = new HashMap(4);
  private static final HashMap validValues_reRoutingLookupKeyModifier = new HashMap(3);
  private static final HashMap validValues_reShutdownOnOrOff = new HashMap(2);
  private static final HashMap validValues_reSgElementTransport = new HashMap(3);
  private static final HashMap validValues_reSgPing = new HashMap(2);
  private static final HashMap validValues_reSetRootXCLRoute = new HashMap(2);
  private static final HashMap validValues_reEnumRootRecurse = new HashMap(2);
  private static final HashMap validValues_dsReStateMode = new HashMap(3);
  private static final HashMap validValues_dsReSend305 = new HashMap(3);
  private static final HashMap validValues_dsReQos = new HashMap(2);
  private static final HashMap validValues_dsReSend100 = new HashMap(2);
  private static final HashMap validValues_reRoutingNrsMapField = new HashMap(1);

  private static final HashMap defaults = new HashMap(78);

  private static final HashMap mins = new HashMap(54);

  private static final HashMap maxs = new HashMap(34);

  private static final HashMap validValues = new HashMap();

  static {
    allStrings.add(dsReAccounting);
    allStrings.add(dsReAccountingClientSide);
    allStrings.add(dsReAccountingServerSide);
    allStrings.add(dsReCommit);
    allStrings.add(dsReCompactHeaderForm);
    allStrings.add(dsReConnectionBasedSelection);
    allStrings.add(dsReConnectionCacheSize);
    allStrings.add(dsReDefaultContactExpires);
    allStrings.add(dsReDefaultQValue);
    allStrings.add(dsReEnumServerStatus);
    allStrings.add(dsReFirewallMode);
    allStrings.add(dsReMaxContactExpires);
    allStrings.add(dsReMaxIterationDepth);
    allStrings.add(dsReMaxMediaDescriptions);
    allStrings.add(dsReMinContactExpires);
    allStrings.add(dsReNatMedia);
    allStrings.add(dsReOnFirewallFailure);
    allStrings.add(dsReOnNextHopFailure);
    allStrings.add(dsRePerformanceData);
    allStrings.add(dsReQos);
    allStrings.add(dsReQosSgElementStatus);
    allStrings.add(dsReRadiusSgElementStatus);
    allStrings.add(dsReRecurseOnRedirect);
    allStrings.add(dsReRollback);
    allStrings.add(dsReRouteFile);
    allStrings.add(dsReSend100);
    allStrings.add(dsReSend305);
    allStrings.add(dsReShowRoutes);
    allStrings.add(dsReStateMode);
    allStrings.add(dsReXCLDebug);
    allStrings.add(dsReXCLTest);
    allStrings.add(dsReAccountingSipHdr);
    allStrings.add(reAccountingSipHdrName);
    allStrings.add(reAccountingSipHdrSource);
    allStrings.add(reAccountingSipHdrRowStatus);
    allStrings.add(dsReAddServiceRoute);
    allStrings.add(reAddServiceRouteDirection);
    allStrings.add(reAddServiceRouteAddress);
    allStrings.add(reAddServiceRouteTransport);
    allStrings.add(reAddServiceRouteSequence);
    allStrings.add(reAddServiceRoutePort);
    allStrings.add(reAddServiceRouteParams);
    allStrings.add(reAddServiceRouteRowStatus);
    allStrings.add(dsReApplicationConfig);
    allStrings.add(reApplicationName);
    allStrings.add(reApplicationSg);
    allStrings.add(reReturnApplicationSg);
    allStrings.add(reApplicationResource);
    allStrings.add(reApplicationConfigRowStatus);
    allStrings.add(dsReEnumRoot);
    allStrings.add(reEnumRootName);
    allStrings.add(reEnumRootTimeout);
    allStrings.add(reEnumRootAttempts);
    allStrings.add(reEnumRootRecurse);
    allStrings.add(reEnumRootExponentialBackOff);
    allStrings.add(reEnumRootRowStatus);
    allStrings.add(dsReEnumServer);
    allStrings.add(reEnumServerRoot);
    allStrings.add(reEnumServerIp);
    allStrings.add(reEnumServerPort);
    allStrings.add(reEnumServerQValue);
    allStrings.add(reEnumServerRowStatus);
    allStrings.add(dsReFirewallGroup);
    allStrings.add(reFirewallGroupGroupID);
    allStrings.add(reFirewallGroupDomain);
    allStrings.add(reFirewallGroupRowStatus);
    allStrings.add(dsReFirewallQueue);
    allStrings.add(reFirewallQueueSize);
    allStrings.add(reFirewallQueueBusyReset);
    allStrings.add(reFirewallQueueMaxThreads);
    allStrings.add(reFirewallQueueRowStatus);
    allStrings.add(dsReFirewallTimeout);
    allStrings.add(reFirewallTimeoutSessionTimeout);
    allStrings.add(reFirewallTimeoutSessionClose);
    allStrings.add(reFirewallTimeoutRowStatus);
    allStrings.add(dsReListen);
    allStrings.add(reListenInterface);
    allStrings.add(reListenDirection);
    allStrings.add(reListenPort);
    allStrings.add(reListenTransport);
    allStrings.add(reListenRowStatus);
    allStrings.add(dsReLoadXCL);
    allStrings.add(reLoadXCLScriptName);
    allStrings.add(reLoadXCLScriptFile);
    allStrings.add(reLoadXCLRowStatus);
    allStrings.add(dsReMask);
    allStrings.add(reMaskDirection);
    allStrings.add(reMaskHeader);
    allStrings.add(reMaskRowStatus);
    allStrings.add(dsReMaxPinholeBandwidth);
    allStrings.add(reMaxPinholeBandwidthBits);
    allStrings.add(reMaxPinholeBandwidthCodec);
    allStrings.add(reMaxPinholeBandwidthRowStatus);
    allStrings.add(dsReModifyServiceRoute);
    allStrings.add(reModifyServiceRouteDirection);
    allStrings.add(reModifyServiceRouteAddress);
    allStrings.add(reModifyServiceRouteTransport);
    allStrings.add(reModifyServiceRoutePort);
    allStrings.add(reModifyServiceRouteRowStatus);
    allStrings.add(dsReModuleTrigger);
    allStrings.add(reModuleTriggerModuleName);
    allStrings.add(reModuleTriggerSequenceNo);
    allStrings.add(reModuleTriggerTriggerName);
    allStrings.add(reModuleTriggerAction);
    allStrings.add(reModuleTriggerActionParam);
    allStrings.add(reModuleTriggerPolicy);
    allStrings.add(reModuleTriggerRowStatus);
    allStrings.add(dsReNetscreenFirewall);
    allStrings.add(reNetscreenFirewallIP);
    allStrings.add(reNetscreenFirewallPort);
    allStrings.add(reNetscreenFirewallGroupID);
    allStrings.add(reNetscreenFirewallRowStatus);
    allStrings.add(dsReNetwork);
    allStrings.add(reNetworkDirection);
    allStrings.add(reNetworkType);
    allStrings.add(reNetworkConnection);
    allStrings.add(reNetworkRowStatus);
    allStrings.add(dsReNormalizationPolicy);
    allStrings.add(reNormalizationPolicy);
    allStrings.add(reNormalizationSequence);
    allStrings.add(reNormalizationHeader);
    allStrings.add(reNormalizationAction);
    allStrings.add(reNormalizationMsgType);
    allStrings.add(reNormalizationMethod);
    allStrings.add(reNormalizationHdrInstance);
    allStrings.add(reNormalizationParameter);
    allStrings.add(reNormalizationField);
    allStrings.add(reNormalizationActionParam);
    allStrings.add(reNormalizationRegexMatch);
    allStrings.add(reNormalizationRegexReplace);
    allStrings.add(reNormalizationPolicyRowStatus);
    allStrings.add(dsRePath);
    allStrings.add(rePathAddress);
    allStrings.add(rePathTransport);
    allStrings.add(rePathDirection);
    allStrings.add(rePathPort);
    allStrings.add(rePathRowStatus);
    allStrings.add(dsRePopId);
    allStrings.add(rePrimaryPopId);
    allStrings.add(rePopIdRowStatus);
    allStrings.add(dsRePopNames);
    allStrings.add(rePopNamesName);
    allStrings.add(rePopNamesElementName);
    allStrings.add(rePopNamesRowStatus);
    allStrings.add(dsReQosSg);
    allStrings.add(reQosSgName);
    allStrings.add(reQosSgRowStatus);
    allStrings.add(dsReQosSgElement);
    allStrings.add(reQosSgElementSgName);
    allStrings.add(reQosSgElementUri);
    allStrings.add(reQosSgElementConnectionPoolSize);
    allStrings.add(reQosSgElementSgReference);
    allStrings.add(reQosSgElementPipelineSize);
    allStrings.add(reQosSgElementTimeout);
    allStrings.add(reQosSgElementQValue);
    allStrings.add(reQosSgElementRowStatus);
    allStrings.add(dsReRadiusSg);
    allStrings.add(reRadiusSgName);
    allStrings.add(reRadiusSgSourceIP);
    allStrings.add(reRadiusSgRowStatus);
    allStrings.add(dsReRadiusSgElement);
    allStrings.add(reRadiusSgElementSgName);
    allStrings.add(reRadiusSgElementHost);
    allStrings.add(reRadiusSgElementPort);
    allStrings.add(reRadiusSgElementSharedSecret);
    allStrings.add(reRadiusSgElementSgReference);
    allStrings.add(reRadiusSgElementRetxCount);
    allStrings.add(reRadiusSgElementRetxTimeout);
    allStrings.add(reRadiusSgElementQValue);
    allStrings.add(reRadiusSgElementRowStatus);
    allStrings.add(dsReRecordRoute);
    allStrings.add(reRecordRouteHost);
    allStrings.add(reRecordRouteTransport);
    allStrings.add(reRecordRouteDirection);
    allStrings.add(reRecordRoutePort);
    allStrings.add(reRecordRouteRowStatus);
    allStrings.add(dsReRetransmitCount);
    allStrings.add(reRetransmitCountType);
    allStrings.add(reRetransmitCountValue);
    allStrings.add(reRetransmitCountNetwork);
    allStrings.add(reRetransmitCountRowStatus);
    allStrings.add(dsReRetransmitTimer);
    allStrings.add(reRetransmitTimerType);
    allStrings.add(reRetransmitTimerValue);
    allStrings.add(reRetransmitTimerNetwork);
    allStrings.add(reRetransmitTimerRowStatus);
    allStrings.add(dsReRoute);
    allStrings.add(reRouteTable);
    allStrings.add(reRouteKey);
    allStrings.add(reRouteOutput);
    allStrings.add(reRouteServerGroup);
    allStrings.add(reRouteVariables);
    allStrings.add(reRoutePolicyAdvance);
    allStrings.add(reRouteGroup);
    allStrings.add(reRouteRowStatus);
    allStrings.add(dsReRouteElement);
    allStrings.add(reRouteElementGroupName);
    allStrings.add(reRouteElementRuri);
    allStrings.add(reRouteElementRoute);
    allStrings.add(reRouteElementNetwork);
    allStrings.add(reRouteElementFailover);
    allStrings.add(reRouteElementQValue);
    allStrings.add(reRouteElementRowStatus);
    allStrings.add(dsReRouteGroup);
    allStrings.add(reRouteGroupName);
    allStrings.add(reRouteGroupRowStatus);
    allStrings.add(dsReRoutePolicy);
    allStrings.add(reRoutePolicy);
    allStrings.add(reRouteSequence);
    allStrings.add(reRouteEnum);
    allStrings.add(reRouteLookup);
    allStrings.add(reRouteNrsMap);
    allStrings.add(reRouteEnumTable);
    allStrings.add(reRouteLookupTable);
    allStrings.add(reRouteNrsMapTable);
    allStrings.add(reRouteEnumField);
    allStrings.add(reRouteLookupField);
    allStrings.add(reRouteNrsMapField);
    allStrings.add(reRouteEnumRoot);
    allStrings.add(reRouteEnumVoidResponse);
    allStrings.add(reRouteLookupRule);
    allStrings.add(reRouteLookupLength);
    allStrings.add(reRouteLookupKeyModifier);
    allStrings.add(reRouteLookupParamValue);
    allStrings.add(reRoutePolicyPolicyAdvance);
    allStrings.add(reRoutePolicyRowStatus);
    allStrings.add(dsReRoutingPolicy);
    allStrings.add(reRoutingPolicy);
    allStrings.add(reRoutingSequence);
    allStrings.add(reRoutingEnum);
    allStrings.add(reRoutingLookup);
    allStrings.add(reRoutingNrsMap);
    allStrings.add(reRoutingEnumTable);
    allStrings.add(reRoutingLookupTable);
    allStrings.add(reRoutingNrsMapTable);
    allStrings.add(reRoutingEnumField);
    allStrings.add(reRoutingLookupField);
    allStrings.add(reRoutingNrsMapField);
    allStrings.add(reRoutingEnumRoot);
    allStrings.add(reRoutingEnumVoidResponse);
    allStrings.add(reRoutingLookupRule);
    allStrings.add(reRoutingLookupLength);
    allStrings.add(reRoutingLookupKeyModifier);
    allStrings.add(reRoutingPolicyRowStatus);
    allStrings.add(dsReSaveRoutes);
    allStrings.add(reSaveRoutesFilename);
    allStrings.add(reSaveRoutesTable);
    allStrings.add(reSaveRoutesRowStatus);
    allStrings.add(dsReSaveServerGroups);
    allStrings.add(reSaveServerGroupsFilename);
    allStrings.add(reSaveServerGroupsSG);
    allStrings.add(reSaveServerGroupsRowStatus);
    allStrings.add(dsReSetRootXCL);
    allStrings.add(reSetRootXCLScriptName);
    allStrings.add(reSetRootXCLRoute);
    allStrings.add(dsReSg);
    allStrings.add(reSgName);
    allStrings.add(reSgNetwork);
    allStrings.add(reSgLbType);
    allStrings.add(reSgPing);
    allStrings.add(reSgRowStatus);
    allStrings.add(dsReSgElement);
    allStrings.add(reSgElementSgName);
    allStrings.add(reSgElementSgReference);
    allStrings.add(reSgElementHost);
    allStrings.add(reSgElementPort);
    allStrings.add(reSgElementTransport);
    allStrings.add(reSgElementQValue);
    allStrings.add(reSgElementRowStatus);
    allStrings.add(dsReShutdown);
    allStrings.add(reShutdownResponse);
    allStrings.add(reShutdownTime);
    allStrings.add(reShutdownRetryAfter);
    allStrings.add(reShutdownRedirectHost);
    allStrings.add(reShutdownRedirectPort);
    allStrings.add(reShutdownRedirectTransport);
    allStrings.add(reShutdownOnOrOff);
    allStrings.add(dsReTable);
    allStrings.add(reTableName);
    allStrings.add(reTableRowStatus);
    allStrings.add(dsReTime);
    allStrings.add(reTimeTable);
    allStrings.add(reTimeQValue);
    allStrings.add(reTimeOutput);
    allStrings.add(reTimeDtStart);
    allStrings.add(reTimeDtEnd);
    allStrings.add(reTimeDuration);
    allStrings.add(reTimeFreq);
    allStrings.add(reTimeInterval);
    allStrings.add(reTimeUntil);
    allStrings.add(reTimeByDay);
    allStrings.add(reTimeByMonth);
    allStrings.add(reTimeByMonthDay);
    allStrings.add(reTimeByYearDay);
    allStrings.add(reTimeByWeekNo);
    allStrings.add(reTimeWkst);
    allStrings.add(reTimeServerGroup);
    allStrings.add(reTimeVariables);
    allStrings.add(reTimeRowStatus);
    allStrings.add(dsReTimeTable);
    allStrings.add(reTimeTableName);
    allStrings.add(reTimeTableRowStatus);
    allStrings.add(dsReTriggerCondition);
    allStrings.add(reTriggerTriggerName);
    allStrings.add(reTriggerSequenceNo);
    allStrings.add(reTriggerRequestMethod);
    allStrings.add(reTriggerMsgType);
    allStrings.add(reTriggerResponseCode);
    allStrings.add(reTriggerRequestUri);
    allStrings.add(reTriggerRequestScheme);
    allStrings.add(reTriggerRequestUser);
    allStrings.add(reTriggerRequestHost);
    allStrings.add(reTriggerRequestPort);
    allStrings.add(reTriggerRequestParam);
    allStrings.add(reTriggerRouteUri);
    allStrings.add(reTriggerRouteScheme);
    allStrings.add(reTriggerRouteUser);
    allStrings.add(reTriggerRouteHost);
    allStrings.add(reTriggerRoutePort);
    allStrings.add(reTriggerRouteUriParam);
    allStrings.add(reTriggerRouteHdrParam);
    allStrings.add(reTriggerLocalIp);
    allStrings.add(reTriggerLocalPort);
    allStrings.add(reTriggerRemoteIP);
    allStrings.add(reTriggerRemotePort);
    allStrings.add(reTriggerInNetwork);
    allStrings.add(reTriggerOutNetwork);
    allStrings.add(reTriggerProtocol);
    allStrings.add(reTriggerSubjectAltName);
    allStrings.add(reTriggerUserAgentHdr);
    allStrings.add(reTriggerRowStatus);
    allStrings.add(dsReVia);
    allStrings.add(reViaAddress);
    allStrings.add(reViaTransport);
    allStrings.add(reViaDirection);
    allStrings.add(reViaPort);
    allStrings.add(reViaSrcPort);
    allStrings.add(reViaSrcAddress);
    allStrings.add(reViaRowStatus);
    allStrings.add(dsReVlanIdentification);
    allStrings.add(reVlanIdentificationID);
    allStrings.add(reVlanIdentificationDomain);
    allStrings.add(reVlanIdentificationRowStatus);
    allStrings.add(dsReXCLCLIParams);
    allStrings.add(reXCLCLIParamsName);
    allStrings.add(reXCLCLIParamsValue);
    allStrings.add(reXCLCLIParamsRowStatus);
    allStrings.add(dsReXclModule);
    allStrings.add(reXclModuleFile);
    allStrings.add(reXclModuleRowStatus);
    allStrings.add(dsReXclQueue);
    allStrings.add(reXclQueueSize);
    allStrings.add(reXclQueueThreadCount);
    allStrings.add(reXclQueueStatus);

    versionableParams.add(dsReRouteFile);
    versionableParams.add(dsReLoadXCL);
    versionableParams.add(dsReRoute);
    versionableParams.add(dsReRouteElement);
    versionableParams.add(dsReRouteGroup);
    versionableParams.add(dsReRoutePolicy);
    versionableParams.add(dsReSetRootXCL);
    versionableParams.add(dsReSg);
    versionableParams.add(dsReSgElement);
    versionableParams.add(dsReTable);
    versionableParams.add(dsReTime);
    versionableParams.add(dsReTimeTable);

    validValues_reRetransmitTimerType.put(
        reRetransmitTimerType_T1, new Integer(index_reRetransmitTimerType_T1));
    validValues_reRetransmitTimerType.put(
        reRetransmitTimerType_T2, new Integer(index_reRetransmitTimerType_T2));
    validValues_reRetransmitTimerType.put(
        reRetransmitTimerType_T4, new Integer(index_reRetransmitTimerType_T4));
    validValues_reRetransmitTimerType.put(
        reRetransmitTimerType_TU1, new Integer(index_reRetransmitTimerType_TU1));
    validValues_reRetransmitTimerType.put(
        reRetransmitTimerType_TU2, new Integer(index_reRetransmitTimerType_TU2));
    validValues_reRetransmitTimerType.put(
        reRetransmitTimerType_TU3, new Integer(index_reRetransmitTimerType_TU3));
    validValues_reRetransmitTimerType.put(
        reRetransmitTimerType_serverTn, new Integer(index_reRetransmitTimerType_serverTn));
    validValues_reRetransmitTimerType.put(
        reRetransmitTimerType_clientTn, new Integer(index_reRetransmitTimerType_clientTn));
    validValues.put(reRetransmitTimerType, validValues_reRetransmitTimerType);

    validValues_reRoutingLookupRule.put(
        reRoutingLookupRule_exact, new Integer(index_reRoutingLookupRule_exact));
    validValues_reRoutingLookupRule.put(
        reRoutingLookupRule_fixed, new Integer(index_reRoutingLookupRule_fixed));
    validValues_reRoutingLookupRule.put(
        reRoutingLookupRule_prefix, new Integer(index_reRoutingLookupRule_prefix));
    validValues_reRoutingLookupRule.put(
        reRoutingLookupRule_subdomain, new Integer(index_reRoutingLookupRule_subdomain));
    validValues_reRoutingLookupRule.put(
        reRoutingLookupRule_subnet, new Integer(index_reRoutingLookupRule_subnet));
    validValues.put(reRoutingLookupRule, validValues_reRoutingLookupRule);

    validValues_dsReAccounting.put(dsReAccounting_on, new Integer(index_dsReAccounting_on));
    validValues_dsReAccounting.put(dsReAccounting_off, new Integer(index_dsReAccounting_off));
    validValues.put(dsReAccounting, validValues_dsReAccounting);

    validValues_reRoutingLookup.put(reRoutingLookup_y, new Integer(index_reRoutingLookup_y));
    validValues_reRoutingLookup.put(reRoutingLookup_yes, new Integer(index_reRoutingLookup_yes));
    validValues.put(reRoutingLookup, validValues_reRoutingLookup);

    validValues_reRouteNrsMap.put(reRouteNrsMap_y, new Integer(index_reRouteNrsMap_y));
    validValues_reRouteNrsMap.put(reRouteNrsMap_yes, new Integer(index_reRouteNrsMap_yes));
    validValues.put(reRouteNrsMap, validValues_reRouteNrsMap);

    validValues_reXclQueueStatus.put(reXclQueueStatus_on, new Integer(index_reXclQueueStatus_on));
    validValues_reXclQueueStatus.put(reXclQueueStatus_off, new Integer(index_reXclQueueStatus_off));
    validValues.put(reXclQueueStatus, validValues_reXclQueueStatus);

    validValues_reTriggerMsgType.put(
        reTriggerMsgType_request, new Integer(index_reTriggerMsgType_request));
    validValues_reTriggerMsgType.put(
        reTriggerMsgType_response, new Integer(index_reTriggerMsgType_response));
    validValues.put(reTriggerMsgType, validValues_reTriggerMsgType);

    validValues_reEnumRootExponentialBackOff.put(
        reEnumRootExponentialBackOff_true, new Integer(index_reEnumRootExponentialBackOff_true));
    validValues_reEnumRootExponentialBackOff.put(
        reEnumRootExponentialBackOff_false, new Integer(index_reEnumRootExponentialBackOff_false));
    validValues.put(reEnumRootExponentialBackOff, validValues_reEnumRootExponentialBackOff);

    validValues_reMaskHeader.put(reMaskHeader_via, new Integer(index_reMaskHeader_via));
    validValues.put(reMaskHeader, validValues_reMaskHeader);

    validValues_reTriggerProtocol.put(
        reTriggerProtocol_UDP, new Integer(index_reTriggerProtocol_UDP));
    validValues_reTriggerProtocol.put(
        reTriggerProtocol_TCP, new Integer(index_reTriggerProtocol_TCP));
    validValues_reTriggerProtocol.put(
        reTriggerProtocol_TLS, new Integer(index_reTriggerProtocol_TLS));
    validValues.put(reTriggerProtocol, validValues_reTriggerProtocol);

    validValues_reRouteEnum.put(reRouteEnum_y, new Integer(index_reRouteEnum_y));
    validValues_reRouteEnum.put(reRouteEnum_yes, new Integer(index_reRouteEnum_yes));
    validValues.put(reRouteEnum, validValues_reRouteEnum);

    validValues_reNormalizationMsgType.put(
        reNormalizationMsgType_request, new Integer(index_reNormalizationMsgType_request));
    validValues_reNormalizationMsgType.put(
        reNormalizationMsgType_response, new Integer(index_reNormalizationMsgType_response));
    validValues_reNormalizationMsgType.put(
        reNormalizationMsgType_both, new Integer(index_reNormalizationMsgType_both));
    validValues.put(reNormalizationMsgType, validValues_reNormalizationMsgType);

    validValues_reFirewallGroupDomain.put(
        reFirewallGroupDomain_default, new Integer(index_reFirewallGroupDomain_default));
    validValues.put(reFirewallGroupDomain, validValues_reFirewallGroupDomain);

    validValues_reNetworkType.put(
        reNetworkType_standard, new Integer(index_reNetworkType_standard));
    validValues_reNetworkType.put(reNetworkType_icmp, new Integer(index_reNetworkType_icmp));
    validValues_reNetworkType.put(reNetworkType_noicmp, new Integer(index_reNetworkType_noicmp));
    validValues_reNetworkType.put(reNetworkType_nat, new Integer(index_reNetworkType_nat));
    validValues_reNetworkType.put(reNetworkType_sigcomp, new Integer(index_reNetworkType_sigcomp));
    validValues_reNetworkType.put(reNetworkType_toksip, new Integer(index_reNetworkType_toksip));
    validValues.put(reNetworkType, validValues_reNetworkType);

    validValues_rePathTransport.put(rePathTransport_UDP, new Integer(index_rePathTransport_UDP));
    validValues_rePathTransport.put(rePathTransport_TCP, new Integer(index_rePathTransport_TCP));
    validValues_rePathTransport.put(rePathTransport_TLS, new Integer(index_rePathTransport_TLS));
    validValues.put(rePathTransport, validValues_rePathTransport);

    validValues_dsReFirewallMode.put(dsReFirewallMode_on, new Integer(index_dsReFirewallMode_on));
    validValues_dsReFirewallMode.put(dsReFirewallMode_off, new Integer(index_dsReFirewallMode_off));
    validValues.put(dsReFirewallMode, validValues_dsReFirewallMode);

    validValues_reNormalizationAction.put(
        reNormalizationAction_add, new Integer(index_reNormalizationAction_add));
    validValues_reNormalizationAction.put(
        reNormalizationAction_remove, new Integer(index_reNormalizationAction_remove));
    validValues_reNormalizationAction.put(
        reNormalizationAction_remove_all, new Integer(index_reNormalizationAction_remove_all));
    validValues_reNormalizationAction.put(
        reNormalizationAction_remove_all_except,
        new Integer(index_reNormalizationAction_remove_all_except));
    validValues_reNormalizationAction.put(
        reNormalizationAction_replace, new Integer(index_reNormalizationAction_replace));
    validValues_reNormalizationAction.put(
        reNormalizationAction_dest_uri_to_sip,
        new Integer(index_reNormalizationAction_dest_uri_to_sip));
    validValues_reNormalizationAction.put(
        reNormalizationAction_dest_uri_to_tel,
        new Integer(index_reNormalizationAction_dest_uri_to_tel));
    validValues.put(reNormalizationAction, validValues_reNormalizationAction);

    validValues_reListenTransport.put(
        reListenTransport_UDP, new Integer(index_reListenTransport_UDP));
    validValues_reListenTransport.put(
        reListenTransport_TCP, new Integer(index_reListenTransport_TCP));
    validValues_reListenTransport.put(
        reListenTransport_TLS, new Integer(index_reListenTransport_TLS));
    validValues.put(reListenTransport, validValues_reListenTransport);

    validValues_reRouteLookupField.put(
        reRouteLookupField_ruri, new Integer(index_reRouteLookupField_ruri));
    validValues_reRouteLookupField.put(
        reRouteLookupField_ruri_phone, new Integer(index_reRouteLookupField_ruri_phone));
    validValues_reRouteLookupField.put(
        reRouteLookupField_ruri_lrn, new Integer(index_reRouteLookupField_ruri_lrn));
    validValues_reRouteLookupField.put(
        reRouteLookupField_ruri_cic, new Integer(index_reRouteLookupField_ruri_cic));
    validValues_reRouteLookupField.put(
        reRouteLookupField_ruri_host, new Integer(index_reRouteLookupField_ruri_host));
    validValues_reRouteLookupField.put(
        reRouteLookupField_diversion, new Integer(index_reRouteLookupField_diversion));
    validValues_reRouteLookupField.put(
        reRouteLookupField_diversion_phone, new Integer(index_reRouteLookupField_diversion_phone));
    validValues_reRouteLookupField.put(
        reRouteLookupField_diversion_lrn, new Integer(index_reRouteLookupField_diversion_lrn));
    validValues_reRouteLookupField.put(
        reRouteLookupField_diversion_cic, new Integer(index_reRouteLookupField_diversion_cic));
    validValues_reRouteLookupField.put(
        reRouteLookupField_diversion_host, new Integer(index_reRouteLookupField_diversion_host));
    validValues_reRouteLookupField.put(
        reRouteLookupField_paid, new Integer(index_reRouteLookupField_paid));
    validValues_reRouteLookupField.put(
        reRouteLookupField_paid_phone, new Integer(index_reRouteLookupField_paid_phone));
    validValues_reRouteLookupField.put(
        reRouteLookupField_paid_lrn, new Integer(index_reRouteLookupField_paid_lrn));
    validValues_reRouteLookupField.put(
        reRouteLookupField_paid_cic, new Integer(index_reRouteLookupField_paid_cic));
    validValues_reRouteLookupField.put(
        reRouteLookupField_paid_host, new Integer(index_reRouteLookupField_paid_host));
    validValues_reRouteLookupField.put(
        reRouteLookupField_rpid, new Integer(index_reRouteLookupField_rpid));
    validValues_reRouteLookupField.put(
        reRouteLookupField_rpid_phone, new Integer(index_reRouteLookupField_rpid_phone));
    validValues_reRouteLookupField.put(
        reRouteLookupField_rpid_lrn, new Integer(index_reRouteLookupField_rpid_lrn));
    validValues_reRouteLookupField.put(
        reRouteLookupField_rpid_cic, new Integer(index_reRouteLookupField_rpid_cic));
    validValues_reRouteLookupField.put(
        reRouteLookupField_rpid_host, new Integer(index_reRouteLookupField_rpid_host));
    validValues_reRouteLookupField.put(
        reRouteLookupField_from, new Integer(index_reRouteLookupField_from));
    validValues_reRouteLookupField.put(
        reRouteLookupField_from_phone, new Integer(index_reRouteLookupField_from_phone));
    validValues_reRouteLookupField.put(
        reRouteLookupField_from_lrn, new Integer(index_reRouteLookupField_from_lrn));
    validValues_reRouteLookupField.put(
        reRouteLookupField_from_cic, new Integer(index_reRouteLookupField_from_cic));
    validValues_reRouteLookupField.put(
        reRouteLookupField_from_host, new Integer(index_reRouteLookupField_from_host));
    validValues_reRouteLookupField.put(
        reRouteLookupField_originating_uri, new Integer(index_reRouteLookupField_originating_uri));
    validValues_reRouteLookupField.put(
        reRouteLookupField_ruri_routing_number,
        new Integer(index_reRouteLookupField_ruri_routing_number));
    validValues_reRouteLookupField.put(
        reRouteLookupField_diversion_routing_number,
        new Integer(index_reRouteLookupField_diversion_routing_number));
    validValues_reRouteLookupField.put(
        reRouteLookupField_paid_routing_number,
        new Integer(index_reRouteLookupField_paid_routing_number));
    validValues_reRouteLookupField.put(
        reRouteLookupField_rpid_routing_number,
        new Integer(index_reRouteLookupField_rpid_routing_number));
    validValues_reRouteLookupField.put(
        reRouteLookupField_from_routing_number,
        new Integer(index_reRouteLookupField_from_routing_number));
    validValues_reRouteLookupField.put(
        reRouteLookupField_uri_param, new Integer(index_reRouteLookupField_uri_param));
    validValues_reRouteLookupField.put(
        reRouteLookupField_in_network, new Integer(index_reRouteLookupField_in_network));
    validValues_reRouteLookupField.put(
        reRouteLookupField_local_ip, new Integer(index_reRouteLookupField_local_ip));
    validValues_reRouteLookupField.put(
        reRouteLookupField_local_port, new Integer(index_reRouteLookupField_local_port));
    validValues_reRouteLookupField.put(
        reRouteLookupField_remote_ip, new Integer(index_reRouteLookupField_remote_ip));
    validValues_reRouteLookupField.put(
        reRouteLookupField_remote_port, new Integer(index_reRouteLookupField_remote_port));
    validValues_reRouteLookupField.put(
        reRouteLookupField_local_ip_port, new Integer(index_reRouteLookupField_local_ip_port));
    validValues_reRouteLookupField.put(
        reRouteLookupField_remote_ip_port, new Integer(index_reRouteLookupField_remote_ip_port));
    validValues_reRouteLookupField.put(
        reRouteLookupField_routing_classification,
        new Integer(index_reRouteLookupField_routing_classification));
    validValues.put(reRouteLookupField, validValues_reRouteLookupField);

    validValues_reRetransmitCountType.put(
        reRetransmitCountType_INVITE_CLIENT,
        new Integer(index_reRetransmitCountType_INVITE_CLIENT));
    validValues_reRetransmitCountType.put(
        reRetransmitCountType_CLIENT, new Integer(index_reRetransmitCountType_CLIENT));
    validValues_reRetransmitCountType.put(
        reRetransmitCountType_INVITE_SERVER,
        new Integer(index_reRetransmitCountType_INVITE_SERVER));
    validValues.put(reRetransmitCountType, validValues_reRetransmitCountType);

    validValues_reVlanIdentificationDomain.put(
        reVlanIdentificationDomain_default, new Integer(index_reVlanIdentificationDomain_default));
    validValues.put(reVlanIdentificationDomain, validValues_reVlanIdentificationDomain);

    validValues_dsReOnFirewallFailure.put(
        dsReOnFirewallFailure_pass, new Integer(index_dsReOnFirewallFailure_pass));
    validValues_dsReOnFirewallFailure.put(
        dsReOnFirewallFailure_reject, new Integer(index_dsReOnFirewallFailure_reject));
    validValues.put(dsReOnFirewallFailure, validValues_dsReOnFirewallFailure);

    validValues_dsReRecurseOnRedirect.put(
        dsReRecurseOnRedirect_on, new Integer(index_dsReRecurseOnRedirect_on));
    validValues_dsReRecurseOnRedirect.put(
        dsReRecurseOnRedirect_off, new Integer(index_dsReRecurseOnRedirect_off));
    validValues.put(dsReRecurseOnRedirect, validValues_dsReRecurseOnRedirect);

    validValues_reTriggerRequestScheme.put(
        reTriggerRequestScheme_sip, new Integer(index_reTriggerRequestScheme_sip));
    validValues_reTriggerRequestScheme.put(
        reTriggerRequestScheme_tel, new Integer(index_reTriggerRequestScheme_tel));
    validValues.put(reTriggerRequestScheme, validValues_reTriggerRequestScheme);

    validValues_reFirewallQueueRowStatus.put(
        reFirewallQueueRowStatus_on, new Integer(index_reFirewallQueueRowStatus_on));
    validValues_reFirewallQueueRowStatus.put(
        reFirewallQueueRowStatus_off, new Integer(index_reFirewallQueueRowStatus_off));
    validValues.put(reFirewallQueueRowStatus, validValues_reFirewallQueueRowStatus);

    validValues_reSgLbType.put(reSgLbType_global, new Integer(index_reSgLbType_global));
    validValues_reSgLbType.put(reSgLbType_highest_q, new Integer(index_reSgLbType_highest_q));
    validValues_reSgLbType.put(reSgLbType_request_uri, new Integer(index_reSgLbType_request_uri));
    validValues_reSgLbType.put(reSgLbType_call_id, new Integer(index_reSgLbType_call_id));
    validValues_reSgLbType.put(reSgLbType_to_uri, new Integer(index_reSgLbType_to_uri));
    validValues.put(reSgLbType, validValues_reSgLbType);

    validValues_dsReCompactHeaderForm.put(
        dsReCompactHeaderForm_on, new Integer(index_dsReCompactHeaderForm_on));
    validValues_dsReCompactHeaderForm.put(
        dsReCompactHeaderForm_off, new Integer(index_dsReCompactHeaderForm_off));
    validValues.put(dsReCompactHeaderForm, validValues_dsReCompactHeaderForm);

    validValues_reFirewallTimeoutRowStatus.put(
        reFirewallTimeoutRowStatus_on, new Integer(index_reFirewallTimeoutRowStatus_on));
    validValues_reFirewallTimeoutRowStatus.put(
        reFirewallTimeoutRowStatus_off, new Integer(index_reFirewallTimeoutRowStatus_off));
    validValues.put(reFirewallTimeoutRowStatus, validValues_reFirewallTimeoutRowStatus);

    validValues_dsReNatMedia.put(dsReNatMedia_on, new Integer(index_dsReNatMedia_on));
    validValues_dsReNatMedia.put(dsReNatMedia_off, new Integer(index_dsReNatMedia_off));
    validValues_dsReNatMedia.put(dsReNatMedia_internal, new Integer(index_dsReNatMedia_internal));
    validValues.put(dsReNatMedia, validValues_dsReNatMedia);

    validValues_reShutdownResponse.put(
        reShutdownResponse_redirect, new Integer(index_reShutdownResponse_redirect));
    validValues_reShutdownResponse.put(
        reShutdownResponse_reject, new Integer(index_reShutdownResponse_reject));
    validValues.put(reShutdownResponse, validValues_reShutdownResponse);

    validValues_reAddServiceRouteTransport.put(
        reAddServiceRouteTransport_UDP, new Integer(index_reAddServiceRouteTransport_UDP));
    validValues_reAddServiceRouteTransport.put(
        reAddServiceRouteTransport_TCP, new Integer(index_reAddServiceRouteTransport_TCP));
    validValues_reAddServiceRouteTransport.put(
        reAddServiceRouteTransport_TLS, new Integer(index_reAddServiceRouteTransport_TLS));
    validValues.put(reAddServiceRouteTransport, validValues_reAddServiceRouteTransport);

    validValues_reRoutingLookupField.put(
        reRoutingLookupField_ruri_phone, new Integer(index_reRoutingLookupField_ruri_phone));
    validValues_reRoutingLookupField.put(
        reRoutingLookupField_ruri_lrn, new Integer(index_reRoutingLookupField_ruri_lrn));
    validValues_reRoutingLookupField.put(
        reRoutingLookupField_ruri_host, new Integer(index_reRoutingLookupField_ruri_host));
    validValues_reRoutingLookupField.put(
        reRoutingLookupField_ruri_cic, new Integer(index_reRoutingLookupField_ruri_cic));
    validValues_reRoutingLookupField.put(
        reRoutingLookupField_paid_phone, new Integer(index_reRoutingLookupField_paid_phone));
    validValues_reRoutingLookupField.put(
        reRoutingLookupField_diversion_phone,
        new Integer(index_reRoutingLookupField_diversion_phone));
    validValues.put(reRoutingLookupField, validValues_reRoutingLookupField);

    validValues_dsReRollback.put(dsReRollback_previous, new Integer(index_dsReRollback_previous));
    validValues_dsReRollback.put(dsReRollback_changes, new Integer(index_dsReRollback_changes));
    validValues.put(dsReRollback, validValues_dsReRollback);

    validValues_reRoutingEnumField.put(
        reRoutingEnumField_ruri_phone, new Integer(index_reRoutingEnumField_ruri_phone));
    validValues.put(reRoutingEnumField, validValues_reRoutingEnumField);

    validValues_reNormalizationHdrInstance.put(
        reNormalizationHdrInstance_first, new Integer(index_reNormalizationHdrInstance_first));
    validValues_reNormalizationHdrInstance.put(
        reNormalizationHdrInstance_last, new Integer(index_reNormalizationHdrInstance_last));
    validValues_reNormalizationHdrInstance.put(
        reNormalizationHdrInstance_all, new Integer(index_reNormalizationHdrInstance_all));
    validValues.put(reNormalizationHdrInstance, validValues_reNormalizationHdrInstance);

    validValues_reRouteEnumField.put(
        reRouteEnumField_ruri, new Integer(index_reRouteEnumField_ruri));
    validValues_reRouteEnumField.put(
        reRouteEnumField_ruri_phone, new Integer(index_reRouteEnumField_ruri_phone));
    validValues_reRouteEnumField.put(
        reRouteEnumField_ruri_lrn, new Integer(index_reRouteEnumField_ruri_lrn));
    validValues_reRouteEnumField.put(
        reRouteEnumField_ruri_cic, new Integer(index_reRouteEnumField_ruri_cic));
    validValues_reRouteEnumField.put(
        reRouteEnumField_diversion, new Integer(index_reRouteEnumField_diversion));
    validValues_reRouteEnumField.put(
        reRouteEnumField_diversion_phone, new Integer(index_reRouteEnumField_diversion_phone));
    validValues_reRouteEnumField.put(
        reRouteEnumField_diversion_lrn, new Integer(index_reRouteEnumField_diversion_lrn));
    validValues_reRouteEnumField.put(
        reRouteEnumField_diversion_cic, new Integer(index_reRouteEnumField_diversion_cic));
    validValues_reRouteEnumField.put(
        reRouteEnumField_paid, new Integer(index_reRouteEnumField_paid));
    validValues_reRouteEnumField.put(
        reRouteEnumField_paid_phone, new Integer(index_reRouteEnumField_paid_phone));
    validValues_reRouteEnumField.put(
        reRouteEnumField_paid_lrn, new Integer(index_reRouteEnumField_paid_lrn));
    validValues_reRouteEnumField.put(
        reRouteEnumField_paid_cic, new Integer(index_reRouteEnumField_paid_cic));
    validValues_reRouteEnumField.put(
        reRouteEnumField_rpid, new Integer(index_reRouteEnumField_rpid));
    validValues_reRouteEnumField.put(
        reRouteEnumField_rpid_phone, new Integer(index_reRouteEnumField_rpid_phone));
    validValues_reRouteEnumField.put(
        reRouteEnumField_rpid_lrn, new Integer(index_reRouteEnumField_rpid_lrn));
    validValues_reRouteEnumField.put(
        reRouteEnumField_rpid_cic, new Integer(index_reRouteEnumField_rpid_cic));
    validValues_reRouteEnumField.put(
        reRouteEnumField_from, new Integer(index_reRouteEnumField_from));
    validValues_reRouteEnumField.put(
        reRouteEnumField_from_phone, new Integer(index_reRouteEnumField_from_phone));
    validValues_reRouteEnumField.put(
        reRouteEnumField_from_lrn, new Integer(index_reRouteEnumField_from_lrn));
    validValues_reRouteEnumField.put(
        reRouteEnumField_from_cic, new Integer(index_reRouteEnumField_from_cic));
    validValues_reRouteEnumField.put(
        reRouteEnumField_originating_uri, new Integer(index_reRouteEnumField_originating_uri));
    validValues_reRouteEnumField.put(
        reRouteEnumField_ruri_routing_number,
        new Integer(index_reRouteEnumField_ruri_routing_number));
    validValues_reRouteEnumField.put(
        reRouteEnumField_diversion_routing_number,
        new Integer(index_reRouteEnumField_diversion_routing_number));
    validValues_reRouteEnumField.put(
        reRouteEnumField_paid_routing_number,
        new Integer(index_reRouteEnumField_paid_routing_number));
    validValues_reRouteEnumField.put(
        reRouteEnumField_rpid_routing_number,
        new Integer(index_reRouteEnumField_rpid_routing_number));
    validValues_reRouteEnumField.put(
        reRouteEnumField_from_routing_number,
        new Integer(index_reRouteEnumField_from_routing_number));
    validValues.put(reRouteEnumField, validValues_reRouteEnumField);

    validValues_reRouteLookup.put(reRouteLookup_y, new Integer(index_reRouteLookup_y));
    validValues_reRouteLookup.put(reRouteLookup_yes, new Integer(index_reRouteLookup_yes));
    validValues.put(reRouteLookup, validValues_reRouteLookup);

    validValues_reNormalizationField.put(
        reNormalizationField_hdr_parameter, new Integer(index_reNormalizationField_hdr_parameter));
    validValues_reNormalizationField.put(
        reNormalizationField_uri_parameter, new Integer(index_reNormalizationField_uri_parameter));
    validValues_reNormalizationField.put(
        reNormalizationField_uri, new Integer(index_reNormalizationField_uri));
    validValues.put(reNormalizationField, validValues_reNormalizationField);

    validValues_dsRePerformanceData.put(
        dsRePerformanceData_on, new Integer(index_dsRePerformanceData_on));
    validValues_dsRePerformanceData.put(
        dsRePerformanceData_off, new Integer(index_dsRePerformanceData_off));
    validValues_dsRePerformanceData.put(
        dsRePerformanceData_print, new Integer(index_dsRePerformanceData_print));
    validValues_dsRePerformanceData.put(
        dsRePerformanceData_reset, new Integer(index_dsRePerformanceData_reset));
    validValues.put(dsRePerformanceData, validValues_dsRePerformanceData);

    validValues_reModifyServiceRouteTransport.put(
        reModifyServiceRouteTransport_UDP, new Integer(index_reModifyServiceRouteTransport_UDP));
    validValues_reModifyServiceRouteTransport.put(
        reModifyServiceRouteTransport_TCP, new Integer(index_reModifyServiceRouteTransport_TCP));
    validValues_reModifyServiceRouteTransport.put(
        reModifyServiceRouteTransport_TLS, new Integer(index_reModifyServiceRouteTransport_TLS));
    validValues.put(reModifyServiceRouteTransport, validValues_reModifyServiceRouteTransport);

    validValues_reShutdownRedirectTransport.put(
        reShutdownRedirectTransport_UDP, new Integer(index_reShutdownRedirectTransport_UDP));
    validValues_reShutdownRedirectTransport.put(
        reShutdownRedirectTransport_TCP, new Integer(index_reShutdownRedirectTransport_TCP));
    validValues_reShutdownRedirectTransport.put(
        reShutdownRedirectTransport_TLS, new Integer(index_reShutdownRedirectTransport_TLS));
    validValues.put(reShutdownRedirectTransport, validValues_reShutdownRedirectTransport);

    validValues_reRoutingEnum.put(reRoutingEnum_y, new Integer(index_reRoutingEnum_y));
    validValues_reRoutingEnum.put(reRoutingEnum_yes, new Integer(index_reRoutingEnum_yes));
    validValues.put(reRoutingEnum, validValues_reRoutingEnum);

    validValues_reModuleTriggerModuleName.put(
        reModuleTriggerModuleName_authentication,
        new Integer(index_reModuleTriggerModuleName_authentication));
    validValues_reModuleTriggerModuleName.put(
        reModuleTriggerModuleName_default_route,
        new Integer(index_reModuleTriggerModuleName_default_route));
    validValues_reModuleTriggerModuleName.put(
        reModuleTriggerModuleName_filter_in_request,
        new Integer(index_reModuleTriggerModuleName_filter_in_request));
    validValues_reModuleTriggerModuleName.put(
        reModuleTriggerModuleName_filter_out_request,
        new Integer(index_reModuleTriggerModuleName_filter_out_request));
    validValues_reModuleTriggerModuleName.put(
        reModuleTriggerModuleName_filter_out_response,
        new Integer(index_reModuleTriggerModuleName_filter_out_response));
    validValues_reModuleTriggerModuleName.put(
        reModuleTriggerModuleName_identity_assertion,
        new Integer(index_reModuleTriggerModuleName_identity_assertion));
    validValues_reModuleTriggerModuleName.put(
        reModuleTriggerModuleName_network_direction,
        new Integer(index_reModuleTriggerModuleName_network_direction));
    validValues_reModuleTriggerModuleName.put(
        reModuleTriggerModuleName_privacy, new Integer(index_reModuleTriggerModuleName_privacy));
    validValues_reModuleTriggerModuleName.put(
        reModuleTriggerModuleName_record_route_param,
        new Integer(index_reModuleTriggerModuleName_record_route_param));
    validValues_reModuleTriggerModuleName.put(
        reModuleTriggerModuleName_client_type,
        new Integer(index_reModuleTriggerModuleName_client_type));
    validValues_reModuleTriggerModuleName.put(
        reModuleTriggerModuleName_pcmm_orig_req,
        new Integer(index_reModuleTriggerModuleName_pcmm_orig_req));
    validValues_reModuleTriggerModuleName.put(
        reModuleTriggerModuleName_pcmm_orig_resp,
        new Integer(index_reModuleTriggerModuleName_pcmm_orig_resp));
    validValues_reModuleTriggerModuleName.put(
        reModuleTriggerModuleName_pcmm_params,
        new Integer(index_reModuleTriggerModuleName_pcmm_params));
    validValues_reModuleTriggerModuleName.put(
        reModuleTriggerModuleName_pcmm_term_req,
        new Integer(index_reModuleTriggerModuleName_pcmm_term_req));
    validValues_reModuleTriggerModuleName.put(
        reModuleTriggerModuleName_pcmm_term_resp,
        new Integer(index_reModuleTriggerModuleName_pcmm_term_resp));
    validValues_reModuleTriggerModuleName.put(
        reModuleTriggerModuleName_routing, new Integer(index_reModuleTriggerModuleName_routing));
    validValues_reModuleTriggerModuleName.put(
        reModuleTriggerModuleName_routing_classification,
        new Integer(index_reModuleTriggerModuleName_routing_classification));
    validValues_reModuleTriggerModuleName.put(
        reModuleTriggerModuleName_post_normalize,
        new Integer(index_reModuleTriggerModuleName_post_normalize));
    validValues_reModuleTriggerModuleName.put(
        reModuleTriggerModuleName_pre_normalize,
        new Integer(index_reModuleTriggerModuleName_pre_normalize));
    validValues_reModuleTriggerModuleName.put(
        reModuleTriggerModuleName_radius_server_request,
        new Integer(index_reModuleTriggerModuleName_radius_server_request));
    validValues_reModuleTriggerModuleName.put(
        reModuleTriggerModuleName_radius_server_response,
        new Integer(index_reModuleTriggerModuleName_radius_server_response));
    validValues_reModuleTriggerModuleName.put(
        reModuleTriggerModuleName_radius_client_request,
        new Integer(index_reModuleTriggerModuleName_radius_client_request));
    validValues_reModuleTriggerModuleName.put(
        reModuleTriggerModuleName_radius_client_response,
        new Integer(index_reModuleTriggerModuleName_radius_client_response));
    validValues_reModuleTriggerModuleName.put(
        reModuleTriggerModuleName_N11, new Integer(index_reModuleTriggerModuleName_N11));
    validValues_reModuleTriggerModuleName.put(
        reModuleTriggerModuleName_privacy_in,
        new Integer(index_reModuleTriggerModuleName_privacy_in));
    validValues_reModuleTriggerModuleName.put(
        reModuleTriggerModuleName_privacy_out,
        new Integer(index_reModuleTriggerModuleName_privacy_out));
    validValues.put(reModuleTriggerModuleName, validValues_reModuleTriggerModuleName);

    validValues_reAccountingSipHdrSource.put(
        reAccountingSipHdrSource_REQ, new Integer(index_reAccountingSipHdrSource_REQ));
    validValues_reAccountingSipHdrSource.put(
        reAccountingSipHdrSource_RESP, new Integer(index_reAccountingSipHdrSource_RESP));
    validValues.put(reAccountingSipHdrSource, validValues_reAccountingSipHdrSource);

    validValues_dsReConnectionBasedSelection.put(
        dsReConnectionBasedSelection_on, new Integer(index_dsReConnectionBasedSelection_on));
    validValues_dsReConnectionBasedSelection.put(
        dsReConnectionBasedSelection_off, new Integer(index_dsReConnectionBasedSelection_off));
    validValues.put(dsReConnectionBasedSelection, validValues_dsReConnectionBasedSelection);

    validValues_reRouteLookupRule.put(
        reRouteLookupRule_exact, new Integer(index_reRouteLookupRule_exact));
    validValues_reRouteLookupRule.put(
        reRouteLookupRule_fixed, new Integer(index_reRouteLookupRule_fixed));
    validValues_reRouteLookupRule.put(
        reRouteLookupRule_prefix, new Integer(index_reRouteLookupRule_prefix));
    validValues_reRouteLookupRule.put(
        reRouteLookupRule_subdomain, new Integer(index_reRouteLookupRule_subdomain));
    validValues_reRouteLookupRule.put(
        reRouteLookupRule_subnet, new Integer(index_reRouteLookupRule_subnet));
    validValues.put(reRouteLookupRule, validValues_reRouteLookupRule);

    validValues_reRoutingNrsMap.put(reRoutingNrsMap_y, new Integer(index_reRoutingNrsMap_y));
    validValues_reRoutingNrsMap.put(reRoutingNrsMap_yes, new Integer(index_reRoutingNrsMap_yes));
    validValues.put(reRoutingNrsMap, validValues_reRoutingNrsMap);

    validValues_reModuleTriggerTriggerName.put(
        reModuleTriggerTriggerName_default, new Integer(index_reModuleTriggerTriggerName_default));
    validValues.put(reModuleTriggerTriggerName, validValues_reModuleTriggerTriggerName);

    validValues_dsReOnNextHopFailure.put(
        dsReOnNextHopFailure_drop, new Integer(index_dsReOnNextHopFailure_drop));
    validValues_dsReOnNextHopFailure.put(
        dsReOnNextHopFailure_failover, new Integer(index_dsReOnNextHopFailure_failover));
    validValues.put(dsReOnNextHopFailure, validValues_dsReOnNextHopFailure);

    validValues_dsReAccountingServerSide.put(
        dsReAccountingServerSide_on, new Integer(index_dsReAccountingServerSide_on));
    validValues_dsReAccountingServerSide.put(
        dsReAccountingServerSide_off, new Integer(index_dsReAccountingServerSide_off));
    validValues.put(dsReAccountingServerSide, validValues_dsReAccountingServerSide);

    validValues_reRecordRouteTransport.put(
        reRecordRouteTransport_UDP, new Integer(index_reRecordRouteTransport_UDP));
    validValues_reRecordRouteTransport.put(
        reRecordRouteTransport_TCP, new Integer(index_reRecordRouteTransport_TCP));
    validValues_reRecordRouteTransport.put(
        reRecordRouteTransport_TLS, new Integer(index_reRecordRouteTransport_TLS));
    validValues.put(reRecordRouteTransport, validValues_reRecordRouteTransport);

    validValues_reModuleTriggerAction.put(
        reModuleTriggerAction_add, new Integer(index_reModuleTriggerAction_add));
    validValues_reModuleTriggerAction.put(
        reModuleTriggerAction_set, new Integer(index_reModuleTriggerAction_set));
    validValues_reModuleTriggerAction.put(
        reModuleTriggerAction_reserve, new Integer(index_reModuleTriggerAction_reserve));
    validValues_reModuleTriggerAction.put(
        reModuleTriggerAction_release, new Integer(index_reModuleTriggerAction_release));
    validValues_reModuleTriggerAction.put(
        reModuleTriggerAction_cancel, new Integer(index_reModuleTriggerAction_cancel));
    validValues_reModuleTriggerAction.put(
        reModuleTriggerAction_commit, new Integer(index_reModuleTriggerAction_commit));
    validValues_reModuleTriggerAction.put(
        reModuleTriggerAction_assert_pass_through,
        new Integer(index_reModuleTriggerAction_assert_pass_through));
    validValues_reModuleTriggerAction.put(
        reModuleTriggerAction_assert_reject,
        new Integer(index_reModuleTriggerAction_assert_reject));
    validValues_reModuleTriggerAction.put(
        reModuleTriggerAction_auth_pass_through,
        new Integer(index_reModuleTriggerAction_auth_pass_through));
    validValues_reModuleTriggerAction.put(
        reModuleTriggerAction_auth_reject, new Integer(index_reModuleTriggerAction_auth_reject));
    validValues_reModuleTriggerAction.put(
        reModuleTriggerAction_call_type, new Integer(index_reModuleTriggerAction_call_type));
    validValues_reModuleTriggerAction.put(
        reModuleTriggerAction_dsedge_auth, new Integer(index_reModuleTriggerAction_dsedge_auth));
    validValues_reModuleTriggerAction.put(
        reModuleTriggerAction_dsedge_outbound,
        new Integer(index_reModuleTriggerAction_dsedge_outbound));
    validValues_reModuleTriggerAction.put(
        reModuleTriggerAction_identity_default,
        new Integer(index_reModuleTriggerAction_identity_default));
    validValues_reModuleTriggerAction.put(
        reModuleTriggerAction_none, new Integer(index_reModuleTriggerAction_none));
    validValues_reModuleTriggerAction.put(
        reModuleTriggerAction_privacy_assert,
        new Integer(index_reModuleTriggerAction_privacy_assert));
    validValues_reModuleTriggerAction.put(
        reModuleTriggerAction_privacy_none, new Integer(index_reModuleTriggerAction_privacy_none));
    validValues_reModuleTriggerAction.put(
        reModuleTriggerAction_privacy_service,
        new Integer(index_reModuleTriggerAction_privacy_service));
    validValues_reModuleTriggerAction.put(
        reModuleTriggerAction_remove_header,
        new Integer(index_reModuleTriggerAction_remove_header));
    validValues_reModuleTriggerAction.put(
        reModuleTriggerAction_undetermined, new Integer(index_reModuleTriggerAction_undetermined));
    validValues_reModuleTriggerAction.put(
        reModuleTriggerAction_sticky, new Integer(index_reModuleTriggerAction_sticky));
    validValues_reModuleTriggerAction.put(
        reModuleTriggerAction_non_sticky, new Integer(index_reModuleTriggerAction_non_sticky));
    validValues_reModuleTriggerAction.put(
        reModuleTriggerAction_911, new Integer(index_reModuleTriggerAction_911));
    validValues_reModuleTriggerAction.put(
        reModuleTriggerAction_911_esrn_cli, new Integer(index_reModuleTriggerAction_911_esrn_cli));
    validValues_reModuleTriggerAction.put(
        reModuleTriggerAction_911_esrn_das, new Integer(index_reModuleTriggerAction_911_esrn_das));
    validValues_reModuleTriggerAction.put(
        reModuleTriggerAction_app_dispatch, new Integer(index_reModuleTriggerAction_app_dispatch));
    validValues_reModuleTriggerAction.put(
        reModuleTriggerAction_radius_start, new Integer(index_reModuleTriggerAction_radius_start));
    validValues_reModuleTriggerAction.put(
        reModuleTriggerAction_radius_interim,
        new Integer(index_reModuleTriggerAction_radius_interim));
    validValues_reModuleTriggerAction.put(
        reModuleTriggerAction_radius_stop, new Integer(index_reModuleTriggerAction_radius_stop));
    validValues_reModuleTriggerAction.put(
        reModuleTriggerAction_radius_stop_all_attributes,
        new Integer(index_reModuleTriggerAction_radius_stop_all_attributes));
    validValues_reModuleTriggerAction.put(
        reModuleTriggerAction_routing_classification,
        new Integer(index_reModuleTriggerAction_routing_classification));
    validValues.put(reModuleTriggerAction, validValues_reModuleTriggerAction);

    validValues_reTimeWkst.put(reTimeWkst_MO, new Integer(index_reTimeWkst_MO));
    validValues_reTimeWkst.put(reTimeWkst_TU, new Integer(index_reTimeWkst_TU));
    validValues_reTimeWkst.put(reTimeWkst_WE, new Integer(index_reTimeWkst_WE));
    validValues_reTimeWkst.put(reTimeWkst_TH, new Integer(index_reTimeWkst_TH));
    validValues_reTimeWkst.put(reTimeWkst_FR, new Integer(index_reTimeWkst_FR));
    validValues_reTimeWkst.put(reTimeWkst_SA, new Integer(index_reTimeWkst_SA));
    validValues_reTimeWkst.put(reTimeWkst_SU, new Integer(index_reTimeWkst_SU));
    validValues.put(reTimeWkst, validValues_reTimeWkst);

    validValues_dsReAccountingClientSide.put(
        dsReAccountingClientSide_on, new Integer(index_dsReAccountingClientSide_on));
    validValues_dsReAccountingClientSide.put(
        dsReAccountingClientSide_off, new Integer(index_dsReAccountingClientSide_off));
    validValues.put(dsReAccountingClientSide, validValues_dsReAccountingClientSide);

    validValues_reViaTransport.put(reViaTransport_UDP, new Integer(index_reViaTransport_UDP));
    validValues_reViaTransport.put(reViaTransport_TCP, new Integer(index_reViaTransport_TCP));
    validValues_reViaTransport.put(reViaTransport_TLS, new Integer(index_reViaTransport_TLS));
    validValues.put(reViaTransport, validValues_reViaTransport);

    validValues_rePopIdRowStatus.put(rePopIdRowStatus_on, new Integer(index_rePopIdRowStatus_on));
    validValues_rePopIdRowStatus.put(rePopIdRowStatus_off, new Integer(index_rePopIdRowStatus_off));
    validValues.put(rePopIdRowStatus, validValues_rePopIdRowStatus);

    validValues_reRouteNrsMapField.put(
        reRouteNrsMapField_ruri, new Integer(index_reRouteNrsMapField_ruri));
    validValues_reRouteNrsMapField.put(
        reRouteNrsMapField_ruri_phone, new Integer(index_reRouteNrsMapField_ruri_phone));
    validValues_reRouteNrsMapField.put(
        reRouteNrsMapField_ruri_lrn, new Integer(index_reRouteNrsMapField_ruri_lrn));
    validValues_reRouteNrsMapField.put(
        reRouteNrsMapField_ruri_cic, new Integer(index_reRouteNrsMapField_ruri_cic));
    validValues_reRouteNrsMapField.put(
        reRouteNrsMapField_diversion, new Integer(index_reRouteNrsMapField_diversion));
    validValues_reRouteNrsMapField.put(
        reRouteNrsMapField_diversion_phone, new Integer(index_reRouteNrsMapField_diversion_phone));
    validValues_reRouteNrsMapField.put(
        reRouteNrsMapField_diversion_lrn, new Integer(index_reRouteNrsMapField_diversion_lrn));
    validValues_reRouteNrsMapField.put(
        reRouteNrsMapField_diversion_cic, new Integer(index_reRouteNrsMapField_diversion_cic));
    validValues_reRouteNrsMapField.put(
        reRouteNrsMapField_paid, new Integer(index_reRouteNrsMapField_paid));
    validValues_reRouteNrsMapField.put(
        reRouteNrsMapField_paid_phone, new Integer(index_reRouteNrsMapField_paid_phone));
    validValues_reRouteNrsMapField.put(
        reRouteNrsMapField_paid_lrn, new Integer(index_reRouteNrsMapField_paid_lrn));
    validValues_reRouteNrsMapField.put(
        reRouteNrsMapField_paid_cic, new Integer(index_reRouteNrsMapField_paid_cic));
    validValues_reRouteNrsMapField.put(
        reRouteNrsMapField_rpid, new Integer(index_reRouteNrsMapField_rpid));
    validValues_reRouteNrsMapField.put(
        reRouteNrsMapField_rpid_phone, new Integer(index_reRouteNrsMapField_rpid_phone));
    validValues_reRouteNrsMapField.put(
        reRouteNrsMapField_rpid_lrn, new Integer(index_reRouteNrsMapField_rpid_lrn));
    validValues_reRouteNrsMapField.put(
        reRouteNrsMapField_rpid_cic, new Integer(index_reRouteNrsMapField_rpid_cic));
    validValues_reRouteNrsMapField.put(
        reRouteNrsMapField_from, new Integer(index_reRouteNrsMapField_from));
    validValues_reRouteNrsMapField.put(
        reRouteNrsMapField_from_phone, new Integer(index_reRouteNrsMapField_from_phone));
    validValues_reRouteNrsMapField.put(
        reRouteNrsMapField_from_lrn, new Integer(index_reRouteNrsMapField_from_lrn));
    validValues_reRouteNrsMapField.put(
        reRouteNrsMapField_from_cic, new Integer(index_reRouteNrsMapField_from_cic));
    validValues_reRouteNrsMapField.put(
        reRouteNrsMapField_originating_uri, new Integer(index_reRouteNrsMapField_originating_uri));
    validValues_reRouteNrsMapField.put(
        reRouteNrsMapField_ruri_routing_number,
        new Integer(index_reRouteNrsMapField_ruri_routing_number));
    validValues_reRouteNrsMapField.put(
        reRouteNrsMapField_diversion_routing_number,
        new Integer(index_reRouteNrsMapField_diversion_routing_number));
    validValues_reRouteNrsMapField.put(
        reRouteNrsMapField_paid_routing_number,
        new Integer(index_reRouteNrsMapField_paid_routing_number));
    validValues_reRouteNrsMapField.put(
        reRouteNrsMapField_rpid_routing_number,
        new Integer(index_reRouteNrsMapField_rpid_routing_number));
    validValues_reRouteNrsMapField.put(
        reRouteNrsMapField_from_routing_number,
        new Integer(index_reRouteNrsMapField_from_routing_number));
    validValues.put(reRouteNrsMapField, validValues_reRouteNrsMapField);

    validValues_reNetworkConnection.put(
        reNetworkConnection_true, new Integer(index_reNetworkConnection_true));
    validValues_reNetworkConnection.put(
        reNetworkConnection_false, new Integer(index_reNetworkConnection_false));
    validValues.put(reNetworkConnection, validValues_reNetworkConnection);

    validValues_reTimeFreq.put(reTimeFreq_daily, new Integer(index_reTimeFreq_daily));
    validValues_reTimeFreq.put(reTimeFreq_weekly, new Integer(index_reTimeFreq_weekly));
    validValues_reTimeFreq.put(reTimeFreq_monthly, new Integer(index_reTimeFreq_monthly));
    validValues_reTimeFreq.put(reTimeFreq_yearly, new Integer(index_reTimeFreq_yearly));
    validValues.put(reTimeFreq, validValues_reTimeFreq);

    validValues_reRoutingLookupKeyModifier.put(
        reRoutingLookupKeyModifier_ci, new Integer(index_reRoutingLookupKeyModifier_ci));
    validValues_reRoutingLookupKeyModifier.put(
        reRoutingLookupKeyModifier_ignore_plus,
        new Integer(index_reRoutingLookupKeyModifier_ignore_plus));
    validValues_reRoutingLookupKeyModifier.put(
        reRoutingLookupKeyModifier_tel, new Integer(index_reRoutingLookupKeyModifier_tel));
    validValues.put(reRoutingLookupKeyModifier, validValues_reRoutingLookupKeyModifier);

    validValues_reShutdownOnOrOff.put(
        reShutdownOnOrOff_on, new Integer(index_reShutdownOnOrOff_on));
    validValues_reShutdownOnOrOff.put(
        reShutdownOnOrOff_off, new Integer(index_reShutdownOnOrOff_off));
    validValues.put(reShutdownOnOrOff, validValues_reShutdownOnOrOff);

    validValues_reSgElementTransport.put(
        reSgElementTransport_UDP, new Integer(index_reSgElementTransport_UDP));
    validValues_reSgElementTransport.put(
        reSgElementTransport_TCP, new Integer(index_reSgElementTransport_TCP));
    validValues_reSgElementTransport.put(
        reSgElementTransport_TLS, new Integer(index_reSgElementTransport_TLS));
    validValues.put(reSgElementTransport, validValues_reSgElementTransport);

    validValues_reSgPing.put(reSgPing_on, new Integer(index_reSgPing_on));
    validValues_reSgPing.put(reSgPing_off, new Integer(index_reSgPing_off));
    validValues.put(reSgPing, validValues_reSgPing);

    validValues_reSetRootXCLRoute.put(
        reSetRootXCLRoute_yes, new Integer(index_reSetRootXCLRoute_yes));
    validValues_reSetRootXCLRoute.put(
        reSetRootXCLRoute_no, new Integer(index_reSetRootXCLRoute_no));
    validValues.put(reSetRootXCLRoute, validValues_reSetRootXCLRoute);

    validValues_reEnumRootRecurse.put(
        reEnumRootRecurse_true, new Integer(index_reEnumRootRecurse_true));
    validValues_reEnumRootRecurse.put(
        reEnumRootRecurse_false, new Integer(index_reEnumRootRecurse_false));
    validValues.put(reEnumRootRecurse, validValues_reEnumRootRecurse);

    validValues_dsReStateMode.put(
        dsReStateMode_stateful, new Integer(index_dsReStateMode_stateful));
    validValues_dsReStateMode.put(
        dsReStateMode_stateless, new Integer(index_dsReStateMode_stateless));
    validValues_dsReStateMode.put(
        dsReStateMode_failover_stateful, new Integer(index_dsReStateMode_failover_stateful));
    validValues.put(dsReStateMode, validValues_dsReStateMode);

    validValues_dsReSend305.put(dsReSend305_on, new Integer(index_dsReSend305_on));
    validValues_dsReSend305.put(
        dsReSend305_reject_secondary, new Integer(index_dsReSend305_reject_secondary));
    validValues_dsReSend305.put(dsReSend305_off, new Integer(index_dsReSend305_off));
    validValues.put(dsReSend305, validValues_dsReSend305);

    validValues_dsReQos.put(dsReQos_on, new Integer(index_dsReQos_on));
    validValues_dsReQos.put(dsReQos_off, new Integer(index_dsReQos_off));
    validValues.put(dsReQos, validValues_dsReQos);

    validValues_dsReSend100.put(dsReSend100_on, new Integer(index_dsReSend100_on));
    validValues_dsReSend100.put(dsReSend100_off, new Integer(index_dsReSend100_off));
    validValues.put(dsReSend100, validValues_dsReSend100);

    validValues_reRoutingNrsMapField.put(
        reRoutingNrsMapField_ruri, new Integer(index_reRoutingNrsMapField_ruri));
    validValues.put(reRoutingNrsMapField, validValues_reRoutingNrsMapField);

    defaults.put(dsReOnNextHopFailure, new java.lang.String(dsReOnNextHopFailureDefault));
    defaults.put(reRadiusSgElementPort, new java.lang.Integer(reRadiusSgElementPortDefault));
    defaults.put(reFirewallGroupDomain, new java.lang.String(reFirewallGroupDomainDefault));
    defaults.put(reFirewallQueueSize, new java.lang.Integer(reFirewallQueueSizeDefault));
    defaults.put(rePopIdRowStatus, new java.lang.String(rePopIdRowStatusDefault));
    defaults.put(reListenTransport, new java.lang.String(reListenTransportDefault));
    defaults.put(dsReFirewallMode, new java.lang.String(dsReFirewallModeDefault));
    defaults.put(reNetworkConnection, new java.lang.String(reNetworkConnectionDefault));
    defaults.put(dsReOnFirewallFailure, new java.lang.String(dsReOnFirewallFailureDefault));
    defaults.put(reRouteElementFailover, new java.lang.String(reRouteElementFailoverDefault));
    defaults.put(dsReStateMode, new java.lang.String(dsReStateModeDefault));
    defaults.put(reSgElementPort, new java.lang.Integer(reSgElementPortDefault));
    defaults.put(
        reQosSgElementConnectionPoolSize,
        new java.lang.Integer(reQosSgElementConnectionPoolSizeDefault));
    defaults.put(reEnumRootTimeout, new java.lang.Integer(reEnumRootTimeoutDefault));
    defaults.put(reEnumRootAttempts, new java.lang.Integer(reEnumRootAttemptsDefault));
    defaults.put(reMaskHeader, new java.lang.String(reMaskHeaderDefault));
    defaults.put(dsReSend305, new java.lang.String(dsReSend305Default));
    defaults.put(reNetworkType, new java.lang.String(reNetworkTypeDefault));
    defaults.put(reShutdownRetryAfter, new java.lang.Integer(reShutdownRetryAfterDefault));
    defaults.put(reNormalizationSequence, new java.lang.Integer(reNormalizationSequenceDefault));
    defaults.put(dsReCompactHeaderForm, new java.lang.String(dsReCompactHeaderFormDefault));
    defaults.put(reNormalizationMsgType, new java.lang.String(reNormalizationMsgTypeDefault));
    defaults.put(
        reQosSgElementPipelineSize, new java.lang.Integer(reQosSgElementPipelineSizeDefault));
    defaults.put(
        reFirewallQueueMaxThreads, new java.lang.Integer(reFirewallQueueMaxThreadsDefault));
    defaults.put(reListenPort, new java.lang.Integer(reListenPortDefault));
    defaults.put(reSgLbType, new java.lang.String(reSgLbTypeDefault));
    defaults.put(reQosSgElementTimeout, new java.lang.Integer(reQosSgElementTimeoutDefault));
    defaults.put(
        reNormalizationHdrInstance, new java.lang.String(reNormalizationHdrInstanceDefault));
    defaults.put(
        reShutdownRedirectTransport, new java.lang.String(reShutdownRedirectTransportDefault));
    defaults.put(
        reFirewallTimeoutSessionTimeout,
        new java.lang.Integer(reFirewallTimeoutSessionTimeoutDefault));
    defaults.put(dsReAccountingServerSide, new java.lang.String(dsReAccountingServerSideDefault));
    defaults.put(
        dsReConnectionBasedSelection, new java.lang.String(dsReConnectionBasedSelectionDefault));
    defaults.put(dsReAccounting, new java.lang.String(dsReAccountingDefault));
    defaults.put(dsReMinContactExpires, new java.lang.Integer(dsReMinContactExpiresDefault));
    defaults.put(reXclQueueStatus, new java.lang.String(reXclQueueStatusDefault));
    defaults.put(reQosSgElementQValue, new java.lang.Float(reQosSgElementQValueDefault));
    defaults.put(reTimeInterval, new java.lang.Integer(reTimeIntervalDefault));
    defaults.put(
        reRadiusSgElementRetxCount, new java.lang.Integer(reRadiusSgElementRetxCountDefault));
    defaults.put(
        reRadiusSgElementRetxTimeout, new java.lang.Integer(reRadiusSgElementRetxTimeoutDefault));
    defaults.put(
        reFirewallTimeoutSessionClose, new java.lang.Integer(reFirewallTimeoutSessionCloseDefault));
    defaults.put(dsReQos, new java.lang.String(dsReQosDefault));
    defaults.put(
        reVlanIdentificationDomain, new java.lang.String(reVlanIdentificationDomainDefault));
    defaults.put(reRouteSequence, new java.lang.Integer(reRouteSequenceDefault));
    defaults.put(
        dsReDefaultContactExpires, new java.lang.Integer(dsReDefaultContactExpiresDefault));
    defaults.put(reSgElementTransport, new java.lang.String(reSgElementTransportDefault));
    defaults.put(reXclQueueSize, new java.lang.Integer(reXclQueueSizeDefault));
    defaults.put(reEnumRootRecurse, new java.lang.String(reEnumRootRecurseDefault));
    defaults.put(reShutdownRedirectPort, new java.lang.Integer(reShutdownRedirectPortDefault));
    defaults.put(
        reEnumRootExponentialBackOff, new java.lang.String(reEnumRootExponentialBackOffDefault));
    defaults.put(reRecordRouteTransport, new java.lang.String(reRecordRouteTransportDefault));
    defaults.put(reMaxPinholeBandwidthBits, new java.lang.Long(reMaxPinholeBandwidthBitsDefault));
    defaults.put(dsReMaxMediaDescriptions, new java.lang.Integer(dsReMaxMediaDescriptionsDefault));
    defaults.put(reRoutingSequence, new java.lang.Integer(reRoutingSequenceDefault));
    defaults.put(dsReMaxContactExpires, new java.lang.Integer(dsReMaxContactExpiresDefault));
    defaults.put(reAccountingSipHdrSource, new java.lang.String(reAccountingSipHdrSourceDefault));
    defaults.put(reShutdownOnOrOff, new java.lang.String(reShutdownOnOrOffDefault));
    defaults.put(dsReSend100, new java.lang.String(dsReSend100Default));
    defaults.put(dsReNatMedia, new java.lang.String(dsReNatMediaDefault));
    defaults.put(reSetRootXCLRoute, new java.lang.String(reSetRootXCLRouteDefault));
    defaults.put(reFirewallQueueBusyReset, new java.lang.Integer(reFirewallQueueBusyResetDefault));
    defaults.put(reEnumServerQValue, new java.lang.Float(reEnumServerQValueDefault));
    defaults.put(reSgPing, new java.lang.String(reSgPingDefault));
    defaults.put(reViaPort, new java.lang.Integer(reViaPortDefault));
    defaults.put(reSgElementQValue, new java.lang.Float(reSgElementQValueDefault));
    defaults.put(reXclQueueThreadCount, new java.lang.Integer(reXclQueueThreadCountDefault));
    defaults.put(reFirewallQueueRowStatus, new java.lang.String(reFirewallQueueRowStatusDefault));
    defaults.put(reEnumServerPort, new java.lang.Integer(reEnumServerPortDefault));
    defaults.put(dsReRecurseOnRedirect, new java.lang.String(dsReRecurseOnRedirectDefault));
    defaults.put(dsReDefaultQValue, new java.lang.Float(dsReDefaultQValueDefault));
    defaults.put(reRouteElementQValue, new java.lang.Float(reRouteElementQValueDefault));
    defaults.put(reViaTransport, new java.lang.String(reViaTransportDefault));
    defaults.put(dsReAccountingClientSide, new java.lang.String(dsReAccountingClientSideDefault));
    defaults.put(
        reFirewallTimeoutRowStatus, new java.lang.String(reFirewallTimeoutRowStatusDefault));
    defaults.put(reTimeWkst, new java.lang.String(reTimeWkstDefault));
    defaults.put(dsReConnectionCacheSize, new java.lang.Integer(dsReConnectionCacheSizeDefault));
    defaults.put(
        reModuleTriggerTriggerName, new java.lang.String(reModuleTriggerTriggerNameDefault));
    defaults.put(reRadiusSgElementQValue, new java.lang.Float(reRadiusSgElementQValueDefault));
    defaults.put(dsReMaxIterationDepth, new java.lang.Integer(dsReMaxIterationDepthDefault));

    mins.put(reTriggerSequenceNo, new java.lang.Integer(reTriggerSequenceNoMin));
    mins.put(reShutdownTime, new java.lang.Integer(reShutdownTimeMin));
    mins.put(reRouteLookupLength, new java.lang.Integer(reRouteLookupLengthMin));
    mins.put(reFirewallQueueMaxThreads, new java.lang.Integer(reFirewallQueueMaxThreadsMin));
    mins.put(reEnumServerPort, new java.lang.Integer(reEnumServerPortMin));
    mins.put(reModifyServiceRoutePort, new java.lang.Integer(reModifyServiceRoutePortMin));
    mins.put(reSgElementQValue, new java.lang.Float(reSgElementQValueMin));
    mins.put(reRoutingSequence, new java.lang.Integer(reRoutingSequenceMin));
    mins.put(reShutdownRetryAfter, new java.lang.Integer(reShutdownRetryAfterMin));
    mins.put(dsReMaxIterationDepth, new java.lang.Integer(dsReMaxIterationDepthMin));
    mins.put(reRetransmitCountValue, new java.lang.Integer(reRetransmitCountValueMin));
    mins.put(
        reFirewallTimeoutSessionTimeout, new java.lang.Integer(reFirewallTimeoutSessionTimeoutMin));
    mins.put(reRetransmitTimerValue, new java.lang.Integer(reRetransmitTimerValueMin));
    mins.put(reTimeQValue, new java.lang.Float(reTimeQValueMin));
    mins.put(reShutdownRedirectPort, new java.lang.Integer(reShutdownRedirectPortMin));
    mins.put(reModuleTriggerSequenceNo, new java.lang.Integer(reModuleTriggerSequenceNoMin));
    mins.put(dsReConnectionCacheSize, new java.lang.Integer(dsReConnectionCacheSizeMin));
    mins.put(reRadiusSgElementPort, new java.lang.Integer(reRadiusSgElementPortMin));
    mins.put(dsReMaxMediaDescriptions, new java.lang.Integer(dsReMaxMediaDescriptionsMin));
    mins.put(reQosSgElementQValue, new java.lang.Float(reQosSgElementQValueMin));
    mins.put(reRouteEnumVoidResponse, new java.lang.Integer(reRouteEnumVoidResponseMin));
    mins.put(reFirewallQueueSize, new java.lang.Integer(reFirewallQueueSizeMin));
    mins.put(reRecordRoutePort, new java.lang.Integer(reRecordRoutePortMin));
    mins.put(reQosSgElementPipelineSize, new java.lang.Integer(reQosSgElementPipelineSizeMin));
    mins.put(reEnumRootTimeout, new java.lang.Integer(reEnumRootTimeoutMin));
    mins.put(reFirewallGroupGroupID, new java.lang.Integer(reFirewallGroupGroupIDMin));
    mins.put(rePathPort, new java.lang.Integer(rePathPortMin));
    mins.put(reRadiusSgElementRetxTimeout, new java.lang.Integer(reRadiusSgElementRetxTimeoutMin));
    mins.put(
        reQosSgElementConnectionPoolSize,
        new java.lang.Integer(reQosSgElementConnectionPoolSizeMin));
    mins.put(reAddServiceRouteSequence, new java.lang.Integer(reAddServiceRouteSequenceMin));
    mins.put(dsReDefaultContactExpires, new java.lang.Integer(dsReDefaultContactExpiresMin));
    mins.put(reXclQueueSize, new java.lang.Integer(reXclQueueSizeMin));
    mins.put(reNetscreenFirewallPort, new java.lang.Integer(reNetscreenFirewallPortMin));
    mins.put(reRadiusSgElementRetxCount, new java.lang.Integer(reRadiusSgElementRetxCountMin));
    mins.put(reFirewallQueueBusyReset, new java.lang.Integer(reFirewallQueueBusyResetMin));
    mins.put(reMaxPinholeBandwidthBits, new java.lang.Long(reMaxPinholeBandwidthBitsMin));
    mins.put(reAddServiceRoutePort, new java.lang.Integer(reAddServiceRoutePortMin));
    mins.put(reRadiusSgElementQValue, new java.lang.Float(reRadiusSgElementQValueMin));
    mins.put(reRoutingEnumVoidResponse, new java.lang.Integer(reRoutingEnumVoidResponseMin));
    mins.put(reEnumRootAttempts, new java.lang.Integer(reEnumRootAttemptsMin));
    mins.put(reRoutingLookupLength, new java.lang.Integer(reRoutingLookupLengthMin));
    mins.put(reListenPort, new java.lang.Integer(reListenPortMin));
    mins.put(reRouteSequence, new java.lang.Integer(reRouteSequenceMin));
    mins.put(reNormalizationSequence, new java.lang.Integer(reNormalizationSequenceMin));
    mins.put(reRouteElementQValue, new java.lang.Float(reRouteElementQValueMin));
    mins.put(reTimeInterval, new java.lang.Integer(reTimeIntervalMin));
    mins.put(reEnumServerQValue, new java.lang.Float(reEnumServerQValueMin));
    mins.put(reViaSrcPort, new java.lang.Integer(reViaSrcPortMin));
    mins.put(dsReDefaultQValue, new java.lang.Float(dsReDefaultQValueMin));
    mins.put(reQosSgElementTimeout, new java.lang.Integer(reQosSgElementTimeoutMin));
    mins.put(dsReMinContactExpires, new java.lang.Integer(dsReMinContactExpiresMin));
    mins.put(reViaPort, new java.lang.Integer(reViaPortMin));
    mins.put(reSgElementPort, new java.lang.Integer(reSgElementPortMin));
    mins.put(dsReMaxContactExpires, new java.lang.Integer(dsReMaxContactExpiresMin));

    maxs.put(reRouteLookupLength, new java.lang.Integer(reRouteLookupLengthMax));
    maxs.put(reEnumServerPort, new java.lang.Integer(reEnumServerPortMax));
    maxs.put(reModifyServiceRoutePort, new java.lang.Integer(reModifyServiceRoutePortMax));
    maxs.put(reSgElementQValue, new java.lang.Float(reSgElementQValueMax));
    maxs.put(reRoutingSequence, new java.lang.Integer(reRoutingSequenceMax));
    maxs.put(reRetransmitCountValue, new java.lang.Integer(reRetransmitCountValueMax));
    maxs.put(reRetransmitTimerValue, new java.lang.Integer(reRetransmitTimerValueMax));
    maxs.put(reTimeQValue, new java.lang.Float(reTimeQValueMax));
    maxs.put(reShutdownRedirectPort, new java.lang.Integer(reShutdownRedirectPortMax));
    maxs.put(reRadiusSgElementPort, new java.lang.Integer(reRadiusSgElementPortMax));
    maxs.put(reQosSgElementQValue, new java.lang.Float(reQosSgElementQValueMax));
    maxs.put(reRouteEnumVoidResponse, new java.lang.Integer(reRouteEnumVoidResponseMax));
    maxs.put(reRecordRoutePort, new java.lang.Integer(reRecordRoutePortMax));
    maxs.put(reQosSgElementPipelineSize, new java.lang.Integer(reQosSgElementPipelineSizeMax));
    maxs.put(rePathPort, new java.lang.Integer(rePathPortMax));
    maxs.put(
        reQosSgElementConnectionPoolSize,
        new java.lang.Integer(reQosSgElementConnectionPoolSizeMax));
    maxs.put(reAddServiceRouteSequence, new java.lang.Integer(reAddServiceRouteSequenceMax));
    maxs.put(reXclQueueSize, new java.lang.Integer(reXclQueueSizeMax));
    maxs.put(reNetscreenFirewallPort, new java.lang.Integer(reNetscreenFirewallPortMax));
    maxs.put(reFirewallQueueBusyReset, new java.lang.Integer(reFirewallQueueBusyResetMax));
    maxs.put(reAddServiceRoutePort, new java.lang.Integer(reAddServiceRoutePortMax));
    maxs.put(reRadiusSgElementQValue, new java.lang.Float(reRadiusSgElementQValueMax));
    maxs.put(reRoutingEnumVoidResponse, new java.lang.Integer(reRoutingEnumVoidResponseMax));
    maxs.put(reRoutingLookupLength, new java.lang.Integer(reRoutingLookupLengthMax));
    maxs.put(reListenPort, new java.lang.Integer(reListenPortMax));
    maxs.put(reRouteSequence, new java.lang.Integer(reRouteSequenceMax));
    maxs.put(reNormalizationSequence, new java.lang.Integer(reNormalizationSequenceMax));
    maxs.put(reRouteElementQValue, new java.lang.Float(reRouteElementQValueMax));
    maxs.put(reTimeInterval, new java.lang.Integer(reTimeIntervalMax));
    maxs.put(reEnumServerQValue, new java.lang.Float(reEnumServerQValueMax));
    maxs.put(reViaSrcPort, new java.lang.Integer(reViaSrcPortMax));
    maxs.put(dsReDefaultQValue, new java.lang.Float(dsReDefaultQValueMax));
    maxs.put(reViaPort, new java.lang.Integer(reViaPortMax));
    maxs.put(reSgElementPort, new java.lang.Integer(reSgElementPortMax));
  }

  public static HashSet getAllStrings() {
    return allStrings;
  }

  public static HashSet getVersionableParams() {
    return versionableParams;
  }

  public static Object getDefault(String paramOrColumnName) {
    return defaults.get(paramOrColumnName);
  }

  public static boolean hasDefault(String paramOrColumnName) {
    return defaults.get(paramOrColumnName) != null;
  }

  public static HashMap getDefaults() {
    return defaults;
  }

  public static Number getMin(String paramOrColumnName) {
    return (Number) mins.get(paramOrColumnName);
  }

  public static boolean hasMin(String paramOrColumnName) {
    return mins.get(paramOrColumnName) != null;
  }

  public static HashMap getMins() {
    return mins;
  }

  public static Number getMax(String paramOrColumnName) {
    return (Number) maxs.get(paramOrColumnName);
  }

  public static boolean hasMax(String paramOrColumnName) {
    return maxs.get(paramOrColumnName) != null;
  }

  public static HashMap getMaxs() {
    return maxs;
  }

  public static Set getValidValues(String paramOrColumnName) {
    Set s = null;
    HashMap map = (HashMap) validValues.get(paramOrColumnName);
    if (map != null) s = map.keySet();
    return s;
  }

  public static boolean hasValidValues(String paramOrColumnName) {
    return validValues.get(paramOrColumnName) != null;
  }

  public static HashMap getValidValues() {
    return validValues;
  }

  public static int getValidValueAsInt(String paramOrColumnName, String value) {
    int i = -1;
    HashMap map = (HashMap) validValues.get(paramOrColumnName);
    if (map != null) {
      Integer integer = (Integer) map.get(value);
      if (integer != null) i = integer.intValue();
    }
    return i;
  }

  public static String getValidValueAsString(String paramOrColumnName, int value) {
    String str = null;
    HashMap map = (HashMap) validValues.get(paramOrColumnName);
    if (map != null) {
      for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
        String key = (String) i.next();
        Integer integer = (Integer) map.get(key);
        if (integer.intValue() == value) {
          str = key;
          break;
        }
      }
    }
    return str;
  }
}
