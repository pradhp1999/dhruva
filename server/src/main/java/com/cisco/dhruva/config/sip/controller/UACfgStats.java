package com.cisco.dhruva.config.sip.controller;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import java.util.*;

public abstract class UACfgStats {

  public static final String sipUAStatsEnableMsgs = "sipUAStatsEnableMsgs";
  public static final String sipUAStatsEnableMsgsDefault = "false";
  public static final String sipUAStatsMaxTcpTlsConnections = "sipUAStatsMaxTcpTlsConnections";
  public static final int sipUAStatsMaxTcpTlsConnectionsDefault = 0;
  public static final String sipUAStatsMsgsInterval = "sipUAStatsMsgsInterval";
  public static final int sipUAStatsMsgsIntervalDefault = 0;
  public static final String sipUAStatsTcpTlsConnectionsThreshold =
      "sipUAStatsTcpTlsConnectionsThreshold";
  public static final int sipUAStatsTcpTlsConnectionsThresholdDefault = 0;
  public static final String uaDebugDump = "uaDebugDump";
  public static final String uaMaxForwards = "uaMaxForwards";
  public static final int uaMaxForwardsDefault = 70;
  public static final int uaMaxForwardsMin = 10;
  public static final String uaMaxRequestTimeout = "uaMaxRequestTimeout";
  public static final int uaMaxRequestTimeoutDefault = 32;
  public static final int uaMaxRequestTimeoutMin = 0;
  public static final String uaMaxTCPConnections = "uaMaxTCPConnections";
  public static final int uaMaxTCPConnectionsDefault = 256;
  public static final int uaMaxTCPConnectionsMin = 1;
  public static final String uaSrvRecords = "uaSrvRecords";
  public static final String uaSrvRecordsDefault = "off";
  public static final String uaSrvRecords_on = "on";
  public static final int index_uaSrvRecords_on = 0;
  public static final String uaSrvRecords_off = "off";
  public static final int index_uaSrvRecords_off = 1;
  public static final String uaStatus = "uaStatus";
  public static final String uaTCPConnectionTimeout = "uaTCPConnectionTimeout";
  public static final int uaTCPConnectionTimeoutDefault = 240;
  public static final int uaTCPConnectionTimeoutMin = 0;
  public static final String uaTimerQueue = "uaTimerQueue";
  public static final int uaTimerQueueDefault = 8;
  public static final int uaTimerQueueMin = 4;
  public static final String sipUAOnOverload = "sipUAOnOverload";
  public static final String sipUAOnOverloadResponse = "sipUAOnOverloadResponse";
  public static final String sipUAOnOverloadResponse_redirect = "redirect";
  public static final int index_sipUAOnOverloadResponse_redirect = 0;
  public static final String sipUAOnOverloadResponse_reject = "reject";
  public static final int index_sipUAOnOverloadResponse_reject = 1;
  public static final String sipUAOnOverloadRetryAfter = "sipUAOnOverloadRetryAfter";
  public static final int sipUAOnOverloadRetryAfterDefault = 0;
  public static final int sipUAOnOverloadRetryAfterMin = 0;
  public static final String sipUAOnOverloadRedirectHost = "sipUAOnOverloadRedirectHost";
  public static final String sipUAOnOverloadRedirectPort = "sipUAOnOverloadRedirectPort";
  public static final int sipUAOnOverloadRedirectPortDefault = 5060;
  public static final int sipUAOnOverloadRedirectPortMin = 0;
  public static final int sipUAOnOverloadRedirectPortMax = 65535;
  public static final String sipUAOnOverloadRedirectTransport = "sipUAOnOverloadRedirectTransport";
  public static final String sipUAOnOverloadRedirectTransportDefault = "UDP";
  public static final String sipUAOnOverloadRedirectTransport_UDP = "UDP";
  public static final int index_sipUAOnOverloadRedirectTransport_UDP =
      DsSipTransportType.getTypeAsInt(sipUAOnOverloadRedirectTransport_UDP);
  public static final String sipUAOnOverloadRedirectTransport_TCP = "TCP";
  public static final int index_sipUAOnOverloadRedirectTransport_TCP =
      DsSipTransportType.getTypeAsInt(sipUAOnOverloadRedirectTransport_TCP);
  public static final String sipUAOnOverloadRedirectTransport_TLS = "TLS";
  public static final int index_sipUAOnOverloadRedirectTransport_TLS =
      DsSipTransportType.getTypeAsInt(sipUAOnOverloadRedirectTransport_TLS);
  public static final String sipUAOnOverloadOnOrOff = "sipUAOnOverloadOnOrOff";
  public static final String sipUAOnOverloadOnOrOffDefault = "on";
  public static final String sipUAOnOverloadOnOrOff_on = "on";
  public static final int index_sipUAOnOverloadOnOrOff_on = 0;
  public static final String sipUAOnOverloadOnOrOff_off = "off";
  public static final int index_sipUAOnOverloadOnOrOff_off = 1;
  public static final String sipUAStatsIncomingDupMsgsMetrics = "sipUAStatsIncomingDupMsgsMetrics";
  public static final String sipUAStatsIncomingDupMessageType = "sipUAStatsIncomingDupMessageType";
  public static final String sipUAStatsIncomingDupMessageTypeDefault = "N/A";
  public static final String sipUAStatsIncomingDupMessageMetric =
      "sipUAStatsIncomingDupMessageMetric";
  public static final int sipUAStatsIncomingDupMessageMetricDefault = 0;
  public static final String sipUAStatsIncomingMsgsMetrics = "sipUAStatsIncomingMsgsMetrics";
  public static final String sipUAStatsIncomingMessageType = "sipUAStatsIncomingMessageType";
  public static final String sipUAStatsIncomingMessageTypeDefault = "N/A";
  public static final String sipUAStatsIncomingMessageMetric = "sipUAStatsIncomingMessageMetric";
  public static final int sipUAStatsIncomingMessageMetricDefault = 0;
  public static final String sipUAStatsOutgoingDupMsgsMetrics = "sipUAStatsOutgoingDupMsgsMetrics";
  public static final String sipUAStatsOutgoingDupMessageType = "sipUAStatsOutgoingDupMessageType";
  public static final String sipUAStatsOutgoingDupMessageTypeDefault = "N/A";
  public static final String sipUAStatsOutgoingDupMessageMetric =
      "sipUAStatsOutgoingDupMessageMetric";
  public static final int sipUAStatsOutgoingDupMessageMetricDefault = 0;
  public static final String sipUAStatsOutgoingMsgsMetrics = "sipUAStatsOutgoingMsgsMetrics";
  public static final String sipUAStatsOutgoingMessageType = "sipUAStatsOutgoingMessageType";
  public static final String sipUAStatsOutgoingMessageTypeDefault = "N/A";
  public static final String sipUAStatsOutgoingMessageMetric = "sipUAStatsOutgoingMessageMetric";
  public static final int sipUAStatsOutgoingMessageMetricDefault = 0;
  public static final String uaEventQueueMaxSize = "uaEventQueueMaxSize";
  public static final String event_queue_size = "event_queue_size";
  public static final int event_queue_sizeDefault = 2000;
  public static final int event_queue_sizeMin = 4;
  public static final int event_queue_sizeMax = 1000000;
  public static final String event_queue_type = "event_queue_type";
  public static final String event_queue_typeDefault = "head";
  public static final String event_queue_type_head = "head";
  public static final int index_event_queue_type_head = 0;
  public static final String event_queue_type_tail = "tail";
  public static final int index_event_queue_type_tail = 1;
  public static final String event_queue_busyreset = "event_queue_busyreset";
  public static final int event_queue_busyresetDefault = 80;
  public static final int event_queue_busyresetMin = 1;
  public static final int event_queue_busyresetMax = 100;
  public static final String event_queue_thread = "event_queue_thread";
  public static final int event_queue_threadDefault = 4;
  public static final int event_queue_threadMin = 1;
  public static final String uaMaxRequestRetransmissions = "uaMaxRequestRetransmissions";
  public static final String for_invite = "for_invite";
  public static final int for_inviteDefault = 5;
  public static final int for_inviteMin = 0;
  public static final int for_inviteMax = 127;
  public static final String for_noninvite = "for_noninvite";
  public static final int for_noninviteDefault = 9;
  public static final int for_noninviteMin = 0;
  public static final int for_noninviteMax = 127;
  public static final String uaQueue = "uaQueue";
  public static final String queue_name = "queue_name";
  public static final String queue_size = "queue_size";
  public static final int queue_sizeDefault = 2000;
  public static final int queue_sizeMin = 10;
  public static final int queue_sizeMax = 1000000;
  public static final String queue_type = "queue_type";
  public static final String queue_typeDefault = "head";
  public static final String queue_type_head = "head";
  public static final int index_queue_type_head = 0;
  public static final String queue_type_tail = "tail";
  public static final int index_queue_type_tail = 1;
  public static final String queue_type_unbound = "unbound";
  public static final int index_queue_type_unbound = 2;
  public static final String queue_busyreset = "queue_busyreset";
  public static final int queue_busyresetDefault = 80;
  public static final int queue_busyresetMin = 1;
  public static final int queue_busyresetMax = 100;
  public static final String queue_thread = "queue_thread";
  public static final int queue_threadDefault = 2;
  public static final int queue_threadMin = 1;
  public static final String queue_status = "queue_status";
  public static final String queue_statusDefault = "on";
  public static final String queue_status_on = "on";
  public static final int index_queue_status_on = 0;
  public static final String queue_status_off = "off";
  public static final int index_queue_status_off = 1;
  public static final String uaRequestQueueMaxSize = "uaRequestQueueMaxSize";
  public static final String request_queue_size = "request_queue_size";
  public static final int request_queue_sizeDefault = 2000;
  public static final int request_queue_sizeMin = 50;
  public static final int request_queue_sizeMax = 1000000;
  public static final String request_queue_type = "request_queue_type";
  public static final String request_queue_typeDefault = "head";
  public static final String request_queue_type_head = "head";
  public static final int index_request_queue_type_head = 0;
  public static final String request_queue_type_tail = "tail";
  public static final int index_request_queue_type_tail = 1;
  public static final String request_queue_busyreset = "request_queue_busyreset";
  public static final int request_queue_busyresetDefault = 80;
  public static final int request_queue_busyresetMin = 1;
  public static final int request_queue_busyresetMax = 100;
  public static final String request_queue_thread = "request_queue_thread";
  public static final int request_queue_threadDefault = 2;
  public static final int request_queue_threadMin = 1;
  public static final String request_queue_status = "request_queue_status";
  public static final String request_queue_statusDefault = "on";
  public static final String request_queue_status_on = "on";
  public static final int index_request_queue_status_on = 0;
  public static final String request_queue_status_off = "off";
  public static final int index_request_queue_status_off = 1;
  public static final String uaSrvDNSServer = "uaSrvDNSServer";
  public static final String dns_server = "dns_server";
  public static final String dns_serverDefault = "N/A";
  public static final String row_status = "row_status";
  public static final String uaTLS = "uaTLS";
  public static final String tls_file = "tls_file";
  public static final String tls_fileDefault = "N/A";
  public static final String tls_password = "tls_password";
  public static final String tls_passwordDefault = "N/A";
  public static final String tls_status = "tls_status";
  public static final String tls_statusDefault = "on";
  public static final String tls_status_on = "on";
  public static final int index_tls_status_on = 0;
  public static final String tls_status_off = "off";
  public static final int index_tls_status_off = 1;
  public static final String uaTLSTrustedPeer = "uaTLSTrustedPeer";
  public static final String tls_trusted_Peer_name = "tls_trusted_Peer_name";
  public static final String tls_trusted_Peer_nameDefault = "N/A";
  public static final String tls_trusted_row_status = "tls_trusted_row_status";
  public static final String sipUATrapQueueOkAgain = "sipUATrapQueueOkAgain";
  public static final String sipTrapUAQueueOkAgainName = "sipTrapUAQueueOkAgainName";
  public static final String sipTrapUAQueueOkAgainSize = "sipTrapUAQueueOkAgainSize";
  public static final String sipTrapUAQueueOkAgainThresholdSize =
      "sipTrapUAQueueOkAgainThresholdSize";
  public static final String sipTrapUAQueueOkAgainMaxSize = "sipTrapUAQueueOkAgainMaxSize";
  public static final String sipUATrapQueueThresholdExceeded = "sipUATrapQueueThresholdExceeded";
  public static final String sipTrapUAQueueThresholdName = "sipTrapUAQueueThresholdName";
  public static final String sipTrapUAQueueThresholdSize = "sipTrapUAQueueThresholdSize";
  public static final String sipTrapUAQueueThresholdThresholdSize =
      "sipTrapUAQueueThresholdThresholdSize";
  public static final String sipTrapUAQueueThresholdMaxSize = "sipTrapUAQueueThresholdMaxSize";
  public static final String sipTrapUAQueueMaxSizeExceeded = "sipTrapUAQueueMaxSizeExceeded";
  public static final String sipTrapUAQueueMaxSizeName = "sipTrapUAQueueMaxSizeName";
  public static final String sipTrapUAQueueMaxSizeSize = "sipTrapUAQueueMaxSizeSize";
  public static final String sipTrapUAQueueMaxSizeThresholdSize =
      "sipTrapUAQueueMaxSizeThresholdSize";
  public static final String sipTrapUAQueueMaxSizeMaxSize = "sipTrapUAQueueMaxSizeMaxSize";

