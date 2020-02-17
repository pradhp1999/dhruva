// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipURL;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/** A common interface used to search for a SIP endpoint. */
interface DsSipResolver {
  /**
   * Return a connection to the next endpoint.
   *
   * @return a connection to the next endpoint
   * @throws DsException if there is an exception in the User Agent
   * @throws IOException if the underlying socket throws this exception
   */
  DsSipConnection tryConnect() throws DsException, IOException;

  /**
   * Return the DsBindingInfo for the connection last returned by tryConnect.
   *
   * @return the DsBindingInfo for the connection last returned by tryConnect.
   */
  DsBindingInfo getCurrentBindingInfo();

  /**
   * Return true if the resolver would initialize to it's current set of endpoints given this URL.
   *
   * @return true if the resolver would initialize to it's current set of endpoints given this URL.
   */
  boolean queryMatches(DsSipURL url, boolean sizeExceedsMTU);

  /**
   * Called to initialize the the resolver. The resolver will typically create a list of potential
   * endpoints to contact. Two versions of signatures - with/without local binding specified.
   *
   * @param network the network to use when resolving
   * @param url the URL to resolve
   * @throws DsSipParserException if the is an exception parsing
   * @throws UnknownHostException if the host cannot be found
   * @throws DsSipServerNotFoundException if the server cannot be found
   */
  void initialize(DsNetwork network, DsSipURL url)
      throws DsSipParserException, UnknownHostException, DsSipServerNotFoundException;

  /**
   * Called to initialize the the resolver. The resolver will typically create a list of potential
   * endpoints to contact. Two versions of signatures - with/without local binding specified.
   *
   * @param network the network to use when resolving
   * @param localAddress the local address
   * @param localPort the local port
   * @param url the URL to resolve
   * @throws DsSipParserException if the is an exception parsing
   * @throws UnknownHostException if the host cannot be found
   * @throws DsSipServerNotFoundException if the server cannot be found
   */
  void initialize(DsNetwork network, InetAddress localAddress, int localPort, DsSipURL url)
      throws DsSipParserException, UnknownHostException, DsSipServerNotFoundException;

  /**
   * Called to initialize the the resolver. The resolver will typically create a list of potential
   * endpoints to contact. Two versions of signatures - with/without local binding specified.
   *
   * @param network the network to use when resolving
   * @param host the host to resolve
   * @param port the port to resolve
   * @param transport the transport to resolve for
   * @throws UnknownHostException if the host cannot be found
   * @throws DsSipServerNotFoundException if the server cannot be found
   */
  void initialize(DsNetwork network, String host, int port, int transport)
      throws UnknownHostException, DsSipServerNotFoundException;

  /**
   * Called to initialize the the resolver. The resolver will typically create a list of potential
   * endpoints to contact. Two versions of signatures - with/without local binding specified.
   *
   * @param network the network to use when resolving
   * @param localAddress the local address
   * @param localPort the local port
   * @param host the host to resolve
   * @param port the port to resolve
   * @param transport the transport to resolve for
   * @throws UnknownHostException if the host cannot be found
   * @throws DsSipServerNotFoundException if the server cannot be found
   */
  void initialize(
      DsNetwork network,
      InetAddress localAddress,
      int localPort,
      String host,
      int port,
      int transport)
      throws UnknownHostException, DsSipServerNotFoundException;

  // void initialize(InetAddress lAddr, int lPort, String host, int port, int proto,
  // boolean haveIP, boolean sipsURL) throws UnknownHostException, DsSipServerNotFoundException;

  /**
   * Called to initialize the the resolver. The resolver will typically create a list of potential
   * endpoints to contact. Two versions of signatures - with/without local binding specified.
   *
   * @param network the network to use when resolving
   * @param lAddr the local address
   * @param lPort the local port
   * @param host the host to resolve
   * @param port the port to resolve
   * @param proto the transport to resolve for
   * @param haveIP <code>true</code> if we have an IP address already
   * @throws UnknownHostException if the host cannot be found
   * @throws DsSipServerNotFoundException if the server cannot be found
   */
  void initialize(
      DsNetwork network,
      InetAddress lAddr,
      int lPort,
      String host,
      int port,
      int proto,
      boolean haveIP)
      throws UnknownHostException, DsSipServerNotFoundException;

  /**
   * Indicate to the resolver that the message is larger than the path MTU.
   *
   * @param sizeExceedsMTU set to <code>true</code> to indicate that the message size exceeds the
   *     path MTU, otherwise set to <code>false</code>.
   */
  void setSizeExceedsMTU(boolean sizeExceedsMTU);

  /**
   * Set the supported transports.
   *
   * @param supported_transports a bit mask of the transports supported by this instance of the
   *     stack.
   */
  void setSupportedTransports(byte supported_transports);

  /**
   * Call before calling initialize to determine whether or not it should be called.
   *
   * @param sip_url the SIP URL to check
   * @throws DsSipParserException if the is an exception parsing
   */
  boolean shouldSearch(DsSipURL sip_url) throws DsSipParserException;

  /**
   * Call before calling initialize to determine whether or not it should be called.
   *
   * @param host the host to resolve
   * @param port the port to resolve
   * @param transport the transport to resolve for
   */
  boolean shouldSearch(String host_name, int port, int transport);

  /**
   * Return <code>true</code> if this resolver has been configured to support a particular transport
   * as defined in DsSipObject.DsSipTransportType.
   *
   * @return <code>true</code> if this resolver has been configured to support a particular
   *     transport as defined in DsSipObject.DsSipTransportType.
   */
  boolean isSupported(int transport);
}
