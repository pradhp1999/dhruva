package com.cisco.dhruva.util.saevent;

import com.cisco.dhruva.util.log.Trace;
import com.cisco.dhruva.util.saevent.EventBase.EventType;
import com.cisco.dhruva.util.saevent.dataparam.ConnectionEventDataParam;
import com.cisco.dhruva.util.saevent.dataparam.TlsSendBufferDataParam;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.management.Notification;
import org.apache.logging.log4j.Level;

public class ConnectionSAEventBuilder {
  public static final String defaultIPAddr = "0.0.0.0";
  private static final Trace saEventLog = Trace.getTrace("com.cisco.CloudProxy.SAEvent");
  private static final Trace Log = Trace.getTrace(DiscardSAEventBuilder.class.getName());
  private static final String WARN = "WARN";
  private static long seqNo = 0;

  /**
   * @SAEvent contains Logs connection information for successfull connection
   * LocalAddress,LocalPort,RemortAddress,RemortPort
   */
  public static void logTlsConnectionEvent(
      boolean isPeerNameAuthenticated,
      String peerName,
      String peerCertSerial,
      String tlsversion,
      String cipher,
      String peerCertHash,
      InetAddress localAddr,
      int localPort,
      InetAddress remortAddr,
      int remortPort,
      ArrayList<ArrayList<String>> certInfo) {
    SAEvent connectionSaEvent = new SAEvent();
    connectionSaEvent.setType(EventType.ProtocolEvents);
    connectionSaEvent.setId(SAEventConstants.CONNECTION);

    ConnectionEventDataParam dataParam =
        new ConnectionEventDataParam.Builder()
            .status("Connected")
            .peerPrincipalName(peerName)
            .peerPrincipalNameAuthenticated(isPeerNameAuthenticated ? "Yes" : "No")
            .peerCertificateSerialNumber(peerCertSerial)
            .peerCertificateHash(peerCertHash)
            .eventType(SAEventConstants.EVENT_ID_TLS)
            .tlsVersion(tlsversion)
            .negotiatedCipherSuite(cipher)
            .localIPAddress(localAddr.toString().replace("/", ""))
            .localPort(localPort)
            .remoteIPAddress(remortAddr.toString().replace("/", ""))
            .remotePort(remortPort)
            .build();

    if (certInfo != null) {
      LinkedHashMap certInfos = new LinkedHashMap();
      AtomicInteger counter = new AtomicInteger(1);
      certInfo.forEach(
          ca -> {
            LinkedHashMap cert = new LinkedHashMap();
            cert.put("owner", ca.get(0));
            cert.put("issuer", ca.get(1));
            cert.put("crlUrl", ca.get(2));
            cert.put("ocspUrl", ca.get(3));
            cert.put("expireDate", ca.get(4));
            certInfos.put("certInfo" + counter.getAndIncrement(), cert);
          });
      dataParam.setCertInfos(certInfos);
    } else {
      dataParam.setCertInfos(null);
    }

    connectionSaEvent.setDataParam(dataParam);
    saEventLog.log(Level.OFF, connectionSaEvent.toEventJSON());
  }

  /** @SAEvent contains Logs connection erros, LocalAddress,LocalPort,RemortAddress,RemortPort */
  public static void logTLSConnectionErrorEvent(
      String error,
      InetAddress localAddr,
      int localPort,
      InetAddress remortAddr,
      int remortPort,
      String direction,
      String eventReason) {
    SAEvent connectionSaEvent = new SAEvent();
    connectionSaEvent.setType(EventType.ProtocolEvents);
    connectionSaEvent.setId(SAEventConstants.CONNECTION);
    connectionSaEvent.setLevel(EventBase.EventLevel.err);
    connectionSaEvent.setEventReason(eventReason);

    ConnectionEventDataParam dataParam =
        new ConnectionEventDataParam.Builder()
            .status("Error")
            .eventInfo(error)
            .eventType(SAEventConstants.EVENT_ID_TCP)
            .localIPAddress(localAddr.toString().replace("/", ""))
            .localPort(localPort)
            .remoteIPAddress(remortAddr.toString().replace("/", ""))
            .remotePort(remortPort)
            .direction(direction)
            .build();

    connectionSaEvent.setDataParam(dataParam);
    saEventLog.log(Level.OFF, connectionSaEvent.toEventJSON());
  }

  /**
   * Populates SAEvent for Tcp , Tls Connection & Disconnection. @SAEvent contains
   * LocalAddress,LocalPort,RemortAddress,RemortPort,Operation and direction.
   */
  public static void logConnectionEvent(
      String operation,
      String socketType,
      String direction,
      InetAddress localAddr,
      int localPort,
      InetAddress remortAddr,
      int remortPort) {
    SAEvent connectionSaEvent = new SAEvent();
    connectionSaEvent.setType(EventType.ProtocolEvents);
    connectionSaEvent.setId(SAEventConstants.CONNECTION);

    ConnectionEventDataParam dataParam =
        new ConnectionEventDataParam.Builder()
            .status(operation)
            .type(socketType)
            .localIPAddress(localAddr.toString().replace("/", ""))
            .localPort(localPort)
            .remoteIPAddress(remortAddr.toString().replace("/", ""))
            .remotePort(remortPort)
            .eventType(SAEventConstants.EVENT_ID_TCP)
            .build();

    if (direction != null) {
      dataParam.setDirection(direction);
    }

    connectionSaEvent.setDataParam(dataParam);
    saEventLog.log(Level.OFF, connectionSaEvent.toEventJSON());
  }