  private static final HashSet allStrings = new HashSet(24);

  private static final HashSet versionableParams = new HashSet();

  private static final HashMap validValues_uaSrvRecords = new HashMap(2);
  private static final HashMap validValues_queue_status = new HashMap(2);
  private static final HashMap validValues_tls_status = new HashMap(2);
  private static final HashMap validValues_sipUAOnOverloadResponse = new HashMap(2);
  private static final HashMap validValues_event_queue_type = new HashMap(2);
  private static final HashMap validValues_sipUAOnOverloadRedirectTransport = new HashMap(3);
  private static final HashMap validValues_request_queue_status = new HashMap(2);
  private static final HashMap validValues_request_queue_type = new HashMap(2);
  private static final HashMap validValues_sipUAOnOverloadOnOrOff = new HashMap(2);
  private static final HashMap validValues_queue_type = new HashMap(3);

  private static final HashMap defaults = new HashMap(43);

  private static final HashMap mins = new HashMap(18);

  private static final HashMap maxs = new HashMap(9);

  private static final HashMap validValues = new HashMap();

  static {
    allStrings.add(sipUAStatsEnableMsgs);
    allStrings.add(sipUAStatsMaxTcpTlsConnections);
    allStrings.add(sipUAStatsMsgsInterval);
    allStrings.add(sipUAStatsTcpTlsConnectionsThreshold);
    allStrings.add(uaDebugDump);
    allStrings.add(uaMaxForwards);
    allStrings.add(uaMaxRequestTimeout);
    allStrings.add(uaMaxTCPConnections);
    allStrings.add(uaSrvRecords);
    allStrings.add(uaStatus);
    allStrings.add(uaTCPConnectionTimeout);
    allStrings.add(uaTimerQueue);
    allStrings.add(sipUAOnOverload);
    allStrings.add(sipUAOnOverloadResponse);
    allStrings.add(sipUAOnOverloadRetryAfter);
    allStrings.add(sipUAOnOverloadRedirectHost);
    allStrings.add(sipUAOnOverloadRedirectPort);
    allStrings.add(sipUAOnOverloadRedirectTransport);
    allStrings.add(sipUAOnOverloadOnOrOff);
    allStrings.add(sipUAStatsIncomingDupMsgsMetrics);
    allStrings.add(sipUAStatsIncomingDupMessageType);
    allStrings.add(sipUAStatsIncomingDupMessageMetric);
    allStrings.add(sipUAStatsIncomingMsgsMetrics);
    allStrings.add(sipUAStatsIncomingMessageType);
    allStrings.add(sipUAStatsIncomingMessageMetric);
    allStrings.add(sipUAStatsOutgoingDupMsgsMetrics);
    allStrings.add(sipUAStatsOutgoingDupMessageType);
    allStrings.add(sipUAStatsOutgoingDupMessageMetric);
    allStrings.add(sipUAStatsOutgoingMsgsMetrics);
    allStrings.add(sipUAStatsOutgoingMessageType);
    allStrings.add(sipUAStatsOutgoingMessageMetric);
    allStrings.add(uaEventQueueMaxSize);
    allStrings.add(event_queue_size);
    allStrings.add(event_queue_type);
    allStrings.add(event_queue_busyreset);
    allStrings.add(event_queue_thread);
    allStrings.add(uaMaxRequestRetransmissions);
    allStrings.add(for_invite);
    allStrings.add(for_noninvite);
    allStrings.add(uaQueue);
    allStrings.add(queue_name);
    allStrings.add(queue_size);
    allStrings.add(queue_type);
    allStrings.add(queue_busyreset);
    allStrings.add(queue_thread);
    allStrings.add(queue_status);
    allStrings.add(uaRequestQueueMaxSize);
    allStrings.add(request_queue_size);
    allStrings.add(request_queue_type);
    allStrings.add(request_queue_busyreset);
    allStrings.add(request_queue_thread);
    allStrings.add(request_queue_status);
    allStrings.add(uaSrvDNSServer);
    allStrings.add(dns_server);
    allStrings.add(row_status);
    allStrings.add(uaTLS);
    allStrings.add(tls_file);
    allStrings.add(tls_password);
    allStrings.add(tls_status);
    allStrings.add(uaTLSTrustedPeer);
    allStrings.add(tls_trusted_Peer_name);
    allStrings.add(tls_trusted_row_status);

    validValues_uaSrvRecords.put(uaSrvRecords_on, new Integer(index_uaSrvRecords_on));
    validValues_uaSrvRecords.put(uaSrvRecords_off, new Integer(index_uaSrvRecords_off));
    validValues.put(uaSrvRecords, validValues_uaSrvRecords);

    validValues_queue_status.put(queue_status_on, new Integer(index_queue_status_on));
    validValues_queue_status.put(queue_status_off, new Integer(index_queue_status_off));
    validValues.put(queue_status, validValues_queue_status);

    validValues_tls_status.put(tls_status_on, new Integer(index_tls_status_on));
    validValues_tls_status.put(tls_status_off, new Integer(index_tls_status_off));
    validValues.put(tls_status, validValues_tls_status);

    validValues_sipUAOnOverloadResponse.put(
        sipUAOnOverloadResponse_redirect, new Integer(index_sipUAOnOverloadResponse_redirect));
    validValues_sipUAOnOverloadResponse.put(
        sipUAOnOverloadResponse_reject, new Integer(index_sipUAOnOverloadResponse_reject));
    validValues.put(sipUAOnOverloadResponse, validValues_sipUAOnOverloadResponse);

    validValues_event_queue_type.put(
        event_queue_type_head, new Integer(index_event_queue_type_head));
    validValues_event_queue_type.put(
        event_queue_type_tail, new Integer(index_event_queue_type_tail));
    validValues.put(event_queue_type, validValues_event_queue_type);

    validValues_sipUAOnOverloadRedirectTransport.put(
        sipUAOnOverloadRedirectTransport_UDP,
        new Integer(index_sipUAOnOverloadRedirectTransport_UDP));
    validValues_sipUAOnOverloadRedirectTransport.put(
        sipUAOnOverloadRedirectTransport_TCP,
        new Integer(index_sipUAOnOverloadRedirectTransport_TCP));
    validValues_sipUAOnOverloadRedirectTransport.put(
        sipUAOnOverloadRedirectTransport_TLS,
        new Integer(index_sipUAOnOverloadRedirectTransport_TLS));
    validValues.put(sipUAOnOverloadRedirectTransport, validValues_sipUAOnOverloadRedirectTransport);

    validValues_request_queue_status.put(
        request_queue_status_on, new Integer(index_request_queue_status_on));
    validValues_request_queue_status.put(
        request_queue_status_off, new Integer(index_request_queue_status_off));
    validValues.put(request_queue_status, validValues_request_queue_status);

    validValues_request_queue_type.put(
        request_queue_type_head, new Integer(index_request_queue_type_head));
    validValues_request_queue_type.put(
        request_queue_type_tail, new Integer(index_request_queue_type_tail));
    validValues.put(request_queue_type, validValues_request_queue_type);

    validValues_sipUAOnOverloadOnOrOff.put(
        sipUAOnOverloadOnOrOff_on, new Integer(index_sipUAOnOverloadOnOrOff_on));
    validValues_sipUAOnOverloadOnOrOff.put(
        sipUAOnOverloadOnOrOff_off, new Integer(index_sipUAOnOverloadOnOrOff_off));
    validValues.put(sipUAOnOverloadOnOrOff, validValues_sipUAOnOverloadOnOrOff);

    validValues_queue_type.put(queue_type_head, new Integer(index_queue_type_head));
    validValues_queue_type.put(queue_type_tail, new Integer(index_queue_type_tail));
    validValues_queue_type.put(queue_type_unbound, new Integer(index_queue_type_unbound));
    validValues.put(queue_type, validValues_queue_type);

    defaults.put(tls_password, new java.lang.String(tls_passwordDefault));
    defaults.put(queue_busyreset, new java.lang.Integer(queue_busyresetDefault));
    defaults.put(
        sipUAStatsMaxTcpTlsConnections,
        new java.lang.Integer(sipUAStatsMaxTcpTlsConnectionsDefault));
    defaults.put(event_queue_type, new java.lang.String(event_queue_typeDefault));
    defaults.put(uaTimerQueue, new java.lang.Integer(uaTimerQueueDefault));
    defaults.put(request_queue_type, new java.lang.String(request_queue_typeDefault));
    defaults.put(
        sipUAStatsOutgoingDupMessageType,
        new java.lang.String(sipUAStatsOutgoingDupMessageTypeDefault));
    defaults.put(
        sipUAStatsIncomingMessageType, new java.lang.String(sipUAStatsIncomingMessageTypeDefault));
    defaults.put(event_queue_size, new java.lang.Integer(event_queue_sizeDefault));
    defaults.put(event_queue_busyreset, new java.lang.Integer(event_queue_busyresetDefault));
    defaults.put(
        sipUAOnOverloadRedirectPort, new java.lang.Integer(sipUAOnOverloadRedirectPortDefault));
    defaults.put(queue_status, new java.lang.String(queue_statusDefault));
    defaults.put(
        sipUAStatsIncomingDupMessageMetric,
        new java.lang.Integer(sipUAStatsIncomingDupMessageMetricDefault));
    defaults.put(
        sipUAStatsIncomingMessageMetric,
        new java.lang.Integer(sipUAStatsIncomingMessageMetricDefault));
    defaults.put(queue_size, new java.lang.Integer(queue_sizeDefault));
    defaults.put(for_noninvite, new java.lang.Integer(for_noninviteDefault));
    defaults.put(uaTCPConnectionTimeout, new java.lang.Integer(uaTCPConnectionTimeoutDefault));
    defaults.put(
        sipUAOnOverloadRetryAfter, new java.lang.Integer(sipUAOnOverloadRetryAfterDefault));
    defaults.put(tls_file, new java.lang.String(tls_fileDefault));
    defaults.put(request_queue_busyreset, new java.lang.Integer(request_queue_busyresetDefault));
    defaults.put(sipUAStatsMsgsInterval, new java.lang.Integer(sipUAStatsMsgsIntervalDefault));
    defaults.put(uaMaxRequestTimeout, new java.lang.Integer(uaMaxRequestTimeoutDefault));
    defaults.put(request_queue_size, new java.lang.Integer(request_queue_sizeDefault));
    defaults.put(
        sipUAStatsOutgoingMessageType, new java.lang.String(sipUAStatsOutgoingMessageTypeDefault));
    defaults.put(
        sipUAStatsOutgoingMessageMetric,
        new java.lang.Integer(sipUAStatsOutgoingMessageMetricDefault));
    defaults.put(tls_status, new java.lang.String(tls_statusDefault));
    defaults.put(
        sipUAOnOverloadRedirectTransport,
        new java.lang.String(sipUAOnOverloadRedirectTransportDefault));
    defaults.put(request_queue_status, new java.lang.String(request_queue_statusDefault));
    defaults.put(sipUAOnOverloadOnOrOff, new java.lang.String(sipUAOnOverloadOnOrOffDefault));
    defaults.put(
        sipUAStatsIncomingDupMessageType,
        new java.lang.String(sipUAStatsIncomingDupMessageTypeDefault));
    defaults.put(for_invite, new java.lang.Integer(for_inviteDefault));
    defaults.put(uaSrvRecords, new java.lang.String(uaSrvRecordsDefault));
    defaults.put(
        sipUAStatsTcpTlsConnectionsThreshold,
        new java.lang.Integer(sipUAStatsTcpTlsConnectionsThresholdDefault));
    defaults.put(sipUAStatsEnableMsgs, new java.lang.String(sipUAStatsEnableMsgsDefault));
    defaults.put(tls_trusted_Peer_name, new java.lang.String(tls_trusted_Peer_nameDefault));
    defaults.put(uaMaxTCPConnections, new java.lang.Integer(uaMaxTCPConnectionsDefault));
    defaults.put(queue_type, new java.lang.String(queue_typeDefault));
    defaults.put(uaMaxForwards, new java.lang.Integer(uaMaxForwardsDefault));
    defaults.put(
        sipUAStatsOutgoingDupMessageMetric,
        new java.lang.Integer(sipUAStatsOutgoingDupMessageMetricDefault));
    defaults.put(dns_server, new java.lang.String(dns_serverDefault));
    defaults.put(request_queue_thread, new java.lang.Integer(request_queue_threadDefault));
    defaults.put(queue_thread, new java.lang.Integer(queue_threadDefault));
    defaults.put(event_queue_thread, new java.lang.Integer(event_queue_threadDefault));

    mins.put(uaMaxRequestTimeout, new java.lang.Integer(uaMaxRequestTimeoutMin));
    mins.put(request_queue_size, new java.lang.Integer(request_queue_sizeMin));
    mins.put(queue_busyreset, new java.lang.Integer(queue_busyresetMin));
    mins.put(uaTimerQueue, new java.lang.Integer(uaTimerQueueMin));
    mins.put(for_invite, new java.lang.Integer(for_inviteMin));
    mins.put(event_queue_size, new java.lang.Integer(event_queue_sizeMin));
    mins.put(event_queue_busyreset, new java.lang.Integer(event_queue_busyresetMin));
    mins.put(sipUAOnOverloadRedirectPort, new java.lang.Integer(sipUAOnOverloadRedirectPortMin));
    mins.put(queue_size, new java.lang.Integer(queue_sizeMin));
    mins.put(uaTCPConnectionTimeout, new java.lang.Integer(uaTCPConnectionTimeoutMin));
    mins.put(for_noninvite, new java.lang.Integer(for_noninviteMin));
    mins.put(sipUAOnOverloadRetryAfter, new java.lang.Integer(sipUAOnOverloadRetryAfterMin));
    mins.put(uaMaxTCPConnections, new java.lang.Integer(uaMaxTCPConnectionsMin));
    mins.put(request_queue_busyreset, new java.lang.Integer(request_queue_busyresetMin));
    mins.put(uaMaxForwards, new java.lang.Integer(uaMaxForwardsMin));
    mins.put(request_queue_thread, new java.lang.Integer(request_queue_threadMin));
    mins.put(queue_thread, new java.lang.Integer(queue_threadMin));
    mins.put(event_queue_thread, new java.lang.Integer(event_queue_threadMin));

    maxs.put(event_queue_busyreset, new java.lang.Integer(event_queue_busyresetMax));
    maxs.put(event_queue_size, new java.lang.Integer(event_queue_sizeMax));
    maxs.put(request_queue_size, new java.lang.Integer(request_queue_sizeMax));
    maxs.put(sipUAOnOverloadRedirectPort, new java.lang.Integer(sipUAOnOverloadRedirectPortMax));
    maxs.put(queue_busyreset, new java.lang.Integer(queue_busyresetMax));
    maxs.put(queue_size, new java.lang.Integer(queue_sizeMax));
    maxs.put(for_noninvite, new java.lang.Integer(for_noninviteMax));
    maxs.put(request_queue_busyreset, new java.lang.Integer(request_queue_busyresetMax));
    maxs.put(for_invite, new java.lang.Integer(for_inviteMax));
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
