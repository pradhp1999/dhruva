package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Hashtable;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import javax.net.ssl.SSLEngine;

public interface NetObjectProviderInterface {

  // InetAddress
  public InetAddress getByName(String host) throws UnknownHostException;

  public InetAddress getLocalHost() throws UnknownHostException;

  // InetSocketAddress - cannot mock methods as they are final
  public InetSocketAddress getInetSocketAddress(String hostname, int port);

  public InetSocketAddress getInetSocketAddress(InetAddress hostname, int port);

  // Socket
  public Socket getSocket();

  // DsSSLSocket
  public DsSSLSocket getSocket(
      Socket socket, String host, int port, boolean autoClose, DsSSLContext context)
      throws DsSSLException, IOException;

  // DatagramSocket
  public DatagramSocket getDatagramSocket(int port, InetAddress laddr) throws SocketException;

  //
  public DatagramPacket getDatagramPacket(byte[] buf, int length, InetAddress address, int port);

  //
  public InitialDirContext getJndiDnsInitialDirContext(Hashtable env) throws NamingException;

  //
  public SocketChannel getSocketChannel() throws IOException;

  public ServerSocketChannel getServerSocketChannel() throws IOException;

  public InputStream getInputStream() throws IOException;

  public ByteBuffer getAppSendBuffer();

  public SSLEngine getSSLEngine(DsSSLContext context);
}
