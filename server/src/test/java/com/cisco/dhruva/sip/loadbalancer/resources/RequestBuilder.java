package com.cisco.dhruva.sip.loadbalancer.resources;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;

public class RequestBuilder {

  public static DsSipRequest getRequestWithMParam(String msid, String mParam) {

    byte[] MSG_I =
        ("INVITE sip:12345.mtbt1mgb002@lyncats.webex.com;dir=Outbound;cic=000609;x-cisco-svc-type=cmr;user=phone;transport=tls SIP/2.0\r\n"
                + "Via: SIP/2.0/TLS 10.252.57.25:5061;rport;branch=z9hG4bK2076213269\r\n"
                + "From: <sip:+14006140207224564@10.252.57.25:5061;user=phone>;tag=2021725908\r\n"
                + "To: <sip:12345.mtbt1mgb002@lyncats.webex.com:5061;user=phone>\r\n"
                + "Call-ID: 17033149367\r\n"
                + "CSeq: 20 INVITE\r\n"
                + "Contact: <sip:+14006140207224564@10.252.57.25:53924;transport=TLS;user=phone>\r\n"
                + "Content-Type: application/sdp\r\n"
                + "Max-Forwards: 70\r\n"
                + "User-Agent: eXosip/4.0.0\r\n"
                + "Subject: This is an UCRE Huron OutBound test call. ID:   061402_07224564\r\n"
                + "Supported: timer\r\n"
                + "Session-Expires: 1800\r\n"
                + "Min-SE: 1800\r\n"
                + "Reason: MCT Test.ID:   061402_07224564\r\n"
                + "Ms-Conversation-ID: "
                + msid
                + "\r\n"
                + "P-Charging-Vector: icid-value=1528940592285;icid-generated-at=10.252.57.25;orig-ioi=mct.webex.com\r\n"
                + "Content-Length:   544\r\n")
            .getBytes();

    DsByteString body =
        new DsByteString(
            "v=0\r\n"
                + "o=MCTSipCaller 62931 62933 IN IP4 10.252.57.25\r\n"
                + "s=SIP Call\r\n"
                + "c=IN IP4 10.252.57.25\r\n"
                + "t=0 0\r\n"
                + "m="
                + mParam
                + " 50260 RTP/SAVP 0 8 101 19\r\n"
                + "a=rtpmap:0 PCMU/8000\r\n"
                + "a=rtpmap:8 PCMA/8000\r\n"
                + "a=rtpmap:101 telephone-event/8000\r\n"
                + "a=fmtp:101 0-15\r\n"
                + "a=rtpmap:19 CN/8000\r\n"
                + "a=ptime:20\r\n"
                + "a=crypto:1 AES_CM_128_HMAC_SHA1_80 inline:********MASKED***************\r\n"
                + "a=crypto:2 AES_CM_128_HMAC_SHA1_80 inline:********MASKED***************\r\n"
                + "a=crypto:3 AES_CM_128_HMAC_SHA1_32 inline:********MASKED***************\r\n");

    DsByteString reqString = new DsByteString(MSG_I);
    String reqStr = reqString.toString();
    DsSipRequest request = null;
    try {
      request = (DsSipRequest) DsSipMessage.createMessage(reqStr.getBytes());
      request.setBody(new DsByteString(body), null);
    } catch (DsSipParserListenerException | DsSipParserException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    return request;
  }
}
