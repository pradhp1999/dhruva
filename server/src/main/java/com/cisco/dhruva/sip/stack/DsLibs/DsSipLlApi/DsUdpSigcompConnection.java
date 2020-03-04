// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import org.apache.logging.log4j.Level;

/** For Cisco use only. A compressing connection. */
class DsUdpSigcompConnection extends DsUdpConnection implements Runnable {
  public static final int MAX_FEEDBACK_ERRORS = 3;

  private boolean m_feedbackError; // = false

  protected DsUdpSigcompConnection() {
    super();
  }

  /**
   * Constructs a UDP connection based on the specified binding info.
   *
   * @param binding the binding info that contains the remote address and port number where to make
   *     connection to.
   * @throws SocketException if there is an error while constructing the datagram socket
   */
  protected DsUdpSigcompConnection(DsBindingInfo binding) throws SocketException {
    super(binding);
  }

  /**
   * Constructs a UDP connection to the specified remote address <code>addr</code> and the remote
   * port number <code>port</code>.
   *
   * @param addr the remote address to connect to
   * @param port the remote port number to connect to
   * @throws SocketException if there is an error while constructing the datagram socket
   */
  protected DsUdpSigcompConnection(InetAddress addr, int port) throws SocketException {
    super(addr, port);
  }

  /**
   * Constructs a UDP connection to the specified remote address <code>addr</code> and the remote
   * port number <code>port</code>. It also binds the datagram socket locally to the specified local
   * address <code>laddr</code> and local port number <code>lport</code>.
   *
   * @param laddr the address to bind to locally
   * @param lport the port to bind to locally
   * @param addr the remote address to connect to
   * @param port the remote port number to connect to
   * @throws SocketException if there is an error while constructing the datagram socket
   */
  protected DsUdpSigcompConnection(InetAddress laddr, int lport, InetAddress addr, int port)
      throws SocketException {
    super(laddr, lport, addr, port);
  }

  /**
   * Constructs a UDP connection to the specified remote address <code>addr</code> and the remote
   * port number <code>port</code>.
   *
   * @param network the network to associate with this connection
   * @param addr the remote address to connect to
   * @param port the remote port number to connect to
   * @throws SocketException if there is an error while constructing the datagram socket
   */
  protected DsUdpSigcompConnection(DsNetwork network, InetAddress addr, int port)
      throws SocketException {
    super(network, addr, port);
  }

  /**
   * Constructs a UDP connection to the specified remote address <code>addr</code> and the remote
   * port number <code>port</code>. It also binds the datagram socket locally to the specified local
   * address <code>laddr</code> and local port number <code>lport</code>.
   *
   * @param network the network to associate with this connection
   * @param laddr the address to bind to locally
   * @param lport the port to bind to locally
   * @param addr the remote address to connect to
   * @param port the remote port number to connect to
   * @throws SocketException if there is an error while constructing the datagram socket
   */
  protected DsUdpSigcompConnection(
      DsNetwork network, InetAddress laddr, int lport, InetAddress addr, int port)
      throws SocketException {
    super(network, laddr, lport, addr, port);
  }

  /**
   * Constructs a UDP connection to the specified remote address <code>addr</code> and the remote
   * port number <code>port</code>.
   *
   * @param network the network to associate with this connection
   * @param laddr the address to bind to locally
   * @param lport the port to bind to locally
   * @param addr the remote address to connect to
   * @param port the remote port number to connect to
   * @param doConnect <code>true</code> to connect to the destination
   * @throws SocketException if there is an error while constructing the datagram socket
   */
  protected DsUdpSigcompConnection(
      DsNetwork network,
      InetAddress laddr,
      int lport,
      InetAddress addr,
      int port,
      boolean doConnect)
      throws SocketException {
    super(network, laddr, lport, addr, port, doConnect);
  }

  /**
   * Constructs a UDP connection based on the specified datagram socket.
   *
   * @param socket a DsDatagramSocket object
   */
  DsUdpSigcompConnection(DsDatagramSocket socket) {
    super(socket);
  }

