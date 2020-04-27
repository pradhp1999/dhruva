package com.cisco.dhruva.util;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.Transport;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

public class SIPRequestBuilder {

  public static final String CRLF = "\r\n";

  private String toTag = null;

  private String to = "To: LittleGuy <sip:UserB@there.com>";

  public void setToTag(String toTag) {
    this.toTag = toTag;
  }

  public enum RequestMethod {
    INVITE,
    ACK,
    OPTIONS,
    BYE,
    CANCEL
  };

  /* Response code and their message */
  public static HashMap<Integer, String> ResponseCodeValue =
      new HashMap<Integer, String>() {
        {
          put(200, "OK");
          put(202, "ACCEPTED");
          put(300, "MULTIPLE CHOICES");
          put(301, "MOVED PERMANENTLY");
          put(302, "MOVED TEMPORARILY");
          put(305, "USE PROXY");
          put(380, "ALTERNATIVE SERVICE");
          put(400, "BAD REQUEST");
          put(401, "UNAUTHORIZED");
          put(403, "FORBIDDEN");
          put(404, "NOT FOUND");
          put(500, "SERVER INTERNAL ERROR");
          put(502, "BAD GATEWAY");
          put(504, "GATEWAY TIMEOUT");
          put(600, "BUSY EVERYWHERE");
          put(603, "DECLINE");
        }
      };

