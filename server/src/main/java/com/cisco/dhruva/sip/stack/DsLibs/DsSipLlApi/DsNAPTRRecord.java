// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import com.cisco.dhruva.transport.Transport;
import java.util.StringTokenizer;
import org.slf4j.event.Level;

/** The NAPTR RR with the following format: Order Preference Flags Service Regexp Replacement */
class DsNAPTRRecord {
  public static final String SIP_UDP = "SIP+D2U";
  public static final String SIP_TCP = "SIP+D2T";
  public static final String SIP_SCTP = "SIP+D2S";
  public static final String SIPS_TCP = "SIPS+D2T";

  /**
   * Creates and returns a NAPTR record object from the specified string. The specified string is
   * assumed to be in the format: [Domain TTL Class Type Order Preference Flags Service Regexp
   * Replacement] If there is an exception in extracting the various components from the specified
   * string, then null is returned.
   *
   * @param record the string value of the NAPTR record that needs to be parsed into a NAPTR record
   *     object.
   * @return the NAPTR record object constructed as per the specified string value
   */
  static DsNAPTRRecord getRecord(String record) {
    try {
      DsNAPTRRecord nrec = new DsNAPTRRecord(record);
      if (DsLog4j.resolvCat.isEnabled(Level.DEBUG)) {
        DsLog4j.resolvCat.log(Level.DEBUG, "Created DsNAPTRRecord for " + record + ":\n\t" + nrec);
      }
      return nrec;
    } catch (Exception exc) {
      if (DsLog4j.resolvCat.isEnabled(Level.DEBUG)) {
        DsLog4j.resolvCat.log(Level.DEBUG, "Exception during DsNAPTRRecord creation:\n" + exc);
      }
      return null;
    }
  }

  /**
   * Creates this NAPTR record object from the specified string. The specified string is assumed to
   * be in the format: [Domain TTL Class Type Order Preference Flags Service Regexp Replacement]
   *
   * @param record the string value of the NAPTR record that needs to be parsed into this NAPTR
   *     record object.
   * @throws Exception If there is an exception in extracting the various components from the
   *     specified string.
   */
  public DsNAPTRRecord(String record) throws Exception {
    if (DsLog4j.resolvCat.isEnabled(Level.DEBUG)) {
      DsLog4j.resolvCat.log(Level.DEBUG, "Creating DsNAPTRRecord for: " + record);
    }

    StringTokenizer tokens = new StringTokenizer(record, " ");
    // m_lOrder = Long.parseLong(tokens.nextToken()); // Order
    // m_lPreference = Long.parseLong(tokens.nextToken()); // Preference
    byte bytes[] = tokens.nextToken().getBytes(); // Order
    if (bytes.length != 1)
      throw new Exception("Order Field in NAPTR record differs in length from 1");
    m_lOrder = (long) bytes[0];

    bytes = tokens.nextToken().getBytes(); // Preference
    if (bytes.length != 1)
      throw new Exception("Preference Field in NAPTR record differs in length from 1");
    m_lOrder = (long) bytes[0];

    m_strFlags = unquote(tokens.nextToken()); // Flags
    m_strService = unquote(tokens.nextToken()); // Service
    tokens.nextToken(); // Empty Reg Expression
    m_strReplacement = unquote(tokens.nextToken()); // Replacement, ie. SRV query string
  }

  /**
   * Tells the transport type for the specified NAPTR service value. The returned transport type
   * short value is as per the transport types specified in the DsSipTransportType class. Here is
   * the returned value mappings: <br>
   *
   * <pre>
   * <b>Service              Transport Type</b>
   * SIP+D2U              DsSipTransportType.UDP
   * SIP+D2T              DsSipTransportType.TCP
   * SIPS+D2              DsSipTransportType.TLS
   * SIP+D2S              DsSipTransportType.SCTP
   * [ANY]                DsSipTransportType.NONE
   * <br>
   * </pre>
   */
  public static Transport serviceType(String service) {
    if (service.equalsIgnoreCase(SIP_UDP)) {
      return Transport.UDP;
    } else if (service.equalsIgnoreCase(SIP_TCP)) {
      return Transport.TCP;
    } else if (service.equalsIgnoreCase(SIPS_TCP)) {
      return Transport.TLS;
    } else if (service.equalsIgnoreCase(SIP_SCTP)) {
      return Transport.SCTP;
    } else {
      return Transport.NONE;
    }
  }

  /**
   * Returns the string representation of this record.
   *
   * @return the string representation of this record.
   */
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(m_lOrder);
    buffer.append(" ");
    buffer.append(m_lPreference);
    buffer.append(" ");
    buffer.append(m_strFlags);
    buffer.append(" ");
    buffer.append(m_strService);
    buffer.append(" ");
    buffer.append(m_strReplacement);
    return buffer.toString();
  }

  /** Returns the unquoted value of the specified string value. */
  public static String unquote(String value) {
    if (null != value) {
      value = value.trim();
      int len = value.length();
      if (len > 1) {
        if (value.charAt(0) == '"' && value.charAt(len - 1) == '"') {
          value = value.substring(1, len - 1);
        }
      }
    }
    return value;
  }

  long m_lOrder;
  long m_lPreference;
  String m_strFlags;
  String m_strService;
  String m_strReplacement;
}
