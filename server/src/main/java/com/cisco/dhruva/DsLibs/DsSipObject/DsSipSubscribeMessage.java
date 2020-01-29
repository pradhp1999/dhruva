// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

/** This class represents the SUBSCRIBE message as specified in RFC 3265. */
public final class DsSipSubscribeMessage extends DsSipRequest {

  /** Default constructor. */
  protected DsSipSubscribeMessage() {
    super(SUBSCRIBE);
  }

  protected DsSipSubscribeMessage(boolean encoded) {
    super(SUBSCRIBE, encoded);
  }

  /**
   * Constructs this SUBSCRIBE request with the specified <code>from</code> header, <code>to</code>
   * header, <code>contact</code> header, <code>callId</code>, <code>cSeqNo</code>, <code>bodyType
   * </code> and the specified <code>body</code>. The specified headers, <code>from</code>, <code>to
   * </code> and <code>contact</code> will be cloned.
   *
   * @param from the from header object.
   * @param to the to header object.
   * @param contact the contact header object.
   * @param callId the call id.
   * @param cSeqNo the sequence number.
   * @param bodyType the body type.
   * @param body the body data.
   */
  public DsSipSubscribeMessage(
      DsSipFromHeader from,
      DsSipToHeader to,
      DsSipContactHeader contact,
      DsByteString callId,
      long cSeqNo,
      DsByteString bodyType,
      byte[] body) {
    this(from, to, contact, callId, cSeqNo, bodyType, body, true);
  }

  /**
   * Constructs this SUBSCRIBE request with the specified <code>from</code> header, <code>to</code>
   * header, <code>contact</code> header, <code>callId</code>, <code>cSeqNo</code>, <code>bodyType
   * </code> and the specified <code>body</code>. The specified headers, <code>fromHeader</code>,
   * <code>toHeader</code> and <code>contactHeader</code> will be cloned if the specified option
   * <code>clone</code> is <code>true</code>.
   *
   * @param from the from header object.
   * @param to the to header object.
   * @param contact the contact header object.
   * @param callId the call id.
   * @param cSeqNo the sequence number.
   * @param bodyType the body type.
   * @param body the body data.
   * @param clone if true, it will clone the headers, else just use the references.
   */
  public DsSipSubscribeMessage(
      DsSipFromHeader from,
      DsSipToHeader to,
      DsSipContactHeader contact,
      DsByteString callId,
      long cSeqNo,
      DsByteString bodyType,
      byte[] body,
      boolean clone) {
    super(SUBSCRIBE, from, to, contact, callId, cSeqNo, bodyType, body, clone);
  }
}
