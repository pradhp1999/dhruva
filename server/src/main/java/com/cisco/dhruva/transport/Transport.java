/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.netty.TLSConnection;
import com.cisco.dhruva.transport.netty.UDPConnection;
import com.cisco.dhruva.transport.netty.hanlder.AbstractChannelHandler;
import io.netty.channel.Channel;
import java.util.Arrays;
import java.util.Optional;

public enum Transport {
  NONE(0) {
    @Override
    public boolean isReliable() {
      return false;
    }
  },
  UDP(1) {
    @Override
    public boolean isReliable() {
      return false;
    }

    @Override
    public Connection getConnection(
        Channel channel, DsNetwork networkConfig, AbstractChannelHandler channelHandler) {
      return new UDPConnection(channel, networkConfig, channelHandler);
    }
  },
  TCP(2) {
    @Override
    public boolean isReliable() {
      return true;
    }
  },
  MULTICAST(3) {
    @Override
    public boolean isReliable() {
      return false;
    }
  },
  TLS(4) {
    @Override
    public boolean isReliable() {
      return true;
    }

    @Override
    public Connection getConnection(
        Channel channel, DsNetwork networkConfig, AbstractChannelHandler channelHandler) {
      return new TLSConnection(channel, networkConfig, channelHandler);
    }
  },
  SCTP(5) {
    @Override
    public boolean isReliable() {
      return true;
    }
  };

  private int value;

  Transport(int transport) {
    this.value = transport;
  }

  public static Optional<Transport> valueOf(int value) {
    return Arrays.stream(values()).filter(transport -> transport.value == value).findFirst();
  }

  public int getValue() {
    return value;
  }

  public Connection getConnection(
      Channel channel, DsNetwork networkConfig, AbstractChannelHandler channelHandler)
      throws Exception {
    throw new Exception("Transport not supported");
  }

  /** Byte mask constant for the transport type. */
  public static final byte UDP_MASK = 1;

  public static final byte TCP_MASK = 2;
  public static final byte MULTICAST_MASK = 4;
  public static final byte TLS_MASK = 8;
  public static final byte SCTP_MASK = 16;

  /** Lower case string representation of transport type. */
  public static final String STR_NONE = "none";

  public static final String STR_UDP = "udp";
  public static final String STR_TCP = "tcp";
  public static final String STR_TLS = "tls";

  /** Upper case string representation of transport type. */
  public static final String UC_STR_NONE = "NONE";

  public static final String UC_STR_UDP = "UDP";
  public static final String UC_STR_TCP = "TCP";
  public static final String UC_STR_TLS = "TLS";

  /** Upper case byte string representation of transport type. */
  public static final DsByteString UC_BS_NONE = new DsByteString(UC_STR_NONE);

  public static final DsByteString UC_BS_UDP = new DsByteString(UC_STR_UDP);
  public static final DsByteString UC_BS_TCP = new DsByteString(UC_STR_TCP);
  public static final DsByteString UC_BS_TLS = new DsByteString(UC_STR_TLS);

  /** Lower case byte string representation of no transport type. */
  public static final DsByteString BS_NONE = new DsByteString(STR_NONE);

  public static final DsByteString BS_UDP = new DsByteString(STR_UDP);
  public static final DsByteString BS_TCP = new DsByteString(STR_TCP);
  public static final DsByteString BS_TLS = new DsByteString(STR_TLS);
  public static final String TRANSPORT = "transport";

  public abstract boolean isReliable();

  /**
   * Retrieves the transport type as an integer.
   *
   * @param type the Transport as a Byte String.
   * @return the Transport type as an integer.
   */
  public static Transport getTypeAsInt(DsByteString type) {
    // optimmize for UDP/TCP/TLS
    // case insensitive comparisons are slow.  This is about 12x as fast as the old one.
    if (type.length() == 3) {
      byte ch = type.charAt(0);
      if (ch == 'U' || ch == 'u') // UDP
      {
        ch = type.charAt(1);
        if (ch == 'D' || ch == 'd') {
          ch = type.charAt(2);
          if (ch == 'P' || ch == 'p') {
            return UDP;
          }
        }
      } else if (ch == 'T' || ch == 't') // TCP or TLS
      {
        ch = type.charAt(1);
        if (ch == 'C' || ch == 'c') // TCP
        {
          ch = type.charAt(2);
          if (ch == 'P' || ch == 'p') {
            return TCP;
          }
        } else if (ch == 'L' || ch == 'l') // TLS
        {
          ch = type.charAt(2);
          if (ch == 'S' || ch == 's') {
            return TLS;
          }
        }
      }
    }

    // default
    return UDP;
  }

  public static DsByteString getTypeAsByteString(Transport transport) {

    switch (transport) {
      case NONE:
        return (BS_NONE);
      case UDP:
        return (BS_UDP);
      case TCP:
        return (BS_TCP);
      case TLS:
        return (BS_TLS);
      default:
        return (BS_UDP);
    }
  }

  /**
   * Retrieves the transport type as an upper case Byte string.
   *
   * @param type the Transport type.
   * @return the Transport as an upper case Byte string.
   */
  public static DsByteString getTypeAsUCByteString(Transport type) {
    switch (type) {
      case NONE:
        return (UC_BS_NONE);
      case UDP:
        return (UC_BS_UDP);
      case TCP:
        return (UC_BS_TCP);
      case TLS:
        return (UC_BS_TLS);
      default:
        return (UC_BS_UDP);
    }
  }
}
