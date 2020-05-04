// Copyright (c) 2005-2010 by Cisco Systems, Inc.
// All rights reserved.
package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipElementListener;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipMsgParser;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.Base64;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.slf4j.event.Level;

/**
 * An abstract base class that represents the challenge information used for authentication within
 * authentication headers.
 */
public abstract class DsSipChallengeInfo implements DsSipConstants, DsSipElementListener {
  /** The constant specifying that the client credentials are invalid. */
  public static final short INVALID = 0;
  /** The constant specifying that the client credentials are valid. */
  public static final short VALID = 1;
  /** The constant specifying that the nonce value present in the client credentials is stale. */
  public static final short STALE = 2;
  /**
   * The constant specifying that the client has already tried for the maximum number of times that
   * this server was willing to tolerate.
   */
  public static final short MAX_TRY_OUT = 3;

  /** The default algorithm (MD5) String. */
  public static final DsByteString DEFAULT_ALGORITHM = BS_MD5;

  /** The message digest object for this challenge. */
  protected MessageDigest messageDigest;

  /** Holds the parameters. */
  protected DsParameters m_paramTable;
  /** Holds the realm value. */
  protected DsByteString m_strRealm;

  /** Constructs this challenge info with the default values. */
  protected DsSipChallengeInfo() {}

