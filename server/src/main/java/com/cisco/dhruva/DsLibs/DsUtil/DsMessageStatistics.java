// Copyright (c) 2005-2015 by Cisco Systems, Inc.
// All rights reserved.
// CAFFEINE 1.0 (ranarang) - stats enhancement (EDCS-306362)

package com.cisco.dhruva.DsLibs.DsUtil;

import com.cisco.dhruva.DsLibs.DsSipLlApi.*;
import com.cisco.dhruva.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.DsLibs.DsSipObject.DsSipConstants;
import com.cisco.dhruva.DsLibs.DsSipObject.DsSipHeaderInterface;
import com.cisco.dhruva.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.DsLibs.DsSipObject.DsSipResponseCode;
import com.cisco.dhruva.DsLibs.DsSipParser.DsSipMsgParser;
import java.net.InetAddress;
import java.util.*;
import org.apache.logging.log4j.Level;

/**
 * This class provides statistics for the incoming and outgoing SIP messages. These SIP messages
 * include INVITE, ACK, BYE, REGISTER, INFO, and (100, 200, 300, ....) class responses. The messages
 * types that the user is interested in collecting statistics on can be specified by registering the
 * required messages by invoking
 *
 * <p>registerIncomingMsgs(String[]), registerDupIncomingMsgs(String[]),
 * registerOutgoingMsgs(String[]), and registerDupOutgoingMsgs(String[])
 *
 * <p>methods and passing the array of appropriate message identifiers.
 *
 * <p>For example, if a user is interested in collecting the metrics for the incoming INVITE, ACK,
 * BYE, 200 response and 300 response, he/she can do this by coding the following statements:
 *
 * <p><code>String[] options = {"INVITE","ACK","BYE","200","300"};</code><br>
 * <code>DsMessageStatistics.registerIncomingMsgs(options);</code><br>
 * <br>
 * if the user is especially interested in collecting the metrics for the incoming 200 response to
 * INVITE in addition to those stated above, he/she can do this by coding the following statements:
 *
 * <p><code>String[] options = {"INVITE","ACK","BYE","200","300", "200_INVITE"};</code><br>
 * <code>DsMessageStatistics.registerIncomingMsgs(options);</code><br>
 * <br>
 * Counts of 200 responses for INVITE will fall into 200_INVITE category. Counts of 200 response to
 * BYE and other requests will fall into generic 200 category. This way, users can distinguish
 * responses to different types of requests.
 *
 * <p>To register the message types for all the four types of statistics, user can call <code>
 * registerMsgs(String[])</code>. By default, the following message types are registered:
 *
 * <p>UNREGISTERED_REQUEST - keeps count of all the SIP requests<br>
 * UNREGISTERED_100CLASS - keeps count of all the 100 class SIP responses<br>
 * UNREGISTERED_200CLASS - keeps count of all the 200 class SIP responses<br>
 * UNREGISTERED_300CLASS - keeps count of all the 300 class SIP responses<br>
 * UNREGISTERED_400CLASS - keeps count of all the 400 class SIP responses<br>
 * UNREGISTERED_500CLASS - keeps count of all the 500 class SIP responses<br>
 * UNREGISTERED_600CLASS - keeps count of all the 600 class SIP responses<br>
 *
 * <p>If a user is interested in collecting the metrics for only some remote destinations, he/she
 * can do this by registering destinations and message types:
 *
 * <p><code>String[] dests = {"123.23.34.45", "65.54.43.32"}</code>; <code>
 * String[] options = {"INVITE","ACK","BYE","200","300", "200_INVITE"}</code>; <code>
 * DsMessageStatistics.registerMsgsForDests(dests, options);</code><br>
 *
 * <p><code>registerMsgsForDests(dests, options)</code> can be used repeatedly to specify different
 * message type options for different group of destinations.
 *
 * <p>Later you can retrieve metrics for certain destinations by calling:
 *
 * <p><code>DsMessageStatistics.getStatisticsForDests(dests);</code><br>
 *
 * <p>If you only want to get a count for a certain message type for a destination, use
 * getMsgsCountForADest(). For example, if you want to get count for unique outgoing 200 to INVITE
 * response for destination 123.23.34.45 for the current interval, call <code>
 * getMsgsCountForADest(true, false, false, "123.23.34.45", "200_INVITE");</code> See the Javadoc of
 * that method for more explanation.
 *
 * <p>Note that if a user registers for certain destinations, metrics for messages to and from other
 * destinations will not be collected. Also You can set options array to null when you call
 * registerMsgsForDests(dests, options). In that case, default message types are used. For example,
 * if you do:
 *
 * <p><code>String[] dests = {"123.23.34.45"};</code> <code>String[] options = null;</code> <code>
 * DsMessageStatistics.registerMsgsForDests(dests, options);</code><br>
 *
 * <p>When you call getStatisticsForDests(dests) to get a report, message types showing up will be:
 *
 * <p>123.23.34.45_REQUEST<br>
 * 123.23.34.45_100CLASS<br>
 * 123.23.34.45_200CLASS<br>
 * 123.23.34.45_300CLASS<br>
 * 123.23.34.45_400CLASS<br>
 * 123.23.34.45_500CLASS<br>
 * 123.23.34.45_600CLASS<br>
 *
 * <p>Also, the user can specify the time interval for which the statistics need to be collected.
 * Suppose that the user specifies the time interval of 10 seconds by invoking
 *
 * <p><code>DsMessageStatistics.setStatsInterval(10)</code>,
 *
 * <p>then after every 10 secs the collected metrics will be reset to 0.
 *
 * <p>By default, the message statistics are disabled.
 *
 * <p>User code must invoke
 *
 * <p><code>DsMessageStatistics.setEnabled(boolean)</code> and <code>
 * DsMessageStatistics.setStatsInterval(int)</code>,
 *
 * <p>if interested in the message statistics. By default, the statistics reset interval is 4 hrs.
 *
 * <p>Also, once the user is no longer interested in the message statistics, he/she should invoke
 *
 * <p><code>DsMessageStatistics.clearAll()</code>
 *
 * <p>to stop the timer and free the resources.
 *
 * @since SIP User Agent Java v5.0
 */
public final class DsMessageStatistics implements DsSipConstants {

  // default in non-dest mode
  private static Table3D[] current = new Table3D[4];
  private static Table3D[] previous = new Table3D[4];
  private static HashMap destMap;
  private static final byte INCOMING = 0;
  private static final byte OUTGOING = 1;
  private static final byte DUP_INCOMING = 2;
  private static final byte DUP_OUTGOING = 3;
  private static Vector[] msgOptions = new Vector[4];
  private static Object resettingLock = new Object();
  private static Object resetPeriodLock = new Object();

  private static boolean enabled;
  private static volatile int statsInterval;
  private static int intervalCount;
  private static long startTimeForCurrentInterval;
  // qfang - 02.08.06 - startTimeForCurrentInterval is already used by
  // reset() so define a different variable for snapshot
  private static long lastSnapshotTime;

  private static Timer timer;
  private static TimerTask task;

  private static DsMessageStatsInterface messageStatsInterface;
  private static DsMessageLoggingInterface loggingInterface;

  /** Number of columns, 7 request plus 6 response classes. */
  static final int DEFAULT_COL_NUM = 7; // request, 100CLASS ... 600CLASS

  // public final static int DROP_BIT = 0x10000;

  /** Set to <code>true</code> to keep count of dropped messages. */
  public static boolean m_countDropped = true;

  // CAFFEINE 1.0 - stats enhancement
  public static final byte DS_SIP_SUMMARY_REQ = 1;
  public static final byte DS_SIP_SUMMARY_RESP = 2;
  public static final byte DS_SIP_RETRY_FINAL_RESP = 3;
  public static final byte DS_SIP_RETRY_NON_FINAL_RESP = 4;
  public static final byte DS_SIP_STATUS_CLASS_1XX = 1;
  public static final byte DS_SIP_STATUS_CLASS_2XX = 2;
  public static final byte DS_SIP_STATUS_CLASS_3XX = 3;
  public static final byte DS_SIP_STATUS_CLASS_4XX = 4;
  public static final byte DS_SIP_STATUS_CLASS_5XX = 5;
  public static final byte DS_SIP_STATUS_CLASS_6XX = 6;

  private static boolean resetAllowed = false;

  private static final String[] respCodes = {
    "100", "180", "181", "182", "183", "200", "202", "300", "301", "302", "303", "305", "380",
    "400", "401", "402", "403", "404", "405", "406", "407", "408", "409", "410", "411", "413",
    "414", "415", "416", "420", "423", "480", "481", "482", "483", "484", "485", "486", "487",
    "488", "489", "491", "493", "500", "501", "502", "503", "504", "505", "600", "603", "604", "606"
  };

  // static block which registers the default message types for non-dest
  static {
    String[] msgs = {
      "UNREGISTERED_REQUEST", "UNREGISTERED_100CLASS", "UNREGISTERED_200CLASS",
      "UNREGISTERED_300CLASS", "UNREGISTERED_400CLASS", "UNREGISTERED_500CLASS",
      "UNREGISTERED_600CLASS"
    };
    registerMsgs(msgs);
    setStatsInterval(60 * 60 * 4);
  }

  /**
   * Registers the messages for which the metrics needs to be collected for specified destinations.
   * The interested destinations and message types can be specified in the form of an array of
   * strings, each string specifying the message type the user is interested in collecting the
   * metrics for those destinations. Note that by using this method, statistics about messages to
   * and from those destinations will be collected, regardless whether they are retransmission or
   * not. Note that Do not call this method more than once.
   *
   * @param dests a group of destinations users are interested for which they want to collect
   *     metrics for the same message type group.
   * @param options the message types that users are interested in collecting the metrics for.
   */
  public static void registerMsgsForDests(String[] dests, String[] options) {
    // registerDestinations(dests);

    String aDest = null;
    String[] msgs = null;
    String[] newOptions = null;
    if (options != null) {
      newOptions = new String[options.length];
      destMap = new HashMap(options.length);
    }

    for (int m = 0; m < 4; m++) {
      // since we register for certain dests, we do not care about UNREGISTER_XXX
      if (msgOptions[m] != null) msgOptions[m].clear();
      current[m] = new Table3D(dests.length, DsSipConstants.METHOD_NAMES_SIZE, DEFAULT_COL_NUM);
      current[m].createResponseMapping(null);
      current[m].expand(options);
      previous[m] = (Table3D) current[m].clone();
    }
    for (int i = 0; i < dests.length; i++) {
      aDest = dests[i];

      // register default first
      msgs =
          new String[] {
            aDest + "_REQUEST",
            aDest + "_100CLASS",
            aDest + "_200CLASS",
            aDest + "_300CLASS",
            aDest + "_400CLASS",
            aDest + "_500CLASS",
            aDest + "_600CLASS"
          };

      registerMsgs(msgs);
      if (options != null) {
        // register user options
        for (int j = 0; j < options.length; j++) {
          newOptions[j] = aDest + "_" + options[j];
        }
        registerMsgs(newOptions);
      }

      destMap.put(dests[i].trim(), new Integer(i));
    }
  }

  /**
   * This method is used to adjust remaining destination when of the destination is unset for peg
   * count collection. Method adjust the position of the destination index for array3D. It also,
   * calls a method that removes all the messages for deleted destination which were registed.
   *
   * @param String destID :Destination ID of the destination which is unset.
   * @authtor :Persistent @Date : 29/12/2003 For CR: 8590
   */
  private static int adjustDestination(String destID) {
    int i = 0;
    int keyVal = 0;
    String destKey = null;
    HashMap newMap = new HashMap();

    if (destMap.containsKey(destID.trim())) {
      keyVal = ((Integer) destMap.get(destID.trim())).intValue();
    }
    Iterator itr = (destMap.keySet()).iterator();
    while (itr.hasNext()) {
      destKey = (String) itr.next();
      if (destKey.equals(destID)) continue;
      else {
        newMap.put(destKey.trim(), new Integer(i));
        i++;
      }
    }
    destMap.clear();
    destMap.putAll(newMap);
    // remove all the message options related to the destination
    unregisterMsg(destID);
    return keyVal;
  }

  /**
   * This method is used to remove all the messages related to destination, which is unset, from all
   * the msgOption array.
   *
   * @param String destId :Destination ID of the destination which is unset.
   * @authtor :Persistent @Date : 29/12/2003 For CR: 8590
   */
  private static void unregisterMsg(String destId) {
    String msgOpt = null;
    Vector destMsg = new Vector();
    for (int m = 0; m < 4; m++) {
      // Removes Msg for dest
      destMsg.clear();
      for (Enumeration e = msgOptions[m].elements(); e.hasMoreElements(); ) {
        msgOpt = (String) e.nextElement();
        if (msgOpt.startsWith(destId.trim())) destMsg.add(msgOpt);
      }
      msgOptions[m].removeAll(destMsg);
    }
  }

  /**
   * This method is used to unset a destination for peg count collection.
   *
   * @param destId Destination ID of the destination which is unset.
   * @param options Msg options specified by user
   * @authtor :Persistent @Date : 29/12/2003 For CR: 8590
   */
  public static boolean unregisterDestination(String destID, String[] options) {
    boolean removeSuccess = false;
    int idxDest = 0;

    // There are no destination to unset peg count collection
    if (destMap == null || destMap.isEmpty()) return removeSuccess;

    if (!destMap.containsKey(destID)) return removeSuccess;
    else idxDest = adjustDestination(destID);

    if (destMap == null || destMap.isEmpty()) {
      // It was last destination to be unregistered so register the msg options again irrespective
      // of any destination
      for (int m = 0; m < 4; m++) {
        msgOptions[m].clear();
        registerDefaultMsg(m); // added for 8626
        current[m] = new Table3D(1, DsSipConstants.METHOD_NAMES_SIZE, DEFAULT_COL_NUM);
        current[m].createResponseMapping(null);
        current[m].expand(options);
        previous[m] = (Table3D) current[m].clone();
      }
      registerMsgs(options);
    } else {
      Table3D tempCurrent = null;
      Table3D tempPrevious = null;

      // clear peg count for the destination unregistered.
      for (int m = 0; m < 4; m++) {
        tempCurrent = (Table3D) current[m].clone();
        tempPrevious = (Table3D) previous[m].clone();
        current[m] =
            new Table3D(
                tempCurrent.destNum - 1, DsSipConstants.METHOD_NAMES_SIZE, tempCurrent.respNum);
        previous[m] =
            new Table3D(
                tempPrevious.destNum - 1, DsSipConstants.METHOD_NAMES_SIZE, tempPrevious.respNum);
        current[m].deleteDestination(tempCurrent, idxDest);
        previous[m].deleteDestination(tempPrevious, idxDest);
      }
    }
    return true;
  }

