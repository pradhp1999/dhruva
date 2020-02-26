// Copyright (c) 2005-2015 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipBufferStream;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

/**
 * Defines a concrete connection that is used to send data across the network through the underlying
 * TCP socket. This connection represents a Non-Blocking TCP connection and uses "java.nio" package
 * from JDK v1.4.x to provide Non-Blocking behaviour.
 *
 * <p>This concrete connection can be constructed through the {@link DsDefaultConnectionFactory
 * DsDefaultConnectionFactory} by passing appropriate parameter like transport type and address.
 */
public class DsTcpNBConnection extends DsAbstractConnection implements DsSelectable {

  private static final String EVENTREASON =
      "FrameMessage queue exceeded the threshold, this might be because of continuous stream of requests without 'content-length' header ";

  /**
   * A static reference to the transport layer so the we can remove connections from the connection
   * table when they are closed.
   */
  static DsSipTransportLayer m_transportLayer;

  /** Flag to control the use of direct buffers, they are on by default. */
  static boolean m_useDirectBuffers = true;

  /** A counter to create unique IDs for the connections. */
  private static int counter;
  /** A lock for incrementing the counter. */
  private static Object m_ctrLock = new Object();

  /** The logger for connection information. */
  private static Logger cat = DsLog4j.connectionCat;

  /** The factory that creates message bytes to process the framed message. */
  static DsMessageBytesFactory m_mbFactory = new DsSipMessageBytesFactory();

