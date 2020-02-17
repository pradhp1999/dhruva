/*
    Copyright (c) 2003-2004 by Cisco Systems, Inc.
    All rights reserved.
*/

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipConstants;

/** Low level state machine transition table. */
public class DsSipStateMachineTransitions implements DsSipStateMachineDefinitions, DsSipConstants {

  //////////////////////////////////////////////////////////////////
  /// Client Transaction INITIAL state transitions  (CT_INITIAL)
  //////////////////////////////////////////////////////////////////

  /** client INVITE transaction transitions from INITIAL state. */
  public static final int[] CTI_INITIAL_TRANSITIONS = new int[CTI_TABLE_INDEX];
  /** client non-INVITE transaction transitions from INITIAL state. */
  public static final int[] CT_INITIAL_TRANSITIONS = new int[CT_TABLE_INDEX];

  //////////////////////////////////////////////////////////////////
  /// Client Transaction CALLING state transitions (CT_CALLING)
  //////////////////////////////////////////////////////////////////
  /** client INVITE transaction transitions from CALLING state. */
  public static final int[] CTI_CALLING_TRANSITIONS = new int[CTI_TABLE_INDEX];
  /** client non-INVITE transaction transitions from CALLING state. */
  public static final int[] CT_CALLING_TRANSITIONS = new int[CT_TABLE_INDEX];

  //////////////////////////////////////////////////////////////////
  /// Client Transaction PROCEEDING state transitions (CT_PROCEEDING)
  //////////////////////////////////////////////////////////////////
  /** client INVITE transaction transitions from PROCEEDING state. */
  public static final int[] CTI_PROCEEDING_TRANSITIONS = new int[CTI_TABLE_INDEX];
  /** client non-INVITE transaction transitions from PROCEEDING state. */
  public static final int[] CT_PROCEEDING_TRANSITIONS = new int[CT_TABLE_INDEX];

  //////////////////////////////////////////////////////////////////
  /// Client Transaction RELPROCEEDING state transitions (CT_RELPROCEEDING)
  //////////////////////////////////////////////////////////////////
  /** client INVITE transaction transitions from RELPROCEEDING state. */
  public static final int[] CTI_RELPROCEEDING_TRANSITIONS = new int[CTI_TABLE_INDEX];

  //////////////////////////////////////////////////////////////////
  /// Client Transaction COMPLETED state transitions (CT_COMPLETED)
  //////////////////////////////////////////////////////////////////
  /** client INVITE transaction transitions from COMPLETED state. */
  public static final int[] CTI_COMPLETED_TRANSITIONS = new int[CTI_TABLE_INDEX];
  /** client non-INVITE transaction transitions from COMPLETED state. */
  public static final int[] CT_COMPLETED_TRANSITIONS = new int[CT_TABLE_INDEX];

  //////////////////////////////////////////////////////////////////
  /// Client Transaction extra INITIAL state transitions (CTIX_XINITIAL)
  //////////////////////////////////////////////////////////////////
  /** client INVITE transaction transitions from COMPLETED state. */
  public static final int[] CTIX_XINITIAL_TRANSITIONS = new int[CTIX_TABLE_INDEX];

  //////////////////////////////////////////////////////////////////
  /// Client Transaction extra COMPLETED state transitions (CTIX_XCOMPLETED)
  //////////////////////////////////////////////////////////////////
  /** client INVITE transaction transitions from COMPLETED state. */
  public static final int[] CTIX_XCOMPLETED_TRANSITIONS = new int[CTIX_TABLE_INDEX];

  //////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////
  /////////    Server State Machine Transitions
  //////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////

  //////////////////////////////////////////////////////////////////
  /// Server Transaction INITIAL state transitions (ST_INITIAL)
  //////////////////////////////////////////////////////////////////
  /** server INVITE transaction transitions from INITIAL state. */
  public static final int[] STI_INITIAL_TRANSITIONS = new int[STI_TABLE_INDEX];
  /** client non-INVITE transaction transitions from INITIAL state. */
  public static final int[] ST_INITIAL_TRANSITIONS = new int[ST_TABLE_INDEX];

  //////////////////////////////////////////////////////////////////
  /// Server Transaction CALLING state transitions (ST_CALLING)
  //////////////////////////////////////////////////////////////////
  /** server INVITE transaction transitions from CALLING state. */
  public static final int[] STI_CALLING_TRANSITIONS = new int[STI_TABLE_INDEX];
  /** server non-INVITE transaction transitions from CALLING state. */
  public static final int[] ST_CALLING_TRANSITIONS = new int[ST_TABLE_INDEX];

  //////////////////////////////////////////////////////////////////
  /// Server Transaction PROCEEDING state transitions (ST_PROCEEDING)
  //////////////////////////////////////////////////////////////////
  /** server INVITE transaction transitions from PROCEEDING state. */
  public static final int[] STI_PROCEEDING_TRANSITIONS = new int[STI_TABLE_INDEX];
  /** server non-INVITE transaction transitions from PROCEEDING state. */
  public static final int[] ST_PROCEEDING_TRANSITIONS = new int[ST_TABLE_INDEX];

  //////////////////////////////////////////////////////////////////
  /// Server Transaction COMPLETED state transitions (ST_COMPLETED)
  //////////////////////////////////////////////////////////////////
  /** server INVITE transaction transitions from COMPLETED state. */
  public static final int[] STI_COMPLETED_TRANSITIONS = new int[STI_TABLE_INDEX];
  /** server non-INVITE transaction transitions from COMPLETED state. */
  public static final int[] ST_COMPLETED_TRANSITIONS = new int[ST_TABLE_INDEX];

  //////////////////////////////////////////////////////////////////
  /// Server Transaction CONFIRMED state transitions (STI_CONFIRMED)
  //////////////////////////////////////////////////////////////////
  /** server INVITE transaction transitions from CONFIRMED state. */
  public static final int[] STI_CONFIRMED_TRANSITIONS = new int[STI_TABLE_INDEX];

  //////////////////////////////////////////////////////////////////
  /// Server Transaction WAIT_PRACK state transitions (STI_WAIT_PRACK)
  //////////////////////////////////////////////////////////////////
  /** server INVITE transaction transitions from WAIT_PRACK state. */
  public static final int[] STI_WAIT_PRACK_TRANSITIONS = new int[STI_TABLE_INDEX];

  //////////////////////////////////////////////////////////////////
  /// Server Transaction RELPROCEEDING state transitions (STI_RELPROCEEDING)
  //////////////////////////////////////////////////////////////////
  /** server INVITE transaction transitions from RELPROCEEDING state. */
  public static final int[] STI_RELPROCEEDING_TRANSITIONS = new int[STI_TABLE_INDEX];

  //////////////////////////////////////////////////////////////////
  /// Server Transaction extra INITIAL state transitions (STIX_XINITIAL)
  //////////////////////////////////////////////////////////////////
  /** server INVITE transaction transitions from XINITIAL state. */
  public static final int[] STIX_XINITIAL_TRANSITIONS = new int[STIX_TABLE_INDEX];

  //////////////////////////////////////////////////////////////////
  /// Server Transaction extra COMPLETED state transitions (STIX_XCOMPLETED)
  //////////////////////////////////////////////////////////////////
  /** server INVITE transaction transitions from XCOMPLETED state. */
  public static final int[] STIX_XCOMPLETED_TRANSITIONS = new int[STIX_TABLE_INDEX];

  //////////////////////////////////////////////////////////////////
  /// Server Transaction extra CONFIRMED state transitions (STIX_XCONFIRMED)
  //////////////////////////////////////////////////////////////////
  /** server INVITE transaction transitions from XCONFIRMED state. */
  public static final int[] STIX_XCONFIRMED_TRANSITIONS = new int[STIX_TABLE_INDEX];

