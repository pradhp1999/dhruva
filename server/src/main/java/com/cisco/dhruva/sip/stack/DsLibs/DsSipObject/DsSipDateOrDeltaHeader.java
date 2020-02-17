// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipMsgParser;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsDate;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An abstract base class for the headers that can have either SIP date as its value or delta
 * seconds.
 */
public abstract class DsSipDateOrDeltaHeader extends DsSipDateOnlyHeader {
  /** Represents the delta seconds value for this header. */
  protected long m_lDeltaSeconds;

  /** Represents SIP date value. */
  protected boolean m_bDate;

  /** Default constructor. */
  protected DsSipDateOrDeltaHeader() {
    super();
  }

  /**
   * Constructs this header with the specified <code>deltaSeconds</code> value. It will also
   * initialize the DsDate object that will hold the date vale as (current time + deltaSeconds). It
   * will also set the <code>isSipDate</code> flag to <code>false</code>. So invoking {@link
   * #isSipDate() isSipDate()} will return <code>false</code>.
   *
   * @param deltaSeconds the delta seconds that needs to be set for this header.
   */
  protected DsSipDateOrDeltaHeader(long deltaSeconds) {
    super();
    setDeltaSeconds(deltaSeconds);
  }

  /**
   * Constructs this header with the specified <code>date</code> value. It will also set the delta
   * seconds value based on the time difference between this specified <code>date</code> and the
   * current time in seconds, i.e (delta seconds = date - current time). If the time difference is
   * in negative then delta seconds will be set to 0. It will also set the <code>isSipDate</code>
   * flag to <code>true</code>. So invoking {@link #isSipDate() isSipDate()} will return <code>true
   * </code>.
   *
   * @param date the date that needs to be set for this header.
   */
  protected DsSipDateOrDeltaHeader(DsDate date) {
    super(date);
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
  protected DsSipDateOrDeltaHeader(byte[] value)
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
  protected DsSipDateOrDeltaHeader(byte[] value, int offset, int count)
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
  protected DsSipDateOrDeltaHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Tells whether this header is initialized with the SIP date or the delta seconds.
   *
   * @return <code>true</code> if this header is initialized with the SIP date <code>false</code>
   *     otherwise.
   */
  public boolean isSipDate() {
    return m_bDate;
  }

  /**
   * Returns the delta seconds value present in this header. The returned value will be in seconds.
   *
   * @return the delta seconds value present in this header.
   */
  public long getDeltaSeconds() {
    return m_lDeltaSeconds;
  }

  /**
   * Sets the SIP date value for this header. It will also set the delta seconds value based on the
   * time difference between this specified <code>date</code> and the current time in seconds, i.e
   * (delta seconds = date - current time). If the time difference is in negative then delta seconds
   * will be set to 0. It will also set the <code>isSipDate</code> flag to <code>true</code>. So
   * invoking {@link #isSipDate() isSipDate()} will return <code>true</code>.
   *
   * @param date the date that needs to be set for this header.
   */
  public void setDate(DsDate date) {
    if (date != null) {
      m_Date = date;
      m_bDate = true;
      long diff = date.getDate().getTime() - System.currentTimeMillis();
      m_lDeltaSeconds = (diff > 0) ? diff / 1000 : 0;
    }
  }

  /**
   * Sets the delta seconds value for this header. It will also initialize the DsDate object that
   * will hold the date vale as (current time + deltaSeconds). It will also set the <code>isSipDate
   * </code> flag to <code>false</code>. So invoking {@link #isSipDate() isSipDate()} will return
   * <code>false</code>.
   *
   * @param deltaSeconds the delta seconds that needs to be set for this header.
   */
  public void setDeltaSeconds(long deltaSeconds) {
    if (deltaSeconds < 0) {
      return;
    }
    m_lDeltaSeconds = deltaSeconds;
    m_bDate = false;
    if (m_Date == null) {
      m_Date = new DsDate();
    }
    m_Date.setDate(new java.util.Date(System.currentTimeMillis() + (m_lDeltaSeconds * 1000)));
  }

  /**
   * Copy another header's members to me.
   *
   * @param header the header from which data members are copied
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipDateOrDeltaHeader source = (DsSipDateOrDeltaHeader) header;
    m_bDate = source.m_bDate;
    m_lDeltaSeconds = source.m_lDeltaSeconds;
  }

  /*
   * This method makes a copy of the header.
   */
  /*
      public Object clone()
      {
          DsSipDateOrDeltaHeader clone = (DsSipDateOrDeltaHeader)super.clone();
          clone.m_lDeltaSeconds = m_lDeltaSeconds;
          clone.m_bDate = m_bDate;
          return clone;
      }
  */
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
    DsSipDateOrDeltaHeader header = null;
    try {
      header = (DsSipDateOrDeltaHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }

    if (m_bDate != header.m_bDate) {
      return false;
    }
    if (m_bDate) {
      if (m_Date != null) {
        if (header.m_Date == null) {
          return false;
        }
        if (!m_Date.equals(header.m_Date)) {
          return false;
        }
      } else {
        if (header.m_Date != null) {
          return false;
        }
      }
    } else if (m_lDeltaSeconds != header.m_lDeltaSeconds) {
      return false;
    }
    return true;
  }

  /**
   * Serializes the value of this header to the specified <code>out</code> byte output stream.
   *
   * @param out the byte output stream where this header's value need to be serialized.
   * @throws IOException if there is an error while writing to the output stream
   */
  public void writeValue(OutputStream out) throws IOException {
    byte[] bytes =
        (m_bDate)
            ? DsByteString.getBytes(m_Date.getDateAsString())
            : DsByteString.getBytes(Long.toString(m_lDeltaSeconds));
    out.write(bytes);
  }

  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    DsByteString value =
        (m_bDate)
            ? (new DsByteString(m_Date.getDateAsString()))
            : (new DsByteString(Long.toString(m_lDeltaSeconds)));
    md.getEncoding(value).write(out);
  }

  /**
   * Clears all the member data and made this header reusable. The various components (sub-elements)
   * of this headers can be set again. In this case, we reuse already constructed objects like (URI,
   * Name Address, Parameter Tables, Dates, etc), to avoid expensive object creation.
   */
  public void reInit() {
    super.reInit();
    m_bDate = false;
    m_lDeltaSeconds = 0;
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
    if (DsSipMessage.DEBUG) {
      System.out.println(
          "elementFound - contextId = ["
              + contextId
              + "]["
              + DsSipMsgParser.HEADER_NAMES[contextId]
              + "]");
      System.out.println(
          "elementFound - elementId = ["
              + elementId
              + "]["
              + DsSipMsgParser.ELEMENT_NAMES[elementId]
              + "]");
      System.out.println("elementFound - value [offset, count] = [" + offset + ", " + count + "]");
      System.out.println(
          "elementFound - value = [" + DsByteString.newString(buffer, offset, count) + "]");
      System.out.println();
    }
    switch (elementId) {
      case DELTA_SECONDS:
        try {
          setDeltaSeconds(DsSipMsgParser.parseLong(buffer, offset, count));
        } catch (NumberFormatException nfe) {
          throw new DsSipParserListenerException(
              "Exception while constructing the float value: ", nfe);
        }
        break;
      default:
        super.elementFound(contextId, elementId, buffer, offset, count, valid);
    }
  }
} // Ends class DsSipDateOrDeltaHeader