  /**
   * This method is used to register a destination for peg count collection.
   *
   * @param destId Destination ID of the destination which is unset.
   * @param options Msg options specified by user
   * @author Persistent For CR 8590
   */
  public static boolean registerDestination(String destID, String[] options) {
    boolean addedSuccess = false;
    boolean noDestinations = false;
    String[] msgs = null;
    String[] newOptions = null;

    if (options != null) newOptions = new String[options.length];

    if (destMap == null || destMap.isEmpty()) noDestinations = true;

    if (!noDestinations && destMap.containsKey(destID)) return addedSuccess;
    else if (destMap == null) {
      destMap = new HashMap();
    }

    if (!destMap.containsKey(destID)) {
      int maxIndex = getIndex();
      // Add new destination to last Index +1
      destMap.put(destID.trim(), new Integer(maxIndex + 1));
    }

    if (noDestinations) {
      // Here the previous peg count, Which was irrespective of any specific destination will be
      // lost.
      for (int m = 0; m < 4; m++) {
        msgOptions[m].clear();
        current[m] = new Table3D(1, DsSipConstants.METHOD_NAMES_SIZE, DEFAULT_COL_NUM);
        current[m].createResponseMapping(null);
        current[m].expand(options);
        previous[m] = (Table3D) current[m].clone();
      }
    } else {
      Table3D tempCurrent = null;
      Table3D tempPrevious = null;
      // If there are already destination available then copy the dat n add another destination
      for (int m = 0; m < 4; m++) {
        tempCurrent = (Table3D) current[m].clone();
        tempPrevious = (Table3D) previous[m].clone();
        current[m] =
            new Table3D(
                tempCurrent.destNum + 1, DsSipConstants.METHOD_NAMES_SIZE, tempCurrent.respNum);
        previous[m] =
            new Table3D(
                tempPrevious.destNum + 1, DsSipConstants.METHOD_NAMES_SIZE, tempPrevious.respNum);
        current[m].copy(tempCurrent);
        previous[m].copy(tempPrevious);
      }
    }
    // register default first
    msgs =
        new String[] {
          destID + "_REQUEST",
          destID + "_100CLASS",
          destID + "_200CLASS",
          destID + "_300CLASS",
          destID + "_400CLASS",
          destID + "_500CLASS",
          destID + "_600CLASS"
        };

    registerMsgs(msgs);
    if (options != null) {
      // register user options
      for (int j = 0; j < options.length; j++) {
        newOptions[j] = destID + "_" + options[j];
      }
      registerMsgs(newOptions);
    }
    return true;
  }

  /**
   * This method is used to get the maximum index where a destination can be registered.
   *
   * @authtor :Persistent @Date : 30/12/2003 For CR: 8590
   */
  private static int getIndex() {
    int maxIndex = -1; // Changed so that when there are no destination maxIndex + 1 should give 0
    Iterator itr = (destMap.values()).iterator();

    while (itr.hasNext()) {
      Integer i = (Integer) itr.next();
      if (maxIndex < i.intValue()) maxIndex = i.intValue();
    }
    return maxIndex;
  }

  /**
   * This method is used to register the Default messages "Unregister".
   *
   * @authtor :Persistent @Date : 24/02/2004 For CR: 8626
   */
  private static void registerDefaultMsg(int msgIndex) {
    boolean b_requiredDef = false;
    String[] msgs = {
      "UNREGISTERED_REQUEST",
      "UNREGISTERED_100CLASS",
      "UNREGISTERED_200CLASS",
      "UNREGISTERED_300CLASS",
      "UNREGISTERED_400CLASS",
      "UNREGISTERED_500CLASS",
      "UNREGISTERED_600CLASS"
    };

    b_requiredDef = isDefaultMsgsRegistered(msgIndex);
    if (!b_requiredDef) {
      for (int i = 0; i < msgs.length; i++) {
        msgOptions[msgIndex].add(msgs[i]);
      }
    }
  }

  private static boolean isDefaultMsgsRegistered(int msgIndex) {
    boolean defaultMsgExist = false;
    if (destMap == null
        || destMap.isEmpty()) { // If this message is registered,then default mesg are registered.
      if (msgOptions[msgIndex].contains("UNREGISTERED_100CLASS")) defaultMsgExist = true;
    } else // If there are destination registered for peg count then the default msg for dest are
    // registered
    {
      defaultMsgExist = true;
    }

    return defaultMsgExist;
  }

  /**
   * Registers the messages for which the metrics needs to be collected. The interested message
   * types can be specified in the form of an array of strings, each string specifying the message
   * type, the user is interested in collecting the metrics for.
   *
   * @param options the message types that the user is interested in collecting the metrics for.
   */
  public static void registerMsgs(String[] options) {
    registerIncomingMsgs(options);
    registerDupIncomingMsgs(options);
    registerOutgoingMsgs(options);
    registerDupOutgoingMsgs(options);
  }

  /**
   * Registers the incoming messages for which the metrics needs to be collected. The interested
   * message types can be specified in the form of an array of strings, each string specifying the
   * incoming message type, the user is interested in collecting the metrics for.
   *
   * @param options the incoming message types that the user is interested in collecting the
   *     metrics.
   */
  public static void registerIncomingMsgs(String[] options) {
    if (null == options || options.length == 0) {
      return;
    }

    registerMsgOptions(INCOMING, options);
  }

  /**
   * Registers the incoming messages for which the metrics needs to be collected. The interested
   * message types can be specified in the form of an array of strings, each string specifying the
   * incoming message type, the user is interested in collecting the metrics for.
   *
   * @param options the incoming message types that the user is interested in collecting the
   *     metrics.
   */
  public static void registerOutgoingMsgs(String[] options) {
    if (null == options || options.length == 0) {
      return;
    }

    registerMsgOptions(OUTGOING, options);
  }

  /**
   * Registers the incoming messages for which the metrics needs to be collected. The interested
   * message types can be specified in the form of an array of strings, each string specifying the
   * incoming message type, the user is interested in collecting the metrics for.
   *
   * @param options the incoming message types that the user is interested in collecting the
   *     metrics.
   */
  public static void registerDupIncomingMsgs(String[] options) {
    if (null == options || options.length == 0) {
      return;
    }

    registerMsgOptions(DUP_INCOMING, options);
  }

  /**
   * Registers the incoming messages for which the metrics needs to be collected. The interested
   * message types can be specified in the form of an array of strings, each string specifying the
   * incoming message type, the user is interested in collecting the metrics for.
   *
   * @param options the incoming message types that the user is interested in collecting the
   *     metrics.
   */
  public static void registerDupOutgoingMsgs(String[] options) {
    if (null == options || options.length == 0) {
      return;
    }

    registerMsgOptions(DUP_OUTGOING, options);
  }

  private static void registerMsgOptions(byte tableID, String[] options) {
    if (msgOptions[tableID] == null) {
      msgOptions[tableID] = new Vector(options.length);
    }
    for (int i = 0; i < options.length; i++) {
      msgOptions[tableID].add(options[i]);
    }
    if (destMap == null) {
      if (current[tableID] == null) // default msgTypes
      {
        registerDefaultMsg(tableID); // added for 8626
        current[tableID] =
            new Table3D(
                1,
                DsSipConstants.METHOD_NAMES_SIZE,
                DEFAULT_COL_NUM); // bug 8626 option.length to DEFAULT_COL_NUM
        current[tableID].createResponseMapping(null); // bug 8626 options to null
        // Persistent : Added for Bug :8626
        current[tableID].expand(options);

      } else {
        current[tableID].expand(options);
      }

      previous[tableID] = (Table3D) current[tableID].clone();
    }
    // else registerMsgsForDests() will create 3D array for multiple dests
    return;
  }

  // qfang - 12.01.05 - Bugid used: CSCsc63090 Cannot retrieve UNREGISTERED counters from
  // DsMessageStatistics
  /**
   * A convenience method for registering all response code Note that by calling this method all the
   * "unregistered" stats for reponse classes("UNREGISTERED_X00CLASS") are not retrievable. To get
   * that, call getIncomingStatusCodeClassMsgCount() or getOutgoingStatusCodeClassMsgCount() instead
   */
  public static void registerAllResponseCode() {
    registerMsgs(respCodes);
  }

  private static byte getTableID(boolean duplicate, boolean incoming) {
    if (!duplicate) {
      if (incoming) return INCOMING;
      else return OUTGOING;
    } else {
      if (incoming) return DUP_INCOMING;
      else return DUP_OUTGOING;
    }
  }

  private static Table3D getTable(boolean isCurrent, boolean duplicate, boolean incoming) {
    synchronized (resettingLock) {
      if (isCurrent) {
        return current[getTableID(duplicate, incoming)];
      } else {
        return previous[getTableID(duplicate, incoming)];
      }
    }
  }

  private static Table3D getTable(boolean isCurrent, byte tableID) {
    synchronized (resettingLock) {
      if (isCurrent) {
        return current[tableID];
      } else {
        return previous[tableID];
      }
    }
  }

  /**
   * Updates the messages statistics based on the specified <code>method</code> of the SIP request
   * and the options, if the request is duplicate, as a result of retransmission in case of UDP, and
   * if the request is received or sent. At the very first step, the request is checked for, if its
   * one of the request that the user has opted to collect the metrics for. If its the one of them,
   * then the corresponding counter is incremented.
   *
   * @param method the integer method type of the SIP request that is either received or sent
   * @param duplicate if <code>true</code> then the request is a retransmission, otherwise its the
   *     original request
   * @param incoming if <code>true</code> then the request is received, otherwise the request is
   *     sent.
   */
  private static void updateRequestStat(int method, boolean duplicate, boolean incoming) {
    if (!enabled) {
      return;
    }
    Table3D table = getTable(true, duplicate, incoming);
    if (table == null) return;
    table.get(0, method, 0).incCount();
  }

  /**
   * Updates the messages statistics based on the specified <code>method</code> of the SIP request
   * and the options, if the request is duplicate, as a result of retransmission in case of UDP, and
   * if the request is received or sent. At the very first step, the request is checked for, if its
   * one of the request that the user has opted to collect the metrics for. If its the one of them,
   * then the corresponding counter is incremented.
   *
   * @param method the integer method type of the SIP request that is either received or sent
   * @param duplicate if <code>true</code> then the request is a retransmission, otherwise its the
   *     original request
   * @param incoming if <code>true</code> then the request is received, otherwise the request is
   *     sent.
   * @param binding binding information which is useful when metrics for registered destinations
   *     need to be collected.
   */
  public static void updateRequestStat(
      int method, boolean duplicate, boolean incoming, DsBindingInfo binding) {
    if (!enabled) return;
    if (destMap == null) {
      updateRequestStat(method, duplicate, incoming);
      return;
    }
    if (binding == null) return;
    Table3D table = getTable(true, duplicate, incoming);
    if (table == null) return;
    String destStr = binding.getRemoteAddressStr();
    Integer anInt = (Integer) destMap.get(destStr);
    if (anInt == null) return;

    table.get(anInt.intValue(), method, 0).incCount();
  }

  /**
   * Updates the messages statistics based on the specified <code>scode</code> and <code>
   * method</code> of the SIP response and the options, if the response is duplicate, as a result of
   * retransmission in case of UDP, and if the response is received or sent. At the very first step,
   * the response is checked for, if its one of the status codes that the user has opted to collect
   * the metrics for. If its the one of them, then the corresponding counter is incremented.
   *
   * @param scode the integer status code of the SIP response that is either received or sent
   * @param method the method in the CSeq header of the response.
   * @param duplicate if <code>true</code> then the response is a retransmission, otherwise its the
   *     original response
   * @param incoming if <code>true</code> then the response is received, otherwise the response is
   *     sent.
   */
  private static void updateResponseStat(
      int scode, int method, boolean duplicate, boolean incoming) {
    if (!enabled) return;
    Table3D table = getTable(true, duplicate, incoming);
    if (table == null) return;
    int respIndex = table.getResponseMapping(scode);
    table.get(0, method, respIndex).incCount();
  }

  /**
   * Updates the messages statistics based on the specified <code>scode</code> and <code>
   * method</code> of the SIP response and the options, if the response is duplicate, as a result of
   * retransmission in case of UDP, and if the response is received or sent. At the very first step,
   * the response is checked for, if its one of the status codes that the user has opted to collect
   * the metrics for. If its the one of them, then the corresponding counter is incremented.
   *
   * @param scode the integer status code of the SIP response that is either received or sent
   * @param method the method in the CSeq header of the response.
   * @param duplicate if <code>true</code> then the response is a retransmission, otherwise its the
   *     original response
   * @param incoming if <code>true</code> then the response is received, otherwise the response is
   *     sent.
   * @param addr remote destination address which is useful when metrics for registered destinations
   *     need to be collected.
   */
  public static void updateResponseStat(
      int scode, int method, boolean duplicate, boolean incoming, InetAddress addr) {
    if (!enabled) return;
    if (destMap == null) // non-dest
    {
      updateResponseStat(scode, method, duplicate, incoming);
      return;
    }
    if (addr == null) return;
    Table3D table = getTable(true, duplicate, incoming);
    if (table == null) return;
    String destStr = addr.getHostAddress();
    Integer anInt = (Integer) destMap.get(destStr);
    if (anInt == null) return;
    int respIndex = table.getResponseMapping(scode);
    table.get(anInt.intValue(), method, respIndex).incCount();
  }

  /**
   * Updates the messages statistics based on the specified <code>scode</code> and <code>
   * method</code> of the SIP response and the options, if the response is duplicate, as a result of
   * retransmission in case of UDP, and if the response is received or sent. At the very first step,
   * the response is checked for, if its one of the status codes that the user has opted to collect
   * the metrics for. If its the one of them, then the corresponding counter is incremented.
   *
   * @param scode the integer status code of the SIP response that is either received or sent
   * @param method the method in the CSeq header of the response.
   * @param duplicate if <code>true</code> then the response is a retransmission, otherwise its the
   *     original response
   * @param incoming if <code>true</code> then the response is received, otherwise the response is
   *     sent.
   * @param binding binding information which is useful when metrics for registered destinations
   *     need to be collected.
   */
  public static void updateResponseStat(
      int scode, int method, boolean duplicate, boolean incoming, DsBindingInfo binding) {
    if (!enabled) return;
    if (destMap == null) // non-dest
    {
      updateResponseStat(scode, method, duplicate, incoming);
      return;
    }
    if (binding == null) {
      return;
    }

    updateResponseStat(scode, method, duplicate, incoming, binding.getRemoteAddress());
  }

  /**
   * Updates the messages statistics based on the specified <code>message</code> and the options, if
   * the message is duplicate, as a result of retransmission in case of UDP, and if the message is
   * received or sent. At the very first step, the message is checked for, if its one of the message
   * types that the user has opted to collect the metrics for. If its the one of them, then the
   * corresponding counter is incremented.
   *
   * @param message the SIP message that is either received or sent.
   * @param duplicate if <code>true</code> then the <code>message</code> is a retransmission,
   *     otherwise its the original message
   * @param incoming if <code>true</code> then the <code>message</code> is received, otherwise the
   *     <code>message</code> is sent.
   */
  public static void updateStats(DsSipMessage message, boolean duplicate, boolean incoming) {

    if (!incoming && message.isRequest()) {
      DsSipHeaderInterface route1 = message.getHeader(DsSipConstants.ROUTE);
      if (route1 == null) RoutedCallsSNMP.incrementCallsRouted();
    }

    // The statistics are not updated if is disabled.
    if (!enabled) {
      return;
    }

    if (null == message) {
      return;
    }

    if (message.isRequest()) {
      updateRequestStat(message.getMethodID(), duplicate, incoming, message.getBindingInfo());

    } else {
      updateResponseStat(
          ((DsSipResponse) message).getStatusCode(),
          message.getMethodID(),
          duplicate,
          incoming,
          message.getBindingInfo());
    }
  }

