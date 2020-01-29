// ////////////////////////////////////////////////////////////////
//
// FILENAME:    DsSipRestoreInterface.java
//
// DESCRIPTION: Trigger dynamic restoration of a dialog during failover
//              period.
//
//
// MODULE:      DsUtil
//
// AUTHOR:      Kai Wang (kaiw@cisco.com)
//
// COPYRIGHT:
// ============== Copyright (c) 2004 Cisco Systems, Inc. =================
// ==================== all rights reserved =======================

package com.cisco.dhruva.DsLibs.DsUtil;

/**
 * Interface must be implemented if dynamic restoration of a dialog during failover is require. If
 * dynamic restoration is not require, there is no need to implement this interface.
 */
public interface DsSipRestoreInterface {
  /**
   * Retrive an invite dialog from persistence storage.
   *
   * <p>If an inivte dialog is retrieved from persistent storage, the user <b> MUST </b> invoke
   * reconstruct method on the restored invite dialog as shown below.
   *
   * <p>
   *
   * <pre>
   * retrievedInviteDialog.reconstruct(
   * DsSipInviteDialogInterface dialogInterface,
   * DsSipReinvitationInterface reinviteInterface)
   * </pre>
   *
   * If no dialog is found, the user should return null.
   *
   * @param toTag the toTag of the dialog
   * @param fromTag the fromTag of the dialog
   * @param callId the callId of the dialog
   * @return the dialog that retrieve from presistent storage.
   */
  // REFACTOR
  //  public DsSipInviteDialog restoreInviteDialog(
  //      DsByteString toTag, DsByteString fromTag, DsByteString callId);

  /**
   * Retrive a subscribe dialog from persistence storage.
   *
   * <p>If a subscribe dialog is retrieved from persistent storage, the user <b> MUST </b> invoke
   * reconstruct method on the restored subscribe dialog as shown below.
   *
   * <p>
   *
   * <pre>
   * DsSipSubscription subs[] = retrievedSubscribeDialog.getSubscriptions();
   * for (int i = 0; i <= subs.length - 1; i++)
   * {
   * if (subs[i].getEventRole() == DsSipEventRole.SUBSCRIBER)
   * {
   * // DsSipDirectSubscribeInterface if needed. Otherwise, do nothing
   * }
   * else if (subs[i].getEventRole() == DsSipEventRole.NOTIFIER)
   * {
   * ((DsSipNotifier) subs[i]).setNotifierInterface(DsSipNotifierInterface);
   * }
   * }
   * ret.reconstruct(DsSipSubscribeInterface);
   *
   *
   * </pre>
   *
   * If no dialog is found, the user should return null.
   *
   * @param toTag the toTag of the dialog
   * @param fromTag the fromTag of the dialog
   * @param callId the callId of the dialog
   * @return the dialog that retrieve from presistent storage.
   */
  // REFACTOR
  //  public DsSipSubscribeDialog restoreSubscribeDialog(
  //      DsByteString toTag, DsByteString fromTag, DsByteString callId);
}
