// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

/**
 * This class represents the Content-Version header. It provides methods to build, access, modify,
 * serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Content-Version  =  ( "Content-Version" | "l" ) ":" 1*DIGIT
 * </pre> </code>
 */
public final class DsSipContentVersionHeader extends DsSipIntegerHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_CONTENT_VERSION;
  /** Header ID. */
  public static final byte sID = CONTENT_VERSION;
  /** Compact header token. */
  public static final DsByteString sCompactToken = sToken;

  /** Default constructor. */
  public DsSipContentVersionHeader() {
    super();
  }

  /**
   * Constructs this header with the specified content version value.
   *
   * @param value the content version value.
   */
  public DsSipContentVersionHeader(int value) {
    super(value);
  }

  /**
   * Returns the token which is the name of the header.
   *
   * @return the token value.
   */
  public DsByteString getToken() {
    return sToken;
  }

  /**
   * Returns the token which is the compact name of the header.
   *
   * @return the compact token name.
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
    return BS_CONTENT_VERSION_TOKEN;
  }

  /**
   * Sets the content version value for this header.
   *
   * @param version the content version value for this header.
   */
  public void setVersion(int version) {
    setIntegerValue(version);
  }

  /**
   * Retrieves the content version value for this header.
   *
   * @return the content version value for this header.
   */
  public int getVersion() {
    return m_iValue;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return CONTENT_VERSION;
  }

  //    public static void main(String[] args)
  //    {
  //        try
  //        {
  //            DsSipContentVersionHeader header = new DsSipContentVersionHeader(1);
  //            System.out.println();
  //            System.out.println("<<<<<<<<<<<<<<<<< HEADER >>>>>>>>>>>>>>>>>>>>");
  //            System.out.println();
  //            header.write(System.out);
  //            System.out.println();
  //            System.out.println();
  //            System.out.println("<<<<<<<<<<<<<<<<< CLONE >>>>>>>>>>>>>>>>>>>>");
  //            System.out.println();
  //                    DsSipContentVersionHeader clone = (DsSipContentVersionHeader)
  // header.clone();
  //                    clone.write(System.out);
  //            System.out.println();
  //            System.out.println();
  //            System.out.println("<<<<<<<<<<<<<<<<< (HEADER == CLONE) = "
  //                                                    + header.equals(clone)
  //                                                    +" >>>>>>>>>>>>>>>>>>>>");
  //            System.out.println();
  //            System.out.println();
  //            System.out.println("<<<<<<<<<<<<<<<<< (CLONE == HEADER) = "
  //                                                    + clone.equals(header)
  //                                                    +" >>>>>>>>>>>>>>>>>>>>");
  //            System.out.println();
  //            System.out.println("Version = " + header.getVersion());
  //            System.out.println();
  //            System.out.println("Setting local");
  //            header.setVersion(2);
  //            System.out.println("Version = " + header.getVersion());
  //            System.out.println();
  //            System.out.println();
  //            System.out.println("clone Version = " + clone.getVersion());
  //            System.out.println();
  //            System.out.println("Setting local");
  //            clone.setVersion(2);
  //            System.out.println("clone Version = " + clone.getVersion());
  //            System.out.println();
  //        }
  //        catch(Exception e)
  //        {
  //            e.printStackTrace();
  //        }
  //    }// Ends main()
} // Ends class DsSipContentVersionHeader
