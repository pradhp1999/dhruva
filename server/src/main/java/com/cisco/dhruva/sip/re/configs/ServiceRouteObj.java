/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.sip.re.configs;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipServiceRouteHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;

public class ServiceRouteObj {
  private DsNetwork m_direction = null;
  private String m_ServiceRouteAddress = null;
  private int m_ServiceRoutePort = -1;
  private int m_ServiceRouteTransport = -1;
  private int m_ServiceRouteSequence = -1;
  private String m_ServiceRouteParams = null;
  private DsSipServiceRouteHeader m_ServiceRouteHeader = null;

  public ServiceRouteObj(
      DsNetwork direction,
      String ServiceRouteAddress,
      int ServiceRoutePort,
      int ServiceRouteTransport,
      int ServiceRouteSequence,
      String ServiceRouteParams)
      throws DsSipParserException, DsSipParserListenerException {
    m_direction = direction;
    m_ServiceRouteAddress = ServiceRouteAddress;
    m_ServiceRoutePort = ServiceRoutePort;
    m_ServiceRouteTransport = ServiceRouteTransport;
    m_ServiceRouteSequence = ServiceRouteSequence;
    m_ServiceRouteParams = ServiceRouteParams;
    if (m_ServiceRouteParams == null) {
      m_ServiceRouteHeader =
          new DsSipServiceRouteHeader(
              new DsByteString(
                  "<sip:"
                      + m_ServiceRouteAddress
                      + ':'
                      + m_ServiceRoutePort
                      + ";transport="
                      + m_ServiceRouteTransport
                      + ";lr>"));
    } else {
      m_ServiceRouteHeader =
          new DsSipServiceRouteHeader(
              new DsByteString(
                  "<sip:"
                      + m_ServiceRouteParams
                      + '@'
                      + m_ServiceRouteAddress
                      + ':'
                      + m_ServiceRoutePort
                      + ";transport="
                      + m_ServiceRouteTransport
                      + ";lr>"));
    }
  }

  public ServiceRouteObj(
      DsNetwork direction,
      String ServiceRouteAddress,
      int ServiceRoutePort,
      int ServiceRouteTransport)
      throws DsSipParserException, DsSipParserListenerException {
    this(direction, ServiceRouteAddress, ServiceRoutePort, ServiceRouteTransport, -1, null);
  }

  public DsNetwork getDirection() {
    return m_direction;
  }

  public String getServiceRouteAddress() {
    return m_ServiceRouteAddress;
  }

  public int getServiceRoutePort() {
    return m_ServiceRoutePort;
  }

  public int getServiceRouteTransport() {
    return m_ServiceRouteTransport;
  }

  public int getServiceRouteSequence() {
    return m_ServiceRouteSequence;
  }

  public String getServiceRouteParams() {
    return m_ServiceRouteParams;
  }

  public DsSipServiceRouteHeader getServiceRouteHeader() {
    return (DsSipServiceRouteHeader) m_ServiceRouteHeader.clone();
  }
}
