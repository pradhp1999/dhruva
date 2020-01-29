// Copyright (c) 2005-2010 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsUtil;

import com.cisco.dhruva.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.DsLibs.DsSipObject.DsSipTransportType;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import org.apache.logging.log4j.Level;

/**
 * This class uses JDK Datagram Socket from DsJDKDatagramSocket to listen to the ICMP (Internet
 * Control Messages Protocol) error messages while sending or receiving datagram packets to or from
 * the destination address.
 *
 * @since SIP User Agent Java v5.0
 */
// PRE CAFFEINE 2.0 - DsIcmpDatagramSocket.java is modified
// and DsIcmpDatagramSocketImpl.java is removed from
// dsua6_4_0_2 to dsua6_4_0_3 by dynamicsoft staff
public class DsIcmpDatagramSocket extends DsJDKDatagramSocket {
  /**
   * Constructs a datagram socket and binds it to any available port on the local host machine. This
   * datagram socket can listen to the ICMP error messages When an ICMP error message arrives it
   * throws a SocketException with the relevant error cause message.
   *
   * @throws SocketException if there is an error while creating the socket
   */
  public DsIcmpDatagramSocket() throws SocketException {
    this(null, 0, null);
  } // default constructor

  /**
   * Constructs a datagram socket and binds it to the specified port on the local host machine. This
   * datagram socket can listen to the ICMP error messages. When an ICMP error message arrives it
   * throws a SocketException with the relevant error cause message.
   *
   * @param localPort the port number to which this datagram socket is bound.
   * @throws SocketException if there is an error while creating the socket
   */
  public DsIcmpDatagramSocket(int localPort) throws SocketException {
    this(null, localPort, null);
  } // constructor

  /**
   * Constructs a datagram socket and binds it to the specified local port and the specified local
   * address. The specified port number <code>localPort</code> must be between 0 to 65535 inclusive.
   * This datagram socket can listen to the ICMP error messages. When an ICMP error message arrives
   * it throws a SocketException with the relevant error cause message.
   *
   * @param network the network to associate with this socket
   * @param localPort the port number to which this datagram socket should be bound.
   * @param localAddress the local address to which this datagram socket should be bound.
   * @throws SocketException if there is an error while creating the socket
   */
  public DsIcmpDatagramSocket(DsNetwork network, int localPort, InetAddress localAddress)
      throws SocketException {
    super(network, localPort, localAddress);
  } // constructor

  /**
   * Sends the datagram packet to the destination address specified in the address field of the
   * datagram packet. The first call to the send() method checks the destination address in the
   * datagram packet and connects to that address for transferring data packets to that address.
   *
   * @param packet The datagram packet to be send
   * @throws IOException if there is an error while sending the data
   * @throws java.lang.IllegalArgumentException if the packet address in subsequent calls to send()
   *     mismatches or if the packet is null
   */
  public synchronized void send(DatagramPacket packet)
      throws IOException, IllegalArgumentException {
    if (packet == null) {
      throw new IllegalArgumentException("The datagram packet is null");
    }
    InetAddress addr_ = packet.getAddress();
    ;
    int port_ = packet.getPort();

    if (!impl.isConnected()) // check again to avoid connecting again
    {
      if (addr_ == null) {
        throw new IllegalArgumentException("The packet address is null");
      }
      connect(addr_, port_);
    }

    if (DsLog4j.wireCat.isEnabled(Level.DEBUG)) {
      byte[] pBytes = packet.getData();
      if (pBytes.length > 0 && pBytes[0] == 1) {
        // this is a STUN response, print the debug string instead of the binary data
        // qfang - 12.01.06 - CSCsg93324 - use packet's remote address in logging
        DsLog4j.wireCat.log(
            Level.DEBUG,
            "Sending binary UDP packet on "
                + getLocalAddress().getHostAddress()
                + ":"
                + getLocalPort()
                + ", destination "
                + packet.getAddress().getHostAddress()
                + ":"
                + packet.getPort()
                + "\n"
                + DsString.toStunDebugString(pBytes));
      } else {
        // qfang - 12.01.06 - CSCsg93324 - use packet's remote address in logging
        DsLog4j.wireCat.log(
            Level.DEBUG,
            "Sending UDP packet on "
                + getLocalAddress().getHostAddress()
                + ":"
                + getLocalPort()
                + ", destination "
                + packet.getAddress().getHostAddress()
                + ":"
                + packet.getPort()
                + "\n"
                + DsSipMessage.maskAndWrapSIPMessageToSingleLineOutput(
                    DsByteString.newString(packet.getData(), 0, packet.getLength())));
      }
    } else if (DsLog4j.inoutCat.isEnabled(Level.DEBUG)) {
      DsLog4j.logInOutMessage(
          false,
          DsSipTransportType.UC_STR_UDP,
          packet.getAddress().getHostAddress(),
          packet.getPort(),
          getLocalAddress().getHostAddress(),
          getLocalPort(),
          packet.getData());
    }

    impl.send(packet);
  }

