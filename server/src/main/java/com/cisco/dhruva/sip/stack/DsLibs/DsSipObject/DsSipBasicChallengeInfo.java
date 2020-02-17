// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

/**
 * This class represents the challenge information used in Basic Authentication within the
 * authentication headers.
 */
public class DsSipBasicChallengeInfo extends DsSipChallengeInfo {
  /** Constant representing the BASIC authentication type. */
  public static final DsByteString TYPE = BS_BASIC;

  /** Empty constructor for super classes. */
  protected DsSipBasicChallengeInfo() {
    super();
  }

  /**
   * Constructs a basic challenge info object with the specified <code>realm</code>.
   *
   * @param realm the realm for this basic challenge info.
   */
  public DsSipBasicChallengeInfo(DsByteString realm) {
    super(realm);
  }

  /**
   * Constructs a basic challenge info object with the specified <code>data</code>. The specified
   * <code>data</code> is assumed to be in the format (realm="realm value").
   *
   * @param data the basic challenge information
   */
  protected DsSipBasicChallengeInfo(byte[] data) {
    super();
    int index = -1;
    int len = data.length;
    for (int i = 0; i < len; i++) {
      if (data[i] == '"') {
        index = i;
        break;
      }
    }
    index++;
    if (index > 0 && index < len) {
      setRealm(new DsByteString(data, (index), (len - 1)));
    }
  }

  /**
   * Generates and returns a challenge info string as per this challenge info object for the
   * specified <code>request</code>. In this case, the specified request is not used for challenge
   * generation as all the required information (realm value) is assumed to be present in this
   * challenge info object. The returned challenge info string can be used in an authenticate header
   * to issue a challenge for a request.
   *
   * @param request the sip request
   * @return the challenge string as per this challenge info object.
   */
  public DsByteString generateChallenge(DsSipRequest request) {
    ByteBuffer buffer = ByteBuffer.newInstance();
    buffer.write(BS_REALM);
    buffer.write('=');
    buffer.write('"');
    buffer.write(getRealm());
    buffer.write('"');
    return buffer.getByteString();
  }

  /**
   * Returns the challenge type for this challenge info object. It will always return BS_BASIC.
   *
   * @return the challenge type for this challenge info object.
   */
  public DsByteString getType() {
    return TYPE;
  }

  /**
   * Returns a credentials info object based on this challenge info and, the specified <code>user
   * </code> and <code>password</code>. The returned credentials info object would be of type {@link
   * DsSipBasicCredentialsInfo}.
   *
   * @param user the user name to be used for constructing the credentials info
   * @param password the user password to be used for constructing the credentials info
   * @return the newly constructed DsSipBasicCredentialsInfo object as per this challenge info, the
   *     specified <code>user</code> and the specified <code>password</code>.
   */
  public DsSipCredentialsInfo getCredentialsInfo(DsByteString user, DsByteString password) {
    DsSipBasicCredentialsInfo credentials = new DsSipBasicCredentialsInfo(user, password);
    credentials.setRealm(m_strRealm);
    return credentials;
  }
} // Ends class DsSipBasicChallengeInfo