  /** Maximum size of m_queuedInBB * */
  static int maxsize_m_queuedInBB =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_QUEUE_IN_TCP_FRAME_MESSAGE,
          DsConfigManager.PROP_QUEUE_IN_TCP_FRAME_MESSAGE_DEFAULT);
  /** The unique identifier of this connection. */
  private int m_id;
  /** The underlying socket. */
  protected DsSocket m_socket;
  /** The nio channel for sending and receiving data. */
  protected SocketChannel m_channel;
  /** The selection key associated with this connection. */
  protected SelectionKey m_sk;

  /** The lock to synchronize on when writing data to AppSendBuffer */
  protected Object m_writeLock = new Object();
  /**
   * Since read method is synchronized on this object no readSpecific lock is needed.This acts as
   * lock to ApprecvBuffer
   */
  /** Thread-local factory that holds the network buffer. */
  protected static ThreadLocal m_netFactory = new NetByteBufferInitializer();

  /** Buffer that stores bytes in this connection when a full SIP message has not arrived. */
  protected ByteBuffer m_queuedInBB;
  /**
   * Buffer that stores bytes in this connection when a full SIP message was not completely written.
   */
  protected ByteBuffer m_queuedOutBB;

  /** Protected default constructor for the derived classes. */
  protected DsTcpNBConnection() {
    synchronized (m_ctrLock) {
      m_id = counter++;
    }
  }

  public void setChannel(SocketChannel channel) {
    this.m_channel = channel;
  }
  /**
   * Closes the underlying socket (channel). Also removes it from the connection table.
   *
   * @throws IOException this implementation of the interface DOES NOT throw the IOExcpetion if
   *     there is an error while closing the socket or if the socket is already closed. It simply
   *     logs the occurance since we are no longer interested in using the socket.
   */
  public void closeSocket() throws IOException {
    int localPort = -1;
    if (m_channel != null) {
      // not sure what to do here, we still get IO Exceptions in doRead() on the server
      // side after we close TCP connections, but TLS is fine.
      String socketInfo = null;
      try {
        if (m_socket != null) {
          socketInfo = m_socket.getSocketInfo();
          localPort = m_socket.getLocalPort();
        }

        // this close will remove the socket from the Selector
        m_channel.close();
        // Information for connection get closed
        // localport is coming zero if we use bindinginfo so socket is
        // used and if we use socket for local ip its coming zero so
        // bindinginfo is used
        // TODO saevent-restructure log a ConnectionEvent here to notify a disconnect

        if (DsLog4j.connectionCat.isEnabled(Level.INFO)) {
          DsLog4j.connectionCat.info("closeSocket() - socket" + socketInfo + " closed");
        }
      } catch (Exception e) {
        // ignore any exceptions, since we are done anyway
        if (DsLog4j.connectionCat.isEnabled(Level.INFO)) {
          DsLog4j.connectionCat.info(
              "closeSocket() - exception closing socket: " + socketInfo + "Exception is :", e);
        }
      }

      m_channel = null;
      m_socket = null;

      m_transportLayer.m_ConnectionTable.remove(this);
    } else {
      if (DsLog4j.connectionCat.isEnabled(Level.INFO)) {
        DsLog4j.connectionCat.info("closeSocket() - channel already null,");
      }
    }
  }

  /**
   * Returns the underlying socket. The channel can be retrieved from this socket.
   *
   * @return the underlying socket.
   */
  public DsSocket getSocket() {
    return m_socket;
  }

  /**
   * Constructs a TCP connection based on the specified binding info <code>binding</code>.
   *
   * @param binding the binding info that contains the remote address and port number where to make
   *     connection to.
   * @throws IOException if there is an error with the underlying socket
   * @throws SocketException if there is an error while constructing the TCP socket
   */
  protected DsTcpNBConnection(DsBindingInfo binding) throws IOException, SocketException {
    this(
        binding.getLocalAddress(),
        binding.getLocalPort(),
        binding.getRemoteAddress(),
        binding.getRemotePort(),
        binding.getNetwork());
  }

  /**
   * Constructs a TCP connection to the specified remote address <code>rInetAddress</code> and the
   * remote port number <code>rPortNo</code>.
   *
   * @param rInetAddress the remote address to connect to
   * @param rPortNo the remote port number to connect to
   * @throws IOException if there is an error with the underlying socket
   * @throws SocketException if there is an error while constructing the TCP socket
   */
  protected DsTcpNBConnection(InetAddress rInetAddress, int rPortNo)
      throws IOException, SocketException {
    this(null, DsBindingInfo.LOCAL_PORT_UNSPECIFIED, rInetAddress, rPortNo, DsNetwork.getDefault());
  }

  /**
   * Constructs a TCP connection to the specified remote address <code>anInetAddress</code> and the
   * remote port number <code>aPortNo</code>.
   *
   * @param rInetAddress the remote address to connect to
   * @param rPortNo the remote port number to connect to
   * @param network The network with which this connection is associated.
   * @throws IOException if there is an error with the underlying socket
   * @throws SocketException if there is an error while constructing the TCP socket
   */
  protected DsTcpNBConnection(InetAddress rInetAddress, int rPortNo, DsNetwork network)
      throws IOException, SocketException {
    this(null, DsBindingInfo.LOCAL_PORT_UNSPECIFIED, rInetAddress, rPortNo, network);
  }

  /**
   * Constructs a TCP connection to the specified remote address <code>anInetAddress</code> and the
   * remote port number <code>aPortNo</code>. It also binds the datagram socket locally to the
   * specified local address <code>lInetAddress</code> and local port number <code>lPort</code>.
   *
   * @param lInetAddress the address to bind to locally
   * @param lPort the port to bind to locally
   * @param rInetAddress the remote address to connect to
   * @param rPortNo the remote port number to connect to
   * @throws IOException if there is an error with the underlying socket
   * @throws SocketException if there is an error while constructing the TCP socket
   */
  protected DsTcpNBConnection(
      InetAddress lInetAddress, int lPort, InetAddress rInetAddress, int rPortNo)
      throws IOException, SocketException {
    this(lInetAddress, lPort, rInetAddress, rPortNo, DsNetwork.getDefault());
  }

  /**
   * Constructs a TCP connection to the specified remote address <code>rInetAddress</code> and the
   * remote port number <code>rPortNo</code>. It also binds the datagram socket locally to the
   * specified local address <code>lInetAddress</code> and local port number <code>lPort</code>.
   *
   * @param lInetAddress the address to bind to locally
   * @param lPort the port to bind to locally
   * @param rInetAddress the remote address to connect to
   * @param rPortNo the remote port number to connect to
   * @param network The network with which this connection is associated.
   * @throws IOException if there is an error with the underlying socket
   * @throws SocketException if there is an error while constructing the TCP socket
   */
  protected DsTcpNBConnection(
      InetAddress lInetAddress, int lPort, InetAddress rInetAddress, int rPortNo, DsNetwork network)
      throws IOException, SocketException {
    this();
    init(lInetAddress, lPort, rInetAddress, rPortNo, network);
  }

  /**
   * Constructs a TCP connection to the specified remote address <code>rInetAddress</code> and the
   * remote port number <code>rPortNo</code>.
   *
   * @param lInetAddress the address to bind to locally
   * @param lPort the port to bind to locally
   * @param rInetAddress the remote address to connect to
   * @param rPortNo the remote port number to connect to
   * @param network The network with which this connection is associated.
   * @param doConnect <code>true</code> to connect to the destination
   * @throws IOException if there is an error with the underlying socket
   * @throws SocketException if there is an error while constructing the TCP socket
   */
  protected DsTcpNBConnection(
      InetAddress lInetAddress,
      int lPort,
      InetAddress rInetAddress,
      int rPortNo,
      DsNetwork network,
      boolean doConnect)
      throws IOException, SocketException {
    this();
    if (doConnect) {
      init(lInetAddress, lPort, rInetAddress, rPortNo, network);
    } else {
      m_isConnecting = true;
      if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
        DsLog4j.connectionCat.log(Level.DEBUG, "DsTcpNBConnection():begin");
        DsLog4j.connectionCat.log(
            Level.DEBUG,
            "DsTcpNBConnection(): lInetAddress: "
                + lInetAddress
                + "; lPort: "
                + lPort
                + "; rInetAddress "
                + rInetAddress
                + "; rPortNo"
                + rPortNo);
      }
      m_channel = null;
      m_socket = null;
      m_bindingInfo =
          new DsBindingInfo(lInetAddress, lPort, rInetAddress, rPortNo, DsSipTransportType.TCP);
      m_bindingInfo.setNetwork(network);
      updateTimeStamp();
    }
  }

  protected void init(
      InetAddress lInetAddress, int lPort, InetAddress rInetAddress, int rPortNo, DsNetwork network)
      throws IOException, SocketException {
    Socket socket = null;
    // fix for issue CSCsr74483 - Socket leak due to the Socket Exception.
    try {
      if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
        DsLog4j.connectionCat.log(Level.DEBUG, "DsTcpNBConnection():begin");
        DsLog4j.connectionCat.log(
            Level.DEBUG,
            "DsTcpNBConnection(): lInetAddress: "
                + lInetAddress
                + "; lPort: "
                + lPort
                + "; rInetAddress "
                + rInetAddress
                + "; rPortNo"
                + rPortNo);
      }
      // m_channel = DsSocketChannelFactory.getSocketChannel();
      m_channel = NetObjectsFactory.getSocketChannel();
      socket = m_channel.socket();

      // bind has to be done before connect or else the connect will
      // cause an OS chosen bind to occur
      socket.bind(new InetSocketAddress(lInetAddress, lPort));
      // we could connect via selection process as reads and write are done
      // but we choose not to because it simply won't add to speed of getting
      // up a connection.

      // CSCto35150: TCP connection setup takes a long time to fail
      // call connect on the socket instead of the channel. this gives the ability to
      // set connection setup timeout.
      // m_channel.connect(new InetSocketAddress(rInetAddress, rPortNo));
      socket.connect(
          new InetSocketAddress(rInetAddress, rPortNo), network.getTcpConnectionTimeout());
      m_socket = new DsSocket(socket, network);
      if (m_bindingInfo == null) {
        m_bindingInfo =
            new DsBindingInfo(lInetAddress, lPort, rInetAddress, rPortNo, DsSipTransportType.TCP);
      }
      m_bindingInfo.updateBindingInfo(m_socket);
      m_bindingInfo.setNetwork(network);
      // TODO saevent-restructure log a ConnectionEvent here

      m_channel.finishConnect();
      m_channel.configureBlocking(false);
      register();

      updateTimeStamp();

      if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
        DsLog4j.connectionCat.log(Level.DEBUG, "DsTcpNBConnection():end");
      }
    } catch (SocketException se) {
      // TODO saevent-restructure log a ConnectionErrorEvent here
      DsLog4j.connectionCat.log(Level.ERROR, "DsTcpNBConnection(): Socket Exception", se);
      try {
        if (m_channel != null) {
          m_channel.close();
        }
      } catch (IOException ioe) {
        if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
          DsLog4j.connectionCat.log(
              Level.DEBUG, "DsTcpNBConnection(): I/O Exception on close: ", ioe);
        }
      }
      // REFACTOR
      //      DsProxyTcpConnectException tcpConnectException =
      //          new DsProxyTcpConnectException(se, m_bindingInfo);
      //      se.addSuppressed(tcpConnectException);
      throw se;
    } catch (IOException e) {
      // TODO saevent-restructure log a ConnectionErrorEvent here
      if (DsLog4j.connectionCat.isEnabled(Level.WARN)) {
        DsLog4j.connectionCat.log(Level.WARN, "DsTcpNBConnection(): I/O Exception: ", e);
      }
      // REFACTOR
      //      DsProxyTcpConnectException tcpConnectException =
      //          new DsProxyTcpConnectException(e, m_bindingInfo);
      //      e.addSuppressed(tcpConnectException);
      throw e;
    }
  }

  public void initiateConnect() throws IOException, SocketException {
    if (m_socket != null) {
      if (DsLog4j.connectionCat.isEnabled(Level.ERROR)) {
        DsLog4j.connectionCat.log(
            Level.ERROR, "Connection already connected [ " + m_bindingInfo + "]");
      }
      return;
    }
    init(
        m_bindingInfo.getLocalAddress(),
        m_bindingInfo.getLocalPort(),
        m_bindingInfo.getRemoteAddress(),
        m_bindingInfo.getRemotePort(),
        m_bindingInfo.getNetwork());
  }

  /**
   * Constructs a TCP connection based on the specified socket.
   *
   * @param socket a DsSocket object
   * @throws IOException if there is an error with the underlying socket
   */
  DsTcpNBConnection(DsSocket socket) throws IOException {
    this();

    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.log(Level.DEBUG, "DsTcpNBConnection(socket):begin");
    }

    m_bindingInfo =
        new DsBindingInfo(
            socket.getLocalAddress(),
            socket.getLocalPort(),
            socket.getRemoteInetAddress(),
            socket.getRemotePort(),
            DsSipTransportType.TCP);
    m_bindingInfo.setNetwork(socket.getNetwork());
    m_socket = socket;

    m_channel = socket.getSocket().getChannel();
    m_channel.configureBlocking(false);
    register();

    updateTimeStamp();

    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.log(Level.DEBUG, "DsTcpNBConnection(socket):end");
    }
  }

  /**
   * Registers this Non-Blocking TCP connection with the Socket Selector to receive notifications if
   * the underlying socket is ready to be read.
   */
  public void register() {
    DsSelector.register(this);
  }

  /**
   * Sends the specified data buffer across the network through the underlying socket to the desired
   * destination. The data destination is specified in this connection's binding info.
   *
   * @param buffer the message bytes to send across
   * @throws IOException if there is an I/O error while sending the message
   */
  public void send(byte buffer[]) throws IOException {
    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.log(
          Level.DEBUG,
          new StringBuffer("Sending Message binding info (NB): ").append(m_bindingInfo));
    }
    if (DsLog4j.connectionCat.isEnabled(Level.INFO)) {
      DsLog4j.connectionCat.log(
          Level.INFO,
          new StringBuffer("Sending Message (NB): ")
              .append(
                  DsSipMessage.maskAndWrapSIPMessageToSingleLineOutput(
                      DsByteString.newString(buffer))));
    }

    if (DsDebugTransportImpl.set()) {
      InetAddress laddr = m_bindingInfo.getLocalAddress();
      int lport = m_bindingInfo.getLocalPort();
      InetAddress addr = m_bindingInfo.getRemoteAddress();
      int port = m_bindingInfo.getRemotePort();
      DsDebugTransportImpl.messageOut(
          DsDebugTransport.POS_CONNECTION,
          m_bindingInfo.getTransport(),
          buffer,
          laddr,
          lport,
          addr,
          port);
    }

    updateTimeStamp();

    IOException exc = null;
    synchronized (m_writeLock) {
      ByteBuffer bb = getNetBB();
      bb.clear();
      bb.put(buffer);
      // fix for issue CSCsr74483 - Socket leak due to the Socket Exception.
      try {
        // Try writing to the socket anyway. It will return immediately
        // regardless, whether any bytes are written to the socket.
        if (!doWrite(bb)) {
          DsSelector.addInterestOps(m_sk, SelectionKey.OP_WRITE);
        }
      } catch (IOException e) {
        exc = e;

        if (cat.isEnabled(Level.WARN)) {
          String socketInfo = null;
          if (m_socket != null) socketInfo = m_socket.getSocketInfo();
          cat.warn(
              "Exception while writing to the socket channel for socket "
                  + socketInfo
                  + " : closing socket channel ",
              exc);
        }

        try {
          closeSocket();
        } catch (Exception ioe) {
          if (cat.isEnabled(Level.INFO)) {
            cat.info("Exception while closing the channel:", ioe);
          }
        }
      }
    }

    // move the notify out of the critical section to avoid deadlock
    if (exc != null) {
      notifyListeners(new DsConnectionClosedEvent(this));
      throw exc;
    }
  }

  /**
   * Sends the specified data buffer across the network through the underlying socket to the desired
   * destination. The data destination is specified in this connection's binding info.
   *
   * @param bb the message bytes to send across
   * @throws IOException if there is an I/O error while sending the message
   */
  public void send(ByteBuffer bb) throws IOException {
    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.log(
          Level.DEBUG,
          new StringBuffer("Sending Message binding info (NB): ").append(m_bindingInfo));
    }

    if (DsLog4j.connectionCat.isEnabled(Level.INFO)) {
      DsLog4j.connectionCat.log(
          Level.INFO,
          new StringBuffer("Sending Message (NB): ")
              .append(
                  DsSipMessage.maskAndWrapSIPMessageToSingleLineOutput(getStringFromBuffer(bb))));
    }

    updateTimeStamp();

    IOException exc = null;
    synchronized (m_writeLock) {
      // fix for issue CSCsr74483 - Socket leak due to the Socket Exception.
      try {
        // Try writing to the socket anyway. It will return immediately
        // regardless, whether any bytes are written to the socket.
        if (!doWrite(bb)) {
          DsSelector.addInterestOps(m_sk, SelectionKey.OP_WRITE);
        }
      } catch (IOException e) {
        exc = e;

        if (cat.isEnabled(Level.WARN)) {
          String socketInfo = null;
          if (m_socket != null) socketInfo = m_socket.getSocketInfo();
          cat.warn(
              "Exception while writing to the socket channel for socket "
                  + socketInfo
                  + " : closing  socket channel ",
              exc);
        }

        try {
          closeSocket();
        } catch (Exception ioe) {
          if (cat.isEnabled(Level.INFO)) {
            cat.info("Exception while closing the channel:", ioe);
          }
        }
      }
    }

    // move the notify out of the critical section to avoid deadlock
    if (exc != null) {
      notifyListeners(new DsConnectionClosedEvent(this));
      throw exc;
    }
  }

  /**
   * Sends the specified data buffer across the network through the underlying socket to the desired
   * destination. The data destination is specified in this connection's binding info.
   *
   * @param buffer the message bytes to send across
   * @param addr the remote address to which the bytes will be sent
   * @param port the remote port on the remote host to which the send will be directed
   * @throws IOException if there is an I/O error while sending the message
   * @throws IllegalArgumentException if <code>addr</code> and <code>port</code> do not match their
   *     counterparts in this connection binding info
   */
  public void sendTo(byte buffer[], InetAddress addr, int port) throws IOException {
    // exception if addr and port don't match
    if ((!m_bindingInfo.getRemoteAddress().equals(addr))
        || (port != m_bindingInfo.getRemotePort())) {
      throw new IllegalArgumentException(
          "Attempt to send to "
              + addr
              + ":"
              + port
              + " which is different from "
              + m_bindingInfo.getRemoteAddress()
              + ":"
              + m_bindingInfo.getRemotePort()
              + " where this socket is already connected.");
    }

    send(buffer);
  }

  //////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////////

  //////////////////////////////////
  // BEGIN DsSelectable Interface //
  //////////////////////////////////

  public int getID() {
    return m_id;
  }

  public SelectableChannel getChannel() {
    return m_channel;
  }

  public int getOperation() {
    return SelectionKey.OP_READ;
  }

  public void setSelectionKey(SelectionKey sk) {
    m_sk = sk;
  }

  public SelectionKey getSelectionKey() {
    return m_sk;
  }

  ////////////////////////////////
  // END DsSelectable Interface //
  ////////////////////////////////

  /**
   * Read the bytes from the network and queue them for processing. We do not expect to have
   * multiple threads calling this method at the same time, but it can happen so this method is now
   * synchronized.
   *
   * @throws Exception if there is an exception during reading of the socket channel or processing
   *     the message.
   */
  protected synchronized int doRead() throws Exception {
    int bytesRead = 0;
    ByteBuffer bb = getNetBB();

    bb.clear();
    bytesRead = m_channel.read(bb);
    if (bytesRead < 0) {
      String socketInfo = null;
      if (m_socket != null) socketInfo = m_socket.getSocketInfo();
      throw new IOException("Socket read error in socket " + socketInfo);
    }

    bb.flip();

    if (m_queuedInBB != null) {
      // there are bytes we need to account for before the ones that we just read
      // combine them into one byte buffer, and then read from there
      m_queuedInBB.flip();
      bb = combineBytes(m_queuedInBB, bb);
    }
    frameMessages(bb);

    return bytesRead;
  }

  /**
   * Takes the bytes in the <code>queuedBytes</code> and the bytes in the <code>bb</code> and put
   * them into a single byte buffer, with the bytes in <code>queuedBytes</code> first and then the
   * bytes in <code>bb</code>. The resultant byte buffer is flipped after writing these 2 sets of
   * bytes. <code>bb</code> is cleared.
   *
   * @return the new byte buffer that has been flipped with the combined bytes
   */
  protected ByteBuffer combineBytes(ByteBuffer queuedBytes, ByteBuffer bb) {
    ByteBuffer combinedBB = ByteBuffer.allocate(bb.remaining() + queuedBytes.remaining());
    combinedBB.put(queuedBytes).put(bb).flip();

    // remove when clear logic is changed
    bb.clear();

    return combinedBB;
  }

  /**
   * Frames SIP messages from the byte buffer that is passed in.
   *
   * @param bb the byte buffer from which to frame SIP messages.
   * @throws BufferUnderflowException when there are no more SIP messages to frame
   * @throws Exception if there is any other kind of exception while framing a message
   */
  protected void frameMessages(ByteBuffer bb) throws Exception {
    byte msg[];

    try {
      while ((msg = DsSipBufferStream.readMsg(bb)) != null) {
        if (DsLog4j.wireCat.isEnabled(Level.INFO)) {
          DsLog4j.wireCat.log(
              Level.INFO,
              "Received tcp:\n"
                  + DsSipMessage.maskAndWrapSIPMessageToSingleLineOutput(
                      DsByteString.newString(msg)));
        }

        DsSipTransportLayer.getWorkQueue()
            .execute(m_mbFactory.createMessageBytes(msg, (DsBindingInfo) m_bindingInfo.clone()));
      }

      // all old bytes have been read now
      m_queuedInBB = null;
    } catch (BufferUnderflowException bue) {
      queueRemainingInBytes(bb);
    }
  }

  /**
   * Writes the stored data (if any) and then writes the data stored in the thread-local network
   * output buffer. If there is any data left, it stores that data to be written later.
   *
   * @throws IOException if there is an exception when writing the data to the channel.
   */
  protected boolean doWrite(ByteBuffer bb) throws IOException {
    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.debug("NB-Conn_" + m_id + ": Enter doWrite()");
    }

    synchronized (m_writeLock) {
      bb.flip();

      // The first thing we need to do is write any left over data
      if (!writeQueuedBytes()) {
        if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
          DsLog4j.connectionCat.debug("NB-Conn_" + m_id + ": writeQueuedBytes() failed.");
        }

        ByteBuffer combinedBB = ByteBuffer.allocate(bb.remaining() + m_queuedOutBB.remaining());
        combinedBB.put(m_queuedOutBB).put(bb);

        // remove when clear logic is changed
        bb.clear();

        m_queuedOutBB = combinedBB;

        return false;
      }

      // once we get here, there are no queued bytes waiting to be written
      if (m_channel == null) return false;

      int totalBytes = bb.remaining();
      int bytesWritten = m_channel.write(bb);

      if (bytesWritten < totalBytes) {
        if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
          DsLog4j.connectionCat.debug(
              "NB-Conn_"
                  + m_id
                  + ": bytesWritten < totalBytes. ("
                  + bytesWritten
                  + " < "
                  + totalBytes
                  + ")");
        }

        // make a new array with just the bytes that did not make it
        queueRemainingOutBytes(bb);

        // There is more data to write on this channel
        // Later, we will register for OP_WRITE

        // actually that is not true if we got here through the user calling send()
        return false;
      }

      bb.clear();
    }
    return true;
  }

  /**
   * Writes any un-sent bytes stored as member data of this connection. Any remaining un-sent bytes
   * are again stored as member data of this connection. If this method returns <code>true</code>,
   * m_queuedOutBB is <code>null</code>, else there is still data to write and m_queuedOutBB is not
   * <code>null</code>.
   *
   * <p><b>NOTE: caller MUST hold m_writeLock to call this method.</b>
   *
   * @return <code>true</code> if there was no data to write or all data was written, <code>false
   *     </code> if there was data to write and it was not all writen.
   * @throws IOException if there is an exception when writing the data to the channel.
   */
  protected boolean writeQueuedBytes() throws IOException {
    if (m_queuedOutBB == null) {
      // nothing to write, this will usually be the case
      return true;
    }

    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.debug("NB-Conn_" + m_id + ": Writing Queued Bytes.");
    }

    m_queuedOutBB.flip();

    int totalBytes = m_queuedOutBB.remaining();
    int bytesWritten = m_channel.write(m_queuedOutBB);

    if (bytesWritten < totalBytes) {
      if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
        DsLog4j.connectionCat.debug(
            "NB-Conn_"
                + m_id
                + ": bytesWritten < totalBytes. ("
                + bytesWritten
                + " < "
                + totalBytes
                + ")");
      }

      // There is more data to write on this channel
      // Later, we will register for OP_WRITE

      return false;
    }

    // else - all of the data was written

    m_queuedOutBB = null;

    // All data was written
    // Later, we will register for OP_READ

    return true;
  }

  /**
   * Stores the un-sent bytes as member data of this connection. This data will be sent before any
   * other data, when we are able to write data again.
   *
   * <p><b>NOTE: caller MUST hold m_writeLock to call this method.</b>
   *
   * @param bb the byte buffer that contains the bytes to queue
   */
  protected void queueRemainingOutBytes(ByteBuffer bb) {
    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.debug(
          "NB-Conn_" + m_id + ": queueRemainingOutBytes() called - remaining = " + bb.remaining());
    }

    m_queuedOutBB = ByteBuffer.allocate(bb.remaining());
    m_queuedOutBB.put(bb);

    bb.clear();
  }

  /**
   * Stores the unsed bytes (partial SIP message) as member data of this connection. This data will
   * be pre-pended into the net input buffer before the next read.
   *
   * @param bb the byte buffer that contains the bytes to queue
   */
  protected void queueRemainingInBytes(ByteBuffer bb) {
    // go back to the last mark
    bb.reset();

    if (bb.remaining() == 0) {
      // nothing to save
      m_queuedInBB = null;
    } else {
      if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
        DsLog4j.connectionCat.debug("NB-Conn_" + m_id + ": queueRemainingInBytes() called.");
      }

      // otherwise, there is a partial SIP message that we need to save
      m_queuedInBB = ByteBuffer.allocate(bb.remaining());
      m_queuedInBB.put(bb);
    }
    if (m_queuedInBB != null && m_queuedInBB.position() > maxsize_m_queuedInBB) {
      // TODO saevent-restructure log a ConnectionErrorEvent here

      m_queuedInBB = null;
    }

    bb.clear();
  }

  /**
   * Debug method to get a buffer as a String.
   *
   * @param buf the buffer to get as a String
   */
  private static String getStringFromBuffer(ByteBuffer buf) {
    StringBuffer sb = new StringBuffer(buf.limit());
    for (int i = 0; i < buf.limit(); i++) {
      sb.append(buf.get(i));
    }
    return sb.toString();
  }

  //    /**
  //     * Debug method to dump the contents of a byte buffer.
  //     *
  //     * @param buf the buffer to print.
  //     */
  //    private static void printBuffer(ByteBuffer buf)
  //    {
  //        System.out.println("{" + getStringFromBuffer(buf) + "}");
  //    }

  /**
   * Gets the thread-local network byte buffer.
   *
   * @return the thread-local network output buffer
   */
  protected final ByteBuffer getNetBB() {
    return (ByteBuffer) m_netFactory.get();
  }

  /**
   * Sets the message bytes factory for all nio TCP connections.
   *
   * @param factory the message bytes factory to associate with nio connections.
   */
  public static void setDsMessageBytesFactory(DsMessageBytesFactory factory) {
    if (factory == null) {
      m_mbFactory = new DsSipMessageBytesFactory();
      return;
    }

    m_mbFactory = factory;
  }

  //////////////////////////////////
  // BEGIN DsUnitOfWork Interface //
  //////////////////////////////////

  public void abort() {
    // not implemented
  }

  public void run() {
    process();
  }

  public void process() {
    try {
      if (!m_sk.isValid()) {
        closeSocket();
        return;
      }

      if (m_sk.isReadable()) {
        doRead();
      }

      if (m_sk.isWritable()) {
        synchronized (m_writeLock) {
          if (!writeQueuedBytes()) {
            if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
              DsLog4j.connectionCat.debug("NB-Conn_" + m_id + ": writeQueuedBytes() failed.");
            }

            ByteBuffer tmp = ByteBuffer.allocate(m_queuedOutBB.remaining());
            tmp.put(m_queuedOutBB);
            m_queuedOutBB = tmp;

            DsSelector.addInterestOps(m_sk, SelectionKey.OP_WRITE);
          }
        }
      }

      // re-register interest in this channel
      DsSelector.addInterestOps(m_sk, SelectionKey.OP_READ);

      if (m_queuedOutBB != null) {
        // There are still bytes to write.  We must re-register for write interest
        // This can happen when we get a callback to read while we are waiting to write
        DsSelector.addInterestOps(m_sk, SelectionKey.OP_WRITE);
      }
    } catch (Exception exc) {
      if (cat.isEnabled(Level.WARN)) {
        String socketInfo = null;
        if (m_socket != null) socketInfo = m_socket.getSocketInfo();
        cat.warn(
            "Exception while accessing the socket channel in socket "
                + socketInfo
                + " : closing socket channel , Exception is ",
            exc);
      }

      try {
        closeSocket();
      } catch (Exception ioe) {
        if (cat.isEnabled(Level.INFO)) {
          cat.info("Exception while closing the channel:", ioe);
        }
      }
      notifyListeners(new DsConnectionClosedEvent(this));
    }
  }

  ////////////////////////////////
  // END DsUnitOfWork Interface //
  ////////////////////////////////

  /**
   * Sets the transport layer for one time initial setup.
   *
   * @param transportLayer the transport layer
   */
  static void setTransportLayer(DsSipTransportLayer transportLayer) {
    m_transportLayer = transportLayer;
  }

  /**
   * Turn the use of direct buffers on or off. They are on by default. See the java.nio.ByteBuffer
   * class for more information on why to choose direct buffers. If you set a large buffer size and
   * have many threads doing I/O, you will need to set this to </code>false</code>.
   *
   * @param flag set to <code>true</code> to enable direct buffers
   */
  public static void setUseDirectBuffers(boolean flag) {
    m_useDirectBuffers = flag;
  }
}

/** Thread-local initializer for the network buffer. */
final class NetByteBufferInitializer extends ThreadLocal {
  protected Object initialValue() {
    if (DsTcpNBConnection.m_useDirectBuffers) {
      return ByteBuffer.allocateDirect(
          DsConfigManager.getProperty(
              DsConfigManager.PROP_NON_BLOCKING_BUFFER_SIZE,
              DsConfigManager.PROP_NON_BLOCKING_BUFFER_SIZE_DEFAULT));
    } else {
      return ByteBuffer.allocate(
          DsConfigManager.getProperty(
              DsConfigManager.PROP_NON_BLOCKING_BUFFER_SIZE,
              DsConfigManager.PROP_NON_BLOCKING_BUFFER_SIZE_DEFAULT));
    }
  }
}

/**
 * The default message bytes factory. This is the traditional behvior when SIP messages are recived,
 * and passed into the transaction manager, where they are parsed and processed.
 */
final class DsSipMessageBytesFactory implements DsMessageBytesFactory {
  public DsSipMessageBytesFactory() {}

  public DsMessageBytes createMessageBytes(byte[] bytes, DsBindingInfo binfo) {
    return new DsSipMessageBytes(bytes, binfo);
  }
}
