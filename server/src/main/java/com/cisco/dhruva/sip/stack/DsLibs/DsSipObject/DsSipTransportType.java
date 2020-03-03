// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.transport.Transport;

/** This class defines transport types as specified in RFC 3261. */
public class DsSipTransportType {
  /*
   * The constants which indicate the Transport type
   */

  /** Represents the class type as Integer.TYPE. */
  public static final Class TYPE = Integer.TYPE;

  /** Integer constant for the no transport type. */
  public static final int NONE = 0;
  /** Integer constant for the UDP transport type. */
  public static final int UDP = 1;
  /** Integer constant for the TCP transport type. */
  public static final int TCP = 2;
  /** Integer constant for the MULTICAST transport type. */
  public static final int MULTICAST = 3;
  /** Integer constant for the TLS transport type. */
  public static final int TLS = 4;
  /** Integer constant for the SCTP transport type. */
  public static final int SCTP = 5;
  /** Integer constant for the array size that holds transport types. */
  public static final int ARRAY_SIZE = 6;

  /** Byte mask constant for the UDP transport type. */
  public static final byte UDP_MASK = 1;
  /** Byte mask constant for the TCP transport type. */
  public static final byte TCP_MASK = 2;
  /** Byte mask constant for the MULTICAST transport type. */
  public static final byte MULTICAST_MASK = 4;
  /** Byte mask constant for the TLS transport type. */
  public static final byte TLS_MASK = 8;
  /** Byte mask constant for the SCTP transport type. */
  public static final byte SCTP_MASK = 16;

  /** An integer array that holds Integer values for various transport types. */
  public static final Integer TRANSPORT_ARRAY[];

  /** Lower case string representation of no transport type. */
  public static final String STR_NONE = "none";
  /** Lower case string representation of UDP transport type. */
  public static final String STR_UDP = "udp";
  /** Lower case string representation of TCP transport type. */
  public static final String STR_TCP = "tcp";
  /** Lower case string representation of MULTICAST transport type. */
  public static final String STR_MULTICAST = "multicast";
  /** Lower case string representation of TLS transport type. */
  public static final String STR_TLS = "tls";
  /** Lower case string representation of SCTP transport type. */
  public static final String STR_SCTP = "sctp";

  /** Upper case string representation of no transport type. */
  public static final String UC_STR_NONE = "NONE";
  /** Upper case string representation of UDP transport type. */
  public static final String UC_STR_UDP = "UDP";
  /** Upper case string representation of TCP transport type. */
  public static final String UC_STR_TCP = "TCP";
  /** Upper case string representation of MULTICAST transport type. */
  public static final String UC_STR_MULTICAST = "MULTICAST";
  /** Upper case string representation of TLS transport type. */
  public static final String UC_STR_TLS = "TLS";
  /** Upper case string representation of SCTP transport type. */
  public static final String UC_STR_SCTP = "SCTP";

  /** Lower case byte string representation of no transport type. */
  public static final DsByteString BS_NONE = new DsByteString(STR_NONE);
  /** Lower case byte string representation of UDP transport type. */
  public static final DsByteString BS_UDP = new DsByteString(STR_UDP);
  /** Lower case byte string representation of TCP transport type. */
  public static final DsByteString BS_TCP = new DsByteString(STR_TCP);
  /** Lower case byte string representation of MULTICAST transport type. */
  public static final DsByteString BS_MULTICAST = new DsByteString(STR_MULTICAST);
  /** Lower case byte string representation of TLS transport type. */
  public static final DsByteString BS_TLS = new DsByteString(STR_TLS);
  /** Lower case byte string representation of SCTP transport type. */
  public static final DsByteString BS_SCTP = new DsByteString(STR_SCTP);

  /** Upper case byte string representation of no transport type. */
  public static final DsByteString UC_BS_NONE = new DsByteString(UC_STR_NONE);
  /** Upper case byte string representation of UDP transport type. */
  public static final DsByteString UC_BS_UDP = new DsByteString(UC_STR_UDP);
  /** Upper case byte string representation of TCP transport type. */
  public static final DsByteString UC_BS_TCP = new DsByteString(UC_STR_TCP);
  /** Upper case byte string representation of MULTICAST transport type. */
  public static final DsByteString UC_BS_MULTICAST = new DsByteString(UC_STR_MULTICAST);
  /** Upper case byte string representation of TLS transport type. */
  public static final DsByteString UC_BS_TLS = new DsByteString(UC_STR_TLS);
  /** Upper case byte string representation of SCTP transport type. */
  public static final DsByteString UC_BS_SCTP = new DsByteString(UC_STR_SCTP);

