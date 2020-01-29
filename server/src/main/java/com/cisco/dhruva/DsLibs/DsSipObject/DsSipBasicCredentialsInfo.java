// Copyright (c) 2005-2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsUtil.Base64;

/**
 * This class represents Basic credentials information used in Basic Authentication within
 * authorization headers.
 */
public class DsSipBasicCredentialsInfo extends DsSipBasicChallengeInfo
    implements DsSipCredentialsInfo {
  private DsByteString m_strUser;
  private DsByteString m_strPassword;
  private DsByteString m_strHash;

  /** Construct this basic credentials info with user = null and password = null. */
  public DsSipBasicCredentialsInfo() {
    super();
  }

  /**
   * Constructs this basic credentials info with the specified <code>user</code> and the specified
   * <code>password</code>.
   *
   * @param user the user name for this credentials info.
   * @param password the user password for this credentials info.
   */
  public DsSipBasicCredentialsInfo(DsByteString user, DsByteString password) {
    super();
    m_strUser = user;
    m_strPassword = password;
  }

  /**
   * Constructs this basic credentials info with the specified <code>hash</code> value. The
   * specified <code>hash</code> should be a base-64 encoded string of the user name and the user
   * password in the format (user:password).
   *
   * @param hash the base-64 encoded value of the user name and user password
   */
  public DsSipBasicCredentialsInfo(DsByteString hash) {
    super();

    byte[] decodedBytes =
        Base64.decode(hash.data(), hash.offset(), hash.length(), Base64.NO_OPTIONS);

    DsByteString userpass = new DsByteString(decodedBytes);

    int index = userpass.indexOf(':');
    if (index == -1) {
      index = userpass.length();
    }

    m_strUser = userpass.substring(0, index);
    if (index < userpass.length()) {
      ++index;
      m_strPassword = userpass.substring(index);
    }
  }

  // public static void main(String[] args)
  // {
  //    DsSipBasicCredentialsInfo encode = new DsSipBasicCredentialsInfo(new DsByteString("user"),
  // new DsByteString("pass"));
  //    DsByteString hash = encode.generateCredentials(null);
  //    DsSipBasicCredentialsInfo decode = new DsSipBasicCredentialsInfo(hash);
  //
  //    System.out.println("User = [" + decode.m_strUser + "]");
  //    System.out.println("Pass = [" + decode.m_strPassword + "]");
  // }

  /**
   * Generates and returns a credentials info string as per this credentials info object for the
   * specified <code>request</code>. In this case, the specified request is not used for credentials
   * generation as all the required information (user name and user password) is assumed to be
   * present in this credentials info object. The returned credentials info string can be used in an
   * authorization header to embed the credentials in a request.
   *
   * @param request the sip request
   * @return the basic credentials info string as per this credentials info object.
   */
  public DsByteString generateCredentials(DsSipRequest request) {
    return generateHash();
  }

  /**
   * Validates the specified request as per this credentials info. The user credentials present in
   * the authorization header of the specified request are retrieved and validated against this
   * credentials info. The credentials can be either VALID or INVALID and is denoted by the return
   * value. Where the return values are: <br>
   * VALID - if the credentials are valid <br>
   * INVALID - if the credentials are not valid <br>
   *
   * @param request the request that needs to be validated against this credentials info
   * @return a numeric value specifying whether the request is VALID or INVALID
   */
  public short validate(DsSipRequest request) {
    DsSipAuthorizationHeaderBase header =
        (DsSipAuthorizationHeaderBase) request.getAuthenticationHeader();
    return validate(request, header);
  }

  /**
   * Validates the specified request as per this credentials info. The user credentials present in
   * the specified authorization header <code>header</code> are validated against this credentials
   * info. The credentials can be either VALID or INVALID and is denoted by the return value. Where
   * the return values are: <br>
   * VALID - if the credentials are valid <br>
   * INVALID - if the credentials are not valid <br>
   *
   * @param request the request that needs to be validated against this credentials info
   * @param header the authorization header that needs to be validated against this credentials
   *     info.
   * @return a numeric value specifying whether the request is VALID or INVALID
   */
  public short validate(DsSipRequest request, DsSipAuthorizationHeaderBase header) {
    if (header != null) {
      DsSipBasicCredentialsInfo info = null;
      try {
        info = (DsSipBasicCredentialsInfo) header.getCredentialsInfo();
      } catch (ClassCastException cce) {
        return INVALID;
      }
      if (info == null) {
        return INVALID;
      }
      DsByteString hash1 = generateHash();
      DsByteString hash2 = info.generateHash();
      if (hash1.equals(hash2)) {
        return VALID;
      }
    }
    return INVALID;
  }

  /**
   * Returns the user name in this credentials info object.
   *
   * @return the user name in this credentials info object.
   */
  public DsByteString getUser() {
    return m_strUser;
  }

  /**
   * Sets the user name for this credentials info object.
   *
   * @param user the new user name for this credentials info object.
   */
  public void setUser(DsByteString user) {
    m_strUser = user;
  }

  /**
   * Returns the user password in this credentials info object.
   *
   * @return the user password in this credentials info object.
   */
  public DsByteString getPassword() {
    return m_strPassword;
  }

  /**
   * Sets the user password for this credentials info object.
   *
   * @param password the new user password for this credentials info object.
   */
  public void setPassword(DsByteString password) {
    m_strPassword = password;
  }

  /**
   * Clears all the member data and made this object reusable. The various components (sub-elements)
   * of this object can be set again.
   */
  public void reInit() {
    super.reInit();
    m_strUser = null;
    m_strPassword = null;
    m_strHash = null;
  }

  /** Generates and returns the hash value. */
  private DsByteString generateHash() {
    if (m_strHash != null) {
      return m_strHash;
    }

    int len = (m_strUser != null) ? m_strUser.length() : 0;
    len += (m_strPassword != null) ? m_strPassword.length() : 0;
    len++; // for ':'
    byte[] bytes = new byte[len];
    len = 0;
    if (m_strUser != null) {
      len = m_strUser.length();
      m_strUser.appendTo(bytes, 0);
    }
    bytes[len] = (byte) ':';
    if (m_strPassword != null) {
      len++;
      m_strPassword.appendTo(bytes, len);
    }

    String str = Base64.encodeBytes(bytes);
    m_strHash = new DsByteString(str);

    return m_strHash;
  }
}
