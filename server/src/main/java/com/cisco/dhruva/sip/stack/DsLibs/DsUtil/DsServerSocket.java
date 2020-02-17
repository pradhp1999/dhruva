// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import java.io.*;
import java.net.*;
import org.apache.logging.log4j.Level;

/** Wrapper for the Java server socket class. Allows the server socket to be turned on and off. */
public class DsServerSocket {
  /** The default backlog value. */
  private static final int DEFAULT_BACKLOG = 50;

  /** An object to lock on when changing state. */
  private Object m_state_lock = new Object();

  /** The underlying server socket. */
  protected ServerSocket m_socket; // = null;

  /** The exception thrown for state change exceptions. */
  private Exception m_state_change_exc; // = null;

  /** true if the socket is alive. */
  private boolean m_alive = true;

  /** true if we are in shutdown mode. */
  private boolean m_shutdown; // = false;

  /** The port that is being listened to. */
  protected int m_port = -1;

  /** The address being listenered to. */
  protected InetAddress m_addr; // = null;

  /** To provide the default initialization for the derived classes. */
  protected DsServerSocket() {
    m_socket = null;
    m_alive = true;
  }

  /**
   * Constructs the DsServerSocket with the specified underlying server socket <code>s</code>.
   *
   * @param s the underlying server socket
   */
  public DsServerSocket(ServerSocket s) {
    this.m_port = s.getLocalPort();
    this.m_addr = s.getInetAddress();
  }

  /**
   * Constructor which accepts the port number to listen to and bind address.
   *
   * @param port the local port number to listen
   * @param addr the local address to listen
   * @throws IOException thrown when there is an exception in socket I/O
   */
  public DsServerSocket(int port, InetAddress addr) throws IOException {
    this.m_port = port;
    this.m_addr = addr;
    init(port, addr);
  }

  /**
   * Constructor which accepts the port number to listen to.
   *
   * @param port the local port number to listen
   * @throws IOException thrown when there is an exception in socket I/O
   */
  public DsServerSocket(int port) throws IOException {
    this.m_port = port;
    this.m_addr = null;
    init(port, null);
  }

  /** Used internally to re-initialize the Java server socket. */
  private void init(int port, InetAddress addr) throws IOException {
    m_socket = new ServerSocket(port, DEFAULT_BACKLOG, addr);
    m_alive = true;
  }

  /**
   * Blocks on a port for requests.
   *
   * @return the socket which the server returns
   * @throws IOException thrown when there is an exception in socket I/O
   */
  public DsSocket accept() throws IOException {
    Socket sock = m_socket.accept();
    DsSocket client = new DsSocket(sock);
    return (client);
  }

  /**
   * Used to control whether or not new connections should be accepted. This method is implemented
   * by closing (if value == <code>false</code>) or re-creating the underlying Java server socket.
   *
   * @param value if set to 'true,' this server socket will accept new connections. If set to
   *     'false,' the server socket will not longer accept incoming connections.
   */
  public void setAlive(boolean value) {
    synchronized (m_state_lock) {
      // if no state change do nothing
      if (value == m_alive) {
        return;
      }
      m_alive = value;
      try {
        if (m_alive) {
          if (DsLog4j.socketCat.isEnabled(Level.DEBUG))
            DsLog4j.socketCat.log(
                Level.DEBUG, this + " setAlive: re-initializing underlying server socket");
          init(m_port, m_addr);
        } else {
          if (DsLog4j.socketCat.isEnabled(Level.DEBUG))
            DsLog4j.socketCat.log(
                Level.DEBUG, this + " setAlive: closing underlying server socket");
          m_socket.close();
        }
      }
      // if there is an exception either closing or creating the underlying server
      // socket, the listening thread (in block) should should exit and notify its listeners
      catch (IOException ioe) {
        m_alive = false;
        m_shutdown = true;
        m_state_change_exc = ioe;
        DsLog4j.socketCat.warn(
            this + " setAlive:  error closing or re-initializing socket, shutting down", ioe);
      }
      m_state_lock.notify();
    }
  }

  /**
   * Used to check if the server socket is alive.
   *
   * @return <code>true</code> if the server is listening <code>false</code> otherwise
   */
  public boolean isAlive() {
    synchronized (m_state_lock) {
      return m_alive;
    }
  }

  /**
   * Used to close the server socket.
   *
   * @throws IOException thrown when the socket cannot be closed
   */
  public void close() throws IOException {
    if (DsLog4j.socketCat.isEnabled(Level.DEBUG))
      DsLog4j.socketCat.log(
          Level.DEBUG, this + " setting m_shutdown <- true and calling setAlive(false)");
    synchronized (m_state_lock) {
      m_shutdown = true;
      if (m_alive) {
        setAlive(false);
      } else {
        m_state_lock.notify();
      }
    }
  }

  /**
   * Returns the local address of this server socket.
   *
   * @return the local address of this server socket.
   */
  public InetAddress getInetAddress() {
    return m_socket.getInetAddress();
  }

  /**
   * Returns the local port of this server socket.
   *
   * @return the local port of this server socket.
   */
  public int getLocalPort() {
    return m_socket.getLocalPort();
  }
}
