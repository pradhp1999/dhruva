// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.
package com.cisco.dhruva.DsLibs.DsSipLlApi;

import com.cisco.dhruva.DsLibs.DsSipObject.*;
import com.cisco.dhruva.DsLibs.DsUtil.DsConfigManager;

/**
 * This class holds the values for various timers and retry counts for a network. Also it has the
 * getters/setters. User code should not instantiate this class. An instance of this class should be
 * obtained from DsNetwork, i.e. calling one of DsNetwork's getSipTimers() method.
 */
public final class DsSipTimers implements DsSipConstants {
  String networkName;
  int T1Value = 500; // initial delay
  int T2Value = 4000; // retry delay constant
  int T3Value = 16000; // not used in bis 7 and later
  int T4Value = 5000; // completion timeout for CLIENT_TRANS and INVITE_SERVER_TRANS

  int TU1Value = 5000; // user defined for edge proxy x state
  // timeout
  int TU2Value = 32000; // user defined completion timeout for INVITE_CLIENT_TRANS and SERVER_TRANS

  /**
   * TU3Value: timer for "delayed" provisional response for non-INVITE transactions (It is NOT
   * recommended that this value be set to a value other than Integer.MAX_VALUE as doing so will
   * cause the stack to send provisional responses for non-INVITEs. This behavior is required in
   * certain SIP networks however.)
   */
  int TU3Value = Integer.MAX_VALUE;

  // tn
  int clientTnValue = 64000; // max client transaction timeout
  int serverTnValue = 64000; // max server transaction timeout

  byte INVITE_CLIENT_TRANS_RETRY = DsConfigManager.INVITE_CLIENT_TRANS_RETRY_DEFAULT;
  byte CLIENT_TRANS_RETRY = DsConfigManager.CLIENT_TRANS_RETRY_DEFAULT;
  byte INVITE_SERVER_TRANS_RETRY = DsConfigManager.INVITE_SERVER_TRANS_RETRY_DEFAULT;

  /**
   * Default Constructor for DsSipTimers. Note that User code normally should not instantiate this
   * class. An instance of this class should be obtained from DsNetwork, i.e. calling DsNetwork's
   * getSipTimers() method.
   */
  public DsSipTimers() {
    this(null);
  }

  /**
   * Constructor for DsSipTimers. Note that User code normally should not instantiate this class. An
   * instance of this class should be obtained from DsNetwork, i.e. calling DsNetwork's
   * getSipTimers() method.
   *
   * @param networkName the name of a network.
   */
  public DsSipTimers(String networkName) {
    this.networkName = networkName;
  }
  /**
   * Sets the value for timers T1, T2, T3, T4 as specified in bis 07 and the max transaction
   * duration timer Tn.
   *
   * @param timerID the timer for which you want to set the value. Timer IDs are defined in
   *     DsSipConstants. Valid values are T1, T2, T3, T4, clientTn(for client only), serverTn(for
   *     server only) and Tn(for both client and server, deprecated).
   * @param timerValue the timer value in milliseconds.
   * @return the result of this set operation. <code>false</code> if timerID is invalid, <code>true
   *     </code> otherwise.
   */
  public boolean setTimerValue(byte timerID, int timerValue) {
    if (timerID >= T1 && timerID <= Tn) {
      switch (timerID) {
        case T1:
          T1Value = timerValue;
          break;
        case T2:
          T2Value = timerValue;
          break;
        case T3:
          T3Value = timerValue;
          break;
        case T4:
          T4Value = timerValue;
          break;
        case TU1:
          TU1Value = timerValue;
          break;
        case TU2:
          TU2Value = timerValue;
          break;
        case TU3:
          TU3Value = timerValue;
          break;
        case clientTn:
          clientTnValue = timerValue;
          break;
        case serverTn:
          serverTnValue = timerValue;
          break;
        case Tn: // deprecated
          clientTnValue = timerValue;
          serverTnValue = timerValue;
          break;
      }
      return true;
    }
    return false;
  }

