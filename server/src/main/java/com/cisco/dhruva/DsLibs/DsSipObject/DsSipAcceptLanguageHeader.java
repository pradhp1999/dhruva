// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;

/**
 * This class represents a Accept-Language header as specified in RFC 3261. It provides methods to
 * build, access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Accept-Language =  "Accept-Language" ":" 1#( language-range [ ";" "q" "=" qvalue ] )
 * language-range  =  ( ( 1*8ALPHA *( "-" 1*8ALPHA ) ) | "*" )
 * </pre> </code>
 */
public final class DsSipAcceptLanguageHeader extends DsSipLanguageHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_ACCEPT_LANGUAGE;
  /** Header ID. */
  public static final byte sID = ACCEPT_LANGUAGE;
  /** Compact header token. */
  public static final DsByteString sCompactToken = sToken;

  private float qvalue = (float) -1.0;

  /** Default constructor. */
  public DsSipAcceptLanguageHeader() {
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
  public DsSipAcceptLanguageHeader(byte[] value)
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
  public DsSipAcceptLanguageHeader(byte[] value, int offset, int count)
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
  public DsSipAcceptLanguageHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Constructs this header with the specified language and the specified <code>qvalue</code>.
   *
   * @param language the language value for this header.
   * @param qvalue the <code>qvalue</code> parameter value.
   */
  public DsSipAcceptLanguageHeader(DsByteString language, float qvalue) {
    super();
    m_strLanguage = language;
    this.qvalue = qvalue;
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
    return BS_ACCEPT_LANGUAGE_TOKEN;
  }

  /**
   * Checks if QValue exists.
   *
   * @return true, if QValue exists, false otherwise.
   */
  public boolean hasQValue() {
    return (qvalue != -1.0);
  }

  /**
   * Retrieves the Q value.
   *
   * @return the Q value. Returns -1 when Q value is not found.
   * @see #setQValue(float)
   */
  public float getQValue() {
    return qvalue;
  }

  /** Removes the Q value. */
  public void removeQValue() {
    qvalue = (float) -1.0;
  }

  /**
   * Sets the Q value.
   *
   * @param pValue the Q value.
   * @see #getQValue
   */
  public void setQValue(float pValue) {
    qvalue = pValue;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return ACCEPT_LANGUAGE;
  }
  /**
   * Copy another header's members to me.
   *
   * @param header the header to copy.
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipAcceptLanguageHeader source = (DsSipAcceptLanguageHeader) header;
    qvalue = source.qvalue;
  }

  /*
   * This method makes a copy of the header
   */
  /*
      public Object clone()
      {
          DsSipAcceptLanguageHeader clonedHeader = (DsSipAcceptLanguageHeader)super.clone();
          clonedHeader.qvalue          = qvalue;
          return clonedHeader;
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
    if (!super.equals(obj)) {
      return false;
    }
    DsSipAcceptLanguageHeader header = null;
    try {
      header = (DsSipAcceptLanguageHeader) obj;
    } catch (ClassCastException cce) {
      return false;
    }
    if (qvalue != header.qvalue) {
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
    super.writeValue(out);
    if (qvalue >= 0.0 && qvalue <= 1.0) {
      BS_QVALUE.write(out);
      out.write(DsByteString.getBytes(Float.toString(qvalue)));
    }
  }

  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    super.writeEncodedValue(out, md);

    if (qvalue >= 0.0 && qvalue <= 1.0) {
      DsByteString qval = BS_QVALUE.copy().append(Float.toString(qvalue).getBytes());
      md.getEncoding(qval).write(out);
    }
  }

  /**
   * Clears all the member data and made this header reusable. The various components (sub-elements)
   * of this headers can be set again. In this case, we reuse already constructed object like (URI,
   * Name Address, Parameter Tables, Dates, etc), to avoid expensive object creation.
   */
  public void reInit() {
    super.reInit();
    qvalue = (float) -1.0;
  }

  ////////////////////////////////////////////////////////////////////////////////
  // DsSipElementListener Interface implementation
  ////////////////////////////////////////////////////////////////////////////////
  /*
   * javadoc inherited.
   */
  public void parameterFound(
      int contextId, byte[] buffer, int nameOffset, int nameCount, int valueOffset, int valueCount)
      throws DsSipParserListenerException {
    if (BS_Q.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      try {
        qvalue = DsSipMsgParser.parseFloat(buffer, valueOffset, valueCount);
      } catch (NumberFormatException nfe) {
      }
    }
  }
} // Ends class DsSipAcceptLanguageHeader
