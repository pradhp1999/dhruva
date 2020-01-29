// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.util.*;

/**
 * This class is the base class for Authorization and ProxyAuthorization classes. It provides some
 * common methods and declares some methods which should be implemented.
 */
public abstract class DsSipAuthorizationHeaderBase extends DsSipAuthHeader {
  /** Default constructor. */
  protected DsSipAuthorizationHeaderBase() {
    super();
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header
   * and constructs this header.<br>
   * The byte array <code>value</code> should be the value part (data after the colon ) of this
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
  protected DsSipAuthorizationHeaderBase(byte[] value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value, 0, value.length);
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header
   * and constructs this header.<br>
   * The byte array <code>value</code> should be the value part (data after the colon ) of this
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
  protected DsSipAuthorizationHeaderBase(byte[] value, int offset, int count)
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
   * The specified byte string <code>value</code> should be the value part (data after the colon )
   * of this header.<br>
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
  protected DsSipAuthorizationHeaderBase(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Constructs this header from the specified authentication type and the specified authentication
   * data (that is the credentials info. It concatenates the authentication type and the
   * authentication data parts into in single byte array and then tries to parse the various
   * components into this header.
   *
   * @param type the authentication type for this header. This can be either BASIC or DIGEST.
   * @param data the authentication data (that is the credentials info)
   * @throws DsSipParserException if there is an error while parsing the specified value into this
   *     header.
   * @throws DsSipParserListenerException if there is an error condition detected by this header as
   *     a Parser Listener, while parsing.
   */
  protected DsSipAuthorizationHeaderBase(DsByteString type, DsByteString data)
      throws DsSipParserException, DsSipParserListenerException {
    super(type, data);
  }

  /**
   * Constructs and returns the DsSipCredentialsInfo object from the credentials info present in
   * this header. If the authentication type is Digest, then DsSipDigestCredentialsInfo object is
   * returned, and if authentication type is Basic, then DsSipBasicCredentialsInfo object is
   * returned, otherwise null is returned.
   *
   * @return the DsSipCredentialsInfo object
   */
  public DsSipCredentialsInfo getCredentialsInfo() {
    DsSipCredentialsInfo info = null;
    if (m_strType.equalsIgnoreCase(BS_BASIC)) {
      info = new DsSipBasicCredentialsInfo(m_strData);
    } else if (m_strType.equalsIgnoreCase(BS_DIGEST)) {
      if (m_paramTable == null && null != m_strData) {
        DsSipDigestCredentialsInfo cr = new DsSipDigestCredentialsInfo();
        try {
          cr.parse(m_strData.data(), m_strData.offset(), m_strData.length());
        } catch (DsSipParserException pe) {
          // log?
        } catch (DsSipParserListenerException ple) {
          // log?
        }
        info = cr;
      } else {
        info = new DsSipDigestCredentialsInfo(m_paramTable);
      }
    }
    return info;
  }

  /**
   * Returns the credentials info value present in this header. This in turn call the {@link
   * DsSipAuthHeader#getData() getData()}.
   *
   * @return the credentials info value present in this header.
   */
  public DsByteString getCredentials() {
    return getData();
  }

  /**
   * Sets the credentials info value for this header. This in turn call the {@link
   * DsSipAuthHeader#setData(DsByteString) setData(DsByteString)}.
   *
   * @param credentials the credentials info value that needs to be set in this header.
   */
  public void setCredentials(DsByteString credentials) {
    setData(credentials);
  }

  /**
   * Retrieves the authentication scheme (Basic or Digest). This in turn call the {@link
   * DsSipAuthHeader#getType() getType()}.
   *
   * @return the authentication scheme (Basic or Digest).
   */
  public DsByteString getAuthenticationType() {
    return getType();
  }

  /**
   * Sets the authentication scheme (Basic or Digest). This in turn call the {@link
   * DsSipAuthHeader#setType(DsByteString) setType(DsByteString)}.
   *
   * @param type the authentication scheme that needs to be set. The supported schemes are Basic and
   *     Digest.
   */
  public void setAuthenticationType(DsByteString type) {
    setType(type);
  }
} // Ends class DsSipAuthorizationHeaderBase
