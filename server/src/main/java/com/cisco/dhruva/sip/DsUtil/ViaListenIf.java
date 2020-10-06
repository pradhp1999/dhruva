package com.cisco.dhruva.sip.DsUtil;

import com.cisco.dhruva.sip.proxy.DsViaListenInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.Transport;
import java.net.InetAddress;
import java.net.UnknownHostException;

/*
 * An implementation of the DsViaListenIf interface for storing the via address
 * the re should use.
 */
public class ViaListenIf extends ListenIf implements DsViaListenInterface {

  protected int srcPort = -1;
  protected InetAddress srcAddress = null;
  protected InetAddress translatedSrcAddress = null;

  public ViaListenIf(
      int port,
      Transport protocol,
      DsByteString interfaceIP,
      boolean attachExternalIp,
      DsNetwork direction,
      int srcPort,
      InetAddress scrAddress,
      DsByteString translatedAddress,
      InetAddress translatedSrcAddress,
      int translatedPort)
      throws UnknownHostException, DsException {
    super(
        port,
        protocol,
        interfaceIP,
        direction,
        translatedAddress,
        translatedPort,
        attachExternalIp);
    this.srcPort = srcPort;
    this.srcAddress = srcAddress;
    this.translatedSrcAddress = translatedSrcAddress;
  }

  public int getSourcePort() {
    return this.srcPort;
  }

  public InetAddress getSourceAddress() {
    return this.srcAddress;
  }

  /*
   * To help with debuging
   */
  public String toString() {
    return "Via Address "
        + addressStr
        + ":"
        + port
        + " ["
        + protocol
        + "] "
        + "-- Src Address "
        + srcAddress;
  }
}