  /**
   * Notifies the underlying SIP Message Logging Interface about the specified <code>request</code>
   * and whether its incoming or outgoing as specified by <code>direction</code> flag and what was
   * the reason of this request as specified by <code>reason</code> whose possible values are
   * defined in {@link DsMessageLoggingInterface}.
   *
   * @param reason The reason of the request being received or sent. The various possible values are
   *     defined in {@link DsMessageLoggingInterface}.
   * @param direction if <code>DsMessageLoggingInterface.DIRECTION_IN</code> then the <code>request
   *     </code> was received, otherwise the <code>request</code> was sent.
   * @param request the SIP request that is either received or sent.
   */
  public static void logRequest(int reason, byte direction, DsSipRequest request) {
    if (null == request || null == loggingInterface) {
      return;
    }
    reason = getReason(request, reason);
    loggingInterface.logRequest(reason, direction, request);
  }

  /**
   * Notifies the underlying SIP Message Logging Interface about the specified <code>request</code>
   * and whether its incoming or outgoing as specified by <code>direction</code> flag and what was
   * the reason of this request as specified by <code>reason</code> whose possible values are
   * defined in {@link DsMessageLoggingInterface}.
   *
   * @param reason The reason of the request being received or sent. The various possible values are
   *     defined in {@link DsMessageLoggingInterface}.
   * @param direction if <code>DsMessageLoggingInterface.DIRECTION_IN</code> then the <code>request
   *     </code> was received, otherwise the <code>request</code> was sent.
   * @param bytes the SIP request byte array that is either received or sent.
   * @param method the request method.
   * @param bindingInfo the network binding information.
   */
  public static void logRequest(
      int reason, byte direction, byte[] bytes, int method, DsBindingInfo bindingInfo) {
    if (null == bytes || null == loggingInterface) {
      return;
    }
    loggingInterface.logRequest(reason, direction, bytes, method, bindingInfo);
  }

  /**
   * Notifies the underlying SIP Message Logging Interface about the specified <code>response</code>
   * and whether its incoming or outgoing as specified by <code>direction</code> flag and what was
   * the reason of this response as specified by <code>reason</code> whose possible values are
   * defined in {@link DsMessageLoggingInterface}.
   *
   * @param reason The reason of the response being received or sent. The various possible values
   *     are defined in {@link DsMessageLoggingInterface}.
   * @param direction if <code>DsMessageLoggingInterface.DIRECTION_IN</code> then the response was
   *     received, otherwise the response was sent.
   * @param response the SIP response that is either received or sent.
   * @param request the SIP request that is either received or sent.
   */
  public static void logResponse(
      int reason, byte direction, DsSipResponse response, DsSipRequest request) {
    if (null == response || null == loggingInterface) {
      return;
    }
    int responseReason = getReason(response, reason);
    loggingInterface.logResponse(responseReason, direction, response, request);
  }

  public static void logResponse(int reason, byte direction, DsSipResponse response) {
    if (null == response || null == loggingInterface) {
      return;
    }
    reason = getReason(response, reason);
    loggingInterface.logResponse(reason, direction, response);
  }

  /**
   * Notifies the underlying SIP Message Logging Interface about the specified response <code>bytes
   * </code> and whether its incoming or outgoing as specified by <code>direction</code> flag and
   * what was the reason of this response as specified by <code>reason</code> whose possible values
   * are defined in {@link DsMessageLoggingInterface}.
   *
   * @param reason The reason of the response being received or sent. The various possible values
   *     are defined in {@link DsMessageLoggingInterface}.
   * @param direction if <code>DsMessageLoggingInterface.DIRECTION_IN</code> then the response was
   *     received, otherwise the response was sent.
   * @param bytes the SIP response bytes that is either received or sent.
   * @param statusCode the response status code.
   * @param method the response method name as defined by the CSeq header method name value.
   * @param bindingInfo the network binding information.
   */
  public static void logResponse(
      int reason,
      byte direction,
      byte[] bytes,
      int statusCode,
      int method,
      DsBindingInfo bindingInfo,
      DsSipRequest request) {
    if (null == bytes || null == loggingInterface) {
      return;
    }
    loggingInterface.logResponse(
        reason, direction, bytes, statusCode, method, bindingInfo, request);
  }

  public static void logResponse(
      int reason,
      byte direction,
      byte[] bytes,
      int statusCode,
      int method,
      DsBindingInfo bindingInfo) {
    if (null == bytes || null == loggingInterface) {
      return;
    }
    loggingInterface.logResponse(reason, direction, bytes, statusCode, method, bindingInfo);
  }

  /**
   * Update the dropped response statistics.
   *
   * @param scode the status code of the response
   * @param duplicate <code>true</code> if this is a duplicate request
   * @param incoming <code>true</code> if this is an incoming request
   */
  public static void updateDroppedResponseStat(int scode, boolean duplicate, boolean incoming) {
    if (!enabled) return;
    Table3D table = getTable(true, duplicate, incoming);
    if (table == null) return;
    int respIndex = table.getResponseMapping(scode);
    // treat its method as UNKNOWN
    table.get(0, UNKNOWN, respIndex).incDropCount();
  }

  /**
   * Update the dropped request statistics.
   *
   * @param method the method ID of the dropped message
   * @param duplicate <code>true</code> if this is a duplicate request
   * @param incoming <code>true</code> if this is an incoming request
   */
  public static void updateDroppedRequestStat(int method, boolean duplicate, boolean incoming) {
    if (!enabled) {
      return;
    }
    Table3D table = getTable(true, duplicate, incoming);
    if (table == null) return;
    table.get(0, method, 0).incDropCount();
  }

  /**
   * Determines if dropped messages are counted or not.
   *
   * @param val <code>true</code> to count dropped messages
   */
  public static void setCountDropped(boolean val) {
    m_countDropped = val;
  }

  /**
   * Gets the number of message dropped.
   *
   * @return the number of message dropped.
   */
  public static boolean getCountDropped() {
    return m_countDropped;
  }

  /**
   * A quick and dirty hack -- for tracking dropped incoming messages ONLY.
   *
   * @param msg the msg that was dropped.
   */
  public static void updateStats(byte[] msg) {
    if (!m_countDropped) {
      return;
    }

    int start = 0, i = 0; // beginning of method name (request) or SIP version (response)

    if (msg[i] == 'S'
        && // Response
        msg[i + 1] == 'I'
        && msg[i + 2] == 'P'
        && msg[i + 3] == '/'
        && msg[i + 4] == '2'
        && msg[i + 5] == '.'
        && msg[i + 6] == '0'
        && msg[i + 7] == ' ') {
      // move to the first char after the space
      i = i + 8;
      // always a 3 digit status code
      updateDroppedResponseStat(
          (((msg[i++] - '0') * 100) + ((msg[i++] - '0') * 10) + (msg[i++] - '0')), false, true);
    } else // Request
    {
      // find the method name
      while (msg[i++] != ' ') {
        // just find the space
      }
      updateDroppedRequestStat(
          DsSipMsgParser.getMethod(new DsByteString(msg, start, i - start - 1)), false, true);
    }
  }

  /**
   * Returns an enumeration of all the registered messages based on the specified table. Returns
   * null if no message is registered in the specified table.
   *
   * @param tableID the table to retrieve.
   * @return an enumeration of all the registered messages in the table.
   */
  private static Enumeration registeredMsgs(byte tableID) {
    Vector v = msgOptions[tableID];
    if (null == v) return null;
    return v.elements();
  }

  /**
   * Returns an enumeration of all the registered incoming messages for which the metrics will be
   * collected. The returned enumeration will be of string type. Each string value represents the
   * corresponding message type. Returns null if no message is registered.
   *
   * @return an enumeration of all the registered incoming messages.
   */
  public static Enumeration registeredIncomingMsgs() {
    return registeredMsgs(INCOMING);
  }

  /**
   * Returns an enumeration of all the registered incoming duplicate messages for which the metrics
   * will be collected. The returned enumeration will be of string type. Each string value
   * represents the corresponding message type. Returns null if no message is registered.
   *
   * @return an enumeration of all the registered incoming duplicate messages.
   */
  public static Enumeration registeredDupIncomingMsgs() {
    return registeredMsgs(DUP_INCOMING);
  }

  /**
   * Returns an enumeration of all the registered outgoing messages for which the metrics will be
   * collected. The returned enumeration will be of string type. Each string value represents the
   * corresponding message type. Returns null if no message is registered.
   *
   * @return an enumeration of all the registered outgoing messages.
   */
  public static Enumeration registeredOutgoingMsgs() {
    return registeredMsgs(OUTGOING);
  }

  /**
   * Returns an enumeration of all the registered outgoing duplicate messages for which the metrics
   * will be collected. The returned enumeration will be of string type. Each string value
   * represents the corresponding message type. Returns null if no message is registered.
   *
   * @return an enumeration of all the registered outgoing duplicate messages.
   */
  public static Enumeration registeredDupOutgoingMsgs() {
    return registeredMsgs(DUP_OUTGOING);
  }

  /**
   * Returns the number of messages received or sent, original or duplicate and of the specified
   * <code>msgType</code>.
   *
   * @param isCurrent <code>true</code> if is current
   * @param dest the destination string
   * @param tableID the table (incoming, duplicate incoming, outgoing or duplicate outgoing) in
   *     which the message count is looked for
   * @param msgType the message type for which the count is queried
   */
  private static int getMsgsCount(boolean isCurrent, String dest, byte tableID, String msgType) {
    Table3D table = getTable(isCurrent, tableID);
    // assume msgOptions[tableID] != null when table != null
    // worry about "unregistered_request", drop_bit later
    String optionStr;
    if (dest != null) optionStr = dest + "_" + msgType;
    else optionStr = msgType;
    if (null == table) {
      return 0;
    }
    if (!msgOptions[tableID].contains(optionStr)) {
      if (DsSipMsgParser.getMethod(new DsByteString(msgType)) == DsSipConstants.UNKNOWN) return 0;
      // if user code asks for couts for a known request, do it although it has not
      // been registered. We can not do that for responses b/c we have not allocate
      // columns for specific response codes.
    }
    // parse msgType string to find the right DsCounter(s)
    // only handle non-dest cases. use getDestMsgsCount() for dest cases

    int destIndex = 0;
    if (dest != null) {
      // since msgOptions has this optionsStr, we assume destMap.get(dest) is not null
      destIndex = ((Integer) destMap.get(dest)).intValue();
    }
    int ind = msgType.indexOf('_');
    if (ind >= 0) // response code and msg type or Unregistered_XXXX
    {
      if (!msgType.startsWith("UNREG")) // probably response code and msg type
      {
        int respCode;
        try {
          respCode = Integer.parseInt(msgType.substring(0, ind));
        } catch (NumberFormatException e) {
          return 0;
        }
        int respIndex = table.getResponseMapping(respCode);
        int method = DsSipMsgParser.getMethod(new DsByteString(msgType.substring(ind + 1)));
        return table.get(destIndex, method, respIndex).getCount();
      } else {
        char aChar = msgType.charAt(13); // first char after '_'
        if (aChar >= '0' && aChar <= '9') // like UNREGISTERED_200CLASS
        {
          int respCode;
          try {
            respCode = Integer.parseInt(msgType.substring(13, 16));
          } catch (NumberFormatException e) {
            return 0;
          }

          int respIndex = respCode / 100;
          int total = 0;
          for (int i = 0; i < DsSipConstants.METHOD_NAMES_SIZE; i++) {
            total += table.get(destIndex, i, respIndex).getCount();
          }
          respIndex = table.getResponseMapping(respCode);
          if (respIndex < DEFAULT_COL_NUM) // no column for this specific response code
          return total;
          // otherwise, search for this specific column
          for (int i = 0; i < DsSipConstants.METHOD_NAMES_SIZE; i++) {
            int count = table.get(destIndex, i, respIndex).getCount();
            if (count > 0) {
              if (!msgOptions[tableID].contains(
                      "" + respCode + "_" + DsSipMsgParser.getMethod(i).toString())
                  && !msgOptions[tableID].contains(Integer.toString(respCode))) {
                total = +count;
              }
            }
          }

          return total;
        } else // UNREGISTERED_REQUEST
        {
          int total = 0;
          for (int i = 0; i < DsSipConstants.METHOD_NAMES_SIZE; i++) {
            // count only unregistered requests
            if (!msgOptions[tableID].contains(DsSipMsgParser.getMethod(i).toString())) {
              total += table.get(destIndex, i, 0).getCount();
            }
          }
          return total;
        }
      }
    } else // maybe only response code or request
    {
      char firstChar = msgType.charAt(0);
      if (firstChar >= '0' && firstChar <= '9') // probably response
      {
        int respCode;
        try {
          respCode = Integer.parseInt(msgType);
        } catch (NumberFormatException e) {
          return 0;
        }

        int respIndex = table.getResponseMapping(respCode);
        int total = 0;
        for (int i = 0; i < DsSipConstants.METHOD_NAMES_SIZE; i++) {
          total += table.get(destIndex, i, respIndex).getCount();
        }
        return total;
      } else // probably request
      {
        int requestIndex = DsSipMsgParser.getMethod(new DsByteString(msgType));
        if (requestIndex != DsSipConstants.UNKNOWN) {
          return table.get(destIndex, requestIndex, 0).getCount();
        } else {
          return 0;
        }
      }
    }
  }

  /**
   * Returns the number of incoming messages of type specified by the <code>msgType</code>, that has
   * been received during the specified status metrics time interval.
   *
   * @param msgType the type of message
   * @return the number of incoming messages of type specified by the <code>msgType</code>
   * @see DsMessageStatistics#setStatsInterval(int secs)
   * @see DsMessageStatistics#getStatsInterval()
   */
  public static int getIncomingMsgsCount(String msgType) {
    return getMsgsCount(true, null, INCOMING, msgType);
  }

  // qfang - 02.02.06 - an overloading API for easy migration for Caffeine-based application
  /**
   * Returns the number of incoming messages of type specified by the <code>msgType</code>, that has
   * been received during the specified status metrics time interval.
   *
   * @param isCurrent <code>true</code> if you want to get current message count. <code>false</code>
   *     if you want count for the last snapshot.
   * @param msgType the type of message
   * @return the number of incoming messages of type specified by the <code>msgType</code>
   * @see DsMessageStatistics#setStatsInterval(int secs)
   * @see DsMessageStatistics#getStatsInterval()
   */
  public static int getIncomingMsgsCount(boolean isCurrent, String msgType) {
    return getMsgsCount(isCurrent, null, INCOMING, msgType);
  }

