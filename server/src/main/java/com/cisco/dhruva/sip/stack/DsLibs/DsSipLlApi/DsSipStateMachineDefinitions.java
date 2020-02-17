/*
    Copyright (c) 2003-2004 by Cisco Systems, Inc.
    All rights reserved.
*/

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

/** Low level state machine constants. */
public interface DsSipStateMachineDefinitions {

  // Input type definitions
  /** input mask value. */
  public static final int DS_INPUT_MASK = 0x00ff;

  // Client transaction inputs
  /** client transaction input: the first input. */
  public static final int DS_CT_IN_START = 0x0000;
  /** client transaction input: request rtx timer. */
  public static final int DS_CT_IN_T1 = 0x0001;
  /** client transaction input: request rtx timer expired. */
  public static final int DS_CT_IN_T1_EXPIRED = 0x0002;
  /** client transaction input: time to control duration in PROC expired. */
  public static final int DS_CT_IN_Tp = 0x0003;
  /** client transaction input: COM->TERM timer(formerly 32 second timer) expired. */
  public static final int DS_CT_IN_TIMEOUT = 0x0004;
  /** client transaction input: max transaction duration timer. */
  public static final int DS_CT_IN_Tn = 0x0005;
  /** client transaction input: user code cancels transaction. */
  public static final int DS_CT_IN_CANCEL = 0x0006;
  /** client transaction input: cancel timer expired. */
  public static final int DS_CT_IN_CANCEL_TIMER = 0x0007;
  /** client transaction input: user code acks transaction . */
  public static final int DS_CT_IN_ACK = 0x0008;
  /** client transaction input: received provisional response . */
  public static final int DS_CT_IN_PROVISIONAL = 0x0009;
  /** client transaction input: received 2XX final respnse. */
  public static final int DS_CT_IN_2XX = 0x000a;
  /** client transaction input: received 3XX-6XX final respnse. */
  public static final int DS_CT_IN_3TO6XX = 0x000b;
  /** client transaction input: service unavailable response. */
  public static final int DS_CT_IN_SERVICE_UNAVAILABLE = 0x000c;
  /** client transaction input: next server found. */
  public static final int DS_CT_IN_NEXT_SERVER = 0x000d;
  /** client transaction input: next server not found. */
  public static final int DS_CT_IN_NO_SERVER = 0x000e;
  /** client transaction input: IO exception. */
  public static final int DS_CT_IN_IO_EXCEPTION = 0x000f;
  /** client transaction input: other exception. */
  public static final int DS_CT_IN_OTHER_EXCEPTION = 0x0010;
  /** New input for PRACK: client transaction input: received reliable provisional response . */
  public static final int DS_CT_IN_REL_PROVISIONAL = 0x0011;
  /** client transaction input: user code sends PRACK. */
  public static final int DS_CT_IN_PRACK = 0x0012;

  // Server transaction inputs
  /** server transaction input: the first input. */
  public static final int DS_ST_IN_START = 0x0000;
  /** server transaction input: request was received. */
  public static final int DS_ST_IN_REQUEST = 0x0001;
  /** server transaction input: user code sends provisional. */
  public static final int DS_ST_IN_PROVISIONAL = 0x0002;
  /** server transaction input: delayed provisional timer fires . */
  public static final int DS_ST_IN_TPROVISIONAL = 0x0003;
  /** server transaction input: user code sends 2XX final. */
  public static final int DS_ST_IN_2XX = 0x0004;
  /** server transaction input: user code sends 3XX-6XX final. */
  public static final int DS_ST_IN_3TO6XX = 0x0005;
  /** server transaction input: received cancel. */
  public static final int DS_ST_IN_CANCEL = 0x0006;
  /** server transaction input: timer for COM->TERM(ST) or CONF->TERM(STI) expired. */
  public static final int DS_ST_IN_TIMEOUT = 0x0007;
  /** server transaction input: response rtx timer. */
  public static final int DS_ST_IN_T1 = 0x0008;
  /** server transaction input: response rtx timer expired. */
  public static final int DS_ST_IN_T1_EXPIRED = 0x0009;
  /** server transaction input: max transaction time expired. */
  public static final int DS_ST_IN_Tn = 0x000a;
  /** server transaction input: ACK was received. */
  public static final int DS_ST_IN_ACK = 0x000b;
  /** server transaction input: IO exception. */
  public static final int DS_ST_IN_IO_EXCEPTION = 0x000c;
  /** server transaction input: other exception. */
  public static final int DS_ST_IN_OTHER_EXCEPTION = 0x000d;
  /** server transaction input: next client found. */
  public static final int DS_ST_IN_NEXT_CLIENT = 0x000e;
  /** server transaction input: next client not found. */
  public static final int DS_ST_IN_NO_CLIENT = 0x000f;
  /**
   * New input for PRACK: server transaction input: user code sends reliable provisional response.
   */
  public static final int DS_ST_IN_REL_PROVISIONAL = 0x0010;
  /** New input for PRACK: server transaction input: received PRACK. */
  public static final int DS_ST_IN_PRACK = 0x0011;

