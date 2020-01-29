// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.security.MessageDigest;

/**
 * This class represents the challenge information used in "digest" Authentication within the
 * authentication headers.
 *
 * <p>This class provides the mechanism to generate the challenge info for the "digest"
 * authentication scheme along with other required operations.
 *
 * <p>The nonce computation in this challenge info can be either predictive or the one specified by
 * the RFC 2617.
 *
 * <p>The predictive nonce computation is defined by:<br>
 * <tt>nonce = H(source-IP:&LTcanonicalization of headers to be protected&GT)</tt>
 *
 * <p>where H is a suitable cryptographic hash function.
 *
 * <p>The nonce computation defined by RFC 2617:<br>
 * nonce = time-stamp H(time-stamp ":" ETag ":" private-key)<br>
 * where "time-stamp" is a server-generated time or other non-repeating value, "ETag" is the value
 * of the HTTP ETag header associated with the requested entity or server-generated tag value, and
 * "private-key" is data known only to the server.
 *
 * <p>By default this class uses a default implementation {@link DsSipNonceGenerator} that provides
 * for predictive and normal nonce computation as per the above specified algorithms. User can
 * specify his own implementation by implementing {@link DsSipNonceInterface} interface and setting
 * that implementation by invoking {@link #setNonceGenerator(DsSipNonceInterface)}.
 *
 * <p>User's can either a complete new implementation or can extend the default implementation
 * {@link DsSipNonceGenerator} and override the required API. <br>
 * For predictive - override {@link DsSipNonceGenerator#predictiveNonce(DsSipRequest, int)}<br>
 * For non-predictive (RFC 2617) - override {@link DsSipNonceGenerator#nonce(int)}
 *
 * <p>The choice of nonce computation algorithm can be specified by invoking {@link
 * #setPredictive(boolean) setPredictive(boolean)}.
 *
 * <p>This class also provides for specifying the maximum number of iterations of stale nonces the
 * server is willing to tolerate. In this case the server issues the first challenge for a request
 * using a value N that is a counter of the maximum number of iterations of stale nonces the server
 * is willing to tolerate. The nonce is computed as:
 *
 * <p><tt>nonce = H(source-IP:&LTheaders to protect&GT) + "." + N</tt>
 *
 * <p>where + signifies concatenation.
 *
 * <p>When the request is resubmitted, the server checks the numeric value at the end of the nonce.
 * If nonzero, it decrements it and uses this value for N in the construction of the new nonce. When
 * it receives a request with a value of zero for N, it knows that there has been an error of some
 * sort. This approach does not prevent an attacker from modifying the value of the count, but it
 * does provide a way for the server to detect race conditions with cooperating clients. In these
 * cases, the request should probably be rejected with a 500 class response, and some kind of alarm
 * should be generated to the operations staff. By default this option is not enabled and can be
 * controlled by {@link #setReplay(boolean) setReplay(boolean)} and {@link #setMaxRetry(int)
 * setMaxRetry(int)}
 *
 * <p>The parameters 'realm', 'nonce', 'algorithm' and 'domain' are stored as local member variables
 * in this class and are unquoted values. But these parameter values are serialized as quoted
 * values. When user queries any of these parameter value by invoking {@link
 * DsSipChallengeInfo#getParameter(DsByteString) getParameter(DsByteString)}, it will return
 * unquoted value for the corresponding parameter. While setting any of these parameter values by
 * invoking the {@link DsSipChallengeInfo#setParameter(DsByteString, DsByteString)
 * setParameter(DsByteString, DsByteString) } method, it will try to unquote the passed parameter
 * value if its quoted.
 *
 * <p>To check for 'stale' parameter, user should invoke {@link #isStale()} and to set the 'stale'
 * parameter to true, user should invoke {@link #setStale(boolean)}.
 */
public class DsSipDigestChallengeInfo extends DsSipChallengeInfo {
  /** The DsByteString DsSipConstants.BS_DIGEST. */
  public static final DsByteString TYPE = BS_DIGEST;

  /** The digest nonce parameter byte array value. */
  protected byte[] m_baNonce;

  /** Represents the stale credentials flag. */
  boolean m_stale;

  /** Represents the replay check flag. */
  protected boolean m_replay = REPLAY;

  /** Represents whether predictive digest algorithm should be used. */
  private boolean m_predictive = PREDICTIVE;

  /** Represents the maximum allowed credentials retires. */
  protected int m_retry = MAX_RETRY;

  /**
   * Holds the reference for the nonce generation implementation that implements
   * DsSipNonceInterface.
   */
  protected DsSipNonceInterface nonceGenerator;