  /**
   * Returns the number of duplicate incoming messages of type specified by the <code>msgType</code>
   * , that has been received during the specified status metrics time interval. The duplicate
   * messages are the result of retransmissions, to provide reliable communication, occurred when
   * the transport channel is UDP.
   *
   * @param msgType the type of message
   * @return the number of duplicate incoming messages of type specified by the <code>msgType</code>
   * @see DsMessageStatistics#setStatsInterval(int secs)
   * @see DsMessageStatistics#getStatsInterval()
   */
  public static int getDupIncomingMsgsCount(String msgType) {
    return getMsgsCount(true, null, DUP_INCOMING, msgType);
  }

  // qfang - 02.02.06 - an overloading API for easy migration for Caffeine-based application
  /**
   * Returns the number of duplicate incoming messages of type specified by the <code>msgType</code>
   * , that has been received during the specified status metrics time interval. The duplicate
   * messages are the result of retransmissions, to provide reliable communication, occurred when
   * the transport channel is UDP.
   *
   * @param isCurrent <code>true</code> if you want to get current message count. <code>false</code>
   *     if you want count for the last snapshot.
   * @param msgType the type of message
   * @return the number of duplicate incoming messages of type specified by the <code>msgType</code>
   * @see DsMessageStatistics#setStatsInterval(int secs)
   * @see DsMessageStatistics#getStatsInterval()
   */
  public static int getDupIncomingMsgsCount(boolean isCurrent, String msgType) {
    return getMsgsCount(isCurrent, null, DUP_INCOMING, msgType);
  }

  /**
   * Returns the number of outgoing messages of type specified by the <code>msgType</code>, that has
   * been sent during the specified status metrics time interval.
   *
   * @param msgType the type of message
   * @return the number of outgoing messages of type specified by the <code>msgType</code>
   * @see DsMessageStatistics#setStatsInterval(int secs)
   * @see DsMessageStatistics#getStatsInterval()
   */
  public static int getOutgoingMsgsCount(String msgType) {
    return getMsgsCount(true, null, OUTGOING, msgType);
  }

  // qfang - 02.02.06 - an overloading API for easy migration for Caffeine-based application
  /**
   * Returns the number of outgoing messages of type specified by the <code>msgType</code>, that has
   * been sent during the specified status metrics time interval.
   *
   * @param isCurrent <code>true</code> if you want to get current message count. <code>false</code>
   *     if you want count for the last snapshot.
   * @param msgType the type of message
   * @return the number of outgoing messages of type specified by the <code>msgType</code>
   * @see DsMessageStatistics#setStatsInterval(int secs)
   * @see DsMessageStatistics#getStatsInterval()
   */
  public static int getOutgoingMsgsCount(boolean isCurrent, String msgType) {
    return getMsgsCount(isCurrent, null, OUTGOING, msgType);
  }

  /**
   * Returns the number of duplicate outgoing messages of type specified by the <code>msgType</code>
   * , that has been sent during the specified status metrics time interval. The duplicate messages
   * are the result of retransmissions, to provide reliable communication, occurred when the
   * transport channel is UDP.
   *
   * @param msgType the type of message
   * @return the number of duplicate outgoing messages of type specified by the <code>msgType</code>
   * @see DsMessageStatistics#setStatsInterval(int secs)
   * @see DsMessageStatistics#getStatsInterval()
   */
  public static int getDupOutgoingMsgsCount(String msgType) {
    return getMsgsCount(true, null, DUP_OUTGOING, msgType);
  }

  // qfang - 02.02.06 - an overloading API for easy migration for Caffeine-based application
  /**
   * Returns the number of duplicate outgoing messages of type specified by the <code>msgType</code>
   * , that has been sent during the specified status metrics time interval. The duplicate messages
   * are the result of retransmissions, to provide reliable communication, occurred when the
   * transport channel is UDP.
   *
   * @param msgType the type of message
   * @return the number of duplicate outgoing messages of type specified by the <code>msgType</code>
   * @see DsMessageStatistics#setStatsInterval(int secs)
   * @see DsMessageStatistics#getStatsInterval()
   */
  public static int getDupOutgoingMsgsCount(boolean isCurrent, String msgType) {
    return getMsgsCount(isCurrent, null, DUP_OUTGOING, msgType);
  }

  /**
   * Returns the number of incoming messages of type specified by the <code>msgType</code>, that has
   * been received during the <b>last</b> specified status metrics time interval.
   *
   * @param msgType the type of message
   * @return the number of incoming messages of type specified by the <code>msgType</code>
   * @see DsMessageStatistics#setStatsInterval(int secs)
   * @see DsMessageStatistics#getStatsInterval()
   */
  public static int getLastIncomingMsgsCount(String msgType) {
    return getMsgsCount(false, null, INCOMING, msgType);
  }

  /**
   * Returns the number of duplicate incoming messages of type specified by the <code>msgType</code>
   * , that has been received during the <b>last</b> specified status metrics time interval. The
   * duplicate messages are the result of retransmissions, to provide reliable communication,
   * occurred when the transport channel is UDP.
   *
   * @param msgType the type of message
   * @return the number of duplicate incoming messages of type specified by the <code>msgType</code>
   * @see DsMessageStatistics#setStatsInterval(int secs)
   * @see DsMessageStatistics#getStatsInterval()
   */
  public static int getLastDupIncomingMsgsCount(String msgType) {
    return getMsgsCount(false, null, DUP_INCOMING, msgType);
  }

  /**
   * Returns the number of outgoing messages of type specified by the <code>msgType</code>, that has
   * been sent during the <b>last</b> specified status metrics time interval.
   *
   * @param msgType the type of message
   * @return the number of outgoing messages of type specified by the <code>msgType</code>
   * @see DsMessageStatistics#setStatsInterval(int secs)
   * @see DsMessageStatistics#getStatsInterval()
   */
  public static int getLastOutgoingMsgsCount(String msgType) {
    return getMsgsCount(false, null, OUTGOING, msgType);
  }

  /**
   * Returns the number of duplicate outgoing messages of type specified by the <code>msgType</code>
   * , that has been sent during the <b>last</b> specified status metrics time interval. The
   * duplicate messages are the result of retransmissions, to provide reliable communication,
   * occurred when the transport channel is UDP.
   *
   * @param msgType the type of message
   * @return the number of duplicate outgoing messages of type specified by the <code>msgType</code>
   * @see DsMessageStatistics#setStatsInterval(int secs)
   * @see DsMessageStatistics#getStatsInterval()
   */
  public static int getLastDupOutgoingMsgsCount(String msgType) {
    return getMsgsCount(false, null, DUP_OUTGOING, msgType);
  }

  /**
   * This method returns message count for a destination. The parameters specifies what kind of
   * messages should be counted. For example, if you want to get count for unique outgoing 200 to
   * INVITE response for destination 45.56.78.90 for the current interval, call
   * getMsgsCountForADest(true, false, false, "45.56.78.90", "200_INVITE");
   *
   * @param isCurrent <code>true</code> if you want to get message count for the current interval.
   *     <code>false</code> if you want count for the last one.
   * @param duplicate <code>true</code> if you only want to get count for retransmissions. <code>
   *     false</code> if you only want to get count for unique messages.
   * @param incoming <code>true</code> if you want to get count for incoming messages, <code>false
   *     </code> if you want to get count for outgoing ones.
   * @param aDest The destination for which you want to get message count.
   * @param msgType The message type you want to get a count for.
   * @return the message count
   */
  public static int getMsgsCountForADest(
      boolean isCurrent, boolean duplicate, boolean incoming, String aDest, String msgType) {
    byte tableID = getTableID(duplicate, incoming);
    return getMsgsCount(isCurrent, aDest, tableID, msgType);
  }

  /**
   * Gets the dropped message count for the table and type.
   *
   * @param table the table to look in
   * @param msgType the message type you want to get a count for.
   * @return the number of messages dropped.
   */
  private static int getDroppedMsgsCount(Table3D table, String msgType) {
    char firstChar = msgType.charAt(0);
    if (firstChar >= '0' && firstChar <= '9') // probably response
    {
      int respCode;
      try {
        respCode = Integer.parseInt(msgType);
      } catch (NumberFormatException e) {
        return 0;
      }

      int respIndex = table.getResponseMapping(respCode);
      // we do not support per dest stat collection for drooped msgs
      // UNKNOWN is used as the method for all dropped responses
      int total = table.get(0, UNKNOWN, respIndex).getDropCount();
      return total;
    } else // probably request
    {
      int requestIndex = DsSipMsgParser.getMethod(new DsByteString(msgType));
      if (requestIndex != DsSipConstants.UNKNOWN) {
        return table.get(0, requestIndex, 0).getDropCount();
      } else {
        return 0;
      }
    }
  }

  /**
   * Gets the dropped message count for the type.
   *
   * @param msgType the message type you want to get a count for.
   * @return the number of messages dropped.
   */
  public static int getDroppedMsgsCount(String msgType) {
    return getDroppedMsgsCount(getTable(true, false, true), msgType);
  }

  /**
   * Gets the last dropped message count for the type.
   *
   * @param msgType the message type you want to get a count for.
   * @return the number of messages dropped.
   */
  public static int getLastDroppedMsgsCount(String msgType) {
    return getDroppedMsgsCount(getTable(false, false, true), msgType);
  }

  /**
   * Sets the time interval for which the metrics need to be collected. Suppose user specify the
   * time interval of 10 seconds, then after every 10 seconds the collected metrics will be reset to
   * 0. If 0 secs is specified then the periodic reset functionality for the counters is disabled,
   * the counters will never be reset to 0.
   *
   * @param secs the time interval in seconds
   */
  public static void setStatsInterval(int secs) {
    intervalCount = -1;
    // CAFFEINE 1.0 - stats enhancement
    if (secs <= 0) {
      // Disable the periodic counters reset functionality.
      resetAllowed = false;
      return;
    } else {
      resetAllowed = true;
    }

    statsInterval = secs;
    stop();
    // user code must call setEnabled(true) to start it
    // start();
  }

  /**
   * Returns the time interval for which the metrics are collected.
   *
   * @return the time interval in seconds
   * @see DsMessageStatistics#setStatsInterval(int secs)
   */
  public static int getStatsInterval() {
    return statsInterval;
  }

  /**
   * Returns the time in seconds in the current interval.
   *
   * @return the time in seconds in the current interval
   */
  public static int getStatsTime() {
    return (int) (System.currentTimeMillis() - startTimeForCurrentInterval) / 1000;
  }

  /**
   * Allows the option of enabling or disabling the metrics collection.
   *
   * @param onOff enables the option of collecting the metrics if true, disables otherwise.
   */
  public static void setEnabled(boolean onOff) {
    enabled = onOff;
    if (enabled) {
      start();
    } else {
      stop();
    }
  }

  /**
   * Returns whether the metrics collection is enabled or not.
   *
   * @return <code>true</code> if the metrics collection is enabled, <code>false</code> otherwise.
   */
  public static boolean isEnabled() {
    return enabled;
  }

  /**
   * Clears all the elements in the hash tables and cancel the timer. It is recommended to call this
   * method if user is no more interested in collecting the message statistics.
   */
  public static void clearAll() {
    if (null != timer) {
      timer.cancel();
      timer = null;
    }
    for (int i = 0; i < 4; i++) {
      // do not need to call clear() on tables
      current[i] = null;
      previous[i] = null;
      msgOptions[i] = null;
    }
    destMap = null;
  }

  /**
   * Helper method that returns the string representation of the collected metrics in the specified
   * metrics table.
   *
   * @return the string representation of the collected metrics.
   */
  private static String getStatisticsFromATable(boolean isCurrent, byte tableID, HashSet aDestSet) {
    Table3D table;
    if (isCurrent) table = current[tableID];
    else table = previous[tableID];
    if (null == table) {
      return null;
    }
    Iterator iter = msgOptions[tableID].iterator();
    String key = null;
    StringBuffer buf = new StringBuffer();
    while (iter.hasNext()) {
      key = (String) iter.next();
      if (aDestSet != null) {
        int endIndex = key.indexOf('_');
        if (endIndex == -1) continue;
        String destName = key.substring(0, endIndex);
        if (aDestSet.contains(destName)) {
          buf.append("\n");
          buf.append(key);
          buf.append("\t = \t");
          buf.append(getMsgsCount(isCurrent, destName, tableID, key.substring(endIndex + 1)));
        }
      } else // aDestSet == null
      {
        buf.append("\n");
        buf.append(key);
        buf.append("\t = \t");
        buf.append(getMsgsCount(isCurrent, null, tableID, key));
      }
    }
    return buf.toString();
  }

  private static String getStatistics(boolean isCurrent, HashSet destSet) {
    StringBuffer buf = new StringBuffer();
    String stats = null;

    String intervalStr;
    if (destSet == null) {
      if (isCurrent) intervalStr = "Interval " + intervalCount;
      else intervalStr = "Last Interval(Interval " + (intervalCount - 1) + ")";
    } else {
      if (isCurrent) intervalStr = "certain destinations for Interval " + intervalCount;
      else
        intervalStr =
            "certain destinations for Last Interval(Interval " + (intervalCount - 1) + ")";
    }
    buf.append("Message Statistics for ")
        .append(intervalStr)
        .append(" with Interval Length ")
        .append(statsInterval)
        .append(':');
    stats = getStatisticsFromATable(isCurrent, INCOMING, destSet);
    if (null != stats) {
      buf.append("\n\nIncoming Messages:");
      buf.append(stats);
    }
    stats = getStatisticsFromATable(isCurrent, OUTGOING, destSet);
    if (null != stats) {
      buf.append("\n\nOutgoing Messages:");
      buf.append(stats);
    }
    stats = getStatisticsFromATable(isCurrent, DUP_INCOMING, destSet);
    if (null != stats) {
      buf.append("\n\nIncoming Duplicate Messages:");
      buf.append(stats);
    }
    stats = getStatisticsFromATable(isCurrent, DUP_OUTGOING, destSet);
    if (null != stats) {
      buf.append("\n\nOutgoing Duplicate Messages:");
      buf.append(stats);
    }
    return buf.toString();
  }

  /**
   * Returns the string representation of the collected metrics.
   *
   * @return the string representation of the collected metrics.
   */
  public static String getStatistics() {
    return getStatistics(true, null);
  }

  /**
   * Returns the string representation of the collected metrics for the last interval.
   *
   * @return the string representation of the collected metrics for the last interval.
   */
  public static String getLastStatistics() {
    return getStatistics(false, null);
  }

  /**
   * Returns the string representation of the collected metrics for destinations specified by
   * registerMsgsForDests(String[], String[]).
   *
   * @param dests destinations you want to collect metrics for.
   * @return the string representation of the collected metrics.
   */
  public static String getStatisticsForDests(String[] dests) {
    HashSet aDestSet = new HashSet();
    for (int i = 0; i < dests.length; i++) aDestSet.add(dests[i]);

    return getStatistics(true, aDestSet);
  }

  /**
   * Returns the string representation of the collected metrics for the last interval for
   * destinations specified by registerMsgsForDests(String[], String[]).
   *
   * @param dests destinations you want to collect metrics for.
   * @return the string representation of the collected metrics.
   */
  public static String getLastStatisticsForDests(String[] dests) {
    HashSet aDestSet = new HashSet();
    for (int i = 0; i < dests.length; i++) aDestSet.add(dests[i]);

    return getStatistics(false, aDestSet);
  }

