// Copyright (c) 2005-2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.transport.Transport;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

/**
 * Wrapper for SRV records that knows about binding info and data used by SRV selection algorithm.
 * Used by DsSipServerLocator and DsSipDetServerLocator
 */
public class DsStringSRVWrapper extends DsSRVWrapper {

  boolean _parsed = true;
  String _host = null;
  int _port = DsBindingInfo.REMOTE_PORT_UNSPECIFIED;
  int _weight = -1;
  int _priority = -1;
  int _running_sum = 0;
  Transport _proto = DsBindingInfo.BINDING_TRANSPORT_UNSPECIFIED;
  boolean _enabled = true;

  public DsBindingInfo getBindingInfo() {
    if (!_parsed) return null;
    InetAddress ipAddress = null;
    try {
      ipAddress = getIPAddress();
    } catch (Exception exc) {
      return null;
    }
    return new DsBindingInfo(ipAddress, getPort(), getProtocol());
  }

  /** */
  public void setRunningSum(int sum) {
    _running_sum = sum;
  }

  /**
   * Return the running sum.
   *
   * @return the running sum.
   */
  public int getRunningSum() {
    return _running_sum;
  }

  /**
   * Constructs this instance with the specified <code>record</code> and the <code>protocol</code>.
   *
   * @param record The record string.
   * @param protocol The protocol.
   */
  public DsStringSRVWrapper(String record, Transport protocol) {
    try {
      StringTokenizer tok = new StringTokenizer(record, " ");
      _priority = Integer.parseInt(tok.nextToken());
      _weight = Integer.parseInt(tok.nextToken());
      _port = Integer.parseInt(tok.nextToken());
      _host = tok.nextToken();
      if (_host.endsWith(".")) _host = _host.substring(0, _host.length() - 1);
    } catch (Exception e) {
      _parsed = false;
    }
    _proto = protocol;
  }

  /**
   * Return the InetAddress object of the host name in this record.
   *
   * @return the InetAddress object of the host name in this record.
   */
  public InetAddress getIPAddress() throws UnknownHostException {
    return InetAddress.getByName(_host);
  }

  /**
   * Return the protocol.
   *
   * @return the protocol.
   */
  public Transport getProtocol() {
    return _proto;
  }

  /**
   * Return the weight.
   *
   * @return the weight.
   */
  public int getWeight() {
    return _weight;
  }

  /**
   * Return the priority.
   *
   * @return the priority.
   */
  public int getLevel() {
    return _priority;
  }

  public String getHostName() {
    return _host;
  }

  /**
   * Return the port number.
   *
   * @return the port number.
   */
  public int getPort() {
    return _port;
  }

  /** return boolean to indicate if the local SRV is enabled. */
  public boolean isEnabled() {
    return _enabled;
  }

  /** set enable/disable flag to local SRV */
  public void setEnabled(boolean flag) {
    _enabled = flag;
  }

  /** return SRV record in String format. */
  public String toString() {
    StringBuffer sb = new StringBuffer(100);
    sb.append("host:" + _host)
        .append(" port:" + _port)
        .append(" priority:" + _priority)
        .append(" weight:" + _weight)
        .append(" transport:" + _proto)
        .append(" enabled:" + _enabled);
    return sb.toString();
  }
}