  /** Holds reference to an instance of DsSipTransportType representing NONE transport. */
  public static final DsSipTransportType T_NONE;
  /** Holds reference to an instance of DsSipTransportType representing UDP transport. */
  public static final DsSipTransportType T_UDP;
  /** Holds reference to an instance of DsSipTransportType representing MULTICAST transport. */
  public static final DsSipTransportType T_MULTICAST;
  /** Holds reference to an instance of DsSipTransportType representing TCP transport. */
  public static final DsSipTransportType T_TCP;
  /** Holds reference to an instance of DsSipTransportType representing TLS transport. */
  public static final DsSipTransportType T_TLS;
  /** Holds reference to an instance of DsSipTransportType representing SCTP transport. */
  public static final DsSipTransportType T_SCTP;

  static {
    T_NONE = new DsSipTransportType(STR_NONE, NONE, 5060, false);
    T_UDP = new DsSipTransportType(STR_UDP, UDP, 5060, false);
    T_MULTICAST = new DsSipTransportType(STR_MULTICAST, MULTICAST, 5060, false);

    T_TCP = new DsSipTransportType(STR_TCP, TCP, 5060, true);
    T_TLS = new DsSipTransportType(STR_TLS, TLS, 5061, true);
    T_SCTP = new DsSipTransportType(STR_SCTP, SCTP, 5060, true);

    TRANSPORT_ARRAY = new Integer[ARRAY_SIZE];
    TRANSPORT_ARRAY[NONE] = new Integer(NONE);
    TRANSPORT_ARRAY[UDP] = new Integer(UDP);
    TRANSPORT_ARRAY[TCP] = new Integer(TCP);
    TRANSPORT_ARRAY[MULTICAST] = new Integer(MULTICAST);
    TRANSPORT_ARRAY[TLS] = new Integer(TLS);
    TRANSPORT_ARRAY[SCTP] = new Integer(SCTP);
  }

  /** The integer representation of this transport type. */
  private int m_intRep;
  /** The default port for this transport type. */
  private int m_defaultPort;
  /** The string representation of this transport type. */
  private String m_stringRep;
  /** The string representation of this transport type, in upper case. */
  private String m_stringRepUC;
  /** <code>true</code> if this transport type is reliable. */
  private boolean m_reliable;

  /**
   * Internal construction only.
   *
   * @param string_rep the string representation of this transport type.
   * @param int_rep the integer representation of this transport type.
   * @param def_port the default port for this transport type.
   * @param reliable <code>true</code> if this transport type is reliable.
   */
  private DsSipTransportType(String string_rep, int int_rep, int def_port, boolean reliable) {
    m_intRep = int_rep;
    m_defaultPort = def_port;
    m_stringRep = string_rep;
    m_reliable = reliable;
    m_stringRepUC = string_rep.toUpperCase();
  }

  /**
   * Returns the default port number as per the transport.
   *
   * @return the transport's default port.
   */
  public int getDefaultPort() {
    return m_defaultPort;
  }

  /**
   * Returns the integer representation of this transport.
   *
   * @return the integer representation of the transport.
   */
  public int getAsInt() {
    return m_intRep;
  }

  /**
   * Returns the lower case string representation of the transport.
   *
   * @return the lower case string representation of the transport.
   */
  public String toString() {
    return m_stringRep;
  }

  /**
   * Returns the upper case string representation of the transport.
   *
   * @return the upper case string representation of the transport.
   */
  public String toUCString() {
    return m_stringRepUC;
  }

  /**
   * Tells whether this transport type is reliable or unreliable.
   *
   * @return <code>true</code> if the transport is reliable, otherwise returns <code>false</code>.
   */
  public boolean isReliable() {
    return m_reliable;
  }

  /**
   * Returns the interned transport type object for the specified transport type.
   *
   * @return the transport type for the supplied string representation.
   * @param type the string representation.
   */
  public static DsSipTransportType intern(String type) {
    if (type.equalsIgnoreCase(UC_STR_UDP)) {
      return T_UDP;
    } else if (type.equalsIgnoreCase(UC_STR_TCP)) {
      return T_TCP;
    } else if (type.equalsIgnoreCase(UC_STR_TLS)) {
      return T_TLS;
    } else if (type.equalsIgnoreCase(UC_STR_SCTP)) {
      return T_SCTP;
    } else if (type.equalsIgnoreCase(UC_STR_MULTICAST)) {
      return T_MULTICAST;
    } else if (type.equalsIgnoreCase(UC_STR_NONE)) {
      return T_NONE;
    } else // default is UDP
    {
      return T_UDP;
    }
  }

