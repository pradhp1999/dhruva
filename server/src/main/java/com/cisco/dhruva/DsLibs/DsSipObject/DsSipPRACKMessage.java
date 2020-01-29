// Copyright (c) 2005-2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsUtil.*;

/** This class represents the PRACK message as specified in RFC 3261. */
public final class DsSipPRACKMessage extends DsSipRequest {
  // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
  private static long m_prackCSeqNumber = 0l;

  /** Default constructor. */
  protected DsSipPRACKMessage() {
    super(PRACK);
  }

  protected DsSipPRACKMessage(boolean encoded) {
    super(PRACK, encoded);
  }

  // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
  /**
   * Constructs this PRACK request with specified <code>response</code> and <code>request</code>
   *
   * @param response the SIP response
   * @throws IllegalArgumentException if the passed in response is null.
   */
  public DsSipPRACKMessage(DsSipResponse response) {
    // this(request);
    super(PRACK);
    if (response == null) {
      throw new IllegalArgumentException("Can not create a PRACK message from a null Response");
    }

    DsSipToFromHeader tofrom = null;
    try {
      tofrom = (DsSipToFromHeader) response.getToHeaderValidate();
    } catch (Exception exc) {
      DsLog4j.headerCat.warn("Error from response.getToHeaderValidate()", exc);
    }
    setURI(tofrom.getURI());

    updateHeader((DsSipHeaderInterface) response.getFromHeader());
    updateHeader((DsSipHeaderInterface) response.getToHeader());

    setCallId(response.getCallId());
    // addSessionIdHeader
    addSessionIDHeader();
    if (m_prackCSeqNumber < response.getCSeqNumber()) {
      m_prackCSeqNumber = response.getCSeqNumber() + 1;
    } else {
      m_prackCSeqNumber = m_prackCSeqNumber + 1;
    }
    m_lCSeq = m_prackCSeqNumber;
    m_strCSeq = BS_PRACK;

    long m_CSeqNumber = -1;
    long m_RSeqNumber = -1;
    DsByteString m_1xxResponseToMethod = null;
    m_CSeqNumber = response.getCSeqNumber();
    m_1xxResponseToMethod = response.getCSeqMethod();
    try {
      DsSipRSeqHeader sipRSeqHeader =
          (DsSipRSeqHeader) response.getHeaderValidate(DsSipConstants.RSEQ);
      if (sipRSeqHeader != null) {
        m_RSeqNumber = sipRSeqHeader.getNumber();
      }
    } catch (DsException dse) {
      DsLog4j.headerCat.warn("Error from response.getHeaderValidate(RSEQ)", dse);
    }

    DsSipRAckHeader sipRAckHeader =
        new DsSipRAckHeader(m_RSeqNumber, m_CSeqNumber, m_1xxResponseToMethod);
    addHeader(sipRAckHeader);
  }

  // CAFFEINE 2.0 DEVELOPMENT - Add more functionalities into Harness to do the O/A related
  // testings.
  /**
   * Constructs this PRACK request with specified <code>response</code>, <code>type</code> and
   * <code>body</code> Currently used by the Stack (Harness) only
   *
   * @param response the SIP response
   * @param bodyType content type (of the body)
   * @param body the body of the PRACK request)
   * @throws IllegalArgumentException if the passed in response is null.
   */
  public DsSipPRACKMessage(DsSipResponse response, DsByteString bodyType, byte[] body) {
    this(response);
    setBody(body, bodyType);
  }

  /**
   * Constructs this PRACK request with the specified <code>from</code> header, <code>to</code>
   * header, <code>contact</code> header, <code>callId</code>, <code>cSeqNo</code>, <code>rSeqNo
   * </code>, <code>bodyType</code> and the specified <code>body</code>. The specified headers,
   * <code>from</code>, <code>to</code> and <code>contact</code> will be cloned.
   *
   * @param from the from header object
   * @param to the to header object
   * @param contact the contact header object
   * @param callId the call id
   * @param cSeqNo the sequence number
   * @param bodyType the body type
   * @param body the body data
   */
  public DsSipPRACKMessage(
      DsSipFromHeader from,
      DsSipToHeader to,
      DsSipContactHeader contact,
      DsByteString callId,
      long cSeqNo,
      long rSeqNo,
      DsByteString bodyType,
      byte[] body) {
    // CAFFEINE 2.0 DEVELOPMENT - CSCed08141 PRACK messages are not including RAck
    this(from, to, contact, callId, cSeqNo, rSeqNo, bodyType, body, true);
  }

  /**
   * Constructs this PRACK request with the specified <code>from</code> header, <code>to</code>
   * header, <code>contact</code> header, <code>callId</code>, <code>cSeqNo</code>, <code>rSeqNo
   * </code>, <code>bodyType</code> and the specified <code>body</code>. The specified headers,
   * <code>fromHeader</code>, <code>toHeader</code> and <code>contactHeader</code> will be cloned if
   * the specified option <code>clone</code> is <code>true</code>.
   *
   * @param from the from header object
   * @param to the to header object
   * @param contact the contact header object
   * @param callId the call id
   * @param cSeqNo the sequence number
   * @param bodyType the body type
   * @param body the body data
   * @param clone if true, it will clone the headers, else just use the references
   */
  public DsSipPRACKMessage(
      DsSipFromHeader from,
      DsSipToHeader to,
      DsSipContactHeader contact,
      DsByteString callId,
      long cSeqNo,
      long rSeqNo,
      DsByteString bodyType,
      byte[] body,
      boolean clone) {
    super(PRACK, from, to, contact, callId, cSeqNo, bodyType, body, clone);
    // CAFFEINE 2.0 DEVELOPMENT - CSCed08141 PRACK messages are not including RAck
    DsSipRAckHeader rack = new DsSipRAckHeader(rSeqNo, cSeqNo, BS_INVITE);
    addHeader(rack, false, false);
  }
}
