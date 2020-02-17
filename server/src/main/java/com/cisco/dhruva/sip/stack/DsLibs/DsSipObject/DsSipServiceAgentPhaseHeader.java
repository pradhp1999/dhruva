// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

/**
 * This class represents the Service-Agent-Phase header as specified internally by dynamicsoft. It
 * provides methods to build, access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Service-Agent-Phase =  "SE-Phase:" ("originating" | "terminating")
 * </pre> </code>
 */
public final class DsSipServiceAgentPhaseHeader extends DsSipStringHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_SERVICE_AGENT_PHASE;
  /** Header ID. */
  public static final byte sID = SERVICE_AGENT_PHASE;
  /** Compact header token. */
  public static final DsByteString sCompactToken = BS_SERVICE_AGENT_PHASE;

  /** The "originating" token. */
  private static final DsByteString ORIGINATING = new DsByteString("originating");
  /** The "terminating" token. */
  private static final DsByteString TERMINATING = new DsByteString("terminating");

  /** Default constructor. */
  public DsSipServiceAgentPhaseHeader() {
    super();
  }

  /**
   * Constructs this header with the specified value.<br>
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   */
  public DsSipServiceAgentPhaseHeader(byte[] value) {
    super(value);
  }

  /**
   * Constructs this header with the specified value.<br>
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   * @param offset the offset in the specified byte array, where from the value part, that needs to
   *     be parsed, starts.
   * @param count the total number of bytes, starting from the specified offset, that constitute the
   *     value part.
   */
  public DsSipServiceAgentPhaseHeader(byte[] value, int offset, int count) {
    super(value, offset, count);
  }

  /**
   * Constructs this header with the specified value.<br>
   * The specified byte string <code>value</code> should be the value part (data after the colon) of
   * this header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   */
  public DsSipServiceAgentPhaseHeader(DsByteString value) {
    super(value);
  }

  /**
   * Returns the token which is the complete name of the header.
   *
   * @return The complete token name
   */
  public DsByteString getToken() {
    return sToken;
  }

  /**
   * Returns the token which is the complete name of the header.
   *
   * @return The complete token name
   */
  public DsByteString getCompactToken() {
    return sCompactToken;
  }

  /**
   * Returns the header name plus ": " as a single byte string token. The header name will be in the
   * compact form if this header is set to be in compact form.
   *
   * @return the header name plus ": " as a single byte string token.
   */
  public final DsByteString getTokenC() {
    return BS_SERVICE_AGENT_PHASE_TOKEN;
  }

  /**
   * Checks if the service agent phase value is <b>originating</b>.
   *
   * @return <code>true</code> if the phase value is <b>originating</b>, <code>false</code>
   *     otherwise
   */
  public boolean isOriginating() {
    return (m_strValue != null) ? ORIGINATING.equalsIgnoreCase(m_strValue) : false;
  }

  /**
   * Checks if the service agent phase value is <b>terminating</b>.
   *
   * @return <code>true</code> if the phase value is <b>terminating</b>, <code>false</code>
   *     otherwise
   */
  public boolean isTerminating() {
    return (m_strValue != null) ? TERMINATING.equalsIgnoreCase(m_strValue) : false;
  }

  /** Sets the service agent phase value to <b>originating</b>. */
  public void setOriginating() {
    setValue(ORIGINATING);
  }

  /** Sets the service agent phase value to <b>terminating</b>. */
  public void setTerminating() {
    setValue(TERMINATING);
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return SERVICE_AGENT_PHASE;
  }
  //    public static void main(String[] args)
  //    {
  //        try
  //        {
  //            byte[] bytes = read();
  //            DsSipServiceAgentPhaseHeader header = new DsSipServiceAgentPhaseHeader(bytes);
  // System.out.println();
  // System.out.println("<<<<<<<<<<<<<<<<< HEADER >>>>>>>>>>>>>>>>>>>>");
  // System.out.println();
  //            header.write(System.out);
  // System.out.println();
  // System.out.println();
  // System.out.println("<<<<<<<<<<<<<<<<< CLONE >>>>>>>>>>>>>>>>>>>>");
  // System.out.println();
  //                    DsSipServiceAgentPhaseHeader clone = (DsSipServiceAgentPhaseHeader)
  // header.clone();
  //                    clone.write(System.out);
  // System.out.println();
  // System.out.println();
  // System.out.println("<<<<<<<<<<<<<<<<< (HEADER == CLONE) = "
  //                                        + header.equals(clone)
  //                                        +" >>>>>>>>>>>>>>>>>>>>");
  // System.out.println();
  // System.out.println();
  // System.out.println("<<<<<<<<<<<<<<<<< (CLONE == HEADER) = "
  //                                        + clone.equals(header)
  //                                        +" >>>>>>>>>>>>>>>>>>>>");
  // System.out.println();
  //        }
  //        catch(Exception e)
  //        {
  //            e.printStackTrace();
  //        }
  //    }// Ends main()
} // Ends class DsSipServiceAgentPhaseHeader