  /**
   * This method clears message counts for all types of messages(incoming or outgoing, unique or
   * retransmissions) for both current interval and the last interval. All other settings like
   * interval count, interval value are not changed. This method automatically starts to try to
   * re-collect message statistics for current interval. If user code has called <code>
   * setEnabled(false)</code> before the invocation of this method, message statistics collection
   * remains disabled, otherwise it is still enabled.
   */
  public static void resetMsgCounts() {
    // save the enabled flag. I need to stop collecting during resetting
    boolean savedEnabledFlag = enabled;
    setEnabled(false);

    for (int i = 0; i < 4; i++) {
      if (current[i] != null) current[i].reset();
      if (previous[i] != null) previous[i].reset();
    }

    // decrement interval count so that we recollect for this interval
    if (intervalCount >= 0) {
      intervalCount--;
    }
    // start();

    setEnabled(savedEnabledFlag);
  }

  /**
   * Return the interval count. It increments for each interval. The count will roll over after
   * reaching Integer.MAX_VALUE. It will be reset to zero if the stats interval is changed.
   *
   * @return the interval count.
   */
  int getIntervalCount() {
    return intervalCount;
  }

  /**
   * set the message statistics interface so that when stats interval rolls over, user applications
   * can get notified.
   *
   * @param anInterface the implementation of DsMessageStatsInterface which will be notified when
   *     interval rolls over.
   */
  public void setMessageStatsInterface(DsMessageStatsInterface anInterface) {
    messageStatsInterface = anInterface;
  }

  /**
   * Sets the SIP Message Logging Interface that will receive the notifications for the outgoing and
   * the incoming SIP messages. To unset this interface implementation, pass <code>null</code>. If
   * this interface is set, then even if the message statistics is disabled, refer {@link
   * #isEnabled()}, the message notifications will still be passed to the logging interface.
   *
   * @param loggingInterface The SIP Message Logging Interface implementation that would be notified
   *     of the incoming and outgoing messages and thus would get the oppertunity to log the message
   *     information, for example, for billing purposes.
   * @see DsMessageLoggingInterface
   * @see #getMessageLoggingInterface()
   */
  public static void setMessageLoggingInterface(DsMessageLoggingInterface loggingInterface) {
    DsMessageStatistics.loggingInterface = loggingInterface;
  }

  /**
   * Returns the set SIP Message Logging Interface that will receive the notifications for the
   * outgoing and the incoming SIP messages. If this interface implementation is not set then it
   * would return <code>null</code>.
   *
   * @return The SIP Message Logging Interface implementation that would be notified of the incoming
   *     and outgoing messages and thus would get the oppertunity to log the message information,
   *     for example, for billing purposes.
   * @see DsMessageLoggingInterface
   * @see #setMessageLoggingInterface(DsMessageLoggingInterface)
   */
  public static DsMessageLoggingInterface getMessageLoggingInterface() {
    return loggingInterface;
  }

  /**
   * Starts the timer that will clear the collected metrics, if any, after the fixed time interval
   * specified by the user.
   */
  private static void start() {
    // CAFFEINE 1.0 - stats enhancement
    if (!resetAllowed) {
      return; // The reset functionality has been disabled.
    }

    if (timer == null) {
      // GOGONG 07.31.06 CSCsd90062 - Creates a new timer whose associated thread will be specified
      // to run as a daemon.
      timer = new Timer(true);
    }
    timer.scheduleAtFixedRate(
        task =
            new TimerTask() {
              public void run() {
                DsMessageStatistics.reset();
              }
            },
        new Date(),
        (long) statsInterval * 1000);
  }

  /**
   * Clears the collected metrics, if any, in all the hastables (incoming, duplicate incoming,
   * outgoing, duplicate outgoing). Notify apps about interval rollover when it happens.
   *
   * <p>Persistent :Implementation changed. Current and Previous are both reset. Initially only
   * current were reset to zero.
   */
  private static void reset() {
    synchronized (resettingLock) {
      for (int i = 0; i < 4; i++) {
        if (previous[i] != null) previous[i].reset();
      }
      // Commented Persistent
      // Table3D[] tmp = previous;
      // added Persistent to clear current and previous counts
      for (int i = 0; i < 4; i++) {
        if (current[i] != null) current[i].reset();
      }
      // commented Persistent
      // previous = current;
      // current = tmp; //now current is clean
    }
    // TBD : following code should be changed based on the above code.
    startTimeForCurrentInterval = System.currentTimeMillis();
    // notify app about interval rollover
    if (intervalCount == Integer.MAX_VALUE - 1) {
      intervalCount = 0;
      if (messageStatsInterface != null) messageStatsInterface.onIntervalRollover();
    } else intervalCount++;
  }
  /**
   * Persistent: This method is added to copy the current peg count snapshot to previous and clears
   * the current array for the next peg count snapshot period. Clears the collected metrics, if any,
   * in all the hastables (incoming, duplicate incoming, outgoing, duplicate outgoing). This method
   * is added for the new requirement for delta values Date : 9/March/2004
   */
  public static void resetPeriodPegCount() {
    synchronized (resetPeriodLock) {
      for (int i = 0; i < 4; i++) {
        if (previous[i] != null) {
          previous[i].reset();
          previous[i].copy(current[i]);
        }
      }
    }

    // qfang - 02.02.06 - Port CAFFEINE 1.0's stats enhancement, from now-eliminated
    // takeSnapshotForRealCounters() method
    try {
      DsSipTransactionManager tm = DsSipTransactionManager.getTransactionManager();
      tm.takeSnapshotForTransactionCount();
    } catch (NullPointerException t) {
      if (DsLog4j.exceptionCat.isEnabled(Level.WARN)) {
        DsLog4j.exceptionCat.log(Level.WARN, "DsSipTransactionManager not instantiated");
      }
    }

    // qfang - 02.08.06 - startTimeForCurrentInterval is already used by
    // reset() so use a different variable.
    lastSnapshotTime = System.currentTimeMillis();
  }

  /** Cancels the timer and stops collecting the metrics. */
  private static void stop() {
    if (task != null) {
      task.cancel();
      task = null;
    }
  }

  // qfang - 02.02.06 - added as part of statistics  enhancement in caffeine
  // 1.0 (EDCS-306362), refactored with the elimination of "shadow" table.
  /**
   * Returns the count of incoming or outgoing status code responses for the specified range of
   * status codes, that has been received/sent during the specified status metrics time duration. It
   * can be specified whether unique or/and duplicate (i.e retrasmissions) are to be counted. It can
   * be specified whether counts are required for responses to a particular method or all the
   * methods.
   *
   * @param isCurrent <code>true</code> if you want to get current message count <code>false</code>
   *     if you want count at last snapshot.
   * @param incoming <code>true</code> if incoming responses, otherwise <code>false</code> for
   *     outgoing.
   * @param startCode <code>integer</code> value for the start of the range. The range is inclusive
   *     of the bounds.
   * @param endCode <code>integer</code> value for the end of the range. The range is inclusive of
   *     the bounds.
   * @param unique <code>true</code> if unique responses are to be counted.
   * @param duplicate <code>true</code> if duplicate or retransmitted responses are to be counted.
   * @param method the response method name as defined by the CSeq header method name value. -1 if
   *     responses for all the methods are to be counted.
   */
  public static int getSipStatusCodeRangeCount(
      boolean isCurrent,
      boolean incoming,
      int startCode,
      int endCode,
      boolean unique,
      boolean duplicate,
      int method) {
    // Count all the Registered response codes
    String key = null;
    int respCode;
    int respIndex;
    int respDupIndex;
    Table3D table;
    Table3D tableDup;
    Iterator iter;
    int count = 0;
    int i = 0;

    if (incoming) {
      table = getTable(isCurrent, INCOMING);
      tableDup = getTable(isCurrent, DUP_INCOMING);
      iter = msgOptions[INCOMING].iterator();
    } else {
      table = getTable(isCurrent, OUTGOING);
      tableDup = getTable(isCurrent, DUP_OUTGOING);
      iter = msgOptions[OUTGOING].iterator();
    }

    /*
     * Skip the default columns 0 - request, 1xx - 1, 2xx - 2, ... 6xx - 6
     */
    for (int j = 0; j < DsMessageStatistics.DEFAULT_COL_NUM && iter.hasNext(); j++) {
      key = (String) iter.next();
    }

    while (iter.hasNext()) {
      key = (String) iter.next();
      respCode = Integer.parseInt(key);
      if (respCode < startCode || respCode > endCode) {
        continue;
      }
      respIndex = table.getResponseMapping(respCode);
      respDupIndex = tableDup.getResponseMapping(respCode);

      if (method != -1 && method < METHOD_NAMES_SIZE) {
        i = method;
      } else {
        i = 0;
      }

      while (i < DsSipConstants.METHOD_NAMES_SIZE) {
        if (unique) {
          count += table.get(0, i, respIndex).getCount();
        }
        if (duplicate) {
          count += tableDup.get(0, i, respDupIndex).getCount();
        }
        if (i == method) {
          break;
        } else {
          i++;
        }
      }
    }
    return count;
  }

  /**
   * Returns the total number of SIP request or response messages received by the SIP entity
   * including retransmissions.
   *
   * @param isCurrent <code>true</code> if you want to get current message count <code>false</code>
   *     if you want count at last snapshot.
   * @param msgOption <code>DS_SIP_SUMMARY_REQ</code> for sipSummaryInRequests <code>
   *     DS_SIP_SUMMARY_RESP</code> for sipSummaryInResponses.
   */
  public static int getIncomingSipSummaryMsgCount(boolean isCurrent, byte msgOption) {
    int count = 0;
    Table3D table;
    Table3D tableDup;
    int destIndex = 0;

    switch (msgOption) {
      case DS_SIP_SUMMARY_REQ: // sipSummaryInRequests
        count =
            getIncomingMsgsCount(isCurrent, "UNREGISTERED_REQUEST")
                + getDupIncomingMsgsCount("UNREGISTERED_REQUEST");
        break;
      case DS_SIP_SUMMARY_RESP: // sipSummaryInResponses
        table = getTable(isCurrent, INCOMING);
        tableDup = getTable(isCurrent, DUP_INCOMING);

        // Count all the UNREGISTERED_100CLASS to UNREGISTERED_600CLASS
        // Skip the first column for methods counters (with respIndex=0)
        for (int respIndex = 1; respIndex < DEFAULT_COL_NUM; respIndex++) {
          for (int i = 0; i < DsSipConstants.METHOD_NAMES_SIZE; i++) {
            count += table.get(destIndex, i, respIndex).getCount();
            count += tableDup.get(destIndex, i, respIndex).getCount();
          }
        }

        // Count all the Registered response codes (100-699).
        count += getSipStatusCodeRangeCount(isCurrent, true, 100, 699, true, true, -1);
        break;
    }
    return count;
  }

  /**
   * Returns the total number of SIP request or response messages sent out (originated and relayed)
   * by the SIP entity including retransmissions.
   *
   * @param isCurrent <code>true</code> if you want to get current message count <code>false</code>
   *     if you want count at last snapshot.
   * @param msgOption <code>DS_SIP_SUMMARY_REQ</code> for sipSummaryOutRequests <code>
   *     DS_SIP_SUMMARY_RESP</code> for sipSummaryOutResponses.
   */
  public static int getOutgoingSipSummaryMsgCount(boolean isCurrent, byte msgOption) {
    Table3D table;
    Table3D tableDup;
    int count = 0;
    int destIndex = 0;

    switch (msgOption) {
      case DS_SIP_SUMMARY_REQ: // sipSummaryOutRequests
        count =
            getOutgoingMsgsCount(isCurrent, "UNREGISTERED_REQUEST")
                + getDupOutgoingMsgsCount("UNREGISTERED_REQUEST");
        break;
      case DS_SIP_SUMMARY_RESP: // sipSummaryOutResponses
        table = getTable(isCurrent, OUTGOING);
        tableDup = getTable(isCurrent, DUP_OUTGOING);

        // Count all the UNREGISTERED_100CLASS to UNREGISTERED_600CLASS
        for (int respIndex = 1; respIndex < DEFAULT_COL_NUM; respIndex++) {
          for (int i = 0; i < DsSipConstants.METHOD_NAMES_SIZE; i++) {
            count += table.get(destIndex, i, respIndex).getCount();
            count += tableDup.get(destIndex, i, respIndex).getCount();
          }
        }

        // Count all the Registered response codes
        count += getSipStatusCodeRangeCount(isCurrent, false, 100, 699, true, true, -1);
        break;
      case DS_SIP_RETRY_FINAL_RESP: // sipStatsRetryFinalResponses
        tableDup = getTable(isCurrent, DUP_OUTGOING);

        // Count all the UNREGISTERED_200CLASS to UNREGISTERED_600CLASS
        // Skipping the columns for methods (index=0) and
        // UNREGISTERED_100CLASS (index=1)
        for (int respIndex = 2; respIndex < DEFAULT_COL_NUM; respIndex++) {
          for (int i = 0; i < DsSipConstants.METHOD_NAMES_SIZE; i++) {
            count += tableDup.get(destIndex, i, respIndex).getCount();
          }
        }

        // Count all the Registered response codes (200-699).
        count += getSipStatusCodeRangeCount(isCurrent, false, 200, 699, false, true, -1);

        break;
      case DS_SIP_RETRY_NON_FINAL_RESP: // sipStatsRetryNonFinalResponses
        count = getDupOutgoingMsgsCount(isCurrent, "UNREGISTERED_100CLASS");

        // Count all the Registered response codes (100-199).
        count += getSipStatusCodeRangeCount(isCurrent, false, 100, 199, false, true, -1);
    }
    return count;
  }

