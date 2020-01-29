// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.util.*;

/**
 * This class is the base class for WWWAuthenticate and ProxyAuthenticate classes. It provides some
 * common methods and declares some methods which should be implemented.
 */
public abstract class DsSipAuthenticateHeaderBase extends DsSipAuthHeader {
  /** Default constructor. */
  protected DsSipAuthenticateHeaderBase() {
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
  protected DsSipAuthenticateHeaderBase(byte[] value)
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
  protected DsSipAuthenticateHeaderBase(byte[] value, int offset, int count)
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
  protected DsSipAuthenticateHeaderBase(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Constructs this header from the specified authentication type and the specified authentication
   * data (that is the challenge info). It concatenates the authentication type and the
   * authentication data parts into in single byte array and then tries to parse the various
   * components into this header.
   *
   * @param type the authentication type for this header. This can be either BASIC or DIGEST.
   * @param data the authentication data (that is the challenge info).
   * @throws DsSipParserException if there is an error while parsing the specified value into this
   *     header.
   * @throws DsSipParserListenerException if there is an error condition detected by this header as
   *     a Parser Listener, while parsing.
   */
  protected DsSipAuthenticateHeaderBase(DsByteString type, DsByteString data)
      throws DsSipParserException, DsSipParserListenerException {
    super(type, data);
  }

  /**
   * Sets the challenge info value for this header. This in turn call the {@link
   * DsSipAuthHeader#setData(DsByteString) setData(DsByteString)}.
   *
   * @param data the challenge info value that needs to be set in this header.
   */
  public void setChallenge(DsByteString data) {
    setData(data);
  }

  /**
   * Returns the challenge info value present in this header. This in turn call the {@link
   * DsSipAuthHeader#getData() getData()}.
   *
   * @return the challenge info value present in this header.
   */
  public DsByteString getChallenge() {
    return getData();
  }

  /**
   * Sets the authentication scheme (Basic or Digest). This in turn call the {@link
   * DsSipAuthHeader#setType(DsByteString) setType(DsByteString)}.
   *
   * @param type the authentication scheme that needs to be set. The supported schemes are Basic and
   *     Digest.
   */
  public void setChallengeType(DsByteString type) {
    setType(type);
  }

  /**
   * Retrieves the authentication scheme (Basic or Digest). This in turn call the {@link
   * DsSipAuthHeader#getType() getType()}.
   *
   * @return the authentication scheme (Basic or Digest).
   */
  public DsByteString getChallengeType() {
    return getType();
  }

  /**
   * Constructs and returns the DsSipChallengeInfo object from the challenge info present in this
   * header. If the challenge type is Digest, then DsSipDigestChallengeInfo object is returned, and
   * if challenge type is Basic, then DsSipBasicChallengeInfo object is returned, otherwise null is
   * returned.
   *
   * @return the DsSipChallengeInfo object.
   */
  public DsSipChallengeInfo getChallengeInfo() {
    DsSipChallengeInfo info = null;
    if (m_strType.equalsIgnoreCase(BS_BASIC)) {
      info = new DsSipBasicChallengeInfo();
      if (null != m_paramTable) {
        info.setRealm(m_paramTable.get(BS_REALM));
      } else if (null != m_strData) {
        try {
          info.parse(m_strData.data(), m_strData.offset(), m_strData.length());
        } catch (DsSipParserException pe) {
          // log?
        } catch (DsSipParserListenerException ple) {
          // log?
        }
      }

    } else if (m_strType.equalsIgnoreCase(BS_DIGEST)) {
      if (m_paramTable == null && null != m_strData) {
        info = new DsSipDigestChallengeInfo();
        try {
          info.parse(m_strData.data(), m_strData.offset(), m_strData.length());
        } catch (DsSipParserException pe) {
          // log?
        } catch (DsSipParserListenerException ple) {
          // log?
        }
      } else {
        info = new DsSipDigestChallengeInfo(m_paramTable);
      }
    }
    return info;
  }

  /**
   * Creates the corresponding Authorization header object with the specified type and the specified
   * credentials info <code>data</code>. The credentials info is not parsed here and is set to the
   * corresponding Authorization header as it is. This method should be used only if we know that
   * the specified credentials info is correct and we are not going to access the various parameters
   * separately from the returned Authorization header.
   *
   * @param type the type of the credentials (BASIC or DIGEST).
   * @param data the credentials info.
   * @return the created authorization header.
   */
  public abstract DsSipAuthorizationHeaderBase createAuthorization(
      DsByteString type, DsByteString data);
} // Ends class DsSipAuthenticateHeaderBase