  static {
    // PRACK: New State added: RPR (index 5) for RELPROCEEDING, Reliable Proceeding
    //        New input added: RELP for "received reliable provisional response"
    //        New input added: PRAC for "PRACK sent by USER code"
    //        Both are added at the end of the row/column, so that the original states/inputs
    //        won't be affected too much.
    //
    // Client Transaction State Transition Tables
    // //////////////////////////////////////////////////////////////////////////////////////
    // (Invite)
    //                                       (Input)
    //                     0   1   2      3   4   5   6   7    8   9   10  11   12  13  14  15  16
    // 17  18
    // ---------------------------------------------------------------------------------------------------
    // (CTI_TABLE)
    // ///////////////////////////////////////////////////////////////////////////////////////////////////
    //   State           | ST  T1  T1_EXP Tp  TO  Tn  CNL CNLT ACK PR  2XX 3TO6 SUR NXS NOS IOE OE
    // RELP PRAC
    // ---------------------------------------------------------------------------------------------------
    // 0 INI  INITIAL    | CAL --- TER    --- --- TER --- ---  --- --- --- ---  --- --- --- --- ---
    // --- ---
    // 1 CAL  CALLING    | --- CAL TER    --- --- TER CAL TER  --- PRO TER COM  CAL INI TER CAL ---
    // RPR ---
    // 2 PROC PROCEEDING | --- PRO TER    TER --- TER PRO TER  --- PRO TER COM  CAL --- --- --- ---
    // RPR ---
    // 3 COMP COMPLETED  | --- COM ---    --- TER TER COM COM  COM COM TER COM  COM --- --- TER ---
    // --- ---
    // 4 CONF CONFIRMED  | --- --- ---    --- --- --- --- ---  --- --- --- ---  --- --- --- --- ---
    // --- ---
    // 5 TERM TERMINATED | --- --- ---    --- --- --- TER ---  --- --- --- ---  --- --- --- --- ---
    // --- ---
    // 6 RPRO RELPROCEED | --- RPR TER    TER --- TER RPR TER  --- --- TER COM  CAL --- --- --- ---
    // RPR RPR
    // ////////////////////////////////////////////////////////////////////////////////// (Non-te)
    //  (CT_TABLE)
    // ///////////////////////////////////////////////////////////////////////////////////////////
    //   State           | ST  T1  T1_EXP Tp  TO  Tn  CNL CNLT --- PR  2XX 3TO6 SUR NXS NOS IOE OE
    // -------------------------------------------------------------------------------------------
    // 0 INI  INITIAL    | CAL --- TER    --- --- TER --- ---  --- --- --- ---  --- --- --- --- ---
    // 1 CAL  CALLING    | --- CAL TER    --- --- TER CAL TER  --- PRO COM COM  CAL INI TER CAL ---
    // 2 PROC PROCEEDING | --- PRO TER    --- --- TER PRO TER  --- PRO COM COM  CAL --- --- --- ---
    // 3 COMP COMPLETED  | --- COM ---    --- TER TER --- COM  --- COM COM COM  COM --- --- --- ---
    // 4 CONF CONFIRMED  | --- --- ---    --- --- --- --- ---  --- --- --- ---  --- --- --- --- ---
    // 5 TERM TERMINATED | --- --- ---    --- --- --- TER ---  --- --- --- ---  --- --- --- --- ---
    // ///////////////////////////////////////////////////////////////////////////////////////////

    // Client transaction inputs
    //////////////////////////////////////////////////////
    // ST     ==>  the first input
    // T1     ==>  request rtx timer
    // T1_EXP ==>  request rtx timer expired
    // Tp     ==>  timer to control the duration in PROC state
    // TO     ==>  timeout timer to control the duration in COM state
    // Tn     ==>  max transaction time expired
    // CNL    ==>  user code cancels transaction
    // CNLT   ==>  cancel timer expired
    // ACK    ==>  user code acks transaction
    // PR     ==>  received provisional response
    // 2XX    ==>  2XX final responses received
    // 3TO6XX ==>  3XX-6XX final resonses received
    // SUR    ==>  Service Unavailable (503) response
    // NXS    ==>  Next server
    // NOS    ==>  No next server
    // IOE    ==>  IO exception
    // OE     ==>  other exception
    // RELP   ==>  received reliable provisional response <== New input for PRACK
    // PRAC   ==>  user code sends PRACK <== New input for PRACK
    //////////////////////////////////////////////////////
    // Timer mapping
    // -----------------------------------------------------------------------
    //                      | Timer name in SIP draft and default value
    // Timer event in code  | CTI                         CT
    // ---------------------|-------------------------------------------------
    // T1                   | TA(T1=0.5s or NA)           TE(T1=0.5s or NA)
    // T1_EXP               | TB(64*T1)                   TF(T3=16s)
    // Tp                   | TC(user supplied)
    // To                   | TD(T3 or 0s)                TK(T4 or 0s, T4=5s)
    // Tn                   | user supplied(not in draft) user supplied(not in draft)
    // -----------------------------------------------------------------------
    // * for To, 0s is for reliable transport and the other value is for unreliable ones

    // Client INVITE transaction xref
    //////////////////////////////////////////////////////////
    //
    //  DS_INITIAL (INVITE)
    //
    //  DS_CALLING  | DS_CT_IN_NEXT_SERVER

    // ///////////////////////////////////////////////////////
    //  DS_CALLING (INVITE)
    //
    //  DS_INITIAL  | DS_CT_IN_START
    //  DS_CALLING  | DS_CT_IN_T1
    //  DS_CALLING  | DS_CT_IN_CANCEL
    //  DS_CALLING  | DS_CT_IN_SERVICE_UNAVAILABLE
    //  DS_CALLING  | DS_CT_IN_IO_EXCEPTION
    //  DS_PROCEEDING  | DS_CT_IN_SERVICE_UNAVAILABLE
    //  DS_CTI_RELPROCEEDING  | DS_CT_IN_SERVICE_UNAVAILABLE

    //
    // ///////////////////////////////////////////////////////
    //  DS_PROCEEDING (INVITE)
    //
    //  DS_CALLING  | DS_CT_IN_PROVISIONAL
    //  DS_PROCEEDING  | DS_CT_IN_T1
    //  DS_PROCEEDING  | DS_CT_IN_CANCEL
    //  DS_PROCEEDING  | DS_CT_IN_PROVISIONAL

    // ///////////////////////////////////////////////////////
    //  DS_CTI_RELPROCEEDING (INVITE)
    //
    //  DS_CALLING  | DS_CT_IN_REL_PROVISIONAL
    //  DS_PROCEEDING  | DS_CT_IN_REL_PROVISIONAL
    //  DS_CTI_RELPROCEEDING  | DS_CT_IN_T1
    //  DS_CTI_RELPROCEEDING  | DS_CT_IN_CANCEL
    //  DS_CTI_RELPROCEEDING  | DS_CT_IN_REL_PROVISIONAL
    // Error condition: in REL PROCEEDING and received non-reliable 1xx
    // make it unknown, so that an exception will be thrown.
    // //  DS_CTI_RELPROCEEDING  | DS_CT_IN_PROVISIONAL
    //

    // ///////////////////////////////////////////////////////
    //  DS_COMPLETED (INVITE)
    //
    //  DS_CALLING  | DS_CT_IN_3TO6XX
    //  DS_PROCEEDING  | DS_CT_IN_3TO6XX
    //  DS_CTI_RELPROCEEDING  | DS_CT_IN_3TO6XX
    //  DS_COMPLETED  | DS_CT_IN_T1
    //  DS_COMPLETED  | DS_CT_IN_CANCEL
    //  DS_COMPLETED  | DS_CT_IN_CANCEL_TIMER
    //  DS_COMPLETED  | DS_CT_IN_ACK
    //  DS_COMPLETED  | DS_CT_IN_PROVISIONAL
    //  DS_COMPLETED  | DS_CT_IN_REL_PROVISIONAL
    //  DS_COMPLETED  | DS_CT_IN_3TO6XX
    //  DS_COMPLETED  | DS_CT_IN_SERVICE_UNAVAILABLE

    //
    // ///////////////////////////////////////////////////////
    //  DS_CONFIRMED (INVITE)
    //

    //
    // ///////////////////////////////////////////////////////
    //  DS_TERMINATED (INVITE)
    //
    //  DS_INITIAL  | DS_CT_IN_T1_EXPIRED
    //  DS_INITIAL  | DS_CT_IN_Tn
    //  DS_CALLING  | DS_CT_IN_T1_EXPIRED
    //  DS_CALLING  | DS_CT_IN_Tn
    //  DS_CALLING  | DS_CT_IN_CANCEL_TIMER
    //  DS_CALLING  | DS_CT_IN_2XX
    //  DS_CALLING  | DS_CT_IN_NO_SERVER
    //  DS_PROCEEDING  | DS_CT_IN_T1_EXPIRED
    //  DS_PROCEEDING  | DS_CT_IN_Tp
    //  DS_PROCEEDING  | DS_CT_IN_Tn
    //  DS_PROCEEDING  | DS_CT_IN_CANCEL_TIMER
    //  DS_PROCEEDING  | DS_CT_IN_2XX
    //  DS_CTI_RELPROCEEDING  | DS_CT_IN_T1_EXPIRED
    //  DS_CTI_RELPROCEEDING  | DS_CT_IN_Tp
    //  DS_CTI_RELPROCEEDING  | DS_CT_IN_Tn
    //  DS_CTI_RELPROCEEDING  | DS_CT_IN_CANCEL_TIMER
    //  DS_CTI_RELPROCEEDING  | DS_CT_IN_2XX
    //  DS_COMPLETED  | DS_CT_IN_TIMEOUT
    //  DS_COMPLETED  | DS_CT_IN_Tn
    //  DS_COMPLETED  | DS_CT_IN_2XX
    //  DS_COMPLETED  | DS_CT_IN_IO_EXCEPTION
    //  DS_TERMINATED  | DS_CT_IN_CANCEL

    //
    //

    // Client non INVITE transaction xref
    ////////////////////////////////////
    //
    //  DS_INITIAL (non-INVITE)
    //
    //  DS_CALLING  | DS_CT_IN_NEXT_SERVER

    // ///////////////////////////////////////////////////////
    //  DS_CALLING (non-INVITE)
    //
    //  DS_INITIAL  | DS_CT_IN_START
    //  DS_CALLING  | DS_CT_IN_T1
    //  DS_CALLING  | DS_CT_IN_CANCEL
    //  DS_CALLING  | DS_CT_IN_SERVICE_UNAVAILABLE
    //  DS_CALLING  | DS_CT_IN_IO_EXCEPTION
    //  DS_PROCEEDING  | DS_CT_IN_SERVICE_UNAVAILABLE

    //
    // ///////////////////////////////////////////////////////
    //  DS_PROCEEDING (non-INVITE)
    //
    //  DS_CALLING  | DS_CT_IN_PROVISIONAL
    //  DS_PROCEEDING  | DS_CT_IN_T1
    //  DS_PROCEEDING  | DS_CT_IN_CANCEL
    //  DS_PROCEEDING  | DS_CT_IN_PROVISIONAL

    // ///////////////////////////////////////////////////////
    //  DS_COMPLETED (non-INVITE)
    //
    //  DS_CALLING  | DS_CT_IN_2XX
    //  DS_CALLING  | DS_CT_IN_3TO6XX
    //  DS_PROCEEDING  | DS_CT_IN_2XX
    //  DS_PROCEEDING  | DS_CT_IN_3TO6XX
    //  DS_COMPLETED  | DS_CT_IN_T1
    //  DS_COMPLETED  | DS_CT_IN_CANCEL_TIMER
    //  DS_COMPLETED  | DS_CT_IN_PROVISIONAL
    //  DS_COMPLETED  | DS_CT_IN_2XX
    //  DS_COMPLETED  | DS_CT_IN_3TO6XX
    //  DS_COMPLETED  | DS_CT_IN_SERVICE_UNAVAILABLE

    //
    // ///////////////////////////////////////////////////////
    //  DS_CONFIRMED (non-INVITE)
    //

    //
    // ///////////////////////////////////////////////////////
    //  DS_TERMINATED (non-INVITE)
    //
    //  DS_INITIAL  | DS_CT_IN_T1_EXPIRED
    //  DS_INITIAL  | DS_CT_IN_Tn
    //  DS_CALLING  | DS_CT_IN_T1_EXPIRED
    //  DS_CALLING  | DS_CT_IN_Tn
    //  DS_CALLING  | DS_CT_IN_CANCEL_TIMER
    //  DS_CALLING  | DS_CT_IN_NO_SERVER
    //  DS_PROCEEDING  | DS_CT_IN_T1_EXPIRED
    //  DS_PROCEEDING  | DS_CT_IN_Tn
    //  DS_PROCEEDING  | DS_CT_IN_CANCEL_TIMER
    //  DS_COMPLETED  | DS_CT_IN_TIMEOUT
    //  DS_COMPLETED  | DS_CT_IN_Tn
    //  DS_TERMINATED  | DS_CT_IN_CANCEL

    //
    //

    CTI_INITIAL_TRANSITIONS[DS_CT_IN_START] = DS_CALLING;
    CTI_INITIAL_TRANSITIONS[DS_CT_IN_T1] = DS_UNDEFINED;
    CTI_INITIAL_TRANSITIONS[DS_CT_IN_T1_EXPIRED] = DS_TERMINATED;
    CTI_INITIAL_TRANSITIONS[DS_CT_IN_Tp] = DS_UNDEFINED;
    CTI_INITIAL_TRANSITIONS[DS_CT_IN_TIMEOUT] = DS_UNDEFINED;
    CTI_INITIAL_TRANSITIONS[DS_CT_IN_Tn] = DS_TERMINATED;
    CTI_INITIAL_TRANSITIONS[DS_CT_IN_CANCEL] = DS_UNDEFINED;
    CTI_INITIAL_TRANSITIONS[DS_CT_IN_CANCEL_TIMER] = DS_UNDEFINED;
    CTI_INITIAL_TRANSITIONS[DS_CT_IN_ACK] = DS_UNDEFINED;
    CTI_INITIAL_TRANSITIONS[DS_CT_IN_PROVISIONAL] = DS_UNDEFINED;
    CTI_INITIAL_TRANSITIONS[DS_CT_IN_2XX] = DS_UNDEFINED;
    CTI_INITIAL_TRANSITIONS[DS_CT_IN_3TO6XX] = DS_UNDEFINED;
    CTI_INITIAL_TRANSITIONS[DS_CT_IN_SERVICE_UNAVAILABLE] = DS_UNDEFINED;
    CTI_INITIAL_TRANSITIONS[DS_CT_IN_NEXT_SERVER] = DS_UNDEFINED;
    CTI_INITIAL_TRANSITIONS[DS_CT_IN_NO_SERVER] = DS_UNDEFINED;
    CTI_INITIAL_TRANSITIONS[DS_CT_IN_IO_EXCEPTION] = DS_UNDEFINED;
    CTI_INITIAL_TRANSITIONS[DS_CT_IN_OTHER_EXCEPTION] = DS_UNDEFINED;
    CTI_INITIAL_TRANSITIONS[DS_CT_IN_REL_PROVISIONAL] = DS_UNDEFINED;
    CTI_INITIAL_TRANSITIONS[DS_CT_IN_PRACK] = DS_UNDEFINED;
    /** client non-INVITE transaction transitions from INITIAL state. */
    CT_INITIAL_TRANSITIONS[DS_CT_IN_START] = DS_CALLING;
    CT_INITIAL_TRANSITIONS[DS_CT_IN_T1] = DS_UNDEFINED;
    CT_INITIAL_TRANSITIONS[DS_CT_IN_T1_EXPIRED] = DS_TERMINATED;
    CT_INITIAL_TRANSITIONS[DS_CT_IN_Tp] = DS_UNDEFINED;
    CT_INITIAL_TRANSITIONS[DS_CT_IN_TIMEOUT] = DS_UNDEFINED;
    CT_INITIAL_TRANSITIONS[DS_CT_IN_Tn] = DS_TERMINATED;
    CT_INITIAL_TRANSITIONS[DS_CT_IN_CANCEL] = DS_UNDEFINED;
    CT_INITIAL_TRANSITIONS[DS_CT_IN_CANCEL_TIMER] = DS_UNDEFINED;
    CT_INITIAL_TRANSITIONS[DS_CT_IN_ACK] = DS_UNDEFINED;
    CT_INITIAL_TRANSITIONS[DS_CT_IN_PROVISIONAL] = DS_UNDEFINED;
    CT_INITIAL_TRANSITIONS[DS_CT_IN_2XX] = DS_UNDEFINED;
    CT_INITIAL_TRANSITIONS[DS_CT_IN_3TO6XX] = DS_UNDEFINED;
    CT_INITIAL_TRANSITIONS[DS_CT_IN_SERVICE_UNAVAILABLE] = DS_UNDEFINED;
    CT_INITIAL_TRANSITIONS[DS_CT_IN_NEXT_SERVER] = DS_UNDEFINED;
    CT_INITIAL_TRANSITIONS[DS_CT_IN_NO_SERVER] = DS_UNDEFINED;
    CT_INITIAL_TRANSITIONS[DS_CT_IN_IO_EXCEPTION] = DS_UNDEFINED;
    CT_INITIAL_TRANSITIONS[DS_CT_IN_OTHER_EXCEPTION] = DS_UNDEFINED;

    //////////////////////////////////////////////////////////////////
    /// Client Transaction CALLING state transitions (CT_CALLING)
    //////////////////////////////////////////////////////////////////
    /** client INVITE transaction transitions from CALLING state. */
    CTI_CALLING_TRANSITIONS[DS_CT_IN_START] = DS_UNDEFINED;
    CTI_CALLING_TRANSITIONS[DS_CT_IN_T1] = DS_CALLING;
    CTI_CALLING_TRANSITIONS[DS_CT_IN_T1_EXPIRED] = DS_TERMINATED;
    CTI_CALLING_TRANSITIONS[DS_CT_IN_Tp] = DS_UNDEFINED;
    CTI_CALLING_TRANSITIONS[DS_CT_IN_TIMEOUT] = DS_UNDEFINED;
    CTI_CALLING_TRANSITIONS[DS_CT_IN_Tn] = DS_TERMINATED;
    CTI_CALLING_TRANSITIONS[DS_CT_IN_CANCEL] = DS_CALLING;
    CTI_CALLING_TRANSITIONS[DS_CT_IN_CANCEL_TIMER] = DS_TERMINATED;
    CTI_CALLING_TRANSITIONS[DS_CT_IN_ACK] = DS_UNDEFINED;
    CTI_CALLING_TRANSITIONS[DS_CT_IN_PROVISIONAL] = DS_PROCEEDING;
    CTI_CALLING_TRANSITIONS[DS_CT_IN_2XX] = DS_TERMINATED;
    CTI_CALLING_TRANSITIONS[DS_CT_IN_3TO6XX] = DS_COMPLETED;
    CTI_CALLING_TRANSITIONS[DS_CT_IN_SERVICE_UNAVAILABLE] = DS_CALLING;
    CTI_CALLING_TRANSITIONS[DS_CT_IN_NEXT_SERVER] = DS_INITIAL;
    CTI_CALLING_TRANSITIONS[DS_CT_IN_NO_SERVER] = DS_TERMINATED;
    CTI_CALLING_TRANSITIONS[DS_CT_IN_IO_EXCEPTION] = DS_CALLING;
    CTI_CALLING_TRANSITIONS[DS_CT_IN_OTHER_EXCEPTION] = DS_UNDEFINED;
    CTI_CALLING_TRANSITIONS[DS_CT_IN_REL_PROVISIONAL] = DS_CTI_RELPROCEEDING;
    CTI_CALLING_TRANSITIONS[DS_CT_IN_PRACK] = DS_UNDEFINED;

    /** client non-INVITE transaction transitions from CALLING state. */
    CT_CALLING_TRANSITIONS[DS_CT_IN_START] = DS_UNDEFINED;
    CT_CALLING_TRANSITIONS[DS_CT_IN_T1] = DS_CALLING;
    CT_CALLING_TRANSITIONS[DS_CT_IN_T1_EXPIRED] = DS_TERMINATED;
    CT_CALLING_TRANSITIONS[DS_CT_IN_Tp] = DS_UNDEFINED;
    CT_CALLING_TRANSITIONS[DS_CT_IN_TIMEOUT] = DS_UNDEFINED;
    CT_CALLING_TRANSITIONS[DS_CT_IN_Tn] = DS_TERMINATED;
    CT_CALLING_TRANSITIONS[DS_CT_IN_CANCEL] = DS_CALLING;
    CT_CALLING_TRANSITIONS[DS_CT_IN_CANCEL_TIMER] = DS_TERMINATED;
    CT_CALLING_TRANSITIONS[DS_CT_IN_ACK] = DS_UNDEFINED;
    CT_CALLING_TRANSITIONS[DS_CT_IN_PROVISIONAL] = DS_PROCEEDING;
    CT_CALLING_TRANSITIONS[DS_CT_IN_2XX] = DS_COMPLETED;
    CT_CALLING_TRANSITIONS[DS_CT_IN_3TO6XX] = DS_COMPLETED;
    CT_CALLING_TRANSITIONS[DS_CT_IN_SERVICE_UNAVAILABLE] = DS_CALLING;
    CT_CALLING_TRANSITIONS[DS_CT_IN_NEXT_SERVER] = DS_INITIAL;
    CT_CALLING_TRANSITIONS[DS_CT_IN_NO_SERVER] = DS_TERMINATED;
    CT_CALLING_TRANSITIONS[DS_CT_IN_IO_EXCEPTION] = DS_CALLING;
    CT_CALLING_TRANSITIONS[DS_CT_IN_OTHER_EXCEPTION] = DS_UNDEFINED;

    //////////////////////////////////////////////////////////////////
    /// Client Transaction PROCEEDING state transitions (CT_PROCEEDING)
    //////////////////////////////////////////////////////////////////
    /** client INVITE transaction transitions from PROCEEDING state. */
    CTI_PROCEEDING_TRANSITIONS[DS_CT_IN_START] = DS_UNDEFINED;
    CTI_PROCEEDING_TRANSITIONS[DS_CT_IN_T1] = DS_PROCEEDING;
    CTI_PROCEEDING_TRANSITIONS[DS_CT_IN_T1_EXPIRED] = DS_TERMINATED;
    CTI_PROCEEDING_TRANSITIONS[DS_CT_IN_Tp] = DS_TERMINATED;
    CTI_PROCEEDING_TRANSITIONS[DS_CT_IN_TIMEOUT] = DS_UNDEFINED;
    CTI_PROCEEDING_TRANSITIONS[DS_CT_IN_Tn] = DS_TERMINATED;
    CTI_PROCEEDING_TRANSITIONS[DS_CT_IN_CANCEL] = DS_PROCEEDING;
    CTI_PROCEEDING_TRANSITIONS[DS_CT_IN_CANCEL_TIMER] = DS_TERMINATED;
    CTI_PROCEEDING_TRANSITIONS[DS_CT_IN_ACK] = DS_UNDEFINED;
    CTI_PROCEEDING_TRANSITIONS[DS_CT_IN_PROVISIONAL] = DS_PROCEEDING;
    CTI_PROCEEDING_TRANSITIONS[DS_CT_IN_2XX] = DS_TERMINATED;
    CTI_PROCEEDING_TRANSITIONS[DS_CT_IN_3TO6XX] = DS_COMPLETED;
    CTI_PROCEEDING_TRANSITIONS[DS_CT_IN_SERVICE_UNAVAILABLE] = DS_CALLING;
    CTI_PROCEEDING_TRANSITIONS[DS_CT_IN_NEXT_SERVER] = DS_UNDEFINED;
    CTI_PROCEEDING_TRANSITIONS[DS_CT_IN_NO_SERVER] = DS_UNDEFINED;
    CTI_PROCEEDING_TRANSITIONS[DS_CT_IN_IO_EXCEPTION] = DS_UNDEFINED;
    CTI_PROCEEDING_TRANSITIONS[DS_CT_IN_OTHER_EXCEPTION] = DS_UNDEFINED;
    CTI_PROCEEDING_TRANSITIONS[DS_CT_IN_REL_PROVISIONAL] = DS_CTI_RELPROCEEDING;
    CTI_PROCEEDING_TRANSITIONS[DS_CT_IN_PRACK] = DS_UNDEFINED;

    /** client non-INVITE transaction transitions from PROCEEDING state. */
    CT_PROCEEDING_TRANSITIONS[DS_CT_IN_START] = DS_UNDEFINED;
    CT_PROCEEDING_TRANSITIONS[DS_CT_IN_T1] = DS_PROCEEDING;
    CT_PROCEEDING_TRANSITIONS[DS_CT_IN_T1_EXPIRED] = DS_TERMINATED;
    CT_PROCEEDING_TRANSITIONS[DS_CT_IN_Tp] = DS_UNDEFINED;
    CT_PROCEEDING_TRANSITIONS[DS_CT_IN_TIMEOUT] = DS_UNDEFINED;
    CT_PROCEEDING_TRANSITIONS[DS_CT_IN_Tn] = DS_TERMINATED;
    CT_PROCEEDING_TRANSITIONS[DS_CT_IN_CANCEL] = DS_PROCEEDING;
    CT_PROCEEDING_TRANSITIONS[DS_CT_IN_CANCEL_TIMER] = DS_TERMINATED;
    CT_PROCEEDING_TRANSITIONS[DS_CT_IN_ACK] = DS_UNDEFINED;
    CT_PROCEEDING_TRANSITIONS[DS_CT_IN_PROVISIONAL] = DS_PROCEEDING;
    CT_PROCEEDING_TRANSITIONS[DS_CT_IN_2XX] = DS_COMPLETED;
    CT_PROCEEDING_TRANSITIONS[DS_CT_IN_3TO6XX] = DS_COMPLETED;
    CT_PROCEEDING_TRANSITIONS[DS_CT_IN_SERVICE_UNAVAILABLE] = DS_CALLING;
    CT_PROCEEDING_TRANSITIONS[DS_CT_IN_NEXT_SERVER] = DS_UNDEFINED;
    CT_PROCEEDING_TRANSITIONS[DS_CT_IN_NO_SERVER] = DS_UNDEFINED;
    CT_PROCEEDING_TRANSITIONS[DS_CT_IN_IO_EXCEPTION] = DS_UNDEFINED;
    CT_PROCEEDING_TRANSITIONS[DS_CT_IN_OTHER_EXCEPTION] = DS_UNDEFINED;

    //////////////////////////////////////////////////////////////////
    /// Client Transaction RELPROCEEDING state transitions (CT_RELPROCEEDING)
    //////////////////////////////////////////////////////////////////
    /** client INVITE transaction transitions from RELPROCEEDING state. */
    CTI_RELPROCEEDING_TRANSITIONS[DS_CT_IN_START] = DS_UNDEFINED;
    CTI_RELPROCEEDING_TRANSITIONS[DS_CT_IN_T1] = DS_CTI_RELPROCEEDING;
    CTI_RELPROCEEDING_TRANSITIONS[DS_CT_IN_T1_EXPIRED] = DS_TERMINATED;
    CTI_RELPROCEEDING_TRANSITIONS[DS_CT_IN_Tp] = DS_TERMINATED;
    CTI_RELPROCEEDING_TRANSITIONS[DS_CT_IN_TIMEOUT] = DS_UNDEFINED;
    CTI_RELPROCEEDING_TRANSITIONS[DS_CT_IN_Tn] = DS_TERMINATED;
    CTI_RELPROCEEDING_TRANSITIONS[DS_CT_IN_CANCEL] = DS_CTI_RELPROCEEDING;
    CTI_RELPROCEEDING_TRANSITIONS[DS_CT_IN_CANCEL_TIMER] = DS_TERMINATED;
    CTI_RELPROCEEDING_TRANSITIONS[DS_CT_IN_ACK] = DS_UNDEFINED;
    CTI_RELPROCEEDING_TRANSITIONS[DS_CT_IN_PROVISIONAL] = DS_UNDEFINED;
    CTI_RELPROCEEDING_TRANSITIONS[DS_CT_IN_2XX] = DS_TERMINATED;
    CTI_RELPROCEEDING_TRANSITIONS[DS_CT_IN_3TO6XX] = DS_COMPLETED;
    CTI_RELPROCEEDING_TRANSITIONS[DS_CT_IN_SERVICE_UNAVAILABLE] = DS_CALLING;
    CTI_RELPROCEEDING_TRANSITIONS[DS_CT_IN_NEXT_SERVER] = DS_UNDEFINED;
    CTI_RELPROCEEDING_TRANSITIONS[DS_CT_IN_NO_SERVER] = DS_UNDEFINED;
    CTI_RELPROCEEDING_TRANSITIONS[DS_CT_IN_IO_EXCEPTION] = DS_UNDEFINED;
    CTI_RELPROCEEDING_TRANSITIONS[DS_CT_IN_OTHER_EXCEPTION] = DS_UNDEFINED;
    CTI_RELPROCEEDING_TRANSITIONS[DS_CT_IN_REL_PROVISIONAL] = DS_CTI_RELPROCEEDING;
    CTI_RELPROCEEDING_TRANSITIONS[DS_CT_IN_PRACK] = DS_CTI_RELPROCEEDING;

    //////////////////////////////////////////////////////////////////
    /// Client Transaction COMPLETED state transitions (CT_COMPLETED)
    //////////////////////////////////////////////////////////////////
    /** client INVITE transaction transitions from COMPLETED state. */
    CTI_COMPLETED_TRANSITIONS[DS_CT_IN_START] = DS_UNDEFINED;
    CTI_COMPLETED_TRANSITIONS[DS_CT_IN_T1] = DS_COMPLETED;
    CTI_COMPLETED_TRANSITIONS[DS_CT_IN_T1_EXPIRED] = DS_UNDEFINED;
    CTI_COMPLETED_TRANSITIONS[DS_CT_IN_Tp] = DS_UNDEFINED;
    CTI_COMPLETED_TRANSITIONS[DS_CT_IN_TIMEOUT] = DS_TERMINATED;
    CTI_COMPLETED_TRANSITIONS[DS_CT_IN_Tn] = DS_TERMINATED;
    CTI_COMPLETED_TRANSITIONS[DS_CT_IN_CANCEL] = DS_COMPLETED;
    CTI_COMPLETED_TRANSITIONS[DS_CT_IN_CANCEL_TIMER] = DS_COMPLETED;
    CTI_COMPLETED_TRANSITIONS[DS_CT_IN_ACK] = DS_COMPLETED;
    CTI_COMPLETED_TRANSITIONS[DS_CT_IN_PROVISIONAL] = DS_COMPLETED;
    CTI_COMPLETED_TRANSITIONS[DS_CT_IN_2XX] = DS_TERMINATED;
    CTI_COMPLETED_TRANSITIONS[DS_CT_IN_3TO6XX] = DS_COMPLETED;
    CTI_COMPLETED_TRANSITIONS[DS_CT_IN_SERVICE_UNAVAILABLE] = DS_COMPLETED;
    CTI_COMPLETED_TRANSITIONS[DS_CT_IN_NEXT_SERVER] = DS_UNDEFINED;
    CTI_COMPLETED_TRANSITIONS[DS_CT_IN_NO_SERVER] = DS_UNDEFINED;
    CTI_COMPLETED_TRANSITIONS[DS_CT_IN_IO_EXCEPTION] = DS_TERMINATED;
    CTI_COMPLETED_TRANSITIONS[DS_CT_IN_OTHER_EXCEPTION] = DS_UNDEFINED;
    CTI_COMPLETED_TRANSITIONS[DS_CT_IN_REL_PROVISIONAL] = DS_UNDEFINED;
    CTI_COMPLETED_TRANSITIONS[DS_CT_IN_PRACK] = DS_UNDEFINED;

    /** client non-INVITE transaction transitions from COMPLETED state. */
    CT_COMPLETED_TRANSITIONS[DS_CT_IN_START] = DS_UNDEFINED;
    CT_COMPLETED_TRANSITIONS[DS_CT_IN_T1] = DS_COMPLETED;
    CT_COMPLETED_TRANSITIONS[DS_CT_IN_T1_EXPIRED] = DS_UNDEFINED;
    CT_COMPLETED_TRANSITIONS[DS_CT_IN_Tp] = DS_UNDEFINED;
    CT_COMPLETED_TRANSITIONS[DS_CT_IN_TIMEOUT] = DS_TERMINATED;
    CT_COMPLETED_TRANSITIONS[DS_CT_IN_Tn] = DS_TERMINATED;
    CT_COMPLETED_TRANSITIONS[DS_CT_IN_CANCEL] = DS_UNDEFINED;
    CT_COMPLETED_TRANSITIONS[DS_CT_IN_CANCEL_TIMER] = DS_COMPLETED;
    CT_COMPLETED_TRANSITIONS[DS_CT_IN_ACK] = DS_UNDEFINED;
    CT_COMPLETED_TRANSITIONS[DS_CT_IN_PROVISIONAL] = DS_COMPLETED;
    CT_COMPLETED_TRANSITIONS[DS_CT_IN_2XX] = DS_COMPLETED;
    CT_COMPLETED_TRANSITIONS[DS_CT_IN_3TO6XX] = DS_COMPLETED;
    CT_COMPLETED_TRANSITIONS[DS_CT_IN_SERVICE_UNAVAILABLE] = DS_COMPLETED;
    CT_COMPLETED_TRANSITIONS[DS_CT_IN_NEXT_SERVER] = DS_UNDEFINED;
    CT_COMPLETED_TRANSITIONS[DS_CT_IN_NO_SERVER] = DS_UNDEFINED;
    CT_COMPLETED_TRANSITIONS[DS_CT_IN_IO_EXCEPTION] = DS_UNDEFINED;
    CT_COMPLETED_TRANSITIONS[DS_CT_IN_OTHER_EXCEPTION] = DS_UNDEFINED;

    // INVITE Client Transaction Extra State Transition Tables
    // ////////////////////////////////////////////////////////////////////////////// (Invi/////te)
    //                                       (Input)
    //                       0    1    2      3    4    5    6    7    8    9    10   11    12  13
    // 14   15   16  17  18
    // -----------------------------------------------------------------------------------------------------------------
    // (CTIX_TABLE)
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //   State             | ST   T1   T1_EXP Tp   TO   Tn   CNL  CNLT ACK  PR   2XX  3TO6  SUR NXS
    // NOS  IOE  OE RELP PRAC
    // -----------------------------------------------------------------------------------------------------------------
    // 0 XINI  XINITIAL    | XCOM ---  ---    ---  ---  ---  ---  ---  ---  ---  ---  ---   --- ---
    // ---  ---  --- --- ---
    // 1 XCOM  XCOMPLETE   | ---  XCOM ---    XCOM XTER XTER XCOM XCOM XCOM XCOM XCOM ---   --- ---
    // ---  XTER --- --- ---
    // 2 XCON  XCONFIRMED  | ---  ---  ---    ---  ---  ---  ---  ---  ---  ---  ---  ---   --- ---
    // ---  ---  --- --- ---
    // 3 XTER  XTERMINATED | ---  ---  ---    ---  ---  ---  ---  ---  ---  ---  ---  ---   --- ---
    // ---  ---  --- --- ---
    // //////////////////////////////////////////////////////////////////////////////

    // Client INVITE extra transaction for 2xx xref
    //////////////////////////////////////////////////////////
    //
    //  DS_XINITIAL (INVITE)
    //

    //
    // ///////////////////////////////////////////////////////
    //  DS_XCOMPLETED (INVITE)
    //

    //
    // ///////////////////////////////////////////////////////
    //  DS_XCONFIRMED (INVITE)
    //

    //
    // ///////////////////////////////////////////////////////
    //  DS_XTERMINATED (INVITE)
    //

    //
    //

    //////////////////////////////////////////////////////////////////
    /// Client Transaction extra INITIAL state transitions (CTIX_XINITIAL)
    //////////////////////////////////////////////////////////////////
    /** client INVITE transaction transitions from COMPLETED state. */
    CTIX_XINITIAL_TRANSITIONS[DS_CT_IN_START] = DS_XCOMPLETED;
    CTIX_XINITIAL_TRANSITIONS[DS_CT_IN_T1] = DS_UNDEFINED;
    CTIX_XINITIAL_TRANSITIONS[DS_CT_IN_T1_EXPIRED] = DS_UNDEFINED;
    CTIX_XINITIAL_TRANSITIONS[DS_CT_IN_Tp] = DS_UNDEFINED;
    CTIX_XINITIAL_TRANSITIONS[DS_CT_IN_TIMEOUT] = DS_UNDEFINED;
    CTIX_XINITIAL_TRANSITIONS[DS_CT_IN_Tn] = DS_UNDEFINED;
    CTIX_XINITIAL_TRANSITIONS[DS_CT_IN_CANCEL] = DS_UNDEFINED;
    CTIX_XINITIAL_TRANSITIONS[DS_CT_IN_CANCEL_TIMER] = DS_UNDEFINED;
    CTIX_XINITIAL_TRANSITIONS[DS_CT_IN_ACK] = DS_UNDEFINED;
    CTIX_XINITIAL_TRANSITIONS[DS_CT_IN_PROVISIONAL] = DS_UNDEFINED;
    CTIX_XINITIAL_TRANSITIONS[DS_CT_IN_2XX] = DS_UNDEFINED;
    CTIX_XINITIAL_TRANSITIONS[DS_CT_IN_3TO6XX] = DS_UNDEFINED;
    CTIX_XINITIAL_TRANSITIONS[DS_CT_IN_SERVICE_UNAVAILABLE] = DS_UNDEFINED;
    CTIX_XINITIAL_TRANSITIONS[DS_CT_IN_NEXT_SERVER] = DS_UNDEFINED;
    CTIX_XINITIAL_TRANSITIONS[DS_CT_IN_NO_SERVER] = DS_UNDEFINED;
    CTIX_XINITIAL_TRANSITIONS[DS_CT_IN_IO_EXCEPTION] = DS_UNDEFINED;
    CTIX_XINITIAL_TRANSITIONS[DS_CT_IN_OTHER_EXCEPTION] = DS_UNDEFINED;
    CTIX_XINITIAL_TRANSITIONS[DS_CT_IN_REL_PROVISIONAL] = DS_UNDEFINED;
    CTIX_XINITIAL_TRANSITIONS[DS_CT_IN_PRACK] = DS_UNDEFINED;

    //////////////////////////////////////////////////////////////////
    /// Client Transaction extra COMPLETED state transitions (CTIX_XCOMPLETED)
    //////////////////////////////////////////////////////////////////
    /** client INVITE transaction transitions from COMPLETED state. */
    CTIX_XCOMPLETED_TRANSITIONS[DS_CT_IN_START] = DS_UNDEFINED;
    CTIX_XCOMPLETED_TRANSITIONS[DS_CT_IN_T1] = DS_XCOMPLETED;
    CTIX_XCOMPLETED_TRANSITIONS[DS_CT_IN_T1_EXPIRED] = DS_UNDEFINED;
    CTIX_XCOMPLETED_TRANSITIONS[DS_CT_IN_Tp] = DS_XCOMPLETED;
    CTIX_XCOMPLETED_TRANSITIONS[DS_CT_IN_TIMEOUT] = DS_XTERMINATED;
    CTIX_XCOMPLETED_TRANSITIONS[DS_CT_IN_Tn] = DS_XTERMINATED;
    CTIX_XCOMPLETED_TRANSITIONS[DS_CT_IN_CANCEL] = DS_XCOMPLETED;
    CTIX_XCOMPLETED_TRANSITIONS[DS_CT_IN_CANCEL_TIMER] = DS_XCOMPLETED;
    CTIX_XCOMPLETED_TRANSITIONS[DS_CT_IN_ACK] = DS_XCOMPLETED;
    CTIX_XCOMPLETED_TRANSITIONS[DS_CT_IN_PROVISIONAL] = DS_XCOMPLETED;
    CTIX_XCOMPLETED_TRANSITIONS[DS_CT_IN_2XX] = DS_XCOMPLETED;
    CTIX_XCOMPLETED_TRANSITIONS[DS_CT_IN_3TO6XX] = DS_UNDEFINED;
    CTIX_XCOMPLETED_TRANSITIONS[DS_CT_IN_SERVICE_UNAVAILABLE] = DS_UNDEFINED;
    CTIX_XCOMPLETED_TRANSITIONS[DS_CT_IN_NEXT_SERVER] = DS_UNDEFINED;
    CTIX_XCOMPLETED_TRANSITIONS[DS_CT_IN_NO_SERVER] = DS_UNDEFINED;
    CTIX_XCOMPLETED_TRANSITIONS[DS_CT_IN_IO_EXCEPTION] = DS_XTERMINATED;
    CTIX_XCOMPLETED_TRANSITIONS[DS_CT_IN_OTHER_EXCEPTION] = DS_UNDEFINED;
    CTIX_XCOMPLETED_TRANSITIONS[DS_CT_IN_REL_PROVISIONAL] = DS_UNDEFINED;
    CTIX_XCOMPLETED_TRANSITIONS[DS_CT_IN_PRACK] = DS_UNDEFINED;

    // Server Transaction State Transition Tables
    // ////////////////////////////////////////////////////////////////////////////// (Invite)
    //                                           (Input)
    //                     0    1   2   3   4   5    6   7   8    9     10  11  12  13  14  15  16
    // 17
    // -----------------------------------------------------------------------------------------------
    //  (STI_TABLE)
    // /////////////////////////////////////////////////////////////////////////////////////////////////
    //   State           | ST  REQ PRO TPR 2XX 3TO6 CNL TO  T1  T1_EXP Tn  ACK IOE OE  NXC NOC RELP
    // PRAC
    // -------------------------------------------------------------------------------------------------
    // 0 INI  INITIAL    | CAL --- PRO PRO TER COM  CAL --- INI ---    TER --- --- --- --- --- WAI
    // ---
    // 1 CAL  CALLING    | --- CAL PRO PRO TER COM  CAL --- CAL TER    TER --- --- --- --- --- WAI
    // ---
    // 2 PROC PROCEEDING | --- PRO PRO PRO TER COM  PRO --- PRO TER    TER --- PRO --- PRO TER WAI
    // ---
    // 3 COMP COMPLETED  | --- COM --- --- --- COM  COM --- COM TER    TER CON COM --- COM TER ---
    // ---
    // 4 CONF CONFIRMED  | --- CON --- --- --- ---  CON TER CON ---    TER CON CON --- --- --- ---
    // ---
    // 5 TERM TERMINATED | --- --- --- --- --- ---  --- --- TER TER    --- --- --- --- --- --- ---
    // ---
    // 6 WAIT WAIT_PRACK | --- --- --- --- TER COM  WAI --- WAI TER    TER --- WAI --- --- --- ---
    // RPR
    // 7 RPRO RELPROCEED | --- --- --- --- TER COM  RPR --- RPR TER    TER --- RPR --- RPR TER WAI
    // RPR
    // ////////////////////////////////////////////////////////////////////////// (Non-Invite)
    //  (ST_TABLE)
    // ///////////////////////////////////////////////////////////////////////////////////////
    //   State           | ST  REQ PRO TPR 2XX 3TO6 CNL TO  T1  T1_EXP Tn  --- IOE OE  NXC NOC
    // ---------------------------------------------------------------------------------------
    // 0 INI  INITIAL    | CAL --- PRO PRO COM COM  CAL --- --- ---    TER --- --- --- --- ---
    // 1 CAL  CALLING    | --- CAL PRO PRO COM COM  CAL --- --- ---    TER --- --- --- --- ---
    // 2 PROC PROCEEDING | --- PRO PRO PRO COM COM  PRO --- --- ---    TER --- PRO --- PRO TER
    // 3 COMP COMPLETED  | --- COM --- --- COM COM  COM TER --- ---    TER --- COM --- COM TER
    // 4 CONF CONFIRMED  | --- --- --- --- --- ---  --- --- --- ---    --- --- --- --- --- ---
    // 5 TERM TERMINATED | --- --- --- --- --- ---  --- --- --- ---    --- --- --- --- --- ---
    // ///////////////////////////////////////////////////////////////////////////////////////

    // Server transaction inputs
    ////////////////////////////////////
    // ST     ==> the first input
    // REQ    ==> request was received
    // PRO    ==> user code sends provisional
    // TPR    ==> delayed provisional timer
    // FIN    ==> user code sends final
    // CNL    ==> received cancel
    // TO     ==> timeout timer to control duration in CONF(STI) or COMP(ST)state
    // T1     ==> response rtx timer
    // T1_EXP ==> response rtx timer expired
    // Tn     ==> max transaction time expired
    // ACK    ==> ACK was received
    // IOE    ==> IO exception
    // OE     ==> other exception
    // RELP   ==> user code sends reliable provisional response <== New input for PRACK
    // PRAC   ==> PRACK request was received
    //////////////////////////////////////////////////////
    // Timer mapping
    // ---------------------------------------------------------------------
    //                     | Timer name in SIP draft and default value
    //                     |------------------------------------------------
    // Timer event in code |  STI                         ST
    // --------------------|------------------------------------------------
    // T1                  | TG(T1=0.5s or MAX_INT)
    // T1_EXP              | TH(64*T1)
    // To                  | TI(T4=5s or 0s)             TJ(T3=16s or 0s)
    // Tn                  | user supplied(not in draft) user supplied(not in draft)
    // ---------------------------------------------------------------------
    // * If there are two values, 0s is for reliable transport and the other value is for unreliable
    // ones

    // Server INVITE transaction xref
    ////////////////////////////////////
    //
    //  DS_INITIAL (INVITE)
    //
    //  DS_INITIAL  | DS_ST_IN_T1

    // ///////////////////////////////////////////////////////
    //  DS_CALLING (INVITE)
    //
    //  DS_INITIAL  | DS_ST_IN_START
    //  DS_INITIAL  | DS_ST_IN_CANCEL
    //  DS_CALLING  | DS_ST_IN_REQUEST
    //  DS_CALLING  | DS_ST_IN_CANCEL
    //  DS_CALLING  | DS_ST_IN_T1
    //  DS_CALLING  | DS_ST_IN_ACK  // See comments (by DS) in void calling().

    //
    // ///////////////////////////////////////////////////////
    //  DS_PROCEEDING (INVITE)
    //
    //  DS_INITIAL  | DS_ST_IN_PROVISIONAL
    //  DS_INITIAL  | DS_ST_IN_TPROVISIONAL
    //  DS_CALLING  | DS_ST_IN_PROVISIONAL
    //  DS_CALLING  | DS_ST_IN_TPROVISIONAL
    //  DS_PROCEEDING  | DS_ST_IN_REQUEST
    //  DS_PROCEEDING  | DS_ST_IN_PROVISIONAL
    //  DS_PROCEEDING  | DS_ST_IN_TPROVISIONAL
    //  DS_PROCEEDING  | DS_ST_IN_CANCEL
    //  DS_PROCEEDING  | DS_ST_IN_T1
    //  DS_PROCEEDING  | DS_ST_IN_ACK  // See comments (by DS) in proceeding()
    //  DS_PROCEEDING  | DS_ST_IN_IO_EXCEPTION
    //  DS_PROCEEDING  | DS_ST_IN_NEXT_CLIENT

    //
    // ///////////////////////////////////////////////////////
    //  DS_COMPLETED (INVITE)
    //
    //  DS_INITIAL  | DS_ST_IN_3TO6XX
    //  DS_CALLING  | DS_ST_IN_3TO6XX
    //  DS_PROCEEDING  | DS_ST_IN_3TO6XX
    //  DS_WAIT_PRACK  | DS_ST_IN_3TO6XX
    //  DS_STI_RELPROCEEDING  | DS_ST_IN_3TO6XX
    //  DS_COMPLETED  | DS_ST_IN_REQUEST
    //  DS_COMPLETED  | DS_ST_IN_3TO6XX
    //  DS_COMPLETED  | DS_ST_IN_CANCEL
    //  DS_COMPLETED  | DS_ST_IN_T1
    //  DS_COMPLETED  | DS_ST_IN_IO_EXCEPTION
    //  DS_COMPLETED  | DS_ST_IN_NEXT_CLIENT

    //
    // ///////////////////////////////////////////////////////
    //  DS_CONFIRMED (INVITE)
    //
    //  DS_COMPLETED  | DS_ST_IN_ACK
    //  DS_CONFIRMED  | DS_ST_IN_REQUEST
    //  DS_CONFIRMED  | DS_ST_IN_CANCEL
    //  DS_CONFIRMED  | DS_ST_IN_T1
    //  DS_CONFIRMED  | DS_ST_IN_ACK
    //  DS_CONFIRMED  | DS_ST_IN_IO_EXCEPTION

    //
    // ///////////////////////////////////////////////////////
    //  DS_TERMINATED (INVITE)
    //
    //  DS_INITIAL  | DS_ST_IN_2XX
    //  DS_INITIAL  | DS_ST_IN_Tn
    //  DS_CALLING  | DS_ST_IN_2XX
    //  DS_CALLING  | DS_ST_IN_T1_EXPIRED
    //  DS_CALLING  | DS_ST_IN_Tn
    //  DS_PROCEEDING  | DS_ST_IN_2XX
    //  DS_PROCEEDING  | DS_ST_IN_T1_EXPIRED
    //  DS_PROCEEDING  | DS_ST_IN_Tn
    //  DS_PROCEEDING  | DS_ST_IN_NO_CLIENT
    //  DS_WAIT_PRACK  | DS_ST_IN_2XX
    //  DS_WAIT_PRACK  | DS_ST_IN_T1_EXPIRED
    //  DS_WAIT_PRACK  | DS_ST_IN_Tn
    //  DS_STI_RELPROCEEDING  | DS_ST_IN_T1_EXPIRED
    //  DS_STI_RELPROCEEDING  | DS_ST_IN_Tn
    //  DS_STI_RELPROCEEDING  | DS_ST_IN_NO_CLIENT
    //  DS_STI_RELPROCEEDING  | DS_ST_IN_2XX
    //  DS_COMPLETED  | DS_ST_IN_T1_EXPIRED
    //  DS_COMPLETED  | DS_ST_IN_Tn
    //  DS_COMPLETED  | DS_ST_IN_NO_CLIENT
    //  DS_CONFIRMED  | DS_ST_IN_TIMEOUT
    //  DS_CONFIRMED  | DS_ST_IN_Tn
    //  DS_TERMINATED  | DS_ST_IN_T1
    //  DS_TERMINATED  | DS_ST_IN_T1_EXPIRED

    //
    // ///////////////////////////////////////////////////////
    //  DS_WAIT_PRACK (INVITE)
    //
    //  DS_INITIAL  | DS_ST_IN_REL_PROVISIONAL
    //  DS_CALLING  | DS_ST_IN_REL_PROVISIONAL
    //  DS_PROCEEDING  | DS_ST_IN_REL_PROVISIONAL
    //  DS_WAIT_PRACK  | DS_ST_IN_CANCEL
    //  DS_WAIT_PRACK  | DS_ST_IN_T1
    //  DS_WAIT_PRACK  | DS_ST_IN_IO_EXCEPTION
    // Don't allow sending new 1xx while in WAIT_PRACK state!
    // //  DS_WAIT_PRACK  | DS_ST_IN_REL_PROVISIONAL
    //  DS_STI_RELPROCEEDING  | DS_ST_IN_REL_PROVISIONAL

    //
    // ///////////////////////////////////////////////////////
    //  DS_STI_RELPROCEEDING (INVITE)
    //
    //  DS_WAIT_PRACK  | DS_ST_IN_PRACK
    //  DS_STI_RELPROCEEDING  | DS_ST_IN_PRACK
    //  DS_STI_RELPROCEEDING  | DS_ST_IN_CANCEL
    //  DS_STI_RELPROCEEDING  | DS_ST_IN_T1
    //  DS_STI_RELPROCEEDING  | DS_ST_IN_IO_EXCEPTION
    //  DS_STI_RELPROCEEDING  | DS_ST_IN_NEXT_CLIENT

    //
    //

    // Server non INVITE transaction xref
    ////////////////////////////////////
    //
    //  DS_INITIAL (non-INVITE)
    //

    // ///////////////////////////////////////////////////////
    //  DS_CALLING (non-INVITE)
    //
    //  DS_INITIAL  | DS_ST_IN_START
    //  DS_INITIAL  | DS_ST_IN_CANCEL
    //  DS_CALLING  | DS_ST_IN_REQUEST
    //  DS_CALLING  | DS_ST_IN_CANCEL

    //
    // ///////////////////////////////////////////////////////
    //  DS_PROCEEDING (non-INVITE)
    //
    //  DS_INITIAL  | DS_ST_IN_PROVISIONAL
    //  DS_INITIAL  | DS_ST_IN_TPROVISIONAL
    //  DS_CALLING  | DS_ST_IN_PROVISIONAL
    //  DS_CALLING  | DS_ST_IN_TPROVISIONAL
    //  DS_PROCEEDING  | DS_ST_IN_REQUEST
    //  DS_PROCEEDING  | DS_ST_IN_PROVISIONAL
    //  DS_PROCEEDING  | DS_ST_IN_TPROVISIONAL
    //  DS_PROCEEDING  | DS_ST_IN_CANCEL
    //  DS_PROCEEDING  | DS_ST_IN_IO_EXCEPTION
    //  DS_PROCEEDING  | DS_ST_IN_NEXT_CLIENT

    //
    // ///////////////////////////////////////////////////////
    //  DS_COMPLETED (non-INVITE)
    //
    //  DS_INITIAL  | DS_ST_IN_2XX
    //  DS_INITIAL  | DS_ST_IN_3TO6XX
    //  DS_CALLING  | DS_ST_IN_2XX
    //  DS_CALLING  | DS_ST_IN_3TO6XX
    //  DS_PROCEEDING  | DS_ST_IN_2XX
    //  DS_PROCEEDING  | DS_ST_IN_3TO6XX
    //  DS_COMPLETED  | DS_ST_IN_REQUEST
    //  DS_COMPLETED  | DS_ST_IN_2XX
    //  DS_COMPLETED  | DS_ST_IN_3TO6XX
    //  DS_COMPLETED  | DS_ST_IN_CANCEL
    //  DS_COMPLETED  | DS_ST_IN_IO_EXCEPTION
    //  DS_COMPLETED  | DS_ST_IN_NEXT_CLIENT

    //
    // ///////////////////////////////////////////////////////
    //  DS_CONFIRMED (non-INVITE)
    //

    //
    // ///////////////////////////////////////////////////////
    //  DS_TERMINATED (non-INVITE)
    //
    //  DS_INITIAL  | DS_ST_IN_Tn
    //  DS_CALLING  | DS_ST_IN_Tn
    //  DS_PROCEEDING  | DS_ST_IN_Tn
    //  DS_PROCEEDING  | DS_ST_IN_NO_CLIENT
    //  DS_COMPLETED  | DS_ST_IN_TIMEOUT
    //  DS_COMPLETED  | DS_ST_IN_Tn
    //  DS_COMPLETED  | DS_ST_IN_NO_CLIENT

    //
    //

    //////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////
    /////////    Server State Machine Transitions
    //////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////
    /// Server Transaction INITIAL state transitions (ST_INITIAL)
    //////////////////////////////////////////////////////////////////
    /** server INVITE transaction transitions from INITIAL state. */
    STI_INITIAL_TRANSITIONS[DS_ST_IN_START] = DS_CALLING;
    STI_INITIAL_TRANSITIONS[DS_ST_IN_REQUEST] = DS_UNDEFINED;
    STI_INITIAL_TRANSITIONS[DS_ST_IN_PROVISIONAL] = DS_PROCEEDING;
    STI_INITIAL_TRANSITIONS[DS_ST_IN_TPROVISIONAL] = DS_PROCEEDING;
    STI_INITIAL_TRANSITIONS[DS_ST_IN_2XX] = DS_TERMINATED;
    STI_INITIAL_TRANSITIONS[DS_ST_IN_3TO6XX] = DS_COMPLETED;
    STI_INITIAL_TRANSITIONS[DS_ST_IN_CANCEL] = DS_CALLING;
    STI_INITIAL_TRANSITIONS[DS_ST_IN_TIMEOUT] = DS_UNDEFINED;
    STI_INITIAL_TRANSITIONS[DS_ST_IN_T1] = DS_INITIAL;
    STI_INITIAL_TRANSITIONS[DS_ST_IN_T1_EXPIRED] = DS_UNDEFINED;
    STI_INITIAL_TRANSITIONS[DS_ST_IN_Tn] = DS_TERMINATED;
    STI_INITIAL_TRANSITIONS[DS_ST_IN_ACK] = DS_UNDEFINED;
    STI_INITIAL_TRANSITIONS[DS_ST_IN_IO_EXCEPTION] = DS_UNDEFINED;
    STI_INITIAL_TRANSITIONS[DS_ST_IN_OTHER_EXCEPTION] = DS_UNDEFINED;
    STI_INITIAL_TRANSITIONS[DS_ST_IN_NEXT_CLIENT] = DS_UNDEFINED;
    STI_INITIAL_TRANSITIONS[DS_ST_IN_NO_CLIENT] = DS_UNDEFINED;
    STI_INITIAL_TRANSITIONS[DS_ST_IN_REL_PROVISIONAL] = DS_WAIT_PRACK;
    STI_INITIAL_TRANSITIONS[DS_ST_IN_PRACK] = DS_UNDEFINED;

    /** server non-INVITE transaction transitions from INITIAL state. */
    ST_INITIAL_TRANSITIONS[DS_ST_IN_START] = DS_CALLING;
    ST_INITIAL_TRANSITIONS[DS_ST_IN_REQUEST] = DS_UNDEFINED;
    ST_INITIAL_TRANSITIONS[DS_ST_IN_PROVISIONAL] = DS_PROCEEDING;
    ST_INITIAL_TRANSITIONS[DS_ST_IN_TPROVISIONAL] = DS_PROCEEDING;
    ST_INITIAL_TRANSITIONS[DS_ST_IN_2XX] = DS_COMPLETED;
    ST_INITIAL_TRANSITIONS[DS_ST_IN_3TO6XX] = DS_COMPLETED;
    ST_INITIAL_TRANSITIONS[DS_ST_IN_CANCEL] = DS_CALLING;
    ST_INITIAL_TRANSITIONS[DS_ST_IN_TIMEOUT] = DS_UNDEFINED;
    ST_INITIAL_TRANSITIONS[DS_ST_IN_T1] = DS_UNDEFINED;
    ST_INITIAL_TRANSITIONS[DS_ST_IN_T1_EXPIRED] = DS_UNDEFINED;
    ST_INITIAL_TRANSITIONS[DS_ST_IN_Tn] = DS_TERMINATED;
    ST_INITIAL_TRANSITIONS[DS_ST_IN_ACK] = DS_UNDEFINED;
    ST_INITIAL_TRANSITIONS[DS_ST_IN_IO_EXCEPTION] = DS_UNDEFINED;
    ST_INITIAL_TRANSITIONS[DS_ST_IN_OTHER_EXCEPTION] = DS_UNDEFINED;
    ST_INITIAL_TRANSITIONS[DS_ST_IN_NEXT_CLIENT] = DS_UNDEFINED;
    ST_INITIAL_TRANSITIONS[DS_ST_IN_NO_CLIENT] = DS_UNDEFINED;

    //////////////////////////////////////////////////////////////////
    /// Server Transaction CALLING state transitions (ST_CALLING)
    //////////////////////////////////////////////////////////////////
    /** server INVITE transaction transitions from CALLING state. */
    STI_CALLING_TRANSITIONS[DS_ST_IN_START] = DS_UNDEFINED;
    STI_CALLING_TRANSITIONS[DS_ST_IN_REQUEST] = DS_CALLING;
    STI_CALLING_TRANSITIONS[DS_ST_IN_PROVISIONAL] = DS_PROCEEDING;
    STI_CALLING_TRANSITIONS[DS_ST_IN_TPROVISIONAL] = DS_PROCEEDING;
    STI_CALLING_TRANSITIONS[DS_ST_IN_2XX] = DS_TERMINATED;
    STI_CALLING_TRANSITIONS[DS_ST_IN_3TO6XX] = DS_COMPLETED;
    STI_CALLING_TRANSITIONS[DS_ST_IN_CANCEL] = DS_CALLING;
    STI_CALLING_TRANSITIONS[DS_ST_IN_TIMEOUT] = DS_UNDEFINED;
    STI_CALLING_TRANSITIONS[DS_ST_IN_T1] = DS_CALLING;
    STI_CALLING_TRANSITIONS[DS_ST_IN_T1_EXPIRED] = DS_TERMINATED;
    STI_CALLING_TRANSITIONS[DS_ST_IN_Tn] = DS_TERMINATED;
    STI_CALLING_TRANSITIONS[DS_ST_IN_ACK] = DS_UNDEFINED;
    STI_CALLING_TRANSITIONS[DS_ST_IN_IO_EXCEPTION] = DS_UNDEFINED;
    STI_CALLING_TRANSITIONS[DS_ST_IN_OTHER_EXCEPTION] = DS_UNDEFINED;
    STI_CALLING_TRANSITIONS[DS_ST_IN_NEXT_CLIENT] = DS_UNDEFINED;
    STI_CALLING_TRANSITIONS[DS_ST_IN_NO_CLIENT] = DS_UNDEFINED;
    STI_CALLING_TRANSITIONS[DS_ST_IN_REL_PROVISIONAL] = DS_WAIT_PRACK;
    STI_CALLING_TRANSITIONS[DS_ST_IN_PRACK] = DS_UNDEFINED;

    /** server non-INVITE transaction transitions from CALLING state. */
    ST_CALLING_TRANSITIONS[DS_ST_IN_START] = DS_UNDEFINED;
    ST_CALLING_TRANSITIONS[DS_ST_IN_REQUEST] = DS_CALLING;
    ST_CALLING_TRANSITIONS[DS_ST_IN_PROVISIONAL] = DS_PROCEEDING;
    ST_CALLING_TRANSITIONS[DS_ST_IN_TPROVISIONAL] = DS_PROCEEDING;
    ST_CALLING_TRANSITIONS[DS_ST_IN_2XX] = DS_COMPLETED;
    ST_CALLING_TRANSITIONS[DS_ST_IN_3TO6XX] = DS_COMPLETED;
    ST_CALLING_TRANSITIONS[DS_ST_IN_CANCEL] = DS_CALLING;
    ST_CALLING_TRANSITIONS[DS_ST_IN_TIMEOUT] = DS_UNDEFINED;
    ST_CALLING_TRANSITIONS[DS_ST_IN_T1] = DS_UNDEFINED;
    ST_CALLING_TRANSITIONS[DS_ST_IN_T1_EXPIRED] = DS_UNDEFINED;
    ST_CALLING_TRANSITIONS[DS_ST_IN_Tn] = DS_TERMINATED;
    ST_CALLING_TRANSITIONS[DS_ST_IN_ACK] = DS_UNDEFINED;
    ST_CALLING_TRANSITIONS[DS_ST_IN_IO_EXCEPTION] = DS_UNDEFINED;
    ST_CALLING_TRANSITIONS[DS_ST_IN_OTHER_EXCEPTION] = DS_UNDEFINED;
    ST_CALLING_TRANSITIONS[DS_ST_IN_NEXT_CLIENT] = DS_UNDEFINED;
    ST_CALLING_TRANSITIONS[DS_ST_IN_NO_CLIENT] = DS_UNDEFINED;

    //////////////////////////////////////////////////////////////////
    /// Server Transaction PROCEEDING state transitions (ST_PROCEEDING)
    //////////////////////////////////////////////////////////////////

    /** server INVITE transaction transitions from PROCEEDING state. */
    STI_PROCEEDING_TRANSITIONS[DS_ST_IN_START] = DS_UNDEFINED;
    STI_PROCEEDING_TRANSITIONS[DS_ST_IN_REQUEST] = DS_PROCEEDING;
    STI_PROCEEDING_TRANSITIONS[DS_ST_IN_PROVISIONAL] = DS_PROCEEDING;
    STI_PROCEEDING_TRANSITIONS[DS_ST_IN_TPROVISIONAL] = DS_PROCEEDING;
    STI_PROCEEDING_TRANSITIONS[DS_ST_IN_2XX] = DS_TERMINATED;
    STI_PROCEEDING_TRANSITIONS[DS_ST_IN_3TO6XX] = DS_COMPLETED;
    STI_PROCEEDING_TRANSITIONS[DS_ST_IN_CANCEL] = DS_PROCEEDING;
    STI_PROCEEDING_TRANSITIONS[DS_ST_IN_TIMEOUT] = DS_UNDEFINED;
    STI_PROCEEDING_TRANSITIONS[DS_ST_IN_T1] = DS_PROCEEDING;
    STI_PROCEEDING_TRANSITIONS[DS_ST_IN_T1_EXPIRED] = DS_TERMINATED;
    STI_PROCEEDING_TRANSITIONS[DS_ST_IN_Tn] = DS_TERMINATED;
    STI_PROCEEDING_TRANSITIONS[DS_ST_IN_ACK] = DS_UNDEFINED;
    STI_PROCEEDING_TRANSITIONS[DS_ST_IN_IO_EXCEPTION] = DS_PROCEEDING;
    STI_PROCEEDING_TRANSITIONS[DS_ST_IN_OTHER_EXCEPTION] = DS_UNDEFINED;
    STI_PROCEEDING_TRANSITIONS[DS_ST_IN_NEXT_CLIENT] = DS_PROCEEDING;
    STI_PROCEEDING_TRANSITIONS[DS_ST_IN_NO_CLIENT] = DS_TERMINATED;
    STI_PROCEEDING_TRANSITIONS[DS_ST_IN_REL_PROVISIONAL] = DS_WAIT_PRACK;
    STI_PROCEEDING_TRANSITIONS[DS_ST_IN_PRACK] = DS_UNDEFINED;

    /** server non-INVITE transaction transitions from PROCEEDING state. */
    ST_PROCEEDING_TRANSITIONS[DS_ST_IN_START] = DS_UNDEFINED;
    ST_PROCEEDING_TRANSITIONS[DS_ST_IN_REQUEST] = DS_PROCEEDING;
    ST_PROCEEDING_TRANSITIONS[DS_ST_IN_PROVISIONAL] = DS_PROCEEDING;
    ST_PROCEEDING_TRANSITIONS[DS_ST_IN_TPROVISIONAL] = DS_PROCEEDING;
    ST_PROCEEDING_TRANSITIONS[DS_ST_IN_2XX] = DS_COMPLETED;
    ST_PROCEEDING_TRANSITIONS[DS_ST_IN_3TO6XX] = DS_COMPLETED;
    ST_PROCEEDING_TRANSITIONS[DS_ST_IN_CANCEL] = DS_PROCEEDING;
    ST_PROCEEDING_TRANSITIONS[DS_ST_IN_TIMEOUT] = DS_UNDEFINED;
    ST_PROCEEDING_TRANSITIONS[DS_ST_IN_T1] = DS_UNDEFINED;
    ST_PROCEEDING_TRANSITIONS[DS_ST_IN_T1_EXPIRED] = DS_UNDEFINED;
    ST_PROCEEDING_TRANSITIONS[DS_ST_IN_Tn] = DS_TERMINATED;
    ST_PROCEEDING_TRANSITIONS[DS_ST_IN_ACK] = DS_UNDEFINED;
    ST_PROCEEDING_TRANSITIONS[DS_ST_IN_IO_EXCEPTION] = DS_PROCEEDING;
    ST_PROCEEDING_TRANSITIONS[DS_ST_IN_OTHER_EXCEPTION] = DS_UNDEFINED;
    ST_PROCEEDING_TRANSITIONS[DS_ST_IN_NEXT_CLIENT] = DS_PROCEEDING;
    ST_PROCEEDING_TRANSITIONS[DS_ST_IN_NO_CLIENT] = DS_TERMINATED;

    //////////////////////////////////////////////////////////////////
    /// Server Transaction COMPLETED state transitions (ST_COMPLETED)
    //////////////////////////////////////////////////////////////////
    /** server INVITE transaction transitions from COMPLETED state. */
    STI_COMPLETED_TRANSITIONS[DS_ST_IN_START] = DS_UNDEFINED;
    STI_COMPLETED_TRANSITIONS[DS_ST_IN_REQUEST] = DS_COMPLETED;
    STI_COMPLETED_TRANSITIONS[DS_ST_IN_PROVISIONAL] = DS_UNDEFINED;
    STI_COMPLETED_TRANSITIONS[DS_ST_IN_TPROVISIONAL] = DS_UNDEFINED;
    STI_COMPLETED_TRANSITIONS[DS_ST_IN_2XX] = DS_UNDEFINED;
    STI_COMPLETED_TRANSITIONS[DS_ST_IN_3TO6XX] = DS_COMPLETED;
    STI_COMPLETED_TRANSITIONS[DS_ST_IN_CANCEL] = DS_COMPLETED;
    STI_COMPLETED_TRANSITIONS[DS_ST_IN_TIMEOUT] = DS_UNDEFINED;
    STI_COMPLETED_TRANSITIONS[DS_ST_IN_T1] = DS_COMPLETED;
    STI_COMPLETED_TRANSITIONS[DS_ST_IN_T1_EXPIRED] = DS_TERMINATED;
    STI_COMPLETED_TRANSITIONS[DS_ST_IN_Tn] = DS_TERMINATED;
    STI_COMPLETED_TRANSITIONS[DS_ST_IN_ACK] = DS_CONFIRMED;
    STI_COMPLETED_TRANSITIONS[DS_ST_IN_IO_EXCEPTION] = DS_COMPLETED;
    STI_COMPLETED_TRANSITIONS[DS_ST_IN_OTHER_EXCEPTION] = DS_UNDEFINED;
    STI_COMPLETED_TRANSITIONS[DS_ST_IN_NEXT_CLIENT] = DS_COMPLETED;
    STI_COMPLETED_TRANSITIONS[DS_ST_IN_NO_CLIENT] = DS_TERMINATED;
    STI_COMPLETED_TRANSITIONS[DS_ST_IN_REL_PROVISIONAL] = DS_UNDEFINED;
    STI_COMPLETED_TRANSITIONS[DS_ST_IN_PRACK] = DS_UNDEFINED;

    /** server non-INVITE transaction transitions from COMPLETED state. */
    ST_COMPLETED_TRANSITIONS[DS_ST_IN_START] = DS_UNDEFINED;
    ST_COMPLETED_TRANSITIONS[DS_ST_IN_REQUEST] = DS_COMPLETED;
    ST_COMPLETED_TRANSITIONS[DS_ST_IN_PROVISIONAL] = DS_UNDEFINED;
    ST_COMPLETED_TRANSITIONS[DS_ST_IN_TPROVISIONAL] = DS_UNDEFINED;
    ST_COMPLETED_TRANSITIONS[DS_ST_IN_2XX] = DS_COMPLETED;
    ST_COMPLETED_TRANSITIONS[DS_ST_IN_3TO6XX] = DS_COMPLETED;
    ST_COMPLETED_TRANSITIONS[DS_ST_IN_CANCEL] = DS_COMPLETED;
    ST_COMPLETED_TRANSITIONS[DS_ST_IN_TIMEOUT] = DS_TERMINATED;
    ST_COMPLETED_TRANSITIONS[DS_ST_IN_T1] = DS_UNDEFINED;
    ST_COMPLETED_TRANSITIONS[DS_ST_IN_T1_EXPIRED] = DS_UNDEFINED;
    ST_COMPLETED_TRANSITIONS[DS_ST_IN_Tn] = DS_TERMINATED;
    ST_COMPLETED_TRANSITIONS[DS_ST_IN_ACK] = DS_UNDEFINED;
    ST_COMPLETED_TRANSITIONS[DS_ST_IN_IO_EXCEPTION] = DS_COMPLETED;
    ST_COMPLETED_TRANSITIONS[DS_ST_IN_OTHER_EXCEPTION] = DS_UNDEFINED;
    ST_COMPLETED_TRANSITIONS[DS_ST_IN_NEXT_CLIENT] = DS_COMPLETED;
    ST_COMPLETED_TRANSITIONS[DS_ST_IN_NO_CLIENT] = DS_TERMINATED;

    //////////////////////////////////////////////////////////////////
    /// Server Transaction CONFIRMED state transitions (STI_CONFIRMED)
    //////////////////////////////////////////////////////////////////
    /** server INVITE transaction transitions from CONFIRMED state. */
    STI_CONFIRMED_TRANSITIONS[DS_ST_IN_START] = DS_UNDEFINED;
    STI_CONFIRMED_TRANSITIONS[DS_ST_IN_REQUEST] = DS_CONFIRMED;
    STI_CONFIRMED_TRANSITIONS[DS_ST_IN_PROVISIONAL] = DS_UNDEFINED;
    STI_CONFIRMED_TRANSITIONS[DS_ST_IN_TPROVISIONAL] = DS_UNDEFINED;
    STI_CONFIRMED_TRANSITIONS[DS_ST_IN_2XX] = DS_UNDEFINED;
    STI_CONFIRMED_TRANSITIONS[DS_ST_IN_3TO6XX] = DS_UNDEFINED;
    STI_CONFIRMED_TRANSITIONS[DS_ST_IN_CANCEL] = DS_CONFIRMED;
    STI_CONFIRMED_TRANSITIONS[DS_ST_IN_TIMEOUT] = DS_TERMINATED;
    STI_CONFIRMED_TRANSITIONS[DS_ST_IN_T1] = DS_CONFIRMED;
    STI_CONFIRMED_TRANSITIONS[DS_ST_IN_T1_EXPIRED] = DS_UNDEFINED;
    STI_CONFIRMED_TRANSITIONS[DS_ST_IN_Tn] = DS_TERMINATED;
    STI_CONFIRMED_TRANSITIONS[DS_ST_IN_ACK] = DS_CONFIRMED;
    STI_CONFIRMED_TRANSITIONS[DS_ST_IN_IO_EXCEPTION] = DS_CONFIRMED;
    STI_CONFIRMED_TRANSITIONS[DS_ST_IN_OTHER_EXCEPTION] = DS_UNDEFINED;
    STI_CONFIRMED_TRANSITIONS[DS_ST_IN_NEXT_CLIENT] = DS_UNDEFINED;
    STI_CONFIRMED_TRANSITIONS[DS_ST_IN_NO_CLIENT] = DS_UNDEFINED;
    STI_CONFIRMED_TRANSITIONS[DS_ST_IN_REL_PROVISIONAL] = DS_UNDEFINED;
    STI_CONFIRMED_TRANSITIONS[DS_ST_IN_PRACK] = DS_UNDEFINED;

    //////////////////////////////////////////////////////////////////
    /// Server Transaction WAIT_PRACK state transitions (STI_WAIT_PRACK)
    //////////////////////////////////////////////////////////////////
    /** server INVITE transaction transitions from WAIT_PRACK state. */
    STI_WAIT_PRACK_TRANSITIONS[DS_ST_IN_START] = DS_UNDEFINED;
    STI_WAIT_PRACK_TRANSITIONS[DS_ST_IN_REQUEST] = DS_UNDEFINED;
    STI_WAIT_PRACK_TRANSITIONS[DS_ST_IN_PROVISIONAL] = DS_UNDEFINED;
    STI_WAIT_PRACK_TRANSITIONS[DS_ST_IN_TPROVISIONAL] = DS_UNDEFINED;
    STI_WAIT_PRACK_TRANSITIONS[DS_ST_IN_2XX] = DS_TERMINATED;
    STI_WAIT_PRACK_TRANSITIONS[DS_ST_IN_3TO6XX] = DS_COMPLETED;
    STI_WAIT_PRACK_TRANSITIONS[DS_ST_IN_CANCEL] = DS_WAIT_PRACK;
    STI_WAIT_PRACK_TRANSITIONS[DS_ST_IN_TIMEOUT] = DS_UNDEFINED;
    STI_WAIT_PRACK_TRANSITIONS[DS_ST_IN_T1] = DS_WAIT_PRACK;
    STI_WAIT_PRACK_TRANSITIONS[DS_ST_IN_T1_EXPIRED] = DS_TERMINATED;
    STI_WAIT_PRACK_TRANSITIONS[DS_ST_IN_Tn] = DS_TERMINATED;
    STI_WAIT_PRACK_TRANSITIONS[DS_ST_IN_ACK] = DS_UNDEFINED;
    STI_WAIT_PRACK_TRANSITIONS[DS_ST_IN_IO_EXCEPTION] = DS_WAIT_PRACK;
    STI_WAIT_PRACK_TRANSITIONS[DS_ST_IN_OTHER_EXCEPTION] = DS_UNDEFINED;
    STI_WAIT_PRACK_TRANSITIONS[DS_ST_IN_NEXT_CLIENT] = DS_UNDEFINED;
    STI_WAIT_PRACK_TRANSITIONS[DS_ST_IN_NO_CLIENT] = DS_UNDEFINED;
    // DS_WAIT_PRACK; - make it DS_UNDEFINED; so that an exception will be thrown
    STI_WAIT_PRACK_TRANSITIONS[DS_ST_IN_REL_PROVISIONAL] = DS_UNDEFINED;
    STI_WAIT_PRACK_TRANSITIONS[DS_ST_IN_PRACK] = DS_STI_RELPROCEEDING;

    //////////////////////////////////////////////////////////////////
    /// Server Transaction RELPROCEEDING state transitions (STI_RELPROCEEDING)
    //////////////////////////////////////////////////////////////////
    /** server INVITE transaction transitions from RELPROCEEDING state. */
    STI_RELPROCEEDING_TRANSITIONS[DS_ST_IN_START] = DS_UNDEFINED;
    STI_RELPROCEEDING_TRANSITIONS[DS_ST_IN_REQUEST] = DS_UNDEFINED;
    STI_RELPROCEEDING_TRANSITIONS[DS_ST_IN_PROVISIONAL] = DS_UNDEFINED;
    STI_RELPROCEEDING_TRANSITIONS[DS_ST_IN_TPROVISIONAL] = DS_UNDEFINED;
    STI_RELPROCEEDING_TRANSITIONS[DS_ST_IN_2XX] = DS_TERMINATED;
    STI_RELPROCEEDING_TRANSITIONS[DS_ST_IN_3TO6XX] = DS_COMPLETED;
    STI_RELPROCEEDING_TRANSITIONS[DS_ST_IN_CANCEL] = DS_STI_RELPROCEEDING;
    STI_RELPROCEEDING_TRANSITIONS[DS_ST_IN_TIMEOUT] = DS_UNDEFINED;
    STI_RELPROCEEDING_TRANSITIONS[DS_ST_IN_T1] = DS_STI_RELPROCEEDING;
    STI_RELPROCEEDING_TRANSITIONS[DS_ST_IN_T1_EXPIRED] = DS_TERMINATED;
    STI_RELPROCEEDING_TRANSITIONS[DS_ST_IN_Tn] = DS_TERMINATED;
    STI_RELPROCEEDING_TRANSITIONS[DS_ST_IN_ACK] = DS_UNDEFINED;
    STI_RELPROCEEDING_TRANSITIONS[DS_ST_IN_IO_EXCEPTION] = DS_STI_RELPROCEEDING;
    STI_RELPROCEEDING_TRANSITIONS[DS_ST_IN_OTHER_EXCEPTION] = DS_UNDEFINED;
    STI_RELPROCEEDING_TRANSITIONS[DS_ST_IN_NEXT_CLIENT] = DS_STI_RELPROCEEDING;
    STI_RELPROCEEDING_TRANSITIONS[DS_ST_IN_NO_CLIENT] = DS_TERMINATED;
    STI_RELPROCEEDING_TRANSITIONS[DS_ST_IN_REL_PROVISIONAL] = DS_WAIT_PRACK;
    STI_RELPROCEEDING_TRANSITIONS[DS_ST_IN_PRACK] = DS_STI_RELPROCEEDING;

    // INVITE Server Transaction Extra State Transition Tables
    // ////////////////////////////////////////////////////////////////////////////////// (Invite)
    //                                           (Input)
    //                      0    1     2   3   4   5    6    7    8     9    10   11   12   13  14
    // 15  16   17
    // ------------------------------------------------------------------------------------------------------------
    //  (STIX_TABLE)
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //   State             | ST   REQ  PRO TPR 2XX 3TO6 CNL  TO   T1   T1_EXP Tn   ACK  IOE  OE  NXC
    //  NOC RELP PRAC
    // ------------------------------------------------------------------------------------------------------------
    // 0 XINI  XINITIAL    | XCOM ---  --- --- --- ---  ---  ---  ---  ---    ---  ---  ---  --- ---
    //  --- --- ---
    // 1 XCOM  XCOMPLETED  | ---  XCOM --- --- --- ---  XCOM ---  XCOM XTER   XTER XCON XTER ---
    // XCOM XTER --- XCOM
    // 2 XCON  XCONFIRMED  | ---  XCON --- --- --- ---  XCON XTER XCON ---    XTER XCON XCON --- ---
    //  --- --- XCON
    // 3 XTER  XTERMINATED | ---  ---  --- --- --- ---  ---  ---  ---  ---    ---  ---  ---  --- ---
    //  --- --- ---
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Server INVITE extra transaction for 2xx xref
    //////////////////////////////////////////////////////////
    //
    //  DS_XINITIAL (INVITE)
    //

    //
    // ///////////////////////////////////////////////////////
    //  DS_XCOMPLETED (INVITE)
    //

    //
    // ///////////////////////////////////////////////////////
    //  DS_XCONFIRMED (INVITE)
    //

    //
    // ///////////////////////////////////////////////////////
    //  DS_XTERMINATED (INVITE)
    //

    //
    //

    //////////////////////////////////////////////////////////////////
    /// Server Transaction extra INITIAL state transitions (STIX_XINITIAL)
    //////////////////////////////////////////////////////////////////
    /** server INVITE transaction transitions from XINITIAL state. */
    STIX_XINITIAL_TRANSITIONS[DS_ST_IN_START] = DS_XCOMPLETED;
    STIX_XINITIAL_TRANSITIONS[DS_ST_IN_REQUEST] = DS_UNDEFINED;
    STIX_XINITIAL_TRANSITIONS[DS_ST_IN_PROVISIONAL] = DS_UNDEFINED;
    STIX_XINITIAL_TRANSITIONS[DS_ST_IN_TPROVISIONAL] = DS_UNDEFINED;
    STIX_XINITIAL_TRANSITIONS[DS_ST_IN_2XX] = DS_UNDEFINED;
    STIX_XINITIAL_TRANSITIONS[DS_ST_IN_3TO6XX] = DS_UNDEFINED;
    STIX_XINITIAL_TRANSITIONS[DS_ST_IN_CANCEL] = DS_UNDEFINED;
    STIX_XINITIAL_TRANSITIONS[DS_ST_IN_TIMEOUT] = DS_UNDEFINED;
    STIX_XINITIAL_TRANSITIONS[DS_ST_IN_T1] = DS_UNDEFINED;
    STIX_XINITIAL_TRANSITIONS[DS_ST_IN_T1_EXPIRED] = DS_UNDEFINED;
    STIX_XINITIAL_TRANSITIONS[DS_ST_IN_Tn] = DS_UNDEFINED;
    STIX_XINITIAL_TRANSITIONS[DS_ST_IN_ACK] = DS_UNDEFINED;
    STIX_XINITIAL_TRANSITIONS[DS_ST_IN_IO_EXCEPTION] = DS_UNDEFINED;
    STIX_XINITIAL_TRANSITIONS[DS_ST_IN_OTHER_EXCEPTION] = DS_UNDEFINED;
    STIX_XINITIAL_TRANSITIONS[DS_ST_IN_NEXT_CLIENT] = DS_UNDEFINED;
    STIX_XINITIAL_TRANSITIONS[DS_ST_IN_NO_CLIENT] = DS_UNDEFINED;
    STIX_XINITIAL_TRANSITIONS[DS_ST_IN_REL_PROVISIONAL] = DS_UNDEFINED;
    STIX_XINITIAL_TRANSITIONS[DS_ST_IN_PRACK] = DS_UNDEFINED;

    //////////////////////////////////////////////////////////////////
    /// Server Transaction extra COMPLETED state transitions (STIX_XCOMPLETED)
    //////////////////////////////////////////////////////////////////
    /** server INVITE transaction transitions from XCOMPLETED state. */
    STIX_XCOMPLETED_TRANSITIONS[DS_ST_IN_START] = DS_UNDEFINED;
    STIX_XCOMPLETED_TRANSITIONS[DS_ST_IN_REQUEST] = DS_XCOMPLETED;
    STIX_XCOMPLETED_TRANSITIONS[DS_ST_IN_PROVISIONAL] = DS_UNDEFINED;
    STIX_XCOMPLETED_TRANSITIONS[DS_ST_IN_TPROVISIONAL] = DS_UNDEFINED;
    STIX_XCOMPLETED_TRANSITIONS[DS_ST_IN_2XX] = DS_UNDEFINED;
    STIX_XCOMPLETED_TRANSITIONS[DS_ST_IN_3TO6XX] = DS_UNDEFINED;
    STIX_XCOMPLETED_TRANSITIONS[DS_ST_IN_CANCEL] = DS_XCOMPLETED;
    STIX_XCOMPLETED_TRANSITIONS[DS_ST_IN_TIMEOUT] = DS_UNDEFINED;
    STIX_XCOMPLETED_TRANSITIONS[DS_ST_IN_T1] = DS_XCOMPLETED;
    STIX_XCOMPLETED_TRANSITIONS[DS_ST_IN_T1_EXPIRED] = DS_XTERMINATED;
    STIX_XCOMPLETED_TRANSITIONS[DS_ST_IN_Tn] = DS_XTERMINATED;
    STIX_XCOMPLETED_TRANSITIONS[DS_ST_IN_ACK] = DS_XCONFIRMED;
    STIX_XCOMPLETED_TRANSITIONS[DS_ST_IN_IO_EXCEPTION] = DS_XTERMINATED;
    STIX_XCOMPLETED_TRANSITIONS[DS_ST_IN_OTHER_EXCEPTION] = DS_UNDEFINED;
    STIX_XCOMPLETED_TRANSITIONS[DS_ST_IN_NEXT_CLIENT] = DS_XCOMPLETED;
    STIX_XCOMPLETED_TRANSITIONS[DS_ST_IN_NO_CLIENT] = DS_XTERMINATED;
    STIX_XCOMPLETED_TRANSITIONS[DS_ST_IN_REL_PROVISIONAL] = DS_UNDEFINED;
    STIX_XCOMPLETED_TRANSITIONS[DS_ST_IN_PRACK] = DS_XCOMPLETED;

    //////////////////////////////////////////////////////////////////
    /// Server Transaction extra CONFIRMED state transitions (STIX_XCONFIRMED)
    //////////////////////////////////////////////////////////////////
    /** server INVITE transaction transitions from XCONFIRMED state. */
    STIX_XCONFIRMED_TRANSITIONS[DS_ST_IN_START] = DS_UNDEFINED;
    STIX_XCONFIRMED_TRANSITIONS[DS_ST_IN_REQUEST] = DS_XCONFIRMED;
    STIX_XCONFIRMED_TRANSITIONS[DS_ST_IN_PROVISIONAL] = DS_UNDEFINED;
    STIX_XCONFIRMED_TRANSITIONS[DS_ST_IN_TPROVISIONAL] = DS_UNDEFINED;
    STIX_XCONFIRMED_TRANSITIONS[DS_ST_IN_2XX] = DS_UNDEFINED;
    STIX_XCONFIRMED_TRANSITIONS[DS_ST_IN_3TO6XX] = DS_UNDEFINED;
    STIX_XCONFIRMED_TRANSITIONS[DS_ST_IN_CANCEL] = DS_XCONFIRMED;
    STIX_XCONFIRMED_TRANSITIONS[DS_ST_IN_TIMEOUT] = DS_XTERMINATED;
    STIX_XCONFIRMED_TRANSITIONS[DS_ST_IN_T1] = DS_XCONFIRMED;
    STIX_XCONFIRMED_TRANSITIONS[DS_ST_IN_T1_EXPIRED] = DS_UNDEFINED;
    STIX_XCONFIRMED_TRANSITIONS[DS_ST_IN_Tn] = DS_XTERMINATED;
    STIX_XCONFIRMED_TRANSITIONS[DS_ST_IN_ACK] = DS_XCONFIRMED;
    STIX_XCONFIRMED_TRANSITIONS[DS_ST_IN_IO_EXCEPTION] = DS_XCONFIRMED;
    STIX_XCONFIRMED_TRANSITIONS[DS_ST_IN_OTHER_EXCEPTION] = DS_UNDEFINED;
    STIX_XCONFIRMED_TRANSITIONS[DS_ST_IN_NEXT_CLIENT] = DS_UNDEFINED;
    STIX_XCONFIRMED_TRANSITIONS[DS_ST_IN_NO_CLIENT] = DS_UNDEFINED;
    STIX_XCONFIRMED_TRANSITIONS[DS_ST_IN_REL_PROVISIONAL] = DS_UNDEFINED;
    STIX_XCONFIRMED_TRANSITIONS[DS_ST_IN_PRACK] = DS_XCONFIRMED;
  };

