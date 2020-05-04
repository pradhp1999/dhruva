// Copyright (c) 2005-2010 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipMsgParser;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsHexEncoding;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import java.io.IOException;
import java.security.MessageDigest;
import org.slf4j.event.Level;

/**
 * This class represents the credentials information used in "digest" Authentication within the
 * authorization headers.
 *
 * <p>This class provides the mechanism to validate the credentials info for the "digest"
 * authentication scheme.
 *
 * <p>The credentials can be either <tt>VALID, INVALID, STALE or MAX_TRY_OUT</tt> that are denoted
 * by the numeric value.
 *
 * <p>Where : <br>
 * VALID - if the credentials are valid <br>
 * INVALID - if the credentials are not valid <br>
 * STALE - if the nonce value present in the credentials is stale. In other words, this nonce value
 * is no longer valid and new nonce value should be used. Also this means the user credentials may
 * be valid and this way provides the client the opportunity not to prompt the user for user and
 * password information.<br>
 * MAX_TRY_OUT - if the client has already tried for the maximum number of times that this server
 * was willing to tolerate. The server might respond with 500 class response to this request and
 * possibly raise an alarm or notification to the server administrator for possible replay attacks.
 * <br>
 *
 * <p>The parameters 'username', 'uri' and 'response' are stored as local member variables in this
 * class and are unquoted values. But these parameter values are serialized as quoted values. When
 * user queries any of these parameter value by invoking {@link
 * DsSipDigestCredentialsInfo#getParameter(DsByteString) getParameter(DsByteString)}, it will return
 * unquoted value for the corresponding parameter. While setting any of these parameter values by
 * invoking the {@link DsSipDigestCredentialsInfo#setParameter(DsByteString, DsByteString) *
 * setParameter(DsByteString, DsByteString) } method, it will try to unquote the passed parameter
 * value if its quoted.
 */
