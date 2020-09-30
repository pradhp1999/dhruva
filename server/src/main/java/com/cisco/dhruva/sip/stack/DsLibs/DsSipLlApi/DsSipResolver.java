// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.enums.LocateSIPServerTransportType;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipURL;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.transport.Transport;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;

/** A common interface used to search for a SIP endpoint. */
interface DsSipResolver {

  public LocateSIPServersResponse resolve(
      String name, LocateSIPServerTransportType transport, @Nullable Integer port)
      throws ExecutionException, InterruptedException;

  public LocateSIPServersResponse resolve(
      String name,
      LocateSIPServerTransportType transportLookupType,
      @Nullable Integer port,
      @Nullable String userIdInject)
      throws ExecutionException, InterruptedException;

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
  boolean shouldSearch(String host_name, int port, Transport transport);

  /**
   * Return <code>true</code> if this resolver has been configured to support a particular transport
   * as defined in DsSipObject.DsSipTransportType.
   *
   * @return <code>true</code> if this resolver has been configured to support a particular
   *     transport as defined in DsSipObject.DsSipTransportType.
   */
  boolean isSupported(Transport transport);
}