  public byte[] sendCompressed(byte buffer[]) throws IOException {
    return sendToCompressed(
        buffer, m_bindingInfo.getRemoteAddress(), m_bindingInfo.getRemotePort());
  }

  public byte[] sendToCompressed(byte buffer[], InetAddress addr, int port) throws IOException {

    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.log(
          Level.DEBUG, new StringBuffer("Sending Message binding info: ").append(m_bindingInfo));
    }

    DatagramPacket packet = makeDatagramPacket(buffer, addr, port);

    DsSigcompDatagramSocket sock = null;
    try {
      sock = (DsSigcompDatagramSocket) m_socket;
    } catch (Throwable exc) {
      throw new IOException(
          "sendCompressed: connection is not sigcomp capable: " + exc.getMessage());
    }

    SocketException icmpExc = null;

    if (DsLog4j.connectionCat.isEnabled(Level.INFO)) {
      InetAddress laddr = sock.getLocalAddress();
      int lport = sock.getLocalPort();
      DsLog4j.connectionCat.log(
          Level.INFO,
          new StringBuffer(laddr.getHostAddress()).append(':').append(lport).toString());
      DsLog4j.connectionCat.log(
          Level.INFO, new StringBuffer(addr.getHostAddress()).append(':').append(port).toString());
    }

    // TODO:
    //
    //   here check m_feedbackError -- if true, throw exn because there was
    //   a persistent problem receiving/processing sigcomp feedback packets

    try {
      sock.sendCompressed(packet);
    } catch (SocketException se) {
      if (DsLog4j.connectionCat.isEnabled(Level.WARN))
        DsLog4j.connectionCat.log(Level.WARN, "ICMP Error while sending Message on UDP", se);
      icmpExc = se;

      //  if this is a threadlocal socket, we are not in the connection table
      if (isThreadLocal()) closeSocket();
    } catch (Throwable t) {
      // TOOD:  this can spin forever?  someone should figure out what we want to
      // do!  -dg
      if (DsLog4j.connectionCat.isEnabled(Level.WARN))
        DsLog4j.connectionCat.log(Level.WARN, "Exception while sending Message on UDP", t);
    } finally {
      if (null != icmpExc) {
        throw icmpExc;
      }
    }

    // now the compressed data has to be copied out
    //

    byte[] compressed = new byte[packet.getLength()];
    System.arraycopy(packet.getData(), packet.getOffset(), compressed, 0, compressed.length);

    if (DsDebugTransportImpl.set()) {
      InetAddress laddr = sock.getLocalAddress();
      int lport = sock.getLocalPort();
      DsDebugTransportImpl.messageOut(
          DsDebugTransport.POS_CONNECTION,
          DsSipTransportType.UDP,
          compressed,
          laddr,
          lport,
          addr,
          port);
    }

    return compressed;
  }

  static int m_fbThreadID = 0;

  void receiveFeedback() {
    String name = "SigcompFeedback-" + m_fbThreadID++;

    if (DsLog4j.connectionCat.isEnabled(Level.INFO)) {
      DsLog4j.connectionCat.log(Level.INFO, "starting sigcomp feedback listener thread " + name);
    }

    new Thread(this, name).start();
  }

  /** */
  public void run() {
    int errcount = MAX_FEEDBACK_ERRORS;

    DsSigcompDatagramSocket sock = (DsSigcompDatagramSocket) m_socket;
    while (true) {
      try {
        // this call should not return (unless the socket is closed or there
        //  is a problem reading or processing feedback packets)
        sock.receiveFeedback();
      } catch (Throwable t) {
        --errcount;
        if (DsLog4j.connectionCat.isEnabled(Level.WARN)) {
          DsLog4j.connectionCat.log(
              Level.WARN,
              "exception in receiving feedback on sigcomp datagram count = "
                  + (MAX_FEEDBACK_ERRORS - errcount)
                  + " of "
                  + MAX_FEEDBACK_ERRORS,
              t);
        }
        if (errcount == 0) {
          m_feedbackError = true;
          return;
        }
      }
    }
  }
} // Ends class DsUdpSigcompConnection
