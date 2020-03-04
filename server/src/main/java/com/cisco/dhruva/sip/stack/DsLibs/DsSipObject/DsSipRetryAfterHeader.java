// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import java.io.*;

/**
 * This class represents the Retry-After header as specified in RFC 3261. It provides methods to
 * build, access, modify, serialize and clone the header.
 *
 * <p>SIP Dates are case sensitive.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Retry-After   =  "Retry-After" ":" ( SIP-date | delta-seconds )
 *                    [ comment ] [ ";" "duration" "=" delta-seconds ]
 * SIP-date      =  rfc1123-date
 * rfc1123-date  =   wkday "," SP date1 SP time SP "GMT"
 * date1         =  2DIGIT SP month SP 4DIGIT                   ; day month year (e.g., 02 Jun 1982)
 * time          =  2DIGIT ":" 2DIGIT ":" 2DIGIT                ; 00:00:00 - 23:59:59
 * wkday         =  "Mon" | "Tue" | "Wed" | "Thu" | "Fri" | "Sat" | "Sun"
 * month         =  "Jan" | "Feb" | "Mar" | "Apr" | "May" | "Jun" |
 *                  "Jul" | "Aug" | "Sep" | "Oct" | "Nov" | "Dec"
 * delta-seconds =  1*DIGIT
 * </pre> </code>
 */
