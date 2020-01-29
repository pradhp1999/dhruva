// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsUtil;

import java.net.InetAddress;
import java.net.SocketException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

/** Factory the creates datagram sockets. */
public final class DsDatagramSocketFactory {
  /** The native library name. */
  static final String ICMPLIBNAME = "ds";
  /** The sigcomp library name. */
  static final String SIGCOMPLIBNAME = "dssigcomp";

  // CAFFEINE 2.0 - add supports of ICMP feature in solaris
  /** <code>true</code> if ICMP is enabled. */
  static boolean m_featureICMP;
  /** <code>true</code> if sigcomp is enabled. */
  static boolean m_featureSIGCOMP;

  /** <code>true</code> if the platform is Solaris. */
  static boolean m_solaris;

  static {
    Logger cat = DsLog4j.connectionCat;
    boolean loadAlways =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_LOAD_NATIVE, DsConfigManager.PROP_LOAD_NATIVE_DEFAULT);
    // boolean loadAlways  = true;
    m_solaris = System.getProperty("os.name").equalsIgnoreCase("SunOS");

    if (loadAlways || m_solaris) {
      m_featureICMP = true;
      m_featureSIGCOMP = true;

      // CAFFEINE 2.0 - add supports of ICMP feature in solaris
      try {
        System.loadLibrary(ICMPLIBNAME);
      } catch (SecurityException se) {
        if (cat.isEnabled(Level.WARN)) {
          cat.warn(
              "Couldn't initialize the native library \"lib"
                  + ICMPLIBNAME
                  + ".so for icmp (connected datagrams) \"\n"
                  + se.getMessage());
        }
        m_featureICMP = false;
      } catch (UnsatisfiedLinkError ule) {
        if (cat.isEnabled(Level.WARN)) {
          cat.warn(
              "Couldn't initialize the native library \"lib"
                  + ICMPLIBNAME
                  + ".so for icmp (connected datagrams) \"\n"
                  + ule.getMessage());
        }
        m_featureICMP = false;
      }

      try {
        System.loadLibrary(SIGCOMPLIBNAME);
      } catch (SecurityException se) {
        // use INFO here since the sigcomp stuff is for dynamicsoft use only
        if (cat.isEnabled(Level.INFO)) {
          cat.info(
              "Couldn't initialize the native library \"lib"
                  + SIGCOMPLIBNAME
                  + ".so for sigcomp datagrams \"\n"
                  + se.getMessage());
        }
        m_featureSIGCOMP = false;
      } catch (UnsatisfiedLinkError ule) {
        // use INFO here since the sigcomp stuff is for dynamicsoft use only
        if (cat.isEnabled(Level.INFO)) {
          cat.info(
              "Couldn't initialize the native library \"lib"
                  + SIGCOMPLIBNAME
                  + ".so for sigcomp datagrams \"\n"
                  + ule.getMessage());
        }
        m_featureSIGCOMP = false;
      }

      if (DsLog4j.connectionCat.isEnabled(Level.INFO)) {
        // CAFFEINE 2.0 - add supports of ICMP feature in solaris
        if (m_featureICMP) DsLog4j.connectionCat.info("lib" + ICMPLIBNAME + ".so loaded OK");
        if (m_featureSIGCOMP) DsLog4j.connectionCat.info("lib" + SIGCOMPLIBNAME + ".so loaded OK");
      }
    }
  }

  /**
   * Factory method that creates a datagram socket.
   *
   * @param network the network to associate with this socket
   * @param laddr the local address
   * @param lport the local port
   * @return the created datagram socket
   * @throws SocketException if there is a problem creating the requested socket.
   */
  public static DsDatagramSocket create(DsNetwork network, InetAddress laddr, int lport)
      throws SocketException {
    Logger cat = DsLog4j.connectionCat;

    int dgramType = network.getDatagramType();
    int compression = network.getCompressionType();

    if (cat.isEnabled(Level.INFO)) {
      cat.info("create datagram type is " + dgramType);
    }

    if (compression == DsNetwork.NET_COMP_SIGCOMP && !m_featureSIGCOMP) {
      if (cat.isEnabled(Level.WARN)) {
        cat.warn(
            "User requested sigcomp datagram, but sigcomp library "
                + "is not loaded/supported on this platform.  Using JDK "
                + "default datagram instead.");
      }
      dgramType = DsNetwork.DGRAM_DEFAULT;
      compression = DsNetwork.NET_COMP_NONE;
    }

    // CAFFEINE 2.0 - add supports of ICMP feature in solaris
    if (dgramType == DsNetwork.DGRAM_ICMP && !m_featureICMP) {
      if (cat.isEnabled(Level.WARN)) {
        cat.warn(
            "User requested icmp (connected) datagram, but icmp "
                + "library is not loaded/supported on this platform. "
                + "Using JDK default datagram instead.");
      }
      dgramType = DsNetwork.DGRAM_DEFAULT;
    }

    if (compression == DsNetwork.NET_COMP_SIGCOMP)
      return new DsSigcompDatagramSocket(network, lport, laddr);
    if (dgramType == DsNetwork.DGRAM_ICMP) return new DsIcmpDatagramSocket(network, lport, laddr);
    return new DsJDKDatagramSocket(network, lport, laddr);
  }

  /**
   * Tells if ICMP is supported.
   *
   * @return <code>true</code> if ICMP is supported.
   */
  // CAFFEINE 2.0 - add supports of ICMP feature in solaris
  public static boolean supportsICMP() {
    return m_featureICMP;
  }

  /**
   * Tells is sigcomp is supported.
   *
   * @return <code>true</code> if sigcomp is supported.
   */
  public static boolean supportsSIGCOMP() {
    return m_featureSIGCOMP;
  }
}