  public static DsSipRequest createRequest(String request)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipRequest msg = null;
    msg = (DsSipRequest) DsSipMessage.createMessage(request.getBytes());
    return msg;
  }

  public String getRequestAsString(RequestMethod method, String... optionalRequestURIValueList) {
    String optionalRequestURIValue = "UserB@there.com";
    if (optionalRequestURIValueList != null && optionalRequestURIValueList.length != 0) {
      optionalRequestURIValue = optionalRequestURIValueList[0];
    }

    if (toTag != null) {
      to += ";" + toTag;
    }

    String requestString =
        method.name()
            + " sip:"
            + optionalRequestURIValue
            + " SIP/2.0"
            + CRLF
            + "Via: SIP/2.0/UDP ss1.wcom.com:5060;branch=2d4790.1"
            + CRLF
            + "Via: SIP/2.0/UDP here.com:5060"
            + CRLF
            + "Max-Forwards: 70"
            + CRLF
            + "Route: <sip:UserE@xxx.yyy.com;maddr=ss1.wcom.com>"
            + CRLF
            + "Route: <sip:TinkyWinky@tellytubbyland.com;maddr=ss1.wcom.com>"
            + CRLF
            + to
            + CRLF
            + "From: BigGuy <sip:UserA@here.com>"
            + CRLF
            + "Call-ID: 12345601@here.com"
            + CRLF
            + "CSeq: 1 "
            + method.name()
            + CRLF
            + "Content-Length: 0"
            + CRLF
            + "Contact: <sip:UserA@100.101.102.103>"
            + CRLF
            + "Content-Type: application/sdp"
            + CRLF;

    return requestString;
  }

  public String getRequestAsString(RequestMethod method, boolean random) {

    if (toTag != null) {
      to += ";" + toTag;
    }
    if (random == false) {
      return getRequestAsString(method);
    }
    String randomString = randomAlphaNumeric(20);
    String callId = randomString;
    String requestUri = " sip:" + randomString + "@cisco.com SIP/2.0";
    String toUri = "To: <sip:" + randomString + "@cisco.com>";
    String reuestString =
        method.name()
            + requestUri
            + CRLF
            + "Via: SIP/2.0/UDP ss1.wcom.com:5060;branch=2d4790.1"
            + CRLF
            + "Via: SIP/2.0/UDP here.com:5060"
            + CRLF
            + "Max-Forwards: 70"
            + CRLF
            + "Route: <sip:UserE@xxx.yyy.com;maddr=ss1.wcom.com>"
            + CRLF
            + "Route: <sip:TinkyWinky@tellytubbyland.com;maddr=ss1.wcom.com>"
            + CRLF
            + toUri
            + CRLF
            + "From: BigGuy <sip:UserA@here.com>"
            + CRLF
            + "Call-ID: "
            + callId
            + CRLF
            + "CSeq: 1 "
            + method.name()
            + CRLF
            + "Content-Length: 0"
            + CRLF
            + "Contact: <sip:UserA@100.101.102.103>"
            + CRLF
            + "Content-Type: application/sdp"
            + CRLF;

    return reuestString;
  }

  public String getRequestAsString(RequestMethod method, int maxForwardValue) {

    if (toTag != null) {
      to += ";" + toTag;
    }
    String randomString = randomAlphaNumeric(20);
    String callId = randomString;
    String requestUri = " sip:" + randomString + "@cisco.com SIP/2.0";
    String toUri = "To: <sip:" + randomString + "@cisco.com>";
    String reuestString =
        method.name()
            + requestUri
            + CRLF
            + "Via: SIP/2.0/UDP ss1.wcom.com:5060;branch=2d4790.1"
            + CRLF
            + "Via: SIP/2.0/UDP here.com:5060"
            + CRLF
            + "Max-Forwards:"
            + maxForwardValue
            + CRLF
            + "Route: <sip:UserE@xxx.yyy.com;maddr=ss1.wcom.com>"
            + CRLF
            + "Route: <sip:TinkyWinky@tellytubbyland.com;maddr=ss1.wcom.com>"
            + CRLF
            + toUri
            + CRLF
            + "From: BigGuy <sip:UserA@here.com>"
            + CRLF
            + "Call-ID: "
            + callId
            + CRLF
            + "CSeq: 1 "
            + method.name()
            + CRLF
            + "Content-Length: 0"
            + CRLF
            + "Contact: <sip:UserA@100.101.102.103>"
            + CRLF
            + "Content-Type: application/sdp"
            + CRLF;

    return reuestString;
  }

  public void setBindingInfo(DsSipRequest request, Transport transport)
      throws UnknownHostException {
    DsBindingInfo bindingInfo;
    InetAddress localAddr = null;
    InetAddress remoteAddr = null;
    byte[] localIpAddr = new byte[] {127, 0, 0, 1};
    byte[] remoteIpAddr = new byte[] {127, 0, 0, 1};
    try {
      localAddr = InetAddress.getByAddress(localIpAddr);
      remoteAddr = InetAddress.getByAddress(remoteIpAddr);
    } catch (UnknownHostException e) {
      throw e;
    }
    bindingInfo = new DsBindingInfo();
    bindingInfo.setRemoteAddress(remoteAddr);
    bindingInfo.setRemotePort(5060);
    bindingInfo.setLocalAddress(localAddr);
    bindingInfo.setLocalPort(5060);
    bindingInfo.setNetwork(DsNetwork.getDefault());
    bindingInfo.setTransport(transport);
    request.setBindingInfo(bindingInfo);
  }

  public static String randomAlphaNumeric(int count) {
    StringBuilder builder = new StringBuilder();
    final String ALPHA_NUMERIC_STRING =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmopqrstuvwxyz0123456789";
    while (count-- != 0) {
      int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
      builder.append(character);
    }
    return builder.toString();
  }

  public static DsSipRequest getHybridCascadeRequest() throws Exception {
    String sipMessage =
        "INVITE sip:73sVgblHnSQtmLz08VXs8dag@192.168.65.141:5060;call-type=hybrid-cascade;x-cisco-svc-type=spark-mm SIP/2.0\n"
            + "Via: SIP/2.0/TCP 192.168.65.141:7002;branch=z9hG4bK-4955-1-0\n"
            + "Max-Forwards: 69\n"
            + "To: sut <sip:73sVgblHnSQtmLz08VXs8dag@192.168.65.141:5060>\n"
            + "From: 123 <sip:123@192.168.65.141:7002>;tag=4955SIPpTag001\n"
            + "Contact: sip:123@192.168.65.141:7002;ifocus\n"
            + "Call-ID: 1-4955@192.168.65.141\n"
            + "CSeq: 1 INVITE\n"
            + "Content-Length: 0\n"
            + "Subject: Performance Test\n"
            + "Allow: UPDATE\n"
            + "Supported: timer,resource-priority,replaces\n"
            + "Content-Type: application/sdp\n"
            + "Session-ID: 6d4da3adee523660ab1654a7ba94cf83;remote=00000000000000000000000000000000\n";
    return (DsSipRequest) DsSipMessage.createMessage(sipMessage.getBytes(), true, false);
  }

  public static DsSipRequest getReInviteRequest(String optionalRequestURIValue) throws Exception {
    if (optionalRequestURIValue == null || optionalRequestURIValue.equals("")) {
      optionalRequestURIValue =
          "73sVgblHnSQtmLz08VXs8dag@192.168.65.141:5060;call-type=hybrid-cascade;x-cisco-svc-type=spark-mm";
    }

    String sipMessage =
        "INVITE sip:"
            + optionalRequestURIValue
            + " SIP/2.0\n"
            + "Via: SIP/2.0/TCP 192.168.65.141:7002;branch=z9hG4bK-4955-1-0\n"
            + "Max-Forwards: 69\n"
            + "To: sut <sip:73sVgblHnSQtmLz08VXs8dag@192.168.65.141:5060>;tag=8079SIPpTag011\n"
            + "From: 123 <sip:123@192.168.65.141:7002>;tag=4955SIPpTag001\n"
            + "Contact: sip:123@192.168.65.141:7002;ifocus\n"
            + "Call-ID: 1-4955@192.168.65.141\n"
            + "CSeq: 1 INVITE\n"
            + "Content-Length: 0\n"
            + "Subject: Performance Test\n"
            + "Allow: UPDATE\n"
            + "Supported: timer,resource-priority,replaces\n"
            + "Content-Type: application/sdp\n"
            + "Session-ID: 6d4da3adee523660ab1654a7ba94cf83;remote=00000000000000000000000000000000\n";
    return (DsSipRequest) DsSipMessage.createMessage(sipMessage.getBytes(), true, false);
  }

  public static DsSipResponse get200Response()
      throws DsSipParserListenerException, DsSipParserException {
    String sipMessage =
        "SIP/2.0 200 OK\n"
            + "Via: SIP/2.0/TCP 192.168.65.141:5066;branch=z9hG4bKUsaQangfWbsEVmsoPdTNBA~~0\n"
            + "Via: SIP/2.0/TCP 192.168.65.141:7002;branch=z9hG4bK-8090-1-0\n"
            + "Record-Route: <sip:192.168.65.141:5080;transport=tcp;lr;call-type=sip>\n"
            + "Record-Route: <sip:rr,n=net_sp_@192.168.65.141:5066;transport=tcp;lr;x-cisco-call-type=hybrid-cascade>\n"
            + "To: sut <sip:73sVgblHnSQtmLz08VXs8dag@192.168.65.141:5060>;tag=8079SIPpTag011\n"
            + "From: 123 <sip:123@192.168.65.141:7002>;tag=8090SIPpTag001\n"
            + "Contact: <sip:service@192.168.65.141:5080;transport=TCP>;sip.cisco.multistream;\n"
            + "Call-ID: 1-8090@192.168.65.141\n"
            + "CSeq: 1 INVITE\n"
            + "Content-Length: 0\n"
            + "Allow: UPDATE\n"
            + "Content-Type: application/sdp\n"
            + "Session-ID: bc6d01a4a51c3886a6bf32cd9155dd56;remote=50aa12ba7817323b8342191bcd537513\n";
    return (DsSipResponse) DsSipMessage.createMessage(sipMessage.getBytes());
  }

  /* This method returns a response for the responseCode passed to it */
  public static DsSipResponse getResponse(int code)
      throws DsSipParserListenerException, DsSipParserException {
    String result = "200 OK";
    if (ResponseCodeValue.containsKey(code)) {
      result = code + " " + ResponseCodeValue.get(code);
    }

    String sipMessage =
        "SIP/2.0 "
            + result
            + "\n"
            + "Via: SIP/2.0/TCP 192.168.65.141:5066;branch=z9hG4bKUsaQangfWbsEVmsoPdTNBA~~0\n"
            + "Via: SIP/2.0/TCP 192.168.65.141:7002;branch=z9hG4bK-8090-1-0\n"
            + "Record-Route: <sip:192.168.65.141:5080;transport=tcp;lr;call-type=sip>\n"
            + "Record-Route: <sip:rr,n=net_sp_@192.168.65.141:5066;transport=tcp;lr;x-cisco-call-type=hybrid-cascade>\n"
            + "To: sut <sip:73sVgblHnSQtmLz08VXs8dag@192.168.65.141:5060>;tag=8079SIPpTag011\n"
            + "From: 123 <sip:123@192.168.65.141:7002>;tag=8090SIPpTag001\n"
            + "Contact: <sip:service@192.168.65.141:5080;transport=TCP>;sip.cisco.multistream;\n"
            + "Call-ID: 1-8090@192.168.65.141\n"
            + "CSeq: 1 INVITE\n"
            + "Content-Length: 0\n"
            + "Allow: UPDATE\n"
            + "Content-Type: application/sdp\n"
            + "Session-ID: bc6d01a4a51c3886a6bf32cd9155dd56;remote=50aa12ba7817323b8342191bcd537513\n";
    return (DsSipResponse) DsSipMessage.createMessage(sipMessage.getBytes());
  }
}