  /** client INVITE transaction transitions. */
  public static final int[][] CTI_TRANSITIONS = new int[CTI_TABLE_STATE_INDEX][];

  /** client non-INVITE transaction transitions. */
  public static final int[][] CT_TRANSITIONS = new int[CT_TABLE_STATE_INDEX][];

  /** client INVITE transaction 2XX extra transitions. */
  public static final int[][] CTIX_TRANSITIONS = new int[CTIX_TABLE_STATE_INDEX][];

  /** server INVITE transaction transitions. */
  public static final int[][] STI_TRANSITIONS = new int[STI_TABLE_STATE_INDEX][];

  /** server non-INVITE transaction transitions. */
  public static final int[][] ST_TRANSITIONS = new int[ST_TABLE_STATE_INDEX][];

  /** server INVITE extra transaction transitions for 2XX. */
  public static final int[][] STIX_TRANSITIONS = new int[STIX_TABLE_STATE_INDEX][];

  static {
    // client INVITE transaction transitions
    CTI_TRANSITIONS[DS_INITIAL_IDX] = CTI_INITIAL_TRANSITIONS;
    CTI_TRANSITIONS[DS_CALLING_IDX] = CTI_CALLING_TRANSITIONS;
    CTI_TRANSITIONS[DS_PROCEEDING_IDX] = CTI_PROCEEDING_TRANSITIONS;
    CTI_TRANSITIONS[DS_COMPLETED_IDX] = CTI_COMPLETED_TRANSITIONS;
    CTI_TRANSITIONS[DS_CTI_RELPROCEEDING_IDX] = CTI_RELPROCEEDING_TRANSITIONS;

    // client non-INVITE transaction transitions.
    CT_TRANSITIONS[DS_INITIAL_IDX] = CT_INITIAL_TRANSITIONS;
    CT_TRANSITIONS[DS_CALLING_IDX] = CT_CALLING_TRANSITIONS;
    CT_TRANSITIONS[DS_PROCEEDING_IDX] = CT_PROCEEDING_TRANSITIONS;
    CT_TRANSITIONS[DS_COMPLETED_IDX] = CT_COMPLETED_TRANSITIONS;

    // client INVITE transaction 2XX extra transitions.
    CTIX_TRANSITIONS[DS_XINITIAL_IDX] = CTIX_XINITIAL_TRANSITIONS;
    CTIX_TRANSITIONS[DS_XCOMPLETED_IDX] = CTIX_XCOMPLETED_TRANSITIONS;

    // server INVITE transaction transitions.
    STI_TRANSITIONS[DS_INITIAL_IDX] = STI_INITIAL_TRANSITIONS;
    STI_TRANSITIONS[DS_CALLING_IDX] = STI_CALLING_TRANSITIONS;
    STI_TRANSITIONS[DS_PROCEEDING_IDX] = STI_PROCEEDING_TRANSITIONS;
    STI_TRANSITIONS[DS_COMPLETED_IDX] = STI_COMPLETED_TRANSITIONS;
    STI_TRANSITIONS[DS_CONFIRMED_IDX] = STI_CONFIRMED_TRANSITIONS;
    STI_TRANSITIONS[DS_STI_WAIT_PRACK_IDX] = STI_WAIT_PRACK_TRANSITIONS;
    STI_TRANSITIONS[DS_STI_RELPROCEEDING_IDX] = STI_RELPROCEEDING_TRANSITIONS;

    // server non-INVITE transaction transitions.
    ST_TRANSITIONS[DS_INITIAL_IDX] = ST_INITIAL_TRANSITIONS;
    ST_TRANSITIONS[DS_CALLING_IDX] = ST_CALLING_TRANSITIONS;
    ST_TRANSITIONS[DS_PROCEEDING_IDX] = ST_PROCEEDING_TRANSITIONS;
    ST_TRANSITIONS[DS_COMPLETED_IDX] = ST_COMPLETED_TRANSITIONS;

    // server INVITE extra transaction transitions for 2XX.
    STIX_TRANSITIONS[DS_XINITIAL_IDX] = STIX_XINITIAL_TRANSITIONS;
    STIX_TRANSITIONS[DS_XCOMPLETED_IDX] = STIX_XCOMPLETED_TRANSITIONS;
    STIX_TRANSITIONS[DS_XCONFIRMED_IDX] = STIX_XCONFIRMED_TRANSITIONS;
  };
} // End class DsSipStateMachineDefinitionsNG
