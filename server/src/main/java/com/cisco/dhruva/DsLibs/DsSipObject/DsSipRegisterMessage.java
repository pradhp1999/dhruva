// Copyright (c) 2005-2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;

/** This class represents the Register message as specified in RFC 3261. */
public final class DsSipRegisterMessage extends DsSipRequest {
  /** Default constructor. */
  protected DsSipRegisterMessage() {
    super(REGISTER);
  }

  protected DsSipRegisterMessage(boolean encoded) {
    super(REGISTER, encoded);
  }

  /**
   * Constructs this REGISTER request with the specified <code>from</code> header, <code>to</code>
   * header, <code>contact</code> header, <code>callId</code>, <code>cSeqNo</code>, <code>bodyType
   * </code> and the specified <code>body</code>. The specified headers, <code>from</code>, <code>to
   * </code> and <code>contact</code> will be cloned.
   *
   * @param from the from header object
   * @param to the to header object
   * @param contact the contact header object
   * @param callId the call id
   * @param cSeqNo the sequence number
   * @param bodyType the body type
   * @param body the body data
   */
  public DsSipRegisterMessage(
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
   * Constructs this REGISTER request with the specified <code>from</code> header, <code>to</code>
   * header, <code>contact</code> header, <code>callId</code>, <code>cSeqNo</code>, <code>bodyType
   * </code> and the specified <code>body</code>. The specified headers, <code>fromHeader</code>,
   * <code>toHeader</code> and <code>contactHeader</code> will be cloned if the specified option
   * <code>clone</code> is <code>true</code>.
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
  public DsSipRegisterMessage(
      DsSipFromHeader from,
      DsSipToHeader to,
      DsSipContactHeader contact,
      DsByteString callId,
      long cSeqNo,
      DsByteString bodyType,
      byte[] body,
      boolean clone) {
    super(REGISTER, from, to, contact, callId, cSeqNo, bodyType, body, clone);
    // CAFFEINE 2.0 DEVELOPMENT - Bugid used: CSCef35116 REGISTER: request URI should not have the
    // user name and @ sign in it
    DsURI uri = getURI();
    if (uri.isSipURL()) {
      DsSipURL url = (DsSipURL) uri;
      url.removeUser();
    }
  }

  /**
   * Checks whether this message is valid and returns the integer constant if valid or not. The
   * various integer constants are defined in the class {@link DsSipInvalidReasons}.
   *
   * @return the integer constant that will tell whether this message is valid or not.
   */
  public int isValidWithReason() {
    DsURI uri = getURI();
    DsSipURL url = null;
    if (uri.isSipURL()) {
      url = (DsSipURL) uri;
      DsByteString user = url.getUser();
      if (user != null && user.length() > 0) {
        return DsSipInvalidReasons.REGISTER_HAS_USER;
      }
    }
    return DsSipInvalidReasons.VALID;
    // TODO: we need to have it checked from validator
    //        return super.isValidWithReason();
  }
}