  /**
   * Returns the interned transport type object for the specified transport type.
   *
   * @return the transport type for the supplied integer representation.
   * @param type the integer representation.
   */
  public static DsSipTransportType intern(int type) {
    switch (type) {
      case NONE:
        return T_NONE;
      case UDP:
        return T_UDP;
      case TCP:
        return T_TCP;
      case MULTICAST:
        return T_MULTICAST;
      case TLS:
        return T_TLS;
      case SCTP:
        return T_SCTP;
      default:
        return T_UDP;
    }
  }

  /**
   * Returns the interned transport type object for the specified transport type.
   *
   * @return the transport type for the supplied integer representation.
   * @param type the integer representation.
   */
  public static DsSipTransportType intern(Transport type) {
    switch (type) {
      case NONE:
        return T_NONE;
      case UDP:
        return T_UDP;
      case TCP:
        return T_TCP;
      case TLS:
        return T_TLS;
      default:
        return T_UDP;
    }
  }

  /**
   * Returns the interned transport type object for the transport type represented by the specified
   * buffer.
   *
   * @param buffer byte array that contains the bytes that constitute the transport name.
   * @param offset the offset in the buffer where from thetranport name starts
   * @param count the number of bytes in the buffer beginning from the offset that constitute the
   *     transport name.
   * @return the interned transport type.
   */
  public static DsSipTransportType intern(byte[] buffer, int offset, int count) {
    if (BS_UDP.equalsIgnoreCase(buffer, offset, count)) {
      return T_UDP;
    } else if (BS_TCP.equalsIgnoreCase(buffer, offset, count)) {
      return T_TCP;
    } else if (BS_TLS.equalsIgnoreCase(buffer, offset, count)) {
      return T_TLS;
    } else if (BS_SCTP.equalsIgnoreCase(buffer, offset, count)) {
      return T_SCTP;
    } else if (BS_MULTICAST.equalsIgnoreCase(buffer, offset, count)) {
      return T_MULTICAST;
    } else if (BS_NONE.equalsIgnoreCase(buffer, offset, count)) {
      return T_NONE;
    } else // default is UDP
    {
      return T_UDP;
    }
  }

  /**
   * Returns the interned transport type object for the transport type represented by the specified
   * byte string.
   *
   * @param type the byte string containing the name of the transport.
   * @return the interned transport type.
   */
  public static DsSipTransportType intern(DsByteString type) {
    if (type.equalsIgnoreCase(UC_BS_UDP)) {
      return T_UDP;
    } else if (type.equalsIgnoreCase(UC_BS_TCP)) {
      return T_TCP;
    } else if (type.equalsIgnoreCase(UC_BS_TLS)) {
      return T_TLS;
    } else if (type.equalsIgnoreCase(UC_BS_SCTP)) {
      return T_SCTP;
    } else if (type.equalsIgnoreCase(UC_BS_MULTICAST)) {
      return T_MULTICAST;
    } else if (type.equalsIgnoreCase(UC_BS_NONE)) {
      return T_NONE;
    } else // default is UDP
    {
      return T_UDP;
    }
  }

  /**
   * Retrieves the transport type as a string.
   *
   * @param type the Transport type.
   * @return the Transport as a String.
   */
  public static String getTypeAsString(int type) {
    switch (type) {
      case NONE:
        return (STR_NONE);
      case UDP:
        return (STR_UDP);
      case TCP:
        return (STR_TCP);
      case MULTICAST:
        return (STR_MULTICAST);
      case TLS:
        return (STR_TLS);
      case SCTP:
        return (STR_SCTP);
      default:
        return (STR_UDP);
    }
  }

