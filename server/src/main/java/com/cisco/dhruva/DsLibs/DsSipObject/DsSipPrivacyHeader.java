// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;
import java.util.*;

/**
 * This class represents the Privacy header as specified in RFC 3323, along with the additional
 * privacy type "id" from RFC 3325. It provides methods to build, access, modify, serialize and
 * clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Privacy-hdr  =  "Privacy" HCOLON priv-value *(";" priv-value)
 * priv-value   =  "header" / "session" / "user" / "none" / "critical" / "id" / token
 * </pre> </code>
 */
public final class DsSipPrivacyHeader extends DsSipHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_PRIVACY;
  /** Header ID. */
  public static final byte sID = PRIVACY;
  /** Compact header token. */
  public static final DsByteString sCompactToken = sToken;

  /** <code>true</code> if the header value header is set. */
  private boolean m_header;
  /** <code>true</code> if the session value header is set. */
  private boolean m_session;
  /** <code>true</code> if the user value header is set. */
  private boolean m_user;
  /** <code>true</code> if the none value header is set. */
  private boolean m_none;
  /** <code>true</code> if the critical value header is set. */
  private boolean m_critical;
  /** <code>true</code> if the id value header is set. */
  private boolean m_id;

  /** The list of DsByteStrings that represent extension tokens. */
  private LinkedList m_tokens;

  /** Default constructor. */
  public DsSipPrivacyHeader() {
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
  public DsSipPrivacyHeader(byte[] value)
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
  public DsSipPrivacyHeader(byte[] value, int offset, int count)
      throws DsSipParserException, DsSipParserListenerException {
    /*
     kevmo: 02.24.2006 bug fix - CSCef03455 It is the initialization
     sequence problem.  The origianl super() calling will eventually
     call down to the child and set child's private date member.
     super(value, offset, count);
    */
    this();
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
  public DsSipPrivacyHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Constructor used to set all known values.
   * @param isHeader if <code>true<\code>, set header privacy
   * @param isSession if <code>true<\code>, set session privacy
   * @param isUser if <code>true<\code>, set user privacy
   * @param isNone if <code>true<\code>, set no privacy
   * @param isCritical if <code>true<\code>, set the critical value
   * @param isId if <code>true<\code>, set the id value
   */
  public DsSipPrivacyHeader(
      boolean isHeader,
      boolean isSession,
      boolean isUser,
      boolean isNone,
      boolean isCritical,
      boolean isId) {
    super();

    m_header = isHeader;
    m_session = isSession;
    m_user = isUser;
    m_none = isNone;
    m_critical = isCritical;
    m_id = isId;
  }

  /**
   * Gets the LinkedList of DsByteStrings representing all of the extension tokens for this header.
   * This list will not contain any of the known privacy types, you must use the specialized methods
   * to get and set these values.
   *
   * @return a LinkedList of extension tokens, may return <code>null</code>
   */
  public LinkedList getExtensionTokens() {
    return m_tokens;
  }

  /**
   * Sets the entire list of extension tokens. It is up to the caller to ensure that this list does
   * not contain any of the known tokens.
   *
   * @param tokens the new list of extension tokens, <code>null</code> is OK
   */
  public void setExtensionToken(LinkedList tokens) {
    m_tokens = tokens;
  }

  /**
   * Adds the specified extension token to the list. It is up to the caller to ensure that this
   * token is not one of the known tokens.
   *
   * @param token the extension token to add to the list
   */
  public void addExtensionToken(DsByteString token) {
    if (token == null) {
      return;
    }

    if (m_tokens == null) {
      m_tokens = new LinkedList();
    }

    m_tokens.add(token);
  }

  /**
   * Removes the specified extension token to the list. It is up to the caller to ensure that this
   * token is not one of the known tokens.
   *
   * @param token the extension token to remove from the list
   * @return <code>true</code> if the token existed, else <code>false</code>
   */
  public boolean removeExtensionToken(DsByteString token) {
    if (m_tokens == null || token == null) {
      return false;
    }

    Iterator iter = m_tokens.iterator();
    while (iter.hasNext()) {
      if (token.equalsIgnoreCase((DsByteString) iter.next())) {
        iter.remove();
        return true;
      }
    }

    return false;
  }

  /**
   * Determine if an extension token is in the list of extension tokens. Only use this method for
   * extension tokens, not the known tokens.
   *
   * @param token the extension token to look for
   * @return <code>true</code> if the token exists in the list, else <code>false</code>
   */
  public boolean extensionTokenExists(DsByteString token) {
    if (m_tokens == null || token == null) {
      return false;
    }

    Iterator iter = m_tokens.iterator();
    while (iter.hasNext()) {
      if (token.equalsIgnoreCase((DsByteString) iter.next())) {
        return true;
      }
    }

    return false;
  }

  /**
   * Determines if the header privacy value is set.
   *
   * @return <code>true</code> if the header value is set, else <code>false</code>
   */
  public boolean isHeader() {
    return m_header;
  }

  /**
   * Sets the header privacy value to <code>flag</code>.
   *
   * @param flag determines if the header privacy value is set or not
   */
  public void setHeader(boolean flag) {
    m_header = flag;
  }

  /**
   * Determines if the session privacy value is set.
   *
   * @return <code>true</code> if the session value is set, else <code>false</code>
   */
  public boolean isSession() {
    return m_session;
  }

  /**
   * Sets the session privacy value to <code>flag</code>.
   *
   * @param flag determines if the session privacy value is set or not
   */
  public void setSession(boolean flag) {
    m_session = flag;
  }

  /**
   * Determines if the user privacy value is set.
   *
   * @return <code>true</code> if the user value is set, else <code>false</code>
   */
  public boolean isUser() {
    return m_user;
  }

  /**
   * Sets the user privacy value to <code>flag</code>.
   *
   * @param flag determines if the user privacy value is set or not
   */
  public void setUser(boolean flag) {
    m_user = flag;
  }

  /**
   * Determines if the none privacy value is set.
   *
   * @return <code>true</code> if the none value is set, else <code>false</code>
   */
  public boolean isNone() {
    return m_none;
  }

  /**
   * Sets the none privacy value to <code>flag</code>.
   *
   * @param flag determines if the none privacy value is set or not
   */
  public void setNone(boolean flag) {
    m_none = flag;
  }

  /**
   * Determines if the critical privacy value is set.
   *
   * @return <code>true</code> if the critical value is set, else <code>false</code>
   */
  public boolean isCritical() {
    return m_critical;
  }

  /**
   * Sets the critical privacy value to <code>flag</code>.
   *
   * @param flag determines if the critical privacy value is set or not
   */
  public void setCritical(boolean flag) {
    m_critical = flag;
  }

  /**
   * Determines if the id privacy value is set.
   *
   * @return <code>true</code> if the id value is set, else <code>false</code>
   */
  public boolean isId() {
    return m_id;
  }

  /**
   * Sets the id privacy value to <code>flag</code>.
   *
   * @param flag determines if the id privacy value is set or not
   */
  public void setId(boolean flag) {
    m_id = flag;
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
    return BS_PRIVACY_TOKEN;
  }

  /**
   * Serializes the value of this header to the specified <code>out</code> byte output stream.
   *
   * @param out the byte output stream where this header's value need to be serialized.
   * @throws IOException if there is an error while writing to the output stream
   */
  public void writeValue(OutputStream out) throws IOException {
    if (m_none) {
      BS_NONE.write(out);
    } else {
      boolean shouldWriteSemi = false;

      if (m_header) {
        BS_HEADER.write(out);
        shouldWriteSemi = true;
      }

      if (m_session) {
        writeSemi(out, shouldWriteSemi);
        BS_SESSION.write(out);
        shouldWriteSemi = true;
      }

      if (m_user) {
        writeSemi(out, shouldWriteSemi);
        BS_USER.write(out);
        shouldWriteSemi = true;
      }

      if (m_id) {
        writeSemi(out, shouldWriteSemi);
        BS_ID.write(out);
        shouldWriteSemi = true;
      }

      if (m_tokens != null) {
        Iterator iter = m_tokens.iterator();
        while (iter.hasNext()) {
          writeSemi(out, shouldWriteSemi);
          ((DsByteString) iter.next()).write(out);
          shouldWriteSemi = true;
        }
      }

      if (m_critical) {
        writeSemi(out, shouldWriteSemi);
        BS_CRITICAL.write(out);
      }
    }
  }

  /**
   * Writes a semicolon to the output stream if <code>shouldWriteSemi</code> is <code>true</code>,
   * else just returns.
   *
   * @param out the output stream to write to
   * @param shouldWriteSemi do nothing unless this is <code>true</code>
   * @throws IOException if there is an exception writing to the output stream
   */
  private void writeSemi(OutputStream out, boolean shouldWriteSemi) throws IOException {
    if (shouldWriteSemi) {
      out.write(B_SEMI);
    }
  }

  /**
   * Copy another header's members to me.
   *
   * @param header the header to copy.
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipPrivacyHeader source = (DsSipPrivacyHeader) header;

    m_header = source.m_header;
    m_session = source.m_session;
    m_user = source.m_user;
    m_none = source.m_none;
    m_critical = source.m_critical;
    m_id = source.m_id;

    if (source.m_tokens == null) {
      m_tokens = null;
    } else {
      m_tokens = new LinkedList();
      Iterator iter = m_tokens.iterator();
      while (iter.hasNext()) {
        m_tokens.add(iter.next());
      }
    }
  }

  /** This method makes a copy of the header. */
  public Object clone() {
    DsSipPrivacyHeader clone = (DsSipPrivacyHeader) super.clone();

    if (m_tokens != null) {
      clone.m_tokens = new LinkedList();
      Iterator iter = m_tokens.iterator();
      while (iter.hasNext()) {
        clone.m_tokens.add(iter.next());
      }
    }

    return clone;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return PRIVACY;
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
    DsSipPrivacyHeader header = null;
    try {
      header = (DsSipPrivacyHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }

    // note that if none == true and the nones are equals then these
    // headers will serialize the same, but equals() might return false.
    // In this case, at least one of the headers would be malformed

    if (m_header != header.m_header
        || m_user != header.m_user
        || m_session != header.m_session
        || m_none != header.m_none
        || m_critical != header.m_critical
        || m_id != header.m_id) {
      return false;
    }

    return compareLists(m_tokens, header.m_tokens);
  }

  private boolean compareLists(LinkedList a, LinkedList b) {
    int aSize = 0;
    int bSize = 0;

    if (a != null) {
      aSize = a.size();
    }

    if (b != null) {
      bSize = b.size();
    }

    if (aSize != bSize) {
      return false;
    }

    if (aSize == 0 && bSize == 0) {
      return true;
    }

    Iterator iter = a.iterator();
    while (iter.hasNext()) {
      if (!listContains(b, (DsByteString) iter.next())) {
        return false;
      }
    }

    return true;
  }

  private boolean listContains(LinkedList l, DsByteString s) {
    Iterator iter = l.iterator();
    while (iter.hasNext()) {
      if (((DsByteString) iter.next()).equalsIgnoreCase(s)) {
        return true;
      }
    }

    return false;
  }

  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    if (m_none) {
      md.getEncoding(BS_NONE).write(out);
    } else {
      boolean shouldWriteSemi = false;

      if (m_header) {
        md.getEncoding(BS_HEADER).write(out);
        shouldWriteSemi = true;
      }

      if (m_session) {
        writeSemi(out, shouldWriteSemi);
        md.getEncoding(BS_SESSION).write(out);
        shouldWriteSemi = true;
      }

      if (m_user) {
        writeSemi(out, shouldWriteSemi);
        md.getEncoding(BS_USER).write(out);
        shouldWriteSemi = true;
      }

      if (m_id) {
        writeSemi(out, shouldWriteSemi);
        md.getEncoding(BS_ID).write(out);
        shouldWriteSemi = true;
      }

      if (m_tokens != null) {
        Iterator iter = m_tokens.iterator();
        while (iter.hasNext()) {
          writeSemi(out, shouldWriteSemi);
          md.getEncoding(((DsByteString) iter.next())).write(out);
          shouldWriteSemi = true;
        }
      }

      if (m_critical) {
        writeSemi(out, shouldWriteSemi);
        md.getEncoding(BS_CRITICAL).write(out);
      }
    }
  }

  /**
   * Clears all the member data and made this header reusable. The various components (sub-elements)
   * of this headers can be set again. In this case, we reuse already constructed objects like (URI,
   * Name Address, Parameter Tables, Dates, etc), to avoid expensive object creation.
   */
  public void reInit() {
    super.reInit();
    m_header = false;
    m_session = false;
    m_user = false;
    m_none = false;
    m_critical = false;
    m_id = false;
    m_tokens = null;
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
    if (elementId == SINGLE_VALUE) {
      if (BS_ID.equalsIgnoreCase(buffer, offset, count)) {
        m_id = true;
      } else if (BS_CRITICAL.equalsIgnoreCase(buffer, offset, count)) {
        m_critical = true;
      } else if (BS_NONE.equalsIgnoreCase(buffer, offset, count)) {
        m_none = true;
      } else if (BS_SESSION.equalsIgnoreCase(buffer, offset, count)) {
        m_session = true;
      } else if (BS_HEADER.equalsIgnoreCase(buffer, offset, count)) {
        m_header = true;
      } else if (BS_USER.equalsIgnoreCase(buffer, offset, count)) {
        m_user = true;
      } else {
        if (m_tokens == null) {
          m_tokens = new LinkedList();
        }

        m_tokens.add(new DsByteString(buffer, offset, count));
      }
    } else {
      // exception?
      // log?
    }
  }
}
