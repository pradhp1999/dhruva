/*------------------------------------------------------------------
 * DsSipAcceptRejectContactHeader
 *
 * July 2003, kimle
 *
 * Copyright (c) 2003, 2006 by cisco Systems, Inc.
 * All rights reserved.
 *------------------------------------------------------------------
 */
package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import java.io.*;

/**
 * This class represents the base class for Accept-Contact header and Reject-Contact header as
 * specified in caller prefs.
 *
 * <p>It provides methods to build, access, modify, serialize and clone the header.
 */
public abstract class DsSipAcceptRejectContactHeader extends DsSipParametricHeader {
  public static final DsByteString REQUIRE = new DsByteString("require");
  public static final DsByteString EXPLICIT = new DsByteString("explicit");
  public static final DsByteString SCORE = new DsByteString("qa");
  public static final DsByteString REMOVE = new DsByteString("REMOVE");

  /** Holds the uri value for this header. */
  protected DsByteString uri;
  /** Holds the require flag for this header. */
  protected boolean require;
  /** Holds the explicit flag value for this header. */
  protected boolean explicit;

  /** Default constructor. */
  protected DsSipAcceptRejectContactHeader() {
    super();
  }

  /**
   * Constructs this header with the specified value.
   *
   * <p>The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   */
  public DsSipAcceptRejectContactHeader(byte[] value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value, 0, value.length);
  }

  /**
   * Constructs this header with the specified value.
   *
   * <p>The byte array <code>value</code> should be the value part
   *
   * <p>(data after the colon) of this header.
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   * @param offset the offset in the specified byte array, where from the value part, that needs to
   *     be parsed, starts.
   * @param count the total number of bytes, starting from the specified offset, that constitute the
   *     value part.
   */
  public DsSipAcceptRejectContactHeader(byte[] value, int offset, int count)
      throws DsSipParserException, DsSipParserListenerException {
    /*
     CAFFEINE 2.0 bug fix - CSCef03455 It is the initialization sequence problem.
     The origianl super() calling will eventually call down to the child and set child's private date member.
    */
    parse(value, offset, count);
  }

  /**
   * Constructs this header with the specified value.
   *
   * <p>The specified byte string <code>value</code> should be the value part (data after the colon)
   * of this header.
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   */
  public DsSipAcceptRejectContactHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Returns the uri value for this header.
   *
   * @return value
   */
  public DsByteString getUri() {
    return uri;
  }

  /**
   * Sets the uri value for this header.
   *
   * @param value
   */
  public void setUri(DsByteString value) {
    uri = value;
  }

  /**
   * Sets the required flag
   *
   * @param value of require flag
   */
  public void setRequire(boolean value) {
    require = value;
  }

  /**
   * Returns a boolean if require is set
   *
   * @return true if require is set.
   */
  public boolean isRequire() {
    return require;
  }

  /**
   * Sets the explicit flag
   *
   * @param value of explicit flag
   */
  public void setExplicit(boolean value) {
    explicit = value;
  }

  /**
   * Returns a boolean if explicit is set
   *
   * @return true if explicit is set.
   */
  public boolean isExplicit() {
    return explicit;
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
    DsSipAcceptRejectContactHeader header = null;
    try {
      header = (DsSipAcceptRejectContactHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }
    if (uri != null && !uri.equals(header.uri)) {
      return false;
    }
    if (require != header.require) {
      return false;
    }
    if (explicit != header.explicit) {
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

  /**
   * Copy another header's members to me.
   *
   * @param header the header to copy.
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipAcceptRejectContactHeader source = (DsSipAcceptRejectContactHeader) header;
    uri = source.uri;
    require = source.require;
    explicit = source.explicit;
  }

  /**
   * Serializes the value of this header to the specified <code>out</code> byte output stream.
   *
   * @param out the byte output stream where this header's value need to be serialized.
   * @throws IOException if there is an error while writing to the output stream
   */
  public void writeValue(OutputStream out) throws IOException {
    if (uri != null) {
      uri.write(out);
    }

    if (m_paramTable != null) {
      m_paramTable.write(out);
    }

    if (require) {
      out.write(B_SEMI);
      out.write(REQUIRE.toByteArray());
    }

    if (explicit) {
      out.write(B_SEMI);
      out.write(EXPLICIT.toByteArray());
    }
  }

  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    if (uri != null) {
      md.getEncoding(uri).write(out);
    }

    writeEncodedParameters(out, md);
  }

  protected void writeEncodedParameters(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    super.writeEncodedParameters(out, md);

    if (require) {
      out.write(DsTokenSipConstants.TOKEN_SIP_PARAMETER_LOW);
      md.getEncoding(REQUIRE).write(out);
    }

    if (explicit) {
      out.write(DsTokenSipConstants.TOKEN_SIP_PARAMETER_LOW);
      md.getEncoding(EXPLICIT).write(out);
    }
  }

  ///////////////////////////////////////////////////
  // DsSipElementListener Interface Implementation //
  ///////////////////////////////////////////////////
  public void elementFound(
      int contextId, int elementId, byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {
    switch (elementId) {
      case SINGLE_VALUE:
        if (buffer[offset] != B_WILDCARD) {
          try {
            DsSipURL url = new DsSipURL(buffer, offset, count);
            setParameter(
                DsSipCallerPrefs.URIUSER,
                new DsByteString("\"<" + url.getUser().toString() + ">\""));
            setParameter(
                DsSipCallerPrefs.URIDOMAIN,
                new DsByteString("\"" + url.getHost().toString() + "\""));
            uri = BS_WILDCARD;
          } catch (Exception e) {
            uri = new DsByteString(buffer, offset, count);
          }
        } else {
          uri = BS_WILDCARD;
        }
        break;
      default:
        super.elementFound(contextId, elementId, buffer, offset, count, valid);
    }
  }

  public void parameterFound(
      int contextId, byte[] buffer, int nameOffset, int nameCount, int valueOffset, int valueCount)
      throws DsSipParserListenerException {
    if (REQUIRE.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      require = true;
    } else if (EXPLICIT.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      explicit = true;
    } else {
      super.parameterFound(contextId, buffer, nameOffset, nameCount, valueOffset, valueCount);
    }
  }
}