  /**
   * Constructs this challenge info with the specified <code>realm</code>.
   *
   * @param realm the realm for this challenge info.
   */
  protected DsSipChallengeInfo(DsByteString realm) {
    setRealm(realm);
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
  protected DsSipChallengeInfo(byte[] value, int offset, int count)
      throws DsSipParserException, DsSipParserListenerException {
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
  protected DsSipChallengeInfo(byte[] value)
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
  protected DsSipChallengeInfo(String value)
      throws DsSipParserException, DsSipParserListenerException {
    this(DsByteString.getBytes(value));
  }

  /**
   * Generates and returns a challenge info string as per this challenge info object for the
   * specified <code>request</code>. The returned challenge info string can be used in an
   * authenticate header to issue a challenge for a request.
   *
   * @param request the sip request
   * @return the challenge string as per this challenge info object.
   */
  public abstract DsByteString generateChallenge(DsSipRequest request);

  /**
   * Returns a credentials info object based on this challenge info and, the specified <code>user
   * </code> and <code>password</code>.
   *
   * @param user the user name to be used for constructing the credentials info
   * @param password the user password to be used for constructing the credentials info
   * @return the newly constructed DsSipCredentialsInfo object as per this challenge info, the
   *     specified <code>user</code> and the specified <code>password</code>.
   */
  public abstract DsSipCredentialsInfo getCredentialsInfo(DsByteString user, DsByteString password);

  /**
   * Returns the challenge type for this challenge info object. It can be either BS_DIGEST or
   * BS_BASIC.
   *
   * @return the challenge type for this challenge info object.
   */
  public abstract DsByteString getType();

  /**
   * Constructs and parses a challenge info object of the specified type from the specified byte
   * array <code>value</code>.
   *
   * @param type the type of challenge info that needs to be constructed (DIGEST or BASIC)
   * @param value the input byte buffer that need to be parsed into a challenge info object
   * @param offset the offset of the parameters in the specified buffer
   * @param count the number of bytes to be looked starting from the offset in the specified buffer
   *     for parsing.
   * @return the constructed challenge.
   * @throws DsSipParserException if there is an error while parsing the parameters
   * @throws DsSipParserListenerException if this object, as parser listener, found some problem
   *     while parsing the parameters.
   */
  public static DsSipChallengeInfo constructChallenge(
      DsByteString type, byte[] value, int offset, int count)
      throws DsSipParserException, DsSipParserListenerException {
    DsSipChallengeInfo info = null;
    if (BS_DIGEST.equalsIgnoreCase(type)) {
      info = new DsSipDigestChallengeInfo();
      info.parse(value, offset, count);
    } else if (BS_BASIC.equalsIgnoreCase(type)) {
      info = new DsSipBasicChallengeInfo();
      info.parse(value, offset, count);
    }
    return info;
  }

  /**
   * Constructs and parses a credentials info object of the specified type from the specified byte
   * array <code>value</code>.
   *
   * @param type the type of credentials info that needs to be constructed (DIGEST or BASIC)
   * @param value the input byte buffer that need to be parsed into a credentials info object
   * @param offset the offset of the parameters in the specified buffer
   * @param count the number of bytes to be looked starting from the offset in the specified buffer
   *     for parsing.
   * @return the constructed credentials.
   * @throws DsSipParserException if there is an error while parsing the parameters
   * @throws DsSipParserListenerException if this object, as parser listener, found some problem
   *     while parsing the parameters.
   */
  public static DsSipCredentialsInfo constructCredentials(
      DsByteString type, byte[] value, int offset, int count)
      throws DsSipParserException, DsSipParserListenerException {
    DsSipCredentialsInfo info = null;
    if (BS_DIGEST.equalsIgnoreCase(type)) {
      DsSipDigestCredentialsInfo cr = new DsSipDigestCredentialsInfo();
      cr.parse(value, offset, count);
      info = cr;
    } else if (BS_BASIC.equalsIgnoreCase(type)) {
      DsSipBasicCredentialsInfo cr = new DsSipBasicCredentialsInfo();
      cr.parse(value, offset, count);
      info = cr;
    }
    return info;
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
  public void parse(byte[] value, int offset, int count)
      throws DsSipParserException, DsSipParserListenerException {
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
  public void parse(byte[] value) throws DsSipParserException, DsSipParserListenerException {
    parse(value, 0, value.length);
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
  public void parse(String value) throws DsSipParserException, DsSipParserListenerException {
    parse(DsByteString.getBytes(value));
  }

  /**
   * Returns the value of realm for this challenge info. The returned value would be an unquoted
   * value of the realm.
   *
   * @return the value of realm for this challenge info.
   */
  public DsByteString getRealm() {
    return m_strRealm;
  }

  /**
   * Sets the value of realm for this challenge info. The specified <code>realm</code> value should
   * be an unquoted value of the realm.
   *
   * @param realm the new unquoted value of realm for this challenge info.
   */
  public void setRealm(DsByteString realm) {
    if (realm == null) {
      m_strRealm = null;
    } else {
      m_strRealm = realm.unquoted();
    }
  }

  /**
   * Tells whether this challenge/credentials info has any parameters.
   *
   * @return true if it has parameters, else false.
   */
  public boolean hasParameters() {
    return (m_paramTable != null && !m_paramTable.isEmpty());
  }

  /**
   * Returns the parameters that are present in this challenge/credentials info.
   *
   * @return the parameters that are present in this challenge/credentials info.
   */
  public DsParameters getParameters() {
    return m_paramTable;
  }

  /**
   * Sets the specified parameters for this object. It will override the existing parameters. If the
   * specified parameters object is null, then it will be a nope operation. To remove the parameters
   * from this object use {@link #removeParameters()}.
   *
   * @param paramTable the new parameters object that need to be set for this object.
   */
  public void setParameters(DsParameters paramTable) {
    if (paramTable != m_paramTable) {
      m_paramTable = paramTable;
    }
  }

  /** Removes any existing parameters in this object. */
  public void removeParameters() {
    if (m_paramTable != null) {
      if (!m_paramTable.isEmpty()) {
        m_paramTable.clear();
      }
      m_paramTable = null;
    }
  }

  /**
   * Tells whether this object contains a parameter with the specified parameter <code>name</code>.
   *
   * @param name the name of the parameter that needs to be checked
   * @return <code>true</code> if a parameter with the specified name is present, <code>false</code>
   *     otherwise.
   */
  public boolean hasParameter(DsByteString name) {
    if (name.equalsIgnoreCase(BS_REALM)) {
      return (m_strRealm != null);
    }
    return (m_paramTable != null && m_paramTable.isPresent(name));
  }

  /**
   * Returns the parameter value for the parameter with the specified <code>name</code>, if present,
   * otherwise returns null.
   *
   * @param name the name of the parameter that needs to be retrieved
   * @return the parameter value for the parameter with the specified <code>name</code>, if present,
   *     otherwise returns null.
   */
  public DsByteString getParameter(DsByteString name) {
    DsByteString value = null;
    if (name.equalsIgnoreCase(BS_REALM)) {
      value = m_strRealm;
    } else if (m_paramTable != null && !m_paramTable.isEmpty()) {
      value = m_paramTable.get(name);
    }
    return value;
  }

  /**
   * Sets the specified name-value parameter in this object. In case of "realm" and "algorithm" as
   * parameter name, the value will be unquoted if it is quoted.
   *
   * @param name the name of the parameter
   * @param value the value of the parameter
   */
  public void setParameter(DsByteString name, DsByteString value) {
    if (name == null || value == null) {
      return;
    }
    if (name.equalsIgnoreCase(BS_REALM)) {
      setRealm(value.unquoted());
      return;
    }
    if (m_paramTable == null) {
      m_paramTable = new DsParameters();
    }
    m_paramTable.put(name, value);
  }

  public DsByteString getAlgorithm() {
    if (m_paramTable != null && !m_paramTable.isEmpty()) {
      return m_paramTable.get(BS_ALGORITHM);
    }

    return null;
  }

  public void setAlgorithm(DsByteString value) {
    if (m_paramTable == null) {
      m_paramTable = new DsParameters();
    }
    m_paramTable.put(BS_ALGORITHM, value);
  }

  /**
   * Removes the parameter with the specified <code>name</code>, if present.
   *
   * @param name the name of the parameter that needs to be removed
   */
  public void removeParameter(DsByteString name) {
    if (name.equalsIgnoreCase(BS_REALM)) {
      setRealm(null);
      return;
    }
    if (m_paramTable != null) {
      m_paramTable.remove(name);
    }
  }

  /**
   * Clears all the member data and made this object reusable. The various components (sub-elements)
   * of this object can be set again.
   */
  public void reInit() {
    m_strRealm = null;
    if (m_paramTable != null) {
      m_paramTable.reInit();
    }
  }

  /**
   * This is an utility function that encodes the specified <code>data</code> into a base-64
   * encoding and returns the base-64 encoded byte array.
   *
   * @param data the byte array that needs to be encoded in base-64 encoding.
   * @return a base-64 encoded byte array of the specified byte array data.
   */
  public final byte[] encodeBASE64(byte[] data) {
    String str = Base64.encodeBytes(data);
    return DsByteString.getBytes(str);
  }

  /**
   * This is an utility function that decodes the specified base-64 encoded <code>data</code> byte
   * array and returns the base-64 decoded byte array.
   *
   * @param data the base-64 encoded byte array that needs to be decoded.
   * @return a decoded byte array of the specified base-64 encoded byte array data.
   */
  public final byte[] decodeBASE64(byte[] data) {
    return Base64.decode(data, 0, data.length, Base64.NO_OPTIONS);
  }

  /**
   * Returns a reference to the message digest object for this object based on the algorithm
   * specified. If the message digest is already not created, then create one and return, otherwise
   * reset the existing one and return. If the algorithm in this object is different than the
   * algorithm of the already created message digest object, then a new message digest object will
   * be created and returned.
   *
   * @return the reference to message digest for this challenge info
   */
  protected final MessageDigest getMessageDigest() {
    DsByteString alg = getParameter(BS_ALGORITHM);

    if (alg == null) {
      alg = DEFAULT_ALGORITHM;
    }

    // Check if the algorithm is md5-sess.
    if (alg.equalsIgnoreCase(BS_MD5_SESS)) {
      alg = BS_MD5;
    }

    String strAlg = alg.toString();
    if (messageDigest == null || !messageDigest.getAlgorithm().equalsIgnoreCase(strAlg)) {
      try {
        messageDigest = MessageDigest.getInstance(strAlg);
      } catch (NoSuchAlgorithmException nsae) {
        if (DsLog4j.authCat.isEnabled(Level.WARN)) {
          DsLog4j.authCat.warn(
              "Couldn't instantiate Message Digest for"
                  + " the specified Algorithm ["
                  + strAlg
                  + "]",
              nsae);
        }
      }
    } else {
      messageDigest.reset();
    }
    return messageDigest;
  }

  /**
   * Returns a new array of bytes present in the specified byte string <code>value</code> excluding
   * the quotes if present at the start and end of the specified byte string.
   *
   * @param value The byte string where from unquoted byte array needs to be extracted.
   * @return a new byte array without quotes.
   */
  protected static byte[] unquoteArray(DsByteString value) {
    byte[] bytes = null;
    if (value.charAt(0) == B_QUOTE && value.charAt(value.length() - 1) == B_QUOTE) {
      bytes = value.toByteArray(1, (value.length() - 2));
    } else {
      bytes = value.toByteArray();
    }
    return bytes;
  }

  ////////////////////////////////////////////////////////////////////////////////
  // DsSipElementListener Interface implementation
  ////////////////////////////////////////////////////////////////////////////////

  /*
   * javadoc inherited.
   */
  public DsSipElementListener elementBegin(int contextId, int elementId)
      throws DsSipParserListenerException {
    if (DsSipMessage.DEBUG) {
      System.out.println(
          "elementBegin - contextId = ["
              + contextId
              + "]["
              + DsSipMsgParser.HEADER_NAMES[contextId]
              + "]");
      System.out.println(
          "elementBegin - elementId = ["
              + elementId
              + "]["
              + DsSipMsgParser.ELEMENT_NAMES[elementId]
              + "]");
      System.out.println();
    }
    return null;
  }

  public void elementFound(
      int contextId, int elementId, byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {
    if (DsSipMessage.DEBUG) {
      System.out.println(
          "elementFound - contextId = ["
              + contextId
              + "]["
              + DsSipMsgParser.HEADER_NAMES[contextId]
              + "]");
      System.out.println(
          "elementFound - elementId = ["
              + elementId
              + "]["
              + DsSipMsgParser.ELEMENT_NAMES[elementId]
              + "]");
      System.out.println("elementFound - value [offset, count] = [" + offset + ", " + count + "]");
      System.out.println(
          "elementFound - value = [" + DsByteString.newString(buffer, offset, count) + "]");
      System.out.println();
    }
  }

  /*
   * javadoc inherited.
   */
  public void parameterFound(
      int contextId, byte[] buffer, int nameOffset, int nameCount, int valueOffset, int valueCount)
      throws DsSipParserListenerException {
    if (DsSipMessage.DEBUG) {
      System.out.println("parameterFound - contextId = [" + contextId + "]");
      System.out.println(
          "parameterFound - name [offset, count] = [" + nameOffset + " ," + nameCount + "]");
      System.out.println(
          "parameterFound - name = ["
              + DsByteString.newString(buffer, nameOffset, nameCount)
              + "]");
      System.out.println(
          "parameterFound - value [offset, count] = [" + valueOffset + ", " + valueCount + "]");
      System.out.println(
          "parameterFound - value = ["
              + DsByteString.newString(buffer, valueOffset, valueCount)
              + "]");
      System.out.println();
    }
    setParameter(
        new DsByteString(buffer, nameOffset, nameCount),
        new DsByteString(buffer, valueOffset, valueCount));
  }
}