  /**
   * Returns the number of 1xx, 2xx, 3xx, 4xx, 5xx or 6xx class SIP responses received by the SIP
   * entity including retransmissions.
   *
   * @param isCurrent <code>true</code> if you want to get current message count <code>false</code>
   *     if you want count at last snapshot.
   * @param msgOption <code>DS_SIP_STATUS_CLASS_1XX</code> for 1xx. <code>DS_SIP_STATUS_CLASS_2XX
   *     </code> for 2xx. <code>DS_SIP_STATUS_CLASS_3XX</code> for 3xx. <code>
   *     DS_SIP_STATUS_CLASS_4XX</code> for 4xx. <code>DS_SIP_STATUS_CLASS_5XX</code> for 5xx.
   *     <code>DS_SIP_STATUS_CLASS_6XX</code> for 6xx.
   */
  public static int getIncomingStatusCodeClassMsgCount(boolean isCurrent, byte msgOption) {
    int count = 0;

    switch (msgOption) {
      case DS_SIP_STATUS_CLASS_1XX:
        count =
            getIncomingMsgsCount(isCurrent, "UNREGISTERED_100CLASS")
                + getDupIncomingMsgsCount(isCurrent, "UNREGISTERED_100CLASS");
        // Count all the Registered response codes (100-199).
        count += getSipStatusCodeRangeCount(isCurrent, true, 100, 199, true, true, -1);
        break;
      case DS_SIP_STATUS_CLASS_2XX:
        count =
            getIncomingMsgsCount(isCurrent, "UNREGISTERED_200CLASS")
                + getDupIncomingMsgsCount(isCurrent, "UNREGISTERED_200CLASS");

        // Count all the Registered response codes (200-299).
        count += getSipStatusCodeRangeCount(isCurrent, true, 200, 299, true, true, -1);
        break;
      case DS_SIP_STATUS_CLASS_3XX:
        count =
            getIncomingMsgsCount(isCurrent, "UNREGISTERED_300CLASS")
                + getDupIncomingMsgsCount(isCurrent, "UNREGISTERED_300CLASS");

        // Count all the Registered response codes (300-399).
        count += getSipStatusCodeRangeCount(isCurrent, true, 300, 399, true, true, -1);
        break;
      case DS_SIP_STATUS_CLASS_4XX:
        count =
            getIncomingMsgsCount(isCurrent, "UNREGISTERED_400CLASS")
                + getDupIncomingMsgsCount(isCurrent, "UNREGISTERED_400CLASS");

        // Count all the Registered response codes (400-499).
        count += getSipStatusCodeRangeCount(isCurrent, true, 400, 499, true, true, -1);
        break;
      case DS_SIP_STATUS_CLASS_5XX:
        count =
            getIncomingMsgsCount(isCurrent, "UNREGISTERED_500CLASS")
                + getDupIncomingMsgsCount(isCurrent, "UNREGISTERED_500CLASS");

        // Count all the Registered response codes (500-599).
        count += getSipStatusCodeRangeCount(isCurrent, true, 500, 599, true, true, -1);
        break;
      case DS_SIP_STATUS_CLASS_6XX:
        count =
            getIncomingMsgsCount(isCurrent, "UNREGISTERED_600CLASS")
                + getDupIncomingMsgsCount(isCurrent, "UNREGISTERED_600CLASS");

        // Count all the Registered response codes (600-699).
        count += getSipStatusCodeRangeCount(isCurrent, true, 600, 699, true, true, -1);
        break;
    }
    return count;
  }

  /**
   * Returns the number of 1xx, 2xx, 3xx, 4xx, 5xx or 6xx class SIP responses sent by the SIP entity
   * including retransmissions.
   *
   * @param isCurrent <code>true</code> if you want to get current message count <code>false</code>
   *     if you want count at last snapshot.
   * @param msgOption <code>DS_SIP_STATUS_CLASS_1XX</code> for 1xx. <code>DS_SIP_STATUS_CLASS_2XX
   *     </code> for 2xx. <code>DS_SIP_STATUS_CLASS_3XX</code> for 3xx. <code>
   *     DS_SIP_STATUS_CLASS_4XX</code> for 4xx. <code>DS_SIP_STATUS_CLASS_5XX</code> for 5xx.
   *     <code>DS_SIP_STATUS_CLASS_6XX</code> for 6xx.
   */
  public static int getOutgoingStatusCodeClassMsgCount(boolean isCurrent, byte msgOption) {
    int count = 0;

    switch (msgOption) {
      case DS_SIP_STATUS_CLASS_1XX:
        count =
            getOutgoingMsgsCount(isCurrent, "UNREGISTERED_100CLASS")
                + getDupOutgoingMsgsCount(isCurrent, "UNREGISTERED_100CLASS");

        // Count all the Registered response codes (100-199).
        count += getSipStatusCodeRangeCount(isCurrent, false, 100, 199, true, true, -1);
        break;
      case DS_SIP_STATUS_CLASS_2XX:
        count =
            getOutgoingMsgsCount(isCurrent, "UNREGISTERED_200CLASS")
                + getDupOutgoingMsgsCount(isCurrent, "UNREGISTERED_200CLASS");

        // Count all the Registered response codes (200-299).
        count += getSipStatusCodeRangeCount(isCurrent, false, 200, 299, true, true, -1);
        break;
      case DS_SIP_STATUS_CLASS_3XX:
        count =
            getOutgoingMsgsCount(isCurrent, "UNREGISTERED_300CLASS")
                + getDupOutgoingMsgsCount(isCurrent, "UNREGISTERED_300CLASS");

        // Count all the Registered response codes (300-399).
        count += getSipStatusCodeRangeCount(isCurrent, false, 300, 399, true, true, -1);
        break;
      case DS_SIP_STATUS_CLASS_4XX:
        count =
            getOutgoingMsgsCount(isCurrent, "UNREGISTERED_400CLASS")
                + getDupOutgoingMsgsCount(isCurrent, "UNREGISTERED_400CLASS");

        // Count all the Registered response codes (400-499).
        count += getSipStatusCodeRangeCount(isCurrent, false, 400, 499, true, true, -1);
        break;
      case DS_SIP_STATUS_CLASS_5XX:
        count =
            getOutgoingMsgsCount(isCurrent, "UNREGISTERED_500CLASS")
                + getDupOutgoingMsgsCount(isCurrent, "UNREGISTERED_500CLASS");

        // Count all the Registered response codes (500-599).
        count += getSipStatusCodeRangeCount(isCurrent, false, 500, 599, true, true, -1);
        break;
      case DS_SIP_STATUS_CLASS_6XX:
        count =
            getOutgoingMsgsCount(isCurrent, "UNREGISTERED_600CLASS")
                + getDupOutgoingMsgsCount(isCurrent, "UNREGISTERED_600CLASS");

        // Count all the Registered response codes (600-699).
        count += getSipStatusCodeRangeCount(isCurrent, false, 600, 699, true, true, -1);
        break;
    }
    return count;
  }

  /**
   * Returns the total number of requests received, namely INVITE's, ACK's, BYE's, CANCEL's,
   * OPTIONS, REGISTER's, REFER's, UPDATE's, SUBSCRIBE's or NOTIFY's by the SIP entity including
   * retransmissions.
   *
   * @param isCurrent <code>true</code> if you want to get current message count <code>false</code>
   *     if you want count at last snapshot.
   * @param method <code>name</code> in string form for a particular method. for e.g <code>"INVITE"
   *     </code>
   */
  public static int getIncomingSipMethodMsgCount(boolean isCurrent, String method) {
    return getIncomingMsgsCount(isCurrent, method) + getDupIncomingMsgsCount(isCurrent, method);
  }

  /**
   * Returns the total number of requests sent namely INVITE's, ACK's, BYE's, CANCEL's, OPTIONS,
   * REGISTER's, REFER's, UPDATE's, SUBSCRIBE's or NOTIFY's by the SIP entity including
   * retransmissions.
   *
   * @param isCurrent <code>true</code> if you want to get current message count <code>false</code>
   *     if you want count at last snapshot.
   * @param method <code>name</code> in string form for a particular method. for e.g <code>"INVITE"
   *     </code>
   */
  public static int getOutgoingSipMethodMsgCount(boolean isCurrent, String method) {
    return getOutgoingMsgsCount(isCurrent, method) + getDupOutgoingMsgsCount(isCurrent, method);
  }

  /**
   * Returns the total number of response messages received by the SIP entity including
   * retransmissions.
   *
   * @param isCurrent <code>true</code> if you want to get current message count <code>false</code>
   *     if you want count at last snapshot.
   * @param respCode <code>response code</code> in string form. for e.g <code>"400"</code>
   */
  public static int getIncomingStatusCodeMsgCount(boolean isCurrent, String respCode) {
    return getIncomingMsgsCount(isCurrent, respCode) + getDupIncomingMsgsCount(isCurrent, respCode);
  }

  /**
   * Returns the total number of response messages sent by the SIP entity including retransmissions.
   *
   * @param isCurrent <code>true</code> if you want to get current message count <code>false</code>
   *     if you want count at last snapshot.
   * @param respCode <code>response code</code> in string form. for e.g <code>"400"</code>
   */
  public static int getOutgoingStatusCodeMsgCount(boolean isCurrent, String respCode) {
    return getOutgoingMsgsCount(isCurrent, respCode) + getDupOutgoingMsgsCount(isCurrent, respCode);
  }

  /**
   * Display's on the standard output the summary of the total number of SIP request or response
   * messages sent or received by the SIP entity including retransmissions. The total no. of
   * transactions is also displayed.
   *
   * @param isAbsolute <code>true</code> for displaying current absolute counters. <code>false
   *     </code> for displaying counters since last snaptshot.
   * @param outStr output stream to redirect output.
   */
  public static void writeMessageStatisticsSummary(
      boolean isAbsolute, java.io.OutputStream outStr) {
    DsSipTransactionManager m_tm = null;

    try {
      try {
        m_tm = DsSipTransactionManager.getTransactionManager();
      } catch (NullPointerException t) {
        if (DsLog4j.exceptionCat.isEnabled(Level.WARN)) {
          DsLog4j.exceptionCat.log(Level.WARN, "DsSipTransactionManager not instantiated");
        }
      }
      outStr.write("\n\nThe Summary Message statistics:-\n\n".getBytes());
      outStr.write("-------------------------------\n\n".getBytes());

      if (isAbsolute) {
        outStr.write("\nCounters since reboot:\n\n".getBytes());
        outStr.write(
            ("sipSummaryInRequests: "
                    + getIncomingSipSummaryMsgCount(true, DS_SIP_SUMMARY_REQ)
                    + "\n")
                .getBytes());
        outStr.write(
            ("sipSummaryOutRequests: "
                    + getOutgoingSipSummaryMsgCount(true, DS_SIP_SUMMARY_REQ)
                    + "\n")
                .getBytes());
        outStr.write(
            ("sipSummaryInResponses: "
                    + getIncomingSipSummaryMsgCount(true, DS_SIP_SUMMARY_RESP)
                    + "\n")
                .getBytes());
        outStr.write(
            ("sipSummaryOutResponses: "
                    + getOutgoingSipSummaryMsgCount(true, DS_SIP_SUMMARY_RESP)
                    + "\n")
                .getBytes());
        if (m_tm != null) {
          outStr.write(
              ("sipSummaryTotalTransactions: " + m_tm.getTotalTransactionCount(true) + "\n")
                  .getBytes());
        } else {
          outStr.write("sipSummaryTotalTransactions: 0\n".getBytes());
        }
      } else {
        if (lastSnapshotTime == 0) {
          outStr.write("Counters never taken snapshot before (same as absolute):\n".getBytes());
        } else {
          outStr.write(
              ("Counters since last snapshot (" + (new Date(lastSnapshotTime)).toString() + "):\n")
                  .getBytes());
        }
        // Need to subtract the "reset" counters from "absolute" counters
        outStr.write(
            ("sipSummaryInRequests: "
                    + (getIncomingSipSummaryMsgCount(true, DS_SIP_SUMMARY_REQ)
                        - getIncomingSipSummaryMsgCount(false, DS_SIP_SUMMARY_REQ))
                    + "\n")
                .getBytes());
        outStr.write(
            ("sipSummaryOutRequests: "
                    + (getOutgoingSipSummaryMsgCount(true, DS_SIP_SUMMARY_REQ)
                        - getOutgoingSipSummaryMsgCount(false, DS_SIP_SUMMARY_REQ))
                    + "\n")
                .getBytes());
        outStr.write(
            ("sipSummaryInResponses: "
                    + (getIncomingSipSummaryMsgCount(true, DS_SIP_SUMMARY_RESP)
                        - getIncomingSipSummaryMsgCount(false, DS_SIP_SUMMARY_RESP))
                    + "\n")
                .getBytes());
        outStr.write(
            ("sipSummaryOutResponses: "
                    + (getOutgoingSipSummaryMsgCount(true, DS_SIP_SUMMARY_RESP)
                        - getOutgoingSipSummaryMsgCount(false, DS_SIP_SUMMARY_RESP))
                    + "\n")
                .getBytes());
        if (m_tm != null) {
          outStr.write(
              ("sipSummaryTotalTransactions: "
                      + (m_tm.getTotalTransactionCount(true) - m_tm.getTotalTransactionCount(false))
                      + "\n")
                  .getBytes());
        } else {
          outStr.write("sipSummaryTotalTransactions: 0\n".getBytes());
        }
      }
    } catch (java.io.IOException t) {
      if (DsLog4j.exceptionCat.isEnabled(Level.WARN)) {
        DsLog4j.exceptionCat.log(Level.WARN, "writeMessageStatisticsSummary io exception");
      }
    }
  }

  /**
   * Display's on the standard output the total number of SIP requests sent or received by the SIP
   * entity (including retransmissions). The detail about each method is presented individually.
   *
   * @param isAbsolute <code>true</code> for displaying current absolute counters. <code>false
   *     </code> for displaying counters since last snaptshot.
   * @param outStr output stream to redirect output.
   */
  public static void writeMessageStatisticsMethod(boolean isAbsolute, java.io.OutputStream outStr) {
    final String methods[] = {
      "INVITE",
      "ACK",
      "BYE",
      "CANCEL",
      "OPTIONS",
      "REGISTER",
      "REFER",
      "UPDATE",
      "PUBLISH",
      "SUBSCRIBE",
      "NOTIFY"
    };
    final DsByteString statsName[] = {
      new DsByteString("sipStatsInviteIns"),
      new DsByteString("sipStatsInviteOuts"),
      new DsByteString("sipStatsAckIns"),
      new DsByteString("sipStatsAckOuts"),
      new DsByteString("sipStatsByeIns"),
      new DsByteString("sipStatsByeOuts"),
      new DsByteString("sipStatsCancelIns"),
      new DsByteString("sipStatsCancelOuts"),
      new DsByteString("sipStatsOptionsIns"),
      new DsByteString("sipStatsOptionsOuts"),
      new DsByteString("sipStatsRegisterIns"),
      new DsByteString("sipStatsRegisterOuts"),
      new DsByteString("ReferIns"),
      new DsByteString("ReferOuts"),
      new DsByteString("UpdateIns"),
      new DsByteString("UpdateOuts"),
      new DsByteString("PublishIns"),
      new DsByteString("PublishOuts"),
      new DsByteString("SubscribeIns"),
      new DsByteString("SubscribeOuts"),
      new DsByteString("NotifyIns"),
      new DsByteString("NotifyOuts")
    };
    try {
      outStr.write("\nThe SIP Method Message statistics:-\n\n".getBytes());
      outStr.write("-----------------------------------\n\n".getBytes());
      if (isAbsolute) {
        outStr.write("Counters since reboot:\n\n".getBytes());
        for (int i = 0, j = 0; i < methods.length; i++) {
          outStr.write(
              (statsName[j++] + ": " + getIncomingSipMethodMsgCount(true, methods[i]) + "\n")
                  .getBytes());
          outStr.write(
              (statsName[j++] + ": " + getOutgoingSipMethodMsgCount(true, methods[i]) + "\n")
                  .getBytes());
        }
      } else {
        if (lastSnapshotTime == 0) {
          outStr.write("Counters never taken snapshot before (same as absolute):\n".getBytes());
        } else {
          outStr.write(
              ("Counters since last snapshot (" + (new Date(lastSnapshotTime)).toString() + "):\n")
                  .getBytes());
        }
        for (int i = 0, j = 0; i < methods.length; i++) {
          outStr.write(
              (statsName[j++]
                      + ": "
                      + (getIncomingSipMethodMsgCount(true, methods[i])
                          - getIncomingSipMethodMsgCount(false, methods[i]))
                      + "\n")
                  .getBytes());
          outStr.write(
              (statsName[j++]
                      + ": "
                      + (getOutgoingSipMethodMsgCount(true, methods[i])
                          - getOutgoingSipMethodMsgCount(false, methods[i]))
                      + "\n")
                  .getBytes());
        }
      }
    } catch (java.io.IOException t) {
      if (DsLog4j.exceptionCat.isEnabled(Level.WARN)) {
        DsLog4j.exceptionCat.log(Level.WARN, "writeMessageStatisticsMethod io exception");
      }
    }
  }

