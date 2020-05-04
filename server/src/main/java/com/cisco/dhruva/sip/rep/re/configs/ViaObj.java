/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.sip.rep.re.configs;

import com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy.DsViaListenInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.transport.Transport;
import java.net.InetAddress;

/**
 * Created by IntelliJ IDEA. User: rrachuma Date: May 3, 2005 Time: 4:39:55 PM To change this
 * template use Options | File Templates.
 */
public class ViaObj implements DsViaListenInterface {
  private String m_direction = null;
  private DsByteString m_viaAddress = null;
  private int m_viaPort = -1;
  private Transport m_viaTransport = Transport.NONE;
  private String m_viaSrcAddress = null;
  private InetAddress m_viaSrcInetAddress = null;
  private int m_viaSrcPort = -1;

  public ViaObj(
      String direction,
      String viaAddress,
      int viaPort,
      Transport viaTransport,
      String viaSrcAddress,
      InetAddress viaSrcInetAddress,
      int viaSrcPort) {
    m_direction = direction;
    m_viaAddress = new DsByteString(viaAddress);
    m_viaPort = viaPort;
    m_viaTransport = viaTransport;
    m_viaSrcAddress = viaSrcAddress;
    m_viaSrcInetAddress = viaSrcInetAddress;
    m_viaSrcPort = viaSrcPort;
  }

  public String getDirection() {
    return m_direction;
  }

  public String getViaAddress() {
    return m_viaAddress.toString();
  }

  public int getViaPort() {
    return m_viaPort;
  }

  public Transport getViaTransport() {
    return m_viaTransport;
  }

  public String getViaSrcAddress() {
    return m_viaSrcAddress;
  }

  public int getViaSrcPort() {
    return m_viaSrcPort;
  }

  public InetAddress getSourceAddress() {
    return m_viaSrcInetAddress;
  }

  public int getSourcePort() {
    return getViaSrcPort();
  }

  public int getPort() {
    return getViaPort();
  }

  public Transport getProtocol() {
    return getViaTransport();
  }

  public DsByteString getAddress() {
    return m_viaAddress;
  }

  public String toString() {
    return "Via Address "
        + m_viaAddress
        + ":"
        + m_viaPort
        + " ["
        + m_viaTransport
        + "] "
        + "-- Src Address "
        + m_viaSrcAddress
        + ":"
        + m_viaSrcPort;
  }
}