  /**
   * Retrieves the transport type as an integer.
   *
   * @param type the Transport as a String.
   * @return the Transport type as an integer.
   */
  public static int getTypeAsInt(String type) {
    // optimmize for UDP/TCP/TLS
    // case insensitive comparisons are slow.  This is about 12x as fast as the old one.
    if (type.length() == 3) {
      char ch = type.charAt(0);
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

    // these are the rare cases, if we get here, just start comparing
    if (type.equalsIgnoreCase(UC_STR_NONE)) {
      return NONE;
    }

    if (type.equalsIgnoreCase(UC_STR_MULTICAST)) {
      return MULTICAST;
    }

    if (type.equalsIgnoreCase(UC_STR_SCTP)) {
      return SCTP;
    }

    // default is UDP
    return UDP;
  }

  /**
   * Retrieves the transport type as an upper case string.
   *
   * @param type the Transport type.
   * @return the Transport as an upper case string.
   */
  public static String getTypeAsUCString(int type) {
    switch (type) {
      case NONE:
        return (UC_STR_NONE);
      case UDP:
        return (UC_STR_UDP);
      case TCP:
        return (UC_STR_TCP);
      case MULTICAST:
        return (UC_STR_MULTICAST);
      case TLS:
        return (UC_STR_TLS);
      case SCTP:
        return (UC_STR_SCTP);
      default:
        return (UC_STR_UDP);
    }
  }

  /**
   * Retrieves the transport type as a byte string.
   *
   * @param type the Transport type.
   * @return the Transport as a Byte String.
   */
  public static DsByteString getTypeAsByteString(int type) {
    switch (type) {
      case NONE:
        return (BS_NONE);
      case UDP:
        return (BS_UDP);
      case TCP:
        return (BS_TCP);
      case MULTICAST:
        return (BS_MULTICAST);
      case TLS:
        return (BS_TLS);
      case SCTP:
        return (BS_SCTP);
      default:
        return (BS_UDP);
    }
  }

  /**
   * Retrieves the transport type as an integer.
   *
   * @param type the Transport as a Byte String.
   * @return the Transport type as an integer.
   */
  public static int getTypeAsInt(DsByteString type) {
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

    // these are the rare cases, if we get here, just start comparing
    if (type.equalsIgnoreCase(UC_BS_NONE)) {
      return NONE;
    }

    if (type.equalsIgnoreCase(UC_BS_MULTICAST)) {
      return MULTICAST;
    }

    if (type.equalsIgnoreCase(UC_BS_SCTP)) {
      return SCTP;
    }

    // default is UDP
    return UDP;
  }

  /**
   * Retrieves the transport type as an upper case Byte string.
   *
   * @param type the Transport type.
   * @return the Transport as an upper case Byte string.
   */
  public static DsByteString getTypeAsUCByteString(int type) {
    switch (type) {
      case NONE:
        return (UC_BS_NONE);
      case UDP:
        return (UC_BS_UDP);
      case TCP:
        return (UC_BS_TCP);
      case MULTICAST:
        return (UC_BS_MULTICAST);
      case TLS:
        return (UC_BS_TLS);
      case SCTP:
        return (UC_BS_SCTP);
      default:
        return (UC_BS_UDP);
    }
  }

  /**
   * Retrieves the transport type as a lower case DsByteString.
   *
   * @param buffer the transport byte array.
   * @param offset the offset in the byte array where from the transport starts.
   * @param count the number of bytes in the transport type.
   * @return the Transport type as a lower case DsByteString.
   */
  public static DsByteString toByteString(byte[] buffer, int offset, int count) {
    // optimmize for UDP/TCP/TLS
    // case insensitive comparisons are slow.  This is about 12x as fast as the old one.
    if (count == 3) {
      byte ch = buffer[offset];
      if (ch == 'U' || ch == 'u') // UDP
      {
        ch = buffer[offset + 1];
        if (ch == 'D' || ch == 'd') {
          ch = buffer[offset + 2];
          if (ch == 'P' || ch == 'p') {
            return BS_UDP;
          }
        }
      } else if (ch == 'T' || ch == 't') // TCP or TLS
      {
        ch = buffer[offset + 1];
        if (ch == 'C' || ch == 'c') // TCP
        {
          ch = buffer[offset + 2];
          if (ch == 'P' || ch == 'p') {
            return BS_TCP;
          }
        } else if (ch == 'L' || ch == 'l') // TLS
        {
          ch = buffer[offset + 2];
          if (ch == 'S' || ch == 's') {
            return BS_TLS;
          }
        }
      }
    }

    // these are the rare cases, if we get here, just start comparing
    if (BS_MULTICAST.equalsIgnoreCase(buffer, offset, count)) {
      return BS_MULTICAST;
    } else if (BS_SCTP.equalsIgnoreCase(buffer, offset, count)) {
      return BS_SCTP;
    } else if (BS_NONE.equalsIgnoreCase(buffer, offset, count)) {
      return BS_NONE;
    }
    return new DsByteString(buffer, offset, count);
  }

  /**
   * Retrieves the transport type as an Upper Case DsByteString.
   *
   * @param buffer the transport byte array.
   * @param offset the offset in the byte array where from the transport starts.
   * @param count the number of bytes in the transport type.
   * @return the Transport type as an Upper Case DsByteString.
   */
  public static DsByteString toUCByteString(byte[] buffer, int offset, int count) {
    // optimmize for UDP/TCP/TLS
    // case insensitive comparisons are slow.  This is about 12x as fast as the old one.
    if (count == 3) {
      byte ch = buffer[offset];
      if (ch == 'U' || ch == 'u') // UDP
      {
        ch = buffer[offset + 1];
        if (ch == 'D' || ch == 'd') {
          ch = buffer[offset + 2];
          if (ch == 'P' || ch == 'p') {
            return UC_BS_UDP;
          }
        }
      } else if (ch == 'T' || ch == 't') // TCP or TLS
      {
        ch = buffer[offset + 1];
        if (ch == 'C' || ch == 'c') // TCP
        {
          ch = buffer[offset + 2];
          if (ch == 'P' || ch == 'p') {
            return UC_BS_TCP;
          }
        } else if (ch == 'L' || ch == 'l') // TLS
        {
          ch = buffer[offset + 2];
          if (ch == 'S' || ch == 's') {
            return UC_BS_TLS;
          }
        }
      }
    }

    // these are the rare cases, if we get here, just start comparing
    if (UC_BS_MULTICAST.equalsIgnoreCase(buffer, offset, count)) {
      return UC_BS_MULTICAST;
    } else if (UC_BS_SCTP.equalsIgnoreCase(buffer, offset, count)) {
      return UC_BS_SCTP;
    } else if (UC_BS_NONE.equalsIgnoreCase(buffer, offset, count)) {
      return UC_BS_NONE;
    }
    return new DsByteString(buffer, offset, count);
  }

  //     public final static void main(String args[])
  //     {
  //         DsSipTransportType t = T_UDP;
  //         System.out.println();
  //         System.out.println("--udp--");
  //         System.out.println();
  //
  //         System.out.println("int:      " + t.getAsInt());
  //         System.out.println("lcstring: " + t);
  //         System.out.println("ucstring: " + t.toUCString());
  //         System.out.println("reliable: " + t.isReliable());
  //
  //         t = T_TLS;
  //         System.out.println();
  //         System.out.println("--tls--");
  //         System.out.println();
  //
  //         System.out.println("int:      " + t.getAsInt());
  //         System.out.println("lcstring: " + t);
  //         System.out.println("ucstring: " + t.toUCString());
  //         System.out.println("reliable: " + t.isReliable());
  //
  //         t = T_TCP;
  //         System.out.println();
  //         System.out.println("--tcp--");
  //         System.out.println();
  //
  //         System.out.println("int:      " + t.getAsInt());
  //         System.out.println("lcstring: " + t);
  //         System.out.println("ucstring: " + t.toUCString());
  //         System.out.println("reliable: " + t.isReliable());
  //
  //         t = T_SCTP;
  //         System.out.println();
  //         System.out.println("--sctp--");
  //         System.out.println();
  //
  //         System.out.println("int:      " + t.getAsInt());
  //         System.out.println("lcstring: " + t);
  //         System.out.println("ucstring: " + t.toUCString());
  //         System.out.println("reliable: " + t.isReliable());
  //
  //         t = T_MULTICAST;
  //         System.out.println();
  //         System.out.println("--multicast--");
  //         System.out.println();
  //
  //         System.out.println("int:      " + t.getAsInt());
  //         System.out.println("lcstring: " + t);
  //         System.out.println("ucstring: " + t.toUCString());
  //         System.out.println("reliable: " + t.isReliable());
  //
  //         t = T_NONE;
  //         System.out.println();
  //         System.out.println("--none--");
  //         System.out.println();
  //
  //         System.out.println("int:      " + t.getAsInt());
  //         System.out.println("lcstring: " + t);
  //         System.out.println("ucstring: " + t.toUCString());
  //         System.out.println("reliable: " + t.isReliable());
  //
  //     }
}