  /**
   * Display's on the standard output the total number of SIP status codes (1xx to 6xx) sent or
   * received by the SIP entity (including retransmissions). The detail about each status code is
   * presented individually.
   *
   * @param isAbsolute <code>true</code> for displaying current absolute counters. <code>false
   *     </code> for displaying counters since last snaptshot.
   * @param outStr output stream to redirect output.
   */
  public static void writeMessageStatisticsStatusCode(
      boolean isAbsolute, java.io.OutputStream outStr) {
    try {
      outStr.write("\nThe SIP Status Code Message statistics:-\n\n".getBytes());
      outStr.write("----------------------------------------\n\n".getBytes());
      if (isAbsolute) {
        outStr.write("Counters since reboot:\n\n".getBytes());
        for (int i = 0; i < respCodes.length; i++) {
          outStr.write(
              ("Incoming "
                      + respCodes[i]
                      + ": "
                      + getIncomingStatusCodeMsgCount(true, respCodes[i])
                      + "\n")
                  .getBytes());
          outStr.write(
              ("Outgoing "
                      + respCodes[i]
                      + ": "
                      + getOutgoingStatusCodeMsgCount(true, respCodes[i])
                      + "\n")
                  .getBytes());
        }
      } else {
        if (lastSnapshotTime == 0) {
          outStr.write("Counters never taken snapshot before (same as absolute):\n".getBytes());
        } else {
          outStr.write(
              ("Counters since last snapshot (" + (new Date(lastSnapshotTime)).toString() + "):\n")
                  .getBytes());
        }
        for (int i = 0; i < respCodes.length; i++) {
          outStr.write(
              ("Incoming "
                      + respCodes[i]
                      + ": "
                      + (getIncomingStatusCodeMsgCount(true, respCodes[i])
                          - getIncomingStatusCodeMsgCount(false, respCodes[i]))
                      + "\n")
                  .getBytes());
          outStr.write(
              ("Outgoing "
                      + respCodes[i]
                      + ": "
                      + (getOutgoingStatusCodeMsgCount(true, respCodes[i])
                          - getOutgoingStatusCodeMsgCount(false, respCodes[i]))
                      + "\n")
                  .getBytes());
        }
      }
    } catch (java.io.IOException t) {
      if (DsLog4j.exceptionCat.isEnabled(Level.WARN)) {
        DsLog4j.exceptionCat.log(Level.WARN, "writeMessageStatisticsStatusCode io exception");
      }
    }
  }

  /**
   * Display's on the standard output the total number of SIP status codes (1xx to 6xx) sent or
   * received by the SIP entity (including retransmissions). The counts are presented per status
   * class.
   *
   * @param isAbsolute <code>true</code> for displaying current counters. <code>false</code> for
   *     displaying counters at last snapshot.
   * @param outStr output stream to redirect output.
   */
  public static void writeMessageStatisticsStatusClasses(
      boolean isAbsolute, java.io.OutputStream outStr) {
    final DsByteString statusClasses[] = {
      new DsByteString("sipStatsInfoClassIns"),
      new DsByteString("sipStatsInfoClassOuts"),
      new DsByteString("sipStatsSuccessClassIns"),
      new DsByteString("sipStatsSuccessClassOuts"),
      new DsByteString("sipStatsRedirClassIns"),
      new DsByteString("sipStatsRedirClassOuts"),
      new DsByteString("sipStatsReqFailClassIns"),
      new DsByteString("sipStatsReqFailClassOuts"),
      new DsByteString("sipStatsServerFailClassIns"),
      new DsByteString("sipStatsServerFailClassOuts"),
      new DsByteString("sipStatsGlobalFailClassIns"),
      new DsByteString("sipStatsGlobalFailClassOuts"),
    };
    try {
      outStr.write("\nThe SIP Status Code Classes statistics:-\n\n".getBytes());
      outStr.write("----------------------------------------\n\n".getBytes());
      if (isAbsolute) {
        outStr.write("Counters since reboot:\n\n".getBytes());
        for (byte i = DS_SIP_STATUS_CLASS_1XX, j = 0; i <= DS_SIP_STATUS_CLASS_6XX; i++) {
          outStr.write(
              (statusClasses[j++] + ": " + getIncomingStatusCodeClassMsgCount(true, i) + "\n")
                  .getBytes());
          outStr.write(
              (statusClasses[j++] + ": " + getOutgoingStatusCodeClassMsgCount(true, i) + "\n")
                  .getBytes());
        }
      } else {
        if (lastSnapshotTime == 0) {
          outStr.write("Counters never taken snapshot before (same as absolute):\n".getBytes());
        } else {
          outStr.write(
              ("Counters since last snapshot (" + (new Date(lastSnapshotTime)).toString() + "):\n")
                  .getBytes());
        }
        for (byte i = DS_SIP_STATUS_CLASS_1XX, j = 0; i <= DS_SIP_STATUS_CLASS_6XX; i++) {
          outStr.write(
              (statusClasses[j++]
                      + ": "
                      + (getIncomingStatusCodeClassMsgCount(true, i)
                          - getIncomingStatusCodeClassMsgCount(false, i))
                      + "\n")
                  .getBytes());
          outStr.write(
              (statusClasses[j++]
                      + ": "
                      + (getOutgoingStatusCodeClassMsgCount(true, i)
                          - getOutgoingStatusCodeClassMsgCount(false, i))
                      + "\n")
                  .getBytes());
        }
      }
    } catch (java.io.IOException t) {
      if (DsLog4j.exceptionCat.isEnabled(Level.WARN)) {
        DsLog4j.exceptionCat.log(Level.WARN, "writeMessageStatisticsStatusClasses io exception");
      }
    }
  }

  /**
   * Display's on the standard output the total number of retransmissions "sent" for SIP requests
   * and responses. The counts are presented individually for the methods and in terms of cumulative
   * "final" and "non final" responses.
   *
   * @param isAbsolute <code>true</code> for displaying current absolute counters. <code>false
   *     </code> for displaying counters since last snaptshot.
   * @param outStr output stream to redirect output.
   */
  public static void writeMessageStatisticsRetry(boolean isAbsolute, java.io.OutputStream outStr) {
    try {
      outStr.write("\nThe SIP Common RETRY statistics:-\n\n".getBytes());
      outStr.write("---------------------------------\n\n".getBytes());
      if (isAbsolute) {
        outStr.write("Counters since reboot:\n\n".getBytes());
        outStr.write(
            ("sipStatsRetryInvites" + ": " + getDupOutgoingMsgsCount(true, "INVITE") + "\n")
                .getBytes());
        outStr.write(
            ("sipStatsRetryByes" + ": " + getDupOutgoingMsgsCount(true, "BYE") + "\n").getBytes());
        outStr.write(
            ("sipStatsRetryCancels" + ": " + getDupOutgoingMsgsCount(true, "CANCEL") + "\n")
                .getBytes());
        outStr.write(
            ("sipStatsRetryRegisters" + ": " + getDupOutgoingMsgsCount(true, "REGISTER") + "\n")
                .getBytes());
        outStr.write(
            ("sipStatsRetryOptions" + ": " + getDupOutgoingMsgsCount(true, "OPTIONS") + "\n")
                .getBytes());
        outStr.write(
            ("sipStatsRetryFinalResponses"
                    + ": "
                    + getOutgoingSipSummaryMsgCount(true, DS_SIP_RETRY_FINAL_RESP)
                    + "\n")
                .getBytes());
        outStr.write(
            ("sipStatsRetryNonFinalResponses"
                    + ": "
                    + getOutgoingSipSummaryMsgCount(true, DS_SIP_RETRY_NON_FINAL_RESP)
                    + "\n")
                .getBytes());
      } else {
        if (lastSnapshotTime == 0) {
          outStr.write("Counters never taken snapshot before (same as absolute):\n".getBytes());
        } else {
          outStr.write(
              ("Counters since last snapshot (" + (new Date(lastSnapshotTime)).toString() + "):\n")
                  .getBytes());
        }
        outStr.write(
            ("sipStatsRetryInvites"
                    + ": "
                    + (getDupOutgoingMsgsCount(true, "INVITE")
                        - getDupOutgoingMsgsCount(false, "INVITE"))
                    + "\n")
                .getBytes());
        outStr.write(
            ("sipStatsRetryByes"
                    + ": "
                    + (getDupOutgoingMsgsCount(true, "BYE") - getDupOutgoingMsgsCount(false, "BYE"))
                    + "\n")
                .getBytes());
        outStr.write(
            ("sipStatsRetryCancels"
                    + ": "
                    + (getDupOutgoingMsgsCount(true, "CANCEL")
                        - getDupOutgoingMsgsCount(false, "CANCEL"))
                    + "\n")
                .getBytes());
        outStr.write(
            ("sipStatsRetryRegisters"
                    + ": "
                    + (getDupOutgoingMsgsCount(true, "REGISTER")
                        - getDupOutgoingMsgsCount(false, "REGISTER"))
                    + "\n")
                .getBytes());
        outStr.write(
            ("sipStatsRetryOptions"
                    + ": "
                    + (getDupOutgoingMsgsCount(true, "OPTIONS")
                        - getDupOutgoingMsgsCount(false, "OPTIONS"))
                    + "\n")
                .getBytes());
        outStr.write(
            ("sipStatsRetryFinalResponses"
                    + ": "
                    + (getOutgoingSipSummaryMsgCount(true, DS_SIP_RETRY_FINAL_RESP)
                        - getOutgoingSipSummaryMsgCount(false, DS_SIP_RETRY_FINAL_RESP))
                    + "\n")
                .getBytes());
        outStr.write(
            ("sipStatsRetryNonFinalResponses"
                    + ": "
                    + (getOutgoingSipSummaryMsgCount(true, DS_SIP_RETRY_NON_FINAL_RESP)
                        - getOutgoingSipSummaryMsgCount(false, DS_SIP_RETRY_NON_FINAL_RESP))
                    + "\n")
                .getBytes());
      }
    } catch (java.io.IOException t) {
      if (DsLog4j.exceptionCat.isEnabled(Level.WARN)) {
        DsLog4j.exceptionCat.log(Level.WARN, "writeMessageStatisticsRetry io exception");
      }
    }
  }

  /**
   * Display's on the standard output all sets of statistics i.e per Summary, Methods, Status Code,
   * Status Classes and Retry.
   *
   * @param isAbsolute <code>true</code> for displaying current absolute counters. <code>false
   *     </code> for displaying counters since last snaptshot.
   * @param outStr output stream to redirect output.
   */
  public static void writeMessageStatisticsAll(boolean isAbsolute, java.io.OutputStream outStr) {
    try {
      outStr.write("\nAll the SIP message statistics:-\n\n".getBytes());
      writeMessageStatisticsSummary(isAbsolute, outStr);
      outStr.write("\n".getBytes());
      writeMessageStatisticsMethod(isAbsolute, outStr);
      outStr.write("\n".getBytes());
      writeMessageStatisticsStatusCode(isAbsolute, outStr);
      outStr.write("\n".getBytes());
      writeMessageStatisticsStatusClasses(isAbsolute, outStr);
      outStr.write("\n".getBytes());
      writeMessageStatisticsRetry(isAbsolute, outStr);
    } catch (java.io.IOException t) {
      if (DsLog4j.exceptionCat.isEnabled(Level.WARN)) {
        DsLog4j.exceptionCat.log(Level.WARN, "writeMessageStatisticsAll io exception");
      }
    }
  }

  /**
   * Returns the summary of the total number of SIP request or response messages sent or received by
   * the SIP entity including retransmissions. The total no. of transactions is also returned.
   *
   * @return the message statistics summary
   */
  public static DsSipSummaryStatsEntry getMessageStatisticsSummary() {
    DsSipTransactionManager m_tm = null;
    DsSipSummaryStatsEntry summ_stats = new DsSipSummaryStatsEntry();

    DsSipSummaryStatsEntry.m_sipSummaryInRequests =
        getIncomingSipSummaryMsgCount(true, DS_SIP_SUMMARY_REQ);

    DsSipSummaryStatsEntry.m_sipSummaryOutRequests =
        getOutgoingSipSummaryMsgCount(true, DS_SIP_SUMMARY_REQ);

    DsSipSummaryStatsEntry.m_sipSummaryInResponses =
        getIncomingSipSummaryMsgCount(true, DS_SIP_SUMMARY_RESP);

    DsSipSummaryStatsEntry.m_sipSummaryOutResponses =
        getOutgoingSipSummaryMsgCount(true, DS_SIP_SUMMARY_RESP);

    try {
      m_tm = DsSipTransactionManager.getTransactionManager();
    } catch (NullPointerException t) {
      if (DsLog4j.exceptionCat.isEnabled(Level.WARN)) {
        DsLog4j.exceptionCat.log(Level.WARN, "DsSipTransactionManager not instantiated");
      }
    }

    if (m_tm != null) {
      DsSipSummaryStatsEntry.m_sipSummaryTotalTransactions = m_tm.getTotalTransactionCount(true);
    } else {
      DsSipSummaryStatsEntry.m_sipSummaryTotalTransactions = 0;
    }

    return summ_stats;
  }

  /**
   * Returns the total number of SIP requests sent or received by the SIP entity (including
   * retransmissions). The individual counters for each method are returned.
   *
   * @return the message statistics by method
   */
  public static DsSipMethodStatsEntry getMessageStatisticsMethod() {
    DsSipMethodStatsEntry method_stats = new DsSipMethodStatsEntry();

    DsSipMethodStatsEntry.sipStatsInviteIns = getIncomingSipMethodMsgCount(true, "INVITE");

    DsSipMethodStatsEntry.sipStatsInviteOuts = getOutgoingSipMethodMsgCount(true, "INVITE");

    DsSipMethodStatsEntry.sipStatsAckIns = getIncomingSipMethodMsgCount(true, "ACK");

    DsSipMethodStatsEntry.sipStatsAckOuts = getOutgoingSipMethodMsgCount(true, "ACK");

    DsSipMethodStatsEntry.sipStatsByeIns = getIncomingSipMethodMsgCount(true, "BYE");

    DsSipMethodStatsEntry.sipStatsByeOuts = getOutgoingSipMethodMsgCount(true, "BYE");

    DsSipMethodStatsEntry.sipStatsCancelIns = getIncomingSipMethodMsgCount(true, "CANCEL");

    DsSipMethodStatsEntry.sipStatsCancelOuts = getOutgoingSipMethodMsgCount(true, "CANCEL");

    DsSipMethodStatsEntry.sipStatsOptionsIns = getIncomingSipMethodMsgCount(true, "OPTIONS");

    DsSipMethodStatsEntry.sipStatsOptionsOuts = getOutgoingSipMethodMsgCount(true, "OPTIONS");

    DsSipMethodStatsEntry.sipStatsRegisterIns = getIncomingSipMethodMsgCount(true, "REGISTER");

    DsSipMethodStatsEntry.sipStatsRegisterOuts = getOutgoingSipMethodMsgCount(true, "REGISTER");

    return method_stats;
  }

