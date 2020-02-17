// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsStateMachineException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

/**
 * Simple wrapper for the state table data. Holds the current state and implements state transition
 * logic.
 */
public final class DsSipStateTable
    implements DsSipStateMachineDefinitions, Serializable, DsSipConstants {
  /** Server transaction logging category. */
  protected static Logger serverCat = DsLog4j.LlSMServerSwitchStateCat;
  /** Client transaction logging category. */
  protected static Logger clientCat = DsLog4j.LlSMClientSwitchStateCat;

  // static
  // {
  // serverCat.setLevel(Level.INFO);
  // DsLog4j.quiet(serverCat);
  // clientCat.setLevel(Level.INFO);
  // DsLog4j.quiet(clientCat);
  // }

  /** A reference to the underlying state table. */
  protected int m_stateTable[][];
  /** The current state. */
  protected int m_curState = DS_INITIAL;

  /**
   * Constructs the state table with the specified state table data.
   *
   * @param state_table the state table data array containing states and transitions
   */
  public DsSipStateTable(int[][] state_table) {
    m_curState = DS_INITIAL;
    m_stateTable = state_table;
  }

  /**
   * This method is used after de-serialization.
   *
   * @param state_table The state_table to use.
   */
  public void setStateTable(int[][] state_table) {
    m_stateTable = state_table;
  }

  /**
   * Tells whether the transaction is started or not.
   *
   * @return false if the transaction is in the DsSipStateMachineDefinitions.DS_INITIAL state.
   *     Otherwise returns true.
   */
  public synchronized boolean isStarted() {
    return m_curState != DS_INITIAL;
  }

  /**
   * Switches the state based on the input and the current state.
   *
   * @param input the input to the state machine
   * @return the bitwise OR of the next state and the input
   */
  protected synchronized int switchState(int input) {
    int next_state = 0;
    int transition = input | m_curState;
    try {
      switch (m_curState) {
        case DS_INITIAL:
          next_state = m_stateTable[DS_INITIAL_IDX][input];
          break;
        case DS_CALLING:
          next_state = m_stateTable[DS_CALLING_IDX][input];
          break;
        case DS_PROCEEDING:
          next_state = m_stateTable[DS_PROCEEDING_IDX][input];
          break;
          // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
          // Add two new states: one for wait prack, and one for reliable proceeding
        case DS_WAIT_PRACK:
          next_state = m_stateTable[DS_STI_WAIT_PRACK_IDX][input];
          break;
        case DS_CTI_RELPROCEEDING:
          next_state = m_stateTable[DS_CTI_RELPROCEEDING_IDX][input];
          break;
        case DS_STI_RELPROCEEDING:
          next_state = m_stateTable[DS_STI_RELPROCEEDING_IDX][input];
          break;
        case DS_COMPLETED:
          next_state = m_stateTable[DS_COMPLETED_IDX][input];
          break;
        case DS_CONFIRMED:
          next_state = m_stateTable[DS_CONFIRMED_IDX][input];
          break;
        case DS_XINITIAL:
          next_state = m_stateTable[DS_XINITIAL_IDX][input];
          break;
        case DS_XCOMPLETED:
          next_state = m_stateTable[DS_XCOMPLETED_IDX][input];
          break;
        case DS_XCONFIRMED:
          next_state = m_stateTable[DS_XCONFIRMED_IDX][input];
          break;
        case DS_TERMINATED:
        case DS_UNDEFINED:
        default:
          next_state = DS_UNDEFINED;
          break;
      }
    } catch (IndexOutOfBoundsException exc) {
      next_state = DS_UNDEFINED;
    } catch (Throwable t) {
      next_state = DS_UNDEFINED;
    }

    ///////////////////////////////////////////////////////
    // debug output
    ///////////////////////////////////////////////////////
    if (serverCat.isEnabled(Level.INFO) || clientCat.isEnabled(Level.INFO)) {
      Logger cat = null;

      // CAFFEINE 2.0 - DEVELOPMENT - Moved to a new class DsSipStateMachineTransitions
      if (m_stateTable == DsSipStateMachineTransitions.ST_TRANSITIONS) {
        cat = serverCat;
      } else if (m_stateTable == DsSipStateMachineTransitions.STI_TRANSITIONS) {
        cat = serverCat;
      } else if (m_stateTable == DsSipStateMachineTransitions.STIX_TRANSITIONS) {
        cat = serverCat;
      } else if (m_stateTable == DsSipStateMachineTransitions.CT_TRANSITIONS) {
        cat = clientCat;
      } else if (m_stateTable == DsSipStateMachineTransitions.CTI_TRANSITIONS) {
        cat = clientCat;
      } else if (m_stateTable == DsSipStateMachineTransitions.CTIX_TRANSITIONS) {
        cat = clientCat;
      }
      if ((cat != null) && (cat.isEnabled(Level.INFO))) {
        String input_str = null;
        String table = null;
        String cur_state_str = printState(m_curState);
        String next_state_str = printState(next_state != DS_UNDEFINED ? next_state : m_curState);

        // CAFFEINE 2.0 - DEVELOPMENT - Moved to a new class DsSipStateMachineTransitions
        if (m_stateTable == DsSipStateMachineTransitions.ST_TRANSITIONS) {
          input_str = printSTInput(transition & DS_INPUT_MASK);
          table = "Server non-INVITE";
        } else if (m_stateTable == DsSipStateMachineTransitions.STI_TRANSITIONS) {
          input_str = printSTInput(transition & DS_INPUT_MASK);
          table = "Server INVITE";
        } else if (m_stateTable == DsSipStateMachineTransitions.STIX_TRANSITIONS) {
          input_str = printSTInput(transition & DS_INPUT_MASK);
          table = "XServer INVITE";
        } else if (m_stateTable == DsSipStateMachineTransitions.CT_TRANSITIONS) {
          input_str = printCTInput(transition & DS_INPUT_MASK);
          table = "Client non-INVITE";
        } else if (m_stateTable == DsSipStateMachineTransitions.CTI_TRANSITIONS) {
          input_str = printCTInput(transition & DS_INPUT_MASK);
          table = "Client INVITE";
        } else if (m_stateTable == DsSipStateMachineTransitions.CTIX_TRANSITIONS) {
          input_str = printCTInput(transition & DS_INPUT_MASK);
          table = "XClient INVITE";
        }
        if (cat.isEnabled(Level.INFO))
          cat.log(
              Level.INFO,
              (new StringBuffer("STATECHANGE "))
                  .append(new SimpleDateFormat("HH:mm:ss:S").format(new Date()))
                  .append(' ')
                  .append(table)
                  .append(' ')
                  .append(cur_state_str)
                  .append(' ')
                  .append(input_str)
                  .append(' ')
                  .append(next_state_str));
      }
      // CAFFEINE 2.0 DEVELOPMENT - Add a log statement
      if (next_state == DS_UNDEFINED
          && cat.isEnabled(Level.INFO)) { // Three possible ways this could happen:
        // m_curState was UNKNOWN;
        // IndexOutOfBoundsException;
        // Any other exceptions;
        // DS decided to ignore them, but it should be recorded
        // for debugging purposes.
        cat.log(Level.INFO, "switchState(): Next State is DS_UNDEFINED");
      }
    }
    ///////////////////////////////////////////////////////
    // end debug output
    ///////////////////////////////////////////////////////

    m_curState = next_state != DS_UNDEFINED ? next_state : m_curState;
    return transition;
  }

  String getName() {
    String table = "";

    // CAFFEINE 2.0 - DEVELOPMENT -  Moved to a new class DsSipStateMachineTransitions
    if (m_stateTable == DsSipStateMachineTransitions.ST_TRANSITIONS) {
      table = "Server non-INVITE";
    } else if (m_stateTable == DsSipStateMachineTransitions.STI_TRANSITIONS) {
      table = "Server INVITE";
    } else if (m_stateTable == DsSipStateMachineTransitions.STIX_TRANSITIONS) {
      table = "XServer INVITE";
    } else if (m_stateTable == DsSipStateMachineTransitions.CT_TRANSITIONS) {
      table = "Client non-INVITE";
    } else if (m_stateTable == DsSipStateMachineTransitions.CTI_TRANSITIONS) {
      table = "Client INVITE";
    } else if (m_stateTable == DsSipStateMachineTransitions.CTIX_TRANSITIONS) {
      table = "XClient INVITE";
    }
    return table;
  }

  /**
   * Returns string representation of the client inputs.
   *
   * @return String representing the client input. Useful for debugging.
   * @param input the client transaction input type
   */
  public static String printCTInput(int input) {
    switch (input) {
      case (DS_CT_IN_START):
        return "DS_CT_IN_START";
      case (DS_CT_IN_T1):
        return "DS_CT_IN_T1";
      case (DS_CT_IN_T1_EXPIRED):
        return "DS_CT_IN_T1_EXPIRED";
      case (DS_CT_IN_Tp):
        return "DS_CT_IN_Tp";
      case (DS_CT_IN_TIMEOUT):
        return "DS_CT_IN_TIMEOUT";
      case (DS_CT_IN_Tn):
        return "DS_CT_IN_Tn";
      case (DS_CT_IN_CANCEL):
        return "DS_CT_IN_CANCEL";
      case (DS_CT_IN_CANCEL_TIMER):
        return "DS_CT_IN_CANCEL_TIMER";
      case (DS_CT_IN_ACK):
        return "DS_CT_IN_ACK";
      case (DS_CT_IN_PROVISIONAL):
        return "DS_CT_IN_PROVISIONAL";
      case (DS_CT_IN_2XX):
        return "DS_CT_IN_2XX";
      case (DS_CT_IN_3TO6XX):
        return "DS_CT_IN_3TO6XX";
      case (DS_CT_IN_NEXT_SERVER):
        return "DS_CT_IN_NEXT_SERVER";
      case (DS_CT_IN_NO_SERVER):
        return "DS_CT_IN_NO_SERVER";
      case (DS_CT_IN_IO_EXCEPTION):
        return "DS_CT_IN_IO_EXCEPTION";
      case (DS_CT_IN_OTHER_EXCEPTION):
        return "DS_CT_IN_OTHER_EXCEPTION";
      case (DS_CT_IN_SERVICE_UNAVAILABLE):
        return "DS_CT_IN_SERVICE_UNAVAILABLE";
        // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
      case (DS_CT_IN_REL_PROVISIONAL):
        return "DS_CT_IN_REL_PROVISIONAL";
      case (DS_CT_IN_PRACK):
        return "DS_CT_IN_PRACK";
    }
    return "UNKNOWN CT INPUT";
  }

  /**
   * Returns string representation of the server inputs.
   *
   * @param input the server transaction input type
   * @return String representing the server input. Useful for debugging.
   */
  public static String printSTInput(int input) {
    switch (input) {
      case (DS_ST_IN_START):
        return "DS_ST_IN_START";
      case (DS_ST_IN_REQUEST):
        return "DS_ST_IN_REQUEST";
      case (DS_ST_IN_PROVISIONAL):
        return "DS_ST_IN_PROVISIONAL";
      case (DS_ST_IN_TPROVISIONAL):
        return "DS_ST_IN_TPROVISIONAL";
      case (DS_ST_IN_2XX):
        return "DS_ST_IN_2XX";
      case (DS_ST_IN_3TO6XX):
        return "DS_ST_IN_3TO6XX";
      case (DS_ST_IN_CANCEL):
        return "DS_ST_IN_CANCEL";
      case (DS_ST_IN_TIMEOUT):
        return "DS_ST_IN_TIMEOUT";
      case (DS_ST_IN_T1):
        return "DS_ST_IN_T1";
      case (DS_ST_IN_T1_EXPIRED):
        return "DS_ST_IN_T1_EXPIRED";
      case (DS_ST_IN_Tn):
        return "DS_ST_IN_Tn";
      case (DS_ST_IN_ACK):
        return "DS_ST_IN_ACK";
      case (DS_ST_IN_IO_EXCEPTION):
        return "DS_ST_IN_IO_EXCEPTION";
      case (DS_ST_IN_OTHER_EXCEPTION):
        return "DS_ST_IN_OTHER_EXCEPTION";
      case (DS_ST_IN_NEXT_CLIENT):
        return "DS_ST_IN_NEXT_CLIENT";
      case (DS_ST_IN_NO_CLIENT):
        return "DS_ST_IN_NO_CLIENT";
        // CAFFEINE 2. 0 DEVELOPMENT - (EDCS-295391) PRACK Support
      case (DS_ST_IN_REL_PROVISIONAL):
        return "DS_ST_IN_REL_PROVISIONAL";
      case (DS_ST_IN_PRACK):
        return "DS_ST_IN_PRACK";
    }
    return "UNKNOWN ST INPUT";
  }

  /**
   * Returns string representation of the state.
   *
   * @param state the low level transaction state
   * @return String representing the state. Useful for debugging.
   */
  public static String printState(int state) {
    switch (state) {
      case (DS_INITIAL):
        return "DS_INITIAL";
      case (DS_CALLING):
        return "DS_CALLING";
      case (DS_PROCEEDING):
        return "DS_PROCEEDING";
        // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
      case (DS_WAIT_PRACK):
        return "DS_WAIT_PRACK";
      case (DS_CTI_RELPROCEEDING):
        return "DS_CTI_RELPROCEEDING";
      case (DS_STI_RELPROCEEDING):
        return "DS_STI_RELPROCEEDING";
      case (DS_COMPLETED):
        return "DS_COMPLETED";
      case (DS_CONFIRMED):
        return "DS_CONFIRMED";
      case (DS_TERMINATED):
        return "DS_TERMINATED";
      case (DS_XINITIAL):
        return "DS_XINITIAL";
      case (DS_XCOMPLETED):
        return "DS_XCOMPLETED";
      case (DS_XCONFIRMED):
        return "DS_XCONFIRMED";
      case (DS_XTERMINATED):
        return "DS_XTERMINATED";
      case (DS_UNDEFINED):
        return "DS_UNDEFINED";
    }
    return "UNKNOWN STATE";
  }

  /**
   * Throw a DsStateMachineException with information on current state, input and state table.
   *
   * @param transition the transition during which the exception occurred
   * @throws always thrown showing the transition
   */
  void throwException(int transition) throws DsStateMachineException {
    throwException(transition, null);
  }

  /**
   * Throw a DsStateMachineException with information on current state, input and state table.
   *
   * @param transition the transition during which the exception occurred
   * @param info extra information on the cause of the exception
   * @throws always thrown showing the transition and description info
   */
  void throwException(int transition, String info) throws DsStateMachineException {
    String table_desc = "unknown table";
    String input = "unknown input";

    // CAFFEINE 2.0 - DEVELOPMENT - Moved to a new class DsSipStateMachineTransitions
    if (m_stateTable == DsSipStateMachineTransitions.ST_TRANSITIONS) {
      table_desc = "Server non-INVITE";
      input = printSTInput(transition & DS_INPUT_MASK);
    } else if (m_stateTable == DsSipStateMachineTransitions.STI_TRANSITIONS) {
      table_desc = "Server INVITE";
      input = printSTInput(transition & DS_INPUT_MASK);
    } else if (m_stateTable == DsSipStateMachineTransitions.STIX_TRANSITIONS) {
      table_desc = "Server INVITE 2XX UAS Extra";
      input = printSTInput(transition & DS_INPUT_MASK);
    } else if (m_stateTable == DsSipStateMachineTransitions.CT_TRANSITIONS) {
      table_desc = "Client non-INVITE";
      input = printCTInput(transition & DS_INPUT_MASK);
    } else if (m_stateTable == DsSipStateMachineTransitions.CTI_TRANSITIONS) {
      table_desc = "Client INVITE";
      input = printCTInput(transition & DS_INPUT_MASK);
    } else if (m_stateTable == DsSipStateMachineTransitions.CTIX_TRANSITIONS) {
      table_desc = "Client INVITE 2XX UAC Extra";
      input = printCTInput(transition & DS_INPUT_MASK);
    }
    throw new DsStateMachineException(
        "State Table:"
            + table_desc
            + ", Current State: "
            + printState(transition & DS_MASK)
            + ", Input: "
            + input
            + ((info != null) ? (", Info: " + info) : ""));
  }

  /**
   * Returns the current state.
   *
   * @return the current state
   */
  public int getState() {
    return m_curState;
  }

  /**
   * Sets the "initial retry delay" time, in millisecs, that will be the default values for all the
   * transactions generated throughout the stack. The specified value exhibits the behavior that how
   * much time a transaction should wait for before resending the SIP message in case of UDP, where
   * retransmissions provide the mechanism for the reliability.
   *
   * @deprecated use DsSipTimers.setTimerValue(DsSipConstants.T1, millisecs).
   * @param millisecs initial retry delay time in millisecs
   */
  /*
  public static void setInitialRetryDelay(int millisecs)
  {
      DsSipTimers.setTimerValue(DsSipConstants.T1, millisecs);
  }
  */

  /**
   * Returns the "initial retry delay" time, in millisecs, that is the default values for all the
   * transactions generated throughout the stack. The specified value exhibits the behavior that how
   * much time a transaction waits for before resending the SIP message in case of UDP, where
   * retransmissions provide the mechanism for the reliability. This method is identical to getT1()
   * which is preferred.
   *
   * @deprecated use DsSipTimers.getTimerValue(DsSipConstants.T1).
   * @return initial retry delay time in millisecs
   */
  /*
  public static int getInitialRetryDelay()
  {
      return DsSipTimers.getTimerValue(DsSipConstants.T1);
  }
  */

  /**
   * Sets the timer value T1 as specified in bis05. T1 is used to control the rettransmission of SIP
   * messages in the case of unreliable transport protocol like UDP. Timers using T1 as their
   * values: TA, TE, TG. Timers whose values depend on T1: TB, TH.
   *
   * @param millisecs T1 in millisecs
   */
  /*
  public static void setT1(int millisecs)
  {
      setInitialRetryDelay(millisecs);
  }
  */
  /**
   * Returns the timer value T1 as specified in bis05. T1 is used to control the rettransmission of
   * SIP messages in the case of unreliable transport protocol like UDP. Timers using T1 as their
   * value: TA, TE, TG. Timers whose values depend on T1: TB, TH.
   *
   * @return T1 in millisecs
   */
  /*
  public static int getT1()
  {
      return getInitialRetryDelay();
  }
  */
  /**
   * Sets the timer value T2 as specified in bis05. T2 is used to control the rettransmission of SIP
   * messages in the case of unreliable transport protocol like UDP. No timers directly use T2, but
   * it affects the retransmission delay.
   *
   * @param millisecs T2 in millisecs
   */
  /*
  public static void setT2(int millisecs)
  {
      defaultT2 = millisecs;
  }
  */
  /**
   * Returns the timer value T2 as specified in bis05. T2 is used to control the rettransmission of
   * SIP messages in the case of unreliable transport protocol like UDP. No timers directly use T2,
   * but it affects the retransmission delay.
   *
   * @return T2 in millisecs
   */
  /*
  public static int getT2()
  {
      return defaultT2;
  }
  */
  /**
   * Sets the timer value T3 as specified in bis05. Timers using T3 as their values include TF, TD,
   * TJ.
   *
   * @param millisecs T3 in millisecs.
   */
  /*
  public static void setT3(int millisecs)
  {
      defaultT3 = millisecs;
  }
  */
  /**
   * Returns the timer value T3 as specified in bis05. Timers using T3 as their values include TF,
   * TD, TJ.
   *
   * @return T3 in millisecs
   */
  /*
  public static int getT3()
  {
      return defaultT3;
  }
  */

  /**
   * Sets the timer value T4 as specified in bis05. Timers using T4 as their values include TK, TI.
   *
   * @param millisecs T4 in millisecs.
   */
  /*
  public static void setT4(int millisecs)
  {
      defaultT4 = millisecs;
  }
  */
  /**
   * Returns the timer value T4 as specified in bis05. Timers using T4 as their values include TK,
   * TI.
   *
   * @return T4 in millisecs
   */
  /*
  public static int getT4()
  {
      return defaultT4;
  }
  */

  /**
   * Sets the max transaction duration timer value. This value controls max time duration a
   * transaction stays alive. You may want to set this timer value to prevent a transaction from
   * hanging around for too long in some unexpected situations.
   *
   * @param millisecs the max transaction duration.
   */
  /*
  public static void setTn(int millisecs)
  {
      defaultTn = millisecs;
  }
  */
  /**
   * Returns the max transaction duration timer value. This value controls max time duration a
   * transaction stays alive. You may want to set this timer value to prevent a transaction from
   * hanging around for too long in some unexpected situations.
   *
   * @returns the max transaction duration.
   */
  /*
  public static int getTn()
  {
      return defaultTn;
  }
  */
  /*
   * Sets the value for timer TC as specified in bis05. TC is used in the case of
   * client INVITE transaction to control how long that transaction should stay in
   * PRODCEEDING state(i.e. roughly how long does it "let the phone ring").
   * @param millisecs the max time duration for a client INVITE transaction to stay
   *        in PROCEEDING state.
   */
  /*
  public static void setTcValue(int millisecs)
  {
      defaultTcValue  = millisecs;
  }
  */
  /*
   * Returns the value for timer TC as specified in bis05. TC is used in the case of
   * client INVITE transaction to control how long that transaction should stay in
   * PRODCEEDING state(i.e. roughly how long does it "let the phone ring").
   * @returns the max time duration for a client INVITE transaction to stay
   *        in PROCEEDING state.
   */
  /*
  public static int getTcValue()
  {
      return defaultTcValue;
  }
  */
  /**
   * Sets the "transaction completion timeout" time, in millisecs, that will be the default value
   * for all the transactions generated throughout the stack. The specified value exhibits the
   * behavior that how much time a transaction should wait for and keep on retransmitting the SIP
   * message in case of UDP, where retransmissions provide the mechanism for the reliability, before
   * timing out.
   *
   * @deprecated In bis 7, completion timeout is controlled by timer value 64*T1(for client INVITE
   *     transaction and server non-INVITE transaction) and T4(for server INVITE transaction and
   *     client non-INVITE transaction). This method is now only equivalent to
   *     DsSipTimers.setTimerValue(T4, millisecs). Use setTransactionCompleteTimeout(int transType,
   *     int millisecs) instead.
   * @param millisecs time in millisecs a transaction should wait for before timing out
   */
  /*
  public static void setCompletionTimeout(int millisecs)
  {
      //defaultC2FTimer  = millisecs;
      //defaultT4 = millisecs;
      DsSipTimers.setTimerValue(DsSipConstants.T4, millisecs);
  }
  */

  /**
   * Sets the "transaction completion timeout" time, in millisecs, that will be the default value
   * for all the transactions generated throughout the stack. The specified value exhibits the
   * behavior that how much time a transaction should wait for and keep on retransmitting the SIP
   * message in case of UDP, where retransmissions provide the mechanism for the reliability, before
   * timing out.
   *
   * <p>Default value for Invite server trans and non-Invite client trans is 5 secs and for Invite
   * server trans and non-Invite client trans it is 32(64*T1) secs.
   *
   * <p>Note that this method will not work for Invite server trans and non-Invite client trans
   * since the value depends on T1.
   *
   * @param transType the transaction type defined in DsSipConstants. Possible values are
   *     CLIENT_TRANS(for non-INVITE), INVITE_CLIENT_TRANS, SERVER_TRANS and INVITE_SERVER_TRANS.
   * @param millisecs the time(in millisecs) taken before a transaction gets completed with a
   *     timeout.
   */
  /*
  public static void setTransactionCompleteTimeout(byte transType, int millisecs)
              throws DsException
  {
      if (transType == CLIENT_TRANS || transType == INVITE_SERVER_TRANS)
          DsSipTimers.setTimerValue(T4, millisecs);
      //for others, the default value is 64*T1. Need to re-set T1.
      else
          throw new DsException("for Invite server trans and non-Invite client trans,"+
              " the completion timeout value is 64*T1, can not set this value directly");
  }
  */

  /**
   * Returns the "transaction completion timeout" time, in millisecs, that is the default value for
   * all the transactions generated throughout the stack. The specified value exhibits the behavior
   * that how much time a transaction should wait for and keep on retransmitting the SIP message in
   * case of UDP, where retransmissions provide the mechanism for the reliability, before timing
   * out.
   *
   * @deprecated In bis 7, completion timeout is controlled by timer value 64*T1(for client INVITE
   *     transaction and server non-INVITE transaction) and T4(for server INVITE transaction and
   *     client non-INVITE transaction). This method is now only equivalent to
   *     DsSipTimers.getTimerValue(DsSipConstants.T4). Use getTransactionCompleteTimeout(int
   *     transType) instead.
   * @return time, in millisecs, a transaction should wait for before timing out
   */
  /*
  public static int getCompletionTimeout()
  {
      //return defaultC2FTimer;
      //return defaultT4;
      return DsSipTimers.getTimerValue(DsSipConstants.T4);
  }
  */

  /**
   * Returns the "transaction completion timeout" time, in millisecs, that is the default value for
   * all the transactions generated throughout the stack. The specified value exhibits the behavior
   * that how much time a transaction should wait for and keep on retransmitting the SIP message in
   * case of UDP, where retransmissions provide the mechanism for the reliability, before timing
   * out.
   *
   * <p>Default value for Invite client trans and non-Invite server trans is 32 secs and for Invite
   * server trans and non-Invite client trans it is 5 secs.
   *
   * @param transType the transaction type. its values are defined in DsSipConstants. Possible
   *     values are CLIENT_TRANS(for non-INVITE), INVITE_CLIENT_TRANS, SERVER_TRANS and
   *     INVITE_SERVER_TRANS.
   * @return the time taken before a transaction gets completed with a timeout.
   */
  /*
  public static int getTransactionCompleteTimeout(int transType)
  {
      if (transType == CLIENT_TRANS || transType == INVITE_SERVER_TRANS)
          return DsSipTimers.getTimerValue(T4);
      else
          return 64 * DsSipTimers.getTimerValue(T1);
  }
  */

  // Default initial values for the timers, in milliseconds,
  // which can be configured through SNMP or CLI
  /// **
  // * Default initial value for the T1 timer in milliseconds.
  // */
  // protected static int defaultT1 = 500;
  /// **
  // * Default initial value for the T2 timer in milliseconds.
  // */
  // protected static int defaultT2 = 4000;
  /// **
  // * Default initial value for the T3 timer in milliseconds.
  // */
  // protected static int defaultT3 = 16000;
  /// **
  // * Default initial value for the T4 timer in milliseconds.
  // */
  // protected static int defaultT4 = 5000;
  /// **
  // * Default initial value for the max trans Tn timer in milliseconds.
  // */
  // protected static int defaultTn = 64000;
  /// **
  // * Default initial value for the Tc timer in milliseconds.
  // * It specifies how long a INVITE client trans stay in proceeding state for
  // * a final response. Must be supplied by user code
  // */
  // protected static int defaultTcValue = 32000; //TC
  /// **
  // * Default value for transaction completion time in milliseconds.
  // */
  // protected static int defaultC2FTimer = 32000; //To-->TIMEOUT
}
