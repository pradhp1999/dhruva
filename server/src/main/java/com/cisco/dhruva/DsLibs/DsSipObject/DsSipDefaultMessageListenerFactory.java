// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;

/**
 * Provides for a default implementation for the DsSipMessageListenerFactory.
 *
 * <p>On the request and response event notification from the parser, it constructs corresponding
 * DsSipRequest or DsSipResponse object from the package "com.dynamicsoft.DsLibs.DsSipObject".
 *
 * <p>Users that want to add new method types to the JUA should override the <code>requestBegin()
 * </code> method. It shold check for the new type of method first and if it is a new type, then
 * create the corresponding object and return it. If it is a known method type, then just <code>
 * return super.requestBegin(buffer offset, count)</code>.
 *
 * <p>Implementors of a subclass must also set the member variable <code>message</code> to the
 * returned object from <code>requestBegin()</code>. Since this is a <code>DsSipMessage</code>,
 * implementors must subclass from <code>DsSipRequest</code>.
 *
 * <p>After creating the subclass, it must be registered with DsSipDefaultMessageFactory by calling
 * the static method <code>
 * DsSipDefaultMessageFactory.setMessageListenerFactory(DsSipDefaultMessageListenerFactory factory)
 * </code>.
 */
public class DsSipDefaultMessageListenerFactory
    implements DsSipMessageListenerFactory, DsSipConstants {
  /** The holder for the parsed DsSipMessage object. */
  protected DsSipMessage message;

  /*
   * javadoc inherited.
   */
  public DsSipMessageListener requestBegin(byte[] buffer, int offset, int count, boolean isEncoded)
      throws DsSipParserListenerException {
    if (DsSipMessage.DEBUG) {
      System.out.println(
          "requestBegin - method = [" + DsByteString.newString(buffer, offset, count) + "]");
    }
    int method = DsSipMsgParser.getMethod(buffer, offset, count);
    switch (method) {
      case INVITE:
        if (DsSipMessage.DEBUG) {
          System.out.println();
          System.out.println("<<<<<<<<<<<<<<<<< INVITE >>>>>>>>>>>>>>>>");
          System.out.println();
        }
        if (isEncoded == true) {
          message = new DsSipInviteMessage(isEncoded);
        } else {
          message = new DsSipInviteMessage();
        }
        break;
      case ACK:
        if (DsSipMessage.DEBUG) {
          System.out.println();
          System.out.println("<<<<<<<<<<<<<<<<< ACK >>>>>>>>>>>>>>>>");
          System.out.println();
        }
        if (isEncoded == true) {
          message = new DsSipAckMessage(isEncoded);
        } else {
          message = new DsSipAckMessage();
        }
        break;
      case CANCEL:
        if (DsSipMessage.DEBUG) {
          System.out.println();
          System.out.println("<<<<<<<<<<<<<<<<< CANCEL >>>>>>>>>>>>>>>>");
          System.out.println();
        }
        if (isEncoded == true) {
          message = new DsSipCancelMessage(isEncoded);
        } else {
          message = new DsSipCancelMessage();
        }
        break;
      case BYE:
        if (DsSipMessage.DEBUG) {
          System.out.println();
          System.out.println("<<<<<<<<<<<<<<<<< BYE >>>>>>>>>>>>>>>>");
          System.out.println();
        }
        if (isEncoded == true) {
          message = new DsSipByeMessage(isEncoded);
        } else {
          message = new DsSipByeMessage();
        }
        break;
      case OPTIONS:
        if (DsSipMessage.DEBUG) {
          System.out.println();
          System.out.println("<<<<<<<<<<<<<<<<< OPTIONS >>>>>>>>>>>>>>>>");
          System.out.println();
        }
        if (isEncoded == true) {
          message = new DsSipOptionsMessage(isEncoded);
        } else {
          message = new DsSipOptionsMessage();
        }
        break;
      case REGISTER:
        if (DsSipMessage.DEBUG) {
          System.out.println();
          System.out.println("<<<<<<<<<<<<<<<<< REGISTER >>>>>>>>>>>>>>>>");
          System.out.println();
        }
        if (isEncoded == true) {
          message = new DsSipRegisterMessage(isEncoded);
        } else {
          message = new DsSipRegisterMessage();
        }
        break;
      case PRACK:
        if (DsSipMessage.DEBUG) {
          System.out.println();
          System.out.println("<<<<<<<<<<<<<<<<< PRACK >>>>>>>>>>>>>>>>");
          System.out.println();
        }
        if (isEncoded == true) {
          message = new DsSipPRACKMessage(isEncoded);
        } else {
          message = new DsSipPRACKMessage();
        }
        break;
      case INFO:
        if (DsSipMessage.DEBUG) {
          System.out.println();
          System.out.println("<<<<<<<<<<<<<<<<< INFO >>>>>>>>>>>>>>>>");
          System.out.println();
        }
        if (isEncoded == true) {
          message = new DsSipInfoMessage(isEncoded);
        } else {
          message = new DsSipInfoMessage();
        }
        break;
      case SUBSCRIBE:
        if (DsSipMessage.DEBUG) {
          System.out.println();
          System.out.println("<<<<<<<<<<<<<<<<< SUBSCRIBE >>>>>>>>>>>>>>>>");
          System.out.println();
        }
        if (isEncoded == true) {
          message = new DsSipSubscribeMessage(isEncoded);
        } else {
          message = new DsSipSubscribeMessage();
        }
        break;
      case NOTIFY:
        if (DsSipMessage.DEBUG) {
          System.out.println();
          System.out.println("<<<<<<<<<<<<<<<<< NOTIFY >>>>>>>>>>>>>>>>");
          System.out.println();
        }
        if (isEncoded == true) {
          message = new DsSipNotifyMessage(isEncoded);
        } else {
          message = new DsSipNotifyMessage();
        }
        break;
      case MESSAGE:
        if (DsSipMessage.DEBUG) {
          System.out.println();
          System.out.println("<<<<<<<<<<<<<<<<< MESSAGE >>>>>>>>>>>>>>>>");
          System.out.println();
        }
        if (isEncoded == true) {
          message = new DsSipMessageMessage(isEncoded);
        } else {
          message = new DsSipMessageMessage();
        }
        break;
        // CAFFEINE 2.0 - add to support refer
      case REFER:
        if (DsSipMessage.DEBUG) {
          System.out.println();
          System.out.println("<<<<<<<<<<<<<<<<< REFER >>>>>>>>>>>>>>>>");
          System.out.println();
        }
        // caffeine 3.0 - TODO: Token sip encoding is missing in DsSipReferMessage in DS, add back
        // when it is ready
        if (isEncoded != true) {
          message = new DsSipReferMessage();
        }
        break;
        // CAFFEINE 2.0 - add to support update
      case UPDATE:
        if (DsSipMessage.DEBUG) {
          System.out.println();
          System.out.println("<<<<<<<<<<<<<<<<< UPDATE >>>>>>>>>>>>>>>>");
          System.out.println();
        }
        // caffeine 3.0 - TODO: Token sip encoding is missing in DsSipUpdateMessage in DS, add back
        // when it is ready
        if (isEncoded != true) {
          message = new DsSipUpdateMessage();
        }
        break;
      case PUBLISH:
        if (DsSipMessage.DEBUG) {
          System.out.println();
          System.out.println("<<<<<<<<<<<<<<<<< PUBLISH >>>>>>>>>>>>>>>>");
          System.out.println();
        }
        if (isEncoded == true) {
          message = new DsSipPublishMessage(isEncoded);
        } else {
          message = new DsSipPublishMessage();
        }
        break;
      default:
        if (DsSipMessage.DEBUG) {
          System.out.println();
          System.out.println("<<<<<<<<<<<<<<<<< REQUEST >>>>>>>>>>>>>>>>");
          System.out.println();
        }
        if (isEncoded == true) {
          message = new DsSipRequest(buffer, offset, count, isEncoded);
        } else {
          message = new DsSipRequest(buffer, offset, count);
        }
        break;
    }
    return message;
  }

  /*
   * javadoc inherited.
   */
  public DsSipMessageListener responseBegin(
      byte[] buffer, int code, int offset, int count, boolean isEncoded)
      throws DsSipParserListenerException {
    if (DsSipMessage.DEBUG) {
      System.out.println(
          "responseBegin - reason = [" + DsByteString.newString(buffer, offset, count) + "]");
      System.out.println("responseBegin code = [" + code + "]");
    }
    if (isEncoded == true) {
      message = new DsSipResponse(buffer, code, offset, count, true);
    } else {
      message = new DsSipResponse(buffer, code, offset, count);
    }
    return message;
  }

  /**
   * Returns the message parsed and nullify the local reference. The immediate call to this method
   * will return null.
   *
   * @return the message just parsed or null if already returned the parsed message in the previous
   *     call to this method.
   */
  public DsSipMessage getMessage() {
    DsSipMessage msg = message;
    message = null;
    return msg;
  }
} // Ends class DsSipDefaultMessageListenerFactory