  // state definitions
  /** state mask value. */
  public static final int DS_MASK = 0xff00;
  /** initial state. */
  public static final int DS_INITIAL = 0x0100;
  /** calling state. */
  public static final int DS_CALLING = 0x0200;
  /** PRACK: wait for PRACK state. */
  public static final int DS_WAIT_PRACK = 0x0300;
  /** PRACK: reliable proceeding state (CTI_TABLE). */
  public static final int DS_CTI_RELPROCEEDING = 0x0400;
  /** PRACK: reliable proceeding state (STI_TABLE). */
  public static final int DS_STI_RELPROCEEDING = 0x0500;
  /** proceeding state. */
  public static final int DS_PROCEEDING = 0x0600;
  /** completed state. */
  public static final int DS_COMPLETED = 0x0700;
  /** completed state. */
  public static final int DS_CONFIRMED = 0x0800;
  /** terminated state. */
  public static final int DS_TERMINATED = 0x0900;
  /** extra initial state for 2xx invite trans. */
  public static final int DS_XINITIAL = 0x0A00;
  /** extra completed state for 2xx invite trans. */
  public static final int DS_XCOMPLETED = 0x0B00;
  /** extra completed state for 2xx invite server trans. */
  public static final int DS_XCONFIRMED = 0x0C00;
  /** extra terminated state for 2xx invite trans. */
  public static final int DS_XTERMINATED = 0x0D00;
  /** undefined state. */
  public static final int DS_UNDEFINED = 0x0E00;

  /** initial id. */
  public static final int DS_INITIAL_IDX = 0;
  /** calling id. */
  public static final int DS_CALLING_IDX = 1;
  /** proceeding id. */
  public static final int DS_PROCEEDING_IDX = 2;
  /** completed id. */
  public static final int DS_COMPLETED_IDX = 3;
  /** confirmed id. */
  public static final int DS_CONFIRMED_IDX = 4;
  /** New index for PRACK: index for CTI_TRANSITIONS */
  public static final int DS_CTI_RELPROCEEDING_IDX = 4;
  /** New index for PRACK: index for STI_TRANSITIONS */
  public static final int DS_STI_WAIT_PRACK_IDX = 5;
  /** New index for PRACK: reliable proceeding id. */
  public static final int DS_STI_RELPROCEEDING_IDX = 6;

  /** xinitial id. */
  public static final int DS_XINITIAL_IDX = 0;
  /** xcompleted id. */
  public static final int DS_XCOMPLETED_IDX = 1;
  /** xconfirmed id. */
  public static final int DS_XCONFIRMED_IDX = 2;

  /** Number of columns in each table */
  public final int CTI_TABLE_INDEX = 19;

  public final int CT_TABLE_INDEX = 17;
  public final int STI_TABLE_INDEX = 19;
  public final int ST_TABLE_INDEX = 17;
  public final int CTIX_TABLE_INDEX = 19;
  public final int STIX_TABLE_INDEX = 18;

  /** Number of rows in each table */
  public final int CTI_TABLE_STATE_INDEX = 5;

  public final int CT_TABLE_STATE_INDEX = 4;
  public final int STI_TABLE_STATE_INDEX = 7;
  public final int ST_TABLE_STATE_INDEX = 4;
  public final int CTIX_TABLE_STATE_INDEX = 2;
  public final int STIX_TABLE_STATE_INDEX = 3;
} // End class DsSipStateMachineDefinitionsNG