  /**
   * Gets the value for timers T1, T2, T3, T4 as specified in bis 07 and the max transaction
   * duration timer Tn.
   *
   * @param timerID the timer for which you want to get the value. Timer IDs are defined in
   *     DsSipConstants. Valid values are T1, T2, T3, T4, clientTn(for client only), serverTn(for
   *     server only) and Tn(for both client and server, deprecated).
   * @return the value for the timer. -1 if the timerID is invalid
   */
  public int getTimerValue(byte timerID) {
    if (timerID >= T1 && timerID <= Tn) {
      switch (timerID) {
        case T1:
          return T1Value;
        case T2:
          return T2Value;
        case T3:
          return T3Value;
        case T4:
          return T4Value;
        case TU1:
          return TU1Value;
        case TU2:
          return TU2Value;
        case TU3:
          return TU3Value;
        case clientTn:
          return clientTnValue;
        case serverTn:
          return serverTnValue;
        case Tn: // deprecated
          return serverTnValue;
      }
    }
    return -1;
  }

  /**
   * Sets the retry count for retransmissions. For Invite client transaction, it sets the max number
   * of retransmission of INVITE. For non-Invite client transaction, it sets the max number of
   * retransmission of non-Invite request. For Invite server transaction, it sets the max number of
   * final response retransmissions.
   *
   * @param transType the type of transaction. The valid values are defined in DsSipTimers and they
   *     are INVITE_CLIENT_TRANS, CLIENT_TRANS(for non-Invite) and INVITE_SERVER_TRANS.
   * @param retryCount the number of retransmissions.
   * @return the result of this set operation. <code>false</code> if transType is invalid and <code>
   *     true</code> otherwise.
   */
  public boolean setRetryCount(byte transType, byte retryCount) {
    if (transType >= 0 && transType <= INVITE_SERVER_TRANS) {
      if (transType == INVITE_CLIENT_TRANS) {
        INVITE_CLIENT_TRANS_RETRY = retryCount;
      } else if (transType == CLIENT_TRANS) {
        CLIENT_TRANS_RETRY = retryCount;
      } else if (transType == INVITE_SERVER_TRANS) {
        INVITE_SERVER_TRANS_RETRY = retryCount;
      }

      return true;
    }
    return false;
  }

  /**
   * Gets the retry count for retransmissions. For Invite client transaction, it gets the max number
   * of retransmission of INVITE. For non-Invite client transaction, it gets the max number of
   * retransmission of non-Invite request. For Invite server transaction, it gets the max number of
   * final response retransmissions.
   *
   * @param transType the type of transaction. The valid values are defined in DsSipTimers and they
   *     are INVITE_CLIENT_TRANS, CLIENT_TRANS(for non-Invite) and INVITE_SERVER_TRANS.
   * @return the number of retransmissions for this transaction type. -1 if transaction type is
   *     invalid.
   */
  public byte getRetryCount(byte transType) {
    if (transType >= 0 && transType <= INVITE_SERVER_TRANS) {
      if (transType == INVITE_CLIENT_TRANS) {
        return INVITE_CLIENT_TRANS_RETRY;
      } else if (transType == CLIENT_TRANS) {
        return CLIENT_TRANS_RETRY;
      } else if (transType == INVITE_SERVER_TRANS) {
        return INVITE_SERVER_TRANS_RETRY;
      }
    }
    return -1;
  }

  public String toString() {
    return "Network Name = "
        + networkName
        + ", T1="
        + T1Value
        + ", T2="
        + T2Value
        + ", T3="
        + T3Value
        + ", T4="
        + T4Value
        + ", TU1="
        + TU1Value
        + ", TU2="
        + TU2Value
        + ", TU3="
        + TU3Value
        + ", clientTn="
        + clientTnValue
        + ", serverTn="
        + serverTnValue
        + "\nINVITE_CLIENT_TRANS_RETRY="
        + INVITE_CLIENT_TRANS_RETRY
        + ", CLIENT_TRANS_RETRY="
        + CLIENT_TRANS_RETRY
        + ", INVITE_SERVER_TRANS_RETRY="
        + INVITE_SERVER_TRANS_RETRY;
  }
}