  private DsByteString m_strDomain;
  protected DsByteString m_strQOP;

  /**
   * Represents the byte value that will mark the start index of the delta time value in the nonce
   * byte array value.
   */
  protected static byte DELTA_INDEX = (byte) '_';
  /**
   * Represents the byte value that will mark the start index of the retry count value in the nonce
   * byte array value.
   */
  protected static byte RETRY_INDEX = (byte) '.';

  /**
   * The default value of the maximum number of iterations of stale nonces a server is willing to
   * tolerate.
   */
  protected static int MAX_RETRY = 5;

  /** The default value specifying whether to check for replay attacks. */
  private static boolean REPLAY = false;

  /**
   * The default value specifying whether the predictive or non-predictive nonce computation be
   * used.
   */
  private static boolean PREDICTIVE = false;

  /**
   * Sets the Digest Challenge Info to be predictive or non-predictive by default. If set to
   * predictive by default then all the newly instantiated DsSipDigestChallengeInfo objects will be
   * using predictive nonce computation algorithm, otherwise the non-predictive (normal) nonce
   * computation algorithm will be used.
   *
   * @param predictive If set to true then by default all the newly instantiated
   *     DsSipDigestChallengeInfo objects will be using predictive nonce computation algorithm,
   *     otherwise the non-predictive (normal) nonce computation algorithm will be used.
   */
  public static void setPredictiveByDefault(boolean predictive) {
    PREDICTIVE = predictive;
  }

  /**
   * Tells whether the Digest Challenge Info is predictive or non-predictive by default. If
   * predictive by default then all the newly instantiated DsSipDigestChallengeInfo objects will be
   * using predictive nonce computation algorithm, otherwise the non-predictive (normal) nonce
   * computation algorithm will be used.
   *
   * @return <code>true</code> If by default all the newly instantiated DsSipDigestChallengeInfo
   *     objects will be using predictive nonce computation algorithm, <code>false</code> otherwise
   */
  public static boolean isPredictiveByDefault() {
    return PREDICTIVE;
  }

  /**
   * Sets the default behavior whether to check for replay attacks or not. If set to true then then
   * all the newly instantiated DsSipDigestChallengeInfo objects will check for replay attacks,
   * otherwise replay attacks will not be detected.
   *
   * @param replay If set to true then then all the newly instantiated DsSipDigestChallengeInfo
   *     objects will check for replay attacks, otherwise replay attacks will not be detected.
   */
  public static void setReplayByDefault(boolean replay) {
    REPLAY = replay;
  }

  /**
   * Tells the default behavior whether to check for replay attacks or not. Returns true is all the
   * newly instantiated DsSipDigestChallengeInfo objects will check for replay attacks, otherwise
   * false
   *
   * @return <code>true</code> is all the newly instantiated DsSipDigestChallengeInfo objects will
   *     check for replay attacks, otherwise <code>false</code>
   */
  public static boolean isReplayByDefault() {
    return REPLAY;
  }

  /**
   * Controls the default number of the maximum number of request retries that a server is ready to
   * tolerate for request authentication. This maximum retry count is used only if the replay option
   * is turned on for the challenge info. This specified retry count should be greater than 0,
   * otherwise this method will have no effect on the maximum retry count.
   *
   * @param maxRetry the maximum request retries that will be allowed for the challenge info by
   *     default.
   */
  public static void setMaxRetryByDefault(int maxRetry) {
    if (maxRetry > 0) {
      MAX_RETRY = maxRetry;
    }
  }

  /**
   * Tells the default number of the maximum number of request retries that a server is ready to
   * tolerate for request authentication. This maximum retry count is used only if the replay option
   * is turned on for the challenge info.
   *
   * @return the maximum request retries that will be allowed for the challenge info by default.
   */
  public static int getMaxRetryByDefault() {
    return MAX_RETRY;
  }

  /** Constructs this challenge info with default values. */
  protected DsSipDigestChallengeInfo() {
    this(null, (DsSipNonceInterface) null);
  }

  /**
   * Constructs this challenge info with the specified parameters. If the parameters contain
   * 'realm', 'nonce', 'algorithm' or 'domain' then unquotes (if these parameter values are quoted)
   * these parameter values and set to the corresponding local variables.
   *
   * @param params the list of parameters that need to be set for this challenge info.
   */
  public DsSipDigestChallengeInfo(DsParameters params) {
    this();
    setParameters(params);
  }

  /**
   * Constructs the Digest challenge info with the specified <code>realm</code>. The specified
   * <code>realm</code> value will be unquoted before setting.
   *
   * @param realm the realm for this Digest challenge info
   */
  public DsSipDigestChallengeInfo(DsByteString realm) {
    this(realm, (DsSipNonceInterface) null);
  }