  /**
   * Populates SAEvent for outgoiing Tcp ,Tls Connection failures. @SAEvent contains Error,
   * ConnectionType, LocalAddress, LocalPort, RemortAddress, RemortPort and direction.
   */
  public static void logConnectionErrorEvent(
      String error,
      String socketType,
      InetAddress localAddr,
      int localPort,
      InetAddress remortAddr,
      int remortPort,
      String direction) {
    SAEvent connectionSaEvent = new SAEvent();
    connectionSaEvent.setType(EventType.ProtocolEvents);
    connectionSaEvent.setId(SAEventConstants.CONNECTION);
    connectionSaEvent.setLevel(EventBase.EventLevel.err);
    String localAddress = null;
    if (localAddr != null) localAddress = localAddr.toString().replace("/", "");

    ConnectionEventDataParam dataParam =
        new ConnectionEventDataParam.Builder()
            .status("Error")
            .eventInfo(error)
            .type(socketType)
            .eventType(SAEventConstants.EVENT_ID_TCP)
            .localIPAddress(localAddress)
            .localPort(localPort)
            .remoteIPAddress(remortAddr.toString().replace("/", ""))
            .remotePort(remortPort)
            .direction(direction)
            .build();

    connectionSaEvent.setDataParam(dataParam);
    saEventLog.log(Level.OFF, connectionSaEvent.toEventJSON());
  }

  public static void logSuccessfulAegisRequest(
      String status, String eventType, String eventInfo, String requestURL) {
    SAEvent connectionSaEvent = new SAEvent();
    connectionSaEvent.setType(EventType.SecurityEvents);
    connectionSaEvent.setId(SAEventConstants.CERTIFICATE_REVOCATION);

    ConnectionEventDataParam dataParam =
        new ConnectionEventDataParam.Builder()
            .status(status)
            .eventType(eventType)
            .eventInfo(eventInfo)
            .url(requestURL)
            .build();

    connectionSaEvent.setDataParam(dataParam);
    saEventLog.log(Level.OFF, connectionSaEvent.toEventJSON());
  }

  public static void logFailureAegisRequest(
      String status, String eventType, String eventInfo, String responseCode, String requestURL) {
    SAEvent connectionSaEvent = new SAEvent();
    connectionSaEvent.setType(EventType.SecurityEvents);
    connectionSaEvent.setId(SAEventConstants.CERTIFICATE_REVOCATION);

    ConnectionEventDataParam dataParam =
        new ConnectionEventDataParam.Builder()
            .status(status)
            .eventType(eventType)
            .eventInfo(eventInfo)
            .responseCode(responseCode)
            .url(requestURL)
            .build();

    connectionSaEvent.setDataParam(dataParam);
    saEventLog.log(Level.OFF, connectionSaEvent.toEventJSON());
  }

  public static Notification logTlsSendBufferFull(
      String error,
      InetAddress localAddr,
      int localPort,
      InetAddress remoteAddr,
      int remotePort,
      String eventReason) {
    logTLSConnectionErrorEvent(
        error, localAddr, localPort, remoteAddr, remotePort, SAEventConstants.OUT, eventReason);
    // Send Alarm from here
    TlsSendBufferDataParam dataParam =
        new TlsSendBufferDataParam.Builder()
            .eventType(SAEventConstants.EVENT_ID_TLS)
            .eventInfo(
                SAEventConstants.TLS_SEND_BUFFER_FULL
                    + ",Remote side is slow Reader, Unable to write")
            .endpoint(remoteAddr.getHostName() + ":" + String.valueOf(remotePort))
            .build();
    // REFACTOR
    //		try {
    //			Notification n = new Notification(SAEventConstants.TLS_SEND_BUFFER_FULL,
    //			        SIPServerStatusMBeanImpl.getInstance().getObjectName(),
    //			        seqNo++);
    //			n.setUserData(dataParam);
    //	        SIPServerStatusMBeanImpl.getInstance().sendNotification(n);
    //	        return n;
    //		} catch(Exception e) {
    //			if(Log.on && Log.isErrorEnabled()) {
    //				Log.error("Error while sending TlsSendBufferFull Alarm Notification",e);
    //			}
    //		}
    return null;
  }

  public static InetAddress getDefaultLocalAddress(
      InetAddress msocketLocalAddress,
      InetAddress bindingInfoLocalAddress,
      InetAddress lInetAddress) {
    InetAddress localAddress;

    if (null != msocketLocalAddress) {
      localAddress = msocketLocalAddress;
    } else if (null != bindingInfoLocalAddress) {
      localAddress = bindingInfoLocalAddress;
    } else if (null != lInetAddress) {
      localAddress = lInetAddress;
    } else {
      try {
        localAddress = InetAddress.getByName(defaultIPAddr);
      } catch (UnknownHostException e) {
        if (Log.on && Log.isErrorEnabled())
          Log.error("Error while initializing default ip address '0.0.0.0'", e);
        localAddress = null;
      }
    }
    return localAddress;
  }
}
