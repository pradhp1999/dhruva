// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import org.apache.logging.log4j.Level;

/** This class represents the BYE message as specified in RFC 3261. */
public final class DsSipByeMessage extends DsSipRequest {
  /** The default constructor. */
  protected DsSipByeMessage() {
    super(BYE);
  }

  protected DsSipByeMessage(boolean encoded) {
    super(BYE, encoded);
  }

  /**
   * Constructs this BYE request with the specified <code>from</code> header, <code>to</code>
   * header, <code>contact</code> header, <code>callId</code>, <code>cSeqNo</code>, <code>bodyType
   * </code> and the specified <code>body</code>. The specified headers, <code>fromHeader</code>,
   * <code>toHeader</code> and <code>contactHeader</code> will be cloned if the specified option
   * <code>clone</code> is <code>true</code>.
   *
   * @param from the from header object.
   * @param to the to header object.
   * @param contact the contact header object.
   * @param callId the call id.
   * @param cSeqNo the sequence number.
   */
  public DsSipByeMessage(
      DsSipFromHeader from,
      DsSipToHeader to,
      DsSipContactHeader contact,
      DsByteString callId,
      long cSeqNo) {
    super(BYE, from, to, contact, callId, cSeqNo, null, null, true);
  }

  /**
   * Constructs this BYE request with the specified <code>from</code> header, <code>to</code>
   * header, <code>contact</code> header, <code>callId</code>, <code>cSeqNo</code>, <code>bodyType
   * </code> and the specified <code>body</code>. The specified headers, <code>fromHeader</code>,
   * <code>toHeader</code> and <code>contactHeader</code> will be cloned if the specified option
   * <code>clone</code> is <code>true</code>.
   *
   * @param from the from header object.
   * @param to the to header object.
   * @param contact the contact header object.
   * @param callId the call id.
   * @param cSeqNo the sequence number.
   * @param bodyType the body type.
   * @param body the body data.
   * @param clone if true, it will clone the headers, else just use the references.
   */
  public DsSipByeMessage(
      DsSipFromHeader from,
      DsSipToHeader to,
      DsSipContactHeader contact,
      DsByteString callId,
      long cSeqNo,
      DsByteString bodyType,
      byte[] body,
      boolean clone) {
    super(BYE, from, to, contact, callId, cSeqNo, bodyType, body, clone);
  }

  /**
   * Constructs this BYE message from the specified INVITE request <code>invite</code>. If the
   * specified option <code>clone</code> is <code>true</code> then the headers from the specified
   * INVITE message will be cloned before adding into this new BYE message, otherwise the headers
   * will get removed from the INVITE message and get added to this BYE message. As an header can
   * not be part of two messages simultaneously.
   *
   * @param invite an INVITE message
   * @param clone pass <code>true</code> to clone headers from invite
   * @throws IllegalArgumentException if the passed in invite message is null.
   */
  public DsSipByeMessage(DsSipInviteMessage invite, boolean clone) {
    super(BYE);
    if (invite == null) {
      throw new IllegalArgumentException("Can not create a bye message from a null Invite Message");
    }
    DsSipHeaderInterface header = invite.getHeader(FROM);
    if (header != null) {
      updateHeader(new DsSipHeaderString(TO, header.getValue()));
    }
    header = invite.getHeader(TO);
    if (header != null) {
      updateHeader(new DsSipHeaderString(FROM, header.getValue()));
    }

    try {
      // optimization - don't parse the Contact if we don't have to
      DsSipHeaderInterface cHeader = invite.getHeader(CONTACT);
      setURI(cHeader != null ? createURI(cHeader, true) : createURI(header, false));
    } catch (Exception exc) {
      if (DsLog4j.messageCat.isEnabled(Level.WARN)) {
        DsLog4j.messageCat.warn(
            "Couldn't create the Request URI either from the Contact Header or From header.", exc);
      }
    }

    // Call-Id Header
    setCallId(invite.getCallId());
    // addSessionIdHeader
    addSessionIDHeader();
    // CSeq header
    setCSeqNumber(invite.getCSeqNumber() + 1);
    setCSeqMethod(BS_BYE);
  }

  /**
   * Constructs this BYE message from the specified SIP Response <code>response</code>. It clones
   * the To, From and contact headers in the specified response before adding them in this BYE
   * message.
   *
   * @param response the SIP response message
   * @throws IllegalArgumentException if the passed in response is null.
   */
  public DsSipByeMessage(DsSipResponse response) {
    super(BYE);
    if (response == null) {
      throw new IllegalArgumentException("Can not create a bye message from a null Response");
    }

    // From Header
    updateHeader(response.getHeader(FROM));

    // To Header
    DsSipHeaderInterface to = (DsSipHeaderInterface) response.getHeader(TO).clone();
    updateHeader(to, false);

    try {
      // Contact Header
      DsSipHeaderInterface contact = (DsSipHeaderInterface) response.getHeader(CONTACT);
      setURI((null != contact) ? createURI(contact, true) : createURI(to, false));
    } catch (Exception exc) {
      //  exc.printStackTrace();
    }
    // Call-Id Header
    setCallId(response.getCallId());
    // CSeq Header
    setCSeqNumber(response.getCSeqNumber() + 1);
    setCSeqMethod(BS_BYE);
  }

  /*
      public final static void main(String args[]) throws Exception
      {

          byte[] F5 =    ("INVITE sip:UserB@there.com SIP/2.0\r\n" +
                          "Via: SIP/2.0/UDP ss1.wcom.com:5060;branch=2d4790.1\r\n" +
                          "Via: SIP/2.0/UDP here.com:5060\r\n" +
                          "Record-Route: <sip:UserB@there.com;maddr=ss1.wcom.com>\r\n" +
                          "From: BigGuy <sip:UserA@here.com>\r\n" +
                          "To: LittleGuy <sip:UserB@there.com>\r\n" +
                          "Call-ID: 12345601@here.com\r\n" +
                          "CSeq: 1 INVITE\r\n" +
                          "Contact: Foo <sip:UserA@100.101.102.103>\r\n" +
                          "Content-Type: application/sdp\r\n" +
                          "Content-Length: 147\r\n" +
                          "\r\n" +
                          "v=0\r\n" +
                          "o=UserA 2890844526 2890844526 IN IP4 here.com\r\n" +
                          "s=Session SDP\r\n" +
                          "c=IN IP4 100.101.102.103\r\n" +
                          "t=0 0\r\n" +
                          "m=audio 49172 RTP/AVP 0\r\n" +
                          "a=rtpmap:0 PCMU/8000\r\n").getBytes();

          DsSipMessage F5msg = DsSipMessage.createMessage(F5, true, true);

          DsSipByeMessage bye = new DsSipByeMessage((DsSipInviteMessage)F5msg, false);
          System.out.println("Invite = [" + DsByteString.newString(F5) + "]");
          System.out.println("Bye = [" +bye + "]");

      }
  */
}