  /**
   * Constructs the Digest challenge info with the specified <code>realm</code> and the specified
   * <code>algorithm</code>. The specified <code>realm</code> and the specified <code>algorithm
   * </code> values will be unquoted before setting.
   *
   * @param realm the realm for this Digest challenge info
   * @param algorithm the algorithm that will be used for the nonce generation
   */
  public DsSipDigestChallengeInfo(DsByteString realm, DsByteString algorithm) {
    this(realm, (DsSipNonceInterface) null);
    setParameter(BS_ALGORITHM, algorithm);
  }

  /**
   * Constructs the Digest challenge info with the specified implementation <code>nonceGenerator
   * </code> of the DsSipNonceInterface.
   *
   * @param nonceGenerator the nonce generator that will be used for generating nonce or predictive
   *     nonce values.
   */
  public DsSipDigestChallengeInfo(DsSipNonceInterface nonceGenerator) {
    this(null, nonceGenerator);
  }

  /**
   * Constructs the Digest challenge info with the specified <code>realm</code> and the specified
   * implementation <code>nonceGenerator</code> of the DsSipNonceInterface.
   *
   * <p>The specified <code>realm</code> value will be unquoted before setting.
   *
   * @param realm the realm for this Digest challenge info
   * @param nonceGenerator the nonce generator that will be used for generating nonce or predictive
   *     nonce values.
   */
  public DsSipDigestChallengeInfo(DsByteString realm, DsSipNonceInterface nonceGenerator) {
    super(realm);
    this.nonceGenerator = (nonceGenerator == null) ? new DsSipNonceGenerator() : nonceGenerator;
  }

  /**
   * Constructs this challenge info and parses the name-value pairs from the specified byte array
   * into this object.
   *
   * @param value the input byte buffer that need to be parsed into this challenge info object
   * @param offset the offset of the parameters in the specified buffer
   * @param count the number of bytes to be looked starting from the offset in the specified buffer
   *     for parsing.
   * @throws DsSipParserException if there is an error while parsing the parameters
   * @throws DsSipParserListenerException if this object, as parser listener, found some problem
   *     while parsing the parameters.
   */
  public DsSipDigestChallengeInfo(byte[] value, int offset, int count)
      throws DsSipParserException, DsSipParserListenerException {
    this();
    DsSipMsgParser.parseParameters(this, 0, value, offset, count);
  }

