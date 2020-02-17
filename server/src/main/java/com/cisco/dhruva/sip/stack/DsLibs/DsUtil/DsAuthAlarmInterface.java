// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

/**
 * Defines the contract between the user code and the stack so that the user code will be notified
 * of the authentication retry attacks. In other words, if some intruder is trying to get
 * authenticated by trying different password combinations repeatedly, the user code will be
 * notified and thereby providing the user code ( as part of administrative tasks) the opportunity
 * to take appropriate and precautionary measures to avoid intrusion attacks. To get notified of the
 * authentication retry attacks, the digest authentication scheme should be enabled and the user
 * code should implement this interface and register with <code>DsConfigManager</code> by invoking
 * <code>DsConfigManager.registerAuthAlarmListener(DsAuthAlarmInterface)</code>. If user no longer
 * want to get notified of these authentication retry attacks then he/she can unregister by invoking
 * <code>DsConfigManager.unregisterAuthAlarmListener(DsAuthAlarmInterface)</code>. Also in the
 * Digest authentication scheme the replay option should be enabled for this mechanism and that can
 * be controlled by <code>DsSipDigestChallengeInfo.setReplay(boolean)</code>.
 */
public interface DsAuthAlarmInterface {

  /**
   * Raises the alarm to notify the administrator or the interested party that some intruder is
   * trying to be authenticated by trying different password combinations repeatedly. Thereby
   * providing the administrator or interested party the opportunity to take precautionary measures
   * to avoid authentication intrusion attacks.
   *
   * @param request the string representation of the SIP request that is made by the intruder.
   */
  void raiseAuthRetryExceeded(String request);
}
