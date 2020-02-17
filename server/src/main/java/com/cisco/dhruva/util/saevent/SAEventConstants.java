package com.cisco.dhruva.util.saevent;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;

public final class SAEventConstants {

  public static final String SERVER_GROUP_NAME = "serverGroupName";
  public static final String FAILURE_REASON = "failureReason";
  public static final String ENDPOINT_INFO = "endpointInfo";

  // CP - follows 901 as a Reason Cause when CP in maintenance mode
  public static final int MAINTENANCE_MODE_REASON_CAUSE = 901;
  public static final DsByteString MAINTENANCE_MODE_REASON_PHRASE =
      new DsByteString("\"CP in Maintenance Mode\"");

  private SAEventConstants() {}

  // Formatting
  public static final String SPACE = " ";
  public static final String COMMA = ",";

  // Call flow direction specification
  public static final String IN = "In";
  public static final String OUT = "Out";

  // Socket Connection specification
  public static final String CONNECT = "Connected";
  public static final String DISCONNECT = "Disconnected";
  // Socket_type information
  public static final String TCP = "Tcp";
  public static final String TLS_TCP = "TlsTcp";
  public static final String TLS = "Tls";
  public static final String UDP = "Udp";

  public static final String CONNECTION = "Connection";
  public static final String EVENT_ID_TLS = "TLSConnection";
  public static final String EVENT_ID_TCP = "TCPConnection";

  // Operations Events and Alarms
  public static final String OPERATIONS_EVENT_NOTIFICATION = "OperationsEvent";
  public static final String CLOUDPROXY_SUSPEND_ALARM = "Suspend";
  public static final String CLOUDPROXY_RESUME_ALARM = "Resume";
  public static final String CLOUDPROXY_SUSPEND_TIMER_EXPIRY_ALARM = "SuspendTimerExpiry";

  // Connection failure reasons
  public static final String CONNECTION_LOCK_TIMEDOUT = "ConnectionLockTimeout";
  public static final String SOCKET_WRITE_TIMEDOUT = "SocketWriteTimeout";

  // Sip Message to Large
  public static final String SIP_MESSAGE_TOO_LARGE = "SipMessageTooLarge";

  public static final String INVALID_CONNECTION = "InvalidConnection";

  public static final String GENERATE_ALARM_AND_EVENT = "GenerateAlarmAndEvent";
  public static final String EVENT_LEVEL = "eventLevel";
  public static final String EVENT_TYPE = "eventType";
  public static final String EVENT_INFO = "eventInfo";
  public static final String EVENT_NAME = "eventName";
  // Server Group Events
  public static final String SERVERGROUP_EVENT = "ServerGroupEvent";
  public static final String SERVERGROUP_DOWN = "ServerGroupDown";
  public static final String SERVERGROUP_UP = "ServerGroupUp";
  public static final String SERVERGROUP_ELEMENT_DOWN = "ServerGroupElementDown";
  public static final String SERVERGROUP_ELEMENT_UP = "ServerGroupElementUp";
  public static final String SERVERGROUP_ELEMENT_DOWN_INFO = "Endpoint Unreachable";
  public static final String SERVERGROUP_ELEMENT_UP_INFO = "Endpoint Reachable";

  public static final String CERTIFICATE_REVOCATION = "CertificateRevocation";

  public static final String CERT_REVOKED = "CertRevoked";
  public static final String TLS_HANDSHAKE_FAILED = "TLSHandshakeFailed";
  public static final String TLS_SEND_BUFFER_FULL = "TlsSendBufferFull";
}
