package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.DsSipParserException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DsSipMessageFactoryTest {

  DsSipMessageFactory mf = DsSipDefaultMessageFactory.getInstance();

  @Test(
      dataProvider = "dataProviders",
      enabled = true,
      description = "Test parsing of raw sip message")
  public void testSipMessage(byte[] buf) {
    try {
      DsSipMessage.setParseAllHeaders(true);
      DsSipMessage msg = mf.createMessage(buf, 0, buf.length, true, true);
    } catch (Exception e) {
      if (e instanceof DsSipParserException) {
        Assert.fail("Exception thrown in parser" + e.getCause().getMessage(), e.getCause());
      }
      Assert.fail("Exception thrown in parse" + e.getMessage(), e);
      e.printStackTrace();
    }
  }

  @DataProvider(name = "dataProviders")
  public Object[][] dataProvider() {
    return new Object[][] {
      {
        ("INVITE sip:1234567@alpha.webex.com:5060 SIP/2.0\n"
                + "Via: SIP/2.0/TLS 10.78.98.22:5060;branch=z9hG4bK22OUz2jVIUJvqqCzTbFXXA~~13\n"
                + "Via: SIP/2.0/TLS 10.78.98.22:7777;branch=z9hG4bK-25058-1-0\n"
                + "Max-Forwards: 69\n"
                + "Route: <sip:10.78.98.22:5081;transport=tls;lr>\n"
                + "Record-Route: <sip:rr$n=sip1@10.78.98.22:5060;transport=tls;lr>\n"
                + "To: service <sip:service@10.78.98.22:5060>\n"
                + "From: sipp <sip:sipp@10.78.98.22:7777>;tag=25058SIPpTag001\n"
                + "Contact: sip:sipp@10.78.98.22:7777\n"
                + "Call-ID: 1-25058@10.78.98.22\n"
                + "CSeq: 1 INVITE\n"
                + "Subject: Performance Test\n"
                + "Content-Type: application/sdp\n"
                + "Content-Length: 50\n"
                + "Session-ID: 9a1e37dd952331e29a1a4c6c940528fd;remote=00000000000000000000000000000000\n"
                + "\n"
                + "v=0\n"
                + "o=user1 53655765 2353687637 IN IP4 10.78.98.22\n"
                + "s=-\n"
                + "c=IN IP4 10.78.98.22\n"
                + "t=0 0\n"
                + "m=audio 6001 RTP/AVP 0\n"
                + "a=rtpmap:0 PCMU/8000\n"
                + "")
            .getBytes()
      },
      {
        ("ACK sip:1234567@alpha.webex.com:5060 SIP/2.0\n"
                + "Via: SIP/2.0/TLS 10.78.98.22:5060;branch=z9hG4bK22OUz2jVIUJvqqCzTbFXXA~~13\n"
                + "Via: SIP/2.0/TLS 10.78.98.22:7777;branch=z9hG4bK-25058-1-0\n"
                + "Max-Forcd wards: 69\n"
                + "Route: <sip:10.78.98.22:5081;transport=tls;lr>\n"
                + "Record-Route: <sip:rr$n=sip1@10.78.98.22:5060;transport=tls;lr>\n"
                + "To: service <sip:service@10.78.98.22:5060>\n"
                + "From: sipp <sip:sipp@10.78.98.22:7777>;tag=25058SIPpTag001\n"
                + "Contact: sip:sipp@10.78.98.22:7777\n"
                + "Call-ID: 1-25058@10.78.98.22\n"
                + "CSeq: 1 ACK\n"
                + "Subject: Performance Test\n"
                + "Content-Type: application/sdp\n"
                + "Content-Length: 50\n"
                + "Session-ID: 9a1e37dd952331e29a1a4c6c940528fd;remote=00000000000000000000000000000000\n"
                + "\n"
                + "v=0\n"
                + "o=user1 53655765 2353687637 IN IP4 10.78.98.22\n"
                + "s=-\n"
                + "c=IN IP4 10.78.98.22\n"
                + "t=0 0\n"
                + "m=audio 6001 RTP/AVP 0\n"
                + "a=rtpmap:0 PCMU/8000\n"
                + "")
            .getBytes()
      },
      {
        ("REFER sip:1234567@alpha.webex.com:5060 SIP/2.0\n"
                + "Via: SIP/2.0/TLS 10.78.98.22:5060;branch=z9hG4bK22OUz2jVIUJvqqCzTbFXXA~~13\n"
                + "Via: SIP/2.0/TLS 10.78.98.22:7777;branch=z9hG4bK-25058-1-0\n"
                + "Max-Forwards: 69\n"
                + "Route: <sip:10.78.98.22:5081;transport=tls;lr>\n"
                + "Record-Route: <sip:rr$n=sip1@10.78.98.22:5060;transport=tls;lr>\n"
                + "To: service <sip:service@10.78.98.22:5060>\n"
                + "From: sipp <sip:sipp@10.78.98.22:7777>;tag=25058SIPpTag001\n"
                + "Contact: sip:sipp@10.78.98.22:7777\n"
                + "Call-ID: 1-25058@10.78.98.22\n"
                + "CSeq: 1 REFER\n"
                + "Subject: Performance Test\n"
                + "Content-Type: application/sdp\n"
                + "Content-Length: 50\n"
                + "Session-ID: 9a1e37dd952331e29a1a4c6c940528fd;remote=00000000000000000000000000000000\n"
                + "\n"
                + "v=0\n"
                + "o=user1 53655765 2353687637 IN IP4 10.78.98.22\n"
                + "s=-\n"
                + "c=IN IP4 10.78.98.22\n"
                + "t=0 0\n"
                + "m=audio 6001 RTP/AVP 0\n"
                + "a=rtpmap:0 PCMU/8000\n"
                + "")
            .getBytes()
      }
    };
  }
}
