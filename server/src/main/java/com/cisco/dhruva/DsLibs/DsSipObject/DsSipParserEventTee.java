// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import java.util.Stack;

/**
 * This class is used along with DsSipTransactionKey to create a key at the same time that we parse
 * an incoming message. A tee splits the events and delivers to both of the downstream listeners.
 *
 * <p>Here's how to use it with DsSipTransactionKey
 *
 * <pre>
 * DsSipMessageListenerFactory factory = ...;  // this is the factory that builds the
 * // SIP Object
 *
 * DsSipTransactionKey key = new DsSipTransactionKey();    // an empty key
 *
 * DsSipParserEventTee tee = new DsSipParserEventTee(key, factory); // the T
 * DsSipMsgParser.parse(tee, bytes, offset, count);
 *
 * // now the message is built and the key is populated
 * </pre>
 */
public final class DsSipParserEventTee
    implements DsSipMessageListener,
        DsSipMessageListenerFactory,
        DsSipHeaderListener,
        DsSipElementListener {
  //  statics
  private final int MAX_DEPTH = 10;

  private Stack s1 = new Stack();
  private Stack s2 = new Stack();

  private int[] context = new int[MAX_DEPTH];
  private int idx = 0;

  private DsSipMessageListenerFactory m_f1, m_f2;
  private DsSipMessageListener m_ml1, m_ml2;
  private DsSipHeaderListener m_hl1, m_hl2;
  private DsSipElementListener m_el1, m_el2;

  /**
   * Constructs a Two-Way (Tee) parser event listener with the specified two parser event listeners.
   *
   * @param f1 The first message parser event listener.
   * @param f2 The second message parser event listener.
   */
  public DsSipParserEventTee(DsSipMessageListenerFactory f1, DsSipMessageListenerFactory f2) {
    m_f1 = f1;
    m_f2 = f2;
  }

  /**
   * Returns the first message parser event listener.
   *
   * @return the first message parser event listener.
   */
  public DsSipMessageListenerFactory getF1() {
    return m_f1;
  }

  /**
   * Returns the second message parser event listener.
   *
   * @return the second message parser event listener.
   */
  public DsSipMessageListenerFactory getF2() {
    return m_f2;
  }

  /**
   * Reinitializes this Two-Way (Tee) parser event listener with the new set of message parser event
   * listenerers.
   *
   * @param f1 The first message parser event listener.
   * @param f2 The second message parser event listener.
   */
  public void reInit(DsSipMessageListenerFactory f1, DsSipMessageListenerFactory f2) {
    idx = 0;
    m_f1 = f1;
    m_f2 = f2;
    m_ml1 = null;
    m_ml2 = null;
    m_hl1 = null;
    m_hl2 = null;
    m_el1 = null;
    m_el2 = null;
    s1.clear();
    s2.clear();
  }

  /**
   * Reinitializes the second parser event listener to the new specified listener.
   *
   * @param f2 The second message parser event listener.
   */
  public void reInitF2(DsSipMessageListenerFactory f2) {
    idx = 0;
    m_f2 = f2;
    m_ml1 = null;
    m_ml2 = null;
    m_hl1 = null;
    m_hl2 = null;
    m_el1 = null;
    m_el2 = null;
    s1.clear();
    s2.clear();
  }

  // /////////////////////////////////////
  // ///// Factory ///////////////////////
  // /////////////////////////////////////

  /*
   * javadoc inherited
   */
  public DsSipMessageListener responseBegin(
      byte[] buffer, int code, int offset, int count, boolean isEncoded)
      throws DsSipParserListenerException {
    if (m_f1 != null) m_ml1 = m_f1.responseBegin(buffer, code, offset, count, isEncoded);
    if (m_f2 != null) m_ml2 = m_f2.responseBegin(buffer, code, offset, count, isEncoded);
    if ((m_ml1 == null) && (m_ml2 == null)) return null;

    return this;
  }

  /*
   * javadoc inherited
   */
  public DsSipMessageListener requestBegin(
      byte[] buffer, int methodOffset, int methodCount, boolean isEncoded)
      throws DsSipParserListenerException {
    if (m_f1 != null) m_ml1 = m_f1.requestBegin(buffer, methodOffset, methodCount, isEncoded);
    if (m_f2 != null) m_ml2 = m_f2.requestBegin(buffer, methodOffset, methodCount, isEncoded);
    if ((m_ml1 == null) && (m_ml2 == null)) return null;

    return this;
  }

  // /////////////////////////////////////
  // ////// Message Listener /////////////
  // /////////////////////////////////////

  /*
   * javadoc inherited
   */
  public DsSipElementListener requestURIBegin(byte[] buffer, int schemeOffset, int schemeCount)
      throws DsSipParserListenerException {
    if (m_ml1 != null) m_el1 = m_ml1.requestURIBegin(buffer, schemeOffset, schemeCount);
    if (m_ml2 != null) m_el2 = m_ml2.requestURIBegin(buffer, schemeOffset, schemeCount);

    if ((m_el1 == null) && (m_el2 == null)) return null;

    return this;
  }

  /*
   * javadoc inherited
   */
  public void requestURIFound(byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {
    if (m_ml1 != null) m_ml1.requestURIFound(buffer, offset, count, valid);
    if (m_ml2 != null) m_ml2.requestURIFound(buffer, offset, count, valid);
  }

  /*
   * javadoc inherited
   */
  public void protocolFound(
      byte[] buffer,
      int protocolOffset,
      int protocolCount,
      int majorOffset,
      int majorCount,
      int minorOffset,
      int minorCount,
      boolean valid)
      throws DsSipParserListenerException {
    if (m_ml1 != null)
      m_ml1.protocolFound(
          buffer,
          protocolOffset,
          protocolCount,
          majorOffset,
          majorCount,
          minorOffset,
          minorCount,
          valid);
    if (m_ml2 != null)
      m_ml2.protocolFound(
          buffer,
          protocolOffset,
          protocolCount,
          majorOffset,
          majorCount,
          minorOffset,
          minorCount,
          valid);
  }

  /*
   * javadoc inherited
   */
  public void messageFound(byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {
    if (m_ml1 != null) m_ml1.messageFound(buffer, offset, count, valid);
    if (m_ml2 != null) m_ml2.messageFound(buffer, offset, count, valid);
  }

  /*
   * javadoc inherited
   */
  public void bodyFoundInRequestURI(byte[] buffer, int offset, int count)
      throws DsSipParserListenerException {
    if (m_ml1 != null) m_ml1.bodyFoundInRequestURI(buffer, offset, count);
    if (m_ml2 != null) m_ml2.bodyFoundInRequestURI(buffer, offset, count);
  }

  /*
   * javadoc inherited
   */
  public DsSipHeaderListener getHeaderListener() {
    if (m_ml1 != null) m_hl1 = m_ml1.getHeaderListener();
    if (m_ml2 != null) m_hl2 = m_ml2.getHeaderListener();
    if ((m_hl1 == null) && (m_hl2 == null)) return null;

    return this;
  }

  // /////////////////////////////////////
  // ////////  Header Listener ///////////
  // /////////////////////////////////////

  public DsSipElementListener headerBegin(int headerId) throws DsSipParserListenerException {
    if (m_hl1 != null) m_el1 = m_hl1.headerBegin(headerId);
    if (m_hl2 != null) m_el2 = m_hl2.headerBegin(headerId);

    if ((m_el1 == null) && (m_el2 == null)) return null;

    return this;
  }

  public void headerFound(int headerId, byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {
    if (m_hl1 != null) m_hl1.headerFound(headerId, buffer, offset, count, valid);
    if (m_hl2 != null) m_hl2.headerFound(headerId, buffer, offset, count, valid);
  }

  // /////////////////////////////////////
  // ///////// Element Listener //////////

  /*
   * javadoc inherited
   */
  // /////////////////////////////////////
  public DsSipElementListener elementBegin(int contextId, int elementId)
      throws DsSipParserListenerException {
    if (idx == MAX_DEPTH - 1) {
      throw new DsSipParserListenerException(
          "Event Tee exceeded max element nesting: " + MAX_DEPTH);
    }

    DsSipElementListener tel1 = null, tel2 = null;

    if (m_el1 != null) tel1 = m_el1.elementBegin(contextId, elementId);
    if (m_el2 != null) tel2 = m_el2.elementBegin(contextId, elementId);

    if ((tel1 == null) && (tel2 == null)) {
      return null;
    }

    s1.push(m_el1);
    s2.push(m_el2);

    m_el1 = tel1;
    m_el2 = tel2;

    context[idx++] = contextId;

    return this;
  }

  /*
   * javadoc inherited
   */
  public void elementFound(
      int contextId, int elementId, byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {

    if (m_el1 != null) m_el1.elementFound(contextId, elementId, buffer, offset, count, valid);
    if (m_el2 != null) m_el2.elementFound(contextId, elementId, buffer, offset, count, valid);

    if ((idx > 0) && (contextId == context[idx - 1])) {
      idx--;
      m_el1 = (DsSipElementListener) s1.pop();
      m_el2 = (DsSipElementListener) s2.pop();
    }
  }

  /*
   * javadoc inherited
   */
  public void parameterFound(
      int contextId, byte[] buffer, int nameOffset, int nameCount, int valueOffset, int valueCount)
      throws DsSipParserListenerException {
    if (m_el1 != null)
      m_el1.parameterFound(contextId, buffer, nameOffset, nameCount, valueOffset, valueCount);
    if (m_el2 != null)
      m_el2.parameterFound(contextId, buffer, nameOffset, nameCount, valueOffset, valueCount);
  }

  /*
   * javadoc inherited
   */
  public void unknownFound(
      byte[] buffer, int nameOffset, int nameCount, int valueOffset, int valueCount, boolean valid)
      throws DsSipParserListenerException {
    if (m_hl1 != null)
      m_hl1.unknownFound(buffer, nameOffset, nameCount, valueOffset, valueCount, valid);
    if (m_hl2 != null)
      m_hl2.unknownFound(buffer, nameOffset, nameCount, valueOffset, valueCount, valid);
  }
}
