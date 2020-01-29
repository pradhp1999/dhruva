// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class represents the Subscription-State header as specified in RFC 3265. It provides methods
 * to build, access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Subscription-State     =  "Subscription-State" HCOLON substate-value *( SEMI subexp-params )
 * substate-value         =  "active" / "pending" / "terminated" / extension-substate
 * extension-substate     =  token
 * subexp-params          =  ("reason" EQUAL event-reason-value) / ("expires" EQUAL delta-seconds) /
 *                           ("retry-after" EQUAL delta-seconds) / generic-param
 * event-reason-value     =  "deactivated" / "probation" / "rejected" / "timeout" /
 *                           "giveup" / "noresource" / event-reason-extension
 * event-reason-extension =  token
 * </pre> </code>
 */
public final class DsSipSubscriptionStateHeader extends DsSipParametricHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_SUBSCRIPTION_STATE;
  /** Header ID. */
  public static final byte sID = SUBSCRIPTION_STATE;
  /** Compact header token. */
  public static final DsByteString sCompactToken = BS_SUBSCRIPTION_STATE;

  /** The state. */
  private DsByteString m_strState = null;

  /** Default constructor. */
  public DsSipSubscriptionStateHeader() {
    super();
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header
   * and constructs this header.<br>
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   * If there is an exception during parsing phase, it will set the invalid flag of this header and
   * retain the various components that it already parsed. One should check the valid flag before
   * retrieving the various components of this header.
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   * @throws DsSipParserException if there is an error while parsing the specified value into this
   *     header.
   * @throws DsSipParserListenerException if there is an error condition detected by this header as
   *     a Parser Listener, while parsing.
   */
  public DsSipSubscriptionStateHeader(byte[] value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value, 0, value.length);
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header
   * and constructs this header.<br>
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   * If there is an exception during parsing phase, it will set the invalid flag of this header and
   * retain the various components that it already parsed. One should check the valid flag before
   * retrieving the various components of this header.
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   * @param offset the offset in the specified byte array, where from the value part, that needs to
   *     be parsed, starts.
   * @param count the total number of bytes, starting from the specified offset, that constitute the
   *     value part.
   * @throws DsSipParserException if there is an error while parsing the specified value into this
   *     header.
   * @throws DsSipParserListenerException if there is an error condition detected by this header as
   *     a Parser Listener, while parsing.
   */
  public DsSipSubscriptionStateHeader(byte[] value, int offset, int count)
      throws DsSipParserException, DsSipParserListenerException {
    /*
     CAFFEINE 2.0 bug fix - CSCef03455 It is the initialization sequence problem.
     The origianl super() calling will eventually call down to the child and set child's private date member.
    */
    parse(value, offset, count);
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header
   * and constructs this header.<br>
   * The specified byte string <code>value</code> should be the value part (data after the colon) of
   * this header.<br>
   * If there is an exception during parsing phase, it will set the invalid flag of this header and
   * retain the various components that it already parsed. One should check the valid flag before
   * retrieving the various components of this header.
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   * @throws DsSipParserException if there is an error while parsing the specified value into this
   *     header.
   * @throws DsSipParserListenerException if there is an error condition detected by this header as
   *     a Parser Listener, while parsing.
   */
  public DsSipSubscriptionStateHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
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
   * Returns the token which is the compact name of the header.
   *
   * @return The compact token name
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
    return BS_SUBSCRIPTION_STATE_TOKEN;
  }

  /**
   * Retrieves the subscription state information.
   *
   * @return the subscription state
   */
  public DsByteString getState() {
    return m_strState;
  }

  /**
   * Sets the subscription state information.
   *
   * @param state the new subscription state
   */
  public void setState(DsByteString state) {
    m_strState = state;
  }

  /**
   * Sets the reason parameter.
   *
   * @param reason the reason parameter value
   */
  public void setReason(DsByteString reason) {
    setParameter(BS_REASON, reason);
  }

  /**
   * Gets the reason parameter.
   *
   * @return the reason parameter value
   */
  public DsByteString getReason() {
    return getParameter(BS_REASON);
  }

  /** Method used to remove the reason parameter. */
  public void removeReason() {
    removeParameter(BS_REASON);
  }

  /**
   * Sets the retry after parameter.
   *
   * @param retryAfter the retry after parameter value
   */
  public void setRetryAfter(DsByteString retryAfter) {
    setParameter(BS_RETRY_AFTER_VALUE, retryAfter);
  }

  /**
   * Gets the retry after parameter.
   *
   * @return the retry after parameter value
   */
  public DsByteString getRetryAfter() {
    return getParameter(BS_RETRY_AFTER_VALUE);
  }

  /** Method used to remove the retry after parameter. */
  public void removeRetryAfter() {
    removeParameter(BS_RETRY_AFTER_VALUE);
  }

  /**
   * Sets the expires parameter.
   *
   * @param expires the expires parameter value
   */
  public void setExpires(long expires) {
    setParameter(BS_EXPIRES_VALUE, DsByteString.valueOf(expires));
  }

  /**
   * Gets the expires parameter.
   *
   * @return the expires parameter value
   */
  public DsByteString getExpires() {
    return getParameter(BS_EXPIRES_VALUE);
  }

  /** Method used to remove the expires parameter. */
  public void removeExpires() {
    removeParameter(BS_EXPIRES_VALUE);
  }

  /**
   * Sets the retry-after parameter.
   *
   * @param retryAfter the retry-after parameter value
   */
  public void setRetryAfter(long retryAfter) {
    setParameter(BS_RETRY_AFTER_VALUE, DsByteString.valueOf(retryAfter));
  }

  /**
   * Serializes the value of this header to the specified <code>out</code> byte output stream.
   *
   * @param out the byte output stream where this header's value need to be serialized.
   * @throws IOException if there is an error while writing to the output stream
   */
  public void writeValue(OutputStream out) throws IOException {
    if (m_strState != null) {
      m_strState.write(out);
    }
    if (m_paramTable != null) {
      m_paramTable.write(out);
    }
  }

  /**
   * Copy another header's members to me.
   *
   * @param header the header to copy.
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipSubscriptionStateHeader source = (DsSipSubscriptionStateHeader) header;
    m_strState = source.m_strState;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return SUBSCRIPTION_STATE;
  }

  /**
   * Checks for equality of headers.
   *
   * @param obj the object to check
   * @return <code>true</code> if the headers are equal <code>false</code> otherwise
   */
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    DsSipSubscriptionStateHeader header = null;
    try {
      header = (DsSipSubscriptionStateHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }
    if (m_strState != null && !m_strState.equalsIgnoreCase(header.m_strState)) {
      return false;
    }
    if (m_paramTable != null && header.m_paramTable != null) {
      if (!m_paramTable.equals(header.m_paramTable)) {
        return false;
      }
    } else if (m_paramTable == null && header.m_paramTable != null) {
      if (!header.m_paramTable.isEmpty()) {
        return false;
      }
    } else if (header.m_paramTable == null && m_paramTable != null) {
      if (!m_paramTable.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    md.getEncoding(this.m_strState).write(out);
    writeEncodedParameters(out, md);
  }

  /**
   * Clears all the member data and made this header reusable. The various components (sub-elements)
   * of this headers can be set again. In this case, we reuse already constructed object like (URI,
   * Name Address, Parameter Tables, Dates, etc), to avoid expensive object creation.
   */
  public void reInit() {
    super.reInit();
    m_strState = null;
  }

  ////////////////////////////////////////////////////////////////////////////////
  // DsSipElementListener Interface implementation
  ////////////////////////////////////////////////////////////////////////////////
  /*
   * javadoc inherited.
   */
  public void elementFound(
      int contextId, int elementId, byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {
    switch (elementId) {
      case SINGLE_VALUE:
        m_strState = new DsByteString(buffer, offset, count);
        break;
      default:
        super.elementFound(contextId, elementId, buffer, offset, count, valid);
    }
  }

  /*
      public static void main(String[] args)
      {
          try
          {
              byte[] bytes = read();
              DsSipSubscriptionStateHeader header = new DsSipSubscriptionStateHeader(bytes);
  System.out.println();
  System.out.println("<<<<<<<<<<<<<<<<< HEADER >>>>>>>>>>>>>>>>>>>>");
  System.out.println();
              header.write(System.out);
  System.out.println();
  System.out.println();
  System.out.println("<<<<<<<<<<<<<<<<< CLONE >>>>>>>>>>>>>>>>>>>>");
  System.out.println();
                      DsSipSubscriptionStateHeader clone = (DsSipSubscriptionStateHeader) header.clone();
                      clone.write(System.out);
  System.out.println();
  System.out.println();
  System.out.println("<<<<<<<<<<<<<<<<< (HEADER == CLONE) = "
                                          + header.equals(clone)
                                          +" >>>>>>>>>>>>>>>>>>>>");
  System.out.println();
  System.out.println();
  System.out.println("<<<<<<<<<<<<<<<<< (CLONE == HEADER) = "
                                          + clone.equals(header)
                                          +" >>>>>>>>>>>>>>>>>>>>");
  System.out.println();
          }
          catch(Exception e)
          {
              e.printStackTrace();
          }
      }// Ends main()
  */

} // Ends class DsSipSubscriptionState