public class DsSipDigestCredentialsInfo extends DsSipDigestChallengeInfo
    implements DsSipCredentialsInfo {
  private static final byte[] emptyStrHash = {
    (byte) 'd', (byte) '4', (byte) '1', (byte) 'd',
    (byte) '8', (byte) 'c', (byte) 'd', (byte) '9',
    (byte) '8', (byte) 'f', (byte) '0', (byte) '0',
    (byte) 'b', (byte) '2', (byte) '0', (byte) '4',
    (byte) 'e', (byte) '9', (byte) '8', (byte) '0',
    (byte) '0', (byte) '9', (byte) '9', (byte) '8',
    (byte) 'e', (byte) 'c', (byte) 'f', (byte) '8',
    (byte) '4', (byte) '2', (byte) '7', (byte) 'e',
  };

  private DsByteString m_strA1Hash;
  private DsByteString m_strUser;
  private DsByteString m_strPassword;
  private DsByteString m_strURI;
  private DsByteString m_strResponse;
  private long m_nonceCount = 0;
  private byte[] m_baCNonce;

  /** Constructs this credentials info with the default values. */
  protected DsSipDigestCredentialsInfo() {
    super();
  }

  /**
   * Constructs this digest credentials info with the specified <code>realm</code>, <code>username
   * </code> and the specified <code>password</code>. The specified <code>realm</code> and the
   * <code>username</code> values will be unquoted.
   *
   * @param realm the realm for this credentials info
   * @param username the username for this credentials info
   * @param password the password for this credentials info
   */
  public DsSipDigestCredentialsInfo(
      DsByteString realm, DsByteString username, DsByteString password) {
    this(realm, username, password, null);
  }

  /**
   * Constructs this digest credentials info with the specified <code>realm</code>, <code>username
   * </code>, <code>password</code> and the <code>algorithm</code>. The specified <code>realm</code>
   * , <code>username</code> and <code>algorithm</code> values will be unquoted.
   *
   * @param realm the realm for this credentials info
   * @param username the username for this credentials info
   * @param password the password for this credentials info
   * @param algorithm the algorithm used to generate the hash
   */
  public DsSipDigestCredentialsInfo(
      DsByteString realm, DsByteString username, DsByteString password, DsByteString algorithm) {
    super(realm);
    setUser(username);
    m_strPassword = password;
    setAlgorithm(algorithm);
  }

  /**
   * Constructs this digest credentials info with the specified <code>realm</code> and the specified
   * <code>hashA1</code>. The specified <code>hashA1</code> should be an hex-encoded value of the
   * hash computation of the "user:realm:password". The specified <code>realm</code> will be
   * unquoted.
   *
   * @param realm the realm for this credentials info
   * @param hashA1 the hex encoded hash value for the A1("user:realm:password")
   */
  public DsSipDigestCredentialsInfo(DsByteString realm, DsByteString hashA1) {
    super(realm);
    m_strA1Hash = hashA1;
  }

  /**
   * Constructs this credentials info with the specified parameters. If the parameters contain
   * 'realm', 'nonce', 'algorithm', 'username', 'uri' or 'response' then unquotes (if these
   * parameter values are quoted) these parameter values and set to the corresponding local
   * variables.
   *
   * @param params the list of parameters that need to be set for this challenge info.
   */
  public DsSipDigestCredentialsInfo(DsParameters params) {
    super();
    setParameters(params);
  }

  /**
   * Constructs this credentials info and parses the name-value pairs from the specified byte array
   * into this object.
   *
   * @param value the input byte buffer that need to be parsed into this credentials info object
   * @param offset the offset of the parameters in the specified buffer
   * @param count the number of bytes to be looked starting from the offset in the specified buffer
   *     for parsing.
   * @throws DsSipParserException if there is an error while parsing the parameters
   * @throws DsSipParserListenerException if this object, as parser listener, found some problem
   *     while parsing the parameters.
   */
  public DsSipDigestCredentialsInfo(byte[] value, int offset, int count)
      throws DsSipParserException, DsSipParserListenerException {
    super();
    DsSipMsgParser.parseParameters(this, 0, value, offset, count);
  }

  /**
   * Constructs this credentials info and parses the name-value pairs from the specified byte array
   * into this object.
   *
   * @param value the input byte buffer that need to be parsed into this credentials info object
   * @throws DsSipParserException if there is an error while parsing the parameters
   * @throws DsSipParserListenerException if this object, as parser listener, found some problem
   *     while parsing the parameters.
   */
  public DsSipDigestCredentialsInfo(byte[] value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value, 0, value.length);
  }

  /**
   * Constructs this credentials info and parses the name-value pairs from the specified String
   * value into this object.
   *
   * @param value the input String value that need to be parsed into this credentials info object
   * @throws DsSipParserException if there is an error while parsing the parameters
   * @throws DsSipParserListenerException if this object, as parser listener, found some problem
   *     while parsing the parameters.
   */
  public DsSipDigestCredentialsInfo(String value)
      throws DsSipParserException, DsSipParserListenerException {
    this(DsByteString.getBytes(value));
  }

  /**
   * Generates and returns the credentials info string as per this challenge info object for the
   * specified <code>request</code>. The returned challenge info string can be used in an
   * authorization header to submit credentials for a request.
   *
   * @param request the sip request
   * @return the credentials string as per this credentials info object.
   */
  public DsByteString generateCredentials(DsSipRequest request) {
    return new DsByteString(toByteArray(request));
  }

  /**
   * Generates and returns the credentials info byte array as per this challenge info object for the
   * specified <code>request</code>. The returned challenge info byte array can be used in an
   * authorization header to submit credentials for a request.
   *
   * @param request the sip request.
   * @return the credentials byte array as per this credentials info object.
   */
  private byte[] toByteArray(DsSipRequest request) {
    ByteBuffer buffer = ByteBuffer.newInstance(100);
    DsByteString data = null;
    try {
      // append username
      BS_USERNAME.write(buffer);
      buffer.write(B_EQUAL);
      buffer.write(B_QUOTE);
      if (m_strUser != null) {
        m_strUser.write(buffer);
      }
      buffer.write(B_QUOTE);
      buffer.write(B_COMMA);

      // append realm
      BS_REALM.write(buffer);
      buffer.write(B_EQUAL);
      buffer.write(B_QUOTE);
      if (m_strRealm != null) {
        m_strRealm.write(buffer);
      }
      buffer.write(B_QUOTE);
      buffer.write(B_COMMA);

      // append nonce
      BS_NONCE.write(buffer);
      buffer.write(B_EQUAL);
      buffer.write(B_QUOTE);
      if (m_baNonce != null) {
        buffer.write(m_baNonce, 0, m_baNonce.length);
      }
      buffer.write(B_QUOTE);

      // append qop
      if (m_strQOP != null) {
        buffer.write(B_COMMA);
        BS_QOP.write(buffer);
        buffer.write(B_EQUAL);
        m_strQOP.write(buffer);

        // append nonce-count
        buffer.write(B_COMMA);
        BS_NC.write(buffer);
        buffer.write(B_EQUAL);
        if (request != null) {
          m_nonceCount++;
        }
        buffer.write(getNc());

        // check for cnonce
        if (null == m_baCNonce) {
          m_baCNonce = generateCNonce();
        }
        buffer.write(B_COMMA);
        BS_CNONCE.write(buffer);
        buffer.write(B_EQUAL);
        buffer.write(B_QUOTE);
        buffer.write(m_baCNonce, 0, m_baCNonce.length);
        buffer.write(B_QUOTE);
      }

      // append response
      buffer.write(B_COMMA);
      BS_RESPONSE.write(buffer);
      buffer.write(B_EQUAL);
      buffer.write(B_QUOTE);
      data = (request != null) ? getRequestDigest(request) : m_strResponse;
      if (data != null) {
        data.write(buffer);
      }
      buffer.write(B_QUOTE);

      // append digest-uri
      buffer.write(B_COMMA);
      BS_URI.write(buffer);
      buffer.write(B_EQUAL);
      buffer.write(B_QUOTE);
      data =
          (m_strURI != null)
              ? m_strURI
              : ((request != null) ? request.getURI().toByteString() : null);

      if (data != null) {
        data.write(buffer);
      }
      buffer.write(B_QUOTE);

      // append parameters
      if (m_paramTable != null) {
        m_paramTable.startWithDelimiter(true);
        m_paramTable.setDelimiter(B_COMMA);
        m_paramTable.write(buffer);
      }
    } catch (IOException ioe) {
      // We know we won't get IOException here
    }

    // GOGONG - 12.09.05 CSCsc71452 - added debugging message to detect the created credential
    // content
    if (DsLog4j.authCat.isDebugEnabled()) {
      DsLog4j.authCat.debug("Created credential info-[" + buffer.toString() + "]");
    }

    return buffer.toByteArray();
  }

  /**
   * Method used to create the request digest.
   *
   * @param request the request message.
   * @return the request digest.
   */
  private DsByteString getRequestDigest(DsSipRequest request) {
    DsByteString str = null;
    byte[] bytes = null;
    ByteBuffer buff = new ByteBuffer(100);

    try {
      // if the user specified the hash value
      if (m_strA1Hash != null) {
        buff.write(m_strA1Hash);
      } else {
        bytes = generateA1();
        if (bytes != null) buff.write(bytes);
      }
      buff.write(B_COLON);
      if (m_baNonce != null) buff.write(m_baNonce);
      buff.write(B_COLON);
      DsByteString value = null;
      if (m_strQOP != null) {
        if (m_strQOP.equalsIgnoreCase(BS_AUTH) || m_strQOP.equalsIgnoreCase(BS_AUTH_INT)) {
          value = getNc();
          if (value != null) {
            buff.write(value);
          }
          buff.write(B_COLON);
          if (m_baCNonce != null) {
            buff.write(m_baCNonce);
          }
          buff.write(B_COLON);
          buff.write(m_strQOP);
          buff.write(B_COLON);
        }
      }

      bytes = generateA2(request);

      if (bytes != null) buff.write(bytes);

      if (DsLog4j.authCat.isDebugEnabled()) {
        DsLog4j.authCat.debug("Request-Digest (before digesting)-[" + buff.toString() + "]");
      }

      MessageDigest digest = getMessageDigest();
      if (digest != null) {
        bytes = digest.digest(buff.toByteArray());
      }

      str = new DsByteString(DsHexEncoding.toHexByteString(bytes));

      if (DsLog4j.authCat.isDebugEnabled()) {
        DsLog4j.authCat.debug("Request-Digest-[" + str.toString() + "]");
      }
    } finally {
      try {
        buff.close();
      } catch (Exception e) {
        // ignore
      }
    }
    return str;
  }

  /**
   * Generates the first part (A1) of the request digest. Returns the Hex-Encoded value of the hash
   * computation of the "user:realm:password".<br>
   * If there is an algorithm parameter present in this credentials info then that algorithm will be
   * used for generating the hash, otherwise default MD5 hash algorithm will be used.
   *
   * @return the hex-encoded value of the hash value of the "user:realm:password".
   */
  public byte[] generateA1() {
    ByteBuffer buffer = ByteBuffer.newInstance();

    // append user name
    if (m_strUser != null) {
      buffer.write(m_strUser);
    }
    buffer.write(B_COLON);
    // append Realm
    if (m_strRealm != null) {
      buffer.write(m_strRealm);
    }
    buffer.write(B_COLON);
    // append Password
    if (m_strPassword != null) {
      buffer.write(m_strPassword);
    }

    byte[] bytes = null;
    byte[] udBytes = buffer.toByteArray();

    MessageDigest digest = getMessageDigest();
    if (digest != null) {
      bytes = digest.digest(udBytes);
    }

    // If MD5-sess
    if (isMD5Session()) {
      // http://skrb.org/ietf/http_errata.html#md5sess_sample
      bytes = DsHexEncoding.toHexBytes(bytes);
      buffer = ByteBuffer.newInstance();
      buffer.write(bytes);
      buffer.write(B_COLON);
      // append nonce
      buffer.write(m_baNonce);
      buffer.write(B_COLON);
      // check for cnonce
      if (null == m_baCNonce) {
        m_baCNonce = generateCNonce();
      }
      // append cnonce
      buffer.write(m_baCNonce);

      udBytes = buffer.toByteArray();
      if (digest != null) {
        bytes = digest.digest(udBytes);
      }
    }

    bytes = DsHexEncoding.toHexBytes(bytes);
    if (DsLog4j.authCat.isDebugEnabled()) {
      DsLog4j.authCat.debug("A1-[" + DsByteString.newString(udBytes) + "]");
      DsLog4j.authCat.debug("Hex-Encoded H(A1)-[" + DsByteString.newString(bytes) + "]");
    }
    return bytes;
  }

  /**
   * Generates the second part (A2) of the request digest. The value is an hash computation of the
   * "method :digest_URI" and the body of the message in case the Qop type is 'auth-int' If there is
   * an algorithm parameter present in this credentials info then that algorithm will be used for
   * generating the hash, otherwise default MD5 hash algorithm will be used.
   *
   * @param request a SIP request message
   * @return the hash value for the "method :digest_URI"
   */
  byte[] generateA2(DsSipRequest request) {
    ByteBuffer buffer = ByteBuffer.newInstance();

    // Request Method
    DsByteString data = request.getMethod();
    if (data != null) {
      buffer.write(data);
    }
    buffer.write(B_COLON);

    // Request URI
    data =
        (m_strURI != null)
            ? m_strURI
            : ((request != null) ? request.getURI().toByteString() : null);
    if (data != null) {
      buffer.write(data);
    }

    MessageDigest digest = getMessageDigest();

    // If QOP == auth-int then append request body
    if (m_strQOP != null) {
      if (m_strQOP.equalsIgnoreCase(BS_AUTH_INT)) {
        data = request.getBody();
        // Append request body if not null
        if (data == null) {
          buffer.write(B_COLON);
          buffer.write(emptyStrHash);
          if (DsLog4j.authCat.isDebugEnabled()) {
            DsLog4j.authCat.debug(
                "Empty body: H(Entity-Body)-[" + DsByteString.newString(emptyStrHash) + "]");
          }
        } else {
          byte[] body = data.toByteArray();
          if (digest != null) {
            body = digest.digest(body);
            body = DsHexEncoding.toHexBytes(body);
          }
          if (DsLog4j.authCat.isDebugEnabled()) {
            DsLog4j.authCat.debug("H(Entity-Body)-[" + DsByteString.newString(body) + "]");
          }
          buffer.write(B_COLON);
          buffer.write(body);
        }
      }
    }

    byte[] bytes = buffer.toByteArray();

    if (DsLog4j.authCat.isDebugEnabled()) {
      DsLog4j.authCat.debug("A2-[" + DsByteString.newString(bytes) + "]");
    }

    if (digest != null) {
      bytes = digest.digest(bytes);
    }
    bytes = DsHexEncoding.toHexBytes(bytes);
    if (DsLog4j.authCat.isDebugEnabled()) {
      DsLog4j.authCat.debug("Hex-Encoded H(A2)-[" + DsByteString.newString(bytes) + "]");
    }
    return bytes;
  }

  /**
   * Validates the specified request as per this credentials info. The user credentials present in
   * the authorization header of the specified request are retrieved and validated against this
   * credentials info. The credentials can be either VALID, INVALID, STALE or MAX_TRY_OUT and is
   * denoted by the return value. Where the return values are: <br>
   * VALID - if the credentials are valid <br>
   * INVALID - if the credentials are not valid <br>
   * STALE - if the nonce value present in the credentials is stale. In other words, this nonce
   * value is no longer valid and new nonce value should be used. Also this means that the user
   * credentials may be valid but the time for what the nonce value was valid has expired. So in
   * this case, client can just resend the request with these credentials itself and may not need to
   * prompt the user to enter his/her user and password information.<br>
   * MAX_TRY_OUT - if the client has already tried for the maximum number of times that this server
   * was willing to tolerate. The server might response with 500 class response to this request and
   * possibly raise an alarm or notification to the server administrator for possible replay
   * attacks.<br>
   *
   * @param request the request that needs to be validated against this credentials info
   * @return a numeric value specifying whether the request is VALID, INVALID, STALE or MAX_TRY_OUT.
   */
  public short validate(DsSipRequest request) {
    // get the authorization header in the request
    DsSipAuthorizationHeaderBase header =
        (DsSipAuthorizationHeaderBase) request.getAuthenticationHeader();
    return validate(request, header);
  }

  /**
   * Validates the specified request as per this credentials info. The user credentials present in
   * the specified authorization header <code>header</code> are validated against this credentials
   * info. The credentials can be either VALID, INVALID, STALE or MAX_TRY_OUT and is denoted by the
   * return value. Where the return values are: <br>
   * VALID - if the credentials are valid <br>
   * INVALID - if the credentials are not valid <br>
   * STALE - if the nonce value present in the credentials is stale. In other words, this nonce
   * value is no longer valid and new nonce value should be used. Also this means that the user
   * credentials may be valid but the time for what the nonce value was valid has expired. So in
   * this case, client can just resend the request with these credentials itself and may not need to
   * prompt the user to enter his/her user and password information.<br>
   * MAX_TRY_OUT - if the client has already tried for the maximum number of times that this server
   * was willing to tolerate. The server might response with 500 class response to this request and
   * possibly raise an alarm or notification to the server administrator for possible replay
   * attacks.<br>
   * The values STALE and MAX_TRY_OUT can be returned only in case of Digest Authentication scheme.
   *
   * @param request the request that needs to be validated against this credentials info
   * @param header the authorization header that needs to be validated against this credentials
   *     info.
   * @return a numeric value specifying whether the request is VALID, INVALID, STALE or MAX_TRY_OUT.
   */
  public short validate(DsSipRequest request, DsSipAuthorizationHeaderBase header) {
    if (header == null) {
      if (DsLog4j.authCat.isEnabled(Level.INFO)) {
        DsLog4j.authCat.info("The Authorization header is not present in the Request");
      }
      return INVALID;
    }
    DsSipDigestCredentialsInfo ci = null;
    try {
      // Retrieve the credentials info provided in the authorization header
      ci = (DsSipDigestCredentialsInfo) header.getCredentialsInfo();
    } catch (ClassCastException cce) {
      if (DsLog4j.authCat.isEnabled(Level.INFO)) {
        DsLog4j.authCat.info("The Authentication scheme is not Digest");
      }
      return INVALID;
    }

    // get the "algorithm" from the request and set to this credentials info
    // in order to compute the request-digest, that needs to be compared with
    // the client's credentials info.
    DsByteString alg = ci.getAlgorithm();
    if (alg != null) {
      setAlgorithm(alg);
    }

    byte[] inonce = ci.getNonce();
    short res = INVALID;
    if ((res = validateNonce(inonce, request)) != VALID) {
      return res;
    }
    // now we know that the nonce value is valid and we should assign the
    // incoming nonce value to this nonce value as it will be used in
    // request-digest generation.
    m_baNonce = inonce;

    // get the digest "uri" from the request and set to this credentials info
    // in order to compute the request-digest, that needs to be compared with
    // the client's credentials info
    DsByteString uri = ci.getUri();
    if (uri != null) {
      m_strURI = uri;
    }

    // get the "qop" parameter from the request and set to this credentials info
    // in order to compute the request-digest, that needs to be compared with
    // the client's credentials info
    DsByteString qop = ci.getParameter(BS_QOP);
    if (qop != null) {
      setQop(qop);
    }

    // get the "cnonce" parameter from the request and set to this credentials info
    // in order to compute the request-digest, that needs to be compared with
    // the client's credentials info
    DsByteString cn = ci.getCnonce();
    if (cn != null) {
      setCnonce(cn);
    }

    // get the "nc" parameter from the request and set to this credentials info
    // in order to compute the request-digest, that needs to be compared with
    // the client's credentials info
    DsByteString nc = ci.getNc();
    if (nc != null) {
      setNc(nc);
    }

    // Retrieve the request digest, generated by the client, from the response
    // property of the challenge info and check against this request digest
    // to validate for the correct user name and password.
    DsByteString response = ci.getResponse();
    if (response == null) {
      return INVALID;
    }
    DsByteString genRequest = getRequestDigest(request);

    response = response.unquoted();

    if (DsLog4j.authCat.isDebugEnabled()) {
      DsLog4j.authCat.debug("[" + response + "]-[" + genRequest + "]");
    }

    if (response.equalsIgnoreCase(genRequest)) {
      return VALID;
    }

    return INVALID;
  }

  /**
   * Retrieves the hash value of the username, realm and password, if set.
   *
   * @return the hash value of the username, realm and password.
   */
  public DsByteString getDigest_A1_Hashed() {
    return m_strA1Hash;
  }

  /**
   * Sets the hash value of the username, realm and password.
   *
   * @param hashA1 the hash value of the username, realm and password.
   */
  public void setDigest_A1_Hashed(DsByteString hashA1) {
    m_strA1Hash = hashA1;
  }

  /**
   * Returns the user name for this credentials info.
   *
   * @return the user name for this credentials info
   */
  public DsByteString getUser() {
    return m_strUser;
  }

  /**
   * Sets the user name for this credentials info.
   *
   * @param user the new user name for this credentials info
   */
  public void setUser(DsByteString user) {
    if (user == null) {
      m_strUser = null;
    } else {
      m_strUser = user.unquoted();
    }
  }

  /**
   * Returns the user password for this credentials info.
   *
   * @return the user password for this credentials info
   */
  public DsByteString getPassword() {
    return m_strPassword;
  }

  /**
   * Sets the user password for this credentials info.
   *
   * @param password the new user password for this credentials info
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
    m_strA1Hash = null;
  }

  /**
   * Tells whether this object contains a parameter with the specified parameter <code>name</code>.
   *
   * @param name the name of the parameter that needs to be checked
   * @return <code>true</code> if a parameter with the specified name is present, <code>false</code>
   *     otherwise.
   */
  public boolean hasParameter(DsByteString name) {
    if (name.equalsIgnoreCase(BS_URI)) {
      return (m_strURI != null);
    }
    if (name.equalsIgnoreCase(BS_RESPONSE)) {
      return (m_strResponse != null);
    }
    if (name.equalsIgnoreCase(BS_USERNAME)) {
      return (m_strUser != null);
    }
    if (name.equalsIgnoreCase(BS_NC)) {
      return (m_nonceCount > 0);
    }
    if (name.equalsIgnoreCase(BS_CNONCE)) {
      return (m_baCNonce != null);
    }
    return super.hasParameter(name);
  }

  /**
   * Returns the parameter value for the parameter with the specified <code>name</code>, if present,
   * otherwise returns null. <b>Note:</b> The parameter values for 'realm', 'nonce', 'nc', 'cnonce',
   * 'algorithm', 'domain', 'uri', 'username', 'qop' and 'response' will be returned as unquoted
   * values. Where as for all other parameters, their associated values will be returned as they
   * were set. To check for 'stale' parameter, user should invoke {@link
   * DsSipDigestChallengeInfo#isStale()} and to set the 'stale' parameter to true, user should
   * invoke {@link DsSipDigestChallengeInfo#setStale(boolean)}.
   *
   * @param name the name of the parameter that needs to be retrieved
   * @return the parameter value for the parameter with the specified <code>name</code>, if present,
   *     otherwise returns null.
   */
  public DsByteString getParameter(DsByteString name) {
    DsByteString value = null;
    if (name.equalsIgnoreCase(BS_URI)) {
      value = m_strURI;
    } else if (name.equalsIgnoreCase(BS_RESPONSE)) {
      value = m_strResponse;
    } else if (name.equalsIgnoreCase(BS_USERNAME)) {
      value = m_strUser;
    } else if (name.equalsIgnoreCase(BS_NC)) {
      value = getNc();
    } else if (name.equalsIgnoreCase(BS_CNONCE)) {
      value = getCnonce();
    } else {
      value = super.getParameter(name);
    }
    return value;
  }

  /**
   * Gets the "uri" parameter. This is more efficient than called getParamter("uri").
   *
   * @return the value of the "uri" parameter
   */
  public DsByteString getUri() {
    return m_strURI;
  }

  /**
   * Gets the "response" parameter. This is more efficient than called getParamter("response").
   *
   * @return the value of the "response" parameter
   */
  public DsByteString getResponse() {
    return m_strResponse;
  }

  /**
   * Gets the "nc" parameter. This is more efficient than called getParamter("nc").
   *
   * @return the value of the "nc" parameter
   */
  public DsByteString getNc() {
    if (m_nonceCount > 0) {
      return DsByteString.toHexString0Pad(m_nonceCount, 8);
    }

    return null;
  }

  /**
   * Gets the "cnonce" parameter. This is more efficient than called getParamter("cnonce").
   *
   * @return the value of the "cnonce" parameter
   */
  public DsByteString getCnonce() {
    if (m_baCNonce != null) {
      return new DsByteString(m_baCNonce);
    }

    return null;
  }

  /**
   * Gets the "username" parameter. This is more efficient than called getParamter("username").
   *
   * @return the value of the "username" parameter
   */
  public DsByteString getUsername() {
    return m_strUser;
  }

  /**
   * Sets the specified name-value parameter in this credentials info. In case of "username",
   * "realm", "algorithm", "nonce", "nc", "cnonce", "domain", "uri", "qop" and "response" as
   * parameter name, the value is unquoted if it is already quoted. To check for 'stale' parameter,
   * user should invoke {@link DsSipDigestChallengeInfo#isStale()} and to set the 'stale' parameter
   * to true, user should invoke {@link DsSipDigestChallengeInfo#setStale(boolean)}.
   *
   * @param name the name of the parameter
   * @param value the value of the parameter
   */
  public void setParameter(DsByteString name, DsByteString value) {
    if (name == null || value == null) {
      return;
    }
    if (name.equalsIgnoreCase(BS_URI)) {
      setUri(value);
    } else if (name.equalsIgnoreCase(BS_RESPONSE)) {
      setResponse(value);
    } else if (name.equalsIgnoreCase(BS_USERNAME)) {
      setUser(value.unquoted());
    } else if (name.equalsIgnoreCase(BS_QOP)) {
      setQop(value);
    } else if (name.equalsIgnoreCase(BS_NC)) {
      setNc(value);
    } else if (name.equalsIgnoreCase(BS_CNONCE)) {
      setCnonce(value);
    } else {
      super.setParameter(name, value);
    }
  }

  /**
   * Sets the value of the "uri" parameter. This is more efficient than calling setParameter("uri",
   * value).
   *
   * @param value the new value of the "uri" parameter
   */
  public void setUri(DsByteString value) {
    m_strURI = value.unquoted();
  }

  /**
   * Sets the value of the "response" parameter. This is more efficient than calling
   * setParameter("response", value).
   *
   * @param value the new value of the "response" parameter
   */
  public void setResponse(DsByteString value) {
    m_strResponse = value.unquoted();
  }

  /**
   * Sets the value of the "qop" parameter. This is more efficient than calling setParameter("qop",
   * value).
   *
   * @param value the new value of the "qop" parameter
   */
  public void setQop(DsByteString value) {
    m_strQOP = value.unquoted();
    int index = m_strQOP.indexOf(',');
    if (index != -1) {
      m_strQOP = m_strQOP.substring(0, index);
      m_strQOP.trim();
    }
  }

  /**
   * Sets the value of the "nc" parameter. This is more efficient than calling setParameter("nc",
   * value).
   *
   * @param value the new value of the "nc" parameter
   */
  public void setNc(DsByteString value) {
    String str = value.unquoted().toString();
    m_nonceCount = Long.parseLong(str, 16);
  }

  /**
   * Sets the value of the "cnonce" parameter. This is more efficient than calling
   * setParameter("cnonce", value).
   *
   * @param value the new value of the "cnonce" parameter
   */
  public void setCnonce(DsByteString value) {
    m_baCNonce = value.unquoted().toByteArray();
  }

  /**
   * Removes the parameter with the specified <code>name</code>, if present.
   *
   * @param name the name of the parameter that needs to be removed
   */
  public void removeParameter(DsByteString name) {
    if (name.equalsIgnoreCase(BS_URI)) {
      m_strURI = null;
    } else if (name.equalsIgnoreCase(BS_RESPONSE)) {
      m_strResponse = null;
    } else if (name.equalsIgnoreCase(BS_USERNAME)) {
      m_strUser = null;
    } else if (name.equalsIgnoreCase(BS_NC)) {
      m_nonceCount = 0;
    } else if (name.equalsIgnoreCase(BS_CNONCE)) {
      m_baCNonce = null;
    } else {
      super.removeParameter(name);
    }
  }

  /**
   * Gets a string representation of the parameters present in this credentials info.
   *
   * @return a string representation of the parameters present in this credentials info
   */
  public String toString() {
    return DsByteString.newString(toByteArray(null));
  }

  /**
   * Gets byte string representation of the parameters present in this credentials info.
   *
   * @return a byte string representation of the parameters present in this credentials info
   */
  public DsByteString toByteString() {
    return new DsByteString(toByteArray(null));
  }

  /**
   * Gets byte array representation of the parameters present in this credentials info.
   *
   * @return a byte array representation of the parameters present in this credentials info
   */
  public byte[] toByteArray() {
    return toByteArray(null);
  }

  /**
   * Sets the Client Nonce "cnonce" time window value in seconds. This time window defines that how
   * long the generated cnonce will be valid.
   *
   * @param timeInSeconds the cnonce time window in seconds
   */
  public void setCNTimeWindow(int timeInSeconds) {
    nonceGenerator.setCNTimeWindow(timeInSeconds);
  }

  /**
   * Returns the Client Nonce "cnonce" time window value in seconds. This time window defines that
   * how long the generated cnonce will be valid.
   *
   * @return the cnonce time window in seconds
   */
  public int getCNTimeWindow() {
    return nonceGenerator.getCNTimeWindow();
  }

  /**
   * Returns the current tick of the cnonce time window interval.
   *
   * @return the current tick of the cnonce time window interval
   */
  private int cnTick() {
    return (int) (System.currentTimeMillis() / (getCNTimeWindow() * 1000));
  }

  private byte[] generateCNonce() {
    byte[] cn = nonceGenerator.nonce(cnTick());

    MessageDigest digest = getMessageDigest();
    if (digest != null) {
      cn = digest.digest(cn);
    }
    return encodeBASE64(cn);
  }

  private short validateNonce(byte[] nonce, DsSipRequest request) {
    if (m_replay) {
      if (DsLog4j.authCat.isDebugEnabled()) {
        DsLog4j.authCat.debug(
            "Checking for replay counter-[" + DsByteString.newString(nonce) + "]");
      }

      int index = -1;
      // find '.' RETRY_INDEX index
      for (int i = nonce.length - 1; i > -1; i--) {
        if (nonce[i] == RETRY_INDEX) {
          index = i;
          break;
        }
      }
      int retries = -1;
      if (index > 0) {
        try {
          index++;
          retries = DsSipMsgParser.parseInt(nonce, index, (nonce.length - index));
        } catch (NumberFormatException nfe) {
          if (DsLog4j.authCat.isEnabled(Level.WARN)) {
            DsLog4j.authCat.warn(
                "Exception while checking for replay counter-["
                    + DsByteString.newString(nonce)
                    + "]",
                nfe);
          }
        }
      }
      // if retried for the maximum no. of retries allowed, return MAX_TRY_OUT
      if (retries <= 0) {
        m_retry = MAX_RETRY;
        return MAX_TRY_OUT;
      } else // update the retry count
      {
        m_retry = retries;
        int len = index - 1;
        byte[] temp = new byte[len];
        System.arraycopy(nonce, 0, temp, 0, len);
        nonce = temp;
      }
    } // _if replay

    byte[] newNonce = null;
    // get the time delta from the nonce.
    int index = -1;
    // find '_' DELTA_INDEX index
    for (int i = nonce.length - 1; i > -1; i--) {
      if (nonce[i] == DELTA_INDEX) {
        index = i;
        break;
      }
    }
    int delta = 0;
    if (index > 0) {
      if (DsLog4j.authCat.isDebugEnabled()) {
        DsLog4j.authCat.debug("Checking for delta time-[" + DsByteString.newString(nonce) + "]");
      }
      try {
        index++;
        delta = Integer.parseInt(DsByteString.newString(nonce, index, (nonce.length - index)), 16);
        int len = index - 1;
        byte[] temp = new byte[len];
        System.arraycopy(nonce, 0, temp, 0, len);
        nonce = temp;
      } catch (NumberFormatException nfe) {
        if (DsLog4j.authCat.isEnabled(Level.WARN)) {
          DsLog4j.authCat.warn(
              "Exception while checking for delta time-[" + DsByteString.newString(nonce) + "]",
              nfe);
        }
      }
    }
    int deltax = delta();
    int tick = tick();
    // we should be considering the previous time window to
    // generate the normal nonce as the time window interval is
    // not expired for it.
    if (deltax < delta) {
      tick--;
    }

    newNonce = generatePreNonce(request, tick);

    if (DsLog4j.authCat.isDebugEnabled()) {
      DsLog4j.authCat.debug(
          "Compare nonce["
              + DsByteString.newString(nonce)
              + "]-["
              + DsByteString.newString(newNonce)
              + "]");
    }
    // Compare the newly generated nonce with the nonce value in the request.
    // If the nonce value in the request is not valid, then it can either
    // be STALE or INVALID. We return STALE to give the client the opportunity
    // to recalculate the response hash value with the new nonce value.
    if (!MessageDigest.isEqual(nonce, newNonce)) {
      m_stale = true;
      return STALE;
    }
    return VALID;
  }

  /*
   * public static void main(String args[])
   * {
   * try
   * {
   * String credentials = "username= \"Sandeep\",realm = \"user@arealm.com\", cnonce= anonce, uri = abc@abc.com, response = encrypted rsponse, algorithm = MD5 \n";
   * DsSipDigestCredentialsInfo info = new DsSipDigestCredentialsInfo();
   * info.parse(new DsByteArrayInputStream(credentials.getBytes()));
   * }
   * catch(IOException e)
   * {
   * }
   * }
   */
}