  /**
   * Returns the total number of SIP status codes (1xx to 6xx) sent or received by the SIP entity
   * (including retransmissions). The counts are returned per status class.
   *
   * @return the messages statitics status by classes
   */
  public static DsSipStatusCodeClassesEntry getMessageStatisticsStatusClasses() {
    DsSipStatusCodeClassesEntry statusCls_stats = new DsSipStatusCodeClassesEntry();
    DsSipStatusCodeClassesEntry.sipStatsInfoClassIns =
        getIncomingStatusCodeClassMsgCount(true, DS_SIP_STATUS_CLASS_1XX);

    DsSipStatusCodeClassesEntry.sipStatsInfoClassOuts =
        getOutgoingStatusCodeClassMsgCount(true, DS_SIP_STATUS_CLASS_1XX);

    DsSipStatusCodeClassesEntry.sipStatsSuccessClassIns =
        getIncomingStatusCodeClassMsgCount(true, DS_SIP_STATUS_CLASS_2XX);

    DsSipStatusCodeClassesEntry.sipStatsSuccessClassOuts =
        getOutgoingStatusCodeClassMsgCount(true, DS_SIP_STATUS_CLASS_2XX);

    DsSipStatusCodeClassesEntry.sipStatsRedirClassIns =
        getIncomingStatusCodeClassMsgCount(true, DS_SIP_STATUS_CLASS_3XX);

    DsSipStatusCodeClassesEntry.sipStatsRedirClassOuts =
        getOutgoingStatusCodeClassMsgCount(true, DS_SIP_STATUS_CLASS_3XX);

    DsSipStatusCodeClassesEntry.sipStatsReqFailClassIns =
        getIncomingStatusCodeClassMsgCount(true, DS_SIP_STATUS_CLASS_4XX);

    DsSipStatusCodeClassesEntry.sipStatsReqFailClassOuts =
        getOutgoingStatusCodeClassMsgCount(true, DS_SIP_STATUS_CLASS_4XX);

    DsSipStatusCodeClassesEntry.sipStatsServerFailClassIns =
        getIncomingStatusCodeClassMsgCount(true, DS_SIP_STATUS_CLASS_5XX);

    DsSipStatusCodeClassesEntry.sipStatsServerFailClassOuts =
        getOutgoingStatusCodeClassMsgCount(true, DS_SIP_STATUS_CLASS_5XX);

    DsSipStatusCodeClassesEntry.sipStatsGlobalFailClassIns =
        getIncomingStatusCodeClassMsgCount(true, DS_SIP_STATUS_CLASS_6XX);

    DsSipStatusCodeClassesEntry.sipStatsGlobalFailClassOuts =
        getOutgoingStatusCodeClassMsgCount(true, DS_SIP_STATUS_CLASS_6XX);

    DsSipStatusCodeClassesEntry.sipStatsOtherClassesIns = 0;

    DsSipStatusCodeClassesEntry.sipStatsOtherClassesOuts = 0;

    return statusCls_stats;
  }

  /**
   * Returns the total number of retransmissions "sent" for SIP requests and responses. The counts
   * are returned individually for the methods and in terms of cumulative "final" and "non final"
   * responses.
   *
   * @return the messages statitics status by retry
   */
  public static DsSipCommonStatsRetryEntry getMessageStatisticsStatusRetry() {
    DsSipCommonStatsRetryEntry retryOut_stats = new DsSipCommonStatsRetryEntry();

    DsSipCommonStatsRetryEntry.sipStatsRetryInvites = getDupOutgoingMsgsCount(true, "INVITE");

    DsSipCommonStatsRetryEntry.sipStatsRetryByes = getDupOutgoingMsgsCount(true, "BYE");

    DsSipCommonStatsRetryEntry.sipStatsRetryCancels = getDupOutgoingMsgsCount(true, "CANCEL");

    DsSipCommonStatsRetryEntry.sipStatsRetryRegisters = getDupOutgoingMsgsCount(true, "REGISTER");

    DsSipCommonStatsRetryEntry.sipStatsRetryOptions = getDupOutgoingMsgsCount(true, "OPTIONS");

    DsSipCommonStatsRetryEntry.sipStatsRetryFinalResponses =
        getOutgoingSipSummaryMsgCount(true, DS_SIP_RETRY_FINAL_RESP);

    DsSipCommonStatsRetryEntry.sipStatsRetryNonFinalResponses =
        getOutgoingSipSummaryMsgCount(true, DS_SIP_RETRY_NON_FINAL_RESP);

    return retryOut_stats;
  }

  /**
   * Returns the total number of SIP status codes (1xx to 6xx) sent or received by the SIP entity
   * (including retransmissions). The detail about each status code is returned individually.
   *
   * @return the messages statitics status by code
   */
  public static DsSipStatusCodesEntry[] getMessageStatisticsStatusCode() {
    DsSipStatusCodesEntry[] statusCode_stats;

    statusCode_stats = new DsSipStatusCodesEntry[respCodes.length];

    for (int i = 0; i < respCodes.length; i++) {
      statusCode_stats[i] = new DsSipStatusCodesEntry();
      statusCode_stats[i].statusCode = respCodes[i];
      statusCode_stats[i].ins = getIncomingStatusCodeMsgCount(true, respCodes[i]);
      statusCode_stats[i].outs = getOutgoingStatusCodeMsgCount(true, respCodes[i]);
    }

    return statusCode_stats;
  }

  /**
   * Returns the total number of SIP requests for extension methods, sent or received by the SIP
   * entity (including retransmissions). The individual counters for each method are returned.
   *
   * @return the messages statitics status by ext method
   */
  public static DsSipExtMethodStatsEntry[] getMessageStatisticsExtMethod() {
    final String extMethods[] = {"REFER", "UPDATE", "PUBLISH", "SUBSCRIBE", "NOTIFY"};
    DsSipExtMethodStatsEntry[] extMethod_stats;

    extMethod_stats = new DsSipExtMethodStatsEntry[extMethods.length];
    for (int i = 0; i < extMethods.length; i++) {
      extMethod_stats[i] = new DsSipExtMethodStatsEntry();
      extMethod_stats[i].method = extMethods[i];
      extMethod_stats[i].ins = getIncomingSipMethodMsgCount(true, extMethods[i]);
      extMethod_stats[i].outs = getOutgoingSipMethodMsgCount(true, extMethods[i]);
    }
    return extMethod_stats;
  }

  /**
   * Disallow creation of an instance of this class as there is no need to have more than one
   * instance for the whole stack.
   */
  private DsMessageStatistics() {}

  /**
   * Returns the effective message generation reason , returns REASON_AUTO if its set in message or
   * returns the passed reason
   *
   * @param message Incoming sip message to be logged
   * @param reason message generation reason
   * @return
   */
  private static int getReason(DsSipMessage message, int reason) {
    return (message.getApplicationReason() == DsMessageLoggingInterface.REASON_AUTO)
        ? message.getApplicationReason()
        : reason;
  }

  /*
      public static void main(String[] args)
      {
          registerIncomingMsgs(new String[]{"100", "ACK", "200", "INVITE"});
          setEnabled(true);
          long startTime = System.currentTimeMillis();
          for (int i=0; i<10000000; i++)
          {
              if (i<1)
              {
                  try
                  {
                      Thread.currentThread().sleep(1);
                  }
                  catch(InterruptedException e)
                  {
                      e.printStackTrace();
                  }
              }
              updateResponseStat(100, 1, false, true);

              updateDroppedResponseStat(100, false, true);
          }
          System.out.println("##total time: "+(System.currentTimeMillis()-startTime));
          System.out.println("##msgCount: "+getIncomingMsgsCount("100"));
          System.out.println("##dropped cout: "+getDroppedMsgsCount("100"));
          Enumeration enum = registeredIncomingMsgs();
          while (enum.hasMoreElements())
          {
              System.out.println(enum.nextElement());
          }
      }
  */
} // end of DsMessageStatistics

class Table3D implements Cloneable {
  int destNum, msgNum, respNum;
  DsCounter[][][] array3D;
  private int[] responseMap; // = new int[507]; //response code 100-606
  // Persistent : Added to fix CR: 8590
  private static Object copyLock = new Object();
  private static Object deleteLock = new Object();
  // End

  Table3D(int destinationNum, int msgTypeNum, int respTypeNum) {
    destNum = destinationNum;
    msgNum = msgTypeNum;
    respNum = respTypeNum;
    responseMap = new int[calRespArraySize()];
    array3D = new DsCounter[destNum][msgNum][respNum];
    reset();
  }

  // this method is called only at table creation time. no adjustment of col needed
  void createResponseMapping(String[] options) {
    for (int i = 0; i < responseMap.length; i++) {
      responseMap[i] = 1 + i / 100;
    }
    if (options == null) return;

    int respCode;
    int colIndex =
        DsMessageStatistics.DEFAULT_COL_NUM; // 0 - request, 100 - 1, 200 - 2, ... 600 - 6
    for (int i = 0; i < options.length; i++) {
      // "unregistered_XXX" will not be processed here, they are default
      // "Dest_XXX" will not be processed here, they are default for dest case
      if (options[i].indexOf('.') >= 0) // like 127.0.0.1_request
      continue;
      try {
        respCode = Integer.parseInt(options[i].substring(0, 3));
        responseMap[respCode - 100] = colIndex;
        colIndex++;
      } catch (NumberFormatException e) {
      }
    }
  }

  int getResponseMapping(int respCode) {
    return responseMap[respCode - 100];
  }

  DsCounter get(int destIndex, int msgIndex, int respIndex) {
    return array3D[destIndex][msgIndex][respIndex];
  }

  void reset() {
    for (int i = 0; i < destNum; i++)
      for (int j = 0; j < msgNum; j++)
        for (int k = 0; k < respNum; k++) {
          if (array3D[i][j][k] == null) {
            array3D[i][j][k] = new DsCounter(0);
          } else {
            array3D[i][j][k].setCount(0);
          }
        }
  }

  // after table creation. adjust response cols.
  void expand(String[] options) {
    int colIndex = respNum; // 0 - request, 100 - 1, 200 - 2, ... 600 - 6
    respNum += options.length;
    array3D = new DsCounter[destNum][msgNum][respNum];
    reset();
    int respCode;
    int reduceCol = 0;
    for (int i = 0; i < options.length; i++) {
      // "unregistered_XXX" will not be processed here, they are default
      // "Dest_XXX" will not be processed here, they are default for dest case
      try {
        respCode = Integer.parseInt(options[i].substring(0, 3));
        if (responseMap[respCode - 100] >= DsMessageStatistics.DEFAULT_COL_NUM) {
          // this response already there
          reduceCol++;
        } else {
          responseMap[respCode - 100] = colIndex;
          colIndex++;
        }
      } catch (NumberFormatException e) {
        // like INVITE, UNREGISTERED_XXX, DEST_XXX
        reduceCol++;
      }
    }
    if (reduceCol > 0) {
      respNum -= reduceCol;
      array3D = new DsCounter[destNum][msgNum][respNum];
      reset();
    }
  }

  /**
   * Calculate the response code array size based on the range of response code. e.g. [100-606]
   *
   * @return size of the array of response code
   */
  private int calRespArraySize() {
    return (DsSipResponseCode.MAX_RESPONSE_CODE - DsSipResponseCode.MIN_RESPONSE_CODE) + 1;
  }

  // Persistent : Method added to delete a peg count for destination which is unregistered
  public void deleteDestination(Table3D source, int destIdx) {
    synchronized (deleteLock) {
      for (int i = 0, l = 0; i < source.destNum; i++) {
        if (i == destIdx) continue;
        else {
          for (int j = 0; j < this.msgNum; j++)
            for (int k = 0; k < respNum; k++) {
              this.array3D[l][j][k].setCount(source.array3D[i][j][k].getCount());
              this.array3D[l][j][k].setDropCount(source.array3D[i][j][k].getDropCount());
            }
          l++;
        }
      }
    }
    this.responseMap = new int[calRespArraySize()];
    for (int i = 0; i < responseMap.length; i++) this.responseMap[i] = source.responseMap[i];
  }
  // Persistent : Method added to copy the peg count collected for other destinations
  // when a new destination is added
  public void copy(Table3D source) {
    synchronized (copyLock) {
      for (int i = 0; i < source.destNum; i++)
        for (int j = 0; j < this.msgNum; j++)
          for (int k = 0; k < this.respNum; k++) {
            this.array3D[i][j][k].setCount(source.array3D[i][j][k].getCount());
            this.array3D[i][j][k].setDropCount(source.array3D[i][j][k].getDropCount());
          }
    }
    this.responseMap = new int[calRespArraySize()]; // response code 100-606
    for (int i = 0; i < responseMap.length; i++) this.responseMap[i] = source.responseMap[i];
  }

  public Object clone() {
    Table3D clone = null;
    try {
      clone = (Table3D) super.clone();
    } catch (CloneNotSupportedException e) {
      // e.printStackTrace();
    }
    clone.destNum = this.destNum;
    clone.msgNum = this.msgNum;
    clone.respNum = this.respNum;
    clone.array3D = new DsCounter[clone.destNum][clone.msgNum][clone.respNum];
    clone.reset();
    for (int i = 0; i < destNum; i++)
      for (int j = 0; j < msgNum; j++)
        for (int k = 0; k < respNum; k++) {
          clone.array3D[i][j][k].setCount(this.array3D[i][j][k].getCount());
          // Persistent. Drop count was not retained earlier.
          clone.array3D[i][j][k].setDropCount(this.array3D[i][j][k].getDropCount());
        }
    clone.responseMap = new int[calRespArraySize()]; // response code 100-606
    for (int i = 0; i < responseMap.length; i++) clone.responseMap[i] = this.responseMap[i];
    return clone;
  }
}

/**
 * The helper class which is used as a counter. It can store stats for both normal messages received
 * or sent and messages that are dropped.
 */
class DsCounter {
  /** Count for normal messages received or sent. */
  int count;
  /** Count for messages dropped. */
  int dropCount;

  /**
   * Constructor that takes a count.
   *
   * @param val the count
   */
  public DsCounter(int val) {
    count = val;
  }

  public synchronized void incCount() {
    count = (count + 1) % Integer.MAX_VALUE;
  }

  public synchronized void setCount(int val) {
    count = val;
  }

  public synchronized int getCount() {
    return count;
  }
  // Persistent : Added this function to set the drop count
  public synchronized void setDropCount(int dropVal) {
    dropCount = dropVal;
  }

  public synchronized void incDropCount() {
    dropCount = (dropCount + 1) % Integer.MAX_VALUE;
  }

  public synchronized int getDropCount() {
    return dropCount;
  }

  public String toString() {
    return String.valueOf(count);
  }

  public Object clone() {
    return new DsCounter(count);
  }
} // Ends class DsCounter