  /**
   * Receives the incoming Datagram Packet on this Datagram Socket and set the information (data,
   * data length, remote address, remote port) in the specified datagram packet. This method blocks
   * until the datagram packet is received.
   *
   * @throws SocketException if there is an error while receiving the datagram Packet or if the
   *     socket is already closed
   */
  public void receive(DatagramPacket packet) throws IOException {
    impl.receive(packet);
  }

  /** Closes the socket after calling disconnect() and releases the resources. */
  public void close() throws IOException {
    if (impl.isConnected()) {
      impl.disconnect();
    }
    super.close();
  }

  /*    public static void main(String[] args)
      {
          System.out.println("Check if current DsDatagramSocketFactory supports ICMP: "+
                             new String(DsDatagramSocketFactory.supportsICMP()?"true":"false"));
          DsIcmpDatagramSocket dsSocket = null;

          try
          {
              InetAddress localAddress = InetAddress.getByName("127.0.0.1");
              boolean socketServer = args[0].equals("server");
              if(socketServer)
              {
                  System.out.println("Running Server...");
                  dsSocket = new DsIcmpDatagramSocket(9700);
              }
              else
              {
                  System.out.println("Running Client with send string \"" + args[0] + "\".");
                  dsSocket = new DsIcmpDatagramSocket();
              }

              System.out.println("Receive Buffer = " + dsSocket.getReceiveBufferSize());
              System.out.println("Send Buffer = " + dsSocket.getSendBufferSize());
              dsSocket.setSendBufferSize(1024);
              dsSocket.setReceiveBufferSize(1024);
              System.out.println("Receive Buffer = " + dsSocket.getReceiveBufferSize());
              System.out.println("Send Buffer = " + dsSocket.getSendBufferSize());
              System.out.println("Socket = \n" + dsSocket.toString());

              DatagramPacket packet = null;
              int i = 2;
              if(socketServer)
              {
                  while (i-- > 0)
                  {
                      packet = new DatagramPacket(new byte[256],256);
                      System.out.println("Receiving...");
                      dsSocket.receive(packet);
                      System.out.println("Received packet:" +
                                         "\n\t Data: " + DsByteString.newString(packet.getData()));
                  }
              }
              else
              {
                  while ( i-->0)
                  {
                      packet = new DatagramPacket(args[0].getBytes(),args[0].length(),localAddress, 9700);
                      System.out.println("Sending....");
                      dsSocket.send(packet);
                      System.out.println("Sent....");
                  }
              }
          }
          catch(Exception exc)
          {
              System.out.println("Exception caught during socket operatons: " +
                                 exc.getMessage());
              exc.printStackTrace();
          }
          finally
          {
              try
              {
                  dsSocket.close();
                  dsSocket = null;
              }
              catch(Exception exc)
              {
                  System.out.println("Exception caught upon DsIcmpDatagramSocket close.");
                  exc.printStackTrace();
              }
          }
      }
  */
} // Ends class
