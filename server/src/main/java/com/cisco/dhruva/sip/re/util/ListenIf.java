/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.sip.re.util;

import com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy.DsListenInterface;
import com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy.DsSipProxyManager;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.Trace;
import java.io.*;
import java.net.*;
import java.net.InetAddress;

/** the class describing an interface (port/protocol for now) to listen on */
public class ListenIf implements DsListenInterface {

  protected int port;
  protected Transport protocol;
  protected DsByteString addressStr = null;

  protected InetAddress addressInet = null;
  protected static final int UDP_CLEANUP_TIME = 90; // in secs

  protected InetAddress translatedAddressInet = null;

  DsSipProxyManager manager;

  protected DsNetwork network =
      null; // this data member indicated INTERNAL, EXTERNAL or EXTERNAL_OBTAIN in listen command

  // hold translated address/port from listen external command
  protected DsByteString translatedAddressStr = null;
  protected int translatedPort = -1;

  // Our Log object
  protected static Trace Log = Trace.getTrace(ListenIf.class.getName());

  /**
   * Creates a new ListenIf that based on the resolveAddress parameter, will try create an
   * internally stored InetAddress used when startListening() is called.
   *
   * @param port The port to listen on
   * @param interfaceIP The host name or ip address to listen on
   * @param translatedAddress The address that will be put in the via if this is an external
   *     interface. Usefull if you are listening on one interface, but want external devices to see
   *     another.
   * @param translatedPort The port that will be put in the via if this is an external interface.
   *     Usefull if you are listening on one interface, but want external devices to see another.
   */
  public ListenIf(
      int port,
      Transport protocol,
      DsByteString interfaceIP,
      InetAddress address,
      DsNetwork direction,
      DsByteString translatedInterfaceIP,
      InetAddress translatedAddress,
      int translatedPort) {
    this.port = port;
    this.protocol = protocol;
    this.network = direction;

    this.addressInet = address;
    addressStr = interfaceIP;

    if (translatedInterfaceIP != null && translatedAddress != null) {
      this.translatedAddressStr = translatedInterfaceIP;
      this.translatedPort = translatedPort;
      this.translatedAddressInet = translatedAddress;
    } else {
      this.translatedAddressStr = addressStr;
      this.translatedAddressInet = address;
      this.translatedPort = port;
    }
  }

  public ListenIf(
      int port,
      Transport protocol,
      DsByteString interfaceIP,
      DsNetwork direction,
      DsByteString translatedInterfaceIP,
      int translatedPort)
      throws UnknownHostException {
    this(
        port,
        protocol,
        interfaceIP,
        InetAddress.getByName(interfaceIP.toString()),
        direction,
        translatedInterfaceIP,
        null,
        translatedPort);

    this.addressInet = InetAddress.getByName(interfaceIP.toString());

    if (translatedInterfaceIP != null) {
      this.translatedAddressInet = InetAddress.getByName(translatedInterfaceIP.toString());
    } else {
      this.translatedAddressInet = this.addressInet;
    }
  }

  /**
   * This constructor is useful when creating temporary ListenIfs to do lookups in a hashmap with,
   * since the hash and equality depends only on these three values. Using this constructor will
   * cause getAddress to return null.
   */
  public ListenIf(int port, Transport protocol, InetAddress address) {
    this(port, protocol, null, address, null, null, null, 0);
  }

  public int getPort() {
    return port;
  }

  /**
   * @return The normalized protocol this interface is listening on
   * @see //DsControllerConfig.normalizedProtocol()
   */
  public Transport getProtocol() {
    return protocol;
  }

  public InetAddress getInetAddress() {
    if (translatedAddressInet != null) {
      return translatedAddressInet;
    } else {
      return addressInet;
    }
  }

  /** @return the translated address for the external interface or null if none was specified; */
  public DsByteString getNonTranslatedAddress() {
    return addressStr;
  }
  /** @return the translated address for the external interface or null if none was specified; */
  public InetAddress getNonTranslatedInetAddress() {
    return addressInet;
  }

  /**
   * Returns a String representation of the interface this object represents. It will always be the
   * IP address of this address, even if a host name was used to construct it.
   */
  public DsByteString getAddress() {
    if (translatedAddressStr != null) {
      return translatedAddressStr;
    } else {
      return addressStr;
    }
  }

  /** @return the translated address for the external interface or null if none was specified; */
  public DsByteString getTranslatedAddress() {
    return translatedAddressStr;
  }

  /**
   * @return the translated port for the external interface or the listen port if none was
   *     specified;
   */
  public int getTranslatedPort() {
    return translatedPort;
  }

  /** Returns true if the port, protocol and address are the same. */
  public boolean equals(Object o) {
    if (o == null) return false;

    ListenIf listenIf = (ListenIf) o;

    return listenIf.getPort() == this.port
        && listenIf.getProtocol() == this.protocol
        &&
        // listenIf.getAddress().equals( this.getAddress() ) )
        listenIf.getNonTranslatedInetAddress().equals(addressInet);
  }

  public int hashCode() {
    long sum = (port * addressInet.hashCode() * protocol.ordinal());
    return (int) (sum % Integer.MAX_VALUE);
  }

  public String toString() {
    return "ListenIf: addressStr="
        + this.addressStr
        + ", port = "
        + this.port
        + ", protocol= "
        + protocol
        + ", protocolInt= "
        + protocol
        + ", translatedAddressStr = "
        + this.translatedAddressStr
        + ", translatedPort = "
        + this.translatedPort
        + ", direction="
        + network;
  }

  public DsNetwork getNetwork() {
    return network;
  }
}
