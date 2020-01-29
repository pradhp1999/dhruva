// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import java.util.Stack;

/**
 * This class serves as a three way multiplexor for the parser events. The three parser listeners,
 * that are interested in being notified of the parser events simultaneously in a single pass, can
 * register to this class instance and in turn register this class instance to the parser. Parser
 * will notify the parser events to this three way instance which in turn will notify to the three
 * registered parser listeners.
 */
public final class DsSipParserEvent3Way
    implements DsSipMessageListener,
        DsSipMessageListenerFactory,
        DsSipHeaderListener,
        DsSipElementListener {
  //  statics
  private static final int MAX_DEPTH = 10;

  private Stack s1 = new Stack();
  private Stack s2 = new Stack();
  private Stack s3 = new Stack();

  private int[] context = new int[MAX_DEPTH];
  private int idx = 0;

  private DsSipMessageListenerFactory m_f1, m_f2, m_f3;
  private DsSipMessageListener m_ml1, m_ml2, m_ml3;
  private DsSipHeaderListener m_hl1, m_hl2, m_hl3;
  private DsSipElementListener m_el1, m_el2, m_el3;

  /**
   * Constructs a Three-Way parser event listener with the specified three parser event listeners.
   *
   * @param f1 The first message parser event listener.
   * @param f2 The second message parser event listener.
   * @param f3 The third message parser event listener.
   */
  public DsSipParserEvent3Way(
      DsSipMessageListenerFactory f1,
      DsSipMessageListenerFactory f2,
      DsSipMessageListenerFactory f3) {
    m_f1 = f1;
    m_f2 = f2;
    m_f3 = f3;
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
   * Returns the third message parser event listener.
   *
   * @return the third message parser event listener.
   */
  public DsSipMessageListenerFactory getF3() {
    return m_f3;
  }

  /**
   * Reinitializes this Three-Way parser event listener with the new set of message parser event
   * listenerers.
   *
   * @param f1 The first message parser event listener.
   * @param f2 The second message parser event listener.
   * @param f3 The third message parser event listener.
   */
  public void reInit(
      DsSipMessageListenerFactory f1,
      DsSipMessageListenerFactory f2,
      DsSipMessageListenerFactory f3) {
    idx = 0;
    m_f1 = f1;
    m_f2 = f2;
    m_f3 = f3;
    m_ml1 = null;
    m_ml2 = null;
    m_ml3 = null;
    m_hl1 = null;
    m_hl2 = null;
    m_hl3 = null;
    m_el1 = null;
    m_el2 = null;
    m_el3 = null;
    s1.clear();
    s2.clear();
    s3.clear();
  }

  /** Clears all the states so that it will be clean for new parser events. */
  public void clear() {
    idx = 0;

    m_ml1 = null;
    m_ml2 = null;
    m_ml3 = null;
    m_hl1 = null;
    m_hl2 = null;
    m_hl3 = null;
    m_el1 = null;
    m_el2 = null;
    m_el3 = null;
    s1.clear();
    s2.clear();
    s3.clear();
  }

  /**
   * Reinitializes the third parser event listener to the new specified listener.
   *
   * @param f3 The third message parser event listener.
   */
  public void reInitF3(DsSipMessageListenerFactory f3) {
    idx = 0;
    m_f3 = f3;
    m_ml1 = null;
    m_ml2 = null;
    m_ml3 = null;
    m_hl1 = null;
    m_hl2 = null;
    m_hl3 = null;
    m_el1 = null;
    m_el2 = null;
    m_el3 = null;
    s1.clear();
    s2.clear();
    s3.clear();
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
    m_ml3 = null;
    m_hl1 = null;
    m_hl2 = null;
    m_hl3 = null;
    m_el1 = null;
    m_el2 = null;
    m_el3 = null;
    s1.clear();
    s2.clear();
    s3.clear();
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
    if (m_f3 != null) m_ml3 = m_f3.responseBegin(buffer, code, offset, count, isEncoded);
    if ((m_ml1 == null) && (m_ml2 == null) && (m_ml3 == null)) return null;
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
    if (m_f3 != null) m_ml3 = m_f3.requestBegin(buffer, methodOffset, methodCount, isEncoded);
    if ((m_ml1 == null) && (m_ml2 == null) && (m_ml3 == null)) return null;
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
    if (m_ml3 != null) m_el3 = m_ml3.requestURIBegin(buffer, schemeOffset, schemeCount);

    if ((m_el1 == null) && (m_el2 == null) && (m_el3 == null)) return null;
    return this;
  }

  /*
   * javadoc inherited
   */
  public void requestURIFound(byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {
    if (m_ml1 != null) m_ml1.requestURIFound(buffer, offset, count, valid);
    if (m_ml2 != null) m_ml2.requestURIFound(buffer, offset, count, valid);
    if (m_ml3 != null) m_ml3.requestURIFound(buffer, offset, count, valid);
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
    if (m_ml3 != null)
      m_ml3.protocolFound(
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
    if (m_ml3 != null) m_ml3.messageFound(buffer, offset, count, valid);
  }

  /*
   * javadoc inherited
   */
  public void bodyFoundInRequestURI(byte[] buffer, int offset, int count)
      throws DsSipParserListenerException {
    if (m_ml1 != null) m_ml1.bodyFoundInRequestURI(buffer, offset, count);
    if (m_ml2 != null) m_ml2.bodyFoundInRequestURI(buffer, offset, count);
    if (m_ml3 != null) m_ml3.bodyFoundInRequestURI(buffer, offset, count);
  }

  /*
   * javadoc inherited
   */
  public DsSipHeaderListener getHeaderListener() {
    if (m_ml1 != null) m_hl1 = m_ml1.getHeaderListener();
    if (m_ml2 != null) m_hl2 = m_ml2.getHeaderListener();
    if (m_ml3 != null) m_hl3 = m_ml3.getHeaderListener();
    if ((m_hl1 == null) && (m_hl2 == null) && (m_hl3 == null)) return null;
    return this;
  }

  // /////////////////////////////////////
  // ////////  Header Listener ///////////
  // /////////////////////////////////////

  /*
   * javadoc inherited
   */
  public DsSipElementListener headerBegin(int headerId) throws DsSipParserListenerException {
    if (m_hl1 != null) m_el1 = m_hl1.headerBegin(headerId);
    if (m_hl2 != null) m_el2 = m_hl2.headerBegin(headerId);
    if (m_hl3 != null) m_el3 = m_hl3.headerBegin(headerId);

    if ((m_el1 == null) && (m_el2 == null) && (m_el3 == null)) return null;
    return this;
  }

  /*
   * javadoc inherited
   */
  public void headerFound(int headerId, byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {
    if (m_hl1 != null) m_hl1.headerFound(headerId, buffer, offset, count, valid);
    if (m_hl2 != null) m_hl2.headerFound(headerId, buffer, offset, count, valid);
    if (m_hl3 != null) m_hl3.headerFound(headerId, buffer, offset, count, valid);
  }

  // /////////////////////////////////////
  // ///////// Element Listener //////////
  // /////////////////////////////////////

  /*
   * javadoc inherited
   */
  public DsSipElementListener elementBegin(int contextId, int elementId)
      throws DsSipParserListenerException {
    if (idx == MAX_DEPTH - 1) {
      throw new DsSipParserListenerException(
          "Event Tee exceeded max element nesting: " + MAX_DEPTH);
    }

    DsSipElementListener tel1 = null, tel2 = null, tel3 = null;

    if (m_el1 != null) tel1 = m_el1.elementBegin(contextId, elementId);
    if (m_el2 != null) tel2 = m_el2.elementBegin(contextId, elementId);
    if (m_el3 != null) tel3 = m_el3.elementBegin(contextId, elementId);

    if ((tel1 == null) && (tel2 == null) && (tel3 == null)) {
      return null;
    }

    s1.push(m_el1);
    s2.push(m_el2);
    s3.push(m_el3);

    m_el1 = tel1;
    m_el2 = tel2;
    m_el3 = tel3;

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
    if (m_el3 != null) m_el3.elementFound(contextId, elementId, buffer, offset, count, valid);

    if ((idx > 0) && (contextId == context[idx - 1])) {
      idx--;
      m_el1 = (DsSipElementListener) s1.pop();
      m_el2 = (DsSipElementListener) s2.pop();
      m_el3 = (DsSipElementListener) s3.pop();
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
    if (m_el3 != null)
      m_el3.parameterFound(contextId, buffer, nameOffset, nameCount, valueOffset, valueCount);
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
    if (m_hl3 != null)
      m_hl3.unknownFound(buffer, nameOffset, nameCount, valueOffset, valueCount, valid);
  }
}
