// ////////////////////////////////////////////////////////////////
// FILENAME:    DsSipDialogIDWithParamsHeader.java
//
// MODULE:      DsSipObject
//
// COPYRIGHT:
// ============== copyright 2004 dynamicsoft Inc. =================
// ==================== all rights reserved =======================

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.IOException;
import java.io.OutputStream;

/** This class is a abstract base class for SIP JOIN and REPLACES headers. */
public abstract class DsSipDialogIDWithParamsHeader extends DsSipParametricHeader {
  /** Holds the first parameter value for this header. */
  protected DsByteString callId;

  /** Default constructor. */
  protected DsSipDialogIDWithParamsHeader() {
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
  public DsSipDialogIDWithParamsHeader(byte[] value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value, 0, value.length);
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
  public DsSipDialogIDWithParamsHeader(byte[] value, int offset, int count)
      throws DsSipParserException, DsSipParserListenerException {
    /*
     CAFFEINE 2.0 bug fix - CSCef03455 It is the initialization sequence problem.
     The origianl super() calling will eventually call down to the child and set child's private date member.
    */
    parse(value, offset, count);
  }

  /**
   * Constructs this header with the specified value.<br>
   * The specified byte string <code>value</code> should be the value part (data after the colon) of
   * this header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   */
  public DsSipDialogIDWithParamsHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Returns the first singlevalue parameter value for this header, which is callId.
   *
   * @return the callId.
   */
  public DsByteString getCallId() {
    return callId;
  }

  /**
   * Sets the call ID information.
   *
   * @param callId the new call ID
   */
  public void setCallId(DsByteString callId) {
    this.callId = callId;
  }

  /**
   * Sets the to-tag parameter.
   *
   * @param toTag the to-tag parameter value
   */
  public void setToTag(DsByteString toTag) {
    this.setParameter(BS_TO_TAG, toTag);
  }

  /**
   * Gets the to-tag parameter.
   *
   * @return the to-tag parameter value
   */
  public DsByteString getToTag() {
    return this.getParameter(BS_TO_TAG);
  }

  /** Method used to remove the to-tag parameter. */
  public void removeToTag() {
    this.removeParameter(BS_TO_TAG);
  }

  /**
   * Sets the from-tag parameter.
   *
   * @param fromTag the from-tag parameter value
   */
  public void setFromTag(DsByteString fromtag) {
    this.setParameter(BS_FROM_TAG, fromtag);
  }

  /**
   * Gets the from-tag parameter.
   *
   * @return the from-tag parameter value
   */
  public DsByteString getFromTag() {
    return this.getParameter(BS_FROM_TAG);
  }

  /** Method used from remove the from-tag parameter. */
  public void removeFromTag() {
    removeParameter(BS_FROM_TAG);
  }

  /** Sets the early-only flag. */
  public void setEarlyOnly() {
    setParameter(BS_EARLY_ONLY, DsByteString.BS_EMPTY_STRING);
  }

  /**
   * Check for the existence of the early-only flag.
   *
   * @return <code>true</code> if the early only flag is set, else <code>false</code>
   */
  public boolean isEarlyOnly() {
    return hasParameter(BS_EARLY_ONLY);
  }

  /** Method used from remove the early-only flag. */
  public void removeEarlyOnly() {
    removeParameter(BS_EARLY_ONLY);
  }

  /**
   * Serializes the value of this header to the specified <code>out</code> byte output stream.
   *
   * @param out the byte output stream where this header's value need to be serialized.
   * @throws IOException if there is an error while writing to the output stream
   */
  public void writeValue(OutputStream out) throws IOException {
    if (callId != null) {
      callId.write(out);
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
    DsSipDialogIDWithParamsHeader source = (DsSipDialogIDWithParamsHeader) header;
    this.callId = source.callId;
    // table copy is done by super class
  }

  /**
   * Returns a deep copy of the header object and all of the other elements on the list that it is
   * associated with. NOTE: This behavior will change when the deprecated methods are removed and it
   * will just clone the single header.
   *
   * @return the cloned JOIN header object
   */
  public Object clone() {
    DsSipDialogIDWithParamsHeader clone = (DsSipDialogIDWithParamsHeader) super.clone();
    clone.setCallId(this.callId);
    return clone;
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
    DsSipDialogIDWithParamsHeader header = null;
    try {
      header = (DsSipDialogIDWithParamsHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }
    if (callId != null && !callId.equalsIgnoreCase(header.callId)) {
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
    md.getEncoding(this.callId).write(out);
    writeEncodedParameters(out, md);
  }

  /**
   * Clears all the member data and made this header reusable. The various components (sub-elements)
   * of this headers can be set again. In this case, we reuse already constructed object like (URI,
   * Name Address, Parameter Tables, Dates, etc), to avoid expensive object creation.
   */
  public void reInit() {
    super.reInit();
    this.callId = null;
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
        callId = new DsByteString(buffer, offset, count);
        break;
      default:
        super.elementFound(contextId, elementId, buffer, offset, count, valid);
    }
  }
}
