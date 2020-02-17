// Copyright (c) 2003-2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class represents the SIP App-Info header as specified in the draft
 * (draft-jennings-sip-app-info-01.txt). It provides methods to build, access, modify, serialize and
 * clone the App-Info header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 *  App-Info    = "App-Info" HCOLON app *(COMMA app)
 *  app         = [ display-name ] LAQUOT [absolute-uri] RAQUOT
 *                                          *(SEMI app-param)
 *  app-param   = app-id-param / app-name-param / generic-param
 *  app-id-param = "id" EQUAL app-id-value
 *  app-id-value = app-instance-id "!" app-class-id
 *  app-instance-id = app-token
 *  app-class-id = app-token
 *  app-token    = 1*(alphanum / "-" / "." / "%" / "*" / "_" / "+"
 *                  / "'" / "`" / "~" ) ; this is a token with no "!"
 *  app-name-param = "app-name" EQUAL gen-value
 *
 *  An example App-Info header field is:
 *
 *  App-Info: "Call Timer"
 *             <http://mediasvr.provider.net/calltimer.vxml>;
 *             id=app4323!sub4+svr56.provider.net
 *
 *  This indicates that the UA should fetch and execute the script found
 *  at http://mediasvr.provider.net/calltimer.vxml.
 * </pre> </code>
 */
public final class DsSipAppInfoHeader extends DsSipNameAddressHeader {
  /** Header ID. */
  public static final byte sID = APP_INFO;

  private static final DsByteString STR_ID = new DsByteString(";id=");
  private static final DsByteString STR_APPNAME = new DsByteString(";app-name=");

  /** Holds the cid value for this header. */
  protected DsByteString m_id;

  protected DsByteString m_appname;

  /** Default constructor. */
  public DsSipAppInfoHeader() {
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
  public DsSipAppInfoHeader(byte[] value)
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
  public DsSipAppInfoHeader(byte[] value, int offset, int count)
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
  public DsSipAppInfoHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Constructs this App-Info header with the specificed <code>nameAddress</code>.
   *
   * @param nameAddress the name address for this App-Info header.
   */
  public DsSipAppInfoHeader(DsSipNameAddress nameAddress) {
    super();
    this.m_nameAddress = nameAddress;
  }

  /**
   * Constructs this header with the specified <code>nameAddress</code> and the specified <code>
   * parameters</code>. The name address value is first parsed into a valid DsSipNameAddress.
   *
   * @param nameAddress the name address for this header.
   * @param parameters the list of parameters for this header.
   * @throws DsSipParserException if there is an error while parsing the nameAddress value
   */
  public DsSipAppInfoHeader(DsByteString nameAddress, DsParameters parameters)
      throws DsSipParserException {
    super(nameAddress, parameters);
  }

  /**
   * Constructs this header with the specified <code>nameAddress</code> and the specified <code>
   * parameters</code>.
   *
   * @param nameAddress the name address for this header.
   * @param parameters the list of parameters for this header.
   */
  public DsSipAppInfoHeader(DsSipNameAddress nameAddress, DsParameters parameters) {
    super(nameAddress, parameters);
  }

  /**
   * Constructs this App-Info header with the specified <code>uri</code>.
   *
   * @param uri the uri for this App-Info header.
   * @throws DsException does not throw.
   */
  public DsSipAppInfoHeader(DsURI uri) throws DsException {
    super();
    if (m_nameAddress == null) {
      m_nameAddress = new DsSipNameAddress();
    }
    m_nameAddress.setURI(uri);
  }

  /**
   * Returns the token which is the name of the header.
   *
   * @return the token value.
   */
  public DsByteString getToken() {
    return BS_APP_INFO;
  }

  /**
   * Returns the token which is the compact name of the header.
   *
   * @return the compact token name.
   */
  public DsByteString getCompactToken() {
    return BS_APP_INFO;
  }

  /**
   * Returns the header name plus ": " as a single byte string token. The header name will be in the
   * compact form if this header is set to be in compact form.
   *
   * @return the header name plus ": " as a single byte string token.
   */
  public final DsByteString getTokenC() {
    return BS_APP_INFO_TOKEN;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return APP_INFO;
  }

  /**
   * Copy another header's members to me.
   *
   * @param header the header to copy.
   */
  protected void copy(DsSipHeader header) {
    DsSipAppInfoHeader source = (DsSipAppInfoHeader) header;
    super.copy(header);
    this.m_id = source.m_id;
    this.m_appname = source.m_appname;
  }

  /**
   * Returns a deep copy of the header object and all of the other elements on the list that it is
   * associated with. NOTE: This behavior will change when the deprecated methods are removed and it
   * will just clone the single header.
   *
   * @return the cloned DsSipAppInfoHeader header object
   */
  public Object clone() {
    DsSipAppInfoHeader clone = (DsSipAppInfoHeader) super.clone();
    clone.m_id = ((m_id == null) ? null : m_id.copy());
    clone.m_appname = ((m_appname == null) ? null : m_appname.copy());
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
    if (!super.equals(obj)) {
      return false;
    }

    DsSipAppInfoHeader header = null;
    header = (DsSipAppInfoHeader) obj;

    if (DsByteString.compareIgnoreNull(m_id, header.getId()) != 0) {
      return false;
    }
    if (DsByteString.compareIgnoreNull(m_appname, header.getAppName()) != 0) {
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
    // Todo: write out display name
    if (m_nameAddress != null) {
      m_nameAddress.setBrackets(true);
      m_nameAddress.write(out);
    }
    if (m_id != null && m_id.length() > 0) {
      STR_ID.write(out);
      m_id.write(out);
    }
    if (m_appname != null && m_appname.length() > 0) {
      STR_APPNAME.write(out);
      m_appname.write(out);
    }
    if (m_paramTable != null) {
      m_paramTable.write(out);
    }
  }

  /**
   * Gets the id parameter value
   *
   * @return the id value
   */
  public DsByteString getId() {
    return m_id;
  }

  /**
   * Sets the id parameter value
   *
   * @param id the id value
   */
  public void setId(DsByteString id) {
    this.m_id = id;
  }

  /**
   * Gets the app-name parameter value
   *
   * @return the app-name value
   */
  public DsByteString getAppName() {
    return m_appname;
  }

  /**
   * Sets the app-name parameter value
   *
   * @param appname the app-name value
   */
  public void setAppName(DsByteString appname) {
    this.m_appname = appname;
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
    if (BS_ID.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      m_id = new DsByteString(buffer, valueOffset, valueCount);
    } else if (BS_APP_NAME.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      m_appname = new DsByteString(buffer, valueOffset, valueCount);
    } else {
      super.parameterFound(contextId, buffer, nameOffset, nameCount, valueOffset, valueCount);
    }
  }
}