  /**
   * Constructs this challenge info and parses the name-value pairs from the specified byte array
   * into this object.
   *
   * @param value the input byte buffer that need to be parsed into this challenge info object
   * @throws DsSipParserException if there is an error while parsing the parameters
   * @throws DsSipParserListenerException if this object, as parser listener, found some problem
   *     while parsing the parameters.
   */
  public DsSipDigestChallengeInfo(byte[] value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value, 0, value.length);
  }

  /**
   * Constructs this challenge info and parses the name-value pairs from the specified String value
   * into this object.
   *
   * @param value the input String value that need to be parsed into this challenge info object
   * @throws DsSipParserException if there is an error while parsing the parameters
   * @throws DsSipParserListenerException if this object, as parser listener, found some problem
   *     while parsing the parameters.
   */
  public DsSipDigestChallengeInfo(String value)
      throws DsSipParserException, DsSipParserListenerException {
    this(DsByteString.getBytes(value));
  }

  /**
   * Returns a credentials info object based on this challenge info and, the specified <code>user
   * </code> and <code>password</code>. The returned credentials info object would be of type {@link
   * DsSipDigestCredentialsInfo}.
   *
   * @param user the user name to be used for constructing the credentials info
   * @param pass the user password to be used for constructing the credentials info
   * @return the newly constructed DsSipDigestCredentialsInfo object as per this challenge info, the
   *     specified <code>user</code> and the specified <code>password</code>.
   */
  public DsSipCredentialsInfo getCredentialsInfo(DsByteString user, DsByteString pass) {
    DsSipDigestCredentialsInfo credentials = new DsSipDigestCredentialsInfo(m_strRealm, user, pass);
    credentials.setNonce(m_baNonce);
    if (m_strQOP != null) {
      credentials.setParameter(BS_QOP, m_strQOP);
    }
    credentials.setParameters(m_paramTable);
    return credentials;
  }

  /**
   * Generates and returns a challenge info string as per this challenge info object for the
   * specified <code>request</code>. The request URI in the specified <code>request</code> will be
   * used as 'domain' parameter value. The nonce will also be generated (either normal or predictive
   * based on the option set for this challenge info, refer {@link #isPredictive()}) as per the
   * specified <code>request</code>. The returned challenge info string can be used in an
   * authenticate header to issue a challenge for a request.
   *
   * @param request the sip request
   * @return the challenge string as per this challenge info object.
   */
  public DsByteString generateChallenge(DsSipRequest request) {
    return new DsByteString(toByteArray(request));
  }

  /**
   * Generates and returns a challenge info byte array as per this challenge info object for the
   * specified <code>request</code>. The request URI in the specified <code>request</code> will be
   * used as 'domain' parameter value. The nonce will also be generated (either normal or predictive
   * based on the option set for this challenge info, refer {@link #isPredictive()}) as per the
   * specified <code>request</code>. The returned challenge info btye array can be used in an
   * authenticate header to issue a challenge for a request.
   *
   * @param request the sip request
   * @return the challenge byte array as per this challenge info object.
   */
  private byte[] toByteArray(DsSipRequest request) {
    ByteBuffer buffer = ByteBuffer.newInstance(100);
    boolean comma = false;
    try {
      // append realm
      if (m_strRealm != null) {
        BS_REALM.write(buffer);
        buffer.write(B_EQUAL);
        buffer.write(B_QUOTE);
        m_strRealm.write(buffer);
        buffer.write(B_QUOTE);
        comma = true;
      }
      if (comma) {
        buffer.write(B_COMMA);
      }
      // append domain
      BS_DOMAIN.write(buffer);
      buffer.write(B_EQUAL);
      buffer.write(B_QUOTE);
      if (m_strDomain == null && request != null) {
        m_strDomain = request.getURI().toByteString();
      }
      if (m_strDomain != null) {
        m_strDomain.write(buffer);
      }
      buffer.write(B_QUOTE);
      buffer.write(B_COMMA);

      // append nonce
      BS_NONCE.write(buffer);
      buffer.write(B_EQUAL);
      buffer.write(B_QUOTE);
      if (request != null) {
        m_baNonce = generateNonce(request);
      }
      if (m_baNonce != null) {
        buffer.write(m_baNonce, 0, m_baNonce.length);
      }
      buffer.write(B_QUOTE);
      // append stale
      if (m_stale) {
        buffer.write(B_COMMA);
        BS_STALE.write(buffer);
        buffer.write(B_EQUAL);
        buffer.write(B_QUOTE);
        BS_TRUE.write(buffer);
        buffer.write(B_QUOTE);
      }

      // append qop
      if (m_strQOP != null) {
        buffer.write(B_COMMA);
        BS_QOP.write(buffer);
        buffer.write(B_EQUAL);
        buffer.write(B_QUOTE);
        m_strQOP.write(buffer);
        buffer.write(B_QUOTE);
      }

      // append parameters
      if (m_paramTable != null) {
        m_paramTable.startWithDelimiter(true);
        m_paramTable.setDelimiter(B_COMMA);
        m_paramTable.write(buffer);
      }
    } catch (Exception exc) {
      // exc.printStackTrace();
      // We know there won't be any IOException
    }
    return buffer.toByteArray();
  }

  /**
   * Gets a string representation of the parameters present in this challenge info.
   *
   * @return a string representation of the parameters present in this challenge info
   */
  public String toString() {
    return DsByteString.newString(toByteArray(null));
  }

  /**
   * Gets byte string representation of the parameters present in this challenge info.
   *
   * @return a byte string representation of the parameters present in this
   */
  public DsByteString toByteString() {
    return new DsByteString(toByteArray(null));
  }

  /**
   * Gets byte array representation of the parameters present in this challenge info.
   *
   * @return a byte array representation of the parameters present in this challenge info
   */
  public byte[] toByteArray() {
    return toByteArray(null);
  }

  /**
   * Returns the challenge type for this challenge info object. It will always return BS_DIGEST.
   *
   * @return the challenge type for this challenge info object.
   */
  public DsByteString getType() {
    return DsSipDigestChallengeInfo.TYPE;
  }

  /**
   * Sets the current time window. It in turn invokes the underlying nonce generator's {@link
   * DsSipNonceInterface#setTimeWindow(int)} method. This time window is used in case of normal
   * nonce generation. This time window defines that after how long the generated nonce will be
   * valid. It means with in this time period, all parameters being same, generated nonce value will
   * be same.
   *
   * @param timeInMinutes the time window in minutes
   */
  public void setTimeWindow(int timeInMinutes) {
    nonceGenerator.setTimeWindow(timeInMinutes);
  }

  /**
   * Returns the current time window. It in turn invokes the underlying nonce generator's {@link
   * DsSipNonceInterface#getTimeWindow()} method. This time window is used in case of normal nonce
   * generation. This time window defines that after how long the generated nonce will be valid. It
   * means with in this time period, all parameters being same, generated nonce value will be same.
   *
   * @return the time window in minutes
   */
  public int getTimeWindow() {
    return nonceGenerator.getTimeWindow();
  }

  /**
   * Controls the nonce computation algorithm choice. If <code>true</code> then the predictive nonce
   * computation algorithm will be used while generating the challenge info and validating the
   * credentials. Otherwise, the normal algorithm specified by RFC 2617 will be used.
   *
   * @param predictive if <code>true</code>, then predictive nonce computation will be used.
   *     Otherwise normal nonce computation as specified by RFC 2617 will be used.
   */
  public void setPredictive(boolean predictive) {
    m_predictive = predictive;
  }

  /**
   * Tells whether the predictive nonce computation algorithm will be used while generating
   * challenge info and validating credentials or normal nonce computation as specified by RFC 2617
   * will be used. If <code>true</code> then the predictive nonce computation algorithm will be used
   * while generating the challenge info and validating the credentials. Otherwise, the normal
   * algorithm as specified by RFC 2617 will be used.
   *
   * @return <code>true</code> if predictive nonce computation will be used. <code>false</code> if
   *     normal nonce computation as specified by RFC 2617 will be used.
   */
  public boolean isPredictive() {
    return m_predictive;
  }

  /**
   * Controls the replay attack prevention flag. If set to <code>true</code>, then the request from
   * the client will be checked to determine whether the request retries is not maxed out. If set to
   * <code>false</code>, then there is no limit for the maximum number of request retries. By
   * default this flag is false.
   *
   * @param replay if <code>true</code>, then the request will be checked to determine whether the
   *     request retries is not maxed out. If <code>false</code>, then there is no limit for the
   *     maximum number of request retries.
   */
  public void setReplay(boolean replay) {
    m_replay = replay;
  }

  /**
   * Tells whether the replay attack prevention flag is enabled or not. If set to <code>true</code>,
   * then the request from the client will be checked to determine whether the request retries is
   * not maxed out. And if it is maxed out then the implementations can raise some kind of alarm to
   * notify administrator of the intrusion attack. If set to <code>false</code>, then there is no
   * limit for the maximum number of request retries. By default this flag is false.
   *
   * @return <code>true</code> if the request will be checked to determine whether the request
   *     retries is not maxed out. <code>false</code> if there is no limit for the maximum number of
   *     request retries.
   */
  public boolean isReplay() {
    return m_replay;
  }

  /**
   * Controls the maximum number of request retries that a server is ready to tolerate for request
   * authentication. This maximum retry count is used only if the replay option is turned on for
   * this challenge info. And the replay option is controlled by <code>setReplay(boolean)</code>. By
   * default this maximum retry count is 5 and this default value can be changed by
   * setMaxRetryByDefault(int). The specified retry count should be greater than 0, otherwise this
   * method will have no effect on the maximum retry count.
   *
   * @param retry the maximum request retries that will be allowed for this challenge info.
   */
  public void setMaxRetry(int retry) {
    if (retry < 1) {
      return;
    }
    m_retry = retry;
  }

  /**
   * Returns the maximum number of request retries count that a server is ready to tolerate for
   * request authentication. This maximum retry count is used only if the replay option is turned on
   * for this challenge info. And the replay option is controlled by <code>setReplay(boolean)</code>
   * . By default this maximum retry count is 5.
   *
   * @return the maximum request retries that will be allowed for this challenge info.
   */
  public int getMaxRetry() {
    return m_retry;
  }

  /**
   * Sets the list of headers to be protected by predictive nonce computation. The specified array
   * should contain the constant header Ids, as defined in the {@link DsSipConstants} class, of the
   * headers that needs to be protected by predictive nonce computation.
   *
   * <p>It in turn invokes the underlying nonce generator's {@link
   * DsSipNonceInterface#setHeadersToProtect(int[])} method. This header ids list is used in case of
   * predictive nonce generation.
   *
   * @param hIds the array containing the integer constant header Ids as defined in the
   *     DsSipParserConstants class, of the headers that needs to be protected by predictive nonce
   *     computation
   */
  public void setHeadersToProtect(int[] hIds) {
    nonceGenerator.setHeadersToProtect(hIds);
  }

  /**
   * Tells the list of headers that will be protected by predictive nonce computation. The returned
   * array contains the constant header Ids, as defined in the {@link DsSipConstants} class, of the
   * headers that will be protected by predictive nonce computation.
   *
   * <p>It in turn invokes the underlying nonce generator's {@link
   * DsSipNonceInterface#getHeadersToProtect()} method. This header ids list is used in case of
   * predictive nonce generation.
   *
   * @return the array containing the integer constant header Ids as defined in the
   *     DsSipParserConstants class, of the headers that will be protected by predictive nonce
   *     computation
   */
  public int[] getHeadersToProtect() {
    return nonceGenerator.getHeadersToProtect();
  }

  /**
   * Sets the option whether the CSeq method should be protected while calculating the predictive
   * nonce. The CSeq method will be included if this option is set to <code>true</code>, otherwise
   * the CSeq method will not be protected.
   *
   * @param protect if <code>true</code> then the CSeq method will be protected in the predictive
   *     nonce computation.
   */
  public void setMethodProtection(boolean protect) {
    nonceGenerator.setMethodProtection(protect);
  }

  /**
   * Tells whether the CSeq method will be protected while calculating the predictive nonce. The
   * CSeq method will be included if this option is set to <code>true</code>, otherwise the CSeq
   * method will not be protected.
   *
   * @return <code>true</code> if the CSeq method will be protected in the predictive nonce
   *     computation, <code>false</code> otherwise.
   */
  public boolean isMethodProtection() {
    return nonceGenerator.isMethodProtection();
  }

  /**
   * Sets the specified <code>nonceGenerator</code> for this challenge info. The specified nonce
   * generator won't get set if its null.
   *
   * @param nonceGenerator the new nonce generator that will be used for generating nonce or
   *     predictive nonce values.
   */
  public void setNonceGenerator(DsSipNonceInterface nonceGenerator) {
    if (nonceGenerator != null) {
      this.nonceGenerator = nonceGenerator;
    }
  }

  /**
   * Returns the nonce generator for this challenge info.
   *
   * @return the nonce generator for this challenge info.
   */
  public DsSipNonceInterface getNonceGenerator() {
    return nonceGenerator;
  }

  /**
   * Tells whether this object contains a parameter with the specified parameter <code>name</code>.
   *
   * @param name the name of the parameter that needs to be checked
   * @return <code>true</code> if a parameter with the specified name is present, <code>false</code>
   *     otherwise.
   */
  public boolean hasParameter(DsByteString name) {
    if (name.equalsIgnoreCase(BS_NONCE)) {
      return (m_baNonce != null);
    }
    if (name.equalsIgnoreCase(BS_DOMAIN)) {
      return (m_strDomain != null);
    }
    if (name.equalsIgnoreCase(BS_QOP)) {
      return (m_strQOP != null);
    }
    return super.hasParameter(name);
  }

  /**
   * Returns the parameter value for the parameter with the specified <code>name</code>, if present,
   * otherwise returns null.
   *
   * <p><b>Note:</b> The parameter values for 'realm', 'nonce', 'algorithm', 'qop' and 'domain' will
   * be returned as unquoted values. Where as for all other parameters, their associated values will
   * be returned as they were set. To check for 'stale' parameter, user should invoke {@link
   * #isStale()} and to set the 'stale' parameter to true, user should invoke {@link
   * #setStale(boolean)}.
   *
   * @param name the name of the parameter that needs to be retrieved
   * @return the parameter value for the parameter with the specified <code>name</code>, if present,
   *     otherwise returns null.
   */
  public DsByteString getParameter(DsByteString name) {
    DsByteString value = null;
    if (name.equalsIgnoreCase(BS_NONCE) && m_baNonce != null) {
      value = new DsByteString(m_baNonce);
    } else if (name.equalsIgnoreCase(BS_DOMAIN)) {
      value = m_strDomain;
    } else if (name.equalsIgnoreCase(BS_QOP)) {
      value = m_strQOP;
    } else {
      value = super.getParameter(name);
    }
    return value;
  }

  /**
   * Sets the specified name-value parameter in this challenge info. In case of "realm",
   * "algorithm", "nonce", "qop" and "domain" as parameter name, the value is unquoted if it is
   * quoted. To check for 'stale' parameter, user should invoke {@link #isStale()} and to set the
   * 'stale' parameter to true, user should invoke {@link #setStale(boolean)}.
   *
   * @param name the name of the parameter
   * @param value the value of the parameter
   */
  public void setParameter(DsByteString name, DsByteString value) {
    if (name == null || value == null) {
      return;
    }
    if (name.equalsIgnoreCase(BS_NONCE)) {
      setNonce(unquoteArray(value));
      return;
    }
    if (name.equalsIgnoreCase(BS_DOMAIN)) {
      m_strDomain = value.unquoted();
      return;
    }
    if (name.equalsIgnoreCase(BS_STALE)) {
      if (value.equalsIgnoreCase(BS_TRUE)) {
        m_stale = true;
      } else {
        m_stale = false;
      }
      return;
    }
    if (name.equalsIgnoreCase(BS_QOP)) {
      m_strQOP = value.unquoted();
      return;
    }
    if (name.equalsIgnoreCase(BS_ALGORITHM)) {
      value = value.unquoted();
    }
    super.setParameter(name, value);
  }

  /**
   * Removes the parameter with the specified <code>name</code>, if present.
   *
   * @param name the name of the parameter that needs to be removed
   */
  public void removeParameter(DsByteString name) {
    if (name.equalsIgnoreCase(BS_NONCE)) {
      setNonce(null);
      return;
    }
    if (name.equalsIgnoreCase(BS_DOMAIN)) {
      m_strDomain = null;
      return;
    } else if (name.equalsIgnoreCase(BS_QOP)) {
      m_strQOP = null;
      return;
    }
    super.removeParameter(name);
  }

  /**
   * Sets the specified parameters for this object. It will override the existing parameters. If the
   * specified parameters object is null, then it will be a nope operation. To remove the parameters
   * from this object use {@link #removeParameters()}.
   *
   * @param paramTable the new parameters object that need to be set for this object.
   */
  public void setParameters(DsParameters paramTable) {
    if (paramTable != null) {
      // Clear off this table.
      if (m_paramTable != null) {
        m_paramTable.clear();
      }

      DsByteString key = null;
      DsByteString value = null;
      DsParameter param = (DsParameter) paramTable.getFirst();
      while (param != null) {
        key = param.getKey();
        value = param.getValue();
        setParameter(key, value);
        param = (DsParameter) param.getNext();
      } //  _while
    } // _if
  }

  /**
   * Returns the nonce value in this challenge info.
   *
   * @return the nonce value in this challenge info.
   */
  public byte getNonce()[] {
    return m_baNonce;
  }

  /**
   * Sets the nonce value in this challenge info.
   *
   * @param nonce the new nonce value for this challenge info.
   */
  public void setNonce(byte[] nonce) {
    m_baNonce = nonce;
  }

  /**
   * Tells whether the nonce in this challenge info is stale or not.
   *
   * @return <code>true</code> if the nonce in this challenge info is stale, <code>false</code>
   *     otherwise.
   */
  public boolean isStale() {
    return m_stale;
  }

  /**
   * Sets whether the nonce in this challenge info is stale or not.
   *
   * @param stale if <code>true</code> then the nonce in this challenge info is marked stale.
   */
  public void setStale(boolean stale) {
    m_stale = stale;
  }

  /**
   * Clears all the member data and made this object reusable. The various components (sub-elements)
   * of this object can be set again.
   */
  public void reInit() {
    super.reInit();
    m_baNonce = null;
    m_replay = REPLAY;
    m_retry = MAX_RETRY;
    m_stale = false;
    m_predictive = PREDICTIVE;
  }

  /**
   * Generates either the predictive nonce or the nonce specified by RFC 2617, depending upon
   * whether the predictive option is set for this challenge info. Generates predictive nonce if
   * predictive option is set, normal nonce otherwise.
   *
   * @param request the SIP request whose headers needs to be protected for message integrity.
   *     Required only in case of predictive nonce computation
   * @return the nonce value as a base-64 encoded byte array of the generated hash value.
   */
  public final byte[] generateNonce(DsSipRequest request) {
    byte[] n1 = generatePreNonce(request, tick());
    if (null == n1) return null;

    byte[] n2 = n1;

    // append delta
    DsByteString deltaHex = DsByteString.toHexString(delta());
    int size = n2.length + deltaHex.length() + 1;
    n1 = new byte[size];
    System.arraycopy(n2, 0, n1, 0, n2.length);
    n1[n2.length] = DELTA_INDEX;
    System.arraycopy(deltaHex.data(), deltaHex.offset(), n1, n2.length + 1, deltaHex.length());
    n2 = n1;

    // check for the replay flag,
    // if set then reset the retry count and append it to the actual nonce
    if (m_replay) {
      byte[] bytes = DsIntStrCache.intToBytes(m_retry);
      size = n2.length + bytes.length + 1;
      n1 = new byte[size];
      System.arraycopy(n2, 0, n1, 0, n2.length);
      n1[n2.length] = RETRY_INDEX;
      System.arraycopy(bytes, 0, n1, n2.length + 1, bytes.length);
      if (--m_retry < 0) {
        m_retry = MAX_RETRY;
      }
    }
    return n1;
  }

  /**
   * Generates either the predictive nonce or the nonce specified by RFC 2617, depending upon
   * whether the predictive option is set for this challenge info. Generates predictive nonce if
   * predictive option is set, normal nonce otherwise. This nonce value doesn't append delta time
   * and retry counter.
   *
   * @param request the SIP request whose headers needs to be protected for message integrity.
   *     Required only in case of predictive nonce computation
   * @param tick the tick of the time window interval.
   * @return the nonce value as a base-64 encoded byte array of the generated hash value.
   */
  protected final byte[] generatePreNonce(DsSipRequest request, int tick) {
    byte[] n1 = null;

    // we have to check for predictive nonce and normal nonce
    if (m_predictive) // predictive
    {
      if (null == request) return null;
      n1 = nonceGenerator.predictiveNonce(request, tick);
    } else // normal nonce
    {
      n1 = nonceGenerator.nonce(tick);
    }
    MessageDigest digest = getMessageDigest();
    if (digest != null) {
      n1 = digest.digest(n1);
    }
    return encodeBASE64(n1);
  }

  /**
   * Checks the specified nonce value, if there is an appended retry value. The appended retry value
   * N is a counter of the maximum number of iterations of stale nonces the server is willing to
   * tolerate. The nonce with this retry number will look like:
   *
   * <p>nonce = actual-nonce + "." + N
   *
   * <p>where + signifies concatenation. If there is a retry number appended to the nonce value,
   * then that number will be returned, otherwise -1 will be returned.
   *
   * @param nValue the nonce value to check if there is an associated retry number
   * @return the retry number associated with the specified nonce value, -1 if there is no
   *     associated retry number
   */
  protected final int checkRetry(byte[] nValue) {
    int ret = -1;
    byte[] data = nValue;
    byte target = (byte) '.';
    int i = data.length - 1;

    while (i >= 0) {
      if (data[i--] == target) {
        try {
          i += 2;
          if (i != data.length) {
            ret = DsSipMsgParser.parseInt(data, i, data.length - i);
            if (ret <= 0) {
              // this is the max_try_out condition
              ret = 0;
            }
          }
        } catch (NumberFormatException nfe) {
          // the nonce value doesn't have retry count appended
        }
        return ret;
      }
    }
    return ret;
  }

  /**
   * Returns the current tick of the time window interval. The time returned is in minutes.
   *
   * @return the current tick of the time window interval.
   */
  protected final int tick() {
    return (int) (System.currentTimeMillis() / (getTimeWindow() * 60000));
  }

  /**
   * Returns the time elapsed since the last tick of the time window interval. The time returned is
   * in minutes.
   *
   * @return the time elapsed since the last tick of the time window interval.
   */
  protected final int delta() {
    return (int) (System.currentTimeMillis() % (getTimeWindow() * 60000));
  }

  /**
   * Tells whether the algorithm is "MD5-sess".
   *
   * @return <code>true</code> if the algorithm is "MD5-sess", <code>false</code> otherwise.
   */
  protected boolean isMD5Session() {
    DsByteString alg = getAlgorithm();
    if (null != alg && alg.equalsIgnoreCase(BS_MD5_SESS)) {
      return true;
    }

    return false;
  }

  /*
   * public static void main(String args[])
   * {
   * try
   * {
   *
   * DsSipDigestChallengeInfo challenge = new DsSipDigestChallengeInfo();
   * challenge.prop.setProperty("algorithm","MD5");
   * long time = System.currentTimeMillis();
   * byte [] hash = challenge.generateHashValue(time);
   * Thread.sleep(6);
   * byte []rehash = challenge.generateHashValue(System.currentTimeMillis());
   * System.exit(0);
   *
   * DsSipDigestChallengeInfo challenge1 =  new DsSipDigestChallengeInfo("realm@testrealm.com");
   * DsSipInviteMessage message = new DsSipInviteMessage
   * (new DsSipFromHeader("sip:" +
   * InetAddress.getLocalHost().getHostName() + ":"+5060, null)
   * , new DsSipToHeader
   * (new DsSipNameAddress("sip:" + "sera@" +
   * InetAddress.getLocalHost().getHostName() + ":"+5070), null)
   * , new DsSipContactHeader
   * (new DsSipNameAddress("sip:" +
   * InetAddress.getLocalHost().getHostName() + ":"+5060))
   * , null, 0, "", "");
   * message.setConnectionAddress(InetAddress.getByName("localhost"));
   * byte[] challenge_data = challenge1.generateChallenge(message);
   *
   * challenge1 = DsSipDigestChallengeInfo.createFromData(challenge_data);
   * }
   * catch(DsException e)
   * {
   * }
   * catch(Exception e1)
   * {
   * }
   * }
   */
}