public final class DsSipRetryAfterHeader extends DsSipDateOrDeltaHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_RETRY_AFTER;
  /** Header ID. */
  public static final byte sID = RETRY_AFTER;
  /** Compact header token. */
  public static final DsByteString sCompactToken = sToken;

  /** Start of the duration parameter. */
  private static final DsByteString STR_DURATION = new DsByteString(";duration=");

  /** The comment. */
  private DsByteString m_strComment;
  /** The duration. */
  private long m_lDuration;
  /** Parameters. */
  private DsParameters m_paramTable;

  /** Default constructor. */
  public DsSipRetryAfterHeader() {
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
  public DsSipRetryAfterHeader(byte[] value)
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
  public DsSipRetryAfterHeader(byte[] value, int offset, int count)
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
  public DsSipRetryAfterHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Constructs this header with the specified <code>deltaSeconds</code> value. It will also
   * initialize the DsDate object that will hold the date vale as (current time + deltaSeconds). It
   * will also set the <code>isSipDate</code> flag to <code>false</code>. So invoking {@link
   * DsSipDateOrDeltaHeader#isSipDate() isSipDate()} will return <code>false</code>.
   *
   * @param deltaSeconds the delta seconds that needs to be set for this header.
   */
  public DsSipRetryAfterHeader(long deltaSeconds) {
    super(deltaSeconds);
  }

  /**
   * Constructs this header with the specified <code>deltaSeconds</code> value and the specified
   * <code>duration</code> value. It will also initialize the DsDate object that will hold the date
   * vale as (current time + deltaSeconds). It will also set the <code>isSipDate</code> flag to
   * <code>false</code>. So invoking {@link DsSipDateOrDeltaHeader#isSipDate() isSipDate()} will
   * return <code>false</code>.
   *
   * @param deltaSeconds the delta seconds that needs to be set for this header.
   * @param duration the duration parameter value for this header.
   */
  public DsSipRetryAfterHeader(long deltaSeconds, long duration) {
    super(deltaSeconds);
    setDuration(duration);
  }

  /**
   * Constructs this header with the specified <code>date</code> value and the specified <code>
   * duration</code> value. It will also set the delta seconds value based on the time difference
   * between this specified <code>date</code> and the current time in seconds, i.e (delta seconds =
   * date - current time). If the time difference is in negative then delta seconds will be set to
   * 0. It will also set the <code>isSipDate</code> flag to <code>true</code>. So invoking {@link
   * DsSipDateOrDeltaHeader#isSipDate() isSipDate()} will return <code>true</code>.
   *
   * @param date the date that needs to be set for this header.
   * @param duration the duration parameter value for this header.
   */
  public DsSipRetryAfterHeader(DsDate date, long duration) {
    super(date);
    setDuration(duration);
  }

  /**
   * Constructs this header with the specified <code>date</code> value. It will also set the delta
   * seconds value based on the time difference between this specified <code>date</code> and the
   * current time in seconds, i.e (delta seconds = date - current time). If the time difference is
   * in negative then delta seconds will be set to 0. It will also set the <code>isSipDate</code>
   * flag to <code>true</code>. So invoking {@link DsSipDateOrDeltaHeader#isSipDate() isSipDate()}
   * will return <code>true</code>.
   *
   * @param date the date that needs to be set for this header.
   */
  public DsSipRetryAfterHeader(DsDate date) {
    super(date);
  }

  /**
   * Returns the token which is the complete name of the header.
   *
   * @return The complete token name.
   */
  public DsByteString getToken() {
    return sToken;
  }

  /**
   * Returns the token which is the compact name of the header.
   *
   * @return The compact token name.
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
    return BS_RETRY_AFTER_TOKEN;
  }

  /**
   * Checks if a comment is present.
   *
   * @return <code>true</code> if a comment is present, <code>false</code> otherwise.
   */
  public boolean hasComment() {
    return (m_strComment != null);
  }

  /**
   * Retrieves the comment information.
   *
   * @return the comment information.
   */
  public DsByteString getComment() {
    return m_strComment;
  }

  /** Removes the comment. */
  public void removeComment() {
    m_strComment = null;
  }

  /**
   * Sets the comment information.
   *
   * @param comment the comment information.
   */
  public void setComment(DsByteString comment) {
    m_strComment = comment;
  }

  /**
   * Checks if a duration is present.
   *
   * @return <code>true</code> if a duration is present, <code>false</code> otherwise.
   */
  public boolean hasDuration() {
    return (m_lDuration > 0);
  }

  /**
   * Retrieves the duration time.
   *
   * @return the duration time in seconds.
   */
  public long getDuration() {
    return m_lDuration;
  }

  /** Removes the duration. */
  public void removeDuration() {
    m_lDuration = 0;
  }

  /**
   * Sets the duration in seconds.
   *
   * @param duration the duration in seconds.
   */
  public void setDuration(long duration) {
    if (duration > 0) {
      m_lDuration = duration;
    }
  }

  /**
   * Serializes the value of this header to the specified <code>out</code> byte output stream.
   *
   * @param out the byte output stream where this header's value need to be serialized.
   * @throws IOException if there is an error while writing to the output stream.
   */
  public void writeValue(OutputStream out) throws IOException {
    DsByteString data = null;
    if (m_bDate) {
      data = new DsByteString(m_Date.getDateAsString());
      data.write(out);
    } else {
      out.write(DsIntStrCache.intToBytes(m_lDeltaSeconds));
    }
    if (m_strComment != null && m_strComment.length() > 0) {
      if (m_strComment.charAt(0) == '(') {
        out.write(B_SPACE);
        m_strComment.write(out);
      } else {
        out.write(B_SPACE);
        out.write(B_LBRACE);
        m_strComment.write(out);
        out.write(B_RBRACE);
      }
    }
    if (m_lDuration > 0) {
      STR_DURATION.write(out);
      out.write(DsIntStrCache.intToBytes(m_lDuration));
    }
    if (m_paramTable != null) {
      m_paramTable.write(out);
    }
  }
  /*
      protected void updateValue()
      {
          Vector dataChunks = new Vector(8);
          DsByteString data =  null;
          int size = 0;

          if (m_bDate)
          {
              data = new DsByteString(m_Date.getDateAsString());
          }
          else
          {
              data = new DsByteString(String.valueOf(m_lDeltaSeconds));
          }
          size += data.appendTo(dataChunks);
          if (m_strComment != null && m_strComment.length() > 0)
          {
              if (m_strComment.charAt(0) == '(')
              {
                  size += BS_SPACE.appendTo(dataChunks);
                  size += m_strComment.appendTo(dataChunks);
              }
              else
              {
                  size += BS_SPACE.appendTo(dataChunks);
                  size += BS_LBRACE.appendTo(dataChunks);
                  size += m_strComment.appendTo(dataChunks);
                  size += BS_RBRACE.appendTo(dataChunks);
              }
          }
          if (m_lDuration > 0)
          {
              size += STR_DURATION.appendTo(dataChunks);
              data = new DsByteString(String.valueOf(m_lDuration));
              size += data.appendTo(dataChunks);
          }
          if (m_paramTable != null)
          {
              data = m_paramTable.getValue();
              if (data != null) size += data.appendTo(dataChunks);
          }
          byte[] bytes = new byte[size];
          size = 0;
          for (int i = 0; i < dataChunks.size(); i++)
          {
              data = (DsByteString)dataChunks.elementAt(i);
              data.appendTo(bytes,size); // Here size actually serves as offset index
              size += data.length();
          }
          dataChunks.clear();
          dataChunks = null;
          setValue(bytes);
      }
  */
  /**
   * Copy another header's members to me.
   *
   * @param header the header to copy.
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipRetryAfterHeader source = (DsSipRetryAfterHeader) header;
    m_strComment = source.m_strComment;
    m_lDuration = source.m_lDuration;
    m_paramTable = source.m_paramTable;
  }

  /*
   * This method makes a copy of the header.
   */
  /*
      public Object clone()
      {
          DsSipRetryAfterHeader clone = (DsSipRetryAfterHeader)super.clone();
          if (m_strComment != null)
          {
              clone.m_strComment = (DsByteString)m_strComment.clone();
          }
          clone.m_lDuration = m_lDuration;
          if (m_paramTable != null)
          {
              clone.m_paramTable = (DsParameters)m_paramTable.clone();
          }
          return clone;
      }
  */
  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return RETRY_AFTER;
  }

  /**
   * Checks for equality of headers.
   *
   * @param obj the object to check.
   * @return <code>true</code> if the headers are equal <code>false</code> otherwise.
   */
  public boolean equals(Object obj) {
    if (!super.equals(obj)) {
      return false;
    }
    DsSipRetryAfterHeader header = null;
    try {
      header = (DsSipRetryAfterHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }
    if (m_lDuration != header.m_lDuration) {
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
    // else both null - ok
    return true;
  }

  /**
   * Tells whether this header has any parameters.
   *
   * @return <code>true</code> if there are any parameters, <code>false</code>. otherwise.
   */
  public boolean hasParameters() {
    return (m_paramTable != null && !m_paramTable.isEmpty());
  }

  /**
   * Returns the parameters that are present in this header.
   *
   * @return the parameters that are present in this header.
   */
  public DsParameters getParameters() {
    return m_paramTable;
  }

  /**
   * Sets the specified parameters for this header. It will override the existing parameters only if
   * the specified parameters object is not null. To remove the parameters from this header use
   * {@link #removeParameters()}.
   *
   * @param paramTable the new parameters object that need to be set for this. header.
   */
  public void setParameters(DsParameters paramTable) {
    if (paramTable != m_paramTable) {
      m_paramTable = paramTable;
    }
  }

  /** Removes any existing parameters in this header. */
  public void removeParameters() {
    if (m_paramTable != null) {
      if (!m_paramTable.isEmpty()) {
        m_paramTable.clear();
      }
      m_paramTable = null;
    }
  }

  /**
   * Tells whether this header contains a parameter with the specified. parameter <code>name</code>.
   *
   * @param key the name of the parameter that needs to be checked.
   * @return <code>true</code> if a parameter with the specified name is. present, <code>false
   *     </code> otherwise.
   */
  public boolean hasParameter(DsByteString key) {
    return (m_paramTable != null && m_paramTable.isPresent(key));
  }

  /**
   * Returns the parameter value for the parameter with the specified. <code>name</code>, if
   * present, otherwise returns null.
   *
   * @param name the name of the parameter that needs to be retrieved.
   * @return the parameter value for the parameter with the specified <code>name</code>, if present,
   *     otherwise returns null.
   */
  public DsByteString getParameter(DsByteString name) {
    DsByteString value = null;
    if (m_paramTable != null && !m_paramTable.isEmpty()) {
      value = m_paramTable.get(name);
    }
    return value;
  }

  /**
   * Sets the specified name-value parameter in this header. It will override the existing value of
   * the parameter, if already present.
   *
   * @param name the name of the parameter.
   * @param value the value of the parameter.
   */
  public void setParameter(DsByteString name, DsByteString value) {
    if (m_paramTable == null) {
      m_paramTable = new DsParameters();
    }
    m_paramTable.put(name, value);
  }

  /**
   * Removes the parameter with the specified <code>name</code>, if present.
   *
   * @param name the name of the parameter that needs to be removed.
   */
  public void removeParameter(DsByteString name) {
    if (m_paramTable != null) {
      m_paramTable.remove(name);
    }
  }

  /**
   * Clears all the member data and made this header reusable. The various components (sub-elements)
   * of this headers can be set again. In this case, we reuse already constructed objects like (URI,
   * Name Address, Parameter Tables, Dates, etc), to avoid expensive object creation.
   */
  public void reInit() {
    super.reInit();
    if (m_paramTable != null) {
      m_paramTable.reInit();
    }
    m_strComment = null;
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
      case COMMENT:
        m_strComment = new DsByteString(buffer, offset, count);
        break;
      default:
        super.elementFound(contextId, elementId, buffer, offset, count, valid);
    }
  }

  /*
   * javadoc inherited.
   */
  public void parameterFound(
      int contextId, byte[] buffer, int nameOffset, int nameCount, int valueOffset, int valueCount)
      throws DsSipParserListenerException {
    if (BS_DURATION.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      try {
        setDuration(DsSipMsgParser.parseLong(buffer, valueOffset, valueCount));
      } catch (NumberFormatException nfe) {
        throw new DsSipParserListenerException(
            "Exception while constructing the long value: ", nfe);
      }
      return;
    }
    setParameter(
        new DsByteString(buffer, nameOffset, nameCount),
        new DsByteString(buffer, valueOffset, valueCount));
  }
}
