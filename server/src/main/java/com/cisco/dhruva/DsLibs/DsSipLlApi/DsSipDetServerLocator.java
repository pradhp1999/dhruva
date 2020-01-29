// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipLlApi;

import com.cisco.dhruva.DsLibs.DsSipObject.*;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.security.*;
import java.util.*;
import org.apache.logging.log4j.Level;

/**
 * This class is a "deterministic" server locator. It is used when SRV RR lookup is desired, but the
 * selection of the server should be consistent between calls. The criteria for sorting the SRV
 * records does not follow the SRV spec, since a random variable is applied in that algorithm.
 * Rather, sorting is done by ordering first by priority and, within a given priority, a record is
 * selected using a factor computed by the messages CallID rather than a random number.
 */
public class DsSipDetServerLocator extends DsSipServerLocator {
  private int m_Factor; // = 0;
  private byte[] bytesToHash;

  private static final double MAX_INT = (double) (Integer.MAX_VALUE);
  private static ThreadLocal md5 = new MD5Initializer();

  /**
   * Constructs the deterministic server locator for the specified SIP message based on the Call-ID
   * header present in the message.
   *
   * @param message the message to use to create this object
   * @throws DsException if either Call-ID header could not be found in the specified request to
   *     calculate the hash or Message digest algorithm object couldn't be instantiated.
   */
  public DsSipDetServerLocator(DsSipMessage message) throws DsException {
    super();

    // just get the value, not the entire header (?)
    DsByteString bs = message.getCallId();

    if (bs == null) {
      throw new DsException("can't find CallId header to calculate hash");
    }

    bytesToHash = bs.toByteArray();
  }

  /**
   * Creates the hash code list String would.
   *
   * @param bytes the bytes to hash
   * @return the generated hash code
   */
  private int makeHashCode(byte[] bytes) {
    int hash = 0;
    for (int i = 0; i < bytes.length; i++) {
      hash = 31 * hash + Math.abs(bytes[i]);
    }
    return hash;
  }

  /*
   * javadoc inherited
   */
  protected int genRandom(int running_sum) {
    if (m_Factor == 0) // not initialized yet
    {
      MessageDigest local_md5 = getMD5();
      if (local_md5 != null) {
        m_Factor = makeHashCode(local_md5.digest(bytesToHash));
      } else {
        m_Factor = makeHashCode(bytesToHash);

        // we lost the ability to throw an exception - for now I am just going to use make hash code
        // without the MD5 hash - jsm
        // throw new DsException("can't create MessageDigest object (no such algorithm?)");
      }
    }

    return (int) (((double) Math.abs(m_Factor)) * ((double) running_sum) / MAX_INT);
  }

  /**
   * Gets the ThreadLocal MD5.
   *
   * @return the ThreadLocal MD5
   */
  private static MessageDigest getMD5() {
    return (MessageDigest) md5.get();
  }

  /** ThreadLocal MD5 hash object initializer. */
  static class MD5Initializer extends ThreadLocal {
    protected Object initialValue() {
      if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
        DsLog4j.connectionCat.log(Level.DEBUG, "ThreadLocal MD5 initializer called");
      }

      try {
        return MessageDigest.getInstance("MD5");
      } catch (NoSuchAlgorithmException e) {
        if (DsLog4j.connectionCat.isEnabled(Level.ERROR)) {
          DsLog4j.connectionCat.log(
              Level.ERROR, "Exception creating thread local instance of MD5", e);
        }
        return null;
      }
    }
  }
}
